# F26 Autonomous-Driving Event-Fabric Integration Plan

> Canonical artifact for the F26 integration. The autonomous-driving / EPU / ARC /
> event-fabric stack is **controlling**; the research-lab files are used only for
> reusable canvas mechanics, event-sourcing, import/export validation, and manual
> structure. Structured data is encoded in [`src/data/eventFabric.js`](./src/data/eventFabric.js)
> and surfaced in the **Event Fabric (F26)** app view. Per the agreed scope, the
> 248 individual latency cards (Appendix A) are represented here by their parsed
> summary counts — **not yet itemized** (honest stub).

---

## Source Inventory Summary

| ID | Source | Role | Main contribution | Extraction limitation |
|---|---|---|---|---|
| S01 | 175-step-bundle-unified.pdf | Controlling (communication bundle) | F1–F26, trust, consensus, vector clocks, routing, resource allocation, multiplexing, WAL, recovery, monitoring, latency records | Text extracted; latency records required reconciliation |
| S02 | ARC_sensory_architecture_EN_1.md | Controlling | ARC sensing as continuous informational absorption fabric; synchronized interaction trace as base unit | None |
| S03 | ARC_sensory_architecture_EN_2.md | Duplicate | Same as S02 | Exact duplicate of S02 |
| S04 | Agent-Based Models to Coordinate Thread Synchronization.html | Supporting | PMP/thread synchronization model for race-condition workbench | Logic embedded in HTML/JS; treated as prototype |
| S05 | Algorithm-Total.pdf | Historical / low-priority | Elliptic, CCA, reduced-order, squarity lineage | Not controlling for vehicle integration |
| S06 | Canvas_Integrated_to_EPU.pdf | Controlling (canvas mechanics) | Canvas as indexed runtime substrate, checkpoints, streams, replay, no hidden action-relevant state | None material |
| S07 | Conservation-Renormalization_Adaptive-Autonomy.pdf | Supporting / proposed | Zero-sum gain budget, topological phase labels, contestable witnesses | Proposed until validated |
| S08 | EPU Hardware Architecture.pdf | Supporting / notional | EPU event-register and architecture diagrams | Image-heavy; text extraction weak; hardware claims not established |
| S09 | EPU-Ghost-Autonomy_English.pdf | Controlling (safety / source standing) | Architecture of refusal, fail-closed veto, measured-vs-projected timing discipline | Hardware timing beyond FPGA guard/veto remains projected/notional |
| S10 | Functionally-Driven Refinement Schedule.pdf | Historical | Original F1–F26 refinement schedule | Image-heavy; superseded by parsed schedule in S01 |
| S11 | Mercedes_Benz_AV_Perception_Stack_v2.html | Supporting / illustrative | AV perception lifecycle prototype from raw reception to decision | Prototype only; brand-specific details not established |
| S12 | Metabolic_Memory_Architecture.md | Controlling | Memory as residence-time organ; reflexive/tactical/strategic partitions | None |
| S13 | PLAN.md | Historical / supporting | Prior F26 artifact plan | Research-lab scope corrected |
| S14 | Quantum-Information-Elliptical-Integral-Algorithm.pdf | Low-priority | Mathematical/topological feature-map lineage | Not controlling |
| S15 | README.md | Supporting | Bounded-autonomy visualization, CQR/TPD notes, measured timing caveat | Needs source-standing filtering |
| S16 | Research_vcs_v7.html | Supporting | Single-tenant cells, coordinate validation, streams, checkpoints | Prototype mechanics only |
| S17 | Subset-2-ellip_zpk_complete.pdf | Low-priority | Elliptic/ZPK supplement | Not controlling |
| S18 | Ten_Articles_corrected.pdf | Controlling (safety doctrine) | One-sided φ, meet-only safety paths, rate-clamp-last, analog veto, fail-closed, source discipline | None material |
| S19 | autonomous driving considerations - canvas fabric.pages | Supporting | Original Pages version of canvas/fabric doctrine | Pages/IWA text only partially recoverable |
| S20 | autonomous_driving_considerations_translated_en.md | Controlling | 12-canvas doctrine, 100-scenario ladder, sensor graduation, latency classes | None |
| S21 | comprehensive_codesign_treaty.docx | Supporting duplicate | Treaty content in DOCX | Use S22 Markdown as primary |
| S22 | comprehensive_codesign_treaty.md | Controlling | Co-design treaty, one timeline, no camera sovereignty, event-register budget, path dust, black-ice slice | Hardware/register-budget claims notional unless verified |
| S23 | main_revised.pdf | Supporting / low-priority | ROM/feature-map/conservation math lineage | Not controlling |
| S24 | multi_agent_research_laboratory.html | Supporting only | Event-sourced app mechanics | Domain is separate |
| S25 | multi_agent_research_laboratory_manual.md | Supporting only | Manual workflow structure | Translate out research-lab vocabulary |
| S26 | multi_agent_research_laboratory_upgrade_plan.md | Supporting only | Canvas/event-sourcing, WAL, validation patterns | Must not dominate autonomous plan |
| S27 | strongest canvas transformations.pages | Supporting | Canvas as spatial operating substrate; edges as contracts | Pages/IWA partially recoverable |
| S28 | ten_article_revised_synthesis.tex | Supporting | Later synthesis tying ARC, CQR/TPD, memory, F26 | Not controlling over direct sources |
| S29 | ten_article_rewrite_integration_notes.md | Historical | Revision notes | Low priority |

