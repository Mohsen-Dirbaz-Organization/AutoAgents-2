import { useEffect, useRef } from 'react';
import * as d3 from 'd3';
import './TimeSeriesChart.css';

function TimeSeriesChart({ history, channels }) {
  const svgRef = useRef();

  useEffect(() => {
    if (!history || !history.timestamps || history.timestamps.length === 0) return;

    const svg = d3.select(svgRef.current);
    svg.selectAll('*').remove();

    const width = svgRef.current.clientWidth;
    const height = 300;
    const margin = { top: 20, right: 120, bottom: 40, left: 60 };

    const chartWidth = width - margin.left - margin.right;
    const chartHeight = height - margin.top - margin.bottom;

    const g = svg.append('g')
      .attr('transform', `translate(${margin.left},${margin.top})`);

    // Create scales
    const xScale = d3.scaleLinear()
      .domain([0, history.timestamps.length - 1])
      .range([0, chartWidth]);

    // Find max bandwidth value across all channels
    const maxBandwidth = d3.max(
      Object.values(history.bandwidth).flat()
    ) || 1000;

    const yScale = d3.scaleLinear()
      .domain([0, maxBandwidth * 1.1])
      .range([chartHeight, 0]);

    // Create line generator
    const line = d3.line()
      .x((d, i) => xScale(i))
      .y(d => yScale(d))
      .curve(d3.curveMonotoneX);

    // Colors for channels
    const colorScale = d3.scaleOrdinal()
      .domain(channels?.map(c => c.id) || [])
      .range(['#E91E63', '#FF9800', '#4CAF50', '#2196F3', '#9C27B0']);

    // Draw bandwidth lines for each channel
    Object.entries(history.bandwidth).forEach(([channelId, data]) => {
      if (data.length > 0) {
        g.append('path')
          .datum(data)
          .attr('class', 'time-series-line')
          .attr('d', line)
          .attr('stroke', colorScale(channelId))
          .attr('stroke-width', 2)
          .attr('fill', 'none')
          .attr('opacity', 0.8);

        // Add label at end of line
        const lastValue = data[data.length - 1];
        const lastIndex = data.length - 1;

        g.append('text')
          .attr('x', xScale(lastIndex) + 5)
          .attr('y', yScale(lastValue))
          .attr('dy', '0.35em')
          .attr('font-size', '10px')
          .attr('fill', colorScale(channelId))
          .text(channelId);
      }
    });

    // Add axes
    const xAxis = d3.axisBottom(xScale)
      .ticks(5)
      .tickFormat(d => `T-${history.timestamps.length - d}`);

    const yAxis = d3.axisLeft(yScale)
      .ticks(5);

    g.append('g')
      .attr('class', 'x-axis')
      .attr('transform', `translate(0,${chartHeight})`)
      .call(xAxis);

    g.append('g')
      .attr('class', 'y-axis')
      .call(yAxis);

    // Axis labels
    g.append('text')
      .attr('transform', 'rotate(-90)')
      .attr('y', -45)
      .attr('x', -chartHeight / 2)
      .attr('text-anchor', 'middle')
      .attr('font-size', '12px')
      .attr('fill', '#666')
      .text('Bandwidth Allocation');

    g.append('text')
      .attr('y', chartHeight + 35)
      .attr('x', chartWidth / 2)
      .attr('text-anchor', 'middle')
      .attr('font-size', '12px')
      .attr('fill', '#666')
      .text('Time (cycles ago)');

  }, [history, channels]);

  return (
    <div className="time-series-chart">
      <h3>Bandwidth Allocation Over Time</h3>
      <svg ref={svgRef} width="100%" height="300"></svg>
      <div className="chart-description">
        <p>
          Shows how bandwidth is dynamically allocated to each channel over time.
          Watch how the system adapts to changing traffic patterns and priorities.
        </p>
      </div>
    </div>
  );
}

export default TimeSeriesChart;
