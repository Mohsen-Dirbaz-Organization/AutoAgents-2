package com.IDE.plugin.ai.multiagent.mechanical.transmission;

import com.IDE.plugin.ai.multiagent.mechanical.validation.MechanicalSignal;
import com.IDE.plugin.ai.multiagent.mechanical.validation.SignalPriority;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * MechanicalSignalQueue: Priority-based queuing with anti-starvation mechanisms
 * Logic: 45% priority management, 30% queue optimization, 25% starvation prevention
 */
public class MechanicalSignalQueue {
    
    private final PriorityQueueManager priorityManager;
    private final QueueOptimizer queueOptimizer;
    private final StarvationPreventer starvationPreventer;
    private final Map<SignalPriority, SignalQueue> priorityQueues;
    private final ExecutorService queueExecutor;
    private final ScheduledExecutorService maintenanceExecutor;
    private final ReentrantLock queueLock;
    private volatile boolean running;
    
    private static final int MAX_QUEUE_SIZE = 10000;
    private static final long STARVATION_THRESHOLD_MS = 30000; // 30 seconds
    
    public MechanicalSignalQueue() {
        this.priorityManager = new PriorityQueueManager();
        this.queueOptimizer = new QueueOptimizer();
        this.starvationPreventer = new StarvationPreventer();
        this.priorityQueues = new ConcurrentHashMap<>();
        this.queueExecutor = Executors.newFixedThreadPool(4);
        this.maintenanceExecutor = Executors.newScheduledThreadPool(2);
        this.queueLock = new ReentrantLock(true); // Fair lock
        this.running = true;
        
        initialize();
    }
    
    private void initialize() {
        // Initialize priority queues
        for (SignalPriority priority : SignalPriority.values()) {
            priorityQueues.put(priority, new SignalQueue(priority));
        }
        
        // Schedule maintenance tasks
        maintenanceExecutor.scheduleAtFixedRate(
            this::performMaintenance, 10, 10, TimeUnit.SECONDS
        );
        
        // Schedule starvation prevention
        maintenanceExecutor.scheduleAtFixedRate(
            this::preventStarvation, 5, 5, TimeUnit.SECONDS
        );
    }
    
    /**
     * Enqueues a signal with priority-based management
     */
    public QueueResult enqueue(MechanicalSignal signal) {
        queueLock.lock();
        try {
            // Priority management (45% of logic)
            SignalPriority effectivePriority = priorityManager.determineEffectivePriority(signal);
            
            // Check queue capacity
            SignalQueue queue = priorityQueues.get(effectivePriority);
            if (queue.isFull()) {
                // Queue optimization (30% of logic)
                OptimizationResult optimization = queueOptimizer.optimizeQueue(queue);
                if (!optimization.madeSpace) {
                    return new QueueResult(false, "Queue full for priority: " + effectivePriority);
                }
            }
            
            // Enqueue signal
            QueuedSignal queuedSignal = new QueuedSignal(signal, effectivePriority);
            queue.add(queuedSignal);
            
            // Update starvation tracking (25% of logic)
            starvationPreventer.trackEnqueue(queuedSignal);
            
            return new QueueResult(true, "Signal queued successfully");
            
        } finally {
            queueLock.unlock();
        }
    }
    
    /**
     * Dequeues the next signal based on priority and anti-starvation
     */
    public MechanicalSignal dequeue() {
        queueLock.lock();
        try {
            // Check for starving signals first
            QueuedSignal starving = starvationPreventer.getStarvingSignal();
            if (starving != null) {
                removeFromQueue(starving);
                return starving.getSignal();
            }
            
            // Normal priority-based dequeue
            for (SignalPriority priority : SignalPriority.values()) {
                SignalQueue queue = priorityQueues.get(priority);
                if (!queue.isEmpty()) {
                    QueuedSignal queuedSignal = queue.poll();
                    starvationPreventer.trackDequeue(queuedSignal);
                    return queuedSignal.getSignal();
                }
            }
            
            return null;
            
        } finally {
            queueLock.unlock();
        }
    }
    
