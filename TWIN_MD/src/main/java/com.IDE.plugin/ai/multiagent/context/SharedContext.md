# SharedContext Class Documentation

## Overview
`SharedContext` provides a thread-safe shared data repository for agents to exchange information within the multi-agent system. It serves as a central knowledge base where agents can store and retrieve shared state, configuration data, and runtime information. The class uses a `ConcurrentHashMap` to ensure thread safety in concurrent agent environments.

## Multi-Level Architecture

### System Level
- **Role**: Central shared state repository
- **Purpose**: Enable information exchange between agents
- **Thread Safety**: Fully concurrent access support
- **Pattern**: Shared memory pattern for inter-agent communication

### Component Level
- **Type**: Thread-safe data container
- **Package**: `com.IDE.plugin.ai.multiagent.context`
- **Implementation**: Wrapper around ConcurrentHashMap
- **Features**: Type-safe retrieval, null-safe operations

## Core Features and Functionality

### Thread-Safe Storage
- **Concurrent Access**: Multiple agents can read/write simultaneously
- **Atomic Operations**: All operations are atomic
- **No Locking Required**: Uses ConcurrentHashMap internally
- **Scalable**: Performs well under high concurrency

### Flexible Data Storage
- **Any Object Type**: Stores any Java object
- **Key-Value Model**: String keys for easy access
- **Dynamic Schema**: No predefined structure required
- **Runtime Flexibility**: Add/remove data dynamically

### Type-Safe Retrieval
- **Generic Get Method**: Type-safe retrieval with Class parameter
- **Automatic Casting**: Handles type casting internally
- **Null Safety**: Returns null for missing keys
- **ClassCastException**: Thrown for type mismatches

## Component Props and Data Structures

### Core Field
```java
private final Map<String, Object> sharedData  // Thread-safe storage
```

### Key Methods
```java
// Store value
public Object put(String key, Object value)

// Retrieve value
public Object get(String key)

// Type-safe retrieval
public <T> T get(String key, Class<T> type)

// Remove value
public Object remove(String key)

// Check existence
public boolean containsKey(String key)

// Clear all data
public void clear()

// Get size
public int size()

// Get snapshot
public Map<String, Object> getAll()
```

## Usage Patterns and Integration Points

### Basic Storage and Retrieval
```java
SharedContext context = new SharedContext();

// Store various types of data
context.put("system.version", "1.0.0");
context.put("agents.max", 100);
context.put("configuration", configObject);

// Retrieve with explicit casting
String version = (String) context.get("system.version");
Integer maxAgents = (Integer) context.get("agents.max");

// Type-safe retrieval
String safeVersion = context.get("system.version", String.class);
Integer safeMaxAgents = context.get("agents.max", Integer.class);
Configuration config = context.get("configuration", Configuration.class);
```

### Agent State Sharing
```java
// Agent registration
public void registerAgent(String agentId, AgentInfo info) {
    context.put("agent." + agentId + ".info", info);
    context.put("agent." + agentId + ".status", "ACTIVE");
    context.put("agent." + agentId + ".lastUpdate", System.currentTimeMillis());
}

// Agent discovery
public List<String> getActiveAgents() {
    return context.getAll().keySet().stream()
        .filter(key -> key.startsWith("agent.") && key.endsWith(".status"))
        .filter(key -> "ACTIVE".equals(context.get(key)))
        .map(key -> key.split("\\.")[1])
        .collect(Collectors.toList());
}
```

### Configuration Management
```java
// Load configuration
public void loadConfiguration(Properties props) {
    props.forEach((key, value) -> {
        context.put("config." + key, value);
    });
}

// Retrieve configuration
public String getConfig(String key, String defaultValue) {
    String value = context.get("config." + key, String.class);
    return value != null ? value : defaultValue;
}
```

### Task Coordination
```java
// Share task information
public void shareTaskInfo(Task task) {
    String prefix = "task." + task.getId();
    context.put(prefix + ".type", task.getType());
    context.put(prefix + ".status", task.getStatus());
    context.put(prefix + ".assignedAgents", task.getAssignedAgents());
    context.put(prefix + ".priority", task.getPriority());
}

// Query task status
public TaskStatus getTaskStatus(String taskId) {
    return context.get("task." + taskId + ".status", TaskStatus.class);
}
```

### Metrics and Monitoring
```java
// Store metrics
public void updateMetric(String metricName, double value) {
    String key = "metrics." + metricName;
    context.put(key + ".value", value);
    context.put(key + ".timestamp", System.currentTimeMillis());
}

// Aggregate metrics
public Map<String, Double> getAllMetrics() {
    Map<String, Double> metrics = new HashMap<>();
    context.getAll().forEach((key, value) -> {
        if (key.startsWith("metrics.") && key.endsWith(".value")) {
            String metricName = key.substring(8, key.length() - 6);
            metrics.put(metricName, (Double) value);
        }
    });
    return metrics;
}
```

