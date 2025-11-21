# CollisionResolution Class Documentation

## Overview
`CollisionResolution` represents the outcome and instructions for resolving a detected collision between agents. It encapsulates the resolution strategy, status, and specific actions each involved agent should take. The class uses the Builder pattern for flexible construction and maintains immutability for thread-safe operations across the distributed agent system.

## Multi-Level Architecture

### System Level
- **Role**: Resolution outcome and instruction carrier
- **Purpose**: Communicates resolution decisions to involved agents
- **Integration**: Created by resolution strategies, consumed by agents and monitors
- **Pattern**: Immutable value object with Builder pattern

### Component Level
- **Type**: Immutable data class with Builder
- **Package**: `com.IDE.plugin.ai.multiagent.collision`
- **Key Features**:
  - Strategy documentation
  - Status tracking
  - Agent-specific action mapping
  - Optional winner designation
  - Temporal tracking

## Core Features and Functionality

### Resolution Strategies
The class supports various resolution strategies through the `ResolutionStrategy` enum:

- **PRIORITY_BASED**: Higher priority agent wins resource access
- **ROUND_ROBIN**: Agents take turns accessing resource
- **TIME_SLICING**: Time-based resource sharing with allocated slots
- **CAPABILITY_BASED**: Best suited agent for the task wins
- **VOTING**: Agents vote on resolution outcome
- **RETRY**: Agents retry with exponential backoff
- **DELEGATION**: Work delegated to another capable agent
- **ABORT**: Conflicting operations are aborted

### Resolution Status Tracking
The `ResolutionStatus` enum tracks resolution lifecycle:

- **PENDING**: Resolution identified but not started
- **IN_PROGRESS**: Resolution actively being applied
- **RESOLVED**: Successfully resolved
- **FAILED**: Resolution attempt failed
- **ESCALATED**: Escalated to higher authority

### Agent Action Mapping
- Maps each agent ID to specific action instructions
- Provides clear directives for conflict resolution
- Enables coordinated multi-agent responses

### Winner Designation
- Optional winner field for competitive resolutions
- Used in PRIORITY_BASED and CAPABILITY_BASED strategies
- Wrapped in Optional for null safety

## Component Props and Data Structures

### Core Fields
```java
private final String resolutionId       // Unique resolution identifier
private final String collisionId        // Reference to original collision
private final ResolutionStrategy strategy  // Applied strategy
private final ResolutionStatus status   // Current resolution status
private final String winnerId           // Optional winning agent
private final Map<String, String> agentActions  // Agent-specific actions
private final Instant resolvedAt        // Resolution timestamp
private final String description        // Human-readable description
```

### Builder Pattern Implementation
```java
public static class Builder {
    // Builder fields mirror main class fields
    // Provides fluent API for object construction
    // Handles defaults and validation
}
```

## Usage Patterns and Integration Points

### Basic Resolution Creation
```java
// Simple resolution with winner
CollisionResolution resolution = CollisionResolution.builder()
    .resolutionId("resolution-123")
    .collisionId("collision-456")
    .strategy(ResolutionStrategy.PRIORITY_BASED)
    .status(ResolutionStatus.RESOLVED)
    .winnerId("high-priority-agent")
    .description("Resolved based on agent priority")
    .build();

// Complex resolution with agent actions
Map<String, String> actions = Map.of(
    "agent-1", "retry after 5 seconds",
    "agent-2", "proceed with operation",
    "agent-3", "abort and reschedule"
);

CollisionResolution complexResolution = CollisionResolution.builder()
    .resolutionId("resolution-789")
    .collisionId("collision-101")
    .strategy(ResolutionStrategy.TIME_SLICING)
    .status(ResolutionStatus.IN_PROGRESS)
    .agentActions(actions)
    .description("Time-sliced access with 10-second windows")
    .build();
```

### Strategy-Specific Patterns

#### Priority-Based Resolution
```java
CollisionResolution priorityResolution = CollisionResolution.builder()
    .resolutionId(UUID.randomUUID().toString())
    .collisionId(collision.getId())
    .strategy(ResolutionStrategy.PRIORITY_BASED)
    .status(ResolutionStatus.RESOLVED)
    .winnerId(highestPriorityAgent.getId())
    .agentActions(Map.of(
        highestPriorityAgent.getId(), "proceed",
        otherAgent.getId(), "wait and retry"
    ))
    .description("Highest priority agent granted access")
    .build();
```

