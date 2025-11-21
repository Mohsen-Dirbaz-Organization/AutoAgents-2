# MessageBus Class Documentation

## Overview
`MessageBus` is the core messaging infrastructure that provides reliable message routing, delivery, and queue management for the multi-agent system. It implements a sophisticated publish-subscribe pattern with direct messaging, role-based routing, and comprehensive delivery tracking. The system ensures reliable communication through retry mechanisms, queue management, and performance monitoring.

## Multi-Level Architecture

### System Level
- **Role**: Central message routing and delivery infrastructure
- **Responsibilities**: Message routing, queue management, delivery tracking, maintenance
- **Pattern**: Message broker with publish-subscribe and point-to-point messaging
- **Reliability**: Built-in retry logic, delivery tracking, and timeout handling

### Component Level
- **Type**: Core service class
- **Package**: `com.IDE.plugin.ai.multiagent.communication`
- **Major Components**:
  - Agent-specific message queues
  - Role-based routing registry
  - Delivery tracking system
  - Maintenance and monitoring threads
  - Global broadcast queue

### Threading Architecture
- **Delivery Executor**: 10-thread pool for parallel message delivery
- **Maintenance Executor**: Scheduled executor for periodic tasks
- **Processing Thread**: Dedicated thread for broadcast message processing
- **Non-blocking Design**: Asynchronous delivery with CompletableFuture

## Core Features and Functionality

### Agent Registration System
- **Dedicated Queues**: Each agent gets a private message queue
- **Role Mapping**: Agents registered by role for group messaging
- **Handler Registration**: MessageHandler interface for message processing
- **Subscription Management**: Flexible subscription to message types

### Message Routing Capabilities
- **Direct Messaging**: Point-to-point delivery to specific agents
- **Role-Based Routing**: Send to all agents with specific role
- **Broadcast Messaging**: System-wide message distribution
- **Subscription-Based Routing**: Type-based message filtering

### Delivery Management
- **Delivery Tracking**: CompletableFuture-based delivery status
- **Retry Mechanism**: Exponential backoff for failed deliveries
- **Timeout Detection**: Automatic timeout after 30 seconds
- **Delivery Metrics**: Success/failure rate tracking

### Queue Management
- **Per-Agent Queues**: Isolated queues prevent interference
- **Message Expiration**: Automatic removal of old messages (5 minutes)
- **Overflow Detection**: Queue health monitoring
- **Global Queue**: Separate queue for broadcast processing

### Maintenance Operations
- **Periodic Cleanup**: Removes expired messages every 30 seconds
- **Health Checks**: Queue overflow detection
- **Statistics Generation**: Regular performance metrics
- **Delivery Status Checks**: Timeout detection every 10 seconds

## Component Props and Data Structures

### Core Data Structures
```java
private final Map<String, MessageQueue> agentQueues         // Agent-specific queues
private final Map<AgentRole, Set<String>> roleRegistry      // Role to agents mapping
private final Map<String, MessageHandler> messageHandlers    // Message handlers
private final BlockingQueue<Message> globalQueue             // Broadcast queue
private final Map<String, SubscriptionInfo> subscriptions   // Subscription data
private final MessageRouter router                           // Routing logic
private final DeliveryTracker deliveryTracker               // Delivery monitoring
```

### Inner Classes

#### MessageQueue
```java
private static class MessageQueue {
    private final String agentId
    private final BlockingQueue<Message> queue
    private final long maxAge = 5 minutes
    
    // Queue operations with expiration support
}
```

#### SubscriptionInfo
```java
private static class SubscriptionInfo {
    private final String agentId
    private final AgentRole role
    private final Set<MessageType> subscriptions
    
    // Subscription management with defaults
}
```

#### DeliveryTracker
```java
private static class DeliveryTracker {
    private final Map<String, CompletableFuture<DeliveryStatus>> pendingDeliveries
    private final AtomicLong deliveredCount
    private final AtomicLong failedCount
    
    // Delivery tracking with timeout detection
}
```

## Usage Patterns and Integration Points

### Agent Registration
```java
MessageBus messageBus = new MessageBus();

// Register an agent
messageBus.registerAgent(
    "analyzer-1",
    AgentRole.ANALYZER,
    new MessageHandler() {
        @Override
        public void handleMessage(Message message) {
            // Process message
            processAnalysisRequest(message);
        }
    }
);

// Subscribe to specific message types
messageBus.subscribe("analyzer-1", 
    MessageType.ANALYSIS_REQUEST,
    MessageType.CODE_CHANGED
);
```

### Sending Messages
```java
// Direct message with delivery tracking
Message directMsg = new Message(
    MessageType.REFACTORING_REQUEST,
    "editor-1",
    "architect-1",
    Map.of("target", "UserService.java")
);

CompletableFuture<DeliveryStatus> future = messageBus.sendMessage(directMsg);

// Handle delivery result
future.thenAccept(status -> {
    switch (status) {
        case DELIVERED:
            log("Message delivered successfully");
            break;
        case FAILED:
            handleDeliveryFailure(directMsg);
            break;
        case TIMEOUT:
            retryOrEscalate(directMsg);
            break;
    }
});
```

