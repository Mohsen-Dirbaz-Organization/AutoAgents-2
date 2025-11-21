package com.IDE.plugin.ai.multiagent.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a capability that an agent can provide.
 */
public class AgentCapability {
    private final String capabilityId;
    private final String name;
    private final String description;
    private final Map<String, Object> parameters;
    private final int performanceRating;
    private final boolean enabled;
    
    /**
     * Creates a new agent capability.
     * 
     * @param capabilityId The unique identifier for this capability
     * @param name The name of this capability
     * @param description A description of this capability
     * @param parameters Additional parameters for this capability
     * @param performanceRating A rating of the agent's performance for this capability (1-10)
     * @param enabled Whether this capability is currently enabled
     */
    public AgentCapability(String capabilityId, String name, String description, 
                          Map<String, Object> parameters, int performanceRating, boolean enabled) {
        this.capabilityId = capabilityId;
        this.name = name;
        this.description = description;
        this.parameters = parameters != null ? new HashMap<>(parameters) : new HashMap<>();
        this.performanceRating = Math.max(1, Math.min(10, performanceRating));
        this.enabled = enabled;
    }
    
    /**
     * Returns the unique identifier for this capability.
     */
    public String getCapabilityId() {
        return capabilityId;
    }
    
    /**
     * Returns the name of this capability.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the description of this capability.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Returns the additional parameters for this capability.
     */
    public Map<String, Object> getParameters() {
        return new HashMap<>(parameters);
    }
    
    /**
     * Returns the performance rating for this capability (1-10).
     */
    public int getPerformanceRating() {
        return performanceRating;
    }
    
    /**
     * Returns whether this capability is currently enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Creates a new capability with the enabled state changed.
     * 
     * @param enabled The new enabled state
     * @return A new AgentCapability instance
     */
    public AgentCapability withEnabled(boolean enabled) {
        return new AgentCapability(capabilityId, name, description, parameters, performanceRating, enabled);
    }
    
    /**
     * Creates a new capability with updated parameters.
     * 
     * @param parameters The new parameters
     * @return A new AgentCapability instance
     */
    public AgentCapability withParameters(Map<String, Object> parameters) {
        return new AgentCapability(capabilityId, name, description, parameters, performanceRating, enabled);
    }
    
    /**
     * Creates a new capability with an updated performance rating.
     * 
     * @param performanceRating The new performance rating
     * @return A new AgentCapability instance
     */
    public AgentCapability withPerformanceRating(int performanceRating) {
        return new AgentCapability(capabilityId, name, description, parameters, performanceRating, enabled);
    }
}