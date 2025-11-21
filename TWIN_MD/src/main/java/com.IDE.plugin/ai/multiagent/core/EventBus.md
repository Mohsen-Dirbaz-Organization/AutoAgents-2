# EventBus Class Documentation

## Overview
`EventBus` provides a flexible publish-subscribe event system for the multi-agent framework. It supports both type-based and topic-based event subscriptions, enabling decoupled communication between components. The implementation uses thread-safe collections and handles event hierarchies, making it suitable for complex event-driven architectures.

## Multi-Level Architecture

### System Level
- **Role**: Central event distribution mechanism
- **Purpose**: Enable decoupled, asynchronous communication
- **Pattern**: Publish-subscribe with type hierarchy support
- **Integration**: Used by all agents and system components

### Component Level
- **Type**: Event dispatcher with dual subscription models
- **Package**: `com.IDE.plugin.ai.multiagent.core`
- **Features**:
  - Type-based subscriptions with inheritance
  - Topic-based string subscriptions
  - Thread-safe concurrent operations
  - Error isolation between handlers

## Core Features and Functionality

### Type-Based Subscriptions
- **Class-Based Routing**: Subscribe to specific event types
- **Inheritance Support**: Handles parent class subscriptions
- **Interface Support**: Subscribe to interface implementations
- **Type Safety**: Compile-time type checking for handlers

### Topic-Based Subscriptions
- **String Topics**: Flexible topic naming
- **Dynamic Topics**: Runtime topic creation
- **Broadcast Capability**: One-to-many messaging
- **Decoupled Design**: No type dependencies

### Event Publishing
- **Synchronous Delivery**: Events delivered in caller's thread
- **Error Isolation**: Handler exceptions don't affect others
- **Hierarchy Walking**: Notifies parent type subscribers
- **Order Preservation**: Maintains subscription order

### Handler Management
- **Dynamic Subscribe/Unsubscribe**: Runtime handler changes
- **Thread-Safe Collections**: ConcurrentHashMap and CopyOnWriteArrayList
- **Memory Management**: Clean unsubscription support
- **Clear Operation**: Remove all subscriptions

## Component Props and Data Structures

### Core Fields
```java
// Type-based subscriptions: Class -> List of handlers
private final Map<Class<?>, List<Consumer<Object>>> subscribers = new ConcurrentHashMap<>();

// Topic-based subscriptions: Topic -> List of handlers  
private final Map<String, List<Consumer<Object>>> topicSubscribers = new ConcurrentHashMap<>();
```

### Key Methods
```java
// Type-based subscription
public <T> void subscribe(Class<T> eventType, Consumer<T> handler)

// Topic-based subscription
public void subscribe(String topic, Consumer<Object> handler)

// Publish to type subscribers
public void publish(Object event)

// Publish to topic subscribers
public void publish(String topic, Object event)

// Unsubscribe operations
public <T> void unsubscribe(Class<T> eventType, Consumer<T> handler)
public void unsubscribe(String topic, Consumer<Object> handler)

// Clear all subscriptions
public void clear()
```

## Usage Patterns and Integration Points

### Basic Event Publishing
```java
public class AgentSystem {
    private final EventBus eventBus = new EventBus();
    
    public void notifyAgentStarted(String agentId) {
        AgentEvent event = new AgentEvent(
            EventType.AGENT_STARTED,
            agentId,
            "Agent initialized"
        );
        eventBus.publish(event);
    }
    
    public void broadcastSystemMessage(String message) {
        eventBus.publish("system.broadcast", message);
    }
}
```

### Type-Based Subscription
```java
public class EventMonitor {
    private final EventBus eventBus;
    
    public void initialize() {
        // Subscribe to specific event type
        eventBus.subscribe(AgentEvent.class, this::handleAgentEvent);
        
        // Subscribe to parent type for broader coverage
        eventBus.subscribe(SystemEvent.class, this::handleSystemEvent);
        
        // Lambda subscription
        eventBus.subscribe(ErrorEvent.class, event -> {
            logger.error("Error occurred: {}", event.getMessage());
        });
    }
    
    private void handleAgentEvent(AgentEvent event) {
        switch (event.getType()) {
            case AGENT_STARTED:
                onAgentStarted(event);
                break;
            case AGENT_STOPPED:
                onAgentStopped(event);
                break;
        }
    }
}
```

### Topic-Based Subscription
```java
public class TopicSubscriber {
    private final EventBus eventBus;
    
    public void subscribeToTopics() {
        // System-wide broadcasts
        eventBus.subscribe("system.shutdown", event -> {
            performShutdown();
        });
        
        // Agent-specific topics
        eventBus.subscribe("agent.task.completed", event -> {
            TaskResult result = (TaskResult) event;
            updateMetrics(result);
        });
        
        // Wildcard-like patterns (implement in handler)
        eventBus.subscribe("metrics", event -> {
            if (event instanceof MetricEvent) {
                processMetric((MetricEvent) event);
            }
        });
    }
}
```

### Event Hierarchy Handling
```java
// Event class hierarchy
public class BaseEvent { }
public class SystemEvent extends BaseEvent { }
public class AgentEvent extends SystemEvent { }

public class HierarchyExample {
    private final EventBus eventBus;
    
    public void demonstrateHierarchy() {
        // Subscribe at different levels
        eventBus.subscribe(BaseEvent.class, e -> 
            System.out.println("Base event"));
            
        eventBus.subscribe(SystemEvent.class, e -> 
            System.out.println("System event"));
            
        eventBus.subscribe(AgentEvent.class, e -> 
            System.out.println("Agent event"));
        
        // Publish AgentEvent - all three handlers triggered
        eventBus.publish(new AgentEvent());
    }
}
```

