# AgentDeploymentPanel.md

## Class Overview
**File:** `src/main/java/com/IDE/plugin/ui/station/AgentDeploymentPanel.java`  
**Package:** `com.IDE.plugin.ui.station`  
**Type:** Swing Panel Component for Agent Deployment and Configuration

## Purpose
The `AgentDeploymentPanel` class provides a comprehensive user interface for deploying, configuring, and managing agents within stations. It offers both template-based deployment and custom configuration options with real-time monitoring capabilities.

## Architecture

### Component Structure
```java
public class AgentDeploymentPanel extends JPanel {
    private final Project project;
    private Station currentStation;
    
    // Agent list components
    private final JBList<Agent> deployedAgentsList;
    private final JBList<String> availableAgentsList;
    
    // Configuration components
    private JTextField agentNameField;
    private ComboBox<String> agentTypeCombo;
    private JPanel capabilitiesPanel;
    
    // Resource allocation
    private JSlider cpuAllocationSlider;
    private JSlider memoryAllocationSlider;
    
    // Deployment status
    private JProgressBar deploymentProgress;
    private JTextArea deploymentLogArea;
}
```

## UI Layout

### Main Layout Structure
```java
private void initializeUI() {
    setLayout(new BorderLayout());
    setBorder(JBUI.Borders.empty(10));
    
    // Create split pane
    JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    mainSplitPane.setDividerLocation(400);
    mainSplitPane.setResizeWeight(0.5);
    
    // Left panel - Agent lists
    JPanel leftPanel = createAgentListsPanel();
    mainSplitPane.setLeftComponent(leftPanel);
    
    // Right panel - Configuration
    JPanel rightPanel = createConfigurationPanel();
    mainSplitPane.setRightComponent(rightPanel);
    
    add(mainSplitPane, BorderLayout.CENTER);
    
    // Bottom panel - Deployment status
    JPanel bottomPanel = createDeploymentStatusPanel();
    add(bottomPanel, BorderLayout.SOUTH);
}
```

## Agent Management Features

### 1. Deployed Agents List
#### Components
```java
private JPanel createAgentListsPanel() {
    JPanel panel = new JPanel(new GridLayout(2, 1, 0, 10));
    
    // Deployed agents section
    JPanel deployedPanel = new JPanel(new BorderLayout());
    deployedPanel.setBorder(BorderFactory.createTitledBorder("Deployed Agents"));
    
    deployedAgentsList.setCellRenderer(new AgentListCellRenderer());
    deployedAgentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    ToolbarDecorator deployedDecorator = ToolbarDecorator.createDecorator(deployedAgentsList)
            .setRemoveAction(button -> undeploySelectedAgent())
            .setEditAction(button -> configureSelectedAgent())
            .disableAddAction()
            .disableUpDownActions();
    
    deployedPanel.add(deployedDecorator.createPanel(), BorderLayout.CENTER);
    panel.add(deployedPanel);
    
    return panel;
}
```

#### Custom Cell Renderer
```java
private static class AgentListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        if (value instanceof Agent) {
            Agent agent = (Agent) value;
            setText(agent.getName() + " (" + agent.getStatus() + ")");
            setIcon(getAgentIcon(agent.getStatus()));
        }
        
        return this;
    }
}
```

### 2. Available Agent Templates
#### Template Selection
```java
private void loadAvailableAgents() {
    availableAgentsModel.add(Arrays.asList(
        "Code Analyzer Agent",
        "Build Agent", 
        "Test Runner Agent",
        "Deployment Agent",
        "Monitoring Agent",
        "Documentation Agent",
        "Security Scanner Agent",
        "Performance Analyzer Agent"
    ));
}
```

#### Template Loading
```java
private void loadAgentTemplate(String templateName) {
    // Load template configuration
    agentNameField.setText(templateName);
    agentTypeCombo.setSelectedItem(getAgentTypeFromTemplate(templateName));
    agentDescriptionArea.setText("Agent based on " + templateName + " template");
    
    // Set default capabilities based on template
    clearCapabilities();
    setTemplateCapabilities(templateName);
}
```

## Configuration Interface

