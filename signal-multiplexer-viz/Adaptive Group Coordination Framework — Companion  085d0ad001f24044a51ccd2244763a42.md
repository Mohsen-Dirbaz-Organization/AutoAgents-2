# Adaptive Group Coordination Framework — Companion Documentation

**Companion file to Adaptive Signal Multiplexer: Multi-agent coordination with dynamic problem formulation**

## Purpose

This document provides the **broader multi-agent coordination context** in which the [Adaptive Signal Multiplexer with Dynamic Problem Formulation](https://www.notion.so/Adaptive-Signal-Multiplexer-with-Dynamic-Problem-Formulation-7615112ab78b4713aa2f5b4703746ba4?pvs=21) operates. While the multiplexer handles low-level resource allocation and signal transmission, this framework manages high-level agent group formation, task allocation, and consensus building.

---

## Architectural Relationship

```
┌─────────────────────────────────────────────────────────────┐
│          Multi-Agent Coordination Layer                      │
│   (GroupCoordinationFramework - THIS FILE)                  │
│                                                               │
│  • Forms agent groups based on capabilities & trust         │
│  • Allocates tasks using optimization strategies            │
│  • Achieves consensus through weighted protocols            │
│  • Monitors performance and adapts policies                 │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       │ Coordination signals,
                       │ resource requests,
                       │ performance feedback
                       │
┌──────────────────────▼──────────────────────────────────────┐
│       Signal Multiplexing & Resource Allocation Layer       │
│   (AdaptiveSignalMultiplexer)                               │
│                                                               │
│  • Formulates bandwidth allocation problems                 │
│  • Detects optimization problem structure                   │
│  • Solves with appropriate mathematical methods             │
│  • Adapts to changing channel conditions                    │
└─────────────────────────────────────────────────────────────┘
```

**Key Integration Points:**

- When agents are formed into groups, communication channels must be allocated (via multiplexer)
- Task execution generates signal traffic that must be prioritized and routed
- Consensus protocols require reliable, low-latency communication
- Performance metrics from multiplexer inform coordination decisions

---

## Modernized Implementation

### Core Philosophy

Like the signal multiplexer, this framework treats coordination as a **continuous optimization and adaptation problem** rather than fixed rule-following:

- **Group formation**: Optimization problem balancing capability coverage, trust levels, and resource constraints
- **Task allocation**: Multi-objective optimization considering agent skills, current load, and historical performance
- **Consensus achievement**: Game-theoretic problem finding Nash equilibrium or weighted majority
- **Performance monitoring**: Adaptive controller adjusting strategies based on observed outcomes

---

```java
package [com.IDE.plugin.ai](http://com.IDE.plugin.ai).multiagent.coordination;

import [com.IDE.plugin.ai](http://com.IDE.plugin.ai).multiagent.model.*;
import [com.IDE.plugin.ai](http://com.IDE.plugin.ai).multiagent.agent.Agent;
import [com.IDE.plugin.ai.multiagent.trust](http://com.IDE.plugin.ai.multiagent.trust).TrustManager;
import [com.IDE.plugin.ai](http://com.IDE.plugin.ai).multiagent.memory.MemoryManager;
import [com.IDE.plugin.ai](http://com.IDE.plugin.ai).multiagent.mechanical.persistence.AdaptiveSignalMultiplexer;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import [java.util.stream](http://java.util.stream).Collectors;

/**
 * Adaptive Group Coordination Framework with Dynamic Problem Formulation
 * 
 * Core Philosophy: Multi-agent coordination is not pattern-matching but real-time
 * optimization that must formulate and solve problems continuously.
 * 
 * Architecture:
 * - Group Formation Optimizer: Formulates group composition as optimization problem
 * - Task Allocation Solver: Constructs and solves task assignment problems
 * - Consensus Engine: Models agreement as game-theoretic or voting optimization
 * - Adaptive Strategy Manager: Updates policies based on performance observations
 * - Integration with Signal Multiplexer: Coordinates communication resources
 */
public class AdaptiveGroupCoordinationFramework {

    // Core reasoning components
    private final GroupFormationOptimizer groupFormationOptimizer;
    private final TaskAllocationSolver taskAllocationSolver;
    private final ConsensusEngine consensusEngine;
    private final AdaptiveStrategyManager strategyManager;
    
    // Integration with lower-level systems
    private final AdaptiveSignalMultiplexer signalMultiplexer;
    private final TrustManager trustManager;
    private final MemoryManager memoryManager;
    
    // Active coordination state
    private final Map<String, AgentGroup> activeGroups;
    private final Map<String, CoordinationTask> activeTasks;
    private final Map<String, GroupPerformanceModel> performanceModels;
    
    // Execution infrastructure
    private final ExecutorService coordinationExecutor;
    private final ScheduledExecutorService adaptationExecutor;
    
    // Configuration
    private final CoordinationConfig config;
    
    private static final long STRATEGY_ADAPTATION_INTERVAL_MS = 5000;
    private static final long PERFORMANCE_EVALUATION_INTERVAL_MS = 10000;

    public AdaptiveGroupCoordinationFramework(
            AdaptiveSignalMultiplexer signalMultiplexer,
            TrustManager trustManager,
            MemoryManager memoryManager,
            CoordinationConfig config
    ) {
        this.signalMultiplexer = signalMultiplexer;
        this.trustManager = trustManager;
        this.memoryManager = memoryManager;
        this.config = config;
        
        // Initialize reasoning components
        this.groupFormationOptimizer = new GroupFormationOptimizer(trustManager);
        this.taskAllocationSolver = new TaskAllocationSolver();
        this.consensusEngine = new ConsensusEngine(trustManager);
        this.strategyManager = new AdaptiveStrategyManager();
        
        // Initialize state
        this.activeGroups = new ConcurrentHashMap<>();
        this.activeTasks = new ConcurrentHashMap<>();
        this.performanceModels = new ConcurrentHashMap<>();
        
        // Initialize executors
        this.coordinationExecutor = Executors.newCachedThreadPool(
            r -> new Thread(r, "Coordination-" + Thread.currentThread().getId())
        );
        
        this.adaptationExecutor = Executors.newScheduledThreadPool(2);
        
        initialize();
    }

    private void initialize() {
        // Schedule continuous strategy adaptation
        adaptationExecutor.scheduleAtFixedRate(
            this::adaptStrategies,
            0,
            STRATEGY_ADAPTATION_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );
        
        // Schedule performance evaluation
        adaptationExecutor.scheduleAtFixedRate(
            this::evaluatePerformance,
            0,
            PERFORMANCE_EVALUATION_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );
    }

    /**
     * Forms optimal agent group by solving multi-objective optimization problem.
     * 
     * Formulates and solves:
     * - Decision variables: group membership (binary), role assignments
     * - Objectives: maximize capability coverage, maximize trust, minimize size
     * - Constraints: minimum capabilities, trust thresholds, capacity limits
     */
    public CompletableFuture<AgentGroup> formOptimalGroup(GroupFormationRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. OBSERVE: Gather current agent states and capabilities
                CoordinationSnapshot snapshot = captureCoordinationSnapshot();
                
                // 2. FORMULATE: Construct group formation optimization problem
                GroupFormationProblem problem = groupFormationOptimizer.formulateProblem(
                    snapshot,
                    request
                );
                
                // 3. DETECT STRUCTURE: Identify problem characteristics
                ProblemStructure structure = detectProblemStructure(problem);
                
                // 4. SOLVE: Execute appropriate optimization method
                GroupFormationSolution solution = groupFormationOptimizer.solve(
                    problem,
                    structure
                );
                
                // 5. CONSTRUCT GROUP: Build agent group from solution
                AgentGroup group = constructGroup(solution, request);
                
                // 6. ALLOCATE RESOURCES: Request communication channels from multiplexer
                allocateCommunicationChannels(group);
                
                // 7. INITIALIZE: Set up group coordination infrastructure
                initializeGroupCoordination(group);
                
                // 8. RECORD: Store formation decision for learning
                recordGroupFormation(group, solution);
                
                activeGroups.put(group.getId(), group);
                
                return group;
                
            } catch (Exception e) {
                throw new CoordinationException("Group formation failed", e);
            }
        }, coordinationExecutor);
    }

    /**
     * Allocates task to agents by solving assignment optimization problem.
     * 
     * Formulates and solves:
     * - Decision variables: task-to-agent assignments, execution schedules
     * - Objectives: minimize completion time, balance load, maximize reliability
     * - Constraints: agent capabilities, capacity limits, precedence relations
     */
    public CompletableFuture<TaskAllocation> allocateTaskOptimally(
            String groupId,
            Task task
    ) {
        AgentGroup group = activeGroups.get(groupId);
        if (group == null) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("Group not found: " + groupId)
            );
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. DECOMPOSE: Break task into subtasks if needed
                TaskDecomposition decomposition = decomposeTask(task);
                
                // 2. FORMULATE: Construct task allocation optimization problem
                TaskAllocationProblem problem = taskAllocationSolver.formulateProblem(
                    decomposition,
                    group,
                    performanceModels
                );
                
                // 3. SOLVE: Find optimal assignment
                TaskAllocationSolution solution = taskAllocationSolver.solve(problem);
                
                // 4. VERIFY: Check feasibility and constraints
                verifyAllocationFeasibility(solution, group);
                
                // 5. ALLOCATE: Assign communication bandwidth for task
                allocateTaskBandwidth(solution, group);
                
                // 6. DISTRIBUTE: Send subtasks to assigned agents
                TaskAllocation allocation = distributeSubtasks(solution, group);
                
                // 7. MONITOR: Start tracking execution
                CoordinationTask coordTask = new CoordinationTask(task, group, allocation);
                activeTasks.put(task.getId(), coordTask);
                startTaskMonitoring(coordTask);
                
                return allocation;
                
            } catch (Exception e) {
                throw new CoordinationException("Task allocation failed", e);
            }
        }, coordinationExecutor);
    }

    /**
     * Achieves consensus by solving weighted voting or game-theoretic problem.
     * 
     * Formulates and solves:
     * - Decision space: possible consensus outcomes
     * - Agent preferences: utility functions over outcomes
     * - Mechanism: weighted voting, Nash equilibrium, or multi-round protocol
     * - Constraints: trust-based voting weights, quorum requirements
     */
    public CompletableFuture<ConsensusResult> achieveConsensus(
            String groupId,
            ConsensusRequest request
    ) {
        AgentGroup group = activeGroups.get(groupId);
        if (group == null) {
            return CompletableFuture.failedFuture(
                new IllegalArgumentException("Group not found: " + groupId)
            );
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. FORMULATE: Construct consensus problem
                ConsensusProblem problem = consensusEngine.formulateProblem(
                    group,
                    request,
                    trustManager
                );
                
                // 2. DETECT MECHANISM: Choose voting protocol based on problem type
                ConsensusProtocol protocol = consensusEngine.selectProtocol(problem);
                
                // 3. COLLECT VOTES: Gather agent preferences with deadlines
                Map<String, AgentVote> votes = collectVotes(group, request);
                
                // 4. SOLVE: Compute consensus outcome
                ConsensusResult result = consensusEngine.solve(problem, protocol, votes);
                
                // 5. VERIFY: Check consensus validity
                verifyConsensusValidity(result, group);
                
                // 6. UPDATE TRUST: Adjust trust based on voting patterns
                updateTrustFromConsensus(group, result);
                
                // 7. RECORD: Store for learning
                recordConsensus(group, request, result);
                
                return result;
                
            } catch (Exception e) {
                throw new CoordinationException("Consensus failed", e);
            }
        }, coordinationExecutor);
    }

    /**
     * Adapts coordination strategies based on performance observations.
     */
    private void adaptStrategies() {
        try {
            // Collect recent performance data
            List<CoordinationOutcome> recentOutcomes = collectRecentOutcomes();
            
            // Update performance models
            updatePerformanceModels(recentOutcomes);
            
            // Detect if strategy change is needed
            StrategyChange change = strategyManager.detectStrategyChange(
                recentOutcomes,
                performanceModels
            );
            
            if (change.isRequired()) {
                // Apply strategy change
                applyStrategyChange(change);
                
                // Record adaptation event
                recordStrategyAdaptation(change);
            }
            
        } catch (Exception e) {
            // Log error
        }
    }

    /**
     * Evaluates overall coordination performance.
     */
    private void evaluatePerformance() {
        try {
            // Evaluate each active group
            for (AgentGroup group : activeGroups.values()) {
                GroupPerformanceModel model = performanceModels.get(group.getId());
                if (model != null) {
                    model.update(group);
                    
                    // Check if group is underperforming
                    if (model.isUnderperforming(config.getPerformanceThreshold())) {
                        // Consider group restructuring
                        considerGroupRestructuring(group, model);
                    }
                }
            }
            
        } catch (Exception e) {
            // Log error
        }
    }

    /**
     * Allocates communication channels for newly formed group.
     */
    private void allocateCommunicationChannels(AgentGroup group) {
        // For each pair of agents in group, request channel from multiplexer
        for (String agent1 : group.getMembers()) {
            for (String agent2 : group.getMembers()) {
                if (!agent1.equals(agent2)) {
                    // Request channel with priority based on group trust level
                    signalMultiplexer.sendSignal(
                        group.getId() + "_" + agent1 + "_" + agent2,
                        createChannelAllocationSignal(agent1, agent2, group)
                    );
                }
            }
        }
        
        // Request broadcast channel for group-wide communication
        signalMultiplexer.sendSignal(
            group.getId() + "_broadcast",
            createBroadcastChannelSignal(group)
        );
    }

    /**
     * Allocates bandwidth for task execution.
     */
    private void allocateTaskBandwidth(
            TaskAllocationSolution solution,
            AgentGroup group
    ) {
        // Estimate bandwidth requirements from task characteristics
        double requiredBandwidth = estimateBandwidthRequirement(
            solution.getTask(),
            solution.getAssignments().size()
        );
        
        // Request bandwidth allocation from multiplexer
        // Priority based on task urgency
        for (Map.Entry<String, String> assignment : solution.getAssignments().entrySet()) {
            String subtaskId = assignment.getKey();
            String agentId = assignment.getValue();
            
            signalMultiplexer.sendSignal(
                group.getId() + "_task_" + subtaskId,
                createTaskBandwidthSignal(agentId, requiredBandwidth, solution.getTask())
            );
        }
    }

    private CoordinationSnapshot captureCoordinationSnapshot() {
        // Capture current state of all agents, tasks, and resources
        return new CoordinationSnapshot(
            getAllAvailableAgents(),
            getAgentCapabilities(),
            getAgentWorkloads(),
            getTrustMatrix(),
            getResourceAvailability()
        );
    }

    private ProblemStructure detectProblemStructure(GroupFormationProblem problem) {
        // Analyze problem characteristics
        ProblemStructure structure = new ProblemStructure();
        
        // Group formation is typically a mixed-integer problem
        structure.setMixedInteger(true);
        
        // May be multi-objective (capabilities vs trust vs size)
        structure.setMultiObjective(problem.hasMultipleObjectives());
        
        // Trust uncertainty makes it stochastic
        structure.setStochastic(problem.hasTrustUncertainty());
        
        return structure;
    }

    // ===================================================================
    // GROUP FORMATION OPTIMIZER
    // ===================================================================

    private static class GroupFormationOptimizer {
        private final TrustManager trustManager;
        
        GroupFormationOptimizer(TrustManager trustManager) {
            this.trustManager = trustManager;
        }
        
        /**
         * Formulates group formation as optimization problem.
         * 
         * Decision Variables:
         * - x_i \u2208 {0,1}: whether agent i is in group
         * - r_ij: role assignment for agent i in capability j
         * 
         * Objectives:
         * - Maximize capability coverage: \u03a3_j covered(j)
         * - Maximize group trust: \u03a3_i \u03a3_j x_i * x_j * trust(i,j)
         * - Minimize group size: \u03a3_i x_i
         * 
         * Constraints:
         * - Each required capability must be covered
         * - Group trust must exceed threshold
         * - Group size within bounds
         * - Agent availability constraints
         */
        GroupFormationProblem formulateProblem(
                CoordinationSnapshot snapshot,
                GroupFormationRequest request
        ) {
            GroupFormationProblem problem = new GroupFormationProblem();
            
            // Define binary variables for agent membership
            for (String agentId : snapshot.getAvailableAgents()) {
                problem.addBinaryVariable("x_" + agentId);
            }
            
            // Multi-objective function
            MultiObjective objective = new MultiObjective();
            
            // Objective 1: Maximize capability coverage
            for (String capability : request.getRequiredCapabilities()) {
                objective.addObjective(
                    "coverage_" + capability,
                    1.0, // weight
                    vars -> calculateCapabilityCoverage(capability, vars, snapshot)
                );
            }
            
            // Objective 2: Maximize group cohesion (trust-based)
            objective.addObjective(
                "cohesion",
                0.5,
                vars -> calculateGroupCohesion(vars, snapshot)
            );
            
            // Objective 3: Minimize group size (for efficiency)
            objective.addObjective(
                "size",
                -0.3, // negative weight to minimize
                vars -> vars.values().stream().mapToDouble(v -> v).sum()
            );
            
            problem.setObjective(objective);
            
            // Constraint: Each required capability must be covered
            for (String capability : request.getRequiredCapabilities()) {
                problem.addConstraint(
                    "capability_" + capability,
                    vars -> hasCapabilityCoverage(capability, vars, snapshot),
                    true
                );
            }
            
            // Constraint: Minimum group trust
            problem.addConstraint(
                "min_trust",
                vars -> calculateGroupCohesion(vars, snapshot),
                request.getMinTrustLevel()
            );
            
            // Constraint: Group size bounds
            problem.addConstraint(
                "min_size",
                vars -> vars.values().stream().mapToDouble(v -> v).sum(),
                request.getMinGroupSize()
            );
            
            problem.addConstraint(
                "max_size",
                vars -> vars.values().stream().mapToDouble(v -> v).sum(),
                request.getMaxGroupSize(),
                true // less-than-or-equal
            );
            
            return problem;
        }
        
        GroupFormationSolution solve(
                GroupFormationProblem problem,
                ProblemStructure structure
        ) {
            // Use appropriate solver based on structure
            if (structure.isMixedInteger() && structure.isMultiObjective()) {
                // Use multi-objective mixed-integer solver
                return solveMOMILP(problem);
            } else {
                // Use heuristic
                return solveHeuristic(problem);
            }
        }
        
        private GroupFormationSolution solveMOMILP(GroupFormationProblem problem) {
            // Placeholder: Would use NSGA-II, epsilon-constraint, or weighted sum
            return new GroupFormationSolution();
        }
        
        private GroupFormationSolution solveHeuristic(GroupFormationProblem problem) {
            // Greedy heuristic: iteratively add agents with highest marginal value
            return new GroupFormationSolution();
        }
        
        private static double calculateCapabilityCoverage(
                String capability,
                Map<String, Double> membershipVars,
                CoordinationSnapshot snapshot
        ) {
            // Check if any selected agent has the capability
            return membershipVars.entrySet().stream()
                .filter(e -> e.getValue() > 0.5) // Binary variable
                .anyMatch(e -> {
                    String agentId = e.getKey().substring(2); // Remove "x_" prefix
                    return snapshot.hasCapability(agentId, capability);
                }) ? 1.0 : 0.0;
        }
        
        private static double calculateGroupCohesion(
                Map<String, Double> membershipVars,
                CoordinationSnapshot snapshot
        ) {
            // Calculate average pairwise trust among selected agents
            List<String> selectedAgents = membershipVars.entrySet().stream()
                .filter(e -> e.getValue() > 0.5)
                .map(e -> e.getKey().substring(2))
                .collect(Collectors.toList());
            
            if (selectedAgents.size() < 2) return 0.0;
            
            double totalTrust = 0.0;
            int pairCount = 0;
            
            for (int i = 0; i < selectedAgents.size(); i++) {
                for (int j = i + 1; j < selectedAgents.size(); j++) {
                    totalTrust += snapshot.getTrust(selectedAgents.get(i), selectedAgents.get(j));
                    pairCount++;
                }
            }
            
            return pairCount > 0 ? totalTrust / pairCount : 0.0;
        }
        
        private static boolean hasCapabilityCoverage(
                String capability,
                Map<String, Double> membershipVars,
                CoordinationSnapshot snapshot
        ) {
            return calculateCapabilityCoverage(capability, membershipVars, snapshot) > 0.5;
        }
    }

    // ===================================================================
    // TASK ALLOCATION SOLVER
    // ===================================================================

    private static class TaskAllocationSolver {
        
        /**
         * Formulates task allocation as optimization problem.
         * 
         * Decision Variables:
         * - y_ij \u2208 {0,1}: whether subtask i is assigned to agent j
         * - t_i: start time for subtask i
         * 
         * Objectives:
         * - Minimize makespan: max_i (t_i + duration_i)
         * - Balance load: minimize variance of agent workloads
         * - Maximize reliability: \u03a3_ij y_ij * competence_ij
         * 
         * Constraints:
         * - Each subtask assigned to exactly one agent
         * - Agent capacity constraints
         * - Precedence constraints between subtasks
         * - Agent capability requirements
         */
        TaskAllocationProblem formulateProblem(
                TaskDecomposition decomposition,
                AgentGroup group,
                Map<String, GroupPerformanceModel> performanceModels
        ) {
            TaskAllocationProblem problem = new TaskAllocationProblem();
            
            List<Subtask> subtasks = decomposition.getSubtasks();
            List<String> agents = group.getMembers();
            
            // Define assignment variables y_ij
            for (Subtask subtask : subtasks) {
                for (String agentId : agents) {
                    problem.addBinaryVariable(
                        "y_" + subtask.getId() + "_" + agentId
                    );
                }
            }
            
            // Define start time variables t_i
            for (Subtask subtask : subtasks) {
                problem.addContinuousVariable(
                    "t_" + subtask.getId(),
                    0,
                    Double.MAX_VALUE
                );
            }
            
            // Multi-objective
            MultiObjective objective = new MultiObjective();
            
            // Minimize makespan
            objective.addObjective(
                "makespan",
                1.0,
                vars -> calculateMakespan(vars, subtasks)
            );
            
            // Balance load
            objective.addObjective(
                "load_balance",
                0.5,
                vars -> -calculateLoadVariance(vars, subtasks, agents)
            );
            
            problem.setObjective(objective);
            
            // Constraint: Each subtask assigned to exactly one agent
            for (Subtask subtask : subtasks) {
                List<String> assignmentVars = new ArrayList<>();
                for (String agentId : agents) {
                    assignmentVars.add("y_" + subtask.getId() + "_" + agentId);
                }
                problem.addConstraint(
                    "unique_assignment_" + subtask.getId(),
                    vars -> [assignmentVars.stream](http://assignmentVars.stream)()
                        .mapToDouble(v -> vars.getOrDefault(v, 0.0))
                        .sum(),
                    1.0
                );
            }
            
            // Constraint: Precedence relations
            for (Subtask subtask : subtasks) {
                for (Subtask predecessor : subtask.getPredecessors()) {
                    problem.addConstraint(
                        "precedence_" + predecessor.getId() + "_" + subtask.getId(),
                        vars -> {
                            double t_pred = vars.get("t_" + predecessor.getId());
                            double t_succ = vars.get("t_" + subtask.getId());
                            return t_succ - (t_pred + predecessor.getDuration());
                        },
                        0.0 // t_succ >= t_pred + duration_pred
                    );
                }
            }
            
            return problem;
        }
        
        TaskAllocationSolution solve(TaskAllocationProblem problem) {
            // Use mixed-integer programming solver or heuristic
            return solveGreedyHeuristic(problem);
        }
        
        private TaskAllocationSolution solveGreedyHeuristic(TaskAllocationProblem problem) {
            // Greedy: assign each subtask to most suitable available agent
            return new TaskAllocationSolution();
        }
        
        private static double calculateMakespan(
                Map<String, Double> vars,
                List<Subtask> subtasks
        ) {
            return [subtasks.stream](http://subtasks.stream)()
                .mapToDouble(st -> {
                    double startTime = vars.getOrDefault("t_" + st.getId(), 0.0);
                    return startTime + st.getDuration();
                })
                .max()
                .orElse(0.0);
        }
        
        private static double calculateLoadVariance(
                Map<String, Double> vars,
                List<Subtask> subtasks,
                List<String> agents
        ) {
            // Calculate variance of workload across agents
            Map<String, Double> agentLoads = new HashMap<>();
            
            for (String agent : agents) {
                double load = 0.0;
                for (Subtask subtask : subtasks) {
                    double assigned = vars.getOrDefault(
                        "y_" + subtask.getId() + "_" + agent,
                        0.0
                    );
                    load += assigned * subtask.getDuration();
                }
                agentLoads.put(agent, load);
            }
            
            double mean = agentLoads.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
            
            return agentLoads.values().stream()
                .mapToDouble(load -> Math.pow(load - mean, 2))
                .average()
                .orElse(0.0);
        }
    }

    // ===================================================================
    // CONSENSUS ENGINE
    // ===================================================================

    private static class ConsensusEngine {
        private final TrustManager trustManager;
        
        ConsensusEngine(TrustManager trustManager) {
            this.trustManager = trustManager;
        }
        
        /**
         * Formulates consensus as optimization or game-theoretic problem.
         * 
         * Approaches:
         * - Weighted voting: weights based on trust and expertise
         * - Nash equilibrium: if agents have strategic preferences
         * - Pareto optimality: for multi-criteria decisions
         */
        ConsensusProblem formulateProblem(
                AgentGroup group,
                ConsensusRequest request,
                TrustManager trustManager
        ) {
            ConsensusProblem problem = new ConsensusProblem();
            
            // Define decision space
            problem.setDecisionSpace(request.getOptions());
            
            // Calculate voting weights based on trust
            for (String agentId : group.getMembers()) {
                double weight = calculateVotingWeight(agentId, group, trustManager);
                problem.setAgentWeight(agentId, weight);
            }
            
            // Set consensus rule
            problem.setConsensusRule(request.getConsensusRule());
            
            return problem;
        }
        
        ConsensusProtocol selectProtocol(ConsensusProblem problem) {
            // Select protocol based on problem characteristics
            if (problem.hasSimpleMajority()) {
                return ConsensusProtocol.WEIGHTED_MAJORITY;
            } else if (problem.requiresUnanimity()) {
                return ConsensusProtocol.UNANIMOUS;
            } else {
                return ConsensusProtocol.ITERATIVE_REFINEMENT;
            }
        }
        
        ConsensusResult solve(
                ConsensusProblem problem,
                ConsensusProtocol protocol,
                Map<String, AgentVote> votes
        ) {
            switch (protocol) {
                case WEIGHTED_MAJORITY:
                    return solveWeightedMajority(problem, votes);
                case UNANIMOUS:
                    return solveUnanimous(problem, votes);
                case ITERATIVE_REFINEMENT:
                    return solveIterativeRefinement(problem, votes);
                default:
                    throw new IllegalArgumentException("Unknown protocol");
            }
        }
        
        private ConsensusResult solveWeightedMajority(
                ConsensusProblem problem,
                Map<String, AgentVote> votes
        ) {
            // Calculate weighted vote totals for each option
            Map<Object, Double> optionScores = new HashMap<>();
            
            for (Map.Entry<String, AgentVote> entry : votes.entrySet()) {
                String agentId = entry.getKey();
                AgentVote vote = entry.getValue();
                double weight = problem.getAgentWeight(agentId);
                
                Object choice = vote.getChoice();
                optionScores.merge(choice, weight, Double::sum);
            }
            
            // Find option with highest score
            Object consensus = optionScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
            
            double confidence = optionScores.get(consensus) / 
                optionScores.values().stream().mapToDouble(Double::doubleValue).sum();
            
            return new ConsensusResult(consensus, confidence, votes);
        }
        
        private ConsensusResult solveUnanimous(
                ConsensusProblem problem,
                Map<String, AgentVote> votes
        ) {
            // Check if all votes agree
            Set<Object> uniqueChoices = votes.values().stream()
                .map(AgentVote::getChoice)
                .collect(Collectors.toSet());
            
            if (uniqueChoices.size() == 1) {
                return new ConsensusResult(
                    uniqueChoices.iterator().next(),
                    1.0,
                    votes
                );
            } else {
                return new ConsensusResult(null, 0.0, votes); // No consensus
            }
        }
        
        private ConsensusResult solveIterativeRefinement(
                ConsensusProblem problem,
                Map<String, AgentVote> votes
        ) {
            // Iterative protocol: agents can revise votes based on others
            // Placeholder for multi-round consensus
            return solveWeightedMajority(problem, votes);
        }
        
        private static double calculateVotingWeight(
                String agentId,
                AgentGroup group,
                TrustManager trustManager
        ) {
            // Weight based on average trust from other group members
            double totalTrust = 0.0;
            int count = 0;
            
            for (String otherId : group.getMembers()) {
                if (!agentId.equals(otherId)) {
                    totalTrust += trustManager.getTrustScore(agentId, otherId);
                    count++;
                }
            }
            
            return count > 0 ? totalTrust / count : 0.5; // Default to 0.5
        }
    }

    // ===================================================================
    // ADAPTIVE STRATEGY MANAGER
    // ===================================================================

    private static class AdaptiveStrategyManager {
        private final Map<String, StrategyPerformance> strategyPerformance;
        
        AdaptiveStrategyManager() {
            this.strategyPerformance = new ConcurrentHashMap<>();
        }
        
        StrategyChange detectStrategyChange(
                List<CoordinationOutcome> recentOutcomes,
                Map<String, GroupPerformanceModel> performanceModels
        ) {
            // Analyze recent performance
            double successRate = calculateSuccessRate(recentOutcomes);
            double avgCompletionTime = calculateAvgCompletionTime(recentOutcomes);
            
            // Compare with historical baseline
            double baselineSuccessRate = 0.85; // From config or history
            double baselineCompletionTime = 1000.0; // ms
            
            // Detect regime change
            if (successRate < baselineSuccessRate * 0.8) {
                return new StrategyChange(
                    true,
                    StrategyChangeType.INCREASE_REDUNDANCY,
                    "Success rate dropped significantly"
                );
            }
            
            if (avgCompletionTime > baselineCompletionTime * 1.5) {
                return new StrategyChange(
                    true,
                    StrategyChangeType.REDUCE_GROUP_SIZE,
                    "Completion time increased"
                );
            }
            
            return new StrategyChange(false, null, null);
        }
        
        private double calculateSuccessRate(List<CoordinationOutcome> outcomes) {
            if (outcomes.isEmpty()) return 1.0;
            long successful = [outcomes.stream](http://outcomes.stream)().filter(CoordinationOutcome::isSuccess).count();
            return (double) successful / outcomes.size();
        }
        
        private double calculateAvgCompletionTime(List<CoordinationOutcome> outcomes) {
            return [outcomes.stream](http://outcomes.stream)()
                .mapToDouble(CoordinationOutcome::getCompletionTimeMs)
                .average()
                .orElse(0.0);
        }
    }

    // ===================================================================
    // SUPPORTING CLASSES
    // ===================================================================

    private static class CoordinationSnapshot {
        private final List<String> availableAgents;
        private final Map<String, Set<String>> agentCapabilities;
        private final Map<String, Double> agentWorkloads;
        private final Map<String, Map<String, Double>> trustMatrix;
        private final Map<String, Double> resourceAvailability;
        
        CoordinationSnapshot(
                List<String> availableAgents,
                Map<String, Set<String>> agentCapabilities,
                Map<String, Double> agentWorkloads,
                Map<String, Map<String, Double>> trustMatrix,
                Map<String, Double> resourceAvailability
        ) {
            this.availableAgents = availableAgents;
            this.agentCapabilities = agentCapabilities;
            this.agentWorkloads = agentWorkloads;
            this.trustMatrix = trustMatrix;
            this.resourceAvailability = resourceAvailability;
        }
        
        List<String> getAvailableAgents() { return availableAgents; }
        
        boolean hasCapability(String agentId, String capability) {
            Set<String> capabilities = agentCapabilities.get(agentId);
            return capabilities != null && capabilities.contains(capability);
        }
        
        double getTrust(String agent1, String agent2) {
            Map<String, Double> agent1Trust = trustMatrix.get(agent1);
            return agent1Trust != null ? agent1Trust.getOrDefault(agent2, 0.5) : 0.5;
        }
    }

    private AgentGroup constructGroup(
            GroupFormationSolution solution,
            GroupFormationRequest request
    ) {
        List<String> members = solution.getSelectedAgents();
        return new AgentGroup(
            UUID.randomUUID().toString(),
            members,
            request.getTaskDescription()
        );
    }

    private void initializeGroupCoordination(AgentGroup group) {
        // Initialize performance model
        GroupPerformanceModel model = new GroupPerformanceModel(group.getId());
        performanceModels.put(group.getId(), model);
    }

    private void recordGroupFormation(AgentGroup group, GroupFormationSolution solution) {
        // Record in memory manager for learning
        [memoryManager.store](http://memoryManager.store)(new MemoryEntry(
            UUID.randomUUID().toString(),
            "GroupCoordination",
            MemoryType.SYSTEM_EVENT,
            "Formed group: " + group.getId(),
            Map.of(
                "groupId", group.getId(),
                "members", group.getMembers(),
                "solution_quality", solution.getObjectiveValue()
            ),
            [LocalDateTime.now](http://LocalDateTime.now)()
        ));
    }

    // Placeholder helper methods
    private List<String> getAllAvailableAgents() { return new ArrayList<>(); }
    private Map<String, Set<String>> getAgentCapabilities() { return new HashMap<>(); }
    private Map<String, Double> getAgentWorkloads() { return new HashMap<>(); }
    private Map<String, Map<String, Double>> getTrustMatrix() { return new HashMap<>(); }
    private Map<String, Double> getResourceAvailability() { return new HashMap<>(); }
    private TaskDecomposition decomposeTask(Task task) { return new TaskDecomposition(task); }
    private void verifyAllocationFeasibility(TaskAllocationSolution solution, AgentGroup group) {}
    private TaskAllocation distributeSubtasks(TaskAllocationSolution solution, AgentGroup group) {
        return new TaskAllocation();
    }
    private void startTaskMonitoring(CoordinationTask task) {}
    private Map<String, AgentVote> collectVotes(AgentGroup group, ConsensusRequest request) {
        return new HashMap<>();
    }
    private void verifyConsensusValidity(ConsensusResult result, AgentGroup group) {}
    private void updateTrustFromConsensus(AgentGroup group, ConsensusResult result) {}
    private void recordConsensus(AgentGroup group, ConsensusRequest request, ConsensusResult result) {}
    private List<CoordinationOutcome> collectRecentOutcomes() { return new ArrayList<>(); }
    private void updatePerformanceModels(List<CoordinationOutcome> outcomes) {}
    private void applyStrategyChange(StrategyChange change) {}
    private void recordStrategyAdaptation(StrategyChange change) {}
    private void considerGroupRestructuring(AgentGroup group, GroupPerformanceModel model) {}
    private MechanicalSignal createChannelAllocationSignal(String agent1, String agent2, AgentGroup group) {
        return new MechanicalSignal();
    }
    private MechanicalSignal createBroadcastChannelSignal(AgentGroup group) {
        return new MechanicalSignal();
    }
    private double estimateBandwidthRequirement(Task task, int agentCount) { return 100.0; }
    private MechanicalSignal createTaskBandwidthSignal(String agentId, double bandwidth, Task task) {
        return new MechanicalSignal();
    }

    // Placeholder classes
    private static class GroupFormationRequest {
        Set<String> getRequiredCapabilities() { return new HashSet<>(); }
        double getMinTrustLevel() { return 0.5; }
        int getMinGroupSize() { return 2; }
        int getMaxGroupSize() { return 10; }
        String getTaskDescription() { return ""; }
    }
    
    private static class GroupFormationProblem {
        void addBinaryVariable(String name) {}
        void setObjective(MultiObjective obj) {}
        void addConstraint(String name, java.util.function.Function<Map<String, Double>, Double> func, double rhs) {}
        void addConstraint(String name, java.util.function.Function<Map<String, Double>, Double> func, double rhs, boolean leq) {}
        void addConstraint(String name, java.util.function.Function<Map<String, Double>, Boolean> func, boolean expected) {}
        boolean hasMultipleObjectives() { return true; }
        boolean hasTrustUncertainty() { return true; }
    }
    
    private static class GroupFormationSolution {
        List<String> getSelectedAgents() { return new ArrayList<>(); }
        double getObjectiveValue() { return 0.0; }
    }
    
    private static class TaskDecomposition {
        private final Task task;
        TaskDecomposition(Task task) { this.task = task; }
        List<Subtask> getSubtasks() { return new ArrayList<>(); }
    }
    
    private static class Subtask {
        String getId() { return ""; }
        double getDuration() { return 0.0; }
        List<Subtask> getPredecessors() { return new ArrayList<>(); }
    }
    
    private static class TaskAllocationProblem {
        void addBinaryVariable(String name) {}
        void addContinuousVariable(String name, double lb, double ub) {}
        void setObjective(MultiObjective obj) {}
        void addConstraint(String name, java.util.function.Function<Map<String, Double>, Double> func, double rhs) {}
    }
    
    private static class TaskAllocationSolution {
        Map<String, String> getAssignments() { return new HashMap<>(); }
        Task getTask() { return new Task(); }
    }
    
    private static class TaskAllocation {}
    
    private static class ConsensusProblem {
        void setDecisionSpace(List<Object> options) {}
        void setAgentWeight(String agentId, double weight) {}
        void setConsensusRule(String rule) {}
        boolean hasSimpleMajority() { return true; }
        boolean requiresUnanimity() { return false; }
        double getAgentWeight(String agentId) { return 1.0; }
    }
    
    private enum ConsensusProtocol {
        WEIGHTED_MAJORITY,
        UNANIMOUS,
        ITERATIVE_REFINEMENT
    }
    
    private static class ConsensusResult {
        private final Object decision;
        private final double confidence;
        private final Map<String, AgentVote> votes;
        
        ConsensusResult(Object decision, double confidence, Map<String, AgentVote> votes) {
            this.decision = decision;
            this.confidence = confidence;
            this.votes = votes;
        }
        
        Object getDecision() { return decision; }
        Map<String, AgentVote> getVotes() { return votes; }
    }
    
    private static class AgentVote {
        Object getChoice() { return null; }
    }
    
    private static class MultiObjective {
        void addObjective(String name, double weight, java.util.function.Function<Map<String, Double>, Double> func) {}
    }
    
    private static class ProblemStructure {
        private boolean mixedInteger;
        private boolean multiObjective;
        private boolean stochastic;
        
        void setMixedInteger(boolean value) { this.mixedInteger = value; }
        void setMultiObjective(boolean value) { this.multiObjective = value; }
        void setStochastic(boolean value) { this.stochastic = value; }
        boolean isMixedInteger() { return mixedInteger; }
        boolean isMultiObjective() { return multiObjective; }
    }
    
    private static class AgentGroup {
        private final String id;
        private final List<String> members;
        private final String purpose;
        private LocalDateTime lastActivityTime;
        
        AgentGroup(String id, List<String> members, String purpose) {
            [this.id](http://this.id) = id;
            this.members = members;
            this.purpose = purpose;
            this.lastActivityTime = [LocalDateTime.now](http://LocalDateTime.now)();
        }
        
        String getId() { return id; }
        List<String> getMembers() { return members; }
        LocalDateTime getLastActivityTime() { return lastActivityTime; }
    }
    
    private static class CoordinationTask {
        private final Task task;
        private final AgentGroup group;
        private final TaskAllocation allocation;
        
        CoordinationTask(Task task, AgentGroup group, TaskAllocation allocation) {
            this.task = task;
            [this.group](http://this.group) = group;
            this.allocation = allocation;
        }
        
        Task getOriginalTask() { return task; }
        AgentGroup getGroup() { return group; }
    }
    
    private static class Task {
        String getId() { return ""; }
        String getType() { return ""; }
    }
    
    private static class ConsensusRequest {
        List<Object> getOptions() { return new ArrayList<>(); }
        String getConsensusRule() { return "MAJORITY"; }
    }
    
    private static class GroupPerformanceModel {
        private final String groupId;
        
        GroupPerformanceModel(String groupId) {
            this.groupId = groupId;
        }
        
        void update(AgentGroup group) {}
        boolean isUnderperforming(double threshold) { return false; }
    }
    
    private static class CoordinationOutcome {
        boolean isSuccess() { return true; }
        double getCompletionTimeMs() { return 0.0; }
    }
    
    private static class StrategyChange {
        private final boolean required;
        private final StrategyChangeType type;
        private final String reason;
        
        StrategyChange(boolean required, StrategyChangeType type, String reason) {
            this.required = required;
            this.type = type;
            this.reason = reason;
        }
        
        boolean isRequired() { return required; }
    }
    
    private enum StrategyChangeType {
        INCREASE_REDUNDANCY,
        REDUCE_GROUP_SIZE
    }
    
    private static class CoordinationConfig {
        int getMinGroupSize() { return 2; }
        int getMaxGroupSize() { return 10; }
        double getMinGroupTrustLevel() { return 0.5; }
        long getMaxTaskDuration() { return 60000; }
        int getMaintenanceInterval() { return 5; }
        int getGroupInactivityTimeout() { return 30; }
        int getPreparationTimeout() { return 30; }
        int getProgressUpdateInterval() { return 5; }
        double getPerformanceThreshold() { return 0.7; }
    }
    
    private static class MechanicalSignal {}
    private static class MemoryEntry {
        MemoryEntry(String id, String category, MemoryType type, String content, Map<String, Object> metadata, LocalDateTime timestamp) {}
    }
    private enum MemoryType { SYSTEM_EVENT, ERROR }
    private enum AgentState { IDLE, EXECUTING }
    
    private static class CoordinationException extends RuntimeException {
        CoordinationException(String message) { super(message); }
        CoordinationException(String message, Throwable cause) { super(message, cause); }
    }

    /**
     * Shutdown coordination framework gracefully.
     */
    public void shutdown() {
        adaptationExecutor.shutdown();
        coordinationExecutor.shutdown();
        
        try {
            if (!adaptationExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                adaptationExecutor.shutdownNow();
            }
            if (!coordinationExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                coordinationExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            adaptationExecutor.shutdownNow();
            coordinationExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
```

---

## How This Framework Integrates with Signal Multiplexer

### 1. **Communication Channel Allocation**

When a group is formed, the framework requests communication channels from the multiplexer:

```java
allocateCommunicationChannels(group);
// Creates channels between each pair of agents
// Multiplexer formulates optimization problem to allocate bandwidth
```

### 2. **Task Bandwidth Provisioning**

When tasks are allocated, bandwidth requirements are estimated and requested:

```java
allocateTaskBandwidth(solution, group);
// Sends high-priority signals for task-critical communication
// Multiplexer adapts resource allocation dynamically
```

### 3. **Consensus Communication**

Consensus protocols require reliable, low-latency channels:

```java
collectVotes(group, request);
// Multiplexer ensures voting messages have priority
// Physics-informed constraints guarantee message ordering
```

### 4. **Performance Feedback Loop**

Multiplexer performance metrics inform coordination decisions:

- High communication latency → reduce group size or restructure
- Channel failures → adjust trust ratings and re-form groups
- Bandwidth saturation → defer non-critical coordination

---

## Key Architectural Principles Shared with Multiplexer

### Both Systems Embrace:

1. **Dynamic Problem Formulation**: Neither uses fixed rules; both construct optimization problems from current state
2. **Structure Detection**: Both identify problem class and select appropriate mathematical methods
3. **Adaptive Learning**: Both update models based on observations and detect regime changes
4. **Physics-Informed Constraints**: Both respect fundamental laws (conservation, causality, dynamics)
5. **Anytime Solutions**: Both provide feasible solutions quickly and improve over time
6. **Multi-Objective Optimization**: Both balance competing objectives (efficiency vs reliability, speed vs fairness)

---

## For the Lovable Visualization

When visualizing the complete system, show:

- **Two-layer architecture**: Coordination layer (this file) on top, multiplexing layer (previous file) below
- **Interaction flows**: Group formation → channel allocation → task execution → bandwidth reallocation
- **Shared optimization framework**: Both layers use same reasoning approach (formulate → detect → solve → adapt)
- **Feedback loops**: Performance from multiplexer influences coordination decisions; coordination patterns inform multiplexer optimization

This companion file demonstrates that the mathematical reasoning approach extends beyond signal multiplexing to **all levels of the multi-agent system**, from low-level resource allocation to high-level strategic coordination.