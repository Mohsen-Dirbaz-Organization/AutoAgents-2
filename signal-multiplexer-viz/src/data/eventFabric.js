/**
 * eventFabric.js — F26 Autonomous-Driving Event-Fabric Integration Plan.
 * Structured data transcribed from the F26 synthesis (see
 * F26_Autonomous_Driving_Event_Fabric_Plan.md). Core structures encoded; the
 * 248 latency cards are represented by their parsed summary counts (not yet
 * itemized) and the "144 unique latencies" claim is kept honestly UNCONFIRMED.
 *
 * Source-standing discipline (a core F26 principle): every claim carries a
 * standing, and projected/notional/open claims may NOT expand runtime authority.
 */

export const THESIS = {
  not: [
    'a camera-first perception stack',
    'a generic multi-agent research laboratory',
  ],
  is: `A single-timeline autonomous-driving event fabric where ARC-style continuous sensory traces become synchronized, provenance-bound witnesses across twelve operational canvases. The base unit is a synchronized interaction trace — not a pixel, object, frame, or point cloud. Semantic vision is one canvas among many.`,
  safety: `Safety-bearing paths can only CONTRACT authority — through one-sided safe-rounded φ, meet/intersection composition, rate-clamp-last projection, and fail-closed veto. F26 is the final integration discipline: strict schemas, event dictionaries, source-standing ledgers, validation packs, latency/race workbenches, manuals, import/export packages, and operational playbooks.`,
};

// ---- Source-standing taxonomy (the projected/measured seam, F26 form) ----
export const STANDING_META = {
  established: { label: 'Established', color: '#22a06b', authority: 'May support the safety case', rank: 0 },
  proposed:    { label: 'Proposed',    color: '#2b6cb0', authority: 'Requires a validation pack', rank: 1 },
  projected:   { label: 'Projected',   color: '#DAA520', authority: 'Cannot expand authority', rank: 2 },
  notional:    { label: 'Notional',    color: '#c0392b', authority: 'Cannot expand authority', rank: 3 },
  open:        { label: 'Open',         color: '#8a6d1f', authority: 'Evidence debt only', rank: 4 },
};

// ---- Timeline spine + twelve operational canvases ----
export const CANVASES = [
  { layer: 'T', name: 'Timeline spine / temporal truth', purpose: 'Single physical timeline binding all evidence, witnesses, memory, decisions, and actuation.', spine: true },
  { layer: 1, name: 'World / phenomenon', purpose: 'Road, weather, friction, agents, occlusion, flow — physical phenomena before sensing.' },
  { layer: 2, name: 'Receiver body', purpose: 'Vehicle body, receptor geometry, sensor health, proprioception, calibration, synchronization integrity.' },
  { layer: 3, name: 'Raw wave / analog', purpose: 'Scattering, pressure, vibration, thermal, EM, fluid, phase, latency, slippage traces.' },
  { layer: 4, name: 'Synchronized physical event fabric', purpose: 'Central causal witness fabric: path dust, vector clocks, contradictions, provenance.', central: true },
  { layer: 5, name: 'Semantic scene', purpose: 'Objects, lanes, drivable space, signs, traffic lights — important but NOT sovereign.' },
  { layer: 6, name: 'Agent-intent / social dynamics', purpose: 'Intent, negotiation, social motion, vulnerable road users, traffic behavior.' },
  { layer: 7, name: 'Reflex safety', purpose: 'Immediate contraction, veto, fail-closed, trusted scalar φ.' },
  { layer: 8, name: 'Tactical maneuver', purpose: 'Local maneuver candidates: brake, yield, merge, pass, lane change.' },
  { layer: 9, name: 'Strategic planning', purpose: 'Route, mission, ODD, longer-horizon goals.' },
  { layer: 10, name: 'Physics constraint / execution', purpose: 'Actuator limits, friction, stability, projection to safe command.' },
  { layer: 11, name: 'Meta-safety / epistemic', purpose: 'Source standing, uncertainty, novelty, desynchronization, evidence debt.' },
  { layer: 12, name: 'Scenario graduation / validation', purpose: 'Scenario packs, degraded-stack tests, replay, certification obligations.' },
];

