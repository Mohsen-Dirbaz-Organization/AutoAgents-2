package com.IDE.plugin.ai.multiagent.core;

/**
 * AgentRole: Enumeration defining the different roles agents can have in the system
 */
public enum AgentRole {
    ORCHESTRATOR("Orchestrator", "Coordinates and manages other agents"),
    ARCHITECT("Architect", "Handles system design and architecture decisions"),
    OBSERVER("Observer", "Monitors system health and performance"),
    CODE_EDITOR("Code Editor", "Performs code modifications and refactoring"),
    ANALYZER("Analyzer", "Analyzes code quality and patterns"),
    TESTER("Tester", "Manages testing and quality assurance"),
    DOCUMENTER("Documenter", "Handles documentation generation and maintenance"),
    SECURITY("Security", "Manages security analysis and compliance"),
    OPTIMIZER("Optimizer", "Focuses on performance optimization");
    
    private final String displayName;
    private final String description;
    
    AgentRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}