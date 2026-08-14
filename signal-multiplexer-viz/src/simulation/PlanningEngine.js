/**
 * PlanningEngine.js — the Rigorous Planning Management Framework, instantiated.
 *
 * Implements the framework's computable analyses over the concrete planning
 * instance in canon/planning.js and emits a PlanningResult per the Part X
 * schema discipline: every field is a triple ⟨value, method, grade⟩ with
 * method ∈ {A1…A12} and grade ∈ {exact, guaranteed, heuristic, modelled,
 * measured, open}. "A field with no method is inadmissible."
 *
 * Analyses implemented (Part IX):
 *   A2  hazard census        — RAW/WAW/WAR via pairwise R/W intersection
 *   A4  scope audit          — scopes ranked by induced contention pairs
 *   A5  circuit decomposition— Tarjan SCCs of the causal digraph (exact)
 *   A7  work–span            — W1, W∞, Π = W1/W∞, critical path (exact DP)
 *   A11 schedule bounds      — Brent/Graham band for K agents (guaranteed)
 *   +   independence matrix  — Ic vs Ir per pair (Thm 2.2: incomparable)
 *   +   ladder cost          — Π under genuine causal arcs vs under the
 *                              blanket phase ladder (Remark 4.4)
 *
 * Invariants checked (Part X §10.3 subset applicable without a scheduler):
 *   V4  bounds.Pi = W1/W∞ and critical_path realises W∞
 *   V11 every field carries a method and a grade
 *   V12 registers.debts non-empty or explicitly asserted empty with a reason
 *
 * Framework theorems are cited with standing `established` (published proofs);
 * everything computed HERE is grade `exact` *given the instance*, whose efforts
 * are grade `modelled` — that caveat travels in provenance, per the canon.
 */

import {
  SCOPES, ARTIFACTS, TASKS, CAUSAL_ARCS, PHASE_ORDER, FROZEN_CONVENTIONS
} from '../data/canon/planning.js';

const byId = (arr, key = 'id') => Object.fromEntries(arr.map((x) => [x[key], x]));

// ---- scope helpers (lattice = tree; overlap iff one is ancestor of other) ----
const scopeParents = byId(SCOPES);
function scopeChain(id) {
  const chain = [];
  for (let s = id; s; s = scopeParents[s]?.parent) chain.push(s);
  return chain;
}
function scopesOverlap(a, b) {
  return scopeChain(a).includes(b) || scopeChain(b).includes(a);
}

// ---- A2: hazard census ----
function hazardCensus() {
  const RAW = [], WAW = [], WAR = [];
  for (let i = 0; i < TASKS.length; i++) {
    for (let j = 0; j < TASKS.length; j++) {
      if (i === j) continue;
      const a = TASKS[i], b = TASKS[j];
      const wA = new Set(a.writes);
      for (const x of b.reads) if (wA.has(x)) {
        RAW.push({ writer: a.id, reader: b.id, artifact: x });
      }
    }
  }
  for (let i = 0; i < TASKS.length; i++) {
    for (let j = i + 1; j < TASKS.length; j++) {
      const a = TASKS[i], b = TASKS[j];
      const wB = new Set(b.writes);
      for (const x of a.writes) if (wB.has(x)) WAW.push({ a: a.id, b: b.id, artifact: x });
      const rA = new Set(a.reads);
      for (const x of b.writes) if (rA.has(x)) WAR.push({ reader: a.id, writer: b.id, artifact: x });
    }
  }
  return { RAW, WAW, WAR };
}

// ---- A4: scope audit — contention pairs induced per scope ----
function scopeAudit() {
  const artScope = byId(ARTIFACTS);
  const counts = {};
  for (let i = 0; i < TASKS.length; i++) {
    for (let j = i + 1; j < TASKS.length; j++) {
      const a = TASKS[i], b = TASKS[j];
      for (const xa of a.writes) for (const xb of b.writes) {
        const sa = artScope[xa]?.scope, sb = artScope[xb]?.scope;
        if (sa && sb && scopesOverlap(sa, sb)) {
          const top = scopeChain(sa).includes(sb) ? sb : sa;
          counts[top] = (counts[top] || 0) + 1;
        }
      }
    }
  }
  const ranked = Object.entries(counts).sort((x, y) => y[1] - x[1])
    .map(([scope, pairs]) => ({ scope, pairs }));
  const total = ranked.reduce((s, r) => s + r.pairs, 0) || 1;
  return { ranked, dominant: ranked[0] || null, dominantShare: ranked[0] ? ranked[0].pairs / total : 0 };
}

