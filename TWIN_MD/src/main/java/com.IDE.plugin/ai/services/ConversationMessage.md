# ConversationMessage Documentation

## Overview
The `ConversationMessage` class represents a message in a conversation with Claude. It encapsulates the essential components of conversational context, including role identification, message content, and timing information for proper conversation flow management.

## Class Information
- **Package**: `com.IDE.plugin.ai.services`
- **Type**: Data Transfer Object (DTO)
- **Dependencies**: None (Pure Java POJO)

## Purpose
The ConversationMessage serves as the fundamental building block for:
- Maintaining conversation history with Claude
- Preserving context across multiple interactions
- Supporting role-based conversation flow
- Enabling time-based conversation analysis
- Facilitating conversation state management

## Properties

### Core Message Data
- **role**: `String` - Message sender role ("user", "assistant", "system")
- **content**: `String` - The actual message content/text
- **timestamp**: `long` - Message creation time in milliseconds

## Key Features

### 1. **Role-Based Messaging**
```java
ConversationMessage userMessage = new ConversationMessage("user", "What is dependency injection?");
ConversationMessage assistantMessage = new ConversationMessage("assistant", "Dependency injection is a design pattern...");
ConversationMessage systemMessage = new ConversationMessage("system", "You are a helpful coding assistant.");
```

### 2. **Automatic Timestamping**
```java
ConversationMessage message = new ConversationMessage();
// Timestamp is automatically set to current time
long creationTime = message.getTimestamp();
```

### 3. **Flexible Construction**
```java
// Default constructor with manual setup
ConversationMessage message1 = new ConversationMessage();
message1.setRole("user");
message1.setContent("Hello, Claude!");

// Convenience constructor
ConversationMessage message2 = new ConversationMessage("assistant", "Hello! How can I help you?");
```

## Role Types

### Standard Roles

#### User Role
```java
ConversationMessage userMsg = new ConversationMessage("user", "Can you help me refactor this code?");
```
**Purpose**: Represents messages from the human user or agent requesting assistance.

#### Assistant Role
```java
ConversationMessage assistantMsg = new ConversationMessage("assistant", "I'd be happy to help refactor your code...");
```
**Purpose**: Represents responses from Claude or the AI assistant.

#### System Role
```java
ConversationMessage systemMsg = new ConversationMessage("system", "You are an expert Java developer focused on clean code principles.");
```
**Purpose**: Provides context, instructions, or system-level guidance for the conversation.

## Usage Examples

### Basic Conversation Building
```java
List<ConversationMessage> conversation = new ArrayList<>();

// System context
conversation.add(new ConversationMessage("system", 
    "You are a senior software architect helping with code design."));

// User query
conversation.add(new ConversationMessage("user", 
    "How should I structure a microservices architecture?"));

// Assistant response
conversation.add(new ConversationMessage("assistant", 
    "For microservices architecture, I recommend the following structure..."));

// Follow-up question
conversation.add(new ConversationMessage("user", 
    "What about database design for microservices?"));
```

### Context-Rich Conversations
```java
// Building conversation with code context
ConversationMessage codeContext = new ConversationMessage("system", 
    "The user is working on a Spring Boot application with the following structure:\n" +
    "- UserController: REST endpoints\n" +
    "- UserService: Business logic\n" +
    "- UserRepository: Data access");

ConversationMessage userQuestion = new ConversationMessage("user", 
    "How can I add caching to improve performance?");

ClaudeRequest request = new ClaudeRequest();
request.setConversationHistory(Arrays.asList(codeContext, userQuestion));
```

### Multi-Turn Technical Discussion
```java
List<ConversationMessage> technicalDiscussion = Arrays.asList(
    new ConversationMessage("user", "What are the benefits of using reactive programming?"),
    
    new ConversationMessage("assistant", 
        "Reactive programming offers several benefits:\n" +
        "1. Non-blocking I/O operations\n" +
        "2. Better resource utilization\n" +
        "3. Improved scalability\n" +
        "4. Backpressure handling"),
    
    new ConversationMessage("user", "Can you show me an example with Spring WebFlux?"),
    
    new ConversationMessage("assistant", 
        "Here's a simple WebFlux example:\n\n" +
        "```java\n" +
        "@RestController\n" +
        "public class ReactiveController {\n" +
        "    @GetMapping(\"/users\")\n" +
        "    public Flux<User> getUsers() {\n" +
        "        return userService.findAll();\n" +
        "    }\n" +
        "}\n" +
        "```"),
    
    new ConversationMessage("user", "How does this handle backpressure?")
);
```

## Conversation History Management

### History Trimming
```java
public class ConversationHistoryManager {
    private static final int MAX_HISTORY_SIZE = 20;
    
