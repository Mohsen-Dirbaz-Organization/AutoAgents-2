package com.IDE.plugin.ai.multiagent.communication;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Message: Core message structure for agent communication
 */
public class Message {
    private String id;
    private MessageType type;
    private String sender;
    private String recipient;
    private Map<String, Object> payload;
    private Map<String, String> headers;
    private long timestamp;
    private int retryCount;
    private String signature;
    
    public Message() {
        this.id = UUID.randomUUID().toString();
        this.payload = new HashMap<>();
        this.headers = new HashMap<>();
        this.retryCount = 0;
    }
    
    public Message(MessageType type, String sender, String recipient, Map<String, Object> payload) {
        this();
        this.type = type;
        this.sender = sender;
        this.recipient = recipient;
        this.payload = payload;
    }
    
    public Message copy() {
        Message copy = new Message();
        copy.id = this.id;
        copy.type = this.type;
        copy.sender = this.sender;
        copy.recipient = this.recipient;
        copy.payload = new HashMap<>(this.payload);
        copy.headers = new HashMap<>(this.headers);
        copy.timestamp = this.timestamp;
        copy.retryCount = this.retryCount;
        copy.signature = this.signature;
        return copy;
    }
    
    public void incrementRetryCount() {
        this.retryCount++;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
    
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
    
    @Override
    public String toString() {
        return "Message{" +
               "id='" + id + '\'' +
               ", type=" + type +
               ", sender='" + sender + '\'' +
               ", recipient='" + recipient + '\'' +
               ", timestamp=" + timestamp +
               '}';
    }
}