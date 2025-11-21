# SemanticMemory

## Package
`com.IDE.plugin.ai.multiagent.memory.core`

## Overview
SemanticMemory provides long-term, knowledge-based storage for abstract concepts, patterns, and relationships derived from episodic memories. It implements sophisticated concept learning, relationship modeling, and inference capabilities for advanced agent cognition.

## Key Features

### Concept-Based Knowledge Representation
- Stores abstract concepts with activation levels
- Maintains relationships between concepts
- Supports category-based organization and retrieval

### Advanced Learning Capabilities
- Automatic concept learning from episodic memory
- Relationship inference and strengthening
- Pattern recognition across memory episodes

### Inference Engine
- Query processing with semantic inference
- Confidence-based result ranking
- Context-aware knowledge retrieval

## Core Architecture

### Knowledge Structures
```java
private final Map<String, Concept> concepts;           // Core concept storage
private final Map<String, Set<Relationship>> relationships; // Concept relationships
private final Map<String, Set<String>> categoryIndex;  // Category-based indexing
```

### Supporting Components
```java
private final ConceptLearner conceptLearner;           // Learning algorithms
private final ScheduledExecutorService maintenanceExecutor; // Background processing
```

## Core Methods

### Storage and Retrieval
```java
// Store semantic concept
boolean store(MemoryState state)

// Retrieve semantic knowledge
Optional<Object> retrieve(String key)
```

### Concept Discovery
```java
// Find related concepts
List<Concept> findRelatedConcepts(String conceptId, int maxResults)

// Find concepts by category
List<Concept> findConceptsByCategory(String category)
```

### Learning Integration
```java
// Update semantic memory from episodic consolidation
void updateFromEpisodic(List<ConsolidatedMemory> consolidatedMemories)

// Query semantic memory with inference
List<InferenceResult> queryWithInference(String query, int maxResults)
```

## Concept Management

### Concept Structure
Each concept contains:
- **Unique ID**: Generated from memory patterns
- **Categories**: Classification tags for organization
- **Keywords**: Searchable terms extracted from content
- **Activation Level**: Dynamic importance score
- **Aliases**: Alternative identifiers for flexible retrieval
- **Statistics**: Usage and performance metrics

### Concept Creation
Concepts are created through:
1. **Direct Storage**: From memory state inputs
2. **Learning Process**: From episodic memory consolidation
3. **Merge Operations**: Combining similar concepts

### Concept ID Generation
```java
private String generateConceptId(MemoryState state) {
    String key = state.getKey();
    if (key.contains(".")) {
        return key.substring(0, key.lastIndexOf("."));
    }
    return key;
}
```

## Relationship Modeling

### Relationship Types
```java
enum RelationType {
    ASSOCIATION,    // General associative relationship
    CAUSATION,      // Causal relationship
    SIMILARITY,     // Similarity-based relationship
    TEMPORAL        // Temporal sequence relationship
}
```

### Relationship Properties
- **Source and Target**: Concept identifiers
- **Type**: Relationship classification
- **Strength**: Numeric weight (0.0 to 1.0)
- **Confidence**: Reliability measure

### Relationship Learning
Relationships are learned through:
1. **Co-occurrence Analysis**: Concepts appearing together
2. **Temporal Patterns**: Sequential concept activation
3. **Contextual Similarity**: Shared context analysis

## Activation and Decay

### Activation Model
Concepts have dynamic activation levels that:
- Increase on access (`activate()` method)
- Decay over time (hourly decay process)
- Influence retrieval priority and inference

### Decay Parameters
```java
ACTIVATION_DECAY_RATE = 0.1          // 10% decay per hour
MIN_ACTIVATION_THRESHOLD = 0.01      // Minimum viable activation
```

### Eviction Strategy
Low-activation concepts are evicted when:
- Activation falls below threshold
- Memory capacity is exceeded
- Maintenance operations detect unused concepts

## Learning Process

