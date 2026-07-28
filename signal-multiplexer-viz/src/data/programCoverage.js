/**
 * programCoverage.js - GHOST Autonomy program coverage map.
 * Data transcribed from 'Research Subcategory -> Document Section Mapping'
 * (GHOST Autonomy, Feb 2026): 56 subcategories x main(8).tex chapters x
 * source-material availability. This is a documentation/index artifact -
 * coverage = 'source material available in project knowledge', NOT built silicon.
 */

export const COVERAGE_META = {
  FULL:    { label: 'Full',    color: '#22a06b', desc: 'Complete source material in project knowledge' },
  HIGH:    { label: 'High',    color: '#7cb342', desc: 'Substantial source; minor supplementation' },
  PARTIAL: { label: 'Partial', color: '#f0a500', desc: 'Some content; web research to fill gaps' },
  GAP:     { label: 'Gap',     color: '#dc4d4d', desc: 'No project source; external research required' }
};

export const DOMAINS = ["Sensors & Sensing Technologies", "Processor & Computing Architectures", "Numerical Methods & Precision Computing", "Functional Safety & Standards", "Automotive Applications & Systems", "Advanced & Emerging Topics"];

export const SUBCATEGORIES = [
  {
    "id": 1,
    "domain": "Sensors & Sensing Technologies",
    "name": "MEMS Accelerometer Vibration & Noise",
    "coverage": "GAP",
    "chapters": "Ch.14 (Sensor Physics), Ch.44 (Vibration Filtering)",
    "note": "Needed for PCB-level noise analysis in EPU packaging. ISO 16750-3 compliance.",
    "gapPriority": "MEDIUM",
    "gapEffort": "3-5"
  },
  {
    "id": 2,
    "domain": "Sensors & Sensing Technologies",
    "name": "Vibration Analysis & Measurement (PSD, g RMS, ISO)",
    "coverage": "GAP",
    "chapters": "Ch.14.3, Ch.44, Ch.40 (Safety Validation)",
    "note": "Required for AEC-Q100 qualification (Ch.39). Random vibration profiles for automotive grade.",
    "gapPriority": "HIGH",
    "gapEffort": "5-7"
  },
  {
    "id": 3,
    "domain": "Sensors & Sensing Technologies",
    "name": "Automotive Vibration Environments",
    "coverage": "GAP",
    "chapters": "Ch.14.3, Ch.44, Ch.39.1 (Silicon Results)",
    "note": "Highway/Urban/Off-road vibration spectra feed into sensor reliability models.",
    "gapPriority": "HIGH",
    "gapEffort": "3-5"
  },
  {
    "id": 4,
    "domain": "Sensors & Sensing Technologies",
    "name": "Event-Driven Dynamic Vision Sensors (DVS)",
    "coverage": "PARTIAL",
    "chapters": "Ch.14.2, Ch.15 (Neural Trichotomy), Ch.25 (STOP-5 Bitvector)",
    "note": "DVS aligns with event-driven EPU paradigm. Camera bitvector formats defined but DVS-specific encoding absent."
  },
  {
    "id": 5,
    "domain": "Sensors & Sensing Technologies",
    "name": "Radar Signal Processing (FFT, Fixed-point)",
    "coverage": "FULL",
    "chapters": "Ch.24.2, Ch.25.2, Ch.25.3 (Pipeline Stages)",
    "note": "Complete 7-stage radar pipeline with bit-precise formats. All fixed-point encodings specified."
  },
  {
    "id": 6,
    "domain": "Sensors & Sensing Technologies",
    "name": "Quantum Sensors (Magnetometer, Gravimeter, Accel)",
    "coverage": "GAP",
    "chapters": "Ch.14.5 (Advanced Sensors), Ch.38.3 (Tensor Analysis)",
    "note": "Future roadmap item. Quantum inertial navigation relevant for GPS-denied operation (Ch.38).",
    "gapPriority": "LOW",
    "gapEffort": "7-10"
  },
  {
    "id": 7,
    "domain": "Sensors & Sensing Technologies",
    "name": "Quantum LiDAR",
    "coverage": "GAP",
    "chapters": "Ch.14.5, Ch.38 (Comparative Analysis)",
    "note": "Emerging technology. Photon-counting techniques relevant for low-SNR perception.",
    "gapPriority": "LOW",
    "gapEffort": "5-7"
  },
  {
    "id": 8,
    "domain": "Sensors & Sensing Technologies",
    "name": "Chip-Scale Atomic Clocks",
    "coverage": "PARTIAL",
    "chapters": "Ch.14.4 (Timing), Ch.28.4 (Byzantine Timing)",
    "note": "Vector clock sync protocol defined. CSAC integration for GPS-denied timing requires research."
  },
  {
    "id": 9,
    "domain": "Sensors & Sensing Technologies",
    "name": "Sensor Fusion Algorithms (Kalman Filter, etc.)",
    "coverage": "HIGH",
    "chapters": "Ch.17 (UQ), Ch.24.2, Ch.25.3 (Fusion Stage)",
    "note": "Kalman filter fully specified in ISA (SFSPU.KF) and UQ document. Fusion object format defined."
  },
  {
    "id": 10,
    "domain": "Sensors & Sensing Technologies",
    "name": "Sensor Data Compression & Reduction",
    "coverage": "HIGH",
    "chapters": "Ch.25.3 (Pipeline), Ch.26.2 (Queen Bee Compression)",
    "note": "Two orthogonal compression stories: signal-domain (radar pipeline) and population-domain (moment compression)."
  },
  {
    "id": 11,
    "domain": "Processor & Computing Architectures",
    "name": "Neuromorphic Computing & Chips (Loihi)",
    "coverage": "HIGH",
    "chapters": "Ch.15 (Neural Trichotomy), Ch.22 (On-chip Learning)",
    "note": "Comprehensive landscape assessment. Key finding: sub-100ns inference achievable for 2×5 networks via FPGA/LogicNets."
  },
  {
    "id": 12,
    "domain": "Processor & Computing Architectures",
    "name": "Event-Driven / Sparse Computing",
    "coverage": "FULL",
    "chapters": "Ch.23 (PICAPD ISA), Ch.26 (Queen Bee), Ch.27 (Silicon)",
    "note": "Core architectural paradigm. Event registers provide ISA-level support for sparse, event-driven computation."
  },
  {
    "id": 13,
    "domain": "Processor & Computing Architectures",
    "name": "Domain-Specific Processors / ASICs",
    "coverage": "FULL",
    "chapters": "Ch.23-27 (entire Part V Architecture), Ch.38 (Comparative)",
    "note": "Ghost EPU IS the domain-specific ASIC. Complete programmer's reference + silicon design available."
  },
  {
    "id": 14,
    "domain": "Processor & Computing Architectures",
    "name": "Reconfigurable / CGRA",
    "coverage": "PARTIAL",
    "chapters": "Ch.24.4 (Platform), Ch.27 (Silicon Overlay 25)",
    "note": "FPGA used for prototyping, not production. CGRA comparison needed for Ch.38 competitive analysis."
  },
  {
    "id": 15,
    "domain": "Processor & Computing Architectures",
    "name": "Fixed-Function vs. Programmable Accelerators",
    "coverage": "HIGH",
    "chapters": "Ch.24.1 (ISA Gaps), Ch.27, Ch.38.2",
    "note": "Ghost combines both: programmable ISA-level compute with fixed-function safety enforcement."
  },
  {
    "id": 16,
    "domain": "Processor & Computing Architectures",
    "name": "Automotive SoC Architectures (Mobileye, NVIDIA, Tesla, Qualcomm)",
    "coverage": "HIGH",
    "chapters": "Ch.38.1 (Competitors), Ch.2.2 (Competitive Landscape)",
    "note": "Detailed competitor landscape available. Ghost positioned as only <1μs physics-class system."
  },
  {
    "id": 17,
    "domain": "Processor & Computing Architectures",
    "name": "Agile Hardware/Software Co-Design",
    "coverage": "PARTIAL",
    "chapters": "Ch.5 (Roadmap), Ch.27 (Silicon)",
    "note": "Methodology implicit in design docs but not explicitly documented as a co-design framework."
  },
  {
    "id": 18,
    "domain": "Processor & Computing Architectures",
    "name": "FPGA for Automotive Processing",
    "coverage": "HIGH",
    "chapters": "Ch.27, Ch.39.5 (EPU Emulation), Ch.3 (Product Strategy)",
    "note": "FPGA as Phase 4 validation platform well-documented. LogicNets for sub-100ns inference proven feasible."
  },
  {
    "id": 19,
    "domain": "Processor & Computing Architectures",
    "name": "RISC-V Custom Extensions & ISA Design",
    "coverage": "FULL",
    "chapters": "Ch.23 (PICAPD ISA - entire chapter), Ch.24 (Rectification)",
    "note": "PICAPD extends RISC-V base with domain-specific instructions. Full programmer's reference available."
  },
  {
    "id": 20,
    "domain": "Processor & Computing Architectures",
    "name": "Processor Latency & Determinism (Nanosecond-scale)",
    "coverage": "FULL",
    "chapters": "Ch.23, Ch.26 (Queen Bee), Ch.28 (Byzantine), Ch.29 (Safety)",
    "note": "Deterministic latency is core differentiator. All critical-path timings specified to nanosecond precision."
  },
  {
    "id": 21,
    "domain": "Processor & Computing Architectures",
    "name": "Novel Processor Certification (ASIL-D)",
    "coverage": "HIGH",
    "chapters": "Ch.29 (Safety Architecture), Ch.40.1 (ASIL-D Validation)",
    "note": "ASIL-D certification path defined via TMR + memoryless verification. ISO 26262 gap for ML explicitly identified."
  },
  {
    "id": 22,
    "domain": "Numerical Methods & Precision Computing",
    "name": "Fixed-Point Arithmetic Implementation",
    "coverage": "FULL",
    "chapters": "Ch.24.2 (Fixed-Point), Ch.25.2 (Formats)",
    "note": "Comprehensive fixed-point format library with bit-precise specifications for every pipeline stage."
  },
  {
    "id": 23,
    "domain": "Numerical Methods & Precision Computing",
    "name": "Fixed-Point Precision Analysis (Bit-width, SQNR, Quantization)",
    "coverage": "PARTIAL",
    "chapters": "Ch.24.2, Ch.39.1 (Silicon Validation)",
    "note": "Formats defined but formal SQNR/quantization noise analysis not yet conducted in project docs."
  },
  {
    "id": 24,
    "domain": "Numerical Methods & Precision Computing",
    "name": "Posit Number Format",
    "coverage": "GAP",
    "chapters": "Ch.24.1 (ISA Gaps), Ch.24.2",
    "note": "Posit as potential alternative to fixed-point for dynamic range. Research needed for comparison.",
    "gapPriority": "LOW",
    "gapEffort": "3-5"
  },
  {
    "id": 25,
    "domain": "Numerical Methods & Precision Computing",
    "name": "Mixed-Precision (Fixed/Floating) Computing",
    "coverage": "PARTIAL",
    "chapters": "Ch.24.2, Ch.27 (Silicon)",
    "note": "ISA supports both FP64 and fixed-point. Pipeline is fixed-point only. Mixed-precision strategy needs articulation."
  },
  {
    "id": 26,
    "domain": "Numerical Methods & Precision Computing",
    "name": "Numerical Precision in Physics Simulation (Verlet)",
    "coverage": "FULL",
    "chapters": "Ch.16 (Lagrangian Mechanics), Ch.23.6 (Variational Instructions)",
    "note": "Hardware VERLET instruction preserves symplectic structure. Energy drift bounded by construction."
  },
  {
    "id": 27,
    "domain": "Numerical Methods & Precision Computing",
    "name": "Numerical Precision in Sensor Fusion (Kalman Filter)",
    "coverage": "HIGH",
    "chapters": "Ch.17 (UQ), Ch.24.2",
    "note": "Kalman filter precision tied to Q8.8 covariance encoding. Potential numerical issues at low uncertainty values."
  },
  {
    "id": 28,
    "domain": "Functional Safety & Standards",
    "name": "ISO 26262 Compliance & ASIL Certification",
    "coverage": "HIGH",
    "chapters": "Ch.29 (Safety Architecture), Ch.40 (Safety Validation)",
    "note": "Certification strategy defined. Gap: ISO 26262 not designed for neural networks or online learning."
  },
  {
    "id": 29,
    "domain": "Functional Safety & Standards",
    "name": "ISO 21448 (SOTIF) for Autonomous Driving",
    "coverage": "PARTIAL",
    "chapters": "Ch.29.3, Ch.40.1",
    "note": "SOTIF referenced but not deeply analyzed. Requires research on perception limitation taxonomy."
  },
  {
    "id": 30,
    "domain": "Functional Safety & Standards",
    "name": "ISO 16750-3 (Random Vibration)",
    "coverage": "GAP",
    "chapters": "Ch.14.3, Ch.39.1 (Silicon), Ch.44",
    "note": "Critical for AEC-Q100 qualification. Vibration PSD profiles needed for PCB/packaging design.",
    "gapPriority": "HIGH",
    "gapEffort": "5-7"
  },
  {
    "id": 31,
    "domain": "Functional Safety & Standards",
    "name": "Hardware Safety Mechanisms (Safety Islands, Monitors)",
    "coverage": "FULL",
    "chapters": "Ch.29 (entire), Ch.27 (Silicon Overlays 23-24, 28-29, 33, 43, 45, 47)",
    "note": "Ghost's safety island is the entire EPU architecture, not a bolt-on. Three-principle enforcement fully specified."
  },
  {
    "id": 32,
    "domain": "Functional Safety & Standards",
    "name": "Dynamic / Adaptive Safety Parameters & Thresholds",
    "coverage": "HIGH",
    "chapters": "Ch.17 (UQ), Ch.29.3 (Non-Authorizable)",
    "note": "RMAA bands adapt authority based on risk. UQ OOD thresholds dynamically flag uncertainty."
  },
  {
    "id": 33,
    "domain": "Functional Safety & Standards",
    "name": "Freedom from Interference",
    "coverage": "FULL",
    "chapters": "Ch.29.2 (Non-Routable), Ch.27 (Silicon)",
    "note": "Hardware-enforced freedom from interference via topology design. No wire = no interference path."
  },
  {
    "id": 34,
    "domain": "Functional Safety & Standards",
    "name": "Safety Verification using Physics Models (Conservation Laws)",
    "coverage": "FULL",
    "chapters": "Ch.16 (Lagrangian), Ch.23.5 (Safety Instructions), Ch.29.1",
    "note": "Physics conservation checks are ISA-level primitives. CONS.CHK verifies at hardware speed."
  },
  {
    "id": 35,
    "domain": "Functional Safety & Standards",
    "name": "Plausibility Checks & Runtime Monitoring",
    "coverage": "HIGH",
    "chapters": "Ch.29, Ch.35 (Runtime), Ch.40.2 (Fault Injection)",
    "note": "Multi-layered runtime monitoring: moment realizability, conservation checks, watchdog timers, Byzantine consensus."
  },
  {
    "id": 36,
    "domain": "Functional Safety & Standards",
    "name": "Context-Adaptive Safety & ODD",
    "coverage": "HIGH",
    "chapters": "Ch.29.3, Ch.17.3 (Regime-Aware)",
    "note": "Archipelago provides conceptual framework for context-adaptive formulation. RMAA operationalizes it."
  },
  {
    "id": 37,
    "domain": "Automotive Applications & Systems",
    "name": "ADAS",
    "coverage": "PARTIAL",
    "chapters": "Ch.2 (Market), Ch.3 (Product Strategy), Ch.38.1",
    "note": "Ghost positions beyond ADAS (L4+) but MVP addresses ADAS-level emergency braking."
  },
  {
    "id": 38,
    "domain": "Automotive Applications & Systems",
    "name": "Autonomous Vehicle Navigation (incl. GPS-denied)",
    "coverage": "PARTIAL",
    "chapters": "Ch.14.4, Ch.38, Ch.16 (Lagrangian for Navigation)",
    "note": "Sensor fusion ISA support defined. GPS-denied navigation via quantum inertial nav is future roadmap."
  },
  {
    "id": 39,
    "domain": "Automotive Applications & Systems",
    "name": "Automotive Chip Qualification (AEC-Q100)",
    "coverage": "HIGH",
    "chapters": "Ch.39.1 (Silicon), Ch.27 (Silicon)",
    "note": "AEC-Q100 path defined in validation pyramid. Test infrastructure specified in silicon overlays."
  },
  {
    "id": 40,
    "domain": "Automotive Applications & Systems",
    "name": "Automotive Physics Simulation & Solvers",
    "coverage": "FULL",
    "chapters": "Ch.16 (Lagrangian), Ch.23.6 (Variational Instructions)",
    "note": "Complete physics simulation substrate: mathematical foundation + ISA instructions + hardware implementation."
  },
  {
    "id": 41,
    "domain": "Automotive Applications & Systems",
    "name": "Real-Time Processing for Safety-Critical Systems",
    "coverage": "FULL",
    "chapters": "Ch.20 (Determinism), Ch.23, Ch.26, Ch.28, Ch.29",
    "note": "Real-time determinism is THE core value proposition. Every latency specified to nanosecond precision."
  },
  {
    "id": 42,
    "domain": "Automotive Applications & Systems",
    "name": "Low-Power Computing & Adaptive Wake-up",
    "coverage": "HIGH",
    "chapters": "Ch.27 (Silicon Overlays 1-5, 40, 48), Ch.22",
    "note": "Action-gradient filtering enables intelligent wake-up. WFI/EWAIT provide ISA-level power management."
  },
  {
    "id": 43,
    "domain": "Automotive Applications & Systems",
    "name": "Power Management for Automotive Compute",
    "coverage": "HIGH",
    "chapters": "Ch.27 (Silicon Overlays 1-5, 27, 40, 48)",
    "note": "Power budget fully specified per tier. Context-flow eliminates memory hierarchy power overhead."
  },
  {
    "id": 44,
    "domain": "Automotive Applications & Systems",
    "name": "Automotive Vibration Filtering & Noise Rejection",
    "coverage": "GAP",
    "chapters": "Ch.14.3, Ch.44, Ch.25 (STOP-5)",
    "note": "Requires research on MEMS accelerometer compensation, PCB-level vibration isolation.",
    "gapPriority": "MEDIUM",
    "gapEffort": "5-7"
  },
  {
    "id": 45,
    "domain": "Automotive Applications & Systems",
    "name": "PCB & Chassis Vibration Analysis",
    "coverage": "GAP",
    "chapters": "Ch.27, Ch.39.1, Ch.44",
    "note": "Critical for physical reliability. Needs collaboration with packaging/mechanical engineering.",
    "gapPriority": "MEDIUM",
    "gapEffort": "5-7"
  },
  {
    "id": 46,
    "domain": "Advanced & Emerging Topics",
    "name": "Neuromorphic Data Compression",
    "coverage": "HIGH",
    "chapters": "Ch.15, Ch.22, Ch.26.2 (Compression)",
    "note": "Population moment compression IS neuromorphic data compression. Unique Ghost contribution."
  },
  {
    "id": 47,
    "domain": "Advanced & Emerging Topics",
    "name": "Adaptive Thresholding for Sensors & Computing",
    "coverage": "HIGH",
    "chapters": "Ch.17, Ch.29.3, Ch.23.6",
    "note": "ATHRESH is hardware-level adaptive threshold. Multiple adaptive systems layered."
  },
  {
    "id": 48,
    "domain": "Advanced & Emerging Topics",
    "name": "Cooperative Perception & V2X Safety",
    "coverage": "PARTIAL",
    "chapters": "Ch.13 (Quad-Plus Alliance), Ch.38 (Comparative)",
    "note": "Byzantine protocol designed for multi-agent coordination. V2X-specific adaptation needs research."
  },
  {
    "id": 49,
    "domain": "Advanced & Emerging Topics",
    "name": "Trust Scores & Byzantine Fault Tolerance in Multi-Agent",
    "coverage": "FULL",
    "chapters": "Ch.28 (entire), Ch.23.5 (Safety Instructions)",
    "note": "Most complete BFT specification in the project. Trust scores are first-class ISA citizens."
  },
  {
    "id": 50,
    "domain": "Advanced & Emerging Topics",
    "name": "Adaptive Safety Envelopes (RSS, Safety Force Field)",
    "coverage": "HIGH",
    "chapters": "Ch.29, Ch.17, Ch.8.5 (Risk & Safety)",
    "note": "RMAA + Trusted Scalar + Contractual Roles = complete adaptive safety envelope stack."
  },
  {
    "id": 51,
    "domain": "Advanced & Emerging Topics",
    "name": "Quantum Inertial Navigation (Cold Atom)",
    "coverage": "GAP",
    "chapters": "Ch.14.5, Ch.38.3",
    "note": "Future roadmap item. Relevant for sovereign navigation capability (Ch.13 Quad-Plus).",
    "gapPriority": "LOW",
    "gapEffort": "7-10"
  },
  {
    "id": 52,
    "domain": "Advanced & Emerging Topics",
    "name": "Quantum Magnetometry (NV Center)",
    "coverage": "GAP",
    "chapters": "Ch.14.5, Ch.38.3",
    "note": "Potential for road-surface magnetic mapping. Research-stage technology.",
    "gapPriority": "LOW",
    "gapEffort": "5-7"
  },
  {
    "id": 53,
    "domain": "Advanced & Emerging Topics",
    "name": "Event-Driven Sensor Data Reduction",
    "coverage": "HIGH",
    "chapters": "Ch.14.2, Ch.25.3, Ch.26",
    "note": "Event-driven data reduction at multiple levels: sensor (CFAR), compute (event registers), decision (action gradient)."
  },
  {
    "id": 54,
    "domain": "Advanced & Emerging Topics",
    "name": "Deterministic Ultra-Low Latency Processing",
    "coverage": "FULL",
    "chapters": "Ch.20, Ch.23, Ch.26, Ch.27, Ch.28",
    "note": "The defining capability of Ghost. Every path timed, no non-deterministic elements in critical path."
  },
  {
    "id": 55,
    "domain": "Advanced & Emerging Topics",
    "name": "Hardware/Software Partitioning for Automotive SoCs",
    "coverage": "HIGH",
    "chapters": "Ch.24.4, Ch.27, Ch.29",
    "note": "Clear partition: safety-critical constraints in hardware (non-bypassable), application logic in software."
  },
  {
    "id": 56,
    "domain": "Advanced & Emerging Topics",
    "name": "Custom ASIC Design for Safety Metrics",
    "coverage": "FULL",
    "chapters": "Ch.27 (entire), Ch.29, Ch.39.1",
    "note": "The EPU ASIC IS the custom safety metrics processor. Complete design specification available."
  }
];

