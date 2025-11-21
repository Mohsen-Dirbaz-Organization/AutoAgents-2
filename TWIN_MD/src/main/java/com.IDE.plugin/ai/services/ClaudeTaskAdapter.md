# ClaudeTaskAdapter Documentation

## Overview
The `ClaudeTaskAdapter` class adapts agent tasks for Claude Code processing, converting multi-agent system tasks into Claude-compatible requests. It provides specialized adapters for different task types and manages context injection for optimal Claude interaction.

## Class Information
- **Package**: `com.IDE.plugin.ai.services`
- **Type**: Task Adaptation Service
- **Dependencies**: IntelliJ Platform SDK, PSI API, Java Collections

## Purpose
The ClaudeTaskAdapter serves as the intelligent translation layer for:
- Converting agent tasks into Claude-specific request formats
- Managing task-specific prompt engineering
- Injecting relevant project context and code files
- Optimizing requests for different Claude capabilities
- Providing specialized handling for various development tasks

## Key Features

### 1. **Pluggable Adapter Architecture**
- Specialized adapters for different task types
- Interface-based adapter registration
- Automatic adapter selection based on task type
- Extensible framework for custom task types

### 2. **Context-Aware Request Building**
- Intelligent context file selection
- Project structure analysis
- Code content injection with size limits
- Relevant file filtering and optimization

### 3. **Task-Specific Prompt Engineering**
- Optimized prompts for each task type
- Framework and language-specific customization
- Best practice guidance integration
- Constraint and requirement handling

### 4. **Tool Integration**
- Automatic tool selection based on task type
- Tool parameter optimization
- Function calling configuration
- Tool result processing

## Architecture

### Core Components

#### TaskAdapter Interface
```java
private interface TaskAdapter {
    boolean canHandle(String taskType);
    ClaudeRequest adapt(String taskId, Map<String, Object> taskData);
}
```
**Purpose**: Defines contract for specialized task adapters.

#### Context Management
- **MAX_CONTEXT_FILES**: 10 files maximum per request
- **MAX_FILE_SIZE**: 100KB size limit per file
- Context file filtering and optimization
- Project structure analysis

#### Adapter Registry
- Code generation adapter
- Code review adapter
- Refactoring adapter
- Documentation adapter
- Bug fix adapter
- Test generation adapter

## Specialized Adapters

### 1. **Code Generation Adapter**
```java
private class CodeGenerationAdapter implements TaskAdapter {
    @Override
    public boolean canHandle(String taskType) {
        return "code_generation".equals(taskType) || 
               "generate_code".equals(taskType) ||
               "implement_feature".equals(taskType);
    }
}
```
**Purpose**: Handles code generation tasks with language and framework specifications.

**Task Data Parameters**:
- `description`: Feature requirements
- `language`: Target programming language
- `frameworks`: Required frameworks/libraries
- `constraints`: Implementation constraints

### 2. **Code Review Adapter**
```java
private class CodeReviewAdapter implements TaskAdapter {
    @Override
    public boolean canHandle(String taskType) {
        return "code_review".equals(taskType) || 
               "review_code".equals(taskType);
    }
}
```
**Purpose**: Processes code review requests with focus areas and quality criteria.

**Task Data Parameters**:
- `code`: Code to be reviewed
- `focus_areas`: Specific review aspects
- `quality_standards`: Required quality criteria

### 3. **Refactoring Adapter**
```java
private class RefactoringAdapter implements TaskAdapter {
    @Override
    public boolean canHandle(String taskType) {
        return "refactoring".equals(taskType) || 
               "refactor_code".equals(taskType);
    }
}
```
**Purpose**: Handles code refactoring tasks with specific improvement goals.

**Task Data Parameters**:
- `code`: Code to be refactored
- `refactoring_type`: Type of refactoring needed
- `goals`: Improvement objectives

### 4. **Documentation Adapter**
```java
private class DocumentationAdapter implements TaskAdapter {
    @Override
    public boolean canHandle(String taskType) {
        return "documentation".equals(taskType) || 
               "generate_docs".equals(taskType);
    }
}
```
**Purpose**: Generates documentation for code, APIs, and systems.