// ---- F1–F26: communication-bundle idea → autonomous-driving primitive ----
export const F_MAP = [
  { f: 'F1', idea: 'Trust and reputation', primitive: 'Sensor-witness & source-standing reputation for receptors, modalities, runtime agents, maps, models.' },
  { f: 'F2', idea: 'Byzantine consensus', primitive: 'Quorum for authority-expanding claims; contradiction preserved, not averaged.' },
  { f: 'F3', idea: 'Vector clocks', primitive: 'Causal ordering across physical, capture, inference, fusion, planning, actuation, memory.' },
  { f: 'F4', idea: 'Trust-consensus-clock integration', primitive: 'Witness accepted only if standing, quorum, and causal freshness all pass.' },
  { f: 'F5', idea: 'Error correction', primitive: 'Packet/evidence repair without erasing physical contradiction.' },
  { f: 'F6', idea: 'Cognitive continuity', primitive: 'Scenario, regime, and memory continuity without silent truth mutation.' },
  { f: 'F7', idea: 'Routing as MAB', primitive: 'Evidence routing by risk reduction per latency/resource cost.' },
  { f: 'F8', idea: 'Route value function', primitive: 'Information appetite: route value = uncertainty/risk reduction under deadline.' },
  { f: 'F9', idea: 'Trust-routing integration', primitive: 'Route around untrusted/desynchronized sensors until revalidated.' },
  { f: 'F10', idea: 'Resource allocation', primitive: 'Allocate event registers, compute, WAL, sensor bandwidth, runtime agents by safety authority.' },
  { f: 'F11', idea: 'Routing-resource integration', primitive: 'Select minimum sufficient evidence path that meets the decision deadline.' },
  { f: 'F12', idea: 'Multiplexing', primitive: 'Multiplex modality/event/agent channels by latency class and authority path.' },
  { f: 'F13', idea: 'Selective memory', primitive: 'Metabolic memory: reflexive, tactical, strategic residence-time partitions.' },
  { f: 'F14', idea: 'Consensus-persistence integration', primitive: 'Durable replayable witnesses for consensus, contraction, veto, source-standing changes.' },
  { f: 'F15', idea: 'Causal recovery', primitive: 'Replay and rollback by vector-clock order.' },
  { f: 'F16', idea: 'Recovery as governance', primitive: 'Fallback/recovery patterns promoted only after evidence review.' },
  { f: 'F17', idea: 'Isolation-integration balance', primitive: 'Isolate contradictory/degraded modalities; reintegrate only after validation.' },
  { f: 'F18', idea: 'Complete learning loop', primitive: 'Monitoring/calibration improve future witnesses but do not create runtime authority by prose.' },
  { f: 'F19', idea: 'Physics-informed constraints', primitive: 'Friction, kinematics, conservation, actuator geometry as hard constraints.' },
  { f: 'F20', idea: 'Uncertainty quantification', primitive: 'Structured uncertainty object; trusted scalar φ must be one-sided and safe-rounded.' },
  { f: 'F21', idea: 'Online model adaptation', primitive: 'Regime-aware adaptation quarantined until validated; contraction allowed, expansion certified.' },
  { f: 'F22', idea: 'Anytime algorithms', primitive: 'Partial certificates under deadline may contract/defer, not expand authority.' },
  { f: 'F23', idea: 'Structure recognition', primitive: 'Detect black ice, fog, hydroplaning, dust, sensor desync, ODD transitions.' },
  { f: 'F24', idea: 'Hyperparameter framework', primitive: 'Hyperparameters become audited policy surfaces with rollback and source standing.' },
  { f: 'F25', idea: 'Validation and certification', primitive: 'Scenario graduation, replay, degraded-stack tests, latency/race evidence.' },
  { f: 'F26', idea: 'Final integration discipline', primitive: 'Schemas, event dictionaries, validation packs, manuals, ledgers, workbenches, import/export packages.', capstone: true },
];

