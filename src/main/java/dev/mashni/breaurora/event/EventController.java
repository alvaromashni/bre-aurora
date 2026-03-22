package dev.mashni.breaurora.event;

import dev.mashni.breaurora.engine.RuleEngine;
import dev.mashni.breaurora.engine.dto.EvaluationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final RuleEngine ruleEngine;

    @PostMapping("/evaluate")
    public ResponseEntity<EvaluationResult> evaluate(@RequestBody Map<String, Object> eventData) {
        EvaluationResult result = ruleEngine.evaluate(eventData);

        HttpStatus status = result.isBlocked() ? HttpStatus.FORBIDDEN : HttpStatus.OK;
        return ResponseEntity.status(status).body(result);
    }
}