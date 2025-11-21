package com.IDE.plugin.ai.multiagent.trust;

/**
 * TrustScore: Represents a trust score with detailed metrics
 */
public class TrustScore {
    private final TrustLevel level;
    private final double score;
    private final long timestamp;
    private final String reason;
    
    public TrustScore(TrustLevel level, double score) {
        this(level, score, null);
    }
    
    public TrustScore(TrustLevel level, double score, String reason) {
        this.level = level;
        this.score = score;
        this.reason = reason;
        this.timestamp = System.currentTimeMillis();
    }
    
    public TrustLevel getLevel() {
        return level;
    }
    
    public double getScore() {
        return score;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getReason() {
        return reason;
    }
    
    @Override
    public String toString() {
        return "TrustScore{" +
               "level=" + level +
               ", score=" + score +
               ", reason='" + reason + '\'' +
               '}';
    }
}