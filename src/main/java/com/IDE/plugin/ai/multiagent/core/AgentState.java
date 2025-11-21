package com.IDE.plugin.ai.multiagent.core;

/**
 * Represents the various states an agent can be in within the multi-agent system.
 */
public enum AgentState {
    /**
     * Agent is registered but not yet active
     */
    IDLE,
    
    /**
     * Agent is actively processing a task
     */
    ACTIVE,
    
    /**
     * Agent is temporarily suspended
     */
    SUSPENDED,
    
    /**
     * Agent has encountered an error
     */
    ERROR,
    
    /**
     * Agent is shutting down
     */
    SHUTDOWN
}