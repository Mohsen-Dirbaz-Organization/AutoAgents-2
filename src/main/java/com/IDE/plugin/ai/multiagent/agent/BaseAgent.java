package com.IDE.plugin.ai.multiagent.agent;

import com.IDE.plugin.ai.multiagent.model.*;
import com.IDE.plugin.ai.multiagent.context.SharedContext;
import com.IDE.plugin.ai.multiagent.monitor.PerformanceMetrics;
import com.IDE.plugin.ai.multiagent.event.AgentEvent;
import com.IDE.plugin.ai.multiagent.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Base implementation of an autonomous agent with core functionality
 * for task execution, state management, and inter-agent communication.
 */
public abstract class BaseAgent implements Agent {
    private static final Logger logger = LoggerFactory.getLogger(BaseAgent.class);

    protected final String agentId;
    protected final AgentRole role;
    protected final AtomicReference<AgentState> state;
    protected final SharedContext sharedContext;
    protected final EventBus eventBus;
    protected final ExecutorService executor;
    protected final BlockingQueue<Task> taskQueue;
    protected final Map<String, AgentCapability> capabilities;
    protected final Set<String> dependencies;
    protected final PerformanceMetrics metrics;
    protected final List<TaskResult> completedTasks;
    protected final Map<String, Object> configuration;
    
    // Lifecycle management
    protected volatile boolean running;
    protected final Object lifecycleLock = new Object();
    protected LocalDateTime startTime;
    protected LocalDateTime lastActivityTime;
    
    // Communication channels
    protected final Map<String, MessageChannel> messageChannels;
    protected final BlockingQueue<AgentMessage> incomingMessages;
    
    // Resource management
    protected final Semaphore resourceLimiter;
    protected volatile int activeTaskCount;
    protected final int maxConcurrentTasks;

