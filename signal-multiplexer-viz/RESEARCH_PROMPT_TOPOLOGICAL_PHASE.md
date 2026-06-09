# Research Prompt — Topological Phase Discovery for the Metabolic-Memory Reservoir

*Hand this to a research agent (deep-research register). It is scoped to extract a method, map it onto our discrete invariant `S`, and report honestly where the analogy breaks. Output is contestable: every mapping must carry a warrant and a defeater.*

---

## Role and objective

You are a condensed-matter-theory research agent. Your objective is to **extract the operational essence of the 2016 Nobel Prize in Physics** (David J. Thouless; F. Duncan M. Haldane; J. Michael Kosterlitz — "theoretical discoveries of topological phase transitions and topological phases of matter") and translate it into a usable module for a safety-critical memory reservoir whose discrete state label is

```
  S = 1[ n_z != n_p ] in {0,1},
```

where `n_z` is the number of active memory traces ("support") and `n_p` is a target capacity. `S` empirically **locks** (stops flipping under small perturbations) when the reservoir stabilizes. We want to understand *why* such a label can be robust, *when* it is quantized, and *how* to deliberately drive the system between phases.

Do **not** produce a history lesson. Produce a **method extraction** with explicit mappings and explicit failure boundaries.

---

## Part A — Extract the method (the physics, precisely)

Answer each with the governing equation and a one-line plain-language gloss:

1. **The Landau paradigm it replaces.** What is a "local order parameter," and why can two phases with *identical* symmetry nonetheless be distinct? State the gap-closing criterion for a phase transition.
2. **Kosterlitz–Thouless (BKT).** What are vortices/antivortices as topological defects? What is the binding/unbinding mechanism, and what makes the transition *infinite-order* (no local-order-parameter discontinuity)? Give the role of the topological charge (winding number) `∮ dθ = 2πn`.
3. **TKNN / Thouless (quantum Hall).** Define the **Chern number** as the integral of the Berry curvature over the Brillouin zone, `C = (1/2π) ∫_BZ F d²k ∈ ℤ`. State precisely *why* it is an integer and *why* it is robust to smooth deformations of the Hamiltonian that do not close the gap.
4. **Haldane.** What does the Haldane model show — a topologically non-trivial phase **without** a net magnetic field? What is the Haldane gap in spin-1 chains, and what is the relevant invariant (e.g., a ℤ₂ or string order)?
5. **The unifying redefinition.** State, in one paragraph, how these works **redefine "phase"**: from "broken-symmetry / local order parameter" to "**global topological invariant, robust to continuous deformation, changing only at gap closings**."

Deliverable A: a compact table — *invariant · what it counts · what protects it · what closes the gap*.

---

## Part B — Map onto our system (with warrants and defeaters)

For each mapping, give: the proposed correspondence, the **warrant** (why it should hold), and the **defeater** (the observation/argument that would break it).

1. **`S` as a topological charge.** Is the support-parity `S` (or a refinement: a winding/Chern-like integer read off the **pole–zero topology** of the transfer function `H(s)`) a genuine topological invariant, or only a parity proxy? Propose the most defensible integer-valued invariant computable from `H(s)`'s pole–zero configuration (e.g., a winding number of `H(iω)` around the origin; a count of poles crossing the imaginary axis).
2. **Phase transition as gap closing.** Identify the reservoir's "gap." Candidate: the spectral margin `min_j |Re(p_j)|` (distance of the nearest pole to the imaginary axis), or the smallest active weight `min_i w_i`. A transition = this margin → 0 (a trace activating/forgetting; a pole crossing). State the detection rule.
3. **Robustness ⇒ no flicker.** Explain how topological protection predicts that `S` should be *insensitive* to sub-gap perturbations — i.e., a quantitative reason for the empirical "locking," and a bound on the perturbation size that cannot change `S`.
4. **Control parameter for steering.** Identify the tunable knob (analogous to a mass term / chemical potential / coupling) that drives the system across a transition. In our system, candidate knobs: the learning rate `η`, the running scale `Λ_G`, or the capacity target `C`. Which knob gives a *clean, hysteretic* transition?

---

## Part C — "Manipulated causality" (the application target)

Our working definition (contestable — challenge it if the physics suggests a better one):

> *Establishing regimes in which the effective causal-influence structure is insulated from perturbation within a phase, and changes only at controlled, detectable transitions — so one can choose which causal regime the system occupies and trust it to remain there until deliberately tuned across a transition.*

Address:

1. In what precise sense is the **bulk causal structure "protected"** inside a topological (gapped) phase? Relate to bulk–boundary correspondence: protected edge/boundary modes as the locus where the manipulable causal action lives, while the bulk invariant is fixed.
2. How does one **deliberately move** the system between two robust causal regimes (tune across the transition), and what guarantees the move is *sharp, quantized, and hysteretic* (matching the corpus's **asymmetric hysteresis**: tighten instantly, loosen only after a dwell)?
3. **Hard boundary — what topology does NOT buy.** State explicitly: topological protection bounds robustness *against perturbations that do not close the gap*; it gives **no** protection once the gap closes, and it is **not** a claim about retrocausality or about influencing the past. Flag any framing in the project that would overreach here.

---

## Part D — Operationalization under the project's constraints

1. **Complexity budget.** The host algorithm runs at `O(N + log(1/eps))` per step. Any invariant you propose must be computable within that budget (a winding number from already-computed poles is `O(N)`; flag anything that needs a 2D Brillouin-zone integral as *off-budget* and propose a discrete surrogate).
2. **Real-time + fail-closed.** The detector must emit a phase/transition label within a bounded WCET and degrade fail-closed (ambiguous phase ⇒ treat as transition ⇒ conservative action). Specify the rule.
3. **Subordination to safety.** A phase transition may *tighten* the admissible-command set freely but may *loosen* it only through the trusted scalar `φ` and the conformance gate. Confirm your detector respects this (it informs belief; it cannot directly enact a looser bound).

---

## Required output format

1. **Method extraction** (Part A table + five glosses).
2. **Mapping ledger** (Part B/C): rows of `correspondence · warrant · defeater · standing {grounded | proposed | speculative}`.
3. **The proposed invariant**: exact formula computable from `H(s)` pole–zero data, with complexity.
4. **The detector**: pseudocode, WCET-bounded, fail-closed, safety-subordinate.
5. **Where the analogy breaks**: a frank section listing every place the condensed-matter result does *not* transfer (finite N vs thermodynamic limit; no literal Brillouin zone; classical vs quantum; open vs closed system). This section is mandatory — a mapping with no stated breakage is rejected.

## Sources to prioritize

Thouless–Kohmoto–Nightingale–den Nijs (1982, TKNN); Kosterlitz–Thouless (1973); Haldane (1988 model; 1983 spin chains); the 2016 Nobel Committee's *Scientific Background* document; and a standard topological-band-theory review (e.g., Hasan–Kane) for the Berry-curvature/Chern formalism. Prefer primary sources and the Nobel scientific background over secondary explainers.

## Epistemic discipline

Operate under **contestable → correctable → steerable**. Mark each claim's standing. Where a mapping is only an analogy, say so. The deliverable's value is in the *honest* mapping plus the *explicit* breakage, not in an impressive-sounding unification.
