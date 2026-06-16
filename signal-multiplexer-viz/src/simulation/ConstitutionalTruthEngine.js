/**
 * ConstitutionalTruthEngine.js
 * =========================================================================
 * Constitutional Governance of Truth (Temporal_State_Management.pdf, Part VI)
 * fused with the temporal state model (Part I) and principled memory erasure
 * (Part II). This is the depth the substrate was missing: the system must
 * remain CORRECTABLE.
 *
 * The Constitution (Part VI) in code:
 *   - Ground Truth = the canon of WARRANTED claims (Art. I–II). Warrant =
 *     traceable origin + legible transformation + admissible evidence +
 *     successful resistance to challenge.
 *   - Degrees of reliance (Art. VII): hypothesis → provisional → warranted
 *     → retracted. Required warrant strength rises with consequence (Art. XVI).
 *   - Separation of epistemic powers (Art. VIII–XIV): Proposer ≠ Verifier ≠
 *     Adjudicator; an Archive with versioned custody (erasure without lineage
 *     is unconstitutional); an Auditor / Office of Blind-Spot Discovery.
 *   - Right of Correction (Art. V.3) + Correction Supremacy (Art. XVIII):
 *     stronger admissible evidence beats canon consistency → canon is corrected.
 *   - Anti-Silent-Drift (Art. XIX): any change to a truth judgment must appear
 *     as a constitutional event (challenge / adjudication / amendment).
 *   - Unknown Register + Self-Reference Constraint (Art. XXVIII, XXX): internal
 *     consensus is NOT warrant.
 *   - Safety Monotonicity Under Uncertainty (Art. XXXVI): as warrant falls,
 *     behavioral risk may not rise — the antitone invariant, constitutionalized.
 *
 * Temporal state (Part I): the canon is VERSIONED; snapshots let the system
 * roll back to a prior epistemic state (flow-map reversibility). Each claim
 * carries a modality — realis (actualized / measured) or irrealis (projected).
 *
 * Memory erasure (Part II): non-canon claims age out by correlation decay
 * (|C(t)| < ε) and the τ-hierarchy (t > α·τ, α≈3–5); but warranted canon is a
 * conserved quantity and never silently relaxes (§9.6.3) — erasure always
 * leaves lineage in the Archive.
 *
 * STANDING: per the source, this is a constitutional design, not a field-proven
 * substrate. It governs the simulation; it does not certify a vehicle.
 * =========================================================================
 */

export const RELIANCE = ['hypothesis', 'provisional', 'warranted', 'retracted'];
export const POWERS = [
  { id: 'archive', name: 'The Archive', mandate: 'Versioned custody of canon; no erasure without lineage (Art. IX)' },
  { id: 'proposer', name: 'The Proposers', mandate: 'Generate hypotheses; no authority to canonize own output (Art. X)' },
  { id: 'verifier', name: 'The Verifiers', mandate: 'Test warrant & admissibility; can disagree with Proposers (Art. XI)' },
  { id: 'adjudicator', name: 'The Adjudicators', mandate: 'Resolve conflicts by admissible evidence only (Art. XII)' },
  { id: 'auditor', name: 'Auditor / Blind-Spot Office', mandate: 'Audit compliance; maintain the Unknown Register (Art. XIII, XXVI)' }
];

let _cid = 0;
const cid = () => `c${++_cid}`;

// Warrant strength required to canonize, by consequence (Art. XVI).
const WARRANT_BAR = { low: 0.45, high: 0.75 };

export class ConstitutionalTruthEngine {
  constructor() {
    this.version = 1;
    this.simTime = 0;
    this.tick = 0;
    this.listeners = new Set();

    this.claims = new Map();
    this.archive = [];          // append-only constitutional events (Art. IX, XIX)
    this.unknownRegister = [];  // epistemic risks (Art. XXVIII)
    this.snapshots = [];        // versioned canon snapshots for rollback (Part I)
    this.powerActivity = {};    // last-touched marker per power

    this._seedCanon();
    this._snapshot('genesis');
  }

