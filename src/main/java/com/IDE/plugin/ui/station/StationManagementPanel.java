package com.IDE.plugin.ui.station;

import com.IDE.plugin.core.station.Station;
import com.IDE.plugin.core.station.StationManager;
import com.IDE.plugin.core.station.StationStatus;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main station management interface panel
 */
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
    
    // Station details components
    private JLabel stationNameLabel;
    private JLabel stationTypeLabel;
    private JLabel stationStatusLabel;
    private JLabel agentCountLabel;
    private JLabel taskCountLabel;
    private JTextArea stationDescriptionArea;
    private JProgressBar resourceUsageBar;
    private JPanel statsPanel;
    
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
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(stationNameLabel, gbc);
        
        // Station type
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Type:"), gbc);
        stationTypeLabel = new JLabel("-");
        gbc.gridx = 1;
        panel.add(stationTypeLabel, gbc);
        
        // Station status
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Status:"), gbc);
        stationStatusLabel = new JLabel("-");
        gbc.gridx = 1;
        panel.add(stationStatusLabel, gbc);
        
        // Agent count
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Active Agents:"), gbc);
        agentCountLabel = new JLabel("0");
        gbc.gridx = 1;
        panel.add(agentCountLabel, gbc);
        
        // Task count
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Running Tasks:"), gbc);
        taskCountLabel = new JLabel("0");
        gbc.gridx = 1;
        panel.add(taskCountLabel, gbc);
        
        // Resource usage
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(new JLabel("Resource Usage:"), gbc);
        resourceUsageBar = new JProgressBar(0, 100);
        resourceUsageBar.setStringPainted(true);
        gbc.gridx = 1;
        panel.add(resourceUsageBar, gbc);
        
        return panel;
    }
    
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
    
    private JPanel createAgentDeploymentTab() {
        agentDeploymentPanel = new AgentDeploymentPanel(project);
        return agentDeploymentPanel;
    }
    
    private JPanel createMonitoringTab() {
        stationMonitoringPanel = new StationMonitoringPanel(project);
        return stationMonitoringPanel;
    }
    
    private JPanel createControlTab() {
        stationControlPanel = new StationControlPanel(project);
        return stationControlPanel;
    }
    
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
    
    private void updateStatistics(Station station) {
        // Update statistics cards in the stats panel
        if (statsPanel != null && statsPanel.getComponentCount() > 0) {
            // This would update the actual statistics based on station data
            // For now, using placeholder updates
        }
    }
    
    private void loadStations() {
        List<Station> stations = stationManager.getAllStations();
        stationListModel.removeAll();
        stationListModel.add(stations);
        
        if (!stations.isEmpty()) {
            stationList.setSelectedIndex(0);
        }
    }
    
    private void addNewStation() {
        StationCreationDialog dialog = new StationCreationDialog(project);
        if (dialog.showAndGet()) {
            Station newStation = dialog.createStation();
            stationManager.registerStation(newStation);
            loadStations();
        }
    }
    
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
    
    private void filterStations(String status) {
        // Implement station filtering logic
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
    
    private void saveConfiguration() {
        Station selected = stationList.getSelectedValue();
        if (selected != null) {
            // Save configuration changes
            Messages.showInfoMessage(project, "Configuration saved successfully", "Success");
        }
    }
    
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
    
    private void showSettings() {
        // Show global settings dialog
        DialogBuilder builder = new DialogBuilder(project);
        builder.setTitle("Station Management Settings");
        builder.setCenterPanel(new StationSettingsPanel());
        builder.show();
    }
    
    public void dispose() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }
    
    // Custom cell renderer for station list
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
}