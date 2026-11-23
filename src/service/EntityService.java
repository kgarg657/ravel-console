package service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import model.Entity;
import model.EntityType;

public class EntityService {
    public EntityService() {
    }

    public void register(Entity e) {
    }

    public Entity findById(String id) {
        return null;
    }

    public List<Entity> getAll() {
        return new ArrayList<>();
    }

    public List<Entity> getByType(EntityType t) {
        return new ArrayList<>();
    }

    public void update(Entity e) {
    }

    public void delete(String id) {
    }

    public List<Entity> top5() {
        return new ArrayList<>();
    }

    public Map<EntityType, Integer> countByType() {
        return new EnumMap<>(EntityType.class);
    }
}