### Role-Based Messaging
```java
// Send to all agents with specific role
Message notification = new Message(
    MessageType.CODE_CHANGED,
    "monitor",
    null,
    Map.of("file", "Config.java", "change", "updated")
);

messageBus.sendToRole(AgentRole.CODE_EDITOR, notification);
```

### Broadcasting
```java
// System-wide broadcast
Message systemAlert = new Message(
    MessageType.SYSTEM_BROADCAST,
    "orchestrator",
    null,
    Map.of("event", "maintenance-mode", "duration", "10 minutes")
);

messageBus.broadcast(systemAlert);
```

### Subscription Management
```java
// Dynamic subscription updates
messageBus.subscribe("agent-1", 
    MessageType.CRITICAL_ALERT,
    MessageType.PERFORMANCE_ANALYSIS
);

// Unsubscribe from specific types
messageBus.unsubscribe("agent-1", MessageType.STATUS_UPDATE);
```

## Message Flow Patterns

### Direct Message Flow
1. Message assigned unique ID and timestamp
2. Delivery future created and tracked
3. Message enqueued to recipient's queue
4. Delivery executor processes message
5. Handler invoked with message
6. Delivery status updated
7. Future completed with status

### Broadcast Message Flow
1. Message added to global queue
2. Processing thread determines recipients
3. Message copied for each recipient
4. Individual copies enqueued
5. Parallel delivery to all recipients

### Retry Flow
1. Delivery exception caught
2. Retry count incremented
3. Exponential backoff calculated (2^retry * 1000ms)
4. Retry scheduled with delay
5. Max retries (3) enforced
6. Failed message handled after max retries

## Best Practices and Considerations

### Registration Best Practices
1. **Early Registration**: Register agents before sending messages
2. **Cleanup**: Always unregister agents on shutdown
3. **Handler Efficiency**: Keep handlers fast and non-blocking
4. **Role Assignment**: Use appropriate roles for routing

### Message Design
1. **Size Limits**: Keep messages reasonably sized
2. **Correlation IDs**: Use headers for request-response tracking
3. **Message Types**: Choose appropriate types for routing
4. **Broadcast Sparingly**: Avoid unnecessary broadcasts

### Performance Optimization
1. **Queue Monitoring**: Watch for queue overflow warnings
2. **Handler Performance**: Profile slow message handlers
3. **Batch Processing**: Group related messages when possible
4. **Subscription Filtering**: Minimize unnecessary message delivery

### Reliability Considerations
1. **Idempotent Handlers**: Ensure handlers can process duplicates
2. **Timeout Handling**: Implement appropriate timeout responses
3. **Error Recovery**: Handle delivery failures gracefully
4. **Message Ordering**: Don't assume strict ordering

### Maintenance Guidelines
1. **Monitor Metrics**: Track delivery success rates
2. **Queue Health**: Address overflow warnings promptly
3. **Message Age**: Investigate old undelivered messages
4. **Resource Cleanup**: Ensure proper shutdown procedures

## Advanced Features

### Message Prioritization
```java
// While not built-in, can be implemented via headers
message.getHeaders().put("priority", "HIGH");
// Custom routing logic can check priority
```

### Dead Letter Queue Pattern
```java
private void handleFailedMessage(Message message) {
    // Could implement dead letter queue
    deadLetterQueue.offer(message);
    notifyAdministrator(message);
}
```

### Message Filtering
```java
// Subscription-based filtering is built-in
// Additional filtering can be added in handlers
public void handleMessage(Message message) {
    if (shouldProcess(message)) {
        processMessage(message);
    }
}
```

## Monitoring and Diagnostics

### Built-in Statistics
- Total message count
- Delivered message count
- Failed message count
- Queue sizes per agent
- Delivery success rate

### Health Indicators
- Queue overflow warnings (>1000 messages)
- Expired message cleanup
- Timeout detection
- Handler exception tracking

### Performance Metrics
- Message delivery latency
- Handler processing time
- Queue growth rate
- Retry frequency

## Error Handling

### Delivery Failures
- Automatic retry with exponential backoff
- Max retry limit enforcement
- Failed message logging
- Optional dead letter queue

### Queue Overflows
- Warning logs for large queues
- Automatic expired message cleanup
- Configurable queue size limits
- Back-pressure mechanisms

### Handler Exceptions
- Exception isolation per message
- Delivery marked as failed
- Retry mechanism triggered
- Error details logged

## Shutdown Procedures

### Graceful Shutdown
1. Stop accepting new messages
2. Process remaining queued messages
3. Wait for in-flight deliveries
4. Shutdown executor services
5. Clean up resources

### Forced Shutdown
1. Immediate executor shutdown
2. Interrupt active threads
3. Clear all queues
4. Release resources