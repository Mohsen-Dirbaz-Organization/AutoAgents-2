package com.IDE.plugin.ai.multiagent.model;

/**
 * Defines the possible roles an agent can have in the multi-agent system.
 */
public enum AgentRole {
    /**
     * Architect agent responsible for high-level design decisions.
     */
    ARCHITECT,
    
    /**
     * Code editor agent responsible for implementing code changes.
     */
    CODE_EDITOR,
    
    /**
     * Observer agent that monitors the system and provides feedback.
     */
    OBSERVER,
    
    /**
     * Coordinator agent that manages and coordinates other agents.
     */
    COORDINATOR,
    
    /**
     * Tester agent responsible for testing and validation.
     */
    TESTER,
    
    /**
     * Reviewer agent that reviews code and provides feedback.
     */
    REVIEWER,
    
    /**
     * Generic agent with no specific role.
     */
    GENERIC
}