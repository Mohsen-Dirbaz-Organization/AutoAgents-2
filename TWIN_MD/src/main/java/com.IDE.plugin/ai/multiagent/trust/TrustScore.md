# TrustScore Class

## Overview
The `TrustScore` class represents a comprehensive trust score with detailed metrics in the AutoAgents trust management system. It combines trust level classifications with numeric scores and contextual information for complete trust assessment.

## Package
`com.IDE.plugin.ai.multiagent.trust`

## Description
A data container that encapsulates trust information including the trust level, numeric score, timestamp, and optional reasoning. Provides a complete snapshot of an agent's trust status at a specific point in time.

## Key Features

### Comprehensive Trust Representation
- Trust level enumeration for categorical classification
- Numeric score for precise trust measurement
- Timestamp for temporal tracking
- Optional reason for trust score justification

### Immutable Design
- Thread-safe trust score representation
- Consistent data integrity after creation
- Value object semantics
- No state modification after construction

### Flexible Construction
- Multiple constructor overloads
- Optional reason parameter
- Automatic timestamp generation
- Validation of trust components

## Properties

### Core Trust Data
- **level**: TrustLevel enumeration value
- **score**: Numeric trust score (typically 0.0-1.0)
- **timestamp**: Creation timestamp in milliseconds
- **reason**: Optional explanation for the trust score

## Constructors

### Basic Constructor
```java
public TrustScore(TrustLevel level, double score)
```
Creates a trust score with level and numeric value.

**Parameters:**
- `level` - The trust level classification
- `score` - The numeric trust score

### Extended Constructor
```java
public TrustScore(TrustLevel level, double score, String reason)
```
Creates a trust score with additional reasoning.

**Parameters:**
- `level` - The trust level classification
- `score` - The numeric trust score
- `reason` - Optional explanation for the score

**Behavior:**
- Automatically sets timestamp to current time
- Stores reason for audit and debugging purposes

## Accessor Methods

### getLevel()
```java
public TrustLevel getLevel()
```
Returns the trust level classification.

**Returns:** The TrustLevel enumeration value

### getScore()
```java
public double getScore()
```
Returns the numeric trust score.

**Returns:** The numeric trust score value

### getTimestamp()
```java
public long getTimestamp()
```
Returns the creation timestamp.

**Returns:** Timestamp in milliseconds since epoch

### getReason()
```java
public String getReason()
```
Returns the optional reason for the trust score.

**Returns:** Reason string, or null if not provided

## String Representation

### toString()
```java
@Override
public String toString()
```
Provides formatted string representation of the trust score.

**Format:**
```
TrustScore{level=HIGH, score=0.85, reason='Consistent performance'}
```

**Returns:** Formatted string containing all trust score components

## Usage Examples

### Basic Trust Score Creation
```java
TrustScore basicScore = new TrustScore(TrustLevel.HIGH, 0.85);
System.out.println("Trust Level: " + basicScore.getLevel());
System.out.println("Numeric Score: " + basicScore.getScore());
```

### Trust Score with Reasoning
```java
TrustScore detailedScore = new TrustScore(
    TrustLevel.MEDIUM, 
    0.65, 
    "Agent completed 65% of tasks successfully"
);

System.out.println("Reason: " + detailedScore.getReason());
System.out.println("Created: " + new Date(detailedScore.getTimestamp()));
```

### Trust Score Comparison
```java
TrustScore score1 = new TrustScore(TrustLevel.HIGH, 0.8);
TrustScore score2 = new TrustScore(TrustLevel.MEDIUM, 0.6);

if (score1.getScore() > score2.getScore()) {
    System.out.println("Score1 has higher trust");
}
```

## Integration Points

### Trust Management System
- Used by `TrustManager` for detailed trust representation
- Integrated with `TrustMetrics` for comprehensive assessment
- Referenced in trust decision workflows

### Agent Evaluation
- Provides snapshot of agent trustworthiness
- Supports trust history tracking
- Enables trust trend analysis

### Security Framework
- Used in access control decisions
- Supports audit trail generation
- Enables trust-based authorization

## Validation and Constraints

### Trust Level Consistency
- Trust level should align with numeric score
- Score ranges should match level classifications
- Validation can be implemented in consuming systems

