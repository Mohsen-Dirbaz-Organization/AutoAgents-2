from __future__ import annotations

from dataclasses import dataclass
from typing import Any, Callable, Protocol

from cega.pk0 import (
    ContractError,
    RecordHeader,
    TypedTimePoint,
    payload_digest,
    runtime_eligible,
    validate_record,
)
from cega.pk5_min import (
    Reading,
    ReadingStanding,
    ResourceDelta,
    RunTrace,
    StageStatus,
    StageTrace,
)
from cega.pk7_min import (
    ActionCandidate,
    GateAction,
    GateContext,
    GateDecision,
    GatePredicate,
    evaluate_gate,
)


class EndpointAdapter(Protocol):
    adapter_id: str

    def transport(self, payload: Any) -> Any: ...

    def update(self, surface: Any) -> Any: ...

    def propose(
        self, state: Any, now: TypedTimePoint
    ) -> tuple[ActionCandidate, tuple[GatePredicate, ...]]: ...

    def simulate_outcome(self, action: GateAction, state: Any) -> Any: ...


@dataclass(frozen=True)
class TransactionInput:
    run_id: str
    profile_id: str
    header: RecordHeader
    payload: Any
    gate_context: GateContext
    now: TypedTimePoint
    deadline_ns: int
    protected_reserve_ns: int


@dataclass(frozen=True)
class TransactionResult:
    final_action: GateAction
    gate_decision: GateDecision | None
    response_trace: RunTrace
    full_trace: RunTrace
    outcome: Any
    replay_material: Any
    replay_digest: str
    reason_codes: tuple[str, ...]


def _unknown_resources(valid_results: int = 0) -> ResourceDelta:
    return ResourceDelta(
        operations=(),
        operation_coverage_complete=False,
        resident_bytes=Reading.unavailable("bytes"),
        bytes_read=Reading.unavailable("bytes"),
        bytes_written=Reading.unavailable("bytes"),
        network_bytes=Reading.unavailable("bytes"),
        sync_events=Reading.unavailable("events"),
        messages=Reading.unavailable("messages"),
        queue_wait_ns=Reading.unavailable("ns"),
        energy_uj=Reading.unavailable("uJ"),
        temperature_start_mc=Reading.unavailable("mC"),
        temperature_end_mc=Reading.unavailable("mC"),
        valid_result_count=Reading(
            valid_results,
            "results",
            ReadingStanding.SYNTHETIC,
            "core0",
        ),
    )


def _time_point(clock_id: str, value_ns: int) -> TypedTimePoint:
    return TypedTimePoint(clock_id, value_ns, 0, "native")


def _run_stage(
    *,
    stage_id: str,
    parents: tuple[str, ...],
    clock_id: str,
    clock_ns: Callable[[], int],
    fn: Callable[[], Any],
    valid_results: int = 0,
) -> tuple[Any, StageTrace, ContractError | None]:
    start = _time_point(clock_id, clock_ns())
    try:
        value = fn()
        status = StageStatus.SUCCESS
        reason = ""
        error = None
    except ContractError as exc:
        value = None
        status = StageStatus.FAILED
        reason = exc.code
        error = exc
    end = _time_point(clock_id, clock_ns())
    return (
        value,
        StageTrace(
            stage_id,
            parents,
            start,
            end,
            status,
            _unknown_resources(valid_results),
            reason,
        ),
        error,
    )


def verify_replay_digest(material: Any, expected_digest: str) -> None:
    actual = payload_digest(material)
    if actual != expected_digest:
        raise ContractError(
            "E_REPLAY_DIGEST_MISMATCH",
            f"expected {expected_digest}, calculated {actual}",
        )


