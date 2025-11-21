# TrustMetrics Class

## Overview
The `TrustMetrics` class provides comprehensive metrics for evaluating trust levels between agents in the AutoAgents system. It encapsulates various performance indicators and behavioral measurements that contribute to overall trust assessment.

## Package
`com.IDE.plugin.ai.multiagent.trust`

## Description
An immutable data class that aggregates multiple trust-related metrics including success rates, response times, reliability scores, and capability assessments. Uses the Builder pattern for flexible construction and provides weighted trust score calculations.

## Key Features

### Comprehensive Metrics
- Success rate tracking and analysis
- Response time measurement and evaluation
- Task completion statistics
- Reliability score calculations
- Capability-specific performance scores

### Weighted Trust Calculation
- Multi-factor trust score computation
- Configurable weight distribution
- Balanced performance assessment
- Normalized scoring methodology

### Immutable Design
- Thread-safe metric representation
- Consistent data integrity
- Builder pattern for construction
- Value object semantics

## Core Properties

### Agent Identification
- **agentId**: Unique identifier for the agent
- **lastUpdated**: Timestamp of last metric update

### Performance Metrics
- **successRate**: Percentage of successful operations (0.0-1.0)
- **responseTime**: Average response time in milliseconds
- **totalTasks**: Total number of tasks attempted
- **successfulTasks**: Number of successfully completed tasks
- **reliability**: Overall reliability score (0.0-1.0)

### Capability Assessment
- **capabilityScores**: Map of capability-specific performance scores

## Constructor and Builder

### Private Constructor
```java
private TrustMetrics(Builder builder)
```
Creates TrustMetrics instance from builder with validation and default values.

### Builder Pattern
```java
public static Builder builder()
```
Creates a new builder instance for TrustMetrics construction.

**Example Usage:**
```java
TrustMetrics metrics = TrustMetrics.builder()
    .agentId("agent-001")
    .successRate(0.85)
    .responseTime(150.0)
    .totalTasks(100)
    .successfulTasks(85)
    .reliability(0.88)
    .capabilityScores(Map.of("coding", 0.9, "analysis", 0.8))
    .build();
```

## Key Methods

### Metric Accessors

#### Basic Metrics
```java
public String getAgentId()
public double getSuccessRate()
public double getResponseTime()
public int getTotalTasks()
public int getSuccessfulTasks()
public double getReliability()
public Instant getLastUpdated()
public Map<String, Double> getCapabilityScores()
```

### Trust Score Calculation

#### getOverallTrustScore()
```java
public double getOverallTrustScore()
```
Calculates weighted overall trust score using multiple factors.

**Formula:**
```
Trust Score = (successRate × 0.4) + (reliability × 0.3) + ((1.0 / (1.0 + responseTime)) × 0.3)
```

**Weighting:**
- Success Rate: 40% - Primary performance indicator
- Reliability: 30% - Consistency measure
- Response Time: 30% - Efficiency metric (inverse relationship)

**Returns:** Composite trust score between 0.0 and 1.0

## Builder Class

### Overview
Provides fluent API for constructing TrustMetrics instances with validation.

### Builder Methods

#### Agent Information
```java
public Builder agentId(String agentId)
```
Sets the agent identifier.

#### Performance Metrics
```java
public Builder successRate(double successRate)
public Builder responseTime(double responseTime)
public Builder totalTasks(int totalTasks)
public Builder successfulTasks(int successfulTasks)
public Builder reliability(double reliability)
```

#### Temporal Information
```java
public Builder lastUpdated(Instant lastUpdated)
```
Sets the last update timestamp (defaults to current time).

#### Capability Assessment
```java
public Builder capabilityScores(Map<String, Double> capabilityScores)
```
Sets capability-specific performance scores.

#### Construction
```java
public TrustMetrics build()
```
Creates immutable TrustMetrics instance with validation.

## Usage Examples

### Basic Metric Creation
```java
TrustMetrics metrics = TrustMetrics.builder()
    .agentId("agent-001")
    .successRate(0.92)
    .responseTime(120.5)
    .totalTasks(50)
    .successfulTasks(46)
    .reliability(0.91)
    .build();

double trustScore = metrics.getOverallTrustScore();
```