  on(e, cb) { this.listeners.add({ e, cb }); }
  emit(e, d) { this.listeners.forEach(({ e: ev, cb }) => { if (ev === e) cb(d); }); }

  // Seed claims tied to the actual substrate (so governance is concrete). The
  // mix is deliberate: measured/realis facts canonize; projected/irrealis ones
  // stay hypotheses; one high-consequence claim sits provisional awaiting
  // independent corroboration (Art. XXXIV).
  _seedCanon() {
    const seed = [
      { statement: 'Analog-veto latency ≈ 32 ns holds', modality: 'realis', consequence: 'high',
        domain: 'actuation', origin: 'FPGA witness (measured)', evidence: 0.92, conserved: true },
      { statement: 'As warrant falls, admissible action set only contracts (antitone)', modality: 'realis',
        consequence: 'high', domain: 'safety', origin: 'monotonicity invariant (Art. XXXVI)', evidence: 0.88, conserved: true },
      { statement: 'Low-trajectory object arms the braking manifold', modality: 'realis', consequence: 'high',
        domain: 'perception', origin: 'enforced-causality ingest', evidence: 0.86, conserved: false },
      { statement: 'Camera+radar fusion is pre-fusion admissible', modality: 'realis', consequence: 'high',
        domain: 'fusion', origin: 'witness lineage (Art. XXXIII)', evidence: 0.80, conserved: false },
      { statement: 'ξ tracks physical risk in automotive perception', modality: 'irrealis', consequence: 'high',
        domain: 'fidelity', origin: 'φ-compiler (gasification bin-level only)', evidence: 0.42, conserved: false },
      { statement: 'Memristor monotonicity survives device non-ideality', modality: 'irrealis', consequence: 'high',
        domain: 'substrate', origin: 'Phase-0 hypothesis', evidence: 0.30, conserved: false }
    ];
    for (const s of seed) {
      const claim = this._mkClaim(s);
      this.claims.set(claim.id, claim);
      this._adjudicate(claim, 'genesis canonization');
    }
    // Genesis blind spots (Unknown Register).
    this.unknownRegister = [
      { id: 'u1', risk: 'φ transfer to automotive is gasification-validated only', kind: 'thin-evidence', sinceVersion: 1 },
      { id: 'u2', risk: 'memristor retention/monotonicity numbers are Phase-0', kind: 'untested-assumption', sinceVersion: 1 }
    ];
  }

  _mkClaim(s) {
    return {
      id: cid(),
      statement: s.statement,
      modality: s.modality,              // realis (actual) | irrealis (projected)
      consequence: s.consequence,        // low | high  → warrant bar
      domain: s.domain,
      reliance: 'hypothesis',
      warrant: {
        origin: s.origin,
        transformation: 'legible',
        evidence: s.evidence,            // admissible-evidence strength [0,1]
        challengeResistance: s.conserved ? 0.8 : 0.5, // conserved facts resist correction
        independent: false               // corroborated by ≥2 independent witnesses?
      },
      conserved: !!s.conserved,          // conservation-law claim → permanent memory (§9.6.3)
      t0: this.simTime,                  // temporal anchor (Part I)
      bornVersion: this.version,
      canonVersion: null,
      correlation: 1.0,                  // C(t) for erasure (Part II); decays for non-canon
      contestations: 0,
      lineage: [{ v: this.version, t: this.simTime, event: 'proposed', reliance: 'hypothesis' }]
    };
  }

  // ----- Separation of powers: Verifier (Art. XI) -----
  _verify(claim) {
    this.powerActivity.verifier = this.tick;
    const w = claim.warrant;
    // Admissibility (Art. XV): traceable origin + legible transform + revisable + scoped.
    const admissible = !!w.origin && w.transformation === 'legible';
    // Strength of warrant ∝ consequence (Art. XVI). High consequence needs corroboration.
    const bar = WARRANT_BAR[claim.consequence];
    const strong = w.evidence >= bar && (claim.consequence === 'low' || w.independent || w.evidence >= 0.85);
    claim.verifierVerdict = { admissible, strong, bar };
    return admissible && strong;
  }

