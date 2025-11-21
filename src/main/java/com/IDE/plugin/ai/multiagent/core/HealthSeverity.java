package com.IDE.plugin.ai.multiagent.core;

/**
 * Severity levels for system health issues.
 */
public enum HealthSeverity {
    /**
     * System is healthy
     */
    HEALTHY,
    
    /**
     * Minor issues that don't affect functionality
     */
    INFO,
    
    /**
     * Issues that may affect performance
     */
    WARNING,
    
    /**
     * Serious issues affecting functionality
     */
    ERROR,
    
    /**
     * Critical issues requiring immediate attention
     */
    CRITICAL
}