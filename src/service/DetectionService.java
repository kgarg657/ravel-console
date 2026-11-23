package service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.DetectionReport;
import model.Entity;
import model.Relationship;
import rules.RiskRule;
import util.IdGenerator;

public class DetectionService {
    private final RelationshipService relationshipService;
    private final IdGenerator idGenerator;
    private final List<RiskRule> rules = new ArrayList<>();

    public DetectionService(RelationshipService relationshipService, IdGenerator idGenerator) {
        this.relationshipService = relationshipService;
        this.idGenerator = idGenerator;
    }

    public void addRule(RiskRule rule) {
        if (rule != null) {
            rules.add(rule);
        }
    }

    public List<RiskRule> getRules() {
        return new ArrayList<>(rules);
    }

    public DetectionReport runAll(List<Entity> entities) {
        List<Entity> safeEntities ;
        if(entities == null){
            safeEntities = new ArrayList<>(); 
        } else {
            safeEntities = entities;            
        }

        Set<String> seenKeys = new HashSet<>();
        if (relationshipService != null) {
            for (Relationship existing : relationshipService.findAll()) {
                if (existing != null) {
                    seenKeys.add(existing.key());
                }
            }
        }

        Map<String, Integer> countsByRule = new HashMap<>();
        List<Relationship> newLinks = new ArrayList<>();

        for (RiskRule rule : rules) {
            List<Relationship> candidates = rule.detect(safeEntities);
            int savedForRule = 0;

            if (candidates != null) {
                for (Relationship candidate : candidates) {
                    if (candidate != null && !seenKeys.contains(candidate.key())) {
                        seenKeys.add(candidate.key());
                        if (idGenerator != null && (candidate.getId() == null || candidate.getId().trim().isEmpty())) {
                            candidate.setId(idGenerator.nextLinkId());
                        }
                        if (relationshipService != null) {
                            relationshipService.addLink(candidate);
                        }
                        newLinks.add(candidate);
                        savedForRule++;
                    }
                }
            }

            countsByRule.put(rule.getRuleName(), savedForRule);
        }

        return new DetectionReport(safeEntities.size(), countsByRule, newLinks);
    }
}