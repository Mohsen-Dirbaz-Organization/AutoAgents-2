# ServiceException

## Overview
Custom exception class for handling service-level errors in the multi-agent system.

## Purpose
The `ServiceException` class provides a standardized way to handle and propagate errors that occur within various services of the multi-agent system, including detailed error information and recovery suggestions.

## Key Components

### Fields
- Error code and message
- Service identifier
- Timestamp of occurrence
- Stack trace information
- Recovery suggestions
- Related context data

### Constructors
- Basic constructor with message
- Constructor with message and cause
- Constructor with error code and message
- Full constructor with all parameters

### Methods
- Error serialization for logging
- Context data attachment
- Recovery action suggestions
- Error severity determination

## Error Categories
- Configuration errors
- Communication failures
- Resource unavailability
- Authentication/authorization failures
- Task execution errors

## Usage
Thrown by services when recoverable or non-recoverable errors occur during operation.

## Integration Points
- All service classes throw ServiceException
- `SystemMonitor`: Tracks and analyzes service exceptions
- `HistoryManager`: Logs exception history for analysis

## Related Classes
- `SystemHealthStatus`: Aggregates service exceptions
- `AgentState`: Updated based on service exceptions