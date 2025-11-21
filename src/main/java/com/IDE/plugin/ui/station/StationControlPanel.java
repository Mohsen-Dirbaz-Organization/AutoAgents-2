package com.IDE.plugin.ui.station;

import com.IDE.plugin.core.station.Station;
import com.IDE.plugin.core.task.Task;
import com.IDE.plugin.core.task.TaskPriority;
import com.IDE.plugin.core.task.TaskStatus;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Station control and task management UI
 */
public class StationControlPanel extends JPanel {
    private final Project project;
    private Station currentStation;
    
    // Task queue components
    private JBTable taskQueueTable;
    private DefaultTableModel taskQueueModel;
    private JBTable runningTasksTable;
    private DefaultTableModel runningTasksModel;
    private JBTable completedTasksTable;
    private DefaultTableModel completedTasksModel;
    
    // Task creation components
    private JTextField taskNameField;
    private ComboBox<String> taskTypeCombo;
    private JTextArea taskDescriptionArea;
    private ComboBox<TaskPriority> priorityCombo;
    private JSpinner taskTimeoutSpinner;
    private JCheckBox autoRetryCheckbox;
    private JSpinner retryCountSpinner;
    
    // Control components
    private JButton startStationButton;
    private JButton stopStationButton;
    private JButton pauseStationButton;
    private JButton resumeStationButton;
    private JLabel stationStatusLabel;
    private JProgressBar taskProgressBar;
    
    // Task assignment
    private ComboBox<String> agentAssignmentCombo;
    private JCheckBox autoAssignCheckbox;
    private JPanel taskParametersPanel;
    private Map<String, JComponent> parameterFields;
    
    // Batch operations
    private JCheckBox selectAllCheckbox;
    private JButton batchStartButton;
    private JButton batchStopButton;
    private JButton batchDeleteButton;
    
    public StationControlPanel(Project project) {
        this.project = project;
        this.parameterFields = new ConcurrentHashMap<>();
        
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(10));
        
        // Top control panel
        JPanel topPanel = createTopControlPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center - Task management
        JPanel centerPanel = createTaskManagementPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom - Task creation
        JPanel bottomPanel = createTaskCreationPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Station Control"));
        
        // Station controls
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        startStationButton = new JButton("Start");
        startStationButton.addActionListener(e -> startStation());
        controlsPanel.add(startStationButton);
        
        stopStationButton = new JButton("Stop");
        stopStationButton.addActionListener(e -> stopStation());
        controlsPanel.add(stopStationButton);
        
        pauseStationButton = new JButton("Pause");
        pauseStationButton.addActionListener(e -> pauseStation());
        controlsPanel.add(pauseStationButton);
        
        resumeStationButton = new JButton("Resume");
        resumeStationButton.addActionListener(e -> resumeStation());
        resumeStationButton.setEnabled(false);
        controlsPanel.add(resumeStationButton);
        
        controlsPanel.add(new JSeparator(JSeparator.VERTICAL));
        
        controlsPanel.add(new JLabel("Status:"));
        stationStatusLabel = new JLabel("Stopped");
        stationStatusLabel.setFont(stationStatusLabel.getFont().deriveFont(Font.BOLD));
        controlsPanel.add(stationStatusLabel);
        
        panel.add(controlsPanel, BorderLayout.WEST);
        
        // Task progress
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBorder(JBUI.Borders.empty(5));
        taskProgressBar = new JProgressBar();
        taskProgressBar.setStringPainted(true);
        taskProgressBar.setString("No active tasks");
        progressPanel.add(taskProgressBar, BorderLayout.CENTER);
        
        panel.add(progressPanel, BorderLayout.CENTER);
        
        // Quick stats
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statsPanel.add(new JLabel("Queue:"));
        JLabel queueSizeLabel = new JLabel("0");
        statsPanel.add(queueSizeLabel);
        statsPanel.add(new JLabel("Running:"));
        JLabel runningSizeLabel = new JLabel("0");
        statsPanel.add(runningSizeLabel);
        statsPanel.add(new JLabel("Completed:"));
        JLabel completedSizeLabel = new JLabel("0");
        statsPanel.add(completedSizeLabel);
        
        panel.add(statsPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createTaskManagementPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Task queue tab
        tabbedPane.addTab("Task Queue", createTaskQueuePanel());
        
        // Running tasks tab
        tabbedPane.addTab("Running Tasks", createRunningTasksPanel());
        
        // Completed tasks tab
        tabbedPane.addTab("Completed Tasks", createCompletedTasksPanel());
        
        // Task templates tab
        tabbedPane.addTab("Templates", createTaskTemplatesPanel());
        
        return tabbedPane;
    }
    
