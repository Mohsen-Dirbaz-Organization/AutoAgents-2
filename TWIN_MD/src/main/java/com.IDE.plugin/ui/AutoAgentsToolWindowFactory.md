# AutoAgentsToolWindowFactory.md

## Class Overview
**File:** `src/main/java/com/IDE/plugin/ui/AutoAgentsToolWindowFactory.java`  
**Package:** `com.IDE.plugin.ui`  
**Type:** IntelliJ IDEA Tool Window Factory

## Purpose
The `AutoAgentsToolWindowFactory` class creates and manages the main tool window interface for the AutoAgents plugin. It provides comprehensive functionality for code generation, agent management, station control, and system monitoring through a tabbed interface.

## Architecture

### Interface Implementation
```java
public class AutoAgentsToolWindowFactory implements ToolWindowFactory, AgentManagementListener
```

### Component Structure
- **Tabbed Interface**: Multiple functional views
- **Code Generation**: AI-powered code generation
- **Agent Management**: Agent deployment and configuration
- **Station Management**: Station lifecycle control
- **Monitoring Dashboard**: Real-time system monitoring

## Tool Window Components

### 1. Code Generation Tab
#### UI Components
```java
// Input/Output components
private JBTextArea inputArea;
private JBTextArea outputArea;
private JButton generateButton;
private JComboBox<String> modelComboBox;
private JProgressBar progressBar;
private JLabel statusLabel;
```

#### Layout Structure
- **Header Panel**: Title and model selection
- **Split Pane**: Input and output areas
- **Control Panel**: Generation actions and status

### 2. Agent Management Tab
#### Core Components
```java
// Agent table components
private JBTable agentsTable;
private DefaultTableModel agentsTableModel;
private JButton deployAgentButton;
private JButton undeployAgentButton;
private JButton configureAgentButton;
```

#### Table Schema
| Column | Type | Description |
|--------|------|-------------|
| Agent ID | String | Unique identifier |
| Name | String | Display name |
| Role | AgentRole | Agent type/role |
| State | AgentState | Current status |
| Station | String | Assigned station |

### 3. Station Management Tab
#### Management Interface
```java
// Station table components
private JBTable stationsTable;
private DefaultTableModel stationsTableModel;
private JButton createStationButton;
private JButton removeStationButton;
private JButton assignAgentButton;
```

#### Station Information
| Column | Type | Description |
|--------|------|-------------|
| Station ID | String | Unique identifier |
| Name | String | Station name |
| Active | Boolean | Running status |
| Agents | Integer | Agent count |
| Health | String | Health percentage |

### 4. Monitoring Tab
#### Monitoring Components
```java
// Monitoring interface
private JBTable monitoringTable;
private DefaultTableModel monitoringTableModel;
private JTree agentTree;
private DefaultTreeModel agentTreeModel;
private Timer refreshTimer;
```

## Code Generation Features

### Input Processing
```java
private void generateCode(Project project) {
    String input = inputArea.getText().trim();
    if (input.isEmpty()) {
        statusLabel.setText("Please enter a prompt");
        return;
    }
    
    generateButton.setEnabled(false);
    progressBar.setVisible(true);
    progressBar.setIndeterminate(true);
    statusLabel.setText("Generating...");
    
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
        try {
            AIService aiService = ApplicationManager.getApplication().getService(AIService.class);
            String result = aiService.generateCode(input, (String) modelComboBox.getSelectedItem());
            
            SwingUtilities.invokeLater(() -> {
                outputArea.setText(result);
                statusLabel.setText("Generation complete");
                generateButton.setEnabled(true);
                progressBar.setVisible(false);
            });
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                outputArea.setText("Error: " + e.getMessage());
                statusLabel.setText("Generation failed");
                generateButton.setEnabled(true);
                progressBar.setVisible(false);
            });
        }
    });
}
```

### Editor Integration
```java
private void insertToEditor(Project project) {
    String code = outputArea.getText().trim();
    if (code.isEmpty()) {
        statusLabel.setText("No code to insert");
        return;
    }
    
    Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
    if (editor != null) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            int offset = editor.getCaretModel().getOffset();
            editor.getDocument().insertString(offset, code);
            statusLabel.setText("Code inserted");
        });
    } else {
        statusLabel.setText("No active editor");
    }
}
```

## Agent Management Features

### Agent Deployment Dialog
```java
private void showDeployAgentDialog(Project project) {
    JDialog dialog = new JDialog();
    dialog.setTitle("Deploy Agent");
    dialog.setModal(true);
    dialog.setSize(400, 300);
    
    // Form components
    JTextField nameField = new JTextField(20);
    JComboBox<AgentRole> roleComboBox = new JComboBox<>(AgentRole.values());
    JComboBox<String> stationComboBox = createStationComboBox();
    
    // Layout and action handlers
    setupDeploymentForm(dialog, nameField, roleComboBox, stationComboBox);
    
    dialog.setVisible(true);
}
```

