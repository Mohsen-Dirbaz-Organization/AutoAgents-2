# MessageHandler Interface Documentation

## Overview
`MessageHandler` is a simple yet fundamental interface that defines the contract for handling incoming messages in the multi-agent system. It provides a single method that all message-receiving components must implement, ensuring a consistent approach to message processing across the system.

## Multi-Level Architecture

### System Level
- **Role**: Message processing contract
- **Purpose**: Standardizes how agents and components receive and process messages
- **Pattern**: Callback interface for asynchronous message handling
- **Integration**: Used by MessageBus for message delivery

### Component Level
- **Type**: Functional interface
- **Package**: `com.IDE.plugin.ai.multiagent.communication`
- **Characteristics**:
  - Single abstract method (SAM) - functional interface
  - No return value - asynchronous processing
  - No thrown exceptions - error handling internal

## Core Features and Functionality

### Message Reception
- **Single Entry Point**: All messages flow through handleMessage()
- **Type Agnostic**: Handles all MessageType variants
- **Asynchronous Design**: No return value enables non-blocking processing
- **Error Containment**: Implementations must handle exceptions internally

## Interface Definition

```java
public interface MessageHandler {
    /**
     * Handle an incoming message
     * @param message The message to handle
     */
    void handleMessage(Message message);
}
```

## Usage Patterns and Integration Points

### Basic Implementation
```java
public class SimpleAgent implements MessageHandler {
    @Override
    public void handleMessage(Message message) {
        System.out.println("Received message: " + message.getType());
        
        switch (message.getType()) {
            case ANALYSIS_REQUEST:
                processAnalysisRequest(message);
                break;
            case STATUS_UPDATE:
                updateStatus(message);
                break;
            default:
                log("Unknown message type: " + message.getType());
        }
    }
}
```

### Lambda Implementation
```java
// Functional interface allows lambda expressions
MessageHandler handler = message -> {
    log("Processing: " + message.getId());
    processMessage(message);
};

// Register with message bus
messageBus.registerAgent("agent-1", AgentRole.ANALYZER, handler);
```

### Async Processing Pattern
```java
public class AsyncMessageHandler implements MessageHandler {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    
    @Override
    public void handleMessage(Message message) {
        // Non-blocking processing
        executor.submit(() -> {
            try {
                processMessageAsync(message);
            } catch (Exception e) {
                handleProcessingError(message, e);
            }
        });
    }
}
```

### Type-Safe Handler Pattern
```java
public abstract class TypedMessageHandler implements MessageHandler {
    
    @Override
    public void handleMessage(Message message) {
        switch (message.getType()) {
            case CODE_EDIT_REQUEST:
                handleCodeEditRequest(message);
                break;
            case REFACTORING_REQUEST:
                handleRefactoringRequest(message);
                break;
            default:
                handleUnknownType(message);
        }
    }
    
    protected abstract void handleCodeEditRequest(Message message);
    protected abstract void handleRefactoringRequest(Message message);
    protected void handleUnknownType(Message message) {
        log("Unhandled message type: " + message.getType());
    }
}
```

### Error Handling Pattern
```java
public class ResilientMessageHandler implements MessageHandler {
    
    @Override
    public void handleMessage(Message message) {
        try {
            validateMessage(message);
            processMessage(message);
            acknowledgeMessage(message);
        } catch (ValidationException e) {
            handleValidationError(message, e);
        } catch (ProcessingException e) {
            handleProcessingError(message, e);
        } catch (Exception e) {
            handleUnexpectedError(message, e);
        }
    }
    
    private void handleValidationError(Message message, Exception e) {
        log("Validation failed for message: " + message.getId());
        sendErrorResponse(message.getSender(), e.getMessage());
    }
}
```

## Implementation Guidelines

### Best Practices

1. **Non-Blocking Operations**
   - Never block in handleMessage()
   - Use async processing for long operations
   - Return quickly to avoid queue backup

2. **Error Handling**
   - Catch all exceptions internally
   - Log errors with context
   - Send error responses when appropriate
   - Never let exceptions propagate

3. **Message Validation**
   - Validate message structure
   - Check required payload fields
   - Verify sender authorization
   - Handle malformed messages gracefully