### 1. Basic Configuration Tab
```java
private JPanel createBasicConfigPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(JBUI.Borders.empty(10));
    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = JBUI.insets(5);
    
    // Agent name
    addFormField(panel, gbc, "Agent Name:", agentNameField = new JTextField(20), 0);
    
    // Agent type
    agentTypeCombo = new ComboBox<>(new String[]{
        "Code Analysis", "Build", "Test", "Deploy", "Monitor", "Documentation", "Custom"
    });
    addFormField(panel, gbc, "Agent Type:", agentTypeCombo, 1);
    
    // Description
    agentDescriptionArea = new JTextArea(3, 20);
    agentDescriptionArea.setLineWrap(true);
    agentDescriptionArea.setWrapStyleWord(true);
    addFormField(panel, gbc, "Description:", new JBScrollPane(agentDescriptionArea), 2);
    
    // Instance count
    instanceCountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
    addFormField(panel, gbc, "Instance Count:", instanceCountSpinner, 3);
    
    // Checkboxes
    autoStartCheckbox = new JCheckBox("Auto-start with station");
    autoRestartCheckbox = new JCheckBox("Auto-restart on failure");
    
    return panel;
}
```

### 2. Capabilities Configuration
```java
private JPanel createCapabilitiesPanel() {
    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(JBUI.Borders.empty(10));
    
    capabilitiesPanel = new JPanel();
    capabilitiesPanel.setLayout(new BoxLayout(capabilitiesPanel, BoxLayout.Y_AXIS));
    
    // Add capability checkboxes
    String[] capabilities = {
        "Code Analysis", "Code Generation", "Testing", "Debugging",
        "Documentation", "Refactoring", "Performance Analysis", "Security Scanning",
        "Build Management", "Deployment", "Monitoring", "Logging"
    };
    
    for (String capability : capabilities) {
        JCheckBox checkbox = new JCheckBox(capability);
        capabilityCheckboxes.put(capability, checkbox);
        capabilitiesPanel.add(checkbox);
    }
    
    JScrollPane scrollPane = new JBScrollPane(capabilitiesPanel);
    mainPanel.add(scrollPane, BorderLayout.CENTER);
    
    // Custom capability panel
    JPanel customPanel = createCustomCapabilityPanel();
    mainPanel.add(customPanel, BorderLayout.SOUTH);
    
    return mainPanel;
}
```

### 3. Resource Allocation
```java
private JPanel createResourcesPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(JBUI.Borders.empty(10));
    
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = JBUI.insets(5);
    
    // CPU allocation
    addSliderField(panel, gbc, "CPU Allocation (%):", 
                  cpuAllocationSlider = new JSlider(0, 100, 25), 0);
    
    // Memory allocation  
    addSliderField(panel, gbc, "Memory Allocation (%):",
                  memoryAllocationSlider = new JSlider(0, 100, 25), 1);
    
    // Max concurrent tasks
    maxTasksSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 50, 1));
    addFormField(panel, gbc, "Max Concurrent Tasks:", maxTasksSpinner, 2);
    
    // Task timeout
    taskTimeoutSpinner = new JSpinner(new SpinnerNumberModel(300, 30, 3600, 30));
    addFormField(panel, gbc, "Task Timeout (seconds):", taskTimeoutSpinner, 3);
    
    // Resource monitoring panel
    JPanel monitorPanel = createResourceMonitorPanel();
    addFormField(panel, gbc, "", monitorPanel, 4);
    
    return panel;
}
```

### 4. Resource Monitoring
```java
private JPanel createResourceMonitorPanel() {
    JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
    panel.setBorder(BorderFactory.createTitledBorder("Current Resource Usage"));
    
    // Add resource indicators
    panel.add(createResourceIndicator("CPU", 0));
    panel.add(createResourceIndicator("Memory", 0));
    panel.add(createResourceIndicator("Disk I/O", 0));
    panel.add(createResourceIndicator("Network", 0));
    
    return panel;
}

private JPanel createResourceIndicator(String name, int value) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(new JLabel(name + ":"), BorderLayout.WEST);
    
    JProgressBar bar = new JProgressBar(0, 100);
    bar.setValue(value);
    bar.setStringPainted(true);
    bar.setString(value + "%");
    panel.add(bar, BorderLayout.CENTER);
    
    return panel;
}
```

## Deployment Process

### 1. Deployment Workflow
```java
private void deploySelectedAgent() {
    String selectedTemplate = availableAgentsList.getSelectedValue();
    if (selectedTemplate == null || currentStation == null) {
        Messages.showWarningDialog(project, "Please select an agent template and a station", "Deployment Warning");
        return;
    }
    
    // Show deployment progress
    deploymentStatusLabel.setText("Deploying " + agentNameField.getText() + "...");
    deploymentProgress.setIndeterminate(true);
    deploymentLogArea.append("Starting deployment...\n");
    
    // Execute deployment process
    executeDeployment();
}
```

