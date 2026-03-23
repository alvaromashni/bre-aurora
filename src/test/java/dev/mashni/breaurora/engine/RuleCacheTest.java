package dev.mashni.breaurora.engine;

import dev.mashni.breaurora.rule.Rule;
import dev.mashni.breaurora.rule.RuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleCacheTest {

    @Mock
    private RuleService ruleService;

    @InjectMocks
    private RuleCache ruleCache;

    private Rule rule;

    @BeforeEach
    void setUp() {
        rule = Rule.builder()
                .id(UUID.randomUUID())
                .name("Regra Teste")
                .condition("['valor'] > 1000")
                .actionType("LOG")
                .priority(1)
                .active(true)
                .build();
    }

    // init — cache preenchido ao criar o bean
    @Test
    void devePreencherCacheAoInicializar() {
        when(ruleService.findActiveOrderedByPriority()).thenReturn(List.of(rule));

        ruleCache.init();

        assertThat(ruleCache.getActiveRules()).hasSize(1);
        assertThat(ruleCache.getActiveRules().get(0).getName()).isEqualTo("Regra Teste");
    }

    // refresh — cache atualiza com novas regras
    @Test
    void deveAtualizarCacheAoRefrescar() {
        when(ruleService.findActiveOrderedByPriority()).thenReturn(List.of(rule));
        ruleCache.init();

        Rule novaRegra = Rule.builder()
                .id(UUID.randomUUID())
                .name("Nova Regra")
                .condition("['valor'] > 2000")
                .actionType("ALERT")
                .priority(2)
                .active(true)
                .build();

        when(ruleService.findActiveOrderedByPriority()).thenReturn(List.of(rule, novaRegra));
        ruleCache.refresh();

        assertThat(ruleCache.getActiveRules()).hasSize(2);
    }

    // cache vazio — sem regras ativas
    @Test
    void deveRetornarListaVaziaQuandoNaoHouverRegrasAtivas() {
        when(ruleService.findActiveOrderedByPriority()).thenReturn(Collections.emptyList());

        ruleCache.init();

        assertThat(ruleCache.getActiveRules()).isEmpty();
    }

    // lista imutável — não pode ser modificada externamente
    @Test
    void deveRetornarListaImutavel() {
        when(ruleService.findActiveOrderedByPriority()).thenReturn(List.of(rule));

        ruleCache.init();

        assertThatThrownBy(() -> ruleCache.getActiveRules().add(rule))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}