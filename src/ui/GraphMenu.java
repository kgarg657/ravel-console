package ui;

import exception.DuplicateRecordException;
import exception.InvalidEntityException;
import java.util.List;
import model.DetectionReport;
import model.Relationship;
import model.RelationshipStatus;
import service.DetectionService;
import service.EntityService;
import service.GraphService;
import service.RelationshipService;
import util.ConsoleInput;
import util.GraphPrinter;

public class GraphMenu {
    private final DetectionService detectionService;
    private final GraphService graphService;
    private final RelationshipService relationshipService;
    private final EntityService entityService;
    private final ConsoleInput input;
    private final GraphPrinter printer;

    public GraphMenu(
            DetectionService detectionService,
            GraphService graphService,
            RelationshipService relationshipService,
            EntityService entityService,
            ConsoleInput input) {
        this.detectionService = detectionService;
        this.graphService = graphService;
        this.relationshipService = relationshipService;
        this.entityService = entityService;
        this.input = input;
        this.printer = new GraphPrinter();
    }

    public void show() {
        while (true) {
            System.out.println("\n--- Detection & Graph Analysis Menu ---");
            System.out.println("2. Run Link Detection");
            System.out.println("3. Review Suggested Links (Approve / Reject / Skip)");
            System.out.println("4. Find Connection Path (BFS)");
            System.out.println("5. Detect Circular Loops (DFS)");
            System.out.println("6. Record Vendor Payment");
            System.out.println("0. Back");

            int choice = input.readInt("Choose an option: ", 0, 6);

            try {
                switch (choice) {
                    case 2:
                        handleRunDetection();
                        break;
                    case 3:
                        handleReviewSuggestedLinks();
                        break;
                    case 4:
                        handleFindPath();
                        break;
                    case 5:
                        handleDetectLoops();
                        break;
                    case 6:
                        handleRecordPayment();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Choose 2-6 or 0 to go back.");
                        break;
                }
            } catch (InvalidEntityException | DuplicateRecordException ex) {
                System.out.println("[Error] " + ex.getMessage());
            }
        }
    }

    private void handleRunDetection() {
        DetectionReport report = detectionService.runAll(entityService.getAllEntities());
        System.out.println("\n" + printer.formatDetectionReport(report));
        if (!report.getNewLinks().isEmpty()) {
            System.out.println("Newly Flagged Links:");
            System.out.println(printer.formatRelationshipList(report.getNewLinks(), entityService));
        }
    }

    private void handleReviewSuggestedLinks() {
        List<Relationship> suggested = relationshipService.findByStatus(RelationshipStatus.SUGGESTED);
        if (suggested.isEmpty()) {
            System.out.println("No SUGGESTED links pending review.");
            return;
        }

        System.out.println("\n--- Suggested Links ---");
        System.out.println(printer.formatRelationshipList(suggested, entityService));

        String linkId = input.readNonBlank("Enter Suggested Link ID (or 'skip'): ");
        if ("skip".equalsIgnoreCase(linkId)) {
            return;
        }

        int action = input.readInt("1. Approve  |  2. Reject  |  3. Skip: ", 1, 3);
        if (action == 1) {
            relationshipService.approve(linkId);
            System.out.println("Link approved: " + linkId + " (+15 risk to both entities).");
        } else if (action == 2) {
            relationshipService.reject(linkId);
            System.out.println("Link rejected: " + linkId);
        }
    }

    private void handleFindPath() {
        String startId = input.readNonBlank("Enter Start Entity ID (e.g. EMP-001): ");
        String endId = input.readNonBlank("Enter Target Entity ID (e.g. VEN-004): ");

        List<String> path = graphService.findShortestPath(startId, endId);
        List<Relationship> suggestedBetween = graphService.findSuggestedLinksBetween(startId, endId);

        System.out.println("\n" + printer.formatPath(path, suggestedBetween, entityService));
    }

    private void handleDetectLoops() {
        List<List<Relationship>> loops = graphService.findCircularLoops();
        System.out.println("\n" + printer.formatLoops(loops, entityService));
    }

    private void handleRecordPayment() {
        String srcVendorId = input.readNonBlank("Source Vendor ID (e.g. VEN-001): ");
        String tgtVendorId = input.readNonBlank("Target Vendor ID (e.g. VEN-002): ");
        double amount = input.readDouble("Payment Amount: ");

        relationshipService.recordPayment(srcVendorId, tgtVendorId, amount);
        System.out.println("Payment recorded: " + srcVendorId + " -> " + tgtVendorId + " (" + amount + ")");
    }
}