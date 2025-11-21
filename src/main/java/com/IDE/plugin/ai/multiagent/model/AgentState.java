package com.IDE.plugin.ai.multiagent.model;

/**
 * Defines the possible states of an agent in the multi-agent system.
 */
public enum AgentState {
    /**
     * Agent is initializing and setting up resources.
     */
    INITIALIZING,
    
    /**
     * Agent is ready but not actively processing tasks.
     */
    IDLE,
    
    /**
     * Agent is actively processing one or more tasks.
     */
    WORKING,
    
    /**
     * Agent is paused and not accepting new tasks.
     */
    PAUSED,
    
    /**
     * Agent has encountered an error.
     */
    ERROR,
    
    /**
     * Agent is shutting down and cleaning up resources.
     */
    SHUTTING_DOWN,
    
    /**
     * Agent has been terminated.
     */
    TERMINATED
}