## Common Usage Patterns

### Namespace Convention
```java
// Use dot notation for hierarchical data
"agent.<agentId>.<property>"     // Agent-specific data
"task.<taskId>.<property>"        // Task-specific data
"config.<setting>"                // Configuration values
"metrics.<metric>.<aspect>"       // Performance metrics
"group.<groupId>.<property>"      // Group coordination data
```

### Atomic Updates
```java
// Safe counter increment
public void incrementCounter(String counterName) {
    synchronized (context) {
        Integer current = context.get(counterName, Integer.class);
        context.put(counterName, (current != null ? current : 0) + 1);
    }
}
```

### Snapshot Operations
```java
// Create consistent snapshot
public SystemSnapshot createSnapshot() {
    Map<String, Object> data = context.getAll();
    return new SystemSnapshot(
        data,
        System.currentTimeMillis()
    );
}
```

### Cleanup Patterns
```java
// Remove agent data on shutdown
public void cleanupAgent(String agentId) {
    String prefix = "agent." + agentId + ".";
    context.getAll().keySet().stream()
        .filter(key -> key.startsWith(prefix))
        .forEach(context::remove);
}
```

## Best Practices and Considerations

### Key Naming Conventions
1. **Use Prefixes**: Group related data with common prefixes
2. **Hierarchical Structure**: Use dots for hierarchy
3. **Consistent Format**: Maintain naming consistency
4. **Avoid Collisions**: Use unique prefixes per component

### Performance Considerations
1. **Key Design**: Short, efficient key strings
2. **Value Size**: Avoid storing large objects
3. **Cleanup**: Remove unused data regularly
4. **Iteration**: Use getAll() sparingly on large contexts

### Thread Safety Guidelines
1. **Atomic Operations**: Individual operations are thread-safe
2. **Compound Operations**: May need external synchronization
3. **Iteration Safety**: Snapshot with getAll() for safe iteration
4. **Consistency**: No guarantees across multiple operations

### Memory Management
1. **Size Monitoring**: Track context size growth
2. **Expiration**: Implement TTL for temporary data
3. **Cleanup Tasks**: Schedule periodic cleanup
4. **Reference Management**: Avoid memory leaks

## Common Use Cases

### Service Discovery
```java
// Register service
context.put("service.auth.endpoint", "http://auth:8080");
context.put("service.auth.healthy", true);

// Discover services
String authEndpoint = context.get("service.auth.endpoint", String.class);
```

### Feature Flags
```java
// Set feature flags
context.put("feature.newUI.enabled", true);
context.put("feature.betaAPI.enabled", false);

// Check features
boolean useNewUI = Boolean.TRUE.equals(
    context.get("feature.newUI.enabled", Boolean.class)
);
```

### Session Management
```java
// Store session data
context.put("session." + sessionId + ".user", userId);
context.put("session." + sessionId + ".start", startTime);

// Validate session
boolean isValid = context.containsKey("session." + sessionId + ".user");
```

### Resource Pools
```java
// Track resource usage
context.put("pool.connections.total", 100);
context.put("pool.connections.used", 45);
context.put("pool.connections.available", 55);
```

## Integration Points

### With Agents
- Agents use context for state sharing
- Configuration retrieval
- Task coordination
- Metric reporting

### With Coordination Framework
- Group formation data
- Task allocation information
- Consensus results
- Performance metrics

### With Monitoring Systems
- Real-time metrics storage
- System state snapshots
- Health check data
- Performance indicators

## Anti-Patterns to Avoid

1. **Large Object Storage**: Don't store huge objects
2. **Frequent Full Scans**: Avoid repeated getAll() calls
3. **Deep Nesting**: Limit hierarchy depth in keys
4. **Tight Coupling**: Don't create dependencies on specific keys
5. **Missing Cleanup**: Always clean up temporary data

## Testing Considerations

### Unit Testing
```java
@Test
public void testConcurrentAccess() {
    SharedContext context = new SharedContext();
    ExecutorService executor = Executors.newFixedThreadPool(10);
    
    // Concurrent writes
    for (int i = 0; i < 1000; i++) {
        final int index = i;
        executor.submit(() -> {
            context.put("key" + index, "value" + index);
        });
    }
    
    // Verify all writes succeeded
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);
    assertEquals(1000, context.size());
}
```

### Mock Context
```java
public class MockSharedContext extends SharedContext {
    private final Map<String, Object> predefinedData;
    
    public MockSharedContext(Map<String, Object> data) {
        this.predefinedData = data;
        data.forEach(this::put);
    }
}
```