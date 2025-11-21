# Threshold Settings Implementation Plan

## 1. Current State Analysis

After examining the codebase, I've identified several components that use thresholds for decision-making:

### 1.1 Trust and Authorization System
- **ThresholdSignature.java**: Uses threshold cryptography with hardcoded parameters:
  - `threshold` (constructor parameter)
  - `KEY_SIZE = 2048`
  - `PRIME_CERTAINTY = 100`
  - `SESSION_TIMEOUT_MINUTES = 5`

- **ReputationManager.java**: Uses trust thresholds as static constants:
  - `TRUSTED_THRESHOLD = 0.8`
  - `NEUTRAL_THRESHOLD = 0.5`
  - `SUSPICIOUS_THRESHOLD = 0.3`
  - `UNTRUSTED_THRESHOLD = 0.1`
  - `DECAY_RATE = 0.01`
  - `RECOVERY_RATE = 0.005`
  - `HISTORY_RETENTION_DAYS = 30`

### 1.2 Collision Handling
- **EnhancedCollisionHandler.java**: Uses thresholds as instance variables:
  - `minSeverityThreshold = 1`
  - `minTrustThreshold = 0.5`
  - `monitoringInterval = 5`
  - `historyRetentionDays = 30`

### 1.3 Memory Management
- **SemanticMemory.java**: Uses thresholds as static constants:
  - `DEFAULT_CAPACITY = 50000`
  - `ACTIVATION_DECAY_RATE = 0.1`
  - `MIN_ACTIVATION_THRESHOLD = 0.01`
  - Hardcoded similarity threshold (0.8) for concept consolidation
  - Hardcoded relationship strength threshold (0.1) for pruning

- **EpisodicMemory.java**: Uses threshold as static constant:
  - `CONSOLIDATION_THRESHOLD_MS = TimeUnit.HOURS.toMillis(24)`

- **WorkingMemory.java**: Uses threshold as static constant:
  - `EVICTION_THRESHOLD_MS = TimeUnit.MINUTES.toMillis(5)`

### 1.4 Signal Processing
- **MechanicalSignalQueue.java**: Uses thresholds as static constants:
  - `MAX_QUEUE_SIZE = 10000`
  - `STARVATION_THRESHOLD_MS = 30000`
  - Hardcoded fragmentation threshold (0.3) for queue rebalancing

### 1.5 Consensus Mechanisms
- **ByzantineFaultTolerance.java**: Uses thresholds as static constants:
  - `PHASE_TIMEOUT_MS = 5000`
  - `MAX_RETRIES = 3`
  - `BYZANTINE_THRESHOLD = 0.67`

### 1.6 Agent Coordination
- **AgentCoordinatorService.java**: Uses thresholds as instance variables:
  - `collaborationThreshold = 0.7`
  - `stallTimeout = 60`
  - `maxRecoveryAttempts = 3`
  - `maxErrorRate = 0.3`
  - `recoveryDelay = 10`

## 2. Issues with Current Implementation

1. **Inconsistent Implementation**: Thresholds are implemented inconsistently across the codebase:
   - Some are static constants (cannot be changed at runtime)
   - Some are instance variables (can be changed but not persisted)
   - Some are hardcoded values within methods

2. **No Centralized Management**: Thresholds are scattered throughout the codebase with no centralized management system.

3. **No Persistence**: Most threshold values are not persisted between application sessions.

4. **No UI Configuration**: Users cannot configure thresholds through a UI.

5. **No Documentation**: There's limited documentation about what each threshold does and how it affects system behavior.

## 3. Implementation Plan

### 3.1 Create ThresholdSettings Class

Create a new `ThresholdSettings` class that extends the existing `AutoAgentsSettings` class:

```java
public class ThresholdSettings {
    // Trust and Authorization Thresholds
    private double trustedThreshold = 0.8;
    private double neutralThreshold = 0.5;
    private double suspiciousThreshold = 0.3;
    private double untrustedThreshold = 0.1;
    private double trustDecayRate = 0.01;
    private double trustRecoveryRate = 0.005;
    private int trustHistoryRetentionDays = 30;

    // Collision Handling Thresholds
    private int minSeverityThreshold = 1;
    private double minTrustThreshold = 0.5;

    // Memory Management Thresholds
    private int semanticMemoryCapacity = 50000;
    private double activationDecayRate = 0.1;
    private double minActivationThreshold = 0.01;
    private double conceptSimilarityThreshold = 0.8;
    private double relationshipStrengthThreshold = 0.1;
    private long episodicConsolidationThresholdHours = 24;
    private long workingMemoryEvictionThresholdMinutes = 5;

    // Signal Processing Thresholds
    private int maxQueueSize = 10000;
    private long starvationThresholdMs = 30000;
    private double queueFragmentationThreshold = 0.3;

    // Consensus Thresholds
    private long phaseTimeoutMs = 5000;
    private int maxConsensusRetries = 3;
    private double byzantineThreshold = 0.67;

    // Agent Coordination Thresholds
    private double collaborationThreshold = 0.7;
    private int stallTimeoutMinutes = 60;
    private int maxRecoveryAttempts = 3;
    private double maxErrorRate = 0.3;
    private int recoveryDelaySeconds = 10;

    // Getters and setters for all fields
    // ...
}
```

