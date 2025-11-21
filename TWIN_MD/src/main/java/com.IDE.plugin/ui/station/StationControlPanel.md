# StationControlPanel.md

## Class Overview
**File:** `src/main/java/com/IDE/plugin/ui/station/StationControlPanel.java`  
**Package:** `com.IDE.plugin.ui.station`  
**Type:** Swing Panel Component for Station Control and Task Management

## Purpose
The `StationControlPanel` class provides comprehensive station control and task management functionality. It offers interfaces for task creation, queue management, execution monitoring, and template-based task generation with real-time status updates.

## Architecture

### Component Structure
```java
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
    
    // Control components
    private JButton startStationButton;
    private JButton stopStationButton;
    private JButton pauseStationButton;
    private JLabel stationStatusLabel;
    private JProgressBar taskProgressBar;
}
```

## UI Layout Structure

### Main Layout
```java
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
```

## Station Control Features

### 1. Station Control Panel
```java
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
    
    // Status display
    controlsPanel.add(new JLabel("Status:"));
    stationStatusLabel = new JLabel("Stopped");
    stationStatusLabel.setFont(stationStatusLabel.getFont().deriveFont(Font.BOLD));
    controlsPanel.add(stationStatusLabel);
    
    panel.add(controlsPanel, BorderLayout.WEST);
    
    // Task progress
    JPanel progressPanel = createProgressPanel();
    panel.add(progressPanel, BorderLayout.CENTER);
    
    // Quick stats
    JPanel statsPanel = createQuickStatsPanel();
    panel.add(statsPanel, BorderLayout.EAST);
    
    return panel;
}
```

### 2. Station State Management
```java
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
```

## Task Management Interface

### 1. Task Queue Management
```java
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
            return column == 0; // Only checkbox column is editable
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
```

### 2. Running Tasks Monitoring
```java
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
    
    // Actions renderer and editor
    runningTasksTable.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer());
    runningTasksTable.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor(new JCheckBox()));
    
    JScrollPane scrollPane = new JBScrollPane(runningTasksTable);
    panel.add(scrollPane, BorderLayout.CENTER);
    
    // Control panel
    JPanel controlPanel = createRunningTasksControlPanel();
    panel.add(controlPanel, BorderLayout.SOUTH);
    
    return panel;
}
```

### 3. Completed Tasks History
```java
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
    JPanel controlPanel = createCompletedTasksControlPanel();
    panel.add(controlPanel, BorderLayout.SOUTH);
    
    return panel;
}
```

## Task Creation Interface

### 1. Task Creation Form
```java
private JPanel createTaskCreationPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Create New Task"));
    
    // Task form
    JPanel formPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = JBUI.insets(5);
    
    // Task name
    addFormField(formPanel, gbc, "Task Name:", 
                taskNameField = new JTextField(20), 0, 0);
    
    // Task type
    taskTypeCombo = new ComboBox<>(new String[]{
        "Analysis", "Build", "Test", "Deploy", "Monitor", "Custom"
    });
    addFormField(formPanel, gbc, "Type:", taskTypeCombo, 2, 0);
    
    // Priority
    priorityCombo = new ComboBox<>(TaskPriority.values());
    addFormField(formPanel, gbc, "Priority:", priorityCombo, 4, 0);
    
    // Description
    taskDescriptionArea = new JTextArea(2, 40);
    taskDescriptionArea.setLineWrap(true);
    addFormField(formPanel, gbc, "Description:", 
                new JBScrollPane(taskDescriptionArea), 0, 1, 5);
    
    // Agent assignment
    agentAssignmentCombo = new ComboBox<>(new String[]{"Auto-assign", "Agent 1", "Agent 2"});
    addFormField(formPanel, gbc, "Assign to:", agentAssignmentCombo, 0, 2);
    
    autoAssignCheckbox = new JCheckBox("Auto-assign based on load");
    addFormField(formPanel, gbc, "", autoAssignCheckbox, 2, 2, 2);
    
    // Timeout and retry settings
    addTimeoutAndRetryFields(formPanel, gbc);
    
    panel.add(formPanel, BorderLayout.CENTER);
    
    // Action buttons
    JPanel buttonPanel = createTaskActionButtons();
    panel.add(buttonPanel, BorderLayout.EAST);
    
    return panel;
}
```

### 2. Task Actions
```java
private JPanel createTaskActionButtons() {
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
    
    return buttonPanel;
}
```

## Batch Operations

### 1. Queue Control Panel
```java
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
    JPanel actionsPanel = createQueueActionsPanel();
    panel.add(actionsPanel, BorderLayout.EAST);
    
    return panel;
}
```

### 2. Batch Task Operations
```java
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
```

## Task Templates

### 1. Task Templates Panel
```java
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
    JPanel actionsPanel = createTemplateActionsPanel();
    panel.add(actionsPanel, BorderLayout.SOUTH);
    
    return panel;
}
```

### 2. Template Actions
```java
private JPanel createTemplateActionsPanel() {
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
    
    return actionsPanel;
}
```

## Task Execution Management

### 1. Task Creation Logic
```java
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
    TaskSchedulingDialog dialog = new TaskSchedulingDialog(project);
    if (dialog.showAndGet()) {
        Task task = createTaskFromForm();
        task.setScheduledTime(dialog.getScheduledTime());
        currentStation.scheduleTask(task);
        clearTaskForm();
        refreshTaskQueue();
    }
}
```

### 2. Task Validation
```java
private boolean validateTaskForm() {
    if (taskNameField.getText().trim().isEmpty()) {
        Messages.showErrorDialog(project, "Task name is required", "Validation Error");
        return false;
    }
    
    if (currentStation == null) {
        Messages.showErrorDialog(project, "No station selected", "Error");
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
```

## Station Control Actions

### 1. Station Lifecycle
```java
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
```

## Task Monitoring and Control

### 1. Running Task Actions
```java
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
```

### 2. Individual Task Actions
```java
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
```

## Custom Cell Renderers

### 1. Priority Cell Renderer
```java
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
```

### 2. Status Cell Renderer
```java
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
```

### 3. Progress Cell Renderer
```java
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
```

## Data Refresh Mechanisms

### 1. Automatic Refresh
```java
public void setStation(Station station) {
    this.currentStation = station;
    updateStationStatus();
    refreshTaskLists();
}

private void refreshTaskLists() {
    if (currentStation == null) return;
    
    // Refresh all task views
    refreshTaskQueue();
    refreshRunningTasks();
    refreshCompletedTasks();
}
```

### 2. Real-time Updates
```java
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
```

## Integration Features

### Station Integration
- **Station Binding**: Direct station control interface
- **Real-time Status**: Live station status updates
- **Task Coordination**: Integrated task and station management

### Error Handling
- **Validation**: Comprehensive form validation
- **User Feedback**: Clear error messages and confirmations
- **Graceful Degradation**: Handles service unavailability

### Performance Optimization
- **Efficient Updates**: Minimal UI refresh operations
- **Background Operations**: Non-blocking task operations
- **Memory Management**: Proper resource cleanup

This control panel provides comprehensive station and task management capabilities, enabling users to efficiently control station operations and manage task execution workflows.