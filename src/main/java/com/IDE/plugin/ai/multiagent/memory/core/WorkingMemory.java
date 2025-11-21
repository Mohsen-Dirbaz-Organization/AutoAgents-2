package com.IDE.plugin.ai.multiagent.memory.core;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Working memory implementation for short-term, high-frequency access.
 * Provides fast access with limited capacity and automatic eviction.
 */
public class WorkingMemory {
    
    private static final int DEFAULT_CAPACITY = 1000;
    private static final long EVICTION_THRESHOLD_MS = TimeUnit.MINUTES.toMillis(5);
    private static final double EVICTION_PERCENTAGE = 0.2;
    
    private final MemoryStateManager stateManager;
    private final Map<String, MemoryEntry> memoryStore;
    private final PriorityQueue<MemoryEntry> evictionQueue;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final AtomicInteger size = new AtomicInteger(0);
    
    private final int capacity;
    private final ScheduledExecutorService cleanupExecutor;
    
    public WorkingMemory(MemoryStateManager stateManager) {
        this(stateManager, DEFAULT_CAPACITY);
    }
    
    public WorkingMemory(MemoryStateManager stateManager, int capacity) {
        this.stateManager = stateManager;
        this.capacity = capacity;
        this.memoryStore = new ConcurrentHashMap<>();
        this.evictionQueue = new PriorityQueue<>(
            Comparator.comparingLong(MemoryEntry::getLastAccessTime)
        );
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(
            r -> new Thread(r, "WorkingMemory-Cleanup")
        );
    }
    
    /**
     * Initialize working memory
     */
    public void initialize() {
        // Schedule periodic cleanup
        cleanupExecutor.scheduleAtFixedRate(
            this::performCleanup,
            1, 1, TimeUnit.MINUTES
        );
    }
    