### 3.2 Integrate with AutoAgentsSettings

Add the ThresholdSettings as a field in AutoAgentsSettings:

```java
public class AutoAgentsSettings implements PersistentStateComponent<AutoAgentsSettings> {
    // Existing fields...

    // Threshold Settings
    private ThresholdSettings thresholdSettings = new ThresholdSettings();

    // Getter and setter for thresholdSettings
    public ThresholdSettings getThresholdSettings() {
        return thresholdSettings;
    }

    public void setThresholdSettings(ThresholdSettings thresholdSettings) {
        this.thresholdSettings = thresholdSettings;
    }

    // Existing methods...
}
```

### 3.3 Create Configuration UI

Create a new UI panel for configuring thresholds in the settings dialog:

1. Create `ThresholdSettingsPanel.java` that extends `JPanel`
2. Organize thresholds into categories with appropriate UI controls
3. Add tooltips and documentation for each threshold
4. Implement validation for threshold values

### 3.4 Refactor Components to Use Settings

Refactor each component to use the centralized threshold settings:

1. **ReputationManager.java**:
   ```java
   private final ThresholdSettings thresholdSettings;

   public ReputationManager(ReputationConfig config) {
       this.thresholdSettings = AutoAgentsSettings.getInstance().getThresholdSettings();
       // Use thresholdSettings.getTrustedThreshold() instead of TRUSTED_THRESHOLD
       // ...
   }
   ```

2. **EnhancedCollisionHandler.java**:
   ```java
   private final ThresholdSettings thresholdSettings;

   public EnhancedCollisionHandler(TrustManager trustManager, MemoryManager memoryManager,
                                  AdministrativeHistoryManager adminHistoryManager,
                                  EventBus eventBus, CollisionHandlerConfig config) {
       this.thresholdSettings = AutoAgentsSettings.getInstance().getThresholdSettings();
       // Use thresholdSettings.getMinSeverityThreshold() instead of minSeverityThreshold
       // ...
   }
   ```

3. Apply similar refactoring to all components that use thresholds.

### 3.5 Add Dynamic Threshold Adjustment

Implement a mechanism for dynamically adjusting thresholds based on system performance:

1. Create a `ThresholdOptimizer` class that monitors system metrics
2. Implement algorithms to suggest optimal threshold values
3. Allow automatic or manual application of suggested values

### 3.6 Add Threshold Profiles

Implement threshold profiles for different use cases:

1. Create predefined profiles (e.g., "High Security", "Balanced", "High Performance")
2. Allow users to save and load custom profiles
3. Implement profile switching functionality

## 4. Implementation Phases

### Phase 1: Foundation
1. Create ThresholdSettings class
2. Integrate with AutoAgentsSettings
3. Implement basic UI for configuration

### Phase 2: Refactoring
1. Refactor ReputationManager to use centralized settings
2. Refactor EnhancedCollisionHandler to use centralized settings
3. Refactor remaining components to use centralized settings

### Phase 3: Advanced Features
1. Implement threshold profiles
2. Implement dynamic threshold adjustment
3. Add comprehensive documentation and tooltips

## 5. Benefits

1. **Consistency**: All thresholds will be managed consistently
2. **Centralization**: Single point of configuration for all thresholds
3. **Persistence**: Threshold settings will be persisted between sessions
4. **Configurability**: Users can configure thresholds through a UI
5. **Documentation**: Comprehensive documentation for each threshold
6. **Optimization**: Ability to optimize thresholds based on system performance

## 6. Risks and Mitigations

1. **Risk**: Changing thresholds could destabilize the system
   **Mitigation**: Implement validation rules and provide default safe values

2. **Risk**: Performance impact from frequent settings access
   **Mitigation**: Cache settings values where appropriate

3. **Risk**: Complex refactoring could introduce bugs
   **Mitigation**: Implement comprehensive tests for threshold-dependent behavior

## 7. Conclusion

Implementing a centralized threshold settings system will significantly improve the maintainability, configurability, and robustness of the AutoAgents system. By following this plan, we can transform the current ad-hoc threshold implementation into a well-structured, documented, and user-configurable system.
