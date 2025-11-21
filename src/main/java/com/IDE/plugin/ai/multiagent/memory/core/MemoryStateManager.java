package com.IDE.plugin.ai.multiagent.memory.core;

import com.IDE.plugin.ai.multiagent.trust.TrustVerificationService;
import com.IDE.plugin.ai.multiagent.trust.TrustLevel;
import com.IDE.plugin.ai.multiagent.memory.persistence.StatePersistenceService;
import com.IDE.plugin.ai.multiagent.memory.sync.MemorySynchronizer;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Central coordination of memory operations with trust verification.
 * Manages the three-tier memory architecture and ensures distributed validation.
 */
public class MemoryStateManager {
    
    private final WorkingMemory workingMemory;
    private final EpisodicMemory episodicMemory;
    private final SemanticMemory semanticMemory;
    private final TrustVerificationService trustService;
    private final StatePersistenceService persistenceService;
    private final MemorySynchronizer synchronizer;
    
    private final ReadWriteLock stateLock = new ReentrantReadWriteLock();
    private final Map<String, MemoryState> globalState = new ConcurrentHashMap<>();
    private final Map<String, TrustLevel> agentTrustLevels = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    
    private volatile boolean initialized = false;
    
    public MemoryStateManager(TrustVerificationService trustService) {
        this.trustService = trustService;
        this.workingMemory = new WorkingMemory(this);
        this.episodicMemory = new EpisodicMemory(this);
        this.semanticMemory = new SemanticMemory(this);
        this.persistenceService = new StatePersistenceService(this);
        this.synchronizer = new MemorySynchronizer(this, trustService);
    }
    
    /**
     * Initialize memory state manager with trust verification
     */
    public void initialize() {
        stateLock.writeLock().lock();
        try {
            if (!initialized) {
                // Initialize memory components
                workingMemory.initialize();
                episodicMemory.initialize();
                semanticMemory.initialize();
                
                // Start persistence service
                persistenceService.start();
                
                // Start synchronization service
                synchronizer.start();
                
                // Schedule periodic memory consolidation
                scheduler.scheduleAtFixedRate(
                    this::consolidateMemory,
                    1, 1, TimeUnit.MINUTES
                );
                
                // Schedule trust level updates
                scheduler.scheduleAtFixedRate(
                    this::updateTrustLevels,
                    30, 30, TimeUnit.SECONDS
                );
                
                initialized = true;
            }
        } finally {
            stateLock.writeLock().unlock();
        }
    }
    
    /**
     * Store memory with trust verification
     */
    public CompletableFuture<Boolean> storeMemory(String agentId, String key, 
                                                  Object value, MemoryType type) {
        return CompletableFuture.supplyAsync(() -> {
            // Verify agent trust level
            TrustLevel trustLevel = getAgentTrustLevel(agentId);
            if (trustLevel == TrustLevel.UNTRUSTED) {
                return false;
            }
            
            stateLock.readLock().lock();
            try {
                MemoryState state = new MemoryState(key, value, type, agentId, trustLevel);
                
                // Store in appropriate memory tier
                boolean stored = false;
                switch (type) {
                    case WORKING:
                        stored = workingMemory.store(state);
                        break;
                    case EPISODIC:
                        stored = episodicMemory.store(state);
                        break;
                    case SEMANTIC:
                        stored = semanticMemory.store(state);
                        break;
                }
                
                if (stored) {
                    // Update global state
                    globalState.put(key, state);
                    
                    // Trigger synchronization if needed
                    if (trustLevel == TrustLevel.VERIFIED) {
                        synchronizer.synchronize(state);
                    }
                }
                
                return stored;
            } finally {
                stateLock.readLock().unlock();
            }
        });
    }
    
    /**
     * Retrieve memory with trust verification
     */
    public Optional<Object> retrieveMemory(String agentId, String key) {
        TrustLevel trustLevel = getAgentTrustLevel(agentId);
        if (trustLevel == TrustLevel.UNTRUSTED) {
            return Optional.empty();
        }
        
        stateLock.readLock().lock();
        try {
            MemoryState state = globalState.get(key);
            if (state != null && canAccessMemory(agentId, state)) {
                return Optional.of(state.getValue());
            }
            
            // Try retrieving from memory tiers
            Optional<Object> value = workingMemory.retrieve(key);
            if (!value.isPresent()) {
                value = episodicMemory.retrieve(key);
            }
            if (!value.isPresent()) {
                value = semanticMemory.retrieve(key);
            }
            
            return value;
        } finally {
            stateLock.readLock().unlock();
        }
    }
    
    /**
     * Query memories with pattern matching
     */
    public List<MemoryState> queryMemories(String agentId, MemoryQuery query) {
        TrustLevel trustLevel = getAgentTrustLevel(agentId);
        if (trustLevel == TrustLevel.UNTRUSTED) {
            return Collections.emptyList();
        }
        
        stateLock.readLock().lock();
        try {
            return globalState.values().stream()
                .filter(state -> canAccessMemory(agentId, state))
                .filter(state -> matchesQuery(state, query))
                .collect(Collectors.toList());
        } finally {
            stateLock.readLock().unlock();
        }
    }
    
    /**
     * Consolidate memories across tiers
     */
    private void consolidateMemory() {
        stateLock.writeLock().lock();
        try {
            // Move memories between tiers based on usage patterns
            workingMemory.consolidate();
            episodicMemory.consolidate();
            
            // Update semantic memory with long-term patterns
            semanticMemory.updateFromEpisodic(episodicMemory.getConsolidatedMemories());
            
            // Persist critical memories
            persistenceService.persistCriticalMemories(
                globalState.values().stream()
                    .filter(state -> state.isCritical())
                    .collect(Collectors.toList())
            );
        } finally {
            stateLock.writeLock().unlock();
        }
    }
    
