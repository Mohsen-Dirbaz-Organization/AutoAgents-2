from __future__ import annotations

from dataclasses import dataclass, replace
from enum import Enum
from typing import Any

from cega.pk0 import (
    ContractError,
    RecordHeader,
    TypedTimePoint,
    runtime_eligible,
    validate_record,
    validate_successor,
)


class UncertaintyKind(str, Enum):
    INTERVAL = "INTERVAL"
    BOUNDED_SET = "BOUNDED_SET"
    COVARIANCE = "COVARIANCE"
    SAMPLES = "SAMPLES"
    CATEGORICAL = "CATEGORICAL"
    CREDAL = "CREDAL"
    UNKNOWN = "UNKNOWN"


class SupportStatus(str, Enum):
    IN_SUPPORT = "IN_SUPPORT"
    OUT_OF_SUPPORT = "OUT_OF_SUPPORT"
    UNKNOWN = "UNKNOWN"


class HypothesisLifecycle(str, Enum):
    ACTIVE = "ACTIVE"
    DORMANT = "DORMANT"
    RETIRED = "RETIRED"
    INVALID = "INVALID"
    CONTRADICTED = "CONTRADICTED"


def _nonblank(value: str, code: str, name: str) -> None:
    if not isinstance(value, str) or not value.strip():
        raise ContractError(code, f"{name} must be nonblank")


def _strings(values: tuple[str, ...], code: str, name: str) -> None:
    if any(not isinstance(value, str) or not value.strip() for value in values):
        raise ContractError(code, f"{name} must contain only nonblank strings")
    if len(values) != len(set(values)):
        raise ContractError(code, f"{name} must not contain duplicates")


@dataclass(frozen=True)
class UncertaintyDescriptor:
    kind: UncertaintyKind
    dimension: int
    parameters_ref: str
    support_status: SupportStatus
    support_ref: str
    calibration_ref: str
    reason_codes: tuple[str, ...] = ()

    def __post_init__(self) -> None:
        if type(self.dimension) is not int or self.dimension <= 0:
            raise ContractError("E_UNCERTAINTY_DIMENSION", "dimension must be int > 0")
        _nonblank(self.parameters_ref, "E_UNCERTAINTY_PARAMETERS", "parameters_ref")
        _nonblank(self.support_ref, "E_UNCERTAINTY_SUPPORT", "support_ref")
        _nonblank(self.calibration_ref, "E_UNCERTAINTY_CALIBRATION", "calibration_ref")
        _strings(self.reason_codes, "E_UNCERTAINTY_REASONS", "reason_codes")
        if self.kind is UncertaintyKind.UNKNOWN and self.support_status is SupportStatus.IN_SUPPORT:
            raise ContractError(
                "E_UNKNOWN_UNCERTAINTY_IN_SUPPORT",
                "UNKNOWN uncertainty cannot claim IN_SUPPORT",
            )
        if self.support_status is not SupportStatus.IN_SUPPORT and not self.reason_codes:
            raise ContractError(
                "E_UNCERTAINTY_SUPPORT_REASON",
                "non-supported uncertainty requires a reason code",
            )


@dataclass(frozen=True)
class HypothesisState:
    hypothesis_id: str
    state_ref: str
    lifecycle: HypothesisLifecycle
    support_status: SupportStatus
    weight_ppm: int | None
    reason_codes: tuple[str, ...] = ()

    def __post_init__(self) -> None:
        _nonblank(self.hypothesis_id, "E_HYPOTHESIS_ID", "hypothesis_id")
        _nonblank(self.state_ref, "E_HYPOTHESIS_STATE", "state_ref")
        _strings(self.reason_codes, "E_HYPOTHESIS_REASONS", "reason_codes")
        if self.weight_ppm is not None and (
            type(self.weight_ppm) is not int or not 0 <= self.weight_ppm <= 1_000_000
        ):
            raise ContractError(
                "E_HYPOTHESIS_WEIGHT",
                "weight_ppm must be None or an integer in [0, 1000000]",
            )
        if self.lifecycle is not HypothesisLifecycle.ACTIVE and not self.reason_codes:
            raise ContractError(
                "E_HYPOTHESIS_LIFECYCLE_REASON",
                "non-active hypotheses require a reason code",
            )
        if self.support_status is not SupportStatus.IN_SUPPORT and not self.reason_codes:
            raise ContractError(
                "E_HYPOTHESIS_SUPPORT_REASON",
                "non-supported hypotheses require a reason code",
            )


