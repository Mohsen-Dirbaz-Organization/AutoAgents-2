from __future__ import annotations

from dataclasses import dataclass
from enum import Enum
from typing import Any

from cega.core0 import TransactionInput, TransactionResult, execute_core0, verify_replay_digest
from cega.pk0 import (
    AuthorityCeiling,
    ContractError,
    EndpointID,
    EvidenceStanding,
    RecordHeader,
    RecordStatus,
    TypedTimeInterval,
    TypedTimePoint,
    ValidityContract,
    make_lineage,
    payload_digest,
)
from cega.pk7_min import (
    ActionCandidate,
    GateAction,
    GateContext,
    GatePredicate,
    PredicateClass,
    PredicateStatus,
)


class FixtureMode(str, Enum):
    NOMINAL = "nominal"
    DIGEST_MISMATCH = "digest_mismatch"
    EXPIRED_INPUT = "expired_input"
    UNKNOWN_SUPPORT = "unknown_model_support"
    FAILED_SAFETY = "failed_safety"
    SELF_VERIFICATION = "self_verification"
    AUTHORITY_EXPANSION = "authority_expansion"
    DEADLINE_MISS = "response_deadline_miss"


@dataclass
class SteppedClock:
    step_ns: int = 10
    initial_ns: int = 0
    calls: int = 0

    def __post_init__(self) -> None:
        if type(self.step_ns) is not int or self.step_ns <= 0:
            raise ValueError("step_ns must be int > 0")

    def __call__(self) -> int:
        if self.calls == 0:
            value = self.initial_ns
        else:
            value = self.initial_ns + ((self.calls + 1) // 2) * self.step_ns
        self.calls += 1
        return value


def fixture_point(value_ns: int) -> TypedTimePoint:
    return TypedTimePoint("fixture-clock", value_ns, 0, "native")


class SyntheticAdapter:
    adapter_id = "cega.synthetic.v0.1"

    def __init__(
        self,
        mode: FixtureMode,
        validity: ValidityContract,
        current_ceiling: AuthorityCeiling,
    ) -> None:
        self.mode = mode
        self.validity = validity
        self.current_ceiling = current_ceiling

    def transport(self, payload: Any) -> Any:
        return {"transported": payload}

    def update(self, surface: Any) -> Any:
        return {"state": "synthetic", "surface": surface}

    def _predicate(
        self,
        predicate_id: str,
        predicate_class: PredicateClass,
        status: PredicateStatus = PredicateStatus.PASS,
        reason_codes: tuple[str, ...] = (),
    ) -> GatePredicate:
        return GatePredicate(
            predicate_id,
            predicate_class,
            status,
            True,
            (f"evidence:{predicate_id}",),
            reason_codes,
            self.validity,
            f"evaluator:{predicate_id}",
        )

    def propose(
        self, state: Any, now: TypedTimePoint
    ) -> tuple[ActionCandidate, tuple[GatePredicate, ...]]:
        if self.mode is FixtureMode.AUTHORITY_EXPANSION:
            requested = AuthorityCeiling(
                "fixture-cell",
                ("execute", "observe", "propose"),
                ("synthetic-fixture",),
                fixture_point(1000),
            )
        else:
            requested = AuthorityCeiling(
                "fixture-cell",
                ("observe",),
                ("synthetic-fixture",),
                fixture_point(900),
            )

        model_status = PredicateStatus.UNKNOWN if self.mode is FixtureMode.UNKNOWN_SUPPORT else PredicateStatus.PASS
        model_reasons = ("OUTSIDE_SUPPORT",) if model_status is PredicateStatus.UNKNOWN else ()
        safety_status = PredicateStatus.FAIL if self.mode is FixtureMode.FAILED_SAFETY else PredicateStatus.PASS
        safety_reasons = ("UNSAFE",) if safety_status is PredicateStatus.FAIL else ()
        predicates = (
            self._predicate("P-EVIDENCE", PredicateClass.EVIDENCE),
            self._predicate("P-TEMPORAL", PredicateClass.TEMPORAL),
            self._predicate("P-AUTHORITY", PredicateClass.AUTHORITY),
            self._predicate("P-RECOVERY", PredicateClass.RECOVERY),
            self._predicate("P-MODEL", PredicateClass.MODEL_SUPPORT, model_status, model_reasons),
            self._predicate("P-SAFETY", PredicateClass.ENDPOINT_SAFETY, safety_status, safety_reasons),
        )
        return (
            ActionCandidate(
                "fixture-action",
                EndpointID.COMMON.value,
                "observe",
                "synthetic-fixture",
                "fixture-proposer",
                requested,
            ),
            predicates,
        )

    def simulate_outcome(self, action: GateAction, state: Any) -> Any:
        return {
            "status": "SYNTHETIC_OBSERVATION",
            "action": action.value,
            "state_digest": payload_digest(state),
            "physical_actuation": False,
        }


def build_fixture(mode: FixtureMode = FixtureMode.NOMINAL) -> tuple[TransactionInput, SyntheticAdapter, SteppedClock]:
    payload: dict[str, Any] = {
        "fixture": "CORE0",
        "schema": "0.1.0",
        "values": [1, 2, 3],
    }
    validity = ValidityContract(
        TypedTimeInterval(fixture_point(0), fixture_point(1000)),
        "synthetic-support-v1",
        ("fixture-expired", "clock-reset"),
        "fixture-epoch-change",
    )
    lineage = make_lineage(
        payload,
        producer_id="fixture-source",
        transform_id="ROOT",
        implementation_version="0.1.0",
    )
    current_ceiling = AuthorityCeiling(
        "fixture-cell",
        ("observe", "propose"),
        ("synthetic-fixture",),
        fixture_point(1000),
    )
    header = RecordHeader(
        "0.1.0",
        "fixture-record-0",
        EndpointID.COMMON,
        0,
        RecordStatus.VALID,
        EvidenceStanding.SOURCE_BEARING,
        "native:synthetic-fixture",
        validity,
        lineage,
        current_ceiling,
    )
    if mode is FixtureMode.DIGEST_MISMATCH:
        payload = {**payload, "tampered": True}

    now = fixture_point(1500 if mode is FixtureMode.EXPIRED_INPUT else 500)
    verifier = "fixture-proposer" if mode is FixtureMode.SELF_VERIFICATION else "fixture-verifier"
    deadline_ns = 60 if mode is FixtureMode.DEADLINE_MISS else 1000
    tx = TransactionInput(
        run_id=f"CEGA-CORE0-{mode.value}",
        profile_id="synthetic-no-actuation-v0.1",
        header=header,
        payload=payload,
        gate_context=GateContext(current_ceiling, verifier, True),
        now=now,
        deadline_ns=deadline_ns,
        protected_reserve_ns=0,
    )
    return tx, SyntheticAdapter(mode, validity, current_ceiling), SteppedClock()


def execute_fixture(mode: FixtureMode = FixtureMode.NOMINAL) -> TransactionResult:
    tx, adapter, clock = build_fixture(mode)
    return execute_core0(tx, adapter, clock_ns=clock, clock_id="fixture-clock")


def _observation(case: str, result: TransactionResult, expected: str) -> dict[str, Any]:
    return {
        "case": case,
        "expected": expected,
        "actual": result.final_action.value,
        "reason_codes": list(result.reason_codes),
        "response_margin_ns": result.response_trace.deadline_margin_upper_ns(),
        "response_stage_ids": [stage.stage_id for stage in result.response_trace.stages],
        "full_stage_ids": [stage.stage_id for stage in result.full_trace.stages],
        "critical_span_ns": result.full_trace.critical_span_upper_ns(),
        "replay_digest": result.replay_digest,
        "energy_uj": result.full_trace.sum_reading("energy_uj"),
        "temperature_start_mc": result.full_trace.sum_reading("temperature_start_mc"),
        "physical_actuation": False,
    }


def run_campaign(assert_expected: bool = True) -> tuple[dict[str, Any], ...]:
    expected_actions = {
        FixtureMode.NOMINAL: GateAction.ADMIT,
        FixtureMode.DIGEST_MISMATCH: GateAction.REFUSE,
        FixtureMode.EXPIRED_INPUT: GateAction.REFUSE,
        FixtureMode.UNKNOWN_SUPPORT: GateAction.HOLD,
        FixtureMode.FAILED_SAFETY: GateAction.FALLBACK,
        FixtureMode.SELF_VERIFICATION: GateAction.FALLBACK,
        FixtureMode.AUTHORITY_EXPANSION: GateAction.HOLD,
        FixtureMode.DEADLINE_MISS: GateAction.FALLBACK,
    }
    observations: list[dict[str, Any]] = []
    nominal: TransactionResult | None = None
    for mode, expected in expected_actions.items():
        result = execute_fixture(mode)
        if assert_expected:
            assert result.final_action is expected, (mode.value, result.final_action, expected)
        observations.append(_observation(mode.value, result, expected.value))
        if mode is FixtureMode.NOMINAL:
            nominal = result

    assert nominal is not None
    mutated = dict(nominal.replay_material)
    mutated["outcome"] = {"status": "MUTATED_AFTER_RUN"}
    replay_error = ""
    try:
        verify_replay_digest(mutated, nominal.replay_digest)
    except ContractError as exc:
        replay_error = exc.code
    if assert_expected:
        assert replay_error == "E_REPLAY_DIGEST_MISMATCH"
    observations.append(
        {
            "case": "replay_mutation",
            "expected": "E_REPLAY_DIGEST_MISMATCH",
            "actual": replay_error,
            "original_replay_digest": nominal.replay_digest,
            "mutated_replay_digest": payload_digest(mutated),
            "physical_actuation": False,
        }
    )
    return tuple(observations)
