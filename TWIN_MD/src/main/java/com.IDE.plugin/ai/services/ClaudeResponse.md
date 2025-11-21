# ClaudeResponse Documentation

## Overview
The `ClaudeResponse` class represents a response received from the Claude API. It encapsulates the response content, metadata, token usage information, and processing results, providing a comprehensive data structure for handling Claude's output.

## Class Information
- **Package**: `com.IDE.plugin.ai.services`
- **Type**: Data Transfer Object (DTO)
- **Dependencies**: ToolCall, ClaudeResponseHandler.ProcessingResult

## Purpose
The ClaudeResponse serves as the primary data structure for:
- Containing Claude's response content and metadata
- Tracking token usage and API consumption
- Managing tool calls and function invocations
- Storing processing results and extracted information
- Providing response correlation with original requests

## Properties

### Core Identification
- **id**: `String` - Unique response identifier from Claude API
- **requestId**: `String` - Corresponding request identifier for correlation
- **timestamp**: `long` - Response generation timestamp

### Model Information
- **model**: `String` - Claude model that generated the response
- **type**: `String` - Response content type (default: "text")

### Content Data
- **content**: `String` - Main response content from Claude
- **toolCalls**: `List<ToolCall>` - Tool function calls made by Claude

### Usage Metrics
- **inputTokens**: `int` - Tokens consumed from input (prompt + context)
- **outputTokens**: `int` - Tokens generated in response

### Processing Results
- **processingResult**: `ClaudeResponseHandler.ProcessingResult` - Post-processing results and actions

## Key Features

### 1. **Comprehensive Response Data**
```java
ClaudeResponse response = new ClaudeResponse();
response.setId("resp-12345");
response.setRequestId("req-67890");
response.setModel("claude-3-opus-20240229");
response.setContent("Here's the implementation you requested...");
```

### 2. **Token Usage Tracking**
```java
// Monitor API consumption
int totalTokens = response.getInputTokens() + response.getOutputTokens();
double cost = calculateCost(response.getInputTokens(), response.getOutputTokens());

System.out.println("Total tokens used: " + totalTokens);
System.out.println("Estimated cost: $" + cost);
```

### 3. **Tool Call Management**
```java
if (!response.getToolCalls().isEmpty()) {
    for (ToolCall call : response.getToolCalls()) {
        System.out.println("Tool: " + call.getName());
        System.out.println("Arguments: " + call.getArguments());
        // Process tool call
    }
}
```

### 4. **Processing Result Integration**
```java
ClaudeResponseHandler.ProcessingResult result = response.getProcessingResult();
if (result != null) {
    List<ClaudeResponseHandler.Action> actions = result.getActions();
    // Execute extracted actions
}
```

## Default Values

### Content Defaults
- **Type**: `"text"` (Standard text response)
- **Tool Calls**: Empty ArrayList
- **Timestamp**: Set during response creation

### Processing Defaults
- **Processing Result**: `null` until processed by handler
- **Token Counts**: 0 until populated from API response

## Usage Examples

### Basic Response Handling
```java
ClaudeResponse response = claudeBridge.sendRequest(request);

System.out.println("Response ID: " + response.getId());
System.out.println("Model: " + response.getModel());
System.out.println("Content: " + response.getContent());
System.out.println("Input tokens: " + response.getInputTokens());
System.out.println("Output tokens: " + response.getOutputTokens());
```

### Tool Call Processing
```java
ClaudeResponse response = claudeBridge.sendRequest(request);

if (!response.getToolCalls().isEmpty()) {
    for (ToolCall call : response.getToolCalls()) {
        switch (call.getName()) {
            case "generate_code":
                String code = (String) call.getArguments().get("code");
                String language = (String) call.getArguments().get("language");
                handleCodeGeneration(code, language);
                break;
                
            case "execute_command":
                String command = (String) call.getArguments().get("command");
                executeCommand(command);
                break;
                
            default:
                System.out.println("Unknown tool call: " + call.getName());
        }
    }
}
```

