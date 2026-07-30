/**
 * CanonValidator.js — mechanical enforcement of the Definition of Done.
 * (EPU Companion, Master Index 3/3.) "The remedy is not to correct each figure
 * by hand. It is to make an uncorrected figure impossible to release."
 *
 * Pure and deterministic: no React, no timers, no randomness. Reads the canon
 * registers plus live repo data and returns findings. A `blocking` finding
 * makes the layer non-releasable.
 *
 * Checks (numbered against the Definition of Done):
 *   C1  claim completeness  (source, standing, scope, owner, falsifier, artifact)
 *   C2  gate completeness   (typed interface, invariant, threshold+provenance,
 *                            failure semantics, reason code, test vector)
 *   C4  number integrity    (kind+config+uncertainty present; derived cards
 *                            recompute from stated inputs; repo-count cards
 *                            cross-checked against the live data modules)
 *   C5  symbol resolution   (every colliding glyph has canonical replacements
 *                            and a stated in-repo resolution)
 *   C6  traceability        (hazard→mechanism and requirement→hazard orphans
 *                            LISTED in both directions, never suppressed)
 *   C7  no-op audit         (every mechanically-checkable gate is run against
 *                            its own violation vector; a gate that cannot fail
 *                            is a guaranteed-pass finding — Deck B ret. #3)
 *   C8  standing discipline (measured needs a real configuration; targets are
 *                            never phrased as achieved; standings are canonical)
 */

import { STANDING_ORDER } from '../data/canon/standing.js';
import { SYMBOLS } from '../data/canon/symbols.js';
import { NUMBERS } from '../data/canon/numbers.js';
import { CONFIGURATIONS } from '../data/canon/configurations.js';
import { CLAIMS } from '../data/canon/claims.js';
import { GATES } from '../data/canon/gates.js';
import { HAZARDS, REQUIREMENTS } from '../data/canon/hazards.js';
import { RETIREMENTS } from '../data/canon/retirements.js';
import { OBLIGATIONS } from '../data/canon/obligations.js';
import { SUBCATEGORIES } from '../data/programCoverage.js';
import { maskingProbe, tolConserve } from './ConservationRenormalizationLayer.js';
import { runPlanningAnalysis } from './PlanningEngine.js';

const finding = (check, severity, subject, message, remedy) =>
  ({ id: `${check}:${subject}`, check, severity, subject, message, remedy });

// ---- C1 — claims ----
function checkClaims(out) {
  const FIELDS = ['statement', 'controlling_source', 'standing', 'scope', 'owner', 'falsifier', 'artifact'];
  for (const c of CLAIMS) {
    for (const f of FIELDS) {
      if (!c[f] || String(c[f]).trim().length < 3) {
        out.push(finding('C1', 'blocking', c.claim_id, `Claim missing ${f}.`, `Fill ${f} in canon/claims.js.`));
      }
    }
    if (c.standing && !STANDING_ORDER.includes(c.standing)) {
      out.push(finding('C8', 'blocking', c.claim_id,
        `Non-canonical standing "${c.standing}".`, `Use one of: ${STANDING_ORDER.join(', ')}.`));
    }
  }
}

// ---- C2 — gates ----
function checkGates(out) {
  for (const g of GATES) {
    if (!g.interface?.inputs || !g.interface?.output)
      out.push(finding('C2', 'blocking', g.gate_id, 'Untyped interface.', 'Declare inputs/output.'));
    if (!g.invariant) out.push(finding('C2', 'blocking', g.gate_id, 'No invariant.', 'State the invariant.'));
    if (!g.threshold?.provenance || g.threshold.provenance.length < 10)
      out.push(finding('C2', 'blocking', g.gate_id,
        'Threshold lacks hazard-derived provenance.', 'Cite the hazard the threshold derives from.'));
    if (!g.failureSemantics) out.push(finding('C2', 'blocking', g.gate_id, 'No failure semantics.', 'State fail-closed behaviour.'));
    if (!g.reasonCode) out.push(finding('C2', 'blocking', g.gate_id, 'No reason code.', 'Assign one.'));
    if (!g.testVector?.input) out.push(finding('C2', 'blocking', g.gate_id, 'No test vector.', 'Provide a violation vector.'));
  }
}

