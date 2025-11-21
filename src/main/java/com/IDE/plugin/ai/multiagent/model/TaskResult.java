package com.IDE.plugin.ai.multiagent.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.IDE.plugin.ai.multiagent.model.Task.TaskStatus;

/**
 * Represents the result of a task execution.
 */
public class TaskResult {
    private final String taskId;
    private final String executorAgentId;
    private final TaskStatus status;
    private final Object result;
    private final Map<String, Object> metadata;
    private final LocalDateTime completionTime;
    private final long executionTimeMs;
    private final String errorMessage;

    /**
     * Creates a successful task result.
     * 
     * @param taskId The ID of the completed task
     * @param executorAgentId The ID of the agent that executed the task
     * @param result The result of the task execution
     * @param metadata Additional metadata about the execution
     * @param executionTimeMs The time taken to execute the task in milliseconds
     * @return A new TaskResult instance
     */
    public static TaskResult success(String taskId, String executorAgentId, Object result, 
                                    Map<String, Object> metadata, long executionTimeMs) {
        return new TaskResult(taskId, executorAgentId, TaskStatus.COMPLETED, result, 
                             metadata, executionTimeMs, null);
    }

    /**
     * Creates a failed task result.
     * 
     * @param taskId The ID of the failed task
     * @param executorAgentId The ID of the agent that attempted to execute the task
     * @param errorMessage The error message describing the failure
     * @param metadata Additional metadata about the execution
     * @param executionTimeMs The time taken before the task failed in milliseconds
     * @return A new TaskResult instance
     */
    public static TaskResult failure(String taskId, String executorAgentId, String errorMessage, 
                                    Map<String, Object> metadata, long executionTimeMs) {
        return new TaskResult(taskId, executorAgentId, TaskStatus.FAILED, null, 
                             metadata, executionTimeMs, errorMessage);
    }

    /**
     * Creates a cancelled task result.
     * 
     * @param taskId The ID of the cancelled task
     * @param executorAgentId The ID of the agent that was executing the task
     * @param metadata Additional metadata about the execution
     * @param executionTimeMs The time taken before the task was cancelled in milliseconds
     * @return A new TaskResult instance
     */
    public static TaskResult cancelled(String taskId, String executorAgentId, 
                                      Map<String, Object> metadata, long executionTimeMs) {
        return new TaskResult(taskId, executorAgentId, TaskStatus.CANCELLED, null, 
                             metadata, executionTimeMs, "Task was cancelled");
    }

    private TaskResult(String taskId, String executorAgentId, TaskStatus status, Object result, 
                      Map<String, Object> metadata, long executionTimeMs, String errorMessage) {
        this.taskId = taskId;
        this.executorAgentId = executorAgentId;
        this.status = status;
        this.result = result;
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.completionTime = LocalDateTime.now();
        this.executionTimeMs = executionTimeMs;
        this.errorMessage = errorMessage;
    }

    /**
     * Returns the ID of the task.
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * Returns the ID of the agent that executed the task.
     */
    public String getExecutorAgentId() {
        return executorAgentId;
    }

    /**
     * Returns the status of the task execution.
     */
    public TaskStatus getStatus() {
        return status;
    }

    /**
     * Returns the result of the task execution, or null if the task failed or was cancelled.
     */
    public Object getResult() {
        return result;
    }

    /**
     * Returns additional metadata about the task execution.
     */
    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }

    /**
     * Returns the time when the task execution was completed.
     */
    public LocalDateTime getCompletionTime() {
        return completionTime;
    }

    /**
     * Returns the time taken to execute the task in milliseconds.
     */
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    /**
     * Returns the error message if the task failed, or null if the task succeeded.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Returns whether the task execution was successful.
     */
    public boolean isSuccess() {
        return status == TaskStatus.COMPLETED;
    }
}
