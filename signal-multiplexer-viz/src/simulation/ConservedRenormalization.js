/**
 * ConservedRenormalization.js
 * ----------------------------------------------------------------------------
 * Conserved-Quantity Renormalization (CQR) reference kernel.
 *
 * Zero-sum amplification / attenuation of memory-reservoir signals by
 * projection onto a conservation manifold (the scaled probability simplex),
 * driven by a renormalization-group flow whose running coupling is the
 * corpus's validated, scale-free statistic  xi = ln(k_i / Lambda_G).
 *
 * Design contract (see RENORMALIZATION_FRAMEWORK.md):
 *   - Conservation is STRUCTURAL: sum(w) = M holds by construction, not by
 *     a penalty term. (Dalton simplex / open-water transform.)
 *   - The step is NON-EXPANSIVE (1-Lipschitz): rate-clamp-last o projection,
 *     both 1-Lipschitz, so corrupted salience cannot be amplified.
 *   - Disagreeing salience sources compose by MEET (tighter), never average.
 *   - FAIL-CLOSED: on conservation-check failure, freeze the reservoir
 *     (minimal-risk memory configuration). Freezing cannot loosen any
 *     downstream admissible set (memory-monotonicity invariant).
 *
 * Complexity: O(N + log(1/eps)) per step with the expected-linear projection
 *   (same order as the original elliptic/AGM surrogate). See Part 4 of the doc.
 *
 * NOTE ON STANDING: xi/S are validated AT BIN LEVEL on the gasification corpus
 *   only. Their predictive alignment in the MEMORY domain is a HYPOTHESIS
 *   (open obligation O-3). This kernel is the instrument to test it, not a
 *   claim that it already holds.
 * ----------------------------------------------------------------------------
 */

const DEFAULTS = {
  mass: 1.0,            // M: conserved reservoir budget
  capacity: null,       // C: target active-trace count for S; null => N
  learningRate: 0.1,    // eta
  maxRate: 0.2,         // rate-clamp-last: max |w_i - w_i_prev| per step
  xiClamp: 1.0,         // bound on the running coupling's effect
  activeEps: 1e-6,      // support threshold for S
  consTol: 1e-9,        // CONS.CHK tolerance on |sum(w) - M|
};

/**
 * Euclidean projection onto the scaled simplex { w : w_i >= 0, sum w_i = M }.
 * Sort-based (Duchi et al. 2008). O(N log N) worst case; an expected-O(N)
 * pivot variant exists and is the one assumed in the complexity claim.
 * Projection onto a convex set is 1-Lipschitz (non-expansive).
 */
export function projectToSimplex(v, M = 1.0) {
  const n = v.length;
  if (n === 0) return [];
  const u = [...v].sort((a, b) => b - a);
  let cssv = 0;
  let rho = -1;
  let theta = 0;
  for (let i = 0; i < n; i++) {
    cssv += u[i];
    const t = (cssv - M) / (i + 1);
    if (u[i] - t > 0) {
      rho = i;
      theta = t;
    }
  }
  if (rho < 0) theta = (cssv - M) / n; // degenerate guard
  return v.map((x) => Math.max(x - theta, 0));
}

/** Geometric mean of strictly-positive gains: exp(mean(ln k)). O(N). */
export function geometricMean(k) {
  const n = k.length;
  if (n === 0) return 1;
  let s = 0;
  for (let i = 0; i < n; i++) s += Math.log(Math.max(k[i], Number.MIN_VALUE));
  return Math.exp(s / n);
}

/** Running coupling xi_i = ln(k_i / Lambda_G). Two-tailed; scale-free. O(N). */
export function runningCoupling(k, Lg) {
  return k.map((ki) => Math.log(Math.max(ki, Number.MIN_VALUE) / Lg));
}

/** Rate-clamp-last: bound per-coordinate change. 1-Lipschitz in the difference. */
function rateClamp(wNew, wPrev, maxRate) {
  return wNew.map((wi, i) => {
    const d = wi - wPrev[i];
    const c = Math.max(-maxRate, Math.min(maxRate, d));
    return wPrev[i] + c;
  });
}