  // ----- Adjudicator (Art. XII) + canonization / correction -----
  _adjudicate(claim, reason) {
    this.powerActivity.adjudicator = this.tick;
    const ok = this._verify(claim);
    const prev = claim.reliance;
    let next = prev;
    if (ok) next = 'warranted';
    else if (claim.warrant.evidence >= 0.3) next = 'provisional';
    else next = 'hypothesis';
    if (next !== prev) this._transition(claim, next, reason);
  }

  // A reliance change is a constitutional EVENT (Art. XIX anti-silent-drift).
  _transition(claim, next, reason) {
    const prev = claim.reliance;
    claim.reliance = next;
    if (next === 'warranted') claim.canonVersion = this.version + 1;
    this.version++;
    const ev = {
      v: this.version, t: this.simTime,
      claimId: claim.id, statement: claim.statement,
      from: prev, to: next, reason,
      type: next === 'retracted' ? 'retraction'
        : next === 'warranted' ? 'canonization'
        : 'reclassification'
    };
    claim.lineage.push({ v: this.version, t: this.simTime, event: ev.type, reliance: next, reason });
    this.archive.push(ev);
    if (this.archive.length > 60) this.archive.shift();
    this.emit('constitutional_event', ev);
  }

  // ----- Right of Correction (Art. V.3) + Correction Supremacy (Art. XVIII) -----
  // A challenge carries admissible counter-evidence. If it is stronger than the
  // claim's resistance, correction prevails over consistency.
  challenge(claimId, strength = 0.6, note = 'operator challenge') {
    const claim = this.claims.get(claimId);
    if (!claim) return;
    claim.contestations++;
    this.powerActivity.auditor = this.tick;
    if (strength > claim.warrant.challengeResistance) {
      // Correction: stronger evidence beats canon. Warrant is reduced; repair the
      // causal pathway (Duty of Repair, Art. VI.3) so recurrence drops.
      claim.warrant.evidence = Math.max(0, claim.warrant.evidence - 0.3 * strength);
      claim.warrant.challengeResistance = Math.min(1, claim.warrant.challengeResistance + 0.1);
      this._adjudicate(claim, `${note} (evidence ${strength.toFixed(2)} > resistance) → correction`);
      if (claim.reliance === 'hypothesis' || claim.warrant.evidence < 0.25) {
        this._transition(claim, 'retracted', `${note}: warrant collapsed`);
      }
    } else {
      // Survived a credible challenge → resistance (and warrant) strengthen.
      claim.warrant.challengeResistance = Math.min(1, claim.warrant.challengeResistance + 0.05);
      claim.warrant.evidence = Math.min(1, claim.warrant.evidence + 0.03);
      this._adjudicate(claim, `${note}: survived challenge`);
    }
  }

  // Operator: provide independent corroboration (Art. XXXIV) — raises a
  // high-consequence claim over the bar legitimately.
  corroborate(claimId) {
    const claim = this.claims.get(claimId);
    if (!claim) return;
    claim.warrant.independent = true;
    claim.warrant.evidence = Math.min(1, claim.warrant.evidence + 0.15);
    this._adjudicate(claim, 'independent corroboration added');
  }

  // Attempt a SILENT drift; the Constitution forces it to surface (Art. XIX).
  attemptSilentDrift() {
    // Pick a warranted claim and try to quietly flip its truth.
    const target = [...this.claims.values()].find(c => c.reliance === 'warranted' && !c.conserved);
    if (!target) { this.emit('drift_blocked', { reason: 'no eligible canon' }); return; }
    // The attempt cannot be silent: it is logged as a constitutional event and
    // routed to adjudication rather than mutating canon directly.
    this.powerActivity.auditor = this.tick;
    this.archive.push({
      v: ++this.version, t: this.simTime, claimId: target.id, statement: target.statement,
      from: target.reliance, to: target.reliance, type: 'drift-surfaced',
      reason: 'silent revision attempted → forced to constitutional event (Art. XIX)'
    });
    this.challenge(target.id, 0.7, 'anti-silent-drift audit');
    this.emit('drift_surfaced', { claimId: target.id });
  }

