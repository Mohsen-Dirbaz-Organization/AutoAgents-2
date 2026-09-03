import unittest

from cega.pk0 import ContractError, TypedTimePoint
from cega.pk5_min import (
    Reading,
    ReadingStanding,
    ResourceDelta,
    RunTrace,
    StageStatus,
    StageTrace,
    self_test,
)


def point(value: int) -> TypedTimePoint:
    return TypedTimePoint("trace-clock", value, 0, "native")


def resources(*, complete: bool = False) -> ResourceDelta:
    unavailable = Reading.unavailable
    return ResourceDelta(
        operations=(),
        operation_coverage_complete=complete,
        resident_bytes=unavailable("bytes"),
        bytes_read=unavailable("bytes"),
        bytes_written=unavailable("bytes"),
        network_bytes=unavailable("bytes"),
        sync_events=unavailable("events"),
        messages=unavailable("messages"),
        queue_wait_ns=unavailable("ns"),
        energy_uj=unavailable("uJ"),
        temperature_start_mc=unavailable("mC"),
        temperature_end_mc=unavailable("mC"),
        valid_result_count=Reading(0, "results", ReadingStanding.SYNTHETIC, "test"),
    )


class PK5MinTests(unittest.TestCase):
    def test_reference_self_test(self) -> None:
        self_test()

    def test_parallel_stages_do_not_sum_as_serial(self) -> None:
        stages = (
            StageTrace("A", (), point(0), point(10), StageStatus.SUCCESS, resources(), ""),
            StageTrace("B", ("A",), point(10), point(30), StageStatus.SUCCESS, resources(), ""),
            StageTrace("C", ("A",), point(10), point(20), StageStatus.SUCCESS, resources(), ""),
            StageTrace("D", ("B", "C"), point(30), point(35), StageStatus.SUCCESS, resources(), ""),
        )
        trace = RunTrace("parallel", "test", 100, 0, stages)
        self.assertEqual(trace.critical_span_upper_ns(), 35)
        self.assertEqual(trace.wall_bounds_ns(), (35, 35))

    def test_missing_parent_is_rejected(self) -> None:
        with self.assertRaises(ContractError) as caught:
            RunTrace(
                "missing-parent",
                "test",
                10,
                0,
                (StageTrace("A", ("MISSING",), point(0), point(1), StageStatus.SUCCESS, resources(), ""),),
            )
        self.assertEqual(caught.exception.code, "E_STAGE_PARENT_MISSING")

    def test_unavailable_reading_cannot_carry_zero(self) -> None:
        with self.assertRaises(ContractError) as caught:
            Reading(0, "uJ", ReadingStanding.UNAVAILABLE, "UNAVAILABLE")
        self.assertEqual(caught.exception.code, "E_READING_UNAVAILABLE")


if __name__ == "__main__":
    unittest.main()
