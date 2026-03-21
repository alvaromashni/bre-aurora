package dev.mashni.breaurora.engine.dto;

import java.util.UUID;

public record MatchedRule(
        UUID ruleId,
        String ruleName,
        String actionTaken
) {}