    public List<ConversationMessage> trimHistory(List<ConversationMessage> history) {
        if (history.size() <= MAX_HISTORY_SIZE) {
            return history;
        }
        
        // Keep system messages and recent conversation
        List<ConversationMessage> systemMessages = history.stream()
            .filter(msg -> "system".equals(msg.getRole()))
            .collect(Collectors.toList());
        
        List<ConversationMessage> recentMessages = history.stream()
            .filter(msg -> !"system".equals(msg.getRole()))
            .skip(Math.max(0, history.size() - MAX_HISTORY_SIZE + systemMessages.size()))
            .collect(Collectors.toList());
        
        List<ConversationMessage> trimmed = new ArrayList<>(systemMessages);
        trimmed.addAll(recentMessages);
        return trimmed;
    }
}
```

### Context Preservation
```java
public List<ConversationMessage> preserveImportantContext(
        List<ConversationMessage> fullHistory, 
        String currentQuery) {
    
    List<ConversationMessage> preserved = new ArrayList<>();
    
    // Always preserve system messages
    fullHistory.stream()
        .filter(msg -> "system".equals(msg.getRole()))
        .forEach(preserved::add);
    
    // Preserve messages related to current query
    String queryLowerCase = currentQuery.toLowerCase();
    fullHistory.stream()
        .filter(msg -> !"system".equals(msg.getRole()))
        .filter(msg -> isRelevantToQuery(msg.getContent(), queryLowerCase))
        .forEach(preserved::add);
    
    // Add recent messages
    List<ConversationMessage> recent = fullHistory.stream()
        .filter(msg -> !"system".equals(msg.getRole()))
        .skip(Math.max(0, fullHistory.size() - 6))
        .collect(Collectors.toList());
    
    preserved.addAll(recent);
    return preserved;
}
```

## Timestamp Management

### Time-Based Analysis
```java
public class ConversationAnalyzer {
    public long getConversationDuration(List<ConversationMessage> conversation) {
        if (conversation.size() < 2) return 0;
        
        long start = conversation.get(0).getTimestamp();
        long end = conversation.get(conversation.size() - 1).getTimestamp();
        return end - start;
    }
    
    public List<ConversationMessage> getMessagesInTimeRange(
            List<ConversationMessage> conversation,
            long startTime, 
            long endTime) {
        
        return conversation.stream()
            .filter(msg -> msg.getTimestamp() >= startTime && msg.getTimestamp() <= endTime)
            .collect(Collectors.toList());
    }
    
    public double getMessageFrequency(List<ConversationMessage> conversation, 
                                     String role, 
                                     long timeWindowMs) {
        long currentTime = System.currentTimeMillis();
        long windowStart = currentTime - timeWindowMs;
        
        long messageCount = conversation.stream()
            .filter(msg -> role.equals(msg.getRole()))
            .filter(msg -> msg.getTimestamp() >= windowStart)
            .count();
        
        return (double) messageCount / (timeWindowMs / 1000.0); // messages per second
    }
}
```

### Conversation Metrics
```java
public class ConversationMetrics {
    public Map<String, Object> analyzeConversation(List<ConversationMessage> conversation) {
        Map<String, Object> metrics = new HashMap<>();
        
        // Message counts by role
        Map<String, Long> roleCounts = conversation.stream()
            .collect(Collectors.groupingBy(
                ConversationMessage::getRole,
                Collectors.counting()
            ));
        
        // Average message length by role
        Map<String, Double> averageLengths = conversation.stream()
            .collect(Collectors.groupingBy(
                ConversationMessage::getRole,
                Collectors.averagingInt(msg -> msg.getContent().length())
            ));
        
        // Conversation duration
        long duration = getConversationDuration(conversation);
        
        metrics.put("message_counts", roleCounts);
        metrics.put("average_message_lengths", averageLengths);
        metrics.put("duration_ms", duration);
        metrics.put("total_messages", conversation.size());
        
        return metrics;
    }
}
```

## Content Formatting

### Code Integration
```java
public ConversationMessage createCodeContextMessage(String filePath, String code) {
    String content = String.format(
        "File: %s\n\n```java\n%s\n```", 
        filePath, 
        code
    );
    return new ConversationMessage("system", content);
}

public ConversationMessage createCodeReviewMessage(String code, List<String> concerns) {
    StringBuilder content = new StringBuilder();
    content.append("Please review the following code:\n\n");
    content.append("```java\n").append(code).append("\n```\n\n");
    
    if (!concerns.isEmpty()) {
        content.append("Specific concerns:\n");
        concerns.forEach(concern -> content.append("- ").append(concern).append("\n"));
    }
    
    return new ConversationMessage("user", content.toString());
}
```

### Multi-Part Messages
```java
public List<ConversationMessage> createMultiPartContext(
        Map<String, String> codeFiles, 
        String userQuery) {
    
    List<ConversationMessage> messages = new ArrayList<>();
    
    // Add system context
    messages.add(new ConversationMessage("system", 
        "You are reviewing a Java project with the following structure:"));
    
    // Add file contexts
    codeFiles.forEach((filePath, code) -> {
        String content = String.format("File: %s\n```java\n%s\n```", filePath, code);
        messages.add(new ConversationMessage("system", content));
    });
    
    // Add user query
    messages.add(new ConversationMessage("user", userQuery));
    
    return messages;
}
```

## Validation and Safety

### Content Validation
```java
public boolean isValidMessage(ConversationMessage message) {
    if (message == null) return false;
    if (message.getRole() == null || message.getRole().trim().isEmpty()) return false;
    if (message.getContent() == null || message.getContent().trim().isEmpty()) return false;
    if (message.getTimestamp() <= 0) return false;
    
    // Validate role
    Set<String> validRoles = Set.of("user", "assistant", "system");
    if (!validRoles.contains(message.getRole())) return false;
    
    return true;
}

