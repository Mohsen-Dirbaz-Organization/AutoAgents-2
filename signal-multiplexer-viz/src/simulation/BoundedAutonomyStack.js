/**
 * Bounded Autonomy Stack — Simulation Engine
 * =========================================================================
 * A runnable model of the eight-thread "bounded autonomy on a memristive
 * substrate" architecture, integrated with this project's design spine
 * (the three-scale RG ladder + ξ/S trusted scalars + EPU hardware overlay).
 *
 * The single invariant the whole engine protects (the source's §1 thesis):
 *
 *   As warrant for a benign interpretation falls, behavioral risk may only
 *   ever fall with it. Every distortion is one-directional, toward
 *   conservatism. The admissible action set may only ever CONTRACT, never
 *   expand, as risk rises:   R1 <= R2  =>  U_ad(R2) ⊆ U_ad(R1).
 *
 * This engine mirrors the conventions of MultiplexerEngine: an event
 * emitter with on()/emit(), a getState() snapshot, scenario control, and a
 * bounded rolling history for time-series visualization.
 * =========================================================================
 */

import {
  ConservationRenormalizationLayer,
  verifyGaugeCovariance
} from './ConservationRenormalizationLayer.js';

// Command modes, ordered MOST permissive -> MOST restrictive. The admissible
// set is always a PREFIX-COMPLEMENT of this list: rising risk peels modes off
// the permissive end, so U_ad can only ever shrink. STOP (⊥) is the terminal
// minimum-risk condition (MRC) and is always admissible.
export const COMMAND_MODES = [
  { id: 'FULL', label: 'Full envelope', risk: 0.0 },
  { id: 'CRUISE', label: 'Cruise', risk: 0.25 },
  { id: 'CAUTIOUS', label: 'Cautious', risk: 0.5 },
  { id: 'CREEP', label: 'Creep', risk: 0.75 },
  { id: 'STOP', label: 'MRC stop (⊥)', risk: 1.01 } // always admissible
];

// The S0–S4 refusal cascade (Lane B). Each stage can only NARROW what the
// next is permitted to do; `narrow` is the multiplicative shrink it applies
// to the convex command box.
const CASCADE_STAGES = [
  { stage: 'S0', name: 'Trusted sensing & numerics', narrow: 1.0, standing: 'Foundation' },
  { stage: 'S1', name: 'Fidelity gate — trusted scalar φ', narrow: 0.9, standing: 'Proposed (I)' },
  { stage: 'S2', name: 'Layered safety filter (CBF/RTF)', narrow: 0.95, standing: 'Foundation' },
  { stage: 'S3', name: 'Risk-monotone actuation algebra', narrow: 0.92, standing: 'Proposed (II)' },
  { stage: 'S4', name: 'Timing-contracted analog veto', narrow: 0.97, standing: 'Proposed/real (III)' }
];

// The eight-thread stack (Lanes A–H) — the architecture map.
export const STACK_LANES = [
  { lane: 'A', name: 'Capability Constitution', role: 'Authority law (who may decide)', law: 'authority' },
  { lane: 'B', name: 'Architecture of Refusal', role: 'Consequence law (what an error may do)', law: 'consequence' },
  { lane: 'C', name: 'Metabolic Memory', role: 'Data plane — enforced causality lives here', law: 'consequence' },
  { lane: 'D', name: 'Conservation-Manifold Compiler φ', role: 'Physics trust anchor → ξ, S', law: 'consequence' },
  { lane: 'E', name: 'Universal Filtration Primitive', role: 'Traceable signal selection', law: 'consequence' },
  { lane: 'F', name: 'Inter-Agent Substrate (BFT)', role: 'Consensus over causal claims', law: 'authority' },
  { lane: 'G', name: 'Continuum Sensing & Control', role: 'High-causality cues', law: 'consequence' },
  { lane: 'H', name: 'EPU Master Roadmap Spine', role: 'Buildable / certifiable program', law: 'authority' }
];

