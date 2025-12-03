import { useEffect, useRef } from 'react';
import * as d3 from 'd3';
import './AdaptationMetrics.css';

function AdaptationMetrics({ history, adaptiveParams, performanceMetrics }) {
  const svgRef = useRef();

  useEffect(() => {
    if (!history || !history.timestamps || history.timestamps.length === 0) return;

    const svg = d3.select(svgRef.current);
    svg.selectAll('*').remove();

    const width = 600;
    const height = 250;
    const margin = { top: 20, right: 120, bottom: 40, left: 50 };

    const g = svg
      .attr('width', width)
      .attr('height', height)
      .append('g')
      .attr('transform', `translate(${margin.left},${margin.top})`);

    const chartWidth = width - margin.left - margin.right;
    const chartHeight = height - margin.top - margin.bottom;

    // Create scales
    const xScale = d3.scaleLinear()
      .domain([0, history.timestamps.length - 1])
      .range([0, chartWidth]);

    const yScale = d3.scaleLinear()
      .domain([0, Math.max(
        d3.max(history.constraintTightness) || 2,
        d3.max(history.predictionError) || 1
      )])
      .range([chartHeight, 0]);

    // Draw axes
    const xAxis = d3.axisBottom(xScale)
      .ticks(5)
      .tickFormat(d => `${Math.floor(d * 0.1)}s`);

    const yAxis = d3.axisLeft(yScale).ticks(5);

    g.append('g')
      .attr('class', 'x-axis')
      .attr('transform', `translate(0,${chartHeight})`)
      .call(xAxis);

    g.append('g')
      .attr('class', 'y-axis')
      .call(yAxis);

    // Line generators
    const lineConstraint = d3.line()
      .x((d, i) => xScale(i))
      .y(d => yScale(d))
      .curve(d3.curveMonotoneX);

    const lineError = d3.line()
      .x((d, i) => xScale(i))
      .y(d => yScale(d))
      .curve(d3.curveMonotoneX);

    // Draw constraint tightness line
    if (history.constraintTightness && history.constraintTightness.length > 0) {
      g.append('path')
        .datum(history.constraintTightness)
        .attr('class', 'adaptation-line constraint-line')
        .attr('d', lineConstraint)
        .attr('stroke', '#667eea')
        .attr('stroke-width', 2.5)
        .attr('fill', 'none');

      // Add label
      g.append('text')
        .attr('x', chartWidth + 10)
        .attr('y', yScale(history.constraintTightness[history.constraintTightness.length - 1]))
        .attr('class', 'line-label')
        .attr('fill', '#667eea')
        .style('font-size', '11px')
        .style('font-weight', '600')
        .text('Constraint');
    }

    // Draw prediction error line
    if (history.predictionError && history.predictionError.length > 0) {
      g.append('path')
        .datum(history.predictionError)
        .attr('class', 'adaptation-line error-line')
        .attr('d', lineError)
        .attr('stroke', '#f44336')
        .attr('stroke-width', 2.5)
        .attr('fill', 'none');

      // Add label
      g.append('text')
        .attr('x', chartWidth + 10)
        .attr('y', yScale(history.predictionError[history.predictionError.length - 1]))
        .attr('class', 'line-label')
        .attr('fill', '#f44336')
        .style('font-size', '11px')
        .style('font-weight', '600')
        .text('Error');
    }

    // Add axis labels
    g.append('text')
      .attr('x', chartWidth / 2)
      .attr('y', chartHeight + 35)
      .attr('class', 'axis-label')
      .style('text-anchor', 'middle')
      .style('font-size', '12px')
      .style('fill', '#666')
      .text('Time');

    g.append('text')
      .attr('transform', 'rotate(-90)')
      .attr('x', -chartHeight / 2)
      .attr('y', -35)
      .attr('class', 'axis-label')
      .style('text-anchor', 'middle')
      .style('font-size', '12px')
      .style('fill', '#666')
      .text('Value');

  }, [history]);

  return (
    <div className="adaptation-metrics">
      <h3>Adaptive Learning Parameters</h3>

      <div className="metrics-grid">
        <div className="metric-card constraint-card">
          <div className="metric-icon">🎯</div>
          <div className="metric-content">
            <div className="metric-label">Constraint Tightness</div>
            <div className="metric-value">{adaptiveParams?.constraintTightness?.toFixed(3) || '1.000'}</div>
            <div className="metric-description">Adapts based on prediction error</div>
          </div>
        </div>

        <div className="metric-card error-card">
          <div className="metric-icon">📊</div>
          <div className="metric-content">
            <div className="metric-label">Prediction Error</div>
            <div className="metric-value">{(adaptiveParams?.predictionError * 100)?.toFixed(1) || '0.0'}%</div>
            <div className="metric-description">Queue size prediction accuracy</div>
          </div>
        </div>

        <div className="metric-card accuracy-card">
          <div className="metric-icon">✓</div>
          <div className="metric-content">
            <div className="metric-label">Model Accuracy</div>
            <div className="metric-value">{(adaptiveParams?.modelAccuracy * 100)?.toFixed(1) || '100.0'}%</div>
            <div className="metric-description">Overall model performance</div>
          </div>
        </div>

        <div className="metric-card adaptation-card">
          <div className="metric-icon">🔄</div>
          <div className="metric-content">
            <div className="metric-label">Adaptations</div>
            <div className="metric-value">{performanceMetrics?.adaptationCount || 0}</div>
            <div className="metric-description">Parameter adjustments made</div>
          </div>
        </div>
      </div>

      <div className="adaptation-chart">
        <h4>Parameter Evolution</h4>
        <svg ref={svgRef}></svg>
      </div>

      <div className="adaptation-info">
        <h4>How Adaptation Works</h4>
        <ul>
          <li>
            <strong>Constraint Tightness:</strong> Increases when prediction error is high (&gt;30%),
            tightening optimization constraints to improve control.
          </li>
          <li>
            <strong>Prediction Error:</strong> Measures deviation between predicted and actual queue sizes,
            driving constraint adaptation with exponential smoothing.
          </li>
          <li>
            <strong>Model Accuracy:</strong> Overall system prediction performance, combining multiple
            error sources with weighted averaging.
          </li>
          <li>
            <strong>Learning Rate:</strong> Fixed at {adaptiveParams?.learningRate?.toFixed(2) || '0.10'},
            controls speed of parameter updates to balance stability and responsiveness.
          </li>
        </ul>
        <p className="interplay-note">
          <strong>Parameter Interplay:</strong> Watch how high prediction errors trigger constraint
          tightening, which improves control but may reduce throughput. As accuracy improves,
          constraints relax, increasing efficiency.
        </p>
      </div>
    </div>
  );
}

export default AdaptationMetrics;
