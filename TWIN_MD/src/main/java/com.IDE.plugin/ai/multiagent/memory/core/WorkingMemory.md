# WorkingMemory

## Package
`com.IDE.plugin.ai.multiagent.memory.core`

## Overview
WorkingMemory provides short-term, high-frequency memory access with limited capacity and automatic eviction. It serves as the first tier in the memory hierarchy, optimized for fast access patterns and automatic promotion to episodic memory based on usage characteristics.

## Key Features

### High-Performance Access
- O(1) memory operations using ConcurrentHashMap
- Thread-safe concurrent access patterns
- Optimized for high-frequency read/write operations

### Intelligent Eviction
- LRU-based eviction strategy
- Access pattern tracking for informed decisions
- Automatic promotion to episodic memory for important memories

### Adaptive Memory Management
- Usage-based consolidation decisions
- Configurable capacity limits
- Automatic cleanup of stale memories

## Core Architecture

### Storage Structure
```java
private final Map<String, MemoryEntry> memoryStore;    // Primary storage
private final PriorityQueue<MemoryEntry> evictionQueue; // LRU eviction tracking
private final AtomicInteger size;                       // Thread-safe size tracking
```

### Memory Entry Wrapper
Each memory is wrapped in a MemoryEntry that tracks:
- Original MemoryState
- Creation timestamp
- Last access time
- Access count statistics

## Core Methods

### Basic Operations
```java
// Store memory entry
boolean store(MemoryState state)

// Retrieve memory by key
Optional<Object> retrieve(String key)

// Check if memory exists
boolean contains(String key)

// Remove memory by key
boolean remove(String key)
```

### Management Operations
```java
// Get all memory keys
Set<String> getKeys()

// Get memory statistics
MemoryStats getStats()

// Consolidate memory
void consolidate()
```

## Memory Lifecycle

### Storage Process
1. **Capacity Check**: Verify available space
2. **Eviction**: Remove LRU items if capacity exceeded
3. **Entry Creation**: Wrap MemoryState in MemoryEntry
4. **Index Update**: Update eviction queue
5. **Size Tracking**: Increment atomic counter

### Retrieval Process
1. **Lookup**: Find entry in memory store
2. **Access Recording**: Update access statistics
3. **Queue Update**: Refresh position in eviction queue
4. **Value Return**: Extract value from MemoryState

### Eviction Strategy
When capacity is reached:
1. **Calculate Eviction Size**: 20% of total capacity
2. **LRU Selection**: Choose least recently used entries
3. **Important Memory Check**: Promote frequently accessed memories
4. **Cleanup**: Remove from store and update indices

## Access Pattern Tracking

### MemoryEntry Statistics
```java
private final long creationTime;        // Entry creation timestamp
private volatile long lastAccessTime;   // Last access timestamp
private final AtomicInteger accessCount; // Total access count
```

### Access Recording
```java
public void recordAccess() {
    lastAccessTime = System.currentTimeMillis();
    accessCount.incrementAndGet();
    state.recordAccess(); // Update underlying MemoryState
}
```

## Consolidation Process

### Consolidation Criteria
Memories are consolidated to episodic storage when:
- Not accessed for more than 5 minutes
- Access rate is less than 1 access per minute
- Memory shows low activity patterns

### Consolidation Algorithm
1. **Identify Candidates**: Find memories meeting consolidation criteria
2. **Type Conversion**: Change MemoryType to EPISODIC
3. **Transfer**: Store in episodic memory via state manager
4. **Cleanup**: Remove from working memory

### Consolidation Decision Logic
```java
private boolean shouldConsolidate(MemoryEntry entry, long currentTime) {
    long timeSinceLastAccess = currentTime - entry.getLastAccessTime();
    if (timeSinceLastAccess > EVICTION_THRESHOLD_MS) {
        double accessRate = (double) entry.getAccessCount() / 
            TimeUnit.MILLISECONDS.toMinutes(currentTime - entry.getCreationTime());
        return accessRate < 1.0; // Less than 1 access per minute
    }
    return false;
}
```

## Memory Statistics

### MemoryStats Class
Provides comprehensive memory usage analytics:
```java
public static class MemoryStats {
    private final int size;                  // Current memory count
    private final int capacity;              // Maximum capacity
    private final long totalAccessCount;     // Total access operations
    private final double avgAccessCount;     // Average accesses per memory
    
    public double getUtilization();          // Memory utilization percentage
}
```

### Statistics Calculation
- **Size**: Current number of stored memories
- **Utilization**: Percentage of capacity used
- **Access Patterns**: Total and average access counts
- **Performance Metrics**: Access frequency analysis

## Configuration Parameters
```java
DEFAULT_CAPACITY = 1000                      // Maximum memory entries
EVICTION_THRESHOLD_MS = 5 minutes           // Consolidation age threshold  
EVICTION_PERCENTAGE = 0.2                   // 20% of capacity evicted
```

## Scheduled Operations

### Periodic Cleanup (1 minute intervals)
```java
cleanupExecutor.scheduleAtFixedRate(this::performCleanup, 1, 1, TimeUnit.MINUTES)
```

#### Cleanup Process:
1. **Stale Detection**: Find memories not accessed for 10 minutes
2. **Removal**: Delete stale memories from store
3. **Index Cleanup**: Update eviction queue
4. **Size Adjustment**: Decrement size counter

## Thread Safety Features

### Concurrent Data Structures
- **ConcurrentHashMap**: Thread-safe memory storage
- **AtomicInteger**: Thread-safe size tracking
- **ReadWriteLock**: Coordinated access control

### Synchronization Strategy
- Read operations use read lock for maximum concurrency
- Write operations use write lock for consistency
- Atomic operations for statistics updates

## Performance Characteristics

### Time Complexity
- **Store**: O(log n) due to priority queue operations
- **Retrieve**: O(1) hash map lookup
- **Remove**: O(log n) due to priority queue update
- **Contains**: O(1) hash map lookup

### Memory Efficiency
- Minimal overhead with MemoryEntry wrapper
- Efficient eviction with priority queue
- Automatic cleanup prevents memory leaks

## Integration Points

### MemoryStateManager Integration
- Receives MemoryState objects for storage
- Consolidates memories to episodic tier
- Coordinates with other memory tiers

### Episodic Memory Coordination
- Automatic promotion of important memories
- Consolidation based on access patterns
- Preservation of memory continuity

## Error Handling
- Null safety checks for all operations
- Graceful handling of concurrent modifications
- Exception isolation in scheduled operations
- Proper resource cleanup on shutdown

## Usage Example
```java
WorkingMemory workingMemory = new WorkingMemory(stateManager);
workingMemory.initialize();

// Store memory
MemoryState state = new MemoryState(key, value, MemoryType.WORKING, 
    agentId, trustLevel);
boolean stored = workingMemory.store(state);

// Retrieve memory
Optional<Object> result = workingMemory.retrieve(key);

// Check existence
if (workingMemory.contains(key)) {
    // Memory exists
}

// Get statistics
WorkingMemory.MemoryStats stats = workingMemory.getStats();
System.out.println("Utilization: " + stats.getUtilization() + "%");

// Trigger consolidation
workingMemory.consolidate();

// Cleanup
workingMemory.shutdown();
```

## Best Practices
- Monitor memory utilization to prevent capacity issues
- Use appropriate consolidation thresholds for your use case
- Regular statistics monitoring for performance optimization
- Proper shutdown to ensure resource cleanup