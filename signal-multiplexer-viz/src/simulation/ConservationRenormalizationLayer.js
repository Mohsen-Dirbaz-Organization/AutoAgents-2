/**
 * ConservationRenormalizationLayer.js  — the CRL
 * =========================================================================
 * Faithful implementation of the Conservation-Renormalization Layer from
 * "Conservation-Renormalization for Real-Time Adaptive Autonomy" (GHOST
 * internal research synthesis, v1.0 · 2026-06-06), §3.
 *
 * The substrate already enforces safety as a representational property, but it
 * lacks a *dynamic law that keeps conserved coordinates invariant while signal
 * gains move*. The CRL supplies that law: it turns amplification/attenuation
 * into a CONSERVED quantity through a zero-sum gain budget, so the conserved
 * sector never moves and a compensating gain can no longer mask a real defect.
 *
 * Mechanisms implemented here (section references are to the source):
 *   §3.1 Gauge factorization        x_k = g_k · ŷ_k ;  c reads only the shape ŷ.
 *   §3.2 Zero-sum gain budget        Q = Σ_k w_k ℓ_k = 0,   ℓ_k = log g_k.
 *   §3.3 Renormalization operator    ℓ*_k = ℓ̃_k − (Σ w_m ℓ̃_m)/(Σ w_m)
 *                                    (exact weighted-mean removal = projection
 *                                     onto the zero-sum hyperplane {Σ w ℓ = 0}).
 *   §3.4 Gauge-covariance prop.      c(Rx) = c(x); the graded defect depends
 *                                    only on shape+sign, both R-invariant, so a
 *                                    defect in the null set N stays in N
 *                                    (masking and inflation both blocked).
 *
 * The operator is anytime and non-expansive: a one-pass averaged subtraction is
 * 1-Lipschitz, returning a feasible (zero-sum) gain at every step. Per-tick cost
 * is O(|M|) in the multiplet size — a weighted sum + a broadcast subtract — so
 * it adds no asymptotic complexity (§3.6).
 *
 * STANDING: per the source's §8, the CRL is a *research proposal* presented as a
 * contestable claim. This module is the instrument the source's recommended
 * step (a) calls for — "implement the CRL on the simulation harness and verify
 * the Proposition of §3.4 empirically" — not a claim that it is field-validated.
 * =========================================================================
 */

/**
 * tol_conserve — DERIVED from the arithmetic format, not chosen.
 * (EPU Companion, Master Index open obligations P1: "Conservation tolerance per
 * check, derived from the arithmetic format".) The projection subtracts a
 * weighted mean over M channels of log-gains bounded by ln(maxGain); accumulated
 * rounding in IEEE-754 binary64 is O(M · eps · |ell|max). The constant 8 covers
 * the sum, the broadcast subtract, the exp/log round-trip and the norm.
 */
export function tolConserve(M = 5, maxGain = 1e3) {
  const ellMax = Math.log(Math.max(maxGain, Math.E));
  return 8 * M * Number.EPSILON * Math.max(1, ellMax);
}

// ---- small vector helpers (conserved metric = Euclidean) ----
export function vnorm(v) {
  let s = 0;
  for (let i = 0; i < v.length; i++) s += v[i] * v[i];
  return Math.sqrt(s);
}
export function vunit(v) {
  const n = vnorm(v);
  return n === 0 ? v.map(() => 0) : v.map((x) => x / n);
}
export function vscale(v, a) { return v.map((x) => x * a); }
function vdiffNorm(a, b) {
  let s = 0;
  for (let i = 0; i < a.length; i++) { const d = a[i] - b[i]; s += d * d; }
  return Math.sqrt(s);
}

/**
 * §3.1 — Gauge factorization. Split a channel signal into its gain (norm in the
 * conserved metric) and its shape (unit-normalized). The conserved coordinate
 * is defined to read ONLY the shape, so it is invariant to any positive gain.
 */
export function gaugeFactorize(x) {
  const g = vnorm(x);
  return { gain: g, shape: vunit(x) };
}

/** ℓ_k = log g_k. Gains must be strictly positive. */
export function logGain(g) { return Math.log(Math.max(g, Number.MIN_VALUE)); }

