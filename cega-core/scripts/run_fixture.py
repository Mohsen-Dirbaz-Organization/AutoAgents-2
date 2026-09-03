#!/usr/bin/env python3
from __future__ import annotations

import json

from cega.fixture import run_campaign


if __name__ == "__main__":
    print(json.dumps(run_campaign(), indent=2, sort_keys=True))
