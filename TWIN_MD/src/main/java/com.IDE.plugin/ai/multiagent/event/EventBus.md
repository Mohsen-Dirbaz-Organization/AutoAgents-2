# EventBus

## Overview
Central event distribution system for the multi-agent framework.

## Purpose
The `EventBus` class implements a publish-subscribe pattern for distributing events throughout the system, enabling loose coupling between components.

## Key Components

### Core Features
- Event publishing
- Subscriber registration
- Event filtering
- Asynchronous delivery
- Event prioritization

### Subscription Management
- Topic-based subscriptions
- Wildcard pattern matching
- Dynamic subscription updates
- Subscriber lifecycle management

### Delivery Mechanisms
- Synchronous delivery
- Asynchronous delivery
- Guaranteed delivery
- Ordered delivery
- Broadcast delivery

### Methods
- Publish event
- Subscribe/unsubscribe
- Register event handler
- Configure delivery options
- Query subscription status

## Event Processing
- Event validation
- Event transformation
- Event aggregation
- Dead letter handling
- Retry mechanisms

## Usage
Used by all components for event-driven communication.

## Integration Points
- All agents publish/subscribe to events
- `SystemMonitor`: Monitors event flow
- `MessageBus`: Complementary messaging system
- `HistoryManager`: Records event history

## Related Classes
- `AgentEvent`: Common event type
- `EventHandler`: Event processing interface
- `Message`: Alternative communication mechanism