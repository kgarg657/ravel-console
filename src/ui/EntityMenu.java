package ui;

import exception.DuplicateRecordException;
import exception.InvalidEntityException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.Employee;
import model.Entity;
import model.EntityType;
import model.Invoice;
import model.RelationshipStatus;
import model.Vendor;
import service.EntityService;
import service.RelationshipService;
import util.ConsoleInput;

public class EntityMenu {
    private final EntityService entityService;
    private final RelationshipService relationshipService;
    private final ConsoleInput input;

    public EntityMenu(
            EntityService entityService,
            RelationshipService relationshipService,
            ConsoleInput input) {
        this.entityService = entityService;
        this.relationshipService = relationshipService;
        this.input = input;
    }

    public void show() {
        while (true) {
            System.out.println("\n--- Entity Menu ---");
            System.out.println("1. Entity CRUD (Register, Search, Update, Delete, List, Filter)");
            System.out.println("2. Relationships (coming in M3/M4)");
            System.out.println("3. Fraud & Risk Rules (coming in M3/M4)");
            System.out.println("4. Graph Analysis (coming in M3/M4)");
            System.out.println("5. Audit Cases (coming in M3/M4)");
            System.out.println("6. Export / Reports (coming in M3/M4)");
            System.out.println("7. Settings / Info (coming in M3/M4)");
            System.out.println("8. Top 5 Risk Entities & Counts");
            System.out.println("9. Concurrency / Batch (coming in M3/M4)");
            System.out.println("10. Persistence / Storage (coming in M3/M4)");
            System.out.println("11. Load Demo Data");
            System.out.println("0. Back");

            int choice = input.readInt("Choose an option: ", 0, 11);
            try {
                switch (choice) {
                    case 1 -> showCrudSubMenu();
                    case 8 -> showSummary();
                    case 11 -> loadDemoData();
                    case 0 -> {
                        return;
                    }
                    case 2, 3, 4, 5, 6, 7, 9, 10 -> System.out.println("coming in M3/M4");
                    default -> {
                    }
                }
            } catch (InvalidEntityException | DuplicateRecordException ex) {
                System.out.println("[Error] " + ex.getMessage());
            } catch (RuntimeException ex) {
                System.out.println("[Error] " + ex.getMessage());
            }
        }
    }

    private void showCrudSubMenu() {
        while (true) {
            System.out.println("\n--- Entity CRUD ---");
            System.out.println("1. Register Employee");
            System.out.println("2. Register Vendor");
            System.out.println("3. Register Invoice");
            System.out.println("4. Search Entity by ID");
            System.out.println("5. Update Entity");
            System.out.println("6. Delete Entity");
            System.out.println("7. List All Entities");
            System.out.println("8. Filter Entities by Type");
            System.out.println("0. Back");

            int subChoice = input.readInt("Choose an action: ", 0, 8);
            try {
                switch (subChoice) {
                    case 1 -> registerEmployee();
                    case 2 -> registerVendor();
                    case 3 -> registerInvoice();
                    case 4 -> findEntityById();
                    case 5 -> updateEntity();
                    case 6 -> deleteEntity();
                    case 7 -> listAllEntities();
                    case 8 -> filterByType();
                    case 0 -> {
                        return;
                    }
                    default -> {
                    }
                }
            } catch (InvalidEntityException | DuplicateRecordException ex) {
                System.out.println("[Error] " + ex.getMessage());
            } catch (RuntimeException ex) {
                System.out.println("[Error] " + ex.getMessage());
            }
        }
    }

    private void registerEmployee() {
        String id = input.readNonBlank("Employee ID (e.g. EMP-001): ");
        String name = input.readNonBlank("Name: ");
        int riskScore = input.readInt("Risk Score (0-100): ", 0, 100);
        String department = input.readNonBlank("Department: ");
        String role = input.readNonBlank("Role: ");
        String rawGst = input.readNonBlank("GST (or '-' if none): ");
        String gst = parseOptionalGst(rawGst);
        String phone = input.readNonBlank("Phone: ");
        String bankAccount = input.readNonBlank("Bank Account: ");

        Employee emp = new Employee(
                id, name, riskScore, department, role, gst, phone, bankAccount);
        entityService.registerEntity(emp);
        System.out.println("Employee registered: " + emp.getId());
    }

