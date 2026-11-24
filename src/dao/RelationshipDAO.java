package dao;
import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import model.Relationship;
import model.RelationshipStatus;
import repository.Repository;

public class RelationshipDAO implements Repository<Relationship> {

    @Override
    public void save(Relationship rel) {
        String sql = "INSERT INTO relationship (link_id, source_id, target_id, label, status, amount) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, rel.getId());
            ps.setString(2, rel.getSourceId());
            ps.setString(3, rel.getTargetId());
            ps.setString(4, rel.getLabel());
            ps.setString(5, rel.getStatus().name());
            if (rel.getAmount() == null) {
                ps.setNull(6, Types.DOUBLE);
            } else {
                ps.setDouble(6, rel.getAmount());
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save relationship: " + e.getMessage(), e);
        }
    }

    @Override
    public Relationship findById(String id) {
        if (id == null) {
            return null;
        }
        String sql = "SELECT link_id, source_id, target_id, label, status, amount FROM relationship WHERE link_id = ?";

        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find relationship: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Relationship> findAll() {
        List<Relationship> list = new ArrayList<>();
        String sql = "SELECT link_id, source_id, target_id, label, status, amount FROM relationship";

        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load relationships: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Relationship rel) {
        String sql = "UPDATE relationship SET source_id = ?, target_id = ?, label = ?, status = ?, amount = ? WHERE link_id = ?";

        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, rel.getSourceId());
            ps.setString(2, rel.getTargetId());
            ps.setString(3, rel.getLabel());
            ps.setString(4, rel.getStatus().name());
            if (rel.getAmount() == null) {
                ps.setNull(5, Types.DOUBLE);
            } else {
                ps.setDouble(5, rel.getAmount());
            }
            ps.setString(6, rel.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update relationship: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM relationship WHERE link_id = ?";

        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete relationship: " + e.getMessage(), e);
        }
    }

    public void updateStatus(Connection conn, String linkId, RelationshipStatus status) throws SQLException {
        String sql = "UPDATE relationship SET status = ? WHERE link_id = ? AND status = 'SUGGESTED'";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, linkId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Relationship not found or not SUGGESTED: " + linkId);
            }
        }
    }

    public int findMaxLinkNumber() {
        int max = 0;
        String sql = "SELECT link_id FROM relationship";

        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String linkId = rs.getString("link_id");
                if (linkId != null && linkId.startsWith("LNK-")) {
                    try {
                        int num = Integer.parseInt(linkId.substring(4));
                        if (num > max) {
                            max = num;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            return max;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get max link number: " + e.getMessage(), e);
        }
    }

    private Relationship mapRow(ResultSet rs) throws SQLException {
        String linkId = rs.getString("link_id");
        String sourceId = rs.getString("source_id");
        String targetId = rs.getString("target_id");
        String label = rs.getString("label");
        RelationshipStatus status = RelationshipStatus.valueOf(rs.getString("status"));
        double rawAmount = rs.getDouble("amount");
        Double amount = rs.wasNull() ? null : rawAmount;

        return new Relationship(linkId, sourceId, targetId, label, status, amount);
    }
}