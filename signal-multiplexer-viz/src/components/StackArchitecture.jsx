import './StackArchitecture.css';

/**
 * The eight-thread stack (Lanes A–H). Two laws run in opposite directions and
 * meet at the EPU: an authority law flows top-down, a consequence law flows
 * bottom-up. The authority law changes with autonomy level; the consequence
 * law (the antitone narrowing) is invariant across all of them.
 */
function StackArchitecture({ lanes, risk, veto, scalars }) {
  if (!lanes) return null;
  return (
    <div className="stack-architecture">
      <div className="sa-laws">
        <div className="sa-law authority">↓ Authority law — who may decide (varies with autonomy level)</div>
        <div className="sa-law consequence">↑ Consequence law — what an error may do (invariant)</div>
      </div>

      <div className="sa-lanes">
        {lanes.map(l => (
          <div key={l.lane} className={`sa-lane law-${l.law}`}>
            <div className="sa-lane-tag">Lane {l.lane}</div>
            <div className="sa-lane-body">
              <div className="sa-lane-name">{l.name}</div>
              <div className="sa-lane-role">{l.role}</div>
            </div>
            <div className={`sa-lane-flag ${l.law}`}>{l.law === 'authority' ? 'AUTH' : 'CONSEQ'}</div>
          </div>
        ))}
      </div>

      <div className="sa-epu">
        <div className="sa-epu-title">EPU — Enforcement / Protection Unit</div>
        <div className="sa-epu-sub">where the two laws meet (S0 → S4 terminal pipeline)</div>
        <div className="sa-epu-stats">
          <div className="sa-epu-stat">
            <span className="sa-epu-k">Risk R</span>
            <span className="sa-epu-v" style={{ color: riskColor(risk?.R ?? 0) }}>
              {(risk?.R ?? 0).toFixed(2)}
            </span>
          </div>
          <div className="sa-epu-stat">
            <span className="sa-epu-k">Warrant</span>
            <span className="sa-epu-v">{(risk?.warrant ?? 0).toFixed(2)}</span>
          </div>
          <div className="sa-epu-stat">
            <span className="sa-epu-k">ξ (log-dev)</span>
            <span className="sa-epu-v">{(scalars?.xi ?? 0).toFixed(3)}</span>
          </div>
          <div className="sa-epu-stat">
            <span className="sa-epu-k">S (parity)</span>
            <span className="sa-epu-v">{scalars?.S > 0 ? '+1' : '−1'}</span>
          </div>
          <div className={`sa-epu-stat veto ${veto?.armed ? 'armed' : ''}`}>
            <span className="sa-epu-k">Analog veto</span>
            <span className="sa-epu-v">{veto?.armed ? `⚡ ~${veto.lastVetoLatencyNs} ns` : 'idle'}</span>
          </div>
        </div>
      </div>
    </div>
  );
}

function riskColor(R) {
  if (R < 0.33) return '#228B22';
  if (R < 0.66) return '#DAA520';
  return '#DC143C';
}

export default StackArchitecture;