**Task Data Parameters**:
- `code`: Code to document
- `doc_type`: Documentation type (API, user, technical)
- `format`: Output format (markdown, javadoc, etc.)

### 5. **Bug Fix Adapter**
```java
private class BugFixAdapter implements TaskAdapter {
    @Override
    public boolean canHandle(String taskType) {
        return "bug_fix".equals(taskType) || 
               "fix_bug".equals(taskType);
    }
}
```
**Purpose**: Handles bug identification and fixing tasks.

**Task Data Parameters**:
- `bug_description`: Problem description
- `code`: Code containing the bug
- `error_message`: Error details
- `reproduction_steps`: How to reproduce

### 6. **Test Generation Adapter**
```java
private class TestGenerationAdapter implements TaskAdapter {
    @Override
    public boolean canHandle(String taskType) {
        return "test_generation".equals(taskType) || 
               "generate_tests".equals(taskType);
    }
}
```
**Purpose**: Generates comprehensive test suites for code.

**Task Data Parameters**:
- `code`: Code to test
- `test_framework`: Testing framework to use
- `coverage_goals`: Coverage requirements

## Key Methods

### Main Adaptation
```java
public ClaudeRequest adaptTask(@NotNull String taskId, 
                              @NotNull Map<String, Object> taskData)
```
- Validates task type presence
- Finds appropriate adapter
- Falls back to generic adapter if needed
- Returns optimized Claude request

### Context Injection
```java
private void addContextToRequest(ClaudeRequest request, 
                                Map<String, Object> taskData)
```
- Extracts context files from task data
- Filters files by size and relevance
- Injects file content into conversation history
- Optimizes context for Claude processing

### Generic Request Creation
```java
private ClaudeRequest createGenericRequest(String taskId, 
                                          Map<String, Object> taskData)
```
- Creates base request structure
- Builds generic prompt from task data
- Adds standard configuration
- Injects context if available

## Usage Examples

### Code Generation Task
```java
ClaudeTaskAdapter adapter = new ClaudeTaskAdapter(project);
adapter.initialize();

Map<String, Object> taskData = Map.of(
    "task_type", "code_generation",
    "description", "Create a REST API for user management",
    "language", "java",
    "frameworks", Arrays.asList("spring-boot", "jpa"),
    "constraints", Map.of(
        "max_endpoints", 5,
        "include_validation", true
    )
);

ClaudeRequest request = adapter.adaptTask("gen-123", taskData);
```

**Generated Prompt Example**:
```
Generate code for the following requirement:

Create a REST API for user management

Language: java
Frameworks/Libraries: spring-boot, jpa

Please provide clean, well-documented code following best practices.
```

### Code Review Task
```java
Map<String, Object> reviewTask = Map.of(
    "task_type", "code_review",
    "code", sourceCodeString,
    "focus_areas", Arrays.asList("security", "performance", "maintainability"),
    "context_files", Arrays.asList("User.java", "UserRepository.java")
);

ClaudeRequest request = adapter.adaptTask("review-456", reviewTask);
```

**Generated Prompt Example**:
```
Please review the following code:

```java
public class UserService {
    // code here
}
```

Focus on:
1. Code quality and best practices
2. Potential bugs or issues
3. Performance optimizations
4. Security vulnerabilities
5. Maintainability and readability
```

### Refactoring Task
```java
Map<String, Object> refactorTask = Map.of(
    "task_type", "refactoring",
    "code", legacyCode,
    "refactoring_type", "extract_methods",
    "goals", Arrays.asList("reduce_complexity", "improve_testability")
);

ClaudeRequest request = adapter.adaptTask("refactor-789", refactorTask);
```

