package com.IDE.plugin.ai.multiagent.core;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance metrics for an individual agent.
 */
public class AgentMetrics {
    private final String agentId;
    private final AtomicInteger tasksCompleted = new AtomicInteger(0);
    private final AtomicInteger tasksFailed = new AtomicInteger(0);
    private final AtomicLong totalProcessingTime = new AtomicLong(0);
    private final Instant startTime;
    private volatile Instant lastTaskTime;
    private final Map<String, AtomicInteger> capabilityUsage;
    
    public AgentMetrics(String agentId) {
        this.agentId = agentId;
        this.startTime = Instant.now();
        this.lastTaskTime = startTime;
        this.capabilityUsage = new java.util.concurrent.ConcurrentHashMap<>();
    }
    
    public void recordTaskCompletion(Duration processingTime) {
        tasksCompleted.incrementAndGet();
        totalProcessingTime.addAndGet(processingTime.toMillis());
        lastTaskTime = Instant.now();
    }
    
    public void recordTaskFailure() {
        tasksFailed.incrementAndGet();
        lastTaskTime = Instant.now();
    }
    
    public void recordCapabilityUsage(String capability) {
        capabilityUsage.computeIfAbsent(capability, k -> new AtomicInteger(0))
                       .incrementAndGet();
    }
    
    public String getAgentId() {
        return agentId;
    }
    
    public int getTasksCompleted() {
        return tasksCompleted.get();
    }
    
    public int getTasksFailed() {
        return tasksFailed.get();
    }
    
    public int getTotalTasks() {
        return tasksCompleted.get() + tasksFailed.get();
    }
    
    public double getSuccessRate() {
        int total = getTotalTasks();
        return total == 0 ? 0.0 : (double) tasksCompleted.get() / total;
    }
    
    public double getAverageProcessingTime() {
        int completed = tasksCompleted.get();
        return completed == 0 ? 0.0 : (double) totalProcessingTime.get() / completed;
    }
    
    public Duration getUptime() {
        return Duration.between(startTime, Instant.now());
    }
    
    public Duration getIdleTime() {
        return Duration.between(lastTaskTime, Instant.now());
    }
    
    public Map<String, Integer> getCapabilityUsageStats() {
        Map<String, Integer> stats = new java.util.HashMap<>();
        capabilityUsage.forEach((capability, count) -> 
            stats.put(capability, count.get()));
        return stats;
    }
    
    public MetricsSnapshot getSnapshot() {
        return new MetricsSnapshot(
            agentId,
            getTasksCompleted(),
            getTasksFailed(),
            getSuccessRate(),
            getAverageProcessingTime(),
            getUptime(),
            getIdleTime(),
            getCapabilityUsageStats()
        );
    }
    
    public static class MetricsSnapshot {
        private final String agentId;
        private final int tasksCompleted;
        private final int tasksFailed;
        private final double successRate;
        private final double averageProcessingTime;
        private final Duration uptime;
        private final Duration idleTime;
        private final Map<String, Integer> capabilityUsage;
        
        public MetricsSnapshot(String agentId, int tasksCompleted, int tasksFailed,
                             double successRate, double averageProcessingTime,
                             Duration uptime, Duration idleTime,
                             Map<String, Integer> capabilityUsage) {
            this.agentId = agentId;
            this.tasksCompleted = tasksCompleted;
            this.tasksFailed = tasksFailed;
            this.successRate = successRate;
            this.averageProcessingTime = averageProcessingTime;
            this.uptime = uptime;
            this.idleTime = idleTime;
            this.capabilityUsage = capabilityUsage;
        }
        
        // Getters
        public String getAgentId() { return agentId; }
        public int getTasksCompleted() { return tasksCompleted; }
        public int getTasksFailed() { return tasksFailed; }
        public double getSuccessRate() { return successRate; }
        public double getAverageProcessingTime() { return averageProcessingTime; }
        public Duration getUptime() { return uptime; }
        public Duration getIdleTime() { return idleTime; }
        public Map<String, Integer> getCapabilityUsage() { return capabilityUsage; }
    }
}