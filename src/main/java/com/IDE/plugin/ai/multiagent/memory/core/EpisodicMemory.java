package com.IDE.plugin.ai.multiagent.memory.core;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Episodic memory implementation for event-based, medium-term storage.
 * Stores memories as episodes with temporal relationships and context.
 */
public class EpisodicMemory {
    
    private static final int DEFAULT_CAPACITY = 10000;
    private static final long CONSOLIDATION_THRESHOLD_MS = TimeUnit.HOURS.toMillis(24);
    private static final int MAX_EPISODE_SIZE = 100;
    
    private final MemoryStateManager stateManager;
    private final Map<String, Episode> episodes;
    private final NavigableMap<Long, Set<String>> temporalIndex;
    private final Map<String, Set<String>> contextIndex;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    private final int capacity;
    private final AtomicLong episodeIdGenerator = new AtomicLong(0);
    private final ScheduledExecutorService consolidationExecutor;
    
    public EpisodicMemory(MemoryStateManager stateManager) {
        this(stateManager, DEFAULT_CAPACITY);
    }
    
    public EpisodicMemory(MemoryStateManager stateManager, int capacity) {
        this.stateManager = stateManager;
        this.capacity = capacity;
        this.episodes = new ConcurrentHashMap<>();
        this.temporalIndex = new ConcurrentSkipListMap<>();
        this.contextIndex = new ConcurrentHashMap<>();
        this.consolidationExecutor = Executors.newSingleThreadScheduledExecutor(
            r -> new Thread(r, "EpisodicMemory-Consolidation")
        );
    }
    
    /**
     * Initialize episodic memory
     */
    public void initialize() {
        // Schedule periodic consolidation
        consolidationExecutor.scheduleAtFixedRate(
            this::performConsolidation,
            6, 6, TimeUnit.HOURS
        );
    }
    
