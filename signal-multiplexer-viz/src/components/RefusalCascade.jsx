import './RefusalCascade.css';

/**
 * Architecture of Refusal (Lane B). The EPU is a single pipeline in which each
 * stage can only NARROW what the next is permitted to do. We render the convex
 * command box shrinking S0 → S4, the admissible command modes at the current
 * risk, and the electrically-isolated analog veto.
 */
function RefusalCascade({ admissible, veto, risk }) {
  if (!admissible) return null;
  const { cascade, modes, boxWidth } = admissible;

  return (
    <div className="refusal-cascade">
      <div className="rc-modes">
        <div className="rc-modes-label">Admissible command set U_ad(R) — peeled from the permissive end as R rises</div>
        <div className="rc-mode-row">
          {modes.map(m => (
            <div
              key={m.id}
              className={`rc-mode ${m.admitted ? 'admitted' : 'refused'} ${m.id === 'STOP' ? 'mrc' : ''}`}
              title={`threshold risk ${m.risk <= 1 ? m.risk.toFixed(2) : 'always'}`}
            >
              <span className="rc-mode-id">{m.id}</span>
              <span className="rc-mode-state">{m.admitted ? '✓ admitted' : '✕ refused'}</span>
            </div>
          ))}
        </div>
      </div>

      <div className="rc-cascade">
        <div className="rc-cascade-label">S0 → S4 cascade: each stage can only narrow the convex command box</div>
        {cascade.map((s, i) => (
          <div key={s.stage} className="rc-stage">
            <div className="rc-stage-head">
              <span className="rc-stage-id">{s.stage}</span>
              <span className="rc-stage-name">{s.name}</span>
              <span className={`rc-stage-standing ${standingClass(s.standing)}`}>{s.standing}</span>
            </div>
            <div className="rc-bar-track">
              <div
                className="rc-bar-fill"
                style={{ width: `${Math.max(2, s.width * 100)}%`, background: barColor(i) }}
              />
              {i > 0 && (
                <div
                  className="rc-bar-ghost"
                  style={{ left: `${Math.max(2, cascade[i - 1].width * 100)}%` }}
                  title="previous stage width — narrowing only"
                />
              )}
            </div>
          </div>
        ))}
      </div>

      <div className={`rc-veto ${veto?.armed ? 'armed' : ''}`}>
        <div className="rc-veto-icon">{veto?.armed ? '⚡' : '🛡'}</div>
        <div className="rc-veto-body">
          <div className="rc-veto-title">
            S4 — timing-contracted analog veto {veto?.armed ? '— FIRING' : ''}
          </div>
          <div className="rc-veto-detail">
            Electrically isolated from the digital Queen. Latency
            <strong> ~{veto?.lastVetoLatencyNs ?? 32} ns (measured)</strong> vs.
            ~{veto?.digitalWindowNs ?? 1000} ns digital window. Fired {veto?.analogVetoCount ?? 0}×.
          </div>
        </div>
      </div>

      <div className="rc-footer">
        Final command-box width: <strong>{(boxWidth * 100).toFixed(0)}%</strong> at risk R = {(risk?.R ?? 0).toFixed(2)}
      </div>
    </div>
  );
}

function barColor(i) {
  const colors = ['#22a6b3', '#3498db', '#9b59b6', '#e67e22', '#e74c3c'];
  return colors[i % colors.length];
}
function standingClass(s) {
  if (s.startsWith('Foundation')) return 'foundation';
  if (s.startsWith('Proposed/real')) return 'real';
  return 'proposed';
}

export default RefusalCascade;
