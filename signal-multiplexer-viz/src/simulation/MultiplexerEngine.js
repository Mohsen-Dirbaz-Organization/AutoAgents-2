/**
 * Enhanced Adaptive Signal Multiplexer Simulation Engine
 * With real-time operational scenarios and parameter interplay visualization
 */

export class MultiplexerEngine {
  constructor(config = {}) {
    this.config = {
      totalBandwidth: config.totalBandwidth || 1000,
      totalTimeSlots: config.totalTimeSlots || 100,
      optimizationInterval: config.optimizationInterval || 100,
      adaptationInterval: config.adaptationInterval || 1000,
      historyLength: config.historyLength || 200, // Keep last 200 data points
      ...config
    };

    this.channels = new Map();
    this.resourceState = {
      availableBandwidth: this.config.totalBandwidth,
      totalTimeSlots: this.config.totalTimeSlots,
      allocatedBandwidth: 0
    };

    // Enhanced state tracking
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
      fairnessIndex: 1.0,
      constraintViolations: 0,
      adaptationCount: 0
    };

    // Time-series history for parameter interplay visualization
    this.history = {
      timestamps: [],
      bandwidth: {},  // Per-channel bandwidth over time
      queueSizes: {}, // Per-channel queue sizes
      latency: [],    // Average latency
      throughput: [], // System throughput
      fairness: [],   // Fairness index
      constraintTightness: [], // Adaptation of constraint tightness
      predictionError: [], // Model prediction error
      arrivalRates: {} // Per-channel arrival rates
    };

    // Adaptive learning parameters
    this.adaptiveParams = {
      constraintTightness: 1.1,
      predictionError: 0.0,
      learningRate: 0.1,
      modelAccuracy: 1.0
    };

    // Operational scenario
    this.scenario = {
      mode: 'steady', // steady, burst, overload, recovery, mixed
      intensity: 1.0,
      parameters: {}
    };

    this.state = 'idle';
    this.listeners = new Set();

    // Demultiplexing state
    this.demuxState = {
      extractedSignals: {},
      totalExtracted: 0,
      successRate: 1.0,
      channelQuality: {}
    };
    this.cycleCount = 0;
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

  // ========================================================================
  // ENHANCED CORE LOOP WITH ADAPTATION TRACKING
  // ========================================================================

  reformulateAndSolve() {
    this.cycleCount++;

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

    // 5. SYNTHESIZE CONSTRAINTS (with adaptive tightness)
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

    // 8. LEARN - Update adaptive parameters
    this.updateAdaptiveParameters(snapshot, solution);

    // 9. RECORD HISTORY
    this.recordHistory();

    this.state = 'idle';

    return solution;
  }

  // ========================================================================
  // TIME-SERIES HISTORY TRACKING
  // ========================================================================

  recordHistory() {
    const now = Date.now();
    this.history.timestamps.push(now);

    // Record bandwidth allocations
    this.channels.forEach((channel, id) => {
      if (!this.history.bandwidth[id]) this.history.bandwidth[id] = [];
      if (!this.history.queueSizes[id]) this.history.queueSizes[id] = [];
      if (!this.history.arrivalRates[id]) this.history.arrivalRates[id] = [];

      this.history.bandwidth[id].push(channel.bandwidth);
      this.history.queueSizes[id].push(channel.queue.length);
      this.history.arrivalRates[id].push(channel.arrivalRate || 0);
    });

    // Record system metrics
    this.history.latency.push(this.performanceMetrics.averageLatency);
    this.history.throughput.push(this.performanceMetrics.throughput);
    this.history.fairness.push(this.performanceMetrics.fairnessIndex);
    this.history.constraintTightness.push(this.adaptiveParams.constraintTightness);
    this.history.predictionError.push(this.adaptiveParams.predictionError);

    // Trim history to keep only recent data
    const maxLength = this.config.historyLength;
    if (this.history.timestamps.length > maxLength) {
      const excess = this.history.timestamps.length - maxLength;
      this.history.timestamps.splice(0, excess);
      this.history.latency.splice(0, excess);
      this.history.throughput.splice(0, excess);
      this.history.fairness.splice(0, excess);
      this.history.constraintTightness.splice(0, excess);
      this.history.predictionError.splice(0, excess);

      Object.keys(this.history.bandwidth).forEach(id => {
        this.history.bandwidth[id].splice(0, excess);
        this.history.queueSizes[id].splice(0, excess);
        this.history.arrivalRates[id].splice(0, excess);
      });
    }
  }

