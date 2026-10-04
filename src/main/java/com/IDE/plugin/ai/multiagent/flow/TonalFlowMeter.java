package com.IDE.plugin.ai.multiagent.flow;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiPredicate;

/**
 * Resolution-aware control/measurement module for parallel subagent response streams.
 *
 * <p>The meter keeps prompt-distribution control, subagent authority, equivalence
 * compression, flow control, latency/board limits, erosion accounting and
 * certification state explicit. It deliberately does not infer authority from
 * sample count or response agreement.</p>
 *
 * <p>This class is instrumentation and policy state, not an optimizer. A caller
 * supplies the objective and feasibility semantics and must provide a universal
 * bound before the meter can report global optimality.</p>
 */
public final class TonalFlowMeter {
    public static final String GLOBAL_OPTIMALITY_NOT_PROVED =
        "Best certified candidate; global optimality not proved.";

    private final PromptDistributionContract promptContract;
    private final StreamContract streamContract;
    private final OptimizationContract optimizationContract;
    private final BiPredicate<ResponseRecord, ResponseRecord> equivalenceRelation;

    private final Map<String, ResponseRecord> responses = new ConcurrentHashMap<>();
    private final Map<String, EquivalenceClassState> classes = new ConcurrentHashMap<>();
    private final List<FlowEvent> events = new CopyOnWriteArrayList<>();
    private final List<PromptShift> promptShifts = new CopyOnWriteArrayList<>();
    private final AtomicLong sequence = new AtomicLong();
    private final Set<String> inFlight = ConcurrentHashMap.newKeySet();

    public TonalFlowMeter(
        @NotNull PromptDistributionContract promptContract,
        @NotNull StreamContract streamContract,
        @NotNull OptimizationContract optimizationContract,
        @NotNull BiPredicate<ResponseRecord, ResponseRecord> equivalenceRelation
    ) {
        this.promptContract = Objects.requireNonNull(promptContract);
        this.streamContract = Objects.requireNonNull(streamContract);
        this.optimizationContract = Objects.requireNonNull(optimizationContract);
        this.equivalenceRelation = Objects.requireNonNull(equivalenceRelation);
        if (streamContract.maxBoard() < 1) {
            throw new IllegalArgumentException("maxBoard must be >= 1");
        }
        if (streamContract.deadline().isNegative()) {
            throw new IllegalArgumentException("deadline must be non-negative");
        }
    }

    /** Records one response without treating it as a vote. */
    public void observe(@NotNull ResponseRecord response) {
        if (response.authority().isEmpty()) {
            throw new IllegalArgumentException("Every response must declare an authority class");
        }
        if (!responses.containsKey(response.id()) && inFlight.size() >= streamContract.maxBoard()) {
            recordFlow(FlowAction.BACKPRESSURE, response.id(),
                "Maximum board reached; admission denied.");
            throw new IllegalStateException("Maximum board reached");
        }
        if (responses.size() >= streamContract.maxResponses() &&
            !responses.containsKey(response.id())) {
            recordFlow(FlowAction.BACKPRESSURE, response.id(),
                "Response budget exhausted; admission denied.");
            throw new IllegalStateException("Response budget exhausted");
        }

        responses.put(response.id(), response);
        inFlight.add(response.id());
        assignToMinimumSufficientClass(response);
        recordFlow(FlowAction.ADMIT, response.id(), "Response admitted with provenance.");
    }

    /** Applies a governed prompt intervention with authority, causality and reversal state. */
    public PromptShift shiftPrompt(
        @NotNull String parameter,
        @NotNull String fromValue,
        @NotNull String toValue,
        @NotNull String authority,
        @NotNull String causalHypothesis,
        @NotNull String reversalCondition,
        @NotNull Duration window
    ) {
        if (!promptContract.shiftAuthority().equals(authority)) {
            throw new IllegalArgumentException("Prompt shift authority mismatch");
        }
        if (window.isNegative() || window.compareTo(promptContract.maxShiftWindow()) > 0) {
            throw new IllegalArgumentException("Prompt shift exceeds declared time window");
        }

        PromptShift shift = new PromptShift(
            nextId("shift"),
            Instant.now(),
            parameter,
            fromValue,
            toValue,
            authority,
            causalHypothesis,
            reversalCondition,
            window
        );
        promptShifts.add(shift);
        recordFlow(FlowAction.PROMPT_SHIFT, shift.id(),
            "Governed prompt-distribution intervention recorded.");
        return shift;
    }

