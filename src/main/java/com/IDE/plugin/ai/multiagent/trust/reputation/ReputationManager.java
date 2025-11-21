package com.IDE.plugin.ai.multiagent.trust.reputation;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.time.Instant;
import java.time.Duration;
import java.util.stream.Collectors;

/**
 * Central management system for agent reputation scores and trust metrics.
 * Implements a multi-factor reputation system with decay, recovery, and
 * behavioral analysis capabilities.
 */
public class ReputationManager {
    
    private final ConcurrentHashMap<String, AgentReputation> reputations;
    private final ConcurrentHashMap<String, List<TrustEvent>> trustHistory;
    private final ConcurrentHashMap<String, BehaviorProfile> behaviorProfiles;
    private final ReentrantReadWriteLock globalLock;
    private final ScheduledExecutorService scheduler;
    private final ReputationConfig config;
    private final AtomicLong eventCounter;
    
    // Reputation thresholds
    private static final double TRUSTED_THRESHOLD = 0.8;
    private static final double NEUTRAL_THRESHOLD = 0.5;
    private static final double SUSPICIOUS_THRESHOLD = 0.3;
    private static final double UNTRUSTED_THRESHOLD = 0.1;
    
    // Decay and recovery parameters
    private static final double DECAY_RATE = 0.01; // Per hour
    private static final double RECOVERY_RATE = 0.005; // Per hour
    private static final int HISTORY_RETENTION_DAYS = 30;
    private static final int UPDATE_INTERVAL_MINUTES = 5;
    
    public ReputationManager(ReputationConfig config) {
        this.config = config != null ? config : ReputationConfig.defaultConfig();
        this.reputations = new ConcurrentHashMap<>();
        this.trustHistory = new ConcurrentHashMap<>();
        this.behaviorProfiles = new ConcurrentHashMap<>();
        this.globalLock = new ReentrantReadWriteLock();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.eventCounter = new AtomicLong(0);
        
        initializeScheduledTasks();
    }
    
    /**
     * Records a trust event for an agent
     */
    public void recordTrustEvent(String agentId, TrustEventType type, 
                                double impact, Map<String, Object> metadata) {
        TrustEvent event = new TrustEvent(
            eventCounter.incrementAndGet(),
            agentId,
            type,
            impact,
            Instant.now(),
            metadata
        );
        
        // Add to history
        trustHistory.computeIfAbsent(agentId, k -> new CopyOnWriteArrayList<>())
                   .add(event);
        
        // Update reputation
        updateReputation(agentId, event);
        
        // Update behavior profile
        updateBehaviorProfile(agentId, event);
        
        // Check for anomalies
        detectAnomalies(agentId, event);
    }
    
    /**
     * Gets the current reputation score for an agent
     */
    public double getReputationScore(String agentId) {
        AgentReputation reputation = reputations.get(agentId);
        return reputation != null ? reputation.getScore() : config.getInitialReputation();
    }
    
    /**
     * Gets the trust level classification for an agent
     */
    public TrustLevel getTrustLevel(String agentId) {
        double score = getReputationScore(agentId);
        
        if (score >= TRUSTED_THRESHOLD) {
            return TrustLevel.TRUSTED;
        } else if (score >= NEUTRAL_THRESHOLD) {
            return TrustLevel.NEUTRAL;
        } else if (score >= SUSPICIOUS_THRESHOLD) {
            return TrustLevel.SUSPICIOUS;
        } else if (score >= UNTRUSTED_THRESHOLD) {
            return TrustLevel.UNTRUSTED;
        } else {
            return TrustLevel.BLACKLISTED;
        }
    }
    
    /**
     * Gets comprehensive trust metrics for an agent
     */
    public TrustMetrics getTrustMetrics(String agentId) {
        globalLock.readLock().lock();
        try {
            AgentReputation reputation = reputations.get(agentId);
            if (reputation == null) {
                return TrustMetrics.defaultMetrics(agentId);
            }
            
            List<TrustEvent> history = trustHistory.getOrDefault(agentId, new ArrayList<>());
            BehaviorProfile profile = behaviorProfiles.get(agentId);
            
            return calculateTrustMetrics(reputation, history, profile);
        } finally {
            globalLock.readLock().unlock();
        }
    }
    