/** Discrete phase label S = 1[ n_z != n_p ], n_z = #active traces. */
export function squarityIndex(w, capacity, eps) {
  let nz = 0;
  for (let i = 0; i < w.length; i++) if (w[i] > eps) nz++;
  const np = capacity == null ? w.length : capacity;
  return { S: nz !== np ? 1 : 0, activeCount: nz, capacity: np };
}

/** Zero-sum verification (CONS.CHK analog): |sum(w) - M| < tol. */
export function conservationResidual(w, M) {
  let s = 0;
  for (let i = 0; i < w.length; i++) s += w[i];
  return Math.abs(s - M);
}

export class ConservedRenormalization {
  /**
   * @param {number} n      number of memory traces
   * @param {object} config see DEFAULTS
   */
  constructor(n, config = {}) {
    this.cfg = { ...DEFAULTS, ...config };
    const M = this.cfg.mass;
    // Initialize on the manifold: uniform mass (max-entropy reservoir).
    this.w = new Array(n).fill(M / n);
    this.prevS = squarityIndex(this.w, this.cfg.capacity, this.cfg.activeEps).S;
    this.lastXi = new Array(n).fill(0);
    this.lastLg = 1;
    this.frozen = false;
  }

  /**
   * One CQR step.
   *
   * @param {number[]} gains  per-trace salience gains k_i (e.g. from the
   *                          existing elliptic/AGM surrogate). Strictly > 0.
   * @returns {{w:number[], xi:number[], Lg:number, S:number,
   *            transition:boolean, conserved:boolean, frozen:boolean}}
   */
  step(gains) {
    const { learningRate, maxRate, xiClamp, mass, capacity, activeEps, consTol } = this.cfg;
    const wPrev = this.w;

    // 1. Running scale + coupling (reuse the surrogate's gains; no new kernel).
    const Lg = geometricMean(gains);
    const xi = runningCoupling(gains, Lg);

    // 2. Additive evidence update, magnitude first.
    //    xi > 0 amplifies, xi < 0 attenuates; bounded by xiClamp.
    const wUpdated = wPrev.map((wi, i) => {
      const drive = Math.max(-xiClamp, Math.min(xiClamp, xi[i]));
      return wi + learningRate * drive;
    });

    // 3. Rate-clamp-last (bounded jerk), then 4. project (restore conservation).
    //    Both maps are 1-Lipschitz => the composed step is non-expansive.
    const wClamped = rateClamp(wUpdated, wPrev, maxRate);
    const wProj = projectToSimplex(wClamped, mass);

    // 5. CONS.CHK; fail-closed on violation (freeze = minimal-risk config).
    const residual = conservationResidual(wProj, mass);
    if (residual > consTol) {
      this.frozen = true;
      return this._emit(wPrev, xi, Lg, /*conserved*/ false);
    }

    this.w = wProj;
    this.frozen = false;
    return this._emit(wProj, xi, Lg, /*conserved*/ true);
  }

  _emit(w, xi, Lg, conserved) {
    const { S, activeCount, capacity } = squarityIndex(w, this.cfg.capacity, this.cfg.activeEps);
    const transition = S !== this.prevS; // gap-closing / phase-change event
    this.prevS = S;
    this.lastXi = xi;
    this.lastLg = Lg;
    return {
      w: [...w],
      xi: [...xi],
      Lg,
      S,
      activeCount,
      capacity,
      transition,
      conserved,
      frozen: this.frozen,
    };
  }

  /** Current reservoir state (defensive copy). */
  getState() {
    return {
      w: [...this.w],
      S: this.prevS,
      Lg: this.lastLg,
      frozen: this.frozen,
      conservationResidual: conservationResidual(this.w, this.cfg.mass),
    };
  }
}

export default ConservedRenormalization;
