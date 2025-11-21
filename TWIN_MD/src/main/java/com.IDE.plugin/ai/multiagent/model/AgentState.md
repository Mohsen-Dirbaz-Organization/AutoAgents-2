# AgentState

## Overview
Model representing the current state and status of an agent.

## Purpose
The `AgentState` class captures the complete runtime state of an agent, including operational status, current activities, and state history.

## Key Components

### State Information
- Current state enum
- State timestamp
- Previous state
- State transition reason
- State duration

### Operational Status
- Availability status
- Current task
- Resource utilization
- Performance metrics
- Health indicators

### State Types
- IDLE: Ready for tasks
- BUSY: Executing task
- PAUSED: Temporarily suspended
- ERROR: Error condition
- TERMINATED: Shutdown

### Methods
- State transition
- State validation
- History tracking
- State persistence
- State querying

## State Transitions
- Valid transition paths
- Transition conditions
- Transition callbacks
- State guards
- Rollback support

## Usage
Used for agent lifecycle management, task assignment, and monitoring.

## Integration Points
- `Agent`: State holder
- `AgentManagementService`: State management
- `TaskScheduler`: State-based scheduling
- `SystemMonitor`: State monitoring

## Related Classes
- `Agent`: State owner
- `AgentEvent`: State change events
- `AgentMetrics`: State-based metrics