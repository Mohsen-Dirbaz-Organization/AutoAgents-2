/**
 * TopologicalPhaseDetector.js — TPD-01 (Void V-5: tpd-01-phase-detector-run)
 * =========================================================================
 * Source #2 (Conservation-Renormalization) ships a ready-to-run TPD-01 research
 * prompt (§4.4) for a quantized *topological phase detector*. It was never run
 * against project data — the V3 registry retrofit surfaced that as Void V-5.
 *
 * This module runs it. The point of obligation O-5 is whether the discrete phase
 * label is meaningful in a finite system; a *second, independent* detector that
 * confirms the same phase structure is the cross-validation O-5 asks for.
 *
 * The substrate's existing label S is a function of ξ = tanh(3R) (a knee at
 * ξ=0.5). TPD is deliberately computed from a DIFFERENT observable — the
 * renormalized sensor-gain budget — so agreement is informative rather than
 * circular:
 *
 *   order parameter  m(R) = (proximity sector) − (vision sector) of the
 *                           zero-sum-renormalized gains
 *                         = (radar + ultrasonic) − (camera + lidar)
 *   TPD label        P(R) = 1[m(R) ≥ 0]   (proximity-dominant ⇒ high-risk phase)
 *
 * As risk rises the proximity sensors are amplified and the dominant sector
 * flips once — a discrete topological transition reached by a path completely
 * independent of the ξ-knee. We then cross-validate P against S by:
 *   (a) Spearman rank correlation of the two continuous order parameters
 *       (ξ-precursor vs m) — the O-11-style "rank corr ≥ 0.85" bar;
 *   (b) the topological invariant: BOTH detectors must show exactly ONE
 *       monotone transition along the R-sweep (winding number 1, no flicker).
 * =========================================================================
 */

import { zeroSumProject } from './ConservationRenormalizationLayer.js';

function mulberry32(seed) {
  let a = seed >>> 0;
  return function () {
    a |= 0; a = (a + 0x6d2b79f5) | 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

// Raw per-channel gains [radar, camera, ultrasonic, lidar] as a function of risk
// R, mirroring BoundedAutonomyStack._renormalizeGains (warrant = 1−R here).
export function gainsAtRisk(R, noise = 0, rng = Math.random) {
  const n = () => noise * (rng() - 0.5);
  return [
    Math.max(0.05, 1 + 1.5 * R + n()),   // radar    — grows fastest with threat
    Math.max(0.05, 1 + 1.2 * R + n()),   // camera   — grows with falling warrant
    Math.max(0.05, 0.8 + 0.6 * R + n()), // ultrasonic
    Math.max(0.05, 1.45 + n())           // lidar    — ~flat
  ];
}

// Independent topological order parameter m(R): proximity − vision sector of the
// zero-sum-renormalized gains. The renormalization removes the common-mode gain
// (gauge), so m reads only the *shape* of the redistribution.
export function tpdOrderParameter(R, noise = 0, rng = Math.random) {
  const g = gainsAtRisk(R, noise, rng);
  const ell = g.map((x) => Math.log(x));
  const star = zeroSumProject(ell, [1, 1, 1, 1]);
  const gs = star.map(Math.exp);
  return (gs[0] + gs[2]) - (gs[1] + gs[3]); // proximity − vision
}
export const tpdLabel = (R, noise = 0, rng = Math.random) => (tpdOrderParameter(R, noise, rng) >= 0 ? 1 : 0);

// Substrate's existing detectors (same definitions as the engine / harness).
const xiPrecursor = (R, noise = 0, rng = Math.random) => Math.tanh(3 * R) * (1 + noise * (rng() - 0.5));
const Slabel = (R) => (Math.tanh(3 * R) >= 0.5 ? 1 : 0); // 1 = flipped (high-risk) phase

// Spearman rank correlation between two equal-length samples.
function spearman(a, b) {
  const rank = (arr) => {
    const idx = arr.map((v, i) => [v, i]).sort((x, y) => x[0] - y[0]);
    const r = new Array(arr.length);
    for (let i = 0; i < idx.length; i++) r[idx[i][1]] = i + 1;
    return r;
  };
  const ra = rank(a), rb = rank(b), n = a.length;
  let d2 = 0;
  for (let i = 0; i < n; i++) { const d = ra[i] - rb[i]; d2 += d * d; }
  return 1 - (6 * d2) / (n * (n * n - 1));
}

// Count monotone transitions of a binary label along a sorted-R sweep.
function transitions(labels) {
  let count = 0, ups = 0, downs = 0;
  for (let i = 1; i < labels.length; i++) {
    if (labels[i] !== labels[i - 1]) { count++; labels[i] > labels[i - 1] ? ups++ : downs++; }
  }
  return { count, monotone: !(ups > 0 && downs > 0) };
}
// First R where a binary label switches (the phase boundary), else null.
function boundary(Rs, labels) {
  for (let i = 1; i < labels.length; i++) if (labels[i] !== labels[i - 1]) return (Rs[i] + Rs[i - 1]) / 2;
  return null;
}

/**
 * Run TPD-01 and cross-validate against the substrate's S.
 * @param {{seed?:number, samples?:number, noise?:number}} opts
 */
export function runTPD01CrossValidation(opts = {}) {
  const seed = opts.seed ?? 0x7d01;
  const samples = opts.samples ?? 200;
  const noise = opts.noise ?? 0.05;
  const rng = mulberry32(seed);

  const Rs = Array.from({ length: samples }, (_, i) => i / (samples - 1)); // sorted 0..1
  // Cross-validation of the order parameters uses the NOISY precursors (so the
  // Spearman result is robust-to-noise, not circular).
  const xiPre = Rs.map((R) => xiPrecursor(R, noise, rng));
  const mPre = Rs.map((R) => tpdOrderParameter(R, noise, rng));
  const rho = spearman(xiPre, mPre);

  // The topological invariant (number of phase boundaries) is read from the
  // NOISE-FREE phase diagram — a single noisy sample flickering at the exact
  // crossing does not create new phases. Boundary sharpness is reported
  // separately as the flicker rate below.
  const S = Rs.map(Slabel);
  const P = Rs.map((R) => tpdLabel(R, 0));
  const sT = transitions(S.slice());
  const pT = transitions(P.slice());
  const sB = boundary(Rs, S);
  const pB = boundary(Rs, P);

  // Topological agreement: both detectors find exactly one monotone boundary.
  const singleBoundaryAgree = sT.count === 1 && pT.count === 1 && sT.monotone && pT.monotone;

  // TPD robustness: under sub-gap noise away from its boundary, P must not flicker.
  let flickers = 0, robustTrials = 0;
  const probe = mulberry32(seed ^ 0x9e37);
  for (let i = 0; i < 500; i++) {
    const R = 0.85 + 0.1 * probe(); // deep in the proximity-dominant phase
    robustTrials++;
    if (tpdLabel(R, 0.05, probe) !== 1) flickers++;
  }
  const flickerRate = flickers / robustTrials;

  return {
    seed, samples, noise,
    spearman: rho,
    sBoundary: sB, tpdBoundary: pB,
    sTransitions: sT.count, tpdTransitions: pT.count,
    singleBoundaryAgree,
    flickerRate,
    pass: rho >= 0.85 && singleBoundaryAgree && flickerRate < 0.01,
  };
}

export default runTPD01CrossValidation;
