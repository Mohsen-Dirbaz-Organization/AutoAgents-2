/**
 * canon/standing.js — the canonical claim-standing ladder.
 * (EPU Companion, Glossary 3/3 "Evidence and claim vocabulary".)
 * One ledger, many renderings: every register and view reads THIS ladder.
 */

export const STANDING = {
  established: {
    label: 'Established',
    color: '#22a06b',
    meaning: 'Proof or reproducible measurement with stated scope',
    authority: 'May support the safety case',
    rank: 0
  },
  constructed: {
    label: 'Constructed',
    color: '#6b46c1',
    meaning: 'True by explicit definition or architecture; supports design semantics only',
    authority: 'Design semantics only — never empirical evidence',
    rank: 1
  },
  proposed: {
    label: 'Proposed',
    color: '#2b6cb0',
    meaning: 'Coherent design with named proof or test debt',
    authority: 'Requires a validation pack',
    rank: 2
  },
  target: {
    label: 'Target',
    color: '#DAA520',
    meaning: 'Budget, threshold or roadmap objective; never phrased as achieved',
    authority: 'Cannot expand authority; cannot be phrased as achieved',
    rank: 3
  },
  unsupported: {
    label: 'Unsupported',
    color: '#c0392b',
    meaning: 'No artifact in the delivered corpus',
    authority: 'Quarantine',
    rank: 4
  }
};

export const STANDING_ORDER = ['established', 'constructed', 'proposed', 'target', 'unsupported'];

/**
 * Crosswalk to the F26 event-fabric ladder already used by
 * `src/data/eventFabric.js` (established|proposed|projected|notional|open), so
 * the two views render consistently against one canonical ladder.
 */
export const F26_CROSSWALK = {
  established: 'established',
  proposed: 'proposed',
  projected: 'target',      // a performance/design target
  notional: 'unsupported',  // conceptual/speculative — no artifact
  open: 'unsupported'       // known unresolved obligation — evidence debt
};

/** Additional qualifier used by the simulation: established *within* the sim. */
export const IN_SIM_NOTE =
  'established-in-sim = reproducible inside this simulation harness under its stated ' +
  'configuration; NOT field/hardware evidence. Rendered as Established with an in-sim scope.';