// ---- A5: Tarjan SCCs over the causal digraph ----
function tarjanSCC(nodes, arcs) {
  const adj = {}; nodes.forEach((n) => { adj[n] = []; });
  arcs.forEach((e) => adj[e.from].push(e.to));
  let index = 0;
  const idx = {}, low = {}, onStack = {}, stack = [], sccs = [];
  function strong(v) {
    idx[v] = low[v] = index++; stack.push(v); onStack[v] = true;
    for (const w of adj[v]) {
      if (idx[w] === undefined) { strong(w); low[v] = Math.min(low[v], low[w]); }
      else if (onStack[w]) low[v] = Math.min(low[v], idx[w]);
    }
    if (low[v] === idx[v]) {
      const comp = [];
      let w;
      do { w = stack.pop(); onStack[w] = false; comp.push(w); } while (w !== v);
      sccs.push(comp);
    }
  }
  nodes.forEach((n) => { if (idx[n] === undefined) strong(n); });
  return sccs;
}

// ---- A7: work–span over a DAG (longest-path DP on a topological order) ----
function workSpan(tasks, arcs) {
  const effort = Object.fromEntries(tasks.map((t) => [t.id, t.effortPd]));
  const ids = tasks.map((t) => t.id);
  const adj = {}, indeg = {};
  ids.forEach((n) => { adj[n] = []; indeg[n] = 0; });
  arcs.forEach((e) => { adj[e.from].push(e.to); indeg[e.to]++; });
  const order = [], q = ids.filter((n) => indeg[n] === 0);
  const indegC = { ...indeg };
  while (q.length) {
    const n = q.shift(); order.push(n);
    for (const m of adj[n]) if (--indegC[m] === 0) q.push(m);
  }
  const acyclic = order.length === ids.length;
  const W1 = tasks.reduce((s, t) => s + t.effortPd, 0);
  if (!acyclic) return { acyclic, W1, Winf: null, Pi: null, criticalPath: [] };
  const finish = {}, pred = {};
  for (const n of order) {
    let best = 0, from = null;
    for (const e of arcs) if (e.to === n && finish[e.from] > best) { best = finish[e.from]; from = e.from; }
    finish[n] = best + effort[n]; pred[n] = from;
  }
  let end = order[0];
  for (const n of order) if (finish[n] > finish[end]) end = n;
  const path = [];
  for (let n = end; n; n = pred[n]) path.unshift(n);
  const Winf = finish[end];
  return { acyclic, W1, Winf, Pi: W1 / Winf, criticalPath: path };
}

// Blanket phase ladder Eφ: every task in phase p precedes every task in p' > p.
function phaseLadderArcs(tasks) {
  const arcs = [];
  for (const a of tasks) for (const b of tasks) {
    if (PHASE_ORDER.indexOf(a.phase) < PHASE_ORDER.indexOf(b.phase)) {
      arcs.push({ from: a.id, to: b.id });
    }
  }
  return arcs;
}

// ---- A11: Brent/Graham band for K agents (Thm 4.2) ----
function brentBand(W1, Winf, K) {
  return {
    K,
    lower: Math.max(W1 / K, Winf),   // universal lower bound
    upper: (W1 - Winf) / K + Winf,   // Brent upper bound (greedy, free assignment)
    grahamFactor: 2 - 1 / K          // list-schedule guarantee
  };
}

// ---- Independence matrix: Ic (causal) vs Ir (resource) per pair (Thm 2.2) ----
function independenceMatrix(tasks, arcs) {
  const ids = tasks.map((t) => t.id);
  const reach = {}; ids.forEach((n) => { reach[n] = new Set([n]); });
  let changed = true;
  while (changed) {
    changed = false;
    for (const e of arcs) {
      for (const r of [...reach[e.to]]) {
        if (!reach[e.from].has(r)) { reach[e.from].add(r); changed = true; }
      }
    }
  }
  const artScope = byId(ARTIFACTS);
  const taskMap = byId(TASKS);
  const pairs = [];
  for (let i = 0; i < ids.length; i++) for (let j = i + 1; j < ids.length; j++) {
    const a = ids[i], b = ids[j];
    const Ic = !reach[a].has(b) && !reach[b].has(a);
    let Ir = true;
    for (const xa of taskMap[a].writes) for (const xb of taskMap[b].writes) {
      if (scopesOverlap(artScope[xa].scope, artScope[xb].scope)) Ir = false;
    }
    pairs.push({ a, b, Ic, Ir });
  }
  return pairs;
}

