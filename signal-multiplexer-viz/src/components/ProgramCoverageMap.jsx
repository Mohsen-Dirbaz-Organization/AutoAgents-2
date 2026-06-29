import { useState, useMemo } from 'react';
import {
  COVERAGE_META, DOMAINS, SUBCATEGORIES, SOURCE_XREF, PROGRAM_SUMMARY
} from '../data/programCoverage';
import GapClosurePlan from './GapClosurePlan';
import './ProgramCoverageMap.css';

const COVERAGE_ORDER = ['FULL', 'HIGH', 'PARTIAL', 'GAP'];

/**
 * Program Coverage Map — the GHOST Autonomy "Research Subcategory → Document
 * Section Mapping" rendered as an interactive dashboard. 56 subcategories
 * across 6 domains, colored by source-material coverage, with a gap analysis
 * and a source → subcategory cross-reference.
 *
 * Honest framing: "coverage" here is *source-material availability in project
 * knowledge* (a design corpus), NOT fabricated or measured silicon.
 */
function ProgramCoverageMap() {
  const [selectedId, setSelectedId] = useState(null);
  const [filter, setFilter] = useState(null); // coverage filter

  const counts = useMemo(() => {
    const c = { FULL: 0, HIGH: 0, PARTIAL: 0, GAP: 0 };
    SUBCATEGORIES.forEach(s => { c[s.coverage]++; });
    return c;
  }, []);
  const total = SUBCATEGORIES.length;

  const selected = selectedId != null
    ? SUBCATEGORIES.find(s => s.id === selectedId)
    : null;
  const selectedSources = selected
    ? SOURCE_XREF.filter(x => x.ids.includes(selected.id)).map(x => x.file)
    : [];

  const gaps = SUBCATEGORIES.filter(s => s.coverage === 'GAP');

  return (
    <div className="pcm">
      <div className="pcm-banner">
        <div>
          <strong>{total}</strong> research subcategories · 6 domains · mapped onto
          <code> {PROGRAM_SUMMARY.masterDoc}</code>. Coverage is <em>source-material availability</em>
          (the design corpus), not built or measured silicon.
        </div>
      </div>

      {/* Coverage summary + stacked bar (doubles as a filter) */}
      <div className="pcm-summary">
        {COVERAGE_ORDER.map(k => {
          const m = COVERAGE_META[k];
          const pct = Math.round((counts[k] / total) * 100);
          return (
            <button
              key={k}
              className={`pcm-stat ${filter === k ? 'active' : ''} ${filter && filter !== k ? 'dim' : ''}`}
              style={{ '--c': m.color }}
              onClick={() => setFilter(filter === k ? null : k)}
              title={m.desc}
            >
              <span className="pcm-stat-count">{counts[k]}</span>
              <span className="pcm-stat-label">{m.label}</span>
              <span className="pcm-stat-pct">{pct}%</span>
            </button>
          );
        })}
      </div>
      <div className="pcm-bar">
        {COVERAGE_ORDER.map(k => (
          <div
            key={k}
            className="pcm-bar-seg"
            style={{ width: `${(counts[k] / total) * 100}%`, background: COVERAGE_META[k].color }}
            title={`${COVERAGE_META[k].label}: ${counts[k]}`}
          />
        ))}
      </div>

      {/* Domain grids */}
      <div className="pcm-domains">
        {DOMAINS.map(domain => {
          const cells = SUBCATEGORIES.filter(s => s.domain === domain);
          const dc = { FULL: 0, HIGH: 0, PARTIAL: 0, GAP: 0 };
          cells.forEach(s => dc[s.coverage]++);
          return (
            <div key={domain} className="pcm-domain">
              <div className="pcm-domain-head">
                <span className="pcm-domain-name">{domain}</span>
                <span className="pcm-domain-tally">
                  {COVERAGE_ORDER.filter(k => dc[k] > 0).map((k, i, arr) => (
                    <span key={k} style={{ color: COVERAGE_META[k].color }}>
                      {dc[k]} {k[0]}{i < arr.length - 1 ? ' · ' : ''}
                    </span>
                  ))}
                </span>
              </div>
              <div className="pcm-grid">
                {cells.map(s => {
                  const m = COVERAGE_META[s.coverage];
                  const dimmed = filter && filter !== s.coverage;
                  return (
                    <button
                      key={s.id}
                      className={`pcm-cell ${selectedId === s.id ? 'sel' : ''} ${dimmed ? 'dim' : ''}`}
                      style={{ '--c': m.color }}
                      onClick={() => setSelectedId(selectedId === s.id ? null : s.id)}
                      title={`${s.name} — ${m.label}`}
                    >
                      <span className="pcm-cell-id">{s.id}</span>
                      <span className="pcm-cell-name">{s.name}</span>
                      <span className="pcm-cell-tag" style={{ background: m.color }}>{m.label}</span>
                    </button>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>

      {/* Detail drawer */}
      {selected && (
        <div className="pcm-detail" style={{ '--c': COVERAGE_META[selected.coverage].color }}>
          <div className="pcm-detail-head">
            <span className="pcm-detail-id">#{selected.id}</span>
            <span className="pcm-detail-name">{selected.name}</span>
            <span className="pcm-detail-cov" style={{ background: COVERAGE_META[selected.coverage].color }}>
              {COVERAGE_META[selected.coverage].label}
            </span>
            <button className="pcm-detail-x" onClick={() => setSelectedId(null)}>✕</button>
          </div>
          <div className="pcm-detail-body">
            <div className="pcm-detail-row"><span className="pcm-k">Domain</span><span>{selected.domain}</span></div>
            <div className="pcm-detail-row"><span className="pcm-k">Target chapters</span><span>{selected.chapters}</span></div>
            <div className="pcm-detail-row"><span className="pcm-k">Note</span><span>{selected.note}</span></div>
            {selectedSources.length > 0 && (
              <div className="pcm-detail-row">
                <span className="pcm-k">Source files</span>
                <span className="pcm-srcs">
                  {selectedSources.map(f => <code key={f}>{f}</code>)}
                </span>
              </div>
            )}
            {selected.gapPriority && (
              <div className="pcm-detail-row">
                <span className="pcm-k">Gap</span>
                <span>priority <strong>{selected.gapPriority}</strong> · est. {selected.gapEffort} person-days</span>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Gap-closure plan (Void V-2) — gaps with owner / acquisition / exit, in waves */}
      <div className="pcm-gaps">
        <h3>Gap-closure plan — {gaps.length} gaps sequenced into waves</h3>
        <GapClosurePlan onSelect={setSelectedId} />
      </div>

      {/* Source cross-reference */}
      <div className="pcm-sources">
        <h3>Source files → subcategories served</h3>
        <div className="pcm-src-rows">
          {[...SOURCE_XREF].sort((a, b) => b.count - a.count).map(x => (
            <div key={x.file} className="pcm-src">
              <code className="pcm-src-file">{x.file}</code>
              <div className="pcm-src-bar">
                <div className="pcm-src-fill" style={{ width: `${(x.count / 20) * 100}%` }} />
              </div>
              <span className="pcm-src-count">{x.count}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default ProgramCoverageMap;
