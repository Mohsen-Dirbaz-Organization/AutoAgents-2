import unittest

from cega.pk0 import AuthorityCeiling, TypedTimeInterval, TypedTimePoint, ValidityContract
from cega.pk7_min import (
    ActionCandidate,
    GateAction,
    GateContext,
    GatePredicate,
    PredicateClass,
    PredicateStatus,
    evaluate_gate,
    self_test,
)


def point(value: int) -> TypedTimePoint:
    return TypedTimePoint("gate-clock", value, 0, "native")


def fixture() -> tuple[ActionCandidate, GateContext, ValidityContract]:
    validity = ValidityContract(
        TypedTimeInterval(point(0), point(100)),
        "test-support",
        ("expired",),
        "epoch-change",
    )
    current = AuthorityCeiling("cell", ("observe", "propose"), ("object",), point(100))
    requested = AuthorityCeiling("cell", ("observe",), ("object",), point(90))
    candidate = ActionCandidate("action", "COMMON", "observe", "object", "proposer", requested)
    return candidate, GateContext(current, "verifier", True), validity


class PK7MinTests(unittest.TestCase):
    def test_reference_self_test(self) -> None:
        self_test()

    def test_absent_required_predicates_hold(self) -> None:
        candidate, context, _ = fixture()
        decision = evaluate_gate(candidate, (), context, point(50))
        self.assertIs(decision.action, GateAction.HOLD)
        self.assertIn("E_NO_REQUIRED_PREDICATES", decision.reason_codes)

    def test_required_failure_without_fallback_refuses(self) -> None:
        candidate, context, validity = fixture()
        failed = GatePredicate(
            "safety",
            PredicateClass.ENDPOINT_SAFETY,
            PredicateStatus.FAIL,
            True,
            ("evidence:safety",),
            ("UNSAFE",),
            validity,
            "safety-evaluator",
        )
        no_fallback = GateContext(context.current_ceiling, context.verifier_id, False)
        decision = evaluate_gate(candidate, (failed,), no_fallback, point(50))
        self.assertIs(decision.action, GateAction.REFUSE)
        self.assertIn("UNSAFE", decision.reason_codes)


if __name__ == "__main__":
    unittest.main()
