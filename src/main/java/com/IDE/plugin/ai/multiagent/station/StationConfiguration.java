/**
 * Configuration class for stations that defines their capabilities, constraints, and behavior.
 * Used by the Station and StationManager classes to manage station creation and operation.
 */
package com.IDE.plugin.ai.multiagent.station;

import com.IDE.plugin.ai.multiagent.agent.AgentCapability;
import com.IDE.plugin.ai.multiagent.core.AgentRole;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StationConfiguration {
    private final String name;
    private final int maxAgents;
    private final Set<AgentCapability> requiredCapabilities;
    private final Set<String> supportedTaskTypes;
    private final Set<String> primaryTaskTypes;
    private final Map<String, Object> properties;
    private final Set<AgentRole> preferredRoles;

    private StationConfiguration(Builder builder) {
        this.name = builder.name;
        this.maxAgents = builder.maxAgents;
        this.requiredCapabilities = Collections.unmodifiableSet(new HashSet<>(builder.requiredCapabilities));
        this.supportedTaskTypes = Collections.unmodifiableSet(new HashSet<>(builder.supportedTaskTypes));
        this.primaryTaskTypes = Collections.unmodifiableSet(new HashSet<>(builder.primaryTaskTypes));
        this.properties = Collections.unmodifiableMap(new HashMap<>(builder.properties));
        this.preferredRoles = Collections.unmodifiableSet(new HashSet<>(builder.preferredRoles));
    }

    /**
     * Gets the station name.
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Gets the maximum number of agents allowed in the station.
     */
    public int getMaxAgents() {
        return maxAgents;
    }

    /**
     * Gets the set of capabilities required for agents in this station.
     */
    @NotNull
    public Set<AgentCapability> getRequiredCapabilities() {
        return requiredCapabilities;
    }

    /**
     * Checks if the station supports a specific task type.
     */
    public boolean supportsTaskType(@NotNull String taskType) {
        return supportedTaskTypes.contains(taskType);
    }

    /**
     * Checks if the task type is a primary focus for this station.
     */
    public boolean isPrimaryTaskType(@NotNull String taskType) {
        return primaryTaskTypes.contains(taskType);
    }

    /**
     * Gets a property value by key.
     */
    public Object getProperty(@NotNull String key) {
        return properties.get(key);
    }

    /**
     * Gets the set of preferred agent roles for this station.
     */
    @NotNull
    public Set<AgentRole> getPreferredRoles() {
        return preferredRoles;
    }

    /**
     * Converts the configuration to a map representation.
     */
    @NotNull
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("maxAgents", maxAgents);
        map.put("requiredCapabilities", requiredCapabilities);
        map.put("supportedTaskTypes", supportedTaskTypes);
        map.put("primaryTaskTypes", primaryTaskTypes);
        map.put("properties", properties);
        map.put("preferredRoles", preferredRoles);
        return map;
    }

    /**
     * Builder for creating StationConfiguration instances.
     */
    public static class Builder {
        private String name;
        private int maxAgents = 5; // Default value
        private final Set<AgentCapability> requiredCapabilities = new HashSet<>();
        private final Set<String> supportedTaskTypes = new HashSet<>();
        private final Set<String> primaryTaskTypes = new HashSet<>();
        private final Map<String, Object> properties = new HashMap<>();
        private final Set<AgentRole> preferredRoles = new HashSet<>();

        public Builder(@NotNull String name) {
            this.name = name;
        }

        public Builder maxAgents(int maxAgents) {
            this.maxAgents = maxAgents;
            return this;
        }

        public Builder addRequiredCapability(@NotNull AgentCapability capability) {
            requiredCapabilities.add(capability);
            return this;
        }

        public Builder addSupportedTaskType(@NotNull String taskType) {
            supportedTaskTypes.add(taskType);
            return this;
        }

        public Builder addPrimaryTaskType(@NotNull String taskType) {
            primaryTaskTypes.add(taskType);
            supportedTaskTypes.add(taskType); // Primary tasks are also supported
            return this;
        }

        public Builder addProperty(@NotNull String key, @NotNull Object value) {
            properties.put(key, value);
            return this;
        }

        public Builder addPreferredRole(@NotNull AgentRole role) {
            preferredRoles.add(role);
            return this;
        }

        public StationConfiguration build() {
            return new StationConfiguration(this);
        }
    }
}