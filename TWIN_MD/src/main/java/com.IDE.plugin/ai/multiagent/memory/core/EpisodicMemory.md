# EpisodicMemory

## Package
`com.IDE.plugin.ai.multiagent.memory.core`

## Overview
EpisodicMemory is a sophisticated implementation for event-based, medium-term storage. It organizes memories as episodes with temporal relationships and context, providing advanced consolidation and retrieval capabilities for agent learning and adaptation.

## Key Features

### Episode-Based Organization
- Groups related memories into episodes based on temporal proximity and context
- Maintains temporal and contextual indices for efficient retrieval
- Supports automatic episode consolidation for long-term learning

### Advanced Indexing
- **Temporal Index**: NavigableMap for time-based queries
- **Context Index**: Map-based indexing for context-driven retrieval
- **Relationship Tracking**: Similarity-based episode relationships

### Automatic Consolidation
- Periodic consolidation of mature episodes
- Importance scoring based on access patterns and trust levels
- Episode summarization for semantic memory transfer

## Core Architecture

### Episode Structure
Episodes contain:
- Multiple related memory states
- Contextual metadata
- Temporal boundaries (start/end times)
- Consolidation status and summaries
- Importance scores

### Memory Management
```java
private final Map<String, Episode> episodes;
private final NavigableMap<Long, Set<String>> temporalIndex;
private final Map<String, Set<String>> contextIndex;
```

## Core Methods

### Storage Operations
```java
// Store memory in episodic structure
boolean store(MemoryState state)

// Retrieve memory by key
Optional<Object> retrieve(String key)
```

### Episode Retrieval
```java
// Get episodes by time range
List<Episode> getEpisodesByTimeRange(long startTime, long endTime)

// Get episodes by context
List<Episode> getEpisodesByContext(String context)

// Get related episodes
List<Episode> getRelatedEpisodes(String episodeId, int maxResults)
```

### Consolidation
```java
// Consolidate episodic memories
void consolidate()

// Get consolidated memories for semantic learning
List<ConsolidatedMemory> getConsolidatedMemories()
```

## Episode Management

### Episode Creation
Episodes are created when:
- No suitable existing episode found within time window (30 minutes)
- Context similarity is insufficient
- Maximum episode size reached

### Episode Selection Algorithm
1. Find recent episodes within time window
2. Check context compatibility (`canAddMemory()`)
3. Create new episode if no suitable match found

### Context Extraction
Automatically extracts contexts from memory states:
- Memory type as context (`type:WORKING`)
- Trust level as context (`trust:VERIFIED`)
- Domain extraction from key patterns (`domain:task`)
- Field extraction from map values (`field:result`)

## Consolidation Process

### Consolidation Criteria
Episodes are consolidated when:
- Age exceeds 24 hours since last update
- Episode contains more than 5 memories
- Not already consolidated

### Consolidation Algorithm
1. **Mark as Consolidated**: Prevents re-processing
2. **Create Summary**: Extract key metadata and statistics
3. **Calculate Importance**: Combine access, trust, and recency scores
4. **Generate ConsolidatedMemory**: Package for semantic transfer

### Importance Scoring
```java
importance = (accessScore * 0.4) + (trustScore * 0.4) + (recencyScore * 0.2)
```
- **Access Score**: Average access count across memories
- **Trust Score**: Average trust level ordinal values
- **Recency Score**: Time-decay function based on episode age

## Memory Similarity and Relationships

### Episode Similarity
Uses Jaccard similarity on context sets:
```java
similarity = |intersection| / |union|
```

### Related Episodes
- Finds episodes sharing common contexts
- Sorts by similarity score
- Returns top N most related episodes

## Configuration Parameters
```java
DEFAULT_CAPACITY = 10000                    // Maximum episodes
CONSOLIDATION_THRESHOLD_MS = 24 hours       // Consolidation age threshold
MAX_EPISODE_SIZE = 100                      // Maximum memories per episode
```

## Scheduled Operations

### Consolidation Schedule
- Runs every 6 hours
- Processes episodes meeting consolidation criteria
- Updates episode summaries and importance scores

### Capacity Management
- Evicts oldest episodes when capacity reached
- Removes 10% of capacity when threshold exceeded
- Cleans up temporal and context indices

## Thread Safety
- Uses ReadWriteLock for concurrent access
- ConcurrentHashMap for thread-safe collections
- ConcurrentSkipListMap for temporal indexing
- Single-threaded executor for consolidation

## Integration Points

### MemoryStateManager Integration
- Receives MemoryState objects for storage
- Provides consolidated memories for semantic learning
- Coordinates with working memory for memory tier management

### Trust System Integration
- Respects trust levels in importance calculations
- Validates memory states during storage
- Supports trust-based access control

## Performance Characteristics
- O(log n) temporal range queries via NavigableMap
- O(1) context-based lookups via HashMap indices
- Periodic consolidation minimizes memory footprint
- Efficient similarity calculations using set operations

## Usage Example
```java
EpisodicMemory episodicMemory = new EpisodicMemory(stateManager);
episodicMemory.initialize();

// Store memory
MemoryState state = new MemoryState(key, value, type, agentId, trustLevel);
episodicMemory.store(state);

// Query by time range
List<Episode> episodes = episodicMemory.getEpisodesByTimeRange(
    startTime, endTime);

// Get consolidated memories
List<ConsolidatedMemory> consolidated = 
    episodicMemory.getConsolidatedMemories();

// Find related episodes
List<Episode> related = episodicMemory.getRelatedEpisodes(
    "episode_123", 5);
```

## Error Handling
- Graceful handling of null or invalid inputs
- Defensive programming for episode operations
- Exception isolation in scheduled operations
- Proper cleanup on shutdown