package com.IDE.plugin.ai.multiagent.communication;

/**
 * MessageHandler: Interface for handling messages
 */
public interface MessageHandler {
    /**
     * Handle an incoming message
     * @param message The message to handle
     */
    void handleMessage(Message message);
}