// ---- C4 — numbers ----
function checkNumbers(out) {
  const configIds = new Set(CONFIGURATIONS.map((c) => c.config_id));
  const byId = Object.fromEntries(NUMBERS.map((n) => [n.number_id, n]));

  for (const n of NUMBERS) {
    if (!configIds.has(n.configuration_id))
      out.push(finding('C4', 'blocking', n.number_id,
        `Unknown configuration ${n.configuration_id}.`, 'Add it to canon/configurations.js.'));
    if (!n.uncertainty)
      out.push(finding('C4', 'blocking', n.number_id, 'No uncertainty stated.', 'State an interval, basis, or n/a-with-reason.'));
    if (!['measured', 'derived', 'simulated', 'target', 'constructed'].includes(n.kind))
      out.push(finding('C4', 'blocking', n.number_id, `Unknown kind "${n.kind}".`, 'Label measured|derived|simulated|target|constructed.'));

    // DoD-4: derived numbers RECOMPUTE from their stated inputs.
    if (n.derive) {
      const deps = {};
      let missing = false;
      for (const ref of n.inputs) {
        if (!(ref in byId)) {
          out.push(finding('C4', 'blocking', n.number_id, `Input ${ref} not in the register.`, 'Add the input card.'));
          missing = true;
        } else deps[ref] = byId[ref].value;
      }
      if (!missing) {
        const recomputed = n.derive(deps);
        if (Math.abs(recomputed - n.value) > 1e-9) {
          out.push(finding('C4', 'blocking', n.number_id,
            `Derived value ${n.value} does not recompute (${recomputed}) from its stated inputs.`,
            'Fix the value or the derivation — never release the mismatch.'));
        }
      }
    }
  }

  // Cross-check repo-count cards against the LIVE data module.
  const live = { FULL: 0, HIGH: 0, PARTIAL: 0, GAP: 0 };
  SUBCATEGORIES.forEach((s) => { live[s.coverage] = (live[s.coverage] || 0) + 1; });
  const expect = [
    ['num.coverage_full', live.FULL], ['num.coverage_high', live.HIGH],
    ['num.coverage_partial', live.PARTIAL], ['num.gap_count', live.GAP],
    ['num.coverage_total', SUBCATEGORIES.length]
  ];
  for (const [id, liveVal] of expect) {
    const card = byId[id];
    if (card && card.value !== liveVal) {
      out.push(finding('C4', 'blocking', id,
        `Card says ${card.value} but the live data module counts ${liveVal}.`,
        'The ledger and the rendering diverged — reconcile programCoverage.js and the card.'));
    }
  }
}

// ---- C5 — symbols ----
function checkSymbols(out) {
  for (const s of SYMBOLS) {
    if (!s.canonical || s.canonical.length < s.denotations.length - 1)
      out.push(finding('C5', 'blocking', s.glyph,
        'Colliding glyph without enough canonical replacements.', 'One canonical name per denotation.'));
    if (!s.usedInRepo)
      out.push(finding('C5', 'warning', s.glyph,
        'No statement of how the glyph resolves in this repo.', 'State the in-repo resolution or "not used".'));
  }
}

