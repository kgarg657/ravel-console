package exception;

public class CaseFullException extends Exception {
    private final String caseId;

    public CaseFullException(String caseId){
        super("Case " + caseId + " is full. Maximum auditors already assigned.");
        this.caseId = caseId;
    }

    public String getCaseId(){
        return caseId;
    }
    
}
