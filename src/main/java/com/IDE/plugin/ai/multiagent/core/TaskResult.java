package com.IDE.plugin.ai.multiagent.core;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the result of a task execution by an agent.
 */
public class TaskResult {
    private final String taskId;
    private final String agentId;
    private final boolean success;
    private final Object result;
    private final String errorMessage;
    private final Instant completedAt;
    private final Map<String, Object> metadata;
    
    private TaskResult(Builder builder) {
        this.taskId = builder.taskId;
        this.agentId = builder.agentId;
        this.success = builder.success;
        this.result = builder.result;
        this.errorMessage = builder.errorMessage;
        this.completedAt = builder.completedAt != null ? builder.completedAt : Instant.now();
        this.metadata = builder.metadata != null ? builder.metadata : Map.of();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public String getTaskId() {
        return taskId;
    }
    
    public String getAgentId() {
        return agentId;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public Optional<Object> getResult() {
        return Optional.ofNullable(result);
    }
    
    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }
    
    public Instant getCompletedAt() {
        return completedAt;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public static class Builder {
        private String taskId;
        private String agentId;
        private boolean success;
        private Object result;
        private String errorMessage;
        private Instant completedAt;
        private Map<String, Object> metadata;
        
        public Builder taskId(String taskId) {
            this.taskId = taskId;
            return this;
        }
        
        public Builder agentId(String agentId) {
            this.agentId = agentId;
            return this;
        }
        
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public Builder result(Object result) {
            this.result = result;
            return this;
        }
        
        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }
        
        public Builder completedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public TaskResult build() {
            return new TaskResult(this);
        }
    }
}