package model;
import java.util.ArrayList;
import java.util.List;

import exception.InvalidEntityException;


public class AuditCase implements HasId{
    private final String caseId;
    private final String title;
    private final FraudType fraudType;
    private CaseStatus status;
    private final int maxAuditors;
    private final List<String> entityIds;

    AuditCase(String caseId, String title, FraudType fraudType, CaseStatus status, int maxAuditors, List<String> entityIds){
        if (caseId == null || caseId.trim().isEmpty()) {
            throw new InvalidEntityException("Case ID cannot be blank.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new InvalidEntityException("Case title cannot be blank.");
        }
        if (fraudType == null){
            throw new InvalidEntityException("Fraud Type cannot be null.");
        }
        if (status == null){
            throw new InvalidEntityException("Relationship status cannot be null.");
        }
        if(maxAuditors<1){
            throw new InvalidEntityException("Please enter a valid no. of Auditors.");
        }
        if (entityIds == null || entityIds.isEmpty()) {
            throw new InvalidEntityException("An audit case must include at least one entity.");
        }

        List<String> cleanedIds = new ArrayList<>();
        for (String id : entityIds) {
            if (id == null || id.trim().isEmpty()) {
                throw new InvalidEntityException("Entity ID inside a case cannot be blank.");
            }
            String cleanId = id.trim();
            if (!cleanedIds.contains(cleanId)) {
                cleanedIds.add(cleanId);
            }
        }

        this.caseId = caseId.trim();
        this.title = title.trim();
        this.fraudType = fraudType;
        this.status = status;
        this.maxAuditors = maxAuditors;
        this.entityIds = cleanedIds;
    }

    public AuditCase(String caseId, String title, FraudType fraudType, List<String> entityIds) {
        this(caseId, title, fraudType, CaseStatus.OPEN, 2, entityIds);
    }

    @Override
    public String getId() {
        return caseId;
    }

    public String getTitle() {
        return title;
    }

    public FraudType getFraudType() {
        return fraudType;
    }

    public CaseStatus getStatus() {
        return status;
    }

    public int getMaxAuditors() {
        return maxAuditors;
    }

    public List<String> getEntityIds() {
        return new ArrayList<>(entityIds);
    }

    public void setStatus(CaseStatus status) {
        if (status == null) {
            throw new InvalidEntityException("Case status cannot be null.");
        }
        this.status = status;
    }

    public void addEntityId(String entityId) {
        if (entityId == null || entityId.trim().isEmpty()) {
            throw new InvalidEntityException("Entity ID cannot be blank.");
        }
        String cleanId = entityId.trim();
        if (!entityIds.contains(cleanId)) {
            entityIds.add(cleanId);
        }
    }
}

