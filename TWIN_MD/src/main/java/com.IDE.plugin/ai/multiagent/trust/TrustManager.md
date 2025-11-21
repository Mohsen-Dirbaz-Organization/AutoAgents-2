# TrustManager Class

## Overview
The `TrustManager` class handles trust scores and reputation management between agents in the AutoAgents multi-agent system. It provides a comprehensive framework for tracking, updating, and evaluating agent trustworthiness over time.

## Package
`com.IDE.plugin.ai.multiagent.trust`

## Description
A centralized manager that maintains trust scores for agents, tracks interaction outcomes, and provides trust-based decision support. Uses a dynamic scoring system with configurable thresholds and automatic trust adjustments.

## Key Features

### Trust Score Management
- Maintains per-agent trust scores (0.0 to 1.0 range)
- Default trust score of 0.5 for new agents
- Automatic score adjustment based on interactions
- Boundary enforcement to prevent invalid scores

### Interaction Tracking
- Records successful and failed interactions
- Calculates success rates and reliability metrics
- Tracks temporal patterns in agent behavior
- Maintains detailed interaction statistics

### Trust Evaluation
- Provides trust threshold checking (default: 0.6)
- Supports custom trust criteria evaluation
- Enables trust-based decision making
- Offers comprehensive trust metrics

## Core Methods

### Trust Score Operations

#### getTrustScore(String agentId)
```java
public double getTrustScore(String agentId)
```
Retrieves the current trust score for an agent.

**Parameters:**
- `agentId` - The identifier of the agent

**Returns:** Trust score between 0.0 and 1.0, or default value (0.5) if agent is unknown

#### setTrustScore(String agentId, double score)
```java
public void setTrustScore(String agentId, double score)
```
Sets an explicit trust score for an agent.

**Parameters:**
- `agentId` - The identifier of the agent
- `score` - Trust score between 0.0 and 1.0

**Throws:** `IllegalArgumentException` if score is outside valid range

#### updateTrust(String agentId, boolean successful)
```java
public void updateTrust(String agentId, boolean successful)
```
Updates trust score based on interaction outcome.

**Parameters:**
- `agentId` - The identifier of the agent
- `successful` - Whether the interaction was successful

**Behavior:**
- Successful interactions: +0.1 adjustment
- Failed interactions: -0.2 adjustment (penalizes failures more)
- Automatically updates interaction metrics

### Trust Evaluation

#### isTrusted(String agentId)
```java
public boolean isTrusted(String agentId)
```
Checks if an agent meets the trust threshold (≥ 0.6).

**Parameters:**
- `agentId` - The identifier of the agent

**Returns:** True if agent is trusted, false otherwise

#### getMetrics(String agentId)
```java
public TrustMetrics getMetrics(String agentId)
```
Retrieves comprehensive trust metrics for an agent.

**Parameters:**
- `agentId` - The identifier of the agent

**Returns:** TrustMetrics instance containing detailed statistics

### Management Operations

#### resetTrust(String agentId)
```java
public void resetTrust(String agentId)
```
Resets an agent's trust to default value and clears metrics.

#### clear()
```java
public void clear()
```
Clears all trust data for all agents.

## TrustMetrics Inner Class

### Overview
Tracks detailed interaction statistics for each agent.

### Key Metrics
- **Total Interactions**: Count of all recorded interactions
- **Successful Interactions**: Count of successful outcomes
- **Success Rate**: Percentage of successful interactions
- **Last Interaction Time**: Timestamp of most recent interaction

### Methods

#### recordInteraction(boolean successful)
```java
public void recordInteraction(boolean successful)
```
Records a new interaction outcome and updates statistics.

#### getSuccessRate()
```java
public double getSuccessRate()
```
Calculates the success rate (successful/total interactions).

## Configuration Constants

### Trust Score Boundaries
- `DEFAULT_TRUST = 0.5` - Initial trust score for new agents
- `MIN_TRUST = 0.0` - Minimum allowable trust score
- `MAX_TRUST = 1.0` - Maximum allowable trust score

### Trust Threshold
- Default trust threshold: 0.6 (configurable)

### Adjustment Values
- Success adjustment: +0.1
- Failure adjustment: -0.2 (asymmetric to penalize failures)

## Usage Examples

### Basic Trust Management
```java
TrustManager trustManager = new TrustManager();

// Record successful interaction
trustManager.updateTrust("agent-001", true);

// Check if agent is trusted
if (trustManager.isTrusted("agent-001")) {
    // Grant additional permissions
}

// Get current trust score
double score = trustManager.getTrustScore("agent-001");
```

### Trust Metrics Analysis
```java
TrustMetrics metrics = trustManager.getMetrics("agent-001");
double successRate = metrics.getSuccessRate();
int totalInteractions = metrics.getTotalInteractions();

System.out.println("Agent success rate: " + successRate);
System.out.println("Total interactions: " + totalInteractions);
```

### Explicit Trust Management
```java
// Set explicit trust score
trustManager.setTrustScore("critical-agent", 0.9);

// Reset problematic agent
trustManager.resetTrust("problem-agent");
```

## Integration Points

### Agent Communication
- Used by `MessageBus` for message routing decisions
- Integrated with `TrustedMessageBus` for secure communications
- Supports trust-based message filtering

### Task Allocation
- Influences task assignment in `TaskScheduler`
- Used by `AgentCoordinatorService` for delegation decisions
- Supports capability-based task routing

### Security Framework
- Integrates with authorization systems
- Supports access control decisions
- Enables graduated security policies

## Thread Safety

### Concurrent Access
- Uses `ConcurrentHashMap` for thread-safe operations
- Atomic updates for trust score modifications
- Thread-safe metric tracking and retrieval

### Performance Considerations
- O(1) trust score lookup and update
- Minimal synchronization overhead
- Efficient memory usage with concurrent collections

## Monitoring and Observability

### Trust Metrics Tracking
```java
// Monitor trust distribution
for (String agentId : getAllAgentIds()) {
    TrustMetrics metrics = trustManager.getMetrics(agentId);
    // Log or analyze metrics
}
```

### Trust Score Trends
- Track trust score changes over time
- Identify patterns in trust evolution
- Detect sudden trust degradation

## Error Handling

### Invalid Input Handling
- Validates trust score ranges
- Handles null agent IDs gracefully
- Provides safe defaults for unknown agents

### Boundary Conditions
- Enforces minimum/maximum trust scores
- Handles edge cases in metric calculations
- Maintains system stability

## Best Practices

### Trust Score Management
1. Use automatic trust updates based on interactions
2. Set explicit scores only for special cases
3. Regularly review trust thresholds
4. Monitor trust score distributions

### Performance Optimization
1. Batch trust updates when possible
2. Use trust checks efficiently in hot paths
3. Cache frequently accessed trust scores
4. Monitor memory usage of trust data

### Security Considerations
1. Validate trust scores before setting
2. Audit trust score changes
3. Implement trust score persistence
4. Protect against trust score manipulation

## Related Components
- **TrustLevel**: Enumeration for trust classifications
- **TrustScore**: Detailed trust score representation
- **TrustMetrics**: Extended trust metrics calculation
- **ReputationManager**: Advanced reputation tracking

## Future Enhancements

### Planned Features
- Weighted trust adjustments based on interaction types
- Temporal decay of trust scores
- Machine learning-based trust prediction
- Advanced anomaly detection in trust patterns

### Configuration Extensions
- Configurable trust thresholds per agent type
- Custom trust adjustment algorithms
- Integration with external trust systems
- Trust score persistence and recovery