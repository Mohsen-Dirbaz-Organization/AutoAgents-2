import './ConstraintPanel.css';

function ConstraintPanel({ constraints }) {
  if (!constraints || constraints.length === 0) {
    return (
      <div className="constraint-panel">
        <div className="empty-state">
          <p>No constraints synthesized yet. Start the simulation to see physics-informed constraint generation.</p>
        </div>
      </div>
    );
  }

  const groupedConstraints = constraints.reduce((groups, constraint) => {
    const category = constraint.category || 'other';
    if (!groups[category]) {
      groups[category] = [];
    }
    groups[category].push(constraint);
    return groups;
  }, {});

  const categoryIcons = {
    'conservation': '⚖️',
    'dynamics': '🔄',
    'service': '📋',
    'causality': '⏰',
    'other': '📌'
  };

  const categoryDescriptions = {
    'conservation': 'Conservation Laws - Fundamental physical constraints (bandwidth, energy, resources)',
    'dynamics': 'System Dynamics - Queue evolution and stability requirements',
    'service': 'Service Guarantees - Minimum performance requirements',
    'causality': 'Temporal Ordering - Causal relationships and time constraints'
  };

  return (
    <div className="constraint-panel">
      <div className="constraint-summary">
        <div className="summary-item">
          <span className="label">Total Constraints:</span>
          <span className="value">{constraints.length}</span>
        </div>
        <div className="summary-item">
          <span className="label">Physics-Informed:</span>
          <span className="value">
            {constraints.filter(c => c.type === 'physics-informed').length}
          </span>
        </div>
      </div>

      {Object.entries(groupedConstraints).map(([category, categoryConstraints]) => (
        <div key={category} className="constraint-category">
          <div className="category-header">
            <span className="category-icon">{categoryIcons[category] || '📌'}</span>
            <span className="category-name">{category.toUpperCase()}</span>
            <span className="category-count">({categoryConstraints.length})</span>
          </div>

          {categoryDescriptions[category] && (
            <div className="category-description">{categoryDescriptions[category]}</div>
          )}

          <div className="constraints-list">
            {categoryConstraints.slice(0, 5).map((constraint, i) => (
              <div key={i} className={`constraint-item type-${constraint.type}`}>
                <div className="constraint-header">
                  <span className="constraint-name">{constraint.name}</span>
                  {constraint.type === 'physics-informed' && (
                    <span className="physics-badge">Physics</span>
                  )}
                </div>
                <div className="constraint-description">{constraint.description}</div>
                {constraint.formula && (
                  <div className="constraint-formula">
                    <code>{constraint.formula}</code>
                  </div>
                )}
                {constraint.tightness && (
                  <div className="constraint-tightness">
                    <span className="label">Tightness:</span>
                    <div className="tightness-bar">
                      <div
                        className="tightness-fill"
                        style={{ width: `${Math.min(100, constraint.tightness * 50)}%` }}
                      ></div>
                    </div>
                    <span className="value">{constraint.tightness.toFixed(2)}</span>
                  </div>
                )}
              </div>
            ))}
            {categoryConstraints.length > 5 && (
              <div className="more-constraints">
                + {categoryConstraints.length - 5} more constraints
              </div>
            )}
          </div>
        </div>
      ))}

      <div className="constraint-philosophy">
        <h4>Physics-Informed Constraint Synthesis</h4>
        <p>
          Constraints are <strong>synthesized dynamically</strong> based on fundamental physical laws:
        </p>
        <ul>
          <li><strong>Conservation:</strong> Total allocated resources ≤ available resources</li>
          <li><strong>Dynamics:</strong> Queue stability requires service rate &gt; arrival rate</li>
          <li><strong>Causality:</strong> Events must respect temporal ordering</li>
          <li><strong>Adaptation:</strong> Constraint tightness adjusts based on prediction accuracy</li>
        </ul>
      </div>
    </div>
  );
}

export default ConstraintPanel;
