/**
 * canon/symbols.js — symbol collision register.
 * (EPU Companion, Glossary 1/3.) "No symbol, term or gate number inherits
 * authority by resemblance. Where one glyph denotes objects of different
 * mathematical type in different sections, the glyph is not a construct — it is
 * a collision, and every claim that crosses the collision is unsupported until
 * re-derived under disambiguated names."
 */

export const SYMBOLS = [
  {
    glyph: 'σᵢⱼ',
    denotations: [
      'Constraint stress tensor (Deck B — RETIRED: undefined on a state manifold)',
      'Hallucination-severity score',
      'Lyapunov function',
      'Automotive state/input constraint sets X, U'
    ],
    conflict: 'Rank-2 field vs scalar score vs scalar function vs admissible set — four types',
    canonical: ['J_active (active-constraint Jacobian)', 'h_score', 'V_lyap', 'X_state / U_input'],
    usedInRepo: 'Not used — the stress-tensor reading (T5) is retired; see retirements.js'
  },
  {
    glyph: 'S',
    denotations: [
      'Pole/zero count parity (revised paper §3)',
      'Safety-case scalar (EPU)',
      'Scatter matrices S_B, S_W',
      'Thermodynamic entropy'
    ],
    conflict: 'Integer parity vs safety construct vs matrix vs state function',
    canonical: ['S_parity', 'S_safety', 'S_between / S_within', 's_entropy'],
    usedInRepo: 'BoundedAutonomyStack.scalars.S = S_parity (±1 structural parity). S_safety is NOT computed anywhere in this repo.'
  },
  {
    glyph: 'ξ',
    denotations: [
      'Log-ratio feature from the gasification pipeline',
      'Loosely used as a risk indicator in transfer discussion'
    ],
    conflict: 'Unitless feature vs asserted one-sided risk quantity',
    canonical: ['xi_feature', 'phi_risk (only after one-sided calibration — which does not exist)'],
    usedInRepo: 'BoundedAutonomyStack.scalars.xi = xi_feature. In the sim it is DRIVEN from risk.R (a modelling choice); the claim "ξ tracks physical risk" is proposed, not established.'
  },
  {
    glyph: 'ε',
    denotations: [
      'Gate-2 closure tolerance 2⁻³⁰',
      'ε_action control axis',
      'DP privacy budget',
      'Conservation residual tolerance (never stated in the deck)'
    ],
    conflict: 'Numeric tolerance vs control parameter vs privacy parameter vs undeclared residual bound',
    canonical: ['tol_closure', 'eps_action', 'eps_dp', 'tol_conserve (MUST be derived from the arithmetic format)'],
    usedInRepo: 'eps_correlation (memory/claim aging, 0.05, §9.1.1) in BoundedAutonomyStack._ageMemory + ConstitutionalTruthEngine._ageClaims; tol_conserve derived in ConservationRenormalizationLayer.tolConserve() from Number.EPSILON.'
  },
  {
    glyph: 'ρ',
    denotations: [
      'Deterministic rigidity feature (revised paper §3.7)',
      'Mass density in conservation PDEs',
      'Spectral radius in stability discussion'
    ],
    conflict: 'Unitless feature vs physical density vs operator norm',
    canonical: ['rho_rigidity', 'rho_mass', 'spec_radius'],
    usedInRepo: 'Not used as a state symbol in this repo.'
  },
  {
    glyph: 'φ',
    denotations: [
      'Golden ratio (EPU)',
      'Risk scalar in transfer discussion',
      'Potential field in variational sections'
    ],
    conflict: 'Mathematical constant vs calibrated risk output vs field',
    canonical: ['phi_golden (constant, never a variable)', 'phi_risk', 'Phi_potential'],
    usedInRepo: 'The "φ-compiler" naming refers to the conservation-manifold compiler producing xi_feature/S_parity; no phi_risk exists (no one-sided calibration).'
  }
];

export const SYMBOL_RULE =
  'A released document may use a colliding glyph only with its canonical name beside it. ' +
  'Every claim that crosses an unresolved collision is unsupported until re-derived under ' +
  'disambiguated names.';
