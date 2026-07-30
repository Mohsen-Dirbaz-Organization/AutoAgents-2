import { useState, useMemo } from 'react';
import { STANDING, STANDING_ORDER } from '../data/canon/standing';
import { SYMBOLS, SYMBOL_RULE } from '../data/canon/symbols';
import { NUMBERS } from '../data/canon/numbers';
import { CLAIMS } from '../data/canon/claims';
import { GATES } from '../data/canon/gates';
import { RETIREMENTS } from '../data/canon/retirements';
import { OBLIGATIONS, PRIORITY_META } from '../data/canon/obligations';
import { runCanonValidation } from '../simulation/CanonValidator';
import { runPlanningAnalysis } from '../simulation/PlanningEngine';
import { TASKS } from '../data/canon/planning';
import './CanonView.css';

const KIND_META = {
  measured: { color: '#22a06b' }, derived: { color: '#2b6cb0' },
  simulated: { color: '#6b46c1' }, target: { color: '#DAA520' },
  constructed: { color: '#667089' }
};

/**
 * Canon & Integrity — the EPU Companion's "one ledger, many renderings" made
 * runnable: the canonical registers (claims, numbers, symbols, gates,
 * retirements, obligations) plus the CanonValidator's mechanical enforcement
 * of the Definition of Done. A blocking finding means non-releasable.
 */
