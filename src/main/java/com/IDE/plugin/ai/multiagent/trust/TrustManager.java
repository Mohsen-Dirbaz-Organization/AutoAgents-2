package com.IDE.plugin.ai.multiagent.trust;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * TrustManager handles trust scores and reputation management between agents
 */
public class TrustManager {
    private final Map<String, Double> trustScores = new ConcurrentHashMap<>();
    private final Map<String, TrustMetrics> trustMetrics = new ConcurrentHashMap<>();
    
    private static final double DEFAULT_TRUST = 0.5;
    private static final double MIN_TRUST = 0.0;
    private static final double MAX_TRUST = 1.0;
    
    /**
     * Get the trust score for an agent
     */
    public double getTrustScore(String agentId) {
        return trustScores.getOrDefault(agentId, DEFAULT_TRUST);
    }
    
    /**
     * Update trust score based on interaction outcome
     */
    public void updateTrust(String agentId, boolean successful) {
        double currentTrust = getTrustScore(agentId);
        double adjustment = successful ? 0.1 : -0.2; // Penalize failures more
        
        double newTrust = Math.max(MIN_TRUST, Math.min(MAX_TRUST, currentTrust + adjustment));
        trustScores.put(agentId, newTrust);
        
        // Update metrics
        TrustMetrics metrics = trustMetrics.computeIfAbsent(agentId, k -> new TrustMetrics());
        metrics.recordInteraction(successful);
    }
    
    /**
     * Set explicit trust score for an agent
     */
    public void setTrustScore(String agentId, double score) {
        if (score < MIN_TRUST || score > MAX_TRUST) {
            throw new IllegalArgumentException("Trust score must be between 0.0 and 1.0");
        }
        trustScores.put(agentId, score);
    }
    
    /**
     * Check if an agent is trusted (above threshold)
     */
    public boolean isTrusted(String agentId) {
        return getTrustScore(agentId) >= 0.6;
    }
    
    /**
     * Get trust metrics for an agent
     */
    public TrustMetrics getMetrics(String agentId) {
        return trustMetrics.getOrDefault(agentId, new TrustMetrics());
    }
    
    /**
     * Reset trust for an agent
     */
    public void resetTrust(String agentId) {
        trustScores.put(agentId, DEFAULT_TRUST);
        trustMetrics.remove(agentId);
    }
    
    /**
     * Clear all trust data
     */
    public void clear() {
        trustScores.clear();
        trustMetrics.clear();
    }
    
    /**
     * Inner class to track trust metrics
     */
    public static class TrustMetrics {
        private int totalInteractions = 0;
        private int successfulInteractions = 0;
        private long lastInteractionTime = System.currentTimeMillis();
        
        public void recordInteraction(boolean successful) {
            totalInteractions++;
            if (successful) {
                successfulInteractions++;
            }
            lastInteractionTime = System.currentTimeMillis();
        }
        
        public double getSuccessRate() {
            return totalInteractions == 0 ? 0.0 : 
                   (double) successfulInteractions / totalInteractions;
        }
        
        public int getTotalInteractions() {
            return totalInteractions;
        }
        
        public int getSuccessfulInteractions() {
            return successfulInteractions;
        }
        
        public long getLastInteractionTime() {
            return lastInteractionTime;
        }
    }
}