4. **Performance Considerations**
   - Keep processing lightweight
   - Offload heavy work to thread pools
   - Avoid synchronous I/O operations
   - Monitor processing times

### Common Implementation Patterns

#### State Machine Pattern
```java
public class StatefulHandler implements MessageHandler {
    private State currentState = State.IDLE;
    
    @Override
    public void handleMessage(Message message) {
        State newState = currentState.handleMessage(message);
        if (newState != currentState) {
            transitionTo(newState);
        }
    }
}
```

#### Chain of Responsibility Pattern
```java
public class ChainedHandler implements MessageHandler {
    private final List<MessageProcessor> processors;
    
    @Override
    public void handleMessage(Message message) {
        for (MessageProcessor processor : processors) {
            if (processor.canHandle(message)) {
                processor.process(message);
                break;
            }
        }
    }
}
```

#### Decorator Pattern
```java
public class LoggingHandler implements MessageHandler {
    private final MessageHandler delegate;
    
    public LoggingHandler(MessageHandler delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void handleMessage(Message message) {
        log("Received: " + message);
        long start = System.currentTimeMillis();
        
        delegate.handleMessage(message);
        
        long duration = System.currentTimeMillis() - start;
        log("Processed in " + duration + "ms");
    }
}
```

## Integration Scenarios

### With MessageBus
```java
// Registration
MessageBus bus = new MessageBus();
MessageHandler handler = new MyHandler();
bus.registerAgent("agent-1", AgentRole.ANALYZER, handler);

// Message delivery flow
// 1. MessageBus receives message
// 2. Routes to appropriate agent queue
// 3. Delivery thread invokes handler.handleMessage()
// 4. Handler processes asynchronously
```

### With Agent Classes
```java
public class BaseAgent implements MessageHandler {
    
    @Override
    public void handleMessage(Message message) {
        recordMessageReceived(message);
        
        if (canHandle(message)) {
            processMessage(message);
        } else {
            forwardMessage(message);
        }
    }
}
```

### With Trust System
```java
public class TrustedHandler implements MessageHandler {
    private final TrustManager trustManager;
    
    @Override
    public void handleMessage(Message message) {
        if (!trustManager.isTrusted(message.getSender())) {
            rejectUntrustedMessage(message);
            return;
        }
        
        processeTrustedMessage(message);
    }
}
```

## Anti-Patterns to Avoid

1. **Blocking Operations**
   ```java
   // BAD: Blocks message processing
   public void handleMessage(Message message) {
       Thread.sleep(5000); // Never do this!
       processMessage(message);
   }
   ```

2. **Exception Propagation**
   ```java
   // BAD: Throws exceptions
   public void handleMessage(Message message) throws Exception {
       processMessage(message); // Don't declare throws
   }
   ```

3. **Synchronous I/O**
   ```java
   // BAD: Synchronous database call
   public void handleMessage(Message message) {
       database.save(message); // Blocks on I/O
   }
   ```

4. **Infinite Loops**
   ```java
   // BAD: Can hang the system
   public void handleMessage(Message message) {
       while (!processed) { // Potential infinite loop
           tryProcess(message);
       }
   }
   ```

## Testing Considerations

### Unit Testing
```java
@Test
public void testMessageHandling() {
    MessageHandler handler = new MyHandler();
    Message testMessage = new Message(
        MessageType.STATUS_UPDATE,
        "sender",
        "recipient",
        Map.of("status", "active")
    );
    
    handler.handleMessage(testMessage);
    
    // Verify expected behavior
    verify(mockService).updateStatus("active");
}
```

### Mock Implementation
```java
public class MockMessageHandler implements MessageHandler {
    private final List<Message> receivedMessages = new ArrayList<>();
    
    @Override
    public void handleMessage(Message message) {
        receivedMessages.add(message);
    }
    
    public List<Message> getReceivedMessages() {
        return new ArrayList<>(receivedMessages);
    }
}
```

## Performance Implications

1. **Handler Speed**: Directly impacts message throughput
2. **Queue Buildup**: Slow handlers cause queue overflow
3. **Memory Usage**: Stateful handlers consume more memory
4. **Thread Safety**: Concurrent calls require synchronization