// ---- Source inventory S01–S29 (role + standing + contribution) ----
export const SOURCES = [
  { id: 'S01', name: '175-step-bundle-unified.pdf', role: 'Controlling', standing: 'established', contribution: 'F1–F26, trust, consensus, vector clocks, routing, resource allocation, multiplexing, WAL, recovery, monitoring, latency records.', limitation: 'Latency records required reconciliation.' },
  { id: 'S02', name: 'ARC_sensory_architecture_EN_1.md', role: 'Controlling', standing: 'established', contribution: 'ARC sensing as continuous informational absorption fabric; synchronized interaction trace as base unit.', limitation: 'None.' },
  { id: 'S03', name: 'ARC_sensory_architecture_EN_2.md', role: 'Duplicate', standing: 'established', contribution: 'Same as S02.', limitation: 'Exact duplicate of S02.' },
  { id: 'S04', name: 'Agent-Based Models to Coordinate Thread Synchronization.html', role: 'Supporting', standing: 'proposed', contribution: 'PMP/thread synchronization model for the race-condition workbench.', limitation: 'Logic embedded in HTML/JS; treated as prototype.' },
  { id: 'S05', name: 'Algorithm-Total.pdf', role: 'Historical / low-priority', standing: 'open', contribution: 'Elliptic, CCA, reduced-order, squarity lineage.', limitation: 'Not controlling for vehicle integration.' },
  { id: 'S06', name: 'Canvas_Integrated_to_EPU.pdf', role: 'Controlling (canvas mechanics)', standing: 'established', contribution: 'Canvas as indexed runtime substrate, checkpoints, streams, replay, no hidden action-relevant state.', limitation: 'None material.' },
  { id: 'S07', name: 'Conservation-Renormalization_Adaptive-Autonomy.pdf', role: 'Supporting / proposed', standing: 'proposed', contribution: 'Zero-sum gain budget, topological phase labels, contestable witnesses.', limitation: 'Proposed until validated.' },
  { id: 'S08', name: 'EPU Hardware Architecture.pdf', role: 'Supporting / notional', standing: 'notional', contribution: 'EPU event-register and architecture diagrams.', limitation: 'Image-heavy; text extraction weak; hardware claims not established.' },
  { id: 'S09', name: 'EPU-Ghost-Autonomy_English.pdf', role: 'Controlling (safety / source standing)', standing: 'established', contribution: 'Architecture of refusal, fail-closed veto, measured-vs-projected timing discipline.', limitation: 'Timing beyond FPGA guard/veto remains projected/notional.' },
  { id: 'S10', name: 'Functionally-Driven Refinement Schedule.pdf', role: 'Historical', standing: 'open', contribution: 'Original F1–F26 refinement schedule.', limitation: 'Image-heavy; superseded by parsed schedule in S01.' },
  { id: 'S11', name: 'Mercedes_Benz_AV_Perception_Stack_v2.html', role: 'Supporting / illustrative', standing: 'proposed', contribution: 'AV perception lifecycle prototype from raw reception to decision.', limitation: 'Prototype only; brand-specific details not established.' },
  { id: 'S12', name: 'Metabolic_Memory_Architecture.md', role: 'Controlling', standing: 'established', contribution: 'Memory as residence-time organ; reflexive/tactical/strategic partitions.', limitation: 'None.' },
  { id: 'S13', name: 'PLAN.md', role: 'Historical / supporting', standing: 'open', contribution: 'Prior F26 artifact plan.', limitation: 'Research-lab scope corrected.' },
  { id: 'S14', name: 'Quantum-Information-Elliptical-Integral-Algorithm.pdf', role: 'Low-priority', standing: 'open', contribution: 'Mathematical/topological feature-map lineage.', limitation: 'Not controlling.' },
  { id: 'S15', name: 'README.md', role: 'Supporting', standing: 'proposed', contribution: 'Bounded-autonomy visualization, CQR/TPD notes, measured timing caveat.', limitation: 'Needs source-standing filtering.' },
  { id: 'S16', name: 'Research_vcs_v7.html', role: 'Supporting', standing: 'proposed', contribution: 'Single-tenant cells, coordinate validation, streams, checkpoints.', limitation: 'Prototype mechanics only.' },
  { id: 'S17', name: 'Subset-2-ellip_zpk_complete.pdf', role: 'Low-priority', standing: 'open', contribution: 'Elliptic/ZPK supplement.', limitation: 'Not controlling.' },
  { id: 'S18', name: 'Ten_Articles_corrected.pdf', role: 'Controlling (safety doctrine)', standing: 'established', contribution: 'One-sided φ, meet-only safety paths, rate-clamp-last, analog veto, fail-closed, source discipline.', limitation: 'None material.' },
  { id: 'S19', name: 'autonomous driving considerations - canvas fabric.pages', role: 'Supporting', standing: 'proposed', contribution: 'Original Pages version of canvas/fabric doctrine.', limitation: 'Pages/IWA text only partially recoverable.' },
  { id: 'S20', name: 'autonomous_driving_considerations_translated_en.md', role: 'Controlling', standing: 'established', contribution: '12-canvas doctrine, 100-scenario ladder, sensor graduation, latency classes.', limitation: 'None.' },
  { id: 'S21', name: 'comprehensive_codesign_treaty.docx', role: 'Supporting (duplicate)', standing: 'established', contribution: 'Treaty content in DOCX.', limitation: 'Use S22 Markdown as primary.' },
  { id: 'S22', name: 'comprehensive_codesign_treaty.md', role: 'Controlling', standing: 'established', contribution: 'Co-design treaty, one timeline, no camera sovereignty, event-register budget, path dust, black-ice slice.', limitation: 'Hardware/register-budget claims notional unless verified.' },
  { id: 'S23', name: 'main_revised.pdf', role: 'Supporting / low-priority', standing: 'open', contribution: 'ROM/feature-map/conservation math lineage.', limitation: 'Not controlling.' },
  { id: 'S24', name: 'multi_agent_research_laboratory.html', role: 'Supporting (mechanics only)', standing: 'proposed', contribution: 'Event-sourced app mechanics.', limitation: 'Domain is separate.' },
  { id: 'S25', name: 'multi_agent_research_laboratory_manual.md', role: 'Supporting (mechanics only)', standing: 'proposed', contribution: 'Manual workflow structure.', limitation: 'Translate out research-lab vocabulary.' },
  { id: 'S26', name: 'multi_agent_research_laboratory_upgrade_plan.md', role: 'Supporting (mechanics only)', standing: 'proposed', contribution: 'Canvas/event-sourcing, WAL, validation patterns.', limitation: 'Must not dominate the autonomous plan.' },
  { id: 'S27', name: 'strongest canvas transformations.pages', role: 'Supporting', standing: 'proposed', contribution: 'Canvas as spatial operating substrate; edges as contracts.', limitation: 'Pages/IWA partially recoverable.' },
  { id: 'S28', name: 'ten_article_revised_synthesis.tex', role: 'Supporting', standing: 'proposed', contribution: 'Later synthesis tying ARC, CQR/TPD, memory, F26.', limitation: 'Not controlling over direct sources.' },
  { id: 'S29', name: 'ten_article_rewrite_integration_notes.md', role: 'Historical', standing: 'open', contribution: 'Revision notes.', limitation: 'Low priority.' },
];

