/**
 * canon/level.js — Multi-Level Policy (MLP v1.0): Level as the Locality
 * Operator.
 *
 * A sibling formalism to the Rigorous Planning Management Framework already
 * in this canon (planning.js reuses its Π = W1/W∞; its Cor. 2.4 "conventions
 * frozen before decomposition" is generalized here as "apparatus precedes
 * architecture, like informational independence, it cannot be scheduled into
 * existence"). MLP's central move: LEVEL (an ordinal derivation axis
 * G ≺ R ≺ C) is orthogonal to LOCUS (where a value physically lives) —
 * `level ⫫ locus`. A value's level is fixed by what apparatus it cites, never
 * by where it is stored or who computed it.
 *
 * The sharpest generalization for THIS project: MLP-7 "demotion, not
 * mutation" — E′ ⪯ E ⟹ Auth(x;E′) ⊆ Auth(x;E) (weaker evidence ⟹ narrower
 * authority, never wider) — is exactly `clm.antitone` (canon/claims.js:
 * "as warrant falls, admissible action set only contracts"), restated as a
 * general law over evidence strength rather than one specific risk scalar.
 * LevelEngine.js's checkDemotionNotMutation() tests it directly against
 * ConstitutionalTruthEngine, the one place in this repo where evidence
 * genuinely strengthens and weakens at runtime.
 */

export const LEVEL = {
  G: { label: 'Ground', color: '#8a8880', apparatus: 'none', form: 'v_G — as measured or asserted', meaning: 'native units, native vocabulary; comparable inside one frame only; the default, since a value citing nothing IS ground' },
  R: { label: 'Referenced', color: '#2a78d6', apparatus: 'a basis B', form: 'v_R = f(v_G, B)', meaning: 'B declares a reference set, a transformation f, and a resolution rule; comparable IFF bases are stated and compatible; non-unique, so the choice of basis is itself an operation record' },
  C: { label: 'Classified', color: '#4a3aa7', apparatus: 'a registry@version', form: 'v_C = g(v_R | v_G, registry@ver) with P(v_C)', meaning: 'the registry declares its domain, its space (the referenced coordinates classes are drawn over), membership criteria, an explicit `unclassified`, and a version' }
};
export const LEVEL_ORDER = ['G', 'R', 'C']; // ordinal: derivation DEPTH, not physical scale
export const LEVEL_RANK = { G: 0, R: 1, C: 2 };

/**
 * Apparatus registry — concrete G/R/C examples grounded in THIS project's own
 * canon, not the source document's external roadmap vocabulary. Each entry
 * names the actual field/file that plays the role of value, basis, or
 * registry.
 */
export const APPARATUS_EXAMPLES = [
  {
    quantity: 'num.monotonicity_violation_rate (canon/numbers.js)',
    level: 'G',
    detail: 'A raw simulated count-ratio. No basis cited — it is read directly off MonotonicityMonitor counters. Comparable only within this one simulation run.'
  },
  {
    quantity: 'Π = W1/W∞ (PlanningEngine.js bounds field)',
    level: 'R',
    basis: 'the effort model in canon/planning.js (TASKS[].effortPd, grade modelled) + the causal-arc graph (CAUSAL_ARCS)',
    detail: 'A ratio derived via a stated transformation from a declared basis. Comparable to another Π only if the SAME effort basis and arc set is stated alongside it — never bare.'
  },
  {
    quantity: 'claim.standing (canon/standing.js STANDING registry)',
    level: 'C',
    registry: 'STANDING @ this repo\'s canon (established | constructed | proposed | target | unsupported)',
    detail: 'Every claim in canon/claims.js is classified against this versioned, explicit-unclassified-bearing registry (there is no silent default — a claim without a stated standing is a validator finding, C1).'
  },
  {
    quantity: 'claim.reliance (ConstitutionalTruthEngine RELIANCE ladder)',
    level: 'C',
    registry: 'RELIANCE @ ConstitutionalTruthEngine.js (hypothesis | provisional | warranted | retracted)',
    detail: 'A second, independent C-level registry over the SAME underlying evidence — proof that two registries can classify one ground value without either owning it (MLP-2: apparatus is contract, not container).'
  },
  {
    quantity: 'gate.conservation threshold (tolConserve(M))',
    level: 'R',
    basis: 'IEEE-754 binary64 Number.EPSILON, multiplet size M, and the maximum gain bound — a stated, reproducible formula (ConservationRenormalizationLayer.tolConserve)',
    detail: 'Replaced a chosen constant (1e-9) specifically because a G-level chosen tolerance has no apparatus — this was the Phase-0 fix that first applied "derive the tolerance from the arithmetic format" (EPU Companion P1), before this document supplied the general vocabulary for why that fix was a level promotion.'
  }
];

