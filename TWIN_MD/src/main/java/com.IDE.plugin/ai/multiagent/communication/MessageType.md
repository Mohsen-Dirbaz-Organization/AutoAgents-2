# MessageType Enum Documentation

## Overview
`MessageType` is a comprehensive enumeration that defines all possible message types in the multi-agent system. It serves as a central registry of communication intents, enabling type-safe message routing, subscription filtering, and handler dispatching. The enum categorizes messages into logical groups including system operations, architectural decisions, monitoring activities, code modifications, trust management, and collaboration requests.

## Multi-Level Architecture

### System Level
- **Role**: Message classification and routing foundation
- **Purpose**: Standardizes communication protocols across agents
- **Benefits**: Type safety, clear intent, subscription filtering
- **Integration**: Used by Message, MessageBus, and all agents

### Component Level
- **Type**: Enumeration
- **Package**: `com.IDE.plugin.ai.multiagent.communication`
- **Categories**:
  - System messages
  - Architecture messages
  - Observer messages
  - Code Editor messages
  - Trust and security messages
  - Collaboration messages
  - Error and exception messages

## Message Categories and Types

### System Messages
Fundamental system operations and control messages.

- **SYSTEM_BROADCAST**: System-wide announcements
- **SYSTEM_COMMAND**: Direct system control commands
- **EMERGENCY_STOP**: Critical shutdown signals
- **STATUS_UPDATE**: Agent status notifications
- **HEARTBEAT**: Liveness check messages

### Architecture Messages
Design and architectural decision-related communications.

- **DESIGN_REQUEST**: Request for design proposal
- **DESIGN_PROPOSAL**: Proposed design solution
- **ARCHITECTURE_REVIEW**: Request for architecture evaluation
- **PATTERN_SUGGESTION**: Request for pattern recommendations
- **PATTERN_RECOMMENDATION**: Recommended design patterns
- **REVIEW_RESULT**: Architecture review outcomes
- **SYSTEM_ANALYSIS**: System-wide analysis request

### Observer Messages
Monitoring, metrics, and analysis-related messages.

- **MONITOR_REQUEST**: Start monitoring specific target
- **MONITORING_STARTED**: Confirmation of monitoring initiation
- **ANALYSIS_REQUEST**: Request for data analysis
- **ANALYSIS_RESULT**: Analysis findings
- **ANALYSIS_COMPLETE**: Analysis completion notification
- **ANOMALY_REPORT**: Detected anomaly notification
- **METRICS_QUERY**: Request for specific metrics
- **METRICS_RESPONSE**: Metrics data response
- **PERFORMANCE_ANALYSIS**: Performance evaluation request
- **CRITICAL_ALERT**: High-priority system alert
- **CRITICAL_FINDINGS**: Critical analysis discoveries

### Code Editor Messages
Code modification and analysis communications.

- **CODE_EDIT_REQUEST**: Request for code modification
- **EDIT_COMPLETE**: Code edit completion notification
- **REFACTORING_REQUEST**: Code refactoring request
- **REFACTORING_COMPLETE**: Refactoring completion status
- **OPTIMIZATION_REQUEST**: Code optimization request
- **OPTIMIZATION_COMPLETE**: Optimization completion status
- **CODE_ANALYSIS_REQUEST**: Static code analysis request
- **AUTOFIX_REQUEST**: Automatic fix request
- **AUTOFIX_APPLIED**: Autofix completion notification
- **CODE_REVIEW_REQUEST**: Code review initiation
- **REVIEW_COMPLETE**: Code review completion
- **CODE_CHANGED**: Code change notification
- **CRITICAL_CODE_ISSUES**: Critical code problem alert
- **MANUAL_FIX_REQUIRED**: Manual intervention needed

### Trust and Security Messages
Trust management and security-related communications.

- **TRUST_UPDATE**: Trust score modification
- **TRUST_VIOLATION**: Trust breach notification
- **SECURITY_ALERT**: Security issue detection
- **AUTHENTICATION_REQUEST**: Authentication initiation
- **AUTHENTICATION_RESPONSE**: Authentication result

### Collaboration Messages
Multi-agent collaboration and approval workflows.

- **APPROVAL_REQUEST**: Request for action approval
- **APPROVAL_RESPONSE**: Approval decision
- **COLLABORATION_REQUEST**: Multi-agent collaboration initiation
- **COLLABORATION_RESPONSE**: Collaboration acceptance/rejection

### Error and Exception Messages
Error handling and validation communications.

- **ERROR**: General error notification
- **EXCEPTION**: Exception occurrence notification
- **VALIDATION_ERROR**: Validation failure notification

## Usage Patterns and Integration Points

### Message Type Selection
```java
// Select appropriate type based on intent
Message statusMsg = new Message(
    MessageType.STATUS_UPDATE,  // Clear intent
    "agent-1",
    null,
    Map.of("status", "active", "cpu", 45.2)
);

Message criticalMsg = new Message(
    MessageType.CRITICAL_ALERT,  // High priority
    "monitor",
    null,
    Map.of("issue", "memory leak", "severity", "HIGH")
);
```

