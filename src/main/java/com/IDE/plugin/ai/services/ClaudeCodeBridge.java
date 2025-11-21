package com.IDE.plugin.ai.services;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.net.HttpConfigurable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Bridge between multi-agent system and Claude API.
 * Handles API communication, connection pooling, and request/response translation.
 */
public class ClaudeCodeBridge {
    private static final Logger LOG = Logger.getInstance(ClaudeCodeBridge.class);
    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";
    private static final String API_VERSION = "2023-06-01";
    private static final int CONNECTION_TIMEOUT_MS = 30000;
    private static final int READ_TIMEOUT_MS = 120000;
    
    private final Project project;
    private final Gson gson;
    private final ExecutorService requestExecutor;
    private final Map<String, HttpURLConnection> activeConnections;
    private final Map<String, RequestMetrics> requestMetrics;
    
    private volatile String apiKey;
    private volatile boolean initialized = false;
    
    /**
     * Request metrics for monitoring
     */
    private static class RequestMetrics {
        final long startTime;
        long endTime;
        int responseCode;
        long responseSize;
        boolean success;
        
        RequestMetrics() {
            this.startTime = System.currentTimeMillis();
        }
        
        void complete(int code, long size, boolean success) {
            this.endTime = System.currentTimeMillis();
            this.responseCode = code;
            this.responseSize = size;
            this.success = success;
        }
        
        long getDuration() {
            return endTime - startTime;
        }
    }
    
