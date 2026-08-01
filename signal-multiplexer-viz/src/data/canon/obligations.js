/**
 * canon/obligations.js — the P0–P3 open-obligations register.
 * (EPU Companion, Master Index 2/3: "the layer states them, the programme must
 * close them.") This register SUPERSEDES the ad-hoc O-1…O-11 lists and the
 * Void Map V-1…V-4 as the single obligations ledger (maintenance rule: extend
 * registers, don't spawn documents). Prior IDs are kept in `absorbs` so no
 * obligation is silently dropped.
 */

export const PRIORITY_META = {
  P0: { label: 'P0 — release-blocking', color: '#c0392b' },
  P1: { label: 'P1 — required for standing', color: '#d98b1f' },
  P2: { label: 'P2 — engineering debt', color: '#2b6cb0' },
  P3: { label: 'P3 — governance/process', color: '#667089' }
};

export const OBLIGATIONS = [
  // ---- from the companion's register, scoped to what this repo can own ----
  {
    obligation_id: 'ob.retire_vocabulary',
    priority: 'P0',
    text: 'Retire the retired vocabulary everywhere; apply the errata at source.',
    owner: 'this repo (documentation & release)',
    status: 'closed',
    closure: 'T5 struck + retirement register; §3.4 no-op language removed at source; symbol register applied at declaration sites.',
    absorbs: []
  },
  {
    obligation_id: 'ob.no_achieved_targets',
    priority: 'P0',
    text: 'Withdraw "achieved" phrasing from any claim lacking an artifact identifier; every number carries kind + configuration.',
    owner: 'this repo',
    status: 'closed',
    closure: 'Number cards in canon/numbers.js; validator check C-standing enforces it mechanically.',
    absorbs: []
  },
  {
    obligation_id: 'ob.threshold_provenance',
    priority: 'P1',
    text: 'Threshold provenance for every gate, derived from hazards.',
    owner: 'this repo (sim gates); systems safety (vehicle gates)',
    status: 'closed-in-sim',
    closure: 'Every gate card in canon/gates.js names its hazard; validator C2 fails release on a missing provenance.',
    absorbs: []
  },
  {
    obligation_id: 'ob.tol_conserve_derived',
    priority: 'P1',
    text: 'Conservation tolerance per check, derived from the arithmetic format.',
    owner: 'this repo (numerical methods)',
    status: 'closed',
    closure: 'tolConserve() derives from Number.EPSILON, multiplet size and gain bound; replaces the chosen 1e-9.',
    absorbs: []
  },
  {
    obligation_id: 'ob.device_monotonicity',
    priority: 'P1',
    text: 'Monotone admission must survive device non-ideality on MEASURED memristor models (violation rate → 0 under discipline).',
    owner: 'PoC Workstream A (external)',
    status: 'open',
    closure: null,
    absorbs: ['O-10', 'Void V-1 (device slice)'],
    note: 'Cannot close in-sim; hz.device_nonideality is deliberately listed as an orphan hazard until device data exists.'
  },
  {
    obligation_id: 'ob.phi_transfer',
    priority: 'P1',
    text: 'φ-compiler transfer gasification → automotive: cross-domain correlation r > 0.7 + deterministic-surrogate rank corr ≥ 0.85.',
    owner: 'Conservation-Manifold Compiler team (external)',
    status: 'open',
    closure: null,
    absorbs: ['O-3', 'O-11'],
    note: 'clm.xi_tracks_risk stays proposed until closed.'
  },
  {
    obligation_id: 'ob.formal_proofs',
    priority: 'P2',
    text: 'Formal bounds still owed: one-sided bound for xi_feature (O-1); Lyapunov/convergence for the CQR flow (O-2); rigorous finite-N invariant for S_parity (O-5); impulsive stability (O-7); finite-time ergodic guarantee (O-8).',
    owner: 'formal analysis (open)',
    status: 'open',
    closure: null,
    absorbs: ['O-1', 'O-2', 'O-5', 'O-7', 'O-8', 'Void V-1 (proof slice)'],
    note: 'In-sim evidence exists (ValidationHarness / Validation Frontier): O-2 Monte-Carlo non-expansiveness bound and O-8 measured t_conv reach verified-in-sim (their stated exit conditions); O-1/O-5/O-7 advanced (TPD-01 cross-validation for O-5). The FORMAL proofs remain owed — in-sim evidence does not close them.'
  },
  {
    obligation_id: 'ob.gap_closure',
    priority: 'P2',
    text: 'Close the 11 GAP subcategories of the coverage map (num.gap_effort_pd_lo–hi person-days of source acquisition).',
    owner: 'program planning',
    status: 'open',
    closure: null,
    absorbs: ['Void V-2']
  },
  {
    obligation_id: 'ob.latency_convention',
    priority: 'P2',
    text: 'Canonical counting convention + reconciled ledger for the bundle\'s latency records (the "144" claim stays NOT CONFIRMED until then); itemize the 248 cards.',
    owner: 'F26 integration / upstream bundle',
    status: 'open',
    closure: null,
    absorbs: ['F26 unresolved claim #1']
  },
  {
    obligation_id: 'ob.registry_v3',
    priority: 'P3',
    text: 'Registry hygiene for Source #1–#3 assessments (identity anchors, constraint scoring) — now as register EXTENSIONS, not new documents.',
    owner: 'this repo',
    status: 'open',
    closure: null,
    absorbs: ['Void V-4'],
    note: 'The maintenance rule forbids growing by file count; any retrofit lands in canon/claims.js, not a new markdown.'
  },
  {
    obligation_id: 'ob.review_charter',
    priority: 'P3',
    text: 'Review charter with the contradiction register as its first matter.',
    owner: 'governance (external)',
    status: 'open',
    closure: null,
    absorbs: []
  },
  // ---- closed by earlier phases, recorded so absorption is complete ----
  {
    obligation_id: 'ob.wcet_partition',
    priority: 'P2',
    text: 'WCET of the simplex projection: resolved architecturally — projection off the hard real-time path; only the latched S_parity-bit read is on it.',
    owner: 'architecture',
    status: 'closed',
    closure: 'Architectural resolution (O-4). No silicon timing claim is made (that would need a configuration artifact).',
    absorbs: ['O-4']
  },
  {
    obligation_id: 'ob.channel_metric',
    priority: 'P2',
    text: 'Discrete channel-space geometry: define the metric or flag as heuristic.',
    owner: 'this repo',
    status: 'closed',
    closure: 'Defined: Euclidean conserved metric on gauge-fixed shapes; log-gain is the zero-sum gauge coordinate (CRL §3.1).',
    absorbs: ['O-9']
  },
  {
    obligation_id: 'ob.evidence_order_safety',
    priority: 'P1',
    text: 'Introduction order of evidence backing a claim must not create an unsafe reading: no prefix of the declared order may license \'proceed\' when the complete evidence would not.',
    owner: 'this repo',
    status: 'closed',
    closure: 'Lemma Composition and Introduction-Order Formalism instantiated (canon/evidence.js, EvidenceCompositionEngine.js). Ground truth is order-invariant by construction (meet is commutative); the falsifiable check is per-prefix safety, verified live for 3 registered instances and enforced by CanonValidator C10 (a well-formed instance regressing to unsafe, or a demonstration instance losing its hazard, both block release — confirmed by deliberate corruption in both directions).',
    absorbs: ['opening challenge (session dialogue, 2026-07-28): "which evidence components are independent and exchangeable, and which must be introduced before others"']
  },
  {
    obligation_id: 'ob.level_locality_discipline',
    priority: 'P1',
    text: 'Every consequential quantity\'s derivation level (G/R/C) is explicit; R needs a cited basis, C a cited registry; scope namespaces used as independence axes must not collide; and weakening evidence must never promote a claim\'s standing (demotion, not mutation).',
    owner: 'this repo',
    status: 'closed',
    closure: 'Multi-Level Policy instantiated (canon/level.js, LevelEngine.js). Multiplication licence (MLP-5) verified disjoint across canon/planning.js and canon/evidence.js scope namespaces (falsified by deliberate collision, restored). Gate-level inheritance (5.4) applied to all 7 gates.js entries — 2 honest apparatus-not-frozen warnings surfaced (gate.masking_probe epsilon, gate.llc_quarantine delta), not silently passed. Demotion-not-mutation (MLP-7) scenario-tested against ConstitutionalTruthEngine (3 scenarios, 10 steps, 0 violations), with the detector\'s own falsifiability proven against a synthetic violation before trusting its pass on the real engine.',
    absorbs: ['session dialogue, 2026-07-28: MLP_visual_abstract.html incorporation request']
  },
  {
    obligation_id: 'ob.archive_record_format',
    priority: 'P1',
    text: 'Versioned events in ConstitutionalTruthEngine.archive follow a well-formed PCG record: 〈Aspect·Modality·Level〉 + value + U + provenance, checked against CC1–CC4 / W1 / W2 / W4.',
    owner: 'this repo',
    status: 'closed',
    closure: 'Process Characterization Grammar instantiated (canon/pcg.js, PcgRecords.js, PcgEngine.js). Every archive event now additionally carries `.record`: reliance transitions are FL·DID·C (registry RELIANCE, instance-of gate.challenge — mirroring the spec\'s own R05 example); erasure is AT·DID·C against a new archive.disposition registry (not RELIANCE — `erased` is not a RELIANCE member, which was the pre-existing bug this retrofit fixes). CanonValidator C13 validates every record from a driven scenario (11 events, 5 event types, 0 ill-formed), with the record validator\'s own falsifiability proven first. Additive only — existing archive/lineage fields and ConstitutionalLog.jsx are unchanged; the PCG address is now also shown inline there.',
    absorbs: ['session dialogue, 2026-07-28: process_characterization_grammar.pdf incorporation request']
  },
  {
    obligation_id: 'ob.retraction_churn_engine_note',
    priority: 'P3',
    text: 'A claim already at reliance=retracted, hit with a further strong challenge, is silently re-adjudicated to `hypothesis` (a rank increase, MLP-7-relevant) with UNCHANGED evidence before the retraction check re-fires it back to `retracted` within the same challenge() call — because _adjudicate does not treat `retracted` as terminal.',
    owner: 'this repo (engine)',
    status: 'open',
    closure: null,
    absorbs: [],
    note: 'Discovered while driving the PCG archive audit scenario (ob.archive_record_format); both resulting archive records are individually well-formed PCG records, so this is not a record-format defect — it is a pre-existing engine-behavior nuance, out of scope for this incorporation. The demo scenario caps repeated challenges at 2 hits to avoid exercising the churn.'
  },
  {
    obligation_id: 'ob.multiplexer_lifecycle',
    priority: 'P2',
    text: 'Multiplexer simulation must be view-scoped (was App-scoped, kept ticking in the background).',
    owner: 'this repo',
    status: 'closed',
    closure: 'App.jsx stops the simulation on navigation away from the multiplexer view.',
    absorbs: ['Void V-3']
  }
];
