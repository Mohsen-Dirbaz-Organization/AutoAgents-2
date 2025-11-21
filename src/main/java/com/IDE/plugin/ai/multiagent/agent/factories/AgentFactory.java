package com.IDE.plugin.ai.multiagent.agent.factories;

import com.IDE.plugin.ai.multiagent.agent.Agent;
import com.IDE.plugin.ai.multiagent.agent.impl.CodeReviewAgent;
import com.IDE.plugin.ai.multiagent.agent.impl.DocumentationAgent;
import com.IDE.plugin.ai.multiagent.agent.impl.RefactoringAgent;
import com.IDE.plugin.ai.multiagent.agent.impl.TestGeneratorAgent;
import com.IDE.plugin.ai.multiagent.core.ServiceManager;

/**
 * Factory for creating different types of agents
 */
public class AgentFactory {
    private final ServiceManager serviceManager;
    
    public AgentFactory(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }
    
    /**
     * Creates an agent of the specified type
     */
    public Agent createAgent(AgentType type) {
        switch (type) {
            case CODE_REVIEW:
                return new CodeReviewAgent(serviceManager);
            case DOCUMENTATION:
                return new DocumentationAgent(serviceManager);
            case REFACTORING:
                return new RefactoringAgent(serviceManager);
            case TEST_GENERATOR:
                return new TestGeneratorAgent(serviceManager);
            default:
                throw new IllegalArgumentException("Unknown agent type: " + type);
        }
    }
    
    /**
     * Creates an agent by class name
     */
    @SuppressWarnings("unchecked")
    public Agent createAgent(String className) {
        try {
            Class<? extends Agent> agentClass = (Class<? extends Agent>) Class.forName(className);
            return agentClass.getConstructor(ServiceManager.class).newInstance(serviceManager);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create agent: " + className, e);
        }
    }
    
    /**
     * Agent types supported by the factory
     */
    public enum AgentType {
        CODE_REVIEW("Code Review Agent"),
        DOCUMENTATION("Documentation Agent"),
        REFACTORING("Refactoring Agent"),
        TEST_GENERATOR("Test Generator Agent");
        
        private final String displayName;
        
        AgentType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}