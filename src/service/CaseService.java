package service;

import exception.CaseAlreadyActiveException;
import exception.InvalidEntityException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import model.AuditCase;
import model.CaseStatus;
import model.Entity;
import model.FraudType;
import repository.Repository;
import util.IdGenerator;

public class CaseService {
    private final Repository<AuditCase> caseRepository;
    private final Repository<Entity> entityRepository;
    private final IdGenerator idGenerator;

    public CaseService(
            Repository<AuditCase> caseRepository,
            Repository<Entity> entityRepository,
            IdGenerator idGenerator) {
        if (caseRepository == null || entityRepository == null || idGenerator == null) {
            throw new InvalidEntityException("CaseService dependencies must not be null.");
        }
        this.caseRepository = caseRepository;
        this.entityRepository = entityRepository;
        this.idGenerator = idGenerator;
    }

    public synchronized AuditCase openCase(String title, FraudType fraudType, List<String> entityIds) {
        if (title == null || title.isBlank()) {
            throw new InvalidEntityException("Case title must not be blank.");
        }
        if (fraudType == null) {
            throw new InvalidEntityException("Fraud type must not be null.");
        }
        if (entityIds == null || entityIds.isEmpty()) {
            throw new InvalidEntityException("At least one entity ID is required to open a case.");
        }

        Set<String> uniqueEntityIds = new LinkedHashSet<>();
        for (String rawId : entityIds) {
            if (rawId == null || rawId.isBlank()) {
                throw new InvalidEntityException("Entity ID must not be blank.");
            }
            String cleanEntityId = rawId.trim().toUpperCase();
            if (entityRepository.findById(cleanEntityId) == null) {
                throw new InvalidEntityException("Entity not found: " + cleanEntityId);
            }
            String activeCaseId = findActiveCaseIdFor(cleanEntityId);
            if (activeCaseId != null) {
                throw new CaseAlreadyActiveException(
                        "Entity " + cleanEntityId + " is already in active case " + activeCaseId + ".");
            }
            uniqueEntityIds.add(cleanEntityId);
        }

        String caseId = idGenerator.nextCaseId();
        AuditCase auditCase = new AuditCase(
                caseId,
                title.trim(),
                fraudType,
                new ArrayList<>(uniqueEntityIds));
        caseRepository.save(auditCase);
        return auditCase;
    }

    public synchronized AuditCase addEntityToCase(String caseId, String entityId) {
        if (caseId == null || caseId.isBlank()) {
            throw new InvalidEntityException("Case ID must not be blank.");
        }
        if (entityId == null || entityId.isBlank()) {
            throw new InvalidEntityException("Entity ID must not be blank.");
        }

        String cleanCaseId = caseId.trim().toUpperCase();
        String cleanEntityId = entityId.trim().toUpperCase();

        AuditCase auditCase = caseRepository.findById(cleanCaseId);
        if (auditCase == null) {
            throw new InvalidEntityException("Case not found: " + cleanCaseId);
        }

        if (auditCase.getStatus() == CaseStatus.CLOSED) {
            throw new InvalidEntityException("Cannot add entities to a CLOSED case: " + cleanCaseId);
        }

        if (entityRepository.findById(cleanEntityId) == null) {
            throw new InvalidEntityException("Entity not found: " + cleanEntityId);
        }

        String activeCaseId = findActiveCaseIdFor(cleanEntityId);
        if (activeCaseId != null) {
            throw new CaseAlreadyActiveException(
                    "Entity " + cleanEntityId + " is already in active case " + activeCaseId + ".");
        }

        if (!auditCase.getEntityIds().contains(cleanEntityId)) {
            auditCase.addEntityId(cleanEntityId);
            caseRepository.update(auditCase);
        }
        return auditCase;
    }

    public synchronized AuditCase changeStatus(String caseId, CaseStatus nextStatus) {
        if (caseId == null || caseId.isBlank()) {
            throw new InvalidEntityException("Case ID must not be blank.");
        }
        if (nextStatus == null) {
            throw new InvalidEntityException("Target case status must not be null.");
        }

        String cleanCaseId = caseId.trim().toUpperCase();
        AuditCase auditCase = caseRepository.findById(cleanCaseId);
        if (auditCase == null) {
            throw new InvalidEntityException("Case not found: " + cleanCaseId);
        }

        CaseStatus currentStatus = auditCase.getStatus();
        if (currentStatus == CaseStatus.CLOSED) {
            throw new InvalidEntityException("Case " + cleanCaseId + " is CLOSED and final.");
        }

        boolean validTransition =
                (currentStatus == CaseStatus.OPEN
                        && (nextStatus == CaseStatus.UNDER_REVIEW || nextStatus == CaseStatus.CLOSED))
                        || (currentStatus == CaseStatus.UNDER_REVIEW && nextStatus == CaseStatus.CLOSED);

        if (!validTransition) {
            throw new InvalidEntityException(
                    "Invalid status transition for " + cleanCaseId + ": " + currentStatus + " -> " + nextStatus);
        }

        auditCase.setStatus(nextStatus);
        caseRepository.update(auditCase);
        return auditCase;
    }

    public List<AuditCase> findAll() {
        return new ArrayList<>(caseRepository.findAll());
    }

    public AuditCase findById(String caseId) {
        if (caseId == null || caseId.isBlank()) {
            return null;
        }
        return caseRepository.findById(caseId.trim().toUpperCase());
    }

    public synchronized boolean isInActiveCase(String entityId) {
        if (entityId == null || entityId.isBlank()) {
            return false;
        }
        return findActiveCaseIdFor(entityId.trim().toUpperCase()) != null;
    }

    private String findActiveCaseIdFor(String entityId) {
        for (AuditCase auditCase : caseRepository.findAll()) {
            CaseStatus status = auditCase.getStatus();
            if ((status == CaseStatus.OPEN || status == CaseStatus.UNDER_REVIEW)
                    && auditCase.getEntityIds().contains(entityId)) {
                return auditCase.getId();
            }
        }
        return null;
    }
}