    private String parseOptionalGst(String rawGst) {
        if (rawGst == null) {
            return null;
        }
        String trimmed = rawGst.trim();
        if (trimmed.isEmpty()
                || trimmed.equals("-")
                || trimmed.equalsIgnoreCase("N/A")
                || trimmed.equalsIgnoreCase("NONE")) {
            return null;
        }
        return trimmed;
    }

    private void registerVendor() {
        String id = input.readNonBlank("Vendor ID (e.g. VEN-001): ");
        String name = input.readNonBlank("Name: ");
        int riskScore = input.readInt("Risk Score (0-100): ", 0, 100);
        String category = input.readNonBlank("Category: ");
        String gst = input.readNonBlank("GST: ");
        String phone = input.readNonBlank("Phone: ");
        String bankAccount = input.readNonBlank("Bank Account: ");

        Vendor vendor = new Vendor(
                id, name, riskScore, category, gst, phone, bankAccount);
        entityService.registerEntity(vendor);
        System.out.println("Vendor registered: " + vendor.getId());
    }

    private void registerInvoice() {
        String id = input.readNonBlank("Invoice ID (e.g. INV-101): ");
        String name = input.readNonBlank("Invoice Title / Name: ");
        int riskScore = input.readInt("Risk Score (0-100): ", 0, 100);
        double amount = input.readDouble("Amount: ");
        String approverId = input.readNonBlank("Approver Employee ID (e.g. EMP-001): ");
        String vendorId = input.readNonBlank("Vendor ID (e.g. VEN-001): ");
        LocalDate invoiceDate = readLocalDate("Invoice Date (YYYY-MM-DD): ");

        Invoice inv = new Invoice(
                id, name, riskScore, amount, approverId, vendorId, invoiceDate);
        entityService.registerEntity(inv);
        System.out.println("Invoice registered: " + inv.getId());
    }

    private void findEntityById() {
        String id = input.readNonBlank("Enter Entity ID: ");
        Entity found = findByIdFromAll(id);
        if (found == null) {
            System.out.println("No entity found with ID: " + id);
            return;
        }
        System.out.println(found.describe());
    }

    private void listAllEntities() {
        List<Entity> all = entityService.getAllEntities();
        if (all.isEmpty()) {
            System.out.println("No entities found.");
            return;
        }
        for (Entity e : all) {
            System.out.println(e.describe());
            System.out.println("----------------------------");
        }
    }

    private void filterByType() {
        EntityType type = promptEntityType();
        List<Entity> matches = entityService.getAllEntities().stream()
                .filter(e -> e.getType() == type)
                .collect(Collectors.toList());
        if (matches.isEmpty()) {
            System.out.println("No entities found for type: " + type);
            return;
        }
        for (Entity e : matches) {
            System.out.println(e.describe());
            System.out.println("----------------------------");
        }
    }

    private void updateEntity() {
        String id = input.readNonBlank("Enter Entity ID to update: ");
        Entity existing = findByIdFromAll(id);
        if (existing == null) {
            System.out.println("No entity found with ID: " + id);
            return;
        }

        System.out.println("Loaded entity for review: " + existing.describe());
        System.out.println("Note: Add update(Entity) to EntityService if persisting edits to DB.");
    }

    private void deleteEntity() {
        String id = input.readNonBlank("Enter Entity ID to delete: ");
        entityService.delete(id);
        System.out.println("Entity deleted: " + id);
    }

