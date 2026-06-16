import './StackControlPanel.css';

const SCENARIOS = [
  { id: 'nominal', label: 'Nominal', hint: 'Warrant high and stable' },
  { id: 'degrading', label: 'Degrading warrant', hint: 'Benign-interpretation warrant falls over time' },
  { id: 'sensor_drift', label: 'Sensor drift', hint: 'Oscillating, noisy warrant' },
  { id: 'adversarial', label: 'Adversarial', hint: 'Low warrant, spoof-prone' },
  { id: 'recovery', label: 'Recovery', hint: 'Warrant climbing back' }
];

function StackControlPanel({
  isRunning, onToggle, onReset,
  scenario, onScenarioChange,
  conservativeDiscipline, onToggleDiscipline,
  onRaiseRisk, onInjectDrift, onSpoof
}) {
  return (
    <div className="stack-control-panel">
      <div className="scp-row scp-primary">
        <button className={`scp-btn ${isRunning ? 'running' : 'stopped'}`} onClick={onToggle}>
          {isRunning ? '⏸ Pause stack' : '▶ Run stack'}
        </button>
        <button className="scp-btn neutral" onClick={onReset}>↺ Reset</button>
      </div>

      <div className="scp-block">
        <div className="scp-label">Operational scenario</div>
        <div className="scp-scenarios">
          {SCENARIOS.map(s => (
            <button
              key={s.id}
              className={`scp-chip ${scenario.mode === s.id ? 'active' : ''}`}
              title={s.hint}
              onClick={() => onScenarioChange(s.id, scenario.intensity)}
            >
              {s.label}
            </button>
          ))}
        </div>
        <div className="scp-intensity">
          <span>Intensity</span>
          <input
            type="range" min="0.2" max="3" step="0.1"
            value={scenario.intensity}
            onChange={e => onScenarioChange(scenario.mode, parseFloat(e.target.value))}
          />
          <span className="scp-intensity-val">{scenario.intensity.toFixed(1)}×</span>
        </div>
      </div>

      <div className="scp-block">
        <div className="scp-label">WS-2 conservative read/write discipline</div>
        <button
          className={`scp-toggle ${conservativeDiscipline ? 'on' : 'off'}`}
          onClick={() => onToggleDiscipline(!conservativeDiscipline)}
        >
          <span className="scp-toggle-knob" />
          <span className="scp-toggle-text">
            {conservativeDiscipline ? 'ON — antitone law enforced by construction' : 'OFF — raw analog devices (non-monotone)'}
          </span>
        </button>
        <p className="scp-help">
          With discipline ON, device noise can only ever shrink the admissible set
          (monotonicity violations → 0). Turn it OFF to watch raw memristor
          non-idealities re-admit forbidden commands.
        </p>
      </div>

      <div className="scp-block">
        <div className="scp-label">Inject events</div>
        <div className="scp-injectors">
          <button className="scp-inject" onClick={() => onRaiseRisk(0.25)}>⚠ Drop warrant</button>
          <button className="scp-inject" onClick={onInjectDrift}>📈 Inject drift (LLC)</button>
          <button className="scp-inject danger" onClick={onSpoof}>🎭 Spoof spike</button>
        </div>
      </div>
    </div>
  );
}

export default StackControlPanel;
