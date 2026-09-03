from __future__ import annotations

from dataclasses import dataclass, replace
from enum import Enum
from typing import Any

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
)
from cega.pk1_min import (
    HypothesisLifecycle,
    HypothesisState,
    PredictiveStateEpoch,
    SupportStatus,
    UncertaintyDescriptor,
    UncertaintyKind,
    validate_epoch_successor,
)


class BoundaryKind(str, Enum):
    SOURCE_DISCONTINUITY = "SOURCE_DISCONTINUITY"
    MODE_CHANGE = "MODE_CHANGE"
    SUPPORT_EXIT = "SUPPORT_EXIT"
    CALIBRATION_CHANGE = "CALIBRATION_CHANGE"
    TOPOLOGY_CHANGE = "TOPOLOGY_CHANGE"
    MODEL_CHANGE = "MODEL_CHANGE"
    CLOCK_RESET = "CLOCK_RESET"
    AUTHORITY_CHANGE = "AUTHORITY_CHANGE"
    DEADLINE_MISS = "DEADLINE_MISS"
    EXTERNAL = "EXTERNAL"


class ArtifactKind(str, Enum):
    STATE = "STATE"
    CACHE = "CACHE"
    MODEL = "MODEL"
    PROOF = "PROOF"
    ACTION = "ACTION"


class InvalidationStatus(str, Enum):
    REQUIRED = "REQUIRED"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"
    UNKNOWN = "UNKNOWN"


class TransitionDisposition(str, Enum):
    PROPOSED = "PROPOSED"
    COMMITTED = "COMMITTED"
    ROLLED_BACK = "ROLLED_BACK"
    REFUSED = "REFUSED"


def _nonblank(value: str, code: str, name: str) -> None:
    if not isinstance(value, str) or not value.strip():
        raise ContractError(code, f"{name} must be nonblank")


def _strings(values: tuple[str, ...], code: str, name: str) -> None:
    if any(not isinstance(value, str) or not value.strip() for value in values):
        raise ContractError(code, f"{name} must contain only nonblank strings")
    if len(values) != len(set(values)):
        raise ContractError(code, f"{name} must not contain duplicates")


@dataclass(frozen=True)
class BoundaryTrigger:
    boundary_id: str
    kind: BoundaryKind
    observed_at: TypedTimePoint
    source_ref: str
    evidence_ref: str
    affected_dimensions: tuple[str, ...]
    reason_codes: tuple[str, ...]

    def __post_init__(self) -> None:
        _nonblank(self.boundary_id, "E_BOUNDARY_ID", "boundary_id")
        _nonblank(self.source_ref, "E_BOUNDARY_SOURCE", "source_ref")
        _nonblank(self.evidence_ref, "E_BOUNDARY_EVIDENCE", "evidence_ref")
        _strings(
            self.affected_dimensions,
            "E_BOUNDARY_DIMENSIONS",
            "affected_dimensions",
        )
        _strings(self.reason_codes, "E_BOUNDARY_REASONS", "reason_codes")
        if not self.reason_codes:
            raise ContractError("E_BOUNDARY_REASONS", "boundary requires a reason code")


@dataclass(frozen=True)
class InvalidationTarget:
    kind: ArtifactKind
    target_ref: str
    status: InvalidationStatus
    reason_codes: tuple[str, ...]
    evidence_ref: str | None = None

    def __post_init__(self) -> None:
        _nonblank(self.target_ref, "E_INVALIDATION_TARGET", "target_ref")
        _strings(self.reason_codes, "E_INVALIDATION_REASONS", "reason_codes")
        if not self.reason_codes:
            raise ContractError(
                "E_INVALIDATION_REASONS",
                "invalidation target requires a reason code",
            )
        if self.status is InvalidationStatus.COMPLETED:
            if self.evidence_ref is None:
                raise ContractError(
                    "E_INVALIDATION_EVIDENCE",
                    "completed invalidation requires evidence_ref",
                )
            _nonblank(
                self.evidence_ref,
                "E_INVALIDATION_EVIDENCE",
                "evidence_ref",
            )
        elif self.evidence_ref is not None:
            _nonblank(
                self.evidence_ref,
                "E_INVALIDATION_EVIDENCE",
                "evidence_ref",
            )


