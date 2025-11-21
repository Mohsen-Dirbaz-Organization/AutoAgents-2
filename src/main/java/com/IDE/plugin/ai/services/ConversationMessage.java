package com.IDE.plugin.ai.services;

/**
 * Represents a message in a conversation with Claude
 */
public class ConversationMessage {
    private String role;
    private String content;
    private long timestamp;
    
    public ConversationMessage() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public ConversationMessage(String role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and setters
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}