### Agent Configuration Management
```java
private void showConfigureAgentDialog() {
    int selectedRow = agentsTable.getSelectedRow();
    if (selectedRow == -1) return;
    
    String agentId = agentsTableModel.getValueAt(selectedRow, 0).toString();
    AgentDeploymentConfig config = agentManagementService.getAgentDeploymentConfig(agentId);
    
    if (config == null) return;
    
    // Create configuration dialog
    JDialog dialog = createConfigurationDialog(agentId, config);
    dialog.setVisible(true);
}
```

## Station Management Features

### Station Creation
```java
private void showCreateStationDialog(Project project) {
    JDialog dialog = new JDialog();
    dialog.setTitle("Create Station");
    dialog.setModal(true);
    dialog.setSize(400, 300);
    
    // Form fields
    JTextField nameField = new JTextField(20);
    JSpinner maxAgentsSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
    Map<AgentRole, JCheckBox> roleCheckboxes = createRoleCheckboxes();
    
    // Create button handler
    JButton createButton = new JButton("Create");
    createButton.addActionListener(e -> {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Name is required", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int maxAgents = (Integer) maxAgentsSpinner.getValue();
        StationConfiguration.Builder builder = new StationConfiguration.Builder(name)
            .maxAgents(maxAgents);
        
        // Add preferred roles
        roleCheckboxes.entrySet().stream()
            .filter(entry -> entry.getValue().isSelected())
            .forEach(entry -> builder.addPreferredRole(entry.getKey()));
        
        agentManagementService.createStation(builder.build());
        dialog.dispose();
    });
}
```

### Agent Assignment
```java
private void showAssignAgentDialog() {
    int selectedRow = stationsTable.getSelectedRow();
    if (selectedRow == -1) return;
    
    String stationId = stationsTableModel.getValueAt(selectedRow, 0).toString();
    
    // Get deployed agents
    List<String> agentIds = agentManagementService.getDeployedAgents();
    DefaultListModel<String> agentListModel = new DefaultListModel<>();
    
    agentIds.forEach(agentId -> {
        AgentDeploymentConfig config = agentManagementService.getAgentDeploymentConfig(agentId);
        if (config != null) {
            agentListModel.addElement(agentId + " (" + config.getName() + ")");
        }
    });
    
    // Show selection dialog
    showAgentSelectionDialog(stationId, agentListModel);
}
```

## Monitoring Features

### Agent Tree Structure
```java
private void refreshAgentTree() {
    if (agentManagementService == null) return;
    
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) agentTreeModel.getRoot();
    root.removeAllChildren();
    
    // Create role nodes
    Map<AgentRole, DefaultMutableTreeNode> roleNodes = new HashMap<>();
    for (AgentRole role : AgentRole.values()) {
        DefaultMutableTreeNode roleNode = new DefaultMutableTreeNode(role.getDisplayName());
        roleNodes.put(role, roleNode);
        root.add(roleNode);
    }
    
    // Add agents to role nodes
    List<String> agentIds = agentManagementService.getDeployedAgents();
    for (String agentId : agentIds) {
        AgentMonitoringData data = agentManagementService.getAgentMonitoringData(agentId);
        if (data != null) {
            DefaultMutableTreeNode roleNode = roleNodes.get(data.getRole());
            if (roleNode != null) {
                roleNode.add(new DefaultMutableTreeNode(agentId));
            }
        }
    }
    
    agentTreeModel.reload();
    expandAllNodes();
}
```

### Monitoring Data Display
```java
private void updateMonitoringTable(String agentId) {
    if (agentManagementService == null) return;
    
    monitoringTableModel.setRowCount(0);
    AgentMonitoringData data = agentManagementService.getAgentMonitoringData(agentId);
    if (data == null) return;
    
    // Add monitoring metrics
    monitoringTableModel.addRow(new Object[] {"Agent ID", data.getAgentId()});
    monitoringTableModel.addRow(new Object[] {"Role", data.getRole().getDisplayName()});
    monitoringTableModel.addRow(new Object[] {"State", data.getState().toString()});
    monitoringTableModel.addRow(new Object[] {"Success Rate", String.format("%.2f%%", data.getSuccessRate() * 100)});
    monitoringTableModel.addRow(new Object[] {"Error Rate", String.format("%.2f%%", data.getErrorRate() * 100)});
    monitoringTableModel.addRow(new Object[] {"Avg Response Time", data.getAverageResponseTime() + " ms"});
    monitoringTableModel.addRow(new Object[] {"Tasks Processed", data.getTasksProcessed()});
    monitoringTableModel.addRow(new Object[] {"Trust Score", String.format("%.2f", data.getTrustScore())});
    monitoringTableModel.addRow(new Object[] {"Memory Usage", data.getMemoryUsage() / 1024 + " KB"});
    monitoringTableModel.addRow(new Object[] {"Uptime", formatDuration(data.getUptime())});
}
```

## Real-time Updates

