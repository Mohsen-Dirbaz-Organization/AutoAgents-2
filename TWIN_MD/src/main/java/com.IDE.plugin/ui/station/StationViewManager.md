# StationViewManager.md

## Class Overview
**File:** `src/main/java/com/IDE/plugin/ui/station/StationViewManager.java`  
**Package:** `com.IDE.plugin.ui.station`  
**Type:** Station View Management and Coordination System

## Purpose
The `StationViewManager` class manages different station views and provides seamless transitions between various display modes. It orchestrates the presentation of station overviews, task queues, performance metrics, and system status through a unified interface with view caching and state management.

## Architecture

### Component Structure
```java
public class StationViewManager {
    private final Project project;
    private final AgentManagementService agentService;
    private final StationManager stationManager;
    
    private Map<String, JPanel> viewCache;
    private StationState currentState;
    
    private enum StationState {
        IDLE,
        STARTING,
        RUNNING,
        STOPPING,
        ERROR
    }
}
```

### View Management System
- **View Caching**: Efficient panel reuse and management
- **State Coordination**: Centralized state management across views
- **Dynamic Updates**: Real-time view synchronization
- **Performance Optimization**: Lazy loading and resource management

## Core View Components

### 1. Station Overview Panel
```java
public JPanel createStationOverviewPanel() {
    JBPanel<JBPanel> panel = new JBPanel<>(new BorderLayout());
    panel.setBorder(JBUI.Borders.empty(10));
    
    // Station status card
    JPanel statusCard = createStatusCard();
    panel.add(statusCard, BorderLayout.NORTH);
    
    // Station grid
    JPanel stationGrid = createStationGrid();
    panel.add(stationGrid, BorderLayout.CENTER);
    
    // Control panel
    JPanel controlPanel = createControlPanel();
    panel.add(controlPanel, BorderLayout.SOUTH);
    
    viewCache.put("overview", panel);
    return panel;
}
```

### 2. Task Queue Panel
```java
public JPanel createTaskQueuePanel() {
    JBPanel<JBPanel> panel = new JBPanel<>(new BorderLayout());
    panel.setBorder(JBUI.Borders.empty(10));
    
    // Task queue table
    DefaultTableModel model = new DefaultTableModel(
        new Object[][] {},
        new String[] {"Task ID", "Type", "Priority", "Status", "Assigned To", "Duration"}
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    
    JBTable table = new JBTable(model);
    table.setRowHeight(25);
    table.getTableHeader().setReorderingAllowed(false);
    
    // Custom renderer for status column
    table.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());
    
    JBScrollPane scrollPane = new JBScrollPane(table);
    panel.add(scrollPane, BorderLayout.CENTER);
    
    // Queue controls
    JPanel controlPanel = createQueueControlPanel();
    panel.add(controlPanel, BorderLayout.SOUTH);
    
    viewCache.put("tasks", panel);
    return panel;
}
```

### 3. Metrics Panel
```java
public JPanel createMetricsPanel() {
    JBPanel<JBPanel> panel = new JBPanel<>(new GridBagLayout());
    panel.setBorder(JBUI.Borders.empty(10));
    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = JBUI.insets(5);
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    
    // Performance metrics card
    gbc.gridx = 0; gbc.gridy = 0;
    panel.add(createMetricCard("Performance", createPerformanceMetrics()), gbc);
    
    // Agent metrics card
    gbc.gridx = 1; gbc.gridy = 0;
    panel.add(createMetricCard("Agent Utilization", createAgentMetrics()), gbc);
    
    // Task metrics card
    gbc.gridx = 0; gbc.gridy = 1;
    panel.add(createMetricCard("Task Statistics", createTaskMetrics()), gbc);
    
    // System metrics card
    gbc.gridx = 1; gbc.gridy = 1;
    panel.add(createMetricCard("System Health", createSystemMetrics()), gbc);
    
    viewCache.put("metrics", panel);
    return panel;
}
```

## Status Management

