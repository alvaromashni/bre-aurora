package dev.mashni.breaurora.rule;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RuleServiceTest {

    @Mock
    private RuleRepository ruleRepository;

    @InjectMocks
    private RuleService ruleService;

    private Rule rule;

    @BeforeEach
    void setUp(){
        rule = Rule.builder()
                .id(UUID.randomUUID())
                .name("Regra teste")
                .condition("['valor'] > 1000")
                .actionType("LOG")
                .priority(1)
                .active(true)
                .build();
    }

    @Test
    void deveSalvarRegra(){
        when(ruleRepository.save(rule)).thenReturn(rule);

        Rule result = ruleService.create(rule);

        assertThat(result).isEqualTo(rule);
        verify(ruleRepository).save(rule);
    }

    // findAll
    @Test
    void deveRetornarTodasAsRegras() {
        when(ruleRepository.findAll()).thenReturn(List.of(rule));

        List<Rule> result = ruleService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Regra teste");
    }

    // findById — encontrado
    @Test
    void deveRetornarRegraPorId() {
        when(ruleRepository.findById(rule.getId())).thenReturn(Optional.of(rule));

        Rule result = ruleService.findById(rule.getId());

        assertThat(result.getId()).isEqualTo(rule.getId());
    }

    // findById — não encontrado
    @Test
    void deveLancarExcecaoQuandoRegraNaoEncontrada() {
        UUID id = UUID.randomUUID();
        when(ruleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ruleService.findById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    // update
    @Test
    void deveAtualizarRegra() {
        Rule updated = Rule.builder()
                .name("Regra Atualizada")
                .condition("['valor'] > 2000")
                .actionType("ALERT")
                .priority(2)
                .active(false)
                .build();

        when(ruleRepository.findById(rule.getId())).thenReturn(Optional.of(rule));
        when(ruleRepository.save(any(Rule.class))).thenAnswer(inv -> inv.getArgument(0));

        Rule result = ruleService.update(rule.getId(), updated);

        assertThat(result.getName()).isEqualTo("Regra Atualizada");
        assertThat(result.getCondition()).isEqualTo("['valor'] > 2000");
        assertThat(result.getActionType()).isEqualTo("ALERT");
        assertThat(result.getPriority()).isEqualTo(2);
        assertThat(result.getActive()).isFalse();
    }

    // update: regra nao encontrada
    @Test
    void deveLancarExcecaoAoAtualizarRegraNaoEncontrada() {
        UUID id = UUID.randomUUID();
        when(ruleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ruleService.update(id, rule))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // delete
    @Test
    void deveDeletarRegra() {
        when(ruleRepository.findById(rule.getId())).thenReturn(Optional.of(rule));

        ruleService.delete(rule.getId());

        verify(ruleRepository).delete(rule);
    }

    // delete — regra não encontrada
    @Test
    void deveLancarExcecaoAoDeletarRegraNaoEncontrada() {
        UUID id = UUID.randomUUID();
        when(ruleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ruleService.delete(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // findActiveOrderedByPriority
    @Test
    void deveRetornarApenasRegrasAtivas() {
        when(ruleRepository.findByActiveTrueOrderByPriorityAsc()).thenReturn(List.of(rule));

        List<Rule> result = ruleService.findActiveOrderedByPriority();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getActive()).isTrue();
    }

}