    /**
     * Updates reputation based on a trust event
     */
    private void updateReputation(String agentId, TrustEvent event) {
        globalLock.writeLock().lock();
        try {
            AgentReputation reputation = reputations.computeIfAbsent(
                agentId, 
                k -> new AgentReputation(agentId, config.getInitialReputation())
            );
            
            // Calculate new score based on event type and impact
            double currentScore = reputation.getScore();
            double adjustment = calculateScoreAdjustment(event, currentScore);
            double newScore = Math.max(0.0, Math.min(1.0, currentScore + adjustment));
            
            reputation.updateScore(newScore, event.getType(), event.getTimestamp());
            
            // Update statistics
            reputation.incrementEventCount(event.getType());
            
            // Check for reputation milestones
            checkReputationMilestones(agentId, currentScore, newScore);
            
        } finally {
            globalLock.writeLock().unlock();
        }
    }
    
    /**
     * Calculates score adjustment based on event
     */
    private double calculateScoreAdjustment(TrustEvent event, double currentScore) {
        double baseAdjustment = event.getImpact() * getEventWeight(event.getType());
        
        // Apply diminishing returns for extreme scores
        if (currentScore > 0.9 && baseAdjustment > 0) {
            baseAdjustment *= (1.0 - currentScore);
        } else if (currentScore < 0.1 && baseAdjustment < 0) {
            baseAdjustment *= currentScore;
        }
        
        // Consider event frequency
        double frequencyMultiplier = getFrequencyMultiplier(event.getAgentId(), event.getType());
        
        return baseAdjustment * frequencyMultiplier;
    }
    
    /**
     * Gets weight multiplier for different event types
     */
    private double getEventWeight(TrustEventType type) {
        switch (type) {
            case SUCCESSFUL_OPERATION:
                return 0.05;
            case FAILED_OPERATION:
                return -0.1;
            case SECURITY_VIOLATION:
                return -0.3;
            case COLLABORATIVE_SUCCESS:
                return 0.1;
            case BYZANTINE_BEHAVIOR:
                return -0.5;
            case RECOVERY_ACTION:
                return 0.15;
            case TIMEOUT:
                return -0.05;
            case RESOURCE_ABUSE:
                return -0.2;
            default:
                return 0.0;
        }
    }
    
    /**
     * Updates behavior profile based on events
     */
    private void updateBehaviorProfile(String agentId, TrustEvent event) {
        BehaviorProfile profile = behaviorProfiles.computeIfAbsent(
            agentId,
            k -> new BehaviorProfile(agentId)
        );
        
        profile.addBehaviorSample(event);
        
        // Analyze patterns
        if (profile.getSampleCount() % 10 == 0) {
            analyzeBehaviorPatterns(profile);
        }
    }
    
    /**
     * Detects anomalous behavior
     */
    private void detectAnomalies(String agentId, TrustEvent event) {
        BehaviorProfile profile = behaviorProfiles.get(agentId);
        if (profile == null || profile.getSampleCount() < 20) {
            return; // Not enough data
        }
        
        // Check for sudden behavior changes
        if (profile.detectAnomaly(event)) {
            recordTrustEvent(
                agentId,
                TrustEventType.ANOMALY_DETECTED,
                -0.1,
                Map.of("originalEvent", event.getType())
            );
        }
    }
    
    /**
     * Analyzes behavior patterns for an agent
     */
    private void analyzeBehaviorPatterns(BehaviorProfile profile) {
        // Detect cyclic patterns
        List<TrustEventType> recentEvents = profile.getRecentEventTypes(20);
        Map<String, Integer> patterns = findPatterns(recentEvents);
        
        // Update profile with detected patterns
        profile.updatePatterns(patterns);
        
        // Check for suspicious patterns
        for (Map.Entry<String, Integer> pattern : patterns.entrySet()) {
            if (isSuspiciousPattern(pattern.getKey()) && pattern.getValue() > 3) {
                recordTrustEvent(
                    profile.getAgentId(),
                    TrustEventType.SUSPICIOUS_PATTERN,
                    -0.15,
                    Map.of("pattern", pattern.getKey(), "count", pattern.getValue())
                );
            }
        }
    }
    
