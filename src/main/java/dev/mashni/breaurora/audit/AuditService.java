package dev.mashni.breaurora.audit;

import dev.mashni.breaurora.rule.Rule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;


import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    public void log(Rule rule, Map<String, Object> eventData, boolean matched, String actionTaken) {
        String eventJson = objectMapper.convertValue(eventData, JsonNode.class).toString();

        AuditLog log = AuditLog.builder()
                .rule(rule)
                .ruleName(rule.getName())
                .eventData(eventJson)
                .matched(matched)
                .actionTaken(matched ? actionTaken : null)
                .build();
        auditRepository.save(log);
    }

    public List<AuditLog> findAll() {
        return auditRepository.findAll();
    }

    public List<AuditLog> findByRule(UUID ruleId) {
        return auditRepository.findByRuleId(ruleId);
    }

    public List<AuditLog> findMatched() {
        return auditRepository.findByMatched(true);
    }
}
