# AgentStatusBar.md

## Class Overview
**File:** `src/main/java/com/IDE/plugin/ui/AgentStatusBar.java`  
**Package:** `com.IDE.plugin.ui`  
**Type:** IntelliJ IDEA Status Bar Widget

## Purpose
The `AgentStatusBar` class provides a real-time status indicator for agent activities in the IDE's status bar. It displays visual feedback about agent states, station status, and provides quick access to management functions through contextual menus.

## Architecture

### Interface Implementation
```java
public class AgentStatusBar implements StatusBarWidget, StatusBarWidget.IconPresentation
```

### Core Components
- **Visual Status Indicator**: Color-coded icon display
- **Tooltip Information**: Detailed status summaries
- **Contextual Menu**: Quick action access
- **Real-time Updates**: Periodic status refresh

## Status States

### Status Enumeration
```java
private enum Status {
    IDLE(createIcon(Color.GRAY), "AutoAgents: Idle"),
    ACTIVE(createIcon(new Color(0, 150, 0)), "AutoAgents: Active"),
    BUSY(createIcon(new Color(200, 150, 0)), "AutoAgents: Busy"),
    ERROR(createIcon(new Color(200, 0, 0)), "AutoAgents: Error");
}
```

### Status Determination Logic
| Condition | Status | Color | Description |
|-----------|--------|-------|-------------|
| No active stations/agents | IDLE | Gray | System inactive |
| Normal operations | ACTIVE | Green | System running normally |
| High utilization (>80%) | BUSY | Orange | High system load |
| Errors detected | ERROR | Red | System errors present |

## Key Features

### 1. Dynamic Icon Generation
```java
private static Icon createIcon(Color color) {
    return new Icon() {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                               RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw circle with border
            g2.setColor(color);
            g2.fillOval(x + 2, y + 2, 12, 12);
            g2.setColor(color.darker());
            g2.drawOval(x + 2, y + 2, 12, 12);
            
            g2.dispose();
        }
    };
}
```

### 2. Contextual Popup Menu
- **Status Summary**: Current system state
- **Quick Actions**: Common operations
- **Station Management**: Active station list
- **Agent Management**: Deployed agent list

### 3. Auto-refresh Mechanism
```java
private void startUpdateTimer() {
    updateTimer = new Timer(5000, e -> {
        SwingUtilities.invokeLater(() -> {
            updateStatus();
            // Trigger status bar update
            if (project != null && !project.isDisposed()) {
                StatusBar statusBar = project.getService(StatusBar.class);
                if (statusBar != null) {
                    statusBar.updateWidget(ID);
                }
            }
        });
    });
    updateTimer.start();
}
```

## Status Bar Integration

### Widget Registration
```java
private static final String ID = "AutoAgents.StatusBar";

@Override
@NotNull
public String ID() {
    return ID;
}
```

### Presentation Interface
```java
@Override
@Nullable
public WidgetPresentation getPresentation(@NotNull PlatformType type) {
    return this;
}
```

## Status Calculation

### Status Assessment Algorithm
```java
private Status calculateStatus() {
    if (agentService == null || stationManager == null) {
        return Status.ERROR;
    }
    
    int activeStations = stationManager.getActiveStations().size();
    int deployedAgents = agentService.getDeployedAgents().size();
    
    if (activeStations == 0 || deployedAgents == 0) {
        return Status.IDLE;
    }
    
    // Check for errors
    boolean hasErrors = stationManager.getActiveStations().stream()
        .anyMatch(station -> station.getErrorRate() > 0.1);
    
    if (hasErrors) {
        return Status.ERROR;
    }
    
    // Check utilization
    double avgUtilization = calculateAverageUtilization();
    if (avgUtilization > 0.8) {
        return Status.BUSY;
    }
    
    return Status.ACTIVE;
}
```

### Detailed Status Information
```java
private String getDetailedStatus() {
    if (agentService == null || stationManager == null) {
        return "Services not available";
    }
    
    int activeStations = stationManager.getActiveStations().size();
    int deployedAgents = agentService.getDeployedAgents().size();
    
    return String.format("Stations: %d | Agents: %d", activeStations, deployedAgents);
}
```

## Popup Menu System

### Menu Structure
```java
private void showPopupMenu(Component component, int x, int y) {
    JPopupMenu popup = new JPopupMenu();
    
    // Status summary (disabled item)
    JMenuItem statusItem = new JMenuItem(getDetailedStatus());
    statusItem.setEnabled(false);
    popup.add(statusItem);
    popup.addSeparator();
    
    // Quick actions
    popup.add(createMenuItem("Open AutoAgents", this::openToolWindow));
    popup.add(createMenuItem("Start New Station", this::startNewStation));
    popup.add(createMenuItem("Deploy New Agent", this::deployNewAgent));
    
    popup.addSeparator();
    
    // Dynamic station submenu
    JMenu stationsMenu = createStationsMenu();
    popup.add(stationsMenu);
    
    // Dynamic agent submenu
    JMenu agentsMenu = createAgentsMenu();
    popup.add(agentsMenu);
    
    popup.show(component, x, y);
}
```

