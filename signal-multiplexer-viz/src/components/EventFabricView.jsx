import { useState, useMemo } from 'react';
import {
  THESIS, STANDING_META, CANVASES, F_MAP, SOURCES, LATENCY,
  RACE_RISKS, AUTHORITY_LAYERS, SCHEMA, UNRESOLVED, SOURCE_SUMMARY,
} from '../data/eventFabric';
import './EventFabricView.css';

const STANDING_ORDER = ['established', 'proposed', 'projected', 'notional', 'open'];

/**
 * Event Fabric view — the F26 Autonomous-Driving Event-Fabric Integration Plan
 * rendered as an interactive dashboard. The base unit is a synchronized
 * interaction trace, not a pixel; semantic vision is one canvas among twelve.
 * Source-standing discipline is load-bearing: projected/notional/open claims
 * may not expand runtime authority — surfaced explicitly throughout.
 */
function EventFabricView() {
  const [standingFilter, setStandingFilter] = useState(null);
  const [selectedCanvas, setSelectedCanvas] = useState(null);

  const standingCounts = useMemo(() => {
    const c = {};
    STANDING_ORDER.forEach(k => { c[k] = SOURCES.filter(s => s.standing === k).length; });
    return c;
  }, []);

  const shownSources = standingFilter ? SOURCES.filter(s => s.standing === standingFilter) : SOURCES;

  return (
    <div className="efv">
      {/* Corrected thesis */}
      <section className="section full-width">
        <div className="efv-thesis">
          <div className="efv-thesis-not">
            Not {THESIS.not.map((n, i) => <span key={n} className="efv-strike">{n}{i < THESIS.not.length - 1 ? ', ' : ''}</span>)}.
          </div>
          <p className="efv-thesis-is">{THESIS.is}</p>
          <p className="efv-thesis-safety">{THESIS.safety}</p>
        </div>
      </section>

      <div className="efv-grid">
        {/* Twelve-canvas architecture */}
        <section className="section">
          <h2>Timeline Spine + Twelve Operational Canvases</h2>
          <div className="efv-canvases">
            {CANVASES.map(c => (
              <button
                key={c.layer}
                className={`efv-canvas ${c.spine ? 'spine' : ''} ${c.central ? 'central' : ''} ${selectedCanvas === c.layer ? 'sel' : ''}`}
                onClick={() => setSelectedCanvas(selectedCanvas === c.layer ? null : c.layer)}
              >
                <span className="efv-canvas-layer">{c.layer}</span>
                <span className="efv-canvas-name">{c.name}</span>
                {c.central && <span className="efv-canvas-tag">witness substrate</span>}
                {c.spine && <span className="efv-canvas-tag spine">single timeline</span>}
              </button>
            ))}
          </div>
          {selectedCanvas != null && (
            <div className="efv-canvas-detail">
              <strong>{CANVASES.find(c => c.layer === selectedCanvas).name}</strong>
              <span>{CANVASES.find(c => c.layer === selectedCanvas).purpose}</span>
            </div>
          )}
          <p className="efv-note">
            The synchronized physical event fabric (canvas 4) is <strong>central but not the whole system</strong>:
            it is the witness substrate connecting raw traces to semantic, tactical, safety, execution, and
            validation canvases. Semantic scene (canvas 5) is important but <em>not sovereign</em>.
          </p>
        </section>

        {/* Source-standing ledger */}
        <section className="section">
          <h2>Source-Standing Ledger — {SOURCE_SUMMARY.total} sources</h2>
          <div className="efv-standing-row">
            {STANDING_ORDER.map(k => {
              const m = STANDING_META[k];
              return (
                <button
                  key={k}
                  className={`efv-standing-chip ${standingFilter === k ? 'active' : ''} ${standingFilter && standingFilter !== k ? 'dim' : ''}`}
                  style={{ '--c': m.color }}
                  onClick={() => setStandingFilter(standingFilter === k ? null : k)}
                  title={m.authority}
                >
                  <span className="efv-chip-n">{standingCounts[k]}</span>
                  <span className="efv-chip-l">{m.label}</span>
                </button>
              );
            })}
          </div>
          <p className="efv-policy">
            Safety rule: <strong>projected / notional / open claims may not expand runtime authority.</strong>
          </p>
          <div className="efv-sources">
            {shownSources.map(s => {
              const m = STANDING_META[s.standing];
              return (
                <div key={s.id} className="efv-source" style={{ '--c': m.color }}>
                  <div className="efv-source-head">
                    <span className="efv-source-id">{s.id}</span>
                    <code className="efv-source-name">{s.name}</code>
                    <span className="efv-source-standing" style={{ background: m.color }}>{m.label}</span>
                  </div>
                  <div className="efv-source-role">{s.role}</div>
                  <div className="efv-source-contrib">{s.contribution}</div>
                  {s.limitation && s.limitation !== 'None.' && s.limitation !== 'None material.' && (
                    <div className="efv-source-limit">⚠ {s.limitation}</div>
                  )}
                </div>
              );
            })}
          </div>
        </section>
      </div>

      {/* F1–F26 matrix */}
      <section className="section full-width">
        <h2>F1–F26 → Autonomous-Driving Event-Fabric Primitives</h2>
        <div className="efv-fmatrix">
          {F_MAP.map(f => (
            <div key={f.f} className={`efv-f ${f.capstone ? 'capstone' : ''}`}>
              <div className="efv-f-head">
                <span className="efv-f-id">{f.f}</span>
                <span className="efv-f-idea">{f.idea}</span>
                {f.capstone && <span className="efv-f-cap">capstone</span>}
              </div>
              <div className="efv-f-prim">{f.primitive}</div>
            </div>
          ))}
        </div>
      </section>

      {/* Latency reconciliation */}
      <section className="section full-width">
        <h2>Latency Reconciliation — the honest count</h2>
        <div className="efv-latency">
          <div className="efv-lat-claim">
            <div className="efv-lat-claim-head">{LATENCY.claim.label}</div>
            <div className="efv-lat-claim-status">{LATENCY.claim.status}</div>
            <p className="efv-lat-claim-note">{LATENCY.conclusion}</p>
          </div>
          <div className="efv-lat-parse">
            {LATENCY.parse.map(p => (
              <div key={p.k} className="efv-lat-stat">
                <span className="efv-lat-stat-v">{p.v}</span>
                <span className="efv-lat-stat-k">{p.k}</span>
              </div>
            ))}
          </div>
        </div>
        <div className="efv-regimes">
          {LATENCY.regimes.map(r => {
            const m = STANDING_META[r.standing];
            return (
              <div key={r.regime} className="efv-regime" style={{ '--c': m.color }}>
                <div className="efv-regime-head">
                  <span className="efv-regime-name">{r.regime}</span>
                  <span className="efv-regime-standing" style={{ background: m.color }}>{m.label}</span>
                </div>
                <div className="efv-regime-interp">{r.interpretation}</div>
                <div className="efv-regime-rule">{r.rule}</div>
              </div>
            );
          })}
        </div>
        {!LATENCY.cardsItemized && (
          <p className="efv-note">
            The {LATENCY.cardCount} individual latency cards (Appendix A) are represented here by their parsed
            summary counts and regimes — <strong>not yet itemized</strong> in this build (honest stub).
          </p>
        )}
      </section>

      <div className="efv-grid">
        {/* Build-time vs runtime authority */}
        <section className="section">
          <h2>Build-Time vs Runtime Authority</h2>
          {AUTHORITY_LAYERS.map(a => (
            <div key={a.layer} className="efv-auth">
              <div className="efv-auth-layer">{a.layer}</div>
              <div className="efv-auth-row"><span className="efv-auth-k allow">may</span>{a.allowed}</div>
              <div className="efv-auth-row"><span className="efv-auth-k forbid">may not</span>{a.forbidden}</div>
              <div className="efv-auth-out"><code>{a.output}</code></div>
            </div>
          ))}
          <div className="efv-races">
            <div className="efv-races-head">Race-condition risks the workbench must model</div>
            <ul>{RACE_RISKS.map(r => <li key={r}>{r}</li>)}</ul>
          </div>
        </section>

        {/* Event-fabric schema */}
        <section className="section">
          <h2>Event-Fabric Package Schema <code className="efv-schema-ver">{SCHEMA.version}</code></h2>
          <div className="efv-schema-block">
            <div className="efv-schema-label">Top-level keys</div>
            <div className="efv-tags">{SCHEMA.topLevelKeys.map(k => <code key={k}>{k}</code>)}</div>
          </div>
          <div className="efv-schema-block">
            <div className="efv-schema-label">Typed node families ({SCHEMA.nodeFamilies.length})</div>
            <div className="efv-tags">{SCHEMA.nodeFamilies.map(k => <code key={k}>{k}</code>)}</div>
          </div>
          <div className="efv-schema-block">
            <div className="efv-schema-label">Typed edge families (contracts, {SCHEMA.edgeFamilies.length})</div>
            <div className="efv-tags">{SCHEMA.edgeFamilies.map(k => <code key={k} className="edge">{k}</code>)}</div>
          </div>
          <div className="efv-schema-block">
            <div className="efv-schema-label">Validation rules</div>
            <ul className="efv-rules">{SCHEMA.validationRules.map(r => <li key={r}>{r}</li>)}</ul>
          </div>
        </section>
      </div>

      {/* Unresolved claims */}
      <section className="section full-width">
        <h2>Open Questions / Unresolved Claims — evidence debt</h2>
        <table className="efv-unresolved">
          <thead><tr><th>Claim / issue</th><th>Status</th><th>Required closure</th></tr></thead>
          <tbody>
            {UNRESOLVED.map(u => (
              <tr key={u.claim}>
                <td className="efv-u-claim">{u.claim}</td>
                <td className="efv-u-status">{u.status}</td>
                <td className="efv-u-closure">{u.closure}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  );
}

export default EventFabricView;