**Duplicates:** `ARC_sensory_architecture_EN_1.md` ↔ `ARC_sensory_architecture_EN_2.md` (exact). `comprehensive_codesign_treaty.docx` ↔ `.md` (format variants; Markdown is the cleaner controlling text). Pages files overlap with translated/canvas materials but were not fully text-extractable.

---

## Corrected Thesis

The project is **not** a camera-first perception stack and **not** a generic multi-agent research laboratory.

It is a **single-timeline autonomous-driving event fabric** where ARC-style continuous sensory traces become synchronized, provenance-bound witnesses across **twelve operational canvases**. The base unit is a **synchronized interaction trace** — not a pixel, object, frame, or point cloud. Semantic vision is one canvas among many.

Safety-bearing paths can only **contract** authority — through one-sided safe-rounded φ, meet/intersection composition, rate-clamp-last projection, and fail-closed veto. **F26 is the final integration discipline** that turns this into strict schemas, event dictionaries, source-standing ledgers, validation packs, latency/race workbenches, manuals, import/export packages, and operational playbooks.

---

## 1. Corrected Scope

Controlling stack:

```
ARC sensory fabric → synchronized physical event fabric → twelve autonomous-driving canvases
→ metabolic memory → safety/refusal algebra → EPU/event-register execution substrate
→ validation and F26 packaging
```

The multi-agent laboratory is **separate**. Its useful elements are retained only as mechanics: single-tenant cells, typed nodes and edges, event-sourced replay, streams/worldlines, checkpoints, import validation, provenance ledgers, manual structure, validation workflows. The vehicle domain model is autonomous driving, not research coordination.

## 2. Governing Source Map

Controlling sources: ARC sensory doctrine **S02**; scenario/canvas doctrine **S20**; co-design treaty + twelve-canvas stack **S22**; communication bundle F1–F26 + latency records **S01**; canvas state-substrate mechanics **S06, S16, S24, S27**; safety/refusal/source-standing doctrine **S18, S09**; metabolic memory **S12**; conservation-renormalization + topological phase **S07** (supported by **S15**); prior F26 planning **S13** (reused only after scope correction).

Hardware-architecture sources are useful but **not governing**. Any EPU timing, ASIC, register, cost, yield, ISA, or opcode detail remains **projected/notional unless directly measured**. The recovered corpus supports only the FPGA guard/veto timing as measured.

## 3. Twelve-Canvas Architecture

One real **timeline spine** plus twelve operational canvases. (Some files describe an optional outer human research/design canvas; that is **not** a runtime vehicle canvas and is preserved as a distinct, optional outer layer.)

