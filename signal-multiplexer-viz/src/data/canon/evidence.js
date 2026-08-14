/**
 * canon/evidence.js — Lemma Composition and Introduction-Order Formalism.
 *
 * "The order in which evidence enters the claim may change the claim."
 *
 * A claim Φ is assembled from a planning-instance-like tuple
 *   P = ⟨C, G, π, D, S, Φ⟩
 * where C is the component inventory, G the grouping structure, π the
 * introduction order, D the dependency/override relation, S the scope map,
 * and Φ the resulting claim. π is semantically active exactly when it changes
 * scope, authority, or state effect — i.e. when D is left implicit and a
 * reader/system must infer an override from POSITION rather than from a
 * stated relation.
 *
 * Formalization used here (see EvidenceCompositionEngine.js for the algebra):
 *  - Each component targets a scope and either ASSIGNS a label to it (a
 *    positive local claim) or OVERRIDES it (an authority-bearing restriction).
 *  - The admissible ladder is ['proceed','caution','stop'], most permissive
 *    first; composition is `meet` = most-restrictive-wins, which is
 *    commutative/associative BY CONSTRUCTION — so the full-evidence result
 *    Φ_full is order-invariant with standing `constructed` (true by
 *    definition, not something to "verify" — see the §3.4 no-op lesson this
 *    canon already encodes for ConservationRenormalizationLayer).
 *  - The FALSIFIABLE content is prefix safety: does every PARTIAL reading
 *    (components 1..k in the declared order) already forbid what the full
 *    evidence forbids? A component that ASSIGNS 'proceed' unconditionally,
 *    when a later component will override it, creates an unsafe window — the
 *    officer/light hazard. This is the antitone law applied to evidence
 *    introduction instead of to risk: partial evidence must never license
 *    more than complete evidence would (echoes F26's F22, anytime algorithms:
 *    partial certificates under a deadline may contract, never expand).
 *  - Formation discipline (declaring `dependsOn`) fixes the hazard WITHOUT
 *    reordering: a component that names its own open dependency contributes
 *    'caution', not 'proceed', until that dependency is resolvable. This is
 *    the mechanical content of "Formation control → Semantic control →
 *    Proposer-quality control": lock scope+dependency first; only THEN may
 *    presentation order be freely optimized for clarity.
 *
 * Instances below are exactly three, each with a stated `intent` the
 * validator checks against (CanonValidator C10):
 *   'demonstration-unsafe' — MUST exhibit ≥1 unsafe prefix (proves the hazard
 *                            is real); a demo that stops failing is pedagogical
 *                            drift, flagged the same as a broken proof.
 *   'well-formed'          — MUST exhibit zero unsafe prefixes (proves the
 *                            Formation fix actually works, not merely asserted).
 */

export const THESIS = {
  quote: 'The order in which evidence enters the claim may change the claim.',
  tuple: 'P = ⟨C, G, π, D, S, Φ⟩ — components, grouping, introduction order, dependency relation, scope map, resulting claim.',
  layering: 'Formation control → Semantic control → Proposer-quality control',
  rule: 'No quality score can compensate for a semantically altered safety claim.',
  origin: 'Opening challenge: which evidence components are independent and exchangeable, and which must be introduced before others because they determine their scope or authority?'
};

// Most permissive first. Operationally: 'proceed' = act; 'caution' = hold,
// gather more evidence; 'stop' = actively restrict. Only 'proceed' licenses
// action, so it is the only label whose PREMATURE assertion is unsafe.
export const ADMISSIBLE_LADDER = ['proceed', 'caution', 'stop'];
export const RANK = { proceed: 0, caution: 1, stop: 2 };

export const EVIDENCE_SETS = [
  {
    id: 'ev.lane_entry_naive',
    title: '"The lane is safe to enter" — bare assertion, override introduced late',
    intent: 'demonstration-unsafe',
    queryScope: 'scope.signal',
    components: [
      { id: 'c.light_green', statement: 'The light is green.', scope: 'scope.signal', assigns: 'proceed' },
      { id: 'c.pavement_dry', statement: 'The pavement is dry.', scope: 'scope.traction', assigns: 'proceed' },
      { id: 'c.officer_stop', statement: 'The officer commands stop.', scope: 'scope.signal', overrides: [{ scope: 'scope.signal', to: 'stop' }] }
    ],
    order: ['c.light_green', 'c.pavement_dry', 'c.officer_stop'],
    note: 'This is the literal example from the opening challenge: "the light is green; therefore proceed" is read, and licenses proceeding, BEFORE the officer\'s override is known.'
  },
  {
    id: 'ev.lane_entry_well_formed',
    title: '"The lane is safe to enter" — Formation-qualified: same facts, same order, no hazard',
    intent: 'well-formed',
    queryScope: 'scope.signal',
    components: [
      { id: 'c.light_green_q', statement: 'The light is green — subject to the officer.', scope: 'scope.signal', assigns: 'proceed', dependsOn: ['scope.signal'] },
      { id: 'c.officer_stop2', statement: 'The officer commands stop.', scope: 'scope.signal', overrides: [{ scope: 'scope.signal', to: 'stop' }] }
    ],
    order: ['c.light_green_q', 'c.officer_stop2'],
    note: 'Same override, same order (officer still introduced second). The fix is NOT reordering — it is Formation: the light component declares its own dependency on scope.signal, so it contributes only \'caution\' until that dependency is known to be clear. The unsafe window closes without touching π.'
  },
  {
    id: 'ev.stack_refusal',
    title: 'Bounded Autonomy Stack — sensor claim vs. the analog veto, narrated',
    intent: 'demonstration-unsafe',
    queryScope: 'scope.actuation',
    components: [
      { id: 'c.sensor_clear', statement: 'Lane sensors report clear.', scope: 'scope.actuation', assigns: 'proceed' },
      { id: 'c.analog_veto', statement: 'Electrically-isolated analog veto asserted.', scope: 'scope.actuation', overrides: [{ scope: 'scope.actuation', to: 'stop' }], crossRef: 'gate.analog_veto' }
    ],
    order: ['c.sensor_clear', 'c.analog_veto'],
    note: 'The same hazard exists in the runtime domain: "sensors clear" alone would license proceed. This is exactly why gate.analog_veto (canon/gates.js) is never narrated — it is a standing, always-recomputed override, not a fact mentioned once. This instance shows the analytical tool correctly reproduces the reason that design choice is load-bearing.'
  }
];
