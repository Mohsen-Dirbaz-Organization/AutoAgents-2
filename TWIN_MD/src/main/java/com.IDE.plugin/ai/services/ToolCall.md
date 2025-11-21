# ToolCall Documentation

## Overview
The `ToolCall` class represents a tool call in Claude's response. It encapsulates function invocations that Claude makes when using tools, providing a structured way to capture tool names, arguments, and execution context for automated processing.

## Class Information
- **Package**: `com.IDE.plugin.ai.services`
- **Type**: Data Transfer Object (DTO)
- **Dependencies**: Java Collections (Map)

## Purpose
The ToolCall serves as the fundamental data structure for:
- Capturing Claude's tool invocation requests
- Providing structured access to tool parameters
- Enabling automated tool execution workflows
- Supporting function calling capabilities in conversations
- Facilitating tool result processing and feedback

## Properties

### Core Tool Data
- **id**: `String` - Unique identifier for the tool call
- **name**: `String` - Name of the tool/function being called
- **arguments**: `Map<String, Object>` - Parameters passed to the tool

## Key Features

### 1. **Structured Tool Invocation**
```java
ToolCall toolCall = new ToolCall();
toolCall.setId("call-123");
toolCall.setName("generate_code");
toolCall.setArguments(Map.of(
    "language", "java",
    "framework", "spring-boot",
    "feature", "user authentication"
));
```

### 2. **Type-Safe Parameter Access**
```java
Map<String, Object> args = toolCall.getArguments();
String language = (String) args.get("language");
Integer maxLines = (Integer) args.get("max_lines");
Boolean includeTests = (Boolean) args.get("include_tests");
```

### 3. **Dynamic Argument Handling**
```java
// Handle various argument types
Object value = toolCall.getArguments().get("config");
if (value instanceof Map) {
    Map<String, Object> config = (Map<String, Object>) value;
    // Process configuration map
} else if (value instanceof List) {
    List<Object> items = (List<Object>) value;
    // Process list of items
}
```

## Common Tool Types

### Code Generation Tools
```java
ToolCall codeGenCall = new ToolCall();
codeGenCall.setName("generate_code");
codeGenCall.setArguments(Map.of(
    "language", "java",
    "class_name", "UserService",
    "methods", Arrays.asList("findById", "save", "delete"),
    "include_javadoc", true,
    "framework", "spring"
));
```

### File Operation Tools
```java
ToolCall fileOpCall = new ToolCall();
fileOpCall.setName("create_file");
fileOpCall.setArguments(Map.of(
    "path", "src/main/java/com/example/UserController.java",
    "content", "public class UserController { ... }",
    "overwrite", false
));
```

### Analysis Tools
```java
ToolCall analysisCall = new ToolCall();
analysisCall.setName("analyze_code");
analysisCall.setArguments(Map.of(
    "code", sourceCodeString,
    "analysis_type", "security",
    "depth", "comprehensive",
    "report_format", "json"
));
```

### Command Execution Tools
```java
ToolCall commandCall = new ToolCall();
commandCall.setName("execute_command");
commandCall.setArguments(Map.of(
    "command", "mvn clean compile",
    "working_directory", "/project/root",
    "timeout_seconds", 120,
    "capture_output", true
));
```

## Usage Examples

### Processing Tool Calls from Response
```java
ClaudeResponse response = claudeBridge.sendRequest(request);
List<ToolCall> toolCalls = response.getToolCalls();

for (ToolCall call : toolCalls) {
    System.out.println("Tool: " + call.getName());
    System.out.println("ID: " + call.getId());
    
    switch (call.getName()) {
        case "generate_code":
            handleCodeGeneration(call);
            break;
        case "create_file":
            handleFileCreation(call);
            break;
        case "execute_command":
            handleCommandExecution(call);
            break;
        default:
            System.out.println("Unknown tool: " + call.getName());
    }
}
```

### Code Generation Tool Processing
```java
public void handleCodeGeneration(ToolCall call) {
    Map<String, Object> args = call.getArguments();
    
    String language = (String) args.get("language");
    String className = (String) args.get("class_name");
    List<String> methods = (List<String>) args.get("methods");
    Boolean includeJavadoc = (Boolean) args.get("include_javadoc");
    String framework = (String) args.get("framework");
    
    CodeGenerator generator = new CodeGenerator(language, framework);
    String generatedCode = generator.generateClass(className, methods, includeJavadoc);
    
    System.out.println("Generated " + language + " class: " + className);
    System.out.println(generatedCode);
}
```