  // ========================================================================
  // ADAPTIVE LEARNING
  // ========================================================================

  updateAdaptiveParameters(snapshot, solution) {
    // Calculate prediction error based on queue behavior
    let totalError = 0;
    let count = 0;

    this.channels.forEach((channel, id) => {
      const state = snapshot.channelStates[id];
      if (state) {
        // Predicted queue size based on arrival rate and bandwidth
        const serviceRate = channel.bandwidth / 10;
        const predictedQueue = Math.max(0, state.arrivalRate - serviceRate);
        const actualQueue = state.queueSize;
        const error = Math.abs(predictedQueue - actualQueue) / (actualQueue + 1);

        totalError += error;
        count++;
      }
    });

    if (count > 0) {
      const avgError = totalError / count;
      this.adaptiveParams.predictionError =
        (this.adaptiveParams.predictionError * 0.9) + (avgError * 0.1);

      // Adapt constraint tightness based on prediction error
      if (this.adaptiveParams.predictionError > 0.3) {
        // High error - tighten constraints
        this.adaptiveParams.constraintTightness = Math.min(
          2.0,
          this.adaptiveParams.constraintTightness * 1.05
        );
        this.performanceMetrics.adaptationCount++;
      } else if (this.adaptiveParams.predictionError < 0.1) {
        // Low error - can relax constraints
        this.adaptiveParams.constraintTightness = Math.max(
          1.0,
          this.adaptiveParams.constraintTightness * 0.98
        );
      }

      // Update model accuracy
      this.adaptiveParams.modelAccuracy = 1.0 - this.adaptiveParams.predictionError;
    }

    // Calculate fairness index (Jain's fairness index)
    const bandwidths = Array.from(this.channels.values()).map(c => c.bandwidth);
    if (bandwidths.length > 0) {
      const sum = bandwidths.reduce((a, b) => a + b, 0);
      const sumSq = bandwidths.reduce((a, b) => a + b * b, 0);
      this.performanceMetrics.fairnessIndex =
        (sum * sum) / (bandwidths.length * sumSq);
    }
  }

  // ========================================================================
  // REALISTIC OPERATIONAL SCENARIOS
  // ========================================================================

  setScenario(mode, intensity = 1.0) {
    this.scenario.mode = mode;
    this.scenario.intensity = Math.max(0.1, Math.min(3.0, intensity));

    // Configure scenario-specific parameters
    switch (mode) {
      case 'steady':
        this.scenario.parameters = {
          burstProbability: 0.1,
          baseArrivalRate: 5 * intensity,
          priorityMix: { CRITICAL: 0.1, HIGH: 0.2, NORMAL: 0.5, LOW: 0.2 }
        };
        break;

      case 'burst':
        this.scenario.parameters = {
          burstProbability: 0.4,
          baseArrivalRate: 3 * intensity,
          burstMultiplier: 5,
          priorityMix: { CRITICAL: 0.3, HIGH: 0.4, NORMAL: 0.2, LOW: 0.1 }
        };
        break;

      case 'overload':
        this.scenario.parameters = {
          burstProbability: 0.6,
          baseArrivalRate: 15 * intensity,
          priorityMix: { CRITICAL: 0.4, HIGH: 0.3, NORMAL: 0.2, LOW: 0.1 }
        };
        break;

      case 'recovery':
        this.scenario.parameters = {
          burstProbability: 0.05,
          baseArrivalRate: 2 * intensity,
          drainMode: true,
          priorityMix: { CRITICAL: 0.05, HIGH: 0.15, NORMAL: 0.5, LOW: 0.3 }
        };
        break;

      case 'mixed':
        // Oscillating pattern
        this.scenario.parameters = {
          oscillation: true,
          period: 50, // cycles
          baseArrivalRate: 5 * intensity,
          priorityMix: { CRITICAL: 0.15, HIGH: 0.25, NORMAL: 0.4, LOW: 0.2 }
        };
        break;
    }

    this.emit('scenario_changed', this.scenario);
  }

