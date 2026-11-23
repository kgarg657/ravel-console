package concurrency;

import exception.CaseFullException;

public class UnsafeSlotManager extends CaseSlotManager {

    public UnsafeSlotManager(String caseId) {
        super(caseId, 2);
    }

    public UnsafeSlotManager(String caseId, int maxAuditors) {
        super(caseId, maxAuditors);
    }

    @Override
    public void claim(String auditor) throws CaseFullException {
        if (assigned >= maxAuditors) {
            throw new CaseFullException(caseId);
        }
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        assigned++;
    }
}