# StationManagementPanel.md

## Class Overview
**File:** `src/main/java/com/IDE/plugin/ui/station/StationManagementPanel.java`  
**Package:** `com.IDE.plugin.ui.station`  
**Type:** Main Station Management Interface Panel

## Purpose
The `StationManagementPanel` class serves as the central interface for comprehensive station management within the AutoAgents system. It provides a unified view for station lifecycle management, configuration, monitoring, and control through an integrated tabbed interface.

## Architecture

### Component Structure
```java
public class StationManagementPanel extends JPanel {
    private final Project project;
    private final StationManager stationManager;
    private final JBList<Station> stationList;
    private final CollectionListModel<Station> stationListModel;
    private final JPanel detailsPanel;
    private final JBTabbedPane tabbedPane;
    private final Map<String, JComponent> stationDetailPanels;
    private final Timer refreshTimer;
    
    // Sub-panels
    private AgentDeploymentPanel agentDeploymentPanel;
    private StationMonitoringPanel stationMonitoringPanel;
    private StationControlPanel stationControlPanel;
}
```

### Layout Architecture
```java
private void initializeUI() {
    setLayout(new BorderLayout());
    setBorder(JBUI.Borders.empty(10));
    
    // Create split pane
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setDividerLocation(300);
    splitPane.setResizeWeight(0.3);
    
    // Left panel - Station list
    JPanel leftPanel = createStationListPanel();
    splitPane.setLeftComponent(leftPanel);
    
    // Right panel - Station details and controls
    JPanel rightPanel = createStationDetailsPanel();
    splitPane.setRightComponent(rightPanel);
    
    add(splitPane, BorderLayout.CENTER);
    
    // Bottom toolbar
    add(createBottomToolbar(), BorderLayout.SOUTH);
}
```

## Station List Management

### 1. Station List Panel
```java
private JPanel createStationListPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(JBUI.Borders.empty(5));
    
    // Title
    JLabel titleLabel = new JLabel("Stations");
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
    panel.add(titleLabel, BorderLayout.NORTH);
    
    // Station list with toolbar
    stationList.setCellRenderer(new StationListCellRenderer());
    stationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    ToolbarDecorator decorator = ToolbarDecorator.createDecorator(stationList)
            .setAddAction(button -> addNewStation())
            .setRemoveAction(button -> removeSelectedStation())
            .setEditAction(button -> editSelectedStation())
            .disableUpDownActions();
    
    panel.add(decorator.createPanel(), BorderLayout.CENTER);
    
    // Filter panel
    panel.add(createFilterPanel(), BorderLayout.SOUTH);
    
    return panel;
}
```

### 2. Custom Station List Renderer
```java
private static class StationListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        if (value instanceof Station) {
            Station station = (Station) value;
            setText(station.getName());
            setIcon(getStationIcon(station.getStatus()));
        }
        
        return this;
    }
    
    private Icon getStationIcon(StationStatus status) {
        // Return appropriate icon based on status
        return UIManager.getIcon("Tree.collapsedIcon"); // Placeholder
    }
}
```

### 3. Station Filtering
```java
private JPanel createFilterPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.setBorder(BorderFactory.createTitledBorder("Filter"));
    
    // Status filter
    JComboBox<String> statusFilter = new JComboBox<>(new String[]{
        "All", "Running", "Stopped", "Error", "Maintenance"
    });
    statusFilter.addActionListener(e -> filterStations((String) statusFilter.getSelectedItem()));
    panel.add(new JLabel("Status:"));
    panel.add(statusFilter);
    
    // Search field
    JTextField searchField = new JTextField(15);
    searchField.putClientProperty("JTextField.placeholderText", "Search stations...");
    panel.add(searchField);
    
    return panel;
}

private void filterStations(String status) {
    if ("All".equals(status)) {
        loadStations();
    } else {
        List<Station> filtered = new ArrayList<>();
        for (Station station : stationManager.getAllStations()) {
            if (station.getStatus().toString().equalsIgnoreCase(status)) {
                filtered.add(station);
            }
        }
        stationListModel.removeAll();
        stationListModel.add(filtered);
    }
}
```

## Station Details Interface