### Score Range Validation
- Typically expects scores between 0.0 and 1.0
- No enforced constraints in the class itself
- Validation responsibility lies with creators

## Thread Safety

### Immutable Design
- All fields are final after construction
- No mutable state modification
- Safe for concurrent access

### Concurrent Usage
- Multiple threads can safely read trust scores
- No synchronization required for access
- Thread-safe for sharing across agent systems

## Temporal Considerations

### Timestamp Management
- Automatic timestamp generation at creation
- Millisecond precision for temporal ordering
- Supports trust evolution tracking

### Age-Based Analysis
```java
long currentTime = System.currentTimeMillis();
long scoreAge = currentTime - trustScore.getTimestamp();
boolean isRecent = scoreAge < TimeUnit.HOURS.toMillis(1);
```

## Audit and Debugging

### Reason Tracking
- Optional reason field for trust score justification
- Supports debugging of trust calculations
- Enables audit trail creation

### Diagnostic Information
```java
TrustScore diagnosticScore = new TrustScore(
    TrustLevel.LOW,
    0.3,
    "Failed 3 consecutive security validations"
);

// Full diagnostic output
System.out.println(diagnosticScore.toString());
```

## Serialization Considerations

### Data Persistence
- Simple structure suitable for serialization
- All fields are primitives or standard types
- Compatible with JSON serialization frameworks

### Example JSON Representation
```json
{
  "level": "HIGH",
  "score": 0.85,
  "timestamp": 1639123456789,
  "reason": "Consistent high performance"
}
```

## Performance Characteristics

### Memory Efficiency
- Minimal memory footprint
- No complex object hierarchies
- Efficient storage of trust information

### Computational Efficiency
- O(1) accessor operations
- No complex calculations required
- Minimal CPU overhead

## Best Practices

### Trust Score Creation
1. Choose appropriate trust level for numeric score
2. Provide meaningful reasons for audit purposes
3. Create new instances rather than modifying existing ones
4. Use consistent scoring methodologies

### Trust Score Usage
1. Consider timestamp when evaluating trust
2. Use reasons for debugging and analysis
3. Implement validation in consuming systems
4. Store trust scores for historical analysis

### Integration Guidelines
1. Align trust levels with system policies
2. Document trust score interpretation
3. Implement proper error handling
4. Consider trust score lifecycle management

## Error Handling

### Construction Validation
- Accepts any TrustLevel enumeration value
- No validation of score ranges in constructor
- Null handling for optional reason parameter

### Runtime Safety
- No runtime exceptions from accessor methods
- Safe handling of null reason values
- Consistent behavior across all operations

## Monitoring and Metrics

### Trust Score Tracking
```java
// Log trust score creation
logger.info("Created trust score: {}", trustScore.toString());

// Track trust score distribution
Map<TrustLevel, Integer> distribution = new HashMap<>();
distribution.merge(trustScore.getLevel(), 1, Integer::sum);
```

### Historical Analysis
- Track trust score changes over time
- Analyze trust score patterns
- Identify trust degradation or improvement

## Related Components
- **TrustLevel**: Enumeration for trust classifications
- **TrustManager**: Primary trust score consumer
- **TrustMetrics**: Detailed trust measurement system
- **ReputationManager**: Advanced reputation tracking

## Future Enhancements

### Planned Features
- Trust score validation methods
- Confidence intervals for trust scores
- Trust score aggregation utilities
- Enhanced temporal tracking capabilities

### Configuration Extensions
- Configurable trust score ranges
- Custom trust level mappings
- Integration with external trust systems
- Automated trust score validation

## Usage Patterns

### Trust Decision Making
```java
public boolean allowOperation(TrustScore trustScore) {
    return trustScore.getLevel().getValue() >= TrustLevel.MEDIUM.getValue() &&
           trustScore.getScore() >= 0.6;
}
```

### Trust History Tracking
```java
List<TrustScore> trustHistory = new ArrayList<>();
trustHistory.add(new TrustScore(TrustLevel.LOW, 0.3, "Initial assessment"));
trustHistory.add(new TrustScore(TrustLevel.MEDIUM, 0.6, "Improved performance"));
```

### Trust Score Comparison
```java
public TrustScore getHigherTrust(TrustScore score1, TrustScore score2) {
    return score1.getScore() > score2.getScore() ? score1 : score2;
}
```