    private void showSummary() {
        List<Entity> all = entityService.getAllEntities();

        System.out.println("\n--- Top 5 Risk Entities ---");
        List<Entity> top = all.stream()
                .sorted(Comparator.comparingInt(Entity::getRiskScore).reversed())
                .limit(5)
                .collect(Collectors.toList());
        if (top.isEmpty()) {
            System.out.println("No entities available.");
        } else {
            for (int i = 0; i < top.size(); i++) {
                Entity e = top.get(i);
                System.out.println(
                        (i + 1) + ". " + e.getId() + " | " + e.getName()
                                + " | Risk: " + e.getRiskScore()
                                + " (" + e.getRiskTier() + ")");
            }
        }

        System.out.println("\n--- Counts by Entity Type ---");
        Map<EntityType, Integer> typeCounts = new EnumMap<>(EntityType.class);
        for (EntityType type : EntityType.values()) {
            typeCounts.put(type, 0);
        }
        for (Entity e : all) {
            if (e.getType() != null) {
                typeCounts.put(e.getType(), typeCounts.get(e.getType()) + 1);
            }
        }
        for (EntityType type : EntityType.values()) {
            System.out.println(type + ": " + typeCounts.get(type));
        }

        System.out.println("\n--- Counts by Relationship Status ---");
        Map<RelationshipStatus, Integer> statusCounts = relationshipService.countByStatus();
        for (RelationshipStatus status : RelationshipStatus.values()) {
            System.out.println(status + ": " + statusCounts.get(status));
        }
    }

    private void loadDemoData() {
        registerIfMissing(new Employee(
                "EMP-001", "Aarav Sharma", 35,
                "Finance", "Analyst", null, "9876543210", "ACC123456"));
        registerIfMissing(new Employee(
                "EMP-002", "Meera Nair", 25,
                "Audit", "Reviewer", null, "9876543214", "ACC123457"));

        registerIfMissing(new Vendor(
                "VEN-001", "Apex Supplies", 55,
                "Office", "22AAAAA0000A1Z5", "9876543211", "ACC654321"));
        registerIfMissing(new Vendor(
                "VEN-002", "Prime Logistics", 45,
                "Transport", "27BBBBB1111B1Z2", "9876543212", "ACC789012"));
        registerIfMissing(new Vendor(
                "VEN-003", "Crest Tech", 65,
                "IT Services", "29CCCCC2222C1Z9", "9876543215", "ACC890123"));
        registerIfMissing(new Vendor(
                "VEN-004", "Vertex Traders", 50,
                "Supplies", "07DDDDD3333D1Z1", "9876543210", "ACC890124"));

        registerIfMissing(new Invoice(
                "INV-101", "Office Supplies Order", 40,
                12500.0, "EMP-001", "VEN-001", LocalDate.of(2026, 11, 1)));
        registerIfMissing(new Invoice(
                "INV-102", "Fleet Transport Bill", 50,
                18000.0, "EMP-002", "VEN-002", LocalDate.of(2026, 11, 5)));

        recordPaymentIfMissing("VEN-001", "VEN-002", 25000.0);
        recordPaymentIfMissing("VEN-002", "VEN-003", 15000.0);
        recordPaymentIfMissing("VEN-003", "VEN-001", 30000.0);

        System.out.println("Demo data loaded.");
    }

    private Entity findByIdFromAll(String id) {
        if (id == null) {
            return null;
        }
        String target = id.trim();
        for (Entity e : entityService.getAllEntities()) {
            if (target.equalsIgnoreCase(e.getId())) {
                return e;
            }
        }
        return null;
    }

    private void registerIfMissing(Entity e) {
        if (findByIdFromAll(e.getId()) == null) {
            entityService.registerEntity(e);
        }
    }

    private void recordPaymentIfMissing(String srcVendorId, String tgtVendorId, double amount) {
        try {
            relationshipService.recordPayment(srcVendorId, tgtVendorId, amount);
        } catch (DuplicateRecordException | InvalidEntityException ignored) {
            // Skip if already recorded
        }
    }

    private LocalDate readLocalDate(String prompt) {
        while (true) {
            String raw = input.readNonBlank(prompt);
            try {
                return LocalDate.parse(raw);
            } catch (DateTimeParseException ex) {
                System.out.println("[Error] Invalid date format. Use YYYY-MM-DD.");
            }
        }
    }

    private EntityType promptEntityType() {
        EntityType[] values = EntityType.values();
        for (int i = 0; i < values.length; i++) {
            System.out.println((i + 1) + ". " + values[i]);
        }
        int idx = input.readInt("Choose type: ", 1, values.length);
        return values[idx - 1];
    }
}