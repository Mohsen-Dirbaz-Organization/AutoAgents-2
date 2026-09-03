from __future__ import annotations

from dataclasses import dataclass, fields, is_dataclass
from enum import Enum
from hashlib import sha256
import json
import re
from typing import Any, Mapping


SEMVER_RE = re.compile(r"^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-[0-9A-Za-z.-]+)?$")
SHA256_RE = re.compile(r"^sha256:[0-9a-f]{64}$")


class ContractError(ValueError):
    def __init__(self, code: str, message: str) -> None:
        super().__init__(f"{code}: {message}")
        self.code = code


class EndpointID(str, Enum):
    COMMON = "COMMON"
    AV_EVENT = "AV_EVENT"
    AV_FRAME = "AV_FRAME"
    CFNI_BENCH = "CFNI_BENCH"


class RecordStatus(str, Enum):
    VALID = "VALID"
    INVALID = "INVALID"
    UNKNOWN = "UNKNOWN"
    CONTRADICTED = "CONTRADICTED"
    EXPIRED = "EXPIRED"


class EvidenceStanding(str, Enum):
    TITLE_ONLY = "TITLE_ONLY"
    SOURCE_BEARING = "SOURCE_BEARING"
    SPECIFIED = "SPECIFIED"
    IMPLEMENTED = "IMPLEMENTED"
    MEASURED = "MEASURED"
    AUTHORITY_QUALIFIED = "AUTHORITY_QUALIFIED"


class TimeOrder(str, Enum):
    BEFORE = "BEFORE"
    AFTER = "AFTER"
    OVERLAP = "OVERLAP"
    INDETERMINATE = "INDETERMINATE"


class ValidityState(str, Enum):
    VALID = "VALID"
    NOT_YET_VALID = "NOT_YET_VALID"
    EXPIRED = "EXPIRED"
    INDETERMINATE = "INDETERMINATE"


class AuthorityTransition(str, Enum):
    NON_EXPANDING = "NON_EXPANDING"
    REQUIRES_EXTERNAL_CERTIFIED_CHECKPOINT = "REQUIRES_EXTERNAL_CERTIFIED_CHECKPOINT"


def _require_nonblank(value: str, code: str, name: str) -> None:
    if not isinstance(value, str) or not value.strip():
        raise ContractError(code, f"{name} must be a nonblank string")


def _require_int(value: int, minimum: int, code: str, name: str) -> None:
    if type(value) is not int or value < minimum:
        raise ContractError(code, f"{name} must be an integer >= {minimum}")


def _require_string_tuple(value: tuple[str, ...], code: str, name: str) -> None:
    if any(not isinstance(item, str) or not item.strip() for item in value):
        raise ContractError(code, f"{name} must contain only nonblank strings")


@dataclass(frozen=True)
class TypedTimePoint:
    clock_domain: str
    value_ns: int
    uncertainty_ns: int
    sync_model_id: str

    def __post_init__(self) -> None:
        _require_nonblank(self.clock_domain, "E_TIME_CLOCK", "clock_domain")
        _require_nonblank(self.sync_model_id, "E_TIME_SYNC", "sync_model_id")
        _require_int(self.value_ns, -(2**63), "E_TIME_VALUE", "value_ns")
        _require_int(self.uncertainty_ns, 0, "E_TIME_UNCERTAINTY", "uncertainty_ns")

    @property
    def lower_ns(self) -> int:
        return self.value_ns - self.uncertainty_ns

    @property
    def upper_ns(self) -> int:
        return self.value_ns + self.uncertainty_ns


@dataclass(frozen=True)
class TypedTimeInterval:
    start: TypedTimePoint
    end: TypedTimePoint

    def __post_init__(self) -> None:
        if not clocks_compatible(self.start, self.end):
            raise ContractError(
                "E_INTERVAL_CLOCK",
                "interval endpoints must share clock_domain and sync_model_id",
            )
        if self.start.value_ns > self.end.value_ns:
            raise ContractError("E_INTERVAL_ORDER", "interval start must not exceed interval end")


