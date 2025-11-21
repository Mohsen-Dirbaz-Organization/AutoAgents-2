package com.IDE.plugin.ai.multiagent.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * EventBus for publishing and subscribing to events across the agent system
 */
public class EventBus {
    private final Map<Class<?>, List<Consumer<Object>>> subscribers = new ConcurrentHashMap<>();
    private final Map<String, List<Consumer<Object>>> topicSubscribers = new ConcurrentHashMap<>();
    
    /**
     * Subscribe to events of a specific type
     */
    public <T> void subscribe(Class<T> eventType, Consumer<T> handler) {
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                   .add((Consumer<Object>) handler);
    }
    
    /**
     * Subscribe to a specific topic
     */
    public void subscribe(String topic, Consumer<Object> handler) {
        topicSubscribers.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>())
                        .add(handler);
    }
    
    /**
     * Publish an event to all subscribers
     */
    public void publish(Object event) {
        Class<?> eventType = event.getClass();
        
        // Notify type-based subscribers
        List<Consumer<Object>> handlers = subscribers.get(eventType);
        if (handlers != null) {
            handlers.forEach(handler -> {
                try {
                    handler.accept(event);
                } catch (Exception e) {
                    // Log error but don't interrupt other handlers
                    System.err.println("Error in event handler: " + e.getMessage());
                }
            });
        }
        
        // Check parent classes and interfaces
        notifyParentTypeSubscribers(event, eventType.getSuperclass());
        for (Class<?> iface : eventType.getInterfaces()) {
            notifyParentTypeSubscribers(event, iface);
        }
    }
    
    /**
     * Publish an event to a specific topic
     */
    public void publish(String topic, Object event) {
        List<Consumer<Object>> handlers = topicSubscribers.get(topic);
        if (handlers != null) {
            handlers.forEach(handler -> {
                try {
                    handler.accept(event);
                } catch (Exception e) {
                    System.err.println("Error in topic handler: " + e.getMessage());
                }
            });
        }
    }
    
    /**
     * Unsubscribe from a specific event type
     */
    public <T> void unsubscribe(Class<T> eventType, Consumer<T> handler) {
        List<Consumer<Object>> handlers = subscribers.get(eventType);
        if (handlers != null) {
            handlers.remove(handler);
        }
    }
    
    /**
     * Unsubscribe from a specific topic
     */
    public void unsubscribe(String topic, Consumer<Object> handler) {
        List<Consumer<Object>> handlers = topicSubscribers.get(topic);
        if (handlers != null) {
            handlers.remove(handler);
        }
    }
    
    /**
     * Clear all subscriptions
     */
    public void clear() {
        subscribers.clear();
        topicSubscribers.clear();
    }
    
    private void notifyParentTypeSubscribers(Object event, Class<?> parentType) {
        if (parentType != null && parentType != Object.class) {
            List<Consumer<Object>> handlers = subscribers.get(parentType);
            if (handlers != null) {
                handlers.forEach(handler -> {
                    try {
                        handler.accept(event);
                    } catch (Exception e) {
                        System.err.println("Error in parent type handler: " + e.getMessage());
                    }
                });
            }
            notifyParentTypeSubscribers(event, parentType.getSuperclass());
        }
    }
}