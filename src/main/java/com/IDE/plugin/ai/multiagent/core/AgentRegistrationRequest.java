package com.IDE.plugin.ai.multiagent.core;

import com.IDE.plugin.ai.multiagent.agent.Agent;

import java.util.Map;
import java.util.Set;

/**
 * Request object for registering a new agent in the system.
 */
public class AgentRegistrationRequest {
    private final Agent agent;
    private final Set<AgentCapability> capabilities;
    private final Map<String, Object> metadata;
    
    public AgentRegistrationRequest(Agent agent, Set<AgentCapability> capabilities) {
        this(agent, capabilities, Map.of());
    }
    
    public AgentRegistrationRequest(Agent agent, Set<AgentCapability> capabilities, Map<String, Object> metadata) {
        this.agent = agent;
        this.capabilities = capabilities;
        this.metadata = metadata;
    }
    
    public Agent getAgent() {
        return agent;
    }
    
    public Set<AgentCapability> getCapabilities() {
        return capabilities;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
}