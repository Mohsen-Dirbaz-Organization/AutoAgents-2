# Task Class Documentation

## Overview
`Task` represents a unit of work to be executed by an agent in the multi-agent system. It encapsulates task metadata, state management, and execution lifecycle with built-in CompletableFuture support for asynchronous operations. The class provides a comprehensive task model with status tracking, error handling, and timing information.

## Multi-Level Architecture

### System Level
- **Role**: Core work unit abstraction
- **Purpose**: Standardize task representation and lifecycle
- **Pattern**: Asynchronous task with future-based completion
- **Integration**: Used by agents, schedulers, and coordinators

### Component Level
- **Type**: Task representation with lifecycle management
- **Package**: `com.IDE.plugin.ai.multiagent.core`
- **Features**:
  - UUID-based identification
  - Status tracking with state machine
  - CompletableFuture integration
  - Timing and performance tracking

## Core Features and Functionality

### Task Identification
- **Unique ID**: Auto-generated UUID for global uniqueness
- **Type Classification**: String-based task type for routing
- **Creation Timestamp**: Instant-based creation time
- **Immutable Identity**: ID cannot be changed after creation

### State Management
- **Status Tracking**: Five distinct states (PENDING, RUNNING, COMPLETED, FAILED, CANCELLED)
- **State Transitions**: Methods for proper state progression
- **Thread Safety**: Volatile fields for concurrent access
- **State Consistency**: Ensures valid state transitions

### Execution Lifecycle
- **Asynchronous Support**: Built-in CompletableFuture
- **Start Tracking**: Records execution start time
- **Completion Handling**: Success and failure paths
- **Cancellation Support**: Graceful task cancellation
- **Duration Calculation**: Start and end time tracking

### Result Management
- **Generic Result**: Object type for flexibility
- **Error Capture**: Exception storage for failures
- **Future Integration**: Automatic future completion
- **Result Availability**: Null-safe result access

## Component Props and Data Structures

### Core Fields
```java
private final String id                    // Unique task identifier
private final String type                  // Task classification
private final Object input                 // Task input data
private final Instant createdAt            // Creation timestamp
private volatile TaskStatus status         // Current status
private volatile Object result             // Execution result
private volatile Exception error           // Failure exception
private volatile Instant startedAt         // Start timestamp
private volatile Instant completedAt       // Completion timestamp
private final CompletableFuture<Object> future  // Async handle
```

### TaskStatus Enum
```java
public enum TaskStatus {
    PENDING,    // Created but not started
    RUNNING,    // Currently executing
    COMPLETED,  // Successfully finished
    FAILED,     // Failed with error
    CANCELLED   // Cancelled before completion
}
```

## Usage Patterns and Integration Points

### Basic Task Creation and Execution
```java
public class TaskExample {
    public void executeTask() {
        // Create task
        Task task = new Task("ANALYSIS", analysisData);
        
        // Submit to agent
        agent.submitTask(task);
        
        // Wait for completion
        task.getFuture().thenAccept(result -> {
            System.out.println("Task completed with result: " + result);
        }).exceptionally(error -> {
            System.err.println("Task failed: " + error.getMessage());
            return null;
        });
    }
}
```

### Task Lifecycle Management
```java
public class TaskExecutor {
    public void executeTask(Task task) {
        try {
            // Mark as running
            task.start();
            
            // Perform work
            Object result = performWork(task.getInput());
            
            // Mark as completed
            task.complete(result);
            
        } catch (Exception e) {
            // Mark as failed
            task.fail(e);
        }
    }
    
    public void cancelTask(Task task) {
        if (task.getStatus() == TaskStatus.PENDING || 
            task.getStatus() == TaskStatus.RUNNING) {
            task.cancel();
        }
    }
}
```

### Async Task Handling
```java
public class AsyncTaskHandler {
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    
    public CompletableFuture<Object> processTaskAsync(Task task) {
        executor.submit(() -> {
            task.start();
            
            try {
                // Simulate async processing
                Object result = processAsynchronously(task);
                task.complete(result);
            } catch (Exception e) {
                task.fail(e);
            }
        });
        
        return task.getFuture();
    }
    
    public void handleMultipleTasks(List<Task> tasks) {
        List<CompletableFuture<Object>> futures = tasks.stream()
            .map(this::processTaskAsync)
            .collect(Collectors.toList());
        
        // Wait for all tasks
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenRun(() -> System.out.println("All tasks completed"));
    }
}
```

### Task Monitoring
```java
public class TaskMonitor {
    public void monitorTask(Task task) {
        // Check status
        switch (task.getStatus()) {
            case PENDING:
                Duration waitTime = Duration.between(task.getCreatedAt(), Instant.now());
                if (waitTime.toMinutes() > 5) {
                    alertLongWaitTime(task);
                }
                break;
                
            case RUNNING:
                Duration runTime = Duration.between(task.getStartedAt(), Instant.now());
                if (runTime.toMinutes() > 30) {
                    alertLongRunningTask(task);
                }
                break;
                
            case FAILED:
                handleTaskFailure(task);
                break;
        }
    }
    
    public TaskMetrics calculateMetrics(Task task) {
        if (task.getStatus() != TaskStatus.COMPLETED && 
            task.getStatus() != TaskStatus.FAILED) {
            return null;
        }
        
        Duration queueTime = Duration.between(
            task.getCreatedAt(), 
            task.getStartedAt()
        );
        
        Duration executionTime = Duration.between(
            task.getStartedAt(), 
            task.getCompletedAt()
        );
        
        return new TaskMetrics(queueTime, executionTime, task.getStatus());
    }
}
```