    /**
     * Update agent trust levels
     */
    private void updateTrustLevels() {
        Map<String, TrustLevel> newLevels = trustService.getAllAgentTrustLevels();
        agentTrustLevels.putAll(newLevels);
    }
    
    /**
     * Get agent trust level
     */
    private TrustLevel getAgentTrustLevel(String agentId) {
        return agentTrustLevels.getOrDefault(agentId, 
            trustService.getAgentTrustLevel(agentId));
    }
    
    /**
     * Check if agent can access memory
     */
    private boolean canAccessMemory(String agentId, MemoryState state) {
        TrustLevel agentTrust = getAgentTrustLevel(agentId);
        TrustLevel memoryTrust = state.getTrustLevel();
        
        // Access control based on trust levels
        if (state.getOwnerAgentId().equals(agentId)) {
            return true; // Owner always has access
        }
        
        if (agentTrust == TrustLevel.VERIFIED) {
            return true; // Verified agents can access all memories
        }
        
        if (agentTrust == TrustLevel.TRUSTED && 
            memoryTrust != TrustLevel.VERIFIED) {
            return true; // Trusted agents can access non-verified memories
        }
        
        return false;
    }
    
    /**
     * Check if memory matches query
     */
    private boolean matchesQuery(MemoryState state, MemoryQuery query) {
        if (query.getType() != null && state.getType() != query.getType()) {
            return false;
        }
        
        if (query.getKeyPattern() != null && 
            !state.getKey().matches(query.getKeyPattern())) {
            return false;
        }
        
        if (query.getMinTrustLevel() != null &&
            state.getTrustLevel().ordinal() < query.getMinTrustLevel().ordinal()) {
            return false;
        }
        
        if (query.getTimeRange() != null &&
            !query.getTimeRange().contains(state.getTimestamp())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Shutdown memory state manager
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        persistenceService.stop();
        synchronizer.stop();
    }
    
    // Getters for memory components
    public WorkingMemory getWorkingMemory() { return workingMemory; }
    public EpisodicMemory getEpisodicMemory() { return episodicMemory; }
    public SemanticMemory getSemanticMemory() { return semanticMemory; }
    public StatePersistenceService getPersistenceService() { return persistenceService; }
    public MemorySynchronizer getSynchronizer() { return synchronizer; }
}

/**
 * Memory state representation
 */
class MemoryState {
    private final String key;
    private final Object value;
    private final MemoryType type;
    private final String ownerAgentId;
    private final TrustLevel trustLevel;
    private final long timestamp;
    private volatile int accessCount;
    private volatile long lastAccessTime;
    private volatile boolean critical;
    
    public MemoryState(String key, Object value, MemoryType type, 
                      String ownerAgentId, TrustLevel trustLevel) {
        this.key = key;
        this.value = value;
        this.type = type;
        this.ownerAgentId = ownerAgentId;
        this.trustLevel = trustLevel;
        this.timestamp = System.currentTimeMillis();
        this.lastAccessTime = timestamp;
        this.accessCount = 0;
        this.critical = false;
    }
    
    public void recordAccess() {
        accessCount++;
        lastAccessTime = System.currentTimeMillis();
    }
    
    // Getters and setters
    public String getKey() { return key; }
    public Object getValue() { return value; }
    public MemoryType getType() { return type; }
    public String getOwnerAgentId() { return ownerAgentId; }
    public TrustLevel getTrustLevel() { return trustLevel; }
    public long getTimestamp() { return timestamp; }
    public int getAccessCount() { return accessCount; }
    public long getLastAccessTime() { return lastAccessTime; }
    public boolean isCritical() { return critical; }
    public void setCritical(boolean critical) { this.critical = critical; }
}

/**
 * Memory types
 */
enum MemoryType {
    WORKING,   // Short-term, high-frequency access
    EPISODIC,  // Event-based, medium-term storage
    SEMANTIC   // Long-term, knowledge-based storage
}

/**
 * Memory query parameters
 */
class MemoryQuery {
    private MemoryType type;
    private String keyPattern;
    private TrustLevel minTrustLevel;
    private TimeRange timeRange;
    
    // Builder pattern implementation
    public static class Builder {
        private MemoryQuery query = new MemoryQuery();
        
        public Builder withType(MemoryType type) {
            query.type = type;
            return this;
        }
        
        public Builder withKeyPattern(String pattern) {
            query.keyPattern = pattern;
            return this;
        }
        
        public Builder withMinTrustLevel(TrustLevel level) {
            query.minTrustLevel = level;
            return this;
        }
        
        public Builder withTimeRange(TimeRange range) {
            query.timeRange = range;
            return this;
        }
        
        public MemoryQuery build() {
            return query;
        }
    }
    
    // Getters
    public MemoryType getType() { return type; }
    public String getKeyPattern() { return keyPattern; }
    public TrustLevel getMinTrustLevel() { return minTrustLevel; }
    public TimeRange getTimeRange() { return timeRange; }
}

/**
 * Time range for queries
 */
class TimeRange {
    private final long start;
    private final long end;
    
    public TimeRange(long start, long end) {
        this.start = start;
        this.end = end;
    }
    
    public boolean contains(long timestamp) {
        return timestamp >= start && timestamp <= end;
    }
}