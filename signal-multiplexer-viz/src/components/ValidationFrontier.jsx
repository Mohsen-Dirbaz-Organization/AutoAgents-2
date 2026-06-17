import { useState, useCallback } from 'react';
import { runValidationCampaign, VALIDATION_STATUS_META } from '../simulation/ValidationHarness';
import './ValidationFrontier.css';

/**
 * The Validation Frontier — Void V-1 made runnable.
 *
 * The EVD V3 extraction named the project's true frontier as *validation
 * closure*, not another module. This panel runs the README's Open Obligations
 * (O-1 … O-11) whose acceptance condition is a computation, and reports a
 * measured result — while keeping the obligations that need external/measured
 * data honestly `owed` (the projected/measured seam, C-1).
 */
function ValidationFrontier() {
  const [campaign, setCampaign] = useState(() => runValidationCampaign({ seed: 0x5eed, trials: 2000 }));

  const rerun = useCallback(() => {
    // a fresh seed each click → re-confirm the bars are not seed-specific.
    const seed = (Math.random() * 0xffffffff) >>> 0;
    setCampaign(runValidationCampaign({ seed, trials: 2000 }));
  }, []);

  const { results, summary, seed } = campaign;

  return (
    <div className="vf-panel">
      <p className="vf-preamble">
        The V3 extraction named the frontier: <em>validation closure</em>, not another module.
        This runs the README&apos;s open obligations whose acceptance is a <strong>computation</strong>.
        Data-bound obligations (external corpus, <em>measured</em> silicon) stay
        honestly <strong>owed</strong> — the harness will not synthesize them.
      </p>

      <div className="vf-summary">
        <Chip n={summary.resolved} status="resolved" />
        <Chip n={summary.verifiedInSim} status="verified-in-sim" />
        <Chip n={summary.advanced} status="advanced" />
        <Chip n={summary.owed} status="owed" />
        <div className={`vf-reg ${summary.regressions === 0 ? 'ok' : 'bad'}`}>
          {summary.regressions === 0 ? '✓ no regressions' : `⚠ ${summary.regressions} regression(s)`}
        </div>
      </div>

      <div className="vf-rerun-row">
        <button className="vf-rerun" onClick={rerun}>Re-run campaign (new seed)</button>
        <span className="vf-seed">seed 0x{seed.toString(16)} · 2000 trials · deterministic</span>
      </div>

      <ul className="vf-list">
        {results.map((r) => {
          const meta = VALIDATION_STATUS_META[r.status];
          return (
            <li key={r.id} className="vf-item">
              <div className="vf-item-head">
                <span className="vf-id">{r.id}</span>
                <span className="vf-title">{r.title}</span>
                <span className="vf-badge" style={{ background: meta.color }}>
                  {r.pass === false ? '⚠ ' : ''}{meta.label}
                </span>
              </div>
              <div className="vf-accept"><span className="vf-k">exit</span> {r.acceptance}</div>
              <div className="vf-metrics">
                {Object.entries(r.metrics).map(([k, v]) => (
                  <span key={k} className="vf-metric">
                    <span className="vf-mk">{k}</span>
                    <span className="vf-mv">{v}</span>
                  </span>
                ))}
              </div>
              <div className="vf-caveat">{r.caveat}</div>
            </li>
          );
        })}
      </ul>
    </div>
  );
}

function Chip({ n, status }) {
  const meta = VALIDATION_STATUS_META[status];
  return (
    <div className="vf-chip" style={{ borderColor: meta.color }}>
      <span className="vf-chip-n" style={{ color: meta.color }}>{n}</span>
      <span className="vf-chip-l">{meta.label}</span>
    </div>
  );
}

export default ValidationFrontier;
