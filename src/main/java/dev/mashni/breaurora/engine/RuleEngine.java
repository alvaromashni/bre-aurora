package dev.mashni.breaurora.engine;

import dev.mashni.breaurora.action.ActionExecutorRegistry;
import dev.mashni.breaurora.audit.AuditService;
import dev.mashni.breaurora.engine.dto.EvaluationResult;
import dev.mashni.breaurora.engine.dto.MatchedRule;
import dev.mashni.breaurora.rule.Rule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleEngine {

    private final RuleEvaluator ruleEvaluator;
    private final ActionExecutorRegistry actionRegistry;
    private final AuditService auditService;
    private final RuleCache ruleCache;

    public EvaluationResult evaluate(Map<String, Object> eventData){
        List<Rule> rules = ruleCache.getActiveRules();
        List<MatchedRule> matched = new ArrayList<>();

        for (Rule rule : rules){
            boolean result = ruleEvaluator.evaluate(rule.getCondition(), eventData);

            auditService.log(rule, eventData, result, rule.getActionType());

            if(result) {
                log.info("Regra disparada: '{}' -> ação: {}", rule.getName(), rule.getActionType());
                actionRegistry.execute(rule, eventData);
                matched.add(new MatchedRule(rule.getId(), rule.getName(), rule.getActionType()));
            }
        }
        return new EvaluationResult(rules.size(), matched);
    }
}
