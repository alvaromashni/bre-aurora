package dev.mashni.breaurora.engine.dto;

import java.util.List;

public record EvaluationResult(
        int totalRulesEvaluated,
        List<MatchedRule> matchedRuleList
) {
    public boolean isBlocked() {
        return matchedRuleList.stream()
                .anyMatch(r -> "BLOCK".equals(r.actionTaken()));
    }
}