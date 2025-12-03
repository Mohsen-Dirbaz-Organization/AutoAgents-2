import { useState } from 'react';
import './ConceptExplainer.css';

const CONCEPTS = [
  {
    id: 'dynamic-formulation',
    title: '🎯 Dynamic Problem Formulation',
    subtitle: 'The Core Innovation',
    traditional: {
      label: 'Traditional Approach',
      description: 'Uses fixed rules like "give priority signals 80% bandwidth" or "process in strict order"',
      icon: '📋',
      limitations: [
        'Cannot adapt to changing conditions',
        'Wastes resources when priorities change',
        'Requires manual tuning for each scenario'
      ]
    },
    innovative: {
      label: 'Our Dynamic Approach',
      description: 'Every 100ms, constructs a brand new optimization problem based on current queues, arrivals, and constraints',
      icon: '🔄',
      benefits: [
        'Automatically adapts to current conditions',
        'Optimizes resource allocation in real-time',
        'No manual tuning needed - self-configuring'
      ]
    },
    analogy: 'Think of it like GPS navigation: traditional = following printed directions, dynamic = recalculating route every second based on current traffic'
  },
  {
    id: 'structure-detection',
    title: '🔍 Intelligent Structure Detection',
    subtitle: 'Choosing the Right Tool',
    traditional: {
      label: 'Traditional Approach',
      description: 'Uses one algorithm for everything, like a hammer for every problem',
      icon: '🔨',
      limitations: [
        'One-size-fits-all is often inefficient',
        'Cannot handle complex scenarios',
        'Slow when problem structure changes'
      ]
    },
    innovative: {
      label: 'Our Detection System',
      description: 'Automatically analyzes each problem and selects the best solver: Convex → fast optimization, Mixed-Integer → exact solutions, Stochastic → handles uncertainty',
      icon: '🎯',
      benefits: [
        'Uses the fastest algorithm for each situation',
        'Handles complex scenarios automatically',
        'Adapts solver choice in real-time'
      ]
    },
    analogy: 'Like a master chef selecting the right knife: paring knife for vegetables, cleaver for meat, serrated for bread - using the optimal tool for each task'
  },
  {
    id: 'physics-constraints',
    title: '⚛️ Physics-Informed Constraints',
    subtitle: 'Respecting Reality',
    traditional: {
      label: 'Traditional Approach',
      description: 'Simple limits like "don\'t exceed 100% bandwidth" with no understanding of system dynamics',
      icon: '🚫',
      limitations: [
        'Can violate physical laws',
        'Ignores causality (future affecting past)',
        'Unstable under rapid changes'
      ]
    },
    innovative: {
      label: 'Our Physics-Based System',
      description: 'Enforces conservation laws (total bandwidth), dynamics (queue evolution), and causality (time ordering)',
      icon: '⚡',
      benefits: [
        'Mathematically guaranteed stability',
        'Respects physical limitations',
        'Prevents impossible solutions'
      ]
    },
    analogy: 'Like structural engineering: you can\'t just stack bricks randomly - you must respect gravity, load distribution, and material properties'
  },
  {
    id: 'dual-purpose',
    title: '🤝 Dual-Purpose Agent Deployment',
    subtitle: 'Specialized + Shared Processing',
    traditional: {
      label: 'Traditional Approach',
      description: 'Each signal type gets dedicated agents, leading to idle agents when their signal type is quiet',
      icon: '👤',
      limitations: [
        'Wasted capacity during low activity',
        'Cannot help overloaded channels',
        'Fixed allocation regardless of need'
      ]
    },
    innovative: {
      label: 'Our Dual-Purpose System',
      description: 'Agents handle BOTH their specialized signals AND contribute to public utility pool when available',
      icon: '👥',
      benefits: [
        'Zero idle time - always productive',
        'Automatic load balancing',
        'Adapts to demand in real-time'
      ]
    },
    analogy: 'Like hospital doctors: they have specialties (cardiology, surgery) but also help in general ER when their specialty is quiet'
  },
  {
    id: 'adaptive-learning',
    title: '🧠 Performance-Based Adaptation',
    subtitle: 'Learning from Experience',
    traditional: {
      label: 'Traditional Approach',
      description: 'Fixed parameters set by engineers, requiring manual updates when conditions change',
      icon: '⚙️',
      limitations: [
        'Cannot learn from mistakes',
        'Requires expert tuning',
        'Degrades when conditions shift'
      ]
    },
    innovative: {
      label: 'Our Learning System',
      description: 'Tracks prediction errors and automatically adjusts constraint tightness: high error → tighter control, low error → more efficiency',
      icon: '📈',
      benefits: [
        'Self-improving over time',
        'Adapts to changing workloads',
        'No manual intervention needed'
      ]
    },
    analogy: 'Like cruise control that learns your driving style: it adjusts acceleration based on whether you prefer comfort or speed'
  },
  {
    id: 'dirac-delta',
    title: '⚡ Impulsive Control Theory',
    subtitle: 'Instantaneous Reallocation',
    traditional: {
      label: 'Traditional Approach',
      description: 'Gradual adjustments over many cycles, slow to respond to sudden changes',
      icon: '🐌',
      limitations: [
        'Slow response to emergencies',
        'Wastes time during transitions',
        'Cannot handle burst events'
      ]
    },
    innovative: {
      label: 'Our Impulsive Control',
      description: 'Uses Dirac delta functions to model instantaneous state changes - agents can be reassigned in zero time when critical signals arrive',
      icon: '💥',
      benefits: [
        'Instant response to critical events',
        'Mathematical framework for discrete jumps',
        'Optimal for burst traffic scenarios'
      ]
    },
    analogy: 'Like emergency vehicle preemption at traffic lights: instant green for ambulances rather than waiting for the normal cycle'
  }
];

