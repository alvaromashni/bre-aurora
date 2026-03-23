package dev.mashni.breaurora.audit;

import dev.mashni.breaurora.rule.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuditServiceTest {

    @Mock
    private AuditRepository auditRepository;

    private AuditService auditService;

    private Rule rule;
    private Map<String, Object> eventData;

    @BeforeEach
    void setUp() {
        auditService = new AuditService(auditRepository, new ObjectMapper());
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

    @Test
    void deveSalvarAuditLogQuandoRegraDisparou(){
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);

        auditService.log(rule, eventData, true, "ALERT");

        verify(auditRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getRuleName()).isEqualTo("Regra Teste");
        assertThat(saved.getMatched()).isTrue();
        assertThat(saved.getActionTaken()).isEqualTo("ALERT");
        assertThat(saved.getEventData()).contains("valor");
    }

    // log sem match — actionTaken deve ser null
    @Test
    void deveSalvarAuditLogComActionTakenNullQuandoNaoHouveMatch() {
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);

        auditService.log(rule, eventData, false, "ALERT");

        verify(auditRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getMatched()).isFalse();
        assertThat(saved.getActionTaken()).isNull();
    }

    // findAll
    @Test
    void deveRetornarTodosOsLogs() {
        AuditLog log = AuditLog.builder().ruleName("Regra Teste").matched(true).build();
        when(auditRepository.findAll()).thenReturn(List.of(log));

        List<AuditLog> result = auditService.findAll();

        assertThat(result).hasSize(1);
    }

    // findByRule
    @Test
    void deveRetornarLogsPorRegra() {
        AuditLog log = AuditLog.builder().ruleName("Regra Teste").matched(true).build();
        when(auditRepository.findByRuleId(rule.getId())).thenReturn(List.of(log));

        List<AuditLog> result = auditService.findByRule(rule.getId());

        assertThat(result).hasSize(1);
        verify(auditRepository).findByRuleId(rule.getId());
    }

    // findMatched
    @Test
    void deveRetornarApenasLogsQueDispararam() {
        AuditLog log = AuditLog.builder().matched(true).build();
        when(auditRepository.findByMatched(true)).thenReturn(List.of(log));

        List<AuditLog> result = auditService.findMatched();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getMatched()).isTrue();
    }
}
