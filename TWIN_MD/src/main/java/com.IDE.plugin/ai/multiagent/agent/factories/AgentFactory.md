# AgentFactory Class Documentation

## Overview
`AgentFactory` is a factory class responsible for creating different types of agents in the multi-agent system. It provides a centralized mechanism for agent instantiation, supporting both enum-based and reflection-based creation patterns. The factory ensures consistent agent initialization and dependency injection across the system.

## Multi-Level Architecture

### System Level
- **Role**: Central agent creation and instantiation point
- **Purpose**: Standardizes agent creation process and dependency management
- **Integration**: Works with ServiceManager for dependency injection
- **Pattern**: Factory pattern implementation for agent instantiation

### Component Level
- **Type**: Factory class with static and dynamic creation methods
- **Package**: `com.IDE.plugin.ai.multiagent.agent.factories`
- **Dependencies**:
  - ServiceManager for dependency injection
  - Various agent implementations (CodeReviewAgent, DocumentationAgent, etc.)
  - Java reflection API for dynamic instantiation

## Core Features and Functionality

### Enum-Based Agent Creation
- **Type Safety**: Strongly typed agent creation using AgentType enum
- **Supported Types**:
  - CODE_REVIEW: Code review and analysis agents
  - DOCUMENTATION: Documentation generation agents
  - REFACTORING: Code refactoring agents
  - TEST_GENERATOR: Test case generation agents
- **Switch-Based Routing**: Direct instantiation based on type

### Reflection-Based Agent Creation
- **Dynamic Loading**: Create agents by fully qualified class name
- **Runtime Flexibility**: Add new agent types without factory modification
- **Constructor Pattern**: Expects agents to have ServiceManager constructor
- **Error Handling**: Wraps reflection exceptions in RuntimeException

### Dependency Injection
- **ServiceManager Integration**: Passes ServiceManager to all agents
- **Consistent Initialization**: Ensures all agents receive required services
- **Centralized Management**: Single point for service distribution

## Component Props and Data Structures

### Core Components
```java
private final ServiceManager serviceManager;  // Service dependency provider
```

### AgentType Enum
```java
public enum AgentType {
    CODE_REVIEW("Code Review Agent"),        // Code analysis and review
    DOCUMENTATION("Documentation Agent"),     // Documentation generation
    REFACTORING("Refactoring Agent"),        // Code refactoring
    TEST_GENERATOR("Test Generator Agent");  // Test generation
    
    private final String displayName;        // Human-readable name
}
```

### Method Signatures
```java
// Enum-based creation
public Agent createAgent(AgentType type)

// Reflection-based creation
public Agent createAgent(String className)
```

## Usage Patterns and Integration Points

### Basic Agent Creation
```java
// Create factory with service manager
ServiceManager serviceManager = new ServiceManager();
AgentFactory factory = new AgentFactory(serviceManager);

// Create agent by type
Agent codeReviewer = factory.createAgent(AgentType.CODE_REVIEW);
Agent documentor = factory.createAgent(AgentType.DOCUMENTATION);

// Create agent by class name
Agent customAgent = factory.createAgent("com.example.CustomAgent");
```

### Integration with Agent Management
```java
public class AgentManager {
    private final AgentFactory factory;
    private final Map<String, Agent> agents = new HashMap<>();
    
    public void deployAgent(AgentType type) {
        Agent agent = factory.createAgent(type);
        agent.initialize();
        agents.put(agent.getId(), agent);
    }
    
    public void deployCustomAgent(String className) {
        try {
            Agent agent = factory.createAgent(className);
            agent.initialize();
            agents.put(agent.getId(), agent);
        } catch (RuntimeException e) {
            log.error("Failed to deploy agent: " + className, e);
        }
    }
}
```

### Dynamic Agent Loading Pattern
```java
// Configuration-based agent loading
List<String> agentClasses = config.getAgentClasses();
for (String className : agentClasses) {
    try {
        Agent agent = factory.createAgent(className);
        registerAgent(agent);
    } catch (Exception e) {
        log.warn("Skipping agent: " + className, e);
    }
}
```

## Agent Type Specifications

### CODE_REVIEW Agent
- **Purpose**: Analyzes code quality, style, and potential issues
- **Implementation**: CodeReviewAgent class
- **Capabilities**: Static analysis, style checking, bug detection

### DOCUMENTATION Agent
- **Purpose**: Generates and maintains code documentation
- **Implementation**: DocumentationAgent class
- **Capabilities**: JavaDoc generation, API documentation, README updates

### REFACTORING Agent
- **Purpose**: Performs automated code refactoring
- **Implementation**: RefactoringAgent class
- **Capabilities**: Method extraction, renaming, code cleanup

### TEST_GENERATOR Agent
- **Purpose**: Creates unit and integration tests
- **Implementation**: TestGeneratorAgent class
- **Capabilities**: Test case generation, assertion creation, mock setup

## Best Practices and Considerations

### Factory Usage Guidelines
1. **Centralized Creation**: Always use factory for agent instantiation
2. **Service Manager**: Ensure ServiceManager is properly initialized
3. **Error Handling**: Handle RuntimeException for dynamic creation
4. **Type Safety**: Prefer enum-based creation when possible

### Extension Guidelines
1. **Adding New Types**: 
   - Add to AgentType enum
   - Update switch statement
   - Implement corresponding agent class
2. **Custom Agents**:
   - Implement Agent interface
   - Provide ServiceManager constructor
   - Use reflection-based creation

### Error Handling Strategies
1. **Invalid Agent Type**: Throws IllegalArgumentException
2. **Class Not Found**: Wrapped in RuntimeException
3. **Constructor Issues**: Detailed error in RuntimeException
4. **Missing Dependencies**: Check ServiceManager initialization

### Performance Considerations
1. **Reflection Overhead**: Cache Class objects for repeated creation
2. **Agent Pooling**: Consider reusing agents instead of creating new
3. **Lazy Initialization**: Delay agent initialization until needed
4. **Memory Management**: Properly dispose of unused agents

### Design Patterns Applied
1. **Factory Pattern**: Core pattern for object creation
2. **Dependency Injection**: ServiceManager injection
3. **Strategy Pattern**: Different agent types as strategies
4. **Enum Pattern**: Type-safe agent type definition

### Common Integration Patterns

#### With Dependency Injection Frameworks
```java
@Component
public class SpringAgentFactory {
    @Autowired
    private ServiceManager serviceManager;
    
    private final AgentFactory delegateFactory;
    
    @PostConstruct
    public void init() {
        delegateFactory = new AgentFactory(serviceManager);
    }
    
    public Agent createAgent(AgentType type) {
        return delegateFactory.createAgent(type);
    }
}
```

#### With Configuration Systems
```java
public class ConfigurableAgentFactory {
    private final AgentFactory factory;
    private final Configuration config;
    
    public List<Agent> createConfiguredAgents() {
        return config.getAgentConfigs().stream()
            .map(conf -> {
                AgentType type = AgentType.valueOf(conf.getType());
                Agent agent = factory.createAgent(type);
                configureAgent(agent, conf);
                return agent;
            })
            .collect(Collectors.toList());
    }
}
```

### Testing Considerations
1. **Mock ServiceManager**: Use mock for unit testing
2. **Test All Types**: Ensure all enum types create successfully
3. **Error Cases**: Test invalid class names and types
4. **Integration Tests**: Verify agents work with real ServiceManager

### Future Extensibility
1. **Plugin Architecture**: Support for external agent plugins
2. **Version Management**: Handle different agent versions
3. **Configuration**: Agent-specific configuration injection
4. **Monitoring**: Factory-level metrics and monitoring