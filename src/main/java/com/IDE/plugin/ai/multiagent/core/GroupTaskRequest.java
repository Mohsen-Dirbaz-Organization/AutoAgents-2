package com.IDE.plugin.ai.multiagent.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Request for a group task that requires coordination between multiple agents.
 */
public class GroupTaskRequest {
    private final String groupId;
    private final List<Task> tasks;
    private final Set<String> requiredAgentIds;
    private final CoordinationStrategy strategy;
    private final Map<String, Object> metadata;
    
    public enum CoordinationStrategy {
        PARALLEL,      // Execute all tasks in parallel
        SEQUENTIAL,    // Execute tasks in order
        PIPELINE,      // Output of one task feeds into the next
        VOTING,        // Multiple agents vote on results
        CONSENSUS      // Agents must reach consensus
    }
    
    public GroupTaskRequest(String groupId, List<Task> tasks, CoordinationStrategy strategy) {
        this(groupId, tasks, Set.of(), strategy, Map.of());
    }
    
    public GroupTaskRequest(String groupId, List<Task> tasks, Set<String> requiredAgentIds, 
                          CoordinationStrategy strategy, Map<String, Object> metadata) {
        this.groupId = groupId;
        this.tasks = tasks;
        this.requiredAgentIds = requiredAgentIds;
        this.strategy = strategy;
        this.metadata = metadata;
    }
    
    public String getGroupId() {
        return groupId;
    }
    
    public List<Task> getTasks() {
        return tasks;
    }
    
    public Set<String> getRequiredAgentIds() {
        return requiredAgentIds;
    }
    
    public CoordinationStrategy getStrategy() {
        return strategy;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
}