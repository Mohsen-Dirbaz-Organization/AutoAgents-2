import unittest

from cega.pk0 import (
    ContractError,
    TimeOrder,
    TypedTimePoint,
    canonical_json,
    compare_time_points,
    payload_digest,
    self_test,
)


class PK0Tests(unittest.TestCase):
    def test_reference_self_test(self) -> None:
        self_test()

    def test_canonical_key_order_and_set_order(self) -> None:
        self.assertEqual(canonical_json({"b": 2, "a": 1}), b'{"a":1,"b":2}')
        self.assertEqual(
            payload_digest({"values": {"beta", "alpha"}}),
            payload_digest({"values": {"alpha", "beta"}}),
        )

    def test_nonfinite_json_is_rejected(self) -> None:
        with self.assertRaises(ContractError) as caught:
            canonical_json(float("nan"))
        self.assertEqual(caught.exception.code, "E_CANONICAL_VALUE")

    def test_incompatible_clocks_remain_indeterminate(self) -> None:
        left = TypedTimePoint("clock-a", 10, 0, "native")
        right = TypedTimePoint("clock-b", 10, 0, "native")
        self.assertIs(compare_time_points(left, right), TimeOrder.INDETERMINATE)


if __name__ == "__main__":
    unittest.main()