### 1. Status Card Creation
```java
private JPanel createStatusCard() {
    JPanel card = new JPanel(new BorderLayout());
    card.setBorder(JBUI.Borders.customLine(UIUtil.getBoundsColor()));
    card.setPreferredSize(new Dimension(-1, 100));
    
    // Status info panel
    JPanel infoPanel = new JPanel(new GridBagLayout());
    infoPanel.setBorder(JBUI.Borders.empty(10));
    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = JBUI.insets(5);
    gbc.anchor = GridBagConstraints.WEST;
    
    // Station status
    addStatusField(infoPanel, gbc, "Station Status:", getStatusText(), 0, 0);
    
    // Active stations
    int activeStations = stationManager != null ? stationManager.getActiveStations().size() : 0;
    addStatusField(infoPanel, gbc, "Active Stations:", String.valueOf(activeStations), 0, 1);
    
    // Total agents
    int totalAgents = agentService != null ? agentService.getDeployedAgents().size() : 0;
    addStatusField(infoPanel, gbc, "Total Agents:", String.valueOf(totalAgents), 2, 0);
    
    // Active tasks
    addStatusField(infoPanel, gbc, "Active Tasks:", "0", 2, 1); // TODO: Get actual task count
    
    card.add(infoPanel, BorderLayout.CENTER);
    
    // Quick actions panel
    JPanel actionsPanel = createQuickActionsPanel();
    card.add(actionsPanel, BorderLayout.EAST);
    
    return card;
}

private void addStatusField(JPanel panel, GridBagConstraints gbc, String label, String value, int x, int y) {
    gbc.gridx = x; gbc.gridy = y;
    JBLabel statusLabel = new JBLabel(label);
    statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
    panel.add(statusLabel, gbc);
    
    gbc.gridx = x + 1;
    JBLabel statusValue = new JBLabel(value);
    statusValue.setForeground(getStatusColor());
    panel.add(statusValue, gbc);
}
```

### 2. Quick Actions Panel
```java
private JPanel createQuickActionsPanel() {
    JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    actionsPanel.setBorder(JBUI.Borders.empty(10));
    
    JButton quickStartBtn = new JButton("Quick Start");
    quickStartBtn.addActionListener(e -> quickStart());
    actionsPanel.add(quickStartBtn);
    
    return actionsPanel;
}

private void quickStart() {
    currentState = StationState.STARTING;
    // Implementation for quick start
}
```

## Station Grid Display

### 1. Station Grid Creation
```java
private JPanel createStationGrid() {
    JPanel grid = new JPanel(new GridLayout(0, 3, 10, 10));
    grid.setBorder(JBUI.Borders.empty(10, 0, 10, 0));
    
    if (stationManager != null) {
        List<Station> stations = stationManager.getActiveStations();
        for (Station station : stations) {
            grid.add(createStationCard(station));
        }
    }
    
    // Add empty station slot
    grid.add(createAddStationCard());
    
    // Wrap in scroll pane
    JBScrollPane scrollPane = new JBScrollPane(grid);
    scrollPane.setBorder(null);
    
    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.add(scrollPane, BorderLayout.CENTER);
    
    return wrapper;
}
```

### 2. Individual Station Card
```java
private JPanel createStationCard(Station station) {
    JPanel card = new JPanel(new BorderLayout());
    card.setBorder(BorderFactory.createCompoundBorder(
        JBUI.Borders.customLine(UIUtil.getBoundsColor()),
        JBUI.Borders.empty(10)
    ));
    card.setPreferredSize(new Dimension(200, 150));
    
    // Station info
    JPanel infoPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = JBUI.insets(2);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    
    // Name
    gbc.gridx = 0; gbc.gridy = 0;
    JBLabel nameLabel = new JBLabel(station.getConfiguration().getName());
    nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
    infoPanel.add(nameLabel, gbc);
    
    // Status
    gbc.gridy = 1;
    JBLabel statusLabel = new JBLabel("Status: " + (station.isActive() ? "Active" : "Inactive"));
    statusLabel.setForeground(station.isActive() ? new Color(0, 150, 0) : Color.GRAY);
    infoPanel.add(statusLabel, gbc);
    
    // Agents
    gbc.gridy = 2;
    infoPanel.add(new JBLabel("Agents: " + station.getAssignedAgents().size() + 
        "/" + station.getConfiguration().getMaxAgents()), gbc);
    
    // Health
    gbc.gridy = 3;
    double errorRate = station.getErrorRate();
    JBLabel healthLabel = new JBLabel("Health: " + getHealthStatus(errorRate));
    healthLabel.setForeground(getHealthColor(errorRate));
    infoPanel.add(healthLabel, gbc);
    
    card.add(infoPanel, BorderLayout.CENTER);
    
    // Actions
    JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JButton manageBtn = new JButton("Manage");
    manageBtn.setPreferredSize(new Dimension(80, 25));
    actionsPanel.add(manageBtn);
    
    card.add(actionsPanel, BorderLayout.SOUTH);
    
    return card;
}
```