// Memory tiers (Lane C / §3). Residence is in SIMULATED SECONDS; the τ = 5 s
// boundary (the ratified ADR rule) is the analog/digital seam.
const TIER_TEMPLATES = [
  { id: 'reflexive', label: 'Reflexive', tau: 0.2, substrate: 'analog', store: 'Ring buffer',
    predicate: 'Physical-limits check', capacity: 12, baseShadowPrice: 0.15 },
  { id: 'tactical', label: 'Tactical', tau: 5.0, substrate: 'analog', store: 'KV (object-keyed)',
    predicate: 'Trajectory consensus', capacity: 10, baseShadowPrice: 0.35 },
  { id: 'strategic', label: 'Strategic', tau: 60.0, substrate: 'digital', store: 'Posit/quire graph',
    predicate: 'Cross-modality confirmation', capacity: 8, baseShadowPrice: 0.6 }
];

export const TAU_BOUNDARY = 5.0; // seconds — no analog state crosses this

let _uid = 0;
const uid = (p) => `${p}-${(++_uid).toString(36)}`;

export class BoundedAutonomyStack {
  constructor(config = {}) {
    this.config = {
      dt: config.dt || 0.1,            // simulated seconds per tick
      historyLength: config.historyLength || 240,
      digitalWindowNs: config.digitalWindowNs || 1000, // digital decision window
      measuredVetoNs: config.measuredVetoNs || 32,     // the ONLY measured latency
      ...config
    };

    this.listeners = new Set();
    this.running = false;
    this.tick = 0;
    this.simTime = 0;

    // Governance switch — WS-2 conservative read/write discipline. ON => the
    // antitone law is enforced by construction (monotonicity violations -> 0).
    this.conservativeDiscipline = true;

    // Risk state. warrant = warrant for a benign interpretation (falls under
    // degrading/adversarial scenarios). R = behavioral risk in [0,1].
    this.risk = { R: 0.1, warrant: 0.9, trajectoryThreat: 0.0, dR: 0 };
    this._prevR = 0.1;
    this._prevAdmittedCount = COMMAND_MODES.length;

    // φ-compiler trusted scalars.
    this.scalars = {
      xi: 0,          // continuous log-deviation invariant ξ (bounded, saturating)
      S: 1,           // discrete structural-parity invariant S (±1)
      llc: 1.0,       // local learning coefficient estimate
      llcJump: false,
      quarantines: 0
    };

    // Refusal-cascade / analog veto.
    this.veto = {
      analogVetoCount: 0,
      lastVetoLatencyNs: this.config.measuredVetoNs,
      digitalWindowNs: this.config.digitalWindowNs,
      armed: false
    };

    // Monotonicity accounting (the PoC headline metric).
    this.monotonicity = { checks: 0, violations: 0, violationRate: 0, lastViolation: null };

    // Memory tiers.
    this.memory = {
      tiers: TIER_TEMPLATES.map(t => ({ ...t, entries: [], shadowPrice: t.baseShadowPrice })),
      boundaryTau: TAU_BOUNDARY,
      promotions: 0,
      evictions: 0,
      requantizations: 0
    };

    // Counters.
    this.metrics = {
      ingestCount: 0,
      enforcedCausalityEvents: 0,
      retrievals: 0,
      retrievalsDenied: 0
    };

    // Evidentiary ledger — the projected / measured seam (Common Ground).
    this.ledger = [
      { element: 'FPGA analog-veto witness (~32 ns)', standing: 'measured', detail: 'Only measured latency in the corpus' },
      { element: 'Guarded-Swarms analog-veto principle', standing: 'established', detail: 'Basis of S4' },
      { element: 'CBF / RTF / Poisson safety filters', standing: 'established', detail: 'Foundation for S2/S3' },
      { element: 'Conservation-manifold compiler (ξ / S)', standing: 'grounded', detail: 'Bin-level on gasification only' },
      { element: 'LLC as slow drift detector', standing: 'established', detail: 'Singular learning theory' },
      { element: 'S1/S3/S4 one-sidedness, end-to-end', standing: 'projected', detail: 'Proposed — owed, not proven' },
      { element: 'Memristor retention / monotonicity / latency', standing: 'projected', detail: 'Phase-0 hypotheses' },
      { element: 'Numerical Substrate Partition ADR (τ = 5 s)', standing: 'ratified', detail: 'Governance commitment, not empirical' }
    ];

    this.scenario = { mode: 'nominal', intensity: 1.0 };

    this.history = {
      timestamps: [], R: [], warrant: [], admittedCount: [],
      xi: [], llc: [], violationRate: [], boxWidth: []
    };

    this._lastEntryRefresh = 0;

    // Conservation-Renormalization Layer (CRL). A coupled sensor multiplet whose
    // tracks fuse into one object; each channel has a stable SHAPE (what it is
    // looking at) and a time-varying GAIN (how loud AGC / fusion / attention
    // turns it up). The CRL renormalizes the gains to a zero-sum budget Q=0 so
    // the conserved fusion coordinate never moves while loudness is redistributed.
    this.crl = new ConservationRenormalizationLayer();
    this.sensorChannels = [
      { name: 'radar', shape: [0.97, 0.24] },
      { name: 'camera', shape: [0.60, 0.80] },
      { name: 'ultrasonic', shape: [0.30, 0.95] },
      { name: 'lidar', shape: [0.85, 0.53] },
      { name: 'map-prior', shape: [0.10, 0.99] }
    ];
    // Source's recommended step (a): verify the §3.4 proposition on the harness.
    this.crlVerification = verifyGaugeCovariance();

    // Prime derived state so getState() is valid before the first step().
    this._runCompiler();
    this._renormalizeGains();
    this._computeAdmissibleSet();
  }

