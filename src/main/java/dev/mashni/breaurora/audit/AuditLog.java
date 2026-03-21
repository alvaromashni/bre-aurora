package dev.mashni.breaurora.audit;

import dev.mashni.breaurora.rule.Rule;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private Rule rule;

    @Column(name = "rule_name")
    private String ruleName;

    @Column(name = "event_data", columnDefinition = "jsonb")
    private String eventData;

    @Column(nullable = false)
    private Boolean matched;

    @Column(name = "action_taken")
    private String actionTaken;

    @CreationTimestamp
    @Column(name = "evaluated_at", updatable = false)
    private LocalDateTime evaluatedAt;
}