/** Weighted mean of the log-gains, Σ w_k ℓ_k / Σ w_k. */
export function weightedMean(ell, w) {
  let num = 0, den = 0;
  for (let i = 0; i < ell.length; i++) { num += w[i] * ell[i]; den += w[i]; }
  return den === 0 ? 0 : num / den;
}

/**
 * §3.2 — Zero-sum gain budget Q = Σ_k w_k ℓ_k. A conserved charge: any boost
 * (ℓ_k ↑) must be financed by a cut elsewhere (ℓ_j ↓). The multiplet may
 * redistribute loudness freely but can neither manufacture nor destroy it.
 */
export function gainBudget(ell, w) {
  let q = 0;
  for (let i = 0; i < ell.length; i++) q += w[i] * ell[i];
  return q;
}

/**
 * §3.3 — The renormalization operator R: exact weighted-mean removal, the
 * projection onto the zero-sum hyperplane {Σ w ℓ = 0}. Non-expansive (averaged
 * subtraction is 1-Lipschitz); idempotent; anytime.
 */
export function zeroSumProject(ell, w) {
  const mu = weightedMean(ell, w);
  return ell.map((l) => l - mu);
}

/**
 * §1.3 / §3.4(ii) — graded-symmetry defect of the bilinear (outer-product)
 * witness:  δ = μ(a,b) − σ·μ(b,a),  μ(a,b) = a bᵀ.  Returned as a Frobenius
 * norm. When computed on SHAPES (gauge-fixed a,b) it is invariant to gain; on
 * raw signals a compensating gain can scale it (masking/inflation).
 */
export function gradedDefectNorm(a, b, sigma = 1) {
  let s = 0;
  for (let i = 0; i < a.length; i++) {
    for (let j = 0; j < b.length; j++) {
      const d = a[i] * b[j] - sigma * b[i] * a[j];
      s += d * d;
    }
  }
  return Math.sqrt(s);
}

export class ConservationRenormalizationLayer {
  /**
   * @param {object} cfg
   *   weights   — per-channel budget weights w_k (default uniform)
   *   resTol    — tolerance for the |Q| critical-zero check
   */
  constructor(cfg = {}) {
    // tol_conserve is DERIVED (see tolConserve) unless explicitly overridden.
    this.cfg = { resTol: cfg.resTol ?? tolConserve(cfg.M ?? 5), ...cfg };
    this.last = null;
  }

  /**
   * One renormalization step over a coupled multiplet.
   *
   * @param {number[][]} signals  raw channel vectors x_k (each a small vector).
   * @param {number[]}   weights  optional per-channel weights w_k.
   * @returns {{
   *   gains:number[], shapes:number[][],
   *   ellRaw:number[], ellStar:number[],
   *   gainsStar:number[], signalsStar:number[][],
   *   Qbefore:number, Qafter:number, residual:number,
   *   conserved:boolean, conservedDrift:number
   * }}
   */
  step(signals, weights) {
    const M = signals.length;
    const w = weights && weights.length === M ? weights : new Array(M).fill(1);

    // §3.1 factor each channel into gain + shape.
    const factored = signals.map(gaugeFactorize);
    const gains = factored.map((f) => f.gain);
    const shapes = factored.map((f) => f.shape);

    // §3.2 raw log-gain budget (generally ≠ 0).
    const ellRaw = gains.map(logGain);
    const Qbefore = gainBudget(ellRaw, w);

    // §3.3 project to the zero-sum hyperplane.
    const ellStar = zeroSumProject(ellRaw, w);
    const Qafter = gainBudget(ellStar, w);
    const residual = Math.abs(Qafter);

    // Renormalized gains and signals; the SHAPE is untouched (gauge-fixed), so
    // the conserved coordinate c (read from shapes) does not move.
    const gainsStar = ellStar.map((l) => Math.exp(l));
    const signalsStar = shapes.map((yhat, k) => vscale(yhat, gainsStar[k]));

    // §3.4(i) conserved-coordinate drift.
    //
    // STANDING: **constructed**, not established. signalsStar[k] is shapes[k]
    // scaled by the strictly positive gainsStar[k], so vunit(signalsStar[k]) is
    // shapes[k] up to rounding — this quantity is zero BY CONSTRUCTION and the
    // comparison cannot fail. It is reported as a numerical DIAGNOSTIC (it does
    // detect a rounding/implementation fault) and must never be presented as
    // empirical verification of the proposition. See noOpAudit() and
    // maskingProbe() for the falsifiable content of §3.4.
    // (EPU Companion, Deck B retirement #3: a check that cannot fail is retired
    // as a guaranteed-pass no-op.)
    let conservedDrift = 0;
    for (let k = 0; k < M; k++) {
      conservedDrift += vdiffNorm(shapes[k], vunit(signalsStar[k]));
    }

    const out = {
      gains, shapes, ellRaw, ellStar, gainsStar, signalsStar,
      weights: w, Qbefore, Qafter, residual,
      // Likewise **constructed**: zeroSumProject removes the weighted mean, so
      // Q after projection is zero by algebraic identity, not by measurement.
      conserved: residual <= this.cfg.resTol,
      conservedDrift,
      // Explicit standing so consumers cannot mistake a definitional identity
      // for evidence.
      standing: { conservedDrift: 'constructed', residual: 'constructed' }
    };
    this.last = out;
    return out;
  }

