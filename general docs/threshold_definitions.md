# Threshold Definitions in AutoAgents

This document provides detailed explanations of all thresholds used in the AutoAgents system, including their purpose, mathematical foundations (where applicable), and practical implications.

## 1. Trust and Authorization System Thresholds

### 1.1 ThresholdSignature Thresholds

#### `threshold` (Constructor Parameter)
**Purpose**: Defines the minimum number of shares (k) required to reconstruct a secret in a k-of-n threshold signature scheme.

**Mathematical Explanation**: In Shamir's Secret Sharing, a secret is divided into n shares such that any k shares can reconstruct the secret, but k-1 shares reveal no information. This is based on polynomial interpolation where a polynomial of degree k-1 is uniquely determined by k points.

**Practical Implication**: Higher threshold values increase security by requiring more participants to agree, but reduce availability as more participants must be online for operations to proceed.

#### `KEY_SIZE = 2048`
**Purpose**: Defines the bit length of the RSA keys used in the threshold signature scheme.

**Mathematical Explanation**: RSA security is based on the difficulty of factoring large numbers. A 2048-bit key means the modulus n = p×q is approximately 2^2048, making factorization computationally infeasible with current technology.

**Practical Implication**: Larger key sizes provide stronger security but require more computational resources for key generation and signature operations.

#### `PRIME_CERTAINTY = 100`
**Purpose**: Defines the statistical certainty that generated numbers are prime.

**Mathematical Explanation**: Uses probabilistic primality testing where a certainty of 100 means the probability of a composite number passing the test is 2^-100 (approximately 10^-30).

**Practical Implication**: Higher certainty values increase security but make key generation slower.

#### `SESSION_TIMEOUT_MINUTES = 5`
**Purpose**: Defines how long a signing session remains active before timing out.

**Non-Mathematical Explanation**: Limits the window of opportunity for an attacker to compromise a signing session.

**Practical Implication**: Shorter timeouts improve security but may frustrate users if legitimate operations take longer than the timeout period.

### 1.2 ReputationManager Thresholds

#### `TRUSTED_THRESHOLD = 0.8`
**Purpose**: Defines the minimum reputation score for an agent to be considered trusted.

**Mathematical Explanation**: Represents a probability-like measure where 0.8 means the system is 80% confident in the agent's trustworthiness.

**Practical Implication**: Higher thresholds create a more secure system but may exclude potentially valuable agents.

#### `NEUTRAL_THRESHOLD = 0.5`
**Purpose**: Defines the minimum reputation score for an agent to be considered neutral (neither trusted nor suspicious).

**Non-Mathematical Explanation**: Represents the midpoint between trust and distrust.

**Practical Implication**: Agents with scores between NEUTRAL_THRESHOLD and TRUSTED_THRESHOLD are monitored but allowed to operate normally.

#### `SUSPICIOUS_THRESHOLD = 0.3`
**Purpose**: Defines the minimum reputation score for an agent to be considered suspicious rather than untrusted.

**Non-Mathematical Explanation**: Represents a level of doubt that warrants increased scrutiny but not outright rejection.

**Practical Implication**: Agents with scores between SUSPICIOUS_THRESHOLD and NEUTRAL_THRESHOLD face additional verification steps.

#### `UNTRUSTED_THRESHOLD = 0.1`
**Purpose**: Defines the minimum reputation score for an agent to be considered untrusted rather than blacklisted.

**Non-Mathematical Explanation**: Represents a level of distrust that severely restricts operations but allows for potential rehabilitation.

**Practical Implication**: Agents with scores between UNTRUSTED_THRESHOLD and SUSPICIOUS_THRESHOLD can only perform low-risk operations.

#### `DECAY_RATE = 0.01`
**Purpose**: Defines how quickly reputation scores decay over time without activity.

**Mathematical Explanation**: For each hour of inactivity, the reputation score moves toward the neutral value by DECAY_RATE. If S is the current score and N is the neutral score, the new score after t hours is: S' = S - (S-N) × (1-(1-DECAY_RATE)^t)

**Practical Implication**: Higher decay rates mean reputation must be actively maintained, preventing agents from resting on past achievements.

#### `RECOVERY_RATE = 0.005`
**Purpose**: Defines how quickly reputation scores can recover from negative events.

**Mathematical Explanation**: For each positive action, the reputation score increases by a base amount multiplied by RECOVERY_RATE. This creates a diminishing returns effect where recovery becomes progressively harder.