/**
 * MP families — reproduced verbatim from the source document as REFERENCE
 * DATA (standing: external-reference). This repo does NOT restructure its
 * own views/modules into this exact mini-plan taxonomy — that would be a
 * false correspondence (the source's MP-01..MP-12 describe an external
 * roadmap-management programme this repo is not part of). It is retained so
 * the level-floor / apparatus / architecture pattern is citable, and because
 * MP-05 (dependency) and MP-12 (roadmap) visibly share vocabulary (Π,
 * horizon-grade) with modules already built here.
 */
export const MP_FAMILIES = [
  { id: 'MP-01', name: 'authority', floor: ['C'], apparatus: 'authority-order registry · errata profile BEP-01 · governance charter', architecture: 'which source governs a contested term — and therefore every downstream invariant' },
  { id: 'MP-02', name: 'normalization', floor: ['C'], apparatus: 'convention digest (identifiers, units, clocks, aliases) · entity register at a version', architecture: 'identity granularity — which fixes the nodes of the dependency graph' },
  { id: 'MP-03', name: 'manifest', floor: ['C'], apparatus: 'axis registry at a version · applicability grammar · prohibitions', architecture: 'which axes may multiply — the size and shape of the candidate surface C0' },
  { id: 'MP-04', name: 'assurance', floor: ['C', 'R'], apparatus: 'guarantee-template registry at a version · a declared adversary and threat basis', architecture: 'the route bundle — which is the evidence architecture, not a report about it' },
  { id: 'MP-05', name: 'dependency', floor: ['R'], apparatus: 'an effort basis: units, resolution, and the budget under which effort is stated', architecture: 'a low Π = W1/W∞ returns the plan to structural repair — i.e. re-partition' },
  { id: 'MP-06', name: 'portfolio', floor: ['R', 'C'], apparatus: 'equal comparison budget as a declared basis (BEP-04) · certainty-stratum and risk registries', architecture: 'the dominance filter and the selected portfolio — and the standing prohibition on concatenating local frontiers' },
  { id: 'MP-07', name: 'formal / PVS', floor: ['C'], apparatus: 'the E0–E5 ladder · exact tool revision — a version pin', architecture: 'E4 is a seam by construction ⇒ a refinement or conformance architecture, not a stronger theorem' },
  { id: 'MP-08', name: 'empirical', floor: ['R'], apparatus: 'frozen protocol · oracle and coverage design · held-out design; unmodelled difference carried in U', architecture: 'the validity interval — which IS the ODD and configuration boundary' },
  { id: 'MP-09', name: 'concept', floor: ['C', 'R'], apparatus: 'platform-option registry at a version, whose space is drawn over measured workload-stability and value coordinates', architecture: 'ASIC | FPGA | dataflow | hybrid — unclassifiable until its R-coordinates exist' },
  { id: 'MP-10', name: 'seam', floor: ['C'], apparatus: 'seam register · per-boundary certificate, or an explicit exclusion — one per C-boundary', architecture: 'every non-composing boundary becomes an owned plan — the integration architecture is the list of refused compositions' },
  { id: 'MP-11', name: 'release', floor: ['C', 'G'], apparatus: 'hazard-derived thresholds fixed ex ante · exact-scope, exact-configuration evidence', architecture: 'the release slice — and with it the fallback, rollback and monitoring architecture' },
  { id: 'MP-12', name: 'roadmap', floor: ['C'], apparatus: 'horizon-grade registry {bound·envelope·forecast·open} · option-state registry', architecture: 'the level ceiling as a function of distance from the decision horizon — hence the commitment architecture' }
];

export const AXIOMS = [
  { id: 'MLP-1', text: 'Level is derivation, not location — G ≺ R ≺ C is depth, not scale.' },
  { id: 'MLP-2', text: 'Apparatus is contract, not container — bases and registries hold criteria, never values.' },
  { id: 'MLP-3', text: 'Evidence fixes the level (W2); horizon caps it (CI-9); a record closes inside the band.' },
  { id: 'MLP-4', text: 'spec ⊖ licence = evidence obligation, and its route IS the architectural choice.' },
  { id: 'MLP-5', text: 'Multiplication licence — an axis is independent iff its registry space is disjoint.' },
  { id: 'MLP-6', text: 'Composition licence — G in-frame, R iff bases compatible, C never; seams record the refusal.' },
  { id: 'MLP-7', text: 'Demotion, not mutation — supersede the citation; the ground value is immutable.' },
  { id: 'MLP-8', text: '`open`/`unclassified` is a class — unbound values are written, never invented (W4 · C2 · CI-4).' },
  { id: 'L1', text: 'Unilocality — one locus ∀t; a level raise is never a move.' },
  { id: 'L2', text: 'Transport, not duplication — cite, not copy.' },
  { id: 'L3', text: 'class ⊥ place — extended by MLP to level ⫫ locus.' }
];

export const THESIS = {
  quote: 'Cite to globalize · derive, never relocate · one locus, k citations.',
  rule: 'Because Level is fixed by cited evidence and never by the coder, no level raise moves a value: globality has no locus.',
  origin: 'Multi-Level Policy v1.0 — a proposed policy layer over an external planning-conformance specification and roadmap bridge, amending neither.'
};