def clocks_compatible(a: TypedTimePoint, b: TypedTimePoint) -> bool:
    return a.clock_domain == b.clock_domain and a.sync_model_id == b.sync_model_id


def compare_time_points(a: TypedTimePoint, b: TypedTimePoint) -> TimeOrder:
    if not clocks_compatible(a, b):
        return TimeOrder.INDETERMINATE
    if a.upper_ns < b.lower_ns:
        return TimeOrder.BEFORE
    if a.lower_ns > b.upper_ns:
        return TimeOrder.AFTER
    return TimeOrder.OVERLAP


@dataclass(frozen=True)
class ValidityContract:
    interval: TypedTimeInterval
    support_id: str
    expiry_reasons: tuple[str, ...]
    recheck_trigger: str

    def __post_init__(self) -> None:
        _require_nonblank(self.support_id, "E_VALIDITY_SUPPORT", "support_id")
        _require_nonblank(self.recheck_trigger, "E_VALIDITY_RECHECK", "recheck_trigger")
        _require_string_tuple(self.expiry_reasons, "E_VALIDITY_REASON", "expiry_reasons")


def classify_validity(contract: ValidityContract, now: TypedTimePoint) -> ValidityState:
    start = contract.interval.start
    end = contract.interval.end
    if not clocks_compatible(start, now) or not clocks_compatible(end, now):
        return ValidityState.INDETERMINATE
    if now.upper_ns < start.lower_ns:
        return ValidityState.NOT_YET_VALID
    if now.lower_ns > end.upper_ns:
        return ValidityState.EXPIRED
    if now.lower_ns >= start.upper_ns and now.upper_ns <= end.lower_ns:
        return ValidityState.VALID
    return ValidityState.INDETERMINATE


def _plain(value: Any) -> Any:
    if isinstance(value, Enum):
        return value.value
    if is_dataclass(value):
        return {field.name: _plain(getattr(value, field.name)) for field in fields(value)}
    if isinstance(value, Mapping):
        result: dict[str, Any] = {}
        for key, item in value.items():
            string_key = str(key)
            if string_key in result:
                raise ContractError("E_CANONICAL_KEY_COLLISION", f"duplicate canonical key {string_key!r}")
            result[string_key] = _plain(item)
        return result
    if isinstance(value, (tuple, list)):
        return [_plain(item) for item in value]
    if isinstance(value, (set, frozenset)):
        items = [_plain(item) for item in value]
        return sorted(
            items,
            key=lambda item: json.dumps(
                item,
                sort_keys=True,
                separators=(",", ":"),
                ensure_ascii=False,
                allow_nan=False,
            ),
        )
    if value is None or isinstance(value, (str, int, float, bool)):
        return value
    raise ContractError("E_CANONICAL_TYPE", f"unsupported canonical value type: {type(value)!r}")


def canonical_json(value: Any) -> bytes:
    try:
        return json.dumps(
            _plain(value),
            sort_keys=True,
            separators=(",", ":"),
            ensure_ascii=False,
            allow_nan=False,
        ).encode("utf-8")
    except (TypeError, ValueError) as exc:
        raise ContractError("E_CANONICAL_VALUE", str(exc)) from exc


def payload_digest(value: Any) -> str:
    return "sha256:" + sha256(canonical_json(value)).hexdigest()


@dataclass(frozen=True)
class Lineage:
    producer_id: str
    transform_id: str
    implementation_version: str
    input_refs: tuple[str, ...]
    payload_digest: str

    def __post_init__(self) -> None:
        _require_nonblank(self.producer_id, "E_LINEAGE_PRODUCER", "producer_id")
        _require_nonblank(self.transform_id, "E_LINEAGE_TRANSFORM", "transform_id")
        if not SEMVER_RE.match(self.implementation_version):
            raise ContractError("E_LINEAGE_VERSION", "implementation_version must be semantic")
        _require_string_tuple(self.input_refs, "E_LINEAGE_INPUT", "input_refs")
        if len(set(self.input_refs)) != len(self.input_refs):
            raise ContractError("E_LINEAGE_DUPLICATE", "input_refs must be unique")
        if not SHA256_RE.match(self.payload_digest):
            raise ContractError("E_LINEAGE_DIGEST", "payload_digest must be sha256:<64 lowercase hex>")