  // Scenario-driven signal generation
  generateScenarioSignals() {
    const params = this.scenario.parameters;

    // Determine if this cycle should have a burst
    const isBurst = Math.random() < (params.burstProbability || 0.1);
    let signalCount = params.baseArrivalRate || 5;

    if (isBurst && params.burstMultiplier) {
      signalCount *= params.burstMultiplier;
    }

    // Mixed mode: oscillating pattern
    if (params.oscillation) {
      const phase = (this.cycleCount % params.period) / params.period;
      const oscillationFactor = 0.5 + 1.5 * Math.sin(2 * Math.PI * phase);
      signalCount *= oscillationFactor;
    }

    // Generate signals according to scenario
    const channelIds = Array.from(this.channels.keys());
    if (channelIds.length === 0) return;

    for (let i = 0; i < Math.floor(signalCount); i++) {
      // Select random channel
      const channelId = channelIds[Math.floor(Math.random() * channelIds.length)];

      // Select priority based on scenario mix
      const priority = this.selectPriorityFromMix(params.priorityMix);

      // Send signal
      this.sendSignal(channelId, {
        priority,
        data: `scenario-${this.scenario.mode}-${Date.now()}-${i}`,
        scenario: this.scenario.mode
      });
    }
  }

  selectPriorityFromMix(mix) {
    const rand = Math.random();
    let cumulative = 0;

    for (const [priority, probability] of Object.entries(mix)) {
      cumulative += probability;
      if (rand < cumulative) {
        return priority;
      }
    }

    return 'NORMAL';
  }

  // ========================================================================
  // ORIGINAL METHODS (Enhanced)
  // ========================================================================

  captureSnapshot() {
    const channelStates = {};

    this.channels.forEach((channel, id) => {
      channelStates[id] = {
        queueSize: channel.queue.length,
        priorityWeight: this.getPriorityWeight(channel.priority),
        utilization: channel.utilization || 0.7,
        minBandwidth: this.getMinBandwidthForPriority(channel.priority),
        arrivalRate: channel.arrivalRate || 10.0,
        currentBandwidth: channel.bandwidth,
        serviceRate: channel.bandwidth / 10
      };
    });

    return {
      channelStates,
      availableBandwidth: this.resourceState.availableBandwidth,
      totalTimeSlots: this.resourceState.totalTimeSlots,
      timestamp: Date.now(),
      scenarioMode: this.scenario.mode
    };
  }

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

    // Define objectives (weights adapt based on scenario)
    const latencyWeight = this.scenario.mode === 'overload' ? 1.5 : 1.0;
    const throughputWeight = this.scenario.mode === 'recovery' ? 1.2 : 0.8;
    const fairnessWeight = this.scenario.mode === 'steady' ? 0.7 : 0.5;

    problem.objectives.push({
      name: 'minimize_latency',
      weight: latencyWeight,
      description: 'Minimize total latency: Σ(queue_i / b_i)',
      formula: 'Σ(queue_i / b_i) × priority_i'
    });

    problem.objectives.push({
      name: 'maximize_throughput',
      weight: throughputWeight,
      description: 'Maximize throughput: Σ(b_i × utilization_i)',
      formula: '-Σ(b_i × utilization_i)'
    });

    problem.objectives.push({
      name: 'maximize_fairness',
      weight: fairnessWeight,
      description: 'Balance fairness: minimize variance of service rates',
      formula: 'minimize variance(b_i)'
    });