### 1. Station Header Panel
```java
private JPanel createStationHeaderPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(JBUI.Borders.empty(10));
    panel.setBackground(UIUtil.getPanelBackground());
    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = JBUI.insets(5);
    
    // Station name
    stationNameLabel = new JLabel("No Station Selected");
    stationNameLabel.setFont(stationNameLabel.getFont().deriveFont(Font.BOLD, 16f));
    addComponent(panel, gbc, stationNameLabel, 0, 0, 2);
    
    // Station details
    addLabelValuePair(panel, gbc, "Type:", stationTypeLabel = new JLabel("-"), 0, 1);
    addLabelValuePair(panel, gbc, "Status:", stationStatusLabel = new JLabel("-"), 0, 2);
    addLabelValuePair(panel, gbc, "Active Agents:", agentCountLabel = new JLabel("0"), 0, 3);
    addLabelValuePair(panel, gbc, "Running Tasks:", taskCountLabel = new JLabel("0"), 0, 4);
    
    // Resource usage
    addComponent(panel, gbc, new JLabel("Resource Usage:"), 0, 5);
    resourceUsageBar = new JProgressBar(0, 100);
    resourceUsageBar.setStringPainted(true);
    addComponent(panel, gbc, resourceUsageBar, 1, 5);
    
    return panel;
}
```

### 2. Tabbed Detail Views
```java
private JPanel createStationDetailsPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(JBUI.Borders.empty(5));
    
    // Station info header
    JPanel headerPanel = createStationHeaderPanel();
    panel.add(headerPanel, BorderLayout.NORTH);
    
    // Tabbed pane for different views
    tabbedPane.addTab("Overview", createOverviewPanel());
    tabbedPane.addTab("Agent Deployment", createAgentDeploymentTab());
    tabbedPane.addTab("Monitoring", createMonitoringTab());
    tabbedPane.addTab("Control", createControlTab());
    tabbedPane.addTab("Configuration", createConfigurationPanel());
    
    panel.add(tabbedPane, BorderLayout.CENTER);
    
    return panel;
}
```

## Overview Tab

### 1. Overview Panel Structure
```java
private JPanel createOverviewPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(JBUI.Borders.empty(10));
    
    // Station description
    JPanel descPanel = new JPanel(new BorderLayout());
    descPanel.setBorder(BorderFactory.createTitledBorder("Description"));
    stationDescriptionArea = new JTextArea(5, 30);
    stationDescriptionArea.setLineWrap(true);
    stationDescriptionArea.setWrapStyleWord(true);
    stationDescriptionArea.setEditable(false);
    descPanel.add(new JBScrollPane(stationDescriptionArea), BorderLayout.CENTER);
    
    panel.add(descPanel, BorderLayout.NORTH);
    
    // Statistics panel
    statsPanel = createStatisticsPanel();
    panel.add(statsPanel, BorderLayout.CENTER);
    
    // Quick actions
    JPanel actionsPanel = createQuickActionsPanel();
    panel.add(actionsPanel, BorderLayout.SOUTH);
    
    return panel;
}
```

### 2. Statistics Display
```java
private JPanel createStatisticsPanel() {
    JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
    panel.setBorder(BorderFactory.createTitledBorder("Statistics"));
    
    // Add various statistics displays
    panel.add(createStatCard("Total Tasks Executed", "0"));
    panel.add(createStatCard("Success Rate", "0%"));
    panel.add(createStatCard("Average Response Time", "0ms"));
    panel.add(createStatCard("Uptime", "0h 0m"));
    
    return panel;
}

private JPanel createStatCard(String title, String value) {
    JPanel card = new JPanel(new BorderLayout());
    card.setBorder(BorderFactory.createLineBorder(UIUtil.getBorderColor()));
    card.setBackground(UIUtil.getPanelBackground());
    
    JLabel titleLabel = new JLabel(title);
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 12f));
    titleLabel.setBorder(JBUI.Borders.empty(5));
    card.add(titleLabel, BorderLayout.NORTH);
    
    JLabel valueLabel = new JLabel(value);
    valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 18f));
    valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
    card.add(valueLabel, BorderLayout.CENTER);
    
    return card;
}
```

### 3. Quick Actions Panel
```java
private JPanel createQuickActionsPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.setBorder(BorderFactory.createTitledBorder("Quick Actions"));
    
    JButton startButton = new JButton("Start Station");
    startButton.addActionListener(e -> startStation());
    panel.add(startButton);
    
    JButton stopButton = new JButton("Stop Station");
    stopButton.addActionListener(e -> stopStation());
    panel.add(stopButton);
    
    JButton restartButton = new JButton("Restart Station");
    restartButton.addActionListener(e -> restartStation());
    panel.add(restartButton);
    
    JButton deployAgentButton = new JButton("Deploy Agent");
    deployAgentButton.addActionListener(e -> {
        tabbedPane.setSelectedIndex(1); // Switch to Agent Deployment tab
    });
    panel.add(deployAgentButton);
    
    return panel;
}
```

## Sub-Panel Integration

