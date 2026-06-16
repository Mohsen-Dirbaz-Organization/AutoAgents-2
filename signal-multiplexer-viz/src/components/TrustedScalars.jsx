import { useEffect, useRef } from 'react';
import * as d3 from 'd3';
import './TrustedScalars.css';

/**
 * The conservation-manifold compiler φ (Lane D) emits two trusted scalars:
 *   ξ — a continuous, bounded, saturating log-deviation invariant
 *   S — a discrete structural-parity invariant (±1)
 * Alongside them, the Local Learning Coefficient (LLC) is estimated online; an
 * LLC jump marks a developmental phase transition and triggers quarantine.
 */
function TrustedScalars({ scalars, history }) {
  const sparkRef = useRef();

  useEffect(() => {
    if (!history || !history.xi || !history.llc || history.xi.length < 2) return;
    const svg = d3.select(sparkRef.current);
    svg.selectAll('*').remove();

    const width = sparkRef.current.clientWidth;
    if (!width) return; // container not laid out yet (e.g. hidden) — skip this draw
    const height = 120;
    const margin = { top: 12, right: 12, bottom: 16, left: 12 };
    const cw = width - margin.left - margin.right;
    const ch = height - margin.top - margin.bottom;
    const g = svg.append('g').attr('transform', `translate(${margin.left},${margin.top})`);

    const n = history.xi.length;
    const x = d3.scaleLinear().domain([0, n - 1]).range([0, cw]);

    // ξ sparkline (0..1)
    const yXi = d3.scaleLinear().domain([0, 1]).range([ch, 0]);
    const lineXi = d3.line().x((_, i) => x(i)).y(d => yXi(d)).curve(d3.curveMonotoneX);
    g.append('path').datum(history.xi)
      .attr('fill', 'none').attr('stroke', '#2563eb').attr('stroke-width', 2).attr('d', lineXi);

    // LLC sparkline (auto-scaled)
    const llcMax = d3.max(history.llc) || 1;
    const yLlc = d3.scaleLinear().domain([0, Math.max(2, llcMax)]).range([ch, 0]);
    const lineLlc = d3.line().x((_, i) => x(i)).y(d => yLlc(d)).curve(d3.curveMonotoneX);
    g.append('path').datum(history.llc)
      .attr('fill', 'none').attr('stroke', '#9333ea').attr('stroke-width', 1.5)
      .attr('stroke-dasharray', '3 2').attr('d', lineLlc);

    // Mark LLC jumps (bounded by the shorter of the two series, defensively).
    const nJump = Math.min(n, history.llc.length);
    for (let i = 1; i < nJump; i++) {
      if (history.llc[i] - history.llc[i - 1] > 0.25) {
        g.append('circle').attr('cx', x(i)).attr('cy', yLlc(history.llc[i]))
          .attr('r', 4).attr('fill', '#dc143c');
      }
    }

    g.append('text').attr('x', 0).attr('y', -2).attr('font-size', '9px').attr('fill', '#2563eb').text('ξ');
    g.append('text').attr('x', 16).attr('y', -2).attr('font-size', '9px').attr('fill', '#9333ea').text('LLC');
  }, [history]);

  const xi = scalars?.xi ?? 0;
  const llc = scalars?.llc ?? 1;
  const parity = scalars?.S > 0 ? 1 : -1;

  return (
    <div className="trusted-scalars">
      <div className="ts-gauges">
        <div className="ts-gauge">
          <RadialGauge value={xi} max={1} color="#2563eb" label="ξ" />
          <div className="ts-gauge-cap">log-deviation<br />(saturating)</div>
        </div>

        <div className={`ts-parity ${parity > 0 ? 'pos' : 'neg'}`}>
          <div className="ts-parity-val">{parity > 0 ? '+1' : '−1'}</div>
          <div className="ts-parity-cap">S — structural parity</div>
        </div>

        <div className={`ts-llc ${scalars?.llcJump ? 'jump' : ''}`}>
          <div className="ts-llc-val">{llc.toFixed(2)}</div>
          <div className="ts-llc-cap">LLC {scalars?.llcJump ? '⚠ jump' : ''}</div>
          <div className="ts-llc-q">{scalars?.quarantines ?? 0} quarantined</div>
        </div>
      </div>

      <svg ref={sparkRef} width="100%" height="120" />

      <p className="ts-note">
        ξ and S are bounded, conservation-licensed scalars consumed by the S1 fidelity gate.
        A simultaneous LLC jump (red marker) quarantines the strategic consolidation — it is
        logged as a challengeable event, never silently committed.
      </p>
    </div>
  );
}

function RadialGauge({ value, max, color, label }) {
  const r = 38, c = 2 * Math.PI * r;
  const frac = Math.max(0, Math.min(1, value / max));
  return (
    <svg width="96" height="96" viewBox="0 0 96 96" className="ts-radial">
      <circle cx="48" cy="48" r={r} fill="none" stroke="#eef0f4" strokeWidth="9" />
      <circle
        cx="48" cy="48" r={r} fill="none" stroke={color} strokeWidth="9"
        strokeLinecap="round" strokeDasharray={c}
        strokeDashoffset={c * (1 - frac)}
        transform="rotate(-90 48 48)"
      />
      <text x="48" y="44" textAnchor="middle" fontSize="20" fontWeight="800" fill={color}>{label}</text>
      <text x="48" y="62" textAnchor="middle" fontSize="13" fill="#475569">{value.toFixed(2)}</text>
    </svg>
  );
}

export default TrustedScalars;