### Dynamic Submenus
#### Station Submenu
```java
JMenu stationsMenu = new JMenu("Active Stations");
if (stationManager != null) {
    stationManager.getActiveStations().forEach(station -> {
        JMenuItem stationItem = new JMenuItem(station.getConfiguration().getName());
        stationItem.addActionListener(e -> showStationDetails(station.getId()));
        stationsMenu.add(stationItem);
    });
}
```

#### Agent Submenu
```java
JMenu agentsMenu = new JMenu("Deployed Agents");
if (agentService != null) {
    agentService.getDeployedAgents().forEach(agentId -> {
        JMenuItem agentItem = new JMenuItem(agentId);
        agentItem.addActionListener(e -> showAgentDetails(agentId));
        agentsMenu.add(agentItem);
    });
}
```

## Event Handling

### Click Handler
```java
this.clickConsumer = event -> {
    if (event.getButton() == MouseEvent.BUTTON1) {
        showPopupMenu(event.getComponent(), event.getX(), event.getY());
    }
};
```

### Action Implementations
```java
private void openToolWindow() {
    // Implementation to open the AutoAgents tool window
}

private void startNewStation() {
    // Implementation to start a new station
}

private void deployNewAgent() {
    // Implementation to deploy a new agent
}

private void showStationDetails(String stationId) {
    // Implementation to show station details
}

private void showAgentDetails(String agentId) {
    // Implementation to show agent details
}
```

## Service Dependencies

### Required Services
- **AgentManagementService**: Agent deployment and status
- **StationManager**: Station lifecycle and monitoring
- **StatusBar**: IntelliJ platform status bar service

### Service Injection
```java
public AgentStatusBar(@NotNull Project project) {
    this.project = project;
    this.agentService = project.getService(AgentManagementService.class);
    this.stationManager = project.getService(StationManager.class);
    
    updateStatus();
    setupEventHandlers();
    startUpdateTimer();
}
```

## Lifecycle Management

### Widget Installation
```java
@Override
public void install(@NotNull StatusBar statusBar) {
    // Installation handled by status bar
}
```

### Resource Cleanup
```java
@Override
public void dispose() {
    if (updateTimer != null) {
        updateTimer.stop();
        updateTimer = null;
    }
}
```

## Visual Design

### Icon Specifications
- **Size**: 16x16 pixels
- **Shape**: Circular indicator
- **Border**: Darker border for definition
- **Anti-aliasing**: Smooth rendering

### Color Scheme
| Status | Primary Color | RGB Values | Usage |
|--------|---------------|------------|-------|
| IDLE | Gray | (128, 128, 128) | Inactive state |
| ACTIVE | Green | (0, 150, 0) | Normal operation |
| BUSY | Orange | (200, 150, 0) | High load |
| ERROR | Red | (200, 0, 0) | Error condition |

## Performance Considerations

### Update Frequency
- **Timer Interval**: 5 seconds
- **Efficient Polling**: Minimal resource usage
- **UI Thread Safety**: SwingUtilities.invokeLater usage

### Resource Management
- **Icon Caching**: Reuse icon instances
- **Service Availability**: Null checks for robustness
- **Memory Cleanup**: Proper timer disposal

## Integration Points

### IntelliJ Platform Integration
- **Status Bar Framework**: Standard widget implementation
- **Project Services**: Service locator pattern
- **UI Threading**: EDT compliance

### Plugin Component Integration
- **Agent Management**: Real-time agent status
- **Station Management**: Station health monitoring
- **Tool Window**: Quick access to main UI

## Error Handling

### Service Unavailability
```java
private Status calculateStatus() {
    if (agentService == null || stationManager == null) {
        return Status.ERROR;
    }
    // ... continue with normal logic
}
```

### Null Safety
- Service existence validation
- Graceful degradation
- Default status fallbacks

## Accessibility Features

### Tooltip Support
```java
@Override
@NotNull
public String getTooltipText() {
    return currentTooltip + "\n" + getDetailedStatus();
}
```

### Keyboard Navigation
- Standard popup menu navigation
- Accessible menu items
- Focus management

## Testing Considerations

### Unit Testing
- Status calculation logic
- Icon generation
- Service interaction

### Integration Testing
- Status bar registration
- Menu functionality
- Update mechanisms

## Configuration Options

### Customizable Aspects
- Update frequency
- Status thresholds
- Menu content
- Visual appearance

### Extension Points
- Custom status providers
- Additional menu items
- Alternative visualizations

## Future Enhancements

### Planned Improvements
- **Rich Tooltips**: HTML-formatted status information
- **Notification Integration**: System notification triggers
- **Keyboard Shortcuts**: Quick action hotkeys
- **Theme Integration**: Adaptive color schemes

### Advanced Features
- **Historical Data**: Status trend visualization
- **Performance Metrics**: Real-time performance indicators
- **Custom Alerts**: User-defined alert conditions
- **Dashboard Preview**: Mini-dashboard in tooltip

This status bar widget provides essential real-time feedback about the AutoAgents system state, enabling users to quickly assess system health and access common management functions without leaving their current workflow.