/**
 * canon/claims.js — consequential-claim register.
 * (Definition of Done item 1: every consequential claim has one controlling
 * source, a standing, a scope, an owner, a falsifier and an artifact id.)
 */

export const CLAIMS = [
  {
    claim_id: 'clm.antitone',
    statement: 'As risk rises the admissible action set only contracts: R1 ≤ R2 ⇒ U_ad(R2) ⊆ U_ad(R1).',
    controlling_source: 'Ten Articles / Memristive-Substrate reference §1 (antitone authority)',
    standing: 'constructed',
    scope: 'Enforced by construction in the simulation (prefix-complement admissible set). In-sim violation rate is a separate simulated number (num.monotonicity_violation_rate). Real-device behaviour is an open obligation.',
    owner: 'this repo (sim); PoC Workstream A (device)',
    falsifier: 'Any tick where risk rose and admittedCount grew with conservative discipline ON.',
    artifact: 'src/simulation/BoundedAutonomyStack.js#_computeAdmissibleSet/_checkMonotonicity'
  },
  {
    claim_id: 'clm.crl_conservation',
    statement: 'The CRL keeps the weighted log-gain budget at Q = 0 while redistributing loudness; the conserved coordinate reads only the gauge-fixed shape.',
    controlling_source: 'Conservation-Renormalization source §3 (external PDF, cited not shipped)',
    standing: 'constructed',
    scope: 'Q = 0 after projection and shape-invariance are definitional identities of the implemented operator — design semantics, not evidence.',
    owner: 'this repo',
    falsifier: 'A rounding fault: |Q| or drift exceeding tol_conserve (diagnostic, not verification).',
    artifact: 'src/simulation/ConservationRenormalizationLayer.js#step'
  },
  {
    claim_id: 'clm.masking_blocked',
    statement: '§3.4(ii): a compensating gain cannot mask a genuine graded defect from the gauge-fixed shape band.',
    controlling_source: 'Conservation-Renormalization source §3.4',
    standing: 'established',
    scope: 'IN-SIM: adversarial masking probe passes live each tick and fails when sabotaged (raw band). Not field/hardware evidence.',
    owner: 'this repo',
    falsifier: 'maskingProbe() returning masked=true on the honest shape band; or noOpAudit() classifying the probe as guaranteed-pass.',
    artifact: 'src/simulation/ConservationRenormalizationLayer.js#maskingProbe/noOpAudit'
  },
  {
    claim_id: 'clm.xi_tracks_risk',
    statement: 'xi_feature correlates monotonically with physical risk in automotive perception.',
    controlling_source: 'φ-compiler transfer discussion (gasification bin-level validation only)',
    standing: 'proposed',
    scope: 'Validated at bin level on a gasification corpus ONLY. In this sim xi_feature is driven from risk.R — a modelling choice that must not be cited as evidence. phi_risk requires a one-sided calibration that does not exist.',
    owner: 'Conservation-Manifold Compiler team (open)',
    falsifier: 'Cross-domain study with correlation r ≤ 0.7 on an automotive corpus.',
    artifact: 'src/simulation/BoundedAutonomyStack.js#_runCompiler (driven, not evidential)'
  },
  {
    claim_id: 'clm.correctability',
    statement: 'The governance layer keeps the system correctable: challenges demote canon, silent drift is forced to surface, rollback restores prior epistemic state, erasure leaves lineage.',
    controlling_source: 'Temporal State Management Part VI (external PDF, cited not shipped)',
    standing: 'established',
    scope: 'IN-SIM: demonstrated in a real browser (challenge → Warranted→Provisional; drift surfaced; rollback restores canon). Governs the simulation, not a vehicle.',
    owner: 'this repo',
    falsifier: 'A truth-status change that produces no constitutional event in the Archive; or a rollback that fails to restore the snapshot canon.',
    artifact: 'src/simulation/ConstitutionalTruthEngine.js'
  },
  {
    claim_id: 'clm.veto_only_measured',
    statement: 'The ~32 ns analog-veto witness is the only measured latency in the corpus; every other timing figure is a target.',
    controlling_source: 'Memristive-Substrate reference §2.2/§7',
    standing: 'established',
    scope: 'Cited upstream measurement (cfg-fpga-veto-upstream). This repo performs no timing measurement.',
    owner: 'upstream reference',
    falsifier: 'A second measured latency with a configuration artifact; or retraction of the upstream measurement.',
    artifact: 'canon/numbers.js#num.veto_latency_ns'
  },
  {
    claim_id: 'clm.f26_latency_unconfirmed',
    statement: 'The "144 unique latencies" claim is not confirmed under any tested counting convention (248 records / 237 exact / 240 & 229 unique raw / 84 endpoints / 68 exact values).',
    controlling_source: 'F26 plan, Latency and Race Investigation (mechanical parse)',
    standing: 'established',
    scope: 'Holds for the parse conventions tested; a canonical upstream convention could still reconcile it (open).',
    owner: 'F26 integration',
    falsifier: 'An upstream counting convention that reproduces 144 from the raw records.',
    artifact: 'F26_Autonomous_Driving_Event_Fabric_Plan.md#latency-and-race-investigation'
  },
  {
    claim_id: 'clm.coverage_is_sources',
    statement: 'Program coverage (15/20/10/11 over 56) measures source-material availability in the design corpus — never fabricated or measured silicon.',
    controlling_source: 'GHOST Research Subcategory → Document Section Mapping (Feb 2026)',
    standing: 'constructed',
    scope: 'A property of the mapping\'s definition of "coverage".',
    owner: 'this repo',
    falsifier: 'n/a for the definition; the counts themselves are derived numbers recomputed by the validator.',
    artifact: 'src/data/programCoverage.js'
  }
];
