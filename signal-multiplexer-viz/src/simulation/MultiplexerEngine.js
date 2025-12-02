/**
 * Adaptive Signal Multiplexer Simulation Engine
 * Simplified JavaScript implementation for visualization
 */

export class MultiplexerEngine {
  constructor(config = {}) {
    this.config = {
      totalBandwidth: config.totalBandwidth || 1000,
      totalTimeSlots: config.totalTimeSlots || 100,
      optimizationInterval: config.optimizationInterval || 100,
      adaptationInterval: config.adaptationInterval || 1000,
      ...config
    };

    this.channels = new Map();
    this.resourceState = {
      availableBandwidth: this.config.totalBandwidth,
      totalTimeSlots: this.config.totalTimeSlots
    };

    this.currentProblem = null;
    this.currentSolution = null;
    this.currentStructure = null;
    this.selectedSolver = null;
    this.constraints = [];
    this.observations = [];
    this.performanceMetrics = {
      totalSignalsProcessed: 0,
      averageLatency: 0,
      throughput: 0,
      fairnessIndex: 1.0
    };

    this.state = 'idle'; // idle, observing, formulating, solving, applying
    this.listeners = new Set();

    // Demultiplexing state
    this.demuxState = {
      extractedSignals: {},
      totalExtracted: 0,
      successRate: 1.0,
      channelQuality: {}
    };
  }

  // Event handling
  on(event, callback) {
    this.listeners.add({ event, callback });
  }

  emit(event, data) {
    this.listeners.forEach(({ event: e, callback }) => {
      if (e === event) callback(data);
    });
  }

  // Core optimization loop (called every 100ms)
  reformulateAndSolve() {
    // 1. OBSERVE
    this.state = 'observing';
    const snapshot = this.captureSnapshot();
    this.emit('snapshot', snapshot);

    // 2. FORMULATE
    this.state = 'formulating';
    const problem = this.formulateProblem(snapshot);
    this.currentProblem = problem;
    this.emit('problem_formulated', problem);

    // 3. DETECT STRUCTURE
    const structure = this.detectStructure(problem);
    this.currentStructure = structure;
    this.emit('structure_detected', structure);

    // 4. SELECT SOLVER
    const solver = this.selectSolver(structure);
    this.selectedSolver = solver;
    this.emit('solver_selected', solver);

    // 5. SYNTHESIZE CONSTRAINTS
    const constraints = this.synthesizeConstraints(snapshot, structure);
    this.constraints = constraints;
    problem.constraints = constraints;
    this.emit('constraints_synthesized', constraints);

    // 6. SOLVE
    this.state = 'solving';
    const solution = this.solve(problem, solver);
    this.currentSolution = solution;
    this.emit('solution_found', solution);

    // 7. APPLY
    this.state = 'applying';
    this.applySolution(solution);
    this.emit('solution_applied', solution);

    this.state = 'idle';

    return solution;
  }

  // 1. OBSERVE: Capture system snapshot
  captureSnapshot() {
    const channelStates = {};

    this.channels.forEach((channel, id) => {
      channelStates[id] = {
        queueSize: channel.queue.length,
        priorityWeight: this.getPriorityWeight(channel.priority),
        utilization: channel.utilization || 0.7,
        minBandwidth: this.getMinBandwidthForPriority(channel.priority),
        arrivalRate: channel.arrivalRate || 10.0,
        currentBandwidth: channel.bandwidth
      };
    });

    return {
      channelStates,
      availableBandwidth: this.resourceState.availableBandwidth,
      totalTimeSlots: this.resourceState.totalTimeSlots,
      timestamp: Date.now()
    };
  }