// ---- C6 — traceability orphans (both directions, listed) ----
function checkTraceability(out) {
  const gateIds = new Set(GATES.map((g) => g.gate_id));
  const reqIds = new Set(REQUIREMENTS.map((r) => r.requirement_id));
  const referencedReqs = new Set();

  for (const h of HAZARDS) {
    if (!h.mechanisms || h.mechanisms.length === 0) {
      out.push(finding('C6', 'warning', h.hazard_id,
        `ORPHAN HAZARD (no mechanism): ${h.description}`,
        'Listed, not suppressed — closing it needs the evidence named in the obligations register.'));
    }
    for (const m of h.mechanisms) {
      if (!gateIds.has(m)) {
        out.push(finding('C6', 'blocking', h.hazard_id,
          `Mechanism ${m} is not a registered gate.`, 'Register the gate or fix the reference.'));
      }
    }
    if (!reqIds.has(h.requirement)) {
      out.push(finding('C6', 'blocking', h.hazard_id,
        `Requirement ${h.requirement} not registered.`, 'Add it to REQUIREMENTS.'));
    } else referencedReqs.add(h.requirement);
  }
  for (const r of REQUIREMENTS) {
    if (!referencedReqs.has(r.requirement_id)) {
      out.push(finding('C6', 'warning', r.requirement_id,
        `ORPHAN REQUIREMENT (no hazard traces to it): ${r.text}`,
        'Listed, not suppressed — attach a hazard or record why it stands alone.'));
    }
  }
}

// ---- C7 — no-op audit: run each gate against its own violation vector ----
// The predicate is the gate CONTRACT (the spec the engine is checked against
// in the browser); a contract that accepts its violation vector cannot fail
// and is a guaranteed-pass finding.
const GATE_PREDICATES = {
  'gate.monotonicity': (i) => (i.R > i.prevR && i.admitted > i.prevAdmitted), // must flag violation
  'gate.analog_veto': (i) => (i.dR > 0.18 || i.R > 0.85),                      // must arm
  'gate.conservation': (i) => !(i.residual <= tolConserve(5)),                 // must reject 1e-6
  'gate.masking_probe': (i) => !maskingProbe(i).pass,                          // sabotaged probe must FAIL
  'gate.llc_quarantine': (i) => (i.llc - i.prevLlc > 0.25),                    // must quarantine
  'gate.requantize': (i) => !(i.residence >= 5 && (i.deltaJ / i.cost) >= 1.0), // must refuse promotion
  'gate.challenge': (i) => (i.strength > i.resistance)                          // must demote
};

function checkNoOps(out) {
  for (const g of GATES) {
    const pred = GATE_PREDICATES[g.gate_id];
    if (!pred) {
      out.push(finding('C7', 'warning', g.gate_id,
        'No mechanical probe registered — falsifiability unverified.', 'Add a predicate to GATE_PREDICATES.'));
      continue;
    }
    const fired = pred(g.testVector.input);
    if (!fired) {
      out.push(finding('C7', 'blocking', g.gate_id,
        'GUARANTEED-PASS: the gate did not fire on its own violation vector — a check that cannot fail has no truth value.',
        'Deck B retirement #3: fix the check or retire it.'));
    }
  }
  // The honest probe must also PASS on its non-adversarial path (both sides falsifiable).
  if (!maskingProbe().pass) {
    out.push(finding('C7', 'blocking', 'gate.masking_probe',
      'The honest masking probe fails — the live §3.4(ii) safety check is broken.', 'Fix maskingProbe/shape band.'));
  }
}

// ---- C8 — standing discipline ----
function checkStanding(out) {
  for (const n of NUMBERS) {
    if (n.kind === 'measured' && n.configuration_id === 'cfg-none')
      out.push(finding('C8', 'blocking', n.number_id,
        'Measured number without a configuration artifact.', 'No number is an engineering claim without one.'));
    if (n.kind === 'target' && /achiev/i.test(`${n.quantity} ${n.scope}`))
      out.push(finding('C8', 'blocking', n.number_id,
        'Target phrased as achieved.', 'A target is never phrased as achieved.'));
  }
  for (const o of OBLIGATIONS) {
    if ((o.status === 'closed' || o.status === 'closed-in-sim') && !o.closure)
      out.push(finding('C8', 'blocking', o.obligation_id,
        'Closed obligation without a closure statement.', 'State what closed it or reopen it.'));
  }
  for (const r of RETIREMENTS) {
    if (!r.reason || !r.disposition)
      out.push(finding('C8', 'blocking', r.retirement_id,
        'Retirement without reason/disposition.', 'Preserve the negative result completely.'));
  }
}

