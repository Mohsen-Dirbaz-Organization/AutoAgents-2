# TaskResult

## Overview
Model representing the outcome of task execution by an agent or group of agents.

## Purpose
The `TaskResult` class captures comprehensive information about task execution results, including success/failure status, output data, performance metrics, and execution details.

## Key Components

### Result Information
- Task identifier
- Execution status
- Start/end timestamps
- Executing agent(s)
- Result code

### Output Data
- Primary results
- Secondary outputs
- Generated artifacts
- Log information
- Error details

### Performance Metrics
- Execution duration
- Resource usage
- Quality metrics
- Efficiency scores
- Cost analysis

### Methods
- Result creation
- Status determination
- Metric calculation
- Result validation
- Aggregation support

## Result Categories
- SUCCESS: Completed successfully
- PARTIAL_SUCCESS: Partially completed
- FAILURE: Execution failed
- TIMEOUT: Exceeded time limit
- CANCELLED: Execution cancelled

## Usage
Used for task completion tracking, performance analysis, and decision making.

## Integration Points
- `Task`: Source task
- `Agent`: Result producer
- `TaskScheduler`: Result processing
- `HistoryManager`: Result archival

## Related Classes
- `Task`: Executed task
- `AgentMetrics`: Performance data
- `GroupTaskRequest`: Multi-agent results