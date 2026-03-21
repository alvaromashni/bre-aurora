package dev.mashni.breaurora.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

@Component
@Slf4j
public class SpelRuleEvaluator implements RuleEvaluator {

    @Override
    public boolean evaluate(String condition, Map<String, Object> eventData) {
        try {
            StandardEvaluationContext context = buildContext(eventData);
            Expression expression = new SpelExpressionParser().parseExpression(condition);
            Boolean result = expression.getValue(context, Boolean.class);
            return Boolean.TRUE.equals(result);

        } catch (EvaluationException | ParseException e) {
            log.warn("Erro ao validar condição '{}' : {}", condition, e.getMessage());
            return false;
        }
    }

    private StandardEvaluationContext buildContext(Map<String, Object> eventData){
        StandardEvaluationContext context = new StandardEvaluationContext(eventData);
        context.setVariables(eventData);
        return context;
    }

}
