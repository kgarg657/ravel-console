package concurrency;

import exception.CaseFullException;

public class CaseSlotManager {
    protected final String caseId;
    protected final int maxAuditors;
    protected int assigned;

    public CaseSlotManager(String caseId) {
        this(caseId, 2);
    }

    public CaseSlotManager(String caseId, int maxAuditors) {
        this.caseId = caseId;
        this.maxAuditors = maxAuditors;
        this.assigned = 0;
    }

    public synchronized void claim(String auditor) throws CaseFullException {
        if (assigned >= maxAuditors) {
            throw new CaseFullException(caseId);
        }
        assigned++;
    }

    public String getCaseId() {
        return caseId;
    }

    public int getMaxAuditors() {
        return maxAuditors;
    }

    public synchronized int getAssigned() {
        return assigned;
    }
}