package dev.mashni.breaurora.engine;

import java.util.Map;

public interface RuleEvaluator {
    boolean evaluate(String condition, Map<String, Object> eventData);
}