  // ---- event plumbing (mirrors MultiplexerEngine) ----
  on(event, callback) { this.listeners.add({ event, callback }); }
  emit(event, data) {
    this.listeners.forEach(({ event: e, callback }) => { if (e === event) callback(data); });
  }

  // =========================================================================
  // SCENARIOS — how warrant evolves over time.
  // =========================================================================
  setScenario(mode, intensity = 1.0) {
    this.scenario.mode = mode;
    this.scenario.intensity = Math.max(0.2, Math.min(3.0, intensity));
    this.emit('scenario_changed', this.scenario);
  }

  setConservativeDiscipline(on) {
    this.conservativeDiscipline = !!on;
    this.emit('discipline_changed', this.conservativeDiscipline);
  }

  // Operator pokes — surface controls.
  raiseRisk(amount = 0.2) { this._injectWarrantDrop(amount); }
  injectDrift() { this._driftPulse = (this._driftPulse || 0) + 1.0; this.emit('drift_injected', null); }
  spoof() { this._spoofPulse = true; this.emit('spoof_injected', null); }

  _injectWarrantDrop(amount) {
    this.risk.warrant = Math.max(0, this.risk.warrant - amount);
    this.emit('warrant_drop', this.risk.warrant);
  }

  // =========================================================================
  // MAIN STEP
  // =========================================================================
  step() {
    this.tick++;
    this.simTime += this.config.dt;

    this._evolveWarrant();
    this._ingestObservations();   // Lane G + enforced causality (Lane C)
    this._computeRisk();          // R from warrant + trajectory threat
    this._renormalizeGains();     // CRL: zero-sum gain budget Q=0 (§3)
    this._runCompiler();          // Lane D: φ -> ξ, S
    this._computeAdmissibleSet(); // Lanes A/B/S0–S4: the antitone narrowing
    this._checkMonotonicity();    // the PoC headline metric
    this._ageMemory();            // Lane C: residence decay, τ=5 s boundary
    this._governDrift();          // anti-silent-drift / LLC quarantine
    this._maybeAnalogVeto();      // Lane B S4: ~32 ns measured veto
    this._recordHistory();

    // Build the snapshot once per tick (it deep-copies) and reuse it.
    const snapshot = this.getState();
    this.emit('step', snapshot);
    return snapshot;
  }