### Response with Processing Results
```java
ClaudeResponse response = responseHandler.processResponse(rawResponse, context);

ClaudeResponseHandler.ProcessingResult result = response.getProcessingResult();
if (result != null) {
    // Handle extracted actions
    for (ClaudeResponseHandler.Action action : result.getActions()) {
        switch (action.getType()) {
            case CREATE_FILE:
                String path = (String) action.getParameters().get("path");
                String content = (String) action.getParameters().get("content");
                createFile(path, content);
                break;
                
            case SHOW_NOTIFICATION:
                String message = (String) action.getParameters().get("message");
                showNotification(message);
                break;
        }
    }
    
    // Access metadata
    Object codeBlocks = result.getMetadata("code_blocks");
    if (codeBlocks != null) {
        processCodeBlocks((List<CodeBlock>) codeBlocks);
    }
}
```

### Token Usage Analysis
```java
ClaudeResponse response = claudeBridge.sendRequest(request);

// Calculate token efficiency
double efficiency = (double) response.getOutputTokens() / response.getInputTokens();
System.out.println("Token efficiency ratio: " + efficiency);

// Track cumulative usage
totalInputTokens += response.getInputTokens();
totalOutputTokens += response.getOutputTokens();

// Cost calculation (example rates)
double inputCost = response.getInputTokens() * 0.000015; // $0.015 per 1K tokens
double outputCost = response.getOutputTokens() * 0.000075; // $0.075 per 1K tokens
double totalCost = inputCost + outputCost;
```

## Response Content Types

### Text Responses
```java
if ("text".equals(response.getType())) {
    String content = response.getContent();
    // Process text content
    extractCodeBlocks(content);
    extractFileReferences(content);
    parseInstructions(content);
}
```

### Tool Use Responses
```java
if ("tool_use".equals(response.getType())) {
    // Response contains tool calls
    List<ToolCall> calls = response.getToolCalls();
    for (ToolCall call : calls) {
        executeToolCall(call);
    }
}
```

### Mixed Responses
```java
// Some responses contain both text and tool calls
String textContent = response.getContent();
List<ToolCall> toolCalls = response.getToolCalls();

if (textContent != null && !textContent.isEmpty()) {
    processTextContent(textContent);
}

if (!toolCalls.isEmpty()) {
    processToolCalls(toolCalls);
}
```

## Token Usage Patterns

### Input Token Composition
Input tokens typically include:
- System prompt tokens
- Conversation history tokens
- Current request content tokens
- Tool definition tokens

### Output Token Tracking
```java
// Monitor response length vs token count
int contentLength = response.getContent().length();
int outputTokens = response.getOutputTokens();
double tokensPerChar = (double) outputTokens / contentLength;

System.out.println("Tokens per character: " + tokensPerChar);
```

### Cost Optimization
```java
public class TokenUsageOptimizer {
    private static final int TARGET_MAX_TOKENS = 4000;
    
    public ClaudeRequest optimizeRequest(ClaudeRequest request) {
        int estimatedInputTokens = estimateInputTokens(request);
        
        if (estimatedInputTokens > TARGET_MAX_TOKENS * 0.8) {
            // Reduce conversation history
            request.setConversationHistory(
                trimHistory(request.getConversationHistory())
            );
        }
        
        return request;
    }
}
```

## Processing Result Integration

### Action Extraction
```java
ClaudeResponseHandler.ProcessingResult result = response.getProcessingResult();
if (result != null && result.success) {
    List<ClaudeResponseHandler.Action> actions = result.getActions();
    
    // Filter actions by type
    List<ClaudeResponseHandler.Action> fileActions = actions.stream()
        .filter(action -> action.getType() == ActionType.CREATE_FILE || 
                         action.getType() == ActionType.MODIFY_FILE)
        .collect(Collectors.toList());
    
    // Execute file operations
    executeFileActions(fileActions);
}
```

### Metadata Access
```java
ClaudeResponseHandler.ProcessingResult result = response.getProcessingResult();
if (result != null) {
    // Extract code blocks
    List<CodeBlock> codeBlocks = (List<CodeBlock>) result.getMetadata("code_blocks");
    if (codeBlocks != null) {
        for (CodeBlock block : codeBlocks) {
            System.out.println("Language: " + block.getLanguage());
            System.out.println("Code: " + block.getCode());
        }
    }
    
    // Extract file references
    List<String> fileRefs = (List<String>) result.getMetadata("file_references");
    if (fileRefs != null) {
        fileRefs.forEach(ref -> System.out.println("Referenced file: " + ref));
    }
}
```

