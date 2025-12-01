# Adaptive Signal Multiplexer Visualization

Interactive web visualization for the **Adaptive Signal Multiplexer with Dynamic Problem Formulation** - a modernized approach to signal multiplexing through continuous optimization.

## Overview

This visualization demonstrates how signal multiplexing can be treated as a **continuous mathematical reasoning task** rather than a fixed engineering pattern. The system formulates and solves optimization problems in real-time (100ms cycles), adapts to changing conditions, and selects appropriate solvers based on detected problem structure.

## Key Features

### 🔄 **Optimization Loop (100ms cycle)**
- Real-time visualization of the 7-step optimization cycle:
  1. **OBSERVE** - Capture system snapshot
  2. **FORMULATE** - Construct optimization problem
  3. **DETECT** - Identify problem structure
  4. **SELECT** - Choose appropriate solver
  5. **SYNTHESIZE** - Generate physics-informed constraints
  6. **SOLVE** - Execute optimization
  7. **APPLY** - Update system configuration

### 📊 **Interactive Visualizations**
- **System State**: Real-time channel bandwidth and queue visualization using D3.js
- **Problem Formulation**: Mathematical notation (KaTeX) showing decision variables and objectives
- **Solver Selection**: Dynamic solver choice based on problem structure
- **Physics-Informed Constraints**: Conservation laws, dynamics, and causality
- **Performance Metrics**: Latency, throughput, and fairness tracking

### 🎮 **Interactive Controls**
- Start/stop continuous optimization
- Manual signal injection into specific channels
- Traffic burst disturbances to test adaptation
- Priority selection (CRITICAL, HIGH, NORMAL, LOW)

### 📚 **Source Code Reference**
- Collapsible panel with Java implementation
- Architectural philosophy and research connections
- Context from companion GroupCoordinationFramework

## Architecture Philosophy

**Core Insight**: Signal multiplexing is not a fixed pattern but a continuous optimization problem requiring:
- Dynamic problem formulation from current state
- Structure detection and algorithm selection
- Physics-informed constraint synthesis
- Adaptive learning and model updates
- Anytime solving with performance certificates

## Technologies

- **React** - Component framework
- **Vite** - Build tool and dev server
- **D3.js** - Data visualization
- **KaTeX** - Mathematical notation rendering
- **Lucide React** - Icon library

## Getting Started

### Prerequisites
- Node.js 18+ and npm

### Installation

```bash
npm install
```

### Development

```bash
npm run dev
```

Open [http://localhost:5173](http://localhost:5173) to view the visualization.

### Build

```bash
npm run build
```

Outputs to `dist/` directory.

### Preview Production Build

```bash
npm run preview
```

## How to Use

1. **Start the simulation** - Click the "Start Simulation" button to begin the continuous optimization loop
2. **Watch the loop** - Observe the 7-step optimization cycle running every 100ms
3. **Inject signals** - Use the control panel to manually add signals to specific channels
4. **Create disturbances** - Click "Inject Traffic Burst" to test system adaptation
5. **Explore the code** - Toggle "Show Java Implementation" to see the source code

## What You'll See

- **Channels** requesting bandwidth with different priorities (CRITICAL, HIGH, NORMAL, LOW)
- **Optimization problems** being formulated with decision variables (b_i for bandwidth, s_i for time slots)
- **Problem structure detection** identifying whether problems are convex, mixed-integer, stochastic, etc.
- **Solver selection** choosing appropriate methods (Interior Point, Branch & Bound, Weighted Sum, etc.)
- **Constraint synthesis** generating physics-informed constraints for conservation, dynamics, and causality
- **Real-time adaptation** as the system responds to changing conditions

## Academic Context

This visualization supports research in:
- **Robust Multiplexed MPC**: Problem formulation constructs MPC-style optimization with constraint tightening
- **VLC for MARL**: Adaptive bandwidth allocation mirrors frequency/amplitude-division multiplexing
- **6TiSCH for Swarms**: Time-slot allocation handled through optimization solver

## Audiences

Designed for three audiences with one unified visualization:
- **Academic**: Scholarly, balanced, intellectually rigorous
- **Technical**: Precise, methodological, empirically grounded
- **Policy**: Authoritative, action-oriented, pragmatic

## Project Structure

```
src/
├── simulation/
│   └── MultiplexerEngine.js    # Simplified multiplexer simulation
├── components/
│   ├── OptimizationLoop.jsx    # 7-step loop visualization
│   ├── ChannelVisualization.jsx # D3.js channel/bandwidth charts
│   ├── ProblemFormulation.jsx  # Mathematical formulation (KaTeX)
│   ├── SolverVisualization.jsx # Solver selection and results
│   ├── ConstraintPanel.jsx     # Physics-informed constraints
│   ├── PerformanceMetrics.jsx  # System metrics
│   ├── ControlPanel.jsx        # Interactive controls
│   └── CodePanel.jsx           # Java code display
├── App.jsx                     # Main application
└── index.css                   # Global styles
```

## Research Connections

Based on:
- **AdaptiveSignalMultiplexer.java** - Main implementation
- **GroupCoordinationFramework.java** - Broader multi-agent context

Demonstrates the same dynamic problem formulation approach applied to coordination problems beyond just signal multiplexing.

## License

See parent repository for license information.

## Contributing

This is a research visualization project. For questions or contributions, please refer to the parent AutoAgents-2 repository.
