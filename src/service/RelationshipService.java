package service;

import exception.DuplicateRecordException;
import exception.InvalidEntityException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.Entity;
import model.Invoice;
import model.Relationship;
import model.RelationshipStatus;
import model.Vendor;
import repository.Repository;
import util.IdGenerator;

public class RelationshipService {
    private Repository<Relationship> links;
    private Repository<Entity> entities;
    private IdGenerator ids;
    private Set<String> linkKeys;

    public RelationshipService(
            Repository<Relationship> links,
            Repository<Entity> entities,
            IdGenerator ids) {
        this.links = links;
        this.entities = entities;
        this.ids = ids;
        this.linkKeys = new HashSet<>();
    }

    public void addLink(Relationship r) {
        if (r == null) {
            throw new InvalidEntityException("Relationship cannot be null.");
        }
        if (r.getId() == null || r.getId().trim().isEmpty()) {
            throw new InvalidEntityException("Relationship linkId must be set.");
        }
        String key = r.key();
        if (linkKeys.contains(key)) {
            throw new DuplicateRecordException("Duplicate relationship key: " + key);
        }
        links.save(r);
        linkKeys.add(key);
    }

    public List<Relationship> findAll() {
        return links.findAll();
    }

    public List<Relationship> findByStatus(RelationshipStatus s) {
        List<Relationship> result = new ArrayList<>();
        if (s == null) {
            return result;
        }
        for (Relationship r : links.findAll()) {
            if (r.getStatus() == s) {
                result.add(r);
            }
        }
        return result;
    }

    public Relationship findById(String linkId) {
        if (linkId == null) {
            return null;
        }
        return links.findById(linkId);
    }

    public void createInvoiceLinks(Invoice inv) {
        if (inv == null) {
            throw new InvalidEntityException("Invoice cannot be null.");
        }
        String invoiceId = inv.getId();
        String approverId = inv.getApproverId();
        String vendorId = inv.getVendorId();

        Relationship approvedBy = new Relationship(
                ids.nextLinkId(),
                approverId,
                invoiceId,
                "APPROVED_BY",
                RelationshipStatus.APPROVED,
                null
        );
        addLink(approvedBy);

        Relationship payableTo = new Relationship(
                ids.nextLinkId(),
                invoiceId,
                vendorId,
                "PAYABLE_TO",
                RelationshipStatus.APPROVED,
                null
        );
        addLink(payableTo);
    }

    public void recordPayment(String srcVendorId, String tgtVendorId, double amount) {
        if (srcVendorId == null || tgtVendorId == null || srcVendorId.equals(tgtVendorId)) {
            throw new InvalidEntityException("Payment requires two distinct vendor IDs.");
        }
        if (amount <= 0) {
            throw new InvalidEntityException("Payment amount must be greater than 0.");
        }
        Entity src = entities.findById(srcVendorId);
        Entity tgt = entities.findById(tgtVendorId);
        if (!(src instanceof Vendor) || !(tgt instanceof Vendor)) {
            throw new InvalidEntityException("Both source and target entities must be Vendors.");
        }

        Relationship payment = new Relationship(
                ids.nextLinkId(),
                srcVendorId,
                tgtVendorId,
                "PAYMENT",
                RelationshipStatus.APPROVED,
                Double.valueOf(amount)
        );
        addLink(payment);
    }

    public void approve(String linkId) {
        Relationship r = findById(linkId);
        if (r == null) {
            throw new InvalidEntityException("Relationship not found: " + linkId);
        }
        if (r.getStatus() != RelationshipStatus.SUGGESTED) {
            throw new InvalidEntityException("Only SUGGESTED links can be approved.");
        }
        r.setStatus(RelationshipStatus.APPROVED);
        links.update(r);

        Entity first = entities.findById(r.getSourceId());
        if (first != null) {
            first.addRisk(15);
            entities.update(first);
        }

        Entity second = entities.findById(r.getTargetId());
        if (second != null) {
            second.addRisk(15);
            entities.update(second);
        }
    }

    public void reject(String linkId) {
        Relationship r = findById(linkId);
        if (r == null) {
            throw new InvalidEntityException("Relationship not found: " + linkId);
        }
        if (r.getStatus() != RelationshipStatus.SUGGESTED) {
            throw new InvalidEntityException("Only SUGGESTED links can be rejected.");
        }
        r.setStatus(RelationshipStatus.REJECTED);
        links.update(r);
    }

    public boolean hasLinks(String entityId) {
        if (entityId == null) {
            return false;
        }
        for (Relationship r : links.findAll()) {
            if (entityId.equals(r.getSourceId()) || entityId.equals(r.getTargetId())) {
                return true;
            }
        }
        return false;
    }

    public Map<RelationshipStatus, Integer> countByStatus() {
        Map<RelationshipStatus, Integer> counts = new EnumMap<>(RelationshipStatus.class);
        for (RelationshipStatus status : RelationshipStatus.values()) {
            counts.put(status, Integer.valueOf(0));
        }
        for (Relationship r : links.findAll()) {
            RelationshipStatus st = r.getStatus();
            if (st != null) {
                counts.put(st, Integer.valueOf(counts.get(st).intValue() + 1));
            }
        }
        return counts;
    }
}