| Layer | Canvas | Runtime purpose |
|---|---|---|
| T | Timeline spine / temporal truth | Single physical timeline binding all evidence, witnesses, memory, decisions, and actuation |
| 1 | World / phenomenon | Road, weather, friction, agents, occlusion, flow — physical phenomena before sensing |
| 2 | Receiver body | Vehicle body, receptor geometry, sensor health, proprioception, calibration, synchronization integrity |
| 3 | Raw wave / analog | Scattering, pressure, vibration, thermal, EM, fluid, phase, latency, slippage traces |
| 4 | Synchronized physical event fabric | Central causal witness fabric: path dust, vector clocks, contradictions, provenance |
| 5 | Semantic scene | Objects, lanes, drivable space, signs, traffic lights — important but not sovereign |
| 6 | Agent-intent / social dynamics | Intent, negotiation, social motion, vulnerable road users, traffic behavior |
| 7 | Reflex safety | Immediate contraction, veto, fail-closed, trusted scalar φ |
| 8 | Tactical maneuver | Local maneuver candidates: brake, yield, merge, pass, lane change |
| 9 | Strategic planning | Route, mission, ODD, longer-horizon goals |
| 10 | Physics constraint / execution | Actuator limits, friction, stability, projection to safe command |
| 11 | Meta-safety / epistemic | Source standing, uncertainty, novelty, desynchronization, evidence debt |
| 12 | Scenario graduation / validation | Scenario packs, degraded-stack tests, replay, certification obligations |

Canvas 4 is **central but not the whole system** — the witness substrate connecting raw physical traces to semantic, tactical, safety, execution, and validation canvases.

## 4. ARC Sensor Architecture Integration

ARC is integrated as a **sensor-discovery and trace-witness doctrine**, not a fixed sensor bill of materials.

> The vehicle does not begin by asking "camera, radar, or LiDAR?" It asks: **"What synchronized interaction traces are required to make or refuse this decision safely?"**

Each trace:

```json
{
  "trace_id": "trace_...",
  "modality_family": "camera | radar | lidar | acoustic | vibration | tactile | thermal | em | fluid_aero | chemical | proprioceptive | magnetic | learning_meta | other",
  "receptor_geometry": "fiber | strand | surface | array | body_proprioceptive | notional_candidate",
  "physical_time": 0,
  "capture_time": 0,
  "phase": "string",
  "momentum_or_relative_velocity": "string or numeric",
  "latency": "string or numeric",
  "slippage": "none | suspected | confirmed",
  "continuity_status": "continuous | rupture_candidate | ruptured",
  "sync_integrity": 0.0,
  "information_appetite_question": "What decision uncertainty does this trace reduce?",
  "source_standing": "established | proposed | projected | notional | open",
  "provenance": []
}
```

Sensor families remain **discovery candidates** until validated. A modality graduates only if it materially improves decision quality, timeliness, degradation robustness, sensor economy, synchronization integrity, or safety-case evidence. Sensors are bought by **decision value**, not by habit or camera sovereignty.

## 5. F1–F26 Autonomous-Driving Mapping

See [`src/data/eventFabric.js`](./src/data/eventFabric.js) `F_MAP` and the Event Fabric view for the full table. F1 trust/reputation → … → **F26 final integration discipline** (the capstone that makes F1–F25 operational, inspectable, importable, replayable, and certifiable).

## 6. Event-Fabric Schema

Strict JSON plus Markdown companion. Top-level package keys: `schema_version, package_id, package_title, source_standing_policy, timeline, canvases, nodes, edges, events, witnesses, latency_records, race_risks, f1_f26_matrix, validation_tests, source_ledger, export_manifest, unresolved_claims`. Full event/node/edge field lists and validation rules are encoded in `eventFabric.js` (`SCHEMA`) and shown in the view.

**Validation rules (load-bearing):** single-tenant `(canvas_id, cell_id, timeline_tick)`; every safety-bearing event carries validity window + vector clock + source standing + provenance + replay hash; safety paths use **meet/intersection, not union/averaging**; **rate clamp last**; deadline miss / stale evidence / corrupted data / unresolved contradiction / missing warrant **contracts authority or fails closed**; **projected/notional/open claims cannot expand runtime authority**; checkpoints replay into identical canvas projections.

## 7. Build-Time vs Runtime Agent Separation