    return problem;
  }

  detectStructure(problem) {
    const structure = {
      isConvex: true,
      isMixedInteger: true,
      isStochastic: this.scenario.mode === 'mixed',
      isGameTheoretic: false,
      isRecedingHorizon: false,
      hasMultipleObjectives: problem.objectives.length > 1
    };

    if (structure.isMixedInteger && structure.hasMultipleObjectives) {
      structure.class = 'Multi-Objective Mixed-Integer Program (MOMIP)';
    } else if (structure.isConvex) {
      structure.class = 'Convex Optimization';
    } else {
      structure.class = 'General Nonlinear Program';
    }

    return structure;
  }

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

  synthesizeConstraints(snapshot, structure) {
    const constraints = [];

    // Conservation with adaptive tightness
    constraints.push({
      name: 'bandwidth_conservation',
      type: 'physics-informed',
      category: 'conservation',
      description: 'Total allocated bandwidth ≤ available bandwidth',
      formula: 'Σ b_i ≤ B_total',
      tightness: this.adaptiveParams.constraintTightness
    });

    // Stability constraints with adaptive margins
    Object.keys(snapshot.channelStates).forEach(channelId => {
      const state = snapshot.channelStates[channelId];
      constraints.push({
        name: `stability_${channelId}`,
        type: 'physics-informed',
        category: 'dynamics',
        description: `Service rate must exceed arrival rate for channel ${channelId}`,
        formula: `b_${channelId} ≥ λ_${channelId} × ${this.adaptiveParams.constraintTightness.toFixed(2)}`,
        tightness: this.adaptiveParams.constraintTightness,
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

    // Causality constraints
    constraints.push({
      name: 'causality',
      type: 'physics-informed',
      category: 'causality',
      description: 'Events must respect temporal ordering',
      formula: 't_i < t_j for all dependent events'
    });

    return constraints;
  }

  solve(problem, solver) {
    const solution = {
      feasible: true,
      bandwidthAllocations: {},
      scheduleSlots: {},
      objectiveValue: 0,
      solverTime: Math.random() * 30 + 10,
      iterations: Math.floor(Math.random() * 100) + 50
    };

    const snapshot = this.captureSnapshot();
    const channelIds = Object.keys(snapshot.channelStates);

    const totalWeight = channelIds.reduce((sum, id) => {
      return sum + snapshot.channelStates[id].priorityWeight;
    }, 0);

    channelIds.forEach(channelId => {
      const state = snapshot.channelStates[channelId];
      const queueFactor = state.queueSize > 0 ? Math.log(state.queueSize + 1) : 0;
      const weight = state.priorityWeight * (1 + queueFactor);

      // Enhanced allocation considering arrival rate
      const baseBandwidth = (state.priorityWeight / totalWeight) * snapshot.availableBandwidth;
      const demandBandwidth = state.arrivalRate * 10 * this.adaptiveParams.constraintTightness;
      const adjustedBandwidth = Math.max(
        state.minBandwidth,
        Math.min(
          Math.max(baseBandwidth * (1 + queueFactor / 10), demandBandwidth),
          snapshot.availableBandwidth
        )
      );

      solution.bandwidthAllocations[channelId] = adjustedBandwidth;

      const slots = Math.floor((adjustedBandwidth / snapshot.availableBandwidth) * snapshot.totalTimeSlots);
      solution.scheduleSlots[channelId] = Math.max(1, slots);
    });

    return solution;
  }

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

    // Update arrival rate with exponential smoothing
    const alpha = 0.3;
    channel.arrivalRate = alpha * (channel.arrivalRate + 1) + (1 - alpha) * channel.arrivalRate;

    this.observations.push({
      type: 'signal_enqueued',
      channelId,
      timestamp: Date.now()
    });

    this.emit('signal_enqueued', { channelId, signal });

    if (signal.priority === 'CRITICAL') {
      this.reformulateAndSolve();
    }

    return { success: true, channelId };
  }

  processSignals() {
    this.channels.forEach((channel, channelId) => {
      const signalsToProcess = Math.min(
        channel.queue.length,
        Math.floor(channel.bandwidth / 10)
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

      // Update utilization based on queue length
      channel.utilization = Math.min(1.0, channel.queue.length / 100);

      // Exponential decay of arrival rate when no signals
      if (channel.queue.length === 0) {
        channel.arrivalRate *= 0.95;
      }
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
      adaptiveParams: this.adaptiveParams,
      scenario: this.scenario,
      history: this.history,
      cycleCount: this.cycleCount,
      state: this.state
    };
  }

  start() {
    this.startTime = Date.now();
    this.running = true;
    // Start with steady scenario by default
    if (!this.scenario.mode || this.scenario.mode === 'steady') {
      this.setScenario('steady', 1.0);
    }
  }

  stop() {
    this.running = false;
  }
}