export const SOURCE_XREF = [
  {
    "file": "PICAPD_INSTRUCTION_SET_ARCHITECTURE.md",
    "ids": [
      5,
      9,
      12,
      13,
      15,
      19,
      20,
      22,
      26,
      27,
      31,
      34,
      35,
      40,
      41,
      42,
      47,
      49,
      53,
      54
    ],
    "count": 20
  },
  {
    "file": "PICAPD_Silicon.md",
    "ids": [
      8,
      12,
      13,
      14,
      15,
      18,
      19,
      20,
      21,
      22,
      25,
      31,
      33,
      39,
      42,
      43,
      54,
      55,
      56
    ],
    "count": 19
  },
  {
    "file": "revised_queen_bee_architecture.docx",
    "ids": [
      10,
      12,
      13,
      15,
      16,
      18,
      20,
      21,
      31,
      33,
      37,
      39,
      41,
      42,
      43,
      46,
      54,
      55,
      56
    ],
    "count": 19
  },
  {
    "file": "revised_mom_pbe_agent_governance.docx",
    "ids": [
      10,
      12,
      13,
      41,
      46
    ],
    "count": 5
  },
  {
    "file": "Part_1_BFT",
    "ids": [
      8,
      20,
      41,
      48,
      49,
      54
    ],
    "count": 6
  },
  {
    "file": "Part_2_Signal_Routing",
    "ids": [
      48
    ],
    "count": 1
  },
  {
    "file": "Part_3_Persistence",
    "ids": [
      35,
      49
    ],
    "count": 2
  },
  {
    "file": "Bitvector_Data_Pipeline",
    "ids": [
      5,
      9,
      10,
      22,
      23,
      25,
      27,
      44,
      53
    ],
    "count": 9
  },
  {
    "file": "Calibrated_UQ",
    "ids": [
      9,
      27,
      32,
      34,
      36,
      47,
      50
    ],
    "count": 7
  },
  {
    "file": "EPU_Frontier",
    "ids": [
      12,
      26,
      40,
      42,
      53,
      54
    ],
    "count": 6
  },
  {
    "file": "EPU__Lagrangian_",
    "ids": [
      9,
      26,
      38,
      40,
      42,
      53
    ],
    "count": 6
  },
  {
    "file": "rmaa_admissibility_evaluation.docx",
    "ids": [
      31,
      32,
      33,
      36,
      47,
      50,
      55
    ],
    "count": 7
  },
  {
    "file": "trusted_scalar_admissibility_evaluation.docx",
    "ids": [
      32,
      35,
      50
    ],
    "count": 3
  },
  {
    "file": "contractual_roles_admissibility_evaluation.docx",
    "ids": [
      50,
      55
    ],
    "count": 2
  },
  {
    "file": "Archipelago_Revisions.docx",
    "ids": [
      36,
      47
    ],
    "count": 2
  },
  {
    "file": "core_philosophy_document",
    "ids": [
      13,
      15,
      21,
      31,
      33,
      34,
      41,
      55,
      56
    ],
    "count": 9
  },
  {
    "file": "neural-network_gap",
    "ids": [
      4,
      11,
      16,
      21,
      37
    ],
    "count": 5
  },
  {
    "file": "On-chip_Lagrangian_NNs",
    "ids": [
      11,
      16,
      18,
      21,
      23,
      28,
      29,
      49
    ],
    "count": 8
  },
  {
    "file": "memristor-based_neuromorphic_compute",
    "ids": [
      11,
      43,
      48
    ],
    "count": 3
  }
];

