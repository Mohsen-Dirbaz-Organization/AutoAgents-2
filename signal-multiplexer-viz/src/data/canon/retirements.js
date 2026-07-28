/**
 * canon/retirements.js — the retirement register.
 * (EPU Companion release rule: "Retired constructs are removed rather than
 * assigned a provisional standing that implies a path to evidence." And the
 * maintenance rule: "Negative results, rejected mappings and counterexamples
 * are preserved. They are what prevent the same overclaim from being made
 * twice.")
 */

export const RETIREMENTS = [
  {
    retirement_id: 'ret.stress_tensor',
    construct: 'Constraint stress tensor / "constraint satisfaction as stress fields" (Source #3 Part V, derivative T5)',
    reason: 'Undefined: the Cauchy stress tensor is DERIVED from mass density, traction, momentum and subbody balance — none of which exist on a state manifold. What the idea actually uses is the active-constraint Jacobian, i.e. a polytope, which MPC already provides. The trace needs a metric never given; the determinant is not a polytope volume; the "yield surface" is generically empty.',
    disposition: 'REMOVED from the backlog (was "not yet built" in the README). Do not rebuild a stress-field panel.',
    source: 'EPU Companion, Deck B retirement #2',
    appliedInRepo: 'README T5 row struck through and marked RETIRED; no stress-field code exists.'
  },
  {
    retirement_id: 'ret.landauer',
    construct: 'Landauer-cost accounting for memory/erasure decisions',
    reason: 'Inert: the irreversibility premise is not expressible in the deck\'s own formalism (translation operators are invertible; irreversibility lives in state-map non-injectivity, never written down); a dissipative ODE produces no Landauer cost without a physical embedding; and kT·ln2 ≈ 2.87e-21 J/bit is 6–9 orders below CMOS switching — every derived recommendation optimizes a negligible term.',
    disposition: 'RETIRED-INBOUND: never adopted in this repo; recorded so it cannot enter via a future source. Memory erasure here is justified by correlation decay + τ-hierarchy (Part II), not thermodynamics.',
    source: 'EPU Companion, Deck B retirement #1',
    appliedInRepo: 'No Landauer reasoning exists in engines or docs (verified by search).'
  },
  {
    retirement_id: 'ret.bianchi_check',
    construct: '"Bianchi identity" constraint-consistency check',
    reason: 'A guaranteed-pass no-op occupying the terminal governance position: the (misnamed) identity is automatically true for any affine connection, so it has no truth value to lose. Consistency is satisfiability (feasible set non-empty) — a global combinatorial property, not a local differential one.',
    disposition: 'RETIRED-INBOUND, and generalized: the noOpAudit() tautology detector screens EVERY registered check for the guaranteed-pass pattern.',
    source: 'EPU Companion, Deck B retirement #3',
    appliedInRepo: 'Same pattern found and fixed in-repo: the §3.4 (i)/(iii) "live per-tick verification" was guaranteed-pass; replaced by the falsifiable masking probe (see clm.masking_blocked).'
  },
  {
    retirement_id: 'ret.live_drift_check',
    construct: '§3.4 clauses (i)/(iii) asserted as live per-tick VERIFICATION',
    reason: 'conservedDrift compares unit(shape·g) to shape — identical for any g>0; residual is |Q| after a projection that removes the weighted mean by algebraic identity. Neither can fail; both were advertised as "re-confirmed live every tick".',
    disposition: 'REMOVED as verification; retained as rounding-fault DIAGNOSTICS with standing constructed. The live check is now the adversarial masking probe, which can fail.',
    source: 'This repo, applying EPU Companion Deck B retirement #3',
    appliedInRepo: 'ConservationRenormalizationLayer.step() standing fields; BoundedAutonomyStack crlState.diagnostics/probe; panel + README rewritten.'
  }
];