def execute_core0(
    tx: TransactionInput,
    adapter: EndpointAdapter,
    *,
    clock_ns: Callable[[], int],
    clock_id: str = "python.monotonic_ns",
) -> TransactionResult:
    stages: list[StageTrace] = []
    reasons: list[str] = []

    _, stage, error = _run_stage(
        stage_id="0-CONDITION",
        parents=(),
        clock_id=clock_id,
        clock_ns=clock_ns,
        fn=lambda: validate_record(tx.header, tx.payload),
    )
    stages.append(stage)
    if error is not None:
        return _terminal_refusal(tx, stages, reasons + [error.code], clock_ns, clock_id)

    _, stage, error = _run_stage(
        stage_id="1-ADMIT",
        parents=("0-CONDITION",),
        clock_id=clock_id,
        clock_ns=clock_ns,
        fn=lambda: _require_runtime_eligible(tx),
    )
    stages.append(stage)
    if error is not None:
        return _terminal_refusal(tx, stages, reasons + [error.code], clock_ns, clock_id)

    surface, stage, error = _run_stage(
        stage_id="2-TRANSPORT",
        parents=("1-ADMIT",),
        clock_id=clock_id,
        clock_ns=clock_ns,
        fn=lambda: adapter.transport(tx.payload),
    )
    stages.append(stage)
    if error is not None:
        return _terminal_refusal(tx, stages, reasons + [error.code], clock_ns, clock_id)

    state, stage, error = _run_stage(
        stage_id="3-UPDATE",
        parents=("2-TRANSPORT",),
        clock_id=clock_id,
        clock_ns=clock_ns,
        fn=lambda: adapter.update(surface),
    )
    stages.append(stage)
    if error is not None:
        return _terminal_refusal(tx, stages, reasons + [error.code], clock_ns, clock_id)

    proposal, stage, error = _run_stage(
        stage_id="4-PROPOSE",
        parents=("3-UPDATE",),
        clock_id=clock_id,
        clock_ns=clock_ns,
        fn=lambda: adapter.propose(state, tx.now),
    )
    stages.append(stage)
    if error is not None:
        return _terminal_refusal(tx, stages, reasons + [error.code], clock_ns, clock_id)
    candidate, predicates = proposal

    gate, stage, error = _run_stage(
        stage_id="5-VERIFY",
        parents=("4-PROPOSE",),
        clock_id=clock_id,
        clock_ns=clock_ns,
        fn=lambda: evaluate_gate(candidate, predicates, tx.gate_context, tx.now),
    )
    stages.append(stage)
    if error is not None:
        return _terminal_refusal(tx, stages, reasons + [error.code], clock_ns, clock_id)

    tentative, stage, error = _run_stage(
        stage_id="6-EMIT",
        parents=("5-VERIFY",),
        clock_id=clock_id,
        clock_ns=clock_ns,
        fn=lambda: gate.action,
    )
    stages.append(stage)
    if error is not None:
        return _terminal_refusal(tx, stages, reasons + [error.code], clock_ns, clock_id)

    response_trace = RunTrace(
        tx.run_id + ":response",
        tx.profile_id,
        tx.deadline_ns,
        tx.protected_reserve_ns,
        tuple(stages),
    )
    final_action = tentative
    if response_trace.deadline_margin_upper_ns() < 0:
        reasons.append("E_RESPONSE_DEADLINE_MISS")
        final_action = GateAction.FALLBACK if tx.gate_context.fallback_available else GateAction.REFUSE

    outcome, stage, error = _run_stage(
        stage_id="7-OBSERVE",
        parents=("6-EMIT",),
        clock_id=clock_id,
        clock_ns=clock_ns,
        fn=lambda: adapter.simulate_outcome(final_action, state),
    )
    stages.append(stage)
    if error is not None:
        reasons.append(error.code)
        outcome = {"status": "UNKNOWN", "reason": error.code}

    replay_material = {
        "run_id": tx.run_id,
        "profile_id": tx.profile_id,
        "input_digest": tx.header.lineage.payload_digest,
        "adapter_id": adapter.adapter_id,
        "candidate": candidate,
        "gate": gate,
        "final_action": final_action,
        "outcome": outcome,
        "stage_ids": tuple(item.stage_id for item in stages),
    }
    replay, stage, error = _run_stage(
        stage_id="8-REPLAY",
        parents=("7-OBSERVE",),
        clock_id=clock_id,
        clock_ns=clock_ns,
        fn=lambda: payload_digest(replay_material),
    )
    stages.append(stage)
    if error is not None:
        reasons.append(error.code)
        replay = "UNAVAILABLE"

    _, stage, error = _run_stage(
        stage_id="9-SUCCEED-OR-ROLLBACK",
        parents=("8-REPLAY",),
        clock_id=clock_id,
        clock_ns=clock_ns,
        fn=lambda: final_action,
        valid_results=1,
    )
    stages.append(stage)
    if error is not None:
        reasons.append(error.code)

    full_trace = RunTrace(
        tx.run_id,
        tx.profile_id,
        tx.deadline_ns,
        tx.protected_reserve_ns,
        tuple(stages),
    )
    reasons.extend(gate.reason_codes)
    return TransactionResult(
        final_action,
        gate,
        response_trace,
        full_trace,
        outcome,
        replay_material,
        replay,
        tuple(sorted(set(reasons))),
    )


def _require_runtime_eligible(tx: TransactionInput) -> None:
    if not runtime_eligible(tx.header, tx.now):
        raise ContractError("E_RUNTIME_INELIGIBLE", "record is invalid, unknown, or outside validity")


def _terminal_refusal(
    tx: TransactionInput,
    stages: list[StageTrace],
    reasons: list[str],
    clock_ns: Callable[[], int],
    clock_id: str,
) -> TransactionResult:
    parent = stages[-1].stage_id
    replay_material = {
        "run_id": tx.run_id,
        "action": GateAction.REFUSE,
        "reasons": tuple(sorted(set(reasons))),
    }
    replay = payload_digest(replay_material)
    _, final_stage, _ = _run_stage(
        stage_id="9-SUCCEED-OR-ROLLBACK",
        parents=(parent,),
        clock_id=clock_id,
        clock_ns=clock_ns,
        fn=lambda: GateAction.REFUSE,
        valid_results=1,
    )
    stages.append(final_stage)
    trace = RunTrace(
        tx.run_id,
        tx.profile_id,
        tx.deadline_ns,
        tx.protected_reserve_ns,
        tuple(stages),
    )
    return TransactionResult(
        GateAction.REFUSE,
        None,
        trace,
        trace,
        {"status": "NOT_OBSERVED"},
        replay_material,
        replay,
        tuple(sorted(set(reasons))),
    )