### Bug Fix Task
```java
Map<String, Object> bugFixTask = Map.of(
    "task_type", "bug_fix",
    "bug_description", "NullPointerException in user authentication",
    "code", problematicCode,
    "error_message", "java.lang.NullPointerException at line 42",
    "reproduction_steps", Arrays.asList(
        "1. Login with empty username",
        "2. Click submit button",
        "3. Exception occurs"
    )
);

ClaudeRequest request = adapter.adaptTask("bugfix-101", bugFixTask);
```

## Context Management

### File Selection Strategy
```java
private void addContextToRequest(ClaudeRequest request, Map<String, Object> taskData) {
    List<String> contextFiles = (List<String>) taskData.get("context_files");
    if (contextFiles == null || contextFiles.isEmpty()) {
        return;
    }
    
    List<ConversationMessage> contextMessages = new ArrayList<>();
    
    ReadAction.run(() -> {
        PsiManager psiManager = PsiManager.getInstance(project);
        
        contextFiles.stream()
            .limit(MAX_CONTEXT_FILES)
            .forEach(filePath -> {
                VirtualFile file = project.getBaseDir().findFileByRelativePath(filePath);
                if (file != null && file.isValid() && file.getLength() < MAX_FILE_SIZE) {
                    // Add file content to context
                }
            });
    });
    
    request.setConversationHistory(contextMessages);
}
```

### Context Optimization
- **Size Limits**: Files over 100KB are skipped
- **Relevance Filtering**: Only specified context files included
- **Format Optimization**: Files formatted for Claude consumption
- **Count Limits**: Maximum 10 files per request

## Prompt Engineering Patterns

### Generic Prompt Structure
```java
private String buildGenericPrompt(Map<String, Object> taskData) {
    StringBuilder prompt = new StringBuilder();
    
    prompt.append("Task: ").append(taskData.get("task_type")).append("\n\n");
    
    if (taskData.containsKey("description")) {
        prompt.append("Description: ").append(taskData.get("description")).append("\n\n");
    }
    
    if (taskData.containsKey("requirements")) {
        prompt.append("Requirements:\n");
        List<String> requirements = (List<String>) taskData.get("requirements");
        requirements.forEach(req -> prompt.append("- ").append(req).append("\n"));
        prompt.append("\n");
    }
    
    return prompt.toString();
}
```

### Task-Specific Prompts

#### Code Generation Prompt
```
Generate code for the following requirement:

{description}

Language: {language}
Frameworks/Libraries: {frameworks}

Please provide clean, well-documented code following best practices.
```

#### Code Review Prompt
```
Please review the following code:

```{language}
{code}
```

Focus on:
1. Code quality and best practices
2. Potential bugs or issues
3. Performance optimizations
4. Security vulnerabilities
5. Maintainability and readability
```

#### Refactoring Prompt
```
Refactor the following code:

```{language}
{code}
```

Refactoring type: {refactoring_type}

Ensure the refactored code:
- Maintains the same functionality
- Follows SOLID principles
- Is more maintainable and readable
- Has better performance if possible
```

## Tool Integration

### Code Generation Tool
```java
private Map<String, Object> createCodeGenerationTool() {
    return Map.of(
        "name", "generate_code",
        "description", "Generate code based on requirements",
        "parameters", Map.of(
            "type", "object",
            "properties", Map.of(
                "code", Map.of("type", "string", "description", "Generated code"),
                "language", Map.of("type", "string", "description", "Programming language"),
                "explanation", Map.of("type", "string", "description", "Code explanation")
            )
        )
    );
}
```

### Tool Selection Logic
```java
// Automatically add relevant tools based on task type
switch (taskType) {
    case "code_generation":
        request.addTool(createCodeGenerationTool());
        break;
    case "code_review":
        request.addTool(createCodeAnalysisTool());
        break;
    case "bug_fix":
        request.addTool(createDebuggingTool());
        break;
}
```

## System Prompt Optimization

