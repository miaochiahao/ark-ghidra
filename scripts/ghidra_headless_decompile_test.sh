#!/usr/bin/env bash
#
# ghidra_headless_decompile_test.sh — Full-pipeline E2E decompilation test
#
# Import HAP into Ghidra headless, run auto-analysis, decompile all methods,
# save output, collect metrics, and compare against baselines.
#
# Usage:
#   ./scripts/ghidra_headless_decompile_test.sh [hap_file ...]
#   ./scripts/ghidra_headless_decompile_test.sh ~/Downloads/entry-default-unsigned.hap
#
# If no files given, processes all .hap from ~/Downloads/
#
set -euo pipefail

# --- Helper functions (must be defined before use) ---

extract_metric() {
    local file="$1"
    local key="$2"
    local default="$3"
    if [ -f "$file" ]; then
        # Ghidra logs prefix: "TIMESTAMP INFO (GhidraScript) ScriptName.java> key=value"
        local val
        val=$(grep -E "\\b${key}=" "$file" 2>/dev/null | head -1 | sed -E "s/.*${key}=([0-9]+).*/\\1/") || true
        if [ -n "$val" ]; then
            echo "$val"
        else
            echo "$default"
        fi
    else
        echo "$default"
    fi
}

# --- Configuration ---

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="${SCRIPT_DIR}/../build/ghidra_test_project"
OUTPUT_DIR="${SCRIPT_DIR}/../build/ghidra_test_output"
GHIDRA_SCRIPTS="${SCRIPT_DIR}/../ghidra_scripts"
BASELINE_DIR="${SCRIPT_DIR}/../data/test_hap"

GHIDRA_HOME="${GHIDRA_HOME:-$HOME/Documents/ghidra_12.0.4_PUBLIC}"
JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home}"
METHOD_TIMEOUT="${METHOD_TIMEOUT:-30000}"

export JAVA_HOME

HEADLESS="$GHIDRA_HOME/support/analyzeHeadless"

if [ ! -x "$HEADLESS" ]; then
    echo "ERROR: Ghidra headless analyzer not found at $HEADLESS"
    echo "Set GHIDRA_HOME to your Ghidra installation directory."
    exit 1
fi

