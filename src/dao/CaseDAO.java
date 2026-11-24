package dao;

import java.util.List;
import java.util.Optional;
import model.AuditCase;

public class CaseDAO {
    public void save(AuditCase auditCase) {
        throw new UnsupportedOperationException("CaseDAO.save not implemented yet");
    }

    public Optional<AuditCase> findById(String caseId) {
        throw new UnsupportedOperationException("CaseDAO.findById not implemented yet");
    }

    public List<AuditCase> findAll() {
        throw new UnsupportedOperationException("CaseDAO.findAll not implemented yet");
    }

    public void update(AuditCase auditCase) {
        throw new UnsupportedOperationException("CaseDAO.update not implemented yet");
    }

    public void delete(String caseId) {
        throw new UnsupportedOperationException("CaseDAO.delete not implemented yet");
    }

    public void addEntityToCase(String caseId, String entityId) {
        throw new UnsupportedOperationException("CaseDAO.addEntityToCase not implemented yet");
    }

    public List<String> findEntityIdsByCaseId(String caseId) {
        throw new UnsupportedOperationException("CaseDAO.findEntityIdsByCaseId not implemented yet");
    }

    public String findActiveCaseIdFor(String entityId) {
        throw new UnsupportedOperationException("CaseDAO.findActiveCaseIdFor not implemented yet");
    }

    public int findMaxCaseNumber() {
        throw new UnsupportedOperationException("CaseDAO.findMaxCaseNumber not implemented yet");
    }
}