    /**
     * Finds repeating patterns in event sequences
     */
    private Map<String, Integer> findPatterns(List<TrustEventType> events) {
        Map<String, Integer> patterns = new HashMap<>();
        
        // Look for patterns of length 2-4
        for (int len = 2; len <= 4 && len <= events.size() / 2; len++) {
            for (int i = 0; i <= events.size() - len; i++) {
                String pattern = events.subList(i, i + len).stream()
                    .map(Enum::name)
                    .collect(Collectors.joining("-"));
                
                patterns.merge(pattern, 1, Integer::sum);
            }
        }
        
        // Filter out non-repeating patterns
        patterns.entrySet().removeIf(e -> e.getValue() < 2);
        
        return patterns;
    }
    
    /**
     * Checks if a pattern is suspicious
     */
    private boolean isSuspiciousPattern(String pattern) {
        return pattern.contains("FAILED_OPERATION") && 
               pattern.contains("SUCCESSFUL_OPERATION") &&
               pattern.split("-").length >= 3;
    }
    
    /**
     * Calculates frequency multiplier for events
     */
    private double getFrequencyMultiplier(String agentId, TrustEventType type) {
        List<TrustEvent> recentEvents = getRecentEvents(agentId, Duration.ofHours(1));
        long typeCount = recentEvents.stream()
            .filter(e -> e.getType() == type)
            .count();
        
        // Reduce impact of repeated events
        if (typeCount > 5) {
            return 0.5;
        } else if (typeCount > 3) {
            return 0.7;
        }
        return 1.0;
    }
    
    /**
     * Gets recent events for an agent
     */
    private List<TrustEvent> getRecentEvents(String agentId, Duration duration) {
        List<TrustEvent> history = trustHistory.get(agentId);
        if (history == null) {
            return Collections.emptyList();
        }
        
        Instant cutoff = Instant.now().minus(duration);
        return history.stream()
            .filter(e -> e.getTimestamp().isAfter(cutoff))
            .collect(Collectors.toList());
    }
    
    /**
     * Calculates comprehensive trust metrics
     */
    private TrustMetrics calculateTrustMetrics(AgentReputation reputation,
                                              List<TrustEvent> history,
                                              BehaviorProfile profile) {
        // Calculate various metrics
        double volatility = calculateVolatility(history);
        double reliability = calculateReliability(history);
        double consistency = profile != null ? profile.getConsistencyScore() : 0.5;
        int totalInteractions = history.size();
        Map<TrustEventType, Integer> eventDistribution = calculateEventDistribution(history);
        
        return new TrustMetrics(
            reputation.getAgentId(),
            reputation.getScore(),
            getTrustLevel(reputation.getAgentId()),
            volatility,
            reliability,
            consistency,
            totalInteractions,
            reputation.getLastUpdate(),
            eventDistribution
        );
    }
    
    /**
     * Calculates reputation volatility
     */
    private double calculateVolatility(List<TrustEvent> history) {
        if (history.size() < 10) {
            return 0.5; // Default for insufficient data
        }
        
        // Calculate standard deviation of impacts
        double mean = history.stream()
            .mapToDouble(TrustEvent::getImpact)
            .average()
            .orElse(0.0);
        
        double variance = history.stream()
            .mapToDouble(e -> Math.pow(e.getImpact() - mean, 2))
            .average()
            .orElse(0.0);
        
        return Math.min(1.0, Math.sqrt(variance));
    }
    
    /**
     * Calculates agent reliability
     */
    private double calculateReliability(List<TrustEvent> history) {
        if (history.isEmpty()) {
            return 0.5;
        }
        
        long successCount = history.stream()
            .filter(e -> e.getType() == TrustEventType.SUCCESSFUL_OPERATION ||
                        e.getType() == TrustEventType.COLLABORATIVE_SUCCESS)
            .count();
        
        long failureCount = history.stream()
            .filter(e -> e.getType() == TrustEventType.FAILED_OPERATION ||
                        e.getType() == TrustEventType.TIMEOUT)
            .count();
        
        if (successCount + failureCount == 0) {
            return 0.5;
        }
        
        return (double) successCount / (successCount + failureCount);
    }
    
