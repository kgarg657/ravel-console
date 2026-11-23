package ui;

import exception.DuplicateRecordException;
import exception.InvalidEntityException;
import java.util.List;
import java.util.Map;
import model.Relationship;
import model.RelationshipStatus;
import service.RelationshipService;
import util.ConsoleInput;

public class RelationshipMenu {
    private final RelationshipService relationshipService;
    private final ConsoleInput input;

    public RelationshipMenu(RelationshipService relationshipService, ConsoleInput input) {
        this.relationshipService = relationshipService;
        this.input = input;
    }

    public void show() {
        while (true) {
            System.out.println("\n--- Relationship Menu ---");
            System.out.println("1. List All Relationships");
            System.out.println("2. Filter Relationships by Status");
            System.out.println("3. Find Relationship by ID");
            System.out.println("4. Record Vendor Payment");
            System.out.println("5. Approve Suggested Relationship");
            System.out.println("6. Reject Suggested Relationship");
            System.out.println("7. Relationship Status Counts");
            System.out.println("0. Back");

            int choice = input.readInt("Choose an option: ", 0, 7);
            try {
                switch (choice) {
                    case 1:
                        listAll();
                        break;
                    case 2:
                        filterByStatus();
                        break;
                    case 3:
                        findById();
                        break;
                    case 4:
                        recordPayment();
                        break;
                    case 5:
                        approveLink();
                        break;
                    case 6:
                        rejectLink();
                        break;
                    case 7:
                        showStatusCounts();
                        break;
                    case 0:
                        return;
                    default:
                        break;
                }
            } catch (InvalidEntityException | DuplicateRecordException ex) {
                System.out.println("[Error] " + ex.getMessage());
            }
        }
    }

    private void listAll() {
        List<Relationship> all = relationshipService.findAll();
        if (all.isEmpty()) {
            System.out.println("No relationships found.");
            return;
        }
        for (Relationship r : all) {
            printRelationship(r);
        }
    }

    private void filterByStatus() {
        RelationshipStatus status = promptStatus();
        List<Relationship> matches = relationshipService.findByStatus(status);
        if (matches.isEmpty()) {
            System.out.println("No relationships found with status: " + status);
            return;
        }
        for (Relationship r : matches) {
            printRelationship(r);
        }
    }

    private void findById() {
        String linkId = input.readNonBlank("Enter Link ID: ");
        Relationship found = relationshipService.findById(linkId);
        if (found == null) {
            System.out.println("No relationship found with ID: " + linkId);
            return;
        }
        printRelationship(found);
    }

    private void recordPayment() {
        String srcVendorId = input.readNonBlank("Source Vendor ID (e.g. VEN-001): ");
        String tgtVendorId = input.readNonBlank("Target Vendor ID (e.g. VEN-002): ");
        double amount = input.readDouble("Payment Amount: ");

        relationshipService.recordPayment(srcVendorId, tgtVendorId, amount);
        System.out.println("Payment recorded between " + srcVendorId + " and " + tgtVendorId + ".");
    }

    private void approveLink() {
        String linkId = input.readNonBlank("Enter Suggested Link ID to approve: ");
        relationshipService.approve(linkId);
        System.out.println("Relationship approved: " + linkId);
    }

    private void rejectLink() {
        String linkId = input.readNonBlank("Enter Suggested Link ID to reject: ");
        relationshipService.reject(linkId);
        System.out.println("Relationship rejected: " + linkId);
    }

    private void showStatusCounts() {
        System.out.println("\n--- Counts by Relationship Status ---");
        Map<RelationshipStatus, Integer> counts = relationshipService.countByStatus();
        for (RelationshipStatus status : RelationshipStatus.values()) {
            System.out.println(status + ": " + counts.get(status));
        }
    }

    private void printRelationship(Relationship r) {
        System.out.println(
                r.getId()
                        + " | " + r.getSourceId()
                        + " -> " + r.getTargetId()
                        + " | Status: " + r.getStatus());
    }

    private RelationshipStatus promptStatus() {
        RelationshipStatus[] values = RelationshipStatus.values();
        for (int i = 0; i < values.length; i++) {
            System.out.println((i + 1) + ". " + values[i]);
        }
        int idx = input.readInt("Choose status: ", 1, values.length);
        return values[idx - 1];
    }
}