package dev.mashni.breaurora.engine;

import dev.mashni.breaurora.rule.Rule;
import dev.mashni.breaurora.rule.RuleService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.time.zone.ZoneRulesProvider.refresh;

@Component
@RequiredArgsConstructor
@Slf4j
public class RuleCache {

    private final RuleService ruleService;

    private List<Rule> cachedRules = new ArrayList<>();

    /**
     * @PostConstruct — garante que o cache é preenchido assim que
     * o bean é criado, antes de qualquer requisição chegar.
     */
    @PostConstruct
    public void init(){
        refresh();
    }

    @Scheduled(fixedRateString = "${rule.cache.refresh-rate-ms:300000}")
    public void refresh(){
        cachedRules = ruleService.findActiveOrderedByPriority();
        log.info("Cache de regras atualizado: {} regras ativas", cachedRules.size());
    }

    /**
     * Collections.unmodifiableList — o cache expõe uma lista somente leitura.
     * Nenhum código externo consegue modificar acidentalmente as regras em memória.
     */
    public List<Rule> getActiveRules(){
        return Collections.unmodifiableList(cachedRules);
    }

}