    /**
     * Batch dequeue for efficiency
     */
    public List<MechanicalSignal> dequeueBatch(int maxBatchSize) {
        List<MechanicalSignal> batch = new ArrayList<>();
        queueLock.lock();
        try {
            // First add any starving signals
            List<QueuedSignal> starvingSignals = starvationPreventer.getStarvingSignals(maxBatchSize / 2);
            for (QueuedSignal starving : starvingSignals) {
                removeFromQueue(starving);
                batch.add(starving.getSignal());
            }
            
            // Fill remaining batch with priority order
            int remaining = maxBatchSize - batch.size();
            for (SignalPriority priority : SignalPriority.values()) {
                if (remaining <= 0) break;
                
                SignalQueue queue = priorityQueues.get(priority);
                int taken = 0;
                while (!queue.isEmpty() && taken < remaining) {
                    QueuedSignal queuedSignal = queue.poll();
                    starvationPreventer.trackDequeue(queuedSignal);
                    batch.add(queuedSignal.getSignal());
                    taken++;
                }
                remaining -= taken;
            }
            
            return batch;
            
        } finally {
            queueLock.unlock();
        }
    }
    
    /**
     * Performs periodic queue maintenance
     */
    private void performMaintenance() {
        // Optimize queues
        for (SignalQueue queue : priorityQueues.values()) {
            queueOptimizer.performMaintenance(queue);
        }
        
        // Generate statistics
        QueueStatistics stats = generateStatistics();
        publishStatistics(stats);
    }
    
    /**
     * Prevents signal starvation
     */
    private void preventStarvation() {
        starvationPreventer.checkAndPromoteStarvingSignals();
    }
    
    /**
     * Removes a queued signal from its queue
     */
    private void removeFromQueue(QueuedSignal signal) {
        SignalQueue queue = priorityQueues.get(signal.getPriority());
        if (queue != null) {
            queue.remove(signal);
        }
    }
    
    /**
     * Inner class for priority queue management
     */
    private static class PriorityQueueManager {
        private final Map<String, PriorityAdjustment> adjustments = new ConcurrentHashMap<>();
        
        SignalPriority determineEffectivePriority(MechanicalSignal signal) {
            SignalPriority basePriority = signal.getPriority();
            
            // Check for priority adjustments
            PriorityAdjustment adjustment = adjustments.get(signal.getSenderId());
            if (adjustment != null) {
                return adjustment.adjust(basePriority);
            }
            
            // Dynamic priority based on signal characteristics
            if (isUrgent(signal)) {
                return upgradePriority(basePriority);
            }
            
            return basePriority;
        }
        
        private boolean isUrgent(MechanicalSignal signal) {
            // Check metadata for urgency indicators
            Object urgent = signal.getMetadata().get("urgent");
            return Boolean.TRUE.equals(urgent);
        }
        
        private SignalPriority upgradePriority(SignalPriority current) {
            switch (current) {
                case LOW: return SignalPriority.NORMAL;
                case NORMAL: return SignalPriority.HIGH;
                case HIGH: return SignalPriority.CRITICAL;
                default: return current;
            }
        }
        
        void adjustAgentPriority(String agentId, PriorityAdjustment adjustment) {
            adjustments.put(agentId, adjustment);
        }
    }
    
    /**
     * Inner class for queue optimization
     */
    private static class QueueOptimizer {
        
        OptimizationResult optimizeQueue(SignalQueue queue) {
            int removed = 0;
            
            // Remove expired signals
            removed += queue.removeExpired();
            
            // Merge duplicate signals
            removed += queue.mergeDuplicates();
            
            // Compress if still full
            if (queue.isFull()) {
                removed += queue.compressOldest(10); // Remove 10% oldest
            }
            
            return new OptimizationResult(removed > 0, removed);
        }
        
        void performMaintenance(SignalQueue queue) {
            // Rebalance queue if needed
            if (queue.getFragmentation() > 0.3) {
                queue.rebalance();
            }
            
            // Update statistics
            queue.updateStatistics();
        }
    }
    
    /**
     * Inner class for starvation prevention
     */
    private class StarvationPreventer {
        private final Map<String, QueuedSignal> trackedSignals = new ConcurrentHashMap<>();
        private final PriorityQueue<QueuedSignal> starvingSignals = new PriorityQueue<>(
            Comparator.comparingLong(QueuedSignal::getEnqueueTime)
        );
        
        void trackEnqueue(QueuedSignal signal) {
            trackedSignals.put(signal.getId(), signal);
        }
        
        void trackDequeue(QueuedSignal signal) {
            trackedSignals.remove(signal.getId());
            starvingSignals.remove(signal);
        }
        
        QueuedSignal getStarvingSignal() {
            return starvingSignals.poll();
        }
        
        List<QueuedSignal> getStarvingSignals(int max) {
            List<QueuedSignal> result = new ArrayList<>();
            for (int i = 0; i < max && !starvingSignals.isEmpty(); i++) {
                result.add(starvingSignals.poll());
            }
            return result;
        }
        
