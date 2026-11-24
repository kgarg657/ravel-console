package dao;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import model.AuditCase;
import model.CaseStatus;
import model.FraudType;

public class CaseDAO {

    private static final String INSERT_CASE_SQL = """
            INSERT INTO audit_case (case_id, title, fraud_type, status)
            VALUES (?, ?, ?, ?)
            """;

    private static final String SELECT_CASE_BY_ID_SQL = """
            SELECT case_id, title, fraud_type, status
            FROM audit_case
            WHERE case_id = ?
            """;

    private static final String SELECT_ALL_CASES_SQL = """
            SELECT case_id, title, fraud_type, status
            FROM audit_case
            """;

    private static final String UPDATE_CASE_SQL = """
            UPDATE audit_case
            SET title = ?, fraud_type = ?, status = ?
            WHERE case_id = ?
            """;

    private static final String DELETE_CASE_SQL = """
            DELETE FROM audit_case
            WHERE case_id = ?
            """;

    private static final String INSERT_CASE_ENTITY_SQL = """
            INSERT INTO case_entity (case_id, entity_id)
            VALUES (?, ?)
            """;

    private static final String SELECT_ENTITY_IDS_BY_CASE_SQL = """
            SELECT entity_id
            FROM case_entity
            WHERE case_id = ?
            """;

    private static final String FIND_ACTIVE_CASE_FOR_ENTITY_SQL = """
            SELECT ac.case_id
            FROM audit_case ac
            JOIN case_entity ce ON ac.case_id = ce.case_id
            WHERE ce.entity_id = ?
              AND ac.status <> 'CLOSED'
            LIMIT 1
            """;

    private static final String SELECT_ALL_CASE_IDS_SQL = """
            SELECT case_id
            FROM audit_case
            """;

    public void save(AuditCase auditCase) {
        if (auditCase == null) {
            throw new IllegalArgumentException("AuditCase cannot be null.");
        }
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(INSERT_CASE_SQL)) {
            ps.setString(1, auditCase.getId());
            ps.setString(2, auditCase.getTitle());
            ps.setString(3, auditCase.getFraudType().name());
            ps.setString(4, auditCase.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save audit case: " + auditCase.getId(), e);
        }
    }

    public Optional<AuditCase> findById(String caseId) {
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(SELECT_CASE_BY_ID_SQL)) {
            ps.setString(1, caseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToAuditCase(conn, rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find audit case by id: " + caseId, e);
        }
    }

    public List<AuditCase> findAll() {
        List<AuditCase> cases = new ArrayList<>();
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_CASES_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cases.add(mapRowToAuditCase(conn, rs));
            }
            return cases;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load all audit cases", e);
        }
    }

    public void update(AuditCase auditCase) {
        if (auditCase == null) {
            throw new IllegalArgumentException("AuditCase cannot be null.");
        }
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(UPDATE_CASE_SQL)) {
            ps.setString(1, auditCase.getTitle());
            ps.setString(2, auditCase.getFraudType().name());
            ps.setString(3, auditCase.getStatus().name());
            ps.setString(4, auditCase.getId());

            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new IllegalStateException("No audit case found to update with id: " + auditCase.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update audit case: " + auditCase.getId(), e);
        }
    }

    public void delete(String caseId) {
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(DELETE_CASE_SQL)) {
            ps.setString(1, caseId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete audit case: " + caseId, e);
        }
    }

    public void addEntityToCase(String caseId, String entityId) {
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(INSERT_CASE_ENTITY_SQL)) {
            ps.setString(1, caseId);
            ps.setString(2, entityId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to link entity " + entityId + " to case " + caseId, e);
        }
    }

    public List<String> findEntityIdsByCaseId(String caseId) {
        List<String> entityIds = new ArrayList<>();
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(SELECT_ENTITY_IDS_BY_CASE_SQL)) {
            ps.setString(1, caseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entityIds.add(rs.getString("entity_id"));
                }
            }
            return entityIds;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load entity IDs for case: " + caseId, e);
        }
    }

    public String findActiveCaseIdFor(String entityId) {
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(FIND_ACTIVE_CASE_FOR_ENTITY_SQL)) {
            ps.setString(1, entityId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("case_id");
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check active case for entity: " + entityId, e);
        }
    }

    public int findMaxCaseNumber() {
        int maxNumber = 0;
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_CASE_IDS_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String caseId = rs.getString("case_id");
                if (caseId != null) {
                    String digitsOnly = caseId.replaceAll("\\D+", "");
                    if (!digitsOnly.isEmpty()) {
                        int parsed = Integer.parseInt(digitsOnly);
                        if (parsed > maxNumber) {
                            maxNumber = parsed;
                        }
                    }
                }
            }
            return maxNumber;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find max case number", e);
        }
    }

    private AuditCase mapRowToAuditCase(Connection conn, ResultSet rs) throws SQLException {
        String id = rs.getString("case_id");
        String title = rs.getString("title");
        FraudType fraudType = FraudType.valueOf(rs.getString("fraud_type"));
        CaseStatus status = CaseStatus.valueOf(rs.getString("status"));

        List<String> entityIds = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ENTITY_IDS_BY_CASE_SQL)) {
            ps.setString(1, id);
            try (ResultSet entityRs = ps.executeQuery()) {
                while (entityRs.next()) {
                    entityIds.add(entityRs.getString("entity_id"));
                }
            }
        }

        if (entityIds.isEmpty()) {
            entityIds.add("UNKNOWN");
        }

        AuditCase auditCase = new AuditCase(id, title, fraudType, entityIds);
        auditCase.setStatus(status);
        return auditCase;
    }
}