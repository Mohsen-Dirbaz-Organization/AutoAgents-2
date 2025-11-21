package com.IDE.plugin.ai.multiagent.mechanical.transmission;

import com.IDE.plugin.ai.multiagent.mechanical.validation.MechanicalSignal;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * MechanicalSignalRouter: Routes signals with optimized delivery paths and load balancing
 * Logic: 35% routing optimization, 30% failure detection, 35% load balancing
 */
public class MechanicalSignalRouter {
    
    private final RoutingOptimizer routingOptimizer;
    private final FailureDetector failureDetector;
    private final LoadBalancer loadBalancer;
    private final Map<String, RouteInfo> routingTable;
    private final Map<String, NodeInfo> networkNodes;
    private final ExecutorService routingExecutor;
    private final ScheduledExecutorService maintenanceExecutor;
    private volatile boolean running;
    
    private static final int MAX_ROUTE_HOPS = 10;
    private static final long ROUTE_CACHE_TTL_MS = 60000; // 1 minute
    
    public MechanicalSignalRouter() {
        this.routingOptimizer = new RoutingOptimizer();
        this.failureDetector = new FailureDetector();
        this.loadBalancer = new LoadBalancer();
        this.routingTable = new ConcurrentHashMap<>();
        this.networkNodes = new ConcurrentHashMap<>();
        this.routingExecutor = Executors.newFixedThreadPool(6);
        this.maintenanceExecutor = Executors.newScheduledThreadPool(3);
        this.running = true;
        
        initialize();
    }
    
    private void initialize() {
        // Schedule periodic tasks
        maintenanceExecutor.scheduleAtFixedRate(
            this::updateRoutingTable, 30, 30, TimeUnit.SECONDS
        );
        
        maintenanceExecutor.scheduleAtFixedRate(
            this::detectFailures, 10, 10, TimeUnit.SECONDS
        );
        
        maintenanceExecutor.scheduleAtFixedRate(
            this::balanceLoad, 15, 15, TimeUnit.SECONDS
        );
    }
    
    /**
     * Routes a mechanical signal to its destination
     */
    public CompletableFuture<RoutingResult> route(MechanicalSignal signal, String destination) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Routing optimization (35% of logic)
                RouteInfo route = routingOptimizer.findOptimalRoute(
                    signal.getSenderId(), destination, signal
                );
                
                if (route == null || route.isExpired()) {
                    route = calculateRoute(signal.getSenderId(), destination);
                    if (route == null) {
                        return new RoutingResult(false, "No route to destination");
                    }
                }
                
                // Failure detection (30% of logic)
                if (failureDetector.isRouteFailed(route)) {
                    // Recalculate route avoiding failed nodes
                    route = recalculateRouteAvoidingFailures(signal.getSenderId(), destination);
                    if (route == null) {
                        return new RoutingResult(false, "All routes have failed");
                    }
                }
                
                // Load balancing (35% of logic)
                String nextHop = loadBalancer.selectNextHop(route, signal);
                if (nextHop == null) {
                    return new RoutingResult(false, "No available next hop");
                }
                
                // Update signal routing information
                signal.incrementHopCount();
                signal.addMetadata("nextHop", nextHop);
                signal.addMetadata("route", route.getPath());
                
                // Cache route for future use
                cacheRoute(destination, route);
                
