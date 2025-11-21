# StationManager Class Documentation

## Overview
The `StationManager` class serves as the central coordination service for managing multiple stations within the AutoAgents multi-agent system. It provides comprehensive station lifecycle management, agent assignment coordination, performance monitoring, and integration with the broader agent coordination infrastructure.

## Package Location
```
com.IDE.plugin.ai.multiagent.station.StationManager
```

## Class Declaration
```java
@Service(Service.Level.PROJECT)
public final class StationManager
```

## Service Architecture
- **IntelliJ Service**: Project-level service with automatic lifecycle management
- **Final Class**: Cannot be extended, ensuring implementation integrity
- **Singleton Pattern**: One instance per project for centralized management

## Dependencies

### Core Services
- **`AgentCoordinatorService`**: Agent lifecycle and coordination
- **`TrustManager`**: Agent trust evaluation and management
- **`MemoryService`**: System memory and historical data

### IntelliJ Platform
- **`Project`**: IntelliJ project context
- **`Logger`**: Centralized logging infrastructure

### Concurrency
- **`ScheduledExecutorService`**: Background task scheduling
- **`ConcurrentHashMap`**: Thread-safe collections
- **`CopyOnWriteArrayList`**: Thread-safe listener management

## Core Attributes

### Station Management
- **`stations`**: Thread-safe map of active stations (`ConcurrentHashMap<String, Station>`)
- **`statusCache`**: Real-time station status cache (`ConcurrentHashMap<String, StationStatus>`)

### Service Integration
- **`project`**: IntelliJ project context
- **`coordinatorService`**: Agent coordination service
- **`trustManager`**: Trust evaluation service
- **`memoryService`**: Memory management service

### Background Processing
- **`scheduler`**: Scheduled executor for background tasks
- **`listeners`**: Thread-safe list of event listeners (`CopyOnWriteArrayList<StationListener>`)

### Configuration Constants
```java
private static final int MAX_STATIONS = 10;
private static final long STATUS_UPDATE_INTERVAL = 5000; // 5 seconds
private static final long IDLE_TIMEOUT = 300000; // 5 minutes
private static final double MIN_TRUST_THRESHOLD = 0.3;
```

## Constructor

### Service Constructor
```java
public StationManager(@NotNull Project project)
```
**Purpose**: Initializes station manager with project dependencies
**Behavior**:
- Obtains required service references
- Creates scheduled executor with 2 threads
- Initializes scheduled background tasks
**Dependencies Resolved**:
- `AgentCoordinatorService`
- `TrustManager`
- `MemoryService`

## Station Lifecycle Management

### `createStation(StationConfiguration configuration)`
**Purpose**: Creates new station with specified configuration
**Validation**:
- Checks maximum station limit (10 stations)
- Generates unique station ID
**Integration**:
- Records creation in memory service
- Notifies registered listeners
- Initializes status cache entry
**Returns**: New `Station` instance
**Exceptions**: `IllegalStateException` if maximum stations exceeded

### `removeStation(String stationId)`
**Purpose**: Removes station and handles agent reassignment
**Behavior**:
- Reassigns orphaned agents to compatible stations
- Cleans up status cache
- Notifies listeners of removal
**Agent Handling**: Automatically finds alternative stations for displaced agents

## Agent Assignment Management

### `assignAgentToStation(String agentId, String stationId)`
**Purpose**: Assigns agent to specified station with validation
**Validation Process**:
1. Verifies station existence
2. Validates agent existence
3. Checks agent trust score against minimum threshold (0.3)
4. Validates agent compatibility with station requirements

**Integration**:
- Records assignment in memory service
- Updates station status
- Notifies listeners
**Exceptions**:
- `IllegalArgumentException`: Station or agent not found
- `IllegalStateException`: Trust score too low

### `removeAgentFromStation(String agentId)`
**Purpose**: Removes agent from current station assignment
**Behavior**:
- Searches all stations for agent
- Removes from found station
- Updates station status
- Records removal in memory service

## Station Operations

### `activateStation(String stationId)`
**Purpose**: Activates station for task processing
**Process**:
1. Validates station existence
2. Activates the station
3. Starts all assigned idle agents
4. Updates station status
5. Notifies listeners

**Agent Coordination**: Automatically starts idle agents when station activates

### `deactivateStation(String stationId)`
**Purpose**: Deactivates station and stops processing
**Process**:
1. Validates station existence
2. Deactivates the station
3. Stops all active agents
4. Updates station status
5. Notifies listeners

**Agent Coordination**: Automatically stops active agents when station deactivates

## Status and Monitoring

### `getStationStatus(String stationId)`
**Purpose**: Retrieves current station status
**Returns**: `StationStatus` or `null` if station not found
**Data Source**: Real-time status cache

### `getActiveStations()`
**Purpose**: Returns list of all currently active stations
**Returns**: `List<Station>` filtered for active stations only
**Usage**: System health monitoring, load balancing

### `findBestStation(String taskType)`
**Purpose**: Intelligent station selection for task routing
**Algorithm**:
1. Filters active stations
2. Checks task type support
3. Calculates station scores based on:
   - Health score
   - Current load
   - Task type specialization (1.5x boost for primary types)
**Returns**: Optimal `Station` or `null` if none suitable

## Event Management

### `addListener(StationListener listener)`
**Purpose**: Registers station event listener
**Thread Safety**: Uses `CopyOnWriteArrayList` for safe concurrent access

### `removeListener(StationListener listener)`
**Purpose**: Unregisters station event listener
**Thread Safety**: Safe removal during concurrent access

### Event Types
```java
public enum StationEvent {
    CREATED, ACTIVATED, DEACTIVATED, REMOVED, AGENT_ASSIGNED, AGENT_REMOVED
}
```

