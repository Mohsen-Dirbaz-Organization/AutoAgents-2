package com.IDE.plugin.ai.multiagent.trust;

import java.time.Instant;
import java.util.Map;

/**
 * Metrics used to evaluate trust levels between agents.
 */
public class TrustMetrics {
    private final String agentId;
    private final double successRate;
    private final double responseTime;
    private final int totalTasks;
    private final int successfulTasks;
    private final double reliability;
    private final Instant lastUpdated;
    private final Map<String, Double> capabilityScores;
    
    private TrustMetrics(Builder builder) {
        this.agentId = builder.agentId;
        this.successRate = builder.successRate;
        this.responseTime = builder.responseTime;
        this.totalTasks = builder.totalTasks;
        this.successfulTasks = builder.successfulTasks;
        this.reliability = builder.reliability;
        this.lastUpdated = builder.lastUpdated != null ? builder.lastUpdated : Instant.now();
        this.capabilityScores = builder.capabilityScores != null ? builder.capabilityScores : Map.of();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public String getAgentId() {
        return agentId;
    }
    
    public double getSuccessRate() {
        return successRate;
    }
    
    public double getResponseTime() {
        return responseTime;
    }
    
    public int getTotalTasks() {
        return totalTasks;
    }
    
    public int getSuccessfulTasks() {
        return successfulTasks;
    }
    
    public double getReliability() {
        return reliability;
    }
    
    public Instant getLastUpdated() {
        return lastUpdated;
    }
    
    public Map<String, Double> getCapabilityScores() {
        return capabilityScores;
    }
    
    public double getOverallTrustScore() {
        // Weighted average of different metrics
        return (successRate * 0.4) + (reliability * 0.3) + 
               ((1.0 / (1.0 + responseTime)) * 0.3);
    }
    
    public static class Builder {
        private String agentId;
        private double successRate;
        private double responseTime;
        private int totalTasks;
        private int successfulTasks;
        private double reliability;
        private Instant lastUpdated;
        private Map<String, Double> capabilityScores;
        
        public Builder agentId(String agentId) {
            this.agentId = agentId;
            return this;
        }
        
        public Builder successRate(double successRate) {
            this.successRate = successRate;
            return this;
        }
        
        public Builder responseTime(double responseTime) {
            this.responseTime = responseTime;
            return this;
        }
        
        public Builder totalTasks(int totalTasks) {
            this.totalTasks = totalTasks;
            return this;
        }
        
        public Builder successfulTasks(int successfulTasks) {
            this.successfulTasks = successfulTasks;
            return this;
        }
        
        public Builder reliability(double reliability) {
            this.reliability = reliability;
            return this;
        }
        
        public Builder lastUpdated(Instant lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }
        
        public Builder capabilityScores(Map<String, Double> capabilityScores) {
            this.capabilityScores = capabilityScores;
            return this;
        }
        
        public TrustMetrics build() {
            return new TrustMetrics(this);
        }
    }
}