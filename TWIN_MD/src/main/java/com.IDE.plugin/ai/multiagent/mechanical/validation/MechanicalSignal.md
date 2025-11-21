# MechanicalSignal

## Overview
Core signal representation for the mechanical signaling system, providing the foundational data structure for inter-agent communication within the mechanical signaling protocol.

## Class Description
`MechanicalSignal` represents a complete signal packet transmitted between agents in the mechanical signaling system. It encapsulates all necessary information for reliable, secure, and traceable communication.

## Key Components

### Core Properties
- **id**: Unique identifier for the signal
- **senderId**: Identifier of the agent sending the signal
- **recipientId**: Identifier of the intended recipient agent
- **payload**: Byte array containing the actual signal data
- **timestamp**: Time when the signal was created
- **sequenceNumber**: Order identifier for signal sequencing
- **priority**: Signal priority level (CRITICAL, HIGH, NORMAL, LOW)
- **checksum**: Integrity verification hash
- **signature**: Cryptographic signature for authenticity
- **metadata**: Additional context information map
- **hopCount**: Number of routing hops
- **retryCount**: Number of transmission attempts

### Constructor
```java
public MechanicalSignal(String id, String senderId, String recipientId, 
                      byte[] payload, int sequenceNumber, SignalPriority priority)
```
Creates a new mechanical signal with:
- Automatic timestamp generation
- Checksum calculation
- Metadata map initialization
- Zero hop and retry counts

### Methods

#### Getters (Immutable Access)
- `getId()`: Returns signal identifier
- `getSenderId()`: Returns sender identifier
- `getRecipientId()`: Returns recipient identifier
- `getPayload()`: Returns copy of payload (defensive copying)
- `getTimestamp()`: Returns creation timestamp
- `getSequenceNumber()`: Returns sequence number
- `getPriority()`: Returns priority level
- `getChecksum()`: Returns calculated checksum
- `getSignature()`: Returns cryptographic signature
- `getMetadata()`: Returns copy of metadata map
- `getHopCount()`: Returns current hop count
- `getRetryCount()`: Returns retry attempt count

#### Setters (Mutable Operations)
- `setSignature(String signature)`: Sets cryptographic signature
- `incrementHopCount()`: Increments hop counter
- `incrementRetryCount()`: Increments retry counter
- `addMetadata(String key, Object value)`: Adds metadata entry

### SignalPriority Enum
Defines signal priority levels with numeric values:
- **CRITICAL** (0): Highest priority signals
- **HIGH** (1): High priority signals
- **NORMAL** (2): Standard priority signals
- **LOW** (3): Low priority signals

## Design Patterns

### Immutability Pattern
- Core fields are final and immutable
- Payload is defensively copied on creation and retrieval
- Metadata map is copied on retrieval

### Builder-like Pattern
- Constructor initializes all required fields
- Mutable fields can be set post-creation
- Metadata can be added incrementally

## Security Features

### Data Integrity
- Automatic checksum calculation on creation
- Checksum based on payload hash
- Immutable core fields prevent tampering

### Authentication Support
- Signature field for cryptographic signing
- Sender identification through senderId
- Sequence numbers prevent replay attacks

## Performance Considerations

### Memory Efficiency
- Payload stored as byte array for efficiency
- Lazy initialization where possible
- Concurrent hashmap for thread-safe metadata

### Processing Efficiency
- Simple checksum calculation (array hash)
- Minimal object creation
- Direct field access through getters

## Usage Example
```java
// Create a signal
byte[] data = "Hello Agent".getBytes();
MechanicalSignal signal = new MechanicalSignal(
    UUID.randomUUID().toString(),
    "agent-1",
    "agent-2",
    data,
    1,
    SignalPriority.NORMAL
);

// Add metadata
signal.addMetadata("contentType", "text/plain");
signal.addMetadata("encoding", "UTF-8");

// Set signature (typically done by sender)
signal.setSignature(cryptoService.sign(signal));

// Track routing
signal.incrementHopCount();

// Access signal data
String id = signal.getId();
byte[] payload = signal.getPayload();
SignalPriority priority = signal.getPriority();
```

## Thread Safety
- Immutable core fields are inherently thread-safe
- Metadata uses ConcurrentHashMap for thread safety
- Atomic operations for hop and retry counts

## Integration Points
- Used by `MechanicalSignalValidator` for validation
- Analyzed by `MechanicalSignalAnalyzer` for patterns
- Coordinated by `SignalValidationCoordinator`
- Transmitted through mechanical signaling protocol

## Best Practices
1. Always validate signals before processing
2. Use appropriate priority levels
3. Add relevant metadata for context
4. Monitor hop counts for routing efficiency
5. Implement proper signature verification
6. Handle payload size limits appropriately

## Future Enhancements
- Compression support for large payloads
- Encryption for sensitive data
- Extended metadata schemas
- Signal versioning support
- TTL (Time To Live) implementation