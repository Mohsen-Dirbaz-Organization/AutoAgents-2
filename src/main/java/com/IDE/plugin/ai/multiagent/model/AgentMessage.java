package com.IDE.plugin.ai.multiagent.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a message exchanged between agents.
 */
public class AgentMessage {
    private final String messageId;
    private final String senderAgentId;
    private final String targetAgentId;
    private final String content;
    private final Map<String, Object> metadata;
    private final LocalDateTime timestamp;
    private final MessageType type;
    private final int priority;
    
    /**
     * Creates a new agent message.
     * 
     * @param senderAgentId The ID of the agent sending the message
     * @param targetAgentId The ID of the agent receiving the message
     * @param content The content of the message
     * @param metadata Additional metadata for the message
     * @param type The type of message
     * @param priority The priority of the message (higher values indicate higher priority)
     */
    public AgentMessage(String senderAgentId, String targetAgentId, String content, 
                       Map<String, Object> metadata, MessageType type, int priority) {
        this.messageId = UUID.randomUUID().toString();
        this.senderAgentId = senderAgentId;
        this.targetAgentId = targetAgentId;
        this.content = content;
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.timestamp = LocalDateTime.now();
        this.type = type;
        this.priority = priority;
    }
    
    /**
     * Returns the unique identifier for this message.
     */
    public String getMessageId() {
        return messageId;
    }
    
    /**
     * Returns the ID of the agent that sent this message.
     */
    public String getSenderAgentId() {
        return senderAgentId;
    }
    
    /**
     * Returns the ID of the agent that should receive this message.
     */
    public String getTargetAgentId() {
        return targetAgentId;
    }
    
    /**
     * Returns the content of this message.
     */
    public String getContent() {
        return content;
    }
    
    /**
     * Returns the additional metadata for this message.
     */
    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }
    
    /**
     * Returns the timestamp when this message was created.
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    /**
     * Returns the type of this message.
     */
    public MessageType getType() {
        return type;
    }
    
    /**
     * Returns the priority of this message.
     */
    public int getPriority() {
        return priority;
    }
    
    /**
     * Enum representing the possible types of agent messages.
     */
    public enum MessageType {
        /**
         * Regular message for general communication.
         */
        REGULAR,
        
        /**
         * Command message that requests an action.
         */
        COMMAND,
        
        /**
         * Response message that contains a reply to a previous message.
         */
        RESPONSE,
        
        /**
         * Notification message that informs about an event.
         */
        NOTIFICATION,
        
        /**
         * Error message that indicates a problem.
         */
        ERROR,
        
        /**
         * System message for internal communication.
         */
        SYSTEM
    }
}