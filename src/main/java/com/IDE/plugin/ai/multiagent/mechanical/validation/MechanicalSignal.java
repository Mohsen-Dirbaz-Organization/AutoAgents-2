package com.IDE.plugin.ai.multiagent.mechanical.validation;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MechanicalSignal: Core signal representation for mechanical signaling system
 */
public class MechanicalSignal {
    private final String id;
    private final String senderId;
    private final String recipientId;
    private final byte[] payload;
    private final long timestamp;
    private final int sequenceNumber;
    private final SignalPriority priority;
    private final String checksum;
    private final String signature;
    private final Map<String, Object> metadata;
    private int hopCount;
    private int retryCount;
    
    public MechanicalSignal(String id, String senderId, String recipientId, 
                          byte[] payload, int sequenceNumber, SignalPriority priority) {
        this.id = id;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.payload = Arrays.copyOf(payload, payload.length);
        this.timestamp = System.currentTimeMillis();
        this.sequenceNumber = sequenceNumber;
        this.priority = priority;
        this.checksum = calculateChecksum();
        this.signature = ""; // Will be set by sender
        this.metadata = new ConcurrentHashMap<>();
        this.hopCount = 0;
        this.retryCount = 0;
    }
    
    private String calculateChecksum() {
        // Simplified checksum calculation
        return String.valueOf(Arrays.hashCode(payload));
    }
    
    // Getters
    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getRecipientId() { return recipientId; }
    public byte[] getPayload() { return Arrays.copyOf(payload, payload.length); }
    public long getTimestamp() { return timestamp; }
    public int getSequenceNumber() { return sequenceNumber; }
    public SignalPriority getPriority() { return priority; }
    public String getChecksum() { return checksum; }
    public String getSignature() { return signature; }
    public Map<String, Object> getMetadata() { return new ConcurrentHashMap<>(metadata); }
    public int getHopCount() { return hopCount; }
    public int getRetryCount() { return retryCount; }
    
    // Setters for mutable fields
    public void setSignature(String signature) { this.signature = signature; }
    public void incrementHopCount() { this.hopCount++; }
    public void incrementRetryCount() { this.retryCount++; }
    public void addMetadata(String key, Object value) { metadata.put(key, value); }
}

/**
 * Signal priority levels
 */
enum SignalPriority {
    CRITICAL(0),
    HIGH(1),
    NORMAL(2),
    LOW(3);
    
    private final int value;
    
    SignalPriority(int value) {
        this.value = value;
    }
    
    public int getValue() { return value; }
}