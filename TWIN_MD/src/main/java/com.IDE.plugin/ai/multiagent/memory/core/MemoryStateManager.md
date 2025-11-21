# MemoryStateManager

## Package
`com.IDE.plugin.ai.multiagent.memory.core`

## Overview
MemoryStateManager serves as the central coordinator for memory operations with trust verification. It manages the three-tier memory architecture (Working, Episodic, Semantic) and ensures distributed validation across the agent network.

## Architecture Overview

### Three-Tier Memory System
1. **Working Memory**: Short-term, high-frequency access
2. **Episodic Memory**: Event-based, medium-term storage  
3. **Semantic Memory**: Long-term, knowledge-based storage

### Core Components
```java
private final WorkingMemory workingMemory;
private final EpisodicMemory episodicMemory;
private final SemanticMemory semanticMemory;
private final TrustVerificationService trustService;
private final StatePersistenceService persistenceService;
private final MemorySynchronizer synchronizer;
```

## Key Features

### Trust-Based Access Control
- Verifies agent trust levels before memory operations
- Enforces access control based on memory trust requirements
- Supports dynamic trust level updates

### Memory Lifecycle Management
- Automatic memory consolidation across tiers
- Periodic trust level synchronization
- Scheduled persistence of critical memories

### Distributed Synchronization
- Coordinates memory state across agent network
- Ensures consistency through synchronization service
- Validates operations through trust verification

## Core Methods

### Memory Operations
```java
// Store memory with trust verification
CompletableFuture<Boolean> storeMemory(String agentId, String key, 
                                       Object value, MemoryType type)

// Retrieve memory with trust verification
Optional<Object> retrieveMemory(String agentId, String key)

// Query memories with pattern matching
List<MemoryState> queryMemories(String agentId, MemoryQuery query)
```

### Initialization and Lifecycle
```java
// Initialize memory state manager with trust verification
void initialize()

// Shutdown memory state manager
void shutdown()
```

## Trust-Based Access Control

### Access Control Matrix
| Agent Trust | Memory Trust | Access Level |
|------------|--------------|--------------|
| UNTRUSTED  | Any         | No Access    |
| TRUSTED    | TRUSTED     | Read/Write   |
| TRUSTED    | VERIFIED    | No Access    |
| VERIFIED   | Any         | Full Access  |

### Special Cases
- **Owner Access**: Agents always have access to their own memories
- **Trust Promotion**: Higher trust agents can access lower trust memories
- **Dynamic Updates**: Trust levels updated every 30 seconds

## Memory Tier Coordination

### Storage Strategy
Memory is stored in appropriate tiers based on `MemoryType`:
```java
switch (type) {
    case WORKING:  -> workingMemory.store(state)
    case EPISODIC: -> episodicMemory.store(state)
    case SEMANTIC: -> semanticMemory.store(state)
}
```

### Retrieval Strategy
Memory retrieval follows a cascading approach:
1. Check global state cache first
2. Try working memory
3. Try episodic memory  
4. Try semantic memory

### Consolidation Process
Automatic consolidation occurs every minute:
1. **Working Memory**: Move infrequently accessed items to episodic
2. **Episodic Memory**: Consolidate mature episodes
3. **Semantic Learning**: Update semantic memory with episodic patterns
4. **Persistence**: Save critical memories to persistent storage

## Query System

### MemoryQuery Builder
```java
MemoryQuery query = new MemoryQuery.Builder()
    .withType(MemoryType.EPISODIC)
    .withKeyPattern("task\\..*")
    .withMinTrustLevel(TrustLevel.TRUSTED)
    .withTimeRange(new TimeRange(startTime, endTime))
    .build();
```

### Query Filtering
Queries are filtered by:
- **Memory Type**: Filter by working/episodic/semantic
- **Key Pattern**: Regex matching on memory keys
- **Trust Level**: Minimum trust level requirement
- **Time Range**: Temporal boundaries for memories

## MemoryState Class

### Properties
```java
private final String key;           // Unique memory identifier
private final Object value;         // Memory content
private final MemoryType type;      // Memory tier classification
private final String ownerAgentId;  // Owner agent identifier
private final TrustLevel trustLevel; // Trust level requirement
private final long timestamp;       // Creation timestamp
private volatile int accessCount;   // Access frequency tracking
private volatile boolean critical;  // Critical memory flag
```

### Access Tracking
- Records access count and last access time
- Used for consolidation decisions
- Supports memory usage analytics

## Scheduled Operations

### Memory Consolidation (1 minute intervals)
```java
scheduler.scheduleAtFixedRate(this::consolidateMemory, 1, 1, TimeUnit.MINUTES)
```

### Trust Level Updates (30 second intervals)
```java
scheduler.scheduleAtFixedRate(this::updateTrustLevels, 30, 30, TimeUnit.SECONDS)
```

## Integration Services

### StatePersistenceService
- Persists critical memories to permanent storage
- Ensures data consistency across system restarts
- Provides recovery mechanisms for system failures

### MemorySynchronizer
- Synchronizes memory state across agent network
- Coordinates distributed memory operations
- Resolves conflicts in distributed environments

### TrustVerificationService
- Validates agent identity and trust levels
- Provides security for memory operations
- Maintains agent trust metadata

## Configuration and Types

### MemoryType Enumeration
```java
enum MemoryType {
    WORKING,   // Short-term, high-frequency access
    EPISODIC,  // Event-based, medium-term storage
    SEMANTIC   // Long-term, knowledge-based storage
}
```

### TimeRange Class
```java
class TimeRange {
    private final long start;
    private final long end;
    
    boolean contains(long timestamp)
}
```

## Thread Safety
- Uses ReadWriteLock for state management
- ConcurrentHashMap for agent trust levels
- Scheduled thread pool for background operations
- Atomic operations for system state

## Error Handling
- Graceful degradation when services unavailable
- Defensive programming for trust verification
- Exception isolation in scheduled operations
- Proper resource cleanup on shutdown

## Performance Characteristics
- Asynchronous memory operations via CompletableFuture
- Cached trust levels for performance
- Batch operations for memory consolidation
- Efficient query processing with early filtering

## Usage Example
```java
// Initialize
MemoryStateManager manager = new MemoryStateManager(trustService);
manager.initialize();

// Store memory
CompletableFuture<Boolean> result = manager.storeMemory(
    "agent1", "task.result", taskData, MemoryType.WORKING);

// Retrieve memory
Optional<Object> memory = manager.retrieveMemory("agent1", "task.result");

// Query memories
MemoryQuery query = new MemoryQuery.Builder()
    .withType(MemoryType.EPISODIC)
    .withMinTrustLevel(TrustLevel.TRUSTED)
    .build();
List<MemoryState> results = manager.queryMemories("agent1", query);

// Shutdown
manager.shutdown();
```

## Security Considerations
- All memory operations require agent trust verification
- Memory access controlled by trust level hierarchy
- Synchronization includes trust validation
- Critical memories protected by enhanced verification