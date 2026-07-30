/**
 * canon/planning.js — the concrete planning instance P = (T, A, S, φ-scope,
 * effort, phase, R, W, Ec) that instantiates the Rigorous Planning Management
 * Framework (Part I, Def 1.1) over this project's OPEN obligations.
 *
 * Framework discipline honoured here:
 *  - Efforts are person-days, grade `modelled` — planning estimates, never
 *    phrased as measured (they propagate into every bound's provenance).
 *  - Precedence arcs are GENUINE causal arcs (each carries its reason); the
 *    blanket P0–P3 phase ladder is kept separately so the engine can show what
 *    the ladder costs (Remark 4.4: Π is a property of the ordering).
 *  - Conventions must be frozen, not scheduled (Cor. 2.4): the frozen
 *    convention set C for this instance IS the canon — the symbol collision
 *    register and the standing ladder. Ii is not decidable from P; it is
 *    established by that freeze.
 */

// ---- scope lattice S (Def 1.2) — a small tree, child ⊑ parent ----
export const SCOPES = [
  { id: 'scope.root', parent: null },
  { id: 'scope.external', parent: 'scope.root' },
  { id: 'scope.external.device_lab', parent: 'scope.external' },
  { id: 'scope.external.corpora', parent: 'scope.external' },
  { id: 'scope.repo', parent: 'scope.root' },
  { id: 'scope.repo.registers', parent: 'scope.repo' },
  { id: 'scope.repo.evidence', parent: 'scope.repo' },
  { id: 'scope.repo.docs', parent: 'scope.repo' }
];

// ---- artifacts A with scope map φ : A → S ----
export const ARTIFACTS = [
  { id: 'art.device_models', scope: 'scope.external.device_lab', note: 'measured memristor device models — DOES NOT EXIST YET' },
  { id: 'art.automotive_corpus', scope: 'scope.external.corpora', note: 'labelled automotive-perception corpus — DOES NOT EXIST YET' },
  { id: 'art.gasification_baseline', scope: 'scope.external.corpora', note: 'existing bin-level validation baseline' },
  { id: 'art.latency_records', scope: 'scope.external.corpora', note: 'raw bundle latency records (248 parsed)' },
  { id: 'art.coverage_map', scope: 'scope.repo.docs', note: 'programCoverage.js — the gap list' },
  { id: 'art.coverage_sources', scope: 'scope.repo.docs', note: 'acquired source material for GAP subcategories' },
  { id: 'art.source_assessments', scope: 'scope.repo.docs', note: 'Source #1–#3 EVD assessments' },
  { id: 'art.engines', scope: 'scope.repo.evidence', note: 'simulation engines (definitions the proofs formalize)' },
  { id: 'art.validation_evidence', scope: 'scope.repo.evidence', note: 'the shared evidence dossier' },
  { id: 'art.proof_artifacts', scope: 'scope.repo.evidence', note: 'formal bounds / Lyapunov certificates' },
  { id: 'art.latency_ledger', scope: 'scope.repo.registers', note: 'reconciled latency ledger + counting convention' },
  { id: 'art.canon_registers', scope: 'scope.repo.registers', note: 'src/data/canon/* — claims/numbers/obligations' },
  { id: 'art.charter', scope: 'scope.repo.docs', note: 'signed review charter' }
];

/**
 * Tasks T — the OPEN obligations from canon/obligations.js (closed ones are
 * satisfied predecessors and drop out of the remaining-work instance).
 * effortPd: person-days, grade `modelled`. reads/writes: artifact ids.
 */
export const TASKS = [
  {
    id: 'ob.device_monotonicity', phase: 'P1', effortPd: 20,
    reads: ['art.device_models', 'art.engines'],
    writes: ['art.validation_evidence'],
    external: 'needs a device lab; art.device_models does not exist yet'
  },
  {
    id: 'ob.phi_transfer', phase: 'P1', effortPd: 15,
    reads: ['art.automotive_corpus', 'art.gasification_baseline'],
    writes: ['art.validation_evidence', 'art.canon_registers'],
    external: 'needs an automotive corpus; art.automotive_corpus does not exist yet'
  },
  {
    id: 'ob.formal_proofs', phase: 'P2', effortPd: 25,
    reads: ['art.engines'],
    writes: ['art.proof_artifacts', 'art.canon_registers'],
    external: null
  },
  {
    id: 'ob.gap_closure', phase: 'P2', effortPd: 67.5,
    reads: ['art.coverage_map'],
    writes: ['art.coverage_sources'],
    external: null,
    effortNote: 'midpoint of the carded 55–80 pd range (num.gap_effort_pd_lo/hi)'
  },
  {
    id: 'ob.latency_convention', phase: 'P2', effortPd: 5,
    reads: ['art.latency_records'],
    writes: ['art.latency_ledger'],
    external: null
  },
  {
    id: 'ob.registry_v3', phase: 'P3', effortPd: 3,
    reads: ['art.source_assessments'],
    writes: ['art.canon_registers'],
    external: null
  },
  {
    id: 'ob.review_charter', phase: 'P3', effortPd: 2,
    reads: ['art.canon_registers', 'art.latency_ledger', 'art.validation_evidence'],
    writes: ['art.charter'],
    external: null
  }
];

/**
 * Genuine causal arcs Ec (a must complete before b), each with its reason —
 * NOT the phase ladder. The engine computes bounds under BOTH orderings to
 * show what the blanket ladder costs (Remark 4.4).
 */
export const CAUSAL_ARCS = [
  { from: 'ob.latency_convention', to: 'ob.review_charter', reason: 'the charter\'s first matter is the contradiction register, which needs the reconciled latency ledger' },
  { from: 'ob.device_monotonicity', to: 'ob.review_charter', reason: 'the charter reviews the evidence dossier, which the device study populates' },
  { from: 'ob.phi_transfer', to: 'ob.review_charter', reason: 'same dossier: transfer evidence must exist to be reviewed' },
  { from: 'ob.formal_proofs', to: 'ob.review_charter', reason: 'proof artifacts are review inputs' },
  { from: 'ob.registry_v3', to: 'ob.review_charter', reason: 'register hygiene precedes the register-based review' }
];

export const PHASE_ORDER = ['P0', 'P1', 'P2', 'P3'];

/** Cor. 2.4 — the frozen convention set C for this instance. */
export const FROZEN_CONVENTIONS = {
  statement: 'Ii (informational independence) is not decidable from P (Prop. 2.3); it is established only by freezing conventions BEFORE decomposition. For this instance C is frozen by the canon:',
  frozen: [
    'symbol collision register (canon/symbols.js) — one glyph, one type',
    'claim-standing ladder (canon/standing.js) — five rungs, fixed authority',
    'number-card schema (canon/numbers.js) — kind/configuration/uncertainty mandatory',
    'gate-contract shape (canon/gates.js) — interface/invariant/provenance/reason-code'
  ]
};