**Practical Implication**: Lower recovery rates make the system more punitive, requiring more positive actions to recover from negative events.

#### `HISTORY_RETENTION_DAYS = 30`
**Purpose**: Defines how long trust-related events are kept in history before being purged.

**Non-Mathematical Explanation**: Limits the time window considered for reputation calculations.

**Practical Implication**: Longer retention periods provide more data for decision-making but increase storage requirements and computational load.

## 2. Collision Handling Thresholds

### 2.1 EnhancedCollisionHandler Thresholds

#### `minSeverityThreshold = 1`
**Purpose**: Defines the minimum severity level for a collision to be considered worth resolving.

**Non-Mathematical Explanation**: Filters out trivial collisions to focus resources on significant conflicts.

**Practical Implication**: Higher thresholds reduce system overhead but may allow minor conflicts to accumulate.

#### `minTrustThreshold = 0.5`
**Purpose**: Defines the minimum average trust level required for agents involved in a collision to use the collision resolution system.

**Non-Mathematical Explanation**: Prevents untrusted agents from abusing the collision resolution system.

**Practical Implication**: Higher thresholds improve security but may leave legitimate conflicts between lower-trust agents unresolved.

#### `monitoringInterval = 5`
**Purpose**: Defines how often (in minutes) the system checks for and cleans up old collision records.

**Non-Mathematical Explanation**: Controls the frequency of maintenance operations.

**Practical Implication**: Shorter intervals keep the system cleaner but increase computational overhead.

#### `historyRetentionDays = 30`
**Purpose**: Defines how long collision records are kept before being purged.

**Non-Mathematical Explanation**: Limits the historical data used for pattern analysis and optimization.

**Practical Implication**: Longer retention periods provide more data for analysis but increase storage requirements.

## 3. Memory Management Thresholds

### 3.1 SemanticMemory Thresholds

#### `DEFAULT_CAPACITY = 50000`
**Purpose**: Defines the maximum number of concepts that can be stored in semantic memory.

**Non-Mathematical Explanation**: Limits memory usage to prevent resource exhaustion.

**Practical Implication**: Larger capacity allows more concepts to be stored but increases memory usage.

#### `ACTIVATION_DECAY_RATE = 0.1`
**Purpose**: Defines how quickly concept activation levels decay over time.

**Mathematical Explanation**: For each decay cycle, activation is multiplied by (1-ACTIVATION_DECAY_RATE). After n cycles, activation becomes: A' = A × (1-ACTIVATION_DECAY_RATE)^n

**Practical Implication**: Higher decay rates make the system forget unused concepts faster, freeing up capacity for new concepts.

#### `MIN_ACTIVATION_THRESHOLD = 0.01`
**Purpose**: Defines the minimum activation level for a concept to be retained during memory eviction.

**Mathematical Explanation**: When memory reaches capacity, concepts with activation below this threshold are candidates for removal.

**Practical Implication**: Higher thresholds make the system more aggressive in removing inactive concepts.

#### Concept Similarity Threshold (0.8)
**Purpose**: Defines the minimum similarity score for two concepts to be considered similar enough to merge.

**Mathematical Explanation**: Uses Jaccard similarity coefficient: J(A,B) = |A∩B|/|A∪B| where A and B are the sets of keywords associated with each concept.

**Practical Implication**: Higher thresholds require more similarity for merging, resulting in more distinct concepts but potentially redundant information.

#### Relationship Strength Threshold (0.1)
**Purpose**: Defines the minimum strength for a relationship between concepts to be maintained.

**Mathematical Explanation**: Relationships with strength below this threshold are pruned during maintenance.

**Practical Implication**: Higher thresholds create a sparser relationship graph, improving query performance but potentially losing weak but important connections.

### 3.2 EpisodicMemory Thresholds

#### `CONSOLIDATION_THRESHOLD_MS = TimeUnit.HOURS.toMillis(24)`
**Purpose**: Defines how old an episodic memory must be before it's considered for consolidation into semantic memory.

**Non-Mathematical Explanation**: Mimics human memory consolidation during sleep, where recent experiences are integrated into long-term knowledge.

**Practical Implication**: Longer thresholds delay knowledge integration but reduce the risk of incorporating temporary or erroneous information.

### 3.3 WorkingMemory Thresholds

#### `EVICTION_THRESHOLD_MS = TimeUnit.MINUTES.toMillis(5)`
**Purpose**: Defines how long items remain in working memory before being evicted.