@dataclass(frozen=True)
class PredictiveStateEpoch:
    header: RecordHeader
    epoch_id: str
    task_id: str
    parent_epoch_id: str | None
    state_payload_ref: str
    uncertainty: UncertaintyDescriptor
    hypotheses: tuple[HypothesisState, ...]
    decision_relevant_dimensions: tuple[str, ...]
    observed_dimensions: tuple[str, ...]
    null_directions: tuple[str, ...]
    observability_evidence_refs: tuple[str, ...]
    transition_reasons: tuple[str, ...] = ()

    def __post_init__(self) -> None:
        _nonblank(self.epoch_id, "E_EPOCH_ID", "epoch_id")
        _nonblank(self.task_id, "E_TASK_ID", "task_id")
        _nonblank(self.state_payload_ref, "E_STATE_PAYLOAD_REF", "state_payload_ref")
        if self.epoch_id != self.header.record_id:
            raise ContractError("E_EPOCH_RECORD_ID", "epoch_id must equal header.record_id")
        if self.header.generation == 0:
            if self.parent_epoch_id is not None:
                raise ContractError("E_ROOT_PARENT", "generation zero must not name a parent epoch")
        else:
            if self.parent_epoch_id is None:
                raise ContractError("E_SUCCESSOR_PARENT_EPOCH", "successor epoch must name its parent")
            _nonblank(self.parent_epoch_id, "E_SUCCESSOR_PARENT_EPOCH", "parent_epoch_id")
            if not self.transition_reasons:
                raise ContractError(
                    "E_TRANSITION_REASON",
                    "successor epoch requires at least one transition reason",
                )
        _strings(
            self.decision_relevant_dimensions,
            "E_DECISION_DIMENSIONS",
            "decision_relevant_dimensions",
        )
        _strings(self.observed_dimensions, "E_OBSERVED_DIMENSIONS", "observed_dimensions")
        _strings(self.null_directions, "E_NULL_DIRECTIONS", "null_directions")
        _strings(
            self.observability_evidence_refs,
            "E_OBSERVABILITY_EVIDENCE",
            "observability_evidence_refs",
        )
        _strings(self.transition_reasons, "E_TRANSITION_REASONS", "transition_reasons")
        if not self.decision_relevant_dimensions:
            raise ContractError(
                "E_DECISION_DIMENSIONS_EMPTY",
                "at least one decision-relevant dimension is required",
            )
        observed = set(self.observed_dimensions)
        nulls = set(self.null_directions)
        if observed & nulls:
            raise ContractError(
                "E_OBSERVABILITY_OVERLAP",
                "a dimension cannot be observed and null simultaneously",
            )
        unclassified = set(self.decision_relevant_dimensions) - observed - nulls
        if unclassified:
            raise ContractError(
                "E_DECISION_DIMENSION_UNCLASSIFIED",
                f"unclassified decision dimensions: {sorted(unclassified)}",
            )
        if nulls and not self.observability_evidence_refs:
            raise ContractError(
                "E_NULL_WITHOUT_EVIDENCE",
                "null directions require an observability evidence reference",
            )
        if not self.hypotheses:
            raise ContractError("E_HYPOTHESES_EMPTY", "state epoch requires hypotheses")
        hypothesis_ids = [hypothesis.hypothesis_id for hypothesis in self.hypotheses]
        if len(hypothesis_ids) != len(set(hypothesis_ids)):
            raise ContractError("E_HYPOTHESIS_DUPLICATE", "hypothesis IDs must be unique")
        active = [
            hypothesis
            for hypothesis in self.hypotheses
            if hypothesis.lifecycle is HypothesisLifecycle.ACTIVE
        ]
        active_weights = [hypothesis.weight_ppm for hypothesis in active]
        specified_weights = [weight for weight in active_weights if weight is not None]
        if specified_weights and len(specified_weights) != len(active_weights):
            raise ContractError(
                "E_HYPOTHESIS_PARTIAL_WEIGHTS",
                "active hypotheses must be either all weighted or all unweighted",
            )
        if specified_weights and sum(specified_weights) != 1_000_000:
            raise ContractError(
                "E_HYPOTHESIS_WEIGHT_SUM",
                "active hypothesis weights must sum to 1000000 ppm",
            )


def validate_state_epoch(epoch: PredictiveStateEpoch, payload: Any) -> None:
    validate_record(epoch.header, payload)


