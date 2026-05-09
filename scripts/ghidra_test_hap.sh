#!/usr/bin/env bash
#
# ghidra_test_hap.sh — Automated HAP import + analysis + decompile via Ghidra headless
#
# Usage:
#   ./scripts/ghidra_test_hap.sh [hap_file ...]
#   ./scripts/ghidra_test_hap.sh ~/Downloads/*.hap
#
# If no files given, imports all .hap from ~/Downloads/
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="${SCRIPT_DIR}/../build/ghidra_test_project"

# Resolve Ghidra + Java paths
GHIDRA_HOME="${GHIDRA_HOME:-$HOME/Documents/ghidra_12.0.4_PUBLIC}"
JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home}"
export JAVA_HOME

HEADLESS="$GHIDRA_HOME/support/analyzeHeadless"

if [ ! -x "$HEADLESS" ]; then
    echo "ERROR: Ghidra headless analyzer not found at $HEADLESS"
    echo "Set GHIDRA_HOME to your Ghidra installation directory."
    exit 1
fi

# Collect HAP files
HAPS=("$@")
if [ ${#HAPS[@]} -eq 0 ]; then
    mapfile -t HAPS < <(ls ~/Downloads/*.hap 2>/dev/null || true)
fi

if [ ${#HAPS[@]} -eq 0 ]; then
    echo "ERROR: No .hap files found. Pass files as arguments or put them in ~/Downloads/"
    exit 1
fi

echo "=== Ghidra Headless HAP Test ==="
echo "Ghidra:  $GHIDRA_HOME"
echo "Java:    $JAVA_HOME"
echo "Project: $PROJECT_DIR"
echo "HAPs:    ${#HAPS[@]} file(s)"
echo ""

# Create project dir
rm -rf "$PROJECT_DIR"
mkdir -p "$PROJECT_DIR"

# Import and analyze each HAP
FAILED=0
SUCCEEDED=0

for hap in "${HAPS[@]}"; do
    name=$(basename "$hap")
    log_file="$PROJECT_DIR/${name%.hap}.log"

    echo ">>> Importing: $name ($(du -h "$hap" | cut -f1))"

    "$HEADLESS" "$PROJECT_DIR" ArkGhidraTest \
        -import "$hap" \
        -overwrite \
        -log "$log_file" \
        2>&1 | grep -E "INFO.*REPORT|ERROR|WARN.*Ark|AnalyzeHeadless" || true

    if grep -q "REPORT: Import succeeded" "$log_file" 2>/dev/null; then
        echo "    OK"
        SUCCEEDED=$((SUCCEEDED + 1))
    else
        echo "    FAILED"
        FAILED=$((FAILED + 1))
    fi
done

echo ""
echo "=== Results: $SUCCEEDED succeeded, $FAILED failed ==="

# Print error summary
if [ "$FAILED" -gt 0 ]; then
    echo ""
    echo "--- Errors ---"
    grep "^ERROR\|Exception\|Caused by" "$PROJECT_DIR"/*.log 2>/dev/null | sort -u
fi

exit $FAILED