### Monitoring Refresh Timer
```java
private void startMonitoringRefresh() {
    refreshTimer = new Timer(5000, e -> {
        refreshAgentsTable();
        refreshStationsTable();
        refreshAgentTree();
        
        // Update monitoring table if an agent is selected
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) agentTree.getLastSelectedPathComponent();
        if (node != null && node.isLeaf() && node.getParent() != null && 
            node.getParent().getParent() == agentTree.getModel().getRoot()) {
            String agentId = node.getUserObject().toString();
            updateMonitoringTable(agentId);
        }
    });
    refreshTimer.start();
}
```

## Event Handling

### Agent Management Listener
```java
@Override
public void onEvent(@NotNull AgentManagementEvent event, @NotNull String entityId) {
    SwingUtilities.invokeLater(() -> {
        switch (event) {
            case AGENT_DEPLOYED:
            case AGENT_UNDEPLOYED:
            case AGENT_UPDATED:
            case AGENT_REASSIGNED:
                refreshAgentsTable();
                refreshAgentTree();
                break;
            case STATION_CREATED:
            case STATION_REMOVED:
                refreshStationsTable();
                break;
        }
    });
}
```

## UI Layout Management

### Split Pane Configuration
```java
private JSplitPane createSplitPane() {
    // Input panel
    JPanel inputPanel = new JPanel(new BorderLayout());
    inputPanel.setBorder(BorderFactory.createTitledBorder("Input"));
    
    inputArea = new JBTextArea();
    inputArea.setRows(10);
    inputArea.setLineWrap(true);
    inputArea.setWrapStyleWord(true);
    inputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    inputPanel.add(new JBScrollPane(inputArea), BorderLayout.CENTER);
    
    // Output panel
    JPanel outputPanel = new JPanel(new BorderLayout());
    outputPanel.setBorder(BorderFactory.createTitledBorder("Output"));
    
    outputArea = new JBTextArea();
    outputArea.setEditable(false);
    outputArea.setLineWrap(true);
    outputArea.setWrapStyleWord(true);
    outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    outputArea.setBackground(JBColor.background().darker());
    outputPanel.add(new JBScrollPane(outputArea), BorderLayout.CENTER);
    
    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPanel, outputPanel);
    splitPane.setDividerLocation(200);
    splitPane.setResizeWeight(0.5);
    
    return splitPane;
}
```

## Service Integration

### Service Dependencies
```java
private AgentManagementService agentManagementService;
private StationManager stationManager;

@Override
public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    // Initialize services
    agentManagementService = project.getService(AgentManagementService.class);
    stationManager = project.getService(StationManager.class);
    
    // Register as listener
    if (agentManagementService != null) {
        agentManagementService.addListener(this);
    }
    
    // Create UI components
    createToolWindowInterface(toolWindow);
}
```

## Data Refresh Mechanisms

### Table Refresh Operations
```java
private void refreshAgentsTable() {
    if (agentManagementService == null) return;
    
    agentsTableModel.setRowCount(0);
    List<String> agentIds = agentManagementService.getDeployedAgents();
    
    for (String agentId : agentIds) {
        AgentDeploymentConfig config = agentManagementService.getAgentDeploymentConfig(agentId);
        AgentMonitoringData data = agentManagementService.getAgentMonitoringData(agentId);
        
        if (config != null && data != null) {
            agentsTableModel.addRow(new Object[] {
                agentId,
                config.getName(),
                data.getRole().getDisplayName(),
                data.getState().toString(),
                config.getStationId() != null ? config.getStationId() : "None"
            });
        }
    }
}
```

## Error Handling

### Service Availability Checks
```java
private void validateServices() {
    if (agentManagementService == null) {
        showServiceError("Agent Management Service not available");
        return;
    }
    
    if (stationManager == null) {
        showServiceError("Station Manager not available");
        return;
    }
}

private void showServiceError(String message) {
    SwingUtilities.invokeLater(() -> {
        statusLabel.setText(message);
        statusLabel.setForeground(JBColor.RED);
    });
}
```

## Utility Methods

### Duration Formatting
```java
private String formatDuration(long millis) {
    long hours = TimeUnit.MILLISECONDS.toHours(millis);
    long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
    long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
    
    return String.format("%02d:%02d:%02d", hours, minutes, seconds);
}
```

## Performance Considerations

### Threading
- **Background Tasks**: Code generation on pooled threads
- **UI Updates**: SwingUtilities.invokeLater for thread safety
- **Timer Management**: Proper timer cleanup

### Memory Management
- **Table Model Efficiency**: Row-based updates
- **Service Caching**: Minimize service calls
- **Resource Cleanup**: Proper disposal of resources

## Future Enhancements

### Planned Features
- **Enhanced Monitoring**: Real-time charts and graphs
- **Batch Operations**: Multi-agent/station operations
- **Export Functionality**: Data export capabilities
- **Plugin Integration**: Third-party plugin support

### UI Improvements
- **Drag-and-Drop**: Agent assignment via drag-and-drop
- **Keyboard Shortcuts**: Enhanced keyboard navigation
- **Theme Support**: Adaptive UI themes
- **Accessibility**: Enhanced accessibility features

This tool window factory provides the central interface for all AutoAgents functionality, offering comprehensive management and monitoring capabilities through an intuitive tabbed interface.