    /**
     * Splits a class when measured drift exceeds its erosion budget.
     * Unexamined items remain Not examined; they are never treated as evidence
     * of no distinction.
     */
    public boolean splitIfDriftExceedsBudget(@NotNull String classId, double observedDrift) {
        EquivalenceClassState state = requireClass(classId);
        if (observedDrift <= state.erosionBudget()) {
            return false;
        }
        state.markSplitRequired(observedDrift);
        recordFlow(FlowAction.ESCALATE, classId,
            "Intra-class drift exceeded budget; re-run merge test.");
        return true;
    }

    /**
     * Permits a merge only when the supplied test establishes that no
     * decision-relevant distinction is lost.
     */
    public void mergeClasses(
        @NotNull String targetClassId,
        @NotNull String sourceClassId,
        boolean decisionRelevantDistinctionLost,
        @NotNull ErosionVector erosion
    ) {
        if (targetClassId.equals(sourceClassId)) {
            return;
        }
        EquivalenceClassState target = requireClass(targetClassId);
        EquivalenceClassState source = requireClass(sourceClassId);

        if (decisionRelevantDistinctionLost || erosion.decisionChanging()) {
            recordFlow(FlowAction.ESCALATE, sourceClassId,
                "Merge rejected because decision-relevant erosion was detected.");
            throw new IllegalArgumentException("Unsafe equivalence-class merge");
        }

        target.absorb(source);
        classes.remove(sourceClassId);
        recordFlow(FlowAction.MERGE, targetClassId,
            "Merge accepted with bounded non-decision-changing erosion.");
    }

    public void completeResponse(@NotNull String responseId) {
        if (inFlight.remove(responseId)) {
            recordFlow(FlowAction.ADMIT, responseId, "Response processing completed; board slot released.");
        }
    }

    public void discardResponse(@NotNull String responseId, @NotNull String reason) {
        inFlight.remove(responseId);
        recordFlow(FlowAction.DISCARD, responseId, reason);
    }

    public int getInFlightCount() {
        return inFlight.size();
    }

    public void recordFlow(
        @NotNull FlowAction action,
        @NotNull String subjectId,
        @NotNull String causalJustification
    ) {
        events.add(new FlowEvent(
            sequence.incrementAndGet(),
            Instant.now(),
            action,
            subjectId,
            causalJustification
        ));
    }

    /**
     * A candidate objective is globally certified only when feasibility holds
     * and a universal upper bound is supplied.
     */
    @NotNull
    public CertificationStatus certify(
        @NotNull String candidateId,
        double candidateObjective,
        @NotNull OptionalDouble universalUpperBound
    ) {
        if (!isFeasible(candidateId)) {
            return new CertificationStatus(
                CertificateState.INFEASIBLE,
                candidateId,
                candidateObjective,
                OptionalDouble.empty(),
                "Candidate violates the declared feasible-set contract."
            );
        }

        if (universalUpperBound.isPresent() &&
            universalUpperBound.getAsDouble() <= candidateObjective) {
            return new CertificationStatus(
                CertificateState.GLOBAL_OPTIMALITY_PROVED,
                candidateId,
                candidateObjective,
                universalUpperBound,
                "Universal upper bound matches or falls below the feasible candidate."
            );
        }

        return new CertificationStatus(
            CertificateState.BEST_CERTIFIED_CANDIDATE,
            candidateId,
            candidateObjective,
            universalUpperBound,
            GLOBAL_OPTIMALITY_NOT_PROVED
        );
    }

