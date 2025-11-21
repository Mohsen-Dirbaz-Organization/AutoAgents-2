package com.IDE.plugin.ai.multiagent.event;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Base class for events in the agent system.
 */
public class AgentEvent {
    private final String eventId;
    private final String sourceAgentId;
    private final EventType type;
    private final LocalDateTime timestamp;
    private final Map<String, Object> data;
    
    /**
     * Creates a new agent event.
     * 
     * @param sourceAgentId The ID of the agent that generated this event
     * @param type The type of event
     * @param data Additional data for the event
     */
    public AgentEvent(String sourceAgentId, EventType type, Map<String, Object> data) {
        this.eventId = UUID.randomUUID().toString();
        this.sourceAgentId = sourceAgentId;
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.data = data != null ? new HashMap<>(data) : new HashMap<>();
    }
    
    /**
     * Returns the unique identifier for this event.
     */
    public String getEventId() {
        return eventId;
    }
    
    /**
     * Returns the ID of the agent that generated this event.
     */
    public String getSourceAgentId() {
        return sourceAgentId;
    }
    
    /**
     * Returns the type of this event.
     */
    public EventType getType() {
        return type;
    }
    
    /**
     * Returns the timestamp when this event was created.
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    /**
     * Returns the additional data for this event.
     */
    public Map<String, Object> getData() {
        return new HashMap<>(data);
    }
    
    /**
     * Gets a value from the event data.
     * 
     * @param key The key for the value
     * @return The value associated with the key, or null if no value is associated with the key
     */
    public Object get(String key) {
        return data.get(key);
    }
    
    /**
     * Gets a value from the event data, casting it to the specified type.
     * 
     * @param key The key for the value
     * @param type The type to cast the value to
     * @param <T> The type parameter
     * @return The value associated with the key, cast to the specified type, or null if no value is associated with the key
     * @throws ClassCastException if the value cannot be cast to the specified type
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        return (T) value;
    }
    
    /**
     * Enum representing the possible types of agent events.
     */
    public enum EventType {
        /**
         * Agent lifecycle events (start, stop, etc.).
         */
        LIFECYCLE,
        
        /**
         * Task-related events.
         */
        TASK,
        
        /**
         * Message-related events.
         */
        MESSAGE,
        
        /**
         * Error events.
         */
        ERROR,
        
        /**
         * System events.
         */
        SYSTEM,
        
        /**
         * Custom events.
         */
        CUSTOM
    }
}