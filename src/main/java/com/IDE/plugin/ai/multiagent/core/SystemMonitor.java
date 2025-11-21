package com.IDE.plugin.ai.multiagent.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Monitors system performance metrics and resource usage
 */
public class SystemMonitor {
    private final Map<String, AtomicLong> metrics = new ConcurrentHashMap<>();
    private final Map<String, Double> resourceUsage = new ConcurrentHashMap<>();
    
    /**
     * Records a metric value
     */
    public void recordMetric(String name, long value) {
        metrics.computeIfAbsent(name, k -> new AtomicLong()).addAndGet(value);
    }
    
    /**
     * Updates resource usage
     */
    public void updateResourceUsage(String resource, double usage) {
        resourceUsage.put(resource, usage);
    }
    
    /**
     * Gets current metric value
     */
    public long getMetric(String name) {
        AtomicLong value = metrics.get(name);
        return value != null ? value.get() : 0;
    }
    
    /**
     * Gets current resource usage
     */
    public double getResourceUsage(String resource) {
        return resourceUsage.getOrDefault(resource, 0.0);
    }
    
    /**
     * Gets all metrics
     */
    public Map<String, Long> getAllMetrics() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        metrics.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }
    
    /**
     * Gets all resource usage
     */
    public Map<String, Double> getAllResourceUsage() {
        return new ConcurrentHashMap<>(resourceUsage);
    }
    
    /**
     * Resets all metrics
     */
    public void reset() {
        metrics.clear();
        resourceUsage.clear();
    }
}