    /** Returns a bounded snapshot suitable for deadline-aware reporting. */
    @NotNull
    public MeterSnapshot snapshot(@NotNull Instant startedAt) {
        Duration elapsed = Duration.between(startedAt, Instant.now());
        boolean deadlineReached = elapsed.compareTo(streamContract.deadline()) >= 0;
        int admitted = responses.size();
        int examined = (int) responses.values().stream().filter(ResponseRecord::examined).count();
        int coverageGap = Math.max(0, admitted - examined);

        return new MeterSnapshot(
            promptContract,
            streamContract,
            optimizationContract,
            Collections.unmodifiableMap(new HashMap<>(classes)),
            List.copyOf(promptShifts),
            List.copyOf(events),
            admitted,
            inFlight.size(),
            examined,
            coverageGap,
            deadlineReached,
            deadlineReached || admitted >= streamContract.maxResponses()
        );
    }

    @NotNull
    public Map<String, EquivalenceClassState> getClasses() {
        return Collections.unmodifiableMap(new HashMap<>(classes));
    }

    @NotNull
    public List<FlowEvent> getEvents() {
        return List.copyOf(events);
    }

    @NotNull
    public List<PromptShift> getPromptShifts() {
        return List.copyOf(promptShifts);
    }

    private void assignToMinimumSufficientClass(@NotNull ResponseRecord response) {
        for (EquivalenceClassState state : classes.values()) {
            if (state.requiresSplit()) {
                continue;
            }
            ResponseRecord representative = responses.get(state.representativeResponseId());
            if (representative != null && equivalenceRelation.test(representative, response)) {
                state.add(response.id());
                return;
            }
        }

        String classId = nextId("class");
        classes.put(classId, new EquivalenceClassState(
            classId,
            response.id(),
            streamContract.intraClassErosionBudget()
        ));
    }

    private boolean isFeasible(String candidateId) {
        return optimizationContract.feasibilityRule().test(candidateId, snapshot(Instant.now()));
    }

    private EquivalenceClassState requireClass(String classId) {
        EquivalenceClassState state = classes.get(classId);
        if (state == null) {
            throw new NoSuchElementException("Unknown equivalence class: " + classId);
        }
        return state;
    }

    private String nextId(String prefix) {
        return prefix + "-" + sequence.incrementAndGet();
    }

    public enum FlowAction {
        ADMIT, PAUSE, BACKPRESSURE, MERGE, DISCARD, ESCALATE, PROMPT_SHIFT
    }

    public enum CertificateState {
        GLOBAL_OPTIMALITY_PROVED,
        BEST_CERTIFIED_CANDIDATE,
        INFEASIBLE
    }

    public record PromptDistributionContract(
        @NotNull String basePrompt,
        @NotNull String theta,
        @NotNull String shiftAuthority,
        @NotNull Duration maxShiftWindow,
        boolean reversible
    ) {
        public PromptDistributionContract {
            Objects.requireNonNull(basePrompt);
            Objects.requireNonNull(theta);
            Objects.requireNonNull(shiftAuthority);
            Objects.requireNonNull(maxShiftWindow);
            if (maxShiftWindow.isNegative()) throw new IllegalArgumentException("maxShiftWindow");
        }
    }

    public record StreamContract(
        int subagentCount,
        int maxBoard,
        @NotNull Duration initialLatency,
        @NotNull Duration deadline,
        double streamRatePerSecond,
        int maxResponses,
        double intraClassErosionBudget
    ) {
        public StreamContract {
            if (subagentCount < 1) throw new IllegalArgumentException("subagentCount");
            if (maxBoard < 1) throw new IllegalArgumentException("maxBoard");
            if (initialLatency.isNegative() || deadline.isNegative()) {
                throw new IllegalArgumentException("latencies must be non-negative");
            }
            if (streamRatePerSecond < 0) throw new IllegalArgumentException("streamRatePerSecond");
            if (maxResponses < 1) throw new IllegalArgumentException("maxResponses");
            if (intraClassErosionBudget < 0) throw new IllegalArgumentException("intraClassErosionBudget");
        }
    }

    public record OptimizationContract(
        @NotNull String objective,
        @NotNull String feasibleSet,
        @NotNull String hardConstraints,
        @NotNull Duration computationBudget,
        @NotNull BiPredicate<String, MeterSnapshot> feasibilityRule
    ) {
        public OptimizationContract {
            Objects.requireNonNull(objective);
            Objects.requireNonNull(feasibleSet);
            Objects.requireNonNull(hardConstraints);
            Objects.requireNonNull(computationBudget);
            Objects.requireNonNull(feasibilityRule);
            if (computationBudget.isNegative()) throw new IllegalArgumentException("computationBudget");
        }
    }