# --- Collect HAP files ---
HAPS=("$@")
if [ ${#HAPS[@]} -eq 0 ]; then
    while IFS= read -r f; do HAPS+=("$f"); done < <(ls ~/Downloads/*.hap 2>/dev/null || true)
fi

if [ ${#HAPS[@]} -eq 0 ]; then
    echo "ERROR: No .hap files found. Pass files as arguments or put them in ~/Downloads/"
    exit 1
fi

echo "=== Ghidra Headless Decompilation Test ==="
echo "Ghidra:    $GHIDRA_HOME"
echo "Java:      $JAVA_HOME"
echo "Output:    $OUTPUT_DIR"
echo "Timeout:   ${METHOD_TIMEOUT}ms per method"
echo "HAP files: ${#HAPS[@]}"
echo ""

# --- Prepare output directory ---
rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

# --- Run per-HAP ---
declare -a SUMMARY_LINES
TOTAL_METHODS=0
TOTAL_SUCCESS=0
TOTAL_FAIL=0
TOTAL_TIMEOUT=0
TOTAL_UNHANDLED=0
TOTAL_ELAPSED=0

for hap in "${HAPS[@]}"; do
    name=$(basename "$hap")
    log_file="$OUTPUT_DIR/${name%.hap}.log"

    echo ">>> [$name] ($(du -h "$hap" | cut -f1))"

    # Clean project for each run
    rm -rf "$PROJECT_DIR"
    mkdir -p "$PROJECT_DIR"

    "$HEADLESS" "$PROJECT_DIR" ArkGhidraDecompileTest \
        -import "$hap" \
        -overwrite \
        -postScript HeadlessDecompileAllMethods.java "$OUTPUT_DIR" "$METHOD_TIMEOUT" \
        -scriptPath "$GHIDRA_SCRIPTS" \
        -log "$log_file" \
        2>&1 | tail -20

    # Parse metrics from log
    metrics_file="$OUTPUT_DIR/${name%.hap}_metrics.txt"
    if [ -f "$log_file" ]; then
        sed -n '/===DECOMPILE_METRICS===/,/===END_METRICS===/p' "$log_file" \
            > "$metrics_file" 2>/dev/null || true
    fi

    # Extract values
    methods=$(extract_metric "$metrics_file" "total_methods" "0")
    success=$(extract_metric "$metrics_file" "success" "0")
    fail=$(extract_metric "$metrics_file" "fail" "0")
    timeouts=$(extract_metric "$metrics_file" "timeouts" "0")
    unhandled=$(extract_metric "$metrics_file" "unhandled_opcodes" "0")
    elapsed=$(extract_metric "$metrics_file" "elapsed_ms" "0")

    TOTAL_METHODS=$((TOTAL_METHODS + methods))
    TOTAL_SUCCESS=$((TOTAL_SUCCESS + success))
    TOTAL_FAIL=$((TOTAL_FAIL + fail))
    TOTAL_TIMEOUT=$((TOTAL_TIMEOUT + timeouts))
    TOTAL_UNHANDLED=$((TOTAL_UNHANDLED + unhandled))
    TOTAL_ELAPSED=$((TOTAL_ELAPSED + elapsed))

    SUMMARY_LINES+=("$(printf '%-40s %6s %6s %5s %5s %5s %8sms' \
        "$name" "$methods" "$success" "$fail" "$timeouts" "$unhandled" "$elapsed")")

    echo ""
done

# --- Baseline comparison ---
BASELINE_CHANGES=0
if [ -d "$BASELINE_DIR" ] && [ "$(ls -A "$BASELINE_DIR" 2>/dev/null)" ]; then
    echo "--- Baseline Comparison (data/test_hap/) ---"
    for ts_file in "$OUTPUT_DIR"/*.ts; do
        [ -f "$ts_file" ] || continue
        base_name=$(basename "$ts_file")
        baseline=""
        for bl in "$BASELINE_DIR"/*.ts; do
            [ -f "$bl" ] || continue
            bl_name=$(basename "$bl")
            if [[ "$base_name" == *"${bl_name%.ts}"* ]] || [[ "$bl_name" == *"${base_name%.ts}"* ]]; then
                baseline="$bl"
                break
            fi
        done

        if [ -n "$baseline" ]; then
            if ! diff -q "$ts_file" "$baseline" > /dev/null 2>&1; then
                changed=$(diff --unified=0 "$baseline" "$ts_file" | grep '^[+-]' | wc -l | tr -d ' ')
                echo "  CHANGED: $base_name (${changed} lines diff)"
                BASELINE_CHANGES=$((BASELINE_CHANGES + 1))
            else
                echo "  MATCH:   $base_name"
            fi
        else
            echo "  NEW:     $base_name (no baseline)"
        fi
    done
    echo ""
fi

# --- Summary Report ---
echo "=== Summary Report ==="
printf '%-40s %6s %6s %5s %5s %5s %10s\n' \
    "HAP File" "Methods" "OK" "Fail" "T/O" "Unhdl" "Time"
printf '%-78s\n' "$(printf '%0.s-' {1..78})"
for line in "${SUMMARY_LINES[@]}"; do
    echo "$line"
done
printf '%-78s\n' "$(printf '%0.s-' {1..78})"
printf '%-40s %6s %6s %5s %5s %5s %10s\n' \
    "TOTAL" "$TOTAL_METHODS" "$TOTAL_SUCCESS" \
    "$TOTAL_FAIL" "$TOTAL_TIMEOUT" "$TOTAL_UNHANDLED" "${TOTAL_ELAPSED}ms"
echo ""

# --- Write JSON summary ---
cat > "$OUTPUT_DIR/summary.json" << 'JSONEOF'
{
JSONEOF
echo "  \"total_methods\": $TOTAL_METHODS," >> "$OUTPUT_DIR/summary.json"
echo "  \"success\": $TOTAL_SUCCESS," >> "$OUTPUT_DIR/summary.json"
echo "  \"fail\": $TOTAL_FAIL," >> "$OUTPUT_DIR/summary.json"
echo "  \"timeouts\": $TOTAL_TIMEOUT," >> "$OUTPUT_DIR/summary.json"
echo "  \"unhandled_opcodes\": $TOTAL_UNHANDLED," >> "$OUTPUT_DIR/summary.json"
echo "  \"elapsed_ms\": $TOTAL_ELAPSED," >> "$OUTPUT_DIR/summary.json"
echo "  \"hap_files\": ${#HAPS[@]}," >> "$OUTPUT_DIR/summary.json"
echo "  \"baseline_changes\": $BASELINE_CHANGES" >> "$OUTPUT_DIR/summary.json"
echo "}" >> "$OUTPUT_DIR/summary.json"
echo "Summary saved to $OUTPUT_DIR/summary.json"

# --- Cleanup ---
rm -rf "$PROJECT_DIR"

# Exit with error if too many failures
if [ "$TOTAL_FAIL" -gt 0 ] || [ "$TOTAL_TIMEOUT" -gt 0 ]; then
    echo ""
    echo "WARNING: $TOTAL_FAIL failures, $TOTAL_TIMEOUT timeouts detected"
    exit 1
fi

echo "All decompilations succeeded."
