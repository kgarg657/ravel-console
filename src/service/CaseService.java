package service;

import dao.CaseDAO;
import dao.EntityDAO;
import exception.InvalidEntityException;
import java.util.List;
import java.util.Optional;
import model.AuditCase;
import model.CaseStatus;
import model.Entity;
import model.FraudType;

public class CaseService {
    private final CaseDAO caseDAO;
    private final EntityDAO entityDAO;

    public CaseService(CaseDAO caseDAO, EntityDAO entityDAO) {
        if (caseDAO == null || entityDAO == null) {
            throw new IllegalArgumentException("CaseDAO and EntityDAO cannot be null.");
        }
        this.caseDAO = caseDAO;
        this.entityDAO = entityDAO;
    }

    public synchronized AuditCase openCase(
            String caseId,
            String title,
            FraudType fraudType,
            List<String> entityIds) {
        if (entityIds == null || entityIds.isEmpty()) {
            throw new InvalidEntityException("An audit case must include at least one entity.");
        }

        for (String entityId : entityIds) {
            Entity entity = entityDAO.findById(entityId);
            if (entity == null) {
                throw new InvalidEntityException("Entity not found: " + entityId);
            }
            String activeCaseId = caseDAO.findActiveCaseIdFor(entityId);
            if (activeCaseId != null) {
                throw new IllegalStateException(
                        "Entity " + entityId + " is already part of active case " + activeCaseId + ".");
            }
        }

        AuditCase auditCase = new AuditCase(caseId, title, fraudType, entityIds);
        caseDAO.save(auditCase);

        for (String entityId : auditCase.getEntityIds()) {
            caseDAO.addEntityToCase(auditCase.getId(), entityId);
        }

        return auditCase;
    }

    public synchronized void addEntityToCase(String caseId, String entityId) {
        AuditCase existingCase = caseDAO.findById(caseId)
                .orElseThrow(() -> new InvalidEntityException("Case not found: " + caseId));

        if (existingCase.getStatus() == CaseStatus.CLOSED) {
            throw new IllegalStateException("Cannot add entities to a CLOSED case: " + caseId);
        }

        Entity entity = entityDAO.findById(entityId);
        if (entity == null) {
            throw new InvalidEntityException("Entity not found: " + entityId);
        }

        String activeCaseId = caseDAO.findActiveCaseIdFor(entityId);
        if (activeCaseId != null && !activeCaseId.equals(caseId)) {
            throw new IllegalStateException(
                    "Entity " + entityId + " is already in active case " + activeCaseId + ".");
        }

        existingCase.addEntityId(entityId);
        caseDAO.addEntityToCase(caseId, entityId);
    }

    public synchronized void updateCaseStatus(String caseId, CaseStatus newStatus) {
        AuditCase existingCase = caseDAO.findById(caseId)
                .orElseThrow(() -> new InvalidEntityException("Case not found: " + caseId));
        existingCase.setStatus(newStatus);
        caseDAO.update(existingCase);
    }

    public Optional<AuditCase> findCaseById(String caseId) {
        return caseDAO.findById(caseId);
    }

    public List<AuditCase> findAllCases() {
        return caseDAO.findAll();
    }

    public String findActiveCaseIdForEntity(String entityId) {
        return caseDAO.findActiveCaseIdFor(entityId);
    }
}