  getState() { return this.last; }
}

/**
 * §3.4 — empirical verification of the gauge-covariance proposition. Returns a
 * structured report so the harness (and the visualization) can display PASS/FAIL
 * per clause. This is the source's recommended step (a).
 *
 * (i)   c(Rx) = c(x): the conserved coordinate (shape) is invariant under R.
 * (ii)  a defect that lay in the null set N before R still lies in N after R
 *       (a compensating gain cannot mask it); and a genuine defect outside N
 *       cannot be inflated into N by gain.
 * (iii) Q = 0 is a critical zero, exact after projection.
 */
export function verifyGaugeCovariance(opts = {}) {
  const epsilon = opts.epsilon ?? 0.05;
  const tol = opts.tol ?? 1e-9;
  const crl = new ConservationRenormalizationLayer({ resTol: tol });

  // A coupled multiplet (radar / camera / ultrasonic / lidar / map-prior),
  // each a 2-D evidence vector with its own arbitrary gain.
  const signals = [
    [3.0, 0.4],
    [0.2, 1.7],
    [1.1, 1.1],
    [2.5, 0.1],
    [0.05, 0.9]
  ];
  const r = crl.step(signals);

  // (i) conserved-coordinate invariance.
  const clause_i = r.conservedDrift < 1e-9;

  // (iii) Q == 0 critical zero after projection.
  const clause_iii = r.residual <= tol;

  // (ii) masking blocked. Take a GENUINE defect (outside the null set N). A
  // compensating gain can shrink it on the RAW signal until the raw band is
  // fooled (a false accept) — but the SHAPE defect is gain-invariant and stays
  // outside N, because the band is now read in a sector the gain cannot reach.
  const a = [1.0, 0.5], b = [1.0, 0.0];
  const sigma = 1;
  const defectShape = gradedDefectNorm(vunit(a), vunit(b), sigma);
  const genuine = defectShape > epsilon;                 // real violation, outside N

  const maskGain = 0.05;                                  // adversarial compensating gain
  const aRaw = vscale(a, maskGain);
  const defectRawMasked = gradedDefectNorm(aRaw, b, sigma);
  const rawWouldMask = defectRawMasked <= epsilon;        // raw band fooled (false accept)

  const defectShapeAfter = gradedDefectNorm(vunit(aRaw), vunit(b), sigma);
  const shapeStillGenuine = defectShapeAfter > epsilon;   // shape band NOT fooled
  const shapeInvariant = Math.abs(defectShape - defectShapeAfter) < 1e-9;

  const clause_ii = genuine && rawWouldMask && shapeStillGenuine && shapeInvariant;

  return {
    clause_i,
    clause_ii,
    clause_iii,
    pass: clause_i && clause_ii && clause_iii,
    // Per-clause STANDING (EPU Companion claim ladder). Clauses (i) and (iii)
    // are true by definition/algebra — they support design semantics only.
    // Clause (ii) is the falsifiable content: it compares a raw-band detector
    // against a shape-band detector on an adversarial input, and it CAN fail.
    standing: {
      clause_i: 'constructed',
      clause_ii: 'established-in-sim',
      clause_iii: 'constructed'
    },
    detail: {
      conservedDrift: r.conservedDrift,
      Qbefore: r.Qbefore,
      Qafter: r.Qafter,
      residual: r.residual,
      defectShape,
      defectShapeAfter,
      defectRawMasked,
      epsilon
    }
  };
}

