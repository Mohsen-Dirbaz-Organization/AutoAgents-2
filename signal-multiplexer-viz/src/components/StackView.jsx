import { useState, useEffect, useRef } from 'react';
import { BoundedAutonomyStack } from '../simulation/BoundedAutonomyStack';
import StackArchitecture from './StackArchitecture';
import RefusalCascade from './RefusalCascade';
import MetabolicMemory from './MetabolicMemory';
import MonotonicityMonitor from './MonotonicityMonitor';
import TrustedScalars from './TrustedScalars';
import ConservationRenormalizationPanel from './ConservationRenormalizationPanel';
import EvidentiaryLedger from './EvidentiaryLedger';
import ValidationFrontier from './ValidationFrontier';
import StressFieldPanel from './StressFieldPanel';
import StackControlPanel from './StackControlPanel';
import './StackView.css';

/**
 * The Bounded Autonomy Stack view — a self-contained simulation of the full
 * eight-thread architecture. Mirrors App.jsx's engine/interval/getState
 * pattern so the two views behave consistently.
 */
function StackView() {
  const [engine] = useState(() => new BoundedAutonomyStack());
  const [state, setState] = useState(() => engine.getState());
  const [isRunning, setIsRunning] = useState(false);
  const stepInterval = useRef(null);

  useEffect(() => {
    engine.setScenario('nominal', 1.0);
    setState(engine.getState());
    return () => stop();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const start = () => {
    engine.start();
    setIsRunning(true);
    // 100 ms wall-clock per simulated tick.
    stepInterval.current = setInterval(() => {
      engine.step();
      setState(engine.getState());
    }, 100);
  };

  const stop = () => {
    engine.stop();
    setIsRunning(false);
    if (stepInterval.current) {
      clearInterval(stepInterval.current);
      stepInterval.current = null;
    }
  };

  const toggle = () => (isRunning ? stop() : start());

  const reset = () => {
    stop();
    engine.reset();
    setState(engine.getState());
  };

  const onScenarioChange = (mode, intensity) => {
    engine.setScenario(mode, intensity);
    setState(engine.getState());
  };

  const onToggleDiscipline = (on) => {
    engine.setConservativeDiscipline(on);
    setState(engine.getState());
  };

  const onRaiseRisk = (amt) => { engine.raiseRisk(amt); setState(engine.getState()); };
  const onInjectDrift = () => { engine.injectDrift(); setState(engine.getState()); };
  const onSpoof = () => { engine.spoof(); setState(engine.getState()); };

  return (
    <div className="stack-view">
      <section className="section full-width">
        <div className="sv-banner">
          <div>
            <h2 className="sv-thesis-title">The one invariant the whole stack protects</h2>
            <p className="sv-thesis">
              As warrant for a benign interpretation falls, behavioral risk may only ever fall with it.
              Every distortion is one-directional, toward conservatism — the admissible action set may
              only ever <strong>contract</strong>, never expand, as risk rises.
              <span className="sv-formula"> R₁ ≤ R₂ ⇒ U_ad(R₂) ⊆ U_ad(R₁)</span>
            </p>
          </div>
          <div className="sv-clock">
            <span className="sv-clock-val">t = {state.simTime.toFixed(1)}s</span>
            <span className="sv-clock-sub">tick {state.tick}</span>
          </div>
        </div>
      </section>

      <div className="sv-grid">
        <div className="sv-col">
          <section className="section">
            <h2>Eight-Thread Stack (Lanes A–H)</h2>
            <StackArchitecture
              lanes={state.lanes}
              risk={state.risk}
              veto={state.veto}
              scalars={state.scalars}
            />
          </section>

          <section className="section">
            <h2>Architecture of Refusal — S0 → S4 Narrowing</h2>
            <RefusalCascade
              admissible={state.admissible}
              veto={state.veto}
              risk={state.risk}
            />
          </section>

          <section className="section">
            <h2>Metabolic Memory & the τ = 5 s Boundary</h2>
            <MetabolicMemory memory={state.memory} />
          </section>

          <section className="section">
            <h2>Conservation-Renormalization Layer (Q = 0)</h2>
            <ConservationRenormalizationPanel crl={state.crl} />
          </section>

          <section className="section">
            <h2>Stress-Field Constraints (Part V)</h2>
            <StressFieldPanel state={state} />
          </section>
        </div>

        <div className="sv-col">
          <section className="section">
            <h2>Control Panel</h2>
            <StackControlPanel
              isRunning={isRunning}
              onToggle={toggle}
              onReset={reset}
              scenario={state.scenario}
              onScenarioChange={onScenarioChange}
              conservativeDiscipline={state.conservativeDiscipline}
              onToggleDiscipline={onToggleDiscipline}
              onRaiseRisk={onRaiseRisk}
              onInjectDrift={onInjectDrift}
              onSpoof={onSpoof}
            />
          </section>

          <section className="section">
            <h2>Antitone Monotonicity Monitor</h2>
            <MonotonicityMonitor
              history={state.history}
              monotonicity={state.monotonicity}
              conservativeDiscipline={state.conservativeDiscipline}
            />
          </section>

          <section className="section">
            <h2>Trusted Scalars (φ → ξ, S) & LLC Drift</h2>
            <TrustedScalars scalars={state.scalars} history={state.history} />
          </section>

          <section className="section">
            <h2>Evidentiary Ledger — Projected / Measured Seam</h2>
            <EvidentiaryLedger
              ledger={state.ledger}
              veto={state.veto}
              metrics={state.metrics}
            />
          </section>

          <section className="section">
            <h2>Validation Frontier — Open Obligations (O-1 … O-11)</h2>
            <ValidationFrontier />
          </section>
        </div>
      </div>
    </div>
  );
}

export default StackView;
