package com.IDE.plugin.ai.multiagent.core;

import java.util.concurrent.*;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Schedules and manages task execution
 */
public class TaskScheduler {
    private final ScheduledExecutorService scheduler;
    private final ExecutorService executor;
    private final Map<String, ScheduledFuture<?>> scheduledTasks;
    private final BlockingQueue<Task> taskQueue;
    private volatile boolean running;
    
    public TaskScheduler(int corePoolSize) {
        this.scheduler = Executors.newScheduledThreadPool(corePoolSize);
        this.executor = Executors.newFixedThreadPool(corePoolSize * 2);
        this.scheduledTasks = new ConcurrentHashMap<>();
        this.taskQueue = new LinkedBlockingQueue<>();
        this.running = true;
        startTaskProcessor();
    }
    
    /**
     * Submits a task for immediate execution
     */
    public CompletableFuture<Object> submit(Task task) {
        if (!running) {
            task.fail(new IllegalStateException("Scheduler is shut down"));
            return task.getFuture();
        }
        
        try {
            taskQueue.offer(task);
        } catch (Exception e) {
            task.fail(e);
        }
        
        return task.getFuture();
    }
    
    /**
     * Schedules a task for delayed execution
     */
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        if (!running) {
            throw new IllegalStateException("Scheduler is shut down");
        }
        return scheduler.schedule(task, delay, unit);
    }
    
    /**
     * Schedules a task for periodic execution
     */
    public ScheduledFuture<?> scheduleAtFixedRate(String taskId, Runnable task, 
                                                  long initialDelay, long period, TimeUnit unit) {
        if (!running) {
            throw new IllegalStateException("Scheduler is shut down");
        }
        
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(task, initialDelay, period, unit);
        scheduledTasks.put(taskId, future);
        return future;
    }
    
    /**
     * Cancels a scheduled task
     */
    public boolean cancelScheduledTask(String taskId) {
        ScheduledFuture<?> future = scheduledTasks.remove(taskId);
        if (future != null) {
            return future.cancel(false);
        }
        return false;
    }
    
    /**
     * Gets pending tasks
     */
    public List<Task> getPendingTasks() {
        return new ArrayList<>(taskQueue);
    }
    
    /**
     * Shuts down the scheduler
     */
    public void shutdown() {
        running = false;
        scheduler.shutdown();
        executor.shutdown();
        
        // Cancel all scheduled tasks
        scheduledTasks.values().forEach(future -> future.cancel(false));
        scheduledTasks.clear();
        
        // Fail all pending tasks
        Task task;
        while ((task = taskQueue.poll()) != null) {
            task.fail(new InterruptedException("Scheduler shutdown"));
        }
    }
    
    /**
     * Shuts down the scheduler immediately
     */
    public void shutdownNow() {
        running = false;
        scheduler.shutdownNow();
        executor.shutdownNow();
        scheduledTasks.clear();
        taskQueue.clear();
    }
    
    /**
     * Waits for termination
     */
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return scheduler.awaitTermination(timeout, unit) && 
               executor.awaitTermination(timeout, unit);
    }
    
    private void startTaskProcessor() {
        executor.submit(() -> {
            while (running) {
                try {
                    Task task = taskQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (task != null) {
                        processTask(task);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    // Log error but continue processing
                }
            }
        });
    }
    
    private void processTask(Task task) {
        executor.submit(() -> {
            try {
                task.start();
                // Task execution would be handled by the agent
                // This is a placeholder for the actual execution logic
                task.complete("Task completed");
            } catch (Exception e) {
                task.fail(e);
            }
        });
    }
}