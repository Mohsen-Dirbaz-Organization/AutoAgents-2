package com.IDE.plugin.ai.multiagent.collision;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Represents a collision between multiple agents trying to access the same resource.
 */
public class Collision {
    private final String collisionId;
    private final CollisionType type;
    private final String resourceId;
    private final List<String> involvedAgentIds;
    private final Instant detectedAt;
    private final CollisionSeverity severity;
    private final Map<String, Object> context;
    
    public enum CollisionType {
        RESOURCE_ACCESS,      // Multiple agents accessing same resource
        TASK_CONFLICT,       // Conflicting task assignments
        CAPABILITY_OVERLAP,  // Multiple agents with same capability competing
        COMMUNICATION,       // Message routing conflicts
        STATE_CONFLICT      // Conflicting state modifications
    }
    
    public enum CollisionSeverity {
        LOW,      // Can be resolved automatically
        MEDIUM,   // Requires coordination
        HIGH,     // Requires manual intervention
        CRITICAL  // System stability at risk
    }
    
    public Collision(String collisionId, CollisionType type, String resourceId,
                    List<String> involvedAgentIds, CollisionSeverity severity) {
        this(collisionId, type, resourceId, involvedAgentIds, severity, 
             Instant.now(), Map.of());
    }
    
    public Collision(String collisionId, CollisionType type, String resourceId,
                    List<String> involvedAgentIds, CollisionSeverity severity,
                    Instant detectedAt, Map<String, Object> context) {
        this.collisionId = collisionId;
        this.type = type;
        this.resourceId = resourceId;
        this.involvedAgentIds = List.copyOf(involvedAgentIds);
        this.severity = severity;
        this.detectedAt = detectedAt;
        this.context = Map.copyOf(context);
    }
    
    public String getCollisionId() {
        return collisionId;
    }
    
    public CollisionType getType() {
        return type;
    }
    
    public String getResourceId() {
        return resourceId;
    }
    
    public List<String> getInvolvedAgentIds() {
        return involvedAgentIds;
    }
    
    public Instant getDetectedAt() {
        return detectedAt;
    }
    
    public CollisionSeverity getSeverity() {
        return severity;
    }
    
    public Map<String, Object> getContext() {
        return context;
    }
    
    public boolean involvesAgent(String agentId) {
        return involvedAgentIds.contains(agentId);
    }
    
    public int getConflictSize() {
        return involvedAgentIds.size();
    }
}