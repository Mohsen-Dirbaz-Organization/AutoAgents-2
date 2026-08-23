from __future__ import annotations

from dataclasses import dataclass, replace
from enum import Enum
import re
from typing import Any

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
    runtime_eligible,
    validate_record,
)
from cega.pk1_min import SupportStatus


SEMVER_RE = re.compile(r"^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-[0-9A-Za-z.-]+)?$")


class VariableRole(str, Enum):
    STATE = "STATE"
    INPUT = "INPUT"
    OUTPUT = "OUTPUT"
    PARAMETER = "PARAMETER"


class AssumptionStanding(str, Enum):
    PROVISIONAL = "PROVISIONAL"
    SUPPORTED = "SUPPORTED"
    UNKNOWN = "UNKNOWN"
    CONTRADICTED = "CONTRADICTED"
    EXPIRED = "EXPIRED"


class ConstitutiveKind(str, Enum):
    PHYSICS_BASED = "PHYSICS_BASED"
    EMPIRICAL = "EMPIRICAL"
    HYBRID = "HYBRID"
    CONVENTIONAL = "CONVENTIONAL"
    UNKNOWN = "UNKNOWN"


class CriterionStanding(str, Enum):
    PASS = "PASS"
    FAIL = "FAIL"
    UNKNOWN = "UNKNOWN"
    EXPIRED = "EXPIRED"


class ResidualStanding(str, Enum):
    WITHIN_BOUND = "WITHIN_BOUND"
    EXCEEDS_BOUND = "EXCEEDS_BOUND"


def _nonblank(value: str, code: str, name: str) -> None:
    if not isinstance(value, str) or not value.strip():
        raise ContractError(code, f"{name} must be nonblank")


def _strings(values: tuple[str, ...], code: str, name: str) -> None:
    if any(not isinstance(value, str) or not value.strip() for value in values):
        raise ContractError(code, f"{name} must contain only nonblank strings")
    if len(values) != len(set(values)):
        raise ContractError(code, f"{name} must not contain duplicates")


@dataclass(frozen=True)
class RationalMagnitude:
    numerator: int
    denominator: int
    unit: str

    def __post_init__(self) -> None:
        if type(self.numerator) is not int or self.numerator < 0:
            raise ContractError(
                "E_MAGNITUDE_NUMERATOR",
                "numerator must be an integer >= 0",
            )
        if type(self.denominator) is not int or self.denominator <= 0:
            raise ContractError(
                "E_MAGNITUDE_DENOMINATOR",
                "denominator must be an integer > 0",
            )
        _nonblank(self.unit, "E_MAGNITUDE_UNIT", "unit")

    def compare(self, other: RationalMagnitude) -> int:
        if self.unit != other.unit:
            raise ContractError(
                "E_MAGNITUDE_UNIT_MISMATCH",
                f"cannot compare {self.unit!r} with {other.unit!r}",
            )
        left = self.numerator * other.denominator
        right = other.numerator * self.denominator
        return (left > right) - (left < right)


@dataclass(frozen=True)
class ModelVariable:
    name: str
    role: VariableRole
    unit: str
    clock_domain: str
    shape: tuple[int, ...]
    boundary_semantics_ref: str

    def __post_init__(self) -> None:
        _nonblank(self.name, "E_VARIABLE_NAME", "name")
        _nonblank(self.unit, "E_VARIABLE_UNIT", "unit")
        _nonblank(self.clock_domain, "E_VARIABLE_CLOCK", "clock_domain")
        _nonblank(
            self.boundary_semantics_ref,
            "E_VARIABLE_BOUNDARY",
            "boundary_semantics_ref",
        )
        if any(type(size) is not int or size <= 0 for size in self.shape):
            raise ContractError(
                "E_VARIABLE_SHAPE",
                "shape entries must be positive integers",
            )


