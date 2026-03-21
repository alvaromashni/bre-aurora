package dev.mashni.breaurora.action;

import dev.mashni.breaurora.rule.Rule;

import java.util.Map;

public interface ActionExecutor {
    void execute(Rule rule, Map<String, Object> eventData);
    String getType();
}
