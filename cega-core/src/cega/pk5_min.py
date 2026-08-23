from __future__ import annotations

from collections import defaultdict, deque
from dataclasses import dataclass
from enum import Enum

from cega.pk0 import ContractError, TypedTimePoint, clocks_compatible


class ReadingStanding(str, Enum):
    MEASURED = "MEASURED"
    ESTIMATED = "ESTIMATED"
    SYNTHETIC = "SYNTHETIC"
    UNAVAILABLE = "UNAVAILABLE"


class StageStatus(str, Enum):
    SUCCESS = "SUCCESS"
    FAILED = "FAILED"
    UNKNOWN = "UNKNOWN"
    EXPIRED = "EXPIRED"
    FALLBACK = "FALLBACK"
    CANCELLED = "CANCELLED"


def _nonblank(value: str, code: str, name: str) -> None:
    if not isinstance(value, str) or not value.strip():
        raise ContractError(code, f"{name} must be nonblank")


@dataclass(frozen=True)
class Reading:
    value: int | None
    unit: str
    standing: ReadingStanding
    source_ref: str

    def __post_init__(self) -> None:
        _nonblank(self.unit, "E_READING_UNIT", "unit")
        if self.standing is ReadingStanding.UNAVAILABLE:
            if self.value is not None:
                raise ContractError("E_READING_UNAVAILABLE", "UNAVAILABLE readings must have value=None")
            return
        if type(self.value) is not int:
            raise ContractError("E_READING_VALUE", "available value must be int")
        _nonblank(self.source_ref, "E_READING_SOURCE", "source_ref")

    @classmethod
    def unavailable(cls, unit: str) -> "Reading":
        return cls(None, unit, ReadingStanding.UNAVAILABLE, "UNAVAILABLE")


def _nonnegative(reading: Reading, name: str) -> None:
    if reading.value is not None and reading.value < 0:
        raise ContractError("E_NEGATIVE_COUNTER", f"{name} must be >= 0")


@dataclass(frozen=True)
class OperationCount:
    operation_class: str
    count: int
    standing: ReadingStanding
    source_ref: str

    def __post_init__(self) -> None:
        _nonblank(self.operation_class, "E_OPERATION_CLASS", "operation_class")
        if type(self.count) is not int or self.count < 0:
            raise ContractError("E_OPERATION_COUNT", "count must be int >= 0")
        if self.standing is ReadingStanding.UNAVAILABLE:
            raise ContractError(
                "E_OPERATION_UNAVAILABLE",
                "omit an unavailable operation class instead of assigning a count",
            )
        _nonblank(self.source_ref, "E_OPERATION_SOURCE", "source_ref")


@dataclass(frozen=True)
class ResourceDelta:
    operations: tuple[OperationCount, ...]
    operation_coverage_complete: bool
    resident_bytes: Reading
    bytes_read: Reading
    bytes_written: Reading
    network_bytes: Reading
    sync_events: Reading
    messages: Reading
    queue_wait_ns: Reading
    energy_uj: Reading
    temperature_start_mc: Reading
    temperature_end_mc: Reading
    valid_result_count: Reading

    def __post_init__(self) -> None:
        names = [item.operation_class for item in self.operations]
        if len(names) != len(set(names)):
            raise ContractError("E_OPERATION_DUPLICATE", "operation classes must be unique per stage")
        for name in (
            "resident_bytes",
            "bytes_read",
            "bytes_written",
            "network_bytes",
            "sync_events",
            "messages",
            "queue_wait_ns",
            "energy_uj",
            "valid_result_count",
        ):
            _nonnegative(getattr(self, name), name)


