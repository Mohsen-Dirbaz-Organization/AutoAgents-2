# Message Class Documentation

## Overview
The `Message` class represents the fundamental communication unit in the multi-agent system. It encapsulates all information needed for inter-agent communication, including type, sender/recipient information, payload data, headers, and security features. The class supports message copying, retry tracking, and cryptographic signatures for secure communication.

## Multi-Level Architecture

### System Level
- **Role**: Core communication data structure
- **Purpose**: Standardized message format for all agent interactions
- **Features**: UUID generation, payload flexibility, security signatures
- **Pattern**: Mutable data transfer object with defensive copying

### Component Level
- **Type**: Data transfer object (DTO)
- **Package**: `com.IDE.plugin.ai.multiagent.communication`
- **Key Characteristics**:
  - Auto-generated unique IDs
  - Flexible payload structure
  - Header support for metadata
  - Retry tracking mechanism
  - Digital signature capability

## Core Features and Functionality

### Message Identification
- **Automatic ID Generation**: Uses UUID for globally unique message identifiers
- **Timestamp Tracking**: Records message creation/send time
- **Type Classification**: Strongly typed message categorization via MessageType enum

### Content Management
- **Flexible Payload**: Map<String, Object> allows arbitrary data structures
- **Header Support**: Additional metadata via Map<String, String>
- **Sender/Recipient Tracking**: Clear identification of communication parties

### Reliability Features
- **Retry Counting**: Built-in retry counter for failed deliveries
- **Message Copying**: Deep copy support for broadcast scenarios
- **Integrity Protection**: Digital signature field for verification

### Security Integration
- **Signature Support**: Field for cryptographic signatures
- **Header Metadata**: Security headers can be added
- **Authentication**: Supports token-based authentication via headers

## Component Props and Data Structures

### Core Fields
```java
private String id                     // Unique message identifier (UUID)
private MessageType type              // Message classification
private String sender                 // Sender agent ID
private String recipient              // Target agent ID (null for broadcast)
private Map<String, Object> payload   // Message content
private Map<String, String> headers   // Metadata headers
private long timestamp                // Message timestamp
private int retryCount                // Delivery retry counter
private String signature              // Digital signature
```

### Constructors
```java
// Default constructor - auto-generates ID
public Message()

// Convenience constructor with core fields
public Message(MessageType type, String sender, String recipient, Map<String, Object> payload)
```

### Key Methods
```java
// Creates deep copy of message
public Message copy()

// Increments retry counter
public void incrementRetryCount()

// Standard getters and setters for all fields
```

## Usage Patterns and Integration Points

### Basic Message Creation
```java
// Direct message
Message directMessage = new Message(
    MessageType.CODE_EDIT_REQUEST,
    "editor-agent-1",
    "architect-agent-1",
    Map.of(
        "file", "Example.java",
        "line", 42,
        "change", "refactor method"
    )
);

// Broadcast message (no recipient)
Message broadcast = new Message(
    MessageType.SYSTEM_BROADCAST,
    "orchestrator",
    null,
    Map.of("event", "system startup", "version", "1.0")
);
```

### Message with Headers
```java
Message secureMessage = new Message();
secureMessage.setType(MessageType.CRITICAL_ALERT);
secureMessage.setSender("monitor-agent");
secureMessage.setRecipient("admin-agent");
secureMessage.setPayload(Map.of("alert", "Resource exhaustion"));

// Add security headers
Map<String, String> headers = new HashMap<>();
headers.put("priority", "HIGH");
headers.put("auth-token", "bearer-xyz123");
headers.put("encryption", "AES-256");
secureMessage.setHeaders(headers);
```

### Retry Handling Pattern
```java
public void handleMessageDelivery(Message message) {
    try {
        deliverMessage(message);
    } catch (DeliveryException e) {
        message.incrementRetryCount();
        
        if (message.getRetryCount() < MAX_RETRIES) {
            scheduleRetry(message);
        } else {
            handleDeliveryFailure(message);
        }
    }
}
```

### Message Copying for Broadcast
```java
public void broadcastToAgents(Message template, List<String> agents) {
    for (String agentId : agents) {
        Message copy = template.copy();
        copy.setRecipient(agentId);
        copy.setTimestamp(System.currentTimeMillis());
        sendMessage(copy);
    }
}
```

### Signature Integration
```java
// Sign message before sending
public void signMessage(Message message, SigningService signer) {
    String signature = signer.sign(
        message.getId() + 
        message.getType() + 
        message.getSender() + 
        message.getPayload().toString()
    );
    message.setSignature(signature);
}

// Verify message on receipt
public boolean verifyMessage(Message message, SigningService signer) {
    return signer.verify(
        message.getId() + 
        message.getType() + 
        message.getSender() + 
        message.getPayload().toString(),
        message.getSignature()
    );
}
```

## Best Practices and Considerations

### Message Design Guidelines
1. **Type Selection**: Choose appropriate MessageType for clear intent
2. **Payload Structure**: Use consistent key names across message types
3. **Null Safety**: Handle null recipients for broadcast messages
4. **ID Preservation**: Don't modify message IDs after creation

### Performance Considerations
1. **Payload Size**: Keep payloads reasonably sized
2. **Deep Copying**: Be aware of copy() performance with large payloads
3. **Map Initialization**: Pre-size maps when possible
4. **Object Serialization**: Ensure payload objects are serializable

### Security Best Practices
1. **Sensitive Data**: Avoid putting credentials in payload
2. **Signature Verification**: Always verify signatures for critical messages
3. **Header Validation**: Validate security headers before processing
4. **Timestamp Checks**: Implement message age validation

### Retry Strategy
1. **Exponential Backoff**: Implement increasing delays between retries
2. **Max Retry Limits**: Set reasonable retry count limits
3. **Retry Logging**: Log retry attempts for debugging
4. **Idempotency**: Ensure message processing is idempotent

## Common Message Patterns

### Request-Response Pattern
```java
// Request
Message request = new Message(
    MessageType.ANALYSIS_REQUEST,
    "requester",
    "analyzer",
    Map.of("target", "module-x", "depth", "full")
);
request.getHeaders().put("correlation-id", "req-123");

// Response
Message response = new Message(
    MessageType.ANALYSIS_RESULT,
    "analyzer",
    "requester",
    Map.of("results", analysisResults)
);
response.getHeaders().put("correlation-id", "req-123");
```

### Event Notification Pattern
```java
Message event = new Message(
    MessageType.CODE_CHANGED,
    "editor",
    null, // Broadcast to all interested parties
    Map.of(
        "file", "Service.java",
        "changeType", "method-added",
        "timestamp", System.currentTimeMillis()
    )
);
```

### Command Pattern
```java
Message command = new Message(
    MessageType.SYSTEM_COMMAND,
    "orchestrator",
    "agent-123",
    Map.of(
        "command", "shutdown",
        "graceful", true,
        "timeout", 30000
    )
);
command.getHeaders().put("priority", "IMMEDIATE");
```

## Extension Points

1. **Custom Headers**: Add domain-specific headers
2. **Payload Validation**: Implement type-specific validators
3. **Compression**: Add payload compression for large messages
4. **Encryption**: Implement payload encryption/decryption
5. **Routing Information**: Add routing metadata to headers

## Integration Considerations

### With MessageBus
- Message ID used for tracking delivery
- Headers used for routing decisions
- Retry count managed during delivery attempts

### With Trust System
- Sender verification via trust scores
- Signature validation for trusted communication
- Headers contain trust tokens

### With Audit System
- All fields logged for audit trail
- Timestamp crucial for event ordering
- Correlation IDs in headers for request tracking