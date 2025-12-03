import { useEffect, useRef } from 'react';
import * as d3 from 'd3';
import './AgentDeploymentViz.css';

function AgentDeploymentViz({ channels, state }) {
  const svgRef = useRef();

  useEffect(() => {
    if (!channels || channels.length === 0) return;

    const svg = d3.select(svgRef.current);
    svg.selectAll('*').remove();

    const width = 700;
    const height = 400;
    const margin = { top: 40, right: 20, bottom: 40, left: 20 };

    const g = svg
      .attr('width', width)
      .attr('height', height)
      .append('g')
      .attr('transform', `translate(${margin.left},${margin.top})`);

    const chartWidth = width - margin.left - margin.right;
    const chartHeight = height - margin.top - margin.bottom;

    // Title
    g.append('text')
      .attr('x', chartWidth / 2)
      .attr('y', -15)
      .attr('text-anchor', 'middle')
      .style('font-size', '16px')
      .style('font-weight', '700')
      .style('fill', '#667eea')
      .text('Dual-Purpose Agent Deployment');

    // Calculate agent allocation
    const totalAgents = 20;
    const agentData = channels.map((channel, idx) => {
      const queueSize = channel.queue?.length || 0;
      const totalQueue = channels.reduce((sum, ch) => sum + (ch.queue?.length || 0), 0);

      // Specialized agents (fixed)
      const specializedAgents = Math.floor(totalAgents * 0.15); // 15% dedicated

      // Public utility agents (dynamic based on queue)
      const publicShare = totalQueue > 0 ? queueSize / totalQueue : 0.25;
      const publicAgents = Math.floor((totalAgents - channels.length * specializedAgents) * publicShare);

      return {
        channelId: channel.id,
        priority: channel.priority,
        queueSize,
        specialized: specializedAgents,
        public: publicAgents,
        total: specializedAgents + publicAgents,
        color: getPriorityColor(channel.priority)
      };
    });

    const channelHeight = chartHeight / channels.length;

    // Draw channels
    channels.forEach((channel, idx) => {
      const y = idx * channelHeight;
      const data = agentData[idx];

      // Channel background
      g.append('rect')
        .attr('x', 0)
        .attr('y', y)
        .attr('width', chartWidth)
        .attr('height', channelHeight - 10)
        .attr('fill', data.color)
        .attr('opacity', 0.1)
        .attr('rx', 8);

      // Channel label
      g.append('text')
        .attr('x', 10)
        .attr('y', y + 20)
        .style('font-size', '13px')
        .style('font-weight', '700')
        .style('fill', '#333')
        .text(`${channel.id} (${channel.priority})`);

      // Queue size indicator
      g.append('text')
        .attr('x', 10)
        .attr('y', y + 38)
        .style('font-size', '11px')
        .style('fill', '#666')
        .text(`Queue: ${data.queueSize} signals`);

      // Agent visualization area
      const agentStartX = 200;
      const agentAreaWidth = chartWidth - agentStartX - 20;
      const agentSize = 18;
      const agentSpacing = 4;
      const agentsPerRow = Math.floor(agentAreaWidth / (agentSize + agentSpacing));

      // Draw specialized agents
      for (let i = 0; i < data.specialized; i++) {
        const row = Math.floor(i / agentsPerRow);
        const col = i % agentsPerRow;
        const x = agentStartX + col * (agentSize + agentSpacing);
        const agentY = y + 15 + row * (agentSize + agentSpacing);

        g.append('rect')
          .attr('x', x)
          .attr('y', agentY)
          .attr('width', agentSize)
          .attr('height', agentSize)
          .attr('fill', data.color)
          .attr('stroke', '#333')
          .attr('stroke-width', 2)
          .attr('rx', 3)
          .append('title')
          .text('Specialized Agent - Dedicated to this channel');

        // Add "S" for specialized
        g.append('text')
          .attr('x', x + agentSize / 2)
          .attr('y', agentY + agentSize / 2 + 4)
          .attr('text-anchor', 'middle')
          .style('font-size', '10px')
          .style('font-weight', '700')
          .style('fill', 'white')
          .text('S');
      }

      // Draw public utility agents
      for (let i = 0; i < data.public; i++) {
        const totalIdx = data.specialized + i;
        const row = Math.floor(totalIdx / agentsPerRow);
        const col = totalIdx % agentsPerRow;
        const x = agentStartX + col * (agentSize + agentSpacing);
        const agentY = y + 15 + row * (agentSize + agentSpacing);

        g.append('rect')
          .attr('x', x)
          .attr('y', agentY)
          .attr('width', agentSize)
          .attr('height', agentSize)
          .attr('fill', data.color)
          .attr('opacity', 0.6)
          .attr('stroke', '#999')
          .attr('stroke-width', 1)
          .attr('stroke-dasharray', '2,2')
          .attr('rx', 3)
          .append('title')
          .text('Public Utility Agent - Shared resource allocated dynamically');

        // Add "P" for public
        g.append('text')
          .attr('x', x + agentSize / 2)
          .attr('y', agentY + agentSize / 2 + 4)
          .attr('text-anchor', 'middle')
          .style('font-size', '10px')
          .style('font-weight', '700')
          .style('fill', '#333')
          .text('P');
      }

      // Agent count summary
      g.append('text')
        .attr('x', agentStartX - 10)
        .attr('y', y + channelHeight / 2 + 5)
        .attr('text-anchor', 'end')
        .style('font-size', '12px')
        .style('font-weight', '600')
        .style('fill', '#667eea')
        .text(`${data.total} agents`);
    });

  }, [channels, state]);

  function getPriorityColor(priority) {
    const colors = {
      CRITICAL: '#f44336',
      HIGH: '#ff9800',
      NORMAL: '#4CAF50',
      LOW: '#2196F3'
    };
    return colors[priority] || '#999';
  }

  return (
    <div className="agent-deployment-viz">
      <div className="deployment-explanation">
        <div className="explanation-card">
          <div className="card-icon">👤</div>
          <div className="card-content">
            <h4>Specialized Agents (S)</h4>
            <p>Dedicated to specific channels - always available for their priority signals</p>
          </div>
        </div>
        <div className="explanation-card">
          <div className="card-icon">👥</div>
          <div className="card-content">
            <h4>Public Utility Agents (P)</h4>
            <p>Dynamically allocated based on queue sizes - helps overloaded channels</p>
          </div>
        </div>
      </div>

      <svg ref={svgRef}></svg>

      <div className="deployment-insights">
        <div className="insight-box">
          <strong>Why Dual-Purpose?</strong>
          <p>
            Traditional systems waste resources by dedicating agents to channels that might
            be idle. Our dual-purpose approach ensures zero idle time: specialized agents
            handle their designated signals, while a shared pool of public utility agents
            dynamically helps wherever the queue is longest.
          </p>
        </div>
        <div className="insight-box">
          <strong>How Allocation Works:</strong>
          <p>
            Every 100ms, the system measures queue sizes and reallocates public utility
            agents proportionally. If critical signals arrive, public agents instantly
            shift to that channel. When queues drain, agents become available for other
            channels - achieving optimal resource utilization.
          </p>
        </div>
        <div className="insight-box highlight">
          <strong>Real-Time Adaptation:</strong>
          <p>
            Watch the public utility agents (dashed borders) shift between channels as
            queue sizes change. During burst scenarios, you'll see massive reallocation.
            During steady state, allocation is balanced. This is optimization in action!
          </p>
        </div>
      </div>
    </div>
  );
}

export default AgentDeploymentViz;
