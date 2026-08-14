/**
 * PcgEngine.js — drives ConstitutionalTruthEngine through a scenario and
 * audits the resulting archive's PCG records (attached by the engine itself
 * via PcgRecords.js). One-way dependency only: this file imports the engine
 * to drive it; the engine does not import this file (see PcgRecords.js for
 * why the builder/validator functions live in a separate, cycle-free module).
 */

import { ConstitutionalTruthEngine } from './ConstitutionalTruthEngine.js';
import { validateRecord, noOpAuditRecordValidator } from './PcgRecords.js';

/**
 * Drive the engine through a short, deterministic scenario that exercises
 * canonization/demotion, silent-drift surfacing, and erasure — using only the
 * public API (step/challenge/corroborate/attemptSilentDrift) — then return
 * the resulting archive (each entry now carrying `.record`, per the engine's
 * retrofit).
 */
export function driveArchiveScenario() {
  const engine = new ConstitutionalTruthEngine();

  // Genesis alone exercises canonization + reclassification. Exercise the
  // remaining three archived event types (drift-surfaced, retraction,
  // erasure) each at least once, so the sample below demonstrates every
  // record shape ConstitutionalLog.jsx knows how to render.
  const driftTarget = [...engine.claims.values()].find((c) => c.reliance === 'warranted' && !c.conserved);
  if (driftTarget) {
    engine.corroborate(driftTarget.id);
    engine.attemptSilentDrift(); // pushes 'drift-surfaced', then internally challenges
  }
  const conservedTarget = [...engine.claims.values()].find((c) => c.conserved && c.reliance === 'warranted');
  if (conservedTarget) {
    engine.challenge(conservedTarget.id, 0.95, 'audit-scenario-strong'); // a second, independent demotion
  }
  const retractTarget = [...engine.claims.values()].find((c) => c.reliance === 'provisional');
  if (retractTarget) {
    // Capped at 2 hits deliberately: a 3rd+ strong hit on an ALREADY-retracted
    // claim re-runs _adjudicate (which does not treat 'retracted' as
    // terminal) before the retraction check, producing a retracted→
    // hypothesis→retracted churn pair on UNCHANGED evidence — a pre-existing
    // engine nuance this audit surfaced, not a defect in the record format.
    // Tracked as ob.retraction_churn_engine_note (open, informational);
    // out of scope to fix here. Stopping at 2 hits keeps this demo clean.
    for (let i = 0; i < 2; i++) engine.challenge(retractTarget.id, 0.99, 'audit-scenario-collapse');
  }
  // Age a provisional claim to erasure via the public step() API only.
  for (let i = 0; i < 220 && [...engine.claims.values()].some((c) => c.domain === 'substrate'); i++) {
    engine.step({ simTime: engine.simTime + 0.5, warrant: 0.9 });
  }
  return engine.archive;
}

/** A3 (§8.5): is gate.challenge "dead" — zero instance-of occurrences in this window? */
function checkGateNotDead(records) {
  const occurrences = records.filter((r) => r.links?.some((l) => l.type === 'instance-of' && l.target === 'gate.challenge'));
  return { gate: 'gate.challenge', occurrences: occurrences.length, dead: occurrences.length === 0 };
}

export function runPcgArchiveAudit() {
  const archive = driveArchiveScenario();
  const withRecords = archive.filter((e) => e.record);
  const validations = withRecords.map((e) => ({ event: e, ...validateRecord(e.record) }));
  const noOpAudit = noOpAuditRecordValidator();
  const gateLiveness = checkGateNotDead(withRecords.map((e) => e.record));
  // One representative record per archived event type, in first-seen order —
  // a blind first-N slice would (and did) land entirely on canonization/
  // reclassification, since erasures are appended last by the aging loop,
  // hiding the FL·DID·C vs AT·DID·C distinction this retrofit is about.
  const seenTypes = new Set();
  const sample = [];
  for (const e of withRecords) {
    if (seenTypes.has(e.type)) continue;
    seenTypes.add(e.type);
    sample.push(e.record);
  }
  return {
    archiveLength: archive.length,
    recordedCount: withRecords.length,
    validations,
    allValid: validations.every((v) => v.pass),
    noOpAudit,
    gateLiveness,
    sample
  };
}

export default runPcgArchiveAudit;
