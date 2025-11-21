# AutoAgentsSettings.md

## Class Overview
**File:** `src/main/java/com/IDE/plugin/settings/AutoAgentsSettings.java`  
**Package:** `com.IDE.plugin.settings`  
**Type:** IntelliJ IDEA Plugin Configuration Component

## Purpose
The `AutoAgentsSettings` class manages the persistent configuration settings for the AutoAgents plugin. It implements IntelliJ IDEA's `PersistentStateComponent` interface to handle serialization and deserialization of configuration data.

## Architecture

### Class Declaration
```java
@State(
    name = "com.IDE.plugin.settings.AutoAgentsSettings",
    storages = @Storage("AutoAgentsSettings.xml")
)
public class AutoAgentsSettings implements PersistentStateComponent<AutoAgentsSettings>
```

### Component Categories

#### 1. API Configuration
- **API Key**: Secure storage for authentication
- **API Endpoint**: Target service URL (default: OpenAI)
- **Model Selection**: AI model choice (GPT-4, GPT-3.5, Claude, Custom)
- **Token Limits**: Maximum tokens per request (default: 2048)
- **Temperature**: Response creativity control (default: 0.7)

#### 2. User Interface Settings
- **Notifications**: Toggle for system notifications
- **Auto-formatting**: Automatic code formatting
- **Cursor Insertion**: Insert generated code at cursor position
- **Prompt Templates**: Customizable generation templates

#### 3. Advanced Configuration
- **Timeout Settings**: Request timeout (default: 30 seconds)
- **Proxy Configuration**: Network proxy support
- **Logging**: Debug and monitoring controls
- **Log Levels**: Configurable logging verbosity

#### 4. Code Generation Preferences
- **Comment Inclusion**: Add explanatory comments
- **Import Management**: Automatic import generation
- **Code Style**: Formatting standards
- **Test Generation**: Automatic test creation
- **Testing Framework**: JUnit or custom framework selection

## Key Features

### Configuration Persistence
- **XML Serialization**: Settings stored in `AutoAgentsSettings.xml`
- **Application-Level Storage**: Shared across all projects
- **Atomic Updates**: Consistent state management

### Default Values
```java
// API defaults
private String apiEndpoint = "https://api.openai.com/v1/chat/completions";
private String selectedModel = "GPT-4";
private int maxTokens = 2048;
private double temperature = 0.7;

// UI defaults
private boolean showNotifications = true;
private boolean autoFormat = true;
private boolean insertAtCursor = true;
```

### Singleton Access Pattern
```java
public static AutoAgentsSettings getInstance() {
    return ApplicationManager.getApplication().getService(AutoAgentsSettings.class);
}
```

## Configuration Categories

### 1. API Configuration
| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `apiKey` | String | "" | Authentication key |
| `apiEndpoint` | String | OpenAI URL | Service endpoint |
| `selectedModel` | String | "GPT-4" | AI model selection |
| `maxTokens` | int | 2048 | Token limit |
| `temperature` | double | 0.7 | Response creativity |

### 2. UI Preferences
| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `showNotifications` | boolean | true | System notifications |
| `autoFormat` | boolean | true | Auto-format code |
| `insertAtCursor` | boolean | true | Cursor insertion |
| `defaultPromptTemplate` | String | Template | Generation template |

### 3. Network & Performance
| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `timeout` | int | 30000ms | Request timeout |
| `useProxy` | boolean | false | Proxy usage |
| `proxyHost` | String | "" | Proxy hostname |
| `proxyPort` | int | 8080 | Proxy port |

### 4. Development Tools
| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `includeComments` | boolean | true | Add comments |
| `includeImports` | boolean | true | Auto-imports |
| `codeStyle` | String | "Standard" | Code formatting |
| `generateTests` | boolean | false | Test generation |
| `testFramework` | String | "JUnit" | Testing framework |

## State Management

