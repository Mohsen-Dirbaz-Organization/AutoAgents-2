package com.IDE.plugin.ai.multiagent.mechanical.validation;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SignalValidationCoordinator: Coordinates validation and analysis activities
 * Logic: 45% validation orchestration, 35% analysis coordination, 20% reporting
 */
public class SignalValidationCoordinator {
    
    private final MechanicalSignalValidator validator;
    private final MechanicalSignalAnalyzer analyzer;
    private final ExecutorService coordinationExecutor;
    private final Map<String, ValidationAnalysisResult> resultCache;
    private final BlockingQueue<ValidationTask> validationQueue;
    private final List<ValidationListener> listeners;
    private final ValidationReporter reporter;
    private volatile boolean running;
    
    private final AtomicLong totalProcessed = new AtomicLong(0);
    private final AtomicLong successfulValidations = new AtomicLong(0);
    
    public SignalValidationCoordinator(MechanicalSignalValidator validator, 
                                     MechanicalSignalAnalyzer analyzer) {
        this.validator = validator;
        this.analyzer = analyzer;
        this.coordinationExecutor = Executors.newFixedThreadPool(4);
        this.resultCache = new ConcurrentHashMap<>();
        this.validationQueue = new LinkedBlockingQueue<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.reporter = new ValidationReporter();
        this.running = true;
        
        // Start processing thread
        coordinationExecutor.submit(this::processValidationTasks);
    }
    
    /**
     * Coordinates validation and analysis of a mechanical signal
     */
    public CompletableFuture<ValidationAnalysisResult> validateAndAnalyze(MechanicalSignal signal) {
        CompletableFuture<ValidationAnalysisResult> future = new CompletableFuture<>();
        
        // Check cache first
        ValidationAnalysisResult cached = resultCache.get(signal.getId());
        if (cached != null && !cached.isExpired()) {
            future.complete(cached);
            return future;
        }
        
        // Queue validation task
        ValidationTask task = new ValidationTask(signal, future);
        validationQueue.offer(task);
        
        return future;
    }
    