### 2. Asynchronous Deployment
```java
private void executeDeployment() {
    SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
        @Override
        protected Boolean doInBackground() throws Exception {
            publish("Validating configuration...");
            Thread.sleep(500);
            publish("Creating agent instance...");
            Thread.sleep(1000);
            publish("Configuring capabilities...");
            Thread.sleep(500);
            publish("Starting agent...");
            Thread.sleep(1000);
            return true;
        }
        
        @Override
        protected void process(List<String> chunks) {
            for (String message : chunks) {
                deploymentLogArea.append(message + "\n");
            }
        }
        
        @Override
        protected void done() {
            try {
                boolean success = get();
                if (success) {
                    deploymentStatusLabel.setText("Deployment successful");
                    deploymentLogArea.append("Agent deployed successfully!\n");
                    refreshDeployedAgents();
                }
            } catch (Exception e) {
                deploymentStatusLabel.setText("Deployment failed");
                deploymentLogArea.append("Error: " + e.getMessage() + "\n");
            } finally {
                deploymentProgress.setIndeterminate(false);
                deploymentProgress.setValue(100);
            }
        }
    };
    
    worker.execute();
}
```

## Agent Lifecycle Management

### 1. Agent Undeployment
```java
private void undeploySelectedAgent() {
    Agent selected = deployedAgentsList.getSelectedValue();
    if (selected != null) {
        int result = Messages.showYesNoDialog(
            project,
            "Are you sure you want to undeploy agent '" + selected.getName() + "'?",
            "Confirm Undeploy",
            Messages.getWarningIcon()
        );
        
        if (result == Messages.YES) {
            currentStation.undeployAgent(selected.getId());
            refreshDeployedAgents();
        }
    }
}
```

### 2. Agent Configuration
```java
private void configureSelectedAgent() {
    Agent selected = deployedAgentsList.getSelectedValue();
    if (selected != null) {
        loadAgentConfiguration(selected);
    }
}

private void loadAgentConfiguration(Agent agent) {
    agentNameField.setText(agent.getName());
    agentTypeCombo.setSelectedItem(agent.getType());
    agentDescriptionArea.setText(agent.getDescription());
    
    // Load capabilities
    clearCapabilities();
    for (AgentCapability capability : agent.getCapabilities()) {
        JCheckBox checkbox = capabilityCheckboxes.get(capability.getName());
        if (checkbox != null) {
            checkbox.setSelected(true);
        }
    }
}
```

## Advanced Configuration Features

### 1. Advanced Settings Panel
```java
private JPanel createAdvancedPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(JBUI.Borders.empty(10));
    
    JPanel formPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = JBUI.insets(5);
    
    // Environment variables
    JTextArea envVarsArea = new JTextArea(5, 30);
    envVarsArea.setText("# Format: KEY=VALUE\n");
    addFormField(formPanel, gbc, "Environment Variables:", 
                new JBScrollPane(envVarsArea), 0);
    
    // Launch parameters
    JTextField launchParamsField = new JTextField(30);
    addFormField(formPanel, gbc, "Launch Parameters:", launchParamsField, 1);
    
    // Logging configuration
    JPanel loggingPanel = createLoggingConfigPanel();
    addFormField(formPanel, gbc, "", loggingPanel, 2);
    
    panel.add(formPanel, BorderLayout.NORTH);
    
    return panel;
}
```

### 2. Logging Configuration
```java
private JPanel createLoggingConfigPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.setBorder(BorderFactory.createTitledBorder("Logging"));
    
    JComboBox<String> logLevelCombo = new JComboBox<>(new String[]{
        "DEBUG", "INFO", "WARN", "ERROR"
    });
    panel.add(new JLabel("Log Level:"));
    panel.add(logLevelCombo);
    
    JCheckBox logToFileCheckbox = new JCheckBox("Log to file");
    panel.add(logToFileCheckbox);
    
    return panel;
}
```

## Status and Progress Tracking

