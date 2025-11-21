# MechanicalSignalValidator

## Overview
Validates signal integrity using cryptographic verification in the mechanical signaling system. This component ensures the authenticity and integrity of signals through comprehensive validation mechanisms including checksums, HMAC signatures, and various security checks.

## Class Description
`MechanicalSignalValidator` implements a robust validation framework that verifies both the integrity and authenticity of mechanical signals. It uses cryptographic techniques, caching for performance, and detailed metrics tracking.

## Logic Distribution
- **50%** Integrity Verification
- **30%** Authenticity Validation
- **20%** Performance Monitoring

## Key Components

### Core Properties
- **validationCache**: Caches recent validation results
- **agentSecrets**: Stores agent secret keys for HMAC verification
- **totalValidations**: Counter for all validation attempts
- **successfulValidations**: Counter for successful validations
- **failedValidations**: Counter for failed validations
- **performanceMonitor**: Tracks validation performance metrics

### Configuration Constants
- **HMAC_ALGORITHM**: "HmacSHA256" for signature generation
- **CACHE_EXPIRY_MS**: 60000ms (1 minute) cache retention

## Main Methods

### validateSignal(MechanicalSignal signal)
Primary validation method that:
1. Checks cache for recent validation results
2. Verifies signal integrity (50% of logic)
3. Validates authenticity (30% of logic)
4. Monitors performance (20% of logic)
5. Creates and caches validation result
6. Updates success/failure counters

Returns `SignalValidationResult` containing:
- Overall validation status
- Integrity check result
- Authenticity check result
- Validation time
- Detailed validation report

### Agent Management Methods

#### registerAgent(String agentId, byte[] secret)
Registers an agent with their secret key for HMAC verification.

#### unregisterAgent(String agentId)
Removes an agent from the validation system.

## Validation Components

### Integrity Verification (50%)
Comprehensive integrity checks including:

#### Checksum Verification
- Calculates SHA-256 hash of payload
- Compares with signal's checksum
- Uses Base64 encoding for string representation

#### Sequence Number Verification
- Ensures sequence numbers are valid (>0)
- Prevents replay attacks
- Tracks sequence progression per agent

#### Timestamp Verification
- Validates timestamps within 5-minute window
- Prevents old signal replay
- Handles clock synchronization issues

#### Payload Structure Verification
- Checks payload exists and is non-null
- Validates size constraints (max 1MB)
- Ensures data structure integrity

### Authenticity Validation (30%)
Security-focused validation including:

#### HMAC Signature Verification
- Uses registered agent secrets
- Calculates HMAC-SHA256 signature
- Includes all critical fields in signature:
  - Signal ID
  - Sender ID
  - Payload
  - Timestamp

#### Agent Permission Verification
- Validates agent is registered
- Checks agent has required permissions
- Ensures sender authorization

## Performance Features

### Caching System
- Stores recent validation results
- 1-minute cache expiry
- Automatic cache cleanup at 10,000 entries
- Thread-safe implementation

### Performance Monitoring
Inner class `PerformanceMonitor` tracks:
- Total validation time
- Validation count
- Cache hit rate
- Average validation time

## Security Implementation

### Cryptographic Methods

#### calculateChecksum(byte[] payload)
- Uses SHA-256 message digest
- Returns Base64-encoded hash
- Provides data integrity verification

#### calculateHMAC(MechanicalSignal signal, byte[] secret)
- Implements HMAC-SHA256
- Includes multiple signal fields
- Returns Base64-encoded signature

### Security Checks
1. Unknown agent rejection
2. Signature mismatch detection
3. Timestamp window enforcement
4. Sequence number validation
5. Payload size limits

## Statistics and Monitoring

### ValidationStatistics
Provides comprehensive metrics:
- Total validations performed
- Successful validation count
- Failed validation count
- Average validation time (nanoseconds)
- Cache hit rate percentage

### getStatistics()
Returns current validation statistics for monitoring and analysis.

## Usage Example
```java
// Initialize validator
MechanicalSignalValidator validator = new MechanicalSignalValidator();

// Register agents with secrets
byte[] agentSecret = "secret-key".getBytes();
validator.registerAgent("agent-1", agentSecret);
validator.registerAgent("agent-2", agentSecret);

// Validate a signal
MechanicalSignal signal = new MechanicalSignal(...);
SignalValidationResult result = validator.validateSignal(signal);

// Check validation results
if (result.isValid()) {
    System.out.println("Signal is valid");
    System.out.println("Integrity: " + result.isIntegrityValid());
    System.out.println("Authenticity: " + result.isAuthenticityValid());
} else {
    System.out.println("Validation failed: " + result.getReport());
}

// Get statistics
ValidationStatistics stats = validator.getStatistics();
System.out.println("Total validations: " + stats.totalValidations);
System.out.println("Success rate: " + 
    (stats.successfulValidations * 100.0 / stats.totalValidations) + "%");
System.out.println("Cache hit rate: " + (stats.cacheHitRate * 100) + "%");
```

## Error Handling
- Graceful exception handling in all validation methods
- Detailed error reporting in validation results
- Fallback to failed validation on any error
- Comprehensive error messages for debugging

## Integration Points
- Used by `SignalValidationCoordinator`
- Works with agent registration systems
- Integrates with cryptographic services
- Provides metrics for monitoring systems

## Best Practices
1. Register all agents before validation
2. Use strong secret keys (minimum 256 bits)
3. Monitor cache hit rates for performance
4. Regularly review validation statistics
5. Implement proper time synchronization
6. Handle validation failures gracefully

## Performance Optimization
- Cache recent validations
- Efficient cryptographic operations
- Concurrent data structures
- Minimal object creation
- Fast checksum calculations

## Future Enhancements
- Public key cryptography support
- Certificate-based validation
- Dynamic threshold adjustment
- Extended validation rules
- Blockchain integration
- Multi-signature support