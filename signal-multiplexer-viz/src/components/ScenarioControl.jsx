import { useState } from 'react';
import './ScenarioControl.css';

const SCENARIOS = [
  {
    mode: 'steady',
    name: 'Steady State',
    description: 'Normal operating conditions with occasional bursts',
    icon: '📊',
    color: '#4CAF50'
  },
  {
    mode: 'burst',
    name: 'Bursty Traffic',
    description: 'Frequent traffic bursts with high-priority signals',
    icon: '💥',
    color: '#FF9800'
  },
  {
    mode: 'overload',
    name: 'System Overload',
    description: 'Heavy sustained load testing system limits',
    icon: '🔥',
    color: '#f44336'
  },
  {
    mode: 'recovery',
    name: 'Recovery Mode',
    description: 'Low traffic allowing queues to drain',
    icon: '🔄',
    color: '#2196F3'
  },
  {
    mode: 'mixed',
    name: 'Mixed Pattern',
    description: 'Oscillating traffic with predictable cycles',
    icon: '🌊',
    color: '#9C27B0'
  }
];

function ScenarioControl({ currentScenario, onScenarioChange, disabled }) {
  const [intensity, setIntensity] = useState(currentScenario?.intensity || 1.0);

  const handleScenarioSelect = (mode) => {
    if (!disabled) {
      onScenarioChange(mode, intensity);
    }
  };

  const handleIntensityChange = (e) => {
    const newIntensity = parseFloat(e.target.value);
    setIntensity(newIntensity);
    if (currentScenario?.mode) {
      onScenarioChange(currentScenario.mode, newIntensity);
    }
  };

  return (
    <div className="scenario-control">
      <div className="scenario-header">
        <h3>Operational Scenario</h3>
        <div className="current-scenario-badge">
          <span className="scenario-icon">
            {SCENARIOS.find(s => s.mode === currentScenario?.mode)?.icon || '📊'}
          </span>
          <span className="scenario-name">
            {SCENARIOS.find(s => s.mode === currentScenario?.mode)?.name || 'None'}
          </span>
        </div>
      </div>

      <div className="scenario-grid">
        {SCENARIOS.map(scenario => {
          const isActive = currentScenario?.mode === scenario.mode;
          return (
            <button
              key={scenario.mode}
              className={`scenario-card ${isActive ? 'active' : ''} ${disabled ? 'disabled' : ''}`}
              onClick={() => handleScenarioSelect(scenario.mode)}
              disabled={disabled}
              style={{
                borderLeftColor: isActive ? scenario.color : '#ddd'
              }}
            >
              <div className="scenario-card-icon">{scenario.icon}</div>
              <div className="scenario-card-content">
                <div className="scenario-card-name">{scenario.name}</div>
                <div className="scenario-card-description">{scenario.description}</div>
              </div>
              {isActive && (
                <div className="active-indicator" style={{ background: scenario.color }}></div>
              )}
            </button>
          );
        })}
      </div>

      <div className="intensity-control">
        <label htmlFor="intensity-slider">
          <span className="label-text">Traffic Intensity:</span>
          <span className="intensity-value">{intensity.toFixed(1)}x</span>
        </label>
        <input
          id="intensity-slider"
          type="range"
          min="0.1"
          max="3.0"
          step="0.1"
          value={intensity}
          onChange={handleIntensityChange}
          disabled={disabled}
          className="intensity-slider"
        />
        <div className="intensity-markers">
          <span>Low</span>
          <span>Normal</span>
          <span>High</span>
        </div>
      </div>

      <div className="scenario-info">
        <h4>How Scenarios Work</h4>
        <ul>
          <li><strong>Steady</strong> - Normal operational baseline with predictable patterns</li>
          <li><strong>Bursty</strong> - Tests adaptation to sudden traffic spikes</li>
          <li><strong>Overload</strong> - Stresses system to reveal constraint tightening</li>
          <li><strong>Recovery</strong> - Demonstrates queue drainage and relaxation</li>
          <li><strong>Mixed</strong> - Shows adaptation to cyclical patterns</li>
        </ul>
        <p className="parameter-interplay-note">
          <strong>Parameter Interplay:</strong> Watch how changing scenarios affects bandwidth allocation,
          constraint tightness, queue sizes, and fairness indices in real-time.
        </p>
      </div>
    </div>
  );
}

export default ScenarioControl;
