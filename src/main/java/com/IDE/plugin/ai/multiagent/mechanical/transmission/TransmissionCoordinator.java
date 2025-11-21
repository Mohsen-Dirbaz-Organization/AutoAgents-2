package com.IDE.plugin.ai.multiagent.mechanical.transmission;

import com.IDE.plugin.ai.multiagent.mechanical.validation.MechanicalSignal;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TransmissionCoordinator: Coordinates transmission, queuing, and routing activities
 * Logic: 40% transmission coordination, 35% routing management, 25% performance optimization
 */
public class TransmissionCoordinator {
    
    private final MechanicalSignalTransmitter transmitter;
    private final MechanicalSignalQueue signalQueue;
    private final MechanicalSignalRouter router;
    private final ExecutorService coordinationExecutor;
    private final ScheduledExecutorService scheduledExecutor;
    private final Map<String, TransmissionSession> activeSessions;
    private final TransmissionOptimizer optimizer;
    private final PerformanceTracker performanceTracker;
    private volatile boolean running;
    
    private static final int MAX_BATCH_SIZE = 100;
    private static final long BATCH_WINDOW_MS = 50;
    
    public TransmissionCoordinator(MechanicalSignalTransmitter transmitter,
                                 MechanicalSignalQueue signalQueue,
                                 MechanicalSignalRouter router) {
        this.transmitter = transmitter;
        this.signalQueue = signalQueue;
        this.router = router;
        this.coordinationExecutor = Executors.newFixedThreadPool(8);
        this.scheduledExecutor = Executors.newScheduledThreadPool(3);
        this.activeSessions = new ConcurrentHashMap<>();
        this.optimizer = new TransmissionOptimizer();
        this.performanceTracker = new PerformanceTracker();
        this.running = true;
        
        initialize();
    }
    
    private void initialize() {
        // Start transmission processing
        coordinationExecutor.submit(this::processTransmissions);
        
        // Schedule batch processing
        scheduledExecutor.scheduleAtFixedRate(
            this::processBatchTransmissions, 
            BATCH_WINDOW_MS, BATCH_WINDOW_MS, TimeUnit.MILLISECONDS
        );
        
        // Schedule performance optimization
        scheduledExecutor.scheduleAtFixedRate(
            this::optimizePerformance, 30, 30, TimeUnit.SECONDS
        );
    }
    