### Persistence Interface Implementation
```java
@Nullable
@Override
public AutoAgentsSettings getState() {
    return this;
}

@Override
public void loadState(@NotNull AutoAgentsSettings state) {
    XmlSerializerUtil.copyBean(state, this);
}
```

## Usage Patterns

### Accessing Settings
```java
// Get the settings instance
AutoAgentsSettings settings = AutoAgentsSettings.getInstance();

// Read configuration
String apiKey = settings.getApiKey();
boolean notifications = settings.isShowNotifications();

// Update configuration
settings.setApiKey("new-api-key");
settings.setShowNotifications(false);
```

### Integration Points
- **IDE Settings Dialog**: Configuration UI binding
- **Service Components**: Settings injection
- **Plugin Initialization**: Default value loading
- **User Preferences**: Persistent customization

## Security Considerations

### Sensitive Data Handling
- **API Key Storage**: Encrypted at rest (IDE handles encryption)
- **Proxy Credentials**: Secure storage mechanisms
- **Configuration Validation**: Input sanitization

### Access Control
- **Application-Level Scope**: Settings shared across projects
- **User-Specific**: Individual user configurations
- **Permission Boundaries**: IDE security model compliance

## Configuration Validation

### Input Validation
- **URL Validation**: Endpoint format checking
- **Range Validation**: Numeric parameter bounds
- **Enum Validation**: Selection option constraints

### Default Fallbacks
- **Missing Values**: Automatic default assignment
- **Invalid Configurations**: Graceful degradation
- **Version Migration**: Settings schema updates

## Extension Points

### Custom Model Support
- **Model Registry**: Extensible model definitions
- **Provider Integration**: Multiple AI service support
- **Dynamic Configuration**: Runtime model discovery

### Template System
- **Custom Templates**: User-defined generation patterns
- **Variable Substitution**: Dynamic template processing
- **Template Sharing**: Export/import capabilities

## Performance Considerations

### Lazy Loading
- **On-Demand Access**: Settings loaded when needed
- **Memory Efficiency**: Minimal resource usage
- **Caching Strategy**: IntelliJ platform caching

### Update Optimization
- **Batch Updates**: Multiple setting changes
- **Change Detection**: Dirty flag tracking
- **Persistence Timing**: Optimal save points

## Integration with IntelliJ Platform

### Service Registration
- Component registered in plugin descriptor
- Automatic lifecycle management
- Platform service injection

### Settings UI Integration
- Configuration panels
- Validation feedback
- Real-time preview

## Error Handling

### Configuration Errors
- **Invalid Values**: Default fallback
- **Missing Files**: Auto-regeneration
- **Corruption Recovery**: Backup restoration

### Network Failures
- **Timeout Handling**: Graceful degradation
- **Retry Logic**: Configurable retry attempts
- **Offline Mode**: Limited functionality

## Future Enhancements

### Planned Features
- **Profile Management**: Multiple configuration profiles
- **Team Settings**: Shared team configurations
- **Advanced Security**: Enhanced encryption options
- **Cloud Sync**: Settings synchronization across devices

### Extension APIs
- **Plugin Extensions**: Third-party configuration
- **Custom Providers**: Additional AI service support
- **Integration Hooks**: External system connectivity

## Dependencies

### IntelliJ Platform
- `PersistentStateComponent`: Configuration persistence
- `ApplicationManager`: Service access
- `XmlSerializerUtil`: Serialization utilities

### Plugin Components
- Service layer integration
- UI component binding
- Security subsystem

## Troubleshooting

### Common Issues
1. **Settings Not Persisting**: Check file permissions
2. **Default Values Not Loading**: Verify initialization
3. **Validation Errors**: Check input constraints
4. **Performance Issues**: Review caching strategy

### Diagnostic Tools
- Settings file inspection
- Service registry verification
- Configuration validation logging
- Platform diagnostic tools

This class serves as the central configuration hub for the AutoAgents plugin, ensuring consistent and persistent user preferences across all plugin functionality.