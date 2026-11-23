package util;

public class IdGenerator {
    private int linkCounter;
    private int caseCounter;

    public IdGenerator() {
        this.linkCounter = 0;
        this.caseCounter = 0;
    }

    public synchronized String nextLinkId() {
        linkCounter++;
        return String.format("LNK-%03d", linkCounter);
    }

    public synchronized String nextCaseId() {
        caseCounter++;
        return String.format("CASE-%02d", caseCounter);
    }

    public synchronized void initLinkCounter(int max) {
        if (max > linkCounter) {
            linkCounter = max;
        }
    }

    public synchronized void initCaseCounter(int max) {
        if (max > caseCounter) {
            caseCounter = max;
        }
    }
}