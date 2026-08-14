/**
 * canon/pcg.js — The Process Characterization Grammar, Specification 1.0
 * (normative, 28 July 2026).
 *
 * The base specification the Multi-Level Policy document (canon/level.js) is
 * a policy layer over — confirmed by its own normative-status line ("amending
 * neither... PCG 1.0 §2 W1–W4, D1; §5 Level"). PCG's G/R/C axis IS
 * canon/level.js's LEVEL ladder; this module does not redefine it, it imports
 * it (§5 here is level.js's LEVEL/LEVEL_ORDER/LEVEL_RANK, verbatim).
 *
 *   record = 〈 Aspect · Modality · Level 〉 + value + U + provenance (+ links)
 *
 * "Versioned events in the archive should follow the attached record
 * format" (user directive) — the one append-only, versioned event log in
 * this repo is ConstitutionalTruthEngine.archive (Art. IX: "no erasure
 * without lineage"; Art. XIX anti-silent-drift). Its events are exactly
 * PCG's DID — Occurred archetype: "events, logs, realized durations,
 * executed decisions" — but were shaped ad hoc. PcgEngine.js retrofits them:
 * every archive event now carries a `.record` field that IS a well-formed
 * PCG record, checked by CanonValidator C13.
 */

export { LEVEL, LEVEL_ORDER, LEVEL_RANK } from './level.js';

// The RELIANCE registry — canonically defined HERE (it is the registry
// FL·DID·C archive records cite) and re-exported by ConstitutionalTruthEngine
// for backward compatibility, so existing `import { RELIANCE } from
// './ConstitutionalTruthEngine.js'` call sites are untouched. This also
// breaks what would otherwise be a circular import between the engine and
// its own record-building module (PcgRecords.js needs RELIANCE; the engine
// needs PcgRecords.js).
export const RELIANCE = ['hypothesis', 'provisional', 'warranted', 'retracted'];

// ---- §3 ASPECT — six closed subjects, closed under the residual test (§8.2) ----
export const ASPECTS = {
  AT: { anchor: 'identity', name: 'Attribute', decisionTest: 'Predicates a property or quantity of an identified element, asserting no connection, transfer, order, recursion, or response.' },
  ST: { anchor: 'connection', name: 'Structure', decisionTest: 'Predicates a relation among two or more elements, static in the statement.' },
  FL: { anchor: 'transfer', name: 'Flow', decisionTest: 'Predicates that something moves, or may move, from one element toward another — including admission, routing, passage.' },
  TM: { anchor: 'order', name: 'Time', decisionTest: 'Predicates when, how long, in what order, or over what horizon — and nothing else.' },
  FB: { anchor: 'recursion', name: 'Feedback', decisionTest: 'Asserts a closed causal path — output re-entering as input.' },
  BH: { anchor: 'response', name: 'Behavior', decisionTest: 'Predicates output or condition as a function of input, load, or circumstance.' }
};
export const ASPECT_ORDER = ['AT', 'ST', 'FL', 'TM', 'FB', 'BH'];

// ---- §4 MODALITY — four forces, with total precedence (P1) ----
export const MODALITIES = {
  IS: { force: 'Describes', forceTest: 'Deleting the statement changes what is known, but nothing about what is permitted or controlled.' },
  MUST: { force: 'Binds', forceTest: 'The statement can be violated by a state of affairs, yet names no mechanism that prevents violation.' },
  GATE: { force: 'Controls passage', forceTest: 'The statement names an active test or veto that operates without further decision — remove it and passage changes.' },
  DID: { force: 'Occurred', forceTest: 'The statement is evidenced by a dated occurrence (W3) and would be false only if the occurrence is false.' }
};
// P1: a statement carrying several forces is coded once, at its most operative — DID ≻ GATE ≻ MUST ≻ IS.
export const MODALITY_PRECEDENCE = ['DID', 'GATE', 'MUST', 'IS'];

// ---- §6.1 content-record schema ----
export const RECORD_SCHEMA = [
  { field: 'id', content: 'Stable identifier; survives renaming and registry evolution.', obligation: 'MUST' },
  { field: 'address', content: '〈aspect · modality · level〉 per W1–W2.', obligation: 'MUST' },
  { field: 'subject', content: 'The element(s) the statement concerns.', obligation: 'MUST' },
  { field: 'value', content: 'Typed by level and aspect: quantity/distribution (G), normalized quantity (R), registry class (C), relation, or occurrence.', obligation: 'MUST' },
  { field: 'U', content: 'Uncertainty: interval, distribution, or membership confidence P. `unknown` is admissible; an absent U field is not.', obligation: 'MUST' },
  { field: 'provenance', content: 'Source; method (direct|inferred|proxy|composite); basis/registry@version where W2 requires; timestamp; cited operation records.', obligation: 'MUST' },
  { field: 'links', content: 'Typed references to other records (§6.3).', obligation: 'MAY' }
];

