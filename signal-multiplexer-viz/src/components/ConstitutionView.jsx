import { useState, useEffect, useRef } from 'react';
import { BoundedAutonomyStack } from '../simulation/BoundedAutonomyStack';
import { ConstitutionalTruthEngine } from '../simulation/ConstitutionalTruthEngine';
import GroundTruthCanon from './GroundTruthCanon';
import SeparationOfPowers from './SeparationOfPowers';
import ConstitutionalLog from './ConstitutionalLog';
import UnknownRegister from './UnknownRegister';
import './ConstitutionView.css';

/**
 * Constitution of Truth view. A ConstitutionalTruthEngine governs a live
 * Bounded-Autonomy substrate: the substrate's falling warrant, risk, and LLC
 * jumps drive challenges and corrections in the canon. Demonstrates the core
 * demand — the system must remain CORRECTABLE.
 */
function ConstitutionView() {
  const [stack] = useState(() => new BoundedAutonomyStack());
  const [cte] = useState(() => new ConstitutionalTruthEngine());
  const [state, setState] = useState(() => cte.getState());
  const [substrate, setSubstrate] = useState(() => ({ warrant: 0.9, risk: 0.1 }));
  const [isRunning, setIsRunning] = useState(false);
  const interval = useRef(null);

  useEffect(() => {
    stack.setScenario('degrading', 1.0);
    setState(cte.getState());
    return () => stopSim();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const tick = () => {
    const ss = stack.step();
    cte.step({
      simTime: ss.simTime, warrant: ss.risk.warrant, risk: ss.risk.R,
      llcJump: ss.scalars.llcJump, vetoCount: ss.veto.analogVetoCount,
      crlResidual: ss.crl?.residual
    });
    setSubstrate({ warrant: ss.risk.warrant, risk: ss.risk.R });
    setState(cte.getState());
  };

  const startSim = () => {
    stack.start();
    setIsRunning(true);
    interval.current = setInterval(tick, 120);
  };
  const stopSim = () => {
    stack.stop();
    setIsRunning(false);
    if (interval.current) { clearInterval(interval.current); interval.current = null; }
  };
  const toggle = () => (isRunning ? stopSim() : startSim());

  const reset = () => { stopSim(); stack.reset(); cte.reset(); setState(cte.getState()); setSubstrate({ warrant: 0.9, risk: 0.1 }); };
  const onChallenge = (id, strength) => { cte.challenge(id, strength, strength >= 0.8 ? 'strong counter-evidence' : 'weak challenge'); setState(cte.getState()); };
  const onCorroborate = (id) => { cte.corroborate(id); setState(cte.getState()); };
  const onRollback = (v) => { cte.rollbackTo(v); setState(cte.getState()); };
  const onDrift = () => { cte.attemptSilentDrift(); setState(cte.getState()); };

  return (
    <div className="cview">
      <section className="section full-width">
        <div className="cview-banner">
          <div>
            <h2 className="cview-title">The system must remain correctable</h2>
            <p className="cview-sub">
              Ground Truth is the canon of <em>warranted</em> claims. Stronger admissible evidence
              beats canon consistency (Correction Supremacy, Art. XVIII); no change to a truth
              judgment is silent (Anti-Silent-Drift, Art. XIX); conserved facts are permanent memory.
            </p>
          </div>
          <div className="cview-substrate">
            <div className="cview-gauge">
              <span className="cview-g-k">warrant</span>
              <span className="cview-g-v" style={{ color: warrantColor(substrate.warrant) }}>{substrate.warrant.toFixed(2)}</span>
            </div>
            <div className="cview-gauge">
              <span className="cview-g-k">risk R</span>
              <span className="cview-g-v" style={{ color: riskColor(substrate.risk) }}>{substrate.risk.toFixed(2)}</span>
            </div>
          </div>
        </div>
      </section>

      <div className="cview-grid">
        <div className="cview-col">
          <section className="section">
            <h2>Ground Truth — Canon &amp; Degrees of Reliance</h2>
            <GroundTruthCanon
              claims={state.claims} counts={state.counts}
              onChallenge={onChallenge} onCorroborate={onCorroborate}
            />
          </section>
        </div>

        <div className="cview-col">
          <section className="section">
            <h2>Controls</h2>
            <div className="cview-controls">
              <button className={`cview-btn ${isRunning ? 'run' : 'stop'}`} onClick={toggle}>
                {isRunning ? '⏸ Pause governance' : '▶ Run governance'}
              </button>
              <button className="cview-btn neutral" onClick={reset}>↺ Reset</button>
              <button className="cview-btn drift" onClick={onDrift}>⚡ Attempt silent drift</button>
            </div>
            <p className="cview-help">
              Run the substrate (warrant degrades over time → projected claims get challenged).
              Use a claim's <strong>challenge</strong> to contest canon, <strong>corroborate</strong>
              to raise a provisional claim over its bar, and the <strong>rollback</strong> chips to
              restore a prior epistemic state.
            </p>
          </section>

          <section className="section">
            <h2>Separation of Epistemic Powers</h2>
            <SeparationOfPowers powers={state.powers} selfReferenceNote={state.selfReferenceNote} version={state.version} />
          </section>

          <section className="section">
            <h2>Unknown Register (Blind-Spot Office)</h2>
            <UnknownRegister unknownRegister={state.unknownRegister} />
          </section>
        </div>
      </div>

      <section className="section full-width">
        <h2>The Archive — Versioned Constitutional Events &amp; Rollback</h2>
        <ConstitutionalLog archive={state.archive} snapshots={state.snapshots} onRollback={onRollback} />
      </section>
    </div>
  );
}

function warrantColor(w) { return w > 0.66 ? '#22a06b' : w > 0.4 ? '#f0a500' : '#dc4d4d'; }
function riskColor(r) { return r < 0.33 ? '#22a06b' : r < 0.66 ? '#f0a500' : '#dc4d4d'; }

export default ConstitutionView;