@dataclass(frozen=True)
class ModelAssumption:
    assumption_id: str
    statement_ref: str
    scope_ref: str
    standing: AssumptionStanding
    falsifier_ref: str
    evidence_ref: str | None
    reason_codes: tuple[str, ...] = ()

    def __post_init__(self) -> None:
        _nonblank(self.assumption_id, "E_ASSUMPTION_ID", "assumption_id")
        _nonblank(self.statement_ref, "E_ASSUMPTION_STATEMENT", "statement_ref")
        _nonblank(self.scope_ref, "E_ASSUMPTION_SCOPE", "scope_ref")
        _nonblank(self.falsifier_ref, "E_ASSUMPTION_FALSIFIER", "falsifier_ref")
        _strings(self.reason_codes, "E_ASSUMPTION_REASONS", "reason_codes")
        if self.standing in (
            AssumptionStanding.SUPPORTED,
            AssumptionStanding.CONTRADICTED,
        ):
            if self.evidence_ref is None:
                raise ContractError(
                    "E_ASSUMPTION_EVIDENCE",
                    "supported or contradicted assumption requires evidence_ref",
                )
            _nonblank(
                self.evidence_ref,
                "E_ASSUMPTION_EVIDENCE",
                "evidence_ref",
            )
        elif self.evidence_ref is not None:
            _nonblank(
                self.evidence_ref,
                "E_ASSUMPTION_EVIDENCE",
                "evidence_ref",
            )
        if self.standing is not AssumptionStanding.SUPPORTED and not self.reason_codes:
            raise ContractError(
                "E_ASSUMPTION_REASON",
                "non-supported assumption requires a reason code",
            )


@dataclass(frozen=True)
class ConstitutiveClosure:
    closure_id: str
    kind: ConstitutiveKind
    relation_ref: str
    parameter_source_ref: str
    calibration_ref: str
    boundary_contract_ref: str
    evidence_standing: EvidenceStanding
    reason_codes: tuple[str, ...] = ()

    def __post_init__(self) -> None:
        _nonblank(self.closure_id, "E_CLOSURE_ID", "closure_id")
        _nonblank(self.relation_ref, "E_CLOSURE_RELATION", "relation_ref")
        _nonblank(
            self.parameter_source_ref,
            "E_CLOSURE_PARAMETER_SOURCE",
            "parameter_source_ref",
        )
        _nonblank(self.calibration_ref, "E_CLOSURE_CALIBRATION", "calibration_ref")
        _nonblank(
            self.boundary_contract_ref,
            "E_CLOSURE_BOUNDARY",
            "boundary_contract_ref",
        )
        _strings(self.reason_codes, "E_CLOSURE_REASONS", "reason_codes")
        if self.kind is ConstitutiveKind.UNKNOWN and not self.reason_codes:
            raise ContractError(
                "E_CLOSURE_UNKNOWN_REASON",
                "unknown closure requires a reason code",
            )


@dataclass(frozen=True)
class ResidualContract:
    residual_id: str
    norm_ref: str
    bound: RationalMagnitude
    aggregation_ref: str
    scope_ref: str
    sampling_contract_ref: str

    def __post_init__(self) -> None:
        _nonblank(self.residual_id, "E_RESIDUAL_ID", "residual_id")
        _nonblank(self.norm_ref, "E_RESIDUAL_NORM", "norm_ref")
        _nonblank(self.aggregation_ref, "E_RESIDUAL_AGGREGATION", "aggregation_ref")
        _nonblank(self.scope_ref, "E_RESIDUAL_SCOPE", "scope_ref")
        _nonblank(
            self.sampling_contract_ref,
            "E_RESIDUAL_SAMPLING",
            "sampling_contract_ref",
        )


@dataclass(frozen=True)
class ModelContract:
    header: RecordHeader
    model_id: str
    model_version: str
    scope_ref: str
    variables: tuple[ModelVariable, ...]
    boundary_contract_ref: str
    support_contract_ref: str
    assumptions: tuple[ModelAssumption, ...]
    closure: ConstitutiveClosure
    conventional_baseline_ref: str
    residual_contract: ResidualContract

    def __post_init__(self) -> None:
        _nonblank(self.model_id, "E_MODEL_ID", "model_id")
        if self.model_id != self.header.record_id:
            raise ContractError("E_MODEL_RECORD_ID", "model_id must equal header.record_id")
        if not SEMVER_RE.match(self.model_version):
            raise ContractError("E_MODEL_VERSION", "model_version must be semantic")
        _nonblank(self.scope_ref, "E_MODEL_SCOPE", "scope_ref")
        _nonblank(
            self.boundary_contract_ref,
            "E_MODEL_BOUNDARY",
            "boundary_contract_ref",
        )
        _nonblank(
            self.support_contract_ref,
            "E_MODEL_SUPPORT",
            "support_contract_ref",
        )
        _nonblank(
            self.conventional_baseline_ref,
            "E_MODEL_BASELINE",
            "conventional_baseline_ref",
        )
        if not self.variables:
            raise ContractError("E_MODEL_VARIABLES_EMPTY", "model requires variables")
        names = [variable.name for variable in self.variables]
        if len(names) != len(set(names)):
            raise ContractError(
                "E_MODEL_VARIABLE_DUPLICATE",
                "variable names must be unique across roles",
            )
        roles = {variable.role for variable in self.variables}
        for required in (VariableRole.STATE, VariableRole.INPUT, VariableRole.OUTPUT):
            if required not in roles:
                raise ContractError(
                    "E_MODEL_VARIABLE_ROLE_MISSING",
                    f"model requires at least one {required.value} variable",
                )
        if not self.assumptions:
            raise ContractError("E_MODEL_ASSUMPTIONS_EMPTY", "model requires assumptions")
        assumption_ids = [assumption.assumption_id for assumption in self.assumptions]
        if len(assumption_ids) != len(set(assumption_ids)):
            raise ContractError(
                "E_MODEL_ASSUMPTION_DUPLICATE",
                "assumption IDs must be unique",
            )
        if self.closure.boundary_contract_ref != self.boundary_contract_ref:
            raise ContractError(
                "E_MODEL_CLOSURE_BOUNDARY",
                "closure boundary contract must match model boundary contract",
            )


