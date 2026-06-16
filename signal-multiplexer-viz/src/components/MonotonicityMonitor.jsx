import { useEffect, useRef } from 'react';
import * as d3 from 'd3';
import './MonotonicityMonitor.css';

/**
 * The antitone monotonicity invariant, watched live. As risk R rises, the
 * admissible action set may only ever CONTRACT. We plot R(t) against the
 * (normalized) admissible-set size; under conservative discipline the two move
 * in strict opposition. The violation rate is the PoC headline metric.
 */
function MonotonicityMonitor({ history, monotonicity, conservativeDiscipline }) {
  const svgRef = useRef();

  useEffect(() => {
    if (!history || !history.timestamps || history.timestamps.length < 2) return;

    const svg = d3.select(svgRef.current);
    svg.selectAll('*').remove();

    const width = svgRef.current.clientWidth;
    if (!width) return; // container not laid out yet (e.g. hidden) — skip this draw
    const height = 240;
    const margin = { top: 16, right: 48, bottom: 28, left: 40 };
    const cw = width - margin.left - margin.right;
    const ch = height - margin.top - margin.bottom;

    const g = svg.append('g').attr('transform', `translate(${margin.left},${margin.top})`);

    const n = history.timestamps.length;
    const x = d3.scaleLinear().domain([0, n - 1]).range([0, cw]);
    const yR = d3.scaleLinear().domain([0, 1]).range([ch, 0]);

    const maxAdmit = d3.max(history.admittedCount) || 1;
    const yA = d3.scaleLinear().domain([0, maxAdmit]).range([ch, 0]);

    // Gridlines
    g.append('g')
      .attr('class', 'mmn-grid')
      .call(d3.axisLeft(yR).ticks(4).tickSize(-cw).tickFormat(''))
      .selectAll('line').attr('stroke', '#eef0f4');

    const lineR = d3.line().x((_, i) => x(i)).y(d => yR(d)).curve(d3.curveMonotoneX);
    const lineA = d3.line().x((_, i) => x(i)).y(d => yA(d)).curve(d3.curveStepAfter);

    // Admissible-set size (green) — should fall when R rises.
    g.append('path')
      .datum(history.admittedCount)
      .attr('fill', 'none')
      .attr('stroke', '#22a06b')
      .attr('stroke-width', 2)
      .attr('d', lineA);

    // Risk R (red)
    g.append('path')
      .datum(history.R)
      .attr('fill', 'none')
      .attr('stroke', '#dc143c')
      .attr('stroke-width', 2)
      .attr('d', lineR);

    // Mark violations: points where R rose AND admitted grew.
    const violations = [];
    for (let i = 1; i < n; i++) {
      if (history.R[i] > history.R[i - 1] + 1e-6 &&
          history.admittedCount[i] > history.admittedCount[i - 1]) {
        violations.push(i);
      }
    }
    g.selectAll('.mmn-viol')
      .data(violations)
      .enter()
      .append('circle')
      .attr('class', 'mmn-viol')
      .attr('cx', i => x(i))
      .attr('cy', i => yA(history.admittedCount[i]))
      .attr('r', 5)
      .attr('fill', 'none')
      .attr('stroke', '#dc143c')
      .attr('stroke-width', 2);

    // Axes
    g.append('g').attr('transform', `translate(0,${ch})`)
      .call(d3.axisBottom(x).ticks(5).tickFormat(d => `${d}`))
      .selectAll('text').attr('font-size', '9px').attr('fill', '#94a3b8');
    g.append('g').call(d3.axisLeft(yR).ticks(4))
      .selectAll('text').attr('font-size', '9px').attr('fill', '#dc143c');
    g.append('g').attr('transform', `translate(${cw},0)`)
      .call(d3.axisRight(yA).ticks(maxAdmit))
      .selectAll('text').attr('font-size', '9px').attr('fill', '#22a06b');

    g.append('text').attr('x', 0).attr('y', -4).attr('font-size', '10px').attr('fill', '#dc143c').text('Risk R');
    g.append('text').attr('x', cw - 4).attr('y', -4).attr('text-anchor', 'end')
      .attr('font-size', '10px').attr('fill', '#22a06b').text('|U_ad|');
  }, [history]);

  const rate = monotonicity?.violationRate ?? 0;
  const verdict = rate === 0 ? 'CLEAR' : rate < 0.02 ? 'MARGINAL' : 'VIOLATED';
  const verdictClass = rate === 0 ? 'clear' : rate < 0.02 ? 'marginal' : 'violated';

  return (
    <div className="monotonicity-monitor">
      <div className="mmn-headline">
        <div className={`mmn-rate ${verdictClass}`}>
          <span className="mmn-rate-val">{(rate * 100).toFixed(1)}%</span>
          <span className="mmn-rate-label">violation rate · {verdict}</span>
        </div>
        <div className="mmn-counts">
          <div><strong>{monotonicity?.violations ?? 0}</strong> violations</div>
          <div><strong>{monotonicity?.checks ?? 0}</strong> rising-risk checks</div>
          <div className={`mmn-disc ${conservativeDiscipline ? 'on' : 'off'}`}>
            discipline {conservativeDiscipline ? 'ON' : 'OFF'}
          </div>
        </div>
      </div>
      <svg ref={svgRef} width="100%" height="240" />
      <p className="mmn-note">
        Target under WS-2 discipline: <strong>zero</strong>. Red rings mark ticks where the
        admissible set grew while risk rose — the one failure the whole architecture forbids.
      </p>
    </div>
  );
}

export default MonotonicityMonitor;
