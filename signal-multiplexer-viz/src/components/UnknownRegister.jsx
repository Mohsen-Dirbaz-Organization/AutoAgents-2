import './UnknownRegister.css';

const KIND_META = {
  'thin-evidence':        { label: 'thin evidence', color: '#f0a500' },
  'untested-assumption':  { label: 'untested assumption', color: '#e67e22' },
  'degraded-independence':{ label: 'degraded independence', color: '#dc4d4d' },
  'low-warrant':          { label: 'low warrant', color: '#c0392b' },
  'recurrent-failure':    { label: 'recurrent failure', color: '#9333ea' }
};

/**
 * The Unknown Register (Const. of Truth, Art. XXVIII) — a constitutional
 * artifact recording epistemic risk that travels with the system across
 * versions. The Office of Blind-Spot Discovery (Art. XXVI) maintains it; the
 * Right to Be Surprised (Art. XXV) treats discovered error as civic success.
 */
function UnknownRegister({ unknownRegister }) {
  if (!unknownRegister) return null;
  return (
    <div className="ureg">
      {unknownRegister.length === 0 && <div className="ureg-empty">register clear — no flagged epistemic risk</div>}
      {unknownRegister.map(u => {
        const m = KIND_META[u.kind] || { label: u.kind, color: '#64748b' };
        return (
          <div key={u.id} className="ureg-item" style={{ '--c': m.color }}>
            <span className="ureg-kind" style={{ background: m.color }}>{m.label}</span>
            <span className="ureg-risk">{u.risk}</span>
            <span className="ureg-since">since v{u.sinceVersion}</span>
          </div>
        );
      })}
    </div>
  );
}

export default UnknownRegister;
