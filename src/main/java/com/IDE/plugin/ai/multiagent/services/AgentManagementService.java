/**
 * Service for managing agent deployment, configuration, and monitoring.
 * Integrates with StationManager and AgentCoordinatorService to provide
 * a comprehensive agent management system.
 */
package com.IDE.plugin.ai.multiagent.services;

import com.IDE.plugin.ai.multiagent.agent.Agent;
import com.IDE.plugin.ai.multiagent.agent.AgentState;
import com.IDE.plugin.ai.multiagent.core.AgentRole;
import com.IDE.plugin.ai.multiagent.integration.ClaudeCodeIntegration;
import com.IDE.plugin.ai.multiagent.station.Station;
import com.IDE.plugin.ai.multiagent.station.StationConfiguration;
import com.IDE.plugin.ai.multiagent.station.StationManager;
import com.IDE.plugin.ai.multiagent.trust.TrustManager;
import com.IDE.plugin.ai.multiagent.memory.core.MemoryStateManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service(Service.Level.PROJECT)
public final class AgentManagementService {
    private static final Logger LOG = Logger.getInstance(AgentManagementService.class);

    private final Project project;
    private final StationManager stationManager;
    private final AgentCoordinatorService coordinatorService;
    private final TrustManager trustManager;
    private final MemoryStateManager memoryStateManager;
    private final ExecutorService executorService;
    private final Map<String, AgentDeploymentConfig> deploymentConfigs;
    private final Map<String, AgentMonitoringData> monitoringData;
    private final List<AgentManagementListener> listeners;
    private final ClaudeCodeIntegration claudeCodeIntegration;

    public AgentManagementService(@NotNull Project project) {
        this.project = project;
        this.stationManager = project.getService(StationManager.class);
        this.coordinatorService = project.getService(AgentCoordinatorService.class);
        this.trustManager = project.getService(TrustManager.class);
        this.memoryStateManager = project.getService(MemoryStateManager.class);
        this.executorService = Executors.newCachedThreadPool();
        this.deploymentConfigs = new ConcurrentHashMap<>();
        this.monitoringData = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.claudeCodeIntegration = new ClaudeCodeIntegration();

        initializeMonitoring();
    }

