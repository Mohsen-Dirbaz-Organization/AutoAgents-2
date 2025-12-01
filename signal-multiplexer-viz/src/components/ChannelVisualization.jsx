import { useEffect, useRef } from 'react';
import * as d3 from 'd3';
import './ChannelVisualization.css';

function ChannelVisualization({ channels, resourceState }) {
  const svgRef = useRef();

  useEffect(() => {
    if (!channels || channels.length === 0) return;

    const svg = d3.select(svgRef.current);
    svg.selectAll('*').remove();

    const width = svgRef.current.clientWidth;
    const height = 400;
    const margin = { top: 20, right: 20, bottom: 60, left: 80 };

    const chartWidth = width - margin.left - margin.right;
    const chartHeight = height - margin.top - margin.bottom;

    const g = svg.append('g')
      .attr('transform', `translate(${margin.left},${margin.top})`);

    // Scales
    const xScale = d3.scaleBand()
      .domain(channels.map(c => c.id))
      .range([0, chartWidth])
      .padding(0.2);

    const yScale = d3.scaleLinear()
      .domain([0, d3.max(channels, c => Math.max(c.bandwidth, c.queue.length * 10, 100))])
      .range([chartHeight, 0]);

    // Draw bandwidth bars
    g.selectAll('.bandwidth-bar')
      .data(channels)
      .enter()
      .append('rect')
      .attr('class', 'bandwidth-bar')
      .attr('x', d => xScale(d.id))
      .attr('y', d => yScale(d.bandwidth))
      .attr('width', xScale.bandwidth())
      .attr('height', d => chartHeight - yScale(d.bandwidth))
      .attr('fill', d => getPriorityColor(d.priority))
      .attr('opacity', 0.7);

    // Draw queue size indicators
    g.selectAll('.queue-indicator')
      .data(channels)
      .enter()
      .append('rect')
      .attr('class', 'queue-indicator')
      .attr('x', d => xScale(d.id) + xScale.bandwidth() / 4)
      .attr('y', d => yScale(d.queue.length * 10))
      .attr('width', xScale.bandwidth() / 2)
      .attr('height', d => chartHeight - yScale(d.queue.length * 10))
      .attr('fill', '#FF6B6B')
      .attr('opacity', 0.5);

    // Labels
    g.selectAll('.bandwidth-label')
      .data(channels)
      .enter()
      .append('text')
      .attr('class', 'bandwidth-label')
      .attr('x', d => xScale(d.id) + xScale.bandwidth() / 2)
      .attr('y', d => yScale(d.bandwidth) - 5)
      .attr('text-anchor', 'middle')
      .attr('font-size', '11px')
      .attr('fill', '#333')
      .text(d => `${d.bandwidth.toFixed(1)}`);

    g.selectAll('.queue-label')
      .data(channels)
      .enter()
      .append('text')
      .attr('class', 'queue-label')
      .attr('x', d => xScale(d.id) + xScale.bandwidth() / 2)
      .attr('y', d => yScale(d.queue.length * 10) - 5)
      .attr('text-anchor', 'middle')
      .attr('font-size', '10px')
      .attr('fill', '#FF6B6B')
      .text(d => `Q:${d.queue.length}`);

    // Axes
    const xAxis = d3.axisBottom(xScale);
    const yAxis = d3.axisLeft(yScale);

    g.append('g')
      .attr('class', 'x-axis')
      .attr('transform', `translate(0,${chartHeight})`)
      .call(xAxis)
      .selectAll('text')
      .attr('transform', 'rotate(-45)')
      .style('text-anchor', 'end');

    g.append('g')
      .attr('class', 'y-axis')
      .call(yAxis);

    // Y-axis label
    g.append('text')
      .attr('transform', 'rotate(-90)')
      .attr('y', -60)
      .attr('x', -chartHeight / 2)
      .attr('text-anchor', 'middle')
      .attr('font-size', '12px')
      .text('Bandwidth / Queue Size');

    // Legend
    const legend = g.append('g')
      .attr('class', 'legend')
      .attr('transform', `translate(${chartWidth - 150}, 0)`);

    legend.append('rect')
      .attr('x', 0)
      .attr('y', 0)
      .attr('width', 15)
      .attr('height', 15)
      .attr('fill', '#4CAF50')
      .attr('opacity', 0.7);

    legend.append('text')
      .attr('x', 20)
      .attr('y', 12)
      .attr('font-size', '11px')
      .text('Bandwidth');

    legend.append('rect')
      .attr('x', 0)
      .attr('y', 20)
      .attr('width', 15)
      .attr('height', 15)
      .attr('fill', '#FF6B6B')
      .attr('opacity', 0.5);

    legend.append('text')
      .attr('x', 20)
      .attr('y', 32)
      .attr('font-size', '11px')
      .text('Queue Size');

  }, [channels]);

  function getPriorityColor(priority) {
    const colors = {
      'CRITICAL': '#E91E63',
      'HIGH': '#FF9800',
      'NORMAL': '#4CAF50',
      'LOW': '#2196F3'
    };
    return colors[priority] || '#4CAF50';
  }

  return (
    <div className="channel-visualization">
      <div className="resource-summary">
        <div className="resource-item">
          <span className="label">Total Bandwidth:</span>
          <span className="value">{resourceState?.availableBandwidth?.toFixed(1) || 0}</span>
        </div>
        <div className="resource-item">
          <span className="label">Total Channels:</span>
          <span className="value">{channels?.length || 0}</span>
        </div>
        <div className="resource-item">
          <span className="label">Total Queued:</span>
          <span className="value">{channels?.reduce((sum, c) => sum + c.queue.length, 0) || 0}</span>
        </div>
      </div>

      <svg ref={svgRef} width="100%" height="400"></svg>

      <div className="channel-details">
        {channels && channels.map(channel => (
          <div key={channel.id} className="channel-card">
            <div className="channel-header">
              <span className="channel-id">{channel.id}</span>
              <span className={`priority-badge priority-${channel.priority.toLowerCase()}`}>
                {channel.priority}
              </span>
            </div>
            <div className="channel-stats">
              <div className="stat">
                <span className="stat-label">Bandwidth:</span>
                <span className="stat-value">{channel.bandwidth.toFixed(1)}</span>
              </div>
              <div className="stat">
                <span className="stat-label">Queue:</span>
                <span className="stat-value">{channel.queue.length}</span>
              </div>
              <div className="stat">
                <span className="stat-label">Time Slots:</span>
                <span className="stat-value">{channel.timeSlots || 0}</span>
              </div>
              <div className="stat">
                <span className="stat-label">Utilization:</span>
                <span className="stat-value">{(channel.utilization * 100).toFixed(0)}%</span>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default ChannelVisualization;
