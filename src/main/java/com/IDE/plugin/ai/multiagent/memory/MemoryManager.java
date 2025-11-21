package com.IDE.plugin.ai.multiagent.memory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * MemoryManager handles short-term and long-term memory for agents
 */
public class MemoryManager {
    private final Map<String, Memory> agentMemories = new ConcurrentHashMap<>();
    private final int maxShortTermSize = 100;
    private final int maxLongTermSize = 1000;
    
    /**
     * Store a memory item for an agent
     */
    public void store(String agentId, String key, Object value) {
        Memory memory = agentMemories.computeIfAbsent(agentId, k -> new Memory());
        memory.store(key, value);
    }
    
    /**
     * Retrieve a memory item for an agent
     */
    public Object retrieve(String agentId, String key) {
        Memory memory = agentMemories.get(agentId);
        return memory != null ? memory.retrieve(key) : null;
    }
    
    /**
     * Add an experience to agent's episodic memory
     */
    public void addExperience(String agentId, Experience experience) {
        Memory memory = agentMemories.computeIfAbsent(agentId, k -> new Memory());
        memory.addExperience(experience);
    }
    
    /**
     * Get recent experiences for an agent
     */
    public List<Experience> getRecentExperiences(String agentId, int count) {
        Memory memory = agentMemories.get(agentId);
        return memory != null ? memory.getRecentExperiences(count) : Collections.emptyList();
    }
    
    /**
     * Search experiences by criteria
     */
    public List<Experience> searchExperiences(String agentId, String query) {
        Memory memory = agentMemories.get(agentId);
        return memory != null ? memory.searchExperiences(query) : Collections.emptyList();
    }
    
    /**
     * Clear memory for a specific agent
     */
    public void clearAgentMemory(String agentId) {
        agentMemories.remove(agentId);
    }
    
    /**
     * Clear all memories
     */
    public void clear() {
        agentMemories.clear();
    }
    
    /**
     * Get memory statistics for an agent
     */
    public MemoryStats getStats(String agentId) {
        Memory memory = agentMemories.get(agentId);
        return memory != null ? memory.getStats() : new MemoryStats();
    }
    
    /**
     * Inner class representing agent memory
     */
    private class Memory {
        private final Map<String, Object> shortTermMemory = new ConcurrentHashMap<>();
        private final Map<String, Object> longTermMemory = new ConcurrentHashMap<>();
        private final Deque<Experience> episodicMemory = new ConcurrentLinkedDeque<>();
        
        public void store(String key, Object value) {
            // Store in short-term first
            shortTermMemory.put(key, value);
            
            // If short-term is full, promote oldest to long-term
            if (shortTermMemory.size() > maxShortTermSize) {
                promoteToLongTerm();
            }
        }
        
        public Object retrieve(String key) {
            // Check short-term first
            Object value = shortTermMemory.get(key);
            if (value != null) {
                return value;
            }
            
            // Then check long-term
            value = longTermMemory.get(key);
            if (value != null) {
                // Promote back to short-term on access
                shortTermMemory.put(key, value);
            }
            
            return value;
        }
        
        public void addExperience(Experience experience) {
            episodicMemory.addFirst(experience);
            
            // Limit episodic memory size
            while (episodicMemory.size() > maxLongTermSize) {
                episodicMemory.removeLast();
            }
        }
        
        public List<Experience> getRecentExperiences(int count) {
            List<Experience> recent = new ArrayList<>();
            Iterator<Experience> iterator = episodicMemory.iterator();
            
            while (iterator.hasNext() && recent.size() < count) {
                recent.add(iterator.next());
            }
            
            return recent;
        }
        
        public List<Experience> searchExperiences(String query) {
            List<Experience> matches = new ArrayList<>();
            String lowerQuery = query.toLowerCase();
            
            for (Experience exp : episodicMemory) {
                if (exp.matches(lowerQuery)) {
                    matches.add(exp);
                }
            }
            
            return matches;
        }
        
        private void promoteToLongTerm() {
            // Simple FIFO promotion for now
            Iterator<Map.Entry<String, Object>> iterator = shortTermMemory.entrySet().iterator();
            if (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                longTermMemory.put(entry.getKey(), entry.getValue());
                iterator.remove();
                
                // Ensure long-term doesn't grow too large
                if (longTermMemory.size() > maxLongTermSize) {
                    // Remove oldest entries (simplified approach)
                    Iterator<String> ltIterator = longTermMemory.keySet().iterator();
                    if (ltIterator.hasNext()) {
                        ltIterator.next();
                        ltIterator.remove();
                    }
                }
            }
        }
        
        public MemoryStats getStats() {
            MemoryStats stats = new MemoryStats();
            stats.shortTermSize = shortTermMemory.size();
            stats.longTermSize = longTermMemory.size();
            stats.episodicSize = episodicMemory.size();
            return stats;
        }
    }
    
    /**
     * Experience class for episodic memory
     */
    public static class Experience {
        private final String type;
        private final String description;
        private final Map<String, Object> context;
        private final long timestamp;
        
        public Experience(String type, String description, Map<String, Object> context) {
            this.type = type;
            this.description = description;
            this.context = new HashMap<>(context);
            this.timestamp = System.currentTimeMillis();
        }
        
        public boolean matches(String query) {
            return type.toLowerCase().contains(query) || 
                   description.toLowerCase().contains(query);
        }
        
        // Getters
        public String getType() { return type; }
        public String getDescription() { return description; }
        public Map<String, Object> getContext() { return new HashMap<>(context); }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * Memory statistics
     */
    public static class MemoryStats {
        public int shortTermSize;
        public int longTermSize;
        public int episodicSize;
        
        public int getTotalSize() {
            return shortTermSize + longTermSize + episodicSize;
        }
    }
}