export const PROGRAM_SUMMARY = {
  totalSubcategories: 56,
  masterDoc: 'main(8).tex',
  gapEffortPersonDays: '55-80',
  note: 'Coverage is source-material availability (design corpus), not fabricated/measured silicon.'
};

// =========================================================================
// GAP-CLOSURE PLAN (Void V-2: program-gap-closure-plan)
// -------------------------------------------------------------------------
// The coverage map named 11 gaps and a ~55-80 person-day total, but did not
// say *how* they close. This turns each gap into a workstream item with an
// owner, an acquisition route (how the source material is obtained), an exit
// condition (what closes the obligation), and a target coverage — then
// sequences them into waves by what they unblock. Honest framing is preserved:
// closing a gap raises *source-material coverage*, not built/measured silicon.
// =========================================================================

export const WAVES = [
  { id: 1, name: 'Wave 1 — Qualification-blocking', rationale: 'Gates AEC-Q100 / ISO 16750-3 qualification; do first.' },
  { id: 2, name: 'Wave 2 — Design-supporting', rationale: 'Needed for packaging & reliability robustness, not a hard gate.' },
  { id: 3, name: 'Wave 3 — Future roadmap', rationale: 'Research-stage; parked behind the product MVP.' }
];

// Per-gap closure detail, keyed by subcategory id. Only the 11 GAP rows appear.
export const GAP_CLOSURE = {
  // Wave 1 — HIGH priority, qualification-blocking
  2:  { wave: 1, owner: 'Reliability & Test Eng', target: 'HIGH',
        acquisition: 'License ISO 16750-3 / ISO 21; ingest AEC-Q100 random-vibration profiles',
        exit: 'Bit-precise random-vibration profiles + g_RMS budgets feed Ch.39 silicon results' },
  3:  { wave: 1, owner: 'Reliability & Test Eng', target: 'HIGH',
        acquisition: 'SAE / road-load spectra datasets (highway, urban, off-road)',
        exit: 'Environment-specific PSD spectra parameterize sensor reliability models (Ch.14.3)' },
  30: { wave: 1, owner: 'Functional Safety / Qualification', target: 'HIGH',
        acquisition: 'Purchase ISO 16750-3; map to the AEC-Q100 stress matrix',
        exit: 'Vibration PSD profiles + a test plan close the AEC-Q100 packaging gate' },
  // Wave 2 — MEDIUM priority, design-supporting
  1:  { wave: 2, owner: 'Sensor Physics', target: 'PARTIAL',
        acquisition: 'Vendor datasheets + ISO 16750-3-derived noise models',
        exit: 'A PCB-level noise budget for EPU packaging (Ch.44)' },
  44: { wave: 2, owner: 'Signal Processing / DSP', target: 'PARTIAL',
        acquisition: 'MEMS-compensation literature; co-design with the STOP-5 bitvector',
        exit: 'A vibration-rejection filter spec integrated with STOP-5 (Ch.25)' },
  45: { wave: 2, owner: 'Packaging & Mechanical (cross-team)', target: 'PARTIAL',
        acquisition: 'FEA modal-analysis collaboration; chassis-mount measurement data',
        exit: 'A modal map + isolation strategy for the EPU board (Ch.44)' },
  // Wave 3 — LOW priority, future roadmap
  6:  { wave: 3, owner: 'Advanced Sensing R&D', target: 'PARTIAL',
        acquisition: 'Literature survey (cold-atom / NV-center); vendor whitepapers',
        exit: 'A roadmap brief on quantum inertial relevance for GPS-denied ODD (Ch.38)' },
  7:  { wave: 3, owner: 'Advanced Sensing R&D', target: 'PARTIAL',
        acquisition: 'Photon-counting LiDAR literature',
        exit: 'A feasibility note: low-SNR perception gains vs. cost' },
  24: { wave: 3, owner: 'Numerics Team', target: 'PARTIAL',
        acquisition: 'Posit Working-Group standard + a fixed-point comparison study',
        exit: 'A dynamic-range trade study: Posit vs. Q-format for Ch.24' },
  51: { wave: 3, owner: 'Advanced Sensing R&D', target: 'PARTIAL',
        acquisition: 'Cold-atom interferometry literature + partnership scouting',
        exit: 'A sovereign-navigation roadmap entry (Ch.13 Quad-Plus)' },
  52: { wave: 3, owner: 'Advanced Sensing R&D', target: 'PARTIAL',
        acquisition: 'NV-center magnetometry literature',
        exit: 'A road-surface magnetic-mapping feasibility note' }
};