function CanonView() {
  const [result, setResult] = useState(() => runCanonValidation());
  const [sevFilter, setSevFilter] = useState(null);
  const [kindFilter, setKindFilter] = useState(null);
  const planning = useMemo(() => runPlanningAnalysis(), []);

  const rerun = () => setResult(runCanonValidation());

  const shownFindings = useMemo(
    () => (sevFilter ? result.findings.filter((f) => f.severity === sevFilter) : result.findings),
    [result, sevFilter]
  );
  const shownNumbers = kindFilter ? NUMBERS.filter((n) => n.kind === kindFilter) : NUMBERS;

  return (
    <div className="canon">
      {/* Release banner + DoD scorecard */}
      <section className="section full-width">
        <div className={`cn-release ${result.releasable ? 'ok' : 'blocked'}`}>
          <div className="cn-release-head">
            <span className="cn-release-badge">{result.releasable ? '✓ RELEASABLE' : '✕ NOT RELEASABLE'}</span>
            <span className="cn-release-sub">
              {result.counts.blocking} blocking · {result.counts.warning} listed (orphans preserved, not suppressed)
            </span>
            <button className="cn-rerun" onClick={rerun}>Re-run validation</button>
          </div>
          <div className="cn-dod">
            {result.dod.map((d) => (
              <div key={d.item} className={`cn-dod-row ${d.pass ? 'pass' : 'fail'}`} title={d.note || ''}>
                <span className="cn-dod-mark">{d.pass ? '✓' : '✕'}</span>
                <span className="cn-dod-n">DoD-{d.item}</span>
                <span className="cn-dod-text">{d.text}{d.note ? ' *' : ''}</span>
              </div>
            ))}
          </div>
          <p className="cn-note">
            * structural rows asserted by construction and observed in the running app.
            Registers: {Object.entries(result.registers).map(([k, v]) => `${v} ${k}`).join(' · ')}.
          </p>
        </div>
      </section>

      <div className="cn-grid">
        {/* Findings */}
        <section className="section">
          <h2>Validator findings</h2>
          <div className="cn-sev-row">
            {['blocking', 'warning'].map((s) => (
              <button key={s}
                className={`cn-sev ${s} ${sevFilter === s ? 'active' : ''}`}
                onClick={() => setSevFilter(sevFilter === s ? null : s)}>
                {result.counts[s]} {s}
              </button>
            ))}
          </div>
          {shownFindings.length === 0 && <p className="cn-empty">No findings at this filter.</p>}
          <ul className="cn-findings">
            {shownFindings.map((f) => (
              <li key={f.id} className={`cn-finding ${f.severity}`}>
                <div className="cn-finding-head">
                  <span className="cn-finding-check">{f.check}</span>
                  <span className="cn-finding-subject">{f.subject}</span>
                </div>
                <div className="cn-finding-msg">{f.message}</div>
                <div className="cn-finding-remedy">{f.remedy}</div>
              </li>
            ))}
          </ul>
          <p className="cn-note">
            The two listed orphans are deliberate: an orphan hazard (device non-ideality — needs measured
            device data) and an orphan requirement (field validation). DoD-6 requires they be listed, not
            suppressed; "closing" them cosmetically would be the silent drift this layer exists to prevent.
          </p>
        </section>

        {/* Standing ladder + claims */}
        <section className="section">
          <h2>Claims register — standing ladder</h2>
          <div className="cn-ladder">
            {STANDING_ORDER.map((k) => (
              <span key={k} className="cn-standing" style={{ background: STANDING[k].color }} title={`${STANDING[k].meaning} — ${STANDING[k].authority}`}>
                {STANDING[k].label}
              </span>
            ))}
          </div>
          <ul className="cn-claims">
            {CLAIMS.map((c) => (
              <li key={c.claim_id} className="cn-claim">
                <div className="cn-claim-head">
                  <span className="cn-standing sm" style={{ background: STANDING[c.standing]?.color || '#999' }}>
                    {STANDING[c.standing]?.label || c.standing}
                  </span>
                  <span className="cn-claim-stmt">{c.statement}</span>
                </div>
                <div className="cn-claim-meta">
                  <span><b>scope</b> {c.scope}</span>
                  <span><b>falsifier</b> {c.falsifier}</span>
                  <span><b>artifact</b> <code>{c.artifact}</code></span>
                </div>
              </li>
            ))}
          </ul>
        </section>
      </div>

      <div className="cn-grid">
        {/* Number cards */}
        <section className="section">
          <h2>Number cards — no naked figures</h2>
          <div className="cn-kind-row">
            {Object.keys(KIND_META).map((k) => (
              <button key={k}
                className={`cn-kind ${kindFilter === k ? 'active' : ''}`}
                style={{ '--c': KIND_META[k].color }}
                onClick={() => setKindFilter(kindFilter === k ? null : k)}>
                {NUMBERS.filter((n) => n.kind === k).length} {k}
              </button>
            ))}
          </div>
          <ul className="cn-numbers">
            {shownNumbers.map((n) => (
              <li key={n.number_id} className="cn-number" style={{ '--c': KIND_META[n.kind].color }}>
                <div className="cn-number-head">
                  <span className="cn-number-val">{n.value} {n.units}</span>
                  <span className="cn-number-kind" style={{ background: KIND_META[n.kind].color }}>{n.kind}</span>
                  {n.derive && <span className="cn-number-recompute" title="Recomputed from stated inputs by the validator">↻ recomputes</span>}
                </div>
                <div className="cn-number-q">{n.quantity}</div>
                <div className="cn-number-meta">
                  cfg <code>{n.configuration_id}</code> · unc: {n.uncertainty} · {n.scope}
                </div>
              </li>
            ))}
          </ul>
        </section>

        {/* Symbols + retirements */}
        <section className="section">
          <h2>Symbol collision register</h2>
          <p className="cn-note">{SYMBOL_RULE}</p>
          <table className="cn-symbols">
            <thead><tr><th>Glyph</th><th>Collision</th><th>Canonical names</th><th>In this repo</th></tr></thead>
            <tbody>
              {SYMBOLS.map((s) => (
                <tr key={s.glyph}>
                  <td className="cn-glyph">{s.glyph}</td>
                  <td className="cn-conflict">{s.conflict}</td>
                  <td>{s.canonical.map((c) => <code key={c}>{c}</code>)}</td>
                  <td className="cn-used">{s.usedInRepo}</td>
                </tr>
              ))}
            </tbody>
          </table>

          <h2 className="cn-h2-gap">Retirement register — removed, not owed</h2>
          <ul className="cn-retire">
            {RETIREMENTS.map((r) => (
              <li key={r.retirement_id} className="cn-retire-item">
                <div className="cn-retire-name">✂ {r.construct}</div>
                <div className="cn-retire-reason">{r.reason}</div>
                <div className="cn-retire-disp"><b>{r.disposition.split(':')[0]}</b> — {r.appliedInRepo}</div>
              </li>
            ))}
          </ul>
        </section>
      </div>

      {/* Gates + obligations */}
      <section className="section full-width">
        <h2>Gate contracts — every threshold has a hazard behind it</h2>
        <div className="cn-gates">
          {GATES.map((g) => (
            <div key={g.gate_id} className="cn-gate">
              <div className="cn-gate-head">
                <span className="cn-gate-name">{g.name}</span>
                <code className="cn-gate-reason">{g.reasonCode}</code>
              </div>
              <div className="cn-gate-inv">{g.invariant}</div>
              <div className="cn-gate-meta">
                <span><b>threshold</b> {g.threshold.value}</span>
                <span><b>provenance</b> {g.threshold.provenance}</span>
                <span><b>on failure</b> {g.failureSemantics}</span>
              </div>
            </div>
          ))}
        </div>
      </section>

      <section className="section full-width">
        <h2>Planning module — Rigorous Planning Framework, instantiated on the open obligations</h2>
        <PlanningPanel planning={planning} />
      </section>

      <section className="section full-width">
        <h2>Open-obligations register (P0–P3) — supersedes O-1…O-11 and the Void Map</h2>
        <table className="cn-obligations">
          <thead><tr><th>Pri</th><th>Obligation</th><th>Owner</th><th>Status</th><th>Absorbs</th></tr></thead>
          <tbody>
            {OBLIGATIONS.map((o) => (
              <tr key={o.obligation_id} className={o.status === 'open' ? 'open' : 'closed'}>
                <td><span className="cn-pri" style={{ background: PRIORITY_META[o.priority].color }}>{o.priority}</span></td>
                <td className="cn-ob-text">{o.text}{o.note ? <span className="cn-ob-note"> — {o.note}</span> : null}</td>
                <td className="cn-ob-owner">{o.owner}</td>
                <td className="cn-ob-status">{o.status}{o.closure ? <span className="cn-ob-closure" title={o.closure}> ✓</span> : null}</td>
                <td className="cn-ob-absorbs">{o.absorbs.join(', ') || '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  );
}

/**
 * PlanningPanel — renders the PlanningResult (Part X schema): every field is
 * ⟨value, method, grade⟩; grades are surfaced, not hidden.
 */
function PlanningPanel({ planning }) {
  const { result: r, invariants } = planning;
  const b = r.bounds.value;
  const effortOf = (id) => TASKS.find((t) => t.id === id)?.effortPd;

  return (
    <div className="cn-planning">
      <p className="cn-note">
        The framework's output schema is populated from the concrete instance (the open obligations):
        {' '}{r.instance.value.tasks} tasks · {r.instance.value.artifacts} artifacts · {r.instance.value.causalArcs} genuine
        causal arcs. Efforts are person-days, grade <b>modelled</b> — every bound below inherits that caveat.
      </p>

      <div className="cn-plan-grid">
        <div className="cn-plan-card">
          <div className="cn-plan-head">Bounds <Grade f={r.bounds} /></div>
          <div className="cn-plan-big">
            <span title="total work">W₁ = {b.W1} pd</span>
            <span title="critical path">W∞ = {b.Winf} pd</span>
            <span title="parallelism ceiling">Π = {b.Pi.toFixed(2)}</span>
          </div>
          <div className="cn-plan-line">
            critical path: {b.criticalPath.map((id) => `${id} (${effortOf(id)}pd)`).join(' → ')}
          </div>
          <div className="cn-plan-line warn">{b.ladderCost}</div>
        </div>

        <div className="cn-plan-card">
          <div className="cn-plan-head">Brent/Graham bands <Grade f={r.schedule} /></div>
          <table className="cn-plan-bands">
            <thead><tr><th>K</th><th>lower</th><th>Brent upper</th><th>Graham factor</th></tr></thead>
            <tbody>
              {r.schedule.value.bands.map((band) => (
                <tr key={band.K}>
                  <td>{band.K}</td>
                  <td>{band.lower.toFixed(1)} pd</td>
                  <td>{band.upper.toFixed(1)} pd</td>
                  <td>{band.grahamFactor.toFixed(2)}×</td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="cn-plan-line">{r.schedule.value.ceiling}</div>
        </div>

        <div className="cn-plan-card">
          <div className="cn-plan-head">Hazard census &amp; contention <Grade f={r.hazards} /></div>
          <div className="cn-plan-big">
            <span>RAW {r.hazards.value.RAW}</span>
            <span>WAW {r.hazards.value.WAW}</span>
            <span>WAR {r.hazards.value.WAR}</span>
          </div>
          <div className="cn-plan-line">
            dominant contention scope: <code>{r.contention.value.dominant?.scope}</code>
            {' '}({Math.round(r.contention.value.dominantShare * 100)}% of contended pairs)
          </div>
          <div className="cn-plan-line">{r.structure.value.feedback}</div>
        </div>

        <div className="cn-plan-card">
          <div className="cn-plan-head">Independence (Thm 2.2) <Grade f={r.independence} /></div>
          <div className="cn-plan-line">
            <b>{r.independence.value.icNotIr.length}</b> pairs are causally independent (I<sub>c</sub>) but
            resource-dependent (¬I<sub>r</sub>) — reorderable, not concurrent without arbitration.
          </div>
          <div className="cn-plan-line warn">{r.independence.value.frozenConventions.statement}</div>
          <ul className="cn-plan-frozen">
            {r.independence.value.frozenConventions.frozen.map((f) => <li key={f}>{f}</li>)}
          </ul>
        </div>
      </div>

      <div className="cn-plan-foot">
        <span className="cn-plan-head">Debts (V12):</span>
        {r.registers.value.debts.map((d) => (
          <span key={d.task} className="cn-plan-debt" title={d.debt}>{d.task}</span>
        ))}
        <span className="cn-plan-inv">
          {invariants.map((i) => (
            <span key={i.id} className={i.pass ? 'ok' : 'bad'} title={i.text}>{i.pass ? '✓' : '✕'} {i.id}</span>
          ))}
        </span>
      </div>
    </div>
  );
}

function Grade({ f }) {
  return <span className="cn-grade" title={`method ${f.method}`}>{f.method} · {f.grade}</span>;
}

export default CanonView;