        void checkAndPromoteStarvingSignals() {
            long currentTime = System.currentTimeMillis();
            
            for (QueuedSignal signal : trackedSignals.values()) {
                long waitTime = currentTime - signal.getEnqueueTime();
                if (waitTime > STARVATION_THRESHOLD_MS && !signal.isPromoted()) {
                    // Promote priority
                    promoteSignal(signal);
                    starvingSignals.offer(signal);
                }
            }
        }
        
        private void promoteSignal(QueuedSignal signal) {
            queueLock.lock();
            try {
                // Remove from current queue
                removeFromQueue(signal);
                
                // Promote priority
                signal.promote();
                
                // Re-add to higher priority queue
                SignalQueue newQueue = priorityQueues.get(signal.getPriority());
                if (newQueue != null) {
                    newQueue.add(signal);
                }
            } finally {
                queueLock.unlock();
            }
        }
    }
    
    /**
     * Generates queue statistics
     */
    private QueueStatistics generateStatistics() {
        Map<SignalPriority, Integer> queueSizes = new HashMap<>();
        long totalSize = 0;
        
        for (Map.Entry<SignalPriority, SignalQueue> entry : priorityQueues.entrySet()) {
            int size = entry.getValue().size();
            queueSizes.put(entry.getKey(), size);
            totalSize += size;
        }
        
        return new QueueStatistics(queueSizes, totalSize);
    }
    
    /**
     * Publishes queue statistics
     */
    private void publishStatistics(QueueStatistics stats) {
        // Implementation would publish stats to monitoring system
    }
    
    /**
     * Shuts down the queue
     */
    public void shutdown() {
        running = false;
        
        maintenanceExecutor.shutdown();
        queueExecutor.shutdown();
        
        try {
            if (!maintenanceExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                maintenanceExecutor.shutdownNow();
            }
            if (!queueExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                queueExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            maintenanceExecutor.shutdownNow();
            queueExecutor.shutdownNow();
        }
    }
    
    /**
     * Inner class representing a queued signal
     */
    private static class QueuedSignal {
        private final String id;
        private final MechanicalSignal signal;
        private SignalPriority priority;
        private final long enqueueTime;
        private boolean promoted;
        
        QueuedSignal(MechanicalSignal signal, SignalPriority priority) {
            this.id = signal.getId();
            this.signal = signal;
            this.priority = priority;
            this.enqueueTime = System.currentTimeMillis();
            this.promoted = false;
        }
        
        void promote() {
            if (priority != SignalPriority.CRITICAL) {
                priority = SignalPriority.values()[priority.ordinal() - 1];
                promoted = true;
            }
        }
        
        // Getters
        String getId() { return id; }
        MechanicalSignal getSignal() { return signal; }
        SignalPriority getPriority() { return priority; }
        long getEnqueueTime() { return enqueueTime; }
        boolean isPromoted() { return promoted; }
    }
    
    /**
     * Inner class for individual priority queues
     */
    private static class SignalQueue {
        private final SignalPriority priority;
        private final LinkedList<QueuedSignal> queue;
        private final AtomicLong totalEnqueued = new AtomicLong(0);
        private final AtomicLong totalDequeued = new AtomicLong(0);
        
        SignalQueue(SignalPriority priority) {
            this.priority = priority;
            this.queue = new LinkedList<>();
        }
        
        boolean isFull() {
            return queue.size() >= MAX_QUEUE_SIZE / SignalPriority.values().length;
        }
        
        boolean isEmpty() {
            return queue.isEmpty();
        }
        
        int size() {
            return queue.size();
        }
        
        void add(QueuedSignal signal) {
            queue.offer(signal);
            totalEnqueued.incrementAndGet();
        }
        
        QueuedSignal poll() {
            QueuedSignal signal = queue.poll();
            if (signal != null) {
                totalDequeued.incrementAndGet();
            }
            return signal;
        }
        
        boolean remove(QueuedSignal signal) {
            return queue.remove(signal);
        }
        
        int removeExpired() {
            long expiryTime = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5);
            return queue.removeIf(s -> s.getEnqueueTime() < expiryTime) ? 1 : 0;
        }
        
        int mergeDuplicates() {
            // Simplified - would implement actual deduplication
            return 0;
        }
        
        int compressOldest(int percentage) {
            int toRemove = queue.size() * percentage / 100;
            for (int i = 0; i < toRemove && !queue.isEmpty(); i++) {
                queue.removeFirst();
            }
            return toRemove;
        }
        
        double getFragmentation() {
            // Simplified fragmentation calculation
            return 0.0;
        }
        
        void rebalance() {
            // Rebalance queue for better performance
        }
        
        void updateStatistics() {
            // Update internal statistics
        }
    }
}