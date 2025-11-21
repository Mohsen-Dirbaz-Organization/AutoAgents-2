package com.IDE.plugin.ai.multiagent.station;

import com.IDE.plugin.ai.multiagent.agent.Agent;
import com.IDE.plugin.ai.multiagent.agent.AgentCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents an individual station with agent assignments and task processing capabilities.
 */
public class Station {
    private final String id;
    private final StationConfiguration configuration;
    private final Map<String, Agent> assignedAgents = new ConcurrentHashMap<>();
    private final List<String> taskQueue = new CopyOnWriteArrayList<>();
    private final AtomicBoolean active = new AtomicBoolean(false);
    
    // Performance metrics
    private final AtomicLong tasksProcessed = new AtomicLong(0);
    private final AtomicLong totalProcessingTime = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    private final Map<String, Long> taskStartTimes = new ConcurrentHashMap<>();
    
    // Station metadata
    private final long creationTime;
    private long lastActivityTime;
    private final Map<String, Object> metadata = new ConcurrentHashMap<>();
    
    public Station(@NotNull String id, @NotNull StationConfiguration configuration) {
        this.id = id;
        this.configuration = configuration;
        this.creationTime = System.currentTimeMillis();
        this.lastActivityTime = creationTime;
    }
    
    /**
     * Assigns an agent to this station.
     */
    public void assignAgent(@NotNull Agent agent) {
        if (assignedAgents.size() >= configuration.getMaxAgents()) {
            throw new IllegalStateException("Station has reached maximum agent capacity");
        }
        
        if (!isAgentCompatible(agent)) {
            throw new IllegalArgumentException("Agent capabilities do not match station requirements");
        }
        
        assignedAgents.put(agent.getId(), agent);
        updateLastActivityTime();
    }
    
    /**
     * Removes an agent from this station.
     */
    public void removeAgent(@NotNull String agentId) {
        Agent removed = assignedAgents.remove(agentId);
        if (removed != null) {
            updateLastActivityTime();
        }
    }
    
    /**
     * Checks if this station has a specific agent.
     */
    public boolean hasAgent(@NotNull String agentId) {
        return assignedAgents.containsKey(agentId);
    }
    
    /**
     * Checks if an agent is compatible with this station.
     */
    public boolean isAgentCompatible(@NotNull Agent agent) {
        Set<AgentCapability> requiredCapabilities = configuration.getRequiredCapabilities();
        Set<AgentCapability> agentCapabilities = agent.getCapabilities();
        
        return agentCapabilities.containsAll(requiredCapabilities);
    }
    
    /**
     * Checks if this station can accept more agents.
     */
    public boolean canAcceptAgent(@NotNull Agent agent) {
        return assignedAgents.size() < configuration.getMaxAgents() && isAgentCompatible(agent);
    }
    
    /**
     * Activates the station for processing.
     */
    public void activate() {
        if (assignedAgents.isEmpty()) {
            throw new IllegalStateException("Cannot activate station without assigned agents");
        }
        
        active.set(true);
        updateLastActivityTime();
    }
    
    /**
     * Deactivates the station.
     */
    public void deactivate() {
        active.set(false);
        taskQueue.clear();
        updateLastActivityTime();
    }
    
    /**
     * Adds a task to the station's queue.
     */
    public void queueTask(@NotNull String taskId) {
        if (!active.get()) {
            throw new IllegalStateException("Cannot queue tasks to inactive station");
        }
        
        taskQueue.add(taskId);
        updateLastActivityTime();
    }
    
    /**
     * Starts processing a task.
     */
    public void startTask(@NotNull String taskId) {
        taskStartTimes.put(taskId, System.currentTimeMillis());
        taskQueue.remove(taskId);
        updateLastActivityTime();
    }
    
    /**
     * Completes a task.
     */
    public void completeTask(@NotNull String taskId, boolean success) {
        Long startTime = taskStartTimes.remove(taskId);
        if (startTime != null) {
            long processingTime = System.currentTimeMillis() - startTime;
            totalProcessingTime.addAndGet(processingTime);
            tasksProcessed.incrementAndGet();
            
            if (!success) {
                errorCount.incrementAndGet();
            }
        }
        updateLastActivityTime();
    }
    
    /**
     * Gets the next task from the queue.
     */
    @Nullable
    public String getNextTask() {
        return taskQueue.isEmpty() ? null : taskQueue.get(0);
    }
    
    /**
     * Finds an available agent for a specific capability.
     */
    @Nullable
    public Agent findAvailableAgent(@NotNull AgentCapability capability) {
        return assignedAgents.values().stream()
            .filter(agent -> agent.hasCapability(capability))
            .filter(agent -> agent.isAvailable())
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Gets all assigned agents.
     */
    @NotNull
    public Collection<Agent> getAssignedAgents() {
        return new ArrayList<>(assignedAgents.values());
    }
    
    /**
     * Gets the average processing time in milliseconds.
     */
    public double getAverageProcessingTime() {
        long processed = tasksProcessed.get();
        return processed == 0 ? 0 : (double) totalProcessingTime.get() / processed;
    }
    
    /**
     * Gets the error rate.
     */
    public double getErrorRate() {
        long processed = tasksProcessed.get();
        return processed == 0 ? 0 : (double) errorCount.get() / processed;
    }
    
    /**
     * Gets the current queue size.
     */
    public int getQueueSize() {
        return taskQueue.size();
    }
    
    /**
     * Adds metadata to the station.
     */
    public void setMetadata(@NotNull String key, @NotNull Object value) {
        metadata.put(key, value);
        updateLastActivityTime();
    }
    
    /**
     * Gets metadata from the station.
     */
    @Nullable
    public Object getMetadata(@NotNull String key) {
        return metadata.get(key);
    }
    
    /**
     * Updates the last activity timestamp.
     */
    private void updateLastActivityTime() {
        lastActivityTime = System.currentTimeMillis();
    }
    
    // Getters
    
    @NotNull
    public String getId() {
        return id;
    }
    
    @NotNull
    public StationConfiguration getConfiguration() {
        return configuration;
    }
    
    public boolean isActive() {
        return active.get();
    }
    
    public long getTasksProcessed() {
        return tasksProcessed.get();
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    public long getLastActivityTime() {
        return lastActivityTime;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Station station = (Station) o;
        return id.equals(station.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return "Station{" +
               "id='" + id + '\'' +
               ", name='" + configuration.getName() + '\'' +
               ", agents=" + assignedAgents.size() +
               ", active=" + active.get() +
               ", tasksProcessed=" + tasksProcessed.get() +
               '}';
    }
}