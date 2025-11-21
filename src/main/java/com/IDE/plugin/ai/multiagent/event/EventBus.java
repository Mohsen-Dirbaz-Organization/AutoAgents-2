package com.IDE.plugin.ai.multiagent.event;

import java.util.function.Consumer;

/**
 * Interface for an event bus that allows publishing and subscribing to events.
 */
public interface EventBus {
    /**
     * Publishes an event to all subscribers.
     * 
     * @param event The event to publish
     */
    void publish(AgentEvent event);
    
    /**
     * Subscribes to events of a specific type.
     * 
     * @param eventType The type of events to subscribe to
     * @param handler The handler to call when an event of the specified type is published
     * @param <T> The type of events to subscribe to
     * @return A subscription ID that can be used to unsubscribe
     */
    <T extends AgentEvent> String subscribe(Class<T> eventType, Consumer<T> handler);
    
    /**
     * Unsubscribes from events using the subscription ID.
     * 
     * @param subscriptionId The subscription ID returned by the subscribe method
     * @return true if the subscription was found and removed, false otherwise
     */
    boolean unsubscribe(String subscriptionId);
    
    /**
     * Unsubscribes all handlers for a specific event type.
     * 
     * @param eventType The type of events to unsubscribe from
     * @param <T> The type of events to unsubscribe from
     * @return The number of subscriptions removed
     */
    <T extends AgentEvent> int unsubscribeAll(Class<T> eventType);
    
    /**
     * Clears all subscriptions.
     */
    void clear();
}