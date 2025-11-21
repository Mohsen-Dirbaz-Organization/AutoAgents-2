package com.IDE.plugin.ai.services;

import com.IDE.plugin.trust.TrustVerificationService;
import com.IDE.plugin.messaging.MessageBus;
import com.IDE.plugin.messaging.Message;
import com.IDE.plugin.messaging.MessageType;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Service for integrating Claude Code capabilities into the multi-agent system.
 * Manages API interactions, task routing, and response coordination.
 */
@Service
public final class ClaudeCodeIntegrationService {
    private static final Logger LOG = Logger.getInstance(ClaudeCodeIntegrationService.class);
    private static final String SERVICE_ID = "claude-code-integration";
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;
    
    private final Project project;
    private final MessageBus messageBus;
    private final TrustVerificationService trustService;
    private final ClaudeCodeBridge claudeBridge;
    private final ClaudeTaskAdapter taskAdapter;
    private final ClaudeResponseHandler responseHandler;
    
    private final ExecutorService executorService;
    private final Map<String, CompletableFuture<ClaudeResponse>> pendingRequests;
    private final Map<String, TaskContext> activeContexts;
    private volatile boolean initialized = false;
    
    /**
     * Task context for tracking ongoing Claude operations
     */
    private static class TaskContext {
        final String taskId;
        final String agentId;
        final long startTime;
        final Map<String, Object> metadata;
        volatile TaskStatus status;
        
        TaskContext(String taskId, String agentId) {
            this.taskId = taskId;
            this.agentId = agentId;
            this.startTime = System.currentTimeMillis();
            this.metadata = new ConcurrentHashMap<>();
            this.status = TaskStatus.PENDING;
        }
    }
    
