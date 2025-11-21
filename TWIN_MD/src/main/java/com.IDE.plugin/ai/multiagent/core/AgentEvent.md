# AgentEvent Class Documentation

## Overview
`AgentEvent` represents an event in the agent system, providing a standardized way to capture and communicate significant occurrences. It includes event type classification, source identification, timestamping, and flexible data payloads. The class serves as the foundation for event-driven communication and monitoring within the multi-agent system.

## Multi-Level Architecture

### System Level
- **Role**: Core event representation for system-wide event handling
- **Purpose**: Standardize event communication across all components
- **Pattern**: Immutable event object with type safety
- **Integration**: Used by EventBus, agents, and monitoring systems

### Component Level
- **Type**: Immutable data class with nested EventType enum
- **Package**: `com.IDE.plugin.ai.multiagent.core`
- **Features**:
  - Auto-generated unique IDs
  - Precise timestamping with Instant
  - Type-safe event classification
  - Flexible data payload

## Core Features and Functionality

### Event Identification
- **Unique ID**: UUID-based identification for each event
- **Type Classification**: Strongly typed event categories via enum
- **Source Tracking**: Identifies event origin
- **Temporal Ordering**: Instant-based timestamps

### Event Data Management
- **Message Field**: Human-readable event description
- **Data Payload**: Flexible Map<String, Object> for additional context
- **Immutability**: Data copied on construction for thread safety
- **Null Safety**: Defensive copying prevents external modification

### Event Type Categories
- **Agent Lifecycle**: Started, Stopped, Error events
- **Task Events**: Created, Started, Completed, Failed, Cancelled
- **Communication**: Message Sent, Message Received
- **System Events**: Info, Warning, Error
- **Custom Events**: Extensible for domain-specific needs

## Component Props and Data Structures

### Core Fields
```java
private final String id              // Unique event identifier (UUID)
private final EventType type         // Event classification
private final String source          // Event origin identifier
private final String message         // Human-readable description
private final Instant timestamp      // Event occurrence time
private final Map<String, Object> data  // Additional context data
```

### EventType Enum
```java
public enum EventType {
    // Agent lifecycle
    AGENT_STARTED("Agent Started"),
    AGENT_STOPPED("Agent Stopped"),
    AGENT_ERROR("Agent Error"),
    
    // Task events
    TASK_CREATED("Task Created"),
    TASK_STARTED("Task Started"),
    TASK_COMPLETED("Task Completed"),
    TASK_FAILED("Task Failed"),
    TASK_CANCELLED("Task Cancelled"),
    
    // Communication
    MESSAGE_SENT("Message Sent"),
    MESSAGE_RECEIVED("Message Received"),
    
    // System events
    SYSTEM_INFO("System Information"),
    SYSTEM_WARNING("System Warning"),
    SYSTEM_ERROR("System Error"),
    
    // Extensibility
    CUSTOM("Custom Event")
}
```

## Usage Patterns and Integration Points

### Basic Event Creation
```java
// Simple event without data
AgentEvent startEvent = new AgentEvent(
    EventType.AGENT_STARTED,
    "agent-123",
    "Agent initialized and ready"
);

// Event with contextual data
Map<String, Object> taskData = Map.of(
    "taskId", "task-456",
    "taskType", "ANALYSIS",
    "priority", "HIGH"
);

AgentEvent taskEvent = new AgentEvent(
    EventType.TASK_CREATED,
    "scheduler",
    "New analysis task created",
    taskData
);
```

### Event Publishing Pattern
```java
public class Agent {
    private final EventBus eventBus;
    private final String agentId;
    
    public void start() {
        // Perform startup logic
        
        // Publish startup event
        AgentEvent event = new AgentEvent(
            EventType.AGENT_STARTED,
            agentId,
            "Agent successfully started"
        );
        eventBus.publish(event);
    }
    
    public void handleError(Exception e) {
        // Create error event with details
        Map<String, Object> errorData = Map.of(
            "exception", e.getClass().getName(),
            "message", e.getMessage(),
            "stackTrace", Arrays.toString(e.getStackTrace())
        );
        
        AgentEvent errorEvent = new AgentEvent(
            EventType.AGENT_ERROR,
            agentId,
            "Agent encountered an error",
            errorData
        );
        eventBus.publish(errorEvent);
    }
}
```

### Event Handling Pattern
```java
public class EventMonitor {
    public void handleEvent(AgentEvent event) {
        switch (event.getType()) {
            case AGENT_ERROR:
                handleAgentError(event);
                break;
            case TASK_FAILED:
                handleTaskFailure(event);
                break;
            case SYSTEM_WARNING:
                handleSystemWarning(event);
                break;
            default:
                logEvent(event);
        }
    }
    
    private void handleAgentError(AgentEvent event) {
        String agentId = event.getSource();
        String errorMsg = (String) event.getData("message");
        // Implement error handling logic
    }
}
```