// Parse a "lo-hi" effort string into { lo, hi } person-days.
function _effortRange(s) {
  const [lo, hi] = String(s || '0-0').split('-').map(Number);
  return { lo: lo || 0, hi: hi || lo || 0 };
}

// The plan: every GAP subcategory, augmented with its closure detail + parsed
// effort, sorted by wave then by descending effort (heavier items first).
export const GAP_CLOSURE_PLAN = SUBCATEGORIES
  .filter(s => s.coverage === 'GAP')
  .map(s => {
    const plan = GAP_CLOSURE[s.id] || { wave: 3, owner: 'Unassigned', target: 'PARTIAL', acquisition: 'TBD', exit: 'TBD' };
    const effort = _effortRange(s.gapEffort);
    return {
      id: s.id, name: s.name, domain: s.domain,
      priority: s.gapPriority || 'LOW',
      effortLabel: s.gapEffort, effortLo: effort.lo, effortHi: effort.hi,
      ...plan
    };
  })
  .sort((a, b) => a.wave - b.wave || b.effortHi - a.effortHi);

// Roll-up: totals + per-wave + per-priority breakdown, all derived.
export const GAP_PLAN_SUMMARY = (() => {
  const lo = GAP_CLOSURE_PLAN.reduce((a, g) => a + g.effortLo, 0);
  const hi = GAP_CLOSURE_PLAN.reduce((a, g) => a + g.effortHi, 0);
  const byWave = WAVES.map(w => {
    const items = GAP_CLOSURE_PLAN.filter(g => g.wave === w.id);
    return {
      ...w,
      count: items.length,
      effortLo: items.reduce((a, g) => a + g.effortLo, 0),
      effortHi: items.reduce((a, g) => a + g.effortHi, 0)
    };
  });
  const byPriority = ['HIGH', 'MEDIUM', 'LOW'].reduce((acc, p) => {
    acc[p] = GAP_CLOSURE_PLAN.filter(g => g.priority === p).length;
    return acc;
  }, {});
  return { gaps: GAP_CLOSURE_PLAN.length, effortLo: lo, effortHi: hi, byWave, byPriority };
})();
