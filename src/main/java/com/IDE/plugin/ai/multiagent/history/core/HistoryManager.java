package com.IDE.plugin.ai.multiagent.history.core;

import com.IDE.plugin.ai.multiagent.trust.consensus.ConsensusCoordinator;
import com.IDE.plugin.ai.multiagent.trust.reputation.ReputationManager;
import com.IDE.plugin.ai.multiagent.trust.authorization.SignatureValidator;
import com.intellij.openapi.diagnostic.Logger;

import java.time.Instant;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Central management of administrative history with trust integration.
 * Provides comprehensive audit trails and system maintenance capabilities.
 */
public class HistoryManager {
    private static final Logger LOG = Logger.getInstance(HistoryManager.class);
    
    private final ImmutableEventLedger eventLedger;
    private final ConsensusCoordinator consensusCoordinator;
    private final ReputationManager reputationManager;
    private final SignatureValidator signatureValidator;
    
    private final Map<String, EventMetadata> eventMetadataCache;
    private final Map<String, List<EventRecord>> agentEventHistory;
    private final Map<EventType, EventHandler> eventHandlers;
    
    private final ScheduledExecutorService maintenanceExecutor;
    private final ExecutorService analysisExecutor;
    
    private final BlockingQueue<EventRecord> pendingEvents;
    private final Set<String> activeQueries;
    
    // Configuration
    private final int maxEventRetention = 10000;
    private final Duration retentionPeriod = Duration.ofDays(30);
    private final int batchSize = 100;
    
    public enum EventType {
        AGENT_ACTION,
        SYSTEM_CHANGE,
        RESOURCE_ACCESS,
        COLLABORATION_EVENT,
        TRUST_UPDATE,
        MEMORY_OPERATION,
        ERROR_EVENT,
        MAINTENANCE_EVENT
    }
    
    public static class EventRecord {
        private final String eventId;
        private final EventType type;
        private final String agentId;
        private final Instant timestamp;
        private final Map<String, Object> data;
        private final String signature;
        private final double trustLevel;
        
        public EventRecord(EventType type, String agentId, Map<String, Object> data, 
                          String signature, double trustLevel) {
            this.eventId = UUID.randomUUID().toString();
            this.type = type;
            this.agentId = agentId;
            this.timestamp = Instant.now();
            this.data = new HashMap<>(data);
            this.signature = signature;
            this.trustLevel = trustLevel;
        }
        
        // Getters
        public String getEventId() { return eventId; }
        public EventType getType() { return type; }
        public String getAgentId() { return agentId; }
        public Instant getTimestamp() { return timestamp; }
        public Map<String, Object> getData() { return Collections.unmodifiableMap(data); }
        public String getSignature() { return signature; }
        public double getTrustLevel() { return trustLevel; }
    }
    
    public static class EventMetadata {
        private final String eventId;
        private final long blockNumber;
        private final String blockHash;
        private final Set<String> validators;
        private final Instant validationTime;
        
        public EventMetadata(String eventId, long blockNumber, String blockHash,
                           Set<String> validators) {
            this.eventId = eventId;
            this.blockNumber = blockNumber;
            this.blockHash = blockHash;
            this.validators = new HashSet<>(validators);
            this.validationTime = Instant.now();
        }
    }
    
    @FunctionalInterface
    public interface EventHandler {
        void handle(EventRecord event) throws Exception;
    }
    
    public HistoryManager(ImmutableEventLedger eventLedger,
                         ConsensusCoordinator consensusCoordinator,
                         ReputationManager reputationManager,
                         SignatureValidator signatureValidator) {
        this.eventLedger = eventLedger;
        this.consensusCoordinator = consensusCoordinator;
        this.reputationManager = reputationManager;
        this.signatureValidator = signatureValidator;
        
        this.eventMetadataCache = new ConcurrentHashMap<>();
        this.agentEventHistory = new ConcurrentHashMap<>();
        this.eventHandlers = new ConcurrentHashMap<>();
        
        this.maintenanceExecutor = Executors.newScheduledThreadPool(2);
        this.analysisExecutor = Executors.newFixedThreadPool(4);
        
        this.pendingEvents = new LinkedBlockingQueue<>();
        this.activeQueries = ConcurrentHashMap.newKeySet();
        
        initializeEventHandlers();
        startMaintenanceTasks();
    }
    
