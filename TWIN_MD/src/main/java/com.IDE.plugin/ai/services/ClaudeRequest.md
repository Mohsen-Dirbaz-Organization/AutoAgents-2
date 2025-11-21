# ClaudeRequest Documentation

## Overview
The `ClaudeRequest` class represents a request to the Claude API. It encapsulates all necessary data for communicating with Claude, including message content, model configuration, conversation history, and tool definitions.

## Class Information
- **Package**: `com.IDE.plugin.ai.services`
- **Type**: Data Transfer Object (DTO)
- **Dependencies**: Java Collections, ConversationMessage

## Purpose
The ClaudeRequest serves as the primary data structure for:
- Packaging request data for Claude API communication
- Managing conversation context and history
- Configuring model parameters and behavior
- Supporting tool integration and function calling
- Handling security headers and authentication

## Properties

### Core Identification
- **id**: `String` - Unique identifier for the request
- **taskId**: `String` - Associated task identifier for tracking
- **timestamp**: `long` - Request creation timestamp

### Model Configuration
- **model**: `String` - Claude model identifier (default: "claude-3-opus-20240229")
- **maxTokens**: `int` - Maximum tokens in response (default: 4000)
- **temperature**: `double` - Response creativity level (default: 0.7)

### Content & Prompting
- **content**: `String` - Main request content/prompt
- **systemPrompt**: `String` - System-level instructions
- **conversationHistory**: `List<ConversationMessage>` - Previous conversation context

### Advanced Features
- **tools**: `List<Map<String, Object>>` - Available tool definitions
- **headers**: `Map<String, String>` - Custom HTTP headers

## Key Features

### 1. **Flexible Model Configuration**
```java
ClaudeRequest request = new ClaudeRequest();
request.setModel("claude-3-opus-20240229");
request.setMaxTokens(8000);
request.setTemperature(0.3); // More deterministic responses
```

### 2. **Conversation Context Management**
```java
List<ConversationMessage> history = Arrays.asList(
    new ConversationMessage("user", "What is dependency injection?"),
    new ConversationMessage("assistant", "Dependency injection is a design pattern..."),
    new ConversationMessage("user", "Can you show an example?")
);
request.setConversationHistory(history);
```

### 3. **Tool Integration Support**
```java
Map<String, Object> codeGenerationTool = Map.of(
    "name", "generate_code",
    "description", "Generate code based on requirements",
    "parameters", Map.of(
        "type", "object",
        "properties", Map.of(
            "language", Map.of("type", "string"),
            "requirements", Map.of("type", "string")
        )
    )
);
request.addTool(codeGenerationTool);
```

### 4. **Security Header Management**
```java
request.addSecurityHeader("signature-hash-value");
// Automatically adds X-Security-Signature header
```

## Default Values

### Model Defaults
- **Model**: `claude-3-opus-20240229` (Latest Opus model)
- **Max Tokens**: `4000` (Balanced for most use cases)
- **Temperature**: `0.7` (Good balance of creativity and consistency)

### Initialization Defaults
- **Timestamp**: Current system time in milliseconds
- **Conversation History**: Empty ArrayList
- **Tools**: Empty ArrayList
- **Headers**: Empty HashMap

## Usage Examples

### Basic Request
```java
ClaudeRequest request = new ClaudeRequest();
request.setId("req-123");
request.setTaskId("task-456");
request.setContent("Explain the SOLID principles in software development");
```

### Advanced Request with System Prompt
```java
ClaudeRequest request = new ClaudeRequest();
request.setContent("Review this Java class for potential improvements");
request.setSystemPrompt("You are a senior Java developer with expertise in clean code and design patterns. Provide constructive feedback focusing on maintainability and performance.");
request.setTemperature(0.5); // More consistent responses for code review
```

### Request with Conversation History
```java
ClaudeRequest request = new ClaudeRequest();
request.setContent("Now implement the factory pattern we discussed");

// Add previous conversation context
List<ConversationMessage> history = new ArrayList<>();
history.add(new ConversationMessage("user", "What design patterns help with object creation?"));
history.add(new ConversationMessage("assistant", "Several patterns help with object creation, including Factory, Builder, and Singleton..."));
request.setConversationHistory(history);
```

### Request with Tools
```java
ClaudeRequest request = new ClaudeRequest();
request.setContent("Generate a REST API for user management");

// Add code generation tool
Map<String, Object> tool = Map.of(
    "name", "generate_java_code",
    "description", "Generate Java code with specified framework",
    "parameters", Map.of(
        "type", "object",
        "properties", Map.of(
            "framework", Map.of(
                "type", "string",
                "enum", Arrays.asList("spring", "jakarta", "plain")
            ),
            "include_tests", Map.of("type", "boolean")
        ),
        "required", Arrays.asList("framework")
    )
);
request.addTool(tool);
```

### Request with Custom Headers
```java
ClaudeRequest request = new ClaudeRequest();
request.setContent("Analyze this codebase structure");

// Add custom headers
Map<String, String> customHeaders = Map.of(
    "X-Request-Priority", "high",
    "X-Agent-Type", "architect",
    "X-Analysis-Depth", "comprehensive"
);
request.setHeaders(customHeaders);
```

