import { useEffect, useRef } from 'react';
import katex from 'katex';
import 'katex/dist/katex.min.css';
import './ProblemFormulation.css';

function ProblemFormulation({ problem }) {
  const variablesRef = useRef();
  const objectivesRef = useRef();

  useEffect(() => {
    if (variablesRef.current) {
      renderVariables();
    }
    if (objectivesRef.current) {
      renderObjectives();
    }
  }, [problem]);

  const renderVariables = () => {
    if (!problem || !problem.variables) {
      variablesRef.current.innerHTML = '<em>No problem formulated yet</em>';
      return;
    }

    const variableNames = Object.keys(problem.variables);
    if (variableNames.length === 0) {
      variablesRef.current.innerHTML = '<em>No variables</em>';
      return;
    }

    try {
      const latex = String.raw`
        \begin{aligned}
        \text{Variables:} \\
        ${variableNames.slice(0, 4).map(v => {
          const variable = problem.variables[v];
          return String.raw`${v} &\in [${variable.bounds[0]}, ${variable.bounds[1]}] \quad \text{(${variable.type})}`;
        }).join(' \\\\ ')}
        ${variableNames.length > 4 ? String.raw`\\ \vdots &\quad \text{(${variableNames.length - 4} more)}` : ''}
        \end{aligned}
      `;

      variablesRef.current.innerHTML = katex.renderToString(latex, {
        displayMode: true,
        throwOnError: false
      });
    } catch (e) {
      variablesRef.current.innerHTML = `<pre>${JSON.stringify(problem.variables, null, 2)}</pre>`;
    }
  };

  const renderObjectives = () => {
    if (!problem || !problem.objectives || problem.objectives.length === 0) {
      objectivesRef.current.innerHTML = '<em>No objectives defined</em>';
      return;
    }

    try {
      const latexParts = problem.objectives.map((obj, i) => {
        return String.raw`\text{${obj.name}:} \quad ${obj.formula}`;
      });

      const latex = String.raw`
        \begin{aligned}
        \text{Multi-Objective:} \\
        ${latexParts.join(' \\\\ ')}
        \end{aligned}
      `;

      objectivesRef.current.innerHTML = katex.renderToString(latex, {
        displayMode: true,
        throwOnError: false
      });
    } catch (e) {
      objectivesRef.current.innerHTML = `<pre>${JSON.stringify(problem.objectives, null, 2)}</pre>`;
    }
  };

  if (!problem) {
    return (
      <div className="problem-formulation">
        <div className="empty-state">
          <p>No optimization problem formulated yet. Start the simulation to see the dynamic problem formulation.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="problem-formulation">
      <div className="formulation-section">
        <h3>Decision Variables</h3>
        <div ref={variablesRef} className="math-content"></div>
        <div className="variable-explanations">
          {problem.variables && Object.entries(problem.variables).slice(0, 3).map(([name, variable]) => (
            <div key={name} className="variable-explanation">
              <code>{name}</code>: {variable.description}
            </div>
          ))}
        </div>
      </div>

      <div className="formulation-section">
        <h3>Objectives</h3>
        <div ref={objectivesRef} className="math-content"></div>
        <div className="objective-explanations">
          {problem.objectives && problem.objectives.map((obj, i) => (
            <div key={i} className="objective-explanation">
              <div className="objective-name">{obj.name}</div>
              <div className="objective-description">{obj.description}</div>
              <div className="objective-weight">Weight: {obj.weight.toFixed(2)}</div>
            </div>
          ))}
        </div>
      </div>

      <div className="formulation-section">
        <h3>Problem Type</h3>
        <div className="problem-type">
          <span className="type-badge">{problem.type || 'Multi-Objective Optimization'}</span>
        </div>
      </div>

      <div className="formulation-philosophy">
        <h4>Formulation Philosophy</h4>
        <p>
          The optimization problem is <strong>constructed dynamically</strong> from current system state
          rather than being fixed. Decision variables, objectives, and constraints are synthesized
          based on active channels, queue states, and resource availability.
        </p>
      </div>
    </div>
  );
}

export default ProblemFormulation;
