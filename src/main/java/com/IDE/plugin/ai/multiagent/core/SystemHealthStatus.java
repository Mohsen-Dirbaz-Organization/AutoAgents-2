package com.IDE.plugin.ai.multiagent.core;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Represents the overall health status of the multi-agent system.
 */
public class SystemHealthStatus {
    private final HealthSeverity overallHealth;
    private final Map<String, ComponentHealth> componentHealth;
    private final List<HealthIssue> activeIssues;
    private final SystemMetrics systemMetrics;
    private final Instant timestamp;
    
    public SystemHealthStatus(HealthSeverity overallHealth,
                            Map<String, ComponentHealth> componentHealth,
                            List<HealthIssue> activeIssues,
                            SystemMetrics systemMetrics) {
        this.overallHealth = overallHealth;
        this.componentHealth = Map.copyOf(componentHealth);
        this.activeIssues = List.copyOf(activeIssues);
        this.systemMetrics = systemMetrics;
        this.timestamp = Instant.now();
    }
    
    public HealthSeverity getOverallHealth() {
        return overallHealth;
    }
    
    public Map<String, ComponentHealth> getComponentHealth() {
        return componentHealth;
    }
    
    public List<HealthIssue> getActiveIssues() {
        return activeIssues;
    }
    
    public SystemMetrics getSystemMetrics() {
        return systemMetrics;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public boolean isHealthy() {
        return overallHealth == HealthSeverity.HEALTHY || 
               overallHealth == HealthSeverity.INFO;
    }
    
    public boolean hasCriticalIssues() {
        return overallHealth == HealthSeverity.CRITICAL ||
               activeIssues.stream().anyMatch(issue -> 
                   issue.getSeverity() == HealthSeverity.CRITICAL);
    }
    
    /**
     * Represents health of a specific component in the system.
     */
    public static class ComponentHealth {
        private final String componentName;
        private final HealthSeverity severity;
        private final String status;
        private final Map<String, Object> metrics;
        
        public ComponentHealth(String componentName, HealthSeverity severity,
                             String status, Map<String, Object> metrics) {
            this.componentName = componentName;
            this.severity = severity;
            this.status = status;
            this.metrics = Map.copyOf(metrics);
        }
        
        public String getComponentName() {
            return componentName;
        }
        
        public HealthSeverity getSeverity() {
            return severity;
        }
        
        public String getStatus() {
            return status;
        }
        
        public Map<String, Object> getMetrics() {
            return metrics;
        }
    }
    
    /**
     * Represents a specific health issue in the system.
     */
    public static class HealthIssue {
        private final String issueId;
        private final String component;
        private final HealthSeverity severity;
        private final String description;
        private final Instant detectedAt;
        private final Map<String, Object> context;
        
        public HealthIssue(String issueId, String component, HealthSeverity severity,
                         String description, Instant detectedAt, Map<String, Object> context) {
            this.issueId = issueId;
            this.component = component;
            this.severity = severity;
            this.description = description;
            this.detectedAt = detectedAt;
            this.context = Map.copyOf(context);
        }
        
        public String getIssueId() {
            return issueId;
        }
        
        public String getComponent() {
            return component;
        }
        
        public HealthSeverity getSeverity() {
            return severity;
        }
        
        public String getDescription() {
            return description;
        }
        
        public Instant getDetectedAt() {
            return detectedAt;
        }
        
        public Map<String, Object> getContext() {
            return context;
        }
    }
    
    /**
     * System-wide metrics.
     */
    public static class SystemMetrics {
        private final int activeAgents;
        private final int totalTasks;
        private final int pendingTasks;
        private final double averageResponseTime;
        private final double systemLoad;
        private final long memoryUsage;
        private final long maxMemory;
        
        public SystemMetrics(int activeAgents, int totalTasks, int pendingTasks,
                           double averageResponseTime, double systemLoad,
                           long memoryUsage, long maxMemory) {
            this.activeAgents = activeAgents;
            this.totalTasks = totalTasks;
            this.pendingTasks = pendingTasks;
            this.averageResponseTime = averageResponseTime;
            this.systemLoad = systemLoad;
            this.memoryUsage = memoryUsage;
            this.maxMemory = maxMemory;
        }
        
        public int getActiveAgents() {
            return activeAgents;
        }
        
        public int getTotalTasks() {
            return totalTasks;
        }
        
        public int getPendingTasks() {
            return pendingTasks;
        }
        
        public double getAverageResponseTime() {
            return averageResponseTime;
        }
        
        public double getSystemLoad() {
            return systemLoad;
        }
        
        public long getMemoryUsage() {
            return memoryUsage;
        }
        
        public long getMaxMemory() {
            return maxMemory;
        }
        
        public double getMemoryUsagePercentage() {
            return maxMemory > 0 ? (double) memoryUsage / maxMemory * 100 : 0;
        }
    }
}