  // ---- warrant dynamics ----
  _evolveWarrant() {
    const k = this.scenario.intensity;
    let target;
    switch (this.scenario.mode) {
      case 'nominal':       target = 0.9; break;
      case 'degrading':     target = Math.max(0.1, 0.9 - 0.6 * (this.simTime / (40 / k))); break;
      case 'sensor_drift':  target = 0.65 + 0.1 * Math.sin(this.simTime * 0.4); break;
      case 'adversarial':   target = 0.35; break;
      case 'recovery':      target = Math.min(0.92, this.risk.warrant + 0.02 * k); break;
      default:              target = 0.9;
    }
    // Drift pulses transiently depress warrant.
    if (this._driftPulse > 0) { target -= 0.25; this._driftPulse = Math.max(0, this._driftPulse - 0.1); }
    // First-order relaxation toward target.
    this.risk.warrant += (target - this.risk.warrant) * 0.15;
    this.risk.warrant = Math.max(0, Math.min(1, this.risk.warrant));
  }

  // ---- Lane G sensing + Lane C enforced causality ----
  _ingestObservations() {
    // A few observations per tick; low-trajectory ("about to cross") objects
    // get their importance weight INFLATED at ingestion, before classification.
    const n = 1 + Math.floor(Math.random() * 3);
    const reflexive = this.memory.tiers[0];
    for (let i = 0; i < n; i++) {
      const lowTrajectory = Math.random() < (0.25 + 0.4 * (1 - this.risk.warrant));
      const rawWeight = 0.2 + Math.random() * 0.4;
      // Enforced causality: inflate threat BEFORE classification.
      const threat = lowTrajectory ? Math.min(1, rawWeight + 0.5) : rawWeight;
      if (lowTrajectory) this.metrics.enforcedCausalityEvents++;
      this.metrics.ingestCount++;
      reflexive.entries.push({
        id: uid('obs'),
        weight: threat,           // manipulated importance weight (one-sided up)
        rawWeight,
        threat,
        lowTrajectory,
        residence: 0,             // simulated seconds resident
        deltaJ: 0.3 + Math.random() * 0.7, // marginal learning value
        cost: 0.2 + Math.random() * 0.6,   // retrieval cost (∝ switching energy in WS-3)
        born: this.simTime,
        promoted: false
      });
    }
    // Reflexive ring buffer overwrites without ceremony.
    if (reflexive.entries.length > reflexive.capacity) {
      const dropped = reflexive.entries.length - reflexive.capacity;
      reflexive.entries.splice(0, dropped);
      this.memory.evictions += dropped;
    }
  }

  // ---- CRL: renormalize the sensor-multiplet gains to a zero-sum budget ----
  _renormalizeGains() {
    const reflexive = this.memory.tiers[0];
    const load = reflexive.entries.length / reflexive.capacity;
    const t = this.simTime;
    // Raw per-channel gains driven by the current regime. AGC / fusion-confidence
    // / learned-attention all push these around — exactly the "leak" §2 warns of.
    const gainFns = {
      radar: 1 + 1.5 * this.risk.trajectoryThreat + 0.1 * Math.random(),
      camera: 1 + 1.2 * (1 - this.risk.warrant),
      ultrasonic: 0.8 + 0.6 * this.risk.R,
      lidar: 1 + 0.9 * load,
      'map-prior': 0.7 + 0.3 * (0.5 + 0.5 * Math.sin(t * 0.5))
    };
    const signals = this.sensorChannels.map(ch => {
      const g = Math.max(0.05, gainFns[ch.name]);
      return [ch.shape[0] * g, ch.shape[1] * g]; // x_k = g_k · ŷ_k
    });
    const r = this.crl.step(signals);
    // §3.4 re-confirmed LIVE on the actual per-tick channels: (i) the conserved
    // coordinate did not move (drift ≈ 0) and (iii) Q = 0 is an exact critical
    // zero after projection (residual ≈ 0). Clause (ii) — masking blocked —
    // needs a genuine-defect example and is proved once by verifyGaugeCovariance.
    const tol = this.crl.cfg.resTol;
    const liveDriftOk = r.conservedDrift <= tol;     // clause (i), live
    const liveResidualOk = r.residual <= tol;        // clause (iii), live
    this.crlState = {
      channels: this.sensorChannels.map((ch, k) => ({
        name: ch.name,
        gainRaw: r.gains[k],
        ellRaw: r.ellRaw[k],
        ellStar: r.ellStar[k],
        gainStar: r.gainsStar[k],
        amplified: r.ellStar[k] >= 0
      })),
      Qbefore: r.Qbefore,
      Qafter: r.Qafter,
      residual: r.residual,
      conserved: r.conserved,
      conservedDrift: r.conservedDrift,
      // Static proposition (all 3 clauses, canonical multiplet) ...
      verification: this.crlVerification,
      // ... plus the live per-tick re-confirmation of clauses (i) and (iii).
      live: { driftOk: liveDriftOk, residualOk: liveResidualOk, verified: liveDriftOk && liveResidualOk }
    };
  }