  // 2. FORMULATE: Construct optimization problem
  formulateProblem(snapshot) {
    const problem = {
      variables: {},
      objectives: [],
      constraints: [],
      type: 'multi-objective'
    };

    const activeChannels = Object.keys(snapshot.channelStates);

    // Define decision variables
    activeChannels.forEach(channelId => {
      problem.variables[`b_${channelId}`] = {
        type: 'continuous',
        bounds: [0, snapshot.availableBandwidth],
        description: `Bandwidth allocation for channel ${channelId}`
      };
      problem.variables[`s_${channelId}`] = {
        type: 'integer',
        bounds: [0, snapshot.totalTimeSlots],
        description: `Time slots for channel ${channelId}`
      };
    });

    // Define objectives
    problem.objectives.push({
      name: 'minimize_latency',
      weight: 1.0,
      description: 'Minimize total latency: Σ(queue_i / b_i)',
      formula: 'Σ(queue_i / b_i) × priority_i'
    });

    problem.objectives.push({
      name: 'maximize_throughput',
      weight: 0.8,
      description: 'Maximize throughput: Σ(b_i × utilization_i)',
      formula: '-Σ(b_i × utilization_i)'
    });

    problem.objectives.push({
      name: 'maximize_fairness',
      weight: 0.5,
      description: 'Balance fairness: minimize variance of service rates',
      formula: 'minimize variance(b_i)'
    });

    return problem;
  }

  // 3. DETECT STRUCTURE: Identify problem class
  detectStructure(problem) {
    const structure = {
      isConvex: true,
      isMixedInteger: true, // has time slot variables
      isStochastic: false,
      isGameTheoretic: false,
      isRecedingHorizon: false,
      hasMultipleObjectives: problem.objectives.length > 1
    };

    // Determine problem class
    if (structure.isMixedInteger && structure.hasMultipleObjectives) {
      structure.class = 'Multi-Objective Mixed-Integer Program (MOMIP)';
    } else if (structure.isConvex) {
      structure.class = 'Convex Optimization';
    } else {
      structure.class = 'General Nonlinear Program';
    }

    return structure;
  }

  // 4. SELECT SOLVER: Choose appropriate method
  selectSolver(structure) {
    if (structure.isConvex && !structure.isMixedInteger) {
      return {
        name: 'Interior Point Method',
        type: 'convex',
        description: 'Fast gradient-based solver for convex problems',
        complexity: 'O(n³)',
        guarantees: ['Global optimality', 'Polynomial time']
      };
    } else if (structure.isMixedInteger) {
      return {
        name: 'Branch and Bound',
        type: 'mixed-integer',
        description: 'Exact solver using branch-and-bound with cutting planes',
        complexity: 'Exponential (worst case)',
        guarantees: ['Optimal solution', 'Anytime certificates']
      };
    } else if (structure.hasMultipleObjectives) {
      return {
        name: 'Weighted Sum Method',
        type: 'multi-objective',
        description: 'Scalarization approach for multi-objective optimization',
        complexity: 'O(m × n³)',
        guarantees: ['Pareto optimal solution']
      };
    } else {
      return {
        name: 'Greedy Heuristic',
        type: 'heuristic',
        description: 'Fast approximation algorithm',
        complexity: 'O(n log n)',
        guarantees: ['Feasible solution', 'No optimality guarantee']
      };
    }
  }

  // 5. SYNTHESIZE CONSTRAINTS: Physics-informed constraints
  synthesizeConstraints(snapshot, structure) {
    const constraints = [];

    // Conservation of bandwidth (fundamental physical constraint)
    constraints.push({
      name: 'bandwidth_conservation',
      type: 'physics-informed',
      category: 'conservation',
      description: 'Total allocated bandwidth ≤ available bandwidth',
      formula: 'Σ b_i ≤ B_total',
      tightness: 1.0
    });

    // Stability constraints (queue dynamics)
    Object.keys(snapshot.channelStates).forEach(channelId => {
      const state = snapshot.channelStates[channelId];
      constraints.push({
        name: `stability_${channelId}`,
        type: 'physics-informed',
        category: 'dynamics',
        description: `Service rate must exceed arrival rate for channel ${channelId}`,
        formula: `b_${channelId} ≥ λ_${channelId} × 1.1`,
        tightness: 1.1,
        arrivalRate: state.arrivalRate
      });
    });

    // Minimum service guarantees
    Object.keys(snapshot.channelStates).forEach(channelId => {
      const state = snapshot.channelStates[channelId];
      constraints.push({
        name: `min_service_${channelId}`,
        type: 'requirement',
        category: 'service',
        description: `Minimum bandwidth for channel ${channelId}`,
        formula: `b_${channelId} ≥ b_min_${channelId}`,
        minBandwidth: state.minBandwidth
      });
    });

    // Causality constraints (time ordering)
    constraints.push({
      name: 'causality',
      type: 'physics-informed',
      category: 'causality',
      description: 'Events must respect temporal ordering',
      formula: 't_i < t_j for all dependent events'
    });

    return constraints;
  }

