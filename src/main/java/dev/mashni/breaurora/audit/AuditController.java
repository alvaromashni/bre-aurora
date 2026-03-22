package dev.mashni.breaurora.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public List<AuditLog> findAll() {
        return auditService.findAll();
    }

    @GetMapping("/matched")
    public List<AuditLog> findMatched() {
        return auditService.findMatched();
    }

    @GetMapping("/rule/{ruleId}")
    public List<AuditLog> findByRule(@PathVariable UUID ruleId) {
        return auditService.findByRule(ruleId);
    }
}
