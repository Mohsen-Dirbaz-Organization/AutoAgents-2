package com.IDE.plugin.ai.multiagent.mechanical.validation;

import java.util.List;
import java.util.Map;

/**
 * Supporting model classes for validation island
 */

// Validation Result Models
class SignalValidationResult {
    private final String signalId;
    private final boolean valid;
    private final boolean integrityValid;
    private final boolean authenticityValid;
    private final long validationTime;
    private final String report;
    private final long timestamp;
    
    public SignalValidationResult(String signalId, boolean valid, 
                                boolean integrityValid, boolean authenticityValid,
                                long validationTime, String report) {
        this.signalId = signalId;
        this.valid = valid;
        this.integrityValid = integrityValid;
        this.authenticityValid = authenticityValid;
        this.validationTime = validationTime;
        this.report = report;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters
    public String getSignalId() { return signalId; }
    public boolean isValid() { return valid; }
    public boolean isIntegrityValid() { return integrityValid; }
    public boolean isAuthenticityValid() { return authenticityValid; }
    public long getValidationTime() { return validationTime; }
    public String getReport() { return report; }
    public long getTimestamp() { return timestamp; }
}

// Analysis Result Models
class SignalAnalysisResult {
    private final String signalId;
    private final PatternAnalysisResult patterns;
    private final OptimizationResult optimization;
    private final AnomalyResult anomalies;
    private final List<String> recommendations;
    private final String errorMessage;
    
    public SignalAnalysisResult(String signalId, PatternAnalysisResult patterns,
                              OptimizationResult optimization, AnomalyResult anomalies,
                              List<String> recommendations) {
        this.signalId = signalId;
        this.patterns = patterns;
        this.optimization = optimization;
        this.anomalies = anomalies;
        this.recommendations = recommendations;
        this.errorMessage = null;
    }
    
    public SignalAnalysisResult(String signalId, String errorMessage) {
        this.signalId = signalId;
        this.patterns = null;
        this.optimization = null;
        this.anomalies = null;
        this.recommendations = null;
        this.errorMessage = errorMessage;
    }
    
    // Getters
    public String getSignalId() { return signalId; }
    public boolean hasPatterns() { return patterns != null && patterns.hasPatterns(); }
    public int getPatternCount() { return patterns != null ? patterns.getPatternCount() : 0; }
    public boolean hasOptimizations() { return optimization != null && optimization.canOptimize(); }
    public int getOptimizationPotential() { return optimization != null ? optimization.getCompressionRatio() : 0; }
    public boolean hasAnomalies() { return anomalies != null && anomalies.hasAnomalies(); }
    public int getAnomalyCount() { return anomalies != null ? anomalies.getAnomalyCount() : 0; }
    public List<String> getRecommendations() { return recommendations; }
}

// Pattern Analysis Models
class PatternAnalysisResult {
    private final List<Pattern> patterns;
    
    public PatternAnalysisResult(List<Pattern> patterns) {
        this.patterns = patterns;
    }
    
    public boolean hasPatterns() { return !patterns.isEmpty(); }
    public boolean hasRepeatingPatterns() { 
        return patterns.stream().anyMatch(p -> p.type == PatternType.REPEATING);
    }
    public int getPatternCount() { return patterns.size(); }
}

class Pattern {
    final PatternType type;
    final String description;
    final double confidence;
    
    Pattern(PatternType type, String description, double confidence) {
        this.type = type;
        this.description = description;
        this.confidence = confidence;
    }
}

enum PatternType {
    REPEATING, TEMPORAL, SPATIAL, BEHAVIORAL
}

// Optimization Models
class OptimizationResult {
    private final boolean canOptimize;
    private final int compressionRatio;
    private final boolean canOptimizeRouting;
    private final boolean canBatch;
    
    public OptimizationResult(boolean canOptimize, int compressionRatio,
                            boolean canOptimizeRouting, boolean canBatch) {
        this.canOptimize = canOptimize;
        this.compressionRatio = compressionRatio;
        this.canOptimizeRouting = canOptimizeRouting;
        this.canBatch = canBatch;
    }
    