**Non-Mathematical Explanation**: Mimics the limited attention span of human working memory.

**Practical Implication**: Shorter thresholds free up working memory faster but may discard information still needed for ongoing tasks.

## 4. Signal Processing Thresholds

### 4.1 MechanicalSignalQueue Thresholds

#### `MAX_QUEUE_SIZE = 10000`
**Purpose**: Defines the maximum number of signals that can be queued.

**Non-Mathematical Explanation**: Prevents unbounded queue growth that could lead to memory exhaustion.

**Practical Implication**: Larger queue sizes allow more signals to be buffered during traffic spikes but increase memory usage.

#### `STARVATION_THRESHOLD_MS = 30000`
**Purpose**: Defines how long a signal can wait in the queue before being considered "starving" and given priority.

**Non-Mathematical Explanation**: Prevents low-priority signals from being indefinitely delayed by higher-priority signals.

**Practical Implication**: Shorter thresholds improve worst-case latency but may reduce overall throughput by interrupting the processing of high-priority signals.

#### Queue Fragmentation Threshold (0.3)
**Purpose**: Defines the level of queue fragmentation that triggers rebalancing.

**Mathematical Explanation**: Fragmentation is calculated as 1 - (items_in_queue / queue_capacity). A threshold of 0.3 means rebalancing occurs when the queue is less than 70% utilized.

**Practical Implication**: Lower thresholds trigger more frequent rebalancing, improving memory efficiency but increasing computational overhead.

## 5. Consensus Mechanisms Thresholds

### 5.1 ByzantineFaultTolerance Thresholds

#### `PHASE_TIMEOUT_MS = 5000`
**Purpose**: Defines how long to wait for responses in each phase of the consensus protocol.

**Non-Mathematical Explanation**: Balances between waiting for slow nodes and maintaining system responsiveness.

**Practical Implication**: Longer timeouts improve reliability in unstable networks but increase latency for all operations.

#### `MAX_RETRIES = 3`
**Purpose**: Defines how many times to retry a failed consensus round before giving up.

**Non-Mathematical Explanation**: Provides resilience against temporary network or node failures.

**Practical Implication**: More retries improve success rates but can significantly increase latency when problems occur.

#### `BYZANTINE_THRESHOLD = 0.67`
**Purpose**: Defines the proportion of nodes required to reach consensus.

**Mathematical Explanation**: Based on the theoretical result that Byzantine agreement requires more than 2/3 of nodes to be honest. For n nodes with f Byzantine (malicious) nodes, consensus is possible if and only if n > 3f.

**Practical Implication**: Higher thresholds improve security against Byzantine failures but reduce availability as more nodes must agree.

## 6. Agent Coordination Thresholds

### 6.1 AgentCoordinatorService Thresholds

#### `collaborationThreshold = 0.7`
**Purpose**: Defines the minimum task complexity level that triggers multi-agent collaboration.

**Non-Mathematical Explanation**: Determines when a task is complex enough to warrant the overhead of coordination between multiple agents.

**Practical Implication**: Higher thresholds reduce coordination overhead but may result in suboptimal handling of moderately complex tasks.

#### `stallTimeout = 60`
**Purpose**: Defines how many minutes an agent can be unresponsive before being considered stalled.

**Non-Mathematical Explanation**: Detects agents that have stopped making progress.

**Practical Implication**: Shorter timeouts detect problems faster but may incorrectly flag agents working on time-consuming tasks.

#### `maxRecoveryAttempts = 3`
**Purpose**: Defines how many times to attempt recovery of an unhealthy agent before replacing it.

**Non-Mathematical Explanation**: Balances between giving agents a chance to recover and maintaining system health.

**Practical Implication**: More recovery attempts improve agent stability but may prolong system degradation when agents are truly failing.

#### `maxErrorRate = 0.3`
**Purpose**: Defines the maximum acceptable error rate for an agent before it's considered unhealthy.

**Mathematical Explanation**: Calculated as (failed_tasks / total_tasks). A threshold of 0.3 means an agent is considered unhealthy if more than 30% of its tasks fail.

**Practical Implication**: Lower thresholds create a more reliable system but may unnecessarily flag agents dealing with difficult tasks.

#### `recoveryDelay = 10`
**Purpose**: Defines how many seconds to wait before restarting a recovered agent.

**Non-Mathematical Explanation**: Provides a cooling-off period to prevent rapid restart cycles.

**Practical Implication**: Longer delays reduce thrashing but increase downtime during recovery.