### Error Handling Pattern
```java
public class ResilientSubscriber {
    private final EventBus eventBus;
    
    public void subscribeWithErrorHandling() {
        eventBus.subscribe(DataEvent.class, event -> {
            try {
                processDataEvent(event);
            } catch (ProcessingException e) {
                // Log error - won't affect other handlers
                logger.error("Failed to process event", e);
                
                // Optionally publish error event
                eventBus.publish(new ErrorEvent(e));
            }
        });
    }
}
```

## Best Practices and Considerations

### Subscription Management
1. **Memory Leaks**: Always unsubscribe when components are destroyed
2. **Handler References**: Use weak references for long-lived subscriptions
3. **Anonymous Classes**: Avoid for handlers that need unsubscription
4. **Subscription Timing**: Subscribe during initialization

### Event Design
1. **Immutable Events**: Make event objects immutable
2. **Event Granularity**: Balance between too many and too few event types
3. **Event Data**: Include sufficient context in events
4. **Event Naming**: Use clear, descriptive event class names

### Performance Optimization
1. **Handler Efficiency**: Keep handlers fast and non-blocking
2. **Async Processing**: Offload heavy work to separate threads
3. **Event Frequency**: Consider batching high-frequency events
4. **Subscription Count**: Monitor number of subscribers

### Thread Safety
```java
public class ThreadSafeUsage {
    private final EventBus eventBus;
    private final ExecutorService executor;
    
    public void asyncEventHandling() {
        eventBus.subscribe(HeavyEvent.class, event -> {
            // Offload to separate thread
            executor.submit(() -> processHeavyEvent(event));
        });
    }
    
    public void concurrentPublishing() {
        // Safe to publish from multiple threads
        IntStream.range(0, 100).parallel().forEach(i -> {
            eventBus.publish(new CountEvent(i));
        });
    }
}
```

## Common Event Patterns

### Request-Response Pattern
```java
public class RequestResponsePattern {
    private final EventBus eventBus;
    private final Map<String, CompletableFuture<Object>> pendingRequests;
    
    public CompletableFuture<Object> sendRequest(Request request) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        pendingRequests.put(request.getId(), future);
        
        eventBus.subscribe(Response.class, response -> {
            if (response.getRequestId().equals(request.getId())) {
                future.complete(response.getData());
                pendingRequests.remove(request.getId());
            }
        });
        
        eventBus.publish(request);
        return future;
    }
}
```

### Event Aggregation
```java
public class EventAggregator {
    private final EventBus eventBus;
    private final List<MetricEvent> buffer = new ArrayList<>();
    private final ScheduledExecutorService scheduler;
    
    public void startAggregation() {
        // Collect events
        eventBus.subscribe(MetricEvent.class, event -> {
            synchronized (buffer) {
                buffer.add(event);
            }
        });
        
        // Periodically publish aggregated events
        scheduler.scheduleAtFixedRate(() -> {
            List<MetricEvent> toProcess;
            synchronized (buffer) {
                toProcess = new ArrayList<>(buffer);
                buffer.clear();
            }
            
            if (!toProcess.isEmpty()) {
                AggregatedMetrics aggregated = aggregate(toProcess);
                eventBus.publish(aggregated);
            }
        }, 0, 10, TimeUnit.SECONDS);
    }
}
```

### Event Filtering
```java
public class FilteredSubscriber {
    private final EventBus eventBus;
    
    public void subscribeWithFilter() {
        eventBus.subscribe(TaskEvent.class, event -> {
            // Only handle specific task types
            if (event.getTaskType().equals("ANALYSIS")) {
                handleAnalysisTask(event);
            }
        });
        
        // Alternative: Create filtered wrapper
        subscribeFiltered(TaskEvent.class, 
            event -> event.getTaskType().equals("ANALYSIS"),
            this::handleAnalysisTask);
    }
    
    private <T> void subscribeFiltered(Class<T> type, 
                                      Predicate<T> filter, 
                                      Consumer<T> handler) {
        eventBus.subscribe(type, event -> {
            if (filter.test(event)) {
                handler.accept(event);
            }
        });
    }
}
```

## Integration Considerations

### With Agent System
- Agents publish lifecycle events
- Task completion notifications
- Error propagation
- State change announcements

### With Monitoring
- Metric event collection
- System health updates
- Performance measurements
- Alert distribution

### With UI Components
- User action events
- UI update notifications
- Progress indicators
- Error display

### With External Systems
- Integration events
- API callbacks
- Webhook notifications
- Message queue bridging

## Advanced Features

### Event Replay
```java
public class EventRecorder {
    private final List<Object> recordedEvents = new ArrayList<>();
    private final EventBus eventBus;
    
    public void startRecording() {
        // Record all events
        eventBus.subscribe(Object.class, recordedEvents::add);
    }
    
    public void replay() {
        recordedEvents.forEach(eventBus::publish);
    }
}
```

### Conditional Subscriptions
```java
public class ConditionalSubscriber {
    private final EventBus eventBus;
    private volatile boolean enabled = true;
    
    public void conditionalSubscribe() {
        eventBus.subscribe(DataEvent.class, event -> {
            if (enabled) {
                processEvent(event);
            }
        });
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
```

### Event Transformation
```java
public class EventTransformer {
    private final EventBus eventBus;
    
    public void setupTransformation() {
        // Transform one event type to another
        eventBus.subscribe(RawDataEvent.class, raw -> {
            ProcessedDataEvent processed = transform(raw);
            eventBus.publish(processed);
        });
    }
}
```