// ---- Latency reconciliation (the honest "144 not confirmed" finding) ----
export const LATENCY = {
  claim: { value: 144, label: '"144 unique latencies"', status: 'NOT CONFIRMED' },
  parse: [
    { k: 'Total latency-like records', v: 248 },
    { k: 'Exact "Latency:" records', v: 237 },
    { k: 'Unique raw entries (all)', v: 240 },
    { k: 'Unique raw entries (exact)', v: 229 },
    { k: 'Unique numeric endpoints (exact)', v: 84 },
    { k: 'Exact numeric values (exact)', v: 68 },
  ],
  conclusion: 'The "144 unique latencies" claim is not confirmed under any raw-entry, numeric-value, exact-record, or label-based counting convention tested. It may stem from a separate manual grouping convention not recoverable from the parsed records alone. The full deliverable includes 248 individual latency cards (Appendix A — not yet itemized here).',
  cardsItemized: false,
  cardCount: 248,
  regimes: [
    { regime: 'Sub-5 ns claims', interpretation: 'Projected event-gate/check/comparator budgets', rule: 'Not safety-case evidence unless measured on target hardware', standing: 'projected' },
    { regime: '5–50 ns claims', interpretation: 'Fast event-fabric operations', rule: 'Include in critical-path budget with deadline expiry', standing: 'projected' },
    { regime: '50 ns – 1 µs', interpretation: 'Micro-critical reasoning/fabric path', rule: 'Requires bounded queueing and measured platform behavior', standing: 'projected' },
    { regime: 'Variable / range / network', interpretation: 'Consensus, transport, dispatch, quorum, agent processing', rule: 'Never required for reflex refusal', standing: 'proposed' },
    { regime: 'Millisecond – second', interpretation: 'WAL, persistence, crash recovery, replication', rule: 'Replay and continuity only, not collision avoidance', standing: 'established' },
    { regime: 'Async monitoring', interpretation: 'Audit and observability', rule: 'Off safety path unless separately bounded', standing: 'established' },
  ],
};

// ---- Race-condition risks the workbench must model ----
export const RACE_RISKS = [
  'Timestamp/capture/inference disorder',
  'Stale witness use',
  'Quorum-after-deadline',
  'Semantic update overwriting a sensor-health warning',
  'Contradiction averaged away instead of preserved',
  'Fallback/veto racing the tactical plan',
  'WAL/checkpoint lag',
  'Shared-resource contention',
  'Duplicate step IDs from the communication bundle',
  'Projected/notional claims migrating into runtime authority',
];

