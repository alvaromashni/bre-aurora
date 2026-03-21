package dev.mashni.breaurora.action;

import dev.mashni.breaurora.rule.Rule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ActionExecutorRegistry {

    private final Map<String, ActionExecutor> executors;

    public ActionExecutorRegistry(List<ActionExecutor> executorList) {
        this.executors = executorList.stream()
                .collect(Collectors.toMap(
                        ActionExecutor::getType,
                        executor -> executor
                ));
        log.info("ActionExecutorRegistry iniciado com {} executores: {}",
                executors.size(), executors.keySet());
    }

    public void execute(Rule rule, Map<String, Object> eventData) {
        ActionExecutor executor = executors.get(rule.getActionType());

        if (executor == null) {
            log.warn("Nenhum executor encontrado para o tipo '{}' da regra '{}'",
                    rule.getActionType(), rule.getName());
            return;
        }

        executor.execute(rule, eventData);
    }
}
