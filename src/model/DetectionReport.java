package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetectionReport {
    private final int entitiesScanned;
    private final Map<String, Integer> countsByRule;
    private final List<Relationship> newLinks;
    
    public DetectionReport(int entitiesScanned, Map<String, Integer> countsByRule, List<Relationship> newLinks){
        this.entitiesScanned = entitiesScanned;

    if (countsByRule == null) {
        this.countsByRule = new HashMap<>();
    } else {
        this.countsByRule = countsByRule;
    }

    if (newLinks == null) {
        this.newLinks = new ArrayList<>();
    } else {
        this.newLinks = newLinks;
    }
    }

    public int getEntitiesScanned(){
        return entitiesScanned;
    }

    public Map<String, Integer> getCountsByRule(){
        return new HashMap<>(countsByRule);
    }

    public List<Relationship> getNewLinks(){
        return new ArrayList<>(newLinks);
    }
    
}