def make_lineage(
    payload: Any,
    *,
    producer_id: str,
    transform_id: str,
    implementation_version: str,
    input_refs: tuple[str, ...] = (),
) -> Lineage:
    return Lineage(
        producer_id=producer_id,
        transform_id=transform_id,
        implementation_version=implementation_version,
        input_refs=input_refs,
        payload_digest=payload_digest(payload),
    )


def verify_lineage(payload: Any, lineage: Lineage) -> None:
    actual = payload_digest(payload)
    if actual != lineage.payload_digest:
        raise ContractError(
            "E_DIGEST_MISMATCH",
            f"expected {lineage.payload_digest}, calculated {actual}",
        )


@dataclass(frozen=True)
class AuthorityCeiling:
    decision_cell_id: str
    verbs: tuple[str, ...]
    object_scope: tuple[str, ...]
    valid_until: TypedTimePoint

    def __post_init__(self) -> None:
        _require_nonblank(self.decision_cell_id, "E_AUTH_CELL", "decision_cell_id")
        _require_string_tuple(self.verbs, "E_AUTH_VERBS", "verbs")
        _require_string_tuple(self.object_scope, "E_AUTH_SCOPE", "object_scope")
        normalized_verbs = tuple(sorted(set(self.verbs)))
        normalized_scope = tuple(sorted(set(self.object_scope)))
        if not normalized_verbs:
            raise ContractError("E_AUTH_VERBS", "verbs must be nonempty")
        if not normalized_scope:
            raise ContractError("E_AUTH_SCOPE", "object_scope must be nonempty")
        object.__setattr__(self, "verbs", normalized_verbs)
        object.__setattr__(self, "object_scope", normalized_scope)


def classify_authority_transition(old: AuthorityCeiling, new: AuthorityCeiling) -> AuthorityTransition:
    same_cell = new.decision_cell_id == old.decision_cell_id
    verbs_contract = set(new.verbs).issubset(old.verbs)
    scope_contract = set(new.object_scope).issubset(old.object_scope)
    compatible = clocks_compatible(new.valid_until, old.valid_until)
    time_contract = compatible and new.valid_until.upper_ns <= old.valid_until.upper_ns
    if same_cell and verbs_contract and scope_contract and time_contract:
        return AuthorityTransition.NON_EXPANDING
    return AuthorityTransition.REQUIRES_EXTERNAL_CERTIFIED_CHECKPOINT


@dataclass(frozen=True)
class RecordHeader:
    schema_version: str
    record_id: str
    endpoint_id: EndpointID
    generation: int
    status: RecordStatus
    evidence_standing: EvidenceStanding
    native_evidence_ref: str
    validity: ValidityContract
    lineage: Lineage
    authority_ceiling: AuthorityCeiling

    def __post_init__(self) -> None:
        if not SEMVER_RE.match(self.schema_version):
            raise ContractError("E_SCHEMA_VERSION", "schema_version must be semantic")
        _require_nonblank(self.record_id, "E_RECORD_ID", "record_id")
        _require_int(self.generation, 0, "E_GENERATION", "generation")
        _require_nonblank(self.native_evidence_ref, "E_NATIVE_EVIDENCE", "native_evidence_ref")


def validate_record(header: RecordHeader, payload: Any) -> None:
    verify_lineage(payload, header.lineage)


def validate_successor(parent: RecordHeader, child: RecordHeader) -> None:
    if child.generation != parent.generation + 1:
        raise ContractError("E_SUCCESSOR_GENERATION", "successor generation must equal parent generation + 1")
    if parent.record_id not in child.lineage.input_refs:
        raise ContractError("E_SUCCESSOR_PARENT", "successor lineage must reference parent record_id")
    if child.native_evidence_ref != parent.native_evidence_ref:
        raise ContractError("E_NATIVE_REWRITE", "successor must preserve the native evidence reference")


