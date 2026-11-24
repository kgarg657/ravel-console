package dao;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Employee;
import model.Entity;
import model.Invoice;
import model.Vendor;
import repository.Repository;

public class EntityDAO implements Repository<Entity> {

    private static final String INSERT_SQL = """
            INSERT INTO entity (
                entity_id, entity_type, name, risk_score,
                department, role_title, category, gst_number,
                phone, bank_account, amount, approver_id,
                vendor_id, invoice_date
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SELECT_BY_ID_SQL = """
            SELECT entity_id, entity_type, name, risk_score,
                   department, role_title, category, gst_number,
                   phone, bank_account, amount, approver_id,
                   vendor_id, invoice_date
            FROM entity
            WHERE entity_id = ?
            """;

    private static final String SELECT_ALL_SQL = """
            SELECT entity_id, entity_type, name, risk_score,
                   department, role_title, category, gst_number,
                   phone, bank_account, amount, approver_id,
                   vendor_id, invoice_date
            FROM entity
            """;

    private static final String UPDATE_SQL = """
            UPDATE entity
            SET entity_type = ?, name = ?, risk_score = ?,
                department = ?, role_title = ?, category = ?,
                gst_number = ?, phone = ?, bank_account = ?,
                amount = ?, approver_id = ?, vendor_id = ?,
                invoice_date = ?
            WHERE entity_id = ?
            """;

    private static final String DELETE_BY_ID_SQL = """
            DELETE FROM entity
            WHERE entity_id = ?
            """;

    private static final String ADD_RISK_SQL = """
            UPDATE entity
            SET risk_score = LEAST(100, risk_score + ?)
            WHERE entity_id = ?
            """;

    @Override
    public void save(Entity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null.");
        }
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            bindEntityParameters(ps, entity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save entity: " + entity.getId(), e);
        }
    }

    @Override
    public Entity findById(String id) {
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToEntity(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find entity by id: " + id, e);
        }
    }

    @Override
    public List<Entity> findAll() {
        List<Entity> entities = new ArrayList<>();
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                entities.add(mapRowToEntity(rs));
            }
            return entities;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load all entities", e);
        }
    }

    @Override
    public void update(Entity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null.");
        }
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, resolveEntityType(entity));
            ps.setString(2, entity.getName());
            ps.setInt(3, entity.getRiskScore());
            bindSubtypeColumns(ps, entity, 4);
            ps.setString(14, entity.getId());

            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new IllegalStateException("No entity found to update with id: " + entity.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update entity: " + entity.getId(), e);
        }
    }

    @Override
    public void delete(String id) {
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(DELETE_BY_ID_SQL)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete entity: " + id, e);
        }
    }

    public void addRisk(Connection conn, String id, int delta) {
        if (conn == null) {
            throw new IllegalArgumentException("Connection cannot be null.");
        }
        try (PreparedStatement ps = conn.prepareStatement(ADD_RISK_SQL)) {
            ps.setInt(1, delta);
            ps.setString(2, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new IllegalStateException("No entity row updated for id: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add risk (" + delta + ") to entity: " + id, e);
        }
    }

    private void bindEntityParameters(PreparedStatement ps, Entity entity) throws SQLException {
        ps.setString(1, entity.getId());
        ps.setString(2, resolveEntityType(entity));
        ps.setString(3, entity.getName());
        ps.setInt(4, entity.getRiskScore());
        bindSubtypeColumns(ps, entity, 5);
    }

    private void bindSubtypeColumns(PreparedStatement ps, Entity entity, int startIndex) throws SQLException {
        String department = null;
        String roleTitle = null;
        String category = null;
        String gstNumber = null;
        String phone = null;
        String bankAccount = null;
        Double amount = null;
        String approverId = null;
        String vendorId = null;
        String invoiceDate = null;

        if (entity instanceof Employee emp) {
            department = emp.getDepartment();
            roleTitle = emp.getRole();
            gstNumber = emp.getGst();
            phone = emp.getPhone();
            bankAccount = emp.getBankAccount();
        } else if (entity instanceof Vendor vendor) {
            category = vendor.getCategory();
            gstNumber = vendor.getGst();
            phone = vendor.getPhone();
            bankAccount = vendor.getBankAccount();
        } else if (entity instanceof Invoice invoice) {
            amount = invoice.getAmount();
            approverId = invoice.getApproverId();
            vendorId = invoice.getVendorId();
            LocalDate date = invoice.getInvoiceDate();
            invoiceDate = date != null ? date.toString() : null;
        }

        ps.setString(startIndex, department);
        ps.setString(startIndex + 1, roleTitle);
        ps.setString(startIndex + 2, category);
        ps.setString(startIndex + 3, gstNumber);
        ps.setString(startIndex + 4, phone);
        ps.setString(startIndex + 5, bankAccount);
        if (amount != null) {
            ps.setDouble(startIndex + 6, amount);
        } else {
            ps.setNull(startIndex + 6, Types.DOUBLE);
        }
        ps.setString(startIndex + 7, approverId);
        ps.setString(startIndex + 8, vendorId);
        ps.setString(startIndex + 9, invoiceDate);
    }

    private String resolveEntityType(Entity entity) {
        if (entity instanceof Employee) {
            return "EMPLOYEE";
        }
        if (entity instanceof Vendor) {
            return "VENDOR";
        }
        if (entity instanceof Invoice) {
            return "INVOICE";
        }
        throw new IllegalArgumentException("Unsupported entity subtype: " + entity.getClass().getName());
    }

    private Entity mapRowToEntity(ResultSet rs) throws SQLException {
        String type = rs.getString("entity_type");
        String id = rs.getString("entity_id");
        String name = rs.getString("name");
        int riskScore = rs.getInt("risk_score");

        return switch (type) {
            case "EMPLOYEE" -> new Employee(
                    id,
                    name,
                    riskScore,
                    rs.getString("department"),
                    rs.getString("role_title"),
                    rs.getString("gst_number"),
                    rs.getString("phone"),
                    rs.getString("bank_account"));
            case "VENDOR" -> new Vendor(
                    id,
                    name,
                    riskScore,
                    rs.getString("category"),
                    rs.getString("gst_number"),
                    rs.getString("phone"),
                    rs.getString("bank_account"));
            case "INVOICE" -> {
                double amt = rs.getDouble("amount");
                String dateStr = rs.getString("invoice_date");
                LocalDate parsedDate = (dateStr != null && !dateStr.isBlank())
                        ? LocalDate.parse(dateStr)
                        : null;
                yield new Invoice(
                        id,
                        name,
                        riskScore,
                        amt,
                        rs.getString("approver_id"),
                        rs.getString("vendor_id"),
                        parsedDate);
            }
            default -> throw new IllegalStateException("Unknown entity_type in DB: " + type);
        };
    }
}