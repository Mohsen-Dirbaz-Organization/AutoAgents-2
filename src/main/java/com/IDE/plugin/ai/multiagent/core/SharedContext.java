package com.IDE.plugin.ai.multiagent.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * SharedContext manages shared state and data across agents
 */
public class SharedContext {
    private final Map<String, Object> contextData = new ConcurrentHashMap<>();
    private final Map<String, String> agentStates = new ConcurrentHashMap<>();
    
    /**
     * Store data in the shared context
     */
    public void put(String key, Object value) {
        contextData.put(key, value);
    }
    
    /**
     * Retrieve data from the shared context
     */
    public Object get(String key) {
        return contextData.get(key);
    }
    
    /**
     * Check if a key exists in the context
     */
    public boolean containsKey(String key) {
        return contextData.containsKey(key);
    }
    
    /**
     * Remove data from the context
     */
    public Object remove(String key) {
        return contextData.remove(key);
    }
    
    /**
     * Clear all context data
     */
    public void clear() {
        contextData.clear();
        agentStates.clear();
    }
    
    /**
     * Update agent state
     */
    public void updateAgentState(String agentId, String state) {
        agentStates.put(agentId, state);
    }
    
    /**
     * Get agent state
     */
    public String getAgentState(String agentId) {
        return agentStates.get(agentId);
    }
    
    /**
     * Get all agent states
     */
    public Map<String, String> getAllAgentStates() {
        return new ConcurrentHashMap<>(agentStates);
    }
}