  // 6. SOLVE: Execute optimization
  solve(problem, solver) {
    // Simplified solver - uses greedy allocation weighted by priority
    const solution = {
      feasible: true,
      bandwidthAllocations: {},
      scheduleSlots: {},
      objectiveValue: 0,
      solverTime: Math.random() * 30 + 10, // 10-40ms
      iterations: Math.floor(Math.random() * 100) + 50
    };

    const snapshot = this.captureSnapshot();
    const channelIds = Object.keys(snapshot.channelStates);

    // Calculate total priority weight
    const totalWeight = channelIds.reduce((sum, id) => {
      return sum + snapshot.channelStates[id].priorityWeight;
    }, 0);

    // Allocate bandwidth proportionally to priority and queue size
    channelIds.forEach(channelId => {
      const state = snapshot.channelStates[channelId];
      const queueFactor = state.queueSize > 0 ? Math.log(state.queueSize + 1) : 0;
      const weight = state.priorityWeight * (1 + queueFactor);

      // Bandwidth allocation
      const baseBandwidth = (state.priorityWeight / totalWeight) * snapshot.availableBandwidth;
      const adjustedBandwidth = Math.max(
        state.minBandwidth,
        Math.min(baseBandwidth * (1 + queueFactor / 10), snapshot.availableBandwidth)
      );

      solution.bandwidthAllocations[channelId] = adjustedBandwidth;

      // Time slot allocation
      const slots = Math.floor((adjustedBandwidth / snapshot.availableBandwidth) * snapshot.totalTimeSlots);
      solution.scheduleSlots[channelId] = Math.max(1, slots);
    });

    return solution;
  }

  // 7. APPLY: Update system configuration
  applySolution(solution) {
    Object.entries(solution.bandwidthAllocations).forEach(([channelId, bandwidth]) => {
      const channel = this.channels.get(channelId);
      if (channel) {
        channel.bandwidth = bandwidth;
        channel.timeSlots = solution.scheduleSlots[channelId];
      }
    });

    this.resourceState.allocatedBandwidth = Object.values(solution.bandwidthAllocations)
      .reduce((sum, bw) => sum + bw, 0);
    this.resourceState.availableBandwidth =
      this.config.totalBandwidth - this.resourceState.allocatedBandwidth;
  }

  // Channel management
  createChannel(channelId, priority = 'NORMAL') {
    const minBandwidth = this.getMinBandwidthForPriority(priority);

    const channel = {
      id: channelId,
      priority,
      queue: [],
      bandwidth: minBandwidth,
      timeSlots: 1,
      arrivalRate: 0,
      utilization: 0,
      totalSignals: 0,
      createdAt: Date.now()
    };

    this.channels.set(channelId, channel);
    this.emit('channel_created', channel);

    return channel;
  }

  sendSignal(channelId, signal) {
    let channel = this.channels.get(channelId);

    if (!channel) {
      channel = this.createChannel(channelId, signal.priority || 'NORMAL');
    }

    channel.queue.push({
      ...signal,
      enqueuedAt: Date.now()
    });

    // Update arrival rate (exponential smoothing)
    const alpha = 0.3;
    channel.arrivalRate = alpha * (channel.arrivalRate + 1) + (1 - alpha) * channel.arrivalRate;

    this.observations.push({
      type: 'signal_enqueued',
      channelId,
      timestamp: Date.now()
    });

    this.emit('signal_enqueued', { channelId, signal });

    // Trigger immediate optimization if critical
    if (signal.priority === 'CRITICAL') {
      this.reformulateAndSolve();
    }

    return { success: true, channelId };
  }

  // Process signals based on bandwidth allocation
  processSignals() {
    this.channels.forEach((channel, channelId) => {
      const signalsToProcess = Math.min(
        channel.queue.length,
        Math.floor(channel.bandwidth / 10) // simplified processing rate
      );

      for (let i = 0; i < signalsToProcess; i++) {
        const signal = channel.queue.shift();
        if (signal) {
          const latency = Date.now() - signal.enqueuedAt;
          channel.totalSignals++;

          this.performanceMetrics.totalSignalsProcessed++;
          this.performanceMetrics.averageLatency =
            (this.performanceMetrics.averageLatency * 0.9) + (latency * 0.1);
        }
      }

      channel.utilization = Math.min(1.0, channel.queue.length / 100);
    });

    this.performanceMetrics.throughput = this.performanceMetrics.totalSignalsProcessed /
      ((Date.now() - this.startTime) / 1000);

    // Demultiplex processed signals
    this.demultiplexSignals();
  }