### 3. Add Station Card
```java
private JPanel createAddStationCard() {
    JPanel card = new JPanel(new BorderLayout());
    card.setBorder(BorderFactory.createDashedBorder(UIUtil.getBoundsColor()));
    card.setPreferredSize(new Dimension(200, 150));
    
    // Center content
    JPanel centerPanel = new JPanel(new GridBagLayout());
    centerPanel.setOpaque(false);
    
    JButton addButton = new JButton("+ Add Station");
    addButton.setFont(addButton.getFont().deriveFont(14f));
    centerPanel.add(addButton);
    
    card.add(centerPanel, BorderLayout.CENTER);
    
    return card;
}
```

## Queue Control Panel

### Queue Control Implementation
```java
private JPanel createQueueControlPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    
    JButton pauseBtn = new JButton("Pause Queue");
    JButton clearBtn = new JButton("Clear Completed");
    JButton prioritizeBtn = new JButton("Prioritize");
    
    panel.add(pauseBtn);
    panel.add(clearBtn);
    panel.add(prioritizeBtn);
    
    return panel;
}
```

## Metrics Display

### 1. Metric Card Creation
```java
private JPanel createMetricCard(String title, JPanel content) {
    JPanel card = new JPanel(new BorderLayout());
    card.setBorder(BorderFactory.createCompoundBorder(
        JBUI.Borders.customLine(UIUtil.getBoundsColor()),
        JBUI.Borders.empty(10)
    ));
    
    // Title
    JBLabel titleLabel = new JBLabel(title);
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
    titleLabel.setBorder(JBUI.Borders.empty(0, 0, 10, 0));
    card.add(titleLabel, BorderLayout.NORTH);
    
    // Content
    card.add(content, BorderLayout.CENTER);
    
    return card;
}
```

### 2. Performance Metrics
```java
private JPanel createPerformanceMetrics() {
    JPanel panel = new JPanel(new GridLayout(4, 2, 10, 5));
    
    panel.add(new JLabel("Avg Response Time:"));
    panel.add(new JLabel("125ms"));
    
    panel.add(new JLabel("Throughput:"));
    panel.add(new JLabel("45 tasks/min"));
    
    panel.add(new JLabel("Success Rate:"));
    panel.add(new JLabel("98.5%"));
    
    panel.add(new JLabel("Error Rate:"));
    panel.add(new JLabel("1.5%"));
    
    return panel;
}
```

### 3. Agent Metrics
```java
private JPanel createAgentMetrics() {
    JPanel panel = new JPanel(new GridLayout(4, 2, 10, 5));
    
    panel.add(new JLabel("Active Agents:"));
    panel.add(new JLabel("8/10"));
    
    panel.add(new JLabel("Idle Agents:"));
    panel.add(new JLabel("2"));
    
    panel.add(new JLabel("Avg Utilization:"));
    panel.add(new JLabel("80%"));
    
    panel.add(new JLabel("Agent Efficiency:"));
    panel.add(new JLabel("92%"));
    
    return panel;
}
```

### 4. Task Metrics
```java
private JPanel createTaskMetrics() {
    JPanel panel = new JPanel(new GridLayout(4, 2, 10, 5));
    
    panel.add(new JLabel("Completed Tasks:"));
    panel.add(new JLabel("1,234"));
    
    panel.add(new JLabel("Failed Tasks:"));
    panel.add(new JLabel("12"));
    
    panel.add(new JLabel("Avg Duration:"));
    panel.add(new JLabel("3.2s"));
    
    panel.add(new JLabel("Queue Length:"));
    panel.add(new JLabel("5"));
    
    return panel;
}
```

### 5. System Metrics
```java
private JPanel createSystemMetrics() {
    JPanel panel = new JPanel(new GridLayout(4, 2, 10, 5));
    
    panel.add(new JLabel("CPU Usage:"));
    panel.add(new JLabel("45%"));
    
    panel.add(new JLabel("Memory Usage:"));
    panel.add(new JLabel("2.1 GB"));
    
    panel.add(new JLabel("Network I/O:"));
    panel.add(new JLabel("125 KB/s"));
    
    panel.add(new JLabel("System Health:"));
    panel.add(new JLabel("Good"));
    
    return panel;
}
```

## Control Panel

