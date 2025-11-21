# GroupTaskRequest

## Overview
Represents a request for executing a task that requires coordination among multiple agents.

## Purpose
The `GroupTaskRequest` class encapsulates task information that needs to be executed by a group of agents working collaboratively, including task decomposition, agent assignments, and coordination requirements.

## Key Components

### Fields
- Task identifier and metadata
- Required agent roles and capabilities
- Task decomposition structure
- Coordination strategy
- Deadline and priority information
- Resource requirements

### Methods
- Task validation and feasibility checking
- Agent assignment logic
- Task distribution and scheduling
- Progress tracking and monitoring

## Usage
Used when a complex task needs to be distributed among multiple agents based on their capabilities and availability.

## Integration Points
- `GroupCoordinationFramework`: Manages group task execution
- `TaskScheduler`: Schedules and prioritizes group tasks
- `AgentCoordinatorService`: Assigns agents to group tasks

## Related Classes
- `Task`: Base task representation
- `TaskResult`: Group task execution results
- `AgentRole`: Determines agent eligibility for tasks