@dataclass(frozen=True)
class SupportCriterionReading:
    criterion_id: str
    standing: CriterionStanding
    observed_at: TypedTimePoint
    evidence_ref: str
    reason_codes: tuple[str, ...] = ()

    def __post_init__(self) -> None:
        _nonblank(self.criterion_id, "E_SUPPORT_CRITERION", "criterion_id")
        _nonblank(self.evidence_ref, "E_SUPPORT_EVIDENCE", "evidence_ref")
        _strings(self.reason_codes, "E_SUPPORT_REASONS", "reason_codes")
        if self.standing is not CriterionStanding.PASS and not self.reason_codes:
            raise ContractError(
                "E_SUPPORT_REASON",
                "non-passing support criterion requires a reason code",
            )


@dataclass(frozen=True)
class SupportAssessment:
    model_id: str
    state_epoch_id: str
    monitor_id: str
    support_contract_ref: str
    criteria: tuple[SupportCriterionReading, ...]
    assessment_evidence_ref: str

    def __post_init__(self) -> None:
        _nonblank(self.model_id, "E_SUPPORT_MODEL", "model_id")
        _nonblank(self.state_epoch_id, "E_SUPPORT_EPOCH", "state_epoch_id")
        _nonblank(self.monitor_id, "E_SUPPORT_MONITOR", "monitor_id")
        _nonblank(
            self.support_contract_ref,
            "E_SUPPORT_CONTRACT",
            "support_contract_ref",
        )
        _nonblank(
            self.assessment_evidence_ref,
            "E_SUPPORT_ASSESSMENT_EVIDENCE",
            "assessment_evidence_ref",
        )
        if not self.criteria:
            raise ContractError("E_SUPPORT_CRITERIA_EMPTY", "assessment requires criteria")
        ids = [criterion.criterion_id for criterion in self.criteria]
        if len(ids) != len(set(ids)):
            raise ContractError(
                "E_SUPPORT_CRITERION_DUPLICATE",
                "support criterion IDs must be unique",
            )


@dataclass(frozen=True)
class ResidualObservation:
    model_id: str
    state_epoch_id: str
    residual_id: str
    norm_ref: str
    value: RationalMagnitude
    observed_at: TypedTimePoint
    evidence_ref: str

    def __post_init__(self) -> None:
        _nonblank(self.model_id, "E_RESIDUAL_MODEL", "model_id")
        _nonblank(self.state_epoch_id, "E_RESIDUAL_EPOCH", "state_epoch_id")
        _nonblank(self.residual_id, "E_RESIDUAL_OBSERVATION_ID", "residual_id")
        _nonblank(self.norm_ref, "E_RESIDUAL_OBSERVATION_NORM", "norm_ref")
        _nonblank(self.evidence_ref, "E_RESIDUAL_EVIDENCE", "evidence_ref")


def validate_model_contract(model: ModelContract, payload: Any) -> None:
    validate_record(model.header, payload)


def classify_support(assessment: SupportAssessment) -> SupportStatus:
    standings = {criterion.standing for criterion in assessment.criteria}
    if CriterionStanding.FAIL in standings:
        return SupportStatus.OUT_OF_SUPPORT
    if CriterionStanding.UNKNOWN in standings or CriterionStanding.EXPIRED in standings:
        return SupportStatus.UNKNOWN
    return SupportStatus.IN_SUPPORT