@dataclass(frozen=True)
class InvalidationCone:
    boundary_id: str
    predecessor_epoch_id: str
    coverage_scope_ref: str
    discovery_evidence_ref: str
    targets: tuple[InvalidationTarget, ...]

    def __post_init__(self) -> None:
        _nonblank(self.boundary_id, "E_CONE_BOUNDARY", "boundary_id")
        _nonblank(
            self.predecessor_epoch_id,
            "E_CONE_PREDECESSOR",
            "predecessor_epoch_id",
        )
        _nonblank(self.coverage_scope_ref, "E_CONE_SCOPE", "coverage_scope_ref")
        _nonblank(
            self.discovery_evidence_ref,
            "E_CONE_DISCOVERY",
            "discovery_evidence_ref",
        )
        if not self.targets:
            raise ContractError("E_CONE_EMPTY", "invalidation cone requires targets")
        keys = [(target.kind, target.target_ref) for target in self.targets]
        if len(keys) != len(set(keys)):
            raise ContractError(
                "E_CONE_DUPLICATE_TARGET",
                "invalidation targets must be unique by kind and reference",
            )


@dataclass(frozen=True)
class RollbackContract:
    target_epoch_id: str
    trigger_codes: tuple[str, ...]
    recovery_plan_ref: str
    verifier_id: str
    valid_until: TypedTimePoint

    def __post_init__(self) -> None:
        _nonblank(self.target_epoch_id, "E_ROLLBACK_TARGET", "target_epoch_id")
        _strings(self.trigger_codes, "E_ROLLBACK_TRIGGERS", "trigger_codes")
        if not self.trigger_codes:
            raise ContractError("E_ROLLBACK_TRIGGERS", "rollback requires triggers")
        _nonblank(self.recovery_plan_ref, "E_ROLLBACK_PLAN", "recovery_plan_ref")
        _nonblank(self.verifier_id, "E_ROLLBACK_VERIFIER", "verifier_id")


@dataclass(frozen=True)
class BoundaryTransition:
    transition_id: str
    boundary: BoundaryTrigger
    predecessor_epoch_id: str
    successor_epoch_id: str | None
    invalidation_cone: InvalidationCone
    rollback: RollbackContract
    disposition: TransitionDisposition
    decision_evidence_ref: str | None
    reason_codes: tuple[str, ...] = ()

    def __post_init__(self) -> None:
        _nonblank(self.transition_id, "E_TRANSITION_ID", "transition_id")
        _nonblank(
            self.predecessor_epoch_id,
            "E_TRANSITION_PREDECESSOR",
            "predecessor_epoch_id",
        )
        _strings(self.reason_codes, "E_TRANSITION_REASONS", "reason_codes")
        if self.successor_epoch_id is not None:
            _nonblank(
                self.successor_epoch_id,
                "E_TRANSITION_SUCCESSOR",
                "successor_epoch_id",
            )
        if self.disposition in (
            TransitionDisposition.COMMITTED,
            TransitionDisposition.ROLLED_BACK,
        ):
            if self.successor_epoch_id is None:
                raise ContractError(
                    "E_TRANSITION_SUCCESSOR_REQUIRED",
                    "committed or rolled-back transition requires successor_epoch_id",
                )
            if self.decision_evidence_ref is None:
                raise ContractError(
                    "E_TRANSITION_EVIDENCE",
                    "committed or rolled-back transition requires decision evidence",
                )
            _nonblank(
                self.decision_evidence_ref,
                "E_TRANSITION_EVIDENCE",
                "decision_evidence_ref",
            )
        if self.disposition is TransitionDisposition.REFUSED:
            if self.successor_epoch_id is not None:
                raise ContractError(
                    "E_REFUSED_SUCCESSOR",
                    "refused transition must not name a successor",
                )
            if not self.reason_codes:
                raise ContractError(
                    "E_REFUSAL_REASON",
                    "refused transition requires a reason code",
                )
        if self.decision_evidence_ref is not None:
            _nonblank(
                self.decision_evidence_ref,
                "E_TRANSITION_EVIDENCE",
                "decision_evidence_ref",
            )