    /**
     * Store memory entry
     */
    public boolean store(MemoryState state) {
        if (state == null || state.getKey() == null) {
            return false;
        }
        
        lock.writeLock().lock();
        try {
            // Check capacity and evict if necessary
            if (size.get() >= capacity) {
                evictLeastRecentlyUsed();
            }
            
            MemoryEntry entry = new MemoryEntry(state);
            MemoryEntry existing = memoryStore.put(state.getKey(), entry);
            
            if (existing == null) {
                size.incrementAndGet();
                evictionQueue.offer(entry);
            } else {
                // Update eviction queue
                evictionQueue.remove(existing);
                evictionQueue.offer(entry);
            }
            
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Retrieve memory by key
     */
    public Optional<Object> retrieve(String key) {
        lock.readLock().lock();
        try {
            MemoryEntry entry = memoryStore.get(key);
            if (entry != null) {
                entry.recordAccess();
                return Optional.of(entry.getState().getValue());
            }
            return Optional.empty();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Check if memory exists
     */
    public boolean contains(String key) {
        lock.readLock().lock();
        try {
            return memoryStore.containsKey(key);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Remove memory by key
     */
    public boolean remove(String key) {
        lock.writeLock().lock();
        try {
            MemoryEntry removed = memoryStore.remove(key);
            if (removed != null) {
                evictionQueue.remove(removed);
                size.decrementAndGet();
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Get all memory keys
     */
    public Set<String> getKeys() {
        lock.readLock().lock();
        try {
            return new HashSet<>(memoryStore.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Get memory statistics
     */
    public MemoryStats getStats() {
        lock.readLock().lock();
        try {
            long totalAccess = memoryStore.values().stream()
                .mapToLong(MemoryEntry::getAccessCount)
                .sum();
            
            double avgAccessCount = memoryStore.isEmpty() ? 0 :
                (double) totalAccess / memoryStore.size();
            
            return new MemoryStats(
                size.get(),
                capacity,
                totalAccess,
                avgAccessCount
            );
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Consolidate memory - move infrequently accessed items to episodic memory
     */
    public void consolidate() {
        lock.writeLock().lock();
        try {
            long currentTime = System.currentTimeMillis();
            List<MemoryEntry> toConsolidate = memoryStore.values().stream()
                .filter(entry -> shouldConsolidate(entry, currentTime))
                .collect(Collectors.toList());
            
            for (MemoryEntry entry : toConsolidate) {
                // Move to episodic memory
                MemoryState state = entry.getState();
                state.setType(MemoryType.EPISODIC);
                stateManager.getEpisodicMemory().store(state);
                
                // Remove from working memory
                memoryStore.remove(state.getKey());
                evictionQueue.remove(entry);
                size.decrementAndGet();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Check if memory should be consolidated
     */
    private boolean shouldConsolidate(MemoryEntry entry, long currentTime) {
        // Consolidate if not accessed recently and has low access frequency
        long timeSinceLastAccess = currentTime - entry.getLastAccessTime();
        if (timeSinceLastAccess > EVICTION_THRESHOLD_MS) {
            double accessRate = (double) entry.getAccessCount() / 
                TimeUnit.MILLISECONDS.toMinutes(currentTime - entry.getCreationTime());
            return accessRate < 1.0; // Less than 1 access per minute
        }
        return false;
    }
    
    /**
     * Evict least recently used entries
     */
    private void evictLeastRecentlyUsed() {
        int toEvict = (int) (capacity * EVICTION_PERCENTAGE);
        List<MemoryEntry> evicted = new ArrayList<>();
        
        for (int i = 0; i < toEvict && !evictionQueue.isEmpty(); i++) {
            MemoryEntry entry = evictionQueue.poll();
            if (entry != null) {
                evicted.add(entry);
                memoryStore.remove(entry.getState().getKey());
                size.decrementAndGet();
            }
        }
        
        // Move evicted entries to episodic memory if they're important
        for (MemoryEntry entry : evicted) {
            if (entry.getAccessCount() > 5) {
                MemoryState state = entry.getState();
                state.setType(MemoryType.EPISODIC);
                stateManager.getEpisodicMemory().store(state);
            }
        }
    }
    
    /**
     * Perform periodic cleanup
     */
    private void performCleanup() {
        lock.writeLock().lock();
        try {
            long currentTime = System.currentTimeMillis();
            List<String> toRemove = new ArrayList<>();
            
            for (Map.Entry<String, MemoryEntry> entry : memoryStore.entrySet()) {
                if (currentTime - entry.getValue().getLastAccessTime() > 
                    TimeUnit.MINUTES.toMillis(10)) {
                    toRemove.add(entry.getKey());
                }
            }
            
            for (String key : toRemove) {
                remove(key);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Shutdown working memory
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Memory entry wrapper
     */
    private static class MemoryEntry {
        private final MemoryState state;
        private final long creationTime;
        private volatile long lastAccessTime;
        private final AtomicInteger accessCount;
        
        public MemoryEntry(MemoryState state) {
            this.state = state;
            this.creationTime = System.currentTimeMillis();
            this.lastAccessTime = creationTime;
            this.accessCount = new AtomicInteger(1);
        }
        
        public void recordAccess() {
            lastAccessTime = System.currentTimeMillis();
            accessCount.incrementAndGet();
            state.recordAccess();
        }
        
        public MemoryState getState() { return state; }
        public long getCreationTime() { return creationTime; }
        public long getLastAccessTime() { return lastAccessTime; }
        public int getAccessCount() { return accessCount.get(); }
    }
    
    /**
     * Memory statistics
     */
    public static class MemoryStats {
        private final int size;
        private final int capacity;
        private final long totalAccessCount;
        private final double avgAccessCount;
        
        public MemoryStats(int size, int capacity, 
                          long totalAccessCount, double avgAccessCount) {
            this.size = size;
            this.capacity = capacity;
            this.totalAccessCount = totalAccessCount;
            this.avgAccessCount = avgAccessCount;
        }
        
        public int getSize() { return size; }
        public int getCapacity() { return capacity; }
        public long getTotalAccessCount() { return totalAccessCount; }
        public double getAvgAccessCount() { return avgAccessCount; }
        public double getUtilization() { return (double) size / capacity; }
    }
}