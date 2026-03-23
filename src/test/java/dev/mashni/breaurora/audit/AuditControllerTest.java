package dev.mashni.breaurora.audit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;


import dev.mashni.breaurora.rule.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@WebMvcTest(AuditController.class)
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuditService auditService;

    private AuditLog auditLog;
    private Rule rule;

    @BeforeEach
    void setUp() {
        rule = Rule.builder()
                .id(UUID.randomUUID())
                .name("Regra Teste")
                .condition("['valor'] > 1000")
                .actionType("ALERT")
                .priority(1)
                .active(true)
                .build();

        auditLog = AuditLog.builder()
                .id(UUID.randomUUID())
                .rule(rule)
                .ruleName("Regra Teste")
                .eventData("{\"valor\":1500}")
                .matched(true)
                .actionTaken("ALERT")
                .build();
    }

    // GET /audit
    @Test
    void deveRetornarTodosOsLogs() throws Exception {
        when(auditService.findAll()).thenReturn(List.of(auditLog));

        mockMvc.perform(get("/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].ruleName").value("Regra Teste"))
                .andExpect(jsonPath("$[0].matched").value(true))
                .andExpect(jsonPath("$[0].actionTaken").value("ALERT"));
    }

    // GET /audit/matched
    @Test
    void deveRetornarApenasLogsQueDispararam() throws Exception {
        when(auditService.findMatched()).thenReturn(List.of(auditLog));

        mockMvc.perform(get("/audit/matched"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].matched").value(true));
    }

    // GET /audit/rule/{ruleId}
    @Test
    void deveRetornarLogsPorRegra() throws Exception {
        when(auditService.findByRule(rule.getId())).thenReturn(List.of(auditLog));

        mockMvc.perform(get("/audit/rule/{ruleId}", rule.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].ruleName").value("Regra Teste"));
    }

    // GET /audit — lista vazia
    @Test
    void deveRetornarListaVaziaQuandoNaoHouverLogs() throws Exception {
        when(auditService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // GET /audit/matched — nenhum log com match
    @Test
    void deveRetornarListaVaziaQuandoNaoHouverLogsMatched() throws Exception {
        when(auditService.findMatched()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/audit/matched"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}