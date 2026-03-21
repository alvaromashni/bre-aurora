package dev.mashni.breaurora.action;

import dev.mashni.breaurora.rule.Rule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class LogActionExecutor implements ActionExecutor{

    @Override
    public void execute(Rule rule, Map<String, Object> eventData){
        log.info("[LOG ACTION] Regra ] '{}' disparada. Dados do evento: {}", rule.getName(), eventData);
    }

    @Override
    public String getType(){
        return "LOG";
    }

}
