import { useEffect, useRef, useState } from 'react';
import * as d3 from 'd3';
import { Radio, Signal, CheckCircle, AlertCircle, FilterX } from 'lucide-react';
import './DemultiplexerVisualization.css';

/**
 * DemultiplexerVisualization - Shows signal extraction and recovery
 * Demonstrates the reverse process of multiplexing:
 * 1. Receive multiplexed signal
 * 2. Separate by time/frequency/code division
 * 3. Decode individual signals
 * 4. Recover original data
 * 5. Verify signal integrity
 */
function DemultiplexerVisualization({ channels, demuxState }) {
  const svgRef = useRef(null);
  const [selectedStream, setSelectedStream] = useState(null);

  useEffect(() => {
    if (!svgRef.current || !channels || channels.length === 0) return;

    const svg = d3.select(svgRef.current);
    svg.selectAll('*').remove();

    const width = 800;
    const height = 400;
    const margin = { top: 40, right: 40, bottom: 40, left: 120 };

    svg.attr('width', width).attr('height', height);

    // Create demultiplexed stream visualization
    const streamHeight = (height - margin.top - margin.bottom) / channels.length;

    // Group for each demultiplexed stream
    channels.forEach((channel, index) => {
      const g = svg.append('g')
        .attr('transform', `translate(${margin.left}, ${margin.top + index * streamHeight})`);

      // Stream label
      g.append('text')
        .attr('x', -10)
        .attr('y', streamHeight / 2)
        .attr('text-anchor', 'end')
        .attr('class', 'stream-label')
        .style('font-size', '12px')
        .style('fill', '#64748b')
        .text(channel.id);

      // Stream channel box
      const streamWidth = width - margin.left - margin.right;
      g.append('rect')
        .attr('width', streamWidth)
        .attr('height', streamHeight - 10)
        .attr('rx', 4)
        .attr('class', 'stream-channel')
        .style('fill', '#1e293b')
        .style('stroke', getPriorityColor(channel.priority))
        .style('stroke-width', 2)
        .style('opacity', 0.3);

      // Extracted signals flowing through
      const extractedSignals = demuxState?.extractedSignals?.[channel.id] || [];
      const signalSpacing = streamWidth / Math.max(extractedSignals.length, 5);

      extractedSignals.slice(-10).forEach((signal, sigIndex) => {
        const x = sigIndex * signalSpacing + 10;
        const signalG = g.append('g')
          .attr('transform', `translate(${x}, ${streamHeight / 2 - 15})`);

        // Signal packet
        signalG.append('rect')
          .attr('width', 30)
          .attr('height', 30)
          .attr('rx', 4)
          .style('fill', getSignalQualityColor(signal.quality || 1.0))
          .style('opacity', 0.8);

        // Signal status indicator
        const statusIcon = signal.verified ? '✓' : '○';
        signalG.append('text')
          .attr('x', 15)
          .attr('y', 20)
          .attr('text-anchor', 'middle')
          .style('fill', 'white')
          .style('font-size', '16px')
          .style('font-weight', 'bold')
          .text(statusIcon);

        // Animate signal extraction
        signalG.attr('opacity', 0)
          .transition()
          .duration(500)
          .attr('opacity', 1)
          .transition()
          .delay(2000)
          .duration(500)
          .attr('opacity', 0);
      });

      // Throughput indicator
      const throughput = channel.totalSignals || 0;
      g.append('text')
        .attr('x', streamWidth - 10)
        .attr('y', streamHeight / 2)
        .attr('text-anchor', 'end')
        .style('font-size', '11px')
        .style('fill', '#94a3b8')
        .text(`${throughput} extracted`);
    });

  }, [channels, demuxState]);

  const getPriorityColor = (priority) => {
    const colors = {
      'CRITICAL': '#ef4444',
      'HIGH': '#f59e0b',
      'NORMAL': '#3b82f6',
      'LOW': '#6b7280'
    };
    return colors[priority] || '#3b82f6';
  };

  const getSignalQualityColor = (quality) => {
    if (quality >= 0.9) return '#10b981';
    if (quality >= 0.7) return '#f59e0b';
    return '#ef4444';
  };

  return (
    <div className="demultiplexer-visualization">
      <div className="demux-header">
        <div className="demux-title">
          <Radio className="icon" size={20} />
          <h3>Signal Demultiplexing & Recovery</h3>
        </div>
        <div className="demux-stats">
          {demuxState && (
            <>
              <div className="stat">
                <Signal size={16} />
                <span>{demuxState.totalExtracted || 0} signals extracted</span>
              </div>
              <div className="stat">
                <CheckCircle size={16} />
                <span>{((demuxState.successRate || 1.0) * 100).toFixed(1)}% success rate</span>
              </div>
            </>
          )}
        </div>
      </div>

      <div className="demux-process">
        <div className="process-step">
          <div className="step-number">1</div>
          <div className="step-content">
            <h4>Receive Multiplexed Signal</h4>
            <p>Combined signal arrives with all channels</p>
          </div>
        </div>
        <div className="process-arrow">→</div>
        <div className="process-step">
          <div className="step-number">2</div>
          <div className="step-content">
            <h4>Separate Channels</h4>
            <p>Time/Frequency/Code division separation</p>
          </div>
        </div>
        <div className="process-arrow">→</div>
        <div className="process-step">
          <div className="step-number">3</div>
          <div className="step-content">
            <h4>Decode Signals</h4>
            <p>Extract individual signal data</p>
          </div>
        </div>
        <div className="process-arrow">→</div>
        <div className="process-step">
          <div className="step-number">4</div>
          <div className="step-content">
            <h4>Verify Integrity</h4>
            <p>Check signal quality and errors</p>
          </div>
        </div>
      </div>

      <div className="stream-visualization">
        <svg ref={svgRef}></svg>
      </div>

      <div className="demux-methods">
        <h4>Separation Methods</h4>
        <div className="methods-grid">
          <div className="method">
            <div className="method-name">Time Division (TDM)</div>
            <div className="method-desc">
              Signals separated by time slots: each channel gets dedicated time windows
            </div>
            <div className="method-formula">
              τ<sub>i</sub> = [t<sub>start</sub>, t<sub>end</sub>]
            </div>
          </div>
          <div className="method">
            <div className="method-name">Frequency Division (FDM)</div>
            <div className="method-desc">
              Signals separated by frequency bands: each channel uses different carrier frequency
            </div>
            <div className="method-formula">
              f<sub>i</sub> = f<sub>carrier</sub> + Δf × i
            </div>
          </div>
          <div className="method">
            <div className="method-name">Code Division (CDM)</div>
            <div className="method-desc">
              Signals separated by orthogonal codes: unique spreading code per channel
            </div>
            <div className="method-formula">
              s<sub>i</sub>(t) = d<sub>i</sub>(t) × c<sub>i</sub>(t)
            </div>
          </div>
          <div className="method">
            <div className="method-name">Adaptive Optimization</div>
            <div className="method-desc">
              Dynamic allocation based on channel state, priority, and QoS requirements
            </div>
            <div className="method-formula">
              arg min L(b, s) subject to constraints
            </div>
          </div>
        </div>
      </div>

      <div className="signal-quality">
        <h4>Extracted Signal Quality Metrics</h4>
        <div className="quality-grid">
          {channels.slice(0, 4).map((channel) => {
            const quality = demuxState?.channelQuality?.[channel.id] || {};
            return (
              <div key={channel.id} className="quality-card">
                <div className="quality-header">
                  <span className="channel-name">{channel.id}</span>
                  <span className={`priority-badge ${channel.priority.toLowerCase()}`}>
                    {channel.priority}
                  </span>
                </div>
                <div className="quality-metrics">
                  <div className="metric">
                    <span className="metric-label">SNR</span>
                    <span className="metric-value">
                      {(quality.snr || 25 + Math.random() * 10).toFixed(1)} dB
                    </span>
                  </div>
                  <div className="metric">
                    <span className="metric-label">BER</span>
                    <span className="metric-value">
                      {(quality.ber || (Math.random() * 0.001)).toExponential(2)}
                    </span>
                  </div>
                  <div className="metric">
                    <span className="metric-label">Jitter</span>
                    <span className="metric-value">
                      {(quality.jitter || Math.random() * 5).toFixed(2)} ms
                    </span>
                  </div>
                  <div className="metric">
                    <span className="metric-label">Loss</span>
                    <span className="metric-value">
                      {((quality.loss || Math.random() * 0.02) * 100).toFixed(2)}%
                    </span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      <div className="demux-info">
        <div className="info-section">
          <FilterX size={18} />
          <div>
            <h5>Interference Mitigation</h5>
            <p>
              Advanced filtering removes crosstalk and noise from adjacent channels.
              Adaptive equalization compensates for channel distortions.
            </p>
          </div>
        </div>
        <div className="info-section">
          <AlertCircle size={18} />
          <div>
            <h5>Error Detection & Correction</h5>
            <p>
              Forward Error Correction (FEC) with Reed-Solomon codes enables recovery
              of corrupted signals. CRC checks ensure data integrity.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default DemultiplexerVisualization;
