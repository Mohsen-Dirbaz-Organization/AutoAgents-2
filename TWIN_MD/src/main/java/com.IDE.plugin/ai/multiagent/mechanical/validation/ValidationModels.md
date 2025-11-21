# ValidationModels

## Overview
Supporting model classes for the validation island of the mechanical signaling system. This file contains all data structures used for validation results, analysis outcomes, statistics, and reporting.

## Model Categories

### Validation Result Models
Data structures for validation outcomes and reports.

### Analysis Result Models
Structures for pattern analysis, optimization, and anomaly detection results.

### Pattern Analysis Models
Models for representing detected patterns and their characteristics.

### Optimization Models
Structures for optimization opportunities and results.

### Anomaly Models
Models for anomaly detection and classification.

### Combined Result Models
Structures combining validation and analysis results.

### Statistics Models
Models for tracking system metrics and performance.

### Report Models
Structures for comprehensive reporting.

## Validation Result Models

### SignalValidationResult
Represents the outcome of signal validation.

**Properties:**
- `signalId`: Unique signal identifier
- `valid`: Overall validation status
- `integrityValid`: Integrity check result
- `authenticityValid`: Authenticity check result
- `validationTime`: Time taken for validation (nanoseconds)
- `report`: Detailed validation report
- `timestamp`: Result creation time

**Key Features:**
- Immutable design with final fields
- Automatic timestamp on creation
- Comprehensive validation details

## Analysis Result Models

### SignalAnalysisResult
Contains comprehensive signal analysis outcomes.

**Properties:**
- `signalId`: Signal identifier
- `patterns`: Pattern analysis results
- `optimization`: Optimization analysis results
- `anomalies`: Anomaly detection results
- `recommendations`: List of actionable recommendations
- `errorMessage`: Error details if analysis failed

**Constructor Overloads:**
1. Success constructor with all analysis components
2. Error constructor with error message only

**Helper Methods:**
- `hasPatterns()`: Checks if patterns were detected
- `getPatternCount()`: Returns number of patterns found
- `hasOptimizations()`: Checks for optimization opportunities
- `getOptimizationPotential()`: Returns potential savings percentage
- `hasAnomalies()`: Checks if anomalies were detected
- `getAnomalyCount()`: Returns number of anomalies

## Pattern Analysis Models

### PatternAnalysisResult
Encapsulates pattern detection outcomes.

**Properties:**
- `patterns`: List of detected patterns

**Methods:**
- `hasPatterns()`: Checks if any patterns exist
- `hasRepeatingPatterns()`: Checks for repeating pattern types
- `getPatternCount()`: Returns pattern count

### Pattern
Individual pattern representation.

**Properties:**
- `type`: Pattern type (enum)
- `description`: Human-readable description
- `confidence`: Detection confidence (0.0-1.0)

### PatternType Enum
Pattern classification:
- `REPEATING`: Recurring patterns
- `TEMPORAL`: Time-based patterns
- `SPATIAL`: Location-based patterns
- `BEHAVIORAL`: Behavior patterns

## Optimization Models

### OptimizationResult
Results of optimization analysis.

**Properties:**
- `canOptimize`: Whether optimization is possible
- `compressionRatio`: Potential compression percentage
- `canOptimizeRouting`: Routing optimization possibility
- `canBatch`: Batching possibility

**Methods:**
- `canOptimize()`: Overall optimization check
- `getCompressionRatio()`: Compression potential

### OptimizationOpportunity
Specific optimization recommendation.

**Properties:**
- `agentId`: Target agent identifier
- `description`: Opportunity description
- `type`: Optimization type

### OptimizationType Enum
Optimization categories:
- `COMPRESSION`: Data compression
- `BATCHING`: Signal batching
- `ROUTING`: Path optimization
- `CACHING`: Cache utilization

## Anomaly Models

### AnomalyResult
Anomaly detection outcomes.

**Properties:**
- `anomalies`: List of detected anomalies

**Methods:**
- `hasAnomalies()`: Checks for any anomalies
- `getAnomalyCount()`: Returns anomaly count

### Anomaly
Individual anomaly representation.

**Properties:**
- `type`: Anomaly type
- `description`: Anomaly description
- `severity`: Severity score (0.0-1.0)