### File Operation Tool Processing
```java
public void handleFileCreation(ToolCall call) {
    Map<String, Object> args = call.getArguments();
    
    String path = (String) args.get("path");
    String content = (String) args.get("content");
    Boolean overwrite = (Boolean) args.getOrDefault("overwrite", false);
    
    try {
        Path filePath = Paths.get(path);
        
        // Check if file exists and overwrite policy
        if (Files.exists(filePath) && !overwrite) {
            System.err.println("File already exists: " + path);
            return;
        }
        
        // Create directories if needed
        Files.createDirectories(filePath.getParent());
        
        // Write file content
        Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
        
        System.out.println("Created file: " + path);
        
    } catch (IOException e) {
        System.err.println("Failed to create file: " + path + " - " + e.getMessage());
    }
}
```

### Command Execution Tool Processing
```java
public void handleCommandExecution(ToolCall call) {
    Map<String, Object> args = call.getArguments();
    
    String command = (String) args.get("command");
    String workingDir = (String) args.get("working_directory");
    Integer timeoutSeconds = (Integer) args.getOrDefault("timeout_seconds", 60);
    Boolean captureOutput = (Boolean) args.getOrDefault("capture_output", true);
    
    try {
        ProcessBuilder pb = new ProcessBuilder();
        
        // Parse command (simple space-based parsing)
        pb.command(command.split("\\s+"));
        
        if (workingDir != null) {
            pb.directory(new File(workingDir));
        }
        
        if (captureOutput) {
            pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
            pb.redirectError(ProcessBuilder.Redirect.PIPE);
        }
        
        Process process = pb.start();
        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        
        if (!finished) {
            process.destroyForcibly();
            System.err.println("Command timed out: " + command);
            return;
        }
        
        int exitCode = process.exitValue();
        System.out.println("Command executed with exit code: " + exitCode);
        
        if (captureOutput) {
            String output = new String(process.getInputStream().readAllBytes());
            String error = new String(process.getErrorStream().readAllBytes());
            
            if (!output.isEmpty()) {
                System.out.println("Output:\n" + output);
            }
            if (!error.isEmpty()) {
                System.err.println("Error:\n" + error);
            }
        }
        
    } catch (Exception e) {
        System.err.println("Failed to execute command: " + command + " - " + e.getMessage());
    }
}
```

## Argument Type Handling

### String Arguments
```java
public String getStringArgument(ToolCall call, String key, String defaultValue) {
    Object value = call.getArguments().get(key);
    return value instanceof String ? (String) value : defaultValue;
}
```

### Integer Arguments
```java
public Integer getIntegerArgument(ToolCall call, String key, Integer defaultValue) {
    Object value = call.getArguments().get(key);
    if (value instanceof Integer) {
        return (Integer) value;
    } else if (value instanceof Number) {
        return ((Number) value).intValue();
    }
    return defaultValue;
}
```

### Boolean Arguments
```java
public Boolean getBooleanArgument(ToolCall call, String key, Boolean defaultValue) {
    Object value = call.getArguments().get(key);
    return value instanceof Boolean ? (Boolean) value : defaultValue;
}
```

### List Arguments
```java
public <T> List<T> getListArgument(ToolCall call, String key, Class<T> itemType) {
    Object value = call.getArguments().get(key);
    if (value instanceof List) {
        List<Object> rawList = (List<Object>) value;
        return rawList.stream()
            .filter(itemType::isInstance)
            .map(itemType::cast)
            .collect(Collectors.toList());
    }
    return Collections.emptyList();
}
```

### Map Arguments
```java
public Map<String, Object> getMapArgument(ToolCall call, String key) {
    Object value = call.getArguments().get(key);
    return value instanceof Map ? (Map<String, Object>) value : Collections.emptyMap();
}
```

## Tool Call Validation

