package com.IDE.plugin.ai.multiagent.station;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Real-time status tracking and monitoring for stations.
 */
public class StationStatus {
    private final String stationId;
    private final AtomicReference<StatusLevel> statusLevel = new AtomicReference<>(StatusLevel.NORMAL);
    private final AtomicLong lastUpdateTime = new AtomicLong(System.currentTimeMillis());
    
    // Basic metrics
    private volatile boolean active = false;
    private volatile int agentCount = 0;
    private volatile long tasksProcessed = 0;
    private volatile double currentLoad = 0.0;
    private volatile double averageProcessingTime = 0.0;
    private volatile double healthScore = 1.0;
    
    // Performance metrics
    private volatile long successfulTasks = 0;
    private volatile long failedTasks = 0;
    private volatile double throughput = 0.0; // tasks per minute
    private volatile long totalUptime = 0;
    private volatile long lastDowntime = 0;
    
    // Resource utilization
    private volatile double cpuUsage = 0.0;
    private volatile double memoryUsage = 0.0;
    private volatile int activeConnections = 0;
    
    // Alert tracking
    private volatile String lastAlert = null;
    private volatile long lastAlertTime = 0;
    private volatile int alertCount = 0;
    
    public StationStatus(@NotNull String stationId) {
        this.stationId = stationId;
    }
    
    /**
     * Updates the status level based on current metrics.
     */
    public void updateStatusLevel() {
        StatusLevel newLevel;
        
        if (!active) {
            newLevel = StatusLevel.INACTIVE;
        } else if (healthScore < 0.3 || failedTasks > successfulTasks) {
            newLevel = StatusLevel.CRITICAL;
        } else if (healthScore < 0.6 || currentLoad > 0.9) {
            newLevel = StatusLevel.WARNING;
        } else {
            newLevel = StatusLevel.NORMAL;
        }
        
        statusLevel.set(newLevel);
    }
    
    /**
     * Records a new alert.
     */
    public void recordAlert(@NotNull String alert) {
        this.lastAlert = alert;
        this.lastAlertTime = System.currentTimeMillis();
        this.alertCount++;
        updateStatusLevel();
    }
    
    /**
     * Calculates throughput based on recent performance.
     */
    public void calculateThroughput(long timeWindowMs) {
        if (timeWindowMs <= 0) {
            this.throughput = 0.0;
            return;
        }
        
        double tasksPerMs = (double) tasksProcessed / timeWindowMs;
        this.throughput = tasksPerMs * 60000; // Convert to tasks per minute
    }
    
    /**
     * Updates the timestamp of the last update.
     */
    public void updateTimestamp() {
        lastUpdateTime.set(System.currentTimeMillis());
    }
    
    /**
     * Checks if the status is stale (not updated recently).
     */
    public boolean isStale(long maxAgeMs) {
        return (System.currentTimeMillis() - lastUpdateTime.get()) > maxAgeMs;
    }
    
    /**
     * Resets performance counters.
     */
    public void resetCounters() {
        this.successfulTasks = 0;
        this.failedTasks = 0;
        this.alertCount = 0;
        this.throughput = 0.0;
        updateTimestamp();
    }
    
    /**
     * Gets a summary of the current status.
     */
    @NotNull
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Station: ").append(stationId).append("\n");
        summary.append("Status: ").append(statusLevel.get()).append("\n");
        summary.append("Active: ").append(active).append("\n");
        summary.append("Agents: ").append(agentCount).append("\n");
        summary.append("Load: ").append(String.format("%.2f%%", currentLoad * 100)).append("\n");
        summary.append("Health: ").append(String.format("%.2f", healthScore)).append("\n");
        summary.append("Tasks: ").append(tasksProcessed).append(" (").append(successfulTasks).append(" successful)\n");
        summary.append("Throughput: ").append(String.format("%.2f tasks/min", throughput)).append("\n");
        
        if (lastAlert != null) {
            summary.append("Last Alert: ").append(lastAlert).append("\n");
        }
        
        return summary.toString();
    }
    
    // Getters and setters
    
    @NotNull
    public String getStationId() {
        return stationId;
    }
    
    @NotNull
    public StatusLevel getStatusLevel() {
        return statusLevel.get();
    }
    
    public long getLastUpdateTime() {
        return lastUpdateTime.get();
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
        updateStatusLevel();
    }
    
    public int getAgentCount() {
        return agentCount;
    }
    
    public void setAgentCount(int agentCount) {
        this.agentCount = agentCount;
    }
    
    public long getTasksProcessed() {
        return tasksProcessed;
    }
    
    public void setTasksProcessed(long tasksProcessed) {
        this.tasksProcessed = tasksProcessed;
    }
    
    public double getCurrentLoad() {
        return currentLoad;
    }
    
    public void setCurrentLoad(double currentLoad) {
        this.currentLoad = Math.max(0.0, Math.min(1.0, currentLoad));
        updateStatusLevel();
    }
    
    public double getAverageProcessingTime() {
        return averageProcessingTime;
    }
    
    public void setAverageProcessingTime(double averageProcessingTime) {
        this.averageProcessingTime = averageProcessingTime;
    }
    
    public double getHealthScore() {
        return healthScore;
    }
    
    public void setHealthScore(double healthScore) {
        this.healthScore = Math.max(0.0, Math.min(1.0, healthScore));
        updateStatusLevel();
    }
    
    public long getSuccessfulTasks() {
        return successfulTasks;
    }
    
    public void incrementSuccessfulTasks() {
        this.successfulTasks++;
        updateStatusLevel();
    }
    
    public long getFailedTasks() {
        return failedTasks;
    }
    
    public void incrementFailedTasks() {
        this.failedTasks++;
        updateStatusLevel();
    }
    
    public double getThroughput() {
        return throughput;
    }
    
    public enum StatusLevel {
        NORMAL("Normal", "Station operating within normal parameters"),
        WARNING("Warning", "Station experiencing minor issues"),
        CRITICAL("Critical", "Station requires immediate attention"),
        INACTIVE("Inactive", "Station is not currently active");
        
        private final String displayName;
        private final String description;
        
        StatusLevel(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
}