### Subscription Filtering
```java
// Subscribe to specific message types
messageBus.subscribe("analyzer-agent",
    MessageType.ANALYSIS_REQUEST,
    MessageType.CODE_ANALYSIS_REQUEST,
    MessageType.PERFORMANCE_ANALYSIS
);

// Filter in handler
public void handleMessage(Message message) {
    if (message.getType() == MessageType.ANALYSIS_REQUEST) {
        processAnalysisRequest(message);
    }
}
```

### Type-Based Routing
```java
public class MessageRouter {
    public void route(Message message) {
        switch (message.getType()) {
            // System messages
            case EMERGENCY_STOP:
                broadcastToAll(message);
                break;
                
            // Architecture messages
            case DESIGN_REQUEST:
            case ARCHITECTURE_REVIEW:
                routeToArchitect(message);
                break;
                
            // Observer messages
            case ANOMALY_REPORT:
            case CRITICAL_ALERT:
                routeToMonitor(message);
                break;
                
            // Code messages
            case CODE_EDIT_REQUEST:
            case REFACTORING_REQUEST:
                routeToCodeEditor(message);
                break;
        }
    }
}
```

### Trust Level Mapping
```java
public class TrustRequirements {
    private static final Map<MessageType, TrustLevel> REQUIREMENTS = Map.of(
        MessageType.SYSTEM_COMMAND, TrustLevel.FULL,
        MessageType.CODE_EDIT_REQUEST, TrustLevel.HIGH,
        MessageType.ANALYSIS_REQUEST, TrustLevel.MEDIUM,
        MessageType.STATUS_UPDATE, TrustLevel.LOW
    );
    
    public TrustLevel getRequired(MessageType type) {
        return REQUIREMENTS.getOrDefault(type, TrustLevel.MEDIUM);
    }
}
```

## Best Practices and Conventions

### Type Selection Guidelines

1. **Clear Intent**: Choose types that clearly express message purpose
2. **Appropriate Granularity**: Use specific types over generic ones
3. **Category Alignment**: Select from the appropriate category
4. **Consistency**: Use same types for similar operations

### Message Type Patterns

#### Request-Response Pairs
Many types form natural request-response pairs:
- DESIGN_REQUEST → DESIGN_PROPOSAL
- ANALYSIS_REQUEST → ANALYSIS_RESULT
- AUTHENTICATION_REQUEST → AUTHENTICATION_RESPONSE
- APPROVAL_REQUEST → APPROVAL_RESPONSE

#### Notification Types
Some types are purely informational:
- STATUS_UPDATE
- CODE_CHANGED
- MONITORING_STARTED
- EDIT_COMPLETE

#### Command Types
Direct action messages:
- SYSTEM_COMMAND
- EMERGENCY_STOP
- CODE_EDIT_REQUEST
- REFACTORING_REQUEST

### Integration Patterns

#### With Agent Roles
```java
public Set<MessageType> getDefaultSubscriptions(AgentRole role) {
    switch (role) {
        case ARCHITECT:
            return Set.of(
                MessageType.DESIGN_REQUEST,
                MessageType.ARCHITECTURE_REVIEW,
                MessageType.SYSTEM_ANALYSIS
            );
        case CODE_EDITOR:
            return Set.of(
                MessageType.CODE_EDIT_REQUEST,
                MessageType.REFACTORING_REQUEST,
                MessageType.OPTIMIZATION_REQUEST
            );
        case OBSERVER:
            return Set.of(
                MessageType.MONITOR_REQUEST,
                MessageType.ANALYSIS_REQUEST,
                MessageType.ANOMALY_REPORT
            );
        default:
            return Set.of(MessageType.SYSTEM_BROADCAST);
    }
}
```

#### Priority Mapping
```java
public Priority getPriority(MessageType type) {
    switch (type) {
        case EMERGENCY_STOP:
        case CRITICAL_ALERT:
        case CRITICAL_CODE_ISSUES:
            return Priority.CRITICAL;
            
        case TRUST_VIOLATION:
        case SECURITY_ALERT:
            return Priority.HIGH;
            
        case ANALYSIS_REQUEST:
        case CODE_REVIEW_REQUEST:
            return Priority.MEDIUM;
            
        case STATUS_UPDATE:
        case HEARTBEAT:
            return Priority.LOW;
            
        default:
            return Priority.NORMAL;
    }
}
```

## Extension Considerations

### Adding New Types
When adding new message types:
1. Place in appropriate category
2. Follow naming conventions
3. Document the purpose
4. Update related handlers
5. Consider request-response pairs

### Deprecation Strategy
For removing types:
1. Mark as deprecated in comments
2. Log warnings when used
3. Provide migration path
4. Remove after grace period

### Version Compatibility
- Maintain backward compatibility
- Use message headers for versioning
- Handle unknown types gracefully
- Document breaking changes

## Performance Implications

1. **Enum Comparison**: Very fast, use == operator
2. **Switch Statements**: Optimized by JVM
3. **Set Operations**: Efficient for subscription checks
4. **Memory Usage**: Minimal, enums are singletons