public ConversationMessage sanitizeMessage(ConversationMessage message) {
    if (message == null) return null;
    
    ConversationMessage sanitized = new ConversationMessage();
    sanitized.setRole(message.getRole() != null ? message.getRole().trim() : "");
    sanitized.setContent(message.getContent() != null ? message.getContent().trim() : "");
    sanitized.setTimestamp(message.getTimestamp() > 0 ? message.getTimestamp() : System.currentTimeMillis());
    
    return sanitized;
}
```

### Content Safety
```java
public class MessageSanitizer {
    private static final int MAX_CONTENT_LENGTH = 50000; // 50KB limit
    
    public ConversationMessage limitContentSize(ConversationMessage message) {
        if (message.getContent().length() <= MAX_CONTENT_LENGTH) {
            return message;
        }
        
        String truncatedContent = message.getContent().substring(0, MAX_CONTENT_LENGTH - 3) + "...";
        ConversationMessage limited = new ConversationMessage(message.getRole(), truncatedContent);
        limited.setTimestamp(message.getTimestamp());
        
        return limited;
    }
    
    public List<ConversationMessage> removeSensitiveData(List<ConversationMessage> messages) {
        return messages.stream()
            .map(this::sanitizeContent)
            .collect(Collectors.toList());
    }
    
    private ConversationMessage sanitizeContent(ConversationMessage message) {
        String content = message.getContent();
        
        // Remove potential sensitive patterns
        content = content.replaceAll("password\\s*=\\s*[\"'][^\"']*[\"']", "password=***");
        content = content.replaceAll("api[_-]?key\\s*=\\s*[\"'][^\"']*[\"']", "api_key=***");
        content = content.replaceAll("token\\s*=\\s*[\"'][^\"']*[\"']", "token=***");
        
        ConversationMessage sanitized = new ConversationMessage(message.getRole(), content);
        sanitized.setTimestamp(message.getTimestamp());
        
        return sanitized;
    }
}
```

## Performance Optimization

### Memory Management
```java
public class ConversationOptimizer {
    public List<ConversationMessage> optimizeForMemory(List<ConversationMessage> conversation) {
        return conversation.stream()
            .map(this::compressMessage)
            .collect(Collectors.toList());
    }
    
    private ConversationMessage compressMessage(ConversationMessage message) {
        String content = message.getContent();
        
        // Remove excessive whitespace
        content = content.replaceAll("\\s+", " ");
        content = content.trim();
        
        // Compress code blocks
        content = compressCodeBlocks(content);
        
        ConversationMessage compressed = new ConversationMessage(message.getRole(), content);
        compressed.setTimestamp(message.getTimestamp());
        
        return compressed;
    }
    
    private String compressCodeBlocks(String content) {
        // Remove excessive blank lines in code blocks
        return content.replaceAll("(```[\\w]*\\n)(\\s*\\n){3,}", "$1\n\n");
    }
}
```

## Integration Points

### With Claude Requests
```java
ClaudeRequest request = new ClaudeRequest();
List<ConversationMessage> history = buildConversationHistory();
request.setConversationHistory(history);
```

### With Response Processing
```java
ClaudeResponse response = claudeBridge.sendRequest(request);
ConversationMessage responseMessage = new ConversationMessage("assistant", response.getContent());

// Add to ongoing conversation
conversation.add(responseMessage);
```

### With Multi-Agent System
```java
// Convert agent messages to conversation format
public ConversationMessage fromAgentMessage(AgentMessage agentMsg) {
    String role = agentMsg.isFromUser() ? "user" : "assistant";
    return new ConversationMessage(role, agentMsg.getContent());
}
```

## Future Enhancements

### Planned Features
- Message threading and branching
- Conversation topics and tagging
- Advanced content analysis
- Message importance scoring

### Performance Improvements
- Lazy content loading
- Message compression
- Efficient serialization
- Memory pooling

## Related Components
- [`ClaudeRequest`](ClaudeRequest.md) - Request containing conversation history
- [`ClaudeResponse`](ClaudeResponse.md) - Response generating new messages
- [`ClaudeTaskAdapter`](ClaudeTaskAdapter.md) - Context message creation
- [`ClaudeResponseHandler`](ClaudeResponseHandler.md) - Message processing