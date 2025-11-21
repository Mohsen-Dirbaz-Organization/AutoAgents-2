package com.IDE.plugin.ai.multiagent.agent;

import com.IDE.plugin.ai.multiagent.communication.Message;
import com.IDE.plugin.ai.multiagent.communication.MessageHandler;
import com.IDE.plugin.ai.multiagent.communication.MessageType;
import com.IDE.plugin.ai.multiagent.core.AgentRole;
import com.IDE.plugin.ai.multiagent.trust.TrustScore;

import java.util.Map;
import java.util.concurrent.*;

/**
 * EnhancedBaseAgent: Base class for all specialized agents
 * Provides common functionality for message handling, trust management, and lifecycle
 */
public abstract class EnhancedBaseAgent implements MessageHandler {
    
    protected final String agentId;
    protected final AgentRole role;
    protected final BlockingQueue<Message> messageQueue;
    protected final ExecutorService executor;
    protected volatile boolean running;
    private final Map<String, TrustScore> trustScores;
    
    protected EnhancedBaseAgent(String agentId, AgentRole role) {
        this.agentId = agentId;
        this.role = role;
        this.messageQueue = new LinkedBlockingQueue<>();
        this.executor = Executors.newSingleThreadExecutor();
        this.trustScores = new ConcurrentHashMap<>();
        this.running = true;
        
        startMessageProcessing();
    }
    
    private void startMessageProcessing() {
        executor.submit(() -> {
            while (running) {
                try {
                    Message message = messageQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (message != null) {
                        processMessage(message);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
    
    @Override
    public void handleMessage(Message message) {
        try {
            messageQueue.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log("Failed to queue message: " + e.getMessage());
        }
    }
    
    protected abstract void processMessage(Message message);
    
    protected Message createMessage(MessageType type, String recipient, Map<String, Object> payload) {
        Message message = new Message();
        message.setType(type);
        message.setSender(agentId);
        message.setRecipient(recipient);
        message.setPayload(payload);
        return message;
    }
    
    protected void sendMessage(Message message) {
        // Implementation would connect to message bus
        log("Sending message: " + message.getType() + " to " + message.getRecipient());
    }
    
    protected void broadcastToRole(AgentRole targetRole, Message message) {
        // Implementation would use message bus to broadcast
        log("Broadcasting to role " + targetRole + ": " + message.getType());
    }
    
    protected void broadcastToAll(Message message) {
        // Implementation would use message bus to broadcast
        log("Broadcasting to all: " + message.getType());
    }
    
    protected void handleTrustUpdate(String agentId, TrustScore newScore) {
        trustScores.put(agentId, newScore);
        log("Trust updated for " + agentId + ": " + newScore.getLevel());
    }
    
    protected void log(String message) {
        System.out.println("[" + agentId + "] " + message);
    }
    
    public void shutdown() {
        running = false;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
    
    public String getAgentId() {
        return agentId;
    }
    
    public AgentRole getRole() {
        return role;
    }
}