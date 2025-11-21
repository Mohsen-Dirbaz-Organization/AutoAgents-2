package com.IDE.plugin.ai.multiagent.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a task that can be executed by an agent.
 */
public class Task {
    private final String taskId;
    private final String type;
    private final String description;
    private final Map<String, Object> parameters;
    private final String requesterAgentId;
    private final LocalDateTime creationTime;
    private final int priority;
    private TaskStatus status;
    private LocalDateTime startTime;
    private LocalDateTime completionTime;
    
    /**
     * Creates a new task with the specified parameters.
     * 
     * @param type The type of task
     * @param description A description of the task
     * @param parameters Parameters for the task execution
     * @param requesterAgentId The ID of the agent requesting the task
     * @param priority The priority of the task (higher values indicate higher priority)
     */
    public Task(String type, String description, Map<String, Object> parameters, 
                String requesterAgentId, int priority) {
        this.taskId = UUID.randomUUID().toString();
        this.type = type;
        this.description = description;
        this.parameters = parameters != null ? new HashMap<>(parameters) : new HashMap<>();
        this.requesterAgentId = requesterAgentId;
        this.creationTime = LocalDateTime.now();
        this.priority = priority;
        this.status = TaskStatus.CREATED;
    }
    
    /**
     * Returns the unique identifier for this task.
     */
    public String getTaskId() {
        return taskId;
    }
    
    /**
     * Returns the type of this task.
     */
    public String getType() {
        return type;
    }
    
    /**
     * Returns the description of this task.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Returns the parameters for this task.
     */
    public Map<String, Object> getParameters() {
        return new HashMap<>(parameters);
    }
    
    /**
     * Returns the ID of the agent that requested this task.
     */
    public String getRequesterAgentId() {
        return requesterAgentId;
    }
    
    /**
     * Returns the time when this task was created.
     */
    public LocalDateTime getCreationTime() {
        return creationTime;
    }
    
    /**
     * Returns the priority of this task.
     */
    public int getPriority() {
        return priority;
    }
    
    /**
     * Returns the current status of this task.
     */
    public TaskStatus getStatus() {
        return status;
    }
    
    /**
     * Sets the status of this task.
     * 
     * @param status The new status
     */
    public void setStatus(TaskStatus status) {
        this.status = status;
        
        if (status == TaskStatus.IN_PROGRESS && startTime == null) {
            startTime = LocalDateTime.now();
        } else if (status == TaskStatus.COMPLETED || status == TaskStatus.FAILED) {
            completionTime = LocalDateTime.now();
        }
    }
    
    /**
     * Returns the time when this task was started, or null if not started.
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    /**
     * Returns the time when this task was completed, or null if not completed.
     */
    public LocalDateTime getCompletionTime() {
        return completionTime;
    }
    
    /**
     * Enum representing the possible states of a task.
     */
    public enum TaskStatus {
        CREATED,
        QUEUED,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        CANCELLED
    }
}