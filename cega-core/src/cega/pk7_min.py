from __future__ import annotations

from dataclasses import dataclass
from enum import Enum

from cega.pk0 import (
    AuthorityCeiling,
    AuthorityTransition,
    ContractError,
    TypedTimePoint,
    ValidityContract,
    ValidityState,
    classify_authority_transition,
    classify_validity,
)


class PredicateClass(str, Enum):
    EVIDENCE = "EVIDENCE"
    TEMPORAL = "TEMPORAL"
    MODEL_SUPPORT = "MODEL_SUPPORT"
    INVARIANT = "INVARIANT"
    PHYSICAL_VIABILITY = "PHYSICAL_VIABILITY"
    DEADLINE = "DEADLINE"
    RESOURCE = "RESOURCE"
    ENDPOINT_SAFETY = "ENDPOINT_SAFETY"
    AUTHORITY = "AUTHORITY"
    RECOVERY = "RECOVERY"


class PredicateStatus(str, Enum):
    PASS = "PASS"
    FAIL = "FAIL"
    UNKNOWN = "UNKNOWN"
    EXPIRED = "EXPIRED"


class GateAction(str, Enum):
    ADMIT = "ADMIT"
    HOLD = "HOLD"
    FALLBACK = "FALLBACK"
    REFUSE = "REFUSE"


def _nonblank(value: str, code: str, name: str) -> None:
    if not isinstance(value, str) or not value.strip():
        raise ContractError(code, f"{name} must be nonblank")


@dataclass(frozen=True)
class GatePredicate:
    predicate_id: str
    predicate_class: PredicateClass
    status: PredicateStatus
    required: bool
    evidence_refs: tuple[str, ...]
    reason_codes: tuple[str, ...]
    validity: ValidityContract
    evaluator_id: str

    def __post_init__(self) -> None:
        _nonblank(self.predicate_id, "E_PREDICATE_ID", "predicate_id")
        _nonblank(self.evaluator_id, "E_PREDICATE_EVALUATOR", "evaluator_id")
        if len(set(self.evidence_refs)) != len(self.evidence_refs):
            raise ContractError("E_PREDICATE_EVIDENCE_DUPLICATE", "evidence_refs must be unique")
        if any(not isinstance(item, str) or not item.strip() for item in self.evidence_refs):
            raise ContractError("E_PREDICATE_EVIDENCE", "evidence_refs must be nonblank")
        if self.status is not PredicateStatus.PASS and not self.reason_codes:
            raise ContractError("E_PREDICATE_REASON", "non-PASS predicates require reason_codes")


@dataclass(frozen=True)
class ActionCandidate:
    action_id: str
    endpoint_id: str
    verb: str
    object_ref: str
    proposer_id: str
    requested_ceiling: AuthorityCeiling

    def __post_init__(self) -> None:
        for name in ("action_id", "endpoint_id", "verb", "object_ref", "proposer_id"):
            _nonblank(getattr(self, name), "E_ACTION_FIELD", name)


@dataclass(frozen=True)
class GateContext:
    current_ceiling: AuthorityCeiling
    verifier_id: str
    fallback_available: bool

    def __post_init__(self) -> None:
        _nonblank(self.verifier_id, "E_VERIFIER_ID", "verifier_id")


@dataclass(frozen=True)
class GateDecision:
    action: GateAction
    failed_predicate_ids: tuple[str, ...]
    unknown_predicate_ids: tuple[str, ...]
    expired_predicate_ids: tuple[str, ...]
    reason_codes: tuple[str, ...]
    authority_transition: AuthorityTransition


def effective_predicate_status(predicate: GatePredicate, now: TypedTimePoint) -> PredicateStatus:
    validity = classify_validity(predicate.validity, now)
    if validity is ValidityState.EXPIRED:
        return PredicateStatus.EXPIRED
    if validity is not ValidityState.VALID:
        return PredicateStatus.UNKNOWN
    return predicate.status