## Model Configuration Guidelines

### Temperature Settings
- **0.0 - 0.3**: Highly deterministic (code generation, factual queries)
- **0.4 - 0.7**: Balanced creativity (general assistance, explanations)
- **0.8 - 1.0**: Highly creative (brainstorming, creative writing)

### Token Limits
- **1000-2000**: Short responses (quick answers, simple code)
- **2000-4000**: Medium responses (explanations, moderate code)
- **4000-8000**: Long responses (comprehensive analysis, large code)
- **8000+**: Extended responses (documentation, complex implementations)

### Model Selection
- **claude-3-opus**: Most capable model (complex reasoning, long context)
- **claude-3-sonnet**: Balanced performance (good for most tasks)
- **claude-3-haiku**: Fast responses (simple tasks, quick queries)

## Conversation History Management

### Best Practices
```java
// Maintain reasonable history length
List<ConversationMessage> history = getRecentHistory(10); // Last 10 messages
request.setConversationHistory(history);

// Include relevant context only
List<ConversationMessage> relevantHistory = filterRelevantMessages(
    fullHistory, 
    request.getContent()
);
request.setConversationHistory(relevantHistory);
```

### Context Optimization
- Keep history focused and relevant
- Limit history length to prevent token waste
- Include system messages for context
- Remove obsolete or redundant messages

## Tool Definition Structure

### Standard Tool Format
```java
Map<String, Object> tool = Map.of(
    "name", "tool_function_name",
    "description", "Clear description of tool functionality",
    "parameters", Map.of(
        "type", "object",
        "properties", Map.of(
            "param1", Map.of(
                "type", "string",
                "description", "Parameter description"
            ),
            "param2", Map.of(
                "type", "integer",
                "minimum", 1,
                "maximum", 100
            )
        ),
        "required", Arrays.asList("param1")
    )
);
```

### Common Tool Types
- **Code Generation**: Generate code in specified languages
- **Code Analysis**: Analyze existing code for issues
- **File Operations**: Read, write, or modify files
- **Command Execution**: Execute system commands
- **Search Operations**: Search through codebases or documentation

## Security Considerations

### Header Security
```java
// Security signature added automatically
request.addSecurityHeader(signatureValue);

// Custom security headers
request.getHeaders().put("X-Verification-Token", verificationToken);
request.getHeaders().put("X-Request-Timestamp", String.valueOf(timestamp));
```

### Content Validation
- Validate content before setting
- Sanitize user input appropriately
- Ensure no sensitive data in prompts
- Validate tool parameters for security

## Performance Optimization

### Request Size Management
```java
// Monitor total request size
int estimatedSize = request.getContent().length() + 
                   estimateHistorySize(request.getConversationHistory()) +
                   estimateToolsSize(request.getTools());

if (estimatedSize > MAX_REQUEST_SIZE) {
    // Optimize request size
    request.setConversationHistory(trimHistory(request.getConversationHistory()));
}
```

### Efficient Tool Usage
- Only include necessary tools
- Use specific tool descriptions
- Minimize tool parameter complexity
- Cache tool definitions when possible

## Validation Methods

### Required Field Validation
```java
public boolean isValid() {
    return id != null && !id.isEmpty() &&
           content != null && !content.isEmpty() &&
           model != null && !model.isEmpty() &&
           maxTokens > 0 &&
           temperature >= 0.0 && temperature <= 1.0;
}
```

### Content Length Validation
```java
public boolean isContentValid() {
    return content != null && 
           content.length() > 0 && 
           content.length() <= MAX_CONTENT_LENGTH;
}
```

## Error Handling

### Common Validation Errors
- **Missing ID**: Request identifier not set
- **Empty Content**: No prompt content provided
- **Invalid Model**: Unsupported model identifier
- **Invalid Temperature**: Value outside 0.0-1.0 range
- **Negative Tokens**: Max tokens less than 1

### Error Prevention
```java
ClaudeRequest request = new ClaudeRequest();
// Always set required fields
request.setId(UUID.randomUUID().toString());
request.setContent(validateAndSanitize(userInput));

// Validate before submission
if (!request.isValid()) {
    throw new IllegalArgumentException("Invalid request configuration");
}
```

## Integration Points

### With ClaudeCodeBridge
- Direct serialization to JSON for API communication
- Header extraction for HTTP request building
- Model parameter configuration for API calls

### With ClaudeTaskAdapter
- Task data conversion to request format
- Context injection from task parameters
- Tool selection based on task type

### With Security Services
- Security header injection
- Request signing and validation
- Authentication token management

## Future Enhancements

### Planned Features
- Request validation framework
- Automatic content optimization
- Tool recommendation system
- Context-aware history management

### Performance Improvements
- Request size optimization
- Compression support
- Caching mechanisms
- Batch request support

## Related Components
- [`ConversationMessage`](ConversationMessage.md) - Conversation history component
- [`ClaudeResponse`](ClaudeResponse.md) - Response counterpart
- [`ClaudeCodeBridge`](ClaudeCodeBridge.md) - Request transmission
- [`ClaudeTaskAdapter`](ClaudeTaskAdapter.md) - Request creation from tasks