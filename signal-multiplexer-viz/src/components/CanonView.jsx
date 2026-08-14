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
import { runEvidenceCompositionAnalysis } from '../simulation/EvidenceCompositionEngine';
import { runLevelAnalysis } from '../simulation/LevelEngine';
import { runPcgArchiveAudit } from '../simulation/PcgEngine';
import { TASKS } from '../data/canon/planning';
import { THESIS as EVIDENCE_THESIS } from '../data/canon/evidence';
import { LEVEL, LEVEL_ORDER, APPARATUS_EXAMPLES, AXIOMS, THESIS as LEVEL_THESIS } from '../data/canon/level';
import { ASPECTS, ASPECT_ORDER, MODALITIES, MODALITY_PRECEDENCE, RECORD_SCHEMA, ARCHIVE_DISPOSITION, THESIS as PCG_THESIS } from '../data/canon/pcg';
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
  const evidence = useMemo(() => runEvidenceCompositionAnalysis(), []);
  const level = useMemo(() => runLevelAnalysis(), []);
  const pcg = useMemo(() => runPcgArchiveAudit(), []);

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
        <h2>Evidence composition — Lemma Composition and Introduction-Order Formalism</h2>
        <EvidenceCompositionPanel results={evidence} />
      </section>

      <section className="section full-width">
        <h2>Level &amp; locality — Multi-Level Policy</h2>
        <LevelPanel level={level} />
      </section>

      <section className="section full-width">
        <h2>Archive record format — Process Characterization Grammar</h2>
        <PcgPanel pcg={pcg} />
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

const LABEL_META = {
  proceed: { color: '#c0392b', text: 'proceed' },   // the ONE label that licenses action — red when premature
  caution: { color: '#DAA520', text: 'caution' },
  stop: { color: '#22a06b', text: 'stop' }           // restrictive = safe, shown green here (safety framing, not traffic-light framing)
};

/**
 * EvidenceCompositionPanel — the Lemma Composition and Introduction-Order
 * Formalism (canon/evidence.js), rendered live. Ground truth is order-
 * invariant by construction (standing: constructed); the prefix trace is the
 * falsifiable content — a step licensing 'proceed' before the full evidence
 * would is flagged, exactly the officer/light hazard from the opening
 * challenge.
 */
function EvidenceCompositionPanel({ results }) {
  return (
    <div className="cn-evidence">
      <p className="cn-note">
        &ldquo;{EVIDENCE_THESIS.quote}&rdquo; {EVIDENCE_THESIS.tuple} {EVIDENCE_THESIS.layering}.
        {' '}{EVIDENCE_THESIS.rule}
      </p>
      {results.map((r) => (
        <div key={r.instance.id} className={`cn-ev-card ${r.intentSatisfied ? 'ok' : 'bad'}`}>
          <div className="cn-ev-head">
            <span className="cn-ev-title">{r.instance.title}</span>
            <span className={`cn-ev-intent ${r.intentSatisfied ? 'ok' : 'bad'}`}>
              {r.instance.intent}{r.intentSatisfied ? ' ✓' : ' ✕'}
            </span>
          </div>
          <div className="cn-ev-truth">
            ground truth <span className="cn-grade" title="order-invariant by construction — see file header">A2 · constructed</span>:
            {' '}<b style={{ color: LABEL_META[r.full.label].color }}>{LABEL_META[r.full.label].text}</b>
          </div>
          <div className="cn-ev-trace">
            {r.trace.map((t) => (
              <span
                key={t.k}
                className={`cn-ev-step ${t.unsafe ? 'unsafe' : 'safe'}`}
                title={`${t.componentId} → ${t.label}${t.unsafe ? ' — licenses proceed before the full evidence would' : ''}`}
              >
                <span className="cn-ev-step-k">{t.k}</span>
                <span className="cn-ev-step-id">{t.componentId}</span>
                <span className="cn-ev-step-label" style={{ color: LABEL_META[t.label].color }}>{t.label}</span>
              </span>
            ))}
          </div>
          {r.instance.note && <div className="cn-ev-note">{r.instance.note}</div>}
          {r.formationSmells.length > 0 && (
            <div className="cn-ev-smell">⚠ {r.formationSmells[0].message}</div>
          )}
          <div className="cn-ev-exch">
            {r.exchangeablePairs.filter((p) => p.exchangeable).length} exchangeable pair(s)
            {' · '}{r.exchangeablePairs.filter((p) => !p.exchangeable).length} order-critical pair(s)
            {r.instance.components.some((c) => c.crossRef) && (
              <span> · cross-ref <code>{r.instance.components.find((c) => c.crossRef).crossRef}</code></span>
            )}
          </div>
        </div>
      ))}
    </div>
  );
}

/**
 * LevelPanel — Multi-Level Policy (canon/level.js + LevelEngine.js) rendered
 * live. Level (G≺R≺C, derivation depth) is orthogonal to locus (where a
 * value is stored) — level ⫫ locus. Four audits: the multiplication licence
 * (no scope-namespace collision), gate-level inheritance (do R/C gates cite
 * their apparatus), demotion-not-mutation (scenario-tested against the
 * Constitution engine, with a self-check that the detector is falsifiable),
 * and the satisfaction identity (issue vs. defer).
 */
