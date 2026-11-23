package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import model.Relationship;
import model.RelationshipStatus;

public class GraphService {
    private final RelationshipService relationshipService;

    public GraphService(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }

    public Map<String, List<Relationship>> buildUndirectedApprovedGraph() {
        Map<String, List<Relationship>> adj = new HashMap<>();
        if (relationshipService == null) {
            return adj;
        }

        for (Relationship r : relationshipService.findAll()) {
            if (r == null || r.getStatus() != RelationshipStatus.APPROVED) {
                continue;
            }
            String src = r.getSourceId();
            String tgt = r.getTargetId();
            if (src == null || tgt == null) {
                continue;
            }

            if (!adj.containsKey(src)) {
                adj.put(src, new ArrayList<>());
            }
            adj.get(src).add(r);

            if (!adj.containsKey(tgt)) {
                adj.put(tgt, new ArrayList<>());
            }
            adj.get(tgt).add(r);
        }
        return adj;
    }

    public List<String> findShortestPath(String startId, String endId) {
        List<String> path = new ArrayList<>();
        if (startId == null || endId == null) {
            return path;
        }
        if (startId.equals(endId)) {
            path.add(startId);
            return path;
        }

        Map<String, List<Relationship>> adj = buildUndirectedApprovedGraph();
        if (!adj.containsKey(startId) || !adj.containsKey(endId)) {
            return path;
        }

        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        Map<String, String> parentMap = new HashMap<>();

        queue.add(startId);
        visited.add(startId);

        boolean found = false;
        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(endId)) {
                found = true;
                break;
            }

            List<Relationship> edges = adj.get(current);
            if (edges != null) {
                for (Relationship edge : edges) {
                    String neighbor;
                    if (edge.getSourceId().equals(current)) {
                        neighbor = edge.getTargetId();
                    } else {
                        neighbor = edge.getSourceId();
                    }

                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        parentMap.put(neighbor, current);
                        queue.add(neighbor);
                    }
                }
            }
        }

        if (!found) {
            return path;
        }

        String step = endId;
        while (step != null) {
            path.add(step);
            step = parentMap.get(step);
        }
        Collections.reverse(path);
        return path;
    }

    public List<Relationship> findSuggestedLinksBetween(String idA, String idB) {
        List<Relationship> matches = new ArrayList<>();
        if (relationshipService == null || idA == null || idB == null) {
            return matches;
        }

        for (Relationship r : relationshipService.findAll()) {
            if (r != null && r.getStatus() == RelationshipStatus.SUGGESTED) {
                boolean forward = idA.equals(r.getSourceId()) && idB.equals(r.getTargetId());
                boolean reverse = idB.equals(r.getSourceId()) && idA.equals(r.getTargetId());
                if (forward || reverse) {
                    matches.add(r);
                }
            }
        }
        return matches;
    }

    public List<List<Relationship>> findCircularLoops() {
        List<List<Relationship>> uniqueLoops = new ArrayList<>();
        if (relationshipService == null) {
            return uniqueLoops;
        }

        Map<String, List<Relationship>> paymentAdj = new HashMap<>();
        for (Relationship r : relationshipService.findAll()) {
            if (r != null
                    && r.getStatus() == RelationshipStatus.APPROVED
                    && "PAYMENT".equalsIgnoreCase(r.getLabel())) {
                String src = r.getSourceId();
                String tgt = r.getTargetId();

                if (!paymentAdj.containsKey(src)) {
                    paymentAdj.put(src, new ArrayList<>());
                }
                paymentAdj.get(src).add(r);

                if (!paymentAdj.containsKey(tgt)) {
                    paymentAdj.put(tgt, new ArrayList<>());
                }
            }
        }

        Map<String, Integer> state = new HashMap<>();
        List<String> nodeStack = new ArrayList<>();
        List<Relationship> edgeStack = new ArrayList<>();
        Set<String> seenSignatures = new HashSet<>();

        List<String> allNodes = new ArrayList<>(paymentAdj.keySet());
        Collections.sort(allNodes);

        for (String node : allNodes) {
            if (getNodeState(state, node) == 0) {
                dfsLoops(node, paymentAdj, state, nodeStack, edgeStack, seenSignatures, uniqueLoops);
            }
        }

        return uniqueLoops;
    }

    private void dfsLoops(
            String current,
            Map<String, List<Relationship>> adj,
            Map<String, Integer> state,
            List<String> nodeStack,
            List<Relationship> edgeStack,
            Set<String> seenSignatures,
            List<List<Relationship>> uniqueLoops) {
        state.put(current, 1);
        nodeStack.add(current);

        List<Relationship> edges = adj.get(current);
        if (edges != null) {
            for (Relationship edge : edges) {
                String next = edge.getTargetId();
                int nextState = getNodeState(state, next);

                if (nextState == 1) {
                    int startIdx = nodeStack.indexOf(next);
                    List<Relationship> rawCycle = new ArrayList<>();
                    for (int i = startIdx; i < edgeStack.size(); i++) {
                        rawCycle.add(edgeStack.get(i));
                    }
                    rawCycle.add(edge);

                    List<Relationship> rotated = rotateToSmallestId(rawCycle);
                    String signature = buildCycleSignature(rotated);
                    if (seenSignatures.add(signature)) {
                        uniqueLoops.add(rotated);
                    }
                } else if (nextState == 0) {
                    edgeStack.add(edge);
                    dfsLoops(next, adj, state, nodeStack, edgeStack, seenSignatures, uniqueLoops);
                    edgeStack.remove(edgeStack.size() - 1);
                }
            }
        }

        nodeStack.remove(nodeStack.size() - 1);
        state.put(current, 2);
    }

    private int getNodeState(Map<String, Integer> state, String node) {
        if (state.containsKey(node)) {
            return state.get(node);
        }
        return 0;
    }

    private List<Relationship> rotateToSmallestId(List<Relationship> cycle) {
        int minIdx = 0;
        for (int i = 1; i < cycle.size(); i++) {
            if (cycle.get(i).getSourceId().compareTo(cycle.get(minIdx).getSourceId()) < 0) {
                minIdx = i;
            }
        }
        List<Relationship> rotated = new ArrayList<>();
        for (int i = 0; i < cycle.size(); i++) {
            rotated.add(cycle.get((minIdx + i) % cycle.size()));
        }
        return rotated;
    }

    private String buildCycleSignature(List<Relationship> cycle) {
        StringBuilder sb = new StringBuilder();
        for (Relationship r : cycle) {
            sb.append(r.getSourceId()).append("->");
        }
        sb.append(cycle.get(0).getSourceId());
        return sb.toString();
    }
}