### AnomalyType Enum
Anomaly classifications:
- `PAYLOAD_SIZE`: Unusual data size
- `FREQUENCY`: Abnormal transmission rate
- `PATTERN`: Pattern deviation
- `TIMING`: Timing anomaly
- `SEQUENCE`: Sequence irregularity

## Combined Result Models

### ValidationAnalysisResult
Combines validation and analysis results.

**Properties:**
- `signalId`: Signal identifier
- `validationResult`: Complete validation result
- `analysisResult`: Complete analysis result
- `report`: Coordination report
- `timestamp`: Result creation time

**Methods:**
- `isValid()`: Overall validation status
- `isExpired()`: Checks if result is older than 5 minutes

**Key Features:**
- Unified result representation
- Automatic expiry checking
- Complete result aggregation

## Statistics Models

### ValidationStatistics
Validation system metrics.

**Properties:**
- `totalValidations`: Total validation count
- `successfulValidations`: Success count
- `failedValidations`: Failure count
- `averageValidationTime`: Average time per validation
- `cacheHitRate`: Cache effectiveness ratio

### CoordinationStatistics
Coordination system metrics.

**Properties:**
- `totalProcessed`: Total signals processed
- `successfulValidations`: Successful validation count
- `queueSize`: Current queue depth
- `cacheSize`: Current cache entries
- `reportCount`: Total reports generated

## Report Models

### AnalysisReport
System-wide analysis report.

**Properties:**
- `globalPatterns`: Map of agent patterns
- `opportunities`: List of optimization opportunities

### ValidationReport
Individual validation report.

**Properties:**
- `signalId`: Signal identifier
- `valid`: Validation status
- `timestamp`: Report time
- `details`: Detailed information

## Usage Examples

### Creating Validation Results
```java
// Successful validation
SignalValidationResult validResult = new SignalValidationResult(
    "signal-123",
    true,  // valid
    true,  // integrity valid
    true,  // authenticity valid
    150000, // 150μs validation time
    "All checks passed"
);

// Failed validation
SignalValidationResult invalidResult = new SignalValidationResult(
    "signal-456",
    false, // invalid
    true,  // integrity valid
    false, // authenticity invalid
    200000, // 200μs validation time
    "Authentication failed: Invalid signature"
);
```

### Working with Analysis Results
```java
// Create pattern analysis
List<Pattern> patterns = Arrays.asList(
    new Pattern(PatternType.REPEATING, "High frequency transmission", 0.95),
    new Pattern(PatternType.TEMPORAL, "Peak hour activity", 0.87)
);
PatternAnalysisResult patternResult = new PatternAnalysisResult(patterns);

// Create optimization result
OptimizationResult optimization = new OptimizationResult(
    true,  // can optimize
    30,    // 30% compression possible
    false, // no routing optimization
    true   // can batch
);

// Create anomaly result
List<Anomaly> anomalies = Arrays.asList(
    new Anomaly(AnomalyType.PAYLOAD_SIZE, "Unusually large payload", 0.8)
);
AnomalyResult anomalyResult = new AnomalyResult(anomalies);

// Combine into analysis result
SignalAnalysisResult analysisResult = new SignalAnalysisResult(
    "signal-123",
    patternResult,
    optimization,
    anomalyResult,
    Arrays.asList("Consider batching signals", "Enable compression")
);
```

### Creating Combined Results
```java
ValidationAnalysisResult combined = new ValidationAnalysisResult(
    "signal-123",
    validResult,
    analysisResult,
    coordinationReport
);

// Check expiry
if (combined.isExpired()) {
    // Refresh result
}
```

## Design Patterns

### Immutability
- Most models use final fields
- No setters for core properties
- Thread-safe by design

### Builder Pattern Potential
- Constructor-based initialization
- Could be enhanced with builders

### Value Object Pattern
- Models represent values
- Equality based on content
- No identity beyond data

## Best Practices

1. **Immutable Construction**: Create complete objects at construction
2. **Null Safety**: Use null checks or Optional where appropriate
3. **Type Safety**: Use enums for fixed categories
4. **Defensive Copying**: Return copies of mutable collections
5. **Clear Naming**: Self-documenting property names

## Future Enhancements
- Builder pattern implementation
- JSON serialization support
- Protobuf definitions
- Validation constraints
- Extended metadata support
- Event sourcing compatibility