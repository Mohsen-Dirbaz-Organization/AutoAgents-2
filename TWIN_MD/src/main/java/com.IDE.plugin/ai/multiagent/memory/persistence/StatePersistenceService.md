# StatePersistenceService

## Package
`com.IDE.plugin.ai.multiagent.memory.persistence`

## Overview
StatePersistenceService manages memory persistence with distributed validation, ensuring data integrity and consistency across system restarts. It implements a robust persistence strategy using Write-Ahead Logging (WAL) and checkpoint-based recovery for enterprise-grade reliability.

## Key Features

### Dual Persistence Strategy
- **Write-Ahead Logging (WAL)**: Real-time operation logging
- **Checkpoint System**: Periodic full state snapshots
- **Validation Framework**: Checksum-based integrity verification

### Data Integrity Assurance
- SHA-256 checksums for individual memories
- Global checksum validation for checkpoints
- Trust level verification during recovery
- Atomic operations with rollback capability

### High Availability Design
- Asynchronous persistence operations
- Concurrent read/write capabilities
- Automatic recovery mechanisms
- Minimal downtime during failures

## Core Architecture

### Persistence Storage Layout
```
memory_persistence/
├── checkpoints/         # Full state snapshots
│   ├── checkpoint_<timestamp>.dat
│   └── ...
└── wal/                # Write-ahead logs
    ├── wal_<segment>.log
    └── ...
```

### Core Components
```java
private final Path persistenceRoot;          // Base persistence directory
private final Path checkpointPath;           // Checkpoint storage
private final Path walPath;                  // WAL storage
private volatile WALWriter walWriter;        // WAL management
private final Map<String, PersistenceMetadata> metadataCache; // Memory metadata
```

## Core Methods

### Service Lifecycle
```java
// Start persistence service
void start()

// Stop persistence service  
void stop()
```

### Persistence Operations
```java
// Persist critical memories with validation
CompletableFuture<Boolean> persistCriticalMemories(List<MemoryState> memories)

// Load persisted memories
CompletableFuture<Map<String, MemoryState>> loadPersistedMemories()
```

## Write-Ahead Logging (WAL)

### WAL Entry Structure
```java
class WALEntry {
    enum Type { WRITE, DELETE }
    
    private final Type type;          // Operation type
    private final String key;         // Memory key
    private final byte[] data;        // Serialized memory state
    private final TrustLevel trustLevel; // Trust level requirement
    private final long timestamp;     // Operation timestamp
}
```

### WAL Operations
1. **Write Operations**: Log memory state changes
2. **Delete Operations**: Log memory removals
3. **Segment Management**: Rotate logs when size limits reached
4. **Cleanup**: Remove obsolete log segments after checkpoints

### WAL Writer Features
- **Segment Size Limit**: 1MB per segment for manageable files
- **Atomic Writes**: Ensures consistency during writes
- **Time-based Cleanup**: Removes entries older than last checkpoint
- **Concurrent Access**: Thread-safe write operations

## Checkpoint System

### Checkpoint Structure
```java
class CheckpointHeader {
    private final long timestamp;        // Checkpoint creation time
    private final int memoryCount;       // Number of memories
    private final String globalChecksum; // Validation checksum
}
```

### Checkpoint Process
1. **Memory Collection**: Gather all current memories from state manager
2. **Header Creation**: Generate checkpoint metadata
3. **Memory Serialization**: Write memories with validation
4. **Checksum Calculation**: Generate global integrity checksum
5. **Atomic Write**: Ensure checkpoint consistency

### Checkpoint Schedule
- **Interval**: Every 5 minutes (configurable)
- **Retention**: Keep latest 3 checkpoints
- **Cleanup**: Automatic removal of old checkpoints

## Data Validation

### Memory Checksum Calculation
```java
private String calculateChecksum(MemoryState memory) {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    md.update(memory.getKey().getBytes());
    md.update(serialize(memory.getValue()));
    return Base64.getEncoder().encodeToString(md.digest());
}
```

### Global Checksum Validation
Ensures checkpoint integrity by:
1. **Sorted Processing**: Process memories in key order
2. **Individual Checksums**: Include memory checksums
3. **Combined Hash**: Generate global validation hash
4. **Verification**: Compare during checkpoint loading

### Trust Level Verification
During recovery:
- Verify memory trust levels haven't been downgraded
- Validate agent permissions for memory access
- Ensure trust consistency across persistence boundaries

## Recovery Process