    /**
     * Deploys an agent with the specified configuration.
     * 
     * @param config The agent deployment configuration
     * @return A CompletableFuture that completes with the agent ID when deployment is complete
     */
    public CompletableFuture<String> deployAgent(@NotNull AgentDeploymentConfig config) {
        LOG.info("Deploying agent with role: " + config.getRole());

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Create agent registration request
                AgentRegistrationRequest request = new AgentRegistrationRequest(
                    config.getName(),
                    config.getRole(),
                    config.getCapabilities(),
                    config.getConfiguration()
                );

                // Register agent with coordinator service
                String agentId = coordinatorService.registerAgent(request).get();

                // Store deployment configuration
                deploymentConfigs.put(agentId, config);

                // Initialize monitoring data
                monitoringData.put(agentId, new AgentMonitoringData(agentId, config.getRole()));

                // Assign to station if specified
                if (config.getStationId() != null) {
                    stationManager.assignAgentToStation(agentId, config.getStationId());
                } else if (config.isAutoAssignStation()) {
                    assignToOptimalStation(agentId, config.getRole());
                }

                // Notify listeners
                notifyListeners(AgentManagementEvent.AGENT_DEPLOYED, agentId);

                return agentId;
            } catch (Exception e) {
                LOG.error("Failed to deploy agent", e);
                throw new RuntimeException("Agent deployment failed", e);
            }
        }, executorService);
    }

    /**
     * Undeploys an agent.
     * 
     * @param agentId The ID of the agent to undeploy
     * @return A CompletableFuture that completes when undeployment is complete
     */
    public CompletableFuture<Void> undeployAgent(@NotNull String agentId) {
        LOG.info("Undeploying agent: " + agentId);

        return CompletableFuture.runAsync(() -> {
            try {
                // Remove from station first
                stationManager.removeAgentFromStation(agentId);

                // Unregister from coordinator service
                coordinatorService.unregisterAgent(agentId).get();

                // Clean up local data
                deploymentConfigs.remove(agentId);
                monitoringData.remove(agentId);

                // Notify listeners
                notifyListeners(AgentManagementEvent.AGENT_UNDEPLOYED, agentId);
            } catch (Exception e) {
                LOG.error("Failed to undeploy agent: " + agentId, e);
                throw new RuntimeException("Agent undeployment failed", e);
            }
        }, executorService);
    }

    /**
     * Updates an agent's configuration.
     * 
     * @param agentId The ID of the agent to update
     * @param config The new configuration
     * @return A CompletableFuture that completes when the update is complete
     */
    public CompletableFuture<Void> updateAgentConfiguration(@NotNull String agentId, @NotNull Map<String, Object> config) {
        LOG.info("Updating configuration for agent: " + agentId);

        return CompletableFuture.runAsync(() -> {
            try {
                Agent agent = coordinatorService.getAgent(agentId);
                if (agent == null) {
                    throw new IllegalArgumentException("Agent not found: " + agentId);
                }

                // Update agent configuration
                for (Map.Entry<String, Object> entry : config.entrySet()) {
                    agent.setConfiguration(entry.getKey(), entry.getValue());
                }

                // Update local deployment config
                AgentDeploymentConfig deploymentConfig = deploymentConfigs.get(agentId);
                if (deploymentConfig != null) {
                    deploymentConfig.getConfiguration().putAll(config);
                }

                // Notify listeners
                notifyListeners(AgentManagementEvent.AGENT_UPDATED, agentId);
            } catch (Exception e) {
                LOG.error("Failed to update agent configuration: " + agentId, e);
                throw new RuntimeException("Agent configuration update failed", e);
            }
        }, executorService);
    }

    /**
     * Reassigns an agent to a different station.
     * 
     * @param agentId The ID of the agent to reassign
     * @param stationId The ID of the station to assign the agent to
     * @return A CompletableFuture that completes when reassignment is complete
     */
    public CompletableFuture<Void> reassignAgent(@NotNull String agentId, @NotNull String stationId) {
        LOG.info("Reassigning agent " + agentId + " to station " + stationId);

        return CompletableFuture.runAsync(() -> {
            try {
                // Remove from current station
                stationManager.removeAgentFromStation(agentId);

                // Assign to new station
                stationManager.assignAgentToStation(agentId, stationId);

                // Update deployment config
                AgentDeploymentConfig config = deploymentConfigs.get(agentId);
                if (config != null) {
                    config.setStationId(stationId);
                }

                // Notify listeners
                notifyListeners(AgentManagementEvent.AGENT_REASSIGNED, agentId);
            } catch (Exception e) {
                LOG.error("Failed to reassign agent: " + agentId, e);
                throw new RuntimeException("Agent reassignment failed", e);
            }
        }, executorService);
    }

    /**
     * Creates a new station with the specified configuration.
     * 
     * @param config The station configuration
     * @return The created station
     */
    public Station createStation(@NotNull StationConfiguration config) {
        LOG.info("Creating station: " + config.getName());

        Station station = stationManager.createStation(config);

        // Notify listeners
        notifyListeners(AgentManagementEvent.STATION_CREATED, station.getId());

        return station;
    }

    /**
     * Gets monitoring data for an agent.
     * 
     * @param agentId The ID of the agent
     * @return The monitoring data, or null if the agent is not found
     */
    @Nullable
    public AgentMonitoringData getAgentMonitoringData(@NotNull String agentId) {
        return monitoringData.get(agentId);
    }

    /**
     * Gets all deployed agents.
     * 
     * @return A list of agent IDs
     */
    @NotNull
    public List<String> getDeployedAgents() {
        return new ArrayList<>(deploymentConfigs.keySet());
    }

    /**
     * Gets the deployment configuration for an agent.
     * 
     * @param agentId The ID of the agent
     * @return The deployment configuration, or null if the agent is not found
     */
    @Nullable
    public AgentDeploymentConfig getAgentDeploymentConfig(@NotNull String agentId) {
        return deploymentConfigs.get(agentId);
    }

    /**
     * Adds a listener for agent management events.
     * 
     * @param listener The listener to add
     */
    public void addListener(@NotNull AgentManagementListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener for agent management events.
     * 
     * @param listener The listener to remove
     */
    public void removeListener(@NotNull AgentManagementListener listener) {
        listeners.remove(listener);
    }

    /**
     * Assigns an agent to the optimal station based on its role and capabilities.
     */
    private void assignToOptimalStation(@NotNull String agentId, @NotNull AgentRole role) {
        // Find stations that prefer this role
        List<Station> compatibleStations = stationManager.getActiveStations().stream()
            .filter(s -> s.getConfiguration().getPreferredRoles().contains(role))
            .filter(s -> s.canAcceptAgent(coordinatorService.getAgent(agentId)))
            .collect(Collectors.toList());

        if (!compatibleStations.isEmpty()) {
            // Find station with fewest agents
            Station optimalStation = compatibleStations.stream()
                .min(Comparator.comparing(s -> s.getAssignedAgents().size()))
                .orElse(compatibleStations.get(0));

            stationManager.assignAgentToStation(agentId, optimalStation.getId());
        }
    }

    /**
     * Initializes the monitoring system.
     */
    private void initializeMonitoring() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        // Schedule regular monitoring updates
        scheduler.scheduleAtFixedRate(
            this::updateMonitoringData,
            0,
            5,
            TimeUnit.SECONDS
        );
    }

    /**
     * Updates monitoring data for all agents.
     */
    private void updateMonitoringData() {
        try {
            for (String agentId : deploymentConfigs.keySet()) {
                Agent agent = coordinatorService.getAgent(agentId);
                if (agent != null) {
                    AgentMonitoringData data = monitoringData.computeIfAbsent(
                        agentId,
                        id -> new AgentMonitoringData(id, agent.getRole())
                    );

                    // Update state
                    data.setState(agent.getState());

                    // Update metrics
                    data.setSuccessRate(agent.getMetrics().getSuccessRate());
                    data.setErrorRate(agent.getMetrics().getErrorRate());
                    data.setAverageResponseTime(agent.getMetrics().getAverageResponseTime());
                    data.setTasksProcessed(agent.getMetrics().getTasksProcessed());

                    // Update trust score
                    data.setTrustScore(trustManager.getTrustScore(agentId));

                    // Update memory usage
                    data.setMemoryUsage(memoryStateManager.getAgentMemoryUsage(agentId));
                }
            }
        } catch (Exception e) {
            LOG.error("Error updating monitoring data", e);
        }
    }

    /**
     * Notifies listeners of an event.
     */
    private void notifyListeners(@NotNull AgentManagementEvent event, @NotNull String entityId) {
        for (AgentManagementListener listener : listeners) {
            try {
                listener.onEvent(event, entityId);
            } catch (Exception e) {
                LOG.error("Error notifying listener", e);
            }
        }
    }

    /**
     * Requests code generation from Claude for an agent.
     * 
     * @param agentId The ID of the agent making the request
     * @param prompt The prompt for code generation
     * @param options Additional options for the request
     * @return A CompletableFuture that completes with the generated code
     */
    public CompletableFuture<String> generateCode(@NotNull String agentId, @NotNull String prompt, @NotNull Map<String, Object> options) {
        LOG.info("Agent " + agentId + " requesting code generation");

        Agent agent = coordinatorService.getAgent(agentId);
        if (agent == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Agent not found: " + agentId));
        }

        return claudeCodeIntegration.generateCode(agent, prompt, options);
    }

    /**
     * Requests code analysis from Claude for an agent.
     * 
     * @param agentId The ID of the agent making the request
     * @param code The code to analyze
     * @param analysisType The type of analysis to perform
     * @param options Additional options for the request
     * @return A CompletableFuture that completes with the analysis result
     */
    public CompletableFuture<String> analyzeCode(@NotNull String agentId, @NotNull String code, @NotNull String analysisType, @NotNull Map<String, Object> options) {
        LOG.info("Agent " + agentId + " requesting code analysis of type " + analysisType);

        Agent agent = coordinatorService.getAgent(agentId);
        if (agent == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Agent not found: " + agentId));
        }

        return claudeCodeIntegration.analyzeCode(agent, code, analysisType, options);
    }

    /**
     * Requests code review from Claude for an agent.
     * 
     * @param agentId The ID of the agent making the request
     * @param code The code to review
     * @param options Additional options for the request
     * @return A CompletableFuture that completes with the review result
     */
    public CompletableFuture<String> reviewCode(@NotNull String agentId, @NotNull String code, @NotNull Map<String, Object> options) {
        LOG.info("Agent " + agentId + " requesting code review");

        Agent agent = coordinatorService.getAgent(agentId);
        if (agent == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Agent not found: " + agentId));
        }

        return claudeCodeIntegration.reviewCode(agent, code, options);
    }

    /**
     * Gets the status of a pending Claude Code request.
     * 
     * @param requestId The ID of the request
     * @return The request context, or null if the request is not found
     */
    @Nullable
    public ClaudeCodeIntegration.RequestContext getClaudeRequestStatus(@NotNull String requestId) {
        return claudeCodeIntegration.getRequestStatus(requestId);
    }

    /**
     * Cancels a pending Claude Code request.
     * 
     * @param requestId The ID of the request to cancel
     * @return True if the request was cancelled, false otherwise
     */
    public boolean cancelClaudeRequest(@NotNull String requestId) {
        return claudeCodeIntegration.cancelRequest(requestId);
    }

    /**
     * Disposes of resources.
     */
    public void dispose() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Configuration for agent deployment.
     */
    public static class AgentDeploymentConfig {
        private final String name;
        private final AgentRole role;
        private final Set<String> capabilities;
        private final Map<String, Object> configuration;
        private String stationId;
        private boolean autoAssignStation;

        public AgentDeploymentConfig(
            @NotNull String name,
            @NotNull AgentRole role,
            @NotNull Set<String> capabilities,
            @NotNull Map<String, Object> configuration
        ) {
            this.name = name;
            this.role = role;
            this.capabilities = new HashSet<>(capabilities);
            this.configuration = new HashMap<>(configuration);
            this.autoAssignStation = true;
        }

        @NotNull
        public String getName() {
            return name;
        }

        @NotNull
        public AgentRole getRole() {
            return role;
        }

        @NotNull
        public Set<String> getCapabilities() {
            return capabilities;
        }

        @NotNull
        public Map<String, Object> getConfiguration() {
            return configuration;
        }

        @Nullable
        public String getStationId() {
            return stationId;
        }

        public void setStationId(@Nullable String stationId) {
            this.stationId = stationId;
        }

        public boolean isAutoAssignStation() {
            return autoAssignStation;
        }

        public void setAutoAssignStation(boolean autoAssignStation) {
            this.autoAssignStation = autoAssignStation;
        }
    }

    /**
     * Data for agent monitoring.
     */
    public static class AgentMonitoringData {
        private final String agentId;
        private final AgentRole role;
        private AgentState state;
        private double successRate;
        private double errorRate;
        private long averageResponseTime;
        private long tasksProcessed;
        private double trustScore;
        private long memoryUsage;
        private final long creationTime;
        private long lastUpdateTime;

        public AgentMonitoringData(@NotNull String agentId, @NotNull AgentRole role) {
            this.agentId = agentId;
            this.role = role;
            this.state = AgentState.INITIALIZING;
            this.creationTime = System.currentTimeMillis();
            this.lastUpdateTime = creationTime;
        }

        @NotNull
        public String getAgentId() {
            return agentId;
        }

        @NotNull
        public AgentRole getRole() {
            return role;
        }

        @NotNull
        public AgentState getState() {
            return state;
        }

        public void setState(@NotNull AgentState state) {
            this.state = state;
            this.lastUpdateTime = System.currentTimeMillis();
        }

        public double getSuccessRate() {
            return successRate;
        }

        public void setSuccessRate(double successRate) {
            this.successRate = successRate;
        }

        public double getErrorRate() {
            return errorRate;
        }

        public void setErrorRate(double errorRate) {
            this.errorRate = errorRate;
        }

        public long getAverageResponseTime() {
            return averageResponseTime;
        }

        public void setAverageResponseTime(long averageResponseTime) {
            this.averageResponseTime = averageResponseTime;
        }

        public long getTasksProcessed() {
            return tasksProcessed;
        }

        public void setTasksProcessed(long tasksProcessed) {
            this.tasksProcessed = tasksProcessed;
        }

        public double getTrustScore() {
            return trustScore;
        }

        public void setTrustScore(double trustScore) {
            this.trustScore = trustScore;
        }

        public long getMemoryUsage() {
            return memoryUsage;
        }

        public void setMemoryUsage(long memoryUsage) {
            this.memoryUsage = memoryUsage;
        }

        public long getCreationTime() {
            return creationTime;
        }

        public long getLastUpdateTime() {
            return lastUpdateTime;
        }

        public long getUptime() {
            return System.currentTimeMillis() - creationTime;
        }
    }

    /**
     * Events for agent management.
     */
    public enum AgentManagementEvent {
        AGENT_DEPLOYED,
        AGENT_UNDEPLOYED,
        AGENT_UPDATED,
        AGENT_REASSIGNED,
        STATION_CREATED,
        STATION_REMOVED
    }

    /**
     * Listener for agent management events.
     */
    public interface AgentManagementListener {
        void onEvent(@NotNull AgentManagementEvent event, @NotNull String entityId);
    }

    /**
     * Request for agent registration.
     */
    public static class AgentRegistrationRequest {
        private final String name;
        private final AgentRole role;
        private final Set<String> capabilities;
        private final Map<String, Object> configuration;

        public AgentRegistrationRequest(
            @NotNull String name,
            @NotNull AgentRole role,
            @NotNull Set<String> capabilities,
            @NotNull Map<String, Object> configuration
        ) {
            this.name = name;
            this.role = role;
            this.capabilities = capabilities;
            this.configuration = configuration;
        }

        @NotNull
        public String getName() {
            return name;
        }

        @NotNull
        public AgentRole getRole() {
            return role;
        }

        @NotNull
        public Set<String> getCapabilities() {
            return capabilities;
        }

        @NotNull
        public Map<String, Object> getConfiguration() {
            return configuration;
        }
    }
}
