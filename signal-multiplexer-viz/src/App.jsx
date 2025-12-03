import { useState, useEffect, useRef } from 'react';
import { MultiplexerEngine } from './simulation/MultiplexerEngine';
import OptimizationLoop from './components/OptimizationLoop';
import ChannelVisualization from './components/ChannelVisualization';
import ProblemFormulation from './components/ProblemFormulation';
import SolverVisualization from './components/SolverVisualization';
import ConstraintPanel from './components/ConstraintPanel';
import PerformanceMetrics from './components/PerformanceMetrics';
import ControlPanel from './components/ControlPanel';
import CodePanel from './components/CodePanel';
import DemultiplexerVisualization from './components/DemultiplexerVisualization';
import TimeSeriesChart from './components/TimeSeriesChart';
import ScenarioControl from './components/ScenarioControl';
import AdaptationMetrics from './components/AdaptationMetrics';
import './App.css';

function App() {
  const [engine] = useState(() => new MultiplexerEngine());
  const [state, setState] = useState(engine.getState());
  const [isRunning, setIsRunning] = useState(false);
  const [showCode, setShowCode] = useState(false);
  const [currentScenario, setCurrentScenario] = useState({ mode: 'steady', intensity: 1.0 });
  const optimizationInterval = useRef(null);
  const processingInterval = useRef(null);
  const visualizationInterval = useRef(null);
  const signalInjectorInterval = useRef(null);

  // Initialize some test channels
  useEffect(() => {
    engine.createChannel('channel-1', 'HIGH');
    engine.createChannel('channel-2', 'NORMAL');
    engine.createChannel('channel-3', 'CRITICAL');
    engine.createChannel('channel-4', 'LOW');

    // Set initial scenario
    engine.setScenario('steady', 1.0);

    // Add some initial signals
    for (let i = 0; i < 5; i++) {
      engine.sendSignal('channel-1', { priority: 'HIGH', data: `signal-${i}` });
      engine.sendSignal('channel-2', { priority: 'NORMAL', data: `signal-${i}` });
    }

    setState(engine.getState());

    // Listen to engine events
    engine.on('snapshot', () => updateState());
    engine.on('problem_formulated', () => updateState());
    engine.on('structure_detected', () => updateState());
    engine.on('solver_selected', () => updateState());
    engine.on('constraints_synthesized', () => updateState());
    engine.on('solution_found', () => updateState());
    engine.on('solution_applied', () => updateState());
    engine.on('channel_created', () => updateState());
    engine.on('signal_enqueued', () => updateState());

    return () => {
      stopSimulation();
    };
  }, []);

  const updateState = () => {
    setState(engine.getState());
  };

  const startSimulation = () => {
    engine.start();
    setIsRunning(true);

    // Run optimization loop every 100ms
    optimizationInterval.current = setInterval(() => {
      engine.reformulateAndSolve();
    }, 100);

    // Process signals every 50ms
    processingInterval.current = setInterval(() => {
      engine.processSignals();
    }, 50);

    // Update visualization every 100ms
    visualizationInterval.current = setInterval(() => {
      updateState();
    }, 100);

    // Inject scenario-driven signals
    signalInjectorInterval.current = setInterval(() => {
      engine.generateScenarioSignals();
    }, 200);
  };

  const stopSimulation = () => {
    engine.stop();
    setIsRunning(false);

    if (optimizationInterval.current) {
      clearInterval(optimizationInterval.current);
      optimizationInterval.current = null;
    }

    if (processingInterval.current) {
      clearInterval(processingInterval.current);
      processingInterval.current = null;
    }

    if (visualizationInterval.current) {
      clearInterval(visualizationInterval.current);
      visualizationInterval.current = null;
    }

    if (signalInjectorInterval.current) {
      clearInterval(signalInjectorInterval.current);
      signalInjectorInterval.current = null;
    }
  };

  const toggleSimulation = () => {
    if (isRunning) {
      stopSimulation();
    } else {
      startSimulation();
    }
  };

  const injectSignal = (channelId, priority) => {
    engine.sendSignal(channelId, {
      priority,
      data: `manual-signal-${Date.now()}`
    });
  };

  const injectDisturbance = () => {
    // Inject a burst of critical signals
    for (let i = 0; i < 10; i++) {
      engine.sendSignal('channel-3', {
        priority: 'CRITICAL',
        data: `disturbance-signal-${i}`
      });
    }
  };

  const handleScenarioChange = (mode, intensity) => {
    engine.setScenario(mode, intensity);
    setCurrentScenario({ mode, intensity });
  };

  return (
    <div className="app">
      <header className="app-header">
        <h1>Adaptive Signal Multiplexer with Dynamic Problem Formulation</h1>
        <p className="subtitle">
          Real-time visualization of intelligent coordination through continuous optimization
        </p>
        <div className="header-actions">
          <button onClick={() => setShowCode(!showCode)} className="btn-secondary">
            {showCode ? 'Hide' : 'Show'} Java Implementation
          </button>
        </div>
      </header>

      {showCode && <CodePanel />}

      <div className="main-content">
        <div className="left-panel">
          <section className="section">
            <h2>Optimization Loop (100ms cycle)</h2>
            <OptimizationLoop state={state} />
          </section>

          <section className="section">
            <h2>Problem Formulation</h2>
            <ProblemFormulation problem={state.currentProblem} />
          </section>

          <section className="section">
            <h2>Solver Selection & Execution</h2>
            <SolverVisualization
              structure={state.currentStructure}
              solver={state.selectedSolver}
              solution={state.currentSolution}
            />
          </section>

          <section className="section">
            <h2>Bandwidth Allocation Over Time</h2>
            <TimeSeriesChart
              history={state.history}
              channels={state.channels}
            />
          </section>
        </div>

        <div className="right-panel">
          <section className="section">
            <h2>Control Panel</h2>
            <ControlPanel
              isRunning={isRunning}
              onToggle={toggleSimulation}
              onInjectSignal={injectSignal}
              onInjectDisturbance={injectDisturbance}
              channels={state.channels}
            />
          </section>

          <section className="section">
            <ScenarioControl
              currentScenario={currentScenario}
              onScenarioChange={handleScenarioChange}
              disabled={!isRunning}
            />
          </section>

          <section className="section">
            <h2>System State</h2>
            <ChannelVisualization
              channels={state.channels}
              resourceState={state.resourceState}
            />
          </section>

          <section className="section">
            <h2>Physics-Informed Constraints</h2>
            <ConstraintPanel constraints={state.constraints} />
          </section>

          <section className="section">
            <h2>Performance Metrics</h2>
            <PerformanceMetrics metrics={state.performanceMetrics} />
          </section>

          <section className="section">
            <AdaptationMetrics
              history={state.history}
              adaptiveParams={state.adaptiveParams}
              performanceMetrics={state.performanceMetrics}
            />
          </section>
        </div>
      </div>

      <section className="section full-width">
        <DemultiplexerVisualization
          channels={state.channels}
          demuxState={state.demuxState}
        />
      </section>

      <footer className="app-footer">
        <p>
          Architecture Philosophy: Signal multiplexing as continuous optimization with dynamic problem formulation,
          structure detection, and adaptive solver selection.
        </p>
      </footer>
    </div>
  );
}

export default App;
