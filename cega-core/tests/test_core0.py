from dataclasses import replace
import unittest

from cega.core0 import execute_core0
from cega.fixture import FixtureMode, build_fixture, execute_fixture, run_campaign
from cega.pk7_min import GateAction, GateContext


class Core0Tests(unittest.TestCase):
    def test_nominal_transaction_is_deterministic_and_non_actuating(self) -> None:
        first = execute_fixture(FixtureMode.NOMINAL)
        second = execute_fixture(FixtureMode.NOMINAL)
        self.assertIs(first.final_action, GateAction.ADMIT)
        self.assertEqual(first.replay_digest, second.replay_digest)
        self.assertFalse(first.outcome["physical_actuation"])
        self.assertEqual(
            [stage.stage_id for stage in first.response_trace.stages],
            [
                "0-CONDITION",
                "1-ADMIT",
                "2-TRANSPORT",
                "3-UPDATE",
                "4-PROPOSE",
                "5-VERIFY",
                "6-EMIT",
            ],
        )
        self.assertEqual(len(first.full_trace.stages), 10)
        self.assertIsNone(first.full_trace.sum_reading("energy_uj"))
        self.assertIsNone(first.full_trace.sum_reading("temperature_start_mc"))

    def test_eight_discriminating_mutations(self) -> None:
        observations = {item["case"]: item for item in run_campaign()}
        self.assertEqual(observations["digest_mismatch"]["actual"], "REFUSE")
        self.assertIn("E_DIGEST_MISMATCH", observations["digest_mismatch"]["reason_codes"])
        self.assertEqual(observations["expired_input"]["actual"], "REFUSE")
        self.assertIn("E_RUNTIME_INELIGIBLE", observations["expired_input"]["reason_codes"])
        self.assertEqual(observations["unknown_model_support"]["actual"], "HOLD")
        self.assertIn("OUTSIDE_SUPPORT", observations["unknown_model_support"]["reason_codes"])
        self.assertEqual(observations["failed_safety"]["actual"], "FALLBACK")
        self.assertIn("UNSAFE", observations["failed_safety"]["reason_codes"])
        self.assertEqual(observations["self_verification"]["actual"], "FALLBACK")
        self.assertIn("E_SELF_VERIFICATION", observations["self_verification"]["reason_codes"])
        self.assertEqual(observations["authority_expansion"]["actual"], "HOLD")
        self.assertIn("E_EXTERNAL_CHECKPOINT_REQUIRED", observations["authority_expansion"]["reason_codes"])
        self.assertEqual(observations["response_deadline_miss"]["actual"], "FALLBACK")
        self.assertIn("E_RESPONSE_DEADLINE_MISS", observations["response_deadline_miss"]["reason_codes"])
        self.assertEqual(observations["replay_mutation"]["actual"], "E_REPLAY_DIGEST_MISMATCH")

    def test_deadline_miss_without_fallback_refuses(self) -> None:
        tx, adapter, clock = build_fixture(FixtureMode.DEADLINE_MISS)
        tx = replace(
            tx,
            gate_context=GateContext(tx.gate_context.current_ceiling, tx.gate_context.verifier_id, False),
        )
        result = execute_core0(tx, adapter, clock_ns=clock, clock_id="fixture-clock")
        self.assertIs(result.final_action, GateAction.REFUSE)
        self.assertIn("E_RESPONSE_DEADLINE_MISS", result.reason_codes)


if __name__ == "__main__":
    unittest.main()
