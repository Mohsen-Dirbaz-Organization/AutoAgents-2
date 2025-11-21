package com.IDE.plugin.ai.multiagent.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides a shared context for agents to exchange information.
 */
public class SharedContext {
    private final Map<String, Object> sharedData;
    
    /**
     * Creates a new shared context.
     */
    public SharedContext() {
        this.sharedData = new ConcurrentHashMap<>();
    }
    
    /**
     * Puts a value in the shared context.
     * 
     * @param key The key for the value
     * @param value The value to store
     * @return The previous value associated with the key, or null if there was no previous value
     */
    public Object put(String key, Object value) {
        return sharedData.put(key, value);
    }
    
    /**
     * Gets a value from the shared context.
     * 
     * @param key The key for the value
     * @return The value associated with the key, or null if no value is associated with the key
     */
    public Object get(String key) {
        return sharedData.get(key);
    }
    
    /**
     * Gets a value from the shared context, casting it to the specified type.
     * 
     * @param key The key for the value
     * @param type The type to cast the value to
     * @param <T> The type parameter
     * @return The value associated with the key, cast to the specified type, or null if no value is associated with the key
     * @throws ClassCastException if the value cannot be cast to the specified type
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = sharedData.get(key);
        if (value == null) {
            return null;
        }
        return (T) value;
    }
    
    /**
     * Removes a value from the shared context.
     * 
     * @param key The key for the value to remove
     * @return The value that was removed, or null if no value was associated with the key
     */
    public Object remove(String key) {
        return sharedData.remove(key);
    }
    
    /**
     * Checks if the shared context contains a value for the specified key.
     * 
     * @param key The key to check
     * @return true if the shared context contains a value for the key, false otherwise
     */
    public boolean containsKey(String key) {
        return sharedData.containsKey(key);
    }
    
    /**
     * Clears all values from the shared context.
     */
    public void clear() {
        sharedData.clear();
    }
    
    /**
     * Returns the number of key-value pairs in the shared context.
     * 
     * @return The number of key-value pairs
     */
    public int size() {
        return sharedData.size();
    }
    
    /**
     * Returns a copy of all key-value pairs in the shared context.
     * 
     * @return A map containing all key-value pairs
     */
    public Map<String, Object> getAll() {
        return new ConcurrentHashMap<>(sharedData);
    }
}