function ConceptExplainer({ currentStep }) {
  const [selectedConcept, setSelectedConcept] = useState(CONCEPTS[0]);
  const [showComparison, setShowComparison] = useState(true);

  // Auto-select concept based on current optimization step
  const getRelevantConcept = (step) => {
    const stepToConceptMap = {
      'formulate': 'dynamic-formulation',
      'detect': 'structure-detection',
      'synthesize': 'physics-constraints',
      'solve': 'structure-detection',
      'adapt': 'adaptive-learning'
    };
    return CONCEPTS.find(c => c.id === stepToConceptMap[step]) || CONCEPTS[0];
  };

  const displayConcept = currentStep ? getRelevantConcept(currentStep) : selectedConcept;

  return (
    <div className="concept-explainer">
      <div className="explainer-header">
        <h3>Understanding the Innovation</h3>
        <p className="explainer-subtitle">What makes this approach unique</p>
      </div>

      <div className="concept-tabs">
        {CONCEPTS.map(concept => (
          <button
            key={concept.id}
            className={`concept-tab ${selectedConcept.id === concept.id ? 'active' : ''}`}
            onClick={() => setSelectedConcept(concept)}
            title={concept.subtitle}
          >
            {concept.title.split(' ')[0]}
          </button>
        ))}
      </div>

      <div className="concept-content">
        <div className="concept-header">
          <h4>{displayConcept.title}</h4>
          <p className="concept-subtitle">{displayConcept.subtitle}</p>
        </div>

        <div className="comparison-toggle">
          <label>
            <input
              type="checkbox"
              checked={showComparison}
              onChange={(e) => setShowComparison(e.target.checked)}
            />
            Show comparison with traditional approach
          </label>
        </div>

        {showComparison ? (
          <div className="comparison-view">
            <div className="approach-card traditional">
              <div className="approach-header">
                <span className="approach-icon">{displayConcept.traditional.icon}</span>
                <h5>{displayConcept.traditional.label}</h5>
              </div>
              <p className="approach-description">{displayConcept.traditional.description}</p>
              <div className="limitations">
                <strong>Limitations:</strong>
                <ul>
                  {displayConcept.traditional.limitations.map((limit, idx) => (
                    <li key={idx}>{limit}</li>
                  ))}
                </ul>
              </div>
            </div>

            <div className="comparison-arrow">
              <span>vs</span>
              <div className="arrow-line"></div>
            </div>

            <div className="approach-card innovative">
              <div className="approach-header">
                <span className="approach-icon">{displayConcept.innovative.icon}</span>
                <h5>{displayConcept.innovative.label}</h5>
              </div>
              <p className="approach-description">{displayConcept.innovative.description}</p>
              <div className="benefits">
                <strong>Benefits:</strong>
                <ul>
                  {displayConcept.innovative.benefits.map((benefit, idx) => (
                    <li key={idx}>{benefit}</li>
                  ))}
                </ul>
              </div>
            </div>
          </div>
        ) : (
          <div className="single-view">
            <div className="approach-card innovative standalone">
              <div className="approach-header">
                <span className="approach-icon">{displayConcept.innovative.icon}</span>
                <h5>{displayConcept.innovative.label}</h5>
              </div>
              <p className="approach-description">{displayConcept.innovative.description}</p>
              <div className="benefits">
                <strong>Key Benefits:</strong>
                <ul>
                  {displayConcept.innovative.benefits.map((benefit, idx) => (
                    <li key={idx}>{benefit}</li>
                  ))}
                </ul>
              </div>
            </div>
          </div>
        )}

        <div className="analogy-box">
          <div className="analogy-icon">💡</div>
          <div className="analogy-content">
            <strong>Real-world Analogy:</strong>
            <p>{displayConcept.analogy}</p>
          </div>
        </div>
      </div>

      <div className="explainer-footer">
        <p className="technical-note">
          <strong>Technical Note:</strong> This system reformulates its optimization problem
          every 100ms, detecting structure and selecting solvers in real-time while respecting
          physics-informed constraints and learning from prediction errors.
        </p>
      </div>
    </div>
  );
}

export default ConceptExplainer;
