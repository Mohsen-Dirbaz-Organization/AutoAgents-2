from dataclasses import replace
import unittest

from cega.pk0 import ContractError
from cega.pk1_min import SupportStatus
from cega.pk3_min import (
    AssumptionStanding,
    CriterionStanding,
    RationalMagnitude,
    ResidualStanding,
    _self_test_fixture,
    classify_residual,
    classify_support,
    model_evaluation_blockers,
    self_test,
)


class PK3MinTests(unittest.TestCase):
    def test_reference_self_test(self) -> None:
        self_test()

    def test_failed_support_precedes_unknown(self) -> None:
        model, assessment, _, _ = _self_test_fixture()
        unknown = replace(
            assessment.criteria[0],
            standing=CriterionStanding.UNKNOWN,
            reason_codes=("monitor-unavailable",),
        )
        failed = replace(
            assessment.criteria[1],
            standing=CriterionStanding.FAIL,
            reason_codes=("outside-range",),
        )
        mixed = replace(assessment, criteria=(unknown, failed))
        self.assertIs(classify_support(mixed), SupportStatus.OUT_OF_SUPPORT)
        self.assertEqual(mixed.model_id, model.model_id)

    def test_residual_uses_exact_rational_comparison(self) -> None:
        model, _, residual, _ = _self_test_fixture()
        equal = replace(residual, value=RationalMagnitude(2, 20, "m"))
        high = replace(residual, value=RationalMagnitude(3, 20, "m"))
        self.assertIs(
            classify_residual(model.residual_contract, equal),
            ResidualStanding.WITHIN_BOUND,
        )
        self.assertIs(
            classify_residual(model.residual_contract, high),
            ResidualStanding.EXCEEDS_BOUND,
        )

    def test_unit_mismatch_is_not_coerced(self) -> None:
        with self.assertRaises(ContractError) as caught:
            RationalMagnitude(1, 2, "m").compare(RationalMagnitude(1, 2, "s"))
        self.assertEqual(caught.exception.code, "E_MAGNITUDE_UNIT_MISMATCH")

    def test_provisional_assumption_blocks_evaluation(self) -> None:
        model, assessment, residual, now = _self_test_fixture()
        provisional = replace(
            model.assumptions[0],
            standing=AssumptionStanding.PROVISIONAL,
            evidence_ref=None,
            reason_codes=("not-yet-tested",),
        )
        blockers = model_evaluation_blockers(
            replace(model, assumptions=(provisional,)),
            assessment,
            residual,
            now,
        )
        self.assertIn("E_MODEL_ASSUMPTION_PROVISIONAL:A1", blockers)


if __name__ == "__main__":
    unittest.main()
