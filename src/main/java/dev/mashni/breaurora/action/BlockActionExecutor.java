package dev.mashni.breaurora.action;

import dev.mashni.breaurora.rule.Rule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class BlockActionExecutor implements ActionExecutor {

    @Override
    public void execute(Rule rule, Map<String, Object> eventData) {
        log.warn("[BLOCK ACTION] Evento bloqueado pela regra '{}'.", rule.getName());

        // na camada de cima (RuleEngine) é possível checar se alguma ação foi BLOCK
        // e retornar status diferente na resposta
    }

    @Override
    public String getType() {
        return "BLOCK";
    }
}