### Argument Validation
```java
public class ToolCallValidator {
    public boolean isValid(ToolCall call) {
        if (call == null) return false;
        if (call.getId() == null || call.getId().trim().isEmpty()) return false;
        if (call.getName() == null || call.getName().trim().isEmpty()) return false;
        if (call.getArguments() == null) return false;
        
        return validateToolSpecificArguments(call);
    }
    
    private boolean validateToolSpecificArguments(ToolCall call) {
        switch (call.getName()) {
            case "generate_code":
                return validateCodeGenerationArgs(call.getArguments());
            case "create_file":
                return validateFileCreationArgs(call.getArguments());
            case "execute_command":
                return validateCommandExecutionArgs(call.getArguments());
            default:
                return true; // Unknown tools are valid by default
        }
    }
    
    private boolean validateCodeGenerationArgs(Map<String, Object> args) {
        return args.containsKey("language") && 
               args.get("language") instanceof String &&
               !((String) args.get("language")).trim().isEmpty();
    }
    
    private boolean validateFileCreationArgs(Map<String, Object> args) {
        return args.containsKey("path") && 
               args.get("path") instanceof String &&
               args.containsKey("content") && 
               args.get("content") instanceof String;
    }
    
    private boolean validateCommandExecutionArgs(Map<String, Object> args) {
        return args.containsKey("command") && 
               args.get("command") instanceof String &&
               !((String) args.get("command")).trim().isEmpty();
    }
}
```

### Security Validation
```java
public class ToolCallSecurityValidator {
    private static final Set<String> ALLOWED_COMMANDS = Set.of(
        "mvn", "gradle", "npm", "pip", "git"
    );
    
    private static final Set<String> FORBIDDEN_PATHS = Set.of(
        "/etc", "/usr/bin", "/system"
    );
    
    public boolean isSecure(ToolCall call) {
        switch (call.getName()) {
            case "execute_command":
                return isSecureCommand(call);
            case "create_file":
            case "modify_file":
            case "delete_file":
                return isSecureFilePath(call);
            default:
                return true;
        }
    }
    
    private boolean isSecureCommand(ToolCall call) {
        String command = (String) call.getArguments().get("command");
        if (command == null) return false;
        
        String[] parts = command.split("\\s+");
        if (parts.length == 0) return false;
        
        String executable = parts[0];
        return ALLOWED_COMMANDS.contains(executable);
    }
    
    private boolean isSecureFilePath(ToolCall call) {
        String path = (String) call.getArguments().get("path");
        if (path == null) return false;
        
        // Prevent path traversal
        if (path.contains("..")) return false;
        
        // Check forbidden paths
        for (String forbidden : FORBIDDEN_PATHS) {
            if (path.startsWith(forbidden)) return false;
        }
        
        return true;
    }
}
```

## Tool Execution Framework

### Generic Tool Executor
```java
public class ToolExecutor {
    private final Map<String, ToolHandler> handlers = new HashMap<>();
    
    public void registerHandler(String toolName, ToolHandler handler) {
        handlers.put(toolName, handler);
    }
    
    public ToolExecutionResult execute(ToolCall call) {
        ToolHandler handler = handlers.get(call.getName());
        if (handler == null) {
            return ToolExecutionResult.error("No handler for tool: " + call.getName());
        }
        
        try {
            return handler.execute(call);
        } catch (Exception e) {
            return ToolExecutionResult.error("Tool execution failed: " + e.getMessage());
        }
    }
    
    // Tool handler interface
    public interface ToolHandler {
        ToolExecutionResult execute(ToolCall call) throws Exception;
    }
    
    // Execution result
    public static class ToolExecutionResult {
        private final boolean success;
        private final String message;
        private final Object result;
        
        public static ToolExecutionResult success(Object result) {
            return new ToolExecutionResult(true, null, result);
        }
        
        public static ToolExecutionResult error(String message) {
            return new ToolExecutionResult(false, message, null);
        }
        
        // Constructor and getters...
    }
}
```