    private void initializeEventHandlers() {
        // Register default handlers for each event type
        eventHandlers.put(EventType.AGENT_ACTION, this::handleAgentAction);
        eventHandlers.put(EventType.TRUST_UPDATE, this::handleTrustUpdate);
        eventHandlers.put(EventType.ERROR_EVENT, this::handleErrorEvent);
        eventHandlers.put(EventType.MAINTENANCE_EVENT, this::handleMaintenanceEvent);
    }
    
    private void startMaintenanceTasks() {
        // Periodic event processing
        maintenanceExecutor.scheduleAtFixedRate(
            this::processPendingEvents, 0, 1, TimeUnit.SECONDS
        );
        
        // Periodic cleanup
        maintenanceExecutor.scheduleAtFixedRate(
            this::performMaintenance, 0, 1, TimeUnit.HOURS
        );
    }
    
    /**
     * Records an event with trust verification and consensus validation
     */
    public CompletableFuture<EventMetadata> recordEvent(EventType type, String agentId,
                                                       Map<String, Object> data,
                                                       String signature) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Verify signature
                if (!signatureValidator.validateSignature(signature, agentId, data)) {
                    throw new SecurityException("Invalid signature for event");
                }
                
                // Get agent trust level
                double trustLevel = reputationManager.getTrustScore(agentId);
                
                // Create event record
                EventRecord event = new EventRecord(type, agentId, data, signature, trustLevel);
                
                // Add to pending queue
                pendingEvents.offer(event);
                
                // For critical events, immediate consensus
                if (isCriticalEvent(type)) {
                    return processEventWithConsensus(event).get();
                }
                