@dataclass(frozen=True)
class StageTrace:
    stage_id: str
    parent_stage_ids: tuple[str, ...]
    start: TypedTimePoint
    end: TypedTimePoint
    status: StageStatus
    resources: ResourceDelta
    reason_code: str

    def __post_init__(self) -> None:
        _nonblank(self.stage_id, "E_STAGE_ID", "stage_id")
        if self.stage_id in self.parent_stage_ids:
            raise ContractError("E_STAGE_SELF_PARENT", "stage cannot parent itself")
        if len(set(self.parent_stage_ids)) != len(self.parent_stage_ids):
            raise ContractError("E_STAGE_PARENT_DUPLICATE", "parents must be unique")
        if not clocks_compatible(self.start, self.end):
            raise ContractError("E_STAGE_CLOCK", "stage endpoints require one transported clock")
        if self.start.value_ns > self.end.value_ns:
            raise ContractError("E_STAGE_ORDER", "stage start exceeds stage end")
        if self.status is not StageStatus.SUCCESS:
            _nonblank(self.reason_code, "E_STAGE_REASON", "reason_code")

    def duration_bounds_ns(self) -> tuple[int, int]:
        lower = max(0, self.end.lower_ns - self.start.upper_ns)
        upper = max(0, self.end.upper_ns - self.start.lower_ns)
        return lower, upper


def _topological(stages: tuple[StageTrace, ...]) -> tuple[str, ...]:
    by_id = {stage.stage_id: stage for stage in stages}
    if len(by_id) != len(stages):
        raise ContractError("E_STAGE_DUPLICATE", "stage_id values must be unique")
    children: dict[str, list[str]] = defaultdict(list)
    indegree = {stage.stage_id: 0 for stage in stages}
    for stage in stages:
        for parent in stage.parent_stage_ids:
            if parent not in by_id:
                raise ContractError("E_STAGE_PARENT_MISSING", f"missing parent {parent}")
            children[parent].append(stage.stage_id)
            indegree[stage.stage_id] += 1
    queue = deque(sorted(stage_id for stage_id, degree in indegree.items() if degree == 0))
    order: list[str] = []
    while queue:
        current = queue.popleft()
        order.append(current)
        for child in sorted(children[current]):
            indegree[child] -= 1
            if indegree[child] == 0:
                queue.append(child)
    if len(order) != len(stages):
        raise ContractError("E_STAGE_CYCLE", "stage dependency graph is cyclic")
    return tuple(order)


@dataclass(frozen=True)
class RunTrace:
    run_id: str
    profile_id: str
    deadline_ns: int
    protected_reserve_ns: int
    stages: tuple[StageTrace, ...]

    def __post_init__(self) -> None:
        _nonblank(self.run_id, "E_RUN_ID", "run_id")
        _nonblank(self.profile_id, "E_PROFILE_ID", "profile_id")
        if type(self.deadline_ns) is not int or self.deadline_ns <= 0:
            raise ContractError("E_DEADLINE", "deadline_ns must be int > 0")
        if (
            type(self.protected_reserve_ns) is not int
            or self.protected_reserve_ns < 0
            or self.protected_reserve_ns > self.deadline_ns
        ):
            raise ContractError("E_RESERVE", "reserve must lie in [0, deadline]")
        if not self.stages:
            raise ContractError("E_EMPTY_TRACE", "a run requires at least one stage")
        _topological(self.stages)
        clock = (self.stages[0].start.clock_domain, self.stages[0].start.sync_model_id)
        if any(
            (stage.start.clock_domain, stage.start.sync_model_id) != clock
            or (stage.end.clock_domain, stage.end.sync_model_id) != clock
            for stage in self.stages
        ):
            raise ContractError("E_RUN_CLOCK", "all stages must be transported to one run clock")

    def critical_span_upper_ns(self) -> int:
        order = _topological(self.stages)
        by_id = {stage.stage_id: stage for stage in self.stages}
        longest: dict[str, int] = {}
        for stage_id in order:
            stage = by_id[stage_id]
            parent_span = max((longest[parent] for parent in stage.parent_stage_ids), default=0)
            longest[stage_id] = parent_span + stage.duration_bounds_ns()[1]
        return max(longest.values())

    def wall_bounds_ns(self) -> tuple[int, int]:
        earliest = min((stage.start for stage in self.stages), key=lambda point: point.lower_ns)
        latest = max((stage.end for stage in self.stages), key=lambda point: point.upper_ns)
        return (
            max(0, latest.lower_ns - earliest.upper_ns),
            max(0, latest.upper_ns - earliest.lower_ns),
        )

    def deadline_margin_upper_ns(self) -> int:
        usable = self.deadline_ns - self.protected_reserve_ns
        return usable - self.critical_span_upper_ns()

    def total_operation_counts(self) -> dict[str, int] | None:
        if any(not stage.resources.operation_coverage_complete for stage in self.stages):
            return None
        result: dict[str, int] = defaultdict(int)
        for stage in self.stages:
            for operation in stage.resources.operations:
                result[operation.operation_class] += operation.count
        return dict(sorted(result.items()))

    def sum_reading(self, field: str) -> int | None:
        readings = [getattr(stage.resources, field) for stage in self.stages]
        if any(reading.value is None for reading in readings):
            return None
        return sum(int(reading.value) for reading in readings)

    def energy_per_valid_result_uj(self) -> float | None:
        energy = self.sum_reading("energy_uj")
        valid = self.sum_reading("valid_result_count")
        if energy is None or valid is None or valid == 0:
            return None
        return energy / valid


