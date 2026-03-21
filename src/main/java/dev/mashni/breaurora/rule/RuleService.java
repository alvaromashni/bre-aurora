package dev.mashni.breaurora.rule;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RuleService {

    private final RuleRepository ruleRepository;

    public Rule create(Rule rule){
        return ruleRepository.save(rule);
    }

    public List<Rule> findAll(){
        return ruleRepository.findAll();
    }

    public Rule findById(UUID id){
        return ruleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Regra não encontrada: " + id));
    }

    public Rule update(UUID id, Rule updated){
        Rule existing = findById(id);

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setCondition(updated.getCondition());
        existing.setActionType(updated.getActionType());
        existing.setPriority(updated.getPriority());
        existing.setActive(updated.getActive());

        return ruleRepository.save(existing);

    }

    public List<Rule> findActiveOrderedByPriority(){
        return ruleRepository.findByActiveTrueOrderByPriorityAsc();
    }


}