    public BaseAgent(String agentId, AgentRole role, SharedContext sharedContext, EventBus eventBus) {
        this.agentId = agentId;
        this.role = role;
        this.state = new AtomicReference<>(AgentState.INITIALIZING);
        this.sharedContext = sharedContext;
        this.eventBus = eventBus;
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setName("Agent-" + agentId + "-Worker-" + t.getId());
            return t;
        });
        this.taskQueue = new LinkedBlockingQueue<>();
        this.capabilities = new ConcurrentHashMap<>();
        this.dependencies = ConcurrentHashMap.newKeySet();
        this.metrics = new PerformanceMetrics(agentId);
        this.completedTasks = new CopyOnWriteArrayList<>();
        this.configuration = new ConcurrentHashMap<>();
        this.messageChannels = new ConcurrentHashMap<>();
        this.incomingMessages = new LinkedBlockingQueue<>();
        this.maxConcurrentTasks = determineMaxConcurrentTasks();
        this.resourceLimiter = new Semaphore(maxConcurrentTasks);
        
        initializeCapabilities();
        subscribeToEvents();
    }

    protected abstract void initializeCapabilities();
    
    protected abstract TaskResult executeTaskInternal(Task task) throws Exception;

    @Override
    public void start() {
        synchronized (lifecycleLock) {
            if (running) {
                return;
            }
            
            logger.info("Starting agent: {}", agentId);
            running = true;
            startTime = LocalDateTime.now();
            lastActivityTime = startTime;
            state.set(AgentState.IDLE);
            
            // Start message processor
            executor.submit(this::processMessages);
            
            // Start task processor
            executor.submit(this::processTasks);
            
            // Publish start event
            eventBus.publish(new AgentEvent(
                AgentEvent.Type.AGENT_STARTED,
                agentId,
                Map.of("role", role.name(), "capabilities", capabilities.keySet())
            ));
        }
    }

    @Override
    public void stop() {
        synchronized (lifecycleLock) {
            if (!running) {
                return;
            }
            
            logger.info("Stopping agent: {}", agentId);
            running = false;
            state.set(AgentState.STOPPED);
            
            // Cancel pending tasks
            taskQueue.forEach(task -> {
                task.updateStatus(TaskStatus.CANCELLED);
                eventBus.publish(new AgentEvent(
                    AgentEvent.Type.TASK_CANCELLED,
                    agentId,
                    Map.of("taskId", task.getId(), "reason", "Agent shutdown")
                ));
            });
            taskQueue.clear();
            
            // Shutdown executor
            executor.shutdown();
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            // Publish stop event
            eventBus.publish(new AgentEvent(
                AgentEvent.Type.AGENT_STOPPED,
                agentId,
                Map.of("uptime", java.time.Duration.between(startTime, LocalDateTime.now()).toSeconds())
            ));
        }
    }

    @Override
    public CompletableFuture<TaskResult> executeTask(Task task) {
        if (!running) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("Agent is not running")
            );
        }
        
        // Validate task
        if (!canExecuteTask(task)) {
            return CompletableFuture.failedFuture(
                new UnsupportedOperationException("Agent cannot execute task type: " + task.getType())
            );
        }
        
        // Create task future
        CompletableFuture<TaskResult> future = new CompletableFuture<>();
        
        // Submit task
        taskQueue.offer(new TaskWrapper(task, future));
        metrics.recordTaskQueued();
        
        // Publish event
        eventBus.publish(new AgentEvent(
            AgentEvent.Type.TASK_SUBMITTED,
            agentId,
            Map.of("taskId", task.getId(), "type", task.getType())
        ));
        
        return future;
    }

    @Override
    public boolean canExecuteTask(Task task) {
        return capabilities.containsKey(task.getType()) &&
               state.get() != AgentState.STOPPED &&
               activeTaskCount < maxConcurrentTasks;
    }

    @Override
    public void sendMessage(String targetAgentId, String content, Map<String, Object> metadata) {
        AgentMessage message = new AgentMessage(
            UUID.randomUUID().toString(),
            agentId,
            targetAgentId,
            content,
            metadata,
            LocalDateTime.now()
        );
        
        MessageChannel channel = messageChannels.get(targetAgentId);
        if (channel != null) {
            channel.send(message);
        } else {
            // Use event bus for broadcast
            eventBus.publish(new AgentEvent(
                AgentEvent.Type.MESSAGE_SENT,
                agentId,
                Map.of("message", message)
            ));
        }
        
        metrics.recordMessageSent();
    }

    @Override
    public void receiveMessage(AgentMessage message) {
        if (!running) {
            return;
        }
        
        incomingMessages.offer(message);
        metrics.recordMessageReceived();
        
        eventBus.publish(new AgentEvent(
            AgentEvent.Type.MESSAGE_RECEIVED,
            agentId,
            Map.of("messageId", message.getId(), "from", message.getSenderId())
        ));
    }

    protected void processTasks() {
        while (running) {
            try {
                TaskWrapper wrapper = (TaskWrapper) taskQueue.poll(1, TimeUnit.SECONDS);
                if (wrapper != null) {
                    processTask(wrapper);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error processing task", e);
            }
        }
    }

    protected void processTask(TaskWrapper wrapper) {
        Task task = wrapper.task;
        CompletableFuture<TaskResult> future = wrapper.future;
        
        try {
            // Acquire resource permit
            if (!resourceLimiter.tryAcquire(5, TimeUnit.SECONDS)) {
                future.completeExceptionally(
                    new TimeoutException("Could not acquire resource permit")
                );
                return;
            }
            
            activeTaskCount++;
            state.set(AgentState.EXECUTING);
            task.updateStatus(TaskStatus.IN_PROGRESS);
            lastActivityTime = LocalDateTime.now();
            
            // Execute task
            long startTime = System.currentTimeMillis();
            TaskResult result = executeTaskInternal(task);
            long duration = System.currentTimeMillis() - startTime;
            
            // Update metrics
            metrics.recordTaskCompleted(duration);
            completedTasks.add(result);
            
            // Complete future
            future.complete(result);
            
            // Publish completion event
            eventBus.publish(new AgentEvent(
                AgentEvent.Type.TASK_COMPLETED,
                agentId,
                Map.of("taskId", task.getId(), "duration", duration, "success", result.isSuccess())
            ));
            
        } catch (Exception e) {
            logger.error("Task execution failed", e);
            metrics.recordTaskFailed();
            
            TaskResult failureResult = new TaskResult(
                task.getId(),
                false,
                null,
                e.getMessage(),
                Map.of("exception", e.getClass().getName())
            );
            
            future.complete(failureResult);
            
            eventBus.publish(new AgentEvent(
                AgentEvent.Type.TASK_FAILED,
                agentId,
                Map.of("taskId", task.getId(), "error", e.getMessage())
            ));
            
        } finally {
            activeTaskCount--;
            resourceLimiter.release();
            if (activeTaskCount == 0) {
                state.set(AgentState.IDLE);
            }
        }
    }

    protected void processMessages() {
        while (running) {
            try {
                AgentMessage message = incomingMessages.poll(1, TimeUnit.SECONDS);
                if (message != null) {
                    handleMessage(message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error processing message", e);
            }
        }
    }

    protected abstract void handleMessage(AgentMessage message);

    protected void subscribeToEvents() {
        // Subscribe to relevant events
        eventBus.subscribe(AgentEvent.Type.BROADCAST_MESSAGE, event -> {
            if (!event.getAgentId().equals(agentId)) {
                Map<String, Object> data = event.getData();
                if (data.containsKey("message")) {
                    AgentMessage message = (AgentMessage) data.get("message");
                    if (message.getTargetId().equals(agentId) || message.getTargetId().equals("*")) {
                        receiveMessage(message);
                    }
                }
            }
        });
    }

    protected int determineMaxConcurrentTasks() {
        // Default implementation - can be overridden
        return Runtime.getRuntime().availableProcessors() * 2;
    }

    // Getters
    @Override
    public String getId() {
        return agentId;
    }

    @Override
    public AgentRole getRole() {
        return role;
    }

    @Override
    public AgentState getState() {
        return state.get();
    }

    @Override
    public Set<String> getCapabilities() {
        return new HashSet<>(capabilities.keySet());
    }

    @Override
    public PerformanceMetrics getMetrics() {
        return metrics;
    }

    @Override
    public List<TaskResult> getCompletedTasks() {
        return new ArrayList<>(completedTasks);
    }

    // Inner classes
    protected static class TaskWrapper {
        final Task task;
        final CompletableFuture<TaskResult> future;

        TaskWrapper(Task task, CompletableFuture<TaskResult> future) {
            this.task = task;
            this.future = future;
        }
    }

    protected static class MessageChannel {
        private final BlockingQueue<AgentMessage> queue = new LinkedBlockingQueue<>();

        public void send(AgentMessage message) {
            queue.offer(message);
        }

        public AgentMessage receive() throws InterruptedException {
            return queue.take();
        }

        public AgentMessage poll(long timeout, TimeUnit unit) throws InterruptedException {
            return queue.poll(timeout, unit);
        }
    }
}