def _measured(value: int, unit: str) -> Reading:
    return Reading(value, unit, ReadingStanding.MEASURED, "self-test")


def _resources(energy: Reading) -> ResourceDelta:
    return ResourceDelta(
        operations=(OperationCount("scalar", 10, ReadingStanding.MEASURED, "self-test"),),
        operation_coverage_complete=True,
        resident_bytes=_measured(1024, "bytes"),
        bytes_read=_measured(128, "bytes"),
        bytes_written=_measured(64, "bytes"),
        network_bytes=_measured(0, "bytes"),
        sync_events=_measured(1, "events"),
        messages=_measured(0, "messages"),
        queue_wait_ns=_measured(0, "ns"),
        energy_uj=energy,
        temperature_start_mc=_measured(40000, "mC"),
        temperature_end_mc=_measured(40100, "mC"),
        valid_result_count=_measured(1, "results"),
    )


def self_test() -> None:
    def point(value: int) -> TypedTimePoint:
        return TypedTimePoint("mono-test", value, 0, "native")

    energy = _measured(2, "uJ")
    stages = (
        StageTrace("A", (), point(0), point(10), StageStatus.SUCCESS, _resources(energy), ""),
        StageTrace("B", ("A",), point(10), point(30), StageStatus.SUCCESS, _resources(energy), ""),
        StageTrace("C", ("A",), point(10), point(20), StageStatus.SUCCESS, _resources(energy), ""),
        StageTrace("D", ("B", "C"), point(30), point(35), StageStatus.SUCCESS, _resources(energy), ""),
    )
    run = RunTrace("run-1", "fixture", 100, 10, stages)
    assert run.critical_span_upper_ns() == 35
    assert run.wall_bounds_ns() == (35, 35)
    assert run.deadline_margin_upper_ns() == 55
    assert run.total_operation_counts() == {"scalar": 40}
    assert run.energy_per_valid_result_uj() == 2.0

    unavailable = StageTrace(
        "U",
        (),
        point(0),
        point(1),
        StageStatus.SUCCESS,
        _resources(Reading.unavailable("uJ")),
        "",
    )
    assert RunTrace("run-u", "fixture", 10, 0, (unavailable,)).energy_per_valid_result_uj() is None

    try:
        RunTrace(
            "cycle",
            "fixture",
            10,
            0,
            (
                StageTrace("X", ("Y",), point(0), point(1), StageStatus.SUCCESS, _resources(energy), ""),
                StageTrace("Y", ("X",), point(1), point(2), StageStatus.SUCCESS, _resources(energy), ""),
            ),
        )
    except ContractError as exc:
        assert exc.code == "E_STAGE_CYCLE"
    else:
        raise AssertionError("cycle was not rejected")


if __name__ == "__main__":
    self_test()
    print("CEGA-PK5-min self-test passed")
