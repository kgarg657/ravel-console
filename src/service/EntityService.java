package service;

import exception.DuplicateRecordException;
import exception.InvalidEntityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.Employee;
import model.Entity;
import model.EntityType;
import model.Invoice;
import model.Vendor;
import repository.Repository;

public class EntityService {
    private Repository<Entity> entities;
    private RelationshipService relationshipService;
    private CaseService caseService;
    private Set<String> registeredIds;

    public EntityService(Repository<Entity> entities, RelationshipService relationshipService) {
        this.entities = entities;
        this.relationshipService = relationshipService;
        this.registeredIds = new HashSet<>();
    }

    public void setCaseService(CaseService caseService) {
        this.caseService = caseService;
    }

    public void register(Entity e) {
        if (e == null || e.getId() == null || e.getId().trim().isEmpty()) {
            throw new InvalidEntityException("Entity or ID cannot be null or blank.");
        }
        String id = e.getId();
        if (registeredIds.contains(id)) {
            throw new DuplicateRecordException("Entity already registered with ID: " + id);
        }

        if (e instanceof Invoice) {
            Invoice inv = (Invoice) e;
            Entity approver = entities.findById(inv.getApproverId());
            Entity vendor = entities.findById(inv.getVendorId());
            if (!(approver instanceof Employee)) {
                throw new InvalidEntityException("Invoice approver must be a registered Employee.");
            }
            if (!(vendor instanceof Vendor)) {
                throw new InvalidEntityException("Invoice vendor must be a registered Vendor.");
            }
            entities.save(e);
            registeredIds.add(id);
            if (relationshipService != null) {
                relationshipService.createInvoiceLinks(inv);
            }
            return;
        }

        entities.save(e);
        registeredIds.add(id);
    }

    public Entity findById(String id) {
        if (id == null) {
            return null;
        }
        return entities.findById(id);
    }

    public List<Entity> getAll() {
        return entities.findAll();
    }

    public List<Entity> getByType(EntityType t) {
        List<Entity> matches = new ArrayList<>();
        if (t == null) {
            return matches;
        }
        for (Entity e : entities.findAll()) {
            if (e.getType() == t) {
                matches.add(e);
            }
        }
        return matches;
    }

    public void update(Entity e) {
        if (e == null || e.getId() == null) {
            throw new InvalidEntityException("Entity or ID cannot be null.");
        }
        if (!registeredIds.contains(e.getId())) {
            throw new InvalidEntityException("Entity not found: " + e.getId());
        }
        entities.update(e);
    }

    public void delete(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new InvalidEntityException("ID cannot be null or blank.");
        }
        if (relationshipService != null && relationshipService.hasLinks(id)) {
            throw new InvalidEntityException("Cannot delete entity with existing links: " + id);
        }
        if (caseService != null && caseService.isInActiveCase(id)) {
            throw new InvalidEntityException("Cannot delete entity in an active case: " + id);
        }
        entities.delete(id);
        registeredIds.remove(id);
    }

    public List<Entity> top5() {
        List<Entity> sorted = new ArrayList<>(entities.findAll());
        Collections.sort(sorted, new Comparator<Entity>() {
            @Override
            public int compare(Entity a, Entity b) {
                int riskCmp = Integer.compare(b.getRiskScore(), a.getRiskScore());
                if (riskCmp != 0) {
                    return riskCmp;
                }
                String nameA = a.getName() == null ? "" : a.getName();
                String nameB = b.getName() == null ? "" : b.getName();
                return nameA.compareToIgnoreCase(nameB);
            }
        });
        int limit = Math.min(5, sorted.size());
        return new ArrayList<>(sorted.subList(0, limit));
    }

    public Map<EntityType, Integer> countByType() {
        Map<EntityType, Integer> counts = new EnumMap<>(EntityType.class);
        for (EntityType type : EntityType.values()) {
            counts.put(type, Integer.valueOf(0));
        }
        for (Entity e : entities.findAll()) {
            EntityType type = e.getType();
            if (type != null) {
                counts.put(type, Integer.valueOf(counts.get(type).intValue() + 1));
            }
        }
        return counts;
    }
}