    /**
     * Coordinates end-to-end signal transmission
     */
    public CompletableFuture<TransmissionResult> coordinateTransmission(
            MechanicalSignal signal, String destination) {
        
        // Create transmission session
        TransmissionSession session = new TransmissionSession(signal, destination);
        activeSessions.put(signal.getId(), session);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Transmission coordination (40% of logic)
                
                // 1. Queue the signal
                QueueResult queueResult = signalQueue.enqueue(signal);
                if (!queueResult.isSuccess()) {
                    return new TransmissionResult(false, queueResult.getMessage(), 0);
                }
                
                // 2. Wait for signal to be dequeued
                MechanicalSignal dequeuedSignal = waitForDequeue(signal.getId());
                if (dequeuedSignal == null) {
                    return new TransmissionResult(false, "Signal dequeue timeout", 0);
                }
                
                // Routing management (35% of logic)
                
                // 3. Route the signal
                RoutingResult routingResult = router.route(dequeuedSignal, destination).get();
                if (!routingResult.isSuccess()) {
                    return new TransmissionResult(false, routingResult.getMessage(), 0);
                }
                
                // 4. Transmit via determined route
                String nextHop = routingResult.getNextHop();
                TransmissionResult transmissionResult = transmitter.transmit(
                    dequeuedSignal, nextHop
                ).get();
                
                // Performance optimization (25% of logic)
                
                // 5. Track performance metrics
                performanceTracker.recordTransmission(session, transmissionResult);
                
                // 6. Apply optimizations if needed
                if (transmissionResult.isSuccessful()) {
                    optimizer.recordSuccessfulPath(signal.getSenderId(), destination, 
                        routingResult.getRoute());
                } else {
                    optimizer.recordFailedPath(signal.getSenderId(), destination);
                }
                
                return transmissionResult;
                
            } catch (Exception e) {
                return new TransmissionResult(false, "Coordination error: " + e.getMessage(), 0);
            } finally {
                activeSessions.remove(signal.getId());
            }
        }, coordinationExecutor);
    }
    
    /**
     * Processes transmissions from queue
     */
    private void processTransmissions() {
        while (running) {
            try {
                // Dequeue signal
                MechanicalSignal signal = signalQueue.dequeue();
                if (signal != null) {
                    processSignal(signal);
                } else {
                    Thread.sleep(10); // Short sleep when queue is empty
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                // Log error
            }
        }
    }
    
    /**
     * Processes batch transmissions for efficiency
     */
    private void processBatchTransmissions() {
        try {
            // Dequeue batch
            List<MechanicalSignal> batch = signalQueue.dequeueBatch(MAX_BATCH_SIZE);
            if (batch.isEmpty()) {
                return;
            }
            
            // Group by destination for efficient routing
            Map<String, List<MechanicalSignal>> groupedSignals = groupByDestination(batch);
            
            // Process each group
            for (Map.Entry<String, List<MechanicalSignal>> entry : groupedSignals.entrySet()) {
                String destination = entry.getKey();
                List<MechanicalSignal> signals = entry.getValue();
                
                coordinationExecutor.submit(() -> processBatchToDestination(destination, signals));
            }
            
        } catch (Exception e) {
            // Log error
        }
    }
    
    /**
     * Processes a signal
     */
    private void processSignal(MechanicalSignal signal) {
        TransmissionSession session = activeSessions.get(signal.getId());
        if (session != null) {
            session.signalDequeued();
        } else {
            // Process orphan signal
            String destination = (String) signal.getMetadata().get("destination");
            if (destination != null) {
                coordinateTransmission(signal, destination);
            }
        }
    }
    
    /**
     * Processes batch of signals to same destination
     */
    private void processBatchToDestination(String destination, List<MechanicalSignal> signals) {
        try {
            // Get route once for all signals
            if (!signals.isEmpty()) {
                RoutingResult routingResult = router.route(signals.get(0), destination).get();
                if (routingResult.isSuccess()) {
                    String nextHop = routingResult.getNextHop();
                    
                    // Transmit all signals via same route
                    List<CompletableFuture<TransmissionResult>> futures = signals.stream()
                        .map(signal -> transmitter.transmit(signal, nextHop))
                        .collect(Collectors.toList());
                    
                    // Wait for all transmissions
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
                    
                    // Update performance metrics
                    performanceTracker.recordBatchTransmission(destination, signals.size());
                }
            }
        } catch (Exception e) {
            // Log error
        }
    }
    
    /**
     * Optimizes transmission performance
     */
    private void optimizePerformance() {
        // Analyze performance metrics
        PerformanceAnalysis analysis = performanceTracker.analyze();
        
        // Apply optimizations based on analysis
        if (analysis.hasBottlenecks()) {
            optimizer.applyOptimizations(analysis);
        }
        
        // Adjust batch parameters if needed
        if (analysis.shouldIncreaseBatchSize()) {
            // Increase batch size (would need to make MAX_BATCH_SIZE non-final)
        }
        
        // Clean up old sessions
        cleanupSessions();
    }
    
    /**
     * Waits for a signal to be dequeued
     */
    private MechanicalSignal waitForDequeue(String signalId) {
        TransmissionSession session = activeSessions.get(signalId);
        if (session != null) {
            try {
                return session.waitForDequeue(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * Groups signals by destination
     */
    private Map<String, List<MechanicalSignal>> groupByDestination(List<MechanicalSignal> signals) {
        return signals.stream()
            .collect(Collectors.groupingBy(
                signal -> (String) signal.getMetadata().getOrDefault("destination", "unknown")
            ));
    }
    
    /**
     * Cleans up old transmission sessions
     */
    private void cleanupSessions() {
        long cutoffTime = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5);
        activeSessions.entrySet().removeIf(entry -> 
            entry.getValue().getStartTime() < cutoffTime
        );
    }
    
    /**
     * Gets current coordinator statistics
     */
    public CoordinatorStatistics getStatistics() {
        return new CoordinatorStatistics(
            activeSessions.size(),
            performanceTracker.getTotalTransmissions(),
            performanceTracker.getSuccessRate(),
            performanceTracker.getAverageLatency(),
            optimizer.getOptimizationCount()
        );
    }
    
    /**
     * Shuts down the coordinator
     */
    public void shutdown() {
        running = false;
        
        coordinationExecutor.shutdown();
        scheduledExecutor.shutdown();
        
        try {
            if (!coordinationExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                coordinationExecutor.shutdownNow();
            }
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            coordinationExecutor.shutdownNow();
            scheduledExecutor.shutdownNow();
        }
    }
    
    /**
     * Inner class for transmission sessions
     */
    private static class TransmissionSession {
        private final MechanicalSignal signal;
        private final String destination;
        private final long startTime;
        private final CompletableFuture<MechanicalSignal> dequeueFuture;
        
        TransmissionSession(MechanicalSignal signal, String destination) {
            this.signal = signal;
            this.destination = destination;
            this.startTime = System.currentTimeMillis();
            this.dequeueFuture = new CompletableFuture<>();
        }
        
        void signalDequeued() {
            dequeueFuture.complete(signal);
        }
        
        MechanicalSignal waitForDequeue(long timeout, TimeUnit unit) 
                throws InterruptedException, ExecutionException, TimeoutException {
            return dequeueFuture.get(timeout, unit);
        }
        
        long getStartTime() { return startTime; }
    }
    
    /**
     * Inner class for transmission optimization
     */
    private static class TransmissionOptimizer {
        private final Map<String, RoutePreference> routePreferences = new ConcurrentHashMap<>();
        private final AtomicLong optimizationCount = new AtomicLong(0);
        
        void recordSuccessfulPath(String source, String destination, RouteInfo route) {
            String key = source + "->" + destination;
            RoutePreference preference = routePreferences.computeIfAbsent(
                key, k -> new RoutePreference()
            );
            preference.recordSuccess(route);
        }
        
        void recordFailedPath(String source, String destination) {
            String key = source + "->" + destination;
            RoutePreference preference = routePreferences.computeIfAbsent(
                key, k -> new RoutePreference()
            );
            preference.recordFailure();
        }
        
        void applyOptimizations(PerformanceAnalysis analysis) {
            // Apply various optimizations based on analysis
            optimizationCount.incrementAndGet();
            
            // Example optimizations:
            // 1. Update route preferences
            // 2. Adjust transmission parameters
            // 3. Modify batching strategies
        }
        
        long getOptimizationCount() {
            return optimizationCount.get();
        }
    }
    
    /**
     * Inner class for performance tracking
     */
    private static class PerformanceTracker {
        private final AtomicLong totalTransmissions = new AtomicLong(0);
        private final AtomicLong successfulTransmissions = new AtomicLong(0);
        private final AtomicLong totalLatency = new AtomicLong(0);
        private final Map<String, DestinationMetrics> destinationMetrics = new ConcurrentHashMap<>();
        
        void recordTransmission(TransmissionSession session, TransmissionResult result) {
            totalTransmissions.incrementAndGet();
            if (result.isSuccessful()) {
                successfulTransmissions.incrementAndGet();
            }
            
            long latency = System.currentTimeMillis() - session.getStartTime();
            totalLatency.addAndGet(latency);
            
            // Update destination metrics
            DestinationMetrics metrics = destinationMetrics.computeIfAbsent(
                session.destination, k -> new DestinationMetrics()
            );
            metrics.recordTransmission(result.isSuccessful(), latency);
        }
        
        void recordBatchTransmission(String destination, int batchSize) {
            DestinationMetrics metrics = destinationMetrics.computeIfAbsent(
                destination, k -> new DestinationMetrics()
            );
            metrics.recordBatch(batchSize);
        }
        
        PerformanceAnalysis analyze() {
            return new PerformanceAnalysis(
                getSuccessRate(),
                getAverageLatency(),
                identifyBottlenecks()
            );
        }
        
        long getTotalTransmissions() { return totalTransmissions.get(); }
        
        double getSuccessRate() {
            long total = totalTransmissions.get();
            return total > 0 ? successfulTransmissions.get() / (double) total : 0;
        }
        
        double getAverageLatency() {
            long total = totalTransmissions.get();
            return total > 0 ? totalLatency.get() / (double) total : 0;
        }
        
        private List<String> identifyBottlenecks() {
            return destinationMetrics.entrySet().stream()
                .filter(entry -> entry.getValue().isBottleneck())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        }
    }
    
    // Supporting classes
    private static class RoutePreference {
        private final Map<RouteInfo, RouteStatistics> routeStats = new ConcurrentHashMap<>();
        
        void recordSuccess(RouteInfo route) {
            routeStats.computeIfAbsent(route, k -> new RouteStatistics()).recordSuccess();
        }
        
        void recordFailure() {
            // Record failure for all known routes
            routeStats.values().forEach(RouteStatistics::recordFailure);
        }
    }
    
    private static class RouteStatistics {
        private final AtomicLong successes = new AtomicLong(0);
        private final AtomicLong failures = new AtomicLong(0);
        
        void recordSuccess() { successes.incrementAndGet(); }
        void recordFailure() { failures.incrementAndGet(); }
    }
    
    private static class DestinationMetrics {
        private final AtomicLong transmissionCount = new AtomicLong(0);
        private final AtomicLong failureCount = new AtomicLong(0);
        private final AtomicLong totalLatency = new AtomicLong(0);
        private final AtomicLong batchCount = new AtomicLong(0);
        
        void recordTransmission(boolean success, long latency) {
            transmissionCount.incrementAndGet();
            if (!success) failureCount.incrementAndGet();
            totalLatency.addAndGet(latency);
        }
        
        void recordBatch(int size) {
            batchCount.incrementAndGet();
            transmissionCount.addAndGet(size);
        }
        
        boolean isBottleneck() {
            long total = transmissionCount.get();
            return total > 1000 && failureCount.get() / (double) total > 0.1;
        }
    }
    
    private static class PerformanceAnalysis {
        private final double successRate;
        private final double averageLatency;
        private final List<String> bottlenecks;
        
        PerformanceAnalysis(double successRate, double averageLatency, List<String> bottlenecks) {
            this.successRate = successRate;
            this.averageLatency = averageLatency;
            this.bottlenecks = bottlenecks;
        }
        
        boolean hasBottlenecks() { return !bottlenecks.isEmpty(); }
        boolean shouldIncreaseBatchSize() { return averageLatency > 100 && successRate > 0.95; }
    }
    
    public static class CoordinatorStatistics {
        public final int activeSessions;
        public final long totalTransmissions;
        public final double successRate;
        public final double averageLatency;
        public final long optimizationCount;
        
        public CoordinatorStatistics(int activeSessions, long totalTransmissions,
                                   double successRate, double averageLatency,
                                   long optimizationCount) {
            this.activeSessions = activeSessions;
            this.totalTransmissions = totalTransmissions;
            this.successRate = successRate;
            this.averageLatency = averageLatency;
            this.optimizationCount = optimizationCount;
        }
    }
}