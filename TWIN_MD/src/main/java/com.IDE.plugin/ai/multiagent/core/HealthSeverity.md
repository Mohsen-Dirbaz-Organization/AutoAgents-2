# HealthSeverity

## Overview
Enumeration representing different severity levels for system health status.

## Purpose
The `HealthSeverity` enum provides standardized severity levels for categorizing system health issues, enabling appropriate response and alerting mechanisms.

## Severity Levels

### CRITICAL
- System failure imminent or occurring
- Immediate intervention required
- May affect core functionality

### HIGH
- Significant issues detected
- Performance severely degraded
- Requires prompt attention

### MEDIUM
- Moderate issues present
- Some functionality affected
- Should be addressed soon

### LOW
- Minor issues detected
- Minimal impact on functionality
- Can be addressed during maintenance

### INFO
- Informational status
- No issues detected
- System operating normally

## Usage
Used throughout the system for health monitoring, alerting, and automated response mechanisms.

## Integration Points
- `SystemHealthStatus`: Uses severity levels for health reporting
- `SystemMonitor`: Assigns severity to detected issues
- `ObserverAgent`: Monitors and reports based on severity thresholds

## Related Classes
- `SystemHealthStatus`: Overall system health representation
- `AgentMetrics`: Individual agent health metrics