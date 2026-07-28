import { COVERAGE_META, GAP_CLOSURE_PLAN, GAP_PLAN_SUMMARY, WAVES } from '../data/programCoverage';
import './GapClosurePlan.css';

/**
 * Gap-Closure Plan — Void V-2 (program-gap-closure-plan) made concrete.
 *
 * The coverage map named 11 gaps and a ~55-80 person-day total but stopped
 * there. This turns each gap into a workstream item — owner, acquisition route,
 * exit condition, target coverage — sequenced into waves by what it unblocks.
 * Closing a gap raises *source-material coverage*, never built/measured silicon.
 */
function GapClosurePlan({ onSelect }) {
  const s = GAP_PLAN_SUMMARY;
  return (
    <div className="gcp">
      <div className="gcp-rollup">
        <div className="gcp-rollup-total">
          <span className="gcp-rollup-num">{s.gaps}</span>
          <span className="gcp-rollup-lbl">gaps</span>
        </div>
        <div className="gcp-rollup-total">
          <span className="gcp-rollup-num">{s.effortLo}–{s.effortHi}</span>
          <span className="gcp-rollup-lbl">person-days</span>
        </div>
        <div className="gcp-rollup-pri">
          <span className="gcp-pri pri-high">{s.byPriority.HIGH} HIGH</span>
          <span className="gcp-pri pri-medium">{s.byPriority.MEDIUM} MED</span>
          <span className="gcp-pri pri-low">{s.byPriority.LOW} LOW</span>
        </div>
        <p className="gcp-rollup-note">
          Each gap has an <strong>owner</strong>, an <strong>acquisition route</strong>, and an
          <strong> exit condition</strong>. Closing one raises <em>source-material coverage</em> — not silicon.
        </p>
      </div>

      {WAVES.map(wave => {
        const items = GAP_CLOSURE_PLAN.filter(g => g.wave === wave.id);
        const wsum = s.byWave.find(w => w.id === wave.id);
        return (
          <div key={wave.id} className={`gcp-wave wave-${wave.id}`}>
            <div className="gcp-wave-head">
              <span className="gcp-wave-name">{wave.name}</span>
              <span className="gcp-wave-meta">{wsum.count} gaps · {wsum.effortLo}–{wsum.effortHi}d</span>
            </div>
            <p className="gcp-wave-rationale">{wave.rationale}</p>
            <div className="gcp-items">
              {items.map(g => {
                const tgt = COVERAGE_META[g.target];
                return (
                  <div key={g.id} className="gcp-item">
                    <div className="gcp-item-head">
                      <button className="gcp-item-id" onClick={() => onSelect?.(g.id)} title="Show in grid">#{g.id}</button>
                      <span className="gcp-item-name">{g.name}</span>
                      <span className={`gcp-pri pri-${g.priority.toLowerCase()}`}>{g.priority}</span>
                      <span className="gcp-item-eff">{g.effortLabel}d</span>
                    </div>
                    <div className="gcp-item-grid">
                      <div className="gcp-field">
                        <span className="gcp-fk">Owner</span>
                        <span className="gcp-fv">{g.owner}</span>
                      </div>
                      <div className="gcp-field">
                        <span className="gcp-fk">Target</span>
                        <span className="gcp-fv">
                          <span className="gcp-target" style={{ background: tgt.color }}>{tgt.label}</span>
                        </span>
                      </div>
                      <div className="gcp-field gcp-wide">
                        <span className="gcp-fk">Acquisition</span>
                        <span className="gcp-fv">{g.acquisition}</span>
                      </div>
                      <div className="gcp-field gcp-wide">
                        <span className="gcp-fk">Exit condition</span>
                        <span className="gcp-fv">{g.exit}</span>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        );
      })}
    </div>
  );
}

export default GapClosurePlan;