_REQUIRED_INVALIDATIONS: dict[BoundaryKind, frozenset[ArtifactKind]] = {
    BoundaryKind.SOURCE_DISCONTINUITY: frozenset(
        {ArtifactKind.STATE, ArtifactKind.CACHE, ArtifactKind.PROOF, ArtifactKind.ACTION}
    ),
    BoundaryKind.MODE_CHANGE: frozenset(ArtifactKind),
    BoundaryKind.SUPPORT_EXIT: frozenset(ArtifactKind),
    BoundaryKind.CALIBRATION_CHANGE: frozenset(ArtifactKind),
    BoundaryKind.TOPOLOGY_CHANGE: frozenset(ArtifactKind),
    BoundaryKind.MODEL_CHANGE: frozenset(ArtifactKind),
    BoundaryKind.CLOCK_RESET: frozenset(
        {ArtifactKind.STATE, ArtifactKind.CACHE, ArtifactKind.PROOF, ArtifactKind.ACTION}
    ),
    BoundaryKind.AUTHORITY_CHANGE: frozenset(
        {ArtifactKind.PROOF, ArtifactKind.ACTION}
    ),
    BoundaryKind.DEADLINE_MISS: frozenset({ArtifactKind.ACTION}),
    BoundaryKind.EXTERNAL: frozenset({ArtifactKind.STATE, ArtifactKind.ACTION}),
}


def required_invalidation_kinds(kind: BoundaryKind) -> frozenset[ArtifactKind]:
    return _REQUIRED_INVALIDATIONS[kind]


def invalidation_blockers(
    boundary: BoundaryTrigger,
    cone: InvalidationCone,
) -> tuple[str, ...]:
    blockers: list[str] = []
    if cone.boundary_id != boundary.boundary_id:
        blockers.append("E_CONE_BOUNDARY_MISMATCH")
    present = {target.kind for target in cone.targets}
    for kind in sorted(required_invalidation_kinds(boundary.kind) - present, key=lambda x: x.value):
        blockers.append(f"E_INVALIDATION_KIND_MISSING:{kind.value}")
    for target in cone.targets:
        if target.status is not InvalidationStatus.COMPLETED:
            blockers.append(
                f"E_INVALIDATION_INCOMPLETE:{target.kind.value}:{target.target_ref}"
            )
    return tuple(sorted(set(blockers)))


def invalidation_complete(
    boundary: BoundaryTrigger,
    cone: InvalidationCone,
) -> bool:
    return not invalidation_blockers(boundary, cone)


def transition_blockers(transition: BoundaryTransition) -> tuple[str, ...]:
    blockers = list(
        invalidation_blockers(transition.boundary, transition.invalidation_cone)
    )
    if transition.predecessor_epoch_id != transition.invalidation_cone.predecessor_epoch_id:
        blockers.append("E_CONE_PREDECESSOR_MISMATCH")
    if transition.rollback.target_epoch_id != transition.predecessor_epoch_id:
        blockers.append("E_ROLLBACK_TARGET_MISMATCH")
    return tuple(sorted(set(blockers)))