### Listener Interface
```java
public interface StationListener {
    void onStationEvent(@NotNull StationEvent event, @NotNull Station station);
}
```

## Background Processing

### Scheduled Tasks Initialization
```java
private void initializeScheduledTasks()
```
**Tasks Configured**:
1. **Status Updates**: Every 5 seconds
2. **Idle Cleanup**: Every 5 minutes

### Status Update Process
```java
private void updateAllStationStatuses()
```
**Purpose**: Maintains real-time status cache
**Frequency**: 5-second intervals
**Metrics Updated**:
- Active status
- Agent count
- Tasks processed
- Current load
- Health score

### Idle Station Cleanup
```java
private void cleanupIdleStations()
```
**Purpose**: Removes stations idle for more than 5 minutes
**Criteria**: Inactive stations with no recent status updates
**Behavior**: Automatic removal with agent reassignment

## Performance Calculation

### Station Health Calculation
```java
private double calculateStationHealth(@NotNull Station station)
```
**Factors Considered**:
- **Agent Trust**: Average trust score of assigned agents
- **Error Rate**: Station task failure rate
- **Response Time**: Average processing time (10s baseline)
**Formula**: `baseHealth × avgTrust × (1 - errorRate) × responseTimeFactor`

### Station Scoring for Task Routing
```java
private double calculateStationScore(@NotNull Station station, @NotNull String taskType)
```
**Scoring Components**:
- **Base Score**: Station health score
- **Load Factor**: Penalty for high load `(1 - currentLoad)`
- **Specialization Bonus**: 1.5x multiplier for primary task types

## Memory Integration

### Event Recording
**Station Lifecycle Events**:
```java
memoryService.storeMemory(MemoryEntry.builder()
    .type(MemoryType.SYSTEM)
    .category("station_lifecycle")
    .content("Station created: " + configuration.getName())
    .metadata(Map.of("stationId", stationId, "configuration", configuration.toMap()))
    .build());
```

**Agent Assignment Events**:
```java
memoryService.storeMemory(MemoryEntry.builder()
    .type(MemoryType.COLLABORATION)
    .category("agent_assignment")
    .content("Agent " + agentId + " assigned to station " + stationId)
    .metadata(Map.of("agentId", agentId, "stationId", stationId, "trustScore", trustScore))
    .build());
```

## Trust Integration

### Agent Trust Validation
- **Minimum Threshold**: 0.3 trust score required for assignment
- **Continuous Monitoring**: Trust scores factored into health calculations
- **Assignment Blocking**: Low-trust agents cannot be assigned to stations

### Health Score Integration
- Agent trust scores averaged for station health
- Trust degradation affects station performance metrics
- Trust recovery improves overall station health

## Utility Methods

### Station ID Generation
```java
private String generateStationId(@NotNull String name)
```
**Algorithm**:
1. Converts name to lowercase
2. Replaces non-alphanumeric characters with underscores
3. Appends counter for uniqueness if needed

### Available Station Finding
```java
private Station findAvailableStation(@NotNull Agent agent)
```
**Criteria**:
- Station is active
- Can accept the agent (capacity and compatibility)
**Selection**: Chooses station with lowest agent count

## Error Handling and Resilience

### Service Recovery
- Graceful handling of service unavailability
- Defensive programming for null service references
- Exception containment in listener notifications

### Data Integrity
- Consistent state maintenance across operations
- Transaction-like behavior for complex operations
- Rollback capabilities for failed assignments

### Resource Management
```java
public void dispose()
```
**Cleanup Process**:
1. Shutdown scheduler gracefully (5-second timeout)
2. Force shutdown if necessary
3. Clear all collections
4. Handle interrupted threads appropriately

## Usage Patterns

### Basic Station Management
```java
// Create and configure station
StationConfiguration config = new StationConfiguration.Builder("ProcessingStation")
    .maxAgents(3)
    .addRequiredCapability(AgentCapability.DATA_PROCESSING)
    .build();

Station station = stationManager.createStation(config);

// Assign agents and activate
stationManager.assignAgentToStation("agent_001", station.getId());
stationManager.activateStation(station.getId());
```

### Task Routing
```java
// Find best station for task
Station bestStation = stationManager.findBestStation("data_analysis");
if (bestStation != null) {
    bestStation.queueTask(taskId);
}
```

### Monitoring Integration
```java
// Monitor station health
List<Station> activeStations = stationManager.getActiveStations();
for (Station station : activeStations) {
    StationStatus status = stationManager.getStationStatus(station.getId());
    if (status.getHealthScore() < 0.5) {
        // Handle unhealthy station
    }
}
```

## Best Practices

### Station Creation
- Design stations for specific task types
- Set appropriate agent capacity limits
- Consider trust requirements for sensitive operations

### Agent Management
- Monitor trust scores continuously
- Balance agent distribution across stations
- Reassign agents from unhealthy stations

### Performance Optimization
- Use station scoring for intelligent task routing
- Monitor health scores for proactive management
- Implement custom listeners for specific events

### Resource Management
- Monitor station count against limits
- Clean up unused stations regularly
- Handle agent reassignment gracefully

## Integration Requirements

### Service Dependencies
- **Agent Coordinator**: For agent lifecycle management
- **Trust Manager**: For security and reliability
- **Memory Service**: For historical tracking

### Event Coordination
- **Station Listeners**: For UI updates and monitoring
- **Agent Events**: For coordination with agent lifecycle
- **System Events**: For integration with broader system

This documentation provides comprehensive coverage of the StationManager class, emphasizing its central role in coordinating multi-agent system operations and its integration with the broader AutoAgents infrastructure.