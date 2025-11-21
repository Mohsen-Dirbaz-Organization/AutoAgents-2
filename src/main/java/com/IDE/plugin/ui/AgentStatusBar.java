package com.IDE.plugin.ui;

import com.IDE.plugin.ai.multiagent.services.AgentManagementService;
import com.IDE.plugin.ai.multiagent.station.StationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Status bar widget for agent activity
 */
public class AgentStatusBar implements StatusBarWidget, StatusBarWidget.IconPresentation {
    private static final String ID = "AutoAgents.StatusBar";
    
    private final Project project;
    private final AgentManagementService agentService;
    private final StationManager stationManager;
    
    private Icon currentIcon;
    private String currentTooltip;
    private Consumer<MouseEvent> clickConsumer;
    private Timer updateTimer;
    
    // Status states
    private enum Status {
        IDLE(createIcon(Color.GRAY), "AutoAgents: Idle"),
        ACTIVE(createIcon(new Color(0, 150, 0)), "AutoAgents: Active"),
        BUSY(createIcon(new Color(200, 150, 0)), "AutoAgents: Busy"),
        ERROR(createIcon(new Color(200, 0, 0)), "AutoAgents: Error");
        
        private final Icon icon;
        private final String tooltip;
        
        Status(Icon icon, String tooltip) {
            this.icon = icon;
            this.tooltip = tooltip;
        }
    }
    
    public AgentStatusBar(@NotNull Project project) {
        this.project = project;
        this.agentService = project.getService(AgentManagementService.class);
        this.stationManager = project.getService(StationManager.class);
        
        // Set initial status
        updateStatus();
        
        // Set click handler
        this.clickConsumer = event -> {
            if (event.getButton() == MouseEvent.BUTTON1) {
                showPopupMenu(event.getComponent(), event.getX(), event.getY());
            }
        };
        
        // Start update timer
        startUpdateTimer();
    }
    
    @Override
    @NotNull
    public String ID() {
        return ID;
    }
    
    @Override
    @Nullable
    public WidgetPresentation getPresentation(@NotNull PlatformType type) {
        return this;
    }
    
    @Override
    @Nullable
    public Icon getIcon() {
        return currentIcon;
    }
    
    @Override
    @NotNull
    public String getTooltipText() {
        return currentTooltip + "\n" + getDetailedStatus();
    }
    
    @Override
    @Nullable
    public Consumer<MouseEvent> getClickConsumer() {
        return clickConsumer;
    }
    
    @Override
    public void install(@NotNull StatusBar statusBar) {
        // Installation handled by status bar
    }
    
    @Override
    public void dispose() {
        if (updateTimer != null) {
            updateTimer.stop();
            updateTimer = null;
        }
    }
    
    private void updateStatus() {
        Status status = calculateStatus();
        currentIcon = status.icon;
        currentTooltip = status.tooltip;
    }
    
    private Status calculateStatus() {
        if (agentService == null || stationManager == null) {
            return Status.ERROR;
        }
        
        int activeStations = stationManager.getActiveStations().size();
        int deployedAgents = agentService.getDeployedAgents().size();
        
        if (activeStations == 0 || deployedAgents == 0) {
            return Status.IDLE;
        }
        
        // Check for errors
        boolean hasErrors = stationManager.getActiveStations().stream()
            .anyMatch(station -> station.getErrorRate() > 0.1);
        
        if (hasErrors) {
            return Status.ERROR;
        }
        
        // Check utilization
        double avgUtilization = calculateAverageUtilization();
        if (avgUtilization > 0.8) {
            return Status.BUSY;
        }
        
        return Status.ACTIVE;
    }
    
    private String getDetailedStatus() {
        if (agentService == null || stationManager == null) {
            return "Services not available";
        }
        
        int activeStations = stationManager.getActiveStations().size();
        int deployedAgents = agentService.getDeployedAgents().size();
        
        return String.format("Stations: %d | Agents: %d", activeStations, deployedAgents);
    }
    
    private double calculateAverageUtilization() {
        // TODO: Implement actual utilization calculation
        return 0.5;
    }
    
    private void showPopupMenu(Component component, int x, int y) {
        JPopupMenu popup = new JPopupMenu();
        
        // Status summary
        JMenuItem statusItem = new JMenuItem(getDetailedStatus());
        statusItem.setEnabled(false);
        popup.add(statusItem);
        popup.addSeparator();
        
        // Quick actions
        JMenuItem openToolWindowItem = new JMenuItem("Open AutoAgents");
        openToolWindowItem.addActionListener(e -> openToolWindow());
        popup.add(openToolWindowItem);
        
        JMenuItem startStationItem = new JMenuItem("Start New Station");
        startStationItem.addActionListener(e -> startNewStation());
        popup.add(startStationItem);
        
        JMenuItem deployAgentItem = new JMenuItem("Deploy New Agent");
        deployAgentItem.addActionListener(e -> deployNewAgent());
        popup.add(deployAgentItem);
        
        popup.addSeparator();
        
        // Station submenu
        JMenu stationsMenu = new JMenu("Active Stations");
        if (stationManager != null) {
            stationManager.getActiveStations().forEach(station -> {
                JMenuItem stationItem = new JMenuItem(station.getConfiguration().getName());
                stationItem.addActionListener(e -> showStationDetails(station.getId()));
                stationsMenu.add(stationItem);
            });
        }
        
        if (stationsMenu.getMenuComponentCount() == 0) {
            JMenuItem noStationsItem = new JMenuItem("No active stations");
            noStationsItem.setEnabled(false);
            stationsMenu.add(noStationsItem);
        }
        
        popup.add(stationsMenu);
        
        // Agent submenu
        JMenu agentsMenu = new JMenu("Deployed Agents");
        if (agentService != null) {
            agentService.getDeployedAgents().forEach(agentId -> {
                JMenuItem agentItem = new JMenuItem(agentId);
                agentItem.addActionListener(e -> showAgentDetails(agentId));
                agentsMenu.add(agentItem);
            });
        }
        
        if (agentsMenu.getMenuComponentCount() == 0) {
            JMenuItem noAgentsItem = new JMenuItem("No deployed agents");
            noAgentsItem.setEnabled(false);
            agentsMenu.add(noAgentsItem);
        }
        
        popup.add(agentsMenu);
        
        popup.show(component, x, y);
    }
    
    private void openToolWindow() {
        // Implementation to open the AutoAgents tool window
    }
    
    private void startNewStation() {
        // Implementation to start a new station
    }
    
    private void deployNewAgent() {
        // Implementation to deploy a new agent
    }
    
    private void showStationDetails(String stationId) {
        // Implementation to show station details
    }
    
    private void showAgentDetails(String agentId) {
        // Implementation to show agent details
    }
    
    private void startUpdateTimer() {
        updateTimer = new Timer(5000, e -> {
            SwingUtilities.invokeLater(() -> {
                updateStatus();
                // Trigger status bar update
                if (project != null && !project.isDisposed()) {
                    StatusBar statusBar = project.getService(StatusBar.class);
                    if (statusBar != null) {
                        statusBar.updateWidget(ID);
                    }
                }
            });
        });
        updateTimer.start();
    }
    
    private static Icon createIcon(Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw circle
                g2.setColor(color);
                g2.fillOval(x + 2, y + 2, 12, 12);
                
                // Draw border
                g2.setColor(color.darker());
                g2.drawOval(x + 2, y + 2, 12, 12);
                
                g2.dispose();
            }
            
            @Override
            public int getIconWidth() {
                return 16;
            }
            
            @Override
            public int getIconHeight() {
                return 16;
            }
        };
    }
}