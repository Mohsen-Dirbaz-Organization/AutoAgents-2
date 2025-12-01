import { useState } from 'react';
import './ControlPanel.css';

function ControlPanel({ isRunning, onToggle, onInjectSignal, onInjectDisturbance, channels }) {
  const [selectedChannel, setSelectedChannel] = useState('channel-1');
  const [selectedPriority, setSelectedPriority] = useState('NORMAL');

  const handleInjectSignal = () => {
    onInjectSignal(selectedChannel, selectedPriority);
  };

  return (
    <div className="control-panel">
      <div className="simulation-control">
        <button
          onClick={onToggle}
          className={`btn-primary ${isRunning ? 'running' : 'stopped'}`}
        >
          {isRunning ? '⏸ Pause Simulation' : '▶ Start Simulation'}
        </button>
        <div className="status-indicator">
          <span className={`status-dot ${isRunning ? 'active' : 'inactive'}`}></span>
          <span className="status-text">{isRunning ? 'Running' : 'Stopped'}</span>
        </div>
      </div>

      <div className="signal-injection">
        <h4>Manual Signal Injection</h4>
        <div className="injection-controls">
          <div className="control-group">
            <label htmlFor="channel-select">Channel:</label>
            <select
              id="channel-select"
              value={selectedChannel}
              onChange={(e) => setSelectedChannel(e.target.value)}
              className="control-select"
            >
              {channels && channels.map(channel => (
                <option key={channel.id} value={channel.id}>
                  {channel.id}
                </option>
              ))}
            </select>
          </div>

          <div className="control-group">
            <label htmlFor="priority-select">Priority:</label>
            <select
              id="priority-select"
              value={selectedPriority}
              onChange={(e) => setSelectedPriority(e.target.value)}
              className="control-select"
            >
              <option value="CRITICAL">CRITICAL</option>
              <option value="HIGH">HIGH</option>
              <option value="NORMAL">NORMAL</option>
              <option value="LOW">LOW</option>
            </select>
          </div>

          <button onClick={handleInjectSignal} className="btn-inject">
            Inject Signal
          </button>
        </div>
      </div>

      <div className="disturbance-control">
        <h4>System Disturbances</h4>
        <button onClick={onInjectDisturbance} className="btn-disturbance">
          ⚡ Inject Traffic Burst
        </button>
        <p className="disturbance-description">
          Injects 10 critical signals to test system adaptation
        </p>
      </div>

      <div className="control-info">
        <h4>Interactive Features</h4>
        <ul>
          <li>Start/stop the continuous optimization loop (100ms cycle)</li>
          <li>Manually inject signals into specific channels</li>
          <li>Trigger disturbances to observe adaptive response</li>
          <li>Watch real-time problem reformulation and solving</li>
        </ul>
      </div>
    </div>
  );
}

export default ControlPanel;
