package com.IDE.plugin.ai.multiagent.communication;

import com.IDE.plugin.ai.multiagent.core.AgentRole;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * MessageBus: Core messaging infrastructure for agent communication
 * Provides reliable message routing, delivery, and queue management
 */
public class MessageBus {
    
    private final Map<String, MessageQueue> agentQueues;
    private final Map<AgentRole, Set<String>> roleRegistry;
    private final Map<String, MessageHandler> messageHandlers;
    private final ExecutorService deliveryExecutor;
    private final ScheduledExecutorService maintenanceExecutor;
    private final BlockingQueue<Message> globalQueue;
    private final Map<String, SubscriptionInfo> subscriptions;
    private final AtomicLong messageCounter;
    private final MessageRouter router;
    private final DeliveryTracker deliveryTracker;
    private volatile boolean running;
    
    public MessageBus() {
        this.agentQueues = new ConcurrentHashMap<>();
        this.roleRegistry = new ConcurrentHashMap<>();
        this.messageHandlers = new ConcurrentHashMap<>();
        this.deliveryExecutor = Executors.newFixedThreadPool(10);
        this.maintenanceExecutor = Executors.newScheduledThreadPool(2);
        this.globalQueue = new LinkedBlockingQueue<>();
        this.subscriptions = new ConcurrentHashMap<>();
        this.messageCounter = new AtomicLong(0);
        this.router = new MessageRouter();
        this.deliveryTracker = new DeliveryTracker();
        this.running = true;
        
        initialize();
    }
    
    private void initialize() {
        // Start message processing thread
        Thread processingThread = new Thread(this::processMessages);
        processingThread.setName("MessageBus-Processor");
        processingThread.start();
        
        // Schedule maintenance tasks
        maintenanceExecutor.scheduleAtFixedRate(
            this::performMaintenance,
            30, 30, TimeUnit.SECONDS
        );
        
        // Schedule delivery tracking
        maintenanceExecutor.scheduleAtFixedRate(
            this::checkDeliveryStatus,
            10, 10, TimeUnit.SECONDS
        );
    }
    
    /**
     * Register an agent with the message bus
     */
    public void registerAgent(String agentId, AgentRole role, MessageHandler handler) {
        // Create dedicated queue for agent
        MessageQueue queue = new MessageQueue(agentId);
        agentQueues.put(agentId, queue);
        
        // Register role mapping
        roleRegistry.computeIfAbsent(role, k -> ConcurrentHashMap.newKeySet()).add(agentId);
        
        // Register message handler
        messageHandlers.put(agentId, handler);
        
        // Initialize subscription info
        subscriptions.put(agentId, new SubscriptionInfo(agentId, role));
        
        logEvent("Agent registered: " + agentId + " with role: " + role);
    }
    
    /**
     * Unregister an agent from the message bus
     */
    public void unregisterAgent(String agentId) {
        // Remove from role registry
        AgentRole role = subscriptions.get(agentId).getRole();
        if (role != null) {
            Set<String> agents = roleRegistry.get(role);
            if (agents != null) {
                agents.remove(agentId);
            }
        }
        
        // Clean up resources
        MessageQueue queue = agentQueues.remove(agentId);
        if (queue != null) {
            queue.clear();
        }
        messageHandlers.remove(agentId);
        subscriptions.remove(agentId);
        
        logEvent("Agent unregistered: " + agentId);
    }
    
    /**
     * Send a message to a specific agent
     */
    public CompletableFuture<DeliveryStatus> sendMessage(Message message) {
        // Assign message ID
        message.setId(generateMessageId());
        message.setTimestamp(System.currentTimeMillis());
        
        // Track delivery
        CompletableFuture<DeliveryStatus> deliveryFuture = new CompletableFuture<>();
        deliveryTracker.trackMessage(message.getId(), deliveryFuture);
        
        // Route message
        if (message.getRecipient() != null) {
            // Direct message
            routeDirectMessage(message);
        } else {
            // Broadcast or role-based message
            routeBroadcastMessage(message);
        }
        
        return deliveryFuture;
    }
    
    /**
     * Send a message to all agents with a specific role
     */
    public void sendToRole(AgentRole role, Message message) {
        Set<String> agents = roleRegistry.get(role);
        if (agents != null && !agents.isEmpty()) {
            for (String agentId : agents) {
                Message copy = message.copy();
                copy.setRecipient(agentId);
                sendMessage(copy);
            }
        }
    }
    
    /**
     * Broadcast a message to all agents
     */
    public void broadcast(Message message) {
        for (String agentId : agentQueues.keySet()) {
            if (!agentId.equals(message.getSender())) {
                Message copy = message.copy();
                copy.setRecipient(agentId);
                sendMessage(copy);
            }
        }
    }
    