## Correlation and Tracking

### Request-Response Correlation
```java
// Maintain request-response correlation
Map<String, ClaudeRequest> pendingRequests = new ConcurrentHashMap<>();
Map<String, ClaudeResponse> completedResponses = new ConcurrentHashMap<>();

// When sending request
String requestId = UUID.randomUUID().toString();
ClaudeRequest request = new ClaudeRequest();
request.setId(requestId);
pendingRequests.put(requestId, request);

// When receiving response
ClaudeResponse response = claudeBridge.sendRequest(request);
completedResponses.put(response.getRequestId(), response);
pendingRequests.remove(response.getRequestId());
```

### Response Chain Management
```java
public class ResponseChain {
    private final List<ClaudeResponse> responses = new ArrayList<>();
    
    public void addResponse(ClaudeResponse response) {
        responses.add(response);
    }
    
    public int getTotalTokensUsed() {
        return responses.stream()
            .mapToInt(r -> r.getInputTokens() + r.getOutputTokens())
            .sum();
    }
    
    public List<ToolCall> getAllToolCalls() {
        return responses.stream()
            .flatMap(r -> r.getToolCalls().stream())
            .collect(Collectors.toList());
    }
}
```

## Error Handling

### Response Validation
```java
public boolean isValidResponse(ClaudeResponse response) {
    return response != null &&
           response.getId() != null && !response.getId().isEmpty() &&
           response.getContent() != null &&
           response.getInputTokens() >= 0 &&
           response.getOutputTokens() >= 0;
}
```

### Error Response Detection
```java
public boolean isErrorResponse(ClaudeResponse response) {
    if (response.getContent() == null || response.getContent().isEmpty()) {
        return true;
    }
    
    // Check for error indicators in content
    String content = response.getContent().toLowerCase();
    return content.contains("error:") || 
           content.contains("failed:") ||
           content.contains("unable to");
}
```

## Performance Monitoring

### Response Time Tracking
```java
public class ResponseMetrics {
    private long requestTime;
    private long responseTime;
    
    public void markRequestSent() {
        requestTime = System.currentTimeMillis();
    }
    
    public void markResponseReceived(ClaudeResponse response) {
        responseTime = System.currentTimeMillis();
        long duration = responseTime - requestTime;
        
        System.out.println("Response time: " + duration + "ms");
        System.out.println("Tokens/second: " + 
            (response.getOutputTokens() * 1000.0 / duration));
    }
}
```

### Token Rate Analysis
```java
public class TokenRateAnalyzer {
    public void analyzeTokenRate(ClaudeResponse response, long durationMs) {
        double tokensPerSecond = (response.getOutputTokens() * 1000.0) / durationMs;
        double cost = calculateCost(response);
        double costPerSecond = cost / (durationMs / 1000.0);
        
        System.out.println("Generation rate: " + tokensPerSecond + " tokens/sec");
        System.out.println("Cost rate: $" + costPerSecond + "/sec");
    }
}
```

## Integration Points

### With Response Handler
- Automatic processing result attachment
- Action extraction and execution
- Metadata population and access

### With Message Bus
- Response event publishing
- Status update broadcasting
- Error notification distribution

### With Agent System
- Task completion notification
- Result delivery to requesting agents
- Context sharing for follow-up tasks

## Future Enhancements

### Planned Features
- Response caching mechanisms
- Advanced token usage analytics
- Response quality scoring
- Automatic response optimization

### Performance Improvements
- Streaming response support
- Partial response processing
- Response compression
- Metadata extraction optimization

## Related Components
- [`ClaudeRequest`](ClaudeRequest.md) - Request counterpart
- [`ToolCall`](ToolCall.md) - Tool invocation data
- [`ClaudeResponseHandler`](ClaudeResponseHandler.md) - Response processing
- [`ClaudeCodeBridge`](ClaudeCodeBridge.md) - Response reception