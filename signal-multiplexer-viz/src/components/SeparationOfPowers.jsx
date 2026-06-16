import './SeparationOfPowers.css';

/**
 * Separation of Epistemic Powers (Const. of Truth, Art. VIII–XIV): epistemic
 * power may not concentrate. No single pathway may be both sole proposer and
 * sole judge (Art. XIV). Internal agreement is not warrant (Art. XXX).
 */
function SeparationOfPowers({ powers, selfReferenceNote, version }) {
  if (!powers) return null;
  return (
    <div className="sop">
      <div className="sop-version">epistemic version <strong>v{version}</strong> · powers are structurally independent</div>
      <div className="sop-grid">
        {powers.map(p => (
          <div key={p.id} className={`sop-power ${p.active ? 'active' : ''}`}>
            <div className="sop-power-name">{p.name}{p.active && <span className="sop-pulse" />}</div>
            <div className="sop-power-mandate">{p.mandate}</div>
          </div>
        ))}
      </div>
      <div className="sop-selfref">⚠ {selfReferenceNote}</div>
    </div>
  );
}

export default SeparationOfPowers;
