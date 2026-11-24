package ui;

import concurrency.ConcurrencyDemo;
import concurrency.QueueDemo;
import exception.CaseAlreadyActiveException;
import exception.InvalidEntityException;
import java.util.ArrayList;
import java.util.List;
import model.AuditCase;
import model.CaseStatus;
import model.FraudType;
import service.CaseService;
import service.DetectionService;
import service.EntityService;
import util.ConsoleInput;

public class CaseMenu {
    private final CaseService caseService;
    private final ConsoleInput consoleInput;
    private EntityService entityService;
    private DetectionService detectionService;

    public CaseMenu(CaseService caseService, ConsoleInput consoleInput) {
        this.caseService = caseService;
        this.consoleInput = consoleInput;
    }

    public CaseMenu(
            CaseService caseService,
            ConsoleInput consoleInput,
            EntityService entityService,
            DetectionService detectionService) {
        this.caseService = caseService;
        this.consoleInput = consoleInput;
        this.entityService = entityService;
        this.detectionService = detectionService;
    }

    public void setQueueDemoDependencies(EntityService entityService, DetectionService detectionService) {
        this.entityService = entityService;
        this.detectionService = detectionService;
    }

    public void show() {
        while (true) {
            System.out.println("\n--- Case & Concurrency Menu ---");
            System.out.println("7. Audit Cases (List, Open, View, Change Status)");
            System.out.println("9. Concurrency Demo");
            System.out.println("10. Queue Demo (load demo dataset via option 11 first)");
            System.out.println("0. Back");

            int choice = consoleInput.readInt("Choose an option: ", 0, 10);
            switch (choice) {
                case 7 -> caseSubMenu();
                case 9 -> ConcurrencyDemo.run();
                case 10 -> runQueueDemo();
                case 0 -> {
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void runQueueDemo() {
        if (entityService == null || detectionService == null) {
            System.out.println("[Error] Queue Demo services are not configured.");
            return;
        }
        new QueueDemo(entityService, detectionService).run();
    }

    private void caseSubMenu() {
        while (true) {
            System.out.println("\n--- Audit Cases ---");
            System.out.println("1. List Cases");
            System.out.println("2. Open Case");
            System.out.println("3. View Case by ID");
            System.out.println("4. Change Case Status");
            System.out.println("0. Back");

            int choice = consoleInput.readInt("Choose an option: ", 0, 4);
            try {
                switch (choice) {
                    case 1 -> listCases();
                    case 2 -> openCase();
                    case 3 -> viewCase();
                    case 4 -> changeStatus();
                    case 0 -> {
                        return;
                    }
                    default -> System.out.println("Invalid option.");
                }
            } catch (CaseAlreadyActiveException | InvalidEntityException e) {
                System.out.println("[Error] " + e.getMessage());
            }
        }
    }

    private void listCases() {
        List<AuditCase> cases = caseService.findAll();
        if (cases.isEmpty()) {
            System.out.println("No audit cases found.");
            return;
        }
        for (AuditCase auditCase : cases) {
            System.out.println(auditCase);
        }
    }

    private void openCase() {
        String title = consoleInput.readNonBlank("Case title: ");
        FraudType[] fraudTypes = FraudType.values();
        for (int i = 0; i < fraudTypes.length; i++) {
            System.out.println((i + 1) + ". " + fraudTypes[i]);
        }
        FraudType fraudType = fraudTypes[consoleInput.readInt("Select fraud type: ", 1, fraudTypes.length) - 1];

        int count = consoleInput.readInt("How many entity IDs to include: ", 1, 50);
        List<String> entityIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            entityIds.add(consoleInput.readNonBlank("Entity ID " + (i + 1) + ": "));
        }

        AuditCase created = caseService.openCase(title, fraudType, entityIds);
        System.out.println("Opened case: " + created);
    }

    private void viewCase() {
        String caseId = consoleInput.readNonBlank("Case ID: ");
        AuditCase found = caseService.findById(caseId);
        if (found == null) {
            System.out.println("Case not found: " + caseId.trim().toUpperCase());
        } else {
            System.out.println(found);
        }
    }

    private void changeStatus() {
        String caseId = consoleInput.readNonBlank("Case ID: ");
        CaseStatus[] statuses = CaseStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.println((i + 1) + ". " + statuses[i]);
        }
        CaseStatus nextStatus = statuses[consoleInput.readInt("New status: ", 1, statuses.length) - 1];

        AuditCase updated = caseService.changeStatus(caseId, nextStatus);
        System.out.println("Updated case: " + updated);
    }
}