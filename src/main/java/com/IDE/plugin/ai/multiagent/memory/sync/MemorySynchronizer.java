package com.IDE.plugin.ai.multiagent.memory.sync;

import com.IDE.plugin.ai.multiagent.memory.core.MemoryStateManager;
import com.IDE.plugin.ai.multiagent.memory.core.MemoryState;
import com.IDE.plugin.ai.multiagent.trust.TrustVerificationService;
import com.IDE.plugin.ai.multiagent.trust.TrustLevel;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Memory synchronization across agents with trust verification.
 * Ensures consistent memory state across distributed agent network.
 */
public class MemorySynchronizer {
    
    private static final int SYNC_BATCH_SIZE = 100;
    private static final long SYNC_INTERVAL_MS = 5000; // 5 seconds
    private static final int MAX_SYNC_RETRIES = 3;
    private static final double CONFLICT_RESOLUTION_THRESHOLD = 0.7;
    
    private final MemoryStateManager stateManager;
    private final TrustVerificationService trustService;
    private final Map<String, AgentSyncState> agentStates;
    private final Map<String, SyncTransaction> activeTransactions;
    private final Queue<SyncRequest> syncQueue;
    
    private final ReadWriteLock syncLock = new ReentrantReadWriteLock();
    private final ScheduledExecutorService scheduler;
    private final ExecutorService syncExecutor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong transactionIdGenerator = new AtomicLong(0);
    
    private final ConflictResolver conflictResolver;
    private final SyncProtocol syncProtocol;
    
    public MemorySynchronizer(MemoryStateManager stateManager, 
                             TrustVerificationService trustService) {
        this.stateManager = stateManager;
        this.trustService = trustService;
        this.agentStates = new ConcurrentHashMap<>();
        this.activeTransactions = new ConcurrentHashMap<>();
        this.syncQueue = new ConcurrentLinkedQueue<>();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.syncExecutor = Executors.newFixedThreadPool(4);
        this.conflictResolver = new ConflictResolver(trustService);
        this.syncProtocol = new SyncProtocol();
    }
    