### Capability-Specific Metrics
```java
Map<String, Double> capabilities = Map.of(
    "data-analysis", 0.95,
    "code-generation", 0.88,
    "problem-solving", 0.82
);

TrustMetrics metrics = TrustMetrics.builder()
    .agentId("specialist-agent")
    .successRate(0.89)
    .responseTime(200.0)
    .reliability(0.87)
    .capabilityScores(capabilities)
    .build();
```

### Temporal Tracking
```java
TrustMetrics metrics = TrustMetrics.builder()
    .agentId("agent-002")
    .successRate(0.78)
    .reliability(0.80)
    .lastUpdated(Instant.now().minusSeconds(3600))
    .build();
```

## Integration Points

### Trust Management System
- Used by `TrustManager` for detailed agent assessment
- Integrated with `ReputationManager` for reputation calculations
- Referenced in trust-based decision making

### Performance Monitoring
- Supports `PerformanceMetrics` integration
- Enables trend analysis and reporting
- Facilitates performance optimization

### Agent Coordination
- Used in `AgentCoordinatorService` for task allocation
- Supports capability-based routing decisions
- Enables performance-aware coordination

## Calculation Methodology

### Success Rate Calculation
```
Success Rate = Successful Tasks / Total Tasks
```
- Handles division by zero gracefully
- Provides meaningful default for new agents
- Updates incrementally with new data

### Reliability Assessment
- Based on consistency of performance over time
- Considers task completion rates
- Factors in response time variability

### Response Time Impact
- Uses inverse relationship (lower time = higher score)
- Normalizes using formula: 1.0 / (1.0 + responseTime)
- Prevents negative impact from very long response times

## Data Validation

### Builder Validation
- Ensures required fields are set
- Validates metric ranges and constraints
- Provides sensible defaults for optional fields

### Range Validation
- Success rates: 0.0 to 1.0
- Reliability scores: 0.0 to 1.0
- Response times: Non-negative values
- Task counts: Non-negative integers

## Performance Characteristics

### Memory Efficiency
- Immutable design reduces memory overhead
- Efficient storage of capability maps
- Minimal object creation after construction

### Computational Efficiency
- O(1) metric access operations
- Efficient trust score calculation
- Lazy evaluation where appropriate

## Thread Safety

### Immutable Design
- All fields are final after construction
- No mutable state in constructed instances
- Safe for concurrent access across threads

### Builder Thread Safety
- Builders are not thread-safe (intended for single-thread use)
- Each builder instance should be used by one thread
- Constructed TrustMetrics instances are fully thread-safe

## Error Handling

### Construction Errors
- Builder validates all input parameters
- Provides meaningful error messages
- Fails fast for invalid configurations

### Runtime Safety
- Null-safe accessor methods
- Defensive copying of mutable collections
- Graceful handling of edge cases

## Monitoring and Analytics

### Metric Tracking
```java
// Track trust score evolution
double currentScore = metrics.getOverallTrustScore();
logger.info("Agent {} trust score: {}", metrics.getAgentId(), currentScore);

// Analyze capability distribution
metrics.getCapabilityScores().forEach((capability, score) -> 
    logger.debug("Capability {}: {}", capability, score));
```

### Performance Analysis
- Success rate trend analysis
- Response time distribution tracking
- Reliability pattern identification

## Best Practices

### Metric Construction
1. Use builder pattern for all construction
2. Set all relevant metrics for complete assessment
3. Update timestamps for temporal tracking
4. Validate input ranges before building

### Trust Score Interpretation
1. Consider all contributing factors
2. Account for metric age and relevance
3. Compare scores within similar contexts
4. Use trends rather than absolute values

### Capability Management
1. Define consistent capability categories
2. Use normalized scoring across capabilities
3. Update capability scores regularly
4. Document capability definitions

## Related Components
- **TrustManager**: Primary consumer of TrustMetrics
- **TrustScore**: Simplified trust representation
- **ReputationManager**: Extended reputation tracking
- **PerformanceMetrics**: Performance data source

## Future Enhancements

### Planned Features
- Machine learning-based trust prediction
- Temporal decay modeling for metrics
- Advanced statistical analysis methods
- Integration with external assessment systems

### Configuration Extensions
- Configurable weight distribution for trust calculation
- Custom metric aggregation strategies
- Domain-specific trust models
- Real-time metric streaming capabilities