### Specific Tool Handlers
```java
// Code generation handler
public class CodeGenerationHandler implements ToolExecutor.ToolHandler {
    @Override
    public ToolExecutionResult execute(ToolCall call) throws Exception {
        Map<String, Object> args = call.getArguments();
        
        String language = (String) args.get("language");
        String className = (String) args.get("class_name");
        
        CodeGenerator generator = new CodeGenerator(language);
        String code = generator.generateClass(className);
        
        return ToolExecutionResult.success(Map.of(
            "generated_code", code,
            "language", language,
            "class_name", className
        ));
    }
}

// File creation handler
public class FileCreationHandler implements ToolExecutor.ToolHandler {
    @Override
    public ToolExecutionResult execute(ToolCall call) throws Exception {
        Map<String, Object> args = call.getArguments();
        
        String path = (String) args.get("path");
        String content = (String) args.get("content");
        
        Path filePath = Paths.get(path);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
        
        return ToolExecutionResult.success(Map.of(
            "file_created", path,
            "size_bytes", content.length()
        ));
    }
}
```

## Integration with Response Processing

### Tool Call Extraction
```java
public List<ToolCall> extractToolCalls(JsonArray toolCallsArray) {
    List<ToolCall> toolCalls = new ArrayList<>();
    
    for (JsonElement element : toolCallsArray) {
        JsonObject toolCallJson = element.getAsJsonObject();
        
        ToolCall call = new ToolCall();
        call.setId(toolCallJson.get("id").getAsString());
        call.setName(toolCallJson.get("name").getAsString());
        
        // Parse arguments
        JsonObject argsJson = toolCallJson.getAsJsonObject("arguments");
        Map<String, Object> arguments = parseArguments(argsJson);
        call.setArguments(arguments);
        
        toolCalls.add(call);
    }
    
    return toolCalls;
}
```

### Response Integration
```java
public void processToolCallsInResponse(ClaudeResponse response) {
    List<ToolCall> toolCalls = response.getToolCalls();
    
    if (!toolCalls.isEmpty()) {
        ToolExecutor executor = new ToolExecutor();
        
        for (ToolCall call : toolCalls) {
            ToolExecutionResult result = executor.execute(call);
            
            if (result.isSuccess()) {
                System.out.println("Tool executed successfully: " + call.getName());
            } else {
                System.err.println("Tool execution failed: " + result.getMessage());
            }
        }
    }
}
```

## Performance Considerations

### Argument Processing
```java
// Efficient argument access
public class ToolCallUtils {
    public static <T> T getArgument(ToolCall call, String key, Class<T> type, T defaultValue) {
        Object value = call.getArguments().get(key);
        return type.isInstance(value) ? type.cast(value) : defaultValue;
    }
    
    public static Map<String, String> getStringMap(ToolCall call, String key) {
        Object value = call.getArguments().get(key);
        if (value instanceof Map) {
            Map<String, Object> rawMap = (Map<String, Object>) value;
            return rawMap.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof String)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> (String) entry.getValue()
                ));
        }
        return Collections.emptyMap();
    }
}
```

## Error Handling

### Safe Argument Access
```java
public class SafeToolCallProcessor {
    public void processToolCall(ToolCall call) {
        try {
            switch (call.getName()) {
                case "generate_code":
                    processCodeGeneration(call);
                    break;
                default:
                    System.out.println("Unknown tool: " + call.getName());
            }
        } catch (Exception e) {
            System.err.println("Tool processing failed for " + call.getName() + ": " + e.getMessage());
        }
    }
    
    private void processCodeGeneration(ToolCall call) {
        String language = getStringArgument(call, "language", "java");
        String className = getStringArgument(call, "class_name", "DefaultClass");
        
        // Safe processing with defaults
    }
    
    private String getStringArgument(ToolCall call, String key, String defaultValue) {
        try {
            Object value = call.getArguments().get(key);
            return value instanceof String ? (String) value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
```

## Future Enhancements

### Planned Features
- Tool call result tracking
- Execution context preservation
- Tool call chaining
- Advanced argument validation

### Performance Improvements
- Argument parsing optimization
- Tool execution caching
- Parallel tool execution
- Result serialization

## Related Components
- [`ClaudeResponse`](ClaudeResponse.md) - Container for tool calls
- [`ClaudeResponseHandler`](ClaudeResponseHandler.md) - Tool call processing
- [`ClaudeRequest`](ClaudeRequest.md) - Tool definitions
- [`ClaudeTaskAdapter`](ClaudeTaskAdapter.md) - Tool configuration