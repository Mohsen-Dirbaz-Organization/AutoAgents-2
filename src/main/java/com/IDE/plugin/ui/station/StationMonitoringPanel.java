package com.IDE.plugin.ui.station;

import com.IDE.plugin.core.station.Station;
import com.IDE.plugin.core.station.StationMetrics;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Real-time monitoring dashboard for stations
 */
public class StationMonitoringPanel extends JPanel {
    private final Project project;
    private Station currentStation;
    private Timer refreshTimer;
    
    // Charts and data
    private TimeSeries cpuSeries;
    private TimeSeries memorySeries;
    private TimeSeries taskSeries;
    private DefaultCategoryDataset agentStatusDataset;
    private JFreeChart performanceChart;
    private JFreeChart resourceChart;
    private JFreeChart agentChart;
    private JFreeChart taskDistributionChart;
    
    // Tables
    private JBTable agentTable;
    private DefaultTableModel agentTableModel;
    private JBTable taskTable;
    private DefaultTableModel taskTableModel;
    private JBTable eventTable;
    private DefaultTableModel eventTableModel;
    
    // Status indicators
    private JLabel stationHealthLabel;
    private JProgressBar cpuUsageBar;
    private JProgressBar memoryUsageBar;
    private JProgressBar diskUsageBar;
    private JLabel uptimeLabel;
    private JLabel totalTasksLabel;
    private JLabel successRateLabel;
    private JLabel avgResponseTimeLabel;
    
    // Alert panel
    private JTextArea alertArea;
    private final Queue<String> alertQueue;
    private JLabel alertCountLabel;
    
    // Monitoring settings
    private JSpinner refreshIntervalSpinner;
    private JCheckBox autoRefreshCheckbox;
    private JComboBox<String> chartTypeCombo;
    private JSlider dataPointsSlider;
    
    public StationMonitoringPanel(Project project) {
        this.project = project;
        this.alertQueue = new ConcurrentLinkedQueue<>();
        
        initializeChartData();
        initializeUI();
        setupRefreshTimer();
    }
    
    private void initializeChartData() {
        cpuSeries = new TimeSeries("CPU Usage");
        memorySeries = new TimeSeries("Memory Usage");
        taskSeries = new TimeSeries("Active Tasks");
        agentStatusDataset = new DefaultCategoryDataset();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(10));
        
        // Create tabbed pane for different monitoring views
        JBTabbedPane tabbedPane = new JBTabbedPane();
        
        tabbedPane.addTab("Dashboard", createDashboardPanel());
        tabbedPane.addTab("Performance", createPerformancePanel());
        tabbedPane.addTab("Agents", createAgentsPanel());
        tabbedPane.addTab("Tasks", createTasksPanel());
        tabbedPane.addTab("Events", createEventsPanel());
        tabbedPane.addTab("Alerts", createAlertsPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Bottom control panel
        add(createControlPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(10));
        
        // Top status panel
        JPanel statusPanel = createStatusPanel();
        panel.add(statusPanel, BorderLayout.NORTH);
        
        // Center - Charts grid
        JPanel chartsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        
        // Create mini charts
        performanceChart = createPerformanceChart();
        chartsPanel.add(new ChartPanel(performanceChart));
        
        resourceChart = createResourceChart();
        chartsPanel.add(new ChartPanel(resourceChart));
        
        agentChart = createAgentStatusChart();
        chartsPanel.add(new ChartPanel(agentChart));
        
        taskDistributionChart = createTaskDistributionChart();
        chartsPanel.add(new ChartPanel(taskDistributionChart));
        
        panel.add(chartsPanel, BorderLayout.CENTER);
        
        // Bottom - Quick stats
        panel.add(createQuickStatsPanel(), BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Station Status"));
        panel.setBackground(UIUtil.getPanelBackground());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(5);
        
        // Health status
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Health:"), gbc);
        
        stationHealthLabel = new JLabel("Good");
        stationHealthLabel.setFont(stationHealthLabel.getFont().deriveFont(Font.BOLD));
        stationHealthLabel.setForeground(JBColor.GREEN);
        gbc.gridx = 1;
        panel.add(stationHealthLabel, gbc);
        
        // Uptime
        gbc.gridx = 2;
        gbc.gridy = 0;
        panel.add(new JLabel("Uptime:"), gbc);
        
        uptimeLabel = new JLabel("0h 0m");
        gbc.gridx = 3;
        panel.add(uptimeLabel, gbc);
        
        // Resource usage bars
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        panel.add(createResourceBarsPanel(), gbc);
        
        return panel;
    }
    
    private JPanel createResourceBarsPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        
        // CPU usage
        JPanel cpuPanel = new JPanel(new BorderLayout());
        cpuPanel.add(new JLabel("CPU:"), BorderLayout.WEST);
        cpuUsageBar = new JProgressBar(0, 100);
        cpuUsageBar.setStringPainted(true);
        cpuPanel.add(cpuUsageBar, BorderLayout.CENTER);
        panel.add(cpuPanel);
        
        // Memory usage
        JPanel memPanel = new JPanel(new BorderLayout());
        memPanel.add(new JLabel("Memory:"), BorderLayout.WEST);
        memoryUsageBar = new JProgressBar(0, 100);
        memoryUsageBar.setStringPainted(true);
        memPanel.add(memoryUsageBar, BorderLayout.CENTER);
        panel.add(memPanel);
        
        // Disk usage
        JPanel diskPanel = new JPanel(new BorderLayout());
        diskPanel.add(new JLabel("Disk:"), BorderLayout.WEST);
        diskUsageBar = new JProgressBar(0, 100);
        diskUsageBar.setStringPainted(true);
        diskPanel.add(diskUsageBar, BorderLayout.CENTER);
        panel.add(diskPanel);
        
        return panel;
    }
    