| Layer | Allowed | Forbidden | Output |
|---|---|---|---|
| Build-time enterprise LLM / agents | Generate schemas, scenario packs, null sets, falsifiers, witness dictionaries, validation plans, manuals, source ledgers | Direct runtime actuation or uncertified authority expansion | Strict JSON, Markdown docs, validation evidence |
| Runtime vehicle agents | Match certified witnesses, compute residuals, maintain vector clocks, contract envelopes, request veto, log evidence | Invent new action authority, treat prose as policy, expand envelope from online learning | `witness.accepted`, `envelope.contracted`, `veto.asserted`, `fallback.invoked` |

Powerful LLMs belong in the build-time/certification loop. Runtime agents must be small, bounded, replayable, and authority-limited.

---

## Latency and Race Investigation

The communication bundle's latency-like records were **parsed**, not accepted on the "144 unique latencies" claim.

| Metric | Count |
|---|---|
| Total latency-like records | 248 |
| Exact `Latency:` records | 237 |
| Unique raw entries (all latency-like) | 240 |
| Unique raw entries (exact `Latency:`) | 229 |
| Unique numeric endpoints (exact) | 84 |
| Exact numeric values (exact) | 68 |

**Conclusion:** the "144 unique latencies" claim is **not confirmed** under any raw-entry, numeric-value, exact-record, or label-based counting convention tested. It may come from a separate manual grouping convention not recoverable from the parsed records alone. The full deliverable includes **248 individual latency cards (Appendix A)** — represented here only by these summary counts (honest stub; not yet itemized).

### Latency Regimes

| Regime | Vehicle interpretation | Safety rule |
|---|---|---|
| Sub-5 ns claims | Projected event-gate/check/comparator budgets | Not safety-case evidence unless measured on target hardware |
| 5–50 ns claims | Fast event-fabric operations | Include in critical-path budget with deadline expiry |
| 50 ns – 1 µs | Micro-critical reasoning/fabric path | Requires bounded queueing and measured platform behavior |
| Variable/range/network | Consensus, transport, dispatch, quorum, agent processing | Never required for reflex refusal |
| Millisecond–second | WAL, persistence, crash recovery, replication | Replay and continuity only, not collision avoidance |
| Async monitoring | Audit and observability | Off safety path unless separately bounded |

### Race-Condition Risks

Timestamp/capture/inference disorder · stale witness use · quorum-after-deadline · semantic update overwriting a sensor-health warning · contradiction averaged away instead of preserved · fallback/veto racing the tactical plan · WAL/checkpoint lag · shared-resource contention · duplicate step IDs from the communication bundle · projected/notional claims migrating into runtime authority.

**Mitigations:** vector clocks, validity windows, single-tenant cells, typed causal edges, idempotent events, compare-and-swap for active envelopes, priority ceilings, critical-path analysis, fail-closed guards, WAL/checkpoint replay, PMP-style scheduling of critical sections.

---

## Canvas Upgrade Specification

The canvas becomes an autonomous-driving state substrate, causal scheduler, and event-stream validator:

1. **Timeline spine** — one physical timeline with capture, digitization, inference, fusion, reflex, planning, actuation, memory, validation lanes.
2. **Twelve canvas tabs/layers** — each with its own node types, sharing one timeline and event fabric.
3. **Append-only event stream** — canvas state is a projection from events, not manually edited diagram state.
4. **Typed nodes and typed edges** — edges are contracts (causality, contradiction, support, synchronization, contraction, validation, recovery).
5. **Single-tenant cells** — one occupant per `(canvas, cell, tick)`; import conflicts rejected.
6. **Checkpoint/replay** — every checkpoint reconstructable from event log + package data; no hidden action-relevant state.
7. **Import/export schema** — strict JSON with validation report, migration notes, source-standing checks, replay hashes.
8. **Latency/race workbench** — raw records, normalized groups, critical paths, resource contention, vector-clock conflicts.
9. **Scenario graduation view** — full-stack, minimum-stack, degraded-stack, latency-stressed, replay, adversarial, regression tests.
10. **Source-standing/provenance view** — every claim established/proposed/projected/notional/open with explicit authority impact.

---

## HTML Application Specification (separate standalone tool)

A vehicle event-fabric workbench (not a research-lab clone):

