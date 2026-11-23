package service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import model.Entity;
import model.Invoice;
import model.Relationship;
import model.RelationshipStatus;
import repository.Repository;
import util.IdGenerator;

public class RelationshipService {
    public RelationshipService(
            Repository<Relationship> links,
            Repository<Entity> entities,
            IdGenerator ids) {
    }

    public void addLink(Relationship r) {
    }

    public List<Relationship> findAll() {
        return new ArrayList<>();
    }

    public List<Relationship> findByStatus(RelationshipStatus s) {
        return new ArrayList<>();
    }

    public Relationship findById(String linkId) {
        return null;
    }

    public void createInvoiceLinks(Invoice inv) {
    }

    public void recordPayment(String srcVendorId, String tgtVendorId, double amount) {
    }

    public void approve(String linkId) {
    }

    public void reject(String linkId) {
    }

    public boolean hasLinks(String entityId) {
        return false;
    }

    public Map<RelationshipStatus, Integer> countByStatus() {
        return new EnumMap<>(RelationshipStatus.class);
    }
}