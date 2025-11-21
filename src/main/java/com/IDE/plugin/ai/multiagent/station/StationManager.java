package com.IDE.plugin.ai.multiagent.station;

import com.IDE.plugin.ai.multiagent.agent.Agent;
import com.IDE.plugin.ai.multiagent.agent.AgentState;
import com.IDE.plugin.ai.multiagent.coordination.AgentCoordinatorService;
import com.IDE.plugin.ai.multiagent.trust.TrustManager;
import com.IDE.plugin.ai.multiagent.memory.MemoryService;
import com.IDE.plugin.ai.multiagent.memory.MemoryEntry;
import com.IDE.plugin.ai.multiagent.memory.MemoryType;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Central station coordination service that manages multiple stations
 * and integrates with the agent coordination system.
 */
@Service(Service.Level.PROJECT)
public final class StationManager {
    private static final Logger LOG = Logger.getInstance(StationManager.class);
    
    private final Project project;
    private final Map<String, Station> stations = new ConcurrentHashMap<>();
    private final AgentCoordinatorService coordinatorService;
    private final TrustManager trustManager;
    private final MemoryService memoryService;
    private final ScheduledExecutorService scheduler;
    private final Map<String, StationStatus> statusCache = new ConcurrentHashMap<>();
    private final List<StationListener> listeners = new CopyOnWriteArrayList<>();
    
    // Station management configuration
    private static final int MAX_STATIONS = 10;
    private static final long STATUS_UPDATE_INTERVAL = 5000; // 5 seconds
    private static final long IDLE_TIMEOUT = 300000; // 5 minutes
    private static final double MIN_TRUST_THRESHOLD = 0.3;
    
    public StationManager(@NotNull Project project) {
        this.project = project;
        this.coordinatorService = project.getService(AgentCoordinatorService.class);
        this.trustManager = project.getService(TrustManager.class);
        this.memoryService = project.getService(MemoryService.class);
        this.scheduler = Executors.newScheduledThreadPool(2);
        
        initializeScheduledTasks();
    }
    
    /**
     * Creates a new station with the given configuration.
     */
    @NotNull
    public Station createStation(@NotNull StationConfiguration configuration) {
        if (stations.size() >= MAX_STATIONS) {
            throw new IllegalStateException("Maximum number of stations reached: " + MAX_STATIONS);
        }
        
        String stationId = generateStationId(configuration.getName());
        Station station = new Station(stationId, configuration);
        
        stations.put(stationId, station);
        statusCache.put(stationId, new StationStatus(stationId));
        
        // Record station creation in memory
        memoryService.storeMemory(MemoryEntry.builder()
            .type(MemoryType.SYSTEM)
            .category("station_lifecycle")
            .content("Station created: " + configuration.getName())
            .metadata(Map.of(
                "stationId", stationId,
                "configuration", configuration.toMap()
            ))
            .build());
        
        notifyListeners(StationEvent.CREATED, station);
        LOG.info("Created station: " + stationId);
        
        return station;
    }
    
    /**
     * Assigns an agent to a station.
     */
    public void assignAgentToStation(@NotNull String agentId, @NotNull String stationId) {
        Station station = stations.get(stationId);
        if (station == null) {
            throw new IllegalArgumentException("Station not found: " + stationId);
        }
        
        Agent agent = coordinatorService.getAgent(agentId);
        if (agent == null) {
            throw new IllegalArgumentException("Agent not found: " + agentId);
        }
        
        // Check trust level before assignment
        double trustScore = trustManager.getTrustScore(agentId);
        if (trustScore < MIN_TRUST_THRESHOLD) {
            throw new IllegalStateException("Agent trust score too low for station assignment: " + trustScore);
        }
        
        station.assignAgent(agent);
        updateStationStatus(stationId);
        
        // Record assignment
        memoryService.storeMemory(MemoryEntry.builder()
            .type(MemoryType.COLLABORATION)
            .category("agent_assignment")
            .content("Agent " + agentId + " assigned to station " + stationId)
            .metadata(Map.of(
                "agentId", agentId,
                "stationId", stationId,
                "trustScore", trustScore
            ))
            .build());
        
        notifyListeners(StationEvent.AGENT_ASSIGNED, station);
    }
    