| Screen | Purpose |
|---|---|
| 12-canvas navigator | Switch between timeline and canvas layers; cross-highlight causal evidence |
| Event-fabric inspector | Inspect synchronized traces, witnesses, contradictions, vector clocks, source standing |
| ARC sensor-discovery board | Track candidate sensor families, information appetite, validation status |
| F1–F26 matrix | Verify communication-bundle coverage as autonomous-driving primitives |
| Latency reconciliation dashboard | Preserve raw latency records, group regimes, expose unresolved counts |
| Race-condition workbench | Model critical sections, shared resources, vector-clock races, fail-closed behavior |
| Validation checklist | Run package checks and scenario graduation checks |
| Source-standing ledger | Separate established/proposed/projected/notional/open claims |
| JSON import/export | Strict package import/export with blocked-import diagnostics |
| Manual view | Embedded operator manual |
| Sample seed package | Black-ice/fog/hydroplaning/desync package for testing |

**Recommended seed package:** black-ice proof slice; fog/spray degraded-visibility slice; hydroplaning/friction-change slice; sensor-desynchronization slice; one contradiction preserved rather than averaged; one envelope contraction; one veto/fail-closed event; one scenario graduation test.

> Note: this standalone HTML workbench is a **separate deliverable**. This repo integrates the F26 plan into the existing React visualization (the **Event Fabric (F26)** view) rather than building the standalone tool.

---

## Operator Manual Outline

**What the tool is.** An autonomous-driving evidence-fabric editor and validator. It shows how physical traces become synchronized witnesses, how witnesses update the twelve canvases, and how safety authority contracts, defers, or fails closed.

**How to import a package.** Open → Import JSON → review schema validation → source-standing validation → single-tenant cell validation → vector-clock and replay-hash validation. Fix blocked imports at the package source; **do not silently patch safety-bearing data**.

**How to inspect the twelve canvases.** Start from the timeline spine; select a physical event; walk outward: world phenomenon → receiver body → raw wave/analog → synchronized event fabric → semantic/intent → reflex/tactical/strategic → physics execution → meta-safety → scenario validation. A semantic object without raw-wave evidence, synchronization integrity, provenance, and source standing is **non-authoritative**.

**How to evaluate latency/race risks.** Separate individual records, cumulative totals, variable/range records, group summaries, projected vs measured hardware timing. Test stale witness, quorum-after-deadline, contention, fallback/veto conflict, duplicate-ID overwrite, source-standing migration.

**How to validate F26 coverage.** Each F1–F26 row must have an event primitive, schema fields, validation tests, source-standing rule, operator instruction, replay example, and import/export coverage. F26 is complete only when the package is usable by an engineer, validator, auditor, and runtime safety reviewer.

**How to export/revise/reimport.** Export strict JSON + Markdown companion; revise in version control or with a strict LLM prompt; reimport and require clean validation. The UI never silently repairs safety-bearing defects.

**Source standing → runtime authority.**

| Standing | Meaning | Runtime authority |
|---|---|---|
| Established | Measured, standard, or otherwise well-supported | May support safety case |
| Proposed | Architecture/research proposal with plausible basis | Requires validation pack |
| Projected | Performance/design target | Cannot expand authority |
| Notional | Conceptual hardware/algorithm/speculative design | Cannot expand authority |
| Open | Known unresolved obligation | Evidence debt only |

---

## Enterprise LLM Package-Generation Prompt

> Reproduced for completeness; the runtime/build-time authority split above governs its use.

