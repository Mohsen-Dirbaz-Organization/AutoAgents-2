package com.IDE.plugin.ai.multiagent.collision;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the resolution of a collision between agents.
 */
public class CollisionResolution {
    private final String resolutionId;
    private final String collisionId;
    private final ResolutionStrategy strategy;
    private final ResolutionStatus status;
    private final String winnerId;  // Agent that gets priority (if applicable)
    private final Map<String, String> agentActions;  // Actions each agent should take
    private final Instant resolvedAt;
    private final String description;
    
    public enum ResolutionStrategy {
        PRIORITY_BASED,      // Higher priority agent wins
        ROUND_ROBIN,        // Agents take turns
        TIME_SLICING,       // Time-based resource sharing
        CAPABILITY_BASED,   // Best suited agent wins
        VOTING,            // Agents vote on resolution
        RETRY,             // Agents retry with backoff
        DELEGATION,        // Delegate to another agent
        ABORT              // Abort conflicting operations
    }
    
    public enum ResolutionStatus {
        PENDING,
        IN_PROGRESS,
        RESOLVED,
        FAILED,
        ESCALATED
    }
    
    private CollisionResolution(Builder builder) {
        this.resolutionId = builder.resolutionId;
        this.collisionId = builder.collisionId;
        this.strategy = builder.strategy;
        this.status = builder.status;
        this.winnerId = builder.winnerId;
        this.agentActions = builder.agentActions != null ? 
            Map.copyOf(builder.agentActions) : Map.of();
        this.resolvedAt = builder.resolvedAt != null ? 
            builder.resolvedAt : Instant.now();
        this.description = builder.description;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public String getResolutionId() {
        return resolutionId;
    }
    
    public String getCollisionId() {
        return collisionId;
    }
    
    public ResolutionStrategy getStrategy() {
        return strategy;
    }
    
    public ResolutionStatus getStatus() {
        return status;
    }
    
    public Optional<String> getWinnerId() {
        return Optional.ofNullable(winnerId);
    }
    
    public Map<String, String> getAgentActions() {
        return agentActions;
    }
    
    public Instant getResolvedAt() {
        return resolvedAt;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static class Builder {
        private String resolutionId;
        private String collisionId;
        private ResolutionStrategy strategy;
        private ResolutionStatus status;
        private String winnerId;
        private Map<String, String> agentActions;
        private Instant resolvedAt;
        private String description;
        
        public Builder resolutionId(String resolutionId) {
            this.resolutionId = resolutionId;
            return this;
        }
        
        public Builder collisionId(String collisionId) {
            this.collisionId = collisionId;
            return this;
        }
        
        public Builder strategy(ResolutionStrategy strategy) {
            this.strategy = strategy;
            return this;
        }
        
        public Builder status(ResolutionStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder winnerId(String winnerId) {
            this.winnerId = winnerId;
            return this;
        }
        
        public Builder agentActions(Map<String, String> agentActions) {
            this.agentActions = agentActions;
            return this;
        }
        
        public Builder resolvedAt(Instant resolvedAt) {
            this.resolvedAt = resolvedAt;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public CollisionResolution build() {
            return new CollisionResolution(this);
        }
    }
}