    /**
     * Removes an agent from its current station.
     */
    public void removeAgentFromStation(@NotNull String agentId) {
        for (Station station : stations.values()) {
            if (station.hasAgent(agentId)) {
                station.removeAgent(agentId);
                updateStationStatus(station.getId());
                
                memoryService.storeMemory(MemoryEntry.builder()
                    .type(MemoryType.COLLABORATION)
                    .category("agent_removal")
                    .content("Agent " + agentId + " removed from station " + station.getId())
                    .build());
                
                notifyListeners(StationEvent.AGENT_REMOVED, station);
                break;
            }
        }
    }
    
    /**
     * Activates a station for task processing.
     */
    public void activateStation(@NotNull String stationId) {
        Station station = stations.get(stationId);
        if (station == null) {
            throw new IllegalArgumentException("Station not found: " + stationId);
        }
        
        station.activate();
        updateStationStatus(stationId);
        
        // Start any assigned agents
        for (Agent agent : station.getAssignedAgents()) {
            if (agent.getState() == AgentState.IDLE) {
                coordinatorService.startAgent(agent.getId());
            }
        }
        
        notifyListeners(StationEvent.ACTIVATED, station);
    }
    
    /**
     * Deactivates a station, stopping all processing.
     */
    public void deactivateStation(@NotNull String stationId) {
        Station station = stations.get(stationId);
        if (station == null) {
            throw new IllegalArgumentException("Station not found: " + stationId);
        }
        
        station.deactivate();
        updateStationStatus(stationId);
        
        // Stop any assigned agents
        for (Agent agent : station.getAssignedAgents()) {
            if (agent.getState() == AgentState.ACTIVE) {
                coordinatorService.stopAgent(agent.getId());
            }
        }
        
        notifyListeners(StationEvent.DEACTIVATED, station);
    }
    
    /**
     * Removes a station and reassigns its agents.
     */
    public void removeStation(@NotNull String stationId) {
        Station station = stations.remove(stationId);
        if (station == null) {
            return;
        }
        
        // Reassign agents to other available stations
        List<Agent> orphanedAgents = new ArrayList<>(station.getAssignedAgents());
        for (Agent agent : orphanedAgents) {
            Station newStation = findAvailableStation(agent);
            if (newStation != null) {
                assignAgentToStation(agent.getId(), newStation.getId());
            }
        }
        
        statusCache.remove(stationId);
        notifyListeners(StationEvent.REMOVED, station);
    }
    
    /**
     * Gets the current status of a station.
     */
    @Nullable
    public StationStatus getStationStatus(@NotNull String stationId) {
        return statusCache.get(stationId);
    }
    
    /**
     * Gets all active stations.
     */
    @NotNull
    public List<Station> getActiveStations() {
        return stations.values().stream()
            .filter(Station::isActive)
            .collect(Collectors.toList());
    }
    
    /**
     * Finds the best station for a given task type.
     */
    @Nullable
    public Station findBestStation(@NotNull String taskType) {
        return stations.values().stream()
            .filter(Station::isActive)
            .filter(s -> s.getConfiguration().supportsTaskType(taskType))
            .max(Comparator.comparing(s -> calculateStationScore(s, taskType)))
            .orElse(null);
    }
    
