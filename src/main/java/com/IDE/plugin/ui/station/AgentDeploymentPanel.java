package com.IDE.plugin.ui.station;

import com.IDE.plugin.core.agent.Agent;
import com.IDE.plugin.core.agent.AgentCapability;
import com.IDE.plugin.core.agent.AgentConfiguration;
import com.IDE.plugin.core.station.Station;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

/**
 * Agent deployment and configuration UI
 */
public class AgentDeploymentPanel extends JPanel {
    private final Project project;
    private Station currentStation;
    
    // Agent list components
    private final JBList<Agent> deployedAgentsList;
    private final CollectionListModel<Agent> deployedAgentsModel;
    private final JBList<String> availableAgentsList;
    private final CollectionListModel<String> availableAgentsModel;
    
    // Configuration components
    private JTextField agentNameField;
    private ComboBox<String> agentTypeCombo;
    private JTextArea agentDescriptionArea;
    private JSpinner instanceCountSpinner;
    private JCheckBox autoStartCheckbox;
    private JCheckBox autoRestartCheckbox;
    private JPanel capabilitiesPanel;
    private final Map<String, JCheckBox> capabilityCheckboxes;
    
    // Resource allocation
    private JSlider cpuAllocationSlider;
    private JSlider memoryAllocationSlider;
    private JSpinner maxTasksSpinner;
    private JSpinner taskTimeoutSpinner;
    
    // Deployment status
    private JProgressBar deploymentProgress;
    private JTextArea deploymentLogArea;
    private JLabel deploymentStatusLabel;
    
    public AgentDeploymentPanel(Project project) {
        this.project = project;
        this.deployedAgentsModel = new CollectionListModel<>();
        this.deployedAgentsList = new JBList<>(deployedAgentsModel);
        this.availableAgentsModel = new CollectionListModel<>();
        this.availableAgentsList = new JBList<>(availableAgentsModel);
        this.capabilityCheckboxes = new HashMap<>();
        
        initializeUI();
        loadAvailableAgents();
    }
    
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
    
