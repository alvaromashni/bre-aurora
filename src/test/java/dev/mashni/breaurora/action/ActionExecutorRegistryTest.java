package dev.mashni.breaurora.action;

import dev.mashni.breaurora.rule.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ActionExecutorRegistryTest {

    private ActionExecutorRegistry registry;

    private ActionExecutor logExecutor;
    private ActionExecutor alertExecutor;
    private ActionExecutor blockExecutor;

    @BeforeEach
    void setUp(){

        logExecutor = mock(ActionExecutor.class);
        alertExecutor = mock(ActionExecutor.class);
        blockExecutor = mock(ActionExecutor.class);

        when(logExecutor.getType()).thenReturn("LOG");
        when(alertExecutor.getType()).thenReturn("ALERT");
        when(blockExecutor.getType()).thenReturn("BLOCK");

        registry = new ActionExecutorRegistry(List.of(logExecutor, alertExecutor, blockExecutor));
    }

    // executor correto é chamado para cada tipo
    @Test
    void deveExecutarLogActionQuandoTipoForLog(){
        Rule rule = buildRule("LOG");
        Map<String, Object> event = Map.of("valor", 1000);

        registry.execute(rule, event);
        verify(logExecutor).execute(rule, event);
        verify(alertExecutor, never()).execute(any(), any());
        verify(blockExecutor, never()).execute(any(), any());
    }

    @Test
    void deveExecutarAlertActionQuandoTipoForAlert(){
        Rule rule = buildRule("ALERT");
        Map<String, Object> event = Map.of("valor", 1000);

        registry.execute(rule, event);

        verify(alertExecutor).execute(rule, event);
        verify(logExecutor, never()).execute(any(), any());
        verify(blockExecutor, never()).execute(any(), any());
    }

    @Test
    void deveExecutarBlockActionQuandoTipoForBlock() {
        Rule rule = buildRule("BLOCK");
        Map<String, Object> event = Map.of("valor", 1000);

        registry.execute(rule, event);

        verify(blockExecutor).execute(rule, event);
        verify(logExecutor, never()).execute(any(), any());
        verify(alertExecutor, never()).execute(any(), any());
    }

    // tipo desconhecido — não deve lançar exceção
    @Test
    void deveIgnorarQuandoTipoNaoExistir() {
        Rule rule = buildRule("TIPO_INEXISTENTE");
        Map<String, Object> event = Map.of("valor", 1000);

        assertThatNoException().isThrownBy(() -> registry.execute(rule, event));

        verify(logExecutor, never()).execute(any(), any());
        verify(alertExecutor, never()).execute(any(), any());
        verify(blockExecutor, never()).execute(any(), any());
    }

    private Rule buildRule(String actionType) {
        return Rule.builder()
                .id(UUID.randomUUID())
                .name("Regra Teste")
                .condition("['valor'] > 500")
                .actionType(actionType)
                .priority(1)
                .active(true)
                .build();
    }

}