### Event Filtering and Analysis
```java
public class EventAnalyzer {
    private final List<AgentEvent> events = new ArrayList<>();
    
    public void analyzeAgentPerformance(String agentId) {
        // Filter events by source and type
        List<AgentEvent> agentEvents = events.stream()
            .filter(e -> e.getSource().equals(agentId))
            .filter(e -> e.getType() == EventType.TASK_COMPLETED ||
                        e.getType() == EventType.TASK_FAILED)
            .sorted(Comparator.comparing(AgentEvent::getTimestamp))
            .collect(Collectors.toList());
        
        // Calculate success rate
        long completed = agentEvents.stream()
            .filter(e -> e.getType() == EventType.TASK_COMPLETED)
            .count();
        long total = agentEvents.size();
        double successRate = total > 0 ? (double) completed / total : 0.0;
    }
}
```

## Best Practices and Considerations

### Event Design Guidelines
1. **Meaningful Messages**: Provide clear, actionable descriptions
2. **Appropriate Type**: Choose the most specific event type
3. **Relevant Data**: Include only necessary context in payload
4. **Source Clarity**: Use consistent source identifiers

### Data Payload Best Practices
1. **Serializable Objects**: Ensure all data objects are serializable
2. **Size Limits**: Keep payloads reasonably sized
3. **Key Conventions**: Use consistent naming for data keys
4. **Type Safety**: Document expected data types

### Performance Considerations
1. **Event Volume**: Design for high-frequency event scenarios
2. **Data Copying**: Be aware of defensive copying overhead
3. **Memory Usage**: Monitor event retention and cleanup
4. **Processing Time**: Keep event handling lightweight

### Thread Safety
- Events are immutable after creation
- Data maps are defensively copied
- Safe to share across threads
- No synchronization needed for reading

## Common Event Patterns

### Lifecycle Events
```java
// Agent lifecycle tracking
void trackAgentLifecycle(String agentId) {
    // Startup
    publish(new AgentEvent(EventType.AGENT_STARTED, agentId, "Starting"));
    
    // Running - periodic heartbeat
    publish(new AgentEvent(EventType.CUSTOM, agentId, "Heartbeat"));
    
    // Shutdown
    publish(new AgentEvent(EventType.AGENT_STOPPED, agentId, "Shutting down"));
}
```

### Task Progress Events
```java
// Task execution flow
void executeTask(Task task) {
    publish(new AgentEvent(EventType.TASK_CREATED, "scheduler", 
        "Task created", Map.of("taskId", task.getId())));
    
    publish(new AgentEvent(EventType.TASK_STARTED, "agent-1", 
        "Task started", Map.of("taskId", task.getId())));
    
    try {
        Object result = performTask(task);
        publish(new AgentEvent(EventType.TASK_COMPLETED, "agent-1",
            "Task completed", Map.of("taskId", task.getId(), "result", result)));
    } catch (Exception e) {
        publish(new AgentEvent(EventType.TASK_FAILED, "agent-1",
            "Task failed", Map.of("taskId", task.getId(), "error", e.getMessage())));
    }
}
```

### System Monitoring Events
```java
// System health monitoring
void monitorSystemHealth() {
    double cpuUsage = getCpuUsage();
    if (cpuUsage > 90) {
        publish(new AgentEvent(EventType.SYSTEM_WARNING, "monitor",
            "High CPU usage detected", Map.of("cpu", cpuUsage)));
    }
    
    long memoryUsed = getMemoryUsage();
    if (memoryUsed > threshold) {
        publish(new AgentEvent(EventType.SYSTEM_ERROR, "monitor",
            "Memory threshold exceeded", Map.of("memory", memoryUsed)));
    }
}
```

## Integration Points

### With EventBus
- Events are primary payload for EventBus
- Type-based subscription using EventType
- Hierarchical event handling support

### With Monitoring Systems
- Events provide audit trail
- Enable performance analysis
- Support debugging and troubleshooting

### With Agent Coordination
- Events synchronize agent activities
- Enable reactive behavior
- Support distributed decision making

## Extension Considerations

### Adding New Event Types
1. Add to EventType enum with descriptive display name
2. Document the event's purpose and data schema
3. Update relevant event handlers
4. Consider backward compatibility

### Custom Event Data
- Use CUSTOM event type for domain-specific events
- Document data schema in payload
- Consider creating wrapper classes for complex data

### Event Enrichment
- Add correlation IDs for request tracking
- Include version information for compatibility
- Add security context when needed