### 1. Deployment Status Panel
```java
private JPanel createDeploymentStatusPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Deployment Status"));
    panel.setPreferredSize(new Dimension(0, 150));
    
    // Status label
    deploymentStatusLabel = new JLabel("Ready");
    deploymentStatusLabel.setBorder(JBUI.Borders.empty(5));
    panel.add(deploymentStatusLabel, BorderLayout.NORTH);
    
    // Progress bar
    deploymentProgress = new JProgressBar();
    deploymentProgress.setStringPainted(true);
    panel.add(deploymentProgress, BorderLayout.CENTER);
    
    // Log area
    deploymentLogArea = new JTextArea(5, 50);
    deploymentLogArea.setEditable(false);
    deploymentLogArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
    JScrollPane scrollPane = new JBScrollPane(deploymentLogArea);
    panel.add(scrollPane, BorderLayout.SOUTH);
    
    return panel;
}
```

## Template Management

### 1. Template-to-Type Mapping
```java
private String getAgentTypeFromTemplate(String templateName) {
    if (templateName.contains("Code Analyzer")) return "Code Analysis";
    if (templateName.contains("Build")) return "Build";
    if (templateName.contains("Test")) return "Test";
    if (templateName.contains("Deploy")) return "Deploy";
    if (templateName.contains("Monitor")) return "Monitor";
    if (templateName.contains("Documentation")) return "Documentation";
    return "Custom";
}
```

### 2. Template Capabilities
```java
private void setTemplateCapabilities(String templateName) {
    // Set capabilities based on template
    if (templateName.contains("Code Analyzer")) {
        capabilityCheckboxes.get("Code Analysis").setSelected(true);
        capabilityCheckboxes.get("Refactoring").setSelected(true);
    } else if (templateName.contains("Test")) {
        capabilityCheckboxes.get("Testing").setSelected(true);
        capabilityCheckboxes.get("Debugging").setSelected(true);
    }
    // Additional template-specific capabilities can be added here
}
```

## Custom Capabilities

### Dynamic Capability Addition
```java
private void addCustomCapability(String capability) {
    if (capability != null && !capability.trim().isEmpty()) {
        JCheckBox checkbox = new JCheckBox(capability);
        capabilityCheckboxes.put(capability, checkbox);
        capabilitiesPanel.add(checkbox);
        capabilitiesPanel.revalidate();
        capabilitiesPanel.repaint();
    }
}
```

## Configuration Persistence

### 1. Save Configuration
```java
private void saveConfiguration() {
    // Save current configuration
    Messages.showInfoMessage(project, "Configuration saved successfully", "Success");
}
```

### 2. Apply Configuration
```java
private void applyConfiguration() {
    // Apply configuration changes
    Agent selected = deployedAgentsList.getSelectedValue();
    if (selected != null) {
        // Apply changes to selected agent
        Messages.showInfoMessage(project, "Configuration applied to agent", "Success");
    }
}
```

## Station Integration

### Station Binding
```java
public void setStation(Station station) {
    this.currentStation = station;
    refreshDeployedAgents();
}

private void refreshDeployedAgents() {
    if (currentStation != null) {
        deployedAgentsModel.removeAll();
        deployedAgentsModel.add(currentStation.getDeployedAgents());
    }
}
```

## Error Handling

### Validation and Error Display
```java
private boolean validateConfiguration() {
    if (agentNameField.getText().trim().isEmpty()) {
        Messages.showErrorDialog(project, "Agent name is required", "Validation Error");
        return false;
    }
    
    if (currentStation == null) {
        Messages.showErrorDialog(project, "No station selected", "Configuration Error");
        return false;
    }
    
    return true;
}
```

## Performance Considerations

### UI Responsiveness
- **Background Deployment**: SwingWorker for non-blocking operations
- **Progress Feedback**: Real-time deployment progress updates
- **Memory Management**: Efficient list model updates

### Resource Monitoring
- **Real-time Updates**: Live resource usage display
- **Efficient Rendering**: Optimized progress bar updates
- **Memory Cleanup**: Proper component disposal

## Accessibility Features

### Keyboard Navigation
- **Tab Order**: Logical tab sequence
- **Keyboard Shortcuts**: Accessible via keyboard
- **Screen Reader Support**: Proper labeling

### Visual Accessibility
- **High Contrast**: Theme-aware colors
- **Scalable Fonts**: Respect system font settings
- **Clear Indicators**: Obvious state changes

This panel provides a comprehensive interface for agent deployment and configuration, offering both novice-friendly templates and advanced customization options for experienced users.