package rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Contactable;
import model.Entity;
import model.Relationship;
import model.RelationshipStatus;

public abstract class SharedFieldRule implements RiskRule {

    protected abstract String extract(Contactable c);
    protected abstract String label();

    protected String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        return raw.trim();
    }

    @Override
    public List<Relationship> detect(List<Entity> all) {
        List<Relationship> suggestions = new ArrayList<>();
        if (all == null || all.isEmpty()) {
            return suggestions;
        }

        Map<String, List<Entity>> buckets = new HashMap<>();
        for (Entity entity : all) {
            if (!(entity instanceof Contactable)) {
                continue;
            }
            String rawValue = extract((Contactable) entity);
            String normalizedValue = normalize(rawValue);
            if (normalizedValue == null || normalizedValue.isEmpty()) {
                continue;
            }
            if (!buckets.containsKey(normalizedValue)) {
                buckets.put(normalizedValue, new ArrayList<>());
            }
            buckets.get(normalizedValue).add(entity);
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

                    String sourceId;
                    String targetId;
                    if (id1.compareToIgnoreCase(id2) < 0) {
                        sourceId = id1;
                        targetId = id2;
                    } else {
                        sourceId = id2;
                        targetId = id1;
                    }

                    suggestions.add(new Relationship(
                        sourceId,
                        targetId,
                        label(),
                        RelationshipStatus.SUGGESTED
                    ));
                }
            }
        }

        return suggestions;
    }
}