    public record ResponseRecord(
        @NotNull String id,
        @NotNull String subagentId,
        @NotNull String authority,
        @NotNull String promptTheta,
        @NotNull Instant timestamp,
        @NotNull String payload,
        boolean examined,
        @NotNull ErosionVector erosion
    ) {
        public ResponseRecord {
            Objects.requireNonNull(id);
            Objects.requireNonNull(subagentId);
            Objects.requireNonNull(authority);
            Objects.requireNonNull(promptTheta);
            Objects.requireNonNull(timestamp);
            Objects.requireNonNull(payload);
            Objects.requireNonNull(erosion);
        }
    }

    public record ErosionVector(
        double content,
        double relevance,
        double evidence,
        double uncertainty,
        double authority,
        double distribution,
        double decision,
        boolean decisionChanging,
        boolean directional
    ) {
        public ErosionVector {
            double[] values = {content, relevance, evidence, uncertainty, authority, distribution, decision};
            for (double value : values) {
                if (value < 0 || Double.isNaN(value) || Double.isInfinite(value)) {
                    throw new IllegalArgumentException("erosion components must be finite and non-negative");
                }
            }
        }

        public static ErosionVector none() {
            return new ErosionVector(0, 0, 0, 0, 0, 0, 0, false, false);
        }
    }

    public record PromptShift(
        @NotNull String id,
        @NotNull Instant timestamp,
        @NotNull String parameter,
        @NotNull String fromValue,
        @NotNull String toValue,
        @NotNull String authority,
        @NotNull String causalHypothesis,
        @NotNull String reversalCondition,
        @NotNull Duration validWindow
    ) {}

    public record FlowEvent(
        long sequence,
        @NotNull Instant timestamp,
        @NotNull FlowAction action,
        @NotNull String subjectId,
        @NotNull String causalJustification
    ) {}

    public record CertificationStatus(
        @NotNull CertificateState state,
        @NotNull String candidateId,
        double candidateObjective,
        @NotNull OptionalDouble universalUpperBound,
        @NotNull String statement
    ) {}

    public record MeterSnapshot(
        @NotNull PromptDistributionContract promptContract,
        @NotNull StreamContract streamContract,
        @NotNull OptimizationContract optimizationContract,
        @NotNull Map<String, EquivalenceClassState> classes,
        @NotNull List<PromptShift> promptShifts,
        @NotNull List<FlowEvent> flowEvents,
        int admittedResponses,
        int examinedResponses,
        int inFlightResponses,
        int coverageGap,
        boolean deadlineReached,
        boolean stoppingRuleReached
    ) {}

    public static final class EquivalenceClassState {
        private final String id;
        private final String representativeResponseId;
        private final Set<String> responseIds = ConcurrentHashMap.newKeySet();
        private final double erosionBudget;
        private volatile boolean splitRequired;
        private volatile double observedDrift;

        private EquivalenceClassState(String id, String representativeResponseId, double erosionBudget) {
            this.id = id;
            this.representativeResponseId = representativeResponseId;
            this.erosionBudget = erosionBudget;
            this.responseIds.add(representativeResponseId);
        }

        private void add(String responseId) {
            responseIds.add(responseId);
        }

        private void absorb(EquivalenceClassState other) {
            responseIds.addAll(other.responseIds);
            splitRequired |= other.splitRequired;
            observedDrift = Math.max(observedDrift, other.observedDrift);
        }

        private void markSplitRequired(double drift) {
            splitRequired = true;
            observedDrift = Math.max(observedDrift, drift);
        }

        public String id() { return id; }
        public String representativeResponseId() { return representativeResponseId; }
        public Set<String> responseIds() { return Set.copyOf(responseIds); }
        public double erosionBudget() { return erosionBudget; }
        public boolean requiresSplit() { return splitRequired; }
        public double observedDrift() { return observedDrift; }
    }
}