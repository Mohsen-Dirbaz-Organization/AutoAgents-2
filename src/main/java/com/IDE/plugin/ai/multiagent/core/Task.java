package com.IDE.plugin.ai.multiagent.core;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a task to be executed by an agent
 */
public class Task {
    private final String id;
    private final String type;
    private final Object input;
    private final Instant createdAt;
    private volatile TaskStatus status;
    private volatile Object result;
    private volatile Exception error;
    private volatile Instant startedAt;
    private volatile Instant completedAt;
    private final CompletableFuture<Object> future;
    
    public Task(String type, Object input) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.input = input;
        this.createdAt = Instant.now();
        this.status = TaskStatus.PENDING;
        this.future = new CompletableFuture<>();
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getType() {
        return type;
    }
    
    public Object getInput() {
        return input;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public TaskStatus getStatus() {
        return status;
    }
    
    public Object getResult() {
        return result;
    }
    
    public Exception getError() {
        return error;
    }
    
    public Instant getStartedAt() {
        return startedAt;
    }
    
    public Instant getCompletedAt() {
        return completedAt;
    }
    
    public CompletableFuture<Object> getFuture() {
        return future;
    }
    
    // Status updates
    public void start() {
        this.status = TaskStatus.RUNNING;
        this.startedAt = Instant.now();
    }
    
    public void complete(Object result) {
        this.status = TaskStatus.COMPLETED;
        this.result = result;
        this.completedAt = Instant.now();
        this.future.complete(result);
    }
    
    public void fail(Exception error) {
        this.status = TaskStatus.FAILED;
        this.error = error;
        this.completedAt = Instant.now();
        this.future.completeExceptionally(error);
    }
    
    public void cancel() {
        this.status = TaskStatus.CANCELLED;
        this.completedAt = Instant.now();
        this.future.cancel(true);
    }
    
    /**
     * Task status enum
     */
    public enum TaskStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }
}