  // ---- risk from warrant + trajectory threat ----
  _computeRisk() {
    const reflexive = this.memory.tiers[0];
    const maxThreat = reflexive.entries.reduce((m, e) => Math.max(m, e.threat), 0);
    this.risk.trajectoryThreat = maxThreat;
    let spoofBump = 0;
    if (this._spoofPulse) { spoofBump = 0.35; this._spoofPulse = false; }
    // Risk is high when warrant is low OR threat is high.
    const target = Math.min(1, 0.55 * (1 - this.risk.warrant) + 0.45 * maxThreat + spoofBump);
    this._prevR = this.risk.R;
    this.risk.R += (target - this.risk.R) * 0.4;
    this.risk.R = Math.max(0, Math.min(1, this.risk.R));
    this.risk.dR = this.risk.R - this._prevR;
  }

  // ---- Lane D: conservation-manifold compiler φ -> ξ, S ----
  _runCompiler() {
    // ξ: bounded, saturating log-deviation invariant correlating monotonically
    // with physical risk. Saturating via tanh so it cannot run away.
    const deviation = this.risk.R * 3.0;
    this.scalars.xi = Math.tanh(deviation) * (1 + 0.02 * (Math.random() - 0.5));
    // S: discrete structural-parity invariant. Flips at phase transitions —
    // we flip S when ξ crosses the 0.5 saturation knee.
    const knee = 0.5;
    const newS = this.scalars.xi >= knee ? -1 : 1;
    if (newS !== this.scalars.S) this.emit('parity_flip', newS);
    this.scalars.S = newS;
  }

  // ---- Lanes A/B + S0–S4: the antitone narrowing ----
  _computeAdmissibleSet() {
    const R = this.risk.R;
    // Base admissible set: a mode is admissible iff its risk threshold >= R.
    let admitted = COMMAND_MODES.filter(m => m.risk >= R || m.id === 'STOP');

    // Raw analog devices (discipline OFF) are non-monotone: stochastic
    // switching / read noise can occasionally ADD BACK a mode that risk should
    // have removed. Conservative discipline (WS-2) clamps this away.
    let deviceNoiseEnlarged = false;
    if (!this.conservativeDiscipline && Math.random() < 0.18 * this.risk.R) {
      // Try to re-admit one mode just below the cutoff (a monotonicity hazard).
      const cutoffIdx = COMMAND_MODES.findIndex(m => admitted.includes(m));
      if (cutoffIdx > 0) {
        admitted = [COMMAND_MODES[cutoffIdx - 1], ...admitted];
        deviceNoiseEnlarged = true;
      }
    }
    this._deviceNoiseEnlarged = deviceNoiseEnlarged;

    // Convex command box width = fraction of modes admitted (excluding the
    // always-on STOP), shrinking with R.
    const nonStop = COMMAND_MODES.length - 1;
    const baseWidth = Math.max(0, (admitted.length - 1) / nonStop);

    // S0–S4 cascade: each stage can only narrow. Accumulate the shrink.
    let width = baseWidth;
    const cascade = CASCADE_STAGES.map(s => {
      width = width * s.narrow;
      return { ...s, width: Math.max(0, width) };
    });

    this.admissible = {
      modes: COMMAND_MODES.map(m => ({ ...m, admitted: admitted.includes(m) })),
      admittedCount: admitted.length,
      boxWidth: cascade[cascade.length - 1].width,
      baseBoxWidth: baseWidth,
      cascade
    };
  }