  // Demultiplex signals - extract and recover from multiplexed channels
  demultiplexSignals() {
    this.channels.forEach((channel, channelId) => {
      // Initialize demux arrays for this channel
      if (!this.demuxState.extractedSignals[channelId]) {
        this.demuxState.extractedSignals[channelId] = [];
      }

      // Simulate signal extraction based on bandwidth and time slots
      const extractionRate = channel.bandwidth / 100; // signals per cycle
      const signalsToExtract = Math.floor(extractionRate);

      for (let i = 0; i < signalsToExtract; i++) {
        // Simulate signal quality based on channel conditions
        const baseQuality = 0.85 + Math.random() * 0.15;
        const priorityBonus = this.getPriorityWeight(channel.priority) * 0.02;
        const congestionPenalty = channel.queue.length > 50 ? 0.1 : 0;
        const quality = Math.min(1.0, baseQuality + priorityBonus - congestionPenalty);

        const extractedSignal = {
          id: `extracted-${channelId}-${Date.now()}-${i}`,
          channelId,
          priority: channel.priority,
          quality,
          verified: quality > 0.8,
          timestamp: Date.now(),
          method: this.getDemuxMethod(channel)
        };

        this.demuxState.extractedSignals[channelId].push(extractedSignal);
        this.demuxState.totalExtracted++;

        // Keep only recent signals (last 20)
        if (this.demuxState.extractedSignals[channelId].length > 20) {
          this.demuxState.extractedSignals[channelId].shift();
        }
      }

      // Update channel quality metrics
      this.demuxState.channelQuality[channelId] = {
        snr: 20 + Math.random() * 20, // Signal-to-Noise Ratio (dB)
        ber: Math.random() * 0.001, // Bit Error Rate
        jitter: Math.random() * 5, // Jitter in ms
        loss: Math.random() * 0.02 // Packet loss rate
      };
    });

    // Calculate overall success rate
    let totalVerified = 0;
    let totalSignals = 0;
    Object.values(this.demuxState.extractedSignals).forEach(signals => {
      signals.forEach(signal => {
        totalSignals++;
        if (signal.verified) totalVerified++;
      });
    });

    this.demuxState.successRate = totalSignals > 0 ? totalVerified / totalSignals : 1.0;
  }

  getDemuxMethod(channel) {
    // Determine demultiplexing method based on allocation strategy
    const methods = ['TDM', 'FDM', 'CDM', 'Adaptive'];
    if (channel.timeSlots > channel.bandwidth / 10) return 'TDM'; // Time Division
    if (channel.bandwidth > 200) return 'FDM'; // Frequency Division
    if (channel.priority === 'CRITICAL') return 'CDM'; // Code Division
    return 'Adaptive'; // Adaptive optimization-based
  }

  // Helper methods
  getPriorityWeight(priority) {
    const weights = {
      'CRITICAL': 4.0,
      'HIGH': 2.0,
      'NORMAL': 1.0,
      'LOW': 0.5
    };
    return weights[priority] || 1.0;
  }

  getMinBandwidthForPriority(priority) {
    const minBandwidths = {
      'CRITICAL': 100.0,
      'HIGH': 50.0,
      'NORMAL': 20.0,
      'LOW': 10.0
    };
    return minBandwidths[priority] || 20.0;
  }

  // Get current state for visualization
  getState() {
    return {
      channels: Array.from(this.channels.values()),
      resourceState: this.resourceState,
      currentProblem: this.currentProblem,
      currentSolution: this.currentSolution,
      currentStructure: this.currentStructure,
      selectedSolver: this.selectedSolver,
      constraints: this.constraints,
      performanceMetrics: this.performanceMetrics,
      demuxState: this.demuxState,
      state: this.state
    };
  }

  start() {
    this.startTime = Date.now();
    this.running = true;
  }

  stop() {
    this.running = false;
  }
}
