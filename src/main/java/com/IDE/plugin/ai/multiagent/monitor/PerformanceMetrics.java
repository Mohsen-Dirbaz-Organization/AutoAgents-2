package com.IDE.plugin.ai.multiagent.monitor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks performance metrics for an agent.
 */
public class PerformanceMetrics {
    private final LocalDateTime startTime;
    private final AtomicInteger tasksCompleted;
    private final AtomicInteger tasksFailed;
    private final AtomicInteger messagesSent;
    private final AtomicInteger messagesReceived;
    private final AtomicLong totalTaskExecutionTimeMs;
    private final AtomicLong totalMessageProcessingTimeMs;
    private final AtomicInteger peakConcurrentTasks;
    private final AtomicInteger currentConcurrentTasks;
    
    /**
     * Creates a new performance metrics tracker.
     */
    public PerformanceMetrics() {
        this.startTime = LocalDateTime.now();
        this.tasksCompleted = new AtomicInteger(0);
        this.tasksFailed = new AtomicInteger(0);
        this.messagesSent = new AtomicInteger(0);
        this.messagesReceived = new AtomicInteger(0);
        this.totalTaskExecutionTimeMs = new AtomicLong(0);
        this.totalMessageProcessingTimeMs = new AtomicLong(0);
        this.peakConcurrentTasks = new AtomicInteger(0);
        this.currentConcurrentTasks = new AtomicInteger(0);
    }
    
    /**
     * Records a completed task.
     * 
     * @param executionTimeMs The time taken to execute the task in milliseconds
     */
    public void recordTaskCompleted(long executionTimeMs) {
        tasksCompleted.incrementAndGet();
        totalTaskExecutionTimeMs.addAndGet(executionTimeMs);
        decrementConcurrentTasks();
    }
    
    /**
     * Records a failed task.
     * 
     * @param executionTimeMs The time taken before the task failed in milliseconds
     */
    public void recordTaskFailed(long executionTimeMs) {
        tasksFailed.incrementAndGet();
        totalTaskExecutionTimeMs.addAndGet(executionTimeMs);
        decrementConcurrentTasks();
    }
    
    /**
     * Records a sent message.
     * 
     * @param processingTimeMs The time taken to process and send the message in milliseconds
     */
    public void recordMessageSent(long processingTimeMs) {
        messagesSent.incrementAndGet();
        totalMessageProcessingTimeMs.addAndGet(processingTimeMs);
    }
    
    /**
     * Records a received message.
     * 
     * @param processingTimeMs The time taken to process the received message in milliseconds
     */
    public void recordMessageReceived(long processingTimeMs) {
        messagesReceived.incrementAndGet();
        totalMessageProcessingTimeMs.addAndGet(processingTimeMs);
    }
    
    /**
     * Increments the count of concurrent tasks and updates the peak if necessary.
     */
    public void incrementConcurrentTasks() {
        int current = currentConcurrentTasks.incrementAndGet();
        updatePeakConcurrentTasks(current);
    }
    
    /**
     * Decrements the count of concurrent tasks.
     */
    public void decrementConcurrentTasks() {
        currentConcurrentTasks.decrementAndGet();
    }
    
    /**
     * Updates the peak concurrent tasks if the current count is higher.
     * 
     * @param currentCount The current count of concurrent tasks
     */
    private void updatePeakConcurrentTasks(int currentCount) {
        int peak;
        do {
            peak = peakConcurrentTasks.get();
            if (currentCount <= peak) {
                return;
            }
        } while (!peakConcurrentTasks.compareAndSet(peak, currentCount));
    }
    
    /**
     * Returns the time when metrics tracking started.
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    /**
     * Returns the number of completed tasks.
     */
    public int getTasksCompleted() {
        return tasksCompleted.get();
    }
    
    /**
     * Returns the number of failed tasks.
     */
    public int getTasksFailed() {
        return tasksFailed.get();
    }
    
    /**
     * Returns the total number of tasks (completed + failed).
     */
    public int getTotalTasks() {
        return tasksCompleted.get() + tasksFailed.get();
    }
    
    /**
     * Returns the number of messages sent.
     */
    public int getMessagesSent() {
        return messagesSent.get();
    }
    
    /**
     * Returns the number of messages received.
     */
    public int getMessagesReceived() {
        return messagesReceived.get();
    }
    
    /**
     * Returns the total task execution time in milliseconds.
     */
    public long getTotalTaskExecutionTimeMs() {
        return totalTaskExecutionTimeMs.get();
    }
    
    /**
     * Returns the total message processing time in milliseconds.
     */
    public long getTotalMessageProcessingTimeMs() {
        return totalMessageProcessingTimeMs.get();
    }
    
    /**
     * Returns the peak number of concurrent tasks.
     */
    public int getPeakConcurrentTasks() {
        return peakConcurrentTasks.get();
    }
    
    /**
     * Returns the current number of concurrent tasks.
     */
    public int getCurrentConcurrentTasks() {
        return currentConcurrentTasks.get();
    }
    
    /**
     * Returns the average task execution time in milliseconds, or 0 if no tasks have been completed.
     */
    public double getAverageTaskExecutionTimeMs() {
        int totalTasks = getTotalTasks();
        if (totalTasks == 0) {
            return 0;
        }
        return (double) totalTaskExecutionTimeMs.get() / totalTasks;
    }
    
    /**
     * Returns the average message processing time in milliseconds, or 0 if no messages have been processed.
     */
    public double getAverageMessageProcessingTimeMs() {
        int totalMessages = messagesSent.get() + messagesReceived.get();
        if (totalMessages == 0) {
            return 0;
        }
        return (double) totalMessageProcessingTimeMs.get() / totalMessages;
    }
    
    /**
     * Returns the uptime duration.
     */
    public Duration getUptime() {
        return Duration.between(startTime, LocalDateTime.now());
    }
    
    /**
     * Returns the task success rate (0.0 to 1.0), or 0 if no tasks have been attempted.
     */
    public double getTaskSuccessRate() {
        int totalTasks = getTotalTasks();
        if (totalTasks == 0) {
            return 0;
        }
        return (double) tasksCompleted.get() / totalTasks;
    }
}