// ---- C9 — planning-module invariants (Rigorous Planning Framework, Part X) ----
// The PlanningResult's own invariants (V4/V11/V12) become canon findings: a
// planning field without a method+grade, a Π that does not equal W1/W∞, or a
// silent-completeness debts register is a blocking release defect.
function checkPlanning(out) {
  let analysis;
  try {
    analysis = runPlanningAnalysis();
  } catch (e) {
    out.push(finding('C9', 'blocking', 'planning-engine',
      `Planning analysis failed to run: ${e.message}.`, 'Fix PlanningEngine/canon/planning.js.'));
    return;
  }
  for (const inv of analysis.invariants) {
    if (!inv.pass) {
      out.push(finding('C9', 'blocking', `planning.${inv.id}`,
        `Planning schema invariant ${inv.id} violated: ${inv.text}.`,
        'A field with no method is inadmissible (Part X §10.1).'));
    }
  }
  if (!analysis.result.structure.value || analysis.result.structure.value.maxScc > 1) {
    out.push(finding('C9', 'warning', 'planning.structure',
      'The obligations digraph contains feedback (non-singleton SCC) — tearing (A6) is required before sequencing.',
      'Select tears within components (Prop. 3.3), each with an assumption and a verifier task.'));
  }
}

/** Run the full validation. Deterministic; safe to call from render handlers. */
export function runCanonValidation() {
  const findings = [];
  checkClaims(findings);
  checkGates(findings);
  checkNumbers(findings);
  checkSymbols(findings);
  checkTraceability(findings);
  checkNoOps(findings);
  checkStanding(findings);
  checkPlanning(findings);

  const counts = {
    blocking: findings.filter((f) => f.severity === 'blocking').length,
    warning: findings.filter((f) => f.severity === 'warning').length,
    note: findings.filter((f) => f.severity === 'note').length
  };

  // Definition-of-Done scorecard (8 rows; row 3 and 7 are structural claims
  // about the app itself, asserted here and observed in the browser).
  const dod = [
    { item: 1, text: 'Claims carry source/standing/scope/owner/falsifier/artifact', pass: !findings.some((f) => f.check === 'C1' && f.severity === 'blocking') },
    { item: 2, text: 'Gates carry typed interface/invariant/provenanced threshold/failure/reason/test vector', pass: !findings.some((f) => f.check === 'C2' && f.severity === 'blocking') },
    { item: 3, text: 'State transitions reconstructable from ordered events (Archive + snapshots; engines expose getState snapshots)', pass: true, note: 'structural — observed in the Constitution view (versioned Archive, rollback)' },
    { item: 4, text: 'Numbers labelled + configured + recompute in the validator', pass: !findings.some((f) => f.check === 'C4' && f.severity === 'blocking') },
    { item: 5, text: 'Symbols resolve to one type each', pass: !findings.some((f) => f.check === 'C5' && f.severity === 'blocking') },
    { item: 6, text: 'Hazard↔requirement orphans listed in both directions', pass: !findings.some((f) => f.check === 'C6' && f.severity === 'blocking') },
    { item: 7, text: 'External docs rendered from the ledger (registers are the source of truth)', pass: true, note: 'structural — README obligation tables now defer to canon/obligations.js' },
    { item: 8, text: 'No guaranteed-pass checks; retirements preserved', pass: !findings.some((f) => f.check === 'C7' && f.severity === 'blocking') }
  ];

  return {
    findings,
    counts,
    dod,
    releasable: counts.blocking === 0,
    registers: {
      claims: CLAIMS.length,
      gates: GATES.length,
      numbers: NUMBERS.length,
      symbols: SYMBOLS.length,
      hazards: HAZARDS.length,
      requirements: REQUIREMENTS.length,
      retirements: RETIREMENTS.length,
      obligations: OBLIGATIONS.length
    }
  };
}

export default runCanonValidation;
