import './ConstitutionalLog.css';

const EVENT_META = {
  canonization:   { icon: '✔', color: '#22a06b', label: 'canonized' },
  retraction:     { icon: '⤺', color: '#dc4d4d', label: 'retracted' },
  reclassification: { icon: '↕', color: '#f0a500', label: 'reclassified' },
  erasure:        { icon: '🗑', color: '#8a94a6', label: 'erased (with lineage)' },
  'drift-surfaced': { icon: '⚡', color: '#9333ea', label: 'silent-drift surfaced' }
};

/**
 * The Archive (Art. IX) — versioned custody of canon; erasure without lineage
 * is unconstitutional. Every change to a truth judgment appears here as a
 * constitutional event (Anti-Silent-Drift, Art. XIX). Snapshots provide
 * temporal reversibility: roll the canon back to a prior epistemic state.
 */
function ConstitutionalLog({ archive, snapshots, onRollback }) {
  if (!archive) return null;
  return (
    <div className="clog">
      <div className="clog-snapshots">
        <span className="clog-snap-label">Temporal rollback →</span>
        {snapshots && snapshots.slice().reverse().map(s => (
          <button key={s.version} className="clog-snap" onClick={() => onRollback(s.version)}
            title={`restore canon as of v${s.version} (${s.canonSize} claims)`}>
            v{s.version}<span className="clog-snap-meta">·{s.canonSize}</span>
          </button>
        ))}
      </div>

      <div className="clog-events">
        {archive.length === 0 && <div className="clog-empty">no constitutional events yet</div>}
        {archive.map((e, i) => {
          const m = EVENT_META[e.type] || { icon: '•', color: '#64748b', label: e.type };
          return (
            <div key={`${e.v}-${i}`} className="clog-event" style={{ '--c': m.color }}>
              <span className="clog-ev-icon" style={{ color: m.color }}>{m.icon}</span>
              <span className="clog-ev-v">v{e.v}</span>
              <span className="clog-ev-body">
                <span className="clog-ev-label" style={{ color: m.color }}>{m.label}</span>
                <span className="clog-ev-stmt">{e.statement}</span>
                {e.reason && <span className="clog-ev-reason">{e.reason}</span>}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default ConstitutionalLog;