### Episodic Integration
Semantic learning from episodic memories involves:
1. **Concept Extraction**: Identify new concepts in consolidated episodes
2. **Relationship Discovery**: Find patterns between concepts
3. **Knowledge Consolidation**: Merge with existing semantic knowledge

### ConceptLearner Operations
```java
// Learn concepts from consolidated memory
List<Concept> learnedConcepts = conceptLearner.learnConcepts(consolidatedMemory);

// Learn relationships between concepts
List<Relationship> relationships = conceptLearner.learnRelationships(
    consolidatedMemory, existingConcepts);
```

## Inference Engine

### Query Processing
Semantic queries support:
- **Keyword Matching**: Direct concept keyword search
- **Category Filtering**: Search within concept categories
- **Relationship Traversal**: Following concept relationships
- **Confidence Ranking**: Result ordering by inference confidence

### Query Parser
Parses natural language queries into structured search parameters:
```java
QueryParser parser = new QueryParser();
ParsedQuery parsed = parser.parse("find tasks related to data analysis");
```

### Inference Results
Results include:
- **Concept**: Matching concept object
- **Confidence**: Inference confidence score
- **Path**: Reasoning path for transparency
- **Context**: Supporting contextual information

## Maintenance Operations

### Scheduled Maintenance (Daily)
```java
maintenanceExecutor.scheduleAtFixedRate(this::performMaintenance, 1, 1, TimeUnit.DAYS)
```

#### Maintenance Tasks:
1. **Concept Consolidation**: Merge similar concepts
2. **Relationship Pruning**: Remove weak relationships  
3. **Statistics Update**: Refresh concept metrics

### Concept Similarity
Uses Jaccard similarity for concept comparison:
```java
similarity = |intersection(keywords1, keywords2)| / |union(keywords1, keywords2)|
```

### Consolidation Threshold
Concepts with similarity > 0.8 are candidates for merging.

## Configuration Parameters
```java
DEFAULT_CAPACITY = 50000                     // Maximum concepts
ACTIVATION_DECAY_RATE = 0.1                 // Hourly decay rate
MIN_ACTIVATION_THRESHOLD = 0.01             // Eviction threshold
```

## Index Management

### Category Index
Organizes concepts by category for efficient retrieval:
```java
categoryIndex.get("task_type") -> Set<ConceptId>
```

### Index Updates
Indices are automatically maintained during:
- Concept creation and updates
- Category assignments
- Concept eviction and cleanup

## Thread Safety
- ReadWriteLock for concurrent access control
- ConcurrentHashMap for thread-safe storage
- Single-threaded maintenance executor
- Atomic operations for concept updates

## Performance Characteristics
- O(1) concept lookup by ID
- O(log n) category-based queries
- O(n) similarity calculations (optimized with caching)
- Periodic maintenance minimizes memory footprint

## Integration Points

### MemoryStateManager Integration
- Receives memory states for concept creation
- Provides semantic knowledge for intelligent retrieval
- Coordinates with episodic memory for learning

### Trust System Integration
- Respects trust levels in concept importance
- Validates semantic operations through trust service
- Supports secure knowledge sharing

## Usage Example
```java
SemanticMemory semanticMemory = new SemanticMemory(stateManager);
semanticMemory.initialize();

// Store semantic concept
MemoryState state = new MemoryState(key, conceptData, 
    MemoryType.SEMANTIC, agentId, trustLevel);
semanticMemory.store(state);

// Find related concepts
List<Concept> related = semanticMemory.findRelatedConcepts("task_123", 5);

// Query with inference
List<InferenceResult> results = semanticMemory.queryWithInference(
    "data processing algorithms", 10);

// Update from episodic learning
List<ConsolidatedMemory> consolidated = episodicMemory.getConsolidatedMemories();
semanticMemory.updateFromEpisodic(consolidated);
```

## Error Handling
- Graceful handling of malformed queries
- Defensive programming for learning operations
- Exception isolation in maintenance tasks
- Proper cleanup during shutdown operations