package com.IDE.plugin.ai.multiagent.agent;

import com.IDE.plugin.ai.multiagent.core.AgentCapability;
import com.IDE.plugin.ai.multiagent.core.AgentState;
import com.IDE.plugin.ai.multiagent.core.Task;
import com.IDE.plugin.ai.multiagent.core.TaskResult;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Core interface for all agents in the multi-agent system.
 */
public interface Agent {
    /**
     * @return Unique identifier for this agent
     */
    String getId();
    
    /**
     * @return Human-readable name for this agent
     */
    String getName();
    
    /**
     * @return Current state of the agent
     */
    AgentState getState();
    
    /**
     * @return Set of capabilities this agent possesses
     */
    Set<AgentCapability> getCapabilities();
    
    /**
     * Execute a task asynchronously
     * @param task The task to execute
     * @return Future containing the result of the task execution
     */
    CompletableFuture<TaskResult> executeTask(Task task);
    
    /**
     * Initialize the agent
     */
    void initialize();
    
    /**
     * Shutdown the agent gracefully
     */
    void shutdown();
    
    /**
     * Check if agent can handle a specific capability
     * @param capability The capability to check
     * @return true if agent has the capability
     */
    boolean hasCapability(AgentCapability capability);
}