    public ClaudeCodeBridge(@NotNull Project project) {
        this.project = project;
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
        this.requestExecutor = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r, "Claude-API-Worker");
            thread.setDaemon(true);
            return thread;
        });
        this.activeConnections = new ConcurrentHashMap<>();
        this.requestMetrics = new ConcurrentHashMap<>();
    }
    
    /**
     * Initialize the bridge
     */
    public void initialize() {
        if (initialized) {
            return;
        }
        
        // Load API key from configuration
        loadApiKey();
        
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Claude API key not configured");
        }
        
        initialized = true;
        LOG.info("Claude Code Bridge initialized");
    }
    
    /**
     * Send request to Claude API
     */
    public ClaudeResponse sendRequest(@NotNull ClaudeRequest request) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("Bridge not initialized");
        }
        
        String requestId = request.getId();
        RequestMetrics metrics = new RequestMetrics();
        requestMetrics.put(requestId, metrics);
        
        try {
            // Create connection
            HttpURLConnection connection = createConnection(request);
            activeConnections.put(requestId, connection);
            
            // Send request
            sendRequestData(connection, request);
            
            // Read response
            ClaudeResponse response = readResponse(connection, requestId);
            
            // Update metrics
            metrics.complete(connection.getResponseCode(), 
                           response.getContent().length(), 
                           true);
            
            return response;
            
        } catch (Exception e) {
            metrics.complete(0, 0, false);
            LOG.error("Failed to send Claude request: " + requestId, e);
            throw e;
        } finally {
            activeConnections.remove(requestId);
            
            // Clean up old metrics
            cleanupMetrics();
        }
    }
    
    /**
     * Cancel an active request
     */
    public void cancelRequest(@NotNull String requestId) {
        HttpURLConnection connection = activeConnections.get(requestId);
        if (connection != null) {
            connection.disconnect();
            activeConnections.remove(requestId);
            LOG.info("Cancelled Claude request: " + requestId);
        }
    }
    
    /**
     * Create HTTP connection for Claude API
     */
    private HttpURLConnection createConnection(ClaudeRequest request) throws Exception {
        URL url = new URL(CLAUDE_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        // Configure connection
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        
        // Set headers
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("x-api-key", apiKey);
        connection.setRequestProperty("anthropic-version", API_VERSION);
        connection.setRequestProperty("X-Request-ID", request.getId());
        
        // Add custom headers from request
        request.getHeaders().forEach(connection::setRequestProperty);
        
        // Configure proxy if needed
        HttpConfigurable.getInstance().prepareURL(url.toString());
        
        return connection;
    }
    
    /**
     * Send request data to Claude
     */
    private void sendRequestData(HttpURLConnection connection, ClaudeRequest request) throws IOException {
        JsonObject requestBody = new JsonObject();
        
        // Build request body
        requestBody.addProperty("model", request.getModel());
        requestBody.add("messages", buildMessages(request));
        requestBody.addProperty("max_tokens", request.getMaxTokens());
        requestBody.addProperty("temperature", request.getTemperature());
        
        // Add system prompt if present
        if (request.getSystemPrompt() != null) {
            requestBody.addProperty("system", request.getSystemPrompt());
        }
        
        // Add tools if present
        if (!request.getTools().isEmpty()) {
            requestBody.add("tools", gson.toJsonTree(request.getTools()));
        }
        
        // Write request
        try (OutputStreamWriter writer = new OutputStreamWriter(
                connection.getOutputStream(), StandardCharsets.UTF_8)) {
            gson.toJson(requestBody, writer);
            writer.flush();
        }
    }
    
    /**
     * Build messages array for Claude
     */
    private com.google.gson.JsonArray buildMessages(ClaudeRequest request) {
        com.google.gson.JsonArray messages = new com.google.gson.JsonArray();
        
        // Add conversation history
        request.getConversationHistory().forEach(msg -> {
            JsonObject message = new JsonObject();
            message.addProperty("role", msg.getRole());
            message.addProperty("content", msg.getContent());
            messages.add(message);
        });
        
        // Add current message
        JsonObject currentMessage = new JsonObject();
        currentMessage.addProperty("role", "user");
        currentMessage.addProperty("content", request.getContent());
        messages.add(currentMessage);
        
        return messages;
    }
    
    /**
     * Read response from Claude
     */
    private ClaudeResponse readResponse(HttpURLConnection connection, String requestId) throws IOException {
        int responseCode = connection.getResponseCode();
        
        if (responseCode != HttpURLConnection.HTTP_OK) {
            String error = readErrorResponse(connection);
            throw new IOException("Claude API error (code " + responseCode + "): " + error);
        }
        
        // Read response body
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append('\n');
            }
        }
        
        // Parse response
        return parseResponse(response.toString(), requestId);
    }
    
    /**
     * Read error response from connection
     */
    private String readErrorResponse(HttpURLConnection connection) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            return "Unknown error";
        }
    }
    
    /**
     * Parse Claude API response
     */
    private ClaudeResponse parseResponse(String responseJson, String requestId) {
        JsonObject json = JsonParser.parseString(responseJson).getAsJsonObject();
        
        ClaudeResponse response = new ClaudeResponse();
        response.setId(json.get("id").getAsString());
        response.setRequestId(requestId);
        response.setModel(json.get("model").getAsString());
        
        // Extract content
        JsonObject content = json.getAsJsonArray("content").get(0).getAsJsonObject();
        response.setContent(content.get("text").getAsString());
        response.setType(content.get("type").getAsString());
        
        // Extract usage
        JsonObject usage = json.getAsJsonObject("usage");
        response.setInputTokens(usage.get("input_tokens").getAsInt());
        response.setOutputTokens(usage.get("output_tokens").getAsInt());
        
        // Extract tool calls if present
        if (json.has("tool_calls")) {
            List<ToolCall> toolCalls = parseToolCalls(json.getAsJsonArray("tool_calls"));
            response.setToolCalls(toolCalls);
        }
        
        response.setTimestamp(System.currentTimeMillis());
        
        return response;
    }
    
    /**
     * Parse tool calls from response
     */
    private List<ToolCall> parseToolCalls(com.google.gson.JsonArray toolCallsArray) {
        List<ToolCall> toolCalls = new ArrayList<>();
        
        toolCallsArray.forEach(element -> {
            JsonObject toolCall = element.getAsJsonObject();
            ToolCall call = new ToolCall();
            call.setId(toolCall.get("id").getAsString());
            call.setName(toolCall.get("name").getAsString());
            call.setArguments(gson.fromJson(toolCall.get("arguments"), Map.class));
            toolCalls.add(call);
        });
        
        return toolCalls;
    }
    
    /**
     * Load API key from configuration
     */
    private void loadApiKey() {
        // Try environment variable first
        apiKey = System.getenv("CLAUDE_API_KEY");
        
        if (apiKey == null || apiKey.isEmpty()) {
            // Try IDE settings
            apiKey = ApplicationManager.getApplication().runReadAction(() -> {
                // This would typically load from IDE settings/configuration
                return null; // Placeholder
            });
        }
    }
    
    /**
     * Clean up old metrics
     */
    private void cleanupMetrics() {
        long cutoffTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1);
        requestMetrics.entrySet().removeIf(entry -> 
            entry.getValue().startTime < cutoffTime);
    }
    
    /**
     * Get bridge statistics
     */
    public Map<String, Object> getStatistics() {
        long totalRequests = requestMetrics.size();
        long successfulRequests = requestMetrics.values().stream()
            .filter(m -> m.success)
            .count();
        
        double avgDuration = requestMetrics.values().stream()
            .filter(m -> m.endTime > 0)
            .mapToLong(RequestMetrics::getDuration)
            .average()
            .orElse(0);
        
        return Map.of(
            "total_requests", totalRequests,
            "successful_requests", successfulRequests,
            "active_connections", activeConnections.size(),
            "average_duration_ms", avgDuration,
            "initialized", initialized
        );
    }
    
    /**
     * Shutdown the bridge
     */
    public void shutdown() {
        LOG.info("Shutting down Claude Code Bridge");
        
        // Cancel all active connections
        activeConnections.values().forEach(HttpURLConnection::disconnect);
        activeConnections.clear();
        
        // Shutdown executor
        requestExecutor.shutdown();
        try {
            if (!requestExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                requestExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            requestExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        initialized = false;
    }
}