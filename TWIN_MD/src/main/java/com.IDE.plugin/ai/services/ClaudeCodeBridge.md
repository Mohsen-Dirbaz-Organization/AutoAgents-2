# ClaudeCodeBridge Documentation

## Overview
The `ClaudeCodeBridge` class provides a robust bridge between the multi-agent system and the Claude API. It handles API communication, connection pooling, and request/response translation, ensuring reliable and efficient interaction with Claude's capabilities.

## Class Information
- **Package**: `com.IDE.plugin.ai.services`
- **Type**: Service Bridge/Adapter
- **Dependencies**: IntelliJ Platform SDK, Google Gson, Java HTTP/Network APIs

## Purpose
The ClaudeCodeBridge serves as the primary communication interface for:
- Managing HTTP connections to Claude API
- Handling request serialization and response parsing
- Providing connection pooling and timeout management
- Implementing retry logic and error handling
- Monitoring request metrics and performance

## Key Features

### 1. **Connection Management**
- Automated connection pooling for efficient resource usage
- Configurable timeout settings (connection: 30s, read: 120s)
- Proxy support through IntelliJ's HttpConfigurable
- Active connection tracking and cleanup

### 2. **Request Processing**
- JSON serialization of request data using Gson
- Security header injection for authenticated requests
- Support for conversation history and tool integration
- Custom header management per request

### 3. **Response Handling**
- Structured parsing of Claude API responses
- Token usage tracking (input/output tokens)
- Tool call extraction and processing
- Error response handling with detailed logging

### 4. **Monitoring & Metrics**
- Real-time request tracking with unique IDs
- Performance metrics collection (duration, success rate)
- Connection statistics and health monitoring
- Automatic cleanup of old metrics data

## Architecture

### Core Components

#### RequestMetrics (Inner Class)
```java
private static class RequestMetrics {
    final long startTime;
    long endTime;
    int responseCode;
    long responseSize;
    boolean success;
}
```
**Purpose**: Tracks performance and outcome metrics for each request.

#### Connection Pool Management
- **Active Connections**: `Map<String, HttpURLConnection>` for tracking ongoing requests
- **Request Metrics**: `Map<String, RequestMetrics>` for performance monitoring
- **Thread Pool**: Cached thread pool for concurrent request processing

#### API Configuration
- **Base URL**: `https://api.anthropic.com/v1/messages`
- **API Version**: `2023-06-01`
- **Default Model**: Configurable through ClaudeRequest
- **Content Type**: `application/json`

## Key Methods

### Initialization
```java
public void initialize()
```
- Loads API key from environment or configuration
- Validates authentication credentials
- Marks bridge as ready for operation
- Throws IllegalStateException if API key missing

### Request Processing
```java
public ClaudeResponse sendRequest(@NotNull ClaudeRequest request) throws Exception
```
- Creates HTTP connection with proper headers
- Serializes request data to JSON
- Manages request lifecycle and cleanup
- Returns parsed ClaudeResponse or throws exception

### Request Cancellation
```java
public void cancelRequest(@NotNull String requestId)
```
- Safely disconnects active connection
- Removes from tracking maps
- Logs cancellation for debugging

### Statistics & Monitoring
```java
public Map<String, Object> getStatistics()
```
Returns comprehensive bridge statistics:
- Total and successful request counts
- Active connection count
- Average request duration
- Initialization status

## Security Features

### 1. **API Key Management**
- Environment variable support (`CLAUDE_API_KEY`)
- IDE settings integration
- Secure header injection for authentication

### 2. **Request Validation**
- Initialization state checking
- Request ID validation and tracking
- Connection timeout enforcement

### 3. **Error Handling**
- Comprehensive exception handling
- Secure error message extraction
- Connection cleanup on failures

## Configuration

### Environment Variables
```bash
CLAUDE_API_KEY=your_claude_api_key_here
```

### Request Configuration
- **Connection Timeout**: 30,000ms (30 seconds)
- **Read Timeout**: 120,000ms (2 minutes)
- **Metrics Retention**: 1 hour
- **Thread Pool**: Cached, daemon threads

## Usage Examples

### Basic Request
```java
ClaudeCodeBridge bridge = new ClaudeCodeBridge(project);
bridge.initialize();

ClaudeRequest request = new ClaudeRequest();
request.setContent("Explain this code...");
request.setModel("claude-3-opus-20240229");

ClaudeResponse response = bridge.sendRequest(request);
```

### Request with Context
```java
ClaudeRequest request = new ClaudeRequest();
request.setContent("Review this implementation");
request.setSystemPrompt("You are a code reviewer");

// Add conversation history
List<ConversationMessage> history = Arrays.asList(
    new ConversationMessage("user", "Previous context"),
    new ConversationMessage("assistant", "Previous response")
);
request.setConversationHistory(history);

ClaudeResponse response = bridge.sendRequest(request);
```

### Monitoring
```java
Map<String, Object> stats = bridge.getStatistics();
System.out.println("Total requests: " + stats.get("total_requests"));
System.out.println("Success rate: " + stats.get("successful_requests"));
System.out.println("Avg duration: " + stats.get("average_duration_ms") + "ms");
```

## Error Handling

### Common Exception Types
- **IllegalStateException**: Bridge not initialized
- **IOException**: Network communication errors
- **SecurityException**: Authentication failures
- **RuntimeException**: JSON parsing errors

### Error Recovery
- Automatic retry logic for transient failures
- Connection cleanup on errors
- Detailed error logging for debugging
- Graceful degradation on API issues

## Performance Considerations

### 1. **Connection Efficiency**
- Reuses HTTP connections when possible
- Implements connection pooling
- Manages timeouts appropriately

### 2. **Memory Management**
- Automatic cleanup of old metrics
- Request tracking with size limits
- Efficient JSON processing

### 3. **Concurrency**
- Thread-safe operations
- Concurrent request handling
- Non-blocking request cancellation

## Integration Points

### With Multi-Agent System
- Integrates with `ClaudeCodeIntegrationService`
- Supports agent task routing
- Provides request/response translation

### With IntelliJ Platform
- Uses platform HTTP configuration
- Integrates with IDE settings
- Supports proxy configurations

### With Claude API
- Follows Anthropic API specifications
- Supports latest API version
- Handles tool calls and system prompts

## Maintenance & Monitoring

### Health Checks
- Initialization status tracking
- Connection pool monitoring
- Request success rate analysis

### Debugging Support
- Comprehensive logging
- Request/response tracing
- Performance metrics collection

### Resource Management
- Automatic connection cleanup
- Memory-efficient operation
- Graceful shutdown procedures

## Future Enhancements

### Planned Features
- Request rate limiting
- Advanced retry strategies
- Response caching mechanisms
- Enhanced security features

### Scalability Improvements
- Connection pool optimization
- Metrics aggregation
- Load balancing support
- Circuit breaker patterns

## Related Components
- [`ClaudeCodeIntegrationService`](ClaudeCodeIntegrationService.md) - Main integration service
- [`ClaudeRequest`](ClaudeRequest.md) - Request data structure
- [`ClaudeResponse`](ClaudeResponse.md) - Response data structure
- [`ClaudeResponseHandler`](ClaudeResponseHandler.md) - Response processing