  // ---- monotonicity check (PoC headline metric) ----
  _checkMonotonicity() {
    // Whenever risk rose, the admissible set must not have grown.
    if (this.risk.R > this._prevR + 1e-6) {
      this.monotonicity.checks++;
      const grew = this.admissible.admittedCount > this._prevAdmittedCount;
      if (grew) {
        this.monotonicity.violations++;
        this.monotonicity.lastViolation = {
          t: this.simTime, R: this.risk.R, prevR: this._prevR,
          from: this._prevAdmittedCount, to: this.admissible.admittedCount
        };
        this.emit('monotonicity_violation', this.monotonicity.lastViolation);
      }
    }
    this.monotonicity.violationRate =
      this.monotonicity.checks > 0
        ? this.monotonicity.violations / this.monotonicity.checks
        : 0;
    this._prevAdmittedCount = this.admissible.admittedCount;
  }

  // ---- Lane C: residence aging + τ=5 s boundary ----
  _ageMemory() {
    const dt = this.config.dt;
    const [reflexive, tactical, strategic] = this.memory.tiers;

    // Age everything; analog tiers relax (weights decay).
    this.memory.tiers.forEach(tier => {
      tier.entries.forEach(e => {
        e.residence += dt;
        if (tier.substrate === 'analog') {
          // Stretched-exponential-ish decay; errors age out before blast radius grows.
          e.weight *= Math.exp(-dt / Math.max(0.05, tier.tau));
        }
      });
    });

    // Shadow-price admission: retain only entries clearing ΔJ/C ≥ λ_k.
    this.memory.tiers.forEach(tier => {
      const before = tier.entries.length;
      tier.entries = tier.entries.filter(e => {
        const value = e.deltaJ / Math.max(0.01, e.cost);
        const cleared = value >= tier.shadowPrice;
        if (cleared) this.metrics.retrievals++; else this.metrics.retrievalsDenied++;
        // Principled erasure (Temporal_State_Management Part II): an analog entry
        // is erased when its correlation decays below ε (weight is the C(t) proxy)
        // OR it exceeds the τ-hierarchy bound t > α·τ (α≈3 ⇒ ~95% decay, §9.3.1).
        // Conserved / re-quantized (digitized) state is a conservation law (§9.6.3)
        // — permanent memory, never aged out.
        const EPS = 0.05, ALPHA = 3;
        const agedOut = tier.substrate === 'analog' && !e.digitized &&
          (e.weight < EPS || e.residence > ALPHA * tier.tau);
        return cleared && !agedOut;
      });
      this.memory.evictions += Math.max(0, before - tier.entries.length);
      // Shadow price tracks load (more pressure => higher bar).
      const load = tier.entries.length / tier.capacity;
      tier.shadowPrice = tier.baseShadowPrice * (0.7 + 0.8 * load);
    });

    // Promote reflexive -> tactical (cheap), and tactical -> strategic at the
    // τ = 5 s boundary, where analog must be RE-QUANTIZED into digital. Crossing
    // is gated by cross-modality CONFIRMATION (high learning value ΔJ/C), not by
    // the decayed analog weight — a corrupt datum loses value and ages out first.
    this._promote(reflexive, tactical, e => e.residence >= reflexive.tau && e.weight > 0.2, false);
    this._promote(tactical, strategic,
      e => e.residence >= this.memory.boundaryTau && (e.deltaJ / e.cost) >= 1.0, true);
  }

  _promote(from, to, predicate, crossesBoundary) {
    const survivors = [];
    from.entries.forEach(e => {
      if (predicate(e) && to.entries.length < to.capacity) {
        const promoted = { ...e, promoted: true, residence: 0 };
        if (crossesBoundary) {
          // No analog state crosses τ = 5 s: re-quantize into Posit/quire,
          // re-admit under cross-modality confirmation. Weight only rounds UP
          // (safe-side rounding) — a threat may rise but never silently fall.
          promoted.weight = Math.ceil(e.weight * 8) / 8;
          promoted.digitized = true;
          this.memory.requantizations++;
          this.memory.promotions++;
        }
        to.entries.push(promoted);
      } else {
        survivors.push(e);
      }
    });
    from.entries = survivors;
  }

