package com.IDE.plugin.ai.multiagent.core;

import java.time.Instant;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents an event in the agent system
 */
public class AgentEvent {
    private final String id;
    private final EventType type;
    private final String source;
    private final String message;
    private final Instant timestamp;
    private final Map<String, Object> data;
    
    public AgentEvent(EventType type, String source, String message) {
        this(type, source, message, new HashMap<>());
    }
    
    public AgentEvent(EventType type, String source, String message, Map<String, Object> data) {
        this.id = java.util.UUID.randomUUID().toString();
        this.type = type;
        this.source = source;
        this.message = message;
        this.timestamp = Instant.now();
        this.data = new HashMap<>(data);
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public EventType getType() {
        return type;
    }
    
    public String getSource() {
        return source;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public Map<String, Object> getData() {
        return new HashMap<>(data);
    }
    
    public Object getData(String key) {
        return data.get(key);
    }
    
    /**
     * Event types
     */
    public enum EventType {
        // Agent lifecycle events
        AGENT_STARTED("Agent Started"),
        AGENT_STOPPED("Agent Stopped"),
        AGENT_ERROR("Agent Error"),
        
        // Task events
        TASK_CREATED("Task Created"),
        TASK_STARTED("Task Started"),
        TASK_COMPLETED("Task Completed"),
        TASK_FAILED("Task Failed"),
        TASK_CANCELLED("Task Cancelled"),
        
        // Communication events
        MESSAGE_SENT("Message Sent"),
        MESSAGE_RECEIVED("Message Received"),
        
        // System events
        SYSTEM_INFO("System Information"),
        SYSTEM_WARNING("System Warning"),
        SYSTEM_ERROR("System Error"),
        
        // Custom events
        CUSTOM("Custom Event");
        
        private final String displayName;
        
        EventType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @Override
    public String toString() {
        return String.format("AgentEvent[id=%s, type=%s, source=%s, message=%s, timestamp=%s]",
                id, type, source, message, timestamp);
    }
}