def validate_boundary_transition(
    transition: BoundaryTransition,
    predecessor: PredictiveStateEpoch,
    successor: PredictiveStateEpoch | None,
) -> None:
    if transition.predecessor_epoch_id != predecessor.epoch_id:
        raise ContractError(
            "E_TRANSITION_PREDECESSOR_MISMATCH",
            "transition predecessor must match supplied predecessor epoch",
        )
    if transition.boundary.boundary_id != transition.invalidation_cone.boundary_id:
        raise ContractError(
            "E_CONE_BOUNDARY_MISMATCH",
            "invalidation cone must reference the transition boundary",
        )
    if transition.invalidation_cone.predecessor_epoch_id != predecessor.epoch_id:
        raise ContractError(
            "E_CONE_PREDECESSOR_MISMATCH",
            "invalidation cone must reference the predecessor epoch",
        )
    if transition.rollback.target_epoch_id != predecessor.epoch_id:
        raise ContractError(
            "E_ROLLBACK_TARGET_MISMATCH",
            "rollback target must equal predecessor epoch",
        )

    if successor is None:
        if transition.successor_epoch_id is not None:
            raise ContractError(
                "E_SUCCESSOR_NOT_SUPPLIED",
                "transition names a successor but no successor epoch was supplied",
            )
    else:
        if transition.successor_epoch_id != successor.epoch_id:
            raise ContractError(
                "E_TRANSITION_SUCCESSOR_MISMATCH",
                "transition successor must match supplied successor epoch",
            )
        validate_epoch_successor(predecessor, successor)
        boundary_reason = f"boundary:{transition.boundary.kind.value}"
        if boundary_reason not in successor.transition_reasons:
            raise ContractError(
                "E_SUCCESSOR_BOUNDARY_REASON",
                f"successor transition reasons must include {boundary_reason}",
            )

    blockers = transition_blockers(transition)
    if transition.disposition is TransitionDisposition.COMMITTED and blockers:
        raise ContractError(
            "E_TRANSITION_BLOCKED",
            "; ".join(blockers),
        )


def _expect_error(code: str, fn: Any) -> None:
    try:
        fn()
    except ContractError as exc:
        assert exc.code == code, (exc.code, code)
    else:
        raise AssertionError(f"expected ContractError {code}")


