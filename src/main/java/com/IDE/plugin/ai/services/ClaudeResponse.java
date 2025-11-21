package com.IDE.plugin.ai.services;

import java.util.*;

/**
 * Represents a response from the Claude API
 */
public class ClaudeResponse {
    private String id;
    private String requestId;
    private String model;
    private String content;
    private String type = "text";
    private int inputTokens;
    private int outputTokens;
    private List<ToolCall> toolCalls = new ArrayList<>();
    private long timestamp;
    private ClaudeResponseHandler.ProcessingResult processingResult;
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public int getInputTokens() {
        return inputTokens;
    }
    
    public void setInputTokens(int inputTokens) {
        this.inputTokens = inputTokens;
    }
    
    public int getOutputTokens() {
        return outputTokens;
    }
    
    public void setOutputTokens(int outputTokens) {
        this.outputTokens = outputTokens;
    }
    
    public List<ToolCall> getToolCalls() {
        return toolCalls;
    }
    
    public void setToolCalls(List<ToolCall> toolCalls) {
        this.toolCalls = toolCalls;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public ClaudeResponseHandler.ProcessingResult getProcessingResult() {
        return processingResult;
    }
    
    public void setProcessingResult(ClaudeResponseHandler.ProcessingResult processingResult) {
        this.processingResult = processingResult;
    }
}