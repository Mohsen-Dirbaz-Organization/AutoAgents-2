package com.IDE.plugin.ai.services;

import java.util.Map;

/**
 * Represents a tool call in Claude's response
 */
public class ToolCall {
    private String id;
    private String name;
    private Map<String, Object> arguments;
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Map<String, Object> getArguments() {
        return arguments;
    }
    
    public void setArguments(Map<String, Object> arguments) {
        this.arguments = arguments;
    }
}