  // ----- Temporal state / reversibility (Part I): snapshot + rollback -----
  _snapshot(label) {
    this.snapshots.push({
      version: this.version, t: this.simTime, label,
      canon: [...this.claims.values()]
        .filter(c => c.reliance === 'warranted')
        .map(c => ({ id: c.id, statement: c.statement, evidence: c.warrant.evidence }))
    });
    if (this.snapshots.length > 20) this.snapshots.shift();
  }

  rollbackTo(version) {
    const snap = [...this.snapshots].reverse().find(s => s.version <= version) || this.snapshots[0];
    if (!snap) return;
    const canonIds = new Set(snap.canon.map(c => c.id));
    // Restore reliance to the snapshot's canon set (a reversible correction of
    // the canon — retraction with explanation, Art. VII.iv).
    this.claims.forEach(c => {
      const wasCanon = canonIds.has(c.id);
      if (wasCanon && c.reliance !== 'warranted') this._transition(c, 'warranted', `rollback → v${snap.version}`);
      else if (!wasCanon && c.reliance === 'warranted') this._transition(c, 'provisional', `rollback → v${snap.version}`);
    });
    this.emit('rollback', { to: snap.version });
  }

  // ----- main step: driven by the live substrate signal -----
  step(substrate = {}) {
    this.tick++;
    this.simTime = substrate.simTime ?? (this.simTime + 0.1);
    const warrant = substrate.warrant ?? 0.9;

    // Proposer activity (Art. X): occasionally float a fresh hypothesis.
    this.powerActivity.proposer = this.tick;
    if (this.tick % 40 === 0) this._proposeDynamic(substrate);

    // Falling warrant credibly challenges high-consequence IRREALIS (projected)
    // claims — exactly what should NOT be trusted as warrant falls.
    if (warrant < 0.5) {
      for (const c of this.claims.values()) {
        if (c.modality === 'irrealis' && c.consequence === 'high' && c.reliance !== 'retracted'
            && Math.random() < 0.04) {
          this.challenge(c.id, 0.55 + 0.4 * (0.5 - warrant), 'evidentiary degradation');
        }
      }
    }

    // An LLC consolidation jump retracts the "consolidation is safe" lineage
    // (links the constitution to the substrate's anti-silent-drift detector).
    if (substrate.llcJump) {
      let c = [...this.claims.values()].find(x => x.domain === 'substrate' && x.reliance === 'warranted');
      if (c) this.challenge(c.id, 0.8, 'LLC phase-transition jump (quarantine)');
    }

    // Principled erasure (Part II) for non-canon, non-conserved claims.
    this._ageClaims();

    // Auditor maintains the Unknown Register (Art. XXVIII): a high-consequence
    // claim resting on a single witness is an independence risk.
    this._updateUnknownRegister(warrant);

    // Periodic snapshot for temporal rollback.
    if (this.tick % 25 === 0) this._snapshot(`t=${this.simTime.toFixed(1)}s`);

    return this.getState();
  }

  _proposeDynamic(substrate) {
    const risk = substrate.risk ?? 0.3;
    const claim = this._mkClaim({
      statement: risk > 0.6
        ? `Threat regime active at t=${this.simTime.toFixed(0)}s — conservative envelope`
        : `Nominal regime holds at t=${this.simTime.toFixed(0)}s`,
      modality: 'irrealis', consequence: risk > 0.6 ? 'high' : 'low',
      domain: 'regime', origin: 'proposer (regime inference)',
      evidence: 0.5 + 0.3 * Math.random(), conserved: false
    });
    this._adjudicate(claim, 'fresh proposal');
    this.claims.set(claim.id, claim);
    // bound the claim set
    if (this.claims.size > 16) {
      const oldest = [...this.claims.values()]
        .filter(c => !c.conserved && c.reliance !== 'warranted')
        .sort((a, b) => a.bornVersion - b.bornVersion)[0];
      if (oldest) { this._erase(oldest, 'capacity (lineage retained in Archive)'); }
    }
  }