// ---- Build-time vs runtime authority separation ----
export const AUTHORITY_LAYERS = [
  { layer: 'Build-time enterprise LLM / agents', allowed: 'Generate schemas, scenario packs, null sets, falsifiers, witness dictionaries, validation plans, manuals, source ledgers.', forbidden: 'Direct runtime actuation or uncertified authority expansion.', output: 'Strict JSON, Markdown docs, validation evidence.' },
  { layer: 'Runtime vehicle agents', allowed: 'Match certified witnesses, compute residuals, maintain vector clocks, contract envelopes, request veto, log evidence.', forbidden: 'Invent new action authority, treat prose as policy, expand envelope from online learning.', output: 'witness.accepted, envelope.contracted, veto.asserted, fallback.invoked.' },
];

// ---- Event-fabric package: top-level schema keys (for display) ----
export const SCHEMA = {
  version: 'ad-event-fabric-package-1.0',
  topLevelKeys: [
    'schema_version', 'package_id', 'package_title', 'source_standing_policy',
    'timeline', 'canvases', 'nodes', 'edges', 'events', 'witnesses',
    'latency_records', 'race_risks', 'f1_f26_matrix', 'validation_tests',
    'source_ledger', 'export_manifest', 'unresolved_claims',
  ],
  nodeFamilies: ['phenomenon', 'receptor', 'raw_trace', 'fabric_event', 'semantic_claim', 'intent_hypothesis', 'reflex_constraint', 'maneuver_option', 'strategic_goal', 'physics_envelope', 'meta_safety_signal', 'scenario_case', 'witness', 'memory_item', 'validation_artifact', 'source_claim', 'latency_card', 'race_risk'],
  edgeFamilies: ['observes', 'synchronizes_with', 'causes', 'contradicts', 'supports', 'updates', 'contracts', 'projects_to', 'expires', 'requires', 'guards', 'recovers_from', 'validates', 'derives_from', 'quarantines', 'reintegrates'],
  validationRules: [
    '(canvas_id, cell_id, timeline_tick) is single-tenant.',
    'Every safety-bearing event has a validity window, vector clock, source standing, provenance, and replay hash.',
    'Safety paths use meet/intersection, not union or averaging.',
    'Rate clamp happens last.',
    'Deadline miss, stale evidence, corrupted data, unresolved contradiction, or missing warrant contracts authority or fails closed.',
    'Projected, notional, or open claims cannot expand runtime authority.',
    'Checkpoints must replay into identical canvas projections.',
  ],
};

// ---- Open questions / unresolved claims (evidence debt) ----
export const UNRESOLVED = [
  { claim: '"144 unique latencies"', status: 'Not confirmed by parse', closure: 'Canonical counting convention and reconciled ledger.' },
  { claim: 'EPU/ASIC timing beyond FPGA guard/veto', status: 'Projected/notional', closure: 'Target-hardware measurement.' },
  { claim: '512 event-register budget', status: 'Notional/source-specific', closure: 'Register allocation proof on actual hardware.' },
  { claim: 'One-sided φ', status: 'Proposed safety primitive', closure: 'Proof that φ never understates risk on the certified domain.' },
  { claim: 'φ liveness', status: 'Open', closure: 'Frontier where refusal remains safe but not permanently frozen.' },
  { claim: 'Non-box actuator meet/projection', status: 'Open', closure: 'Formal projection proof and hardware-in-loop validation.' },
  { claim: 'Topological phase labels', status: 'Proposed', closure: 'Validation across black ice, fog, hydroplaning, desync, ODD transitions.' },
  { claim: 'Conservation-renormalization', status: 'Proposed', closure: 'Evidence the zero-sum gain budget preserves relevant conserved quantities in vehicle data.' },
  { claim: 'ARC final sensor families', status: 'Open', closure: 'Information-appetite and scenario-graduation experiments.' },
  { claim: 'Runtime online adaptation', status: 'High-risk proposed mechanism', closure: 'Quarantine protocol and certified update path.' },
  { claim: 'LLM-generated packages', status: 'Useful but not authoritative', closure: 'Strict schema validation, replay, source-standing audit, human/automated review.' },
];

export const SOURCE_SUMMARY = {
  total: SOURCES.length,
  controlling: SOURCES.filter(s => /Controlling/.test(s.role)).length,
  duplicates: ['S02↔S03 (ARC) exact', 'S21↔S22 (treaty) format variants — S22 Markdown is primary'],
};