    /**
     * Subscribe to specific message types
     */
    public void subscribe(String agentId, MessageType... types) {
        SubscriptionInfo info = subscriptions.get(agentId);
        if (info != null) {
            info.addSubscriptions(Arrays.asList(types));
        }
    }
    
    /**
     * Unsubscribe from specific message types
     */
    public void unsubscribe(String agentId, MessageType... types) {
        SubscriptionInfo info = subscriptions.get(agentId);
        if (info != null) {
            info.removeSubscriptions(Arrays.asList(types));
        }
    }
    
    private void routeDirectMessage(Message message) {
        MessageQueue queue = agentQueues.get(message.getRecipient());
        if (queue != null) {
            queue.enqueue(message);
            // Process immediately
            deliveryExecutor.submit(() -> deliverMessage(message));
        } else {
            // Handle undeliverable message
            handleUndeliverableMessage(message);
        }
    }
    
    private void routeBroadcastMessage(Message message) {
        // Add to global queue for processing
        try {
            globalQueue.put(message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logError("Failed to queue broadcast message", e);
        }
    }
    
    private void processMessages() {
        while (running) {
            try {
                Message message = globalQueue.poll(100, TimeUnit.MILLISECONDS);
                if (message != null) {
                    processBroadcastMessage(message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    private void processBroadcastMessage(Message message) {
        // Determine recipients based on message type and subscriptions
        Set<String> recipients = determineRecipients(message);
        
        for (String agentId : recipients) {
            Message copy = message.copy();
            copy.setRecipient(agentId);
            
            MessageQueue queue = agentQueues.get(agentId);
            if (queue != null) {
                queue.enqueue(copy);
                deliveryExecutor.submit(() -> deliverMessage(copy));
            }
        }
    }
    
    private void deliverMessage(Message message) {
        try {
            MessageHandler handler = messageHandlers.get(message.getRecipient());
            if (handler != null) {
                // Deliver message
                handler.handleMessage(message);
                
                // Update delivery status
                deliveryTracker.markDelivered(message.getId());
                
                // Update metrics
                updateDeliveryMetrics(message);
            } else {
                deliveryTracker.markFailed(message.getId(), "No handler found");
            }
        } catch (Exception e) {
            logError("Message delivery failed", e);
            deliveryTracker.markFailed(message.getId(), e.getMessage());
            
            // Attempt retry if applicable
            attemptRetry(message);
        }
    }
    
    private void attemptRetry(Message message) {
        if (message.getRetryCount() < 3) {
            message.incrementRetryCount();
            
            // Schedule retry with exponential backoff
            long delay = (long) Math.pow(2, message.getRetryCount()) * 1000;
            maintenanceExecutor.schedule(
                () -> routeDirectMessage(message),
                delay,
                TimeUnit.MILLISECONDS
            );
        } else {
            // Max retries reached
            handleFailedMessage(message);
        }
    }
    
    private Set<String> determineRecipients(Message message) {
        Set<String> recipients = new HashSet<>();
        
        // Check subscriptions
        for (Map.Entry<String, SubscriptionInfo> entry : subscriptions.entrySet()) {
            SubscriptionInfo info = entry.getValue();
            if (info.isSubscribedTo(message.getType()) && !entry.getKey().equals(message.getSender())) {
                recipients.add(entry.getKey());
            }
        }
        
        return recipients;
    }
    
    private void performMaintenance() {
        // Clean up old messages
        for (MessageQueue queue : agentQueues.values()) {
            queue.removeExpiredMessages();
        }
        
        // Check queue health
        checkQueueHealth();
        
        // Generate statistics
        generateStatistics();
    }
    
    private void checkDeliveryStatus() {
        deliveryTracker.checkTimeouts();
    }
    
    private void checkQueueHealth() {
        for (Map.Entry<String, MessageQueue> entry : agentQueues.entrySet()) {
            MessageQueue queue = entry.getValue();
            if (queue.size() > 1000) {
                logWarning("Queue overflow warning for agent: " + entry.getKey());
            }
        }
    }
    
    private void generateStatistics() {
        long totalMessages = messageCounter.get();
        long deliveredMessages = deliveryTracker.getDeliveredCount();
        long failedMessages = deliveryTracker.getFailedCount();
        
        logEvent(String.format("MessageBus Stats - Total: %d, Delivered: %d, Failed: %d",
            totalMessages, deliveredMessages, failedMessages));
    }
    
    private String generateMessageId() {
        return "MSG-" + System.currentTimeMillis() + "-" + messageCounter.incrementAndGet();
    }
    
    private void handleUndeliverableMessage(Message message) {
        logError("Undeliverable message: " + message.getId() + " to " + message.getRecipient(), null);
        deliveryTracker.markFailed(message.getId(), "Recipient not found");
    }
    
    private void handleFailedMessage(Message message) {
        logError("Message delivery failed after retries: " + message.getId(), null);
        // Could implement dead letter queue here
    }
    
    private void updateDeliveryMetrics(Message message) {
        // Update delivery metrics for monitoring
    }
    
    public void shutdown() {
        running = false;
        
        deliveryExecutor.shutdown();
        maintenanceExecutor.shutdown();
        
        try {
            if (!deliveryExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                deliveryExecutor.shutdownNow();
            }
            if (!maintenanceExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                maintenanceExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            deliveryExecutor.shutdownNow();
            maintenanceExecutor.shutdownNow();
        }
    }
    
    // Inner classes
    private static class MessageQueue {
        private final String agentId;
        private final BlockingQueue<Message> queue;
        private final long maxAge = TimeUnit.MINUTES.toMillis(5);
        
        public MessageQueue(String agentId) {
            this.agentId = agentId;
            this.queue = new LinkedBlockingQueue<>();
        }
        
        public void enqueue(Message message) {
            queue.offer(message);
        }
        
        public Message dequeue() throws InterruptedException {
            return queue.take();
        }
        
        public int size() {
            return queue.size();
        }
        
        public void clear() {
            queue.clear();
        }
        
        public void removeExpiredMessages() {
            long now = System.currentTimeMillis();
            queue.removeIf(msg -> now - msg.getTimestamp() > maxAge);
        }
    }
    
    private static class SubscriptionInfo {
        private final String agentId;
        private final AgentRole role;
        private final Set<MessageType> subscriptions;
        
        public SubscriptionInfo(String agentId, AgentRole role) {
            this.agentId = agentId;
            this.role = role;
            this.subscriptions = ConcurrentHashMap.newKeySet();
            
            // Default subscriptions based on role
            initializeDefaultSubscriptions();
        }
        
        private void initializeDefaultSubscriptions() {
            // Add default subscriptions based on role
            subscriptions.add(MessageType.SYSTEM_BROADCAST);
            subscriptions.add(MessageType.EMERGENCY_STOP);
        }
        
        public void addSubscriptions(List<MessageType> types) {
            subscriptions.addAll(types);
        }
        
        public void removeSubscriptions(List<MessageType> types) {
            subscriptions.removeAll(types);
        }
        
        public boolean isSubscribedTo(MessageType type) {
            return subscriptions.contains(type);
        }
        
        public AgentRole getRole() {
            return role;
        }
    }
    
    private static class MessageRouter {
        // Additional routing logic can be implemented here
    }
    
    private static class DeliveryTracker {
        private final Map<String, CompletableFuture<DeliveryStatus>> pendingDeliveries;
        private final AtomicLong deliveredCount;
        private final AtomicLong failedCount;
        
        public DeliveryTracker() {
            this.pendingDeliveries = new ConcurrentHashMap<>();
            this.deliveredCount = new AtomicLong(0);
            this.failedCount = new AtomicLong(0);
        }
        
        public void trackMessage(String messageId, CompletableFuture<DeliveryStatus> future) {
            pendingDeliveries.put(messageId, future);
        }
        
        public void markDelivered(String messageId) {
            CompletableFuture<DeliveryStatus> future = pendingDeliveries.remove(messageId);
            if (future != null) {
                future.complete(DeliveryStatus.DELIVERED);
                deliveredCount.incrementAndGet();
            }
        }
        
        public void markFailed(String messageId, String reason) {
            CompletableFuture<DeliveryStatus> future = pendingDeliveries.remove(messageId);
            if (future != null) {
                future.complete(DeliveryStatus.FAILED);
                failedCount.incrementAndGet();
            }
        }
        
        public void checkTimeouts() {
            long now = System.currentTimeMillis();
            pendingDeliveries.entrySet().removeIf(entry -> {
                // Timeout after 30 seconds
                if (now - extractTimestamp(entry.getKey()) > 30000) {
                    entry.getValue().complete(DeliveryStatus.TIMEOUT);
                    failedCount.incrementAndGet();
                    return true;
                }
                return false;
            });
        }
        
        private long extractTimestamp(String messageId) {
            try {
                String[] parts = messageId.split("-");
                return Long.parseLong(parts[1]);
            } catch (Exception e) {
                return 0;
            }
        }
        
        public long getDeliveredCount() {
            return deliveredCount.get();
        }
        
        public long getFailedCount() {
            return failedCount.get();
        }
    }
    
    public enum DeliveryStatus {
        DELIVERED, FAILED, TIMEOUT
    }
    
    // Logging methods
    private void logEvent(String message) {
        System.out.println("[MessageBus] " + message);
    }
    
    private void logWarning(String message) {
        System.out.println("[MessageBus-WARN] " + message);
    }
    
    private void logError(String message, Exception e) {
        System.err.println("[MessageBus-ERROR] " + message);
        if (e != null) {
            e.printStackTrace();
        }
    }
}