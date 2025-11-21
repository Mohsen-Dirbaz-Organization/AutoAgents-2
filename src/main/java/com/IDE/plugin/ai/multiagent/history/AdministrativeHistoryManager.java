package com.IDE.plugin.ai.multiagent.history;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

/**
 * AdministrativeHistoryManager tracks administrative actions and history
 */
public class AdministrativeHistoryManager {
    private final Deque<HistoryEntry> historyEntries = new ConcurrentLinkedDeque<>();
    private final Map<String, List<HistoryEntry>> agentHistories = new ConcurrentHashMap<>();
    private final Map<String, ActionStatistics> actionStats = new ConcurrentHashMap<>();
    private final int maxHistorySize = 10000;
    
    /**
     * Record an administrative action
     */
    public void recordAction(String agentId, String action, Map<String, Object> details) {
        HistoryEntry entry = new HistoryEntry(agentId, action, details);
        
        // Add to global history
        historyEntries.addFirst(entry);
        
        // Add to agent-specific history
        agentHistories.computeIfAbsent(agentId, k -> new ArrayList<>()).add(entry);
        
        // Update statistics
        updateStatistics(agentId, action);
        
        // Maintain size limit
        while (historyEntries.size() > maxHistorySize) {
            historyEntries.removeLast();
        }
    }
    
    /**
     * Get recent history entries
     */
    public List<HistoryEntry> getRecentHistory(int count) {
        return historyEntries.stream()
                .limit(count)
                .collect(Collectors.toList());
    }
    
    /**
     * Get history for a specific agent
     */
    public List<HistoryEntry> getAgentHistory(String agentId) {
        return agentHistories.getOrDefault(agentId, Collections.emptyList());
    }
    
    /**
     * Get history by action type
     */
    public List<HistoryEntry> getHistoryByAction(String action) {
        return historyEntries.stream()
                .filter(entry -> entry.getAction().equals(action))
                .collect(Collectors.toList());
    }
    
    /**
     * Get history within a time range
     */
    public List<HistoryEntry> getHistoryInRange(long startTime, long endTime) {
        return historyEntries.stream()
                .filter(entry -> entry.getTimestamp() >= startTime && entry.getTimestamp() <= endTime)
                .collect(Collectors.toList());
    }
    
    /**
     * Search history by criteria
     */
    public List<HistoryEntry> searchHistory(String query) {
        String lowerQuery = query.toLowerCase();
        return historyEntries.stream()
                .filter(entry -> entry.matches(lowerQuery))
                .collect(Collectors.toList());
    }
    
    /**
     * Get action statistics for an agent
     */
    public ActionStatistics getActionStats(String agentId) {
        return actionStats.getOrDefault(agentId, new ActionStatistics());
    }
    
    /**
     * Get overall statistics
     */
    public Map<String, ActionStatistics> getAllStats() {
        return new HashMap<>(actionStats);
    }
    
    /**
     * Clear history for a specific agent
     */
    public void clearAgentHistory(String agentId) {
        agentHistories.remove(agentId);
        actionStats.remove(agentId);
        
        // Remove from global history
        historyEntries.removeIf(entry -> entry.getAgentId().equals(agentId));
    }
    
    /**
     * Clear all history
     */
    public void clear() {
        historyEntries.clear();
        agentHistories.clear();
        actionStats.clear();
    }
    
    /**
     * Export history to a format suitable for analysis
     */
    public HistoryExport exportHistory() {
        HistoryExport export = new HistoryExport();
        export.entries = new ArrayList<>(historyEntries);
        export.statistics = new HashMap<>(actionStats);
        export.exportTime = System.currentTimeMillis();
        return export;
    }
    
    private void updateStatistics(String agentId, String action) {
        ActionStatistics stats = actionStats.computeIfAbsent(agentId, k -> new ActionStatistics());
        stats.incrementAction(action);
    }
    
    /**
     * History entry class
     */
    public static class HistoryEntry {
        private final String id;
        private final String agentId;
        private final String action;
        private final Map<String, Object> details;
        private final long timestamp;
        
        public HistoryEntry(String agentId, String action, Map<String, Object> details) {
            this.id = UUID.randomUUID().toString();
            this.agentId = agentId;
            this.action = action;
            this.details = details != null ? new HashMap<>(details) : new HashMap<>();
            this.timestamp = System.currentTimeMillis();
        }
        
        public boolean matches(String query) {
            return agentId.toLowerCase().contains(query) ||
                   action.toLowerCase().contains(query) ||
                   details.toString().toLowerCase().contains(query);
        }
        
        // Getters
        public String getId() { return id; }
        public String getAgentId() { return agentId; }
        public String getAction() { return action; }
        public Map<String, Object> getDetails() { return new HashMap<>(details); }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * Action statistics class
     */
    public static class ActionStatistics {
        private final Map<String, Integer> actionCounts = new ConcurrentHashMap<>();
        private long firstActionTime = 0;
        private long lastActionTime = 0;
        private int totalActions = 0;
        
        public void incrementAction(String action) {
            actionCounts.merge(action, 1, Integer::sum);
            totalActions++;
            
            long now = System.currentTimeMillis();
            if (firstActionTime == 0) {
                firstActionTime = now;
            }
            lastActionTime = now;
        }
        
        public Map<String, Integer> getActionCounts() {
            return new HashMap<>(actionCounts);
        }
        
        public int getTotalActions() {
            return totalActions;
        }
        
        public long getFirstActionTime() {
            return firstActionTime;
        }
        
        public long getLastActionTime() {
            return lastActionTime;
        }
        
        public double getAverageActionsPerHour() {
            if (firstActionTime == 0 || firstActionTime == lastActionTime) {
                return 0;
            }
            
            long durationHours = (lastActionTime - firstActionTime) / (1000 * 60 * 60);
            return durationHours > 0 ? (double) totalActions / durationHours : totalActions;
        }
    }
    
    /**
     * History export class
     */
    public static class HistoryExport {
        public List<HistoryEntry> entries;
        public Map<String, ActionStatistics> statistics;
        public long exportTime;
    }
}