    private JPanel createTaskQueuePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Task queue table
        String[] columns = {"", "ID", "Name", "Type", "Priority", "Agent", "Created", "Scheduled"};
        taskQueueModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : String.class;
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };
        
        taskQueueTable = new JBTable(taskQueueModel);
        taskQueueTable.getColumnModel().getColumn(0).setMaxWidth(30);
        taskQueueTable.setRowHeight(25);
        configureTableRenderers(taskQueueTable);
        
        JScrollPane scrollPane = new JBScrollPane(taskQueueTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Control panel
        JPanel controlPanel = createQueueControlPanel();
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createQueueControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        // Left - Batch operations
        JPanel batchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        selectAllCheckbox = new JCheckBox("Select All");
        selectAllCheckbox.addActionListener(e -> selectAllTasks());
        batchPanel.add(selectAllCheckbox);
        
        batchPanel.add(new JSeparator(JSeparator.VERTICAL));
        
        batchStartButton = new JButton("Start Selected");
        batchStartButton.addActionListener(e -> startSelectedTasks());
        batchPanel.add(batchStartButton);
        
        batchStopButton = new JButton("Cancel Selected");
        batchStopButton.addActionListener(e -> cancelSelectedTasks());
        batchPanel.add(batchStopButton);
        
        batchDeleteButton = new JButton("Delete Selected");
        batchDeleteButton.addActionListener(e -> deleteSelectedTasks());
        batchPanel.add(batchDeleteButton);
        
        panel.add(batchPanel, BorderLayout.WEST);
        
        // Right - Queue actions
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton moveUpButton = new JButton("Move Up");
        moveUpButton.addActionListener(e -> moveTaskUp());
        actionsPanel.add(moveUpButton);
        
        JButton moveDownButton = new JButton("Move Down");
        moveDownButton.addActionListener(e -> moveTaskDown());
        actionsPanel.add(moveDownButton);
        
        JButton editButton = new JButton("Edit");
        editButton.addActionListener(e -> editSelectedTask());
        actionsPanel.add(editButton);
        
        panel.add(actionsPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createRunningTasksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Running tasks table
        String[] columns = {"ID", "Name", "Type", "Agent", "Progress", "Started", "Duration", "Actions"};
        runningTasksModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Actions column
            }
        };
        
        runningTasksTable = new JBTable(runningTasksModel);
        runningTasksTable.setRowHeight(30);
        
        // Progress renderer
        runningTasksTable.getColumnModel().getColumn(4).setCellRenderer(new ProgressCellRenderer());
        
        // Actions renderer
        runningTasksTable.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer());
        runningTasksTable.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor(new JCheckBox()));
        
        JScrollPane scrollPane = new JBScrollPane(runningTasksTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton pauseAllButton = new JButton("Pause All");
        pauseAllButton.addActionListener(e -> pauseAllTasks());
        controlPanel.add(pauseAllButton);
        
        JButton resumeAllButton = new JButton("Resume All");
        resumeAllButton.addActionListener(e -> resumeAllTasks());
        controlPanel.add(resumeAllButton);
        
        JButton stopAllButton = new JButton("Stop All");
        stopAllButton.addActionListener(e -> stopAllTasks());
        controlPanel.add(stopAllButton);
        
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createCompletedTasksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Completed tasks table
        String[] columns = {"ID", "Name", "Type", "Status", "Started", "Completed", "Duration", "Result"};
        completedTasksModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        completedTasksTable = new JBTable(completedTasksModel);
        completedTasksTable.setRowHeight(25);
        configureTableRenderers(completedTasksTable);
        
        JScrollPane scrollPane = new JBScrollPane(completedTasksTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Filter and control panel
        JPanel controlPanel = new JPanel(new BorderLayout());
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Status:"));
        JComboBox<String> statusFilter = new JComboBox<>(new String[]{
            "All", "Success", "Failed", "Cancelled"
        });
        filterPanel.add(statusFilter);
        
        filterPanel.add(new JLabel("Period:"));
        JComboBox<String> periodFilter = new JComboBox<>(new String[]{
            "Last Hour", "Last 24 Hours", "Last Week", "All Time"
        });
        filterPanel.add(periodFilter);
        
        controlPanel.add(filterPanel, BorderLayout.WEST);
        
        // Action panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton rerunButton = new JButton("Rerun");
        rerunButton.addActionListener(e -> rerunSelectedTask());
        actionPanel.add(rerunButton);
        
        JButton viewDetailsButton = new JButton("View Details");
        viewDetailsButton.addActionListener(e -> viewTaskDetails());
        actionPanel.add(viewDetailsButton);
        
        JButton clearButton = new JButton("Clear History");
        clearButton.addActionListener(e -> clearCompletedTasks());
        actionPanel.add(clearButton);
        
        controlPanel.add(actionPanel, BorderLayout.EAST);
        
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTaskTemplatesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Templates list
        CollectionListModel<String> templatesModel = new CollectionListModel<>();
        templatesModel.add(Arrays.asList(
            "Code Analysis Task",
            "Build Task",
            "Unit Test Task",
            "Integration Test Task",
            "Deployment Task",
            "Backup Task",
            "Cleanup Task"
        ));
        
        JBList<String> templatesList = new JBList<>(templatesModel);
        
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(templatesList)
                .setAddAction(button -> createNewTemplate())
                .setRemoveAction(button -> deleteSelectedTemplate())
                .setEditAction(button -> editSelectedTemplate());
        
        panel.add(decorator.createPanel(), BorderLayout.CENTER);
        
        // Template actions
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JButton useTemplateButton = new JButton("Use Selected Template");
        useTemplateButton.addActionListener(e -> useSelectedTemplate());
        actionsPanel.add(useTemplateButton);
        
        JButton importButton = new JButton("Import");
        importButton.addActionListener(e -> importTemplates());
        actionsPanel.add(importButton);
        
        JButton exportButton = new JButton("Export");
        exportButton.addActionListener(e -> exportTemplates());
        actionsPanel.add(exportButton);
        
        panel.add(actionsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTaskCreationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Create New Task"));
        
        // Task form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(5);
        
        // Task name
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Task Name:"), gbc);
        
        taskNameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formPanel.add(taskNameField, gbc);
        
        // Task type
        gbc.gridx = 2;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Type:"), gbc);
        
        taskTypeCombo = new ComboBox<>(new String[]{
            "Analysis", "Build", "Test", "Deploy", "Monitor", "Custom"
        });
        gbc.gridx = 3;
        formPanel.add(taskTypeCombo, gbc);
        
        // Priority
        gbc.gridx = 4;
        formPanel.add(new JLabel("Priority:"), gbc);
        
        priorityCombo = new ComboBox<>(TaskPriority.values());
        gbc.gridx = 5;
        formPanel.add(priorityCombo, gbc);
        
        // Description
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Description:"), gbc);
        
        taskDescriptionArea = new JTextArea(2, 40);
        taskDescriptionArea.setLineWrap(true);
        gbc.gridx = 1;
        gbc.gridwidth = 5;
        formPanel.add(new JBScrollPane(taskDescriptionArea), gbc);
        
        // Agent assignment
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Assign to:"), gbc);
        
        agentAssignmentCombo = new ComboBox<>(new String[]{"Auto-assign", "Agent 1", "Agent 2"});
        gbc.gridx = 1;
        formPanel.add(agentAssignmentCombo, gbc);
        
        autoAssignCheckbox = new JCheckBox("Auto-assign based on load");
        gbc.gridx = 2;
        gbc.gridwidth = 2;
        formPanel.add(autoAssignCheckbox, gbc);
        
        // Timeout
        gbc.gridx = 4;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Timeout (sec):"), gbc);
        
        taskTimeoutSpinner = new JSpinner(new SpinnerNumberModel(300, 30, 3600, 30));
        gbc.gridx = 5;
        formPanel.add(taskTimeoutSpinner, gbc);
        
        // Retry settings
        gbc.gridx = 0;
        gbc.gridy = 3;
        autoRetryCheckbox = new JCheckBox("Auto-retry on failure");
        formPanel.add(autoRetryCheckbox, gbc);
        
        gbc.gridx = 1;
        formPanel.add(new JLabel("Max retries:"), gbc);
        
        retryCountSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        gbc.gridx = 2;
        formPanel.add(retryCountSpinner, gbc);
        
        panel.add(formPanel, BorderLayout.CENTER);
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton addToQueueButton = new JButton("Add to Queue");
        addToQueueButton.addActionListener(e -> addTaskToQueue());
        buttonPanel.add(addToQueueButton);
        
        JButton executeNowButton = new JButton("Execute Now");
        executeNowButton.addActionListener(e -> executeTaskNow());
        buttonPanel.add(executeNowButton);
        
        JButton scheduleButton = new JButton("Schedule");
        scheduleButton.addActionListener(e -> scheduleTask());
        buttonPanel.add(scheduleButton);
        
        panel.add(buttonPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void configureTableRenderers(JTable table) {
        // Custom renderer for priority column
        int priorityColumn = getColumnIndex(table, "Priority");
        if (priorityColumn >= 0) {
            table.getColumnModel().getColumn(priorityColumn).setCellRenderer(new PriorityCellRenderer());
        }
        
        // Custom renderer for status column
        int statusColumn = getColumnIndex(table, "Status");
        if (statusColumn >= 0) {
            table.getColumnModel().getColumn(statusColumn).setCellRenderer(new StatusCellRenderer());
        }
    }
    
    private int getColumnIndex(JTable table, String columnName) {
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (table.getColumnName(i).equals(columnName)) {
                return i;
            }
        }
        return -1;
    }
    
    public void setStation(Station station) {
        this.currentStation = station;
        updateStationStatus();
        refreshTaskLists();
    }
    
    private void updateStationStatus() {
        if (currentStation != null) {
            stationStatusLabel.setText(currentStation.getStatus().toString());
            updateControlButtons();
        }
    }
    
    private void updateControlButtons() {
        if (currentStation == null) return;
        
        switch (currentStation.getStatus()) {
            case RUNNING:
                startStationButton.setEnabled(false);
                stopStationButton.setEnabled(true);
                pauseStationButton.setEnabled(true);
                resumeStationButton.setEnabled(false);
                break;
            case STOPPED:
                startStationButton.setEnabled(true);
                stopStationButton.setEnabled(false);
                pauseStationButton.setEnabled(false);
                resumeStationButton.setEnabled(false);
                break;
            case PAUSED:
                startStationButton.setEnabled(false);
                stopStationButton.setEnabled(true);
                pauseStationButton.setEnabled(false);
                resumeStationButton.setEnabled(true);
                break;
        }
    }
    
    private void refreshTaskLists() {
        if (currentStation == null) return;
        
        // Refresh task queue
        refreshTaskQueue();
        
        // Refresh running tasks
        refreshRunningTasks();
        
        // Refresh completed tasks
        refreshCompletedTasks();
    }
    
    private void refreshTaskQueue() {
        taskQueueModel.setRowCount(0);
        List<Task> queuedTasks = currentStation.getQueuedTasks();
        for (Task task : queuedTasks) {
            taskQueueModel.addRow(new Object[]{
                false,
                task.getId(),
                task.getName(),
                task.getType(),
                task.getPriority(),
                task.getAssignedAgent(),
                task.getCreatedTime(),
                task.getScheduledTime()
            });
        }
    }
    
    private void refreshRunningTasks() {
        runningTasksModel.setRowCount(0);
        List<Task> runningTasks = currentStation.getRunningTasks();
        for (Task task : runningTasks) {
            runningTasksModel.addRow(new Object[]{
                task.getId(),
                task.getName(),
                task.getType(),
                task.getAssignedAgent(),
                task.getProgress(),
                task.getStartTime(),
                task.getDuration(),
                "Actions"
            });
        }
    }
    
    private void refreshCompletedTasks() {
        completedTasksModel.setRowCount(0);
        List<Task> completedTasks = currentStation.getCompletedTasks();
        for (Task task : completedTasks) {
            completedTasksModel.addRow(new Object[]{
                task.getId(),
                task.getName(),
                task.getType(),
                task.getStatus(),
                task.getStartTime(),
                task.getEndTime(),
                task.getDuration(),
                task.getResult()
            });
        }
    }
    
    // Station control actions
    private void startStation() {
        if (currentStation != null) {
            currentStation.start();
            updateStationStatus();
        }
    }
    
    private void stopStation() {
        if (currentStation != null) {
            int result = Messages.showYesNoDialog(
                project,
                "Are you sure you want to stop the station? Running tasks will be cancelled.",
                "Confirm Stop",
                Messages.getWarningIcon()
            );
            
            if (result == Messages.YES) {
                currentStation.stop();
                updateStationStatus();
            }
        }
    }
    
    private void pauseStation() {
        if (currentStation != null) {
            currentStation.pause();
            updateStationStatus();
        }
    }
    
    private void resumeStation() {
        if (currentStation != null) {
            currentStation.resume();
            updateStationStatus();
        }
    }
    
    // Task management actions
    private void addTaskToQueue() {
        if (validateTaskForm()) {
            Task task = createTaskFromForm();
            currentStation.queueTask(task);
            clearTaskForm();
            refreshTaskQueue();
            Messages.showInfoMessage(project, "Task added to queue", "Success");
        }
    }
    
    private void executeTaskNow() {
        if (validateTaskForm()) {
            Task task = createTaskFromForm();
            task.setPriority(TaskPriority.URGENT);
            currentStation.executeTaskImmediately(task);
            clearTaskForm();
            refreshTaskLists();
        }
    }
    
    private void scheduleTask() {
        // Show scheduling dialog
        TaskSchedulingDialog dialog = new TaskSchedulingDialog(project);
        if (dialog.showAndGet()) {
            Task task = createTaskFromForm();
            task.setScheduledTime(dialog.getScheduledTime());
            currentStation.scheduleTask(task);
            clearTaskForm();
            refreshTaskQueue();
        }
    }
    
    private boolean validateTaskForm() {
        if (taskNameField.getText().trim().isEmpty()) {
            Messages.showErrorDialog(project, "Task name is required", "Validation Error");
            return false;
        }
        return true;
    }
    
    private Task createTaskFromForm() {
        Task task = new Task();
        task.setName(taskNameField.getText());
        task.setType((String) taskTypeCombo.getSelectedItem());
        task.setDescription(taskDescriptionArea.getText());
        task.setPriority((TaskPriority) priorityCombo.getSelectedItem());
        task.setTimeout((Integer) taskTimeoutSpinner.getValue());
        task.setAutoRetry(autoRetryCheckbox.isSelected());
        task.setMaxRetries((Integer) retryCountSpinner.getValue());
        
        if (!autoAssignCheckbox.isSelected()) {
            task.setAssignedAgent((String) agentAssignmentCombo.getSelectedItem());
        }
        
        return task;
    }
    
    private void clearTaskForm() {
        taskNameField.setText("");
        taskDescriptionArea.setText("");
        taskTypeCombo.setSelectedIndex(0);
        priorityCombo.setSelectedItem(TaskPriority.NORMAL);
        autoAssignCheckbox.setSelected(true);
        autoRetryCheckbox.setSelected(false);
    }
    
    // Batch operations
    private void selectAllTasks() {
        boolean selected = selectAllCheckbox.isSelected();
        for (int i = 0; i < taskQueueModel.getRowCount(); i++) {
            taskQueueModel.setValueAt(selected, i, 0);
        }
    }
    
    private void startSelectedTasks() {
        List<String> selectedIds = getSelectedTaskIds();
        if (!selectedIds.isEmpty()) {
            currentStation.startTasks(selectedIds);
            refreshTaskLists();
        }
    }
    
    private void cancelSelectedTasks() {
        List<String> selectedIds = getSelectedTaskIds();
        if (!selectedIds.isEmpty()) {
            currentStation.cancelTasks(selectedIds);
            refreshTaskLists();
        }
    }
    
    private void deleteSelectedTasks() {
        List<String> selectedIds = getSelectedTaskIds();
        if (!selectedIds.isEmpty()) {
            int result = Messages.showYesNoDialog(
                project,
                "Delete " + selectedIds.size() + " selected tasks?",
                "Confirm Delete",
                Messages.getWarningIcon()
            );
            
            if (result == Messages.YES) {
                currentStation.deleteTasks(selectedIds);
                refreshTaskLists();
            }
        }
    }
    
    private List<String> getSelectedTaskIds() {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < taskQueueModel.getRowCount(); i++) {
            if ((Boolean) taskQueueModel.getValueAt(i, 0)) {
                ids.add((String) taskQueueModel.getValueAt(i, 1));
            }
        }
        return ids;
    }
    
    // Other actions
    private void moveTaskUp() {
        int row = taskQueueTable.getSelectedRow();
        if (row > 0) {
            String taskId = (String) taskQueueModel.getValueAt(row, 1);
            currentStation.moveTaskUp(taskId);
            refreshTaskQueue();
            taskQueueTable.setRowSelectionInterval(row - 1, row - 1);
        }
    }
    
    private void moveTaskDown() {
        int row = taskQueueTable.getSelectedRow();
        if (row >= 0 && row < taskQueueModel.getRowCount() - 1) {
            String taskId = (String) taskQueueModel.getValueAt(row, 1);
            currentStation.moveTaskDown(taskId);
            refreshTaskQueue();
            taskQueueTable.setRowSelectionInterval(row + 1, row + 1);
        }
    }
    
    private void editSelectedTask() {
        int row = taskQueueTable.getSelectedRow();
        if (row >= 0) {
            String taskId = (String) taskQueueModel.getValueAt(row, 1);
            // Show task edit dialog
        }
    }
    
    private void pauseAllTasks() {
        currentStation.pauseAllTasks();
        refreshRunningTasks();
    }
    
    private void resumeAllTasks() {
        currentStation.resumeAllTasks();
        refreshRunningTasks();
    }
    
    private void stopAllTasks() {
        int result = Messages.showYesNoDialog(
            project,
            "Stop all running tasks?",
            "Confirm Stop",
            Messages.getWarningIcon()
        );
        
        if (result == Messages.YES) {
            currentStation.stopAllTasks();
            refreshTaskLists();
        }
    }
    
    private void rerunSelectedTask() {
        int row = completedTasksTable.getSelectedRow();
        if (row >= 0) {
            String taskId = (String) completedTasksModel.getValueAt(row, 0);
            currentStation.rerunTask(taskId);
            refreshTaskLists();
        }
    }
    
    private void viewTaskDetails() {
        // Show task details dialog
    }
    
    private void clearCompletedTasks() {
        int result = Messages.showYesNoDialog(
            project,
            "Clear all completed task history?",
            "Confirm Clear",
            Messages.getWarningIcon()
        );
        
        if (result == Messages.YES) {
            currentStation.clearCompletedTasks();
            refreshCompletedTasks();
        }
    }
    
    // Template operations
    private void createNewTemplate() {
        // Show template creation dialog
    }
    
    private void deleteSelectedTemplate() {
        // Delete selected template
    }
    
    private void editSelectedTemplate() {
        // Edit selected template
    }
    
    private void useSelectedTemplate() {
        // Load template into task form
    }
    
    private void importTemplates() {
        // Import templates from file
    }
    
    private void exportTemplates() {
        // Export templates to file
    }
    
    // Custom cell renderers
    private static class PriorityCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof TaskPriority) {
                TaskPriority priority = (TaskPriority) value;
                switch (priority) {
                    case URGENT:
                        setForeground(Color.RED);
                        break;
                    case HIGH:
                        setForeground(Color.ORANGE);
                        break;
                    case NORMAL:
                        setForeground(UIUtil.getLabelForeground());
                        break;
                    case LOW:
                        setForeground(Color.GRAY);
                        break;
                }
            }
            
            return this;
        }
    }
    
    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof TaskStatus) {
                TaskStatus status = (TaskStatus) value;
                switch (status) {
                    case COMPLETED:
                        setForeground(Color.GREEN);
                        break;
                    case FAILED:
                        setForeground(Color.RED);
                        break;
                    case CANCELLED:
                        setForeground(Color.GRAY);
                        break;
                    default:
                        setForeground(UIUtil.getLabelForeground());
                }
            }
            
            return this;
        }
    }
    
    private static class ProgressCellRenderer extends JProgressBar implements TableCellRenderer {
        public ProgressCellRenderer() {
            setStringPainted(true);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            if (value instanceof Integer) {
                setValue((Integer) value);
            }
            return this;
        }
    }
    
    // Button renderer/editor for actions
    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }
    
    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // Handle button click
                int row = runningTasksTable.getSelectedRow();
                if (row >= 0) {
                    String taskId = (String) runningTasksModel.getValueAt(row, 0);
                    showTaskActions(taskId);
                }
            }
            isPushed = false;
            return label;
        }
        
        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
    
    private void showTaskActions(String taskId) {
        JPopupMenu menu = new JPopupMenu();
        
        JMenuItem pauseItem = new JMenuItem("Pause");
        pauseItem.addActionListener(e -> currentStation.pauseTask(taskId));
        menu.add(pauseItem);
        
        JMenuItem stopItem = new JMenuItem("Stop");
        stopItem.addActionListener(e -> currentStation.stopTask(taskId));
        menu.add(stopItem);
        
        JMenuItem viewItem = new JMenuItem("View Details");
        viewItem.addActionListener(e -> viewTaskDetails());
        menu.add(viewItem);
        
        menu.show(runningTasksTable, runningTasksTable.getMousePosition().x, 
                  runningTasksTable.getMousePosition().y);
    }
}