def runtime_eligible(header: RecordHeader, now: TypedTimePoint) -> bool:
    return header.status is RecordStatus.VALID and classify_validity(header.validity, now) is ValidityState.VALID


def _expect_error(code: str, fn: Any) -> None:
    try:
        fn()
    except ContractError as exc:
        assert exc.code == code, (exc.code, code)
    else:
        raise AssertionError(f"expected ContractError {code}")


def self_test() -> None:
    t0 = TypedTimePoint("mono-1", 100, 0, "native")
    t1 = TypedTimePoint("mono-1", 200, 0, "native")
    t2 = TypedTimePoint("mono-1", 300, 0, "native")
    assert compare_time_points(t0, t1) is TimeOrder.BEFORE
    assert compare_time_points(t1, t0) is TimeOrder.AFTER
    assert compare_time_points(
        TypedTimePoint("mono-1", 200, 25, "native"),
        TypedTimePoint("mono-1", 220, 25, "native"),
    ) is TimeOrder.OVERLAP
    assert compare_time_points(t0, TypedTimePoint("device-2", 100, 0, "sync-v2")) is TimeOrder.INDETERMINATE

    validity = ValidityContract(
        TypedTimeInterval(t0, t2),
        "fixture-support-v1",
        ("clock-reset", "calibration-expired"),
        "boundary-or-epoch-change",
    )
    assert classify_validity(validity, t1) is ValidityState.VALID
    assert classify_validity(validity, TypedTimePoint("mono-1", 50, 0, "native")) is ValidityState.NOT_YET_VALID
    assert classify_validity(validity, TypedTimePoint("mono-1", 350, 0, "native")) is ValidityState.EXPIRED

    payload0 = {"kind": "fixture", "value": [1, 2, 3]}
    lineage0 = make_lineage(
        payload0,
        producer_id="fixture-source",
        transform_id="ROOT",
        implementation_version="0.1.0",
    )
    auth0 = AuthorityCeiling("cell-1", ("observe", "propose"), ("fixture",), t2)
    header0 = RecordHeader(
        "0.1.0",
        "record-0",
        EndpointID.COMMON,
        0,
        RecordStatus.VALID,
        EvidenceStanding.SOURCE_BEARING,
        "native-fixture-0",
        validity,
        lineage0,
        auth0,
    )
    validate_record(header0, payload0)
    assert runtime_eligible(header0, t1)

    payload1 = {"kind": "fixture", "value": [1, 2, 3], "qualified": True}
    lineage1 = make_lineage(
        payload1,
        producer_id="qualifier",
        transform_id="qualify-v1",
        implementation_version="0.1.0",
        input_refs=(header0.record_id,),
    )
    auth1 = AuthorityCeiling("cell-1", ("observe",), ("fixture",), t1)
    header1 = RecordHeader(
        "0.1.0",
        "record-1",
        EndpointID.COMMON,
        1,
        RecordStatus.VALID,
        EvidenceStanding.SPECIFIED,
        "native-fixture-0",
        validity,
        lineage1,
        auth1,
    )
    validate_record(header1, payload1)
    validate_successor(header0, header1)
    assert classify_authority_transition(auth0, auth1) is AuthorityTransition.NON_EXPANDING

    expansion = AuthorityCeiling("cell-1", ("observe", "propose", "execute"), ("fixture",), t2)
    assert classify_authority_transition(auth1, expansion) is AuthorityTransition.REQUIRES_EXTERNAL_CERTIFIED_CHECKPOINT
    _expect_error("E_INTERVAL_ORDER", lambda: TypedTimeInterval(t2, t0))
    _expect_error("E_DIGEST_MISMATCH", lambda: verify_lineage({"kind": "tampered"}, lineage0))


if __name__ == "__main__":
    self_test()
    print("CEGA-PK0 self-test passed")
