# Tonal Flow Meter: Higher-Resolution Parallel Subagent Control

The TonalFlowMeter makes the parallel-subagent extension explicit instead of allowing
stochastic sampling, prompt shifts, response compression, or flow control to become
hidden assumptions.

## Governing contract

The meter treats the optimization problem as jointly constrained:

- Objective J: supplied by OptimizationContract.objective.
- Feasible set F: supplied by OptimizationContract.feasibleSet.
- Hard constraints: supplied by OptimizationContract.hardConstraints.
- Finite budgets: computation budget, response budget, board/concurrency limit, and deadline.
- Certification: a feasible candidate is not globally optimal without a universal upper bound.

The implementation therefore reports GLOBAL_OPTIMALITY_PROVED only when a caller supplies
a universal upper bound that is no greater than the candidate objective. Otherwise it reports:

> Best certified candidate; global optimality not proved.

## Resolution model

### 1. Prompt distribution

PromptDistributionContract stores the base prompt, parameterization theta, authorized
shift authority, valid shift window, and reversibility. shiftPrompt records:

prompt shift -> response distribution -> class partition -> downstream evidence/action

The meter records the causal hypothesis and reversal condition. It does not infer causal mediation
from agreement or throughput.

### 2. Parallel subagents

StreamContract.subagentCount and ResponseRecord.authority separate capability from authority.
Parallel samples are perspectives, not votes. The meter never increases authority because more
subagents agree.

### 3. Minimum sufficient equivalence classes

Responses are compressed using a caller-supplied equivalence relation. A response is mergeable only
when the relation says that the decision-relevant distinction set is preserved. A class has an
explicit intra-class erosion budget. If measured drift exceeds that budget, the class is marked
for splitting and re-testing.

An unexamined or discarded response is not evidence for No distinction.

### 4. Continuous stream and flow control

Every response has a stable ID, subagent provenance, authority, prompt parameterization, timestamp,
payload, examination state, and erosion vector.

Flow actions are explicit:

- ADMIT
- PAUSE
- BACKPRESSURE
- MERGE
- DISCARD
- ESCALATE
- PROMPT_SHIFT

Each action carries a causal justification and is retained in the event stream.

### 5. Latency, board, and stopping

initialLatency, maxBoard, deadline, streamRatePerSecond, and maxResponses are explicit
contract dimensions. snapshot exposes deadline status, stopping-rule state, examined coverage,
and coverage gaps.

If the deadline or response budget is reached before the partition is sufficient, the snapshot is
an interim assessment rather than a forced complete answer.

### 6. Erosion accounting

Every response carries:

E = (Ec, Er, Ee, Eu, Ea, Ed, Eq)

for content, relevance, evidence, uncertainty, authority, distribution, and decision erosion,
plus directional and decision-changing flags.

A decision-changing merge is rejected. Drift beyond the class budget triggers escalation.

### 7. Equilibrium

The meter intentionally does not collapse the stream into a consensus point. Downstream policy code
should derive an equilibrium region from the class partition, for example a robust-decision,
acceptable-loss, minimax, fixed-point, or constraint-intersection region.

### 8. Certification

Search, optimization, and certification remain separate. The meter can record a candidate and a
candidate objective, but global optimality requires the universal statement:

forall x in F: J(x) <= J(x*)

A stochastic response stream may generate candidates; it must not change the objective, feasible
set, hard constraints, budget, or optimality criterion.

## Integration guidance

Instantiate one meter at the station/orchestration boundary, not one meter per subagent. Pass
the same meter to response collectors so that board limits, authority, prompt interventions, and
erosion accounting remain global.

The existing station layer can continue to own agent assignment and task lifecycle. The meter owns
the higher-resolution evidence/control state crossing those stations.

This separation keeps station mechanics and optimization/certification semantics distinct.
