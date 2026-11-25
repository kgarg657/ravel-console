package main;

import dao.AuditLogDAO;
import dao.CaseDAO;
import dao.EntityDAO;
import dao.RelationshipDAO;
import database.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;
import rules.SharedBankRule;
import rules.SharedGstRule;
import rules.SharedPhoneRule;
import service.CaseService;
import service.DetectionService;
import service.EntityService;
import service.GraphService;
import service.RelationshipService;
import ui.CaseMenu;
import ui.EntityMenu;
import ui.GraphMenu;
import ui.RelationshipMenu;
import util.ConsoleInput;
import util.IdGenerator;

public class RavelApp {

    public static void main(String[] args) {
        EntityDAO entityDAO = new EntityDAO();
        CaseDAO caseDAO = new CaseDAO();
        RelationshipDAO relationshipDAO = new RelationshipDAO();
        AuditLogDAO auditLogDAO = new AuditLogDAO();
        IdGenerator idGenerator = new IdGenerator();

        if (!verifyDatabaseConnection()) {
            System.out.println(
                    "Warning: Could not connect to MySQL or load config/db.properties. Check your database setup before running DB operations.");
        } else {
            try {
                idGenerator.initLinkCounter(relationshipDAO.findMaxLinkNumber());
                idGenerator.initCaseCounter(caseDAO.findMaxCaseNumber());
            } catch (RuntimeException e) {
                System.out.println("Startup notice: Unable to initialize counters (" + e.getMessage() + ").");
            }
        }

        EntityService entityService = new EntityService(entityDAO, caseDAO);
        CaseService caseService = new CaseService(caseDAO, entityDAO);
        RelationshipService relationshipService = new RelationshipService(
                relationshipDAO, entityDAO, auditLogDAO, idGenerator);
        DetectionService detectionService = new DetectionService(relationshipService, idGenerator);
        GraphService graphService = new GraphService(relationshipService);

        detectionService.addRule(new SharedBankRule());
        detectionService.addRule(new SharedGstRule());
        detectionService.addRule(new SharedPhoneRule());

        runMenu(entityService, caseService, relationshipService, detectionService, graphService, idGenerator);
    }

    private static boolean verifyDatabaseConnection() {
        try (Connection conn = DBConnection.get()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.out.println("Database connection unavailable: " + e.getMessage());
            return false;
        }
    }

    private static void runMenu(
            EntityService entityService,
            CaseService caseService,
            RelationshipService relationshipService,
            DetectionService detectionService,
            GraphService graphService,
            IdGenerator idGenerator) {
        ConsoleInput consoleInput = new ConsoleInput();
        EntityMenu entityMenu = new EntityMenu(entityService, relationshipService, consoleInput);
        RelationshipMenu relationshipMenu = new RelationshipMenu(relationshipService, consoleInput);
        GraphMenu graphMenu = new GraphMenu(
                detectionService, graphService, relationshipService, entityService, consoleInput);
        CaseMenu caseMenu = new CaseMenu(caseService, consoleInput, idGenerator);
        caseMenu.setQueueDemoDependencies(entityService, detectionService);

        while (true) {
            System.out.println("\n=== RAVEL FORENSIC CONSOLE ===");
            System.out.println("1. Entity Management");
            System.out.println("2. Relationship Management");
            System.out.println("3. Detection & Graph Analysis (BFS / DFS / Payments)");
            System.out.println("4. Case Management & Concurrency / Queue Demos");
            System.out.println("0. Exit");

            int choice = consoleInput.readInt("Choose an option: ", 0, 4);
            try {
                switch (choice) {
                    case 1 -> entityMenu.show();
                    case 2 -> relationshipMenu.show();
                    case 3 -> graphMenu.show();
                    case 4 -> caseMenu.show();
                    case 0 -> {
                        System.out.println("Exiting Ravel Forensic Console.");
                        return;
                    }
                    default -> System.out.println("Invalid option.");
                }
            } catch (RuntimeException e) {
                System.out.println("Operation error: " + e.getMessage());
            }
        }
    }
}