def classify_residual(
    contract: ResidualContract,
    observation: ResidualObservation,
) -> ResidualStanding:
    if observation.residual_id != contract.residual_id:
        raise ContractError(
            "E_RESIDUAL_CONTRACT_MISMATCH",
            "observation residual_id must match residual contract",
        )
    if observation.norm_ref != contract.norm_ref:
        raise ContractError(
            "E_RESIDUAL_NORM_MISMATCH",
            "observation norm_ref must match residual contract",
        )
    if observation.value.compare(contract.bound) <= 0:
        return ResidualStanding.WITHIN_BOUND
    return ResidualStanding.EXCEEDS_BOUND


def model_evaluation_blockers(
    model: ModelContract,
    assessment: SupportAssessment,
    residual: ResidualObservation,
    now: TypedTimePoint,
) -> tuple[str, ...]:
    blockers: list[str] = []
    if not runtime_eligible(model.header, now):
        blockers.append("E_MODEL_HEADER_INELIGIBLE")
    if assessment.model_id != model.model_id:
        blockers.append("E_SUPPORT_MODEL_MISMATCH")
    if assessment.support_contract_ref != model.support_contract_ref:
        blockers.append("E_SUPPORT_CONTRACT_MISMATCH")
    if residual.model_id != model.model_id:
        blockers.append("E_RESIDUAL_MODEL_MISMATCH")
    if residual.state_epoch_id != assessment.state_epoch_id:
        blockers.append("E_EVALUATION_EPOCH_MISMATCH")

    support = classify_support(assessment)
    if support is SupportStatus.OUT_OF_SUPPORT:
        blockers.append("E_MODEL_OUT_OF_SUPPORT")
    elif support is SupportStatus.UNKNOWN:
        blockers.append("E_MODEL_SUPPORT_UNKNOWN")

    try:
        residual_standing = classify_residual(model.residual_contract, residual)
    except ContractError as exc:
        blockers.append(exc.code)
    else:
        if residual_standing is ResidualStanding.EXCEEDS_BOUND:
            blockers.append("E_MODEL_RESIDUAL_EXCEEDED")

    if model.closure.kind is ConstitutiveKind.UNKNOWN:
        blockers.append("E_MODEL_CLOSURE_UNKNOWN")
    for assumption in model.assumptions:
        if assumption.standing is not AssumptionStanding.SUPPORTED:
            blockers.append(
                f"E_MODEL_ASSUMPTION_{assumption.standing.value}:{assumption.assumption_id}"
            )
    return tuple(sorted(set(blockers)))


def runtime_eligible_model(
    model: ModelContract,
    assessment: SupportAssessment,
    residual: ResidualObservation,
    now: TypedTimePoint,
) -> bool:
    return not model_evaluation_blockers(model, assessment, residual, now)


def validate_model_evaluation(
    model: ModelContract,
    assessment: SupportAssessment,
    residual: ResidualObservation,
    now: TypedTimePoint,
) -> None:
    blockers = model_evaluation_blockers(model, assessment, residual, now)
    if blockers:
        raise ContractError("E_MODEL_EVALUATION_BLOCKED", "; ".join(blockers))


def _expect_error(code: str, fn: Any) -> None:
    try:
        fn()
    except ContractError as exc:
        assert exc.code == code, (exc.code, code)
    else:
        raise AssertionError(f"expected ContractError {code}")


