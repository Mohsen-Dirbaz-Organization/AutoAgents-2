/**
 * canon/gates.js — typed gate contracts for the runtime gates the simulation
 * implements. (Definition of Done item 2: typed interface, invariant,
 * threshold with hazard-derived provenance, failure semantics, reason code,
 * test vector.)
 *
 * `testVector` gives the validator a concrete input meant to VIOLATE the
 * invariant plus the expected gate response — this is what makes the no-op
 * audit possible for gates (a gate that accepts its own violation vector is a
 * guaranteed-pass and fails release).
 */

export const GATES = [
  {
    gate_id: 'gate.monotonicity',
    name: 'Antitone monotonicity check',
    interface: { inputs: 'prevR:number, R:number, prevAdmitted:int, admitted:int', output: 'violation:boolean' },
    invariant: 'R > prevR ⇒ admitted ≤ prevAdmitted',
    threshold: { value: 'admitted > prevAdmitted while risk rose', provenance: 'hazard hz.unauthorized_expansion — an expanding envelope under rising risk is the top-level hazard the whole stack exists to prevent' },
    failureSemantics: 'Count + emit monotonicity_violation event; discipline ON prevents by construction, OFF surfaces device non-ideality.',
    reasonCode: 'MONO-EXPAND',
    testVector: { input: { prevR: 0.3, R: 0.5, prevAdmitted: 3, admitted: 4 }, expect: 'violation = true' },
    artifact: 'src/simulation/BoundedAutonomyStack.js#_checkMonotonicity',
    level: 'G' // raw count comparison, no basis or registry cited
  },
  {
    gate_id: 'gate.analog_veto',
    name: 'Analog veto (S4)',
    interface: { inputs: 'dR:number, R:number', output: 'armed:boolean' },
    invariant: 'A sharp risk spike arms the electrically-isolated guard before any classifier resolves the scene.',
    threshold: { value: 'dR > 0.18 or R > 0.85', provenance: 'hazard hz.late_refusal — the veto must precede classification; the ~32 ns witness (num.veto_latency_ns) is the cited timing basis' },
    failureSemantics: 'Fail-closed: veto asserts toward the minimum-risk condition (STOP always admissible).',
    reasonCode: 'VETO-SPIKE',
    testVector: { input: { dR: 0.25, R: 0.5 }, expect: 'armed = true' },
    artifact: 'src/simulation/BoundedAutonomyStack.js#_maybeAnalogVeto',
    level: 'G' // dR/R thresholds are chosen constants, no basis cited
  },
  {
    gate_id: 'gate.conservation',
    name: 'Conservation residual check (CRL)',
    interface: { inputs: 'residual:number', output: 'conserved:boolean' },
    invariant: '|Q| after projection ≤ tol_conserve (derived from the arithmetic format)',
    threshold: { value: 'tolConserve(M) ≈ 6e-14 for M=5', provenance: 'derived from IEEE-754 binary64 EPSILON per companion P1 — NOT a chosen constant' },
    failureSemantics: 'A breach is a rounding/implementation FAULT (standing constructed); freeze the reservoir, do not continue.',
    reasonCode: 'CONS-RESIDUAL',
    testVector: { input: { residual: 1e-6 }, expect: 'conserved = false' },
    artifact: 'src/simulation/ConservationRenormalizationLayer.js#step + tolConserve',
    level: 'R',
    basis: 'tolConserve(M) — IEEE-754 binary64 Number.EPSILON, multiplet size, and the maximum gain bound (a stated, reproducible formula, not a chosen constant)'
  },
  {
    gate_id: 'gate.masking_probe',
    name: 'Adversarial masking probe (§3.4(ii), live)',
    interface: { inputs: 'a:vec2, b:vec2, maskGain:number, epsilon:number', output: 'pass:boolean (genuine ∧ detected)' },
    invariant: 'A genuine graded defect remains detected in the gauge-fixed shape band under an adversarial compensating gain.',
    threshold: { value: 'ε = 0.05 defect band', provenance: 'hazard hz.masked_defect — a masked sensor defect admits unwarranted fusion authority' },
    failureSemantics: 'masked=true is a live safety-check failure surfaced in the CRL panel (⚠ MASKED (violation!)).',
    reasonCode: 'MASK-EVADE',
    testVector: { input: { readShape: false }, expect: 'pass = false (raw band is fooled — the check CAN fail)' },
    artifact: 'src/simulation/ConservationRenormalizationLayer.js#maskingProbe',
    level: 'G',
    apparatusNote: 'ε = 0.05 is chosen, not derived from a stated basis or registry — an honest MLP "retro-fitted gate" candidate (a gate whose apparatus is not yet frozen). The FALSIFIABILITY of the check (established-in-sim) is not in question; its threshold\'s level is.'
  },
  {
    gate_id: 'gate.llc_quarantine',
    name: 'LLC drift quarantine',
    interface: { inputs: 'llc:number, prevLlc:number', output: 'quarantine:boolean' },
    invariant: 'A consolidation coinciding with an LLC jump is not committed.',
    threshold: { value: 'Δllc > 0.25 per tick', provenance: 'hazard hz.silent_drift — developmental phase transitions must not silently enter strategic memory' },
    failureSemantics: 'Drop the freshest digitized entry from the strategic tier; log llc_quarantine (challengeable).',
    reasonCode: 'LLC-JUMP',
    testVector: { input: { prevLlc: 1.0, llc: 1.3 }, expect: 'quarantine = true' },
    artifact: 'src/simulation/BoundedAutonomyStack.js#_governDrift',
    level: 'G',
    apparatusNote: 'Δllc > 0.25 is chosen, not derived — the same honest apparatus gap as gate.masking_probe\'s ε.'
  },
  {
    gate_id: 'gate.requantize',
    name: 'τ-boundary re-quantization (Lane C)',
    interface: { inputs: 'entry{residence,deltaJ,cost}', output: 'promoted:boolean (digitized, weight rounded UP)' },
    invariant: 'No analog state crosses τ = 5 s; crossing requires cross-modality confirmation and safe-side rounding.',
    threshold: { value: 'residence ≥ 5 s AND ΔJ/C ≥ 1.0', provenance: 'ratified Numerical Substrate Partition ADR (num.tau_boundary_s) + hazard hz.analog_decay — decayed analog state must not masquerade as durable truth' },
    failureSemantics: 'Entry ages out instead of promoting (erasure by eps_correlation / alpha_tau bounds; lineage in evictions counter).',
    reasonCode: 'TAU-CROSS',
    testVector: { input: { residence: 6, deltaJ: 0.4, cost: 0.8 }, expect: 'promoted = false (ΔJ/C = 0.5 < 1.0)' },
    artifact: 'src/simulation/BoundedAutonomyStack.js#_ageMemory/_promote',
    level: 'C',
    registry: 'Numerical Substrate Partition ADR (num.tau_boundary_s, canon/numbers.js) — a ratified governance decision, cited as this gate\'s classifying apparatus for the τ = 5 s boundary'
  },
  {
    gate_id: 'gate.challenge',
    name: 'Constitutional challenge adjudication',
    interface: { inputs: 'claimId, strength:number', output: 'transition | resistance-strengthened' },
    invariant: 'Correction supremacy: counter-evidence stronger than challengeResistance demotes; weaker strengthens resistance. Either way a constitutional event is archived (anti-silent-drift).',
    threshold: { value: 'strength > warrant.challengeResistance', provenance: 'hazard hz.silent_drift + Art. XVIII/XIX of the Constitution source' },
    failureSemantics: 'No path mutates canon without an Archive event; collapse (evidence < 0.25) retracts.',
    reasonCode: 'CHAL-ADJ',
    testVector: { input: { strength: 0.9, resistance: 0.5 }, expect: 'demotion + archived event' },
    artifact: 'src/simulation/ConstitutionalTruthEngine.js#challenge/_transition',
    level: 'C',
    registry: 'RELIANCE ladder (ConstitutionalTruthEngine.js) + per-claim warrant.challengeResistance — the gate classifies against this registry, never against a bare threshold'
  }
];
