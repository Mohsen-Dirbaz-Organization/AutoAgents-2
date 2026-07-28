/**
 * canon/configurations.js — configuration artifacts.
 * (EPU Companion, Numeric Ledger 3/3: "No number is an engineering claim
 * without: config_id; RTL/ISA commit; process and foundry; ... evidence
 * identifiers.") For this repo the honest configurations are the simulation
 * harness and the upstream FPGA measurement we cite but did not perform.
 */

export const CONFIGURATIONS = [
  {
    config_id: 'cfg-sim-harness',
    kindOfPlatform: 'browser simulation harness',
    identifiers: {
      code: 'signal-multiplexer-viz (this repo, current commit)',
      runtime: 'IEEE-754 binary64 JavaScript (V8/browser)',
      engine: 'BoundedAutonomyStack dt=0.1 s/tick; CRL 5-channel multiplet, uniform weights',
      workload: 'scenario drivers: nominal | degrading | sensor_drift | adversarial | recovery',
      timingMethod: 'simulated time; wall-clock interval 100 ms/tick',
      toolchain: 'vite build; node for smoke tests'
    },
    limits: 'Supports kind: simulated | derived | constructed numbers ONLY. Nothing run on this configuration can produce a `measured` hardware number.'
  },
  {
    config_id: 'cfg-fpga-veto-upstream',
    kindOfPlatform: 'upstream FPGA measurement (cited, not performed here)',
    identifiers: {
      source: 'Bounded Autonomy on a Memristive Substrate — Technical Reference §2.2/§7 (external; not shipped in repo)',
      artifact: 'FPGA analog-veto witness',
      note: 'The identifier set (device, bitstream, clock, instrumentation) lives with the upstream source, not this repo.'
    },
    limits: 'Cited measurement. This repo may repeat the number with kind: measured ONLY while naming this config and its external residence.'
  },
  {
    config_id: 'cfg-none',
    kindOfPlatform: 'no configuration exists',
    identifiers: {},
    limits: 'For kind: target numbers. A number on cfg-none can never be phrased as achieved.'
  }
];