def _self_test_fixture() -> tuple[
    ModelContract,
    SupportAssessment,
    ResidualObservation,
    TypedTimePoint,
]:
    def point(value: int) -> TypedTimePoint:
        return TypedTimePoint("pk3-clock", value, 0, "native")

    payload = {
        "model": "linear-reference",
        "state": ["x"],
        "input": ["u"],
        "output": ["y"],
    }
    validity = ValidityContract(
        TypedTimeInterval(point(0), point(1000)),
        "support:pk3:v1",
        ("calibration-expired", "boundary-change"),
        "support-or-boundary-change",
    )
    header = RecordHeader(
        "0.1.0",
        "model-0",
        EndpointID.COMMON,
        0,
        RecordStatus.VALID,
        EvidenceStanding.IMPLEMENTED,
        "native:model-specification",
        validity,
        make_lineage(
            payload,
            producer_id="pk3-self-test",
            transform_id="ROOT",
            implementation_version="0.1.0",
        ),
        AuthorityCeiling(
            "pk3-cell",
            ("observe", "propose"),
            ("model-evaluation",),
            point(1000),
        ),
    )
    variables = (
        ModelVariable("x", VariableRole.STATE, "m", "pk3-clock", (2,), "boundary:state"),
        ModelVariable("u", VariableRole.INPUT, "m/s^2", "pk3-clock", (1,), "boundary:input"),
        ModelVariable("y", VariableRole.OUTPUT, "m", "pk3-clock", (1,), "boundary:output"),
        ModelVariable("theta", VariableRole.PARAMETER, "1", "atemporal", (3,), "boundary:parameter"),
    )
    assumption = ModelAssumption(
        "A1",
        "statement:local-linearity",
        "scope:fixture",
        AssumptionStanding.SUPPORTED,
        "falsifier:residual-bound",
        "evidence:fixture-construction",
    )
    closure = ConstitutiveClosure(
        "closure-0",
        ConstitutiveKind.HYBRID,
        "relation:x-next=f(x,u,theta)",
        "parameters:theta-v1",
        "calibration:fixture-v1",
        "boundary:model-v1",
        EvidenceStanding.IMPLEMENTED,
    )
    residual_contract = ResidualContract(
        "residual-0",
        "norm:L-infinity",
        RationalMagnitude(1, 10, "m"),
        "aggregate:max",
        "scope:output-y",
        "sampling:every-step",
    )
    model = ModelContract(
        header,
        "model-0",
        "0.1.0",
        "scope:fixture",
        variables,
        "boundary:model-v1",
        "support:model-v1",
        (assumption,),
        closure,
        "baseline:constant-velocity-v1",
        residual_contract,
    )
    assessment = SupportAssessment(
        model.model_id,
        "epoch-1",
        "monitor:independent-fixture",
        model.support_contract_ref,
        (
            SupportCriterionReading(
                "state-range",
                CriterionStanding.PASS,
                point(500),
                "evidence:state-range",
            ),
            SupportCriterionReading(
                "parameter-range",
                CriterionStanding.PASS,
                point(500),
                "evidence:parameter-range",
            ),
        ),
        "evidence:support-assessment",
    )
    residual = ResidualObservation(
        model.model_id,
        assessment.state_epoch_id,
        residual_contract.residual_id,
        residual_contract.norm_ref,
        RationalMagnitude(1, 20, "m"),
        point(500),
        "evidence:residual-observation",
    )
    return model, assessment, residual, point(500)


def self_test() -> None:
    model, assessment, residual, now = _self_test_fixture()
    validate_model_contract(
        model,
        {
            "model": "linear-reference",
            "state": ["x"],
            "input": ["u"],
            "output": ["y"],
        },
    )
    assert classify_support(assessment) is SupportStatus.IN_SUPPORT
    assert classify_residual(model.residual_contract, residual) is ResidualStanding.WITHIN_BOUND
    validate_model_evaluation(model, assessment, residual, now)
    assert runtime_eligible_model(model, assessment, residual, now)

    unknown_criterion = replace(
        assessment.criteria[0],
        standing=CriterionStanding.UNKNOWN,
        reason_codes=("monitor-unavailable",),
    )
    unknown_assessment = replace(
        assessment,
        criteria=(unknown_criterion,) + assessment.criteria[1:],
    )
    assert classify_support(unknown_assessment) is SupportStatus.UNKNOWN
    assert "E_MODEL_SUPPORT_UNKNOWN" in model_evaluation_blockers(
        model,
        unknown_assessment,
        residual,
        now,
    )

    high_residual = replace(residual, value=RationalMagnitude(1, 5, "m"))
    assert classify_residual(
        model.residual_contract,
        high_residual,
    ) is ResidualStanding.EXCEEDS_BOUND
    assert "E_MODEL_RESIDUAL_EXCEEDED" in model_evaluation_blockers(
        model,
        assessment,
        high_residual,
        now,
    )

    contradicted_assumption = replace(
        model.assumptions[0],
        standing=AssumptionStanding.CONTRADICTED,
        evidence_ref="evidence:counterexample",
        reason_codes=("counterexample",),
    )
    contradicted_model = replace(model, assumptions=(contradicted_assumption,))
    assert "E_MODEL_ASSUMPTION_CONTRADICTED:A1" in model_evaluation_blockers(
        contradicted_model,
        assessment,
        residual,
        now,
    )

    _expect_error(
        "E_MAGNITUDE_UNIT_MISMATCH",
        lambda: RationalMagnitude(1, 1, "m").compare(
            RationalMagnitude(1, 1, "s")
        ),
    )


if __name__ == "__main__":
    self_test()
    print("CEGA-PK3-min self-test passed")
