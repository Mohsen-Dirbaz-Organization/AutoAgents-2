# AgentMessage

## Overview
Model representing messages exchanged between agents in the system.

## Purpose
The `AgentMessage` class encapsulates all information needed for inter-agent communication, including message content, metadata, and routing information.

## Key Components

### Message Structure
- Message identifier
- Sender agent ID
- Recipient agent ID(s)
- Message type
- Timestamp

### Message Content
- Primary payload
- Attachments
- Encoding format
- Compression flag
- Encryption status

### Metadata
- Priority level
- Expiration time
- Correlation ID
- Reply-to address
- Custom headers

### Methods
- Message creation
- Serialization/deserialization
- Validation
- Routing determination
- Reply generation

## Message Types
- Request messages
- Response messages
- Event notifications
- Command messages
- Status updates

## Usage
Standard format for all agent-to-agent communication.

## Integration Points
- `MessageBus`: Message transport
- `Agent`: Message sending/receiving
- `MessageHandler`: Message processing
- `TrustedMessageBus`: Secure messaging

## Related Classes
- `Message`: Base message class
- `MessageType`: Message categorization
- `MessageHandler`: Processing logic