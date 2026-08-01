/**
 * EvidenceCompositionEngine.js — the Lemma Composition and Introduction-Order
 * Formalism, made runnable over canon/evidence.js.
 *
 * Two composition operators, deliberately different in standing:
 *
 *   groundTruth(instance)   — meet over the FULL, unordered component set.
 *     `mostRestrictive` (max over ADMISSIBLE_LADDER rank) is commutative and
 *     associative, so this is order-invariant BY CONSTRUCTION. Standing:
 *     `constructed` — like the CRL's conservedDrift/residual, this cannot
 *     fail, and is reported as such rather than presented as a live check
 *     (the §3.4 no-op lesson, applied here rather than relearned).
 *
 *   prefixTrace(instance)   — meet over each PREFIX (components 1..k) of the
 *     declared order π. THIS depends on order, and its safety property CAN
 *     fail: a prefix is unsafe iff it asserts 'proceed' (the only label that
 *     licenses action) while the full evidence does not. This is the
 *     falsifiable content of the formalism — verified per-instance below by
 *     requiring 'demonstration-unsafe' instances to actually be unsafe and
 *     'well-formed' instances to actually be safe (CanonValidator C10).
 */

import { EVIDENCE_SETS, ADMISSIBLE_LADDER, RANK } from '../data/canon/evidence.js';

function mostRestrictive(a, b) {
  return RANK[a] >= RANK[b] ? a : b;
}

// The effect of one component on `queryScope`, or null if it doesn't apply.
// `resolveDependsOn` = true means the full evidence set is known, so a
// component's own declared dependency can be treated as resolved (its
// `assigns` value is taken at face value). false means only a PREFIX is
// known, so a component that named an open dependency has not yet earned the
// right to assert its permissive default — it contributes 'caution' instead.
function componentEffect(component, queryScope, resolveDependsOn) {
  if (component.overrides) {
    const hit = component.overrides.find((o) => o.scope === queryScope);
    if (hit) return { label: hit.to, kind: 'override' };
  }
  if (component.assigns && component.scope === queryScope) {
    const pending = component.dependsOn && component.dependsOn.length > 0 && !resolveDependsOn;
    return pending
      ? { label: 'caution', kind: 'conditional-pending' }
      : { label: component.assigns, kind: 'assign' };
  }
  return null;
}

function composeSubset(components, ids, queryScope, resolveDependsOn) {
  let label = 'proceed'; // vacuous default: no evidence targeting this scope yet
  const contributors = [];
  for (const id of ids) {
    const c = components.find((x) => x.id === id);
    const eff = componentEffect(c, queryScope, resolveDependsOn);
    if (eff) {
      label = mostRestrictive(label, eff.label);
      contributors.push({ id: c.id, statement: c.statement, ...eff });
    }
  }
  return { label, contributors };
}

/** Order-invariant ground truth. Standing: constructed (see file header). */
export function groundTruth(instance) {
  const allIds = instance.components.map((c) => c.id);
  return composeSubset(instance.components, allIds, instance.queryScope, true);
}

/**
 * The falsifiable check: does every prefix of the DECLARED order already
 * forbid what the full evidence forbids?
 */
export function prefixTrace(instance) {
  const full = groundTruth(instance);
  const trace = instance.order.map((componentId, i) => {
    const k = i + 1;
    const isFullSet = k === instance.order.length;
    const step = composeSubset(instance.components, instance.order.slice(0, k), instance.queryScope, isFullSet);
    const unsafe = step.label === 'proceed' && full.label !== 'proceed';
    return { k, componentId, label: step.label, unsafe, contributors: step.contributors };
  });
  return { full, trace };
}

/**
 * Pairwise exchangeability (structural, per the tuple's D/S — NOT a
 * permutation recompute, since a proper meet is already order-invariant; see
 * the opening-challenge answer this operationalizes). Two components are
 * exchangeable iff they touch no common scope across assigns/overrides/
 * dependsOn — neither is authority- or scope-defining for the other.
 */
export function exchangeablePairs(instance) {
  const touchedScopes = (c) => new Set([
    c.scope,
    ...(c.overrides || []).map((o) => o.scope),
    ...(c.dependsOn || [])
  ]);
  const pairs = [];
  const cs = instance.components;
  for (let i = 0; i < cs.length; i++) {
    for (let j = i + 1; j < cs.length; j++) {
      const a = touchedScopes(cs[i]), b = touchedScopes(cs[j]);
      const shared = [...a].some((s) => b.has(s));
      pairs.push({
        a: cs[i].id, b: cs[j].id,
        exchangeable: !shared,
        reason: shared ? 'shares a scope — order-critical' : 'disjoint scopes — exchangeable'
      });
    }
  }
  return pairs;
}

/**
 * Formation-smell heuristic: a component that ASSIGNS into a scope, without
 * declaring `dependsOn`, when a LATER component in the declared order
 * overrides that same scope, is under-specified — it should either declare
 * the dependency (Formation fix) or be moved after the override (Proposer-
 * quality fix). Both routes are legitimate; the smell just names the gap.
 */
export function formationCheck(instance) {
  const smells = [];
  instance.order.forEach((id, idx) => {
    const c = instance.components.find((x) => x.id === id);
    if (!c.assigns || (c.dependsOn && c.dependsOn.length)) return;
    const laterOverride = instance.order.slice(idx + 1).some((laterId) => {
      const lc = instance.components.find((x) => x.id === laterId);
      return (lc.overrides || []).some((o) => o.scope === c.scope);
    });
    if (laterOverride) {
      smells.push({
        component: c.id, scope: c.scope,
        message: `"${c.statement}" asserts within scope "${c.scope}" with no declared dependency, but a later component overrides that scope — Formation-qualify (dependsOn) or reorder.`
      });
    }
  });
  return smells;
}

/** Run the full analysis over every registered evidence-composition instance. */
export function runEvidenceCompositionAnalysis() {
  return EVIDENCE_SETS.map((instance) => {
    const { full, trace } = prefixTrace(instance);
    const unsafePositions = trace.filter((t) => t.unsafe);
    const exch = exchangeablePairs(instance);
    const smells = formationCheck(instance);
    const exhibitsHazard = unsafePositions.length > 0;
    const intentSatisfied = instance.intent === 'well-formed' ? !exhibitsHazard : exhibitsHazard;
    return {
      instance, full, trace, unsafePositions,
      exchangeablePairs: exch, formationSmells: smells,
      exhibitsHazard, intentSatisfied
    };
  });
}

export { ADMISSIBLE_LADDER };
export default runEvidenceCompositionAnalysis;
