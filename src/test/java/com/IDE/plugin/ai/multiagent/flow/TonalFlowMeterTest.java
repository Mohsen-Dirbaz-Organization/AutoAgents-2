package com.IDE.plugin.ai.multiagent.flow;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.*;

class TonalFlowMeterTest {

    private TonalFlowMeter meter() {
        return new TonalFlowMeter(
            new TonalFlowMeter.PromptDistributionContract(
                "base", "theta-0", "system", Duration.ofMinutes(5), true),
            new TonalFlowMeter.StreamContract(
                3, 2, Duration.ofMillis(100), Duration.ofSeconds(10),
                2.0, 20, 0.10),
            new TonalFlowMeter.OptimizationContract(
                "maximize utility J", "declared feasible set F",
                "authority + deadline + board + response budget",
                Duration.ofSeconds(5),
                (candidate, snapshot) -> candidate.startsWith("feasible")),
            (a, b) -> a.payload().trim().equalsIgnoreCase(b.payload().trim())
        );
    }

    @Test
    void promptShiftRequiresDeclaredAuthorityAndRecordsCausalTrace() {
        TonalFlowMeter meter = meter();

        TonalFlowMeter.PromptShift shift = meter.shiftPrompt(
            "theta", "0", "1", "system",
            "shift should expose a missing distinction",
            "restore theta=0 if downstream decision does not change",
            Duration.ofSeconds(30));

        assertEquals("theta", shift.parameter());
        assertEquals("system", shift.authority());
        assertEquals("system", meter.getPromptShifts().get(0).authority());
    }

    @Test
    void driftCrossingBudgetRequiresClassSplit() {
        TonalFlowMeter meter = meter();
        meter.observe(response("r1", "same"));
        meter.observe(response("r2", "same"));

        String classId = meter.getClasses().keySet().iterator().next();
        assertFalse(meter.splitIfDriftExceedsBudget(classId, 0.05));
        assertTrue(meter.splitIfDriftExceedsBudget(classId, 0.11));
        assertTrue(meter.getClasses().get(classId).requiresSplit());
    }

    @Test
    void decisionChangingMergeIsRejected() {
        TonalFlowMeter meter = meter();
        meter.observe(response("r1", "A"));
        meter.observe(response("r2", "B"));

        String[] ids = meter.getClasses().keySet().toArray(String[]::new);
        assertEquals(2, ids.length);

        assertThrows(IllegalArgumentException.class, () ->
            meter.mergeClasses(
                ids[0], ids[1], false,
                new TonalFlowMeter.ErosionVector(
                    0, 0, 0, 0, 0, 0, 1, true, false)));
    }

    @Test
    void certificationNeedsUniversalBound() {
        TonalFlowMeter meter = meter();

        TonalFlowMeter.CertificationStatus unproved =
            meter.certify("feasible-1", 10.0, OptionalDouble.empty());
        assertEquals(TonalFlowMeter.CertificateState.BEST_CERTIFIED_CANDIDATE, unproved.state());
        assertEquals(TonalFlowMeter.GLOBAL_OPTIMALITY_NOT_PROVED, unproved.statement());

        TonalFlowMeter.CertificationStatus proved =
            meter.certify("feasible-1", 10.0, OptionalDouble.of(10.0));
        assertEquals(TonalFlowMeter.CertificateState.GLOBAL_OPTIMALITY_PROVED, proved.state());
    }

    private TonalFlowMeter.ResponseRecord response(String id, String payload) {
        return new TonalFlowMeter.ResponseRecord(
            id, "agent-" + id, "worker", "theta-0", Instant.now(),
            payload, true, TonalFlowMeter.ErosionVector.none());
    }
}