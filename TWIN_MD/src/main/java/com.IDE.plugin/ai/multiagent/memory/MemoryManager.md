# MemoryManager

## Package
`com.IDE.plugin.ai.multiagent.memory`

## Overview
The MemoryManager is the main facade for handling agent memory operations. It provides a simplified interface for storing and retrieving memories, managing both short-term and long-term memory with automatic promotion between tiers.

## Class Hierarchy
```
MemoryManager
├── Memory (inner class)
├── Experience (static inner class)
└── MemoryStats (static inner class)
```

## Key Features

### Memory Tiers
- **Short-term memory**: Fast access, limited capacity (default: 100 items)
- **Long-term memory**: Larger capacity for important memories (default: 1000 items)
- **Episodic memory**: Sequential experiences with context

### Automatic Memory Management
- Promotes frequently accessed memories from short-term to long-term
- Evicts oldest memories when capacity limits are reached
- Maintains access patterns for optimization

## Core Methods

### Memory Operations
```java
// Store a memory item for an agent
void store(String agentId, String key, Object value)

// Retrieve a memory item for an agent
Object retrieve(String agentId, String key)

// Clear memory for specific agent
void clearAgentMemory(String agentId)
```

### Experience Management
```java
// Add an experience to agent's episodic memory
void addExperience(String agentId, Experience experience)

// Get recent experiences
List<Experience> getRecentExperiences(String agentId, int count)

// Search experiences by query
List<Experience> searchExperiences(String agentId, String query)
```

### Statistics
```java
// Get memory statistics for an agent
MemoryStats getStats(String agentId)
```

## Inner Classes

### Memory (Private)
Manages the three-tier memory structure for individual agents:
- Short-term memory (ConcurrentHashMap)
- Long-term memory (ConcurrentHashMap)  
- Episodic memory (ConcurrentLinkedDeque)

#### Key Operations:
- `store()`: Stores in short-term, promotes to long-term when needed
- `retrieve()`: Checks short-term first, then long-term, promotes on access
- `promoteToLongTerm()`: FIFO promotion strategy
- `addExperience()`: Adds to episodic memory with size limits

### Experience (Static)
Represents an episodic memory entry with:
- Type and description
- Contextual metadata
- Timestamp for temporal ordering
- Pattern matching capabilities

#### Properties:
```java
private final String type;
private final String description;
private final Map<String, Object> context;
private final long timestamp;
```

### MemoryStats (Static)
Provides memory usage statistics:
- Short-term memory size
- Long-term memory size
- Episodic memory size
- Total memory utilization

## Configuration
- `maxShortTermSize`: 100 items (configurable)
- `maxLongTermSize`: 1000 items (configurable)
- Automatic promotion when short-term exceeds capacity
- LRU eviction for long-term memory

## Thread Safety
- Uses ConcurrentHashMap for thread-safe operations
- ConcurrentLinkedDeque for episodic memory
- Atomic operations for memory management

## Usage Example
```java
MemoryManager memoryManager = new MemoryManager();

// Store agent memory
memoryManager.store("agent1", "task.result", taskResult);

// Retrieve memory
Object result = memoryManager.retrieve("agent1", "task.result");

// Add experience
Experience exp = new Experience("task_completion", 
    "Successfully completed data analysis", context);
memoryManager.addExperience("agent1", exp);

// Get statistics
MemoryStats stats = memoryManager.getStats("agent1");
System.out.println("Total memories: " + stats.getTotalSize());
```

## Integration Points
- Provides foundation for higher-level memory systems
- Can be extended with persistence and synchronization
- Supports agent-specific memory isolation
- Compatible with trust-based access control

## Performance Characteristics
- O(1) memory access for cached items
- O(n) search operations for experiences
- Automatic memory optimization through usage patterns
- Configurable capacity limits for memory management