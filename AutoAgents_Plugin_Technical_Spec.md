# AutoAgents Plugin Technical Specification

## Plugin Metadata

| Property | Value |
|----------|-------|
| **Plugin ID** | `com.IDE.plugin.autoagents` |
| **Name** | AutoAgents |
| **Version** | 1.0.0 |
| **Vendor** | AutoAgents Team |
| **Vendor Email** | support@autoagents.com |
| **Vendor URL** | https://www.autoagents.com |
| **Compatibility** | IntelliJ IDEA 2021.3+ |

## Dependencies

| Dependency | Description |
|------------|-------------|
| `com.intellij.modules.platform` | Core platform functionality |
| `com.intellij.modules.lang` | Language support |
| `com.intellij.modules.java` | Java language specific features |

## Core Features

1. **AI-assisted code generation**
2. **Automated refactoring suggestions**
3. **Smart code completion**
4. **Code analysis and optimization**
5. **AI Agent Station Management**
6. **Claude AI Integration**
7. **Real-time agent status monitoring**

## Actions & Commands

### Main Menu Actions
Located under "AutoAgents" menu group in the main menu bar:

| Action ID | Class | Description | Shortcut |
|-----------|-------|-------------|----------|
| `AutoAgents.Generate` | `GenerateCodeAction` | Generate code using AI | `Ctrl+Alt+G` |
| `AutoAgents.Analyze` | `AnalyzeCodeAction` | Analyze current file with AI | - |
| `AutoAgents.ShowToolWindow` | `ShowToolWindowAction` | Show AutoAgents tool window | - |
| `AutoAgents.ManageStations` | `ManageStationsAction` | Open AI Station Management | `Ctrl+Alt+S` |
| `AutoAgents.StartAllStations` | `StartAllStationsAction` | Start all AI agent stations | - |
| `AutoAgents.StopAllStations` | `StopAllStationsAction` | Stop all AI agent stations | - |

### Editor Context Menu Actions

| Action ID | Class | Description | Shortcut |
|-----------|-------|-------------|----------|
| `AutoAgents.EditorPopup.Generate` | `GenerateCodeAction` | Generate code using AutoAgents AI | - |
| `AutoAgents.EditorPopup.SendToClaude` | `SendToClaudeAction` | Send selected code to Claude | `Ctrl+Shift+C` |
| `AutoAgents.EditorPopup.AskClaude` | `AskClaudeAction` | Ask Claude about selected code | - |

## Tool Windows

### AutoAgents Main Tool Window
- **ID**: `AutoAgents`
- **Anchor**: Right
- **Factory Class**: `com.IDE.plugin.ui.AutoAgentsToolWindowFactory`
- **Icon**: `AllIcons.Actions.Execute`

### Station Management Tool Window
- **ID**: `AutoAgents.Stations`
- **Anchor**: Bottom
- **Factory Class**: `com.IDE.plugin.station.ui.StationManagementToolWindowFactory`
- **Icon**: `AllIcons.Nodes.Services`

### Claude Console Tool Window
- **ID**: `AutoAgents.Claude`
- **Anchor**: Bottom
- **Factory Class**: `com.IDE.plugin.claude.ui.ClaudeConsoleToolWindowFactory`
- **Icon**: `AllIcons.Debugger.Console`

## Services

### Application-Level Services

| Service | Implementation Class |
|---------|---------------------|
| AutoAgents Settings | `com.IDE.plugin.settings.AutoAgentsSettings` |
| AI Service | `com.IDE.plugin.services.AIService` |
| Station Manager | `com.IDE.plugin.station.services.StationManager` |
| Station Configuration | `com.IDE.plugin.station.services.StationConfigurationService` |
| Station Monitoring | `com.IDE.plugin.station.services.StationMonitoringService` |
| Station Notifications | `com.IDE.plugin.station.services.StationNotificationService` |
| Claude Service | `com.IDE.plugin.claude.services.ClaudeService` |
| Claude Configuration | `com.IDE.plugin.claude.services.ClaudeConfigurationService` |

### Project-Level Services

| Service | Implementation Class |
|---------|---------------------|
| Project AI Service | `com.IDE.plugin.services.ProjectAIService` |

## Configuration

### Settings Pages

1. **Main Settings**
   - **ID**: `com.IDE.plugin.settings.AutoAgentsConfigurable`
   - **Parent**: Tools
   - **Display Name**: AutoAgents

2. **Station Management Settings**
   - **ID**: `com.IDE.plugin.station.settings.StationConfigurable`
   - **Parent**: AutoAgents
   - **Display Name**: Station Management

3. **Claude Integration Settings**
   - **ID**: `com.IDE.plugin.claude.settings.ClaudeConfigurable`
   - **Parent**: AutoAgents
   - **Display Name**: Claude Integration

## UI Components

### Status Bar Widget
- **ID**: `AutoAgents.StationStatus`
- **Factory**: `com.IDE.plugin.station.ui.StationStatusWidgetFactory`
- **Position**: After Position widget

### Completion Contributor
- **Language**: JAVA
- **Class**: `com.IDE.plugin.completion.AutoAgentsCompletionContributor`

### Intentions
- **Generate Method Intention**
  - **Class**: `com.IDE.plugin.intentions.GenerateMethodIntention`
  - **Category**: AutoAgents

## Notification Groups

| Group ID | Display Type | Purpose |
|----------|--------------|---------|
| `AutoAgents.Notification.Group` | BALLOON | General plugin notifications |
| `AutoAgents.Station.Notification.Group` | BALLOON | Station-related notifications |
| `AutoAgents.Claude.Notification.Group` | BALLOON | Claude integration notifications |

## Components

### Application Components
- **Implementation**: `com.IDE.plugin.AutoAgentsApplicationComponent`
- **Lifecycle**: Application-wide initialization

### Project Components
- **Implementation**: `com.IDE.plugin.AutoAgentsProjectComponent`
- **Lifecycle**: Per-project initialization

## Architecture Overview

The plugin follows a modular architecture with three main subsystems:

1. **Core AutoAgents System**
   - Code generation and analysis
   - Completion and intentions
   - Main tool window interface

2. **Station Management System**
   - AI agent station lifecycle management
   - Station monitoring and configuration
   - Status bar integration

3. **Claude Integration System**
   - Claude AI service integration
   - Console interface
   - Code analysis and suggestions

## Version History

### Version 1.0.0
- Initial release
- Basic AI code generation
- Tool window integration
- Settings configuration
- AI Agent Station Management system
- Claude AI integration
- Status bar widget for agent monitoring