#### Round-Robin Resolution
```java
CollisionResolution roundRobinResolution = CollisionResolution.builder()
    .resolutionId(UUID.randomUUID().toString())
    .collisionId(collision.getId())
    .strategy(ResolutionStrategy.ROUND_ROBIN)
    .status(ResolutionStatus.IN_PROGRESS)
    .agentActions(Map.of(
        "agent-1", "access slot 0-100ms",
        "agent-2", "access slot 100-200ms",
        "agent-3", "access slot 200-300ms"
    ))
    .description("Round-robin time slots allocated")
    .build();
```

#### Delegation Resolution
```java
CollisionResolution delegationResolution = CollisionResolution.builder()
    .resolutionId(UUID.randomUUID().toString())
    .collisionId(collision.getId())
    .strategy(ResolutionStrategy.DELEGATION)
    .status(ResolutionStatus.RESOLVED)
    .winnerId("specialized-agent")
    .agentActions(Map.of(
        "agent-1", "delegate to specialized-agent",
        "agent-2", "delegate to specialized-agent",
        "specialized-agent", "handle delegated task"
    ))
    .description("Task delegated to specialized agent")
    .build();
```

### Integration with Agents
```java
public class AgentCollisionHandler {
    public void handleResolution(CollisionResolution resolution) {
        String myAction = resolution.getAgentActions().get(agentId);
        
        switch (myAction) {
            case "proceed":
                executeOriginalTask();
                break;
            case "wait and retry":
                scheduleRetry(resolution);
                break;
            case "abort":
                cancelTask();
                break;
            default:
                handleCustomAction(myAction);
        }
        
        // Check if we won
        resolution.getWinnerId().ifPresent(winner -> {
            if (winner.equals(agentId)) {
                handleWinningScenario();
            }
        });
    }
}
```

## Best Practices and Considerations

### Builder Usage Guidelines
1. **Required Fields**: Always set resolutionId, collisionId, strategy, and status
2. **Defaults**: Builder handles default timestamps and empty collections
3. **Validation**: Consider adding validation in build() method
4. **Immutability**: Never expose mutable collections directly

### Status Management
1. **Lifecycle Tracking**: Update status as resolution progresses
2. **Failure Handling**: Always provide clear failure reasons
3. **Escalation Criteria**: Define when to escalate vs retry
4. **Monitoring**: Track status transitions for analytics

### Action Specification
1. **Clear Instructions**: Make agent actions unambiguous
2. **Standardized Actions**: Use consistent action vocabulary
3. **Fallback Actions**: Include default actions for unknown agents
4. **Validation**: Ensure all involved agents have actions

### Winner Selection
1. **Fairness**: Implement fair winner selection algorithms
2. **Rotation**: Consider rotating winners for repeated collisions
3. **Metrics**: Track win rates to prevent starvation
4. **Null Safety**: Always use Optional for winner access

## Common Resolution Patterns

### Retry with Backoff
```java
Map<String, String> retryActions = agents.stream()
    .collect(Collectors.toMap(
        agent -> agent,
        agent -> String.format("retry after %d ms", 
            calculateBackoff(agent, attemptCount))
    ));
```

### Capability-Based Routing
```java
String mostCapableAgent = findMostCapableAgent(task, agents);
Map<String, String> actions = agents.stream()
    .collect(Collectors.toMap(
        agent -> agent,
        agent -> agent.equals(mostCapableAgent) ? 
            "execute task" : "standby for next task"
    ));
```

### Time-Sliced Access
```java
int slotDuration = 100; // milliseconds
AtomicInteger slot = new AtomicInteger(0);
Map<String, String> timeSlots = agents.stream()
    .collect(Collectors.toMap(
        agent -> agent,
        agent -> String.format("access window %d-%d ms",
            slot.get() * slotDuration,
            slot.incrementAndGet() * slotDuration)
    ));
```

## Performance Considerations

1. **Immutable Maps**: Use Map.copyOf() for defensive copying
2. **Builder Efficiency**: Reuse builders for similar resolutions
3. **String Operations**: Cache formatted descriptions
4. **Optional Usage**: Prefer Optional over null checks

## Extension Points

1. **Custom Strategies**: Extend ResolutionStrategy enum
2. **Status Workflows**: Implement state machine for status transitions
3. **Action Validators**: Add action syntax validation
4. **Metrics Collection**: Extract resolution metrics for analysis