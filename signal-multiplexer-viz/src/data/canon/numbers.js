/**
 * canon/numbers.js — mandatory number cards.
 * (EPU Companion, Numeric Ledger 3/3: "The remedy is not to correct each figure
 * by hand. It is to make an uncorrected figure impossible to release.")
 *
 * Card schema (companion-mandated):
 *   number_id, quantity, value, units,
 *   kind: measured | derived | simulated | target | constructed,
 *   configuration_id, method, uncertainty, scope,
 *   inputs[] (number_ids), derive(deps) — recomputable in the validator,
 *   owner, review_date, supersedes/superseded_by, permitted_release_context.
 *
 * `derive` is a pure function of {number_id: value}; the CanonValidator
 * recomputes every derived card from its stated inputs and fails release on
 * mismatch (Definition of Done item 4).
 */

export const NUMBERS = [
  // ---- measured (exactly one; cited upstream) ----
  {
    number_id: 'num.veto_latency_ns',
    quantity: 'Analog-veto witness latency',
    value: 32, units: 'ns', kind: 'measured',
    configuration_id: 'cfg-fpga-veto-upstream',
    method: 'FPGA witness measurement (upstream; cited)',
    uncertainty: 'approximate ("~32 ns"); upstream does not state an interval',
    scope: 'The ONLY measured latency in the corpus. Electrically-isolated guard path.',
    inputs: [], derive: null,
    owner: 'upstream Memristive-Substrate reference',
    review_date: '2026-07-28',
    permitted_release_context: 'Always with "the only measured latency" qualifier.'
  },

  // ---- targets (budgets; never achieved) ----
  {
    number_id: 'num.epu_gate_ns',
    quantity: 'EPU accept/reject gate budget',
    value: 3, units: 'ns', kind: 'target',
    configuration_id: 'cfg-none',
    method: 'design budget', uncertainty: 'n/a (target)',
    scope: 'Projected EPU silicon; no measurement exists.',
    inputs: [], derive: null,
    owner: 'hardware architecture (open)',
    review_date: '2026-07-28',
    permitted_release_context: 'Only as target/budget; never beside measured figures without the kind stated.'
  },
  {
    number_id: 'num.witness_bitvector_ns',
    quantity: 'Witness bitvector evaluation budget',
    value: 8, units: 'ns', kind: 'target',
    configuration_id: 'cfg-none',
    method: 'design budget', uncertainty: 'n/a (target)',
    scope: 'Projected EPU silicon.',
    inputs: [], derive: null,
    owner: 'hardware architecture (open)',
    review_date: '2026-07-28',
    permitted_release_context: 'Target only.'
  },
  {
    number_id: 'num.gap_effort_pd_lo',
    quantity: 'Coverage-gap closure effort (lower bound)',
    value: 55, units: 'person-days', kind: 'target',
    configuration_id: 'cfg-none',
    method: 'planning estimate over the 11 GAP subcategories',
    uncertainty: 'range with num.gap_effort_pd_hi',
    scope: 'Program Coverage Map: source-material acquisition, not silicon.',
    inputs: [], derive: null,
    owner: 'program planning',
    review_date: '2026-07-28',
    permitted_release_context: 'As the 55–80 range only.'
  },
  {
    number_id: 'num.gap_effort_pd_hi',
    quantity: 'Coverage-gap closure effort (upper bound)',
    value: 80, units: 'person-days', kind: 'target',
    configuration_id: 'cfg-none',
    method: 'planning estimate', uncertainty: 'range with num.gap_effort_pd_lo',
    scope: 'Program Coverage Map.',
    inputs: [], derive: null,
    owner: 'program planning',
    review_date: '2026-07-28',
    permitted_release_context: 'As the 55–80 range only.'
  },

  // ---- constructed (true by architecture/definition) ----
  {
    number_id: 'num.tau_boundary_s',
    quantity: 'Analog/digital residence boundary τ',
    value: 5, units: 's', kind: 'constructed',
    configuration_id: 'cfg-sim-harness',
    method: 'ratified ADR (Numerical Substrate Partition) — a governance commitment, not a measurement',
    uncertainty: 'exact by definition',
    scope: 'No analog state crosses τ = 5 s without re-quantization.',
    inputs: [], derive: null,
    owner: 'architecture decision record',
    review_date: '2026-07-28',
    permitted_release_context: 'Always as a ratified decision, never as an empirical finding.'
  },
  {
    number_id: 'num.tick_ms',
    quantity: 'Simulation tick interval',
    value: 100, units: 'ms', kind: 'constructed',
    configuration_id: 'cfg-sim-harness',
    method: 'implementation constant (setInterval)',
    uncertainty: 'browser timer jitter, unquantified',
    scope: 'Wall-clock pacing of the simulated loop; simulated dt is 0.1 s.',
    inputs: [], derive: null,
    owner: 'this repo',
    review_date: '2026-07-28',
    permitted_release_context: 'Any.'
  },

  // ---- simulated (measured inside the harness) ----
  {
    number_id: 'num.monotonicity_violation_rate',
    quantity: 'Antitone monotonicity violation rate under conservative discipline',
    value: 0, units: '%', kind: 'simulated',
    configuration_id: 'cfg-sim-harness',
    method: 'MonotonicityMonitor counter over live runs with discipline ON',
    uncertainty: 'exact for observed runs; not a proof over all runs',
    scope: 'IN-SIM ONLY. Says nothing about real memristor non-ideality (that obligation is open).',
    inputs: [], derive: null,
    owner: 'this repo',
    review_date: '2026-07-28',
    permitted_release_context: 'Always with the in-sim scope stated.'
  },

  // ---- derived (recomputed by the validator from stated inputs) ----
  {
    number_id: 'num.latency_records_total',
    quantity: 'F26 bundle latency-like records (parse)',
    value: 248, units: 'records', kind: 'derived',
    configuration_id: 'cfg-sim-harness',
    method: 'mechanical parse of the communication bundle (F26 plan, Latency Investigation)',
    uncertainty: 'exact for the parse convention used',
    scope: 'Basis of the "144 unique latencies NOT CONFIRMED" finding.',
    inputs: ['num.latency_records_exact', 'num.latency_records_nonexact'],
    derive: (d) => d['num.latency_records_exact'] + d['num.latency_records_nonexact'],
    owner: 'F26 integration',
    review_date: '2026-07-28',
    permitted_release_context: 'With the parse convention named.'
  },
  {
    number_id: 'num.latency_records_exact',
    quantity: 'Exact "Latency:" records in the parse',
    value: 237, units: 'records', kind: 'derived',
    configuration_id: 'cfg-sim-harness',
    method: 'mechanical parse (class = exact)',
    uncertainty: 'exact for the convention',
    scope: 'F26 latency reconciliation.',
    inputs: [], derive: null,
    owner: 'F26 integration',
    review_date: '2026-07-28',
    permitted_release_context: 'With convention named.'
  },
  {
    number_id: 'num.latency_records_nonexact',
    quantity: 'Variable/approximate/range/bound latency records',
    value: 11, units: 'records', kind: 'derived',
    configuration_id: 'cfg-sim-harness',
    method: 'mechanical parse (class ≠ exact); 248 total − 237 exact',
    uncertainty: 'exact for the convention',
    scope: 'F26 latency reconciliation.',
    inputs: [], derive: null,
    owner: 'F26 integration',
    review_date: '2026-07-28',
    permitted_release_context: 'With convention named.'
  },
  {
    number_id: 'num.gap_count',
    quantity: 'GAP-coverage subcategories',
    value: 11, units: 'subcategories', kind: 'derived',
    configuration_id: 'cfg-sim-harness',
    method: 'count of SUBCATEGORIES entries with coverage === "GAP" in src/data/programCoverage.js',
    uncertainty: 'exact',
    scope: 'Program Coverage Map.',
    inputs: ['num.coverage_full', 'num.coverage_high', 'num.coverage_partial', 'num.coverage_total'],
    derive: (d) => d['num.coverage_total'] - d['num.coverage_full'] - d['num.coverage_high'] - d['num.coverage_partial'],
    owner: 'this repo',
    review_date: '2026-07-28',
    permitted_release_context: 'Any.'
  },
  { number_id: 'num.coverage_full', quantity: 'FULL-coverage subcategories', value: 15, units: 'subcategories', kind: 'derived', configuration_id: 'cfg-sim-harness', method: 'count over programCoverage.js', uncertainty: 'exact', scope: 'Coverage map.', inputs: [], derive: null, owner: 'this repo', review_date: '2026-07-28', permitted_release_context: 'Any.' },
  { number_id: 'num.coverage_high', quantity: 'HIGH-coverage subcategories', value: 20, units: 'subcategories', kind: 'derived', configuration_id: 'cfg-sim-harness', method: 'count over programCoverage.js', uncertainty: 'exact', scope: 'Coverage map.', inputs: [], derive: null, owner: 'this repo', review_date: '2026-07-28', permitted_release_context: 'Any.' },
  { number_id: 'num.coverage_partial', quantity: 'PARTIAL-coverage subcategories', value: 10, units: 'subcategories', kind: 'derived', configuration_id: 'cfg-sim-harness', method: 'count over programCoverage.js', uncertainty: 'exact', scope: 'Coverage map.', inputs: [], derive: null, owner: 'this repo', review_date: '2026-07-28', permitted_release_context: 'Any.' },
  { number_id: 'num.coverage_total', quantity: 'Total research subcategories', value: 56, units: 'subcategories', kind: 'derived', configuration_id: 'cfg-sim-harness', method: 'SUBCATEGORIES.length in programCoverage.js', uncertainty: 'exact', scope: 'Coverage map.', inputs: [], derive: null, owner: 'this repo', review_date: '2026-07-28', permitted_release_context: 'Any.' },

  // ---- the unconfirmed claim, carried as a card so it cannot be laundered ----
  {
    number_id: 'num.claim_144_unique_latencies',
    quantity: '"144 unique latencies" (upstream claim)',
    value: 144, units: 'latencies', kind: 'target',
    configuration_id: 'cfg-none',
    method: 'UNKNOWN — not reproducible under any tested counting convention (raw-entry 240/229, numeric-endpoint 84, exact-value 68)',
    uncertainty: 'unconfirmed',
    scope: 'F26 latency reconciliation. Release as NOT CONFIRMED only.',
    inputs: [], derive: null,
    owner: 'upstream bundle (open obligation: canonical counting convention)',
    review_date: '2026-07-28',
    permitted_release_context: 'ONLY with the NOT CONFIRMED marker.'
  }
];
