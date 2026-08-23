#!/usr/bin/env python3
from __future__ import annotations

from hashlib import sha256
import json
import math
import os
from pathlib import Path
import platform
import statistics
import sys
import time
from typing import Any

from cega.fixture import FixtureMode, execute_fixture, run_campaign
from cega.pk0 import payload_digest
from cega.pk7_min import GateAction


ROOT = Path(__file__).resolve().parents[1]


def digest_files() -> tuple[str, dict[str, str]]:
    files = sorted(
        [*ROOT.joinpath("src").rglob("*.py"), *ROOT.joinpath("tests").rglob("*.py"), *ROOT.joinpath("scripts").rglob("*.py")]
    )
    tree = sha256()
    per_file: dict[str, str] = {}
    for path in files:
        relative = path.relative_to(ROOT).as_posix()
        content = path.read_bytes()
        digest = sha256(content).hexdigest()
        per_file[relative] = f"sha256:{digest}"
        tree.update(relative.encode("utf-8"))
        tree.update(b"\0")
        tree.update(content)
        tree.update(b"\0")
    return f"sha256:{tree.hexdigest()}", per_file


def percentile(values: list[int], percent: float) -> int:
    ordered = sorted(values)
    index = max(0, min(len(ordered) - 1, math.ceil(percent / 100 * len(ordered)) - 1))
    return ordered[index]


def timing_summary(values: list[int]) -> dict[str, int]:
    return {
        "sample_count": len(values),
        "p50_ns": percentile(values, 50),
        "p95_ns": percentile(values, 95),
        "p99_ns": percentile(values, 99),
        "max_ns": max(values),
        "mean_ns": round(statistics.fmean(values)),
    }


def main() -> int:
    campaign = list(run_campaign(assert_expected=True))
    first = execute_fixture(FixtureMode.NOMINAL)
    second = execute_fixture(FixtureMode.NOMINAL)
    if first.final_action is not GateAction.ADMIT or first.replay_digest != second.replay_digest:
        raise RuntimeError("nominal deterministic replay check failed")

    for _ in range(25):
        execute_fixture(FixtureMode.NOMINAL)
    elapsed: list[int] = []
    for _ in range(400):
        start = time.perf_counter_ns()
        result = execute_fixture(FixtureMode.NOMINAL)
        elapsed.append(time.perf_counter_ns() - start)
        if result.final_action is not GateAction.ADMIT:
            raise RuntimeError("benchmark run did not ADMIT")

    clock_overhead: list[int] = []
    for _ in range(1000):
        start = time.perf_counter_ns()
        time.perf_counter_ns()
        clock_overhead.append(time.perf_counter_ns() - start)

    source_tree_digest, source_file_digests = digest_files()
    fixture_contract = {
        "source_payload": "one canonical JSON object",
        "adapter": "pure synthetic adapter",
        "clock": "deterministic stepped monotonic clock",
        "step_ns": 10,
        "candidate_verb": "observe",
        "object_scope": "synthetic-fixture",
        "required_predicates": ["evidence", "temporal", "authority", "recovery", "model_support", "endpoint_safety"],
        "physical_actuation": False,
        "energy": "UNAVAILABLE",
        "temperature": "UNAVAILABLE",
    }
    manifest: dict[str, Any] = {
        "schema_version": "0.1.0",
        "run_id": f"github-actions:{os.getenv('GITHUB_RUN_ID', 'local')}:{os.getenv('GITHUB_RUN_ATTEMPT', '1')}:{platform.python_version()}",
        "source_commit": os.getenv("GITHUB_SHA", "UNAVAILABLE_LOCAL"),
        "source_ref": os.getenv("GITHUB_REF", "UNAVAILABLE_LOCAL"),
        "repository": os.getenv("GITHUB_REPOSITORY", "Mohsen-Dirbaz-Organization/AutoAgents-2"),
        "runtime": {
            "implementation": sys.implementation.name,
            "python_version": platform.python_version(),
            "python_build": platform.python_build(),
            "executable": sys.executable,
        },
        "environment": {
            "platform": platform.platform(),
            "system": platform.system(),
            "release": platform.release(),
            "machine": platform.machine(),
            "runner_os": os.getenv("RUNNER_OS", "UNAVAILABLE_LOCAL"),
            "runner_arch": os.getenv("RUNNER_ARCH", "UNAVAILABLE_LOCAL"),
        },
        "command": "PYTHONPATH=src python scripts/run_evidence.py",
        "expected_exit_status": 0,
        "source_tree_digest": source_tree_digest,
        "source_file_digests": source_file_digests,
        "fixture_digest": payload_digest(fixture_contract),
        "fixture_contract": fixture_contract,
        "campaign_case_count": len(campaign),
        "campaign": campaign,
        "deterministic_replay": {
            "pass": first.replay_digest == second.replay_digest,
            "digest": first.replay_digest,
        },
        "synthetic_trace": {
            "response_critical_span_ns": first.response_trace.critical_span_upper_ns(),
            "response_margin_ns": first.response_trace.deadline_margin_upper_ns(),
            "full_critical_span_ns": first.full_trace.critical_span_upper_ns(),
            "energy_uj": None,
            "temperature": None,
        },
        "host_elapsed_benchmark": timing_summary(elapsed),
        "perf_counter_read_overhead": timing_summary(clock_overhead),
        "evidence_standing": {
            "source": "IMPLEMENTED",
            "execution": "OBSERVED_IN_GITHUB_ACTIONS_OR_LOCAL_RUNTIME",
            "host_timing": "MEASURED_ON_THIS_RUNNER_ONLY",
            "energy": "UNAVAILABLE",
            "temperature": "UNAVAILABLE",
            "endpoint_transfer": "NOT_DEMONSTRATED",
            "authority_qualification": "NOT_ESTABLISHED",
        },
        "limits": [
            "Synthetic fixture only; no vehicle, neural interface, or physical actuation.",
            "Deterministic trace nanoseconds are fixture coordinates, not hardware latency measurements.",
            "Host timings characterize only this shared CI runner and are not hard-real-time bounds.",
            "Passing tests do not establish endpoint safety, deployment readiness, transfer, or authority qualification.",
        ],
    }

    output = ROOT / "evidence" / "core0-run.json"
    output.parent.mkdir(parents=True, exist_ok=True)
    rendered = json.dumps(manifest, indent=2, sort_keys=True, ensure_ascii=False)
    output.write_text(rendered + "\n", encoding="utf-8")
    print(rendered)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
