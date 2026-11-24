package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.AuditLog;

public class AuditLogDAO {

    public void insert(Connection conn, String action, String targetId) throws SQLException {
        if (Boolean.getBoolean("ravel.failAudit")) {
            throw new SQLException("Simulated audit failure (-Dravel.failAudit=true)");
        }

        String sql = "INSERT INTO audit_log (action, target_id) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, action);
            ps.setString(2, targetId);
            ps.executeUpdate();
        }
    }

    public List<AuditLog> findAll() {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT log_id, action, target_id, created_at FROM audit_log ORDER BY log_id";

        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                long logId = rs.getLong("log_id");
                String action = rs.getString("action");
                String targetId = rs.getString("target_id");
                Timestamp ts = rs.getTimestamp("created_at");
                LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : null;
                logs.add(new AuditLog(logId, action, targetId, createdAt));
            }
            return logs;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load audit logs: " + e.getMessage(), e);
        }
    }
}