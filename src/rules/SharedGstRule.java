package rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import model.Entity;
import model.Contactable;
import model.Relationship;
import model.RelationshipStatus;

public class SharedGstRule implements RiskRule {

    @Override 
    public String getRuleName(){
        return "Shared GST";
    }

    @Override 
    public List<Relationship> detect(List<Entity> all){
        List<Relationship> suggestions = new ArrayList<>();
        if (all == null || all.isEmpty()){
            return suggestions;
        }

        HashMap<String, List<Entity>> buckets = new HashMap<>();
        for(Entity entity: all){
            if(!(entity instanceof Contactable)){
                continue;
            }
            String rawGST = ((Contactable) entity).getGst();
            if(rawGST == null|| rawGST.trim().isEmpty()){
                continue;
            }
            String normalizedGst = rawGST.trim().toUpperCase();
            if (!buckets.containsKey(normalizedGst)) {
                buckets.put(normalizedGst, new ArrayList<>());
            }
            buckets.get(normalizedGst).add(entity);
        }
        
        for (List<Entity> group : buckets.values()) {
            if (group.size() < 2) {
                continue;
            }
            for (int i = 0; i < group.size(); i++) {
                for (int j = i + 1; j < group.size(); j++) {
                    String id1 = group.get(i).getId();
                    String id2 = group.get(j).getId();
                    if (id1.equalsIgnoreCase(id2)) {
                        continue;
                    }
                    String sourceId = (id1.compareTo(id2) < 0) ? id1 : id2;
                    String targetId = (id1.compareTo(id2) < 0) ? id2 : id1;

                    suggestions.add(new Relationship(
                        sourceId,
                        targetId,
                        "SHARED_GST",
                        RelationshipStatus.SUGGESTED
                    ));
                }
            }
        }
        return suggestions;
    }
}
