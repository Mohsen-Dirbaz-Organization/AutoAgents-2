from dataclasses import replace
import unittest

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
    runtime_eligible_state,
    self_test,
    state_blockers,
    validate_epoch_successor,
)


def point(value: int) -> TypedTimePoint:
    return TypedTimePoint("test-clock", value, 0, "native")


def root_epoch() -> PredictiveStateEpoch:
    payload = {"x": 1, "v": None}
    validity = ValidityContract(
        TypedTimeInterval(point(0), point(100)),
        "support-v1",
        ("expired",),
        "epoch-change",
    )
    header = RecordHeader(
        "0.1.0",
        "epoch-root",
        EndpointID.COMMON,
        0,
        RecordStatus.VALID,
        EvidenceStanding.IMPLEMENTED,
        "native-evidence",
        validity,
        make_lineage(
            payload,
            producer_id="test",
            transform_id="ROOT",
            implementation_version="0.1.0",
        ),
        AuthorityCeiling("cell", ("observe",), ("state",), point(100)),
    )
    return PredictiveStateEpoch(
        header,
        "epoch-root",
        "task",
        None,
        "payload:root",
        UncertaintyDescriptor(
            UncertaintyKind.INTERVAL,
            2,
            "params:interval",
            SupportStatus.IN_SUPPORT,
            "support:v1",
            "calibration:v1",
        ),
        (
            HypothesisState(
                "H1",
                "state:H1",
                HypothesisLifecycle.ACTIVE,
                SupportStatus.IN_SUPPORT,
                1_000_000,
            ),
        ),
        ("x", "v"),
        ("x",),
        ("v",),
        ("evidence:x",),
    )


class PK1MinTests(unittest.TestCase):
    def test_reference_self_test(self) -> None:
        self_test()

    def test_null_decision_direction_blocks_runtime(self) -> None:
        epoch = root_epoch()
        self.assertFalse(runtime_eligible_state(epoch, point(50)))
        self.assertIn("E_DECISION_NULL_DIRECTION:v", state_blockers(epoch, point(50)))

    def test_partial_active_weights_are_rejected(self) -> None:
        epoch = root_epoch()
        with self.assertRaises(ContractError) as caught:
            replace(
                epoch,
                hypotheses=(
                    epoch.hypotheses[0],
                    HypothesisState(
                        "H2",
                        "state:H2",
                        HypothesisLifecycle.ACTIVE,
                        SupportStatus.IN_SUPPORT,
                        None,
                    ),
                ),
            )
        self.assertEqual(caught.exception.code, "E_HYPOTHESIS_PARTIAL_WEIGHTS")

    def test_out_of_support_state_is_explicitly_blocked(self) -> None:
        epoch = root_epoch()
        out = replace(
            epoch,
            uncertainty=UncertaintyDescriptor(
                UncertaintyKind.INTERVAL,
                2,
                "params:interval",
                SupportStatus.OUT_OF_SUPPORT,
                "support:v1",
                "calibration:v1",
                ("novel-regime",),
            ),
        )
        self.assertIn("E_STATE_OUT_OF_SUPPORT", state_blockers(out, point(50)))

    def test_unrecorded_rank_loss_is_rejected(self) -> None:
        parent = root_epoch()
        payload = {"x": None, "v": None}
        header = RecordHeader(
            "0.1.0",
            "epoch-child",
            EndpointID.COMMON,
            1,
            RecordStatus.VALID,
            EvidenceStanding.IMPLEMENTED,
            "native-evidence",
            parent.header.validity,
            make_lineage(
                payload,
                producer_id="test",
                transform_id="loss",
                implementation_version="0.1.0",
                input_refs=(parent.header.record_id,),
            ),
            parent.header.authority_ceiling,
        )
        child = PredictiveStateEpoch(
            header,
            "epoch-child",
            "task",
            "epoch-root",
            "payload:child",
            parent.uncertainty,
            parent.hypotheses,
            parent.decision_relevant_dimensions,
            (),
            ("x", "v"),
            ("evidence:rank",),
            ("boundary",),
        )
        with self.assertRaises(ContractError) as caught:
            validate_epoch_successor(parent, child)
        self.assertEqual(caught.exception.code, "E_UNRECORDED_RANK_LOSS")


if __name__ == "__main__":
    unittest.main()