                return new RoutingResult(true, nextHop, route);
                
            } catch (Exception e) {
                return new RoutingResult(false, "Routing error: " + e.getMessage());
            }
        }, routingExecutor);
    }
    
    /**
     * Calculates a route between source and destination
     */
    private RouteInfo calculateRoute(String source, String destination) {
        // Check if direct route exists
        if (hasDirectConnection(source, destination)) {
            return new RouteInfo(Arrays.asList(source, destination), 1, 0);
        }
        
        // Use Dijkstra's algorithm for shortest path
        Map<String, RouteCalculation> calculations = new HashMap<>();
        PriorityQueue<RouteCalculation> queue = new PriorityQueue<>(
            Comparator.comparingDouble(RouteCalculation::getCost)
        );
        
        // Initialize
        calculations.put(source, new RouteCalculation(source, 0, null));
        queue.offer(calculations.get(source));
        
        while (!queue.isEmpty()) {
            RouteCalculation current = queue.poll();
            
            if (current.node.equals(destination)) {
                // Build path
                List<String> path = buildPath(calculations, destination);
                return new RouteInfo(path, path.size() - 1, current.cost);
            }
            
            // Check neighbors
            NodeInfo nodeInfo = networkNodes.get(current.node);
            if (nodeInfo != null) {
                for (String neighbor : nodeInfo.getNeighbors()) {
                    double edgeCost = nodeInfo.getLinkCost(neighbor);
                    double newCost = current.cost + edgeCost;
                    
                    RouteCalculation neighborCalc = calculations.get(neighbor);
                    if (neighborCalc == null || newCost < neighborCalc.cost) {
                        neighborCalc = new RouteCalculation(neighbor, newCost, current.node);
                        calculations.put(neighbor, neighborCalc);
                        queue.offer(neighborCalc);
                    }
                }
            }
        }
        
        return null; // No route found
    }
    
    /**
     * Recalculates route avoiding failed nodes
     */
    private RouteInfo recalculateRouteAvoidingFailures(String source, String destination) {
        Set<String> failedNodes = failureDetector.getFailedNodes();
        
        // Temporarily remove failed nodes from consideration
        Map<String, NodeInfo> activeNodes = networkNodes.entrySet().stream()
            .filter(entry -> !failedNodes.contains(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        
        // Recalculate with active nodes only
        return calculateRouteWithNodes(source, destination, activeNodes);
    }
    
    /**
     * Inner class for routing optimization
     */
    private class RoutingOptimizer {
        private final Map<String, RouteCache> routeCache = new ConcurrentHashMap<>();
        
        RouteInfo findOptimalRoute(String source, String destination, MechanicalSignal signal) {
            // Check cache first
            String cacheKey = source + "->" + destination;
            RouteCache cached = routeCache.get(cacheKey);
            
            if (cached != null && !cached.isExpired()) {
                // Verify route is still optimal
                if (isRouteOptimal(cached.route, signal)) {
                    return cached.route;
                }
            }
            
            // Calculate optimal route based on multiple factors
            List<RouteInfo> candidates = findAllRoutes(source, destination);
            if (candidates.isEmpty()) {
                return null;
            }
            
            // Score routes based on optimization criteria
            RouteInfo optimal = candidates.stream()
                .min((r1, r2) -> Double.compare(
                    scoreRoute(r1, signal),
                    scoreRoute(r2, signal)
                ))
                .orElse(null);
            
            // Cache the optimal route
            if (optimal != null) {
                routeCache.put(cacheKey, new RouteCache(optimal));
            }
            
            return optimal;
        }
        
        private double scoreRoute(RouteInfo route, MechanicalSignal signal) {
            double score = 0;
            
            // Hop count (lower is better)
            score += route.getHopCount() * 10;
            
            // Latency estimate
            score += route.getEstimatedLatency() * 5;
            
            // Load on route
            score += calculateRouteLoad(route) * 3;
            
            // Priority adjustment
            if (signal.getPriority() == SignalPriority.CRITICAL) {
                score *= 0.5; // Prefer faster routes for critical signals
            }
            
            return score;
        }
        
        private boolean isRouteOptimal(RouteInfo route, MechanicalSignal signal) {
            // Check if route conditions have changed significantly
            double currentLoad = calculateRouteLoad(route);
            return currentLoad < 0.8; // Route is still good if load is below 80%
        }
        
        private List<RouteInfo> findAllRoutes(String source, String destination) {
            // Simplified - would implement k-shortest paths algorithm
            List<RouteInfo> routes = new ArrayList<>();
            RouteInfo primary = calculateRoute(source, destination);
            if (primary != null) {
                routes.add(primary);
            }
            return routes;
        }
    }
    
    /**
     * Inner class for failure detection
     */
    private class FailureDetector {
        private final Map<String, NodeHealth> nodeHealthMap = new ConcurrentHashMap<>();
        private final Set<String> failedNodes = ConcurrentHashMap.newKeySet();
        private final AtomicLong failureDetectionCount = new AtomicLong(0);
        
        boolean isRouteFailed(RouteInfo route) {
            for (String node : route.getPath()) {
                if (failedNodes.contains(node)) {
                    return true;
                }
                
                NodeHealth health = nodeHealthMap.get(node);
                if (health != null && health.isFailed()) {
                    markNodeFailed(node);
                    return true;
                }
            }
            return false;
        }
        
        void detectFailures() {
            for (Map.Entry<String, NodeInfo> entry : networkNodes.entrySet()) {
                String nodeId = entry.getKey();
                NodeInfo node = entry.getValue();
                
                // Heartbeat check
                if (!node.isResponsive()) {
                    NodeHealth health = nodeHealthMap.computeIfAbsent(
                        nodeId, k -> new NodeHealth(nodeId)
                    );
                    health.recordFailure();
                    
                    if (health.shouldMarkFailed()) {
                        markNodeFailed(nodeId);
                    }
                } else {
                    // Node is responsive
                    NodeHealth health = nodeHealthMap.get(nodeId);
                    if (health != null) {
                        health.recordSuccess();
                        if (health.shouldRecover()) {
                            recoverNode(nodeId);
                        }
                    }
                }
            }
        }
        
        private void markNodeFailed(String nodeId) {
            failedNodes.add(nodeId);
            failureDetectionCount.incrementAndGet();
            
            // Notify affected routes
            invalidateRoutesContainingNode(nodeId);
        }
        
        private void recoverNode(String nodeId) {
            failedNodes.remove(nodeId);
            nodeHealthMap.remove(nodeId);
        }
        
        Set<String> getFailedNodes() {
            return new HashSet<>(failedNodes);
        }
    }
    
    /**
     * Inner class for load balancing
     */
    private class LoadBalancer {
        private final Map<String, NodeLoad> nodeLoadMap = new ConcurrentHashMap<>();
        
        String selectNextHop(RouteInfo route, MechanicalSignal signal) {
            List<String> path = route.getPath();
            if (path.size() < 2) {
                return null;
            }
            
            // Get current position in route
            int currentIndex = 0; // Assuming we're at the start
            String nextHop = path.get(currentIndex + 1);
            
            // Check if next hop is overloaded
            NodeLoad load = nodeLoadMap.get(nextHop);
            if (load != null && load.isOverloaded()) {
                // Find alternative next hop
                String alternative = findAlternativeNextHop(
                    path.get(currentIndex), 
                    path.get(path.size() - 1),
                    nextHop
                );
                
                if (alternative != null) {
                    return alternative;
                }
            }
            
            // Update load information
            updateNodeLoad(nextHop, signal);
            
            return nextHop;
        }
        
        void balanceLoad() {
            // Analyze load distribution
            Map<String, Double> loadDistribution = calculateLoadDistribution();
            
            // Identify overloaded and underloaded nodes
            List<String> overloaded = new ArrayList<>();
            List<String> underloaded = new ArrayList<>();
            
            double avgLoad = loadDistribution.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.5);
            
            for (Map.Entry<String, Double> entry : loadDistribution.entrySet()) {
                if (entry.getValue() > avgLoad * 1.5) {
                    overloaded.add(entry.getKey());
                } else if (entry.getValue() < avgLoad * 0.5) {
                    underloaded.add(entry.getKey());
                }
            }
            
            // Rebalance if needed
            if (!overloaded.isEmpty() && !underloaded.isEmpty()) {
                performLoadRebalancing(overloaded, underloaded);
            }
        }
        
        private String findAlternativeNextHop(String current, String destination, String avoid) {
            // Find alternative path segment
            RouteInfo alternative = calculateRouteAvoidingNode(current, destination, avoid);
            if (alternative != null && alternative.getPath().size() > 1) {
                return alternative.getPath().get(1);
            }
            return null;
        }
        
        private void updateNodeLoad(String nodeId, MechanicalSignal signal) {
            NodeLoad load = nodeLoadMap.computeIfAbsent(nodeId, k -> new NodeLoad(nodeId));
            load.addSignal(signal);
        }
        
        private Map<String, Double> calculateLoadDistribution() {
            Map<String, Double> distribution = new HashMap<>();
            for (Map.Entry<String, NodeLoad> entry : nodeLoadMap.entrySet()) {
                distribution.put(entry.getKey(), entry.getValue().getCurrentLoad());
            }
            return distribution;
        }
        
        private void performLoadRebalancing(List<String> overloaded, List<String> underloaded) {
            // Update routing preferences to favor underloaded nodes
            for (String overloadedNode : overloaded) {
                NodeInfo node = networkNodes.get(overloadedNode);
                if (node != null) {
                    node.setLoadFactor(1.5); // Increase cost
                }
            }
            
            for (String underloadedNode : underloaded) {
                NodeInfo node = networkNodes.get(underloadedNode);
                if (node != null) {
                    node.setLoadFactor(0.7); // Decrease cost
                }
            }
        }
    }
    
    /**
     * Updates the routing table periodically
     */
    private void updateRoutingTable() {
        // Refresh expired routes
        routingTable.entrySet().removeIf(entry -> entry.getValue().isExpired());
        
        // Discover new nodes
        discoverNewNodes();
        
        // Update link costs
        updateLinkCosts();
    }
    
    /**
     * Helper methods
     */
    private boolean hasDirectConnection(String source, String destination) {
        NodeInfo sourceNode = networkNodes.get(source);
        return sourceNode != null && sourceNode.hasNeighbor(destination);
    }
    
    private List<String> buildPath(Map<String, RouteCalculation> calculations, String destination) {
        LinkedList<String> path = new LinkedList<>();
        String current = destination;
        
        while (current != null) {
            path.addFirst(current);
            RouteCalculation calc = calculations.get(current);
            current = calc != null ? calc.previous : null;
        }
        
        return path;
    }
    
    private RouteInfo calculateRouteWithNodes(String source, String destination, 
                                             Map<String, NodeInfo> nodes) {
        // Similar to calculateRoute but with restricted node set
        return null; // Simplified
    }
    
    private RouteInfo calculateRouteAvoidingNode(String source, String destination, String avoid) {
        // Calculate route that doesn't include the specified node
        return null; // Simplified
    }
    
    private double calculateRouteLoad(RouteInfo route) {
        // Calculate average load across all nodes in route
        return route.getPath().stream()
            .map(nodeLoadMap::get)
            .filter(Objects::nonNull)
            .mapToDouble(NodeLoad::getCurrentLoad)
            .average()
            .orElse(0.0);
    }
    
    private void invalidateRoutesContainingNode(String nodeId) {
        routingTable.entrySet().removeIf(entry -> 
            entry.getValue().getPath().contains(nodeId)
        );
    }
    
    private void cacheRoute(String destination, RouteInfo route) {
        routingTable.put(destination, route);
    }
    
    private void discoverNewNodes() {
        // Network discovery logic
    }
    
    private void updateLinkCosts() {
        // Update link costs based on current conditions
    }
    
    /**
     * Shuts down the router
     */
    public void shutdown() {
        running = false;
        
        routingExecutor.shutdown();
        maintenanceExecutor.shutdown();
        
        try {
            if (!routingExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                routingExecutor.shutdownNow();
            }
            if (!maintenanceExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                maintenanceExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            routingExecutor.shutdownNow();
            maintenanceExecutor.shutdownNow();
        }
    }
    
    /**
     * Registers a node in the network
     */
    public void registerNode(String nodeId, Set<String> neighbors) {
        NodeInfo nodeInfo = new NodeInfo(nodeId, neighbors);
        networkNodes.put(nodeId, nodeInfo);
    }
    
    // Supporting classes
    private static class RouteCalculation {
        final String node;
        final double cost;
        final String previous;
        
        RouteCalculation(String node, double cost, String previous) {
            this.node = node;
            this.cost = cost;
            this.previous = previous;
        }
        
        double getCost() { return cost; }
    }
    
    private static class RouteCache {
        final RouteInfo route;
        final long timestamp;
        
        RouteCache(RouteInfo route) {
            this.route = route;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > ROUTE_CACHE_TTL_MS;
        }
    }
    
    private static class NodeHealth {
        private final String nodeId;
        private final AtomicLong failureCount = new AtomicLong(0);
        private final AtomicLong successCount = new AtomicLong(0);
        private long lastFailureTime;
        
        NodeHealth(String nodeId) {
            this.nodeId = nodeId;
        }
        
        void recordFailure() {
            failureCount.incrementAndGet();
            lastFailureTime = System.currentTimeMillis();
        }
        
        void recordSuccess() {
            successCount.incrementAndGet();
        }
        
        boolean isFailed() {
            return failureCount.get() > 3;
        }
        
        boolean shouldMarkFailed() {
            return failureCount.get() > 3 && 
                   (System.currentTimeMillis() - lastFailureTime) < 60000;
        }
        
        boolean shouldRecover() {
            return successCount.get() > 5;
        }
    }
    
    private static class NodeLoad {
        private final String nodeId;
        private final AtomicLong signalCount = new AtomicLong(0);
        private final AtomicLong totalBytes = new AtomicLong(0);
        private long windowStart = System.currentTimeMillis();
        
        NodeLoad(String nodeId) {
            this.nodeId = nodeId;
        }
        
        void addSignal(MechanicalSignal signal) {
            signalCount.incrementAndGet();
            totalBytes.addAndGet(signal.getPayload().length);
        }
        
        double getCurrentLoad() {
            long windowDuration = System.currentTimeMillis() - windowStart;
            if (windowDuration > 60000) {
                // Reset window
                signalCount.set(0);
                totalBytes.set(0);
                windowStart = System.currentTimeMillis();
                return 0.0;
            }
            
            // Calculate load as signals per second
            return signalCount.get() / (windowDuration / 1000.0);
        }
        
        boolean isOverloaded() {
            return getCurrentLoad() > 100; // More than 100 signals per second
        }
    }
}