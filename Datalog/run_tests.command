#!/bin/bash
# Regression suite for the adapted Datalog thesis examples (macOS).
# Double-click in Finder, or run from Terminal:  bash run_tests.command
#
# It loads every *.dl file, runs the //test queries embedded in each file,
# and compares the solution SET (order-independent) against the expected set.
# //reject files must be rejected by the engine (unsafe or unstratifiable).

cd "$(dirname "$0")" || exit 1

if ! command -v java >/dev/null 2>&1; then
  echo "No Java runtime found on PATH."
  echo "Install a JDK, e.g.:   brew install openjdk"
  echo "or download one from:  https://adoptium.net"
  read -r -p "Press Return to close..." _ 2>/dev/null || true
  exit 1
fi

echo "Using: $(java -version 2>&1 | head -n 1)"
echo

# Single-file source launch (no separate compile step needed).
# With a JDK you may instead run:  javac Datalog.java && java Datalog --test *.dl
java Datalog.java --test *.dl
status=$?

echo
if [ "$status" -eq 0 ]; then
  echo "==> All tests passed."
else
  echo "==> Some tests failed (exit $status)."
fi

# Keep the Terminal window open when launched by double-click.
read -r -p "Press Return to close..." _ 2>/dev/null || true
exit "$status"
