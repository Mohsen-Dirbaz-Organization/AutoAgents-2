from dataclasses import replace
import unittest

from cega.pk0 import ContractError
from cega.pk2_min import (
    ArtifactKind,
    BoundaryKind,
    InvalidationStatus,
    InvalidationTarget,
    TransitionDisposition,
    _self_test_fixture,
    invalidation_blockers,
    required_invalidation_kinds,
    self_test,
    validate_boundary_transition,
)


class PK2MinTests(unittest.TestCase):
    def test_reference_self_test(self) -> None:
        self_test()

    def test_support_exit_requires_every_artifact_class(self) -> None:
        self.assertEqual(
            required_invalidation_kinds(BoundaryKind.SUPPORT_EXIT),
            frozenset(ArtifactKind),
        )

    def test_completed_target_requires_evidence(self) -> None:
        with self.assertRaises(ContractError) as caught:
            InvalidationTarget(
                ArtifactKind.STATE,
                "state:1",
                InvalidationStatus.COMPLETED,
                ("boundary",),
                None,
            )
        self.assertEqual(caught.exception.code, "E_INVALIDATION_EVIDENCE")

    def test_committed_transition_cannot_hide_pending_invalidation(self) -> None:
        transition, predecessor, successor = _self_test_fixture()
        pending = replace(
            transition.invalidation_cone.targets[0],
            status=InvalidationStatus.REQUIRED,
            evidence_ref=None,
        )
        cone = replace(
            transition.invalidation_cone,
            targets=(pending,) + transition.invalidation_cone.targets[1:],
        )
        blockers = invalidation_blockers(transition.boundary, cone)
        self.assertTrue(any(item.startswith("E_INVALIDATION_INCOMPLETE") for item in blockers))
        with self.assertRaises(ContractError) as caught:
            validate_boundary_transition(
                replace(transition, invalidation_cone=cone),
                predecessor,
                successor,
            )
        self.assertEqual(caught.exception.code, "E_TRANSITION_BLOCKED")

    def test_refusal_cannot_name_successor(self) -> None:
        transition, _, _ = _self_test_fixture()
        with self.assertRaises(ContractError) as caught:
            replace(
                transition,
                disposition=TransitionDisposition.REFUSED,
                decision_evidence_ref=None,
                reason_codes=("unsafe",),
            )
        self.assertEqual(caught.exception.code, "E_REFUSED_SUCCESSOR")


if __name__ == "__main__":
    unittest.main()