    /**
     * Registers a station event listener.
     */
    public void addListener(@NotNull StationListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes a station event listener.
     */
    public void removeListener(@NotNull StationListener listener) {
        listeners.remove(listener);
    }
    
    private void initializeScheduledTasks() {
        // Regular status updates
        scheduler.scheduleWithFixedDelay(
            this::updateAllStationStatuses,
            STATUS_UPDATE_INTERVAL,
            STATUS_UPDATE_INTERVAL,
            TimeUnit.MILLISECONDS
        );
        
        // Idle station cleanup
        scheduler.scheduleWithFixedDelay(
            this::cleanupIdleStations,
            IDLE_TIMEOUT,
            IDLE_TIMEOUT,
            TimeUnit.MILLISECONDS
        );
    }
    
    private void updateAllStationStatuses() {
        for (String stationId : stations.keySet()) {
            updateStationStatus(stationId);
        }
    }
    
    private void updateStationStatus(@NotNull String stationId) {
        Station station = stations.get(stationId);
        if (station == null) {
            return;
        }
        
        StationStatus status = statusCache.computeIfAbsent(stationId, StationStatus::new);
        
        // Update basic metrics
        status.setActive(station.isActive());
        status.setAgentCount(station.getAssignedAgents().size());
        status.setTasksProcessed(station.getTasksProcessed());
        status.setAverageProcessingTime(station.getAverageProcessingTime());
        
        // Calculate load based on active agents
        long activeAgents = station.getAssignedAgents().stream()
            .filter(a -> a.getState() == AgentState.ACTIVE)
            .count();
        double load = station.getAssignedAgents().isEmpty() ? 0.0 :
            (double) activeAgents / station.getAssignedAgents().size();
        status.setCurrentLoad(load);
        
        // Calculate health score
        double healthScore = calculateStationHealth(station);
        status.setHealthScore(healthScore);
        
        status.updateTimestamp();
    }
    
    private double calculateStationHealth(@NotNull Station station) {
        double baseHealth = 1.0;
        
        // Factor in agent trust scores
        double avgTrust = station.getAssignedAgents().stream()
            .mapToDouble(a -> trustManager.getTrustScore(a.getId()))
            .average()
            .orElse(0.5);
        
        // Factor in error rate
        double errorRate = station.getErrorRate();
        
        // Factor in response time
        double avgResponseTime = station.getAverageProcessingTime();
        double responseTimeFactor = Math.max(0, 1 - (avgResponseTime / 10000)); // 10s baseline
        
        return baseHealth * avgTrust * (1 - errorRate) * responseTimeFactor;
    }
    
    private double calculateStationScore(@NotNull Station station, @NotNull String taskType) {
        StationStatus status = statusCache.get(station.getId());
        if (status == null) {
            return 0.0;
        }
        
        double score = status.getHealthScore();
        
        // Prefer stations with lower load
        score *= (1 - status.getCurrentLoad());
        
        // Boost for specialized stations
        if (station.getConfiguration().isPrimaryTaskType(taskType)) {
            score *= 1.5;
        }
        
        return score;
    }
    
    @Nullable
    private Station findAvailableStation(@NotNull Agent agent) {
        return stations.values().stream()
            .filter(s -> s.isActive() && s.canAcceptAgent(agent))
            .min(Comparator.comparing(s -> s.getAssignedAgents().size()))
            .orElse(null);
    }
    
    private void cleanupIdleStations() {
        long currentTime = System.currentTimeMillis();
        
        List<String> idleStations = stations.entrySet().stream()
            .filter(e -> {
                StationStatus status = statusCache.get(e.getKey());
                return status != null && 
                       !e.getValue().isActive() &&
                       (currentTime - status.getLastUpdateTime()) > IDLE_TIMEOUT;
            })
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        for (String stationId : idleStations) {
            LOG.info("Removing idle station: " + stationId);
            removeStation(stationId);
        }
    }
    
    @NotNull
    private String generateStationId(@NotNull String name) {
        String base = name.toLowerCase().replaceAll("[^a-z0-9]", "_");
        String id = base;
        int counter = 1;
        
        while (stations.containsKey(id)) {
            id = base + "_" + counter++;
        }
        
        return id;
    }
    
    private void notifyListeners(@NotNull StationEvent event, @NotNull Station station) {
        for (StationListener listener : listeners) {
            try {
                listener.onStationEvent(event, station);
            } catch (Exception e) {
                LOG.error("Error notifying station listener", e);
            }
        }
    }
    
    public void dispose() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        stations.clear();
        statusCache.clear();
        listeners.clear();
    }
    
    public enum StationEvent {
        CREATED, ACTIVATED, DEACTIVATED, REMOVED, AGENT_ASSIGNED, AGENT_REMOVED
    }
    
    public interface StationListener {
        void onStationEvent(@NotNull StationEvent event, @NotNull Station station);
    }
}