## Task Type Patterns

### Type-Based Routing
```java
public class TaskRouter {
    private static final Map<String, AgentRole> TASK_TYPE_MAPPING = Map.of(
        "ANALYSIS", AgentRole.ANALYZER,
        "REFACTORING", AgentRole.CODE_EDITOR,
        "TESTING", AgentRole.TESTER,
        "OPTIMIZATION", AgentRole.OPTIMIZER
    );
    
    public void routeTask(Task task) {
        AgentRole targetRole = TASK_TYPE_MAPPING.get(task.getType());
        if (targetRole != null) {
            Agent agent = findAvailableAgent(targetRole);
            agent.submitTask(task);
        } else {
            handleUnknownTaskType(task);
        }
    }
}
```

### Task Chaining
```java
public class TaskChain {
    public CompletableFuture<Object> executeChain(List<String> taskTypes, Object input) {
        CompletableFuture<Object> future = CompletableFuture.completedFuture(input);
        
        for (String taskType : taskTypes) {
            future = future.thenCompose(previousResult -> {
                Task task = new Task(taskType, previousResult);
                agent.submitTask(task);
                return task.getFuture();
            });
        }
        
        return future;
    }
}
```

## Best Practices and Considerations

### Task Design Guidelines
1. **Clear Types**: Use descriptive, consistent task type names
2. **Appropriate Input**: Keep input data serializable and bounded
3. **Result Design**: Design result objects for easy consumption
4. **Error Information**: Provide detailed error context

### Performance Considerations
1. **Input Size**: Limit input data size for efficiency
2. **Result Size**: Avoid large result objects
3. **Timeout Handling**: Implement task timeouts
4. **Resource Cleanup**: Ensure proper cleanup on completion

### Error Handling Strategies
```java
public class TaskErrorHandler {
    public void handleTaskError(Task task) {
        Exception error = task.getError();
        
        if (error instanceof TimeoutException) {
            // Retry with longer timeout
            retryWithTimeout(task);
        } else if (error instanceof ResourceException) {
            // Wait for resources
            queueForRetry(task);
        } else {
            // Log and alert
            logError(task, error);
            alertOperations(task);
        }
    }
}
```

### Task Prioritization
```java
public class PriorityTaskQueue {
    private final PriorityQueue<Task> queue = new PriorityQueue<>(
        Comparator.comparing(this::calculatePriority).reversed()
    );
    
    private int calculatePriority(Task task) {
        // Priority based on type and age
        int typePriority = getTypePriority(task.getType());
        long ageMinutes = Duration.between(task.getCreatedAt(), Instant.now()).toMinutes();
        return typePriority + (int) ageMinutes;
    }
}
```

## Common Task Patterns

### Batch Processing
```java
public class BatchTaskProcessor {
    public Task createBatchTask(List<Object> items) {
        String batchId = UUID.randomUUID().toString();
        Map<String, Object> batchInput = Map.of(
            "batchId", batchId,
            "items", items,
            "batchSize", items.size()
        );
        
        return new Task("BATCH_PROCESSING", batchInput);
    }
    
    public List<Task> splitBatchTask(Task batchTask) {
        Map<String, Object> input = (Map<String, Object>) batchTask.getInput();
        List<Object> items = (List<Object>) input.get("items");
        
        return items.stream()
            .map(item -> new Task("ITEM_PROCESSING", item))
            .collect(Collectors.toList());
    }
}
```

### Scheduled Tasks
```java
public class ScheduledTaskManager {
    private final ScheduledExecutorService scheduler = 
        Executors.newScheduledThreadPool(5);
    
    public void scheduleTask(Task task, Duration delay) {
        scheduler.schedule(() -> {
            if (task.getStatus() == TaskStatus.PENDING) {
                agent.submitTask(task);
            }
        }, delay.toMillis(), TimeUnit.MILLISECONDS);
    }
    
    public void schedulePeriodicTask(String taskType, Object input, Duration period) {
        scheduler.scheduleAtFixedRate(() -> {
            Task task = new Task(taskType, input);
            agent.submitTask(task);
        }, 0, period.toMillis(), TimeUnit.MILLISECONDS);
    }
}
```

### Task Dependencies
```java
public class DependentTaskExecutor {
    public void executeDependentTasks(Map<Task, Set<Task>> dependencies) {
        Map<Task, CompletableFuture<Object>> futures = new HashMap<>();
        
        // Execute tasks with dependencies
        dependencies.forEach((task, deps) -> {
            if (deps.isEmpty()) {
                // No dependencies, execute immediately
                agent.submitTask(task);
                futures.put(task, task.getFuture());
            } else {
                // Wait for dependencies
                CompletableFuture<?>[] depFutures = deps.stream()
                    .map(Task::getFuture)
                    .toArray(CompletableFuture[]::new);
                
                CompletableFuture<Object> future = 
                    CompletableFuture.allOf(depFutures)
                        .thenCompose(v -> {
                            agent.submitTask(task);
                            return task.getFuture();
                        });
                
                futures.put(task, future);
            }
        });
    }
}
```

## Integration Points

### With Agents
- Tasks are primary work units for agents
- Agent state changes based on task lifecycle
- Task results feed agent metrics

### With Schedulers
- Tasks queued and distributed by schedulers
- Priority and scheduling based on task properties
- Batch and periodic task support

### With Monitoring
- Task status tracked for system health
- Performance metrics derived from task timing
- Failure analysis from task errors

### With UI
- Task progress visualization
- Real-time status updates
- Error display and debugging