### 1. Agent Deployment Integration
```java
private JPanel createAgentDeploymentTab() {
    agentDeploymentPanel = new AgentDeploymentPanel(project);
    return agentDeploymentPanel;
}
```

### 2. Monitoring Integration
```java
private JPanel createMonitoringTab() {
    stationMonitoringPanel = new StationMonitoringPanel(project);
    return stationMonitoringPanel;
}
```

### 3. Control Integration
```java
private JPanel createControlTab() {
    stationControlPanel = new StationControlPanel(project);
    return stationControlPanel;
}
```

## Configuration Management

### 1. Configuration Panel
```java
private JPanel createConfigurationPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(JBUI.Borders.empty(10));
    
    // Configuration form
    JPanel formPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = JBUI.insets(5);
    
    // Add configuration fields
    addConfigField(formPanel, gbc, "Station Name:", new JTextField(20), 0);
    addConfigField(formPanel, gbc, "Description:", new JTextArea(3, 20), 1);
    addConfigField(formPanel, gbc, "Max Agents:", new JSpinner(new SpinnerNumberModel(10, 1, 100, 1)), 2);
    addConfigField(formPanel, gbc, "Max Tasks:", new JSpinner(new SpinnerNumberModel(50, 1, 500, 1)), 3);
    addConfigField(formPanel, gbc, "Resource Limit (%):", new JSlider(0, 100, 80), 4);
    
    panel.add(formPanel, BorderLayout.NORTH);
    
    // Save button
    JButton saveButton = new JButton("Save Configuration");
    saveButton.addActionListener(e -> saveConfiguration());
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(saveButton);
    panel.add(buttonPanel, BorderLayout.SOUTH);
    
    return panel;
}

private void addConfigField(JPanel panel, GridBagConstraints gbc, String label, JComponent field, int row) {
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.3;
    panel.add(new JLabel(label), gbc);
    
    gbc.gridx = 1;
    gbc.weightx = 0.7;
    if (field instanceof JTextArea) {
        panel.add(new JBScrollPane(field), gbc);
    } else {
        panel.add(field, gbc);
    }
}
```

## Station Lifecycle Management

### 1. Station Creation
```java
private void addNewStation() {
    StationCreationDialog dialog = new StationCreationDialog(project);
    if (dialog.showAndGet()) {
        Station newStation = dialog.createStation();
        stationManager.registerStation(newStation);
        loadStations();
    }
}
```

### 2. Station Removal
```java
private void removeSelectedStation() {
    Station selected = stationList.getSelectedValue();
    if (selected != null) {
        int result = Messages.showYesNoDialog(
            project,
            "Are you sure you want to remove station '" + selected.getName() + "'?",
            "Confirm Removal",
            Messages.getWarningIcon()
        );
        
        if (result == Messages.YES) {
            stationManager.unregisterStation(selected.getId());
            loadStations();
        }
    }
}
```

### 3. Station Editing
```java
private void editSelectedStation() {
    Station selected = stationList.getSelectedValue();
    if (selected != null) {
        // Open edit dialog
        StationEditDialog dialog = new StationEditDialog(project, selected);
        if (dialog.showAndGet()) {
            dialog.updateStation();
            loadStations();
        }
    }
}
```

## Status Updates and Monitoring

### 1. Real-time Status Updates
```java
private void setupListeners() {
    stationList.addListSelectionListener(new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                Station selected = stationList.getSelectedValue();
                if (selected != null) {
                    updateStationDetails(selected);
                    updateSubPanels(selected);
                }
            }
        }
    });
}
```

### 2. Detail Updates
```java
private void updateStationDetails(Station station) {
    stationNameLabel.setText(station.getName());
    stationTypeLabel.setText(station.getType());
    stationStatusLabel.setText(station.getStatus().toString());
    stationStatusLabel.setForeground(getStatusColor(station.getStatus()));
    agentCountLabel.setText(String.valueOf(station.getActiveAgentCount()));
    taskCountLabel.setText(String.valueOf(station.getRunningTaskCount()));
    stationDescriptionArea.setText(station.getDescription());
    
    int resourceUsage = station.getResourceUsage();
    resourceUsageBar.setValue(resourceUsage);
    resourceUsageBar.setString(resourceUsage + "%");
    
    updateStatistics(station);
}

private Color getStatusColor(StationStatus status) {
    switch (status) {
        case RUNNING:
            return new Color(0, 128, 0);
        case STOPPED:
            return Color.GRAY;
        case ERROR:
            return Color.RED;
        case MAINTENANCE:
            return Color.ORANGE;
        default:
            return UIUtil.getLabelForeground();
    }
}
```

