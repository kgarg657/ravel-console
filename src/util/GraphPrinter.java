package util;

import java.util.List;
import java.util.Map;
import model.DetectionReport;
import model.Entity;
import model.Relationship;
import model.RelationshipStatus;
import service.EntityService;

public class GraphPrinter {
    public static final String SOLID_ARROW = "----->";
    public static final String DASHED_ARROW = "- - ->";

    public String formatDetectionReport(DetectionReport report) {
        if (report == null) {
            return "No detection report available.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== Fraud Detection Report ===\n");
        sb.append("Entities Scanned: ").append(report.getEntitiesScanned()).append("\n");
        sb.append("New Links Saved : ").append(report.getNewLinks().size()).append("\n");

        Map<String, Integer> counts = report.getCountsByRule();
        if (counts.isEmpty()) {
            sb.append("Rules Executed  : None\n");
        } else {
            sb.append("Breakdown by Rule:\n");
            for (String ruleName : counts.keySet()) {
                sb.append("  - ").append(ruleName)
                        .append(": ").append(counts.get(ruleName)).append("\n");
            }
        }
        return sb.toString();
    }

    public String formatLink(Relationship r, EntityService entityService) {
        if (r == null) {
            return "";
        }

        String arrow = SOLID_ARROW;
        if (r.getStatus() == RelationshipStatus.SUGGESTED) {
            arrow = DASHED_ARROW;
        }

        String srcLabel = formatNodeLabel(r.getSourceId(), entityService);
        String tgtLabel = formatNodeLabel(r.getTargetId(), entityService);

        String idPrefix = "";
        if (r.getId() != null) {
            idPrefix = "[" + r.getId() + "] ";
        }

        String amountStr = "";
        if (r.getAmount() != null) {
            amountStr = " (Amount: " + r.getAmount() + ")";
        }

        return idPrefix + srcLabel + " " + arrow + " " + tgtLabel
                + " | " + r.getStatus() + ": " + r.getLabel() + amountStr;
    }

    public String formatRelationshipList(List<Relationship> links, EntityService entityService) {
        if (links == null || links.isEmpty()) {
            return "No relationships to display.";
        }

        StringBuilder sb = new StringBuilder();
        for (Relationship r : links) {
            if (r != null) {
                sb.append(formatLink(r, entityService)).append("\n");
            }
        }
        return sb.toString();
    }

    public String formatPath(List<String> path, List<Relationship> suggestedBetween, EntityService entityService) {
        StringBuilder sb = new StringBuilder();
        if (path == null || path.isEmpty()) {
            sb.append("No approved path found between the specified entities.\n");
        } else {
            sb.append("Shortest APPROVED Path (").append(path.size() - 1).append(" hops):\n  ");
            for (int i = 0; i < path.size(); i++) {
                sb.append(formatNodeLabel(path.get(i), entityService));
                if (i < path.size() - 1) {
                    sb.append(" ").append(SOLID_ARROW).append(" ");
                }
            }
            sb.append("\n");
        }

        if (suggestedBetween != null && !suggestedBetween.isEmpty()) {
            sb.append("\nUnverified SUGGESTED links between these entities:\n");
            for (Relationship s : suggestedBetween) {
                sb.append("  ").append(formatLink(s, entityService)).append("\n");
            }
        }
        return sb.toString();
    }

    public String formatLoops(List<List<Relationship>> loops, EntityService entityService) {
        if (loops == null || loops.isEmpty()) {
            return "No circular payment loops detected.";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < loops.size(); i++) {
            List<Relationship> cycle = loops.get(i);
            sb.append("LOOP #").append(i + 1)
                    .append(" FOUND (").append(cycle.size()).append(" entities)\n");

            for (Relationship edge : cycle) {
                sb.append("  ").append(formatNodeLabel(edge.getSourceId(), entityService)).append("\n");
                sb.append("    | ").append(edge.getLabel());
                if (edge.getAmount() != null) {
                    sb.append(" ").append(edge.getAmount());
                }
                sb.append("\n    v\n");
            }
            String firstId = cycle.get(0).getSourceId();
            sb.append("  ").append(formatNodeLabel(firstId, entityService))
                    .append(" (loop closes here)\n\n");
        }
        return sb.toString();
    }

    private String formatNodeLabel(String entityId, EntityService entityService) {
        if (entityId == null) {
            return "[UNKNOWN]";
        }
        if (entityService != null) {
            for (Entity e : entityService.getAllEntities()) {
                if (entityId.equalsIgnoreCase(e.getId())) {
                    return "[" + e.getId() + " " + e.getName() + "]";
                }
            }
        }
        return "[" + entityId + "]";
    }
}