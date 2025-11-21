# SharedContext

## Overview
Core class providing shared state and context information across all agents in the system.

## Purpose
The `SharedContext` class serves as a centralized repository for system-wide state, configuration, and runtime information that needs to be accessible to all agents and services.

## Key Components

### State Management
- Global system state
- Configuration parameters
- Runtime metrics
- Resource allocation tracking

### Data Structures
- Thread-safe collections for concurrent access
- Immutable configuration objects
- Event history buffer
- Agent registry

### Methods
- Context initialization and cleanup
- State synchronization
- Configuration updates
- Event broadcasting
- Resource management

## Thread Safety
- All operations are thread-safe
- Uses concurrent data structures
- Implements read-write locking where appropriate

## Usage
Accessed by all agents and services to retrieve system-wide information and coordinate activities.

## Integration Points
- `Agent`: Reads context for decision making
- `MessageBus`: Uses context for routing
- `TaskScheduler`: Accesses resource availability
- `SystemMonitor`: Updates system metrics

## Related Classes
- `SharedContext` (in context package): Extended context functionality
- `MemoryManager`: Manages context persistence
- `EventBus`: Propagates context changes