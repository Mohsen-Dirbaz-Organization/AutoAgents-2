import './StressFieldPanel.css';

/**
 * Stress-Field Constraint Panel — Source #3 Part V (T5), Void V-6.
 *
 * Part V reframes constraint satisfaction as a *stress field*: a constraint
 * under pressure carries stress; a violated constraint is a stress singularity.
 * This panel renders the substrate's live invariants through that lens — each
 * stress is DERIVED from existing engine state (no new claim is manufactured).
 *
 * The safety design shows up directly: as the *driving* stresses (falling
 * warrant, rising risk/threat) climb, the *protected* invariants — antitone
 * monotonicity and Q=0 conservation — stay near zero, because the system
 * relieves stress by NARROWING the admissible set (fail-closed), not by
 * violating. Stress yields to contraction, never to expansion.
 */
const clamp01 = (x) => Math.max(0, Math.min(1, x));

function stressColor(s) {
  // green (low) → amber → red (high)
  if (s < 0.33) return '#22a06b';
  if (s < 0.66) return '#d98b1f';
  return '#c0392b';
}

function StressFieldPanel({ state }) {
  if (!state) return null;
  const { risk, monotonicity, crl, scalars, memory } = state;

  const maxLoad = memory?.tiers?.reduce((m, t) => Math.max(m, t.entries.length / t.capacity), 0) ?? 0;
  const residual = crl?.residual ?? 0;

  // Each field: { name, kind: 'driver' | 'protected', stress in [0,1], detail }
  const fields = [
    { name: 'Benign-warrant deficit', kind: 'driver', stress: clamp01(1 - risk.warrant),
      detail: `warrant ${risk.warrant.toFixed(2)} — how far the benign interpretation has fallen` },
    { name: 'Behavioral-risk envelope', kind: 'driver', stress: clamp01(risk.R),
      detail: `R ${risk.R.toFixed(2)} — pressure on the admissible-action envelope` },
    { name: 'Trajectory threat', kind: 'driver', stress: clamp01(risk.trajectoryThreat),
      detail: `max threat ${risk.trajectoryThreat.toFixed(2)} from enforced-causality ingest` },
    { name: 'Developmental drift (LLC)', kind: 'driver', stress: scalars.llcJump ? 1 : clamp01((scalars.llc - 1) / 2),
      detail: scalars.llcJump ? 'LLC jump — quarantined this tick' : `LLC ${scalars.llc.toFixed(2)} consolidation pressure` },
    { name: 'Memory pressure', kind: 'driver', stress: clamp01(maxLoad),
      detail: `peak tier load ${(maxLoad * 100).toFixed(0)}% — a load the shadow-price eviction relieves` },
    { name: 'Antitone monotonicity', kind: 'protected', stress: clamp01(monotonicity.violationRate),
      detail: `violation rate ${(monotonicity.violationRate * 100).toFixed(1)}% — held at 0 under discipline` },
    { name: 'Conservation Q = 0', kind: 'protected', stress: clamp01(residual / 1e-6),
      detail: `|Q| residual ${residual.toExponential(1)} — constraint held at machine zero` },
  ];

  const peak = Math.max(...fields.map((f) => f.stress));
  const drivers = fields.filter((f) => f.kind === 'driver');
  const protectedF = fields.filter((f) => f.kind === 'protected');
  const meanDriver = drivers.reduce((a, f) => a + f.stress, 0) / drivers.length;
  const meanProtected = protectedF.reduce((a, f) => a + f.stress, 0) / protectedF.length;

  return (
    <div className="sf-panel">
      <p className="sf-preamble">
        Constraint satisfaction as a <strong>stress field</strong> (Source #3, Part V). Each value is
        derived from live state. Watch the seam: as <em>driver</em> stresses climb, the
        <em> protected</em> invariants stay near zero — the system yields by <strong>contracting</strong>
        the admissible set, never by violating.
      </p>

      <div className="sf-rollup">
        <div className="sf-roll">
          <span className="sf-roll-num" style={{ color: stressColor(meanDriver) }}>{(meanDriver * 100).toFixed(0)}%</span>
          <span className="sf-roll-lbl">mean driver stress</span>
        </div>
        <div className="sf-roll">
          <span className="sf-roll-num" style={{ color: stressColor(meanProtected) }}>{(meanProtected * 100).toFixed(0)}%</span>
          <span className="sf-roll-lbl">mean protected stress</span>
        </div>
        <div className="sf-roll">
          <span className="sf-roll-num" style={{ color: stressColor(peak) }}>{(peak * 100).toFixed(0)}%</span>
          <span className="sf-roll-lbl">peak stress</span>
        </div>
      </div>

      {['driver', 'protected'].map((kind) => (
        <div key={kind} className="sf-group">
          <div className="sf-group-head">{kind === 'driver' ? 'Driver constraints (push the system)' : 'Protected invariants (must stay slack)'}</div>
          {fields.filter((f) => f.kind === kind).map((f) => (
            <div key={f.name} className="sf-row" title={f.detail}>
              <span className="sf-name">{f.name}</span>
              <div className="sf-track">
                <div className="sf-fill" style={{ width: `${f.stress * 100}%`, background: stressColor(f.stress) }} />
              </div>
              <span className="sf-val" style={{ color: stressColor(f.stress) }}>{(f.stress * 100).toFixed(0)}%</span>
            </div>
          ))}
        </div>
      ))}

      <p className="sf-note">
        A protected invariant climbing into the amber/red band is a <em>real</em> stress singularity — it
        means a safety mechanism is at its limit, not merely that risk is high.
      </p>
    </div>
  );
}

export default StressFieldPanel;