### 3. Sub-Panel Synchronization
```java
private void updateSubPanels(Station station) {
    if (agentDeploymentPanel != null) {
        agentDeploymentPanel.setStation(station);
    }
    if (stationMonitoringPanel != null) {
        stationMonitoringPanel.setStation(station);
    }
    if (stationControlPanel != null) {
        stationControlPanel.setStation(station);
    }
}
```

## Station Control Actions

### 1. Station Operations
```java
private void startStation() {
    Station selected = stationList.getSelectedValue();
    if (selected != null) {
        stationManager.startStation(selected.getId());
        updateStationDetails(selected);
    }
}

private void stopStation() {
    Station selected = stationList.getSelectedValue();
    if (selected != null) {
        stationManager.stopStation(selected.getId());
        updateStationDetails(selected);
    }
}

private void restartStation() {
    Station selected = stationList.getSelectedValue();
    if (selected != null) {
        stationManager.restartStation(selected.getId());
        updateStationDetails(selected);
    }
}
```

### 2. Configuration Persistence
```java
private void saveConfiguration() {
    Station selected = stationList.getSelectedValue();
    if (selected != null) {
        // Save configuration changes
        Messages.showInfoMessage(project, "Configuration saved successfully", "Success");
    }
}
```

## Data Management

### 1. Station Loading
```java
private void loadStations() {
    List<Station> stations = stationManager.getAllStations();
    stationListModel.removeAll();
    stationListModel.add(stations);
    
    if (!stations.isEmpty()) {
        stationList.setSelectedIndex(0);
    }
}
```

### 2. Automatic Refresh
```java
private void refreshStationData() {
    SwingUtilities.invokeLater(() -> {
        loadStations();
        Station selected = stationList.getSelectedValue();
        if (selected != null) {
            updateStationDetails(selected);
            updateSubPanels(selected);
        }
    });
}
```

## Bottom Toolbar

### 1. Toolbar Creation
```java
private JPanel createBottomToolbar() {
    JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    toolbar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtil.getBorderColor()));
    
    JButton refreshButton = new JButton("Refresh");
    refreshButton.addActionListener(e -> refreshStationData());
    toolbar.add(refreshButton);
    
    JButton settingsButton = new JButton("Settings");
    settingsButton.addActionListener(e -> showSettings());
    toolbar.add(settingsButton);
    
    return toolbar;
}
```

### 2. Settings Dialog
```java
private void showSettings() {
    // Show global settings dialog
    DialogBuilder builder = new DialogBuilder(project);
    builder.setTitle("Station Management Settings");
    builder.setCenterPanel(new StationSettingsPanel());
    builder.show();
}
```

## Timer Management

### 1. Refresh Timer Setup
```java
public StationManagementPanel(Project project) {
    this.project = project;
    this.stationManager = StationManager.getInstance(project);
    this.stationListModel = new CollectionListModel<>();
    this.stationList = new JBList<>(stationListModel);
    this.detailsPanel = new JPanel(new BorderLayout());
    this.tabbedPane = new JBTabbedPane();
    this.stationDetailPanels = new ConcurrentHashMap<>();
    
    initializeUI();
    setupListeners();
    loadStations();
    
    // Setup refresh timer
    this.refreshTimer = new Timer(5000, e -> refreshStationData());
    this.refreshTimer.start();
}
```

### 2. Resource Cleanup
```java
public void dispose() {
    if (refreshTimer != null) {
        refreshTimer.stop();
    }
}
```

## Statistics Management

### 1. Statistics Update
```java
private void updateStatistics(Station station) {
    // Update statistics cards in the stats panel
    if (statsPanel != null && statsPanel.getComponentCount() > 0) {
        // This would update the actual statistics based on station data
        // For now, using placeholder updates
    }
}
```

## Integration Features

### Service Integration
- **StationManager**: Direct station service integration
- **Project Services**: IntelliJ project service binding
- **Event Handling**: Real-time station event processing

### UI Coordination
- **Panel Synchronization**: Coordinated sub-panel updates
- **State Management**: Consistent UI state across panels
- **Event Propagation**: Efficient event handling

### Error Handling
- **Service Availability**: Graceful handling of service unavailability
- **User Feedback**: Clear error messages and confirmations
- **State Recovery**: Robust state recovery mechanisms

## Performance Considerations

### UI Efficiency
- **Lazy Loading**: On-demand panel creation
- **Event Batching**: Efficient event processing
- **Memory Management**: Proper resource cleanup

### Data Synchronization
- **Minimal Refresh**: Only necessary UI updates
- **Background Processing**: Non-blocking operations
- **Cache Management**: Efficient data caching

This management panel provides a comprehensive interface for all station-related operations, integrating multiple specialized panels into a cohesive management experience.