    private JPanel createQuickStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Quick Stats"));
        
        // Total tasks
        JPanel tasksPanel = createStatCard("Total Tasks", totalTasksLabel = new JLabel("0"));
        panel.add(tasksPanel);
        
        // Success rate
        JPanel successPanel = createStatCard("Success Rate", successRateLabel = new JLabel("0%"));
        panel.add(successPanel);
        
        // Average response time
        JPanel responsePanel = createStatCard("Avg Response", avgResponseTimeLabel = new JLabel("0ms"));
        panel.add(responsePanel);
        
        // Alert count
        JPanel alertPanel = createStatCard("Active Alerts", alertCountLabel = new JLabel("0"));
        panel.add(alertPanel);
        
        return panel;
    }
    
    private JPanel createStatCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(UIUtil.getBorderColor()));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(JBUI.Borders.empty(5));
        card.add(titleLabel, BorderLayout.NORTH);
        
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 16f));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createPerformancePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(10));
        
        // Create detailed performance chart
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(cpuSeries);
        dataset.addSeries(memorySeries);
        dataset.addSeries(taskSeries);
        
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            "Performance Metrics",
            "Time",
            "Value",
            dataset,
            true,
            true,
            false
        );
        
        ChartPanel chartPanel = new ChartPanel(chart);
        panel.add(chartPanel, BorderLayout.CENTER);
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Chart Options"));
        
        controlPanel.add(new JLabel("Type:"));
        chartTypeCombo = new JComboBox<>(new String[]{"Line", "Area", "Bar"});
        controlPanel.add(chartTypeCombo);
        
        controlPanel.add(new JLabel("Data Points:"));
        dataPointsSlider = new JSlider(50, 500, 200);
        dataPointsSlider.setPreferredSize(new Dimension(150, 30));
        controlPanel.add(dataPointsSlider);
        
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createAgentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(10));
        
        // Agent table
        String[] columns = {"Agent ID", "Name", "Type", "Status", "Tasks", "CPU%", "Memory%", "Uptime"};
        agentTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        agentTable = new JBTable(agentTableModel);
        agentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        agentTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JBScrollPane(agentTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Agent actions panel
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionsPanel.setBorder(BorderFactory.createTitledBorder("Agent Actions"));
        
        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> startSelectedAgent());
        actionsPanel.add(startButton);
        
        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(e -> stopSelectedAgent());
        actionsPanel.add(stopButton);
        
        JButton restartButton = new JButton("Restart");
        restartButton.addActionListener(e -> restartSelectedAgent());
        actionsPanel.add(restartButton);
        
        JButton detailsButton = new JButton("View Details");
        detailsButton.addActionListener(e -> showAgentDetails());
        actionsPanel.add(detailsButton);
        
        panel.add(actionsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTasksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(10));
        
        // Task table
        String[] columns = {"Task ID", "Type", "Agent", "Status", "Progress", "Started", "Duration", "Priority"};
        taskTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        taskTable = new JBTable(taskTableModel);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.setRowHeight(25);
        
        // Add progress bar renderer for progress column
        taskTable.getColumnModel().getColumn(4).setCellRenderer(new ProgressCellRenderer());
        
        JScrollPane scrollPane = new JBScrollPane(taskTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Task filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter"));
        
        filterPanel.add(new JLabel("Status:"));
        JComboBox<String> statusFilter = new JComboBox<>(new String[]{
            "All", "Running", "Completed", "Failed", "Queued"
        });
        filterPanel.add(statusFilter);
        
        filterPanel.add(new JLabel("Agent:"));
        JComboBox<String> agentFilter = new JComboBox<>(new String[]{"All"});
        filterPanel.add(agentFilter);
        
        JButton applyFilterButton = new JButton("Apply");
        filterPanel.add(applyFilterButton);
        
        panel.add(filterPanel, BorderLayout.NORTH);
        
        return panel;
    }
    
    private JPanel createEventsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(10));
        
        // Event table
        String[] columns = {"Timestamp", "Level", "Source", "Event", "Details"};
        eventTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        eventTable = new JBTable(eventTableModel);
        eventTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        eventTable.setRowHeight(25);
        
        // Color code by level
        eventTable.setDefaultRenderer(Object.class, new EventTableCellRenderer());
        
        JScrollPane scrollPane = new JBScrollPane(eventTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Event controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Event Controls"));
        
        controlPanel.add(new JLabel("Level:"));
        JComboBox<String> levelFilter = new JComboBox<>(new String[]{
            "All", "Debug", "Info", "Warning", "Error"
        });
        controlPanel.add(levelFilter);
        
        JCheckBox autoScrollCheckbox = new JCheckBox("Auto-scroll", true);
        controlPanel.add(autoScrollCheckbox);
        
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> eventTableModel.setRowCount(0));
        controlPanel.add(clearButton);
        
        JButton exportButton = new JButton("Export");
        exportButton.addActionListener(e -> exportEvents());
        controlPanel.add(exportButton);
        
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createAlertsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(JBUI.Borders.empty(10));
        
        // Alert area
        alertArea = new JTextArea();
        alertArea.setEditable(false);
        alertArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JBScrollPane(alertArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Alert controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Alert Settings"));
        
        JCheckBox cpuAlertCheckbox = new JCheckBox("CPU > 80%", true);
        controlPanel.add(cpuAlertCheckbox);
        
        JCheckBox memoryAlertCheckbox = new JCheckBox("Memory > 80%", true);
        controlPanel.add(memoryAlertCheckbox);
        
        JCheckBox taskFailureCheckbox = new JCheckBox("Task Failures", true);
        controlPanel.add(taskFailureCheckbox);
        
        JCheckBox agentDownCheckbox = new JCheckBox("Agent Down", true);
        controlPanel.add(agentDownCheckbox);
        
        JButton clearAlertsButton = new JButton("Clear Alerts");
        clearAlertsButton.addActionListener(e -> clearAlerts());
        controlPanel.add(clearAlertsButton);
        
        JButton configureButton = new JButton("Configure");
        configureButton.addActionListener(e -> configureAlerts());
        controlPanel.add(configureButton);
        
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtil.getBorderColor()));
        
        // Refresh controls
        autoRefreshCheckbox = new JCheckBox("Auto-refresh", true);
        panel.add(autoRefreshCheckbox);
        
        panel.add(new JLabel("Interval (sec):"));
        refreshIntervalSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 60, 1));
        refreshIntervalSpinner.addChangeListener(e -> updateRefreshInterval());
        panel.add(refreshIntervalSpinner);
        
        JButton refreshButton = new JButton("Refresh Now");
        refreshButton.addActionListener(e -> refreshMonitoringData());
        panel.add(refreshButton);
        
        JButton exportButton = new JButton("Export Data");
        exportButton.addActionListener(e -> exportMonitoringData());
        panel.add(exportButton);
        
        return panel;
    }
    
    private JFreeChart createPerformanceChart() {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(cpuSeries);
        dataset.addSeries(memorySeries);
        
        return ChartFactory.createTimeSeriesChart(
            "Performance",
            "Time",
            "Usage %",
            dataset,
            true,
            false,
            false
        );
    }
    
    private JFreeChart createResourceChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.setValue(0, "Used", "CPU");
        dataset.setValue(0, "Used", "Memory");
        dataset.setValue(0, "Used", "Disk");
        
        return ChartFactory.createBarChart(
            "Resource Usage",
            "Resource",
            "Usage %",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            false,
            false
        );
    }
    
    private JFreeChart createAgentStatusChart() {
        return ChartFactory.createPieChart(
            "Agent Status",
            agentStatusDataset,
            true,
            false,
            false
        );
    }
    
    private JFreeChart createTaskDistributionChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.setValue(0, "Tasks", "Queued");
        dataset.setValue(0, "Tasks", "Running");
        dataset.setValue(0, "Tasks", "Completed");
        dataset.setValue(0, "Tasks", "Failed");
        
        return ChartFactory.createBarChart(
            "Task Distribution",
            "Status",
            "Count",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            false,
            false
        );
    }
    
    public void setStation(Station station) {
        this.currentStation = station;
        refreshMonitoringData();
    }
    
    private void setupRefreshTimer() {
        refreshTimer = new Timer(5000, e -> {
            if (autoRefreshCheckbox.isSelected()) {
                refreshMonitoringData();
            }
        });
        refreshTimer.start();
    }
    
    private void updateRefreshInterval() {
        int interval = (Integer) refreshIntervalSpinner.getValue() * 1000;
        refreshTimer.setDelay(interval);
    }
    
    private void refreshMonitoringData() {
        if (currentStation == null) return;
        
        SwingUtilities.invokeLater(() -> {
            // Update metrics
            StationMetrics metrics = currentStation.getMetrics();
            updateStatusIndicators(metrics);
            updateCharts(metrics);
            updateTables(metrics);
            checkAlerts(metrics);
        });
    }
    
    private void updateStatusIndicators(StationMetrics metrics) {
        // Update health status
        String health = metrics.getHealthStatus();
        stationHealthLabel.setText(health);
        stationHealthLabel.setForeground(getHealthColor(health));
        
        // Update resource bars
        cpuUsageBar.setValue(metrics.getCpuUsage());
        memoryUsageBar.setValue(metrics.getMemoryUsage());
        diskUsageBar.setValue(metrics.getDiskUsage());
        
        // Update stats
        uptimeLabel.setText(formatUptime(metrics.getUptime()));
        totalTasksLabel.setText(String.valueOf(metrics.getTotalTasks()));
        successRateLabel.setText(String.format("%.1f%%", metrics.getSuccessRate()));
        avgResponseTimeLabel.setText(metrics.getAverageResponseTime() + "ms");
        alertCountLabel.setText(String.valueOf(alertQueue.size()));
    }
    
    private void updateCharts(StationMetrics metrics) {
        // Update time series data
        Second now = new Second();
        cpuSeries.addOrUpdate(now, metrics.getCpuUsage());
        memorySeries.addOrUpdate(now, metrics.getMemoryUsage());
        taskSeries.addOrUpdate(now, metrics.getActiveTasks());
        
        // Update agent status pie chart
        agentStatusDataset.clear();
        agentStatusDataset.setValue("Running", metrics.getRunningAgents());
        agentStatusDataset.setValue("Idle", metrics.getIdleAgents());
        agentStatusDataset.setValue("Error", metrics.getErrorAgents());
    }
    
    private void updateTables(StationMetrics metrics) {
        // Update agent table
        updateAgentTable(metrics.getAgentMetrics());
        
        // Update task table
        updateTaskTable(metrics.getTaskMetrics());
        
        // Update event table
        updateEventTable(metrics.getRecentEvents());
    }
    
    private void updateAgentTable(List<Map<String, Object>> agentMetrics) {
        agentTableModel.setRowCount(0);
        for (Map<String, Object> agent : agentMetrics) {
            agentTableModel.addRow(new Object[]{
                agent.get("id"),
                agent.get("name"),
                agent.get("type"),
                agent.get("status"),
                agent.get("tasks"),
                agent.get("cpu"),
                agent.get("memory"),
                agent.get("uptime")
            });
        }
    }
    
    private void updateTaskTable(List<Map<String, Object>> taskMetrics) {
        taskTableModel.setRowCount(0);
        for (Map<String, Object> task : taskMetrics) {
            taskTableModel.addRow(new Object[]{
                task.get("id"),
                task.get("type"),
                task.get("agent"),
                task.get("status"),
                task.get("progress"),
                task.get("started"),
                task.get("duration"),
                task.get("priority")
            });
        }
    }
    
    private void updateEventTable(List<Map<String, Object>> events) {
        for (Map<String, Object> event : events) {
            eventTableModel.insertRow(0, new Object[]{
                event.get("timestamp"),
                event.get("level"),
                event.get("source"),
                event.get("event"),
                event.get("details")
            });
        }
        
        // Limit table size
        while (eventTableModel.getRowCount() > 1000) {
            eventTableModel.removeRow(eventTableModel.getRowCount() - 1);
        }
    }
    
    private void checkAlerts(StationMetrics metrics) {
        // Check various alert conditions
        if (metrics.getCpuUsage() > 80) {
            addAlert("High CPU usage: " + metrics.getCpuUsage() + "%");
        }
        
        if (metrics.getMemoryUsage() > 80) {
            addAlert("High memory usage: " + metrics.getMemoryUsage() + "%");
        }
        
        if (metrics.getFailedTasks() > 10) {
            addAlert("High number of failed tasks: " + metrics.getFailedTasks());
        }
    }
    
    private void addAlert(String message) {
        String timestamp = new Date().toString();
        String alert = "[" + timestamp + "] " + message + "\n";
        alertQueue.offer(alert);
        alertArea.append(alert);
        
        // Keep only recent alerts
        while (alertQueue.size() > 100) {
            alertQueue.poll();
        }
    }
    
    private Color getHealthColor(String health) {
        switch (health.toLowerCase()) {
            case "good":
                return JBColor.GREEN;
            case "warning":
                return JBColor.ORANGE;
            case "critical":
                return JBColor.RED;
            default:
                return UIUtil.getLabelForeground();
        }
    }
    
    private String formatUptime(long uptimeMillis) {
        long hours = uptimeMillis / 3600000;
        long minutes = (uptimeMillis % 3600000) / 60000;
        return String.format("%dh %dm", hours, minutes);
    }
    
    private void startSelectedAgent() {
        // Start selected agent
        int row = agentTable.getSelectedRow();
        if (row >= 0) {
            String agentId = (String) agentTableModel.getValueAt(row, 0);
            // Implement agent start logic
        }
    }
    
    private void stopSelectedAgent() {
        // Stop selected agent
        int row = agentTable.getSelectedRow();
        if (row >= 0) {
            String agentId = (String) agentTableModel.getValueAt(row, 0);
            // Implement agent stop logic
        }
    }
    
    private void restartSelectedAgent() {
        // Restart selected agent
        int row = agentTable.getSelectedRow();
        if (row >= 0) {
            String agentId = (String) agentTableModel.getValueAt(row, 0);
            // Implement agent restart logic
        }
    }
    
    private void showAgentDetails() {
        // Show detailed agent information
        int row = agentTable.getSelectedRow();
        if (row >= 0) {
            String agentId = (String) agentTableModel.getValueAt(row, 0);
            // Show agent details dialog
        }
    }
    
    private void exportEvents() {
        // Export events to file
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            // Export logic
        }
    }
    
    private void clearAlerts() {
        alertQueue.clear();
        alertArea.setText("");
        alertCountLabel.setText("0");
    }
    
    private void configureAlerts() {
        // Show alert configuration dialog
    }
    
    private void exportMonitoringData() {
        // Export monitoring data
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            // Export logic
        }
    }
    
    public void dispose() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }
    
    // Custom cell renderers
    private static class ProgressCellRenderer extends DefaultTableCellRenderer {
        private final JProgressBar progressBar = new JProgressBar(0, 100);
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            if (value instanceof Integer) {
                progressBar.setValue((Integer) value);
                progressBar.setStringPainted(true);
                return progressBar;
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
    
    private static class EventTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected && column == 1) { // Level column
                String level = (String) value;
                if ("Error".equalsIgnoreCase(level)) {
                    c.setForeground(JBColor.RED);
                } else if ("Warning".equalsIgnoreCase(level)) {
                    c.setForeground(JBColor.ORANGE);
                }
            }
            
            return c;
        }
    }
}