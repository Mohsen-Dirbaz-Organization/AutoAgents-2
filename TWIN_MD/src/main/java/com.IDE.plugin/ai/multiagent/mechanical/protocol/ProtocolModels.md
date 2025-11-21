# ProtocolModels

## Overview
Data models and structures that define the mechanical signaling protocol specifications.

## Purpose
The `ProtocolModels` class provides comprehensive data structures, enumerations, and constants that define the mechanical signaling protocol, ensuring consistency across implementations.

## Key Components

### Protocol Structures
- Message format definitions
- Header structures
- Payload schemas
- Control sequences
- Extension mechanisms

### Protocol Constants
- Version identifiers
- Message type codes
- Error codes
- Status codes
- Timing constants

### Data Models
- Connection state model
- Session information model
- Capability descriptor model
- QoS parameter model
- Security context model

### Enumerations
- Protocol states
- Message types
- Error categories
- Priority levels
- Encoding types

## Model Relationships
- Header-payload associations
- State transitions
- Error mappings
- Version compatibility
- Extension points

## Usage
Referenced by all protocol implementation components for consistent behavior.

## Integration Points
- `MechanicalSignalingProtocol`: Protocol implementation
- `ProtocolCoordinator`: Protocol management
- `ValidationModels`: Validation rules
- `TransmissionModels`: Transmission data

## Related Classes
- `MechanicalSignalingProtocol`: Protocol logic
- `PersistenceModels`: Storage models
- `ValidationModels`: Validation models