function LevelPanel({ level }) {
  return (
    <div className="cn-level">
      <p className="cn-note">
        &ldquo;{LEVEL_THESIS.rule}&rdquo; {LEVEL_THESIS.quote}
      </p>

      <div className="cn-level-ladder">
        {LEVEL_ORDER.map((k) => (
          <div key={k} className="cn-level-rung" style={{ '--c': LEVEL[k].color }}>
            <div className="cn-level-rung-head">
              <span className="cn-level-badge" style={{ background: LEVEL[k].color }}>{k}</span>
              <span className="cn-level-name">{LEVEL[k].label}</span>
              <span className="cn-level-apparatus">apparatus: {LEVEL[k].apparatus}</span>
            </div>
            <div className="cn-level-form">{LEVEL[k].form}</div>
            <div className="cn-level-meaning">{LEVEL[k].meaning}</div>
          </div>
        ))}
      </div>

      <div className="cn-level-grid">
        <div className="cn-level-col">
          <div className="cn-level-h3">Apparatus examples — grounded in this repo</div>
          <ul className="cn-level-examples">
            {APPARATUS_EXAMPLES.map((e) => (
              <li key={e.quantity} className="cn-level-example">
                <span className="cn-level-ex-badge" style={{ background: LEVEL[e.level].color }}>{e.level}</span>
                <div>
                  <div className="cn-level-ex-q">{e.quantity}</div>
                  <div className="cn-level-ex-d">{e.basis ? `basis: ${e.basis}` : e.registry ? `registry: ${e.registry}` : ''}</div>
                  <div className="cn-level-ex-d">{e.detail}</div>
                </div>
              </li>
            ))}
          </ul>

          <div className="cn-level-h3">Satisfaction identity — EO(φ) = Lev_spec ⊖ min(Lev_evidence, Lev_horizon)</div>
          <table className="cn-level-eo">
            <thead><tr><th>spec</th><th>evidence</th><th>horizon</th><th>licence</th><th>gap</th><th>verdict</th></tr></thead>
            <tbody>
              {level.exampleObligations.map((eo, i) => (
                <tr key={i} className={eo.verdict}>
                  <td><span className="cn-level-badge sm" style={{ background: LEVEL[eo.specLevel].color }}>{eo.specLevel}</span></td>
                  <td><span className="cn-level-badge sm" style={{ background: LEVEL[eo.evidenceLevel].color }}>{eo.evidenceLevel}</span></td>
                  <td><span className="cn-level-badge sm" style={{ background: LEVEL[eo.horizonLevel].color }}>{eo.horizonLevel}</span></td>
                  <td><span className="cn-level-badge sm" style={{ background: LEVEL[eo.licenceLevel].color }}>{eo.licenceLevel}</span></td>
                  <td>{eo.gap}</td>
                  <td className={`cn-level-verdict ${eo.verdict}`}>{eo.verdict}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="cn-level-col">
          <div className="cn-level-h3">
            Multiplication licence (MLP-5)
            <span className={`cn-level-check ${level.multiplication.disjoint ? 'ok' : 'bad'}`}>
              {level.multiplication.disjoint ? '✓ disjoint' : '✕ collision'}
            </span>
          </div>
          <p className="cn-level-p">
            {level.multiplication.planningScopeCount} planning scopes · {level.multiplication.evidenceScopeCount} evidence scopes —
            two independently-authored namespaces, checked for accidental collision.
          </p>

          <div className="cn-level-h3">Gate-level inheritance (5.4)</div>
          <table className="cn-level-gates">
            <thead><tr><th>Gate</th><th>Level</th><th>Apparatus</th></tr></thead>
            <tbody>
              {level.gateInheritance.gates.map((g) => (
                <tr key={g.id}>
                  <td className="cn-level-gate-id">{g.id}</td>
                  <td><span className="cn-level-badge sm" style={{ background: LEVEL[g.level].color }}>{g.level}</span></td>
                  <td className="cn-level-gate-app">{g.basis || g.registry || <em>none declared</em>}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {level.gateInheritance.findings.filter((f) => f.severity === 'warning').map((f) => (
            <div key={f.gate} className="cn-level-smell">⚠ <b>{f.gate}</b>: {f.message}</div>
          ))}

          <div className="cn-level-h3">
            Demotion, not mutation (MLP-7)
            <span className={`cn-level-check ${level.demotion.pass ? 'ok' : 'bad'}`}>
              {level.demotion.pass ? '✓ no violation' : '✕ violation found'}
            </span>
          </div>
          <p className="cn-level-p">
            standing <span className="cn-grade">{level.demotion.standing}</span> — scenario-tested against
            ConstitutionalTruthEngine, {level.demotion.scenarios.length} scenarios,
            {' '}{level.demotion.scenarios.reduce((n, s) => n + s.steps.length, 0)} steps total.
            Detector self-check: {level.noOpAudit.detected ? '✓ falsifiable' : '✕ broken (guaranteed-pass)'}.
          </p>
          <div className="cn-level-scenarios">
            {level.demotion.scenarios.map((s) => (
              <div key={s.label} className="cn-level-scenario">
                <b>{s.label}</b> — {s.steps.map((st) => `${st.evidenceBefore.toFixed(2)}→${st.evidenceAfter.toFixed(2)} (${st.relianceBefore}→${st.relianceAfter})`).join('  ·  ')}
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="cn-level-axioms">
        {AXIOMS.map((a) => (
          <span key={a.id} className="cn-level-ax"><b>{a.id}</b> {a.text}</span>
        ))}
      </div>
    </div>
  );
}

/**
 * PcgPanel — the Process Characterization Grammar (canon/pcg.js +
 * PcgEngine.js/PcgRecords.js), rendered against LIVE archive records. Every
 * ConstitutionalTruthEngine.archive event now carries a `.record` field
 * (record = 〈Aspect·Modality·Level〉 + value + U + provenance); this panel
 * drives a scenario and shows the well-formedness result on the real output,
 * not a static example.
 */
function PcgPanel({ pcg }) {
  return (
    <div className="cn-pcg">
      <p className="cn-note">{PCG_THESIS.rule}</p>

      <div className="cn-pcg-grid">
        <div className="cn-pcg-col">
          <div className="cn-level-h3">Aspect — six closed subjects</div>
          <div className="cn-pcg-chips">
            {ASPECT_ORDER.map((k) => (
              <span key={k} className="cn-pcg-chip" title={ASPECTS[k].decisionTest}>
                <b>{k}</b> {ASPECTS[k].name}
              </span>
            ))}
          </div>
          <div className="cn-level-h3">Modality — four forces, total precedence</div>
          <div className="cn-pcg-precedence">
            {MODALITY_PRECEDENCE.map((k, i) => (
              <span key={k}>
                <span className="cn-pcg-chip" title={MODALITIES[k].forceTest}><b>{k}</b> {MODALITIES[k].force}</span>
                {i < MODALITY_PRECEDENCE.length - 1 && <span className="cn-pcg-prec-arrow">≻</span>}
              </span>
            ))}
          </div>
          <div className="cn-level-h3">Content-record schema (§6.1)</div>
          <table className="cn-level-gates">
            <thead><tr><th>Field</th><th>Obligation</th></tr></thead>
            <tbody>
              {RECORD_SCHEMA.map((f) => (
                <tr key={f.field}>
                  <td className="cn-level-gate-id">{f.field}</td>
                  <td>{f.obligation}</td>
                </tr>
              ))}
            </tbody>
          </table>

          <div className="cn-level-h3">archive.disposition registry (v{ARCHIVE_DISPOSITION.version})</div>
          <p className="cn-level-p">{ARCHIVE_DISPOSITION.domain}. Erasure cites THIS registry, not RELIANCE — {'`erased`'} is not a RELIANCE member.</p>
          <div className="cn-pcg-chips">
            {Object.keys(ARCHIVE_DISPOSITION.classes).map((c) => (
              <span key={c} className="cn-pcg-chip" title={ARCHIVE_DISPOSITION.classes[c]}>{c}</span>
            ))}
          </div>
        </div>

        <div className="cn-pcg-col">
          <div className="cn-level-h3">
            Live archive audit
            <span className={`cn-level-check ${pcg.allValid ? 'ok' : 'bad'}`}>
              {pcg.allValid ? `✓ ${pcg.recordedCount}/${pcg.recordedCount} well-formed` : '✕ ill-formed records found'}
            </span>
          </div>
          <p className="cn-level-p">
            A scenario drives ConstitutionalTruthEngine through canonization, reclassification,
            silent-drift surfacing, retraction, and erasure — {pcg.archiveLength} archive events, each
            validated against CC1–CC4 / W1 / W2 / W4. gate.challenge liveness (A3, §8.5):
            {' '}{pcg.gateLiveness.dead ? 'DEAD (0 occurrences)' : `${pcg.gateLiveness.occurrences} instance-of occurrences`}.
            Record-validator self-check: {pcg.noOpAudit.detected ? '✓ falsifiable' : '✕ broken (guaranteed-pass)'}.
          </p>

          <div className="cn-pcg-records">
            {pcg.sample.map((rec) => (
              <div key={rec.id} className="cn-pcg-record">
                <div className="cn-pcg-record-head">
                  <span className="cn-pcg-addr">{rec.address.aspect}·{rec.address.modality}·{rec.address.level}</span>
                  <code className="cn-pcg-id">{rec.id}</code>
                </div>
                <div className="cn-pcg-record-line"><b>value</b> {rec.value.relation}: {rec.value.from} → {rec.value.to}</div>
                <div className="cn-pcg-record-line"><b>U</b> {rec.U.kind} P={rec.U.P}</div>
                <div className="cn-pcg-record-line"><b>provenance</b> {rec.provenance.registry} · {rec.provenance.method}</div>
                {rec.links.length > 0 && (
                  <div className="cn-pcg-record-line"><b>links</b> {rec.links.map((l) => `${l.type}→${l.target}`).join(', ')}</div>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

export default CanonView;