/**
 * maskingProbe — the FALSIFIABLE runtime check for §3.4(ii).
 *
 * An adversary applies a compensating gain chosen to hide a genuine graded
 * defect. A band that reads the RAW signal is fooled (false accept); a band that
 * reads the gauge-fixed SHAPE is not. Unlike conservedDrift/residual this check
 * has a real truth value: point `readShape` at the raw signal and it fails.
 *
 * @param {{a?:number[], b?:number[], epsilon?:number, maskGain?:number,
 *          readShape?:boolean}} opts
 *        readShape=false sabotages the detector (used by noOpAudit).
 */
export function maskingProbe(opts = {}) {
  const a = opts.a ?? [1.0, 0.5];
  const b = opts.b ?? [1.0, 0.0];
  const epsilon = opts.epsilon ?? 0.05;
  const maskGain = opts.maskGain ?? 0.05;
  const readShape = opts.readShape !== false;
  const sigma = 1;

  const truthDefect = gradedDefectNorm(vunit(a), vunit(b), sigma);
  const genuine = truthDefect > epsilon;          // ground truth: a real defect

  const aRaw = vscale(a, maskGain);               // adversarial compensating gain
  const measured = readShape
    ? gradedDefectNorm(vunit(aRaw), vunit(b), sigma)   // gauge-fixed band
    : gradedDefectNorm(aRaw, b, sigma);                // sabotaged: raw band

  const detected = measured > epsilon;
  return {
    genuine,
    detected,
    masked: genuine && !detected,
    truthDefect,
    measured,
    epsilon,
    maskGain,
    readShape,
    // The probe PASSES only when a genuine defect is still detected after the
    // adversary's compensating gain.
    pass: genuine && detected
  };
}

/**
 * noOpAudit — the tautology detector the EPU Companion's Deck B retirement #3
 * demands ("a check that cannot fail"). Each registered check is re-run against
 * a deliberately SABOTAGED variant; a check that still passes has no truth value
 * and is reported as guaranteed-pass.
 *
 * @returns {{checks:object[], guaranteedPass:number, falsifiable:number}}
 */
export function noOpAudit() {
  const crl = new ConservationRenormalizationLayer();
  const signals = [[3.0, 0.4], [0.2, 1.7], [1.1, 1.1], [2.5, 0.1], [0.05, 0.9]];
  const r = crl.step(signals);
  const tol = crl.cfg.resTol;

  const checks = [
    {
      id: 'clause_i_live',
      label: '§3.4(i) live conserved-coordinate drift ≈ 0',
      passesNormally: r.conservedDrift <= tol,
      // Sabotage: the quantity is vunit(scale(shape, g>0)) vs shape. There is no
      // input for which it differs — the sabotage is that the comparison is
      // structurally identical, so it passes unconditionally.
      passesWhenSabotaged: true,
      verdict: 'guaranteed-pass',
      note: 'Zero by construction: unit(shape·g) ≡ shape for any g>0. Diagnostic only.'
    },
    {
      id: 'clause_iii_live',
      label: '§3.4(iii) live |Q| residual ≈ 0 after projection',
      passesNormally: r.residual <= tol,
      passesWhenSabotaged: true,
      verdict: 'guaranteed-pass',
      note: 'Zero by algebraic identity: projection removes the weighted mean. Diagnostic only.'
    },
    {
      id: 'clause_ii_masking',
      label: '§3.4(ii) compensating gain cannot mask a genuine defect',
      passesNormally: maskingProbe().pass,
      // Sabotage: read the raw signal instead of the gauge-fixed shape.
      passesWhenSabotaged: maskingProbe({ readShape: false }).pass,
      verdict: null,
      note: 'Falsifiable: reading the raw band instead of the shape band makes it fail.'
    }
  ];
  for (const c of checks) {
    if (c.verdict === null) {
      c.verdict = c.passesWhenSabotaged ? 'guaranteed-pass' : 'falsifiable';
    }
  }
  return {
    checks,
    guaranteedPass: checks.filter((c) => c.verdict === 'guaranteed-pass').length,
    falsifiable: checks.filter((c) => c.verdict === 'falsifiable').length
  };
}

export default ConservationRenormalizationLayer;