    /**
     * Store memory in episodic structure
     */
    public boolean store(MemoryState state) {
        if (state == null || state.getKey() == null) {
            return false;
        }
        
        lock.writeLock().lock();
        try {
            // Check capacity
            if (episodes.size() >= capacity) {
                evictOldestEpisodes();
            }
            
            // Find or create episode
            Episode episode = findOrCreateEpisode(state);
            
            // Add memory to episode
            boolean added = episode.addMemory(state);
            
            if (added) {
                // Update temporal index
                temporalIndex.computeIfAbsent(
                    state.getTimestamp(), 
                    k -> new ConcurrentHashSet<>()
                ).add(episode.getId());
                
                // Update context index
                updateContextIndex(episode, state);
            }
            
            return added;
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
            for (Episode episode : episodes.values()) {
                Optional<MemoryState> state = episode.getMemory(key);
                if (state.isPresent()) {
                    state.get().recordAccess();
                    return Optional.of(state.get().getValue());
                }
            }
            return Optional.empty();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Retrieve episodes by time range
     */
    public List<Episode> getEpisodesByTimeRange(long startTime, long endTime) {
        lock.readLock().lock();
        try {
            Set<String> episodeIds = new HashSet<>();
            NavigableMap<Long, Set<String>> subMap = 
                temporalIndex.subMap(startTime, true, endTime, true);
            
            for (Set<String> ids : subMap.values()) {
                episodeIds.addAll(ids);
            }
            
            return episodeIds.stream()
                .map(episodes::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Retrieve episodes by context
     */
    public List<Episode> getEpisodesByContext(String context) {
        lock.readLock().lock();
        try {
            Set<String> episodeIds = contextIndex.get(context);
            if (episodeIds == null) {
                return Collections.emptyList();
            }
            
            return episodeIds.stream()
                .map(episodes::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Get related episodes
     */
    public List<Episode> getRelatedEpisodes(String episodeId, int maxResults) {
        lock.readLock().lock();
        try {
            Episode episode = episodes.get(episodeId);
            if (episode == null) {
                return Collections.emptyList();
            }
            
            // Find episodes with similar contexts
            Set<String> relatedIds = new HashSet<>();
            for (String context : episode.getContexts()) {
                Set<String> contextEpisodes = contextIndex.get(context);
                if (contextEpisodes != null) {
                    relatedIds.addAll(contextEpisodes);
                }
            }
            
            relatedIds.remove(episodeId); // Remove self
            
            return relatedIds.stream()
                .map(episodes::get)
                .filter(Objects::nonNull)
                .sorted((e1, e2) -> Double.compare(
                    calculateSimilarity(episode, e2),
                    calculateSimilarity(episode, e1)
                ))
                .limit(maxResults)
                .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Consolidate episodic memories
     */
    public void consolidate() {
        lock.writeLock().lock();
        try {
            long currentTime = System.currentTimeMillis();
            List<Episode> toConsolidate = episodes.values().stream()
                .filter(episode -> shouldConsolidateEpisode(episode, currentTime))
                .collect(Collectors.toList());
            
            for (Episode episode : toConsolidate) {
                consolidateEpisode(episode);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Get consolidated memories for semantic learning
     */
    public List<ConsolidatedMemory> getConsolidatedMemories() {
        lock.readLock().lock();
        try {
            return episodes.values().stream()
                .filter(Episode::isConsolidated)
                .map(this::createConsolidatedMemory)
                .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Find or create episode for memory
     */
    private Episode findOrCreateEpisode(MemoryState state) {
        // Find recent episode with similar context
        long timeWindow = TimeUnit.MINUTES.toMillis(30);
        long startTime = state.getTimestamp() - timeWindow;
        
        List<Episode> recentEpisodes = getEpisodesByTimeRange(startTime, state.getTimestamp());
        
        for (Episode episode : recentEpisodes) {
            if (episode.canAddMemory(state)) {
                return episode;
            }
        }
        
        // Create new episode
        String episodeId = "episode_" + episodeIdGenerator.incrementAndGet();
        Episode newEpisode = new Episode(episodeId, state.getOwnerAgentId());
        episodes.put(episodeId, newEpisode);
        
        return newEpisode;
    }
    
    /**
     * Update context index
     */
    private void updateContextIndex(Episode episode, MemoryState state) {
        // Extract contexts from memory
        Set<String> contexts = extractContexts(state);
        episode.addContexts(contexts);
        
        // Update index
        for (String context : contexts) {
            contextIndex.computeIfAbsent(
                context, 
                k -> new ConcurrentHashSet<>()
            ).add(episode.getId());
        }
    }
    
    /**
     * Extract contexts from memory state
     */
    private Set<String> extractContexts(MemoryState state) {
        Set<String> contexts = new HashSet<>();
        
        // Add type as context
        contexts.add("type:" + state.getType());
        
        // Add trust level as context
        contexts.add("trust:" + state.getTrustLevel());
        
        // Extract from key patterns
        if (state.getKey().contains(".")) {
            String[] parts = state.getKey().split("\\.");
            if (parts.length > 0) {
                contexts.add("domain:" + parts[0]);
            }
        }
        
        // Extract from value if it's a map
        if (state.getValue() instanceof Map) {
            Map<?, ?> valueMap = (Map<?, ?>) state.getValue();
            valueMap.keySet().stream()
                .map(k -> "field:" + k)
                .forEach(contexts::add);
        }
        
        return contexts;
    }
    
    /**
     * Calculate similarity between episodes
     */
    private double calculateSimilarity(Episode e1, Episode e2) {
        Set<String> contexts1 = e1.getContexts();
        Set<String> contexts2 = e2.getContexts();
        
        Set<String> intersection = new HashSet<>(contexts1);
        intersection.retainAll(contexts2);
        
        Set<String> union = new HashSet<>(contexts1);
        union.addAll(contexts2);
        
        if (union.isEmpty()) {
            return 0.0;
        }
        
        // Jaccard similarity
        return (double) intersection.size() / union.size();
    }
    
    /**
     * Check if episode should be consolidated
     */
    private boolean shouldConsolidateEpisode(Episode episode, long currentTime) {
        return !episode.isConsolidated() &&
               currentTime - episode.getLastUpdateTime() > CONSOLIDATION_THRESHOLD_MS &&
               episode.getMemoryCount() > 5;
    }
    
    /**
     * Consolidate episode
     */
    private void consolidateEpisode(Episode episode) {
        // Mark as consolidated
        episode.setConsolidated(true);
        
        // Create summary of episode
        Map<String, Object> summary = new HashMap<>();
        summary.put("memoryCount", episode.getMemoryCount());
        summary.put("contexts", episode.getContexts());
        summary.put("startTime", episode.getStartTime());
        summary.put("endTime", episode.getEndTime());
        summary.put("importance", calculateEpisodeImportance(episode));
        
        episode.setSummary(summary);
    }
    
    /**
     * Calculate episode importance
     */
    private double calculateEpisodeImportance(Episode episode) {
        double accessScore = episode.getMemories().stream()
            .mapToInt(MemoryState::getAccessCount)
            .average()
            .orElse(0.0);
        
        double trustScore = episode.getMemories().stream()
            .mapToInt(m -> m.getTrustLevel().ordinal())
            .average()
            .orElse(0.0);
        
        double recencyScore = 1.0 / (1.0 + TimeUnit.MILLISECONDS.toDays(
            System.currentTimeMillis() - episode.getEndTime()));
        
        return (accessScore * 0.4 + trustScore * 0.4 + recencyScore * 0.2);
    }
    
    /**
     * Create consolidated memory from episode
     */
    private ConsolidatedMemory createConsolidatedMemory(Episode episode) {
        return new ConsolidatedMemory(
            episode.getId(),
            episode.getSummary(),
            episode.getContexts(),
            calculateEpisodeImportance(episode)
        );
    }
    
    /**
     * Evict oldest episodes
     */
    private void evictOldestEpisodes() {
        int toEvict = (int) (capacity * 0.1);
        
        List<Episode> sortedEpisodes = episodes.values().stream()
            .sorted(Comparator.comparingLong(Episode::getLastUpdateTime))
            .limit(toEvict)
            .collect(Collectors.toList());
        
        for (Episode episode : sortedEpisodes) {
            // Remove from indices
            for (MemoryState memory : episode.getMemories()) {
                Set<String> episodeIds = temporalIndex.get(memory.getTimestamp());
                if (episodeIds != null) {
                    episodeIds.remove(episode.getId());
                }
            }
            
            for (String context : episode.getContexts()) {
                Set<String> episodeIds = contextIndex.get(context);
                if (episodeIds != null) {
                    episodeIds.remove(episode.getId());
                }
            }
            
            // Remove episode
            episodes.remove(episode.getId());
        }
    }
    
    /**
     * Perform periodic consolidation
     */
    private void performConsolidation() {
        consolidate();
    }
    
    /**
     * Shutdown episodic memory
     */
    public void shutdown() {
        consolidationExecutor.shutdown();
        try {
            if (!consolidationExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                consolidationExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            consolidationExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}