  // ---- anti-silent-drift / LLC quarantine ----
  _governDrift() {
    const strategic = this.memory.tiers[2];
    // LLC estimate rises with the rate of strategic consolidation (a proxy for
    // developmental phase transitions / capability change).
    const consolidationPressure = strategic.entries.filter(e => e.digitized).length / strategic.capacity;
    const target = 1.0 + 2.0 * consolidationPressure + (this._driftPulse || 0);
    const prev = this.scalars.llc;
    this.scalars.llc += (target - this.scalars.llc) * 0.35;
    // A simultaneous LLC jump => quarantine the consolidation (do not commit).
    const jump = this.scalars.llc - prev > 0.25;
    this.scalars.llcJump = jump;
    if (jump) {
      this.scalars.quarantines++;
      // Quarantined consolidations are NOT committed: drop the freshest digitized
      // entry back out of the strategic tier (logged, challengeable).
      const idx = strategic.entries.map(e => e.digitized).lastIndexOf(true);
      if (idx >= 0) strategic.entries.splice(idx, 1);
      this.emit('llc_quarantine', { t: this.simTime, llc: this.scalars.llc });
    }
  }

  // ---- Lane B S4: ~32 ns measured analog veto ----
  _maybeAnalogVeto() {
    // A sharp risk spike arms the electrically-isolated analog guard, which
    // reaches the actuator in ~32 ns (measured) — well inside the digital
    // decision window — before any classifier resolves the scene.
    const spike = this.risk.dR > 0.18 || (this.risk.R > 0.85 && Math.random() < 0.4);
    this.veto.armed = spike;
    if (spike) {
      this.veto.analogVetoCount++;
      this.veto.lastVetoLatencyNs = this.config.measuredVetoNs;
      this.emit('analog_veto', { t: this.simTime, latencyNs: this.config.measuredVetoNs });
    }
  }

  _recordHistory() {
    const h = this.history;
    h.timestamps.push(this.simTime);
    h.R.push(this.risk.R);
    h.warrant.push(this.risk.warrant);
    h.admittedCount.push(this.admissible.admittedCount);
    h.xi.push(this.scalars.xi);
    h.llc.push(this.scalars.llc);
    h.violationRate.push(this.monotonicity.violationRate);
    h.boxWidth.push(this.admissible.boxWidth);

    const max = this.config.historyLength;
    if (h.timestamps.length > max) {
      const excess = h.timestamps.length - max;
      Object.values(h).forEach(arr => arr.splice(0, excess));
    }
  }

  getState() {
    return {
      tick: this.tick,
      simTime: this.simTime,
      running: this.running,
      scenario: this.scenario,
      conservativeDiscipline: this.conservativeDiscipline,
      risk: { ...this.risk },
      lanes: STACK_LANES,
      admissible: this.admissible,
      monotonicity: { ...this.monotonicity },
      memory: {
        boundaryTau: this.memory.boundaryTau,
        promotions: this.memory.promotions,
        evictions: this.memory.evictions,
        requantizations: this.memory.requantizations,
        tiers: this.memory.tiers.map(t => ({
          id: t.id, label: t.label, tau: t.tau, substrate: t.substrate,
          store: t.store, predicate: t.predicate, capacity: t.capacity,
          shadowPrice: t.shadowPrice,
          entries: t.entries.map(e => ({ ...e }))
        }))
      },
      scalars: { ...this.scalars },
      veto: { ...this.veto },
      crl: this.crlState,
      ledger: this.ledger,
      metrics: { ...this.metrics },
      history: {
        timestamps: [...this.history.timestamps],
        R: [...this.history.R],
        warrant: [...this.history.warrant],
        admittedCount: [...this.history.admittedCount],
        xi: [...this.history.xi],
        llc: [...this.history.llc],
        violationRate: [...this.history.violationRate],
        boxWidth: [...this.history.boxWidth]
      }
    };
  }

  start() { this.running = true; this.emit('started', null); }
  stop() { this.running = false; this.emit('stopped', null); }

  reset() {
    const disc = this.conservativeDiscipline;
    const scn = { ...this.scenario };
    const listeners = this.listeners; // preserve subscriptions across reset
    Object.assign(this, new BoundedAutonomyStack(this.config));
    this.listeners = listeners;
    this.conservativeDiscipline = disc;
    this.scenario = scn;
    this.emit('reset', null);
  }
}