    private enum TaskStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
    }
    
    public ClaudeCodeIntegrationService(@NotNull Project project) {
        this.project = project;
        this.messageBus = project.getService(MessageBus.class);
        this.trustService = project.getService(TrustVerificationService.class);
        this.claudeBridge = new ClaudeCodeBridge(project);
        this.taskAdapter = new ClaudeTaskAdapter(project);
        this.responseHandler = new ClaudeResponseHandler(project);
        
        this.executorService = Executors.newFixedThreadPool(4, r -> {
            Thread thread = new Thread(r, "ClaudeCode-Worker");
            thread.setDaemon(true);
            return thread;
        });
        
        this.pendingRequests = new ConcurrentHashMap<>();
        this.activeContexts = new ConcurrentHashMap<>();
    }
    
    /**
     * Initialize the Claude Code integration service
     */
    public void initialize() {
        if (initialized) {
            return;
        }
        
        try {
            LOG.info("Initializing Claude Code Integration Service");
            
            // Verify trust for Claude integration
            if (!trustService.verifyComponentTrust(SERVICE_ID)) {
                throw new SecurityException("Trust verification failed for Claude Code integration");
            }
            
            // Initialize components
            claudeBridge.initialize();
            taskAdapter.initialize();
            responseHandler.initialize();
            
            // Register message listeners
            registerMessageListeners();
            
            // Send initialization complete message
            messageBus.publish(new Message(
                MessageType.SYSTEM,
                SERVICE_ID,
                "system",
                Map.of("event", "claude_initialized", "timestamp", System.currentTimeMillis())
            ));
            
            initialized = true;
            LOG.info("Claude Code Integration Service initialized successfully");
            
        } catch (Exception e) {
            LOG.error("Failed to initialize Claude Code integration", e);
            showNotification("Failed to initialize Claude Code integration: " + e.getMessage(), 
                           NotificationType.ERROR);
            throw new RuntimeException("Claude Code initialization failed", e);
        }
    }
    
    /**
     * Submit a task to Claude Code for processing
     */
    public CompletableFuture<ClaudeResponse> submitTask(@NotNull String taskId, 
                                                        @NotNull String agentId,
                                                        @NotNull Map<String, Object> taskData) {
        if (!initialized) {
            initialize();
        }
        
        // Create task context
        TaskContext context = new TaskContext(taskId, agentId);
        activeContexts.put(taskId, context);
        
        // Create future for tracking
        CompletableFuture<ClaudeResponse> future = new CompletableFuture<>();
        pendingRequests.put(taskId, future);
        
        // Submit task asynchronously
        executorService.submit(() -> processTask(context, taskData, future));
        
        return future;
    }
    
    /**
     * Process a task through Claude Code
     */
    private void processTask(TaskContext context, Map<String, Object> taskData, 
                           CompletableFuture<ClaudeResponse> future) {
        try {
            context.status = TaskStatus.PROCESSING;
            
            // Adapt task for Claude
            ClaudeRequest request = taskAdapter.adaptTask(context.taskId, taskData);
            
            // Add security signature
            request.addSecurityHeader(generateSecuritySignature(request));
            
            // Send to Claude with retry logic
            ClaudeResponse response = executeWithRetry(() -> 
                claudeBridge.sendRequest(request), MAX_RETRY_ATTEMPTS);
            
            // Process response
            response = responseHandler.processResponse(response, context);
            
            // Update context
            context.status = TaskStatus.COMPLETED;
            
            // Complete future
            future.complete(response);
            
            // Publish completion message
            publishTaskCompletion(context, response);
            
        } catch (Exception e) {
            LOG.error("Failed to process Claude task: " + context.taskId, e);
            context.status = TaskStatus.FAILED;
            future.completeExceptionally(e);
            publishTaskFailure(context, e);
        } finally {
            activeContexts.remove(context.taskId);
            pendingRequests.remove(context.taskId);
        }
    }
    
    /**
     * Execute operation with retry logic
     */
    private <T> T executeWithRetry(Callable<T> operation, int maxAttempts) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                
                if (attempt < maxAttempts) {
                    LOG.warn("Attempt " + attempt + " failed, retrying...", e);
                    Thread.sleep(RETRY_DELAY_MS * attempt);
                }
            }
        }
        
        throw new RuntimeException("All retry attempts exhausted", lastException);
    }
    
    /**
     * Cancel an active task
     */
    public boolean cancelTask(@NotNull String taskId) {
        TaskContext context = activeContexts.get(taskId);
        if (context == null) {
            return false;
        }
        
        context.status = TaskStatus.CANCELLED;
        
        CompletableFuture<ClaudeResponse> future = pendingRequests.get(taskId);
        if (future != null && !future.isDone()) {
            future.cancel(true);
        }
        
        claudeBridge.cancelRequest(taskId);
        
        publishTaskCancellation(context);
        
        activeContexts.remove(taskId);
        pendingRequests.remove(taskId);
        
        return true;
    }
    
    /**
     * Get status of an active task
     */
    @Nullable
    public TaskStatus getTaskStatus(@NotNull String taskId) {
        TaskContext context = activeContexts.get(taskId);
        return context != null ? context.status : null;
    }
    
    /**
     * Register message bus listeners
     */
    private void registerMessageListeners() {
        messageBus.subscribe(MessageType.TASK_REQUEST, message -> {
            if ("claude_task".equals(message.getMetadata().get("type"))) {
                String taskId = (String) message.getMetadata().get("task_id");
                submitTask(taskId, message.getSender(), message.getMetadata());
            }
        });
        
        messageBus.subscribe(MessageType.TASK_CANCEL, message -> {
            String taskId = (String) message.getMetadata().get("task_id");
            if (taskId != null) {
                cancelTask(taskId);
            }
        });
    }
    
    /**
     * Generate security signature for request
     */
    private String generateSecuritySignature(ClaudeRequest request) {
        try {
            String data = request.getTaskId() + request.getTimestamp() + request.getContent();
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(getSecurityKey(), "HmacSHA256"));
            byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to generate security signature", e);
        }
    }
    
    /**
     * Get security key for signing
     */
    private byte[] getSecurityKey() {
        return trustService.getComponentKey(SERVICE_ID);
    }
    
    /**
     * Publish task completion message
     */
    private void publishTaskCompletion(TaskContext context, ClaudeResponse response) {
        messageBus.publish(new Message(
            MessageType.TASK_COMPLETE,
            SERVICE_ID,
            context.agentId,
            Map.of(
                "task_id", context.taskId,
                "duration_ms", System.currentTimeMillis() - context.startTime,
                "response_id", response.getId(),
                "success", true
            )
        ));
    }
    
    /**
     * Publish task failure message
     */
    private void publishTaskFailure(TaskContext context, Exception error) {
        messageBus.publish(new Message(
            MessageType.TASK_FAILED,
            SERVICE_ID,
            context.agentId,
            Map.of(
                "task_id", context.taskId,
                "duration_ms", System.currentTimeMillis() - context.startTime,
                "error", error.getMessage(),
                "error_type", error.getClass().getSimpleName()
            )
        ));
    }
    
    /**
     * Publish task cancellation message
     */
    private void publishTaskCancellation(TaskContext context) {
        messageBus.publish(new Message(
            MessageType.TASK_CANCEL,
            SERVICE_ID,
            context.agentId,
            Map.of(
                "task_id", context.taskId,
                "duration_ms", System.currentTimeMillis() - context.startTime,
                "cancelled", true
            )
        ));
    }
    
    /**
     * Show notification to user
     */
    private void showNotification(String content, NotificationType type) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Claude Code Integration")
            .createNotification(content, type)
            .notify(project);
    }
    
    /**
     * Shutdown the service
     */
    public void shutdown() {
        LOG.info("Shutting down Claude Code Integration Service");
        
        // Cancel all pending requests
        pendingRequests.values().forEach(future -> future.cancel(true));
        
        // Shutdown executor
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // Cleanup components
        claudeBridge.shutdown();
        
        initialized = false;
    }
    
    /**
     * Get service statistics
     */
    public Map<String, Object> getStatistics() {
        return Map.of(
            "active_tasks", activeContexts.size(),
            "pending_requests", pendingRequests.size(),
            "initialized", initialized,
            "service_id", SERVICE_ID
        );
    }
}