def _self_test_fixture() -> tuple[
    BoundaryTransition,
    PredictiveStateEpoch,
    PredictiveStateEpoch,
]:
    def point(value: int) -> TypedTimePoint:
        return TypedTimePoint("pk2-clock", value, 0, "native")

    validity = ValidityContract(
        TypedTimeInterval(point(0), point(1000)),
        "pk2-support",
        ("boundary",),
        "boundary-trigger",
    )
    authority = AuthorityCeiling(
        "pk2-cell",
        ("observe", "propose"),
        ("synthetic-state",),
        point(1000),
    )
    payload0 = {"x": 1, "v": 2}
    header0 = RecordHeader(
        "0.1.0",
        "epoch-0",
        EndpointID.COMMON,
        0,
        RecordStatus.VALID,
        EvidenceStanding.IMPLEMENTED,
        "native-pk2-evidence",
        validity,
        make_lineage(
            payload0,
            producer_id="pk2-self-test",
            transform_id="ROOT",
            implementation_version="0.1.0",
        ),
        authority,
    )
    uncertainty0 = UncertaintyDescriptor(
        UncertaintyKind.BOUNDED_SET,
        2,
        "params:pk2:0",
        SupportStatus.IN_SUPPORT,
        "support:pk2:0",
        "calibration:pk2:0",
    )
    hypothesis0 = HypothesisState(
        "H1",
        "state:H1:0",
        HypothesisLifecycle.ACTIVE,
        SupportStatus.IN_SUPPORT,
        1_000_000,
    )
    predecessor = PredictiveStateEpoch(
        header0,
        "epoch-0",
        "task-pk2",
        None,
        "payload:pk2:0",
        uncertainty0,
        (hypothesis0,),
        ("x", "v"),
        ("x", "v"),
        (),
        ("evidence:x", "evidence:v"),
    )

    payload1 = {"x": 1, "v": None, "support": "outside"}
    header1 = RecordHeader(
        "0.1.0",
        "epoch-1",
        EndpointID.COMMON,
        1,
        RecordStatus.UNKNOWN,
        EvidenceStanding.IMPLEMENTED,
        "native-pk2-evidence",
        validity,
        make_lineage(
            payload1,
            producer_id="pk2-self-test",
            transform_id="support-exit",
            implementation_version="0.1.0",
            input_refs=(header0.record_id,),
        ),
        authority,
    )
    uncertainty1 = UncertaintyDescriptor(
        UncertaintyKind.BOUNDED_SET,
        2,
        "params:pk2:1",
        SupportStatus.OUT_OF_SUPPORT,
        "support:pk2:0",
        "calibration:pk2:0",
        ("support-exit",),
    )
    hypothesis1 = HypothesisState(
        "H1",
        "state:H1:1",
        HypothesisLifecycle.DORMANT,
        SupportStatus.OUT_OF_SUPPORT,
        None,
        ("support-exit",),
    )
    successor = PredictiveStateEpoch(
        header1,
        "epoch-1",
        "task-pk2",
        "epoch-0",
        "payload:pk2:1",
        uncertainty1,
        (hypothesis1,),
        ("x", "v"),
        ("x",),
        ("v",),
        ("evidence:support-monitor",),
        ("boundary:SUPPORT_EXIT", "rank-loss"),
    )

    boundary = BoundaryTrigger(
        "boundary-1",
        BoundaryKind.SUPPORT_EXIT,
        point(500),
        "monitor:support",
        "evidence:support-exit",
        ("v",),
        ("outside-calibrated-support",),
    )
    targets = tuple(
        InvalidationTarget(
            kind,
            f"{kind.value.lower()}:pk2:0",
            InvalidationStatus.COMPLETED,
            ("support-exit",),
            f"evidence:invalidated:{kind.value.lower()}",
        )
        for kind in ArtifactKind
    )
    cone = InvalidationCone(
        boundary.boundary_id,
        predecessor.epoch_id,
        "scope:pk2-fixture",
        "evidence:dependency-discovery",
        targets,
    )
    rollback = RollbackContract(
        predecessor.epoch_id,
        ("successor-invalid", "resource-failure"),
        "plan:restore-predecessor",
        "verifier:independent",
        point(900),
    )
    transition = BoundaryTransition(
        "transition-1",
        boundary,
        predecessor.epoch_id,
        successor.epoch_id,
        cone,
        rollback,
        TransitionDisposition.COMMITTED,
        "evidence:commit-1",
    )
    return transition, predecessor, successor


def self_test() -> None:
    transition, predecessor, successor = _self_test_fixture()
    validate_boundary_transition(transition, predecessor, successor)
    assert invalidation_complete(transition.boundary, transition.invalidation_cone)

    incomplete_target = replace(
        transition.invalidation_cone.targets[0],
        status=InvalidationStatus.REQUIRED,
        evidence_ref=None,
    )
    incomplete_cone = replace(
        transition.invalidation_cone,
        targets=(incomplete_target,) + transition.invalidation_cone.targets[1:],
    )
    _expect_error(
        "E_TRANSITION_BLOCKED",
        lambda: validate_boundary_transition(
            replace(transition, invalidation_cone=incomplete_cone),
            predecessor,
            successor,
        ),
    )

    missing_action = replace(
        transition.invalidation_cone,
        targets=tuple(
            target
            for target in transition.invalidation_cone.targets
            if target.kind is not ArtifactKind.ACTION
        ),
    )
    blockers = invalidation_blockers(transition.boundary, missing_action)
    assert "E_INVALIDATION_KIND_MISSING:ACTION" in blockers

    bad_successor = replace(
        successor,
        transition_reasons=("rank-loss",),
    )
    _expect_error(
        "E_SUCCESSOR_BOUNDARY_REASON",
        lambda: validate_boundary_transition(
            transition,
            predecessor,
            bad_successor,
        ),
    )


if __name__ == "__main__":
    self_test()
    print("CEGA-PK2-min self-test passed")