                // Return placeholder metadata for non-critical events
                return new EventMetadata(event.getEventId(), -1, "", Collections.emptySet());
                
            } catch (Exception e) {
                LOG.error("Failed to record event", e);
                throw new RuntimeException("Event recording failed", e);
            }
        }, analysisExecutor);
    }
    
    private boolean isCriticalEvent(EventType type) {
        return type == EventType.SYSTEM_CHANGE || 
               type == EventType.TRUST_UPDATE ||
               type == EventType.ERROR_EVENT;
    }
    
    private CompletableFuture<EventMetadata> processEventWithConsensus(EventRecord event) {
        return consensusCoordinator.propose("event-validation", event)
            .thenCompose(consensus -> {
                if (consensus.isAccepted()) {
                    return eventLedger.appendEvent(event);
                } else {
                    throw new RuntimeException("Event rejected by consensus");
                }
            });
    }
    
    private void processPendingEvents() {
        try {
            List<EventRecord> batch = new ArrayList<>();
            pendingEvents.drainTo(batch, batchSize);
            
            if (!batch.isEmpty()) {
                // Process events in batch
                CompletableFuture<List<EventMetadata>> futures = 
                    eventLedger.appendEventsBatch(batch);
                
                futures.thenAccept(metadataList -> {
                    for (int i = 0; i < batch.size(); i++) {
                        EventRecord event = batch.get(i);
                        EventMetadata metadata = metadataList.get(i);
                        
                        // Update caches
                        eventMetadataCache.put(event.getEventId(), metadata);
                        agentEventHistory.computeIfAbsent(event.getAgentId(), 
                            k -> new CopyOnWriteArrayList<>()).add(event);
                        
                        // Invoke handler
                        invokeEventHandler(event);
                    }
                }).exceptionally(e -> {
                    LOG.error("Batch processing failed", e);
                    // Re-queue events
                    batch.forEach(pendingEvents::offer);
                    return null;
                });
            }
        } catch (Exception e) {
            LOG.error("Error processing pending events", e);
        }
    }
    
    private void invokeEventHandler(EventRecord event) {
        EventHandler handler = eventHandlers.get(event.getType());
        if (handler != null) {
            try {
                handler.handle(event);
            } catch (Exception e) {
                LOG.error("Event handler failed for " + event.getType(), e);
            }
        }
    }
    
    /**
     * Queries historical events with various filters
     */
    public CompletableFuture<List<EventRecord>> queryEvents(EventQuery query) {
        String queryId = UUID.randomUUID().toString();
        activeQueries.add(queryId);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<EventRecord> results = new ArrayList<>();
                
                // Query from ledger
                List<EventRecord> ledgerEvents = eventLedger.queryEvents(
                    query.getStartTime(), query.getEndTime()
                );
                
                // Apply filters
                results = ledgerEvents.stream()
                    .filter(e -> matchesQuery(e, query))
                    .sorted(Comparator.comparing(EventRecord::getTimestamp))
                    .limit(query.getMaxResults())
                    .collect(Collectors.toList());
                
                return results;
                
            } finally {
                activeQueries.remove(queryId);
            }
        }, analysisExecutor);
    }
    
    private boolean matchesQuery(EventRecord event, EventQuery query) {
        // Type filter
        if (query.getTypes() != null && !query.getTypes().contains(event.getType())) {
            return false;
        }
        
        // Agent filter  
        if (query.getAgentIds() != null && !query.getAgentIds().contains(event.getAgentId())) {
            return false;
        }
        
        // Trust level filter
        if (event.getTrustLevel() < query.getMinTrustLevel()) {
            return false;
        }
        
        // Data filters
        if (query.getDataFilters() != null) {
            for (Map.Entry<String, Object> filter : query.getDataFilters().entrySet()) {
                if (!filter.getValue().equals(event.getData().get(filter.getKey()))) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private void performMaintenance() {
        try {
            // Clean old events
            Instant cutoff = Instant.now().minus(retentionPeriod);
            
            // Clean agent histories
            for (Map.Entry<String, List<EventRecord>> entry : agentEventHistory.entrySet()) {
                List<EventRecord> events = entry.getValue();
                events.removeIf(e -> e.getTimestamp().isBefore(cutoff));
                
                // Limit size
                if (events.size() > maxEventRetention) {
                    events.subList(0, events.size() - maxEventRetention).clear();
                }
            }
            
            // Clean metadata cache
            eventMetadataCache.entrySet().removeIf(entry -> {
                EventMetadata metadata = entry.getValue();
                return metadata.validationTime.isBefore(cutoff);
            });
            
        } catch (Exception e) {
            LOG.error("Maintenance failed", e);
        }
    }
    
    // Event handlers
    private void handleAgentAction(EventRecord event) throws Exception {
        // Update agent activity metrics
        reputationManager.recordAgentActivity(event.getAgentId(), event.getData());
    }
    
    private void handleTrustUpdate(EventRecord event) throws Exception {
        // Propagate trust changes
        String targetAgent = (String) event.getData().get("targetAgent");
        Double newTrust = (Double) event.getData().get("trustLevel");
        if (targetAgent != null && newTrust != null) {
            reputationManager.updateTrustScore(targetAgent, newTrust);
        }
    }
    
    private void handleErrorEvent(EventRecord event) throws Exception {
        // Log critical errors
        LOG.error("System error from agent " + event.getAgentId() + ": " + event.getData());
    }
    
    private void handleMaintenanceEvent(EventRecord event) throws Exception {
        // Record maintenance operations
        LOG.info("Maintenance event: " + event.getData());
    }
    
    public void shutdown() {
        maintenanceExecutor.shutdown();
        analysisExecutor.shutdown();
        try {
            maintenanceExecutor.awaitTermination(30, TimeUnit.SECONDS);
            analysisExecutor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    // Inner class for queries
    public static class EventQuery {
        private Instant startTime;
        private Instant endTime;
        private Set<EventType> types;
        private Set<String> agentIds;
        private double minTrustLevel = 0.0;
        private Map<String, Object> dataFilters;
        private int maxResults = 1000;
        
        // Builder pattern implementation
        public EventQuery withTimeRange(Instant start, Instant end) {
            this.startTime = start;
            this.endTime = end;
            return this;
        }
        
        public EventQuery withTypes(EventType... types) {
            this.types = new HashSet<>(Arrays.asList(types));
            return this;
        }
        
        public EventQuery withAgents(String... agentIds) {
            this.agentIds = new HashSet<>(Arrays.asList(agentIds));
            return this;
        }
        
        public EventQuery withMinTrustLevel(double minTrust) {
            this.minTrustLevel = minTrust;
            return this;
        }
        
        public EventQuery withDataFilter(String key, Object value) {
            if (this.dataFilters == null) {
                this.dataFilters = new HashMap<>();
            }
            this.dataFilters.put(key, value);
            return this;
        }
        
        public EventQuery withMaxResults(int max) {
            this.maxResults = max;
            return this;
        }
        
        // Getters
        public Instant getStartTime() { return startTime; }
        public Instant getEndTime() { return endTime; }
        public Set<EventType> getTypes() { return types; }
        public Set<String> getAgentIds() { return agentIds; }
        public double getMinTrustLevel() { return minTrustLevel; }
        public Map<String, Object> getDataFilters() { return dataFilters; }
        public int getMaxResults() { return maxResults; }
    }
}