def state_blockers(epoch: PredictiveStateEpoch, now: TypedTimePoint) -> tuple[str, ...]:
    blockers: list[str] = []
    if not runtime_eligible(epoch.header, now):
        blockers.append("E_HEADER_RUNTIME_INELIGIBLE")
    if epoch.uncertainty.support_status is SupportStatus.OUT_OF_SUPPORT:
        blockers.append("E_STATE_OUT_OF_SUPPORT")
    elif epoch.uncertainty.support_status is SupportStatus.UNKNOWN:
        blockers.append("E_STATE_SUPPORT_UNKNOWN")
    for dimension in sorted(
        set(epoch.decision_relevant_dimensions) & set(epoch.null_directions)
    ):
        blockers.append(f"E_DECISION_NULL_DIRECTION:{dimension}")
    active = [
        hypothesis
        for hypothesis in epoch.hypotheses
        if hypothesis.lifecycle is HypothesisLifecycle.ACTIVE
    ]
    if not active:
        blockers.append("E_NO_ACTIVE_HYPOTHESIS")
    for hypothesis in active:
        if hypothesis.support_status is not SupportStatus.IN_SUPPORT:
            blockers.append(f"E_ACTIVE_HYPOTHESIS_SUPPORT:{hypothesis.hypothesis_id}")
    return tuple(sorted(set(blockers)))


def runtime_eligible_state(epoch: PredictiveStateEpoch, now: TypedTimePoint) -> bool:
    return not state_blockers(epoch, now)


def validate_epoch_successor(
    parent: PredictiveStateEpoch,
    child: PredictiveStateEpoch,
) -> None:
    validate_successor(parent.header, child.header)
    if child.parent_epoch_id != parent.epoch_id:
        raise ContractError(
            "E_EPOCH_PARENT",
            "child parent_epoch_id must equal parent epoch_id",
        )
    if child.task_id != parent.task_id:
        raise ContractError("E_TASK_REWRITE", "successor must preserve task identity")
    if child.decision_relevant_dimensions != parent.decision_relevant_dimensions:
        raise ContractError(
            "E_DECISION_DIMENSION_REWRITE",
            "successor must preserve the decision-relevant dimension contract",
        )
    if child.uncertainty.dimension != parent.uncertainty.dimension:
        raise ContractError(
            "E_UNCERTAINTY_DIMENSION_REWRITE",
            "successor must preserve uncertainty dimension within one task",
        )

    child_by_id = {hypothesis.hypothesis_id: hypothesis for hypothesis in child.hypotheses}
    for hypothesis in parent.hypotheses:
        successor = child_by_id.get(hypothesis.hypothesis_id)
        if hypothesis.lifecycle is HypothesisLifecycle.CONTRADICTED:
            if successor is None or successor.lifecycle is not HypothesisLifecycle.CONTRADICTED:
                raise ContractError(
                    "E_CONTRADICTION_ERASURE",
                    "contradicted hypotheses must remain explicit in the successor",
                )
        if hypothesis.lifecycle in (
            HypothesisLifecycle.RETIRED,
            HypothesisLifecycle.INVALID,
        ) and successor is not None and successor.lifecycle is HypothesisLifecycle.ACTIVE:
            raise ContractError(
                "E_HYPOTHESIS_REACTIVATION",
                "retired or invalid hypotheses require a new identity before activation",
            )

    parent_nulls = set(parent.null_directions)
    child_nulls = set(child.null_directions)
    resolved_nulls = parent_nulls - child_nulls
    if not resolved_nulls.issubset(set(child.observed_dimensions)):
        raise ContractError(
            "E_NULL_DIRECTION_ERASURE",
            "a removed null direction must become explicitly observed",
        )
    lost_observability = set(parent.observed_dimensions) & child_nulls
    if lost_observability and "rank-loss" not in child.transition_reasons:
        raise ContractError(
            "E_UNRECORDED_RANK_LOSS",
            "observed dimensions may become null only under an explicit rank-loss transition",
        )
    if (
        parent.uncertainty.support_status is not SupportStatus.IN_SUPPORT
        and child.uncertainty.support_status is SupportStatus.IN_SUPPORT
        and child.uncertainty.support_ref == parent.uncertainty.support_ref
        and child.uncertainty.calibration_ref == parent.uncertainty.calibration_ref
    ):
        raise ContractError(
            "E_SUPPORT_PROMOTION_WITHOUT_EVIDENCE",
            "support promotion requires a new support or calibration reference",
        )


