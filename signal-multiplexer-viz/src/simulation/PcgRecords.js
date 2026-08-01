/**
 * PcgRecords.js — pure PCG record builders + validator for
 * ConstitutionalTruthEngine.archive. Deliberately has NO dependency on
 * ConstitutionalTruthEngine.js (which imports THIS module to attach records
 * to the events it pushes) — kept import-direction-clean so there is no
 * cycle. See PcgEngine.js for the scenario-driving audit layer, and
 * canon/pcg.js for the grammar this instantiates.
 */

import { RELIANCE, ARCHIVE_DISPOSITION, ASPECT_ORDER, MODALITY_PRECEDENCE, LEVEL_ORDER } from '../data/canon/pcg.js';

const recordId = (claimId, v) => `pcg.${claimId}.v${v}`;

/**
 * FL·DID·C — the DID-record of `gate.challenge` (canon/gates.js) firing: a
 * claim's standing passing (or being tested against passing) through the
 * RELIANCE registry. Mirrors the spec's own R05 (FL·DID·G, instance-of R04).
 */
export function buildTransitionRecord(ev) {
  const surfaced = ev.type === 'drift-surfaced';
  return {
    id: recordId(ev.claimId, ev.v),
    address: { aspect: 'FL', modality: 'DID', level: 'C' },
    subject: [ev.claimId],
    value: {
      relation: surfaced ? 'gate-tested-no-change' : 'reliance-transition',
      from: ev.from, to: ev.to
    },
    U: {
      kind: 'membership-confidence', P: 1,
      note: 'RELIANCE classification is deterministic given warrant.evidence vs the consequence bar — C4 permits omitting confidence only where criteria are deterministic; stated explicitly here rather than by omission.'
    },
    provenance: {
      source: 'ConstitutionalTruthEngine', method: 'direct',
      registry: 'RELIANCE@1', timestamp: ev.t, reason: ev.reason
    },
    links: [{ type: 'instance-of', target: 'gate.challenge' }]
  };
}

/**
 * AT·DID·C — erasure is NOT a reliance transition (RELIANCE has no 'erased'
 * member; recording it there is the W4-ill-formed bug this retrofit fixes).
 * It is a change in the claim's DISPOSITION, classified against
 * archive.disposition (canon/pcg.js).
 */
export function buildErasureRecord(ev) {
  return {
    id: recordId(ev.claimId, ev.v),
    address: { aspect: 'AT', modality: 'DID', level: 'C' },
    subject: [ev.claimId],
    value: {
      relation: 'disposition-transition', from: 'active', to: 'erased',
      relianceAtErasure: ev.from // informational: the RELIANCE status held at erasure, not the classified value itself
    },
    U: { kind: 'membership-confidence', P: 1, note: 'Disposition is a deterministic function of _ageClaims\' correlation/τ-hierarchy test.' },
    provenance: {
      source: 'ConstitutionalTruthEngine', method: 'direct',
      registry: `${ARCHIVE_DISPOSITION.registry_id}@${ARCHIVE_DISPOSITION.version}`,
      timestamp: ev.t, reason: ev.reason
    },
    links: []
  };
}

/** W1/W2/W4/CC1–CC4 well-formedness check for one record (CC5/CC6 need the full graph; out of scope for a single record). */
export function validateRecord(record) {
  const violations = [];
  if (!record.id) violations.push('CC1: missing id');
  const { aspect, modality, level } = record.address || {};
  if (!ASPECT_ORDER.includes(aspect)) violations.push(`W1/CC1: aspect "${aspect}" not in the closed set`);
  if (!MODALITY_PRECEDENCE.includes(modality)) violations.push(`W1/CC1: modality "${modality}" not in the closed set`);
  if (!LEVEL_ORDER.includes(level)) violations.push(`W1/CC1: level "${level}" not in {G,R,C}`);
  if (!record.subject || record.subject.length === 0) violations.push('CC1: subject empty');
  if (record.value === undefined || record.value === null) violations.push('CC2: value missing');
  if (record.U === undefined) violations.push('CC3: U field absent (unknown is admissible; absent is not)');
  if (!record.provenance || !record.provenance.source || !record.provenance.method) {
    violations.push('CC4: provenance incomplete (source/method)');
  } else if (level === 'C' && !record.provenance.registry) {
    violations.push('W2/CC4: level C but no registry@version cited');
  } else if (level === 'R' && !record.provenance.basis) {
    violations.push('W2/CC4: level R but no basis cited');
  }
  if (level === 'C' && record.provenance?.registry?.startsWith('RELIANCE')) {
    for (const key of ['from', 'to']) {
      const v = record.value?.[key];
      if (v !== undefined && !RELIANCE.includes(v)) {
        violations.push(`W4: value.${key}="${v}" is not a RELIANCE member — ill-formed (invented label)`);
      }
    }
  }
  if (level === 'C' && record.provenance?.registry?.startsWith(ARCHIVE_DISPOSITION.registry_id)) {
    for (const key of ['from', 'to']) {
      const v = record.value?.[key];
      if (v !== undefined && !(v in ARCHIVE_DISPOSITION.classes)) {
        violations.push(`W4: value.${key}="${v}" is not an archive.disposition member — ill-formed`);
      }
    }
  }
  return { pass: violations.length === 0, violations };
}

/**
 * Falsifiability proof for validateRecord itself: a record missing U, and
 * separately a record whose `to` is not a registry member, must both be
 * caught before the real-archive PASS below is trusted.
 */
export function noOpAuditRecordValidator() {
  const missingU = { id: 'x', address: { aspect: 'FL', modality: 'DID', level: 'C' }, subject: ['c1'], value: { to: 'warranted' }, provenance: { source: 's', method: 'direct', registry: 'RELIANCE@1' } };
  const badMember = { id: 'y', address: { aspect: 'FL', modality: 'DID', level: 'C' }, subject: ['c1'], value: { to: 'erased' }, U: { P: 1 }, provenance: { source: 's', method: 'direct', registry: 'RELIANCE@1' } };
  const r1 = validateRecord(missingU);
  const r2 = validateRecord(badMember);
  const detected = !r1.pass && !r2.pass;
  return { detected, verdict: detected ? 'falsifiable (both synthetic violations caught)' : 'BROKEN VALIDATOR — a known-bad record passed' };
}
