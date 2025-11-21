package com.IDE.plugin.ai.multiagent.memory.core;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Semantic memory implementation for long-term, knowledge-based storage.
 * Stores abstract concepts, patterns, and relationships derived from episodic memories.
 */
public class SemanticMemory {
    
    private static final int DEFAULT_CAPACITY = 50000;
    private static final double ACTIVATION_DECAY_RATE = 0.1;
    private static final double MIN_ACTIVATION_THRESHOLD = 0.01;
    
    private final MemoryStateManager stateManager;
    private final Map<String, Concept> concepts;
    private final Map<String, Set<Relationship>> relationships;
    private final Map<String, Set<String>> categoryIndex;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    private final int capacity;
    private final ScheduledExecutorService maintenanceExecutor;
    private final ConceptLearner conceptLearner;
    
    public SemanticMemory(MemoryStateManager stateManager) {
        this(stateManager, DEFAULT_CAPACITY);
    }
    
    public SemanticMemory(MemoryStateManager stateManager, int capacity) {
        this.stateManager = stateManager;
        this.capacity = capacity;
        this.concepts = new ConcurrentHashMap<>();
        this.relationships = new ConcurrentHashMap<>();
        this.categoryIndex = new ConcurrentHashMap<>();
        this.conceptLearner = new ConceptLearner();
        this.maintenanceExecutor = Executors.newSingleThreadScheduledExecutor(
            r -> new Thread(r, "SemanticMemory-Maintenance")
        );
    }
    
    /**
     * Initialize semantic memory
     */
    public void initialize() {
        // Schedule periodic maintenance
        maintenanceExecutor.scheduleAtFixedRate(
            this::performMaintenance,
            1, 1, TimeUnit.DAYS
        );
        
        // Schedule activation decay
        maintenanceExecutor.scheduleAtFixedRate(
            this::decayActivations,
            1, 1, TimeUnit.HOURS
        );
    }
    
