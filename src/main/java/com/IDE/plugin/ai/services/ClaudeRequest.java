package com.IDE.plugin.ai.services;

import java.util.*;

/**
 * Represents a request to the Claude API
 */
public class ClaudeRequest {
    private String id;
    private String taskId;
    private String model = "claude-3-opus-20240229";
    private String content;
    private String systemPrompt;
    private int maxTokens = 4000;
    private double temperature = 0.7;
    private List<ConversationMessage> conversationHistory = new ArrayList<>();
    private List<Map<String, Object>> tools = new ArrayList<>();
    private Map<String, String> headers = new HashMap<>();
    private long timestamp;
    
    public ClaudeRequest() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public void addSecurityHeader(String signature) {
        headers.put("X-Security-Signature", signature);
    }
    
    public void addTool(Map<String, Object> tool) {
        tools.add(tool);
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTaskId() {
        return taskId;
    }
    
    public void setTaskId(String taskId) {
        this.taskId = taskId;
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
    
    public String getSystemPrompt() {
        return systemPrompt;
    }
    
    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }
    
    public int getMaxTokens() {
        return maxTokens;
    }
    
    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }
    
    public double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    
    public List<ConversationMessage> getConversationHistory() {
        return conversationHistory;
    }
    
    public void setConversationHistory(List<ConversationMessage> conversationHistory) {
        this.conversationHistory = conversationHistory;
    }
    
    public List<Map<String, Object>> getTools() {
        return tools;
    }
    
    public void setTools(List<Map<String, Object>> tools) {
        this.tools = tools;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}