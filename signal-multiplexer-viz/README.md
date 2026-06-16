# Adaptive Signal Multiplexer Visualization

Interactive web visualization for the **Adaptive Signal Multiplexer with Dynamic Problem Formulation** - a modernized approach to signal multiplexing through continuous optimization, extended with **Conserved-Quantity Renormalization (CQR)**, **topological phase discovery**, and **Dirac-based impulsive control** for safety-critical memory-reservoir systems.

## Overview

This visualization demonstrates how signal multiplexing can be treated as a **continuous mathematical reasoning task** rather than a fixed engineering pattern. The system formulates and solves optimization problems in real-time (100ms cycles), adapts to changing conditions, and selects appropriate solvers based on detected problem structure.

### Four views (header switcher)

The app ships with a view switcher in the header:

- **Signal Multiplexer** — the original adaptive-multiplexer simulation (optimization loop, channels, demux).
- **Bounded Autonomy Stack** — see below.
- **Program Coverage Map** — the **GHOST Autonomy** *Research Subcategory → Document Section Mapping* (Feb 2026) rendered as an interactive dashboard: **56 research subcategories** across 6 domains, mapped onto `main(8).tex` chapters and colored by source-material coverage (**15 FULL · 20 HIGH · 10 PARTIAL · 11 GAP**), with a strategic gap analysis (~55–80 person-days across 11 gaps) and a source-file → subcategory cross-reference. Data lives in `src/data/programCoverage.js`. *Coverage = source-material availability in the design corpus, **not** fabricated or measured silicon* — this is the concrete answer to "how complete is the substrate?": a thorough specification corpus (27% fully sourced, 36% substantially sourced) with explicit, prioritized gaps, not built hardware.
- **Constitution of Truth** — the correctability layer (Source #3, *Temporal State Management* Part VI). A `ConstitutionalTruthEngine` governs a live Bounded-Autonomy substrate: Ground Truth is the canon of *warranted* claims with degrees of reliance; separation of epistemic powers (Archive / Proposers / Verifiers / Adjudicators / Auditor); **Correction Supremacy** (stronger evidence beats canon consistency); **Anti-Silent-Drift** (every truth change is a logged constitutional event); a versioned Archive with **temporal rollback**; realis/irrealis modality; and the Unknown Register. The operator can challenge any canon claim and watch it get **corrected**.

The **Bounded Autonomy Stack** view is a runnable simulation of the **entire eight-thread bounded-autonomy stack** (Lanes A–H) integrated via the EVD assessment of *Bounded Autonomy on a Memristive Substrate* (see `EVD_Assessment_Source_01_Memristive_Substrate.md`) and the Conservation-Renormalization Layer (Source #2). It renders, live:
  - the **eight-thread stack** with the authority law (top-down) and the consequence law (bottom-up) meeting at the **EPU**;
  - the **S0 → S4 Architecture of Refusal**, showing the convex command box that can only ever *narrow*, plus the electrically-isolated **~32 ns analog veto** (the corpus's only *measured* latency);
  - **metabolic memory** — reflexive / tactical (analog) and strategic (digital Posit/quire) tiers, with the **τ = 5 s boundary** where analog state is re-quantized, and **shadow-price λ** retrieval admission;
  - the **antitone monotonicity monitor** — with WS-2 conservative discipline ON, monotonicity violations stay at **0%**; toggle it OFF to watch raw memristor non-idealities re-admit forbidden commands;
  - the **trusted scalars** ξ (continuous, saturating) and S (discrete parity) from the conservation-manifold compiler, plus the **LLC drift detector** that quarantines strategic consolidations on a phase-transition jump;
  - the **evidentiary ledger** preserving the projected / measured seam.

  - the **Conservation-Renormalization Layer** (`Q=0`) — a live zero-sum gain budget (Source #2's CRL) with the §3.4 gauge-covariance proposition verified on the harness.

  The engine lives in `src/simulation/BoundedAutonomyStack.js` and is driven by `src/components/StackView.jsx`. Scenarios (nominal, degrading warrant, sensor drift, adversarial, recovery) and event injectors (drop warrant, inject drift, spoof spike) are in the control panel.

### Extended Framework: Metabolic Memory Renormalization

The project has evolved to incorporate a **three-scale renormalization group architecture** for safety-critical autonomous systems (automotive, aerospace, robotics), integrating:

- **Conserved-Quantity Renormalization (CQR)**: Zero-sum signal amplification/attenuation via projection onto a conservation manifold (Dalton simplex), driven by a scale-free running coupling ξ = ln(k/Λ_G)
- **Topological Phase Discovery**: Discrete phase label S ∈ {0,1} that empirically "locks" within stable regimes and flips only at gap-closing transitions, providing robust detection of causal-structure changes
- **Dirac-Based Impulsive Control**: Triple-point formulation (space/time/measure) enabling instantaneous reallocation at critical events while maintaining non-expansive flow guarantees
- **Hardware-Mapped Architecture**: Silicon-grounded implementation with nanosecond-scale witness gates, 10ms preprocessing, and 50ms LLM inference forming a physical three-tier renormalization ladder

## Key Features

### 🔄 **Optimization Loop (100ms cycle)**
- Real-time visualization of the 7-step optimization cycle:
  1. **OBSERVE** - Capture system snapshot
  2. **FORMULATE** - Construct optimization problem
  3. **DETECT** - Identify problem structure
  4. **SELECT** - Choose appropriate solver
  5. **SYNTHESIZE** - Generate physics-informed constraints
  6. **SOLVE** - Execute optimization
  7. **APPLY** - Update system configuration

### 📊 **Interactive Visualizations**
- **System State**: Real-time channel bandwidth and queue visualization using D3.js
- **Problem Formulation**: Mathematical notation (KaTeX) showing decision variables and objectives
- **Solver Selection**: Dynamic solver choice based on problem structure
- **Physics-Informed Constraints**: Conservation laws, dynamics, and causality
- **Performance Metrics**: Latency, throughput, and fairness tracking

### 🎮 **Interactive Controls**
- Start/stop continuous optimization
- Manual signal injection into specific channels
- Traffic burst disturbances to test adaptation
- Priority selection (CRITICAL, HIGH, NORMAL, LOW)

### 📚 **Source Code Reference**
- Collapsible panel with Java implementation
- Architectural philosophy and research connections
- Context from companion GroupCoordinationFramework

## Architecture Philosophy

**Core Insight**: Signal multiplexing is not a fixed pattern but a continuous optimization problem requiring:
- Dynamic problem formulation from current state
- Structure detection and algorithm selection
- Physics-informed constraint synthesis
- Adaptive learning and model updates
- Anytime solving with performance certificates

### Extended Philosophy: Governance as Geometry

The metabolic memory extension redefines safety constraints as **structural properties** rather than penalized objectives:

- **Conservation is Structural**: The constraint ∑wᵢ = M holds by construction (simplex geometry), not by penalty term
- **Non-Expansive by Composition**: Rate-clamp-last ∘ simplex-projection both 1-Lipschitz → bounded evolution under perturbation
- **Fail-Closed by Design**: Conservation-check failure → freeze reservoir (minimal-risk configuration) without loosening downstream bounds
- **Topologically Protected Phases**: Discrete invariant S robust to sub-gap perturbations; changes only at detectable gap-closing events
- **Hardware-Realizable Governance**: Every policy concept maps to a physical sub-volume of silicon with bounded latency and trust boundaries

## Conserved-Quantity Renormalization (CQR) Framework

### Mathematical Foundation

The CQR kernel implements a **zero-sum renormalization flow** on the scaled probability simplex:

```
Δ_M = { w ∈ ℝ^N : wᵢ ≥ 0, ∑wᵢ = M }
```

**Running Coupling** (scale-free statistic validated at bin level on gasification corpus):
```
ξᵢ = ln(kᵢ / Λ_G)    where Λ_G = exp(mean(ln k))
```

**CQR Step** (O(N + log(1/ε)) complexity):
```
1. Additive update:    w' ← w + η · clamp(ξ, -ξ_max, ξ_max)
2. Rate-clamp-last:    w'' ← clamp(w' - w_prev, -r_max, r_max) + w_prev
3. Simplex projection: w_new ← Proj_Δ_M(w'')    [Duchi et al. 2008 sort-based]
4. Conservation check: if |∑w_new - M| > tol → freeze (fail-closed)
```

**Squarity Index** (discrete phase label):
```
S = 1[n_z ≠ n_p]    where n_z = #{i : wᵢ > ε}
S ∈ {0,1} empirically locks within phases, flips at gap-closing transitions
```

### Design Contracts

1. **Conservation is Structural**: Simplex geometry enforces ∑w = M by construction, not penalty
2. **Non-Expansive Flow**: Both rate-clamp and projection are 1-Lipschitz → composition is 1-Lipschitz
3. **Meet (Never Average)**: Disagreeing salience sources compose by intersection (tighter bound), preserving safety
4. **Fail-Closed**: Conservation-check failure → freeze reservoir; freezing cannot loosen admissible sets (memory-monotonicity invariant)
5. **Subordinate to Safety**: CQR may tighten bounds freely; loosening only through trusted scalar φ and conformance gate

### Three-Scale Renormalization Group (RG Ladder)

The framework implements a **physical renormalization group** with three timescales:

#### Scale 1: Gate (Impulsive Dirac Layer, <1ms)
- **Mechanism**: Dirac impulses Δw δ(t - tₖ) triggered by critical events
- **Triggers**: S-flip, queue threshold crossed, |ξ| exceeds gap, complexity misprediction
- **Action**: Instantaneous fail-closed freeze (tighten only); actual reallocation deferred to TB tier
- **Implementation**: Nanosecond-scale witness gate (μ(a,b) bilinear crossbar) reads latched phase bit S
- **Guarantee**: Memory-monotonic (amplification free, attenuation gated by φ)

#### Scale 2: TB (Tailored Brain - Adaptive, ~100ms)
- **Mechanism**: Periodic optimization with ergodic balance metrics
- **Operations**: CQR projection, ξ computation, learnable cost gradient descent
- **Ergodic Metric**: Time-averaged queue distribution C_t(q) compared to target ρ_target
- **Continuous Allocation**: Agent density ρ(channel, strength) = ∑wᵢ δ(channel - cᵢ, strength - sᵢ)
- **Output**: Updated bounded tunables (LUT slot), signed for hardware deployment

#### Scale 3: MB (Main Brain - Continuous, ~1s+)
- **Mechanism**: Long-term RG flow on conservation manifold
- **State**: ROM weights (persistent, read-only), bounded invariant library
- **Updates**: Only via governed OTA / signed patches (attestation → canary → commit/rollback)
- **Guarantee**: No silent drift; all loosening requires cryptographic evidence chain

### Hardware Mapping (Enforcement Processing Unit - EPU)

The CQR framework maps cleanly onto the **EPU IC Control Volume** (68 overlay architecture):

| **CQR Component** | **Hardware Placement** | **Latency Budget** | **Overlay Reference** |
|-------------------|------------------------|-------------------|----------------------|
| Squarity S (phase bit) | Witness bitvector (latched) | ~8ns read | 10, 16, 35, 44 |
| Gap-closing detector | Defect-exceeds-null-band signal | ~3ns | 53 (Failure-to-Feasible) |
| CQR projection | LLM feature extraction tier | ~50–100ms | 15, 21 (bounded tunables) |
| Impulsive freeze | EPU 1-bit accept/reject gate | ~3ns | 36, 55, 62 (typed feasibility) |
| Signed updates | OTA staging→attestation→commit | Variable | 28, 38 (governed evolution) |
| Memory persistence | ROM invariant library | Read-only | 35, 58 (evidence vs ephemeral) |
| Provenance log | Crypto+Log ring buffer | ~64KB | 1, 21, 47 (traceability) |

**Key Insight**: Topology computation (CQR, S-label) belongs at the TB tier (50–100ms); only the 1-bit phase readout belongs at the Gate (3ns). This resolves the WCET constraint: the simplex projection is off the hard real-time path by construction.

### Topological Phase Discovery (2016 Nobel Prize Integration)

Per `RESEARCH_PROMPT_TOPOLOGICAL_PHASE.md`, the framework incorporates:

**Part A: Method Extraction**
- **Landau Paradigm Replacement**: Phases distinguished by global topological invariants (Chern number, winding) rather than local order parameters
- **Kosterlitz-Thouless (KBT)**: Vortex binding/unbinding; infinite-order transition with topological charge ∮dθ = 2πn
- **TKNN (Quantum Hall)**: Chern number C = (1/2π)∫_BZ F d²k ∈ ℤ; integer-quantized, robust to smooth H deformations
- **Haldane**: Topologically non-trivial phases without net magnetic field; Haldane gap in spin-1 chains

**Part B: Mapping to S (with Warrants and Defeaters)**

| **Correspondence** | **Warrant** | **Defeater** | **Standing** |
|--------------------|-------------|--------------|-------------|
| S as topological charge | S parity proxy; proposed refinement: winding of H(iω) around origin | S is discrete but not integer-valued invariant; needs pole-zero topology mapping | **Proposed** |
| Gap = spectral margin | min_j \|Re(p_j)\| or min_i w_i; transition when → 0 | Finite N; no thermodynamic limit; gap not rigorously protected | **Proposed** |
| Robustness ⇒ locking | Sub-gap perturbations cannot flip S (non-expansive flow + gap) | Empirical locking observed; quantitative bound on perturbation size owed | **Grounded** (empirical) |
| Control parameter | Learning rate η, scale Λ_G, or capacity C; which gives clean hysteretic transition? | Needs experimental sweep; hysteresis not yet characterized | **Speculative** |

**Part C: Manipulated Causality**
- **Bulk Protection**: Within a phase (gap > 0), the effective causal-influence structure (which traces active/suppressed) is insulated from sub-gap perturbations
- **Boundary Action**: At transitions (gap → 0), impulsive Dirac jumps Δw δ(t - tₖ) enable controlled regime shifts
- **Hard Boundary**: Topology gives NO protection once gap closes; NOT a claim about retrocausality

**Obligations Logged**:
- **O-1**: One-sided bound proof for ξ as salience scalar
- **O-2**: Convergence proof for CQR flow on conservation manifold
- **O-3**: Cross-domain validation of ξ/S in memory domain (currently validated only at bin level on gasification corpus)
- **O-4**: Measured WCET of simplex projection (now resolved: projection off hard real-time path; only S-bit read is on it)
- **O-5**: Topological mapping validation (finite N vs. thermodynamic limit; no literal Brillouin zone)
- **O-7**: Impulsive stability bound (does S-flip-triggered Δw preserve flow stability?)
- **O-8**: Ergodic convergence in finite time (does C_t → ρ_target within operational window?)
- **O-9**: Discrete channel-space geometry (define metric or flag as heuristic)

### Dirac-Based Impulsive Control (Triple Point)

The **triple point of Dirac integral** unifies three primitive operations:

1. **Spatial Localization**: Agent i at "here" → δ(channel - i) or δ(position - xᵢ)
2. **Temporal Impulse**: Control applied "now" → δ(t - tₖ)
3. **Measure (Statistics)**: Time spent "in state" → ∫δ(state - observed(τ)) dτ

**Unity Basis Analogy**: Just as the elliptic framework has (T, sct, κ) as minimal embedding for complex waveforms, the Dirac framework has (space, time, measure) as the minimal embedding for multi-agent coordination. Every trajectory, allocation, and statistical property is a composition of these three primitives.

**Applications to CQR**:

#### Impulsive Consensus Control
- **Literature**: Zhu/Zheng/Wang 2015 — quantized consensus with δ(t - tₖ) impulses
- **CQR Mapping**: When S flips or queue threshold crossed → instantaneous Δw δ(t - tₖ) reallocation
- **Guarantee**: Preserves non-expansiveness if |Δw| ≤ rate-clamp bound (Obligation O-7)

#### Ergodic Coverage for Queue States
- **Literature**: Salman et al. 2017 — spatial ergodic coverage via time-averaged δ distributions
- **CQR Mapping**: Define target queue distribution ρ_target; empirical C_t(q) = (1/Nt)∑∫δ(q - qᵢ(τ))dτ
- **Objective**: Minimize ∫|C_t(q) - ρ_target|² (ensures balanced processing over time)

#### Geometric Task Allocation
- **Literature**: Schwager — distributed allocation via δ point masses minimizing error functional
- **CQR Mapping**: Agent allocation as continuous density ρ(channel, strength) = ∑wᵢδ(...)
- **Benefit**: Smooth gradient-based optimization for large N; discrete recovery via argmax

#### Impulsive Learning Updates
- **CQR Innovation**: When complexity misprediction or new public-utility task emerges → Dirac impulse in learning rate
- **Memory-Monotonic**: Impulses may tighten (increase ξ magnitude) freely; attenuation gated by φ

**Hybrid Flow Equation**:
```
w(t) = w_continuous(t) + ∑ₖ Δwₖ δ(t - tₖ)

Continuous: ξ-driven CQR with rate-clamp + simplex-project
Impulsive:  Triggered by {S-flip, queue spike, |ξ| > gap, error > ε}
After impulse: w ← Proj_Δ(w + Δw) to restore conservation
```

### Contestability Ledger (Epistemic Discipline)

All design choices documented as **contestable → correctable → steerable**:

| **Choice** | **Warrant** | **Defeater** | **Standing** | **Owner / Exit** |
|-----------|-------------|--------------|-------------|------------------|
| **C-1**: Zero-sum by geometry | Simplex ∑w=M structural | If projection fails CONS.CHK | **Grounded** (self-test: residual ~2e-16) | — |
| **C-2**: ξ = ln(k/Λ_G) as coupling | Bin-level validation (gasification corpus) | If memory-domain correlation breaks | **Grounded** (bin) / **Proposed** (memory) | O-3: cross-domain validation |
| **C-3**: S as topological invariant | Empirical locking observed | Finite N; no rigorous protection | **Proposed** | O-5: mapping validation |
| **C-4**: Impulsive jumps non-expansive | Projection 1-Lipschitz; \|Δw\| bounded | If \|Δw\| unbounded | **Grounded** (if bound holds) | O-7: stability proof |
| **C-5**: Ergodic convergence | Literature theorems (Salman 2017) | Finite-time windows; no guarantee | **Proposed** | O-8: measure t_conv |

## Technologies

- **React** - Component framework
- **Vite** - Build tool and dev server
- **D3.js** - Data visualization
- **KaTeX** - Mathematical notation rendering (includes CQR equations, topological invariants)
- **Lucide React** - Icon library
- **JavaScript** - Simulation engine (ConservedRenormalization.js, MultiplexerEngine.js)

## Getting Started

### Prerequisites
- Node.js 18+ and npm

### Installation

```bash
npm install
```

### Development

```bash
npm run dev
```

Open [http://localhost:5173](http://localhost:5173) to view the visualization.

### Build

```bash
npm run build
```

Outputs to `dist/` directory.

### Preview Production Build

```bash
npm run preview
```

## How to Use

1. **Start the simulation** - Click the "Start Simulation" button to begin the continuous optimization loop
2. **Watch the loop** - Observe the 7-step optimization cycle running every 100ms
3. **Inject signals** - Use the control panel to manually add signals to specific channels
4. **Create disturbances** - Click "Inject Traffic Burst" to test system adaptation
5. **Explore the code** - Toggle "Show Java Implementation" to see the source code

## What You'll See

### Original Visualization Features
- **Channels** requesting bandwidth with different priorities (CRITICAL, HIGH, NORMAL, LOW)
- **Optimization problems** formulated with decision variables (bᵢ for bandwidth, sᵢ for time slots)
- **Problem structure detection** identifying whether problems are convex, mixed-integer, stochastic, etc.
- **Solver selection** choosing appropriate methods (Interior Point, Branch & Bound, Weighted Sum, etc.)
- **Constraint synthesis** generating physics-informed constraints for conservation, dynamics, and causality
- **Real-time adaptation** as the system responds to changing conditions

### Extended CQR/Topological Features
- **Conservation Manifold**: Simplex projection visualizing zero-sum weight redistribution
- **Running Coupling ξ**: Scale-free statistic ξᵢ = ln(kᵢ/Λ_G) driving RG flow
- **Squarity Index S**: Discrete phase label {0,1} locking within regimes, flipping at transitions
- **Ergodic Balance**: Time-averaged queue distribution Cₜ(q) vs. target ρ_target
- **Impulsive Events**: Dirac δ(t - tₖ) reallocation triggers highlighted on timeline
- **Three-Scale Ladder**: Gate (ns), TB (100ms), MB (s+) tier separation visualized
- **Memory Monotonicity**: Asymmetric hysteresis — tighten instantly, loosen after dwell
- **Fail-Closed Freeze**: Conservation-check violations triggering reservoir freeze
- **Hardware Mapping**: Overlay references showing physical placement (witness gate, LUT slot, provenance ring)

## Academic Context

This visualization supports research in:
- **Robust Multiplexed MPC**: Problem formulation constructs MPC-style optimization with constraint tightening
- **VLC for MARL**: Adaptive bandwidth allocation mirrors frequency/amplitude-division multiplexing
- **6TiSCH for Swarms**: Time-slot allocation handled through optimization solver

## Audiences

Designed for three audiences with one unified visualization:
- **Academic**: Scholarly, balanced, intellectually rigorous
- **Technical**: Precise, methodological, empirically grounded
- **Policy**: Authoritative, action-oriented, pragmatic

## Project Structure

```
signal-multiplexer-viz/
├── src/
│   ├── simulation/
│   │   ├── MultiplexerEngine.js         # Multiplexer with time-series, scenarios
│   │   └── ConservedRenormalization.js  # CQR reference kernel (O(N+log(1/ε)))
│   ├── components/
│   │   ├── OptimizationLoop.jsx         # 7-step loop visualization
│   │   ├── ChannelVisualization.jsx     # D3.js channel/bandwidth charts
│   │   ├── ProblemFormulation.jsx       # Mathematical formulation (KaTeX)
│   │   ├── SolverVisualization.jsx      # Solver selection and results
│   │   ├── ConstraintPanel.jsx          # Physics-informed constraints
│   │   ├── PerformanceMetrics.jsx       # System metrics
│   │   ├── ControlPanel.jsx             # Interactive controls
│   │   ├── CodePanel.jsx                # Java code display
│   │   ├── ConceptExplainer.jsx         # Educational component (6 concepts)
│   │   └── AgentDeploymentViz.jsx       # Dual-purpose agent allocation viz
│   ├── App.jsx                          # Main application
│   └── index.css                        # Global styles
├── docs/
│   ├── RENORMALIZATION_FRAMEWORK.md      # Complete CQR specification
│   ├── RESEARCH_PROMPT_TOPOLOGICAL_PHASE.md  # 2016 Nobel extraction prompt
│   ├── CORRESPONDENCE_PRINCIPLE_PROPOSAL.md  # Quantum/classical bridge
│   ├── Dirac function report.pdf         # Multi-agent Dirac delta applications
│   └── PICAPD_compressed168.pdf          # EPU hardware overlays (68 pages)
├── README.md                             # This file
└── package.json                          # Dependencies + scripts
```

## Hardware Architecture (EPU IC — 68 Overlay Analysis)

The **Enforcement Processing Unit (EPU)** renders governance as geometry: every policy concept appears as a physical sub-volume of silicon with bounded latency and trust boundaries.

### Governance-as-Geometry Principle

**Core Insight**: An unsafe action is not rejected by software — it is **non-representable** in the hardware witness algebra.

### EPU IC Control Volume (3D Rendering)

The chip is visualized as a **normalized (X, Y, Z) control volume** with major subsystems:

| **Z-Height** | **Subsystem** | **Function** | **Latency** |
|--------------|---------------|--------------|-------------|
| 0.0–0.2 | Actuator Interface | Physical outputs (brake, steering) | ns-scale |
| 0.2–0.4 | EPU Cascade | 1-bit accept/reject witness gate | ~3ns |
| 0.4–0.6 | LLM Tile | Feature extraction, physics compliance | ~50ms |
| 0.6–0.8 | Crypto+Log | Provenance ring, signature verification | Variable |
| 0.8–1.0 | Sensor I/O | Streaming ingress (10,000 bits ephemeral) | ~10ms |
| 1.0–1.2 | IC Control Volume | Latency budgets, vote/checks | ns–ms |
| 1.2–1.4 | Witness Gates | Bilinear μ(a,b) crossbar, defect bounds | ~8ns |

### Key Overlays Mapped to CQR

**Overlay 1**: Authority Volumes (Global Governance vs. Local Safety)
- Global: Coarse constraints, policy model
- Local: Tight accept/reject, high-rate decisions
- **CQR Mapping**: MB (global, slow) vs. Gate (local, fast)

**Overlays 10, 16, 35, 44**: Witness Primitive Hardware (μ(a,b) Crossbar + ROM Consistency)
- Bilinear witness: μ(a,b) = a ⊗ b with graded commutativity enforcement
- Reduced to scalar invariants: trace, spectral summary, gain
- Null band N: μ(a,b) − σμ(b,a) ∈ N ⇒ representable; else pruned
- **CQR Mapping**: Complementary to CONS.CHK — representability (theirs) + conservation (ours)

**Overlay 15**: Where Learning Lives (RO Weights vs. Ephemeral vs. Bounded Tunables)
- ROM basis + invariant library: persistent, read-only
- KV cache: ephemeral (stream only, no persistence)
- LUT slot: bounded tunables (signed updates only)
- **CQR Mapping**: CQR updates the LUT slot via signed TB-tier commits

**Overlays 21, 46, 58**: Memory vs. Ephemeral State (Persistence Contracts)
- Persistent: ROM invariants, provenance ring (circular log)
- Ephemeral: accumulator regs, KV cache (streaming reduction only)
- **CQR Mapping**: Memory-monotonicity — tightening free, loosening via signed φ

**Overlays 28, 38, 61**: OTA Policy Updates + Rollback (Governed Evolution)
- Staging → canary → attestation → commit-or-rollback
- Rollback latch forces conservative fallback (hardware-enforced)
- All decisions signed and auditable
- **CQR Mapping**: The trusted scalar φ; asymmetric hysteresis (tighten instant, loosen after dwell)

**Overlay 53**: Failure-to-Feasible Response (Dependency Discovery Recovery)
- Defect detector: μ-symmetry exceeds null band
- Triggers: dependency discovery, conservative hardware fallback
- **CQR Mapping**: Gap-closing detector; fail-closed freeze when CONS.CHK violated

**Overlays 3, 30, 40, 51**: Latency as Geometry (Propagation + Pipeline Budget)
- Sensor preprocessing: ~10ms
- LLM feature extraction: ~50ms (parallel, pipelined)
- Vote/checks: ~8ns
- EPU decision: ~3ns
- **CQR Mapping**: The three-scale RG ladder latency budget realized in silicon

**Overlay 64**: Spatial Governance Zones (Global vs. Local Agents)
- SOFT+HARD boundary: local agents own bounded sub-volumes
- Global agent: chip-wide invariants + policy
- **CQR Mapping**: Local agents = worker EPUs (micro-invariants); Global = Queen (final authority + provenance)

**Overlays 9, 22, 33, 39, 45, 56**: Agent Partitioning (Global vs. Local in 3D Fabric)
- Local agents: capture+sanity (world+bits)
- Regional agents: manage+compose invariants
- Global agent: Queen (final authority + provenance)
- **CQR Mapping**: Hierarchical coercion (locality = evidence → hierarchical coercion)

### Witness Algebra vs. Conservation (Complementary, Not Identical)

| **Property** | **Witness Algebra (EPU)** | **CQR (Our Framework)** |
|--------------|---------------------------|-------------------------|
| **What it checks** | Representability: μ(a,b) − σμ(b,a) ∈ N (null band) | Conservation: ∑wᵢ = M (zero-sum budget) |
| **Decision** | 1-bit accept/reject (unsafe → non-representable) | Redistribution weights w on simplex Δ_M |
| **Latency** | ~3–8ns (Gate tier) | ~50–100ms (TB tier) |
| **Failure mode** | Defect exceeds null band → prune action | Residual > tol → freeze reservoir |
| **Guarantee** | Graded commutativity enforcement | 1-Lipschitz non-expansive flow |
| **Composition** | Witness gates decide IF expressible; CQR decides HOW to redistribute salience among expressible traces | Clean seam: neither weakens the other |

### Honest Flags (Defects in the Deck)

1. **Duplicates and Gaps**: 68 pages but ~61 distinct overlays; pages repeat 36, 37, 38, 42, 43, 57, 58; overlay numbers 5, 26, 29 never appear
2. **Annotation Legibility**: Overlays 16, 44, 52, 53 have unreadable label clusters at volume centers; need exploded views
3. **Naming Collision on "S"**: Deck uses S for safety case (Overlay 19) and SG1–SG4 goals (Overlay 31); our S is squarity phase label — must disambiguate
4. **Witness ≠ Conservation**: The deck implements representability gating, not zero-sum conservation — stating this distinction keeps the combined claim contestable
5. **Thermodynamic Limit Caveat**: Hardware gives empirical locking (hysteresis, dwell), not rigorous topological protection — Obligation O-5 stands

### Net Assessment: The Missing Physical Layer

The overlays supply the silicon floorplan for our stack: conservation manifold (CQR) → phase label (S) → impulsive control (Dirac) → three-scale RG ladder (MB/TB/Gate) → **bounded volume, latency budget, trust boundary, signed audit path**. The synergy is genuine; the seam is clean.

**Key Architectural Correction**: Topology computation (CQR, S-label) belongs at the TB tier (50–100ms); only the 1-bit phase readout belongs at the Gate (3ns). This resolves the WCET constraint.

## Research Connections

### Original Foundation
- **AdaptiveSignalMultiplexer.java** - Main implementation with dynamic problem formulation
- **GroupCoordinationFramework.java** - Multi-agent context with dual-purpose allocation

### Extended Theoretical Framework
- **Conserved-Quantity Renormalization**: Zero-sum signal reallocation on Dalton simplex (validated at bin level on gasification corpus)
- **2016 Nobel Prize in Physics**: Topological phase transitions (Thouless, Haldane, Kosterlitz) — discrete invariants robust to continuous deformations
- **Dirac Delta Multi-Agent Coordination**: Triple point (space/time/measure) for impulsive consensus, ergodic coverage, geometric allocation
- **Bohr's Correspondence Principle**: Three-regime architecture (quantum n<10, correspondence 10<n<100, classical n>100) with elliptic transfer functions
- **Hardware-Grounded Governance**: EPU IC control volume with 68 overlays rendering policy as physical geometry

### Validation Status
- **Bin-Level (Grounded)**: ξ/S validated on gasification corpus; conservation residual ~2e-16 in self-test
- **Cross-Domain (Proposed)**: Memory-domain correlation owed (Obligation O-3)
- **Topological (Proposed)**: S-locking empirically observed; rigorous protection in finite N systems owed (Obligation O-5)
- **Hardware (Grounded)**: EPU overlays provide physical latency budgets and trust boundaries; witness algebra complementary to CQR

## Design-Space Integration Registry (EVD Protocol Assessments)

This registry positions **external source documents** within the project's open-ended design
space (the CQR / topological-`S` / Dirac-impulse / three-scale-RG-ladder / EPU-hardware spine
documented above). Each source is processed with the **Extraction of Structured Derivatives from
Intertwined Texts** protocol (v0.2), using *this README's design space as the reference layer*
(reference-layer origin = the existing spine). Derivatives are positioned in the 3-axis divergence
space **(α abstraction, β analogical distance, γ domain-specificity)**, where the README spine sits
at `(0,0,0)`; low β = "speaks the spine's own vocabulary," high β = "reaches in from another domain."

> **How to read a position.** A derivative near the origin *confirms or directly extends* the spine.
> A high-α derivative contributes an abstract law; a low-α one contributes concrete hardware/instances.
> A high-γ derivative is deeply domain-embedded (memristor/automotive); a low-γ one is portable method.

---

### Source #1 — *Bounded Autonomy on a Memristive Substrate* (Technical Reference, Rev 2026-05-26)

> 📄 **Full protocol output:** the complete §8 output-format instance for this source — per-derivative
> writing templates (§8.9), entanglement map (§8.4), verification summary (§8.10), and validation
> checklist (§11) — lives in **[`EVD_Assessment_Source_01_Memristive_Substrate.md`](./EVD_Assessment_Source_01_Memristive_Substrate.md)**.
> The summary below is the registry-level condensation of that document.

**Gate (§5.3 Automatic Rejection):** PASS — source is identifiable, carries its own provenance and a
known/built/claimed/unverified evidentiary ledger (§7), and supplies the required evidence components.
Not rejected.

**EVD Header**

| Field | Value |
|-------|-------|
| Source | `Bounded_Autonomy_Memristive_Substrate_Technical_Reference.md` (Rev 2026-05-26) |
| Reference layer | This README's design space (CQR ξ/S · topological `S` · Dirac impulse · Gate/TB/MB ladder · EPU governance) — **Format 3: Conceptual Anchor** |
| Entanglement density | **High** (monotonicity threads through all 8 lanes; concepts fused at clause level → decomposition + cross-referencing required) |
| Conceptual units | **8 derivatives + 1 common ground** |
| Angular diversity | mean pairwise distance ≈ **0.42** ( > 0.3 target → well-layered, PASS ) |

**Reference-layer challenge (what the positioning must answer):**
(RC1) Where does the new substrate attach to the existing three-scale ladder?
(RC2) What does the source *add* that the spine lacks?
(RC3) How is safety/contestability preserved across the new analog/digital seam?
(RC4) Does it *confirm, extend, or compete with* the ξ/S/monotonicity spine?

**Common Ground (applies to the whole source; near origin):**
**Evidentiary discipline — the projected/measured seam** (§7). Every quantitative figure is *projected*
unless marked *measured*; only the FPGA analog-veto witness (~32 ns) is measured. This is the source's
framing of all eight concepts and is the direct counterpart of this README's **Contestability Ledger**
(warrant / defeater / standing). Position ≈ `(0.50, 0.10, 0.10)`.

**Derivative Angular-Positioning Table** (origin = README spine)

| # | Derivative (source §) | α (abstraction) | β (analogical dist.) | γ (domain-spec.) | Standing vs. spine |
|---|----------------------|:---:|:---:|:---:|---|
| **D1** | Antitone Monotonicity Invariant (§1) | 0.85 | 0.10 | 0.35 | **Confirms + generalizes** the memory-monotonicity invariant |
| **D2** | Enforced Causality as Fidelity (§1, §2.3, §3.3) | 0.55 | 0.20 | 0.65 | **Extends** "manipulated causality" into perception/ingestion |
| **D3** | Metabolic-Memory Stratification (§2.3, §3) | 0.45 | 0.25 | 0.55 | **Near-isomorphic** to the Gate/TB/MB ladder |
| **D4** | Budgeted Retrieval / Shadow Price λ (§3.2) | 0.60 | 0.45 | 0.45 | **New** recall-side economics (orthogonal to CQR) |
| **D5** | Conservation-Manifold Compiler φ → ξ/S (§2.4) | 0.70 | 0.05 | 0.40 | **Identity** — same ξ=ln(k/Λ_G) & S engine (anchor) |
| **D6** | Architecture of Refusal / Analog Veto S0–S4 (§2.2) | 0.30 | 0.15 | 0.70 | **Anchors** EPU Gate budgets with the only *measured* 32 ns |
| **D7** | Memristor Co-Design & τ=5 s Boundary (§4) | 0.20 | 0.55 | 0.90 | **New substrate floor** below the digital-IC EPU |
| **D8** | Anti-Silent-Drift / LLC Governance (§2.1, §3) | 0.65 | 0.35 | 0.55 | **New cross-validator** for topological `S` (O-5) |

**Dependency diagram (functional, A→B = "A needs B to be understood"):**
```
D2 → D1     (enforced causality lifts the antitone law into perception)
D3 → D1     (residence-tier verification IS the monotone-with-τ law)
D4 → D3     (shadow price prices retrieval within a residence tier)
D5 → D1     (ξ/S are the bounded scalars that make one-sidedness computable)
D6 → D1     (the cascade enforces narrowing in hardware)
D7 → D3     (device retention is mapped onto the residence tiers)
D8 → D5     (LLC phase label sits beside φ's structural-parity S)
Common Ground → all (evidentiary seam governs every claim)
```

**Synergy matrix (reference-conditional; each solves an RC no single derivative solves alone):**

| Pair / set | Angular pattern | Synergy mechanism | Reference challenge solved |
|-----------|-----------------|-------------------|----------------------------|
| **D3 × D7** | β/γ transfer (mid→high domain) | Residence tiers ↔ device-retention classes; τ=5 s boundary places the analog/digital seam exactly where blast-radius verification demands | **RC1 + RC3** — substrate attaches *and* stays safe |
| **D1 × D5** | abstraction spread (α 0.85↔0.70, low β) | Antitone *law* grounded by ξ/S *instrument* (bounded saturating scalars realize one-sidedness) | **RC4** — monotonicity becomes computable, not just asserted |
| **D5 × D8** | adjacent, phase-label pair | φ's discrete structural-parity `S` + LLC developmental phase-transition → two independent discrete phase detectors that lock/flip | **O-5** — a *second* detector to cross-validate topological `S` |
| **D6 × D7** | concrete pair (low α) | Measured 32 ns analog veto anchors the memristor WS-5 latency certification window | **RC1** — proves the analog floor can honor the veto contract |
| **D4 × D3** | abstraction/domain | Per-partition shadow price λ_k gates *retrieval* within each tier | **RC2** — adds recall economics CQR lacks (CQR only *redistributes*) |
| **D2 × D1** | direct, abstraction spread | Enforced causality extends the antitone law from actuation → perception | **RC2/RC4** — widens the invariant's reach |
| **D1 × D5 × D8** | triplet triangulation | A *law* (D1) + a *continuous scalar* ξ (D5) + *two discrete phase labels* S/LLC (D5,D8) | Robust multi-path validation for the contestability ledger |

**Net position in the design space.** The source is **not a competing framework** — it is a
**downward-and-outward extension that confirms the spine**:

- **Confirms (identity / near-origin):** D5 is literally this README's ξ/S engine (same `ξ = ln(k/Λ_G)`,
  same discrete `S`, same gasification bin-level validation); D1 is the memory-monotonicity invariant
  generalized to the full actuation algebra. The spine's core is independently restated here.
- **Extends downward (new substrate floor, high γ):** D7 adds an *analog memristive device tier* beneath
  the digital-IC EPU overlays — the README's hardware layer previously bottomed out at digital silicon.
- **Extends outward (new orthogonal modules):** D4 adds recall-side shadow-price economics; D8 adds an
  LLC phase-transition detector that cross-validates topological `S`.
- **Anchors the projected with the measured:** D6 supplies the **only measured latency in the corpus
  (~32 ns)**, giving the README's projected EPU Gate budgets (~3 ns / ~8 ns) an empirical reference point
  (still projected; the seam is preserved).

**Effect on open obligations / validation status:**
- **O-4 (WCET)** — reinforced: D6's measured 32 ns analog-veto is consistent with placing topology at the
  TB tier and only a latched bit at the Gate.
- **O-5 (topological mapping)** — *partially advanced*: D8's LLC jump is a second, independent discrete
  phase-transition signal that can be regressed against `S` (cross-validation path now exists).
- **New obligation O-10** — *device-monotonicity*: prove real memristors honor antitone admission under
  non-ideality (the source's own kill-criterion §9.1). Owner: PoC Workstream A. Exit: monotonicity
  violation rate → 0 under conservative discipline on measured-device models.
- **New obligation O-11** — *φ transfer to automotive*: the compiler is bin-level-validated on gasification
  only (same standing as the README's O-3); automotive-perception transfer + `sct`-surrogate
  reproducibility (rank corr ≥ 0.85) owed.

**Honest flags (kept per the source's own evidentiary discipline):**
1. Every memristor retention/monotonicity/latency number is a **Phase-0 hypothesis**, not measured.
2. The *Numerical Substrate Partition* ADR and the *Functionally-Driven Refinement Schedule* are a
   **ratified decision** and a **projected plan** respectively — neither upgrades any device figure to *measured*.
3. D7's analog substrate is, by the ADR, **barred from the strategic tier** — so the "new substrate floor"
   is explicitly a *non-strategic* floor; strategic persistence stays digital Posit/quire.

---

### Source #2 — *Conservation-Renormalization for Real-Time Adaptive Autonomy* (GHOST internal synthesis, v1.0, 2026-06-06)

> 🧩 **Special standing — this source is the *generative origin* of the reference layer.** Where Source #1
> attaches *to* the spine, Source #2 *defines* it: the README's CQR/`S`/metabolic-memory/contestability
> axes are this document's CRL, TPD, MMR, and Contestable Witness Protocol. Per EVD §7 (Reference-Layer
> Definition) its derivatives sit **at/near the origin** (low β by construction) — low angular diversity
> here is an expected *source feature*, not a clustering failure (§6.13/§9.10 do not fire).

**EVD Header**

| Field | Value |
|-------|-------|
| Source | `ConservationRenormalization_AdaptiveAutonomy.pdf` (GHOST · 14 pp · v1.0 · 2026-06-06) |
| Reference layer | This README's design spine — which this document **generates** (Format 3 → *also* §7 Reference-Layer Definition) |
| Entanglement density | **Medium** (five framework layers, cleanly sectioned §3–§7, sharing one premise: the Right of Contestability) |
| Conceptual units | **6 derivatives + 1 common ground** (the Right of Contestability) |

**Derivative Angular-Positioning Table** (origin = README spine; this source *is* the origin frame)

| # | Derivative (source §) | α | β | γ | Standing vs. spine |
|---|----------------------|:---:|:---:|:---:|---|
| **E1** | CRL — zero-sum gain budget `Q = Σ wₖℓₖ = 0` (§3) | 0.75 | 0.05 | 0.30 | **Defines** the CQR ξ law (gauge-fixes gain so `c` is an RG invariant) |
| **E2** | TPD — quantized topological phase labels (§4) | 0.80 | 0.10 | 0.35 | **Defines** the topological `S` invariant + manipulated-causality lever |
| **E3** | MMR — charge-conserving Metabolic Memory Reservoir (§5) | 0.50 | 0.10 | 0.45 | **Defines** the metabolic-memory tiers (conserve charges, not values) |
| **E4** | Contestable Witness Protocol — contestable→correctable→steerable (§6) | 0.60 | 0.10 | 0.30 | **Defines** the Contestability Ledger (warrant travels with the decision) |
| **E5** | Complexity discipline — `O(channels)`, net cost ≤ original (§7) | 0.55 | 0.20 | 0.25 | **New budget constraint** every spine layer must satisfy |
| **E6** | Gauge-covariance proposition `c(Rx)=c(x)` (§3.4) | 0.85 | 0.05 | 0.20 | **New verifiable theorem** — masking/inflation provably blocked |

**Net position.** Not a divergent source — the *parent* of the spine. The genuinely **new, actionable**
contributions beyond restating the spine precisely are: (a) the **exact zero-sum projection operator** (3.3)
and the **§3.4 gauge-covariance proposition** (E6); (b) the explicit **complexity budget** (E5); (c) the
ready-to-run **TPD-01 research prompt** (§4.4).

**Effect on open obligations / validation status — a real upgrade.** The source's §7.1/§8 recommend, as
step (a), *"implement the CRL on the simulation harness and verify the Proposition of §3.4 empirically."*
**This is now done.** `src/simulation/ConservationRenormalizationLayer.js` implements the CRL (gauge
factorization 3.1, zero-sum budget 3.2, projection 3.3) and runs live inside the Bounded-Autonomy-Stack
view (`Q=0` panel). Verification happens at two levels:

1. **The proposition (all three clauses) is proved on a canonical multiplet** by `verifyGaugeCovariance()`:
   - **(i)** `c(Rx) = c(x)` — conserved coordinate invariant (drift ≈ 5×10⁻¹⁶, machine zero);
   - **(ii)** masking blocked — a genuine defect (0.632) is masked on the *raw* signal (→0.035, a false accept)
     but stays genuine (0.632) in the gauge-fixed *shape* sector the gain cannot reach;
   - **(iii)** `Q = 0` is an exact critical zero after projection (residual ≈ 5×10⁻¹⁶).
2. **Clauses (i) and (iii) are then re-confirmed *live, every tick*** on the actual renormalized sensor
   channels (`crlState.live`): the per-tick conserved-coordinate drift and zero-sum residual are both
   asserted ≤ tolerance against the real, time-varying gains — not just the canonical example.

This moves the §3.4 proposition from **Proposed** to **Verified on simulation harness** (still *simulation*,
not silicon — the document's own standing for CRL/TPD/MMR remains *research proposal awaiting field validation*).

**Honest flag.** Per the source's §8, CRL/TPD/MMR/Contestable-Witness are **research proposals**, presented as
contestable claims with explicit warrants and falsifiers — they "await prototype validation on the simulation
harness before any trusted-promotion." The harness verification above *is* that prototype validation for §3.4;
it does not field-validate the substrate.

---

### Source #3 — *Tense Grammar as State Management / Temporal State Management* (64 pp, 2026-05-27)

> 🏛️ **Why this source mattered: it supplied the depth the substrate lacked — CORRECTABILITY.** The prior
> build had a *static* projected/measured ledger and an LLC quarantine with no actual correction. This source's
> **Part VI (Constitution of Truth)** demands the system remain correctable; **Part I** gives a real temporal
> state model (reversible flow maps, realis/irrealis modality, versioned past/present/future); **Part II** gives
> principled memory-erasure criteria. A new governance engine + view were built to make the system genuinely
> correctable. See **the "Constitution of Truth" app view**.

**EVD Header**

| Field | Value |
|-------|-------|
| Source | `Temporal_State_Management.pdf` (64 pp · 2026-05-27 · 6 parts) |
| Reference layer | This README's spine + the Bounded-Autonomy substrate (the thing being governed) |
| Entanglement density | **Medium** (6 cleanly-separated parts; Part VI is the load-bearing one for this build) |
| Conceptual units | **6 derivatives** (one per part) + common ground (the Constitution's supremacy) |

**Derivative Angular-Positioning Table** (origin = README spine)

| # | Derivative (source part) | α | β | γ | Standing vs. spine |
|---|--------------------------|:---:|:---:|:---:|---|
| **T1** | Constitution of Truth — correctable Ground Truth (Part VI) | 0.70 | 0.20 | 0.30 | **Implemented** — new governance engine; *defines* correctability |
| **T2** | Temporal state model — reversible flow maps, realis/irrealis (Part I) | 0.80 | 0.30 | 0.25 | **Implemented** — versioned canon + rollback; modality on every claim |
| **T3** | Principled memory erasure — correlation/τ-hierarchy/conservation (Part II) | 0.65 | 0.25 | 0.40 | **Implemented** — replaced ad-hoc eviction in the metabolic memory |
| **T4** | Continuum-aware sensing (Part III) | 0.45 | 0.35 | 0.70 | Confirms Lane G (sensors-as-witnesses) |
| **T5** | Constraint satisfaction as stress fields (Part V) | 0.75 | 0.55 | 0.55 | New analogy (constraint violation = stress); not yet built |
| **T6** | Verification-before-fusion (Part VI §19) | 0.55 | 0.15 | 0.65 | Grounds the pre-fusion admissibility gate + Art. XXXVI = the antitone law |

**What was built (the correctability depth):**
- **`src/simulation/ConstitutionalTruthEngine.js`** — Ground Truth as the canon of *warranted* claims with
  degrees of reliance (hypothesis → provisional → warranted → retracted, Art. VII); **separation of epistemic
  powers** (Archive / Proposers / Verifiers / Adjudicators / Auditor, Art. VIII–XIV); **Right of Correction +
  Correction Supremacy** (stronger admissible evidence beats canon consistency, Art. V.3 / XVIII);
  **Anti-Silent-Drift** (every truth-judgment change surfaces as a constitutional event, Art. XIX); a versioned
  **Archive** with **temporal rollback** (Part I reversibility); **realis/irrealis** modality on every claim
  (unifying with the projected/measured seam); the **Unknown Register** + Self-Reference Constraint (Art. XXVIII/XXX);
  and **conserved-fact permanence** (Part II §9.6.3 — conservation laws are permanent memory).
- **Constitution-of-Truth view** (4th app tab) — `ConstitutionView` + `GroundTruthCanon`, `SeparationOfPowers`,
  `ConstitutionalLog` (versioned events + rollback chips), `UnknownRegister`. The substrate's falling warrant /
  risk / LLC jumps drive live challenges and corrections; the operator can challenge any claim, add independent
  corroboration, attempt a silent drift (forced to surface), and roll the canon back to a prior epistemic state.
- **Metabolic-memory erasure** (`BoundedAutonomyStack._ageMemory`) regrounded in Part II: analog entries erase by
  correlation decay (|C|<ε) or the τ-hierarchy (t > α·τ, α≈3); re-quantized/conserved state is permanent memory.

**Verified in a real browser** (Playwright + headless Chromium): the Constitution view renders with **zero console
errors**; a strong challenge to a canon claim demotes it Warranted→Provisional (**correction prevails over
consistency**); a silent-drift attempt surfaces as a constitutional event; **rollback restores a prior canon**
(3 → after-run → back to 3). The standalone engine smoke test confirms genesis canon (3 warranted: 2 conserved +
1 correctable), corroboration canonizes a provisional claim, conserved facts resist weak challenges, and erasure
always leaves lineage in the Archive.

**Honest flag.** This is a *constitutional design governing the simulation*, per the source's own standing — it
makes the system demonstrably correctable; it does not certify a vehicle. Parts III–V (continuum sensing, quantum
sensing, stress-field constraint satisfaction) are noted but not yet built.

---

## Session Development (High-Granularity Chronicle)

### Phase 1: Dirac Delta Integration (Methodological Enrichment)

**Input**: `Dirac function report.pdf` (5 pages, ChatGPT-generated)

**Key Concepts Extracted**:
1. **Impulsive Consensus Control**: Dirac δ(t - tₖ) for discrete-time corrections (Zhu/Zheng/Wang 2015)
2. **Ergodic Coverage**: Time-averaged statistics Cₜ(x) = (1/Nt)∑∫δ(x - γⱼ(τ))dτ for spatial sampling
3. **Geometric Task Allocation**: Error functional G(q) with agents as Dirac point masses δ(x - xᵢ)
4. **Point-Mass Representation**: Continuous agent density ρ(channel, strength) = ∑wᵢδ(...)

**Deliverable**: Methodological enrichment analysis integrating Dirac-based multi-agent coordination with CQR framework

**Synergies Identified**:
- **Triple Point** (space/time/measure) as unit basis for multi-agent coordination
- **Impulsive reallocation** at S-flip or queue threshold → Δw δ(t - tₖ)
- **Ergodic balance metric** over queue states instead of physical space
- **Continuous allocation** for large-N scalability with discrete recovery via argmax
- **Hybrid flow equation**: w(t) = w_continuous(t) + ∑Δwₖ δ(t - tₖ)

**Breakage Points** (honest mapping):
1. No literal spatial continuity (channels are discrete indices)
2. Finite N vs. continuous limit (density approximation valid only for large N)
3. Impulsive stability without full theory (Lyapunov convergence proof owed)
4. Ergodic convergence time (finite windows vs. t → ∞ theorems)
5. Topological invariants in finite systems (no rigorous protection)

### Phase 2: Hardware EPU Architecture Study (68 Overlays)

**Input**: `PICAPD_compressed168.pdf` (68 pages, 3D IC control volume rendering)

**Analysis**: Systematic review of all 68 pages in 4 chunks (1-20, 21-40, 41-60, 61-68)

**Key Findings**:
1. **Governance as Geometry**: Every policy concept is a physical sub-volume with bounded latency
2. **Latency Ladder Realized**: Sensor ~10ms, LLM ~50ms, Vote ~8ns, EPU ~3ns → matches MB/TB/Gate
3. **Witness Algebra**: Bilinear μ(a,b) crossbar with null-band defect detection (complementary to CQR)
4. **Memory Contracts**: ROM (persistent), KV cache (ephemeral), LUT (bounded tunables) → memory-monotonicity
5. **Signed Update Path**: OTA staging → canary → attestation → commit/rollback → asymmetric hysteresis
6. **Fail-Closed Hardware**: Defect-exceeds-null-band → conservative fallback (gap-closing detector)

**Placement Resolution**: 
- CQR projection → TB tier (~100ms), off hard real-time path
- Squarity S → latched witness bitvector, Gate reads in O(1) at ~8ns
- Impulsive freeze → EPU 1-bit gate (~3ns), actual reallocation deferred to TB
- **Result**: WCET constraint satisfied (Obligation O-4 partially resolved)

**Defects Flagged**:
1. Duplicates/gaps: ~61 distinct overlays, not 68
2. Annotation legibility issues (overlapping labels)
3. Naming collision on "S" (safety case vs. squarity)
4. Witness ≠ conservation (must state distinction)
5. No rigorous topological protection (thermodynamic limit caveat)

### Phase 3: Integration and Documentation

**Outputs**:
1. **RENORMALIZATION_FRAMEWORK.md** (comprehensive CQR specification with contestability ledger)
2. **ConservedRenormalization.js** (reference kernel, self-test validates conservation ~2e-16)
3. **RESEARCH_PROMPT_TOPOLOGICAL_PHASE.md** (2016 Nobel extraction prompt, Part A–D structure)
4. **README.md** (this file, updated with high granularity)

**Synthesis**: 
- Dirac triple point + CQR conservation + topological S-locking + EPU hardware = coherent four-layer stack
- Clean seams: representability gating (hardware) + conserved redistribution (CQR) compose without weakening
- Honest breakage: finite N, no literal Brillouin zone, empirical locking vs. rigorous protection

### Open Obligations (Contestability → Correctability → Steerability)

| **ID** | **Owed** | **Owner** | **Exit Condition** |
|--------|----------|-----------|-------------------|
| **O-1** | One-sided bound proof for ξ as salience scalar | Formal analysis | Published bound or counterexample |
| **O-2** | Convergence proof for CQR flow on Δ_M | Theory / simulation | Lyapunov function or Monte Carlo bound |
| **O-3** | Cross-domain validation (memory vs. gasification) | Empirical study | Correlation r > 0.7 on memory corpus |
| **O-4** | WCET of simplex projection | Measurement | **Resolved**: projection off real-time path; S-bit read is O(1) |
| **O-5** | Topological mapping validation | Theory | Explicit finite-N invariant or honest disclaimer |
| **O-7** | Impulsive stability bound | Proof / simulation | ‖w(t) - w*‖ bounded after Δw δ(t - tₖ) |
| **O-8** | Ergodic convergence time | Simulation | Measure t_conv for ‖Cₜ - ρ_target‖ < ε |
| **O-9** | Discrete channel-space geometry | Architectural decision | Define metric or flag as heuristic |

### Right of Contestability

Every non-trivial claim in this framework remains **challengeable by evidence or defective warrant**. The contestability ledger (RENORMALIZATION_FRAMEWORK.md) documents:
- **Warrants**: Why each design choice should hold
- **Defeaters**: The observation/argument that would break it
- **Standing**: Grounded / Proposed / Speculative
- **Owner/Exit**: Who owes the validation and what measurement closes the obligation

### Key References

**Primary Sources**:
- Thouless–Kohmoto–Nightingale–den Nijs (1982, TKNN)
- Kosterlitz–Thouless (1973)
- Haldane (1988 model; 1983 spin chains)
- 2016 Nobel Committee *Scientific Background* document
- Duchi et al. (2008) — Euclidean projection onto simplex
- Zhu/Zheng/Wang (2015) — impulsive consensus control
- Salman et al. (2017) — ergodic coverage with obstacles

**Implementation**:
- `ConservedRenormalization.js` — O(N + log(1/ε)) CQR kernel with self-test
- `MultiplexerEngine.js` — time-series history, operational scenarios, adaptive learning
- `PICAPD_compressed168.pdf` — EPU IC 68-overlay hardware architecture

## License

See parent repository for license information.

## Contributing

This is a research visualization project integrating multiple theoretical frameworks (CQR, topological phases, Dirac impulsive control, hardware governance). Contributions welcome in:
- Cross-domain validation (memory corpus for ξ/S)
- Convergence proofs (CQR flow, impulsive stability, ergodic metrics)
- Hardware implementation (FPGA prototype, WCET measurements)
- Visualization enhancements (3D EPU overlay browser, phase-transition animation)

For questions or contributions, please refer to the parent AutoAgents-2 repository.

---

## Quick Reference: Key Equations and Mappings

### Core CQR Equations

**Simplex**:
```
Δ_M = { w ∈ ℝ^N : wᵢ ≥ 0, ∑wᵢ = M }
```

**Running Coupling**:
```
ξᵢ = ln(kᵢ / Λ_G)    where Λ_G = exp((1/N)∑ln kⱼ)
```

**CQR Step**:
```
w ← Proj_Δ_M(RateClamp(w + η·clamp(ξ, -ξ_max, ξ_max), w_prev))
```

**Squarity Index**:
```
S = 1[n_z ≠ n_p]    n_z = #{i : wᵢ > ε}    n_p = capacity
```

**Conservation Check**:
```
if |∑w - M| > tol → freeze (fail-closed)
```

### Dirac Triple Point

**Spatial**: Agent i at channel → δ(channel - i)  
**Temporal**: Impulse at tₖ → δ(t - tₖ)  
**Measure**: Time in state → ∫₀ᵗ δ(state - observed(τ)) dτ

**Hybrid Flow**:
```
w(t) = w_continuous(t) + ∑ₖ Δwₖ δ(t - tₖ)
```

**Ergodic Balance**:
```
Cₜ(q) = (1/Nt) ∑ᵢ ∫₀ᵗ δ(q - qᵢ(τ)) dτ
E = ∫ |Cₜ(q) - ρ_target(q)|² dq
```

### Three-Scale RG Ladder

| **Tier** | **Timescale** | **Mechanism** | **Hardware** |
|----------|---------------|---------------|--------------|
| **Gate** | <1ms (ns-scale) | Impulsive Δw δ(t - tₖ); fail-closed freeze | EPU 1-bit gate (~3ns), witness bitvector (~8ns) |
| **TB** | ~100ms | CQR projection, ξ computation, ergodic balance | LLM feature tier, bounded LUT updates |
| **MB** | ~1s+ | Long-term RG flow, ROM weights | Signed OTA, attestation→canary→commit |

### Hardware-to-CQR Mapping

| **CQR Concept** | **EPU Overlay** | **Latency** | **Notes** |
|-----------------|-----------------|-------------|-----------|
| Squarity S (phase bit) | 10, 16, 35, 44 | ~8ns read | Latched in witness bitvector |
| Gap-closing detector | 53 | ~3ns | Defect-exceeds-null-band signal |
| CQR projection | 15, 21 | ~50–100ms | LUT slot updates at TB tier |
| Impulsive freeze | 36, 55, 62 | ~3ns | EPU accept/reject gate |
| Signed updates | 28, 38 | Variable | OTA staging→attestation→commit |
| Memory persistence | 35, 58 | RO | ROM invariants + evidence library |
| Provenance log | 1, 21, 47 | ~64KB ring | Crypto+Log circular buffer |

### Complexity Guarantees

- **CQR step**: O(N + log(1/ε)) — expected linear simplex projection (Duchi et al. 2008)
- **Squarity S**: O(N) — single pass count of active traces
- **Conservation check**: O(N) — single sum
- **Gate read**: O(1) — latched bit access
- **Non-expansive**: 1-Lipschitz composition (rate-clamp ∘ projection)

### Validation Status Summary

| **Component** | **Status** | **Evidence** |
|---------------|------------|--------------|
| Conservation structural | ✅ Grounded | Self-test: residual ~2e-16 |
| ξ bin-level | ✅ Grounded | Gasification corpus validation |
| ξ memory-domain | ⏳ Proposed | O-3: cross-domain validation owed |
| S empirical locking | ✅ Grounded | Observed in plots (RENORMALIZATION_FRAMEWORK.md) |
| S topological protection | ⏳ Proposed | O-5: finite-N mapping owed |
| Non-expansive flow | ✅ Grounded | 1-Lipschitz proof (composition) |
| Impulsive stability | ⏳ Proposed | O-7: Lyapunov bound owed |
| Ergodic convergence | ⏳ Proposed | O-8: finite-time measure owed |
| Hardware latency budgets | ✅ Grounded | EPU overlays 3, 30, 40, 51 |
| Witness algebra | ✅ Grounded | EPU overlays 10, 16, 35, 44 |
| Analog-veto latency (~32 ns) | ✅ **Measured** | Memristive Substrate §2.2/§7 — the *only* measured latency in the corpus |
| ξ/S engine identity | ✅ Grounded | Memristive Substrate §2.4 — same φ-compiler ξ/S, bin-level on gasification |
| Memristor device-monotonicity | ⏳ Proposed | O-10: PoC Workstream A (violation rate → 0 under discipline) |
| φ transfer to automotive | ⏳ Proposed | O-11: gasification-only; `sct`-surrogate reproducibility (rank corr ≥ 0.85) owed |

### Open Obligations (extended)

| ID | Owed | Owner | Exit |
|----|------|-------|------|
| O-10 | Device-monotonicity under non-ideality | PoC Workstream A | Monotonicity violation rate → 0 under conservative discipline on measured-device models |
| O-11 | φ transfer gasification → automotive perception | Conservation-Manifold Compiler team | Cross-domain validation + deterministic-surrogate rank corr ≥ 0.85 |

---

**Last Updated**: June 2026  
**Session ID**: claude/signal-multiplexer-visualization-016x1jqpdGEcPjHhPJZWH9NY  
**Framework Version**: CQR 1.0 + Dirac Triple Point + EPU Hardware Mapping + Memristive-Substrate Integration (Source #1) + Conservation-Renormalization Layer implemented & §3.4 verified on harness (Source #2) + Constitutional Truth Governance — correctable Ground Truth, separation of powers, temporal rollback (Source #3)
