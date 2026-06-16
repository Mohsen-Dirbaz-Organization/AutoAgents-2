import './GroundTruthCanon.css';

const RELIANCE_META = {
  warranted:   { label: 'Warranted (canon)', color: '#22a06b' },
  provisional: { label: 'Provisional',       color: '#f0a500' },
  hypothesis:  { label: 'Hypothesis',        color: '#8a94a6' },
  retracted:   { label: 'Retracted',         color: '#dc4d4d' }
};

const ORDER = ['warranted', 'provisional', 'hypothesis', 'retracted'];

/**
 * Ground Truth = the canon of WARRANTED claims (Const. of Truth, Art. I–II).
 * Each claim shows its degree of reliance (Art. VII), modality (realis = actual
 * / irrealis = projected), whether it is a conserved fact (permanent memory),
 * and its admissible-evidence strength vs the warrant bar (∝ consequence,
 * Art. XVI). Every claim is challengeable (Art. V.1) — the operator can contest
 * it or add independent corroboration (Art. XXXIV).
 */
function GroundTruthCanon({ claims, counts, onChallenge, onCorroborate }) {
  if (!claims) return null;
  const sorted = [...claims].sort((a, b) =>
    ORDER.indexOf(a.reliance) - ORDER.indexOf(b.reliance) || b.evidence - a.evidence);

  return (
    <div className="gtc">
      <div className="gtc-counts">
        {ORDER.map(r => (
          <div key={r} className="gtc-count" style={{ '--c': RELIANCE_META[r].color }}>
            <span className="gtc-count-n">{counts[r] || 0}</span>
            <span className="gtc-count-l">{RELIANCE_META[r].label}</span>
          </div>
        ))}
      </div>

      <div className="gtc-list">
        {sorted.map(c => {
          const m = RELIANCE_META[c.reliance];
          const over = c.evidence >= (c.bar ?? 0.75);
          return (
            <div key={c.id} className={`gtc-claim reliance-${c.reliance}`} style={{ '--c': m.color }}>
              <div className="gtc-claim-head">
                <span className="gtc-badge" style={{ background: m.color }}>{m.label}</span>
                <span className={`gtc-modality ${c.modality}`}>
                  {c.modality === 'realis' ? '● realis (actual)' : '○ irrealis (projected)'}
                </span>
                {c.conserved && <span className="gtc-conserved" title="conservation law → permanent memory (§9.6.3)">⬛ conserved</span>}
                {c.independent && <span className="gtc-indep" title="independently corroborated (Art. XXXIV)">⚖ corroborated</span>}
                <span className="gtc-domain">{c.domain}</span>
              </div>
              <div className="gtc-statement">{c.statement}</div>
              <div className="gtc-warrant">
                <div className="gtc-bar-track">
                  <div className="gtc-bar-fill" style={{ width: `${c.evidence * 100}%`, background: m.color }} />
                  <div className="gtc-bar-mark" style={{ left: `${(c.bar ?? 0.75) * 100}%` }} title={`warrant bar (${c.consequence}-consequence)`} />
                </div>
                <span className="gtc-evidence">
                  warrant {c.evidence.toFixed(2)} / bar {(c.bar ?? 0.75).toFixed(2)} · {over ? 'meets' : 'below'} · {c.contestations} challenges
                </span>
              </div>
              {c.reliance !== 'retracted' && (
                <div className="gtc-actions">
                  <button className="gtc-act challenge" onClick={() => onChallenge(c.id, 0.95)}>⚔ challenge (strong)</button>
                  <button className="gtc-act weak" onClick={() => onChallenge(c.id, 0.5)}>weak challenge</button>
                  {!c.independent && c.reliance !== 'warranted' && (
                    <button className="gtc-act corrob" onClick={() => onCorroborate(c.id)}>⚖ corroborate</button>
                  )}
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default GroundTruthCanon;
