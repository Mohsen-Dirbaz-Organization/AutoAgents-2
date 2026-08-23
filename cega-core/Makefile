PYTHON ?= python3
export PYTHONPATH := src

.PHONY: compile test fixture evidence validate

compile:
	$(PYTHON) -m compileall -q src tests scripts

test:
	$(PYTHON) -m unittest discover -s tests -v

fixture:
	$(PYTHON) scripts/run_fixture.py

evidence:
	$(PYTHON) scripts/run_evidence.py

validate: compile test fixture evidence