### System Recovery Workflow
1. **Checkpoint Loading**: Find and load latest valid checkpoint
2. **WAL Replay**: Apply WAL entries after checkpoint timestamp
3. **Validation**: Verify memory integrity and trust levels
4. **Memory Restoration**: Restore memories to appropriate tiers
5. **Cleanup**: Remove invalid or corrupted memories

### Recovery Strategy
```java
private void recoverFromPersistence() {
    Map<String, MemoryState> recovered = loadPersistedMemories().get();
    
    for (MemoryState memory : recovered.values()) {
        if (validateMemory(memory)) {
            // Restore to appropriate memory tier
            restoreToMemoryTier(memory);
        }
    }
}
```

## Persistence Metadata

### PersistenceMetadata Class
```java
class PersistenceMetadata {
    private final String key;           // Memory key
    private final MemoryType type;      // Memory type
    private final TrustLevel trustLevel; // Trust requirement
    private final String checksum;      // Integrity checksum
    private final long timestamp;       // Last update time
}
```

### Metadata Cache
- **Purpose**: Fast validation without full deserialization
- **Updates**: Synchronized with persistence operations
- **Cleanup**: Automatic removal of stale metadata
- **Validation**: Integrity checks during recovery

## Configuration Parameters
```java
WAL_SEGMENT_SIZE = 1024 * 1024          // 1MB WAL segments
CHECKPOINT_INTERVAL_MS = 300000         // 5 minute checkpoints
RETENTION_COUNT = 3                     // Keep 3 checkpoints
```

## Thread Safety and Concurrency

### Locking Strategy
```java
private final ReadWriteLock persistenceLock = new ReentrantReadWriteLock();
```

### Executor Services
- **Scheduler**: 2-thread pool for periodic operations
- **Persistence Executor**: 4-thread pool for async operations
- **Atomic Operations**: AtomicBoolean for service state

### Concurrent Operations
- **Read Operations**: Multiple concurrent readers
- **Write Operations**: Exclusive write access
- **Background Tasks**: Non-blocking scheduled operations

## Serialization Framework

### Memory Serialization
```java
private byte[] serialize(Object obj) {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos)) {
        oos.writeObject(obj);
        return baos.toByteArray();
    }
}
```

### Deserialization with Validation
```java
private MemoryState deserialize(byte[] data) {
    try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
         ObjectInputStream ois = new ObjectInputStream(bais)) {
        return (MemoryState) ois.readObject();
    } catch (Exception e) {
        return null; // Graceful failure handling
    }
}
```

## Error Handling and Recovery

### Failure Scenarios
- **Checkpoint Corruption**: Fall back to previous checkpoint
- **WAL Corruption**: Skip corrupted entries, log warnings
- **Disk Space**: Graceful degradation with cleanup
- **Permission Issues**: Error logging and service continuation

### Validation Failures
- **Checksum Mismatch**: Skip corrupted memories
- **Trust Violations**: Reject unauthorized memories
- **Type Mismatches**: Convert to compatible types when possible

## Performance Characteristics

### Persistence Performance
- **Asynchronous Operations**: Non-blocking persistence
- **Batch Processing**: Efficient bulk operations
- **Compression**: Optional compression for large states
- **Caching**: Metadata caching for fast validation

### Recovery Performance
- **Incremental Recovery**: WAL replay from checkpoint
- **Parallel Processing**: Concurrent validation operations
- **Smart Loading**: Load only required memories
- **Progressive Restoration**: Tier-based memory restoration

## Integration Points

### MemoryStateManager Integration
- **Memory Collection**: Gather memories from all tiers
- **Restoration**: Restore memories to appropriate tiers
- **Coordination**: Synchronize with memory operations

### Trust System Integration
- **Validation**: Verify trust levels during persistence
- **Security**: Ensure trust-based access control
- **Auditing**: Log trust-related persistence events

## Usage Example
```java
StatePersistenceService persistenceService = 
    new StatePersistenceService(stateManager);

// Start service
persistenceService.start();

// Persist critical memories
List<MemoryState> criticalMemories = getCriticalMemories();
CompletableFuture<Boolean> result = 
    persistenceService.persistCriticalMemories(criticalMemories);

// Load persisted memories (typically during startup)
CompletableFuture<Map<String, MemoryState>> recovered = 
    persistenceService.loadPersistedMemories();

// Stop service (cleanup)
persistenceService.stop();
```

## Monitoring and Diagnostics
- **Persistence Metrics**: Track operation success rates
- **Recovery Statistics**: Monitor recovery performance
- **Integrity Reports**: Checksum validation results
- **Performance Monitoring**: Operation timing and throughput