  // Part II erasure: correlation decay + τ-hierarchy; conserved canon is exempt.
  _ageClaims() {
    const EPS = 0.05;     // measurement precision (§9.1.1)
    for (const c of this.claims.values()) {
      if (c.reliance === 'warranted' || c.conserved) continue; // conservation = permanent memory
      // Correlation decays for unsupported claims (stretched toward ε).
      c.correlation *= 0.97;
      const age = this.simTime - c.t0;
      const tauExceeded = age > 5 * 3; // t > α·τ, α≈3, τ≈5s
      if (c.correlation < EPS || tauExceeded) {
        this._erase(c, c.correlation < EPS ? 'correlation decay |C|<ε' : 'τ-hierarchy t>α·τ');
      }
    }
  }

  _erase(claim, reason) {
    // Erasure with lineage (Art. IX): the claim leaves Ground Truth but the
    // reason persists in the Archive — never silent.
    this.archive.push({
      v: ++this.version, t: this.simTime, claimId: claim.id, statement: claim.statement,
      from: claim.reliance, to: 'erased', type: 'erasure', reason
    });
    if (this.archive.length > 60) this.archive.shift();
    this.claims.delete(claim.id);
  }

  _updateUnknownRegister(warrant) {
    const reg = [...this.unknownRegister];
    // Independence risk: high-consequence canon without independent corroboration.
    const lone = [...this.claims.values()].filter(c =>
      c.reliance === 'warranted' && c.consequence === 'high' && !c.warrant.independent);
    const haveLone = reg.some(r => r.kind === 'degraded-independence');
    if (lone.length > 0 && !haveLone) {
      reg.push({ id: 'u-ind', kind: 'degraded-independence',
        risk: `${lone.length} high-consequence canon rest on a single witness (Art. XXXIV)`, sinceVersion: this.version });
    } else if (lone.length === 0 && haveLone) {
      this.unknownRegister = reg.filter(r => r.kind !== 'degraded-independence');
      return;
    }
    if (warrant < 0.4 && !reg.some(r => r.kind === 'low-warrant')) {
      reg.push({ id: 'u-warr', kind: 'low-warrant',
        risk: 'global warrant degraded — projected claims under heightened challenge', sinceVersion: this.version });
    } else if (warrant >= 0.4) {
      this.unknownRegister = reg.filter(r => r.kind !== 'low-warrant');
      return;
    }
    this.unknownRegister = reg;
  }

  getState() {
    const claims = [...this.claims.values()].map(c => ({
      id: c.id, statement: c.statement, modality: c.modality, consequence: c.consequence,
      domain: c.domain, reliance: c.reliance, conserved: c.conserved,
      evidence: c.warrant.evidence, resistance: c.warrant.challengeResistance,
      independent: c.warrant.independent, bar: c.verifierVerdict?.bar,
      admissible: c.verifierVerdict?.admissible, strong: c.verifierVerdict?.strong,
      contestations: c.contestations, t0: c.t0, canonVersion: c.canonVersion,
      lineage: c.lineage.slice(-4)
    }));
    const canon = claims.filter(c => c.reliance === 'warranted');
    return {
      version: this.version, simTime: this.simTime, tick: this.tick,
      claims,
      canon,
      counts: RELIANCE.reduce((a, r) => (a[r] = claims.filter(c => c.reliance === r).length, a), {}),
      powers: POWERS.map(p => ({ ...p, active: this.powerActivity[p.id] === this.tick })),
      archive: [...this.archive].slice(-14).reverse(),
      unknownRegister: [...this.unknownRegister],
      snapshots: this.snapshots.map(s => ({ version: s.version, t: s.t, label: s.label, canonSize: s.canon.length })),
      // Self-reference constraint (Art. XXX): internal agreement is NOT warrant.
      selfReferenceNote: 'Internal consensus is not warrant (Art. XXX); canon requires admissible external basis.'
    };
  }

  reset() {
    const listeners = this.listeners;
    Object.assign(this, new ConstitutionalTruthEngine());
    this.listeners = listeners;
    this.emit('reset', null);
  }
}

export default ConstitutionalTruthEngine;
