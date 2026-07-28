/**
 * canon/hazards.js — hazard ↔ mechanism ↔ requirement traceability.
 * (Definition of Done item 6: every hazard traces forward to a mechanism and
 * every invariant traces back to a requirement, with orphans in BOTH
 * directions listed rather than suppressed.)
 *
 * mechanisms[] name gate_ids from canon/gates.js (or engine artifacts);
 * the validator computes orphans mechanically.
 */

export const HAZARDS = [
  {
    hazard_id: 'hz.unauthorized_expansion',
    description: 'The admissible action set expands while warrant is falling / risk is rising.',
    mechanisms: ['gate.monotonicity'],
    requirement: 'req.antitone'
  },
  {
    hazard_id: 'hz.late_refusal',
    description: 'Refusal arrives after the actuation window — a veto slower than the hazard.',
    mechanisms: ['gate.analog_veto'],
    requirement: 'req.fast_veto'
  },
  {
    hazard_id: 'hz.masked_defect',
    description: 'A compensating gain hides a genuine sensor defect from the fusion band.',
    mechanisms: ['gate.masking_probe', 'gate.conservation'],
    requirement: 'req.gauge_fixed_reading'
  },
  {
    hazard_id: 'hz.silent_drift',
    description: 'A truth judgment or learned consolidation changes without a surfaced, contestable event.',
    mechanisms: ['gate.llc_quarantine', 'gate.challenge'],
    requirement: 'req.correctability'
  },
  {
    hazard_id: 'hz.analog_decay',
    description: 'Decayed analog state masquerades as durable truth beyond its physical retention.',
    mechanisms: ['gate.requantize'],
    requirement: 'req.tau_partition'
  },
  {
    // Deliberately mechanism-less: a real orphan the validator must LIST, not
    // suppress. Closing it requires device data this repo does not have.
    hazard_id: 'hz.device_nonideality',
    description: 'Real memristor non-ideality (drift, read noise, retention loss) re-admits forbidden commands on physical hardware.',
    mechanisms: [],
    requirement: 'req.device_monotonicity'
  }
];

export const REQUIREMENTS = [
  { requirement_id: 'req.antitone', text: 'Authority may only contract as warrant falls (antitone law).' },
  { requirement_id: 'req.fast_veto', text: 'The refusal path must be faster than the digital decision window.' },
  { requirement_id: 'req.gauge_fixed_reading', text: 'Safety bands read gauge-fixed shape, never raw gain-scaled signals.' },
  { requirement_id: 'req.correctability', text: 'The system must remain correctable: every truth change surfaces as a constitutional event.' },
  { requirement_id: 'req.tau_partition', text: 'Analog and digital state are partitioned at τ = 5 s with re-quantization at the boundary.' },
  { requirement_id: 'req.device_monotonicity', text: 'Monotone admission must survive device non-ideality on measured hardware.' },
  {
    // Deliberately unreferenced by any hazard: the reverse-direction orphan.
    requirement_id: 'req.field_validation',
    text: 'CRL/TPD/MMR require field validation before any trusted promotion (source §8).'
  }
];
