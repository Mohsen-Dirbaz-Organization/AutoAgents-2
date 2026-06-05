# Conserved-Quantity Renormalization (CQR)
## A real-time, adaptive amplification/attenuation framework for the metabolic-memory reservoir

*Deliverable for: Provably Bounded Autonomous Driving — memory module.*
*Register: contestable → correctable → steerable. Every non-trivial claim below carries a warrant and an explicit challenge condition (Part 5). Nothing here is offered as settled; it is offered as defeasible.*

---

## 0. Provenance and scope (read first)

This framework does **not** introduce a new conserved quantity, a new sensor, or a new optimizer. It re-uses one mechanism the corpus already validates — the **conservation-manifold compiler** (Ch. 8) — and applies it to the memory reservoir under three constraints you set:

1. **Zero-sum / conserved-quantity method** — amplification of one signal must be paid for by attenuation of others, with a total preserved *by the geometry of the representation, not by a penalty term*.
2. **Real-time and adaptive** — bounded per-step cost, online reference tracking, fail-closed.
3. **Complexity ≤ the original** — the per-step cost must be of the same order (or lower) than the existing elliptic/AGM surrogate. Proven in Part 4.

| Element used | Standing **in the source** | How CQR uses it |
|---|---|---|
| Open-water transform (∑yᵢ=1 ↔ sin²θ+cos²θ=1) | Validated (gasification corpus) | The conservation manifold; zero-sum by construction |
| ξ = ln(Λ_S/Λ_G) | Validated *at bin level*; **not** proven one-sided | The running coupling of the RG flow |
| S = 1[n_z≠n_p] | Validated *at bin level*; "locks" at high conversion | The discrete phase label (Part 3 topology) |
| 1-Lipschitz projection, MEET, rate-clamp-last | **Proposed** (exactness theorem owed for non-box) | Stability + non-amplification guarantee of the flow |
| `CONS.CHK`, `SFSPU.PROJ`, `SAFE.ROLL`, MB/TB/Gate | Specified (latencies projected, not silicon) | Real-time substrate; fail-closed; multi-scale coarse-graining |
| "metabolic memory" | **Not located in source** (my framing on MB/TB/Gate) | Named hypothesis; see Part 2 |

I carry the source's discipline forward verbatim: **the seam between what is validated, what is proposed, and what is unverified is never blurred.**

---

## 1. The framework

### 1.1 The conserved quantity and the zero-sum law

Represent the reservoir as **N memory traces** with non-negative weights `w = (w₁,…,w_N)`. The conserved quantity is the total reservoir mass

```
  ∑ᵢ wᵢ = M     (M fixed; w lives on the scaled probability simplex Δ_M)
```

This is the corpus's **Dalton simplex**, identically. "Metabolic" means: the reservoir has a fixed energy budget `M`; remembering more of one thing necessarily means remembering less of another. **Zero-sum is therefore not a rule we enforce — it is the shape of the space.** No penalty, no soft constraint, no post-hoc correction. Exactly the corpus's claim that "the conservation constraint becomes the geometry of the space rather than a penalty added to a loss."

> **Why the simplex and not the sphere.** The corpus's `sin²θ+cos²θ=1` is the sphere picture and is fine for intuition, but the sphere is **non-convex**, so its radial projection can *expand* distances near the centre — which would let the enforcer amplify an oscillation, the one thing §10.4.2 forbids. The probability simplex is **convex**, so Euclidean projection onto it is provably **1-Lipschitz (non-expansive)**. We therefore anchor every guarantee on the simplex and treat the sphere as the unitary-coordinate chart of the *interior*. This is a deliberate, contestable design choice (C-1, Part 5).

### 1.2 Amplification / attenuation = zero-sum reallocation

To **amplify** trace `i` is to move mass toward `wᵢ`; to **attenuate** is to move it away. Two equivalent realizations:

- **Exact (rotational).** A Givens rotation in the `(i,j)` plane on the amplitude chart `aᵢ=√(wᵢ/M)` increases `aᵢ²` at the exact expense of `aⱼ²`. Any product of such rotations is orthogonal, so `∑aᵢ²` is invariant: amplification is *literally* a zero-sum transfer, O(1) per pair. This is the unitary picture.

- **Practical (additive-then-renormalize).** Apply an evidence update `w ← w + η·g`, then **renormalize by projecting back onto the simplex**: `w ← Proj_Δ(w + η·g)`. The projection is the *renormalization step in the literal physics sense* — it re-imposes the invariant after a bare update would have violated it.

Both keep `∑wᵢ=M`. We use the additive-then-project form because it is the one with the non-expansion guarantee and the hardware instruction (`SFSPU.PROJ`).

### 1.3 The renormalization-group flow across scales

"Renormalization" is not just normalization; it is a **flow across scales** with the parameters redefined at each scale so that observables stay invariant. Our scale index `ℓ` is the **memory horizon / abstraction level**:

