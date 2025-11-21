package com.IDE.plugin.ai.multiagent.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Interface defining the core capabilities of an agent in the multi-agent system.
 */
public interface Agent {
    /**
     * Returns the unique identifier of this agent.
     */
    String getId();
    
    /**
     * Returns the role of this agent in the system.
     */
    AgentRole getRole();
    
    /**
     * Returns the current state of this agent.
     */
    AgentState getState();
    
    /**
     * Returns the set of capability identifiers supported by this agent.
     */
    Set<String> getCapabilities();
    
    /**
     * Returns the performance metrics for this agent.
     */
    PerformanceMetrics getMetrics();
    
    /**
     * Returns the list of completed task results.
     */
    List<TaskResult> getCompletedTasks();
    
    /**
     * Starts the agent, initializing resources and beginning task processing.
     */
    void start();
    
    /**
     * Stops the agent, cleaning up resources and halting task processing.
     */
    void stop();
    
    /**
     * Submits a task for execution by this agent.
     * 
     * @param task The task to execute
     * @return A future that will complete with the task result
     */
    CompletableFuture<TaskResult> executeTask(Task task);
    
    /**
     * Checks if this agent can execute the given task.
     * 
     * @param task The task to check
     * @return true if the agent can execute the task, false otherwise
     */
    boolean canExecuteTask(Task task);
    
    /**
     * Sends a message to another agent.
     * 
     * @param targetAgentId The ID of the target agent
     * @param content The message content
     * @param metadata Additional metadata for the message
     */
    void sendMessage(String targetAgentId, String content, Map<String, Object> metadata);
    
    /**
     * Receives a message from another agent.
     * 
     * @param message The message to receive
     */
    void receiveMessage(AgentMessage message);
}