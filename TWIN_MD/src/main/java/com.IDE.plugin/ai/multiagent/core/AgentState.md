# AgentState Enum Documentation

## Overview
`AgentState` represents the various operational states an agent can be in within the multi-agent system. This enumeration provides a simple yet comprehensive state model for agent lifecycle management, enabling consistent state tracking, transitions, and monitoring across the system.

## Multi-Level Architecture

### System Level
- **Role**: Agent lifecycle state management
- **Purpose**: Standardize agent operational states
- **Pattern**: Simple state enumeration
- **Integration**: Used by agents, monitors, and coordinators

### Component Level
- **Type**: State enumeration
- **Package**: `com.IDE.plugin.ai.multiagent.core`
- **States**: Five distinct operational states
- **Simplicity**: Clear, unambiguous state definitions

## State Definitions

### IDLE
- **Description**: Agent is registered but not yet active
- **Characteristics**:
  - Agent is available for task assignment
  - No current task execution
  - Ready to transition to ACTIVE state
  - Normal operational state between tasks
- **Transitions**:
  - To ACTIVE: When task assigned
  - To SUSPENDED: When manually paused
  - To SHUTDOWN: When terminating

### ACTIVE
- **Description**: Agent is actively processing a task
- **Characteristics**:
  - Currently executing assigned work
  - Resources allocated for task
  - Not available for new tasks
  - Performance metrics being recorded
- **Transitions**:
  - To IDLE: When task completes
  - To ERROR: When task fails
  - To SUSPENDED: When interrupted
  - To SHUTDOWN: When forced termination

### SUSPENDED
- **Description**: Agent is temporarily suspended
- **Characteristics**:
  - Execution paused but not terminated
  - State preserved for resumption
  - Not accepting new tasks
  - Resources may be partially released
- **Transitions**:
  - To IDLE: When resumed without task
  - To ACTIVE: When resumed with task
  - To SHUTDOWN: When terminating

### ERROR
- **Description**: Agent has encountered an error
- **Characteristics**:
  - Non-operational due to failure
  - Requires intervention or recovery
  - Not accepting new tasks
  - Error details should be logged
- **Transitions**:
  - To IDLE: After successful recovery
  - To SHUTDOWN: If unrecoverable

### SHUTDOWN
- **Description**: Agent is shutting down
- **Characteristics**:
  - Terminal state
  - Resources being released
  - No new tasks accepted
  - Cleanup in progress
- **Transitions**:
  - None (terminal state)

## Usage Patterns and Integration Points

### Basic State Management
```java
public class Agent {
    private volatile AgentState state = AgentState.IDLE;
    private final Object stateLock = new Object();
    
    public boolean assignTask(Task task) {
        synchronized (stateLock) {
            if (state == AgentState.IDLE) {
                state = AgentState.ACTIVE;
                startTaskExecution(task);
                return true;
            }
            return false;
        }
    }
    
    public void completeTask() {
        synchronized (stateLock) {
            if (state == AgentState.ACTIVE) {
                state = AgentState.IDLE;
                notifyTaskCompletion();
            }
        }
    }
    
    public void handleError(Exception e) {
        synchronized (stateLock) {
            state = AgentState.ERROR;
            logError(e);
            notifyError();
        }
    }
}
```

### State-Based Availability
```java
public class AgentSelector {
    public List<Agent> getAvailableAgents(List<Agent> agents) {
        return agents.stream()
            .filter(agent -> agent.getState() == AgentState.IDLE)
            .collect(Collectors.toList());
    }
    
    public boolean isAgentOperational(Agent agent) {
        AgentState state = agent.getState();
        return state == AgentState.IDLE || state == AgentState.ACTIVE;
    }
    
    public List<Agent> getProblematicAgents(List<Agent> agents) {
        return agents.stream()
            .filter(agent -> agent.getState() == AgentState.ERROR)
            .collect(Collectors.toList());
    }
}
```

### State Transition Validation
```java
public class StateManager {
    private static final Map<AgentState, Set<AgentState>> VALID_TRANSITIONS = Map.of(
        AgentState.IDLE, Set.of(AgentState.ACTIVE, AgentState.SUSPENDED, 
                               AgentState.SHUTDOWN),
        AgentState.ACTIVE, Set.of(AgentState.IDLE, AgentState.ERROR, 
                                 AgentState.SUSPENDED, AgentState.SHUTDOWN),
        AgentState.SUSPENDED, Set.of(AgentState.IDLE, AgentState.ACTIVE, 
                                    AgentState.SHUTDOWN),
        AgentState.ERROR, Set.of(AgentState.IDLE, AgentState.SHUTDOWN),
        AgentState.SHUTDOWN, Set.of() // No transitions from shutdown
    );
    
    public boolean canTransition(AgentState from, AgentState to) {
        Set<AgentState> validTargets = VALID_TRANSITIONS.get(from);
        return validTargets != null && validTargets.contains(to);
    }
    
    public void transition(Agent agent, AgentState newState) {
        AgentState currentState = agent.getState();
        if (!canTransition(currentState, newState)) {
            throw new IllegalStateException(
                String.format("Invalid transition from %s to %s", 
                            currentState, newState)
            );
        }
        agent.setState(newState);
    }
}
```