    private JPanel createAgentListsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 10));
        
        // Deployed agents
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
        
        // Available agents
        JPanel availablePanel = new JPanel(new BorderLayout());
        availablePanel.setBorder(BorderFactory.createTitledBorder("Available Agent Templates"));
        
        availableAgentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        availableAgentsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = availableAgentsList.getSelectedValue();
                if (selected != null) {
                    loadAgentTemplate(selected);
                }
            }
        });
        
        JScrollPane availableScrollPane = new JBScrollPane(availableAgentsList);
        availablePanel.add(availableScrollPane, BorderLayout.CENTER);
        
        // Deploy button
        JButton deployButton = new JButton("Deploy Selected →");
        deployButton.addActionListener(e -> deploySelectedAgent());
        JPanel deployButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        deployButtonPanel.add(deployButton);
        availablePanel.add(deployButtonPanel, BorderLayout.SOUTH);
        
        panel.add(availablePanel);
        
        return panel;
    }
    
    private JPanel createConfigurationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Agent Configuration"));
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Basic configuration tab
        tabbedPane.addTab("Basic", createBasicConfigPanel());
        
        // Capabilities tab
        tabbedPane.addTab("Capabilities", createCapabilitiesPanel());
        
        // Resources tab
        tabbedPane.addTab("Resources", createResourcesPanel());
        
        // Advanced tab
        tabbedPane.addTab("Advanced", createAdvancedPanel());
        
        panel.add(tabbedPane, BorderLayout.CENTER);
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save Configuration");
        saveButton.addActionListener(e -> saveConfiguration());
        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> applyConfiguration());
        buttonPanel.add(applyButton);
        buttonPanel.add(saveButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createBasicConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(JBUI.Borders.empty(10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(5);
        
        // Agent name
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Agent Name:"), gbc);
        
        agentNameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(agentNameField, gbc);
        
        // Agent type
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Agent Type:"), gbc);
        
        agentTypeCombo = new ComboBox<>(new String[]{
            "Code Analysis", "Build", "Test", "Deploy", "Monitor", "Documentation", "Custom"
        });
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(agentTypeCombo, gbc);
        
        // Description
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(new JLabel("Description:"), gbc);
        
        agentDescriptionArea = new JTextArea(3, 20);
        agentDescriptionArea.setLineWrap(true);
        agentDescriptionArea.setWrapStyleWord(true);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(new JBScrollPane(agentDescriptionArea), gbc);
        
        // Instance count
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        panel.add(new JLabel("Instance Count:"), gbc);
        
        instanceCountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(instanceCountSpinner, gbc);
        
        // Auto-start
        autoStartCheckbox = new JCheckBox("Auto-start with station");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(autoStartCheckbox, gbc);
        
        // Auto-restart
        autoRestartCheckbox = new JCheckBox("Auto-restart on failure");
        gbc.gridy = 5;
        panel.add(autoRestartCheckbox, gbc);
        
        return panel;
    }
    
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
        JPanel customPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        customPanel.setBorder(BorderFactory.createTitledBorder("Custom Capabilities"));
        JTextField customCapField = new JTextField(20);
        JButton addCapButton = new JButton("Add");
        addCapButton.addActionListener(e -> addCustomCapability(customCapField.getText()));
        customPanel.add(customCapField);
        customPanel.add(addCapButton);
        
        mainPanel.add(customPanel, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    private JPanel createResourcesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(JBUI.Borders.empty(10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(5);
        
        // CPU allocation
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("CPU Allocation (%):"), gbc);
        
        cpuAllocationSlider = new JSlider(0, 100, 25);
        cpuAllocationSlider.setMajorTickSpacing(25);
        cpuAllocationSlider.setPaintTicks(true);
        cpuAllocationSlider.setPaintLabels(true);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(cpuAllocationSlider, gbc);
        
        // Memory allocation
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Memory Allocation (%):"), gbc);
        
        memoryAllocationSlider = new JSlider(0, 100, 25);
        memoryAllocationSlider.setMajorTickSpacing(25);
        memoryAllocationSlider.setPaintTicks(true);
        memoryAllocationSlider.setPaintLabels(true);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(memoryAllocationSlider, gbc);
        
        // Max concurrent tasks
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(new JLabel("Max Concurrent Tasks:"), gbc);
        
        maxTasksSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 50, 1));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(maxTasksSpinner, gbc);
        
        // Task timeout
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        panel.add(new JLabel("Task Timeout (seconds):"), gbc);
        
        taskTimeoutSpinner = new JSpinner(new SpinnerNumberModel(300, 30, 3600, 30));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(taskTimeoutSpinner, gbc);
        
        // Resource monitoring
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        JPanel monitorPanel = createResourceMonitorPanel();
        panel.add(monitorPanel, gbc);
        
        return panel;
    }
    
    private JPanel createResourceMonitorPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Current Resource Usage"));
        
        // Add resource monitors
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
    
    private JPanel createAdvancedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(10));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(5);
        
        // Environment variables
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Environment Variables:"), gbc);
        
        JTextArea envVarsArea = new JTextArea(5, 30);
        envVarsArea.setText("# Format: KEY=VALUE\n");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        formPanel.add(new JBScrollPane(envVarsArea), gbc);
        
        // Launch parameters
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Launch Parameters:"), gbc);
        
        JTextField launchParamsField = new JTextField(30);
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        formPanel.add(launchParamsField, gbc);
        
        // Logging configuration
        gbc.gridy = 4;
        formPanel.add(createLoggingConfigPanel(), gbc);
        
        panel.add(formPanel, BorderLayout.NORTH);
        
        return panel;
    }
    
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
    
    public void setStation(Station station) {
        this.currentStation = station;
        refreshDeployedAgents();
    }
    
    private void loadAvailableAgents() {
        // Load available agent templates
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
    
    private void refreshDeployedAgents() {
        if (currentStation != null) {
            deployedAgentsModel.removeAll();
            deployedAgentsModel.add(currentStation.getDeployedAgents());
        }
    }
    
    private void loadAgentTemplate(String templateName) {
        // Load template configuration
        agentNameField.setText(templateName);
        agentTypeCombo.setSelectedItem(getAgentTypeFromTemplate(templateName));
        agentDescriptionArea.setText("Agent based on " + templateName + " template");
        
        // Set default capabilities based on template
        clearCapabilities();
        setTemplateCapabilities(templateName);
    }
    
    private String getAgentTypeFromTemplate(String templateName) {
        if (templateName.contains("Code Analyzer")) return "Code Analysis";
        if (templateName.contains("Build")) return "Build";
        if (templateName.contains("Test")) return "Test";
        if (templateName.contains("Deploy")) return "Deploy";
        if (templateName.contains("Monitor")) return "Monitor";
        if (templateName.contains("Documentation")) return "Documentation";
        return "Custom";
    }
    
    private void clearCapabilities() {
        for (JCheckBox checkbox : capabilityCheckboxes.values()) {
            checkbox.setSelected(false);
        }
    }
    
    private void setTemplateCapabilities(String templateName) {
        // Set capabilities based on template
        if (templateName.contains("Code Analyzer")) {
            capabilityCheckboxes.get("Code Analysis").setSelected(true);
            capabilityCheckboxes.get("Refactoring").setSelected(true);
        } else if (templateName.contains("Test")) {
            capabilityCheckboxes.get("Testing").setSelected(true);
            capabilityCheckboxes.get("Debugging").setSelected(true);
        }
        // Add more template-specific capabilities
    }
    
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
        
        // Simulate deployment process
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
    
    private void configureSelectedAgent() {
        Agent selected = deployedAgentsList.getSelectedValue();
        if (selected != null) {
            // Load agent configuration
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
    
    private void addCustomCapability(String capability) {
        if (capability != null && !capability.trim().isEmpty()) {
            JCheckBox checkbox = new JCheckBox(capability);
            capabilityCheckboxes.put(capability, checkbox);
            capabilitiesPanel.add(checkbox);
            capabilitiesPanel.revalidate();
            capabilitiesPanel.repaint();
        }
    }
    
    private void saveConfiguration() {
        // Save current configuration
        Messages.showInfoMessage(project, "Configuration saved successfully", "Success");
    }
    
    private void applyConfiguration() {
        // Apply configuration changes
        Agent selected = deployedAgentsList.getSelectedValue();
        if (selected != null) {
            // Apply changes to selected agent
            Messages.showInfoMessage(project, "Configuration applied to agent", "Success");
        }
    }
    
    // Custom cell renderer for agent list
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
        
        private Icon getAgentIcon(String status) {
            // Return appropriate icon based on status
            return UIManager.getIcon("Tree.leafIcon"); // Placeholder
        }
    }
}