    /**
     * Store semantic concept
     */
    public boolean store(MemoryState state) {
        if (state == null || state.getKey() == null) {
            return false;
        }
        
        lock.writeLock().lock();
        try {
            // Check capacity
            if (concepts.size() >= capacity) {
                evictLowActivationConcepts();
            }
            
            // Create or update concept
            Concept concept = findOrCreateConcept(state);
            
            // Update concept properties
            concept.updateFromMemory(state);
            
            // Update relationships
            updateRelationships(concept, state);
            
            // Update category index
            updateCategoryIndex(concept);
            
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Retrieve semantic knowledge
     */
    public Optional<Object> retrieve(String key) {
        lock.readLock().lock();
        try {
            Concept concept = concepts.get(key);
            if (concept != null) {
                concept.activate();
                return Optional.of(concept.getValue());
            }
            
            // Try to find by related concepts
            for (Concept c : concepts.values()) {
                if (c.hasAlias(key)) {
                    c.activate();
                    return Optional.of(c.getValue());
                }
            }
            
            return Optional.empty();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Find related concepts
     */
    public List<Concept> findRelatedConcepts(String conceptId, int maxResults) {
        lock.readLock().lock();
        try {
            Set<Relationship> rels = relationships.get(conceptId);
            if (rels == null || rels.isEmpty()) {
                return Collections.emptyList();
            }
            
            return rels.stream()
                .sorted((r1, r2) -> Double.compare(r2.getStrength(), r1.getStrength()))
                .map(r -> concepts.get(r.getTargetId()))
                .filter(Objects::nonNull)
                .limit(maxResults)
                .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Find concepts by category
     */
    public List<Concept> findConceptsByCategory(String category) {
        lock.readLock().lock();
        try {
            Set<String> conceptIds = categoryIndex.get(category);
            if (conceptIds == null) {
                return Collections.emptyList();
            }
            
            return conceptIds.stream()
                .map(concepts::get)
                .filter(Objects::nonNull)
                .sorted((c1, c2) -> Double.compare(
                    c2.getActivation(), 
                    c1.getActivation()
                ))
                .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Update semantic memory from episodic consolidation
     */
    public void updateFromEpisodic(List<ConsolidatedMemory> consolidatedMemories) {
        lock.writeLock().lock();
        try {
            for (ConsolidatedMemory cm : consolidatedMemories) {
                // Learn new concepts
                List<Concept> learnedConcepts = conceptLearner.learnConcepts(cm);
                
                for (Concept concept : learnedConcepts) {
                    // Store or merge concept
                    Concept existing = concepts.get(concept.getId());
                    if (existing != null) {
                        existing.merge(concept);
                    } else {
                        concepts.put(concept.getId(), concept);
                    }
                    
                    // Update indices
                    updateCategoryIndex(concept);
                }
                
                // Learn relationships
                List<Relationship> learnedRelationships = 
                    conceptLearner.learnRelationships(cm, concepts);
                
                for (Relationship rel : learnedRelationships) {
                    relationships.computeIfAbsent(
                        rel.getSourceId(), 
                        k -> new ConcurrentHashSet<>()
                    ).add(rel);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Query semantic memory with inference
     */
    public List<InferenceResult> queryWithInference(String query, int maxResults) {
        lock.readLock().lock();
        try {
            // Parse query
            QueryParser parser = new QueryParser();
            ParsedQuery parsed = parser.parse(query);
            
            // Find matching concepts
            List<Concept> matchingConcepts = findMatchingConcepts(parsed);
            
            // Perform inference
            InferenceEngine engine = new InferenceEngine(concepts, relationships);
            List<InferenceResult> results = engine.infer(matchingConcepts, parsed);
            
            return results.stream()
                .sorted((r1, r2) -> Double.compare(r2.getConfidence(), r1.getConfidence()))
                .limit(maxResults)
                .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Find or create concept
     */
    private Concept findOrCreateConcept(MemoryState state) {
        String conceptId = generateConceptId(state);
        
        return concepts.computeIfAbsent(conceptId, id -> {
            Concept concept = new Concept(id);
            concept.addCategory(state.getType().toString());
            return concept;
        });
    }
    
    /**
     * Generate concept ID from memory state
     */
    private String generateConceptId(MemoryState state) {
        // Extract concept from key pattern
        String key = state.getKey();
        if (key.contains(".")) {
            return key.substring(0, key.lastIndexOf("."));
        }
        return key;
    }
    
    /**
     * Update relationships between concepts
     */
    private void updateRelationships(Concept concept, MemoryState state) {
        // Extract related concepts from memory value
        if (state.getValue() instanceof Map) {
            Map<?, ?> valueMap = (Map<?, ?>) state.getValue();
            
            for (Map.Entry<?, ?> entry : valueMap.entrySet()) {
                String relatedKey = entry.getKey().toString();
                Concept related = concepts.get(relatedKey);
                
                if (related != null && !related.getId().equals(concept.getId())) {
                    // Create or strengthen relationship
                    Relationship rel = new Relationship(
                        concept.getId(),
                        related.getId(),
                        RelationType.ASSOCIATION,
                        0.5
                    );
                    
                    relationships.computeIfAbsent(
                        concept.getId(), 
                        k -> new ConcurrentHashSet<>()
                    ).add(rel);
                }
            }
        }
    }
    
    /**
     * Update category index
     */
    private void updateCategoryIndex(Concept concept) {
        for (String category : concept.getCategories()) {
            categoryIndex.computeIfAbsent(
                category, 
                k -> new ConcurrentHashSet<>()
            ).add(concept.getId());
        }
    }
    
    /**
     * Find matching concepts for query
     */
    private List<Concept> findMatchingConcepts(ParsedQuery query) {
        return concepts.values().stream()
            .filter(concept -> matchesQuery(concept, query))
            .collect(Collectors.toList());
    }
    
    /**
     * Check if concept matches query
     */
    private boolean matchesQuery(Concept concept, ParsedQuery query) {
        // Check keywords
        for (String keyword : query.getKeywords()) {
            if (concept.containsKeyword(keyword)) {
                return true;
            }
        }
        
        // Check categories
        for (String category : query.getCategories()) {
            if (concept.hasCategory(category)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Decay activation levels
     */
    private void decayActivations() {
        lock.writeLock().lock();
        try {
            for (Concept concept : concepts.values()) {
                concept.decayActivation(ACTIVATION_DECAY_RATE);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Evict concepts with low activation
     */
    private void evictLowActivationConcepts() {
        int toEvict = (int) (capacity * 0.1);
        
        List<Concept> sortedConcepts = concepts.values().stream()
            .filter(c -> c.getActivation() < MIN_ACTIVATION_THRESHOLD)
            .sorted(Comparator.comparingDouble(Concept::getActivation))
            .limit(toEvict)
            .collect(Collectors.toList());
        
        for (Concept concept : sortedConcepts) {
            // Remove from indices
            for (String category : concept.getCategories()) {
                Set<String> conceptIds = categoryIndex.get(category);
                if (conceptIds != null) {
                    conceptIds.remove(concept.getId());
                }
            }
            
            // Remove relationships
            relationships.remove(concept.getId());
            
            // Remove concept
            concepts.remove(concept.getId());
        }
    }
    
    /**
     * Perform periodic maintenance
     */
    private void performMaintenance() {
        lock.writeLock().lock();
        try {
            // Consolidate similar concepts
            consolidateSimilarConcepts();
            
            // Prune weak relationships
            pruneWeakRelationships();
            
            // Update concept statistics
            updateConceptStatistics();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Consolidate similar concepts
     */
    private void consolidateSimilarConcepts() {
        List<Concept> allConcepts = new ArrayList<>(concepts.values());
        
        for (int i = 0; i < allConcepts.size(); i++) {
            for (int j = i + 1; j < allConcepts.size(); j++) {
                Concept c1 = allConcepts.get(i);
                Concept c2 = allConcepts.get(j);
                
                if (calculateSimilarity(c1, c2) > 0.8) {
                    // Merge concepts
                    c1.merge(c2);
                    concepts.remove(c2.getId());
                    
                    // Update relationships
                    Set<Relationship> c2Rels = relationships.remove(c2.getId());
                    if (c2Rels != null) {
                        relationships.computeIfAbsent(
                            c1.getId(), 
                            k -> new ConcurrentHashSet<>()
                        ).addAll(c2Rels);
                    }
                }
            }
        }
    }
    
    /**
     * Calculate concept similarity
     */
    private double calculateSimilarity(Concept c1, Concept c2) {
        Set<String> keywords1 = c1.getKeywords();
        Set<String> keywords2 = c2.getKeywords();
        
        if (keywords1.isEmpty() || keywords2.isEmpty()) {
            return 0.0;
        }
        
        Set<String> intersection = new HashSet<>(keywords1);
        intersection.retainAll(keywords2);
        
        Set<String> union = new HashSet<>(keywords1);
        union.addAll(keywords2);
        
        return (double) intersection.size() / union.size();
    }
    
    /**
     * Prune weak relationships
     */
    private void pruneWeakRelationships() {
        for (Set<Relationship> rels : relationships.values()) {
            rels.removeIf(rel -> rel.getStrength() < 0.1);
        }
    }
    
    /**
     * Update concept statistics
     */
    private void updateConceptStatistics() {
        for (Concept concept : concepts.values()) {
            concept.updateStatistics();
        }
    }
    
    /**
     * Shutdown semantic memory
     */
    public void shutdown() {
        maintenanceExecutor.shutdown();
        try {
            if (!maintenanceExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                maintenanceExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            maintenanceExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}