    /**
     * Processes validation tasks from queue
     */
    private void processValidationTasks() {
        while (running) {
            try {
                ValidationTask task = validationQueue.poll(100, TimeUnit.MILLISECONDS);
                if (task != null) {
                    processTask(task);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                // Log error
            }
        }
    }
    
    /**
     * Processes a single validation task
     */
    private void processTask(ValidationTask task) {
        MechanicalSignal signal = task.getSignal();
        CompletableFuture<ValidationAnalysisResult> future = task.getFuture();
        
        try {
            // Validation orchestration (45% of logic)
            CompletableFuture<SignalValidationResult> validationFuture = 
                CompletableFuture.supplyAsync(() -> validator.validateSignal(signal), coordinationExecutor);
            
            // Analysis coordination (35% of logic)
            CompletableFuture<SignalAnalysisResult> analysisFuture = 
                CompletableFuture.supplyAsync(() -> analyzer.analyzeSignal(signal), coordinationExecutor);
            
            // Combine results
            CompletableFuture.allOf(validationFuture, analysisFuture).whenComplete((v, ex) -> {
                if (ex != null) {
                    future.completeExceptionally(ex);
                    return;
                }
                
                try {
                    SignalValidationResult validationResult = validationFuture.get();
                    SignalAnalysisResult analysisResult = analysisFuture.get();
                    
                    // Create combined result
                    ValidationAnalysisResult combinedResult = new ValidationAnalysisResult(
                        signal.getId(),
                        validationResult,
                        analysisResult,
                        generateCoordinationReport(validationResult, analysisResult)
                    );
                    
                    // Update statistics
                    totalProcessed.incrementAndGet();
                    if (validationResult.isValid()) {
                        successfulValidations.incrementAndGet();
                    }
                    
                    // Cache result
                    resultCache.put(signal.getId(), combinedResult);
                    
                    // Notify listeners
                    notifyListeners(combinedResult);
                    
                    // Complete future
                    future.complete(combinedResult);
                    
                    // Reporting (20% of logic)
                    reporter.recordResult(combinedResult);
                    
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            });
            
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
    }
    
    /**
     * Generates coordination report combining validation and analysis
     */
    private CoordinationReport generateCoordinationReport(SignalValidationResult validation,
                                                        SignalAnalysisResult analysis) {
        CoordinationReport report = new CoordinationReport();
        
        // Add validation summary
        report.addSection("Validation", validation.isValid() ? "PASSED" : "FAILED");
        report.addDetail("Integrity", validation.isIntegrityValid() ? "Valid" : "Invalid");
        report.addDetail("Authenticity", validation.isAuthenticityValid() ? "Valid" : "Invalid");
        report.addDetail("Validation Time", validation.getValidationTime() + " ns");
        
        // Add analysis summary
        if (analysis.hasPatterns()) {
            report.addSection("Patterns Detected", String.valueOf(analysis.getPatternCount()));
        }
        
        if (analysis.hasOptimizations()) {
            report.addSection("Optimizations Available", "Yes");
            report.addDetail("Potential Savings", analysis.getOptimizationPotential() + "%");
        }
        
        if (analysis.hasAnomalies()) {
            report.addSection("Anomalies", String.valueOf(analysis.getAnomalyCount()));
        }
        
        // Add recommendations
        List<String> recommendations = combineRecommendations(validation, analysis);
        if (!recommendations.isEmpty()) {
            report.addSection("Recommendations", String.join("; ", recommendations));
        }
        
        return report;
    }
    
    /**
     * Combines recommendations from validation and analysis
     */
    private List<String> combineRecommendations(SignalValidationResult validation,
                                               SignalAnalysisResult analysis) {
        List<String> recommendations = new ArrayList<>();
        
        if (!validation.isValid()) {
            recommendations.add("Review signal integrity and authentication");
        }
        
        if (analysis.getRecommendations() != null) {
            recommendations.addAll(analysis.getRecommendations());
        }
        
        return recommendations;
    }
    
    /**
     * Notifies registered listeners of validation results
     */
    private void notifyListeners(ValidationAnalysisResult result) {
        for (ValidationListener listener : listeners) {
            try {
                listener.onValidationComplete(result);
            } catch (Exception e) {
                // Log error but continue
            }
        }
    }
    
    /**
     * Registers a validation listener
     */
    public void addListener(ValidationListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes a validation listener
     */
    public void removeListener(ValidationListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Gets current statistics
     */
    public CoordinationStatistics getStatistics() {
        return new CoordinationStatistics(
            totalProcessed.get(),
            successfulValidations.get(),
            validationQueue.size(),
            resultCache.size(),
            reporter.getReportCount()
        );
    }
    
    /**
     * Performs batch validation and analysis
     */
    public CompletableFuture<List<ValidationAnalysisResult>> validateBatch(List<MechanicalSignal> signals) {
        List<CompletableFuture<ValidationAnalysisResult>> futures = signals.stream()
            .map(this::validateAndAnalyze)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll));
    }
    
    /**
     * Clears the result cache
     */
    public void clearCache() {
        resultCache.clear();
    }
    
    /**
     * Shuts down the coordinator
     */
    public void shutdown() {
        running = false;
        coordinationExecutor.shutdown();
        
        try {
            if (!coordinationExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                coordinationExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            coordinationExecutor.shutdownNow();
        }
    }
    
    /**
     * Inner class for validation tasks
     */
    private static class ValidationTask {
        private final MechanicalSignal signal;
        private final CompletableFuture<ValidationAnalysisResult> future;
        
        ValidationTask(MechanicalSignal signal, CompletableFuture<ValidationAnalysisResult> future) {
            this.signal = signal;
            this.future = future;
        }
        
        MechanicalSignal getSignal() { return signal; }
        CompletableFuture<ValidationAnalysisResult> getFuture() { return future; }
    }
    
    /**
     * Inner class for validation reporting
     */
    private static class ValidationReporter {
        private final AtomicLong reportCount = new AtomicLong(0);
        private final List<ValidationReport> recentReports = new CopyOnWriteArrayList<>();
        
        void recordResult(ValidationAnalysisResult result) {
            reportCount.incrementAndGet();
            
            // Create report
            ValidationReport report = new ValidationReport(
                result.getSignalId(),
                result.isValid(),
                result.getTimestamp(),
                result.getReport()
            );
            
            // Keep only recent reports
            recentReports.add(report);
            if (recentReports.size() > 1000) {
                recentReports.remove(0);
            }
        }
        
        long getReportCount() {
            return reportCount.get();
        }
        
        List<ValidationReport> getRecentReports() {
            return new ArrayList<>(recentReports);
        }
    }
    
    /**
     * Interface for validation listeners
     */
    public interface ValidationListener {
        void onValidationComplete(ValidationAnalysisResult result);
    }
    
    /**
     * Coordination report class
     */
    public static class CoordinationReport {
        private final Map<String, String> sections = new LinkedHashMap<>();
        private final Map<String, List<String>> details = new LinkedHashMap<>();
        
        void addSection(String name, String value) {
            sections.put(name, value);
        }
        
        void addDetail(String key, String value) {
            details.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sections.forEach((k, v) -> sb.append(k).append(": ").append(v).append("\n"));
            details.forEach((k, v) -> sb.append("  ").append(k).append(": ").append(String.join(", ", v)).append("\n"));
            return sb.toString();
        }
    }
}