### State Monitoring
```java
public class AgentMonitor {
    private final Map<String, AgentState> previousStates = new HashMap<>();
    
    public void monitorStateChanges(List<Agent> agents) {
        for (Agent agent : agents) {
            AgentState currentState = agent.getState();
            AgentState previousState = previousStates.get(agent.getId());
            
            if (previousState != null && previousState != currentState) {
                handleStateChange(agent, previousState, currentState);
            }
            
            previousStates.put(agent.getId(), currentState);
        }
    }
    
    private void handleStateChange(Agent agent, AgentState from, AgentState to) {
        // Log state transition
        log(String.format("Agent %s transitioned from %s to %s", 
                         agent.getId(), from, to));
        
        // Handle specific transitions
        if (to == AgentState.ERROR) {
            alertOnError(agent);
        } else if (from == AgentState.ERROR && to == AgentState.IDLE) {
            notifyRecovery(agent);
        } else if (to == AgentState.SHUTDOWN) {
            cleanupAgent(agent);
        }
    }
}
```

## State Management Best Practices

### Thread Safety
```java
public class ThreadSafeAgent {
    private volatile AgentState state = AgentState.IDLE;
    private final ReadWriteLock stateLock = new ReentrantReadWriteLock();
    
    public AgentState getState() {
        stateLock.readLock().lock();
        try {
            return state;
        } finally {
            stateLock.readLock().unlock();
        }
    }
    
    public void setState(AgentState newState) {
        stateLock.writeLock().lock();
        try {
            AgentState oldState = this.state;
            this.state = newState;
            notifyStateListeners(oldState, newState);
        } finally {
            stateLock.writeLock().unlock();
        }
    }
}
```

### State Persistence
```java
public class StatePersistence {
    public void saveAgentState(Agent agent) {
        // Persist state for recovery
        persistence.save(agent.getId(), agent.getState());
    }
    
    public void restoreAgentState(Agent agent) {
        AgentState savedState = persistence.load(agent.getId());
        if (savedState != null && savedState != AgentState.SHUTDOWN) {
            // Don't restore to ACTIVE state
            if (savedState == AgentState.ACTIVE) {
                agent.setState(AgentState.IDLE);
            } else {
                agent.setState(savedState);
            }
        }
    }
}
```

### State-Based Metrics
```java
public class StateMetrics {
    private final Map<AgentState, AtomicInteger> stateCounts = 
        new EnumMap<>(AgentState.class);
    private final Map<AgentState, AtomicLong> stateDurations = 
        new EnumMap<>(AgentState.class);
    
    public void updateMetrics(List<Agent> agents) {
        // Reset counts
        Arrays.stream(AgentState.values())
            .forEach(state -> stateCounts.put(state, new AtomicInteger(0)));
        
        // Count agents in each state
        agents.forEach(agent -> {
            stateCounts.get(agent.getState()).incrementAndGet();
        });
    }
    
    public Map<AgentState, Integer> getStateCounts() {
        Map<AgentState, Integer> result = new EnumMap<>(AgentState.class);
        stateCounts.forEach((state, count) -> 
            result.put(state, count.get()));
        return result;
    }
}
```

## Common State Patterns

### Lifecycle Management
```java
public class AgentLifecycle {
    public void startAgent(Agent agent) {
        if (agent.getState() == AgentState.SHUTDOWN) {
            throw new IllegalStateException("Cannot start shutdown agent");
        }
        agent.setState(AgentState.IDLE);
    }
    
    public void stopAgent(Agent agent) {
        AgentState state = agent.getState();
        if (state == AgentState.ACTIVE) {
            // Gracefully complete current task
            agent.requestShutdown();
        } else {
            agent.setState(AgentState.SHUTDOWN);
        }
    }
    
    public void suspendAgent(Agent agent) {
        AgentState state = agent.getState();
        if (state == AgentState.IDLE || state == AgentState.ACTIVE) {
            agent.setState(AgentState.SUSPENDED);
        }
    }
}
```

### Error Recovery
```java
public class ErrorRecovery {
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private final Map<String, Integer> errorCounts = new HashMap<>();
    
    public void handleAgentError(Agent agent, Exception error) {
        agent.setState(AgentState.ERROR);
        
        int errorCount = errorCounts.getOrDefault(agent.getId(), 0) + 1;
        errorCounts.put(agent.getId(), errorCount);
        
        if (errorCount < MAX_RETRY_ATTEMPTS) {
            scheduleRecovery(agent);
        } else {
            markUnrecoverable(agent);
        }
    }
    
    private void attemptRecovery(Agent agent) {
        try {
            agent.reset();
            agent.setState(AgentState.IDLE);
            errorCounts.remove(agent.getId());
        } catch (Exception e) {
            // Recovery failed, remains in ERROR state
        }
    }
}
```

### State Visualization
```java
public class StateVisualizer {
    public String getStateIcon(AgentState state) {
        switch (state) {
            case IDLE:      return "⚪"; // Ready
            case ACTIVE:    return "🟢"; // Running
            case SUSPENDED: return "🟡"; // Paused
            case ERROR:     return "🔴"; // Error
            case SHUTDOWN:  return "⚫"; // Stopped
            default:        return "❓"; // Unknown
        }
    }
    
    public Color getStateColor(AgentState state) {
        switch (state) {
            case IDLE:      return Color.GRAY;
            case ACTIVE:    return Color.GREEN;
            case SUSPENDED: return Color.YELLOW;
            case ERROR:     return Color.RED;
            case SHUTDOWN:  return Color.BLACK;
            default:        return Color.WHITE;
        }
    }
}
```

## Integration Considerations

### With Task Management
- Only IDLE agents can accept new tasks
- ACTIVE state indicates task in progress
- Task completion triggers state transition

### With Monitoring Systems
- State changes trigger monitoring events
- State duration tracking for performance
- Alerts based on state patterns

### With UI Components
- Visual state indicators
- State-based action availability
- Real-time state updates

### With Recovery Systems
- ERROR state triggers recovery procedures
- State history for diagnosis
- Automated state restoration