```
  ℓ = 0 : raw working traces (Gate / immediate)
  ℓ = 1 : Tailored-Brain on-board consolidated traces
  ℓ = 2 : Main-Brain off-board long-term store
```

Coarse-graining `ℓ → ℓ+1` is the consolidation step (short-term → long-term memory). At each scale we define the **running reference scale** as the cohort geometric mean of per-trace salience gains `kᵢ` (each `kᵢ` produced by the *existing* elliptic/AGM surrogate from the trace's features — we reuse the kernel, we do not add one):

```
  Λ_G^(ℓ) = exp( (1/N) ∑ᵢ ln kᵢ )           (running scale)
  ξᵢ^(ℓ)  = ln( kᵢ / Λ_G^(ℓ) )              (running coupling, two-tailed)
```

`ξᵢ>0` ⇒ above-reference ⇒ **amplify**; `ξᵢ<0` ⇒ below-reference ⇒ **attenuate**. The RG step is:

```
  wᵢ ← Proj_Δ ( RateClamp( wᵢ + η · clamp(ξᵢ) ,  wᵢ_prev ) )      (CQR step)
```

with `η` the (adaptive) learning rate and `clamp` a bounded monotone squashing. `ξ` is the corpus's already-validated, **scale-free** statistic (it stays O(1) even though raw gains span 10⁻⁴⁷–10⁻⁶⁰): it is *built to be compared across regimes*, which is precisely what a running coupling must do.

### 1.4 Why the flow is stable (non-amplification)

Three properties, all inherited from §10.4.2, compose:

1. **`RateClamp` is non-expansive** — it is a clamp on the per-coordinate *difference* `wᵢ−wᵢ_prev`; clamping a difference is 1-Lipschitz. This is **rate-clamp-last**: a salience spike produces bounded *jerk* in the reservoir, not a step-lurch.
2. **`Proj_Δ` is non-expansive** — Euclidean projection onto a convex set is 1-Lipschitz.
3. **Composition of 1-Lipschitz maps is 1-Lipschitz** — so the whole CQR step is a non-expansive map of the reservoir state.

**Consequence (warranted):** a corrupted or hallucinated salience injection cannot be *amplified* by the renormalizer — its effect on the reservoir is bounded by its own magnitude. This is the memory-module analogue of "the enforcer can never amplify an oscillation injected by corrupted software." When several salience sources disagree, we take the **MEET (tighter/attenuating reallocation), never the average** — averaging would re-open the worst-case bound.

> A non-expansive map need not be a contraction, so this guarantees **bounded, non-amplifying** evolution, not convergence to a unique point. Convergence requires the stronger fixed-point argument of §1.5, which is **proposed, not proven** (O-2, Part 5).

### 1.5 Fixed points and the discrete phase label

A **fixed point** `w*` is where the bounded reallocation vanishes (`ξ` aligned with the support). Define the discrete invariant exactly as the corpus does:

```
  n_z = #{ i : wᵢ > ε }         (active traces — "support")
  n_p = C                        (reservoir capacity / target slot count)
  S   = 1[ n_z ≠ n_p ] ∈ {0,1}
```

`S` **locks** when the memory phase stabilizes: a trace's weight reaching 0 (forgetting) or a dormant trace activating (recall) is a **discrete transition** in the support — a "gap-closing" event. `S` is robust to small perturbations *within* a phase and flips only at genuine transitions. That robustness is exactly the property the 2016-Nobel topological machinery formalizes — Part 3.

### 1.6 Real-time and adaptive properties

- **Adaptive.** `η` and `Λ_G` update online by EMA (the corpus's `adaptiveParams` pattern), so the flow tracks non-stationary evidence without re-initialization.
- **Real-time.** Each CQR step is bounded O(N) (Part 4) with a hard WCET; `Proj_Δ` is the bounded-latency `SFSPU.PROJ`; the zero-sum check is `CONS.CHK` (|∑w−M|<tol).
- **Fail-closed.** If `CONS.CHK` fails or `Proj_Δ` cannot complete in budget, **freeze the reservoir** (no reallocation = no new amplification = the minimal-risk memory configuration) via `SAFE.ROLL`. Freezing is safe because a frozen reservoir cannot *loosen* any downstream admissible set (§1.7).

### 1.7 Subordination to safety (the non-negotiable boundary)

Memory amplification changes **what the system believes**, which could change **what it is willing to do**. The corpus's monotonicity law must not be bypassed by the back door of memory:

> **Memory-monotonicity invariant.** A CQR reallocation may *tighten* the admissible-command set freely (attenuation → more conservative), but **any amplification that would expand the admissible set must pass through the trusted scalar φ and the §10.4.4 conformance gate** before it can affect actuation.

Operationally: the metabolic memory lives at the **S1 (epistemic) / governance** altitude. It can propose a sharper belief; it can never *enact* a looser bound. The S3 risk-monotone algebra and the S4 analog veto sit downstream and unmodified. This is what makes "memory poisoning → unsafe action" **structurally unrepresentable** rather than merely improbable — the same wager as the rest of the stack.

---

## 2. The memory module: "metabolic memory" as a renormalized reservoir

### 2.1 The relationship of the multiplex repository to the car stack

The signal-multiplexer repository and the driving stack are **the same machine at two altitudes**, joined at the conservation manifold:

```
  multiplex repo  →  generic conservation-manifold compiler + ξ/S invariants
                      (the validated mechanism, domain-agnostic kernel)
        │
        │  instantiated for one subsystem
        ▼
  car stack       →  the MEMORY MODULE (reservoir) is that compiler applied
                      to knowledge retention, governed by CQR (this document)
```

The multiplexer's job was *bandwidth allocation under a conserved total*. The memory module's job is *retention allocation under a conserved metabolic budget*. **It is the identical optimization** — `∑bandwidthᵢ = B` becomes `∑retentionᵢ = M` — so the multiplexer kernel transfers without re-derivation. That transfer is the concrete answer to "how does the multiplex repo relate to the car stack": **the multiplexer is the prototype of the memory reservoir's allocator.**

### 2.2 Metabolic budget, retention, and forgetting

| Biological metaphor | CQR object |
|---|---|
| Fixed metabolic energy (ATP) | Conserved reservoir mass `M` |
| Long-term potentiation (strengthen) | Amplification: mass toward `wᵢ` (ξᵢ>0) |
| Synaptic decay / forgetting | Attenuation: mass away from `wᵢ` (ξᵢ<0) |
| Consolidation (hippocampus→cortex) | RG coarse-graining `ℓ→ℓ+1` (Gate→TB→MB) |
| Recall | Dormant trace activating: an `S` transition |

The metaphor is *only* a metaphor (C-2). What is rigorous is the conservation law and the non-expansive flow; the biology is exposition.

### 2.3 MB/TB/Gate as the RG ladder

The corpus's cognition allocation is the renormalization ladder, read off directly:

- **Gate (ℓ=0)** — immediate working traces, microsecond budget, escalates only when expected regret exceeds the latency cost.
- **Tailored-Brain (ℓ=1)** — on-board consolidated reservoir; CQR runs here at the control cadence.
- **Main-Brain (ℓ=2)** — off-board long-term store; consolidation is a CQR coarse-graining performed between drives (slow loop), and any change to MB-derived truth is an **anti-silent-drift challengeable event** (LLC-jump detector), never silently absorbed.

The timescale honesty of §9.5 is preserved exactly: CQR at TB is the real-time mechanism; CQR consolidation to MB is the slower assurance layer. **Conflating the two would overstate the guarantee** — so we do not.

---

## 3. The topological phase-discovery module

### 3.1 Why topology, precisely

`S` is already a discrete, perturbation-robust, *locking* invariant — i.e., it behaves like a **topological invariant** (an integer that counts a global feature and is insensitive to smooth local deformation). The 2016 Nobel work (Thouless–Haldane–Kosterlitz) is exactly the theory of *why such invariants exist, when they are quantized, and how phases transition*. Importing its essence gives us three things the bin-level `S` does not yet have:

1. A principled reason `S` is **robust** (it is a topological charge, not a tuned threshold) → fewer spurious phase flickers in the reservoir.
2. A principled definition of a **phase transition** (a "gap closing") → a precise, detectable consolidation/recall event.
3. A way to **deliberately steer** the system between phases by tuning a control parameter across a transition — your "manipulated causality."

### 3.2 "Manipulated causality" — my operational reading (contestable, C-3)

I read "manipulated causality" as: *establishing regimes in which the effective causal-influence structure is insulated from perturbation within a phase, and changes only at controlled, detectable transitions, so that one can choose which causal regime the system occupies and trust it to stay there.* In a topological phase, the bulk is "gapped" — local perturbations cannot change the global invariant — so the causal structure is *protected*; you manipulate it not by fighting perturbations but by **tuning the control parameter across a transition**, where the change is sharp, quantized, and hysteretic (matching the corpus's asymmetric hysteresis). This is the useful, defensible core; anything stronger (literal retrocausality, etc.) is out of scope and I will not assert it.

### 3.3 The tailored research prompt

The standalone prompt to hand to a research agent is in **`RESEARCH_PROMPT_TOPOLOGICAL_PHASE.md`** (this directory). It is scoped to extract the *method*, map it onto `S`/pole-zero topology, and — crucially — report where the analogy **breaks**.

---

## 4. Complexity: proof that CQR ≤ the original

Let `N` = active traces, `ε` = AGM target precision.

**Original elliptic surrogate, per evaluation:**
- `K(m)` via AGM: `O(log(1/ε))` iterations (~5 to machine precision — quadratic convergence).
- Zero/pole construction: `O(N)`. Gain: `O(N)`.
- **Total: `O(N + log(1/ε))`.**

**One CQR step:**
- Per-trace salience `kᵢ` via the **same** AGM kernel (reused, not added): `O(N + log(1/ε))`.
- `Λ_G`, `ξ`: `O(N)`.
- Additive update + rate-clamp: `O(N)`.
- Simplex projection `Proj_Δ`: `O(N)` expected (pivot-based, Duchi et al.) or `O(N log N)` worst case (sort-based). On the target substrate `SFSPU.PROJ` is a **bounded-latency hardware instruction → O(1)**.
- `CONS.CHK`: `O(N)` (the `|∑w−M|` reduction; 2.7 ns in spec).
- **Total: `O(N + log(1/ε))`** with the linear-time projection (software) or hardware `SFSPU.PROJ`; `O(N log N + log(1/ε))` only if the sort-based projection is used in software.

**Claim (warranted):** with the expected-linear projection or the hardware instruction, **CQR is the same order as the original surrogate**, and never worse than an additional `log N` factor in the naïve software path. The complexity budget you set is met. (Challenge condition O-4, Part 5.)

---

## 5. Contestability ledger (contestable → correctable → steerable)

Every non-trivial claim, its warrant, and the evidence that would defeat it.

### Design choices (C)
- **C-1 — Simplex over sphere.** *Warrant:* convexity ⇒ 1-Lipschitz projection ⇒ no oscillation amplification. *Defeated if:* a sphere/Stiefel chart with a proven non-expansive retraction is exhibited that better preserves the unitary structure without losing the bound.
- **C-2 — "Metabolic" metaphor.** *Warrant:* exposition only; all guarantees rest on the conservation law, not the biology. *Defeated if:* the metaphor is shown to smuggle an unstated assumption into a guarantee.
- **C-3 — "Manipulated causality" reading.** *Warrant:* the gapped-phase / protected-invariant reading is the standard topological-matter meaning. *Defeated if:* the project intends a different, specified meaning (please supply it).

### Open obligations (O) — owed, not proven
- **O-1 — One-sidedness of ξ as a salience scalar.** Inherited from the corpus: ξ is validated *accurate*, not *conservative*. Memory amplification gated by φ assumes φ lower-bounds true risk. *Owed:* the same one-sided-bound proof the corpus owes, re-stated for the memory salience.
- **O-2 — Convergence of the CQR flow.** §1.4 proves non-expansion (bounded, non-amplifying), **not** contraction. *Owed:* conditions under which the flow has a unique stable fixed point (e.g., strong monotonicity of `clamp(ξ)` plus a strict-contraction region).
- **O-3 — Validation transfer.** ξ/S are validated *at bin level on gasification*. Their predictive alignment in the **memory** domain is a **hypothesis**, not a result. *Owed:* a held-out study showing ξ tracks a retention-quality metric and S tracks a recall/consolidation metric in this domain. (This is the same cross-domain obligation I flagged earlier; the document agrees — it lists cross-process portability as a *falsifiable prediction*, not a fact.)
- **O-4 — Complexity in practice.** *Owed:* a measured WCET of the software `Proj_Δ` (or the FPGA `SFSPU.PROJ`) under jitter bounds, confirming the O(N) path holds on the real substrate. Until measured, the "≤ original" claim is *projected*, not measured — per the corpus's own notional-vs-built discipline.
- **O-5 — Topology mapping.** *Owed:* the research module (Part 3.3) must report **where the topological analogy breaks** before `S` is treated as a genuine Chern-like charge rather than a useful parity proxy.

### Steerability
Each `O` has an owner-able next action and a measurable exit criterion, so the framework is **steerable**: contest an item → correct the specific link → the rest of the chain is unaffected (the seam is never blurred).

---

## 6. What to build, in order

1. **`ConservedRenormalization.js`** (this directory) — the reference kernel: simplex projection, ξ/Λ_G, rate-clamp-last, CQR step, S invariant, `CONS.CHK`, fail-closed. Complexity-annotated. *(Delivered alongside this doc.)*
2. **O-3 study** — wire CQR to the existing simulation; check ξ↔retention-quality and S↔recall on held-out scenarios. This is the cheapest place to confirm or kill the cross-domain hypothesis.
3. **O-2 / O-1 proofs** — contraction conditions; one-sided salience bound.
4. **Topology module** — execute the research prompt; implement the phase detector only after O-5 is discharged.

The wager, unchanged from the corpus: not that the reservoir never mis-weights a memory, but that **when it does, the error has nowhere unsafe to go** — because amplification is zero-sum, non-expansive, and subordinate to the trusted scalar.
