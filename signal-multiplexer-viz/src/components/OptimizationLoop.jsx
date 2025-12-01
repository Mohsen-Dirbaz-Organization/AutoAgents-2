import { useEffect, useState } from 'react';
import './OptimizationLoop.css';

const LOOP_STEPS = [
  { id: 'observe', name: 'OBSERVE', description: 'Capture system snapshot', icon: '👁️' },
  { id: 'formulate', name: 'FORMULATE', description: 'Construct optimization problem', icon: '📐' },
  { id: 'detect', name: 'DETECT', description: 'Identify problem structure', icon: '🔍' },
  { id: 'select', name: 'SELECT', description: 'Choose solver', icon: '⚙️' },
  { id: 'synthesize', name: 'SYNTHESIZE', description: 'Generate constraints', icon: '🧬' },
  { id: 'solve', name: 'SOLVE', description: 'Execute optimization', icon: '⚡' },
  { id: 'apply', name: 'APPLY', description: 'Update configuration', icon: '✅' }
];

const STATE_STEP_MAP = {
  'observing': 0,
  'formulating': 1,
  'solving': 5,
  'applying': 6,
  'idle': -1
};

function OptimizationLoop({ state }) {
  const [activeStep, setActiveStep] = useState(-1);
  const [cycleCount, setCycleCount] = useState(0);

  useEffect(() => {
    const stepIndex = STATE_STEP_MAP[state.state] !== undefined ? STATE_STEP_MAP[state.state] : -1;
    setActiveStep(stepIndex);

    if (state.state === 'idle' && activeStep === 6) {
      setCycleCount(prev => prev + 1);
    }
  }, [state.state]);

  // Automatically cycle through steps when idle for demo purposes
  useEffect(() => {
    if (state.state === 'idle' && activeStep !== -1) {
      const timer = setTimeout(() => {
        setActiveStep(-1);
      }, 500);
      return () => clearTimeout(timer);
    }
  }, [state.state, activeStep]);

  return (
    <div className="optimization-loop">
      <div className="loop-header">
        <div className="cycle-counter">
          <span className="label">Optimization Cycles:</span>
          <span className="count">{cycleCount}</span>
        </div>
        <div className="loop-status">
          <span className={`status-indicator ${state.state}`}></span>
          <span className="status-text">{state.state || 'idle'}</span>
        </div>
      </div>

      <div className="loop-steps">
        {LOOP_STEPS.map((step, index) => (
          <div key={step.id} className="step-container">
            <div className={`loop-step ${activeStep === index ? 'active' : ''} ${activeStep > index ? 'completed' : ''}`}>
              <div className="step-number">{index + 1}</div>
              <div className="step-icon">{step.icon}</div>
              <div className="step-content">
                <div className="step-name">{step.name}</div>
                <div className="step-description">{step.description}</div>
              </div>
              {activeStep === index && (
                <div className="step-pulse"></div>
              )}
            </div>
            {index < LOOP_STEPS.length - 1 && (
              <div className={`step-connector ${activeStep > index ? 'active' : ''}`}>
                <div className="connector-line"></div>
                <div className="connector-arrow">→</div>
              </div>
            )}
          </div>
        ))}
      </div>

      <div className="loop-cycle-indicator">
        <svg width="100%" height="60" viewBox="0 0 800 60">
          <defs>
            <marker id="arrowhead" markerWidth="10" markerHeight="10" refX="5" refY="3" orient="auto">
              <polygon points="0 0, 10 3, 0 6" fill="#4CAF50" />
            </marker>
          </defs>
          <path
            d="M 750 30 C 750 10, 50 10, 50 30"
            stroke="#4CAF50"
            strokeWidth="2"
            fill="none"
            markerEnd="url(#arrowhead)"
            className={activeStep === 6 ? 'cycle-path active' : 'cycle-path'}
          />
          <text x="400" y="20" textAnchor="middle" fill="#4CAF50" fontSize="12">
            100ms continuous reformulation
          </text>
        </svg>
      </div>

      <div className="loop-description">
        <p>
          <strong>Core Operation:</strong> Every 100ms, the system formulates a new optimization problem
          from current state, detects its mathematical structure, selects an appropriate solver,
          synthesizes physics-informed constraints, and applies the solution.
        </p>
      </div>
    </div>
  );
}

export default OptimizationLoop;