### Task-Specific System Prompts
```java
// Code Generation
request.setSystemPrompt("You are an expert software developer. Generate high-quality, production-ready code.");

// Code Review
request.setSystemPrompt("You are a senior code reviewer. Provide thorough, constructive feedback.");

// Refactoring
request.setSystemPrompt("You are an expert in code refactoring and clean code practices.");

// Documentation
request.setSystemPrompt("You are a technical documentation expert. Create clear, comprehensive documentation.");

// Bug Fix
request.setSystemPrompt("You are a debugging expert. Identify and fix bugs effectively.");

// Test Generation
request.setSystemPrompt("You are a testing expert. Generate comprehensive, well-structured tests.");
```

## Performance Considerations

### Context Size Management
```java
private boolean isContextSizeOptimal(List<String> contextFiles) {
    int totalSize = 0;
    for (String filePath : contextFiles) {
        VirtualFile file = project.getBaseDir().findFileByRelativePath(filePath);
        if (file != null) {
            totalSize += file.getLength();
            if (totalSize > MAX_TOTAL_CONTEXT_SIZE) {
                return false;
            }
        }
    }
    return true;
}
```

### Adapter Selection Optimization
```java
@Nullable
private TaskAdapter findAdapter(String taskType) {
    // Cache frequently used adapters
    return adapterCache.computeIfAbsent(taskType, type ->
        adapters.values().stream()
            .filter(adapter -> adapter.canHandle(type))
            .findFirst()
            .orElse(null)
    );
}
```

## Error Handling

### Task Validation
```java
public ClaudeRequest adaptTask(String taskId, Map<String, Object> taskData) {
    String taskType = (String) taskData.get("task_type");
    if (taskType == null) {
        throw new IllegalArgumentException("Task type not specified");
    }
    
    if (taskData.get("description") == null) {
        throw new IllegalArgumentException("Task description required");
    }
    
    // Continue with adaptation...
}
```

### Context Loading Safety
```java
private void addContextToRequest(ClaudeRequest request, Map<String, Object> taskData) {
    try {
        ReadAction.run(() -> {
            // Safe file loading with error handling
            contextFiles.forEach(filePath -> {
                try {
                    loadFileContent(filePath);
                } catch (Exception e) {
                    LOG.warn("Failed to load context file: " + filePath, e);
                    // Continue with other files
                }
            });
        });
    } catch (Exception e) {
        LOG.error("Context loading failed", e);
        // Continue without context
    }
}
```

## Extension Points

### Custom Adapter Registration
```java
public void registerAdapter(String taskType, TaskAdapter adapter) {
    adapters.put(taskType, adapter);
    LOG.info("Registered custom adapter for task type: " + taskType);
}

// Example custom adapter
public class DatabaseMigrationAdapter implements TaskAdapter {
    @Override
    public boolean canHandle(String taskType) {
        return "database_migration".equals(taskType);
    }
    
    @Override
    public ClaudeRequest adapt(String taskId, Map<String, Object> taskData) {
        // Custom migration task handling
    }
}
```

### Task Type Extension
```java
// New task types can be easily added
public static final String PERFORMANCE_OPTIMIZATION = "performance_optimization";
public static final String SECURITY_AUDIT = "security_audit";
public static final String API_DESIGN = "api_design";
```

## Integration Points

### With Multi-Agent System
- Task data extraction and validation
- Context information gathering
- Agent capability mapping
- Result formatting for agents

### With Claude Integration
- Request optimization for Claude API
- Tool selection and configuration
- System prompt optimization
- Context size management

### With IntelliJ Platform
- Project structure analysis
- File system access and validation
- PSI tree integration
- Code analysis capabilities

## Future Enhancements

### Planned Features
- Machine learning-based context selection
- Dynamic prompt optimization
- Advanced tool recommendation
- Task complexity analysis

### Performance Improvements
- Context caching mechanisms
- Prompt template optimization
- Batch context loading
- Adaptive context sizing

## Related Components
- [`ClaudeRequest`](ClaudeRequest.md) - Request data structure
- [`ClaudeCodeIntegrationService`](ClaudeCodeIntegrationService.md) - Integration service
- [`ConversationMessage`](ConversationMessage.md) - Context message format
- [`ClaudeResponseHandler`](ClaudeResponseHandler.md) - Response processing