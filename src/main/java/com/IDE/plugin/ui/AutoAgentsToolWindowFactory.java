package com.IDE.plugin.ui;

import com.IDE.plugin.ai.multiagent.agent.AgentState;
import com.IDE.plugin.ai.multiagent.core.AgentRole;
import com.IDE.plugin.ai.multiagent.services.AgentManagementService;
import com.IDE.plugin.ai.multiagent.services.AgentManagementService.AgentDeploymentConfig;
import com.IDE.plugin.ai.multiagent.services.AgentManagementService.AgentMonitoringData;
import com.IDE.plugin.ai.multiagent.services.AgentManagementService.AgentManagementEvent;
import com.IDE.plugin.ai.multiagent.services.AgentManagementService.AgentManagementListener;
import com.IDE.plugin.ai.multiagent.station.Station;
import com.IDE.plugin.ai.multiagent.station.StationConfiguration;
import com.IDE.plugin.ai.multiagent.station.StationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.tabs.JBTabbedPane;
import com.IDE.plugin.settings.AutoAgentsSettings;
import com.IDE.plugin.services.AIService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AutoAgentsToolWindowFactory implements ToolWindowFactory, AgentManagementListener {

    // Code generation tab components
    private JBTextArea inputArea;
    private JBTextArea outputArea;
    private JButton generateButton;
    private JComboBox<String> modelComboBox;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    // Agent management tab components
    private JBTable agentsTable;
    private DefaultTableModel agentsTableModel;
    private JButton deployAgentButton;
    private JButton undeployAgentButton;
    private JButton configureAgentButton;

    // Station management tab components
    private JBTable stationsTable;
    private DefaultTableModel stationsTableModel;
    private JButton createStationButton;
    private JButton removeStationButton;
    private JButton assignAgentButton;

    // Monitoring tab components
    private JBTable monitoringTable;
    private DefaultTableModel monitoringTableModel;
    private JTree agentTree;
    private DefaultTreeModel agentTreeModel;
    private Timer refreshTimer;

    // Services
    private AgentManagementService agentManagementService;
    private StationManager stationManager;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // Initialize services
        agentManagementService = project.getService(AgentManagementService.class);
        stationManager = project.getService(StationManager.class);

        // Register as listener for agent management events
        if (agentManagementService != null) {
            agentManagementService.addListener(this);
        }

        // Create tabbed interface
        JBTabbedPane tabbedPane = new JBTabbedPane();

        // Add tabs
        tabbedPane.addTab("Code Generation", createCodeGenerationPanel(project));
        tabbedPane.addTab("Agent Management", createAgentManagementPanel(project));
        tabbedPane.addTab("Station Management", createStationManagementPanel(project));
        tabbedPane.addTab("Monitoring", createMonitoringPanel(project));

        // Create content
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(tabbedPane, "", false);
        toolWindow.getContentManager().addContent(content);

        // Start monitoring refresh timer
        startMonitoringRefresh();
    }

    private JPanel createCodeGenerationPanel(Project project) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header panel
        JPanel headerPanel = createHeaderPanel();
        panel.add(headerPanel, BorderLayout.NORTH);

        // Center panel with split pane
        JSplitPane splitPane = createSplitPane();
        panel.add(splitPane, BorderLayout.CENTER);

        // Bottom panel with controls
        JPanel bottomPanel = createBottomPanel(project);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAgentManagementPanel(Project project) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create table model with columns
        agentsTableModel = new DefaultTableModel(
            new Object[][] {},
            new String[] {"Agent ID", "Name", "Role", "State", "Station"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        // Create table
        agentsTable = new JBTable(agentsTableModel);
        agentsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        agentsTable.getTableHeader().setReorderingAllowed(false);

        // Add table to scroll pane
        JBScrollPane scrollPane = new JBScrollPane(agentsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        deployAgentButton = new JButton("Deploy Agent");
        deployAgentButton.addActionListener(e -> showDeployAgentDialog(project));
        buttonPanel.add(deployAgentButton);

        undeployAgentButton = new JButton("Undeploy Agent");
        undeployAgentButton.addActionListener(e -> undeploySelectedAgent());
        undeployAgentButton.setEnabled(false);
        buttonPanel.add(undeployAgentButton);

        configureAgentButton = new JButton("Configure");
        configureAgentButton.addActionListener(e -> showConfigureAgentDialog());
        configureAgentButton.setEnabled(false);
        buttonPanel.add(configureAgentButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Enable/disable buttons based on selection
        agentsTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = agentsTable.getSelectedRow() != -1;
            undeployAgentButton.setEnabled(hasSelection);
            configureAgentButton.setEnabled(hasSelection);
        });

        // Populate table with existing agents
        refreshAgentsTable();

        return panel;
    }

    private JPanel createStationManagementPanel(Project project) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create table model with columns
        stationsTableModel = new DefaultTableModel(
            new Object[][] {},
            new String[] {"Station ID", "Name", "Active", "Agents", "Health"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        // Create table
        stationsTable = new JBTable(stationsTableModel);
        stationsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stationsTable.getTableHeader().setReorderingAllowed(false);

        // Add table to scroll pane
        JBScrollPane scrollPane = new JBScrollPane(stationsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        createStationButton = new JButton("Create Station");
        createStationButton.addActionListener(e -> showCreateStationDialog(project));
        buttonPanel.add(createStationButton);

        removeStationButton = new JButton("Remove Station");
        removeStationButton.addActionListener(e -> removeSelectedStation());
        removeStationButton.setEnabled(false);
        buttonPanel.add(removeStationButton);

        assignAgentButton = new JButton("Assign Agent");
        assignAgentButton.addActionListener(e -> showAssignAgentDialog());
        assignAgentButton.setEnabled(false);
        buttonPanel.add(assignAgentButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Enable/disable buttons based on selection
        stationsTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = stationsTable.getSelectedRow() != -1;
            removeStationButton.setEnabled(hasSelection);
            assignAgentButton.setEnabled(hasSelection);
        });

        // Populate table with existing stations
        refreshStationsTable();

        return panel;
    }

    private JPanel createMonitoringPanel(Project project) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.3);

        // Create agent tree
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Agents");
        agentTreeModel = new DefaultTreeModel(root);
        agentTree = new Tree(agentTreeModel);
        JBScrollPane treeScrollPane = new JBScrollPane(agentTree);
        splitPane.setLeftComponent(treeScrollPane);

        // Create monitoring table
        monitoringTableModel = new DefaultTableModel(
            new Object[][] {},
            new String[] {"Metric", "Value"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        monitoringTable = new JBTable(monitoringTableModel);
        monitoringTable.getTableHeader().setReorderingAllowed(false);
        JBScrollPane tableScrollPane = new JBScrollPane(monitoringTable);
        splitPane.setRightComponent(tableScrollPane);

        panel.add(splitPane, BorderLayout.CENTER);

        // Add selection listener to tree
        agentTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) agentTree.getLastSelectedPathComponent();
            if (node != null && node.isLeaf() && node.getParent() != null && node.getParent() != root) {
                String agentId = node.getUserObject().toString();
                updateMonitoringTable(agentId);
            } else {
                clearMonitoringTable();
            }
        });

        // Populate tree with existing agents
        refreshAgentTree();

        return panel;
    }

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

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JBLabel titleLabel = new JBLabel("AutoAgents AI Assistant");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Model selection
        JPanel modelPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        modelPanel.add(new JBLabel("Model:"));
        modelComboBox = new JComboBox<>(new String[]{"GPT-4", "GPT-3.5", "Claude", "Custom"});
        modelPanel.add(modelComboBox);
        headerPanel.add(modelPanel, BorderLayout.EAST);

        return headerPanel;
    }

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

    private JPanel createBottomPanel(Project project) {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        generateButton = new JButton("Generate");
        generateButton.addActionListener(e -> generateCode(project));
        buttonPanel.add(generateButton);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearAll());
        buttonPanel.add(clearButton);

        JButton insertButton = new JButton("Insert to Editor");
        insertButton.addActionListener(e -> insertToEditor(project));
        buttonPanel.add(insertButton);

        bottomPanel.add(buttonPanel, BorderLayout.WEST);

        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        statusPanel.add(progressBar, BorderLayout.NORTH);

        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(JBColor.GRAY);
        statusPanel.add(statusLabel, BorderLayout.SOUTH);

        bottomPanel.add(statusPanel, BorderLayout.EAST);

        return bottomPanel;
    }

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

    private void clearAll() {
        inputArea.setText("");
        outputArea.setText("");
        statusLabel.setText("Ready");
    }

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

    // Agent Management Methods

    private void refreshAgentsTable() {
        if (agentManagementService == null) {
            return;
        }

        // Clear table
        agentsTableModel.setRowCount(0);

        // Get deployed agents
        List<String> agentIds = agentManagementService.getDeployedAgents();

        // Add rows for each agent
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

    private void showDeployAgentDialog(Project project) {
        // Create dialog
        JDialog dialog = new JDialog();
        dialog.setTitle("Deploy Agent");
        dialog.setModal(true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null);

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);

        // Name field
        c.gridx = 0;
        c.gridy = 0;
        formPanel.add(new JLabel("Name:"), c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1.0;
        JTextField nameField = new JTextField(20);
        formPanel.add(nameField, c);

        // Role field
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.0;
        formPanel.add(new JLabel("Role:"), c);

        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1.0;
        JComboBox<AgentRole> roleComboBox = new JComboBox<>(AgentRole.values());
        formPanel.add(roleComboBox, c);

        // Station field
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.0;
        formPanel.add(new JLabel("Station:"), c);

        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 1.0;

        // Get station IDs
        List<Station> stations = stationManager.getActiveStations();
        String[] stationOptions = new String[stations.size() + 1];
        stationOptions[0] = "Auto-assign";
        for (int i = 0; i < stations.size(); i++) {
            stationOptions[i + 1] = stations.get(i).getId();
        }

        JComboBox<String> stationComboBox = new JComboBox<>(stationOptions);
        formPanel.add(stationComboBox, c);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);

        JButton deployButton = new JButton("Deploy");
        deployButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name is required", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            AgentRole role = (AgentRole) roleComboBox.getSelectedItem();
            String stationId = stationComboBox.getSelectedIndex() == 0 ? null : stationComboBox.getSelectedItem().toString();

            // Create deployment config
            AgentDeploymentConfig config = new AgentDeploymentConfig(
                name,
                role,
                new HashSet<>(), // Default capabilities
                new HashMap<>()  // Default configuration
            );

            if (stationId != null) {
                config.setStationId(stationId);
                config.setAutoAssignStation(false);
            }

            // Deploy agent
            agentManagementService.deployAgent(config);

            dialog.dispose();
        });
        buttonPanel.add(deployButton);

        // Add panels to dialog
        dialog.setLayout(new BorderLayout());
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void undeploySelectedAgent() {
        int selectedRow = agentsTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        String agentId = agentsTableModel.getValueAt(selectedRow, 0).toString();

        // Confirm undeploy
        int result = JOptionPane.showConfirmDialog(
            null,
            "Are you sure you want to undeploy agent " + agentId + "?",
            "Confirm Undeploy",
            JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            agentManagementService.undeployAgent(agentId);
        }
    }

    private void showConfigureAgentDialog() {
        int selectedRow = agentsTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        String agentId = agentsTableModel.getValueAt(selectedRow, 0).toString();
        AgentDeploymentConfig config = agentManagementService.getAgentDeploymentConfig(agentId);

        if (config == null) {
            return;
        }

        // Create dialog
        JDialog dialog = new JDialog();
        dialog.setTitle("Configure Agent: " + agentId);
        dialog.setModal(true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null);

        // Create configuration panel
        JPanel configPanel = new JPanel(new BorderLayout());

        // Create table for configuration properties
        DefaultTableModel configTableModel = new DefaultTableModel(
            new Object[][] {},
            new String[] {"Property", "Value"}
        );

        JTable configTable = new JBTable(configTableModel);
        configTable.getTableHeader().setReorderingAllowed(false);

        // Add existing configuration properties
        for (Map.Entry<String, Object> entry : config.getConfiguration().entrySet()) {
            configTableModel.addRow(new Object[] {entry.getKey(), entry.getValue()});
        }

        configPanel.add(new JBScrollPane(configTable), BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton addButton = new JButton("Add Property");
        addButton.addActionListener(e -> {
            configTableModel.addRow(new Object[] {"", ""});
        });
        buttonPanel.add(addButton);

        JButton removeButton = new JButton("Remove Property");
        removeButton.addActionListener(e -> {
            int row = configTable.getSelectedRow();
            if (row != -1) {
                configTableModel.removeRow(row);
            }
        });
        buttonPanel.add(removeButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            // Collect configuration properties
            Map<String, Object> newConfig = new HashMap<>();
            for (int i = 0; i < configTableModel.getRowCount(); i++) {
                String key = configTableModel.getValueAt(i, 0).toString();
                Object value = configTableModel.getValueAt(i, 1);
                if (!key.isEmpty()) {
                    newConfig.put(key, value);
                }
            }

            // Update agent configuration
            agentManagementService.updateAgentConfiguration(agentId, newConfig);

            dialog.dispose();
        });
        buttonPanel.add(saveButton);

        // Add panels to dialog
        dialog.setLayout(new BorderLayout());
        dialog.add(configPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // Station Management Methods

    private void refreshStationsTable() {
        if (stationManager == null) {
            return;
        }

        // Clear table
        stationsTableModel.setRowCount(0);

        // Get all stations
        List<Station> stations = stationManager.getActiveStations();

        // Add rows for each station
        for (Station station : stations) {
            stationsTableModel.addRow(new Object[] {
                station.getId(),
                station.getConfiguration().getName(),
                station.isActive() ? "Yes" : "No",
                station.getAssignedAgents().size(),
                String.format("%.2f", station.getErrorRate() * 100) + "%"
            });
        }
    }

    private void showCreateStationDialog(Project project) {
        // Create dialog
        JDialog dialog = new JDialog();
        dialog.setTitle("Create Station");
        dialog.setModal(true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null);

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 5, 5);

        // Name field
        c.gridx = 0;
        c.gridy = 0;
        formPanel.add(new JLabel("Name:"), c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1.0;
        JTextField nameField = new JTextField(20);
        formPanel.add(nameField, c);

        // Max agents field
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.0;
        formPanel.add(new JLabel("Max Agents:"), c);

        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1.0;
        JSpinner maxAgentsSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
        formPanel.add(maxAgentsSpinner, c);

        // Preferred roles field
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.0;
        formPanel.add(new JLabel("Preferred Roles:"), c);

        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 1.0;

        // Create role checkboxes
        JPanel rolesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Map<AgentRole, JCheckBox> roleCheckboxes = new HashMap<>();

        for (AgentRole role : AgentRole.values()) {
            JCheckBox checkbox = new JCheckBox(role.getDisplayName());
            roleCheckboxes.put(role, checkbox);
            rolesPanel.add(checkbox);
        }

        formPanel.add(rolesPanel, c);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);

        JButton createButton = new JButton("Create");
        createButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name is required", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int maxAgents = (Integer) maxAgentsSpinner.getValue();

            // Create station configuration
            StationConfiguration.Builder builder = new StationConfiguration.Builder(name)
                .maxAgents(maxAgents);

            // Add preferred roles
            for (Map.Entry<AgentRole, JCheckBox> entry : roleCheckboxes.entrySet()) {
                if (entry.getValue().isSelected()) {
                    builder.addPreferredRole(entry.getKey());
                }
            }

            // Create station
            agentManagementService.createStation(builder.build());

            dialog.dispose();
        });
        buttonPanel.add(createButton);

        // Add panels to dialog
        dialog.setLayout(new BorderLayout());
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void removeSelectedStation() {
        int selectedRow = stationsTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        String stationId = stationsTableModel.getValueAt(selectedRow, 0).toString();

        // Confirm remove
        int result = JOptionPane.showConfirmDialog(
            null,
            "Are you sure you want to remove station " + stationId + "?",
            "Confirm Remove",
            JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            stationManager.removeStation(stationId);
        }
    }

    private void showAssignAgentDialog() {
        int selectedRow = stationsTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        String stationId = stationsTableModel.getValueAt(selectedRow, 0).toString();

        // Create dialog
        JDialog dialog = new JDialog();
        dialog.setTitle("Assign Agent to Station: " + stationId);
        dialog.setModal(true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null);

        // Create agent selection panel
        JPanel agentPanel = new JPanel(new BorderLayout());

        // Get deployed agents
        List<String> agentIds = agentManagementService.getDeployedAgents();
        DefaultListModel<String> agentListModel = new DefaultListModel<>();

        for (String agentId : agentIds) {
            AgentDeploymentConfig config = agentManagementService.getAgentDeploymentConfig(agentId);
            if (config != null) {
                agentListModel.addElement(agentId + " (" + config.getName() + ")");
            }
        }

        JList<String> agentList = new JList<>(agentListModel);
        agentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        agentPanel.add(new JBScrollPane(agentList), BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);

        JButton assignButton = new JButton("Assign");
        assignButton.addActionListener(e -> {
            int selectedIndex = agentList.getSelectedIndex();
            if (selectedIndex == -1) {
                JOptionPane.showMessageDialog(dialog, "Please select an agent", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String selectedAgent = agentList.getSelectedValue();
            String agentId = selectedAgent.substring(0, selectedAgent.indexOf(" ("));

            // Assign agent to station
            agentManagementService.reassignAgent(agentId, stationId);

            dialog.dispose();
        });
        buttonPanel.add(assignButton);

        // Add panels to dialog
        dialog.setLayout(new BorderLayout());
        dialog.add(agentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // Monitoring Methods

    private void refreshAgentTree() {
        if (agentManagementService == null) {
            return;
        }

        // Get root node
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

        // Reload tree model
        agentTreeModel.reload();

        // Expand all nodes
        for (int i = 0; i < agentTree.getRowCount(); i++) {
            agentTree.expandRow(i);
        }
    }

    private void updateMonitoringTable(String agentId) {
        if (agentManagementService == null) {
            return;
        }

        // Clear table
        monitoringTableModel.setRowCount(0);

        // Get monitoring data
        AgentMonitoringData data = agentManagementService.getAgentMonitoringData(agentId);
        if (data == null) {
            return;
        }

        // Add rows for each metric
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

    private void clearMonitoringTable() {
        monitoringTableModel.setRowCount(0);
    }

    private String formatDuration(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // AgentManagementListener Implementation

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
}