// ---- §6.2 operation-record schema (analyst acts — never system content) ----
export const OPERATION_RECORD_SCHEMA = ['objective', 'options', 'criterion', 'choice'];

// ---- §6.3 link vocabulary ----
export const LINK_TYPES = {
  'condition-of': 'Quantity or state conditioning a bound, gate, or response.',
  'derives-from': 'Value computed from cited records (carries the basis for R-level values).',
  'instance-of': 'Occurrence realizing a standing gate, norm, or pattern.',
  enforces: 'Gate implementing a norm (N4).',
  produces: 'Mechanism or loop yielding a response (N1).',
  contradicts: 'Records that cannot jointly hold; both stay, flagged.'
};

// ---- §2.2 well-formedness + §2.3 decomposition ----
export const WELL_FORMEDNESS = [
  { id: 'W1', text: 'A content record MUST bear exactly one aspect, one modality, and one level. Aspect ∅ (residual) is admissible while open, never at close.' },
  { id: 'W2', text: 'Level R MUST cite a basis; level C MUST cite a registry at a version. A value citing neither is ground. The level of a bound or gate is the level of the quantity it binds or tests.' },
  { id: 'W3', text: 'Modality DID MUST carry occurrence provenance — a log, observation, or timestamp. Assertions about what would or should happen never take DID.' },
  { id: 'W4', text: 'A classified value MUST be a member of its cited registry. `unclassified` is always admissible and auditable; a label outside the registry is ill-formed.' },
  { id: 'D1', text: 'One record codes one statement. A compound statement MUST be decomposed into linked records; precedence (P1) selects the dominant address.' }
];

// ---- §6.4 lifecycle: OPEN → TYPED → CLOSED ----
export const CLOSE_CONDITIONS = [
  { id: 'CC1', text: 'Address complete and well-formed (W1–W4); no ∅ remains.' },
  { id: 'CC2', text: 'Value present and typed per level and aspect.' },
  { id: 'CC3', text: 'U present (possibly unknown).' },
  { id: 'CC4', text: 'Provenance complete, including basis / registry@version where required.' },
  { id: 'CC5', text: 'All links resolvable; every derives-from names its function.' },
  { id: 'CC6', text: 'Decompositions (D1) exhausted — no compound statements remain.' }
];
export const IMMUTABILITY_RULE =
  'Closed records are immutable. Revision means superseding: a new record with a supersedes reference ' +
  'in provenance. The record set is therefore an append-only characterization of the case, and any ' +
  'state of knowledge can be reconstructed at any time.';

// ---- §8.5 audit affordances (mechanical consequences of the type system) ----
export const AUDIT_AFFORDANCES = {
  A1: 'Unenforced norms: MUST records lacking an enforces in-link.',
  A2: 'Ungoverned gates: GATE records lacking a norm they enforce.',
  A3: 'Dead rules: GATE records with no instance-of occurrences over an audit window.',
  A4: 'Incomparable comparisons: cross-case use of R values with incompatible or unstated bases.',
  A5: 'Description gaps: empty completeness-map cells where the case type predicts content.',
  A6: 'Vocabulary strain: rising unclassified rate in any registry.'
};

// ---- the calibrating example from §4.3 — the aspect shift at GATE ----
export const CANONICAL_CHAIN_EXAMPLE = [
  { statement: 'Processing capacity is 95/h', address: 'AT·IS·G' },
  { statement: 'Occupancy must not exceed 90', address: 'AT·MUST·G' },
  { statement: 'Admission is blocked above 90', address: 'FL·GATE·G' },
  { statement: 'Admission was blocked at 09:14', address: 'FL·DID·G' }
];

// ---- a small, real PCG registry this repo defines: archive-record disposition ----
// (C1: a registry declares domain, space, classes with membership criteria, an
// explicit unclassified member, and a version.) Erasure is NOT a RELIANCE
// transition (RELIANCE has no 'erased' member — assigning it there would be
// W4-ill-formed, an invented label outside the cited registry). It is its own
// small classification.
export const ARCHIVE_DISPOSITION = {
  registry_id: 'archive.disposition',
  version: 1,
  domain: 'ConstitutionalTruthEngine archive — a claim\'s standing in the active record set',
  space: 'claim lifecycle position',
  classes: {
    active: 'the claim is a live member of `claims` (any RELIANCE status, including retracted — retraction is an epistemic state, not a disposition)',
    erased: 'removed from the active claim set by principled erasure (Part II: correlation decay or τ-hierarchy); lineage retained in the Archive',
    unclassified: 'legal, auditable — flags this registry for extension'
  }
};

export const THESIS = {
  quote: 'record = 〈 Aspect · Modality · Level 〉 + value + U + provenance',
  rule: IMMUTABILITY_RULE,
  origin: 'Process Characterization Grammar, Specification 1.0 — normative, 28 July 2026.'
};