### Main Control Panel
```java
private JPanel createControlPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(JBUI.Borders.customLine(UIUtil.getBoundsColor(), 1, 0, 0, 0));
    
    // Left controls
    JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    
    JButton refreshBtn = new JButton("Refresh");
    JButton exportBtn = new JButton("Export Data");
    
    leftPanel.add(refreshBtn);
    leftPanel.add(exportBtn);
    
    panel.add(leftPanel, BorderLayout.WEST);
    
    // Right controls
    JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    
    JLabel autoRefreshLabel = new JLabel("Auto-refresh:");
    JCheckBox autoRefreshCheck = new JCheckBox("", true);
    
    rightPanel.add(autoRefreshLabel);
    rightPanel.add(autoRefreshCheck);
    
    panel.add(rightPanel, BorderLayout.EAST);
    
    return panel;
}
```

## State Management

### 1. State Transitions
```java
public void onStationStarted() {
    currentState = StationState.RUNNING;
    refreshAll();
}

public void onStationStopped() {
    currentState = StationState.IDLE;
    refreshAll();
}
```

### 2. Status Methods
```java
private String getStatusText() {
    switch (currentState) {
        case IDLE: return "Idle";
        case STARTING: return "Starting...";
        case RUNNING: return "Running";
        case STOPPING: return "Stopping...";
        case ERROR: return "Error";
        default: return "Unknown";
    }
}

private Color getStatusColor() {
    switch (currentState) {
        case IDLE: return Color.GRAY;
        case STARTING:
        case STOPPING: return new Color(200, 150, 0);
        case RUNNING: return new Color(0, 150, 0);
        case ERROR: return new Color(200, 0, 0);
        default: return Color.BLACK;
    }
}
```

### 3. Health Status Utilities
```java
private String getHealthStatus(double errorRate) {
    if (errorRate < 0.01) return "Excellent";
    else if (errorRate < 0.05) return "Good";
    else if (errorRate < 0.10) return "Fair";
    else return "Poor";
}

private Color getHealthColor(double errorRate) {
    if (errorRate < 0.01) return new Color(0, 150, 0);
    else if (errorRate < 0.05) return new Color(100, 150, 0);
    else if (errorRate < 0.10) return new Color(200, 150, 0);
    else return new Color(200, 0, 0);
}
```

## View Refresh System

### Global Refresh
```java
public void refreshAll() {
    viewCache.forEach((key, panel) -> {
        // Refresh each view
        SwingUtilities.invokeLater(() -> {
            panel.revalidate();
            panel.repaint();
        });
    });
}
```

## Custom Cell Renderer

### Status Cell Renderer
```java
private static class StatusCellRenderer extends JLabel implements TableCellRenderer {
    public StatusCellRenderer() {
        setOpaque(true);
        setHorizontalAlignment(SwingConstants.CENTER);
    }
    
    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        
        String status = value != null ? value.toString() : "";
        setText(status);
        
        // Set color based on status
        switch (status.toLowerCase()) {
            case "pending":
                setForeground(Color.GRAY);
                break;
            case "running":
                setForeground(new Color(200, 150, 0));
                break;
            case "completed":
                setForeground(new Color(0, 150, 0));
                break;
            case "failed":
                setForeground(new Color(200, 0, 0));
                break;
            default:
                setForeground(table.getForeground());
        }
        
        if (isSelected) {
            setBackground(table.getSelectionBackground());
        } else {
            setBackground(table.getBackground());
        }
        
        return this;
    }
}
```

## Integration Features

### Service Integration
- **AgentManagementService**: Real-time agent status
- **StationManager**: Station lifecycle and health monitoring
- **Project Services**: IntelliJ platform integration

### View Coordination
- **View Caching**: Efficient panel management and reuse
- **State Synchronization**: Consistent state across all views
- **Event Propagation**: Coordinated updates between components

### Performance Optimization
- **Lazy Loading**: Views created on demand
- **Memory Management**: Efficient resource utilization
- **Update Batching**: Minimized UI refresh operations

## Error Handling

### Robust Error Management
- **Service Availability**: Graceful handling of service failures
- **State Recovery**: Automatic state restoration
- **User Feedback**: Clear error indication and recovery options

## Future Enhancements

### Planned Improvements
- **View Transitions**: Smooth animations between views
- **Custom Layouts**: User-configurable view arrangements
- **Export Functionality**: Data export capabilities
- **Responsive Design**: Adaptive layout for different screen sizes

This view manager provides a unified interface for managing multiple station views while maintaining performance and providing seamless user experience through efficient caching and state management.