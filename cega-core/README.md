# CEGA CORE0 deterministic validation package

This directory materializes the dependency-light CEGA primitives and their first bottom-up composition:

```text
PK0 identity/time/status/lineage/validity
+ PK5-min work/span/resource trace
+ PK7-min conjunctive gate and authority nonexpansion
→ CORE0 minimal governed transaction spine
```

## Standing

The package is a **synthetic, non-actuating validation target**. Source presence supports `IMPLEMENTED`. A passing run supports execution only for the exact commit, runtime, environment, and fixtures recorded in its manifest. It does not establish vehicle safety, neural-interface safety, physical feasibility, deployment readiness, cross-endpoint transfer, or authority qualification.

## Layout

```text
src/cega/pk0.py       typed identity, uncertain time, lineage, validity, authority ceiling
src/cega/pk5_min.py   dependency DAG, critical span, resource readings, deadline reserve
src/cega/pk7_min.py   conjunctive hard gate and deterministic disposition
src/cega/core0.py     ten-stage governed transaction and replay verification
src/cega/fixture.py   pure synthetic adapter, deterministic clock, fault campaign
tests/                primitive, composition, and negative tests
scripts/              campaign and exact evidence-manifest runners
```

## Fast validation

From this directory:

```bash
make validate
```

Equivalent commands:

```bash
PYTHONPATH=src python -m compileall -q src tests scripts
PYTHONPATH=src python -m unittest discover -s tests -v
PYTHONPATH=src python scripts/run_fixture.py
PYTHONPATH=src python scripts/run_evidence.py
```

Python 3.11 or newer is required. No third-party Python package is required.

## Discriminating campaign

| Case | Required disposition |
|---|---|
| nominal | `ADMIT` |
| digest mismatch | `REFUSE / E_DIGEST_MISMATCH` |
| expired input | `REFUSE / E_RUNTIME_INELIGIBLE` |
| unknown model support | `HOLD / OUTSIDE_SUPPORT` |
| failed safety predicate | `FALLBACK / UNSAFE` |
| self-verification | `FALLBACK / E_SELF_VERIFICATION` |
| authority expansion | `HOLD / E_EXTERNAL_CHECKPOINT_REQUIRED` |
| response deadline miss | `FALLBACK` or `REFUSE / E_RESPONSE_DEADLINE_MISS` |
| replay mutation | `E_REPLAY_DIGEST_MISMATCH` |

The fixture uses one canonical JSON payload, a pure adapter, an injected stepped clock, the `observe` verb, the `synthetic-fixture` object scope, and no physical actuation. Energy and temperature remain `UNAVAILABLE`.

## Evidence output

`python scripts/run_evidence.py` writes `evidence/core0-run.json` and prints the same document. It records runtime and environment, source and fixture digests, every campaign disposition, replay equality, synthetic spans, host timing percentiles, timing-instrument overhead, standing, and nonclaims.

Synthetic trace coordinates are not hardware latency measurements. Host timings describe only the recorded runner and are not hard-real-time bounds.
