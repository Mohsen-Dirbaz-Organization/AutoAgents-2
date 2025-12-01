import './PerformanceMetrics.css';

function PerformanceMetrics({ metrics }) {
  if (!metrics) {
    return (
      <div className="performance-metrics">
        <div className="empty-state">
          <p>No performance metrics available yet.</p>
        </div>
      </div>
    );
  }

  const metricsData = [
    {
      name: 'Total Signals Processed',
      value: metrics.totalSignalsProcessed || 0,
      unit: 'signals',
      icon: '📊',
      color: '#4CAF50'
    },
    {
      name: 'Average Latency',
      value: (metrics.averageLatency || 0).toFixed(2),
      unit: 'ms',
      icon: '⏱️',
      color: '#2196F3'
    },
    {
      name: 'Throughput',
      value: (metrics.throughput || 0).toFixed(2),
      unit: 'signals/s',
      icon: '⚡',
      color: '#FF9800'
    },
    {
      name: 'Fairness Index',
      value: (metrics.fairnessIndex || 1.0).toFixed(3),
      unit: '',
      icon: '⚖️',
      color: '#9C27B0'
    }
  ];

  return (
    <div className="performance-metrics">
      <div className="metrics-grid">
        {metricsData.map((metric, i) => (
          <div key={i} className="metric-card" style={{ borderLeftColor: metric.color }}>
            <div className="metric-icon">{metric.icon}</div>
            <div className="metric-content">
              <div className="metric-name">{metric.name}</div>
              <div className="metric-value">
                <span className="value">{metric.value}</span>
                {metric.unit && <span className="unit">{metric.unit}</span>}
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="performance-explanation">
        <h4>Performance Objectives</h4>
        <div className="objectives-list">
          <div className="objective-item">
            <div className="objective-name">Minimize Latency</div>
            <div className="objective-formula">
              <code>Σ(queue_i / bandwidth_i) × priority_i</code>
            </div>
            <div className="objective-description">
              Weighted sum of queue delays, prioritizing high-priority channels
            </div>
          </div>

          <div className="objective-item">
            <div className="objective-name">Maximize Throughput</div>
            <div className="objective-formula">
              <code>Σ(bandwidth_i × utilization_i)</code>
            </div>
            <div className="objective-description">
              Total effective bandwidth utilization across all channels
            </div>
          </div>

          <div className="objective-item">
            <div className="objective-name">Balance Fairness</div>
            <div className="objective-formula">
              <code>minimize variance(service_rates)</code>
            </div>
            <div className="objective-description">
              Ensure equitable resource distribution while respecting priorities
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default PerformanceMetrics;