def _expect_error(code: str, fn: Any) -> None:
    try:
        fn()
    except ContractError as exc:
        assert exc.code == code, (exc.code, code)
    else:
        raise AssertionError(f"expected ContractError {code}")


def self_test() -> None:
    from cega.pk0 import (
        AuthorityCeiling,
        EndpointID,
        EvidenceStanding,
        RecordStatus,
        TypedTimeInterval,
        ValidityContract,
        make_lineage,
    )

    def point(value: int) -> TypedTimePoint:
        return TypedTimePoint("pk1-clock", value, 0, "native")

    validity = ValidityContract(
        TypedTimeInterval(point(0), point(1000)),
        "pk1-support",
        ("epoch-expired",),
        "boundary-or-calibration-change",
    )
    authority = AuthorityCeiling(
        "pk1-cell",
        ("observe", "propose"),
        ("synthetic-state",),
        point(1000),
    )

    payload0 = {"position": 1, "velocity": None}
    header0 = RecordHeader(
        "0.1.0",
        "epoch-0",
        EndpointID.COMMON,
        0,
        RecordStatus.VALID,
        EvidenceStanding.IMPLEMENTED,
        "native-state-evidence",
        validity,
        make_lineage(
            payload0,
            producer_id="pk1-self-test",
            transform_id="ROOT",
            implementation_version="0.1.0",
        ),
        authority,
    )
    uncertainty0 = UncertaintyDescriptor(
        UncertaintyKind.BOUNDED_SET,
        2,
        "params:bounded-set-0",
        SupportStatus.IN_SUPPORT,
        "support:pk1-v1",
        "calibration:pk1-v1",
    )
    hypotheses0 = (
        HypothesisState(
            "H1",
            "state:H1:0",
            HypothesisLifecycle.ACTIVE,
            SupportStatus.IN_SUPPORT,
            600_000,
        ),
        HypothesisState(
            "H2",
            "state:H2:0",
            HypothesisLifecycle.ACTIVE,
            SupportStatus.IN_SUPPORT,
            400_000,
        ),
    )
    root = PredictiveStateEpoch(
        header0,
        "epoch-0",
        "task-synthetic",
        None,
        "payload:epoch-0",
        uncertainty0,
        hypotheses0,
        ("position", "velocity"),
        ("position",),
        ("velocity",),
        ("evidence:position-only",),
    )
    validate_state_epoch(root, payload0)
    assert not runtime_eligible_state(root, point(500))
    assert "E_DECISION_NULL_DIRECTION:velocity" in state_blockers(root, point(500))

    payload1 = {"position": 1, "velocity": 2}
    header1 = RecordHeader(
        "0.1.0",
        "epoch-1",
        EndpointID.COMMON,
        1,
        RecordStatus.VALID,
        EvidenceStanding.IMPLEMENTED,
        "native-state-evidence",
        validity,
        make_lineage(
            payload1,
            producer_id="pk1-self-test",
            transform_id="resolve-null-direction",
            implementation_version="0.1.0",
            input_refs=(header0.record_id,),
        ),
        authority,
    )
    child = PredictiveStateEpoch(
        header1,
        "epoch-1",
        "task-synthetic",
        "epoch-0",
        "payload:epoch-1",
        uncertainty0,
        (
            replace(hypotheses0[0], state_ref="state:H1:1", weight_ppm=700_000),
            replace(hypotheses0[1], state_ref="state:H2:1", weight_ppm=300_000),
        ),
        ("position", "velocity"),
        ("position", "velocity"),
        (),
        ("evidence:position", "evidence:velocity"),
        ("resolved-null-direction",),
    )
    validate_state_epoch(child, payload1)
    validate_epoch_successor(root, child)
    assert runtime_eligible_state(child, point(500))

    contradicted_parent = replace(
        root,
        hypotheses=(
            replace(hypotheses0[0], weight_ppm=1_000_000),
            HypothesisState(
                "H2",
                "state:H2:0",
                HypothesisLifecycle.CONTRADICTED,
                SupportStatus.IN_SUPPORT,
                None,
                ("evidence-conflict",),
            ),
        ),
    )
    child_without_contradiction = replace(
        child,
        hypotheses=(replace(child.hypotheses[0], weight_ppm=1_000_000),),
    )
    _expect_error(
        "E_CONTRADICTION_ERASURE",
        lambda: validate_epoch_successor(
            contradicted_parent,
            child_without_contradiction,
        ),
    )


if __name__ == "__main__":
    self_test()
    print("CEGA-PK1-min self-test passed")