/** Run the instantiation and emit the PlanningResult with invariant checks. */
export function runPlanningAnalysis() {
  const field = (value, method, grade) => ({ value, method, grade });

  const haz = hazardCensus();
  const scopes = scopeAudit();
  const sccs = tarjanSCC(TASKS.map((t) => t.id), CAUSAL_ARCS);
  const maxScc = Math.max(...sccs.map((c) => c.length));
  const causal = workSpan(TASKS, CAUSAL_ARCS);
  const ladder = workSpan(TASKS, [...CAUSAL_ARCS, ...phaseLadderArcs(TASKS)]);
  const bands = [1, 2, 3, 4].map((K) => brentBand(causal.W1, causal.Winf, K));
  const indep = independenceMatrix(TASKS, CAUSAL_ARCS);
  const icNotIr = indep.filter((p) => p.Ic && !p.Ir);

  const debts = TASKS.filter((t) => t.external).map((t) => ({ task: t.id, debt: t.external }));

  const result = {
    instance: field({
      tasks: TASKS.length, artifacts: ARTIFACTS.length,
      causalArcs: CAUSAL_ARCS.length, phases: PHASE_ORDER.join('<'),
      note: 'open obligations of canon/obligations.js; efforts are person-days, grade modelled'
    }, 'A1', 'modelled'),

    hazards: field({
      RAW: haz.RAW.length, WAW: haz.WAW.length, WAR: haz.WAR.length,
      incidence: haz
    }, 'A2', 'exact'),

    contention: field({
      ranked: scopes.ranked, dominant: scopes.dominant, dominantShare: scopes.dominantShare
    }, 'A4', 'exact'),

    structure: field({
      sccs, maxScc,
      feedback: maxScc > 1
        ? 'feedback present — tear within components (Prop. 3.3)'
        : 'all SCCs are singletons: no feedback to tear (the framework\'s best case)'
    }, 'A5', 'exact'),

    bounds: field({
      W1: causal.W1, Winf: causal.Winf, Pi: causal.Pi,
      criticalPath: causal.criticalPath,
      ladderPi: ladder.Pi, ladderWinf: ladder.Winf,
      ladderCost: `blanket P0–P3 ladder: Π = ${ladder.Pi.toFixed(2)} vs genuine causal arcs: Π = ${causal.Pi.toFixed(2)} — the ladder, not the work, serialises the plan (Remark 4.4)`
    }, 'A7', 'exact'),

    schedule: field({
      bands,
      ceiling: `speed-up ceiling is min(K, Π) = min(K, ${causal.Pi.toFixed(2)}) — agents beyond Π cannot repay coordination (Cor. 4.3)`
    }, 'A11', 'guaranteed'),

    independence: field({
      pairs: indep,
      icNotIr,
      caveat: `Ic and Ir are incomparable (Thm 2.2): ${icNotIr.length} causally-independent pairs share a write scope — they may run in either ORDER but not CONCURRENTLY without arbitration. Ii is not decidable from P (Prop. 2.3) — see frozen conventions.`,
      frozenConventions: FROZEN_CONVENTIONS
    }, 'A2', 'exact'),

    registers: field({
      debts,
      debtsNote: debts.length ? null : 'asserted empty: all open tasks are internally executable',
      giveups: [
        'A6 tearing not run (no non-singleton SCCs to tear)',
        'A8–A10, A12 not instantiated (no multi-agent partition is being chosen for this backlog)'
      ],
      risks: ['efforts are modelled, not measured — every bound inherits that grade']
    }, 'A9', 'modelled'),

    provenance: field({
      generator: 'PlanningEngine.runPlanningAnalysis',
      inputs: ['canon/planning.js (instance)', 'canon/obligations.js (task source)'],
      framework: 'Rigorous Planning Management Framework V1 (Parts I–X)',
      gradeSummary: 'analyses exact/guaranteed on a modelled-effort instance'
    }, 'A1', 'exact')
  };

  // ---- invariants (Part X §10.3, applicable subset) ----
  const invariants = [];
  const pathSum = causal.criticalPath.reduce(
    (s, id) => s + TASKS.find((t) => t.id === id).effortPd, 0);
  invariants.push({
    id: 'V4',
    text: 'bounds.Pi = W1/W∞ and critical_path realises W∞',
    pass: Math.abs(result.bounds.value.Pi - causal.W1 / causal.Winf) < 1e-9 &&
          Math.abs(pathSum - causal.Winf) < 1e-9
  });
  invariants.push({
    id: 'V11',
    text: 'every field carries a method (A1–A12) and a grade',
    pass: Object.values(result).every((f) =>
      /^A\d+$/.test(f.method) &&
      ['exact', 'guaranteed', 'heuristic', 'modelled', 'measured', 'open'].includes(f.grade))
  });
  invariants.push({
    id: 'V12',
    text: 'registers.debts non-empty, or explicitly asserted empty with a reason',
    pass: result.registers.value.debts.length > 0 || !!result.registers.value.debtsNote
  });

  return { result, invariants, allInvariantsPass: invariants.every((i) => i.pass) };
}

export default runPlanningAnalysis;