    /**
     * Calculates event distribution
     */
    private Map<TrustEventType, Integer> calculateEventDistribution(List<TrustEvent> history) {
        return history.stream()
            .collect(Collectors.groupingBy(
                TrustEvent::getType,
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
    }
    
    /**
     * Checks for reputation milestones
     */
    private void checkReputationMilestones(String agentId, double oldScore, double newScore) {
        TrustLevel oldLevel = getTrustLevelForScore(oldScore);
        TrustLevel newLevel = getTrustLevelForScore(newScore);
        
        if (oldLevel != newLevel) {
            // Trust level changed
            System.out.println("Agent " + agentId + " trust level changed from " + 
                             oldLevel + " to " + newLevel);
            
            // Trigger appropriate actions
            if (newLevel == TrustLevel.BLACKLISTED) {
                initiateQuarantine(agentId);
            } else if (newLevel == TrustLevel.TRUSTED && oldLevel != TrustLevel.TRUSTED) {
                grantAdditionalPrivileges(agentId);
            }
        }
    }
    
    private TrustLevel getTrustLevelForScore(double score) {
        if (score >= TRUSTED_THRESHOLD) return TrustLevel.TRUSTED;
        if (score >= NEUTRAL_THRESHOLD) return TrustLevel.NEUTRAL;
        if (score >= SUSPICIOUS_THRESHOLD) return TrustLevel.SUSPICIOUS;
        if (score >= UNTRUSTED_THRESHOLD) return TrustLevel.UNTRUSTED;
        return TrustLevel.BLACKLISTED;
    }
    
    /**
     * Applies reputation decay over time
     */
    private void applyReputationDecay() {
        globalLock.writeLock().lock();
        try {
            Instant now = Instant.now();
            
            for (AgentReputation reputation : reputations.values()) {
                Duration timeSinceUpdate = Duration.between(reputation.getLastUpdate(), now);
                double hoursElapsed = timeSinceUpdate.toMinutes() / 60.0;
                
                if (hoursElapsed > 0) {
                    double currentScore = reputation.getScore();
                    double targetScore = config.getInitialReputation();
                    
                    // Decay towards neutral
                    double decay = DECAY_RATE * hoursElapsed;
                    double newScore;
                    
                    if (currentScore > targetScore) {
                        newScore = Math.max(targetScore, currentScore - decay);
                    } else if (currentScore < targetScore) {
                        newScore = Math.min(targetScore, currentScore + decay * RECOVERY_RATE / DECAY_RATE);
                    } else {
                        continue; // Already at target
                    }
                    
                    reputation.updateScore(newScore, TrustEventType.REPUTATION_DECAY, now);
                }
            }
        } finally {
            globalLock.writeLock().unlock();
        }
    }
    
    /**
     * Cleans up old trust history
     */
    private void cleanupOldHistory() {
        Instant cutoff = Instant.now().minus(Duration.ofDays(HISTORY_RETENTION_DAYS));
        
        for (List<TrustEvent> history : trustHistory.values()) {
            history.removeIf(event -> event.getTimestamp().isBefore(cutoff));
        }
    }
    
    /**
     * Initializes scheduled maintenance tasks
     */
    private void initializeScheduledTasks() {
        // Apply reputation decay
        scheduler.scheduleAtFixedRate(
            this::applyReputationDecay,
            UPDATE_INTERVAL_MINUTES,
            UPDATE_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        );
        
        // Cleanup old history
        scheduler.scheduleAtFixedRate(
            this::cleanupOldHistory,
            1,
            24,
            TimeUnit.HOURS
        );
    }
    
    private void initiateQuarantine(String agentId) {
        // Implement quarantine logic
        System.err.println("Initiating quarantine for agent: " + agentId);
    }
    
    private void grantAdditionalPrivileges(String agentId) {
        // Implement privilege granting logic
        System.out.println("Granting additional privileges to agent: " + agentId);
    }
    
    /**
     * Exports reputation data for persistence or analysis
     */
    public Map<String, ReputationSnapshot> exportReputations() {
        globalLock.readLock().lock();
        try {
            Map<String, ReputationSnapshot> snapshots = new HashMap<>();
            
            for (Map.Entry<String, AgentReputation> entry : reputations.entrySet()) {
                AgentReputation rep = entry.getValue();
                TrustMetrics metrics = getTrustMetrics(entry.getKey());
                
                snapshots.put(entry.getKey(), new ReputationSnapshot(
                    rep.getAgentId(),
                    rep.getScore(),
                    rep.getLastUpdate(),
                    metrics
                ));
            }
            
            return snapshots;
        } finally {
            globalLock.readLock().unlock();
        }
    }
    
    /**
     * Imports reputation data
     */
    public void importReputations(Map<String, ReputationSnapshot> snapshots) {
        globalLock.writeLock().lock();
        try {
            for (ReputationSnapshot snapshot : snapshots.values()) {
                AgentReputation reputation = new AgentReputation(
                    snapshot.getAgentId(),
                    snapshot.getScore()
                );
                reputation.setLastUpdate(snapshot.getLastUpdate());
                reputations.put(snapshot.getAgentId(), reputation);
            }
        } finally {
            globalLock.writeLock().unlock();
        }
    }
    
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
    
    // Inner classes
    private static class AgentReputation {
        private final String agentId;
        private double score;
        private Instant lastUpdate;
        private final Map<TrustEventType, Integer> eventCounts;
        
        public AgentReputation(String agentId, double initialScore) {
            this.agentId = agentId;
            this.score = initialScore;
            this.lastUpdate = Instant.now();
            this.eventCounts = new ConcurrentHashMap<>();
        }
        
        public synchronized void updateScore(double newScore, TrustEventType event, Instant timestamp) {
            this.score = newScore;
            this.lastUpdate = timestamp;
        }
        
        public synchronized void incrementEventCount(TrustEventType type) {
            eventCounts.merge(type, 1, Integer::sum);
        }
        
        // Getters
        public String getAgentId() { return agentId; }
        public double getScore() { return score; }
        public Instant getLastUpdate() { return lastUpdate; }
        public void setLastUpdate(Instant lastUpdate) { this.lastUpdate = lastUpdate; }
    }
    
    private static class BehaviorProfile {
        private final String agentId;
        private final List<TrustEvent> recentEvents;
        private final Map<String, Integer> patterns;
        private double consistencyScore;
        private int sampleCount;
        
        public BehaviorProfile(String agentId) {
            this.agentId = agentId;
            this.recentEvents = new LinkedList<>();
            this.patterns = new ConcurrentHashMap<>();
            this.consistencyScore = 0.5;
            this.sampleCount = 0;
        }
        
        public void addBehaviorSample(TrustEvent event) {
            recentEvents.add(event);
            if (recentEvents.size() > 100) {
                recentEvents.remove(0);
            }
            sampleCount++;
            updateConsistencyScore();
        }
        
        public boolean detectAnomaly(TrustEvent event) {
            if (recentEvents.size() < 10) {
                return false;
            }
            
            // Simple anomaly detection based on impact deviation
            double avgImpact = recentEvents.stream()
                .mapToDouble(TrustEvent::getImpact)
                .average()
                .orElse(0.0);
            
            double stdDev = Math.sqrt(recentEvents.stream()
                .mapToDouble(e -> Math.pow(e.getImpact() - avgImpact, 2))
                .average()
                .orElse(0.0));
            
            return Math.abs(event.getImpact() - avgImpact) > 2 * stdDev;
        }
        
        public List<TrustEventType> getRecentEventTypes(int count) {
            return recentEvents.stream()
                .skip(Math.max(0, recentEvents.size() - count))
                .map(TrustEvent::getType)
                .collect(Collectors.toList());
        }
        
        public void updatePatterns(Map<String, Integer> newPatterns) {
            patterns.clear();
            patterns.putAll(newPatterns);
        }
        
        private void updateConsistencyScore() {
            // Calculate consistency based on event type distribution
            if (recentEvents.size() < 10) {
                return;
            }
            
            Map<TrustEventType, Long> distribution = recentEvents.stream()
                .collect(Collectors.groupingBy(TrustEvent::getType, Collectors.counting()));
            
            double entropy = distribution.values().stream()
                .mapToDouble(count -> {
                    double p = (double) count / recentEvents.size();
                    return -p * Math.log(p) / Math.log(2);
                })
                .sum();
            
            // Normalize entropy to [0, 1] where 1 is most consistent
            double maxEntropy = Math.log(TrustEventType.values().length) / Math.log(2);
            consistencyScore = 1.0 - (entropy / maxEntropy);
        }
        
        // Getters
        public String getAgentId() { return agentId; }
        public double getConsistencyScore() { return consistencyScore; }
        public int getSampleCount() { return sampleCount; }
    }
    
    public static class TrustEvent {
        private final long eventId;
        private final String agentId;
        private final TrustEventType type;
        private final double impact;
        private final Instant timestamp;
        private final Map<String, Object> metadata;
        
        public TrustEvent(long eventId, String agentId, TrustEventType type,
                         double impact, Instant timestamp, Map<String, Object> metadata) {
            this.eventId = eventId;
            this.agentId = agentId;
            this.type = type;
            this.impact = impact;
            this.timestamp = timestamp;
            this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        }
        
        // Getters
        public String getAgentId() { return agentId; }
        public TrustEventType getType() { return type; }
        public double getImpact() { return impact; }
        public Instant getTimestamp() { return timestamp; }
    }
    
    public enum TrustEventType {
        SUCCESSFUL_OPERATION,
        FAILED_OPERATION,
        SECURITY_VIOLATION,
        COLLABORATIVE_SUCCESS,
        BYZANTINE_BEHAVIOR,
        RECOVERY_ACTION,
        TIMEOUT,
        RESOURCE_ABUSE,
        ANOMALY_DETECTED,
        SUSPICIOUS_PATTERN,
        REPUTATION_DECAY
    }
    
    public enum TrustLevel {
        TRUSTED,
        NEUTRAL,
        SUSPICIOUS,
        UNTRUSTED,
        BLACKLISTED
    }
    
    public static class TrustMetrics {
        private final String agentId;
        private final double reputationScore;
        private final TrustLevel trustLevel;
        private final double volatility;
        private final double reliability;
        private final double consistency;
        private final int totalInteractions;
        private final Instant lastUpdate;
        private final Map<TrustEventType, Integer> eventDistribution;
        
        public TrustMetrics(String agentId, double reputationScore, TrustLevel trustLevel,
                           double volatility, double reliability, double consistency,
                           int totalInteractions, Instant lastUpdate,
                           Map<TrustEventType, Integer> eventDistribution) {
            this.agentId = agentId;
            this.reputationScore = reputationScore;
            this.trustLevel = trustLevel;
            this.volatility = volatility;
            this.reliability = reliability;
            this.consistency = consistency;
            this.totalInteractions = totalInteractions;
            this.lastUpdate = lastUpdate;
            this.eventDistribution = new HashMap<>(eventDistribution);
        }
        
        public static TrustMetrics defaultMetrics(String agentId) {
            return new TrustMetrics(
                agentId, 0.5, TrustLevel.NEUTRAL, 0.5, 0.5, 0.5, 0,
                Instant.now(), new HashMap<>()
            );
        }
        
        // Getters
        public double getReputationScore() { return reputationScore; }
        public TrustLevel getTrustLevel() { return trustLevel; }
        public double getReliability() { return reliability; }
    }
    
    public static class ReputationSnapshot {
        private final String agentId;
        private final double score;
        private final Instant lastUpdate;
        private final TrustMetrics metrics;
        
        public ReputationSnapshot(String agentId, double score, 
                                Instant lastUpdate, TrustMetrics metrics) {
            this.agentId = agentId;
            this.score = score;
            this.lastUpdate = lastUpdate;
            this.metrics = metrics;
        }
        
        // Getters
        public String getAgentId() { return agentId; }
        public double getScore() { return score; }
        public Instant getLastUpdate() { return lastUpdate; }
    }
    
    public static class ReputationConfig {
        private final double initialReputation;
        private final double minReputation;
        private final double maxReputation;
        
        public ReputationConfig(double initialReputation, double minReputation, double maxReputation) {
            this.initialReputation = initialReputation;
            this.minReputation = minReputation;
            this.maxReputation = maxReputation;
        }
        
        public static ReputationConfig defaultConfig() {
            return new ReputationConfig(0.5, 0.0, 1.0);
        }
        
        public double getInitialReputation() { return initialReputation; }
    }
}