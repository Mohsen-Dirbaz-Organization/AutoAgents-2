import './SolverVisualization.css';

function SolverVisualization({ structure, solver, solution }) {
  if (!structure || !solver) {
    return (
      <div className="solver-visualization">
        <div className="empty-state">
          <p>No solver selected yet. Start the simulation to see structure detection and solver selection.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="solver-visualization">
      <div className="structure-detection">
        <h3>Problem Structure</h3>
        <div className="structure-properties">
          <div className={`property ${structure.isConvex ? 'true' : 'false'}`}>
            <span className="property-icon">{structure.isConvex ? '✓' : '✗'}</span>
            <span className="property-name">Convex</span>
          </div>
          <div className={`property ${structure.isMixedInteger ? 'true' : 'false'}`}>
            <span className="property-icon">{structure.isMixedInteger ? '✓' : '✗'}</span>
            <span className="property-name">Mixed-Integer</span>
          </div>
          <div className={`property ${structure.isStochastic ? 'true' : 'false'}`}>
            <span className="property-icon">{structure.isStochastic ? '✓' : '✗'}</span>
            <span className="property-name">Stochastic</span>
          </div>
          <div className={`property ${structure.hasMultipleObjectives ? 'true' : 'false'}`}>
            <span className="property-icon">{structure.hasMultipleObjectives ? '✓' : '✗'}</span>
            <span className="property-name">Multi-Objective</span>
          </div>
        </div>
        <div className="structure-class">
          <span className="label">Problem Class:</span>
          <span className="value">{structure.class}</span>
        </div>
      </div>

      <div className="solver-selection">
        <h3>Selected Solver</h3>
        <div className="solver-card">
          <div className="solver-header">
            <span className="solver-name">{solver.name}</span>
            <span className={`solver-type type-${solver.type}`}>{solver.type}</span>
          </div>
          <div className="solver-description">{solver.description}</div>
          <div className="solver-properties">
            <div className="solver-property">
              <span className="property-label">Complexity:</span>
              <span className="property-value">
                <code>{solver.complexity}</code>
              </span>
            </div>
            <div className="solver-guarantees">
              <span className="property-label">Guarantees:</span>
              <ul>
                {solver.guarantees && solver.guarantees.map((guarantee, i) => (
                  <li key={i}>{guarantee}</li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      </div>

      {solution && solution.feasible && (
        <div className="solution-results">
          <h3>Solution</h3>
          <div className="solution-status">
            <span className={`status-badge ${solution.feasible ? 'feasible' : 'infeasible'}`}>
              {solution.feasible ? 'Feasible Solution Found' : 'Infeasible'}
            </span>
          </div>
          <div className="solution-metrics">
            <div className="metric">
              <span className="metric-label">Solver Time:</span>
              <span className="metric-value">{solution.solverTime?.toFixed(2)} ms</span>
            </div>
            <div className="metric">
              <span className="metric-label">Iterations:</span>
              <span className="metric-value">{solution.iterations}</span>
            </div>
            <div className="metric">
              <span className="metric-label">Objective Value:</span>
              <span className="metric-value">{solution.objectiveValue?.toFixed(3)}</span>
            </div>
          </div>
          <div className="allocation-summary">
            <h4>Bandwidth Allocations</h4>
            <div className="allocations">
              {solution.bandwidthAllocations && Object.entries(solution.bandwidthAllocations).map(([channelId, bandwidth]) => (
                <div key={channelId} className="allocation-item">
                  <span className="allocation-channel">{channelId}:</span>
                  <span className="allocation-value">{bandwidth.toFixed(2)}</span>
                  <div className="allocation-bar">
                    <div
                      className="allocation-fill"
                      style={{ width: `${Math.min(100, (bandwidth / 200) * 100)}%` }}
                    ></div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      <div className="solver-philosophy">
        <h4>Solver Selection Philosophy</h4>
        <p>
          The solver is <strong>chosen dynamically</strong> based on detected problem structure.
          Convex problems use interior-point methods. Mixed-integer problems use branch-and-bound.
          Multi-objective problems use weighted scalarization. This ensures efficient, appropriate
          mathematical methods for each reformulation.
        </p>
      </div>
    </div>
  );
}

export default SolverVisualization;