```
You are generating an autonomous-driving event-fabric package for the F26 Autonomous-Driving Event-Fabric Integration Plan.

Return exactly two artifacts in this order:
1. STRICT JSON in a single fenced ```json block.
2. Markdown companion documentation in a single fenced ```markdown block.
Do not return prose outside those two blocks.

Domain rules:
- This is an autonomous-driving / EPU / ARC sensory / event-fabric package, not a generic multi-agent research lab.
- Use one real timeline spine plus twelve operational canvases: world/phenomenon, receiver body, raw wave/analog, synchronized physical event fabric, semantic scene, agent-intent/social dynamics, reflex safety, tactical maneuver, strategic planning, physics constraint/execution, meta-safety/epistemic, scenario graduation/validation.
- Semantic vision is one canvas only.
- The base sensing unit is a synchronized interaction trace, not a pixel/object.
- Include ARC fields: modality family, receptor geometry, phase, momentum/relative velocity, latency, slippage, continuity rupture, synchronization integrity, and information appetite.
- Treat sensor families as candidates until validation proves their decision contribution.
- Safety-bearing paths use one-sided safe-rounded φ, meet/intersection only, rate clamp last, and fail-closed veto.
- Projected, notional, and open claims may not expand runtime authority.
- Runtime agents may match certified witnesses, contract envelopes, request veto, and log evidence. They may not invent new action authority.
- Build-time LLM/agents may generate schemas, witnesses, tests, manuals, falsifiers, and validation packs.

JSON requirements:
- Top-level keys: schema_version, package_id, package_title, source_standing_policy, timeline, canvases, nodes, edges, events, witnesses, latency_records, race_risks, f1_f26_matrix, validation_tests, source_ledger, export_manifest, unresolved_claims.
- Every event must include: event_id, event_type, canvas_id, node_refs, physical_time, capture_time, digitization_time, inference_time, valid_from, valid_until, latency_budget_ms, latency_observed_ms, phase_or_regime, sync_confidence, vector_clock, causal_predecessors, modality_refs, witness_id, source_standing, authority_effect, risk_delta, envelope_delta, provenance, replay_hash.
- Every node must include: node_id, canvas_id, node_type, label, cell_id, timeline_tick, source_standing, provenance, validation_status.
- Every edge must include: edge_id, edge_type, from_node, to_node, contract, source_standing, causal_direction, validation_status.
- Every latency record must preserve raw_text, normalized_value_or_range, class, group, critical_path_impact, source_standing, and mitigation.
- Every race risk must include resource, competing_events, vector_clock_relation, failure_mode, fail_closed_behavior, mitigation, validation_test.
- F1-F26 matrix must contain all rows F1 through F26 with autonomous-driving mapping and validation obligation.
- Source ledger entries must use exactly one of: established, proposed, projected, notional, open.

Validation requirements:
- Include at least one black-ice, fog, hydroplaning, or sensor-desynchronization scenario.
- Include at least one contradiction that is preserved rather than averaged.
- Include at least one envelope contraction and one veto/fail-closed case.
- Include scenario graduation tests: full-stack, minimum-stack, degraded-stack, latency-stressed, and replay.
- Include import validation notes for single-tenant cells, typed edges, vector clocks, source standing, and replay hashes.

Markdown companion requirements:
- Explain the package thesis.
- Explain each canvas briefly.
- Explain ARC sensor-discovery choices and what remains candidate/open.
- Explain latency/race risks and mitigations.
- Explain F26 coverage.
- List unresolved claims and what evidence would close them.
```

---

## Open Questions / Unresolved Claims

| Claim / issue | Status | Required closure |
|---|---|---|
| "144 unique latencies" | Not confirmed by parse | Canonical counting convention and reconciled ledger |
| EPU/ASIC timing beyond FPGA guard/veto | Projected/notional | Target-hardware measurement |
| 512 event-register budget | Notional/source-specific | Register allocation proof on actual hardware |
| One-sided φ | Proposed safety primitive | Proof that φ never understates risk on certified domain |
| φ liveness | Open | Frontier where refusal remains safe but not permanently frozen |
| Non-box actuator meet/projection | Open | Formal projection proof and hardware-in-loop validation |
| Topological phase labels | Proposed | Validation across black ice, fog, hydroplaning, desync, ODD transitions |
| Conservation-renormalization | Proposed | Evidence that zero-sum gain budget preserves relevant conserved quantities in vehicle data |
| ARC final sensor families | Open | Information-appetite and scenario-graduation experiments |
| Runtime online adaptation | High-risk proposed mechanism | Quarantine protocol and certified update path |
| LLM-generated packages | Useful but not authoritative | Strict schema validation, replay, source-standing audit, human/automated review |

---

*The full Markdown deliverable also includes all 248 latency cards (Appendix A), itemized. Those are represented here by their parsed summary counts (248 total / 237 exact / 240 unique-raw / 229 unique-raw-exact / 84 unique-numeric-endpoints / 68 exact-numeric-values) per the agreed honest-stub scope; itemizing them remains an open obligation.*
