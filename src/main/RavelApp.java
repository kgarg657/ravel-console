package main;

import exception.InvalidEntityException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import model.Employee;
import model.Entity;
import model.Invoice;
import model.Relationship;
import model.Vendor;
import rules.RiskRule;
import rules.SharedGstRule;
import util.ConsoleInput;

public class RavelApp {

    public static void main(String[] args) {
        ConsoleInput input = new ConsoleInput();
        List<Entity> entities = new ArrayList<>();
        RiskRule gstRule = new SharedGstRule();

        while (true) {
            System.out.println("\n=== RAVEL FORENSIC AUDIT (M1) ===");
            System.out.println("1. Register Entity");
            System.out.println("2. List Entities");
            System.out.println("3. Run Shared GST Rule");
            System.out.println("0. Exit");

            int choice = input.readInt("Choose option: ", 0, 3);

            if (choice == 0) {
                System.out.println("Exiting Ravel.");
                break;
            } else if (choice == 1) {
                registerEntity(entities, input);
            } else if (choice == 2) {
                listEntities(entities);
            } else if (choice == 3) {
                runGstDetection(entities, gstRule);
            }
        }
    }

    private static void registerEntity(List<Entity> entities, ConsoleInput input) {
        System.out.println("\nEntity Type: 1. Employee | 2. Vendor | 3. Invoice");
        int type = input.readInt("Select type (1-3): ", 1, 3);

        try {
            if (type == 1) {
                String id = input.readNonBlank("Employee ID (e.g. EMP-001): ");
                String name = input.readNonBlank("Name: ");
                String dept = input.readNonBlank("Department: ");
                String role = input.readNonBlank("Role: ");
                boolean hasGst = input.readYesNo("Does employee have a GST number?");
                String gst = hasGst ? input.readNonBlank("GST (15 chars): ") : null;
                String phone = input.readNonBlank("Phone (10 digits): ");
                String bank = input.readNonBlank("Bank Account: ");

                entities.add(new Employee(id, name, 0, dept, role, gst, phone, bank));
                System.out.println("Registered Employee: " + id);

            } else if (type == 2) {
                String id = input.readNonBlank("Vendor ID (e.g. VEN-004): ");
                String name = input.readNonBlank("Vendor Name: ");
                String category = input.readNonBlank("Category: ");
                String gst = input.readNonBlank("GST (15 chars): ");
                String phone = input.readNonBlank("Phone (10 digits): ");
                String bank = input.readNonBlank("Bank Account: ");

                entities.add(new Vendor(id, name, 0, category, gst, phone, bank));
                System.out.println("Registered Vendor: " + id);

            } else {
                String id = input.readNonBlank("Invoice ID (e.g. INV-101): ");
                String name = input.readNonBlank("Description: ");
                double amount = input.readDouble("Amount: ");
                String approverId = input.readNonBlank("Approver Employee ID: ");
                String vendorId = input.readNonBlank("Vendor ID: ");
                String date = input.readNonBlank("Date (YYYY-MM-DD): ");

                entities.add(new Invoice(id, name, 0, amount, approverId, vendorId, LocalDate.parse(date)));
                System.out.println("Registered Invoice: " + id);
            }
        } catch (InvalidEntityException e) {
            System.out.println("Validation Error: " + e.getMessage());
        } catch (DateTimeParseException e) {
            System.out.println("Validation Error: Invalid date format. Use YYYY-MM-DD.");
        }
    }

    private static void listEntities(List<Entity> entities) {
        System.out.println("\n--- Registered Entities ---");
        if (entities.isEmpty()) {
            System.out.println("No entities registered yet.");
            return;
        }
        for (Entity e : entities) {
            System.out.println(e);
        }
    }

    private static void runGstDetection(List<Entity> entities, RiskRule gstRule) {
        System.out.println("\n--- Running Rule: " + gstRule.getRuleName() + " ---");
        List<Relationship> matches = gstRule.detect(entities);

        if (matches.isEmpty()) {
            System.out.println("No shared GST matches found.");
            return;
        }
        for (Relationship link : matches) {
            System.out.println(
                link.getSourceId() + " <-> " + link.getTargetId()
                + " [" + link.getLabel() + " | " + link.getStatus() + "]"
            );
        }
    }
}