def evaluate_gate(
    candidate: ActionCandidate,
    predicates: tuple[GatePredicate, ...],
    context: GateContext,
    now: TypedTimePoint,
) -> GateDecision:
    required = tuple(predicate for predicate in predicates if predicate.required)
    if len({predicate.predicate_id for predicate in predicates}) != len(predicates):
        raise ContractError("E_PREDICATE_DUPLICATE", "predicate IDs must be unique")

    authority = classify_authority_transition(context.current_ceiling, candidate.requested_ceiling)
    failed: list[str] = []
    unknown: list[str] = []
    expired: list[str] = []
    reasons: list[str] = []

    if not required:
        unknown.append("__NO_REQUIRED_PREDICATES__")
        reasons.append("E_NO_REQUIRED_PREDICATES")
    if candidate.proposer_id == context.verifier_id:
        failed.append("__INDEPENDENCE__")
        reasons.append("E_SELF_VERIFICATION")
    if candidate.verb not in candidate.requested_ceiling.verbs:
        failed.append("__VERB_SCOPE__")
        reasons.append("E_VERB_NOT_GRANTED")
    if candidate.object_ref not in candidate.requested_ceiling.object_scope:
        failed.append("__OBJECT_SCOPE__")
        reasons.append("E_OBJECT_NOT_GRANTED")
    if authority is not AuthorityTransition.NON_EXPANDING:
        unknown.append("__AUTHORITY_EXPANSION__")
        reasons.append("E_EXTERNAL_CHECKPOINT_REQUIRED")

    for predicate in required:
        status = effective_predicate_status(predicate, now)
        if status is PredicateStatus.FAIL:
            failed.append(predicate.predicate_id)
            reasons.extend(predicate.reason_codes)
        elif status is PredicateStatus.UNKNOWN:
            unknown.append(predicate.predicate_id)
            reasons.extend(predicate.reason_codes or ("E_PREDICATE_UNKNOWN",))
        elif status is PredicateStatus.EXPIRED:
            expired.append(predicate.predicate_id)
            reasons.extend(predicate.reason_codes or ("E_PREDICATE_EXPIRED",))

    if failed or expired:
        action = GateAction.FALLBACK if context.fallback_available else GateAction.REFUSE
    elif unknown:
        action = GateAction.HOLD
    else:
        action = GateAction.ADMIT

    return GateDecision(
        action=action,
        failed_predicate_ids=tuple(sorted(set(failed))),
        unknown_predicate_ids=tuple(sorted(set(unknown))),
        expired_predicate_ids=tuple(sorted(set(expired))),
        reason_codes=tuple(sorted(set(reasons))),
        authority_transition=authority,
    )


def self_test() -> None:
    from cega.pk0 import TypedTimeInterval

    def point(value: int) -> TypedTimePoint:
        return TypedTimePoint("mono-test", value, 0, "native")

    validity = ValidityContract(TypedTimeInterval(point(0), point(100)), "fixture-support", ("timeout",), "epoch-change")
    current = AuthorityCeiling("cell-1", ("observe", "propose"), ("fixture",), point(100))
    contracted = AuthorityCeiling("cell-1", ("observe",), ("fixture",), point(90))
    candidate = ActionCandidate("action-1", "COMMON", "observe", "fixture", "proposer", contracted)
    context = GateContext(current, "independent-verifier", True)
    passed = GatePredicate(
        "P1",
        PredicateClass.EVIDENCE,
        PredicateStatus.PASS,
        True,
        ("evidence-1",),
        (),
        validity,
        "evaluator-1",
    )
    assert evaluate_gate(candidate, (passed,), context, point(50)).action is GateAction.ADMIT

    unknown = GatePredicate(
        "P2",
        PredicateClass.MODEL_SUPPORT,
        PredicateStatus.UNKNOWN,
        True,
        ("support-1",),
        ("OUTSIDE_SUPPORT",),
        validity,
        "evaluator-2",
    )
    assert evaluate_gate(candidate, (passed, unknown), context, point(50)).action is GateAction.HOLD

    failed = GatePredicate(
        "P3",
        PredicateClass.ENDPOINT_SAFETY,
        PredicateStatus.FAIL,
        True,
        ("safety-1",),
        ("UNSAFE",),
        validity,
        "evaluator-3",
    )
    assert evaluate_gate(candidate, (passed, failed), context, point(50)).action is GateAction.FALLBACK
    assert evaluate_gate(candidate, (passed,), GateContext(current, "proposer", True), point(50)).action is GateAction.FALLBACK

    expansion = AuthorityCeiling("cell-1", ("observe", "propose", "execute"), ("fixture",), point(100))
    expanded = ActionCandidate("action-2", "COMMON", "observe", "fixture", "proposer", expansion)
    decision = evaluate_gate(expanded, (passed,), context, point(50))
    assert decision.action is GateAction.HOLD
    assert decision.authority_transition is AuthorityTransition.REQUIRES_EXTERNAL_CERTIFIED_CHECKPOINT
    assert evaluate_gate(candidate, (passed,), context, point(150)).action is GateAction.FALLBACK


if __name__ == "__main__":
    self_test()
    print("CEGA-PK7-min self-test passed")
