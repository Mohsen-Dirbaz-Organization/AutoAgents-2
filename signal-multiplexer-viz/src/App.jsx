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
import ConceptExplainer from './components/ConceptExplainer';
import AgentDeploymentViz from './components/AgentDeploymentViz';
import StackView from './components/StackView';
import ProgramCoverageMap from './components/ProgramCoverageMap';
import ConstitutionView from './components/ConstitutionView';
import EventFabricView from './components/EventFabricView';
import CanonView from './components/CanonView';
import './App.css';

function App() {
  const [engine] = useState(() => new MultiplexerEngine());
  const [state, setState] = useState(engine.getState());
  const [isRunning, setIsRunning] = useState(false);
  const [showCode, setShowCode] = useState(false);
  const [activeView, setActiveView] = useState('multiplexer');
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

// View-scoped multiplexer lifecycle: the four simulation intervals are
  // App-owned, so navigating away would leave them ticking in the background.
  // Stop the simulation whenever we leave the multiplexer view.
  // V-3 (multiplexer-lifecycle-refactor): the multiplexer simulation is
  // view-scoped. The original engine's four intervals were App-scoped, so
  // switching to another view left them ticking in the background. Stop the
  // simulation whenever we navigate away from the multiplexer view.
  useEffect(() => {
    if (activeView !== 'multiplexer' && isRunning) {
      stopSimulation();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeView]);

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
        <h1>
          {activeView === 'stack'
            ? 'Bounded Autonomy on a Memristive Substrate'
            : activeView === 'coverage'
            ? 'GHOST Autonomy — Program Coverage Map'
            : activeView === 'constitution'
            ? 'The Constitution of Truth — Correctable Ground Truth'
            : activeView === 'eventfabric'
            ? 'F26 Autonomous-Driving Event Fabric'
            : activeView === 'canon'
            ? 'Canon & Integrity — One Ledger, Many Renderings'
            : 'Adaptive Signal Multiplexer with Dynamic Problem Formulation'}
        </h1>
        <p className="subtitle">
          {activeView === 'stack'
            ? 'The full eight-thread safety stack: antitone monotonicity, metabolic memory, and the analog veto'
            : activeView === 'coverage'
            ? '56 research subcategories × main(8).tex chapters × source-material availability'
            : activeView === 'constitution'
            ? 'Separation of epistemic powers · correction supremacy · anti-silent-drift · temporal rollback'
            : activeView === 'eventfabric'
            ? 'Single timeline · twelve canvases · synchronized interaction traces · source-standing discipline'
            : activeView === 'canon'
            ? 'Definition of Done · number cards · symbol register · retirement register · no guaranteed-pass checks'
            : 'Real-time visualization of intelligent coordination through continuous optimization'}
        </p>
        <nav className="view-switcher">
          <button
            className={`view-tab ${activeView === 'multiplexer' ? 'active' : ''}`}
            onClick={() => setActiveView('multiplexer')}
          >
            Signal Multiplexer
          </button>
          <button
            className={`view-tab ${activeView === 'stack' ? 'active' : ''}`}
            onClick={() => setActiveView('stack')}
          >
            Bounded Autonomy Stack
          </button>
          <button
            className={`view-tab ${activeView === 'coverage' ? 'active' : ''}`}
            onClick={() => setActiveView('coverage')}
          >
            Program Coverage Map
          </button>
          <button
            className={`view-tab ${activeView === 'constitution' ? 'active' : ''}`}
            onClick={() => setActiveView('constitution')}
          >
            Constitution of Truth
          </button>
          <button
            className={`view-tab ${activeView === 'eventfabric' ? 'active' : ''}`}
            onClick={() => setActiveView('eventfabric')}
          >
            Event Fabric (F26)
          </button>
          <button
            className={`view-tab ${activeView === 'canon' ? 'active' : ''}`}
            onClick={() => setActiveView('canon')}
          >
            Canon &amp; Integrity
          </button>
        </nav>
        {activeView === 'multiplexer' && (
          <div className="header-actions">
            <button onClick={() => setShowCode(!showCode)} className="btn-secondary">
              {showCode ? 'Hide' : 'Show'} Java Implementation
            </button>
          </div>
        )}
      </header>

      {activeView === 'stack' && <StackView />}

      {activeView === 'coverage' && <ProgramCoverageMap />}

      {activeView === 'constitution' && <ConstitutionView />}

      {activeView === 'eventfabric' && <EventFabricView />}

      {activeView === 'canon' && <CanonView />}

      {activeView === 'multiplexer' && (
      <>
      {showCode && <CodePanel />}

      <section className="section full-width-section">
        <ConceptExplainer currentStep={state.currentStep} />
      </section>

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

          <section className="section">
            <h2>Dual-Purpose Agent Deployment</h2>
            <AgentDeploymentViz
              channels={state.channels}
              state={state}
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
      </>
      )}

      {activeView === 'stack' && (
        <footer className="app-footer">
          <p>
            Architecture Philosophy: bound what an error is allowed to <em>do</em>, not whether it occurs.
            Every quantitative figure is <strong>projected</strong> unless marked <strong>measured</strong>
            — only the ~32 ns analog-veto witness is measured.
          </p>
        </footer>
      )}

      {activeView === 'coverage' && (
        <footer className="app-footer">
          <p>
            Coverage Map: from the GHOST Autonomy <em>Research Subcategory → Document Section Mapping</em>
            (Feb 2026). Coverage = source-material availability in project knowledge (a design corpus),
            not fabricated or measured silicon.
          </p>
        </footer>
      )}

      {activeView === 'constitution' && (
        <footer className="app-footer">
          <p>
            Constitution of Truth (from <em>Temporal State Management</em>, Part VI): a claim that cannot be
            challenged is not trusted — it is merely unexamined. The warrant travels with the decision.
          </p>
        </footer>
      )}

      {activeView === 'eventfabric' && (
        <footer className="app-footer">
          <p>
            F26 Autonomous-Driving Event-Fabric Integration Plan: the base unit is a <em>synchronized
            interaction trace</em>, not a pixel. Safety-bearing paths may only <strong>contract</strong>
            authority; <strong>projected / notional / open</strong> claims may not expand it — and the
            "144 unique latencies" claim is kept honestly <strong>unconfirmed</strong>.
          </p>
        </footer>
      )}

      {activeView === 'canon' && (
        <footer className="app-footer">
          <p>
            Canon &amp; Integrity (EPU Companion set, docs 07–17): <em>"The remedy is not to correct each
            figure by hand. It is to make an uncorrected figure impossible to release."</em> A check that
            cannot fail has no truth value; a target is never phrased as achieved; retired constructs are
            removed, not owed.
          </p>
        </footer>
      )}
    </div>
  );
}

export default App;
