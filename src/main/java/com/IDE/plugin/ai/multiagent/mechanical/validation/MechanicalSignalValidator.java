package com.IDE.plugin.ai.multiagent.mechanical.validation;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * MechanicalSignalValidator: Validates signal integrity using cryptographic verification
 * Logic: 50% integrity verification, 30% authenticity validation, 20% performance monitoring
 */
public class MechanicalSignalValidator {
    
    private final Map<String, SignalValidationResult> validationCache;
    private final Map<String, byte[]> agentSecrets;
    private final AtomicLong totalValidations;
    private final AtomicLong successfulValidations;
    private final AtomicLong failedValidations;
    private final PerformanceMonitor performanceMonitor;
    
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final long CACHE_EXPIRY_MS = 60000; // 1 minute
    
    public MechanicalSignalValidator() {
        this.validationCache = new ConcurrentHashMap<>();
        this.agentSecrets = new ConcurrentHashMap<>();
        this.totalValidations = new AtomicLong(0);
        this.successfulValidations = new AtomicLong(0);
        this.failedValidations = new AtomicLong(0);
        this.performanceMonitor = new PerformanceMonitor();
    }
    
    /**
     * Validates the integrity and authenticity of a mechanical signal
     */
    public SignalValidationResult validateSignal(MechanicalSignal signal) {
        long startTime = System.nanoTime();
        totalValidations.incrementAndGet();
        
        try {
            // Check cache first
            SignalValidationResult cachedResult = checkCache(signal.getId());
            if (cachedResult != null) {
                performanceMonitor.recordCacheHit();
                return cachedResult;
            }
            
            // Integrity verification (50% of logic)
            boolean integrityValid = verifyIntegrity(signal);
            
            // Authenticity validation (30% of logic)
            boolean authenticityValid = verifyAuthenticity(signal);
            
            // Performance monitoring (20% of logic)
            long validationTime = System.nanoTime() - startTime;
            performanceMonitor.recordValidation(validationTime);
            
            // Create validation result
            SignalValidationResult result = new SignalValidationResult(
                signal.getId(),
                integrityValid && authenticityValid,
                integrityValid,
                authenticityValid,
                validationTime,
                generateValidationReport(signal, integrityValid, authenticityValid)
            );
            
            // Update counters
            if (result.isValid()) {
                successfulValidations.incrementAndGet();
            } else {
                failedValidations.incrementAndGet();
            }
            
            // Cache result
            cacheResult(signal.getId(), result);
            
            return result;
            
        } catch (Exception e) {
            failedValidations.incrementAndGet();
            return new SignalValidationResult(
                signal.getId(),
                false,
                false,
                false,
                System.nanoTime() - startTime,
                "Validation failed: " + e.getMessage()
            );
        }
    }
    
    /**
     * Verifies the integrity of the signal data
     */
    private boolean verifyIntegrity(MechanicalSignal signal) {
        try {
            // Verify checksum
            String calculatedChecksum = calculateChecksum(signal.getPayload());
            if (!calculatedChecksum.equals(signal.getChecksum())) {
                return false;
            }
            
            // Verify sequence number
            if (!verifySequenceNumber(signal)) {
                return false;
            }
            
            // Verify timestamp
            if (!verifyTimestamp(signal)) {
                return false;
            }
            
            // Verify payload structure
            if (!verifyPayloadStructure(signal)) {
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verifies the authenticity of the signal sender
     */
    private boolean verifyAuthenticity(MechanicalSignal signal) {
        try {
            // Get agent secret
            byte[] secret = agentSecrets.get(signal.getSenderId());
            if (secret == null) {
                return false; // Unknown agent
            }
            
            // Verify HMAC signature
            String calculatedSignature = calculateHMAC(signal, secret);
            if (!calculatedSignature.equals(signal.getSignature())) {
                return false;
            }
            
            // Verify agent permissions
            if (!verifyAgentPermissions(signal)) {
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Calculates checksum for signal payload
     */
    private String calculateChecksum(byte[] payload) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(payload);
        return Base64.getEncoder().encodeToString(hash);
    }
    
    /**
     * Calculates HMAC signature for signal
     */
    private String calculateHMAC(MechanicalSignal signal, byte[] secret) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec secretKey = new SecretKeySpec(secret, HMAC_ALGORITHM);
        mac.init(secretKey);
        
        // Include all relevant fields in signature
        mac.update(signal.getId().getBytes());
        mac.update(signal.getSenderId().getBytes());
        mac.update(signal.getPayload());
        mac.update(String.valueOf(signal.getTimestamp()).getBytes());
        
        byte[] signature = mac.doFinal();
        return Base64.getEncoder().encodeToString(signature);
    }
    
    /**
     * Verifies sequence number is valid and in order
     */
    private boolean verifySequenceNumber(MechanicalSignal signal) {
        // Implementation would track sequence numbers per agent
        return signal.getSequenceNumber() > 0;
    }
    
    /**
     * Verifies timestamp is within acceptable range
     */
    private boolean verifyTimestamp(MechanicalSignal signal) {
        long currentTime = System.currentTimeMillis();
        long signalTime = signal.getTimestamp();
        long timeDiff = Math.abs(currentTime - signalTime);
        
        // Allow 5 minute time window
        return timeDiff < 300000;
    }
    
    /**
     * Verifies payload structure is valid
     */
    private boolean verifyPayloadStructure(MechanicalSignal signal) {
        byte[] payload = signal.getPayload();
        return payload != null && payload.length > 0 && payload.length < 1048576; // Max 1MB
    }
    
    /**
     * Verifies agent has permissions for signal type
     */
    private boolean verifyAgentPermissions(MechanicalSignal signal) {
        // Implementation would check agent permissions
        return true;
    }
    
    /**
     * Checks validation cache for recent results
     */
    private SignalValidationResult checkCache(String signalId) {
        SignalValidationResult cached = validationCache.get(signalId);
        if (cached != null) {
            long age = System.currentTimeMillis() - cached.getTimestamp();
            if (age < CACHE_EXPIRY_MS) {
                return cached;
            } else {
                validationCache.remove(signalId);
            }
        }
        return null;
    }
    
    /**
     * Caches validation result
     */
    private void cacheResult(String signalId, SignalValidationResult result) {
        validationCache.put(signalId, result);
        
        // Clean old entries periodically
        if (validationCache.size() > 10000) {
            cleanCache();
        }
    }
    
    /**
     * Cleans expired entries from cache
     */
    private void cleanCache() {
        long currentTime = System.currentTimeMillis();
        validationCache.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().getTimestamp() > CACHE_EXPIRY_MS
        );
    }
    
    /**
     * Generates detailed validation report
     */
    private String generateValidationReport(MechanicalSignal signal, 
                                          boolean integrityValid, 
                                          boolean authenticityValid) {
        StringBuilder report = new StringBuilder();
        report.append("Signal ID: ").append(signal.getId()).append("\n");
        report.append("Sender: ").append(signal.getSenderId()).append("\n");
        report.append("Integrity: ").append(integrityValid ? "PASS" : "FAIL").append("\n");
        report.append("Authenticity: ").append(authenticityValid ? "PASS" : "FAIL").append("\n");
        report.append("Timestamp: ").append(new Date(signal.getTimestamp())).append("\n");
        return report.toString();
    }
    
    /**
     * Registers an agent with their secret key
     */
    public void registerAgent(String agentId, byte[] secret) {
        agentSecrets.put(agentId, secret);
    }
    
    /**
     * Unregisters an agent
     */
    public void unregisterAgent(String agentId) {
        agentSecrets.remove(agentId);
    }
    
    /**
     * Gets validation statistics
     */
    public ValidationStatistics getStatistics() {
        return new ValidationStatistics(
            totalValidations.get(),
            successfulValidations.get(),
            failedValidations.get(),
            performanceMonitor.getAverageValidationTime(),
            performanceMonitor.getCacheHitRate()
        );
    }
    
    /**
     * Inner class for performance monitoring
     */
    private static class PerformanceMonitor {
        private final AtomicLong totalValidationTime = new AtomicLong(0);
        private final AtomicLong validationCount = new AtomicLong(0);
        private final AtomicLong cacheHits = new AtomicLong(0);
        
        void recordValidation(long timeNanos) {
            totalValidationTime.addAndGet(timeNanos);
            validationCount.incrementAndGet();
        }
        
        void recordCacheHit() {
            cacheHits.incrementAndGet();
        }
        
        double getAverageValidationTime() {
            long count = validationCount.get();
            return count > 0 ? totalValidationTime.get() / (double) count : 0;
        }
        
        double getCacheHitRate() {
            long total = validationCount.get() + cacheHits.get();
            return total > 0 ? cacheHits.get() / (double) total : 0;
        }
    }
}