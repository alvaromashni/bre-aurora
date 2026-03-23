package dev.mashni.breaurora.engine;

import dev.mashni.breaurora.action.ActionExecutorRegistry;
import dev.mashni.breaurora.audit.AuditService;
import dev.mashni.breaurora.engine.dto.EvaluationResult;
import dev.mashni.breaurora.rule.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleEngineTest {

    @Mock
    private RuleEvaluator ruleEvaluator;

    @Mock
    private ActionExecutorRegistry actionRegistry;

    @Mock
    private AuditService auditService;

    @Mock
    private RuleCache ruleCache;

    @InjectMocks
    private RuleEngine ruleEngine;

    private Rule rule;
    private Map<String, Object> eventData;

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

        eventData = Map.of("valor", 1500, "pais", "US");
    }

    // regra dispara — matched, ação executada, auditoria salva
    @Test
    void deveRetornarRegraDisparadaQuandoCondicaoForSatisfeita() {
        when(ruleCache.getActiveRules()).thenReturn(List.of(rule));
        when(ruleEvaluator.evaluate(rule.getCondition(), eventData)).thenReturn(true);

        EvaluationResult result = ruleEngine.evaluate(eventData);

        assertThat(result.totalRulesEvaluated()).isEqualTo(1);
        assertThat(result.matchedRuleList()).hasSize(1);
        assertThat(result.matchedRuleList().getFirst().ruleName()).isEqualTo("Regra Teste");

        verify(actionRegistry).execute(rule, eventData);
        verify(auditService).log(rule, eventData, true, "ALERT");
    }

    // regra não dispara — nenhuma ação executada
    @Test
    void deveRetornarListaVaziaQuandoNenhumaRegraForSatisfeita() {
        when(ruleCache.getActiveRules()).thenReturn(List.of(rule));
        when(ruleEvaluator.evaluate(rule.getCondition(), eventData)).thenReturn(false);

        EvaluationResult result = ruleEngine.evaluate(eventData);

        assertThat(result.totalRulesEvaluated()).isEqualTo(1);
        assertThat(result.matchedRuleList()).isEmpty();

        verify(actionRegistry, never()).execute(any(), any());
        verify(auditService).log(rule, eventData, false, "ALERT");
    }

    // múltiplas regras — apenas as satisfeitas disparam
    @Test
    void deveDispararApenasAsRegrasQueCumpriremaCondicao() {
        Rule outraRegra = Rule.builder()
                .id(UUID.randomUUID())
                .name("Outra Regra")
                .condition("['valor'] > 9999")
                .actionType("LOG")
                .priority(2)
                .active(true)
                .build();

        when(ruleCache.getActiveRules()).thenReturn(List.of(rule, outraRegra));
        when(ruleEvaluator.evaluate(rule.getCondition(), eventData)).thenReturn(true);
        when(ruleEvaluator.evaluate(outraRegra.getCondition(), eventData)).thenReturn(false);

        EvaluationResult result = ruleEngine.evaluate(eventData);

        assertThat(result.totalRulesEvaluated()).isEqualTo(2);
        assertThat(result.matchedRuleList()).hasSize(1);
        assertThat(result.matchedRuleList().getFirst().ruleName()).isEqualTo("Regra Teste");
    }

    // regra BLOCK — isBlocked retorna true
    @Test
    void deveIndicarBloqueioQuandoRegraBlockDisparar() {
        Rule blockRule = Rule.builder()
                .id(UUID.randomUUID())
                .name("Regra de Bloqueio")
                .condition("['valor'] > 1000")
                .actionType("BLOCK")
                .priority(0)
                .active(true)
                .build();

        when(ruleCache.getActiveRules()).thenReturn(List.of(blockRule));
        when(ruleEvaluator.evaluate(blockRule.getCondition(), eventData)).thenReturn(true);

        EvaluationResult result = ruleEngine.evaluate(eventData);

        assertThat(result.isBlocked()).isTrue();
    }

    // nenhuma regra ativa no cache
    @Test
    void deveRetornarResultadoVazioQuandoNaoHouverRegrasAtivas() {
        when(ruleCache.getActiveRules()).thenReturn(Collections.emptyList());

        EvaluationResult result = ruleEngine.evaluate(eventData);

        assertThat(result.totalRulesEvaluated()).isZero();
        assertThat(result.matchedRuleList()).isEmpty();

        verify(ruleEvaluator, never()).evaluate(any(), any());
        verify(actionRegistry, never()).execute(any(), any());
    }
}
