package dev.mashni.breaurora.rule;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rules")
public class RuleController {

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Rule create(@RequestBody Rule rule){
        return ruleService.create(rule);
    }

    @GetMapping
    public List<Rule> findAll() {
        return ruleService.findAll();
    }

    @GetMapping("/{id}")
    public Rule findById(@PathVariable UUID id) {
        return ruleService.findById(id);
    }

    @PutMapping("/{id}")
    public Rule update(@PathVariable UUID id, @RequestBody Rule rule) {
        return ruleService.update(id, rule);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        ruleService.delete(id);
    }
}
