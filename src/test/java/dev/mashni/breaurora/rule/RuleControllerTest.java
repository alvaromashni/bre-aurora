package dev.mashni.breaurora.rule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.persistence.EntityNotFoundException;

@WebMvcTest(RuleController.class)
class RuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RuleService ruleService;

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
    }

    // POST /rules
    @Test
    void deveCriarRegra() throws Exception {
        when(ruleService.create(any(Rule.class))).thenReturn(rule);

        mockMvc.perform(post("/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rule)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Regra Teste"))
                .andExpect(jsonPath("$.actionType").value("ALERT"));
    }

    // GET /rules
    @Test
    void deveRetornarTodasAsRegras() throws Exception {
        when(ruleService.findAll()).thenReturn(List.of(rule));

        mockMvc.perform(get("/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Regra Teste"));
    }

    // GET /rules/{id}
    @Test
    void deveRetornarRegraPorId() throws Exception {
        when(ruleService.findById(rule.getId())).thenReturn(rule);

        mockMvc.perform(get("/rules/{id}", rule.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Regra Teste"));
    }

    // GET /rules/{id} — não encontrado
    @Test
    void deveRetornar404QuandoRegraNaoEncontrada() throws Exception {
        when(ruleService.findById(any(UUID.class)))
                .thenThrow(new EntityNotFoundException("Regra não encontrada"));

        mockMvc.perform(get("/rules/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    // PUT /rules/{id}
    @Test
    void deveAtualizarRegra() throws Exception {
        Rule atualizada = Rule.builder()
                .id(rule.getId())
                .name("Regra Atualizada")
                .condition("['valor'] > 2000")
                .actionType("BLOCK")
                .priority(0)
                .active(true)
                .build();

        when(ruleService.update(eq(rule.getId()), any(Rule.class))).thenReturn(atualizada);

        mockMvc.perform(put("/rules/{id}", rule.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Regra Atualizada"))
                .andExpect(jsonPath("$.actionType").value("BLOCK"));
    }

    // DELETE /rules/{id}
    @Test
    void deveDeletarRegra() throws Exception {
        doNothing().when(ruleService).delete(rule.getId());

        mockMvc.perform(delete("/rules/{id}", rule.getId()))
                .andExpect(status().isNoContent());
    }
}