    public boolean canOptimize() { return canOptimize; }
    public int getCompressionRatio() { return compressionRatio; }
}

class OptimizationOpportunity {
    final String agentId;
    final String description;
    final OptimizationType type;
    
    OptimizationOpportunity(String agentId, String description, OptimizationType type) {
        this.agentId = agentId;
        this.description = description;
        this.type = type;
    }
}

enum OptimizationType {
    COMPRESSION, BATCHING, ROUTING, CACHING
}

// Anomaly Models
class AnomalyResult {
    private final List<Anomaly> anomalies;
    
    public AnomalyResult(List<Anomaly> anomalies) {
        this.anomalies = anomalies;
    }
    
    public boolean hasAnomalies() { return !anomalies.isEmpty(); }
    public int getAnomalyCount() { return anomalies.size(); }
}

class Anomaly {
    final AnomalyType type;
    final String description;
    final double severity;
    
    Anomaly(AnomalyType type, String description, double severity) {
        this.type = type;
        this.description = description;
        this.severity = severity;
    }
}

enum AnomalyType {
    PAYLOAD_SIZE, FREQUENCY, PATTERN, TIMING, SEQUENCE
}

// Combined Result Models
class ValidationAnalysisResult {
    private final String signalId;
    private final SignalValidationResult validationResult;
    private final SignalAnalysisResult analysisResult;
    private final SignalValidationCoordinator.CoordinationReport report;
    private final long timestamp;
    
    public ValidationAnalysisResult(String signalId, 
                                  SignalValidationResult validationResult,
                                  SignalAnalysisResult analysisResult,
                                  SignalValidationCoordinator.CoordinationReport report) {
        this.signalId = signalId;
        this.validationResult = validationResult;
        this.analysisResult = analysisResult;
        this.report = report;
        this.timestamp = System.currentTimeMillis();
    }
    
    public boolean isValid() { return validationResult.isValid(); }
    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp > 300000; // 5 minutes
    }
    
    // Getters
    public String getSignalId() { return signalId; }
    public SignalValidationResult getValidationResult() { return validationResult; }
    public SignalAnalysisResult getAnalysisResult() { return analysisResult; }
    public SignalValidationCoordinator.CoordinationReport getReport() { return report; }
    public long getTimestamp() { return timestamp; }
}

// Statistics Models
class ValidationStatistics {
    final long totalValidations;
    final long successfulValidations;
    final long failedValidations;
    final double averageValidationTime;
    final double cacheHitRate;
    
    public ValidationStatistics(long totalValidations, long successfulValidations,
                              long failedValidations, double averageValidationTime,
                              double cacheHitRate) {
        this.totalValidations = totalValidations;
        this.successfulValidations = successfulValidations;
        this.failedValidations = failedValidations;
        this.averageValidationTime = averageValidationTime;
        this.cacheHitRate = cacheHitRate;
    }
}

class CoordinationStatistics {
    final long totalProcessed;
    final long successfulValidations;
    final int queueSize;
    final int cacheSize;
    final long reportCount;
    
    public CoordinationStatistics(long totalProcessed, long successfulValidations,
                                int queueSize, int cacheSize, long reportCount) {
        this.totalProcessed = totalProcessed;
        this.successfulValidations = successfulValidations;
        this.queueSize = queueSize;
        this.cacheSize = cacheSize;
        this.reportCount = reportCount;
    }
}

// Report Models
class AnalysisReport {
    final Map<String, List<Pattern>> globalPatterns;
    final List<OptimizationOpportunity> opportunities;
    
    AnalysisReport(Map<String, List<Pattern>> globalPatterns,
                  List<OptimizationOpportunity> opportunities) {
        this.globalPatterns = globalPatterns;
        this.opportunities = opportunities;
    }
}

class ValidationReport {
    final String signalId;
    final boolean valid;
    final long timestamp;
    final String details;
    
    ValidationReport(String signalId, boolean valid, long timestamp, String details) {
        this.signalId = signalId;
        this.valid = valid;
        this.timestamp = timestamp;
        this.details = details;
    }
}