    /**
     * Start synchronization service
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            // Schedule periodic synchronization
            scheduler.scheduleAtFixedRate(
                this::performSynchronization,
                SYNC_INTERVAL_MS,
                SYNC_INTERVAL_MS,
                TimeUnit.MILLISECONDS
            );
            
            // Schedule sync state cleanup
            scheduler.scheduleAtFixedRate(
                this::cleanupSyncStates,
                1, 1, TimeUnit.MINUTES
            );
        }
    }
    
    /**
     * Stop synchronization service
     */
    public void stop() {
        if (running.compareAndSet(true, false)) {
            scheduler.shutdown();
            syncExecutor.shutdown();
            
            try {
                if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
                if (!syncExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    syncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                syncExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Synchronize memory state
     */
    public CompletableFuture<SyncResult> synchronize(MemoryState state) {
        if (!running.get()) {
            return CompletableFuture.completedFuture(
                new SyncResult(false, "Synchronizer not running"));
        }
        
        // Create sync request
        SyncRequest request = new SyncRequest(
            UUID.randomUUID().toString(),
            state,
            System.currentTimeMillis()
        );
        
        // Add to queue
        syncQueue.offer(request);
        
        // Return future for async completion
        return request.getFuture();
    }
    
    /**
     * Register agent for synchronization
     */
    public void registerAgent(String agentId, SyncEndpoint endpoint) {
        syncLock.writeLock().lock();
        try {
            AgentSyncState state = new AgentSyncState(agentId, endpoint);
            agentStates.put(agentId, state);
        } finally {
            syncLock.writeLock().unlock();
        }
    }
    
    /**
     * Unregister agent from synchronization
     */
    public void unregisterAgent(String agentId) {
        syncLock.writeLock().lock();
        try {
            agentStates.remove(agentId);
        } finally {
            syncLock.writeLock().unlock();
        }
    }
    
    /**
     * Handle incoming sync request from another agent
     */
    public SyncResponse handleSyncRequest(String fromAgentId, SyncMessage message) {
        // Verify agent trust
        TrustLevel trustLevel = trustService.getAgentTrustLevel(fromAgentId);
        if (trustLevel == TrustLevel.UNTRUSTED) {
            return new SyncResponse(
                message.getTransactionId(),
                SyncStatus.REJECTED,
                "Agent not trusted"
            );
        }
        
        syncLock.readLock().lock();
        try {
            switch (message.getType()) {
                case PROPOSE:
                    return handlePropose(fromAgentId, message);
                case COMMIT:
                    return handleCommit(fromAgentId, message);
                case ROLLBACK:
                    return handleRollback(fromAgentId, message);
                default:
                    return new SyncResponse(
                        message.getTransactionId(),
                        SyncStatus.ERROR,
                        "Unknown message type"
                    );
            }
        } finally {
            syncLock.readLock().unlock();
        }
    }
    
    /**
     * Perform periodic synchronization
     */
    private void performSynchronization() {
        List<SyncRequest> batch = new ArrayList<>();
        
        // Collect batch of sync requests
        for (int i = 0; i < SYNC_BATCH_SIZE && !syncQueue.isEmpty(); i++) {
            SyncRequest request = syncQueue.poll();
            if (request != null) {
                batch.add(request);
            }
        }
        
        if (!batch.isEmpty()) {
            syncExecutor.submit(() -> processSyncBatch(batch));
        }
    }
    
    /**
     * Process batch of sync requests
     */
    private void processSyncBatch(List<SyncRequest> batch) {
        // Group by target agents
        Map<String, List<SyncRequest>> agentGroups = groupByTargetAgents(batch);
        
        // Process each agent group
        for (Map.Entry<String, List<SyncRequest>> entry : agentGroups.entrySet()) {
            String targetAgentId = entry.getKey();
            List<SyncRequest> requests = entry.getValue();
            
            processSyncWithAgent(targetAgentId, requests);
        }
    }
    
    /**
     * Process sync with specific agent
     */
    private void processSyncWithAgent(String agentId, List<SyncRequest> requests) {
        AgentSyncState agentState = agentStates.get(agentId);
        if (agentState == null || !agentState.isAvailable()) {
            // Complete requests with failure
            requests.forEach(req -> req.complete(
                new SyncResult(false, "Agent not available")));
            return;
        }
        
        // Create transaction
        String transactionId = "tx_" + transactionIdGenerator.incrementAndGet();
        SyncTransaction transaction = new SyncTransaction(
            transactionId,
            agentId,
            requests
        );
        activeTransactions.put(transactionId, transaction);
        
        try {
            // Phase 1: Propose
            boolean proposed = proposeSync(agentState, transaction);
            if (!proposed) {
                rollbackTransaction(transaction, "Proposal failed");
                return;
            }
            
            // Phase 2: Validate
            boolean validated = validateSync(transaction);
            if (!validated) {
                rollbackTransaction(transaction, "Validation failed");
                return;
            }
            
            // Phase 3: Commit
            commitTransaction(transaction);
            
        } catch (Exception e) {
            rollbackTransaction(transaction, "Exception: " + e.getMessage());
        } finally {
            activeTransactions.remove(transactionId);
        }
    }
    
    /**
     * Propose sync to agent
     */
    private boolean proposeSync(AgentSyncState agentState, SyncTransaction transaction) {
        List<MemoryState> memories = transaction.getRequests().stream()
            .map(SyncRequest::getMemoryState)
            .collect(Collectors.toList());
        
        SyncMessage proposeMessage = new SyncMessage(
            SyncMessageType.PROPOSE,
            transaction.getId(),
            memories
        );
        
        try {
            SyncResponse response = agentState.getEndpoint()
                .sendSync(proposeMessage, 5, TimeUnit.SECONDS);
            
            return response.getStatus() == SyncStatus.ACCEPTED;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Validate sync transaction
     */
    private boolean validateSync(SyncTransaction transaction) {
        // Check for conflicts
        List<SyncConflict> conflicts = detectConflicts(transaction);
        
        if (!conflicts.isEmpty()) {
            // Resolve conflicts
            List<SyncConflict> unresolved = conflictResolver.resolveConflicts(conflicts);
            
            if (!unresolved.isEmpty()) {
                return false;
            }
        }
        
        // Validate with trust service
        for (SyncRequest request : transaction.getRequests()) {
            MemoryState state = request.getMemoryState();
            if (!trustService.validateMemoryOperation(
                    state.getOwnerAgentId(), 
                    state.getKey(), 
                    state.getTrustLevel())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Detect conflicts in transaction
     */
    private List<SyncConflict> detectConflicts(SyncTransaction transaction) {
        List<SyncConflict> conflicts = new ArrayList<>();
        
        for (SyncRequest request : transaction.getRequests()) {
            MemoryState newState = request.getMemoryState();
            
            // Check for existing state
            Optional<Object> existing = stateManager.retrieveMemory(
                "system", newState.getKey());
            
            if (existing.isPresent()) {
                // Version conflict
                conflicts.add(new SyncConflict(
                    newState.getKey(),
                    existing.get(),
                    newState.getValue(),
                    ConflictType.VERSION_MISMATCH
                ));
            }
        }
        
        return conflicts;
    }
    
    /**
     * Commit transaction
     */
    private void commitTransaction(SyncTransaction transaction) {
        // Send commit message
        AgentSyncState agentState = agentStates.get(transaction.getTargetAgentId());
        if (agentState != null) {
            SyncMessage commitMessage = new SyncMessage(
                SyncMessageType.COMMIT,
                transaction.getId(),
                Collections.emptyList()
            );
            
            try {
                agentState.getEndpoint().sendSync(commitMessage, 5, TimeUnit.SECONDS);
            } catch (Exception e) {
                // Log error but continue with local commit
            }
        }
        
        // Apply changes locally
        for (SyncRequest request : transaction.getRequests()) {
            MemoryState state = request.getMemoryState();
            stateManager.storeMemory(
                state.getOwnerAgentId(),
                state.getKey(),
                state.getValue(),
                state.getType()
            );
            
            request.complete(new SyncResult(true, "Synchronized"));
        }
        
        // Update sync state
        updateSyncState(transaction.getTargetAgentId(), transaction);
    }
    
    /**
     * Rollback transaction
     */
    private void rollbackTransaction(SyncTransaction transaction, String reason) {
        // Send rollback message
        AgentSyncState agentState = agentStates.get(transaction.getTargetAgentId());
        if (agentState != null) {
            SyncMessage rollbackMessage = new SyncMessage(
                SyncMessageType.ROLLBACK,
                transaction.getId(),
                Collections.emptyList()
            );
            
            try {
                agentState.getEndpoint().sendSync(rollbackMessage, 5, TimeUnit.SECONDS);
            } catch (Exception e) {
                // Ignore errors during rollback
            }
        }
        
        // Complete requests with failure
        for (SyncRequest request : transaction.getRequests()) {
            request.complete(new SyncResult(false, reason));
        }
    }
    
    /**
     * Handle propose message
     */
    private SyncResponse handlePropose(String fromAgentId, SyncMessage message) {
        // Validate memories
        for (MemoryState memory : message.getMemories()) {
            if (!validateIncomingMemory(fromAgentId, memory)) {
                return new SyncResponse(
                    message.getTransactionId(),
                    SyncStatus.REJECTED,
                    "Memory validation failed"
                );
            }
        }
        
        // Check for conflicts
        List<SyncConflict> conflicts = message.getMemories().stream()
            .map(memory -> checkForConflict(memory))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        if (!conflicts.isEmpty()) {
            // Try to resolve
            List<SyncConflict> unresolved = conflictResolver.resolveConflicts(conflicts);
            if (!unresolved.isEmpty()) {
                return new SyncResponse(
                    message.getTransactionId(),
                    SyncStatus.CONFLICT,
                    "Unresolved conflicts"
                );
            }
        }
        
        return new SyncResponse(
            message.getTransactionId(),
            SyncStatus.ACCEPTED,
            "Proposal accepted"
        );
    }
    
    /**
     * Handle commit message
     */
    private SyncResponse handleCommit(String fromAgentId, SyncMessage message) {
        // Apply changes
        for (MemoryState memory : message.getMemories()) {
            stateManager.storeMemory(
                memory.getOwnerAgentId(),
                memory.getKey(),
                memory.getValue(),
                memory.getType()
            );
        }
        
        return new SyncResponse(
            message.getTransactionId(),
            SyncStatus.COMMITTED,
            "Changes committed"
        );
    }
    
    /**
     * Handle rollback message
     */
    private SyncResponse handleRollback(String fromAgentId, SyncMessage message) {
        // Clean up any pending state
        return new SyncResponse(
            message.getTransactionId(),
            SyncStatus.ROLLED_BACK,
            "Transaction rolled back"
        );
    }
    
    /**
     * Validate incoming memory
     */
    private boolean validateIncomingMemory(String fromAgentId, MemoryState memory) {
        // Verify trust level
        TrustLevel agentTrust = trustService.getAgentTrustLevel(fromAgentId);
        if (agentTrust.ordinal() < memory.getTrustLevel().ordinal()) {
            return false; // Agent can't create memories above its trust level
        }
        
        // Additional validation rules
        return true;
    }
    
    /**
     * Check for conflict with existing memory
     */
    private SyncConflict checkForConflict(MemoryState memory) {
        Optional<Object> existing = stateManager.retrieveMemory("system", memory.getKey());
        if (existing.isPresent() && !existing.get().equals(memory.getValue())) {
            return new SyncConflict(
                memory.getKey(),
                existing.get(),
                memory.getValue(),
                ConflictType.VERSION_MISMATCH
            );
        }
        return null;
    }
    
    /**
     * Group sync requests by target agents
     */
    private Map<String, List<SyncRequest>> groupByTargetAgents(List<SyncRequest> requests) {
        // Simple implementation - could be enhanced with routing logic
        Map<String, List<SyncRequest>> groups = new HashMap<>();
        
        for (String agentId : agentStates.keySet()) {
            groups.put(agentId, new ArrayList<>(requests));
        }
        
        return groups;
    }
    
    /**
     * Update sync state for agent
     */
    private void updateSyncState(String agentId, SyncTransaction transaction) {
        AgentSyncState state = agentStates.get(agentId);
        if (state != null) {
            state.updateLastSyncTime();
            state.incrementSyncCount(transaction.getRequests().size());
        }
    }
    
    /**
     * Clean up old sync states
     */
    private void cleanupSyncStates() {
        long cutoffTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1);
        
        agentStates.entrySet().removeIf(entry -> 
            entry.getValue().getLastSyncTime() < cutoffTime &&
            !entry.getValue().isAvailable()
        );
    }
}