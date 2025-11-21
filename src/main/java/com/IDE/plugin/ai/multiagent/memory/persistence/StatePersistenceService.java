package com.IDE.plugin.ai.multiagent.memory.persistence;

import com.IDE.plugin.ai.multiagent.memory.core.MemoryStateManager;
import com.IDE.plugin.ai.multiagent.memory.core.MemoryState;
import com.IDE.plugin.ai.multiagent.trust.TrustLevel;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Manages memory persistence with distributed validation.
 * Ensures data integrity and consistency across system restarts.
 */
public class StatePersistenceService {
    
    private static final String PERSISTENCE_DIR = "memory_persistence";
    private static final String CHECKPOINT_DIR = "checkpoints";
    private static final String WAL_DIR = "wal";
    private static final int WAL_SEGMENT_SIZE = 1024 * 1024; // 1MB
    private static final int CHECKPOINT_INTERVAL_MS = 300000; // 5 minutes
    
    private final MemoryStateManager stateManager;
    private final Path persistenceRoot;
    private final Path checkpointPath;
    private final Path walPath;
    
    private final ReadWriteLock persistenceLock = new ReentrantReadWriteLock();
    private final ScheduledExecutorService scheduler;
    private final ExecutorService persistenceExecutor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    private volatile WALWriter walWriter;
    private volatile long lastCheckpointTime;
    private final Map<String, PersistenceMetadata> metadataCache;
    
    public StatePersistenceService(MemoryStateManager stateManager) {
        this.stateManager = stateManager;
        this.persistenceRoot = Paths.get(PERSISTENCE_DIR);
        this.checkpointPath = persistenceRoot.resolve(CHECKPOINT_DIR);
        this.walPath = persistenceRoot.resolve(WAL_DIR);
        this.metadataCache = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.persistenceExecutor = Executors.newFixedThreadPool(4);
    }
    
    /**
     * Start persistence service
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            try {
                // Create directories
                Files.createDirectories(persistenceRoot);
                Files.createDirectories(checkpointPath);
                Files.createDirectories(walPath);
                
                // Initialize WAL
                walWriter = new WALWriter(walPath);
                walWriter.initialize();
                
                // Recover from existing data
                recoverFromPersistence();
                
                // Schedule checkpoint creation
                scheduler.scheduleAtFixedRate(
                    this::createCheckpoint,
                    CHECKPOINT_INTERVAL_MS,
                    CHECKPOINT_INTERVAL_MS,
                    TimeUnit.MILLISECONDS
                );
                
                // Schedule WAL cleanup
                scheduler.scheduleAtFixedRate(
                    this::cleanupWAL,
                    1, 1, TimeUnit.HOURS
                );
                
            } catch (IOException e) {
                throw new RuntimeException("Failed to start persistence service", e);
            }
        }
    }
    
    /**
     * Stop persistence service
     */
    public void stop() {
        if (running.compareAndSet(true, false)) {
            // Create final checkpoint
            createCheckpoint();
            
            // Shutdown executors
            scheduler.shutdown();
            persistenceExecutor.shutdown();
            
            try {
                if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
                if (!persistenceExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    persistenceExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                persistenceExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            // Close WAL
            if (walWriter != null) {
                walWriter.close();
            }
        }
    }
    
    /**
     * Persist critical memories with validation
     */
    public CompletableFuture<Boolean> persistCriticalMemories(List<MemoryState> memories) {
        return CompletableFuture.supplyAsync(() -> {
            persistenceLock.readLock().lock();
            try {
                // Write to WAL first
                for (MemoryState memory : memories) {
                    WALEntry entry = new WALEntry(
                        WALEntry.Type.WRITE,
                        memory.getKey(),
                        serialize(memory),
                        memory.getTrustLevel()
                    );
                    
                    if (!walWriter.append(entry)) {
                        return false;
                    }
                }
                
                // Update metadata cache
                for (MemoryState memory : memories) {
                    PersistenceMetadata metadata = new PersistenceMetadata(
                        memory.getKey(),
                        memory.getType(),
                        memory.getTrustLevel(),
                        calculateChecksum(memory),
                        System.currentTimeMillis()
                    );
                    metadataCache.put(memory.getKey(), metadata);
                }
                
                return true;
            } finally {
                persistenceLock.readLock().unlock();
            }
        }, persistenceExecutor);
    }
    
    /**
     * Load persisted memories
     */
    public CompletableFuture<Map<String, MemoryState>> loadPersistedMemories() {
        return CompletableFuture.supplyAsync(() -> {
            persistenceLock.readLock().lock();
            try {
                Map<String, MemoryState> memories = new HashMap<>();
                
                // Load from latest checkpoint
                Optional<Path> latestCheckpoint = findLatestCheckpoint();
                if (latestCheckpoint.isPresent()) {
                    memories.putAll(loadCheckpoint(latestCheckpoint.get()));
                }
                
                // Apply WAL entries after checkpoint
                List<WALEntry> walEntries = walWriter.readEntriesAfter(lastCheckpointTime);
                for (WALEntry entry : walEntries) {
                    applyWALEntry(memories, entry);
                }
                
                return memories;
            } finally {
                persistenceLock.readLock().unlock();
            }
        }, persistenceExecutor);
    }
    
    /**
     * Create checkpoint
     */
    private void createCheckpoint() {
        persistenceLock.writeLock().lock();
        try {
            long checkpointTime = System.currentTimeMillis();
            Path checkpointFile = checkpointPath.resolve("checkpoint_" + checkpointTime + ".dat");
            
            // Collect all current memories
            Map<String, MemoryState> allMemories = collectAllMemories();
            
            // Write checkpoint with validation
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(
                        Files.newOutputStream(checkpointFile, 
                            StandardOpenOption.CREATE, 
                            StandardOpenOption.WRITE)))) {
                
                // Write header
                CheckpointHeader header = new CheckpointHeader(
                    checkpointTime,
                    allMemories.size(),
                    calculateGlobalChecksum(allMemories)
                );
                oos.writeObject(header);
                
                // Write memories
                for (MemoryState memory : allMemories.values()) {
                    oos.writeObject(memory);
                }
                
                lastCheckpointTime = checkpointTime;
                
                // Clean old checkpoints
                cleanOldCheckpoints();
                
            } catch (IOException e) {
                Files.deleteIfExists(checkpointFile);
                throw new RuntimeException("Failed to create checkpoint", e);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create checkpoint", e);
        } finally {
            persistenceLock.writeLock().unlock();
        }
    }
    
    /**
     * Recover from persistence
     */
    private void recoverFromPersistence() {
        try {
            Map<String, MemoryState> recovered = loadPersistedMemories().get();
            
            // Validate and restore memories
            for (MemoryState memory : recovered.values()) {
                if (validateMemory(memory)) {
                    // Restore to appropriate memory tier
                    switch (memory.getType()) {
                        case WORKING:
                            stateManager.getWorkingMemory().store(memory);
                            break;
                        case EPISODIC:
                            stateManager.getEpisodicMemory().store(memory);
                            break;
                        case SEMANTIC:
                            stateManager.getSemanticMemory().store(memory);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            // Log error but continue - persistence recovery is best effort
            System.err.println("Failed to recover from persistence: " + e.getMessage());
        }
    }
    
    /**
     * Validate memory integrity
     */
    private boolean validateMemory(MemoryState memory) {
        PersistenceMetadata metadata = metadataCache.get(memory.getKey());
        if (metadata == null) {
            return true; // No metadata to validate against
        }
        
        // Verify checksum
        String currentChecksum = calculateChecksum(memory);
        if (!currentChecksum.equals(metadata.getChecksum())) {
            return false;
        }
        
        // Verify trust level hasn't been downgraded
        if (memory.getTrustLevel().ordinal() < metadata.getTrustLevel().ordinal()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Find latest checkpoint
     */
    private Optional<Path> findLatestCheckpoint() {
        try {
            return Files.list(checkpointPath)
                .filter(path -> path.getFileName().toString().startsWith("checkpoint_"))
                .max(Comparator.comparing(path -> {
                    String filename = path.getFileName().toString();
                    String timestamp = filename.substring(11, filename.lastIndexOf('.'));
                    return Long.parseLong(timestamp);
                }));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Load checkpoint
     */
    private Map<String, MemoryState> loadCheckpoint(Path checkpointFile) {
        Map<String, MemoryState> memories = new HashMap<>();
        
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(checkpointFile)))) {
            
            // Read header
            CheckpointHeader header = (CheckpointHeader) ois.readObject();
            lastCheckpointTime = header.getTimestamp();
            
            // Read memories
            for (int i = 0; i < header.getMemoryCount(); i++) {
                MemoryState memory = (MemoryState) ois.readObject();
                memories.put(memory.getKey(), memory);
            }
            
            // Validate global checksum
            String calculatedChecksum = calculateGlobalChecksum(memories);
            if (!calculatedChecksum.equals(header.getGlobalChecksum())) {
                throw new RuntimeException("Checkpoint validation failed");
            }
            
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to load checkpoint", e);
        }
        
        return memories;
    }
    
    /**
     * Apply WAL entry
     */
    private void applyWALEntry(Map<String, MemoryState> memories, WALEntry entry) {
        switch (entry.getType()) {
            case WRITE:
                MemoryState memory = deserialize(entry.getData());
                if (memory != null && validateMemory(memory)) {
                    memories.put(entry.getKey(), memory);
                }
                break;
            case DELETE:
                memories.remove(entry.getKey());
                break;
        }
    }
    
    /**
     * Collect all memories from state manager
     */
    private Map<String, MemoryState> collectAllMemories() {
        Map<String, MemoryState> allMemories = new HashMap<>();
        
        // Collect from working memory
        for (String key : stateManager.getWorkingMemory().getKeys()) {
            stateManager.getWorkingMemory().retrieve(key)
                .ifPresent(value -> allMemories.put(key, 
                    new MemoryState(key, value, MemoryType.WORKING, "system", TrustLevel.VERIFIED)));
        }
        
        // Add episodic and semantic memories
        // Implementation depends on the specific APIs of these memory types
        
        return allMemories;
    }
    
    /**
     * Calculate checksum for memory
     */
    private String calculateChecksum(MemoryState memory) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(memory.getKey().getBytes());
            md.update(serialize(memory.getValue()));
            
            byte[] digest = md.digest();
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to calculate checksum", e);
        }
    }
    
    /**
     * Calculate global checksum
     */
    private String calculateGlobalChecksum(Map<String, MemoryState> memories) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            
            memories.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    md.update(entry.getKey().getBytes());
                    md.update(calculateChecksum(entry.getValue()).getBytes());
                });
            
            byte[] digest = md.digest();
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to calculate global checksum", e);
        }
    }
    
    /**
     * Serialize object
     */
    private byte[] serialize(Object obj) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize object", e);
        }
    }
    
    /**
     * Deserialize object
     */
    private MemoryState deserialize(byte[] data) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (MemoryState) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }
    
    /**
     * Clean old checkpoints
     */
    private void cleanOldCheckpoints() {
        try {
            List<Path> checkpoints = Files.list(checkpointPath)
                .filter(path -> path.getFileName().toString().startsWith("checkpoint_"))
                .sorted(Comparator.comparing(Path::getFileName).reversed())
                .collect(Collectors.toList());
            
            // Keep only the latest 3 checkpoints
            if (checkpoints.size() > 3) {
                for (int i = 3; i < checkpoints.size(); i++) {
                    Files.deleteIfExists(checkpoints.get(i));
                }
            }
        } catch (IOException e) {
            // Log error but continue
        }
    }
    
    /**
     * Clean up WAL
     */
    private void cleanupWAL() {
        if (walWriter != null && lastCheckpointTime > 0) {
            walWriter.truncateBefore(lastCheckpointTime);
        }
    }
}