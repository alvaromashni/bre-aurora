package dev.mashni.breaurora.event;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import dev.mashni.breaurora.engine.RuleEngine;
import dev.mashni.breaurora.engine.dto.EvaluationResult;
import dev.mashni.breaurora.engine.dto.MatchedRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RuleEngine ruleEngine;

    private Map<String, Object> eventData;

    @BeforeEach
    void setUp() {
        eventData = Map.of("valor", 1500, "pais", "US");
    }

    // evento avaliado — nenhuma regra disparou
    @Test
    void deveRetornar200QuandoNenhumaRegraDisparar() throws Exception {
        EvaluationResult result = new EvaluationResult(3, Collections.emptyList());
        when(ruleEngine.evaluate(any())).thenReturn(result);

        mockMvc.perform(post("/events/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRulesEvaluated").value(3))
                .andExpect(jsonPath("$.matchedRuleList").isArray())
                .andExpect(jsonPath("$.matchedRuleList").isEmpty());
    }

    // evento avaliado — regra ALERT disparou
    @Test
    void deveRetornar200ComRegraDisparada() throws Exception {
        MatchedRule matched = new MatchedRule(UUID.randomUUID(), "Fraude Internacional", "ALERT");
        EvaluationResult result = new EvaluationResult(2, List.of(matched));
        when(ruleEngine.evaluate(any())).thenReturn(result);

        mockMvc.perform(post("/events/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matchedRuleList[0].ruleName").value("Fraude Internacional"))
                .andExpect(jsonPath("$.matchedRuleList[0].actionTaken").value("ALERT"));
    }

    // evento bloqueado — regra BLOCK disparou
    @Test
    void deveRetornar403QuandoRegraBlockDisparar() throws Exception {
        MatchedRule matched = new MatchedRule(UUID.randomUUID(), "Bloqueio Suspeito", "BLOCK");
        EvaluationResult result = new EvaluationResult(1, List.of(matched));
        when(ruleEngine.evaluate(any())).thenReturn(result);

        mockMvc.perform(post("/events/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventData)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.matchedRuleList[0].actionTaken").value("BLOCK"));
    }

    // body vazio — ainda deve funcionar
    @Test
    void deveAvaliarEventoVazio() throws Exception {
        EvaluationResult result = new EvaluationResult(0, Collections.emptyList());
        when(ruleEngine.evaluate(any())).thenReturn(result);

        mockMvc.perform(post("/events/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRulesEvaluated").value(0));
    }
}
