package dev.mashni.breaurora.action;

import dev.mashni.breaurora.rule.Rule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertActionExecutor implements ActionExecutor {

    @Override
    public void execute(Rule rule, Map<String, Object> eventData){
        log.warn("[ALERT ACTION] Alerta disparado pela regra '{}'. Dados: {}", rule.getName(), eventData);

        // ponto de extensão: aqui entraria um webhook, email, Kafka, etc.
    }

    @Override
    public String getType(){
        return "ALERT";
    }

}
