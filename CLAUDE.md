# ark-ghidra — Ghidra ArkTS Decompiler Plugin

## Project Overview

A Ghidra plugin that provides native decompilation support for ArkTS (the language used by HarmonyOS/OpenHarmony applications).

**Tech stack:** Java 21 + Gradle + Ghidra 12.0.4 Extension API

**Repository:** https://github.com/miaochiahao/ark-ghidra

---

## Architecture

```
ark-ghidra/
├── CLAUDE.md              # This file — project rules & context
├── README.md              # User-facing documentation
├── build.gradle           # Gradle build with Ghidra extension support
├── src/
│   ├── main/java/         # Plugin source code
│   └── test/java/         # Unit and integration tests
├── data/                  # Test .abc binaries and fixtures
└── ghidra_scripts/        # Ghidra scripting bridge (optional)
```

### Core components

1. **ABC Loader** — Parses `.abc` files, maps sections into Ghidra address spaces
2. **Bytecode Decoder** — Disassembles ~220 Ark bytecode instructions
3. **SLEIGH Processor Module** — Instruction encoding/disassembly
4. **ArkTS Decompiler** — CFG → AST → ArkTS source code
5. **Auto-Analyzer** — Ghidra analysis pipeline integration
6. **UI Plugin** — Menu actions, structure viewer, decompiler output panel

---

## Development Environment

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"

./gradlew build                  # Compile plugin
./gradlew test                   # Run tests
./gradlew lint                   # Run static analysis
```

### E2E Headless Decompilation Test

Full-pipeline test: import HAP → Ghidra auto-analysis → decompile all methods → collect metrics → compare baselines.

```bash
# Test a single HAP file
./scripts/ghidra_headless_decompile_test.sh ~/Downloads/entry-default-unsigned.hap

# Test all HAP files in ~/Downloads/
./scripts/ghidra_headless_decompile_test.sh

# Custom timeout (default 30000ms per method)
METHOD_TIMEOUT=60000 ./scripts/ghidra_headless_decompile_test.sh ~/Downloads/large.hap
```

Output goes to `build/ghidra_test_output/`: decompiled `.ts` files, per-HAP logs, and `summary.json` with aggregated metrics. Baseline comparison against `data/test_hap/` detects regression in decompiler output.

Requires: Ghidra extension installed at `~/Documents/ghidra_12.0.4_PUBLIC/Ghidra/Extensions/ark-ghidra/` (run `./gradlew buildExtension` and unzip `build/dist/*.zip` to that location first).

---

## Claude Loop Workflow

**Notice: Always handle existing improvements before proposing new ones.**

1. **Propose** — Write a plan if there is no existing open issue
2. **Implement** — Write code, tests, and documentation
3. **Verify** — `./gradlew build test lint` must all pass
4. **Commit & Push** — Descriptive message, push to GitHub
5. **Learn** — Capture new conventions into lint rules below
6. **Repeat** — Next most impactful feature

### Rules

- **Never skip tests.** Every feature needs at least one test.
- **Never skip lint.** Fix all lint errors before pushing.
- **One feature per commit.**
- **Push after each feature.** `git push origin main` after green build.
- **Update this file** when new conventions are discovered.

---

## Git Conventions

- **Branch:** Work on `main` (single-developer project)
- **Commit format:** `feat: description` — Prefixes: `feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`, `lint:`
- **Push:** After every feature with passing tests

---

## Code Style

- **Language:** Java 21 (Ghidra 12.0.4 requires class version 65.0)
- **Indent:** 4 spaces, no tabs
- **Max line length:** 120 characters
- **Naming:** PascalCase classes, camelCase methods/fields, UPPER_SNAKE constants
- **Imports:** No wildcard imports; order: java. → javax. → ghidra. → com. → project
- **Exceptions:** Use Ghidra's exception hierarchy (ghidra.util.exception)
- **Logging:** Ghidra's Msg class with static OWNER string
- **Null:** No javax.annotation — omit @Nonnull/@Nullable
- **Tests:** JUnit 5, descriptive names: `testParseMethodTable_withThreeEntries_returnsCorrectCount()`
- **No TODO comments** — implement it or create a GitHub issue

---

## Lint Rules

### Checkstyle

- AvoidStarImport, ConstantName, EmptyBlock, LeftCurly(eol), NeedBraces, RightCurly, LineLength(120), Indentation(4), MethodParamPad, ParenPad, TypecastParenPad, WhitespaceAfter, WhitespaceAround, ModifierOrder, RedundantModifier, UnusedImports, UpperEll
- **LeftCurly:** No single-line `{ return x; }`. Multi-line: `{` on same line, body next line, `}` own line.
- **WhitespaceAround:** `{}` must be multi-line (not `{ }`).
- **LineLength:** Must be under `Checker` (not `TreeWalker`) in checkstyle.xml.

### Build

- **JAVA_HOME:** Always set to `/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home` before gradlew.
- **Stale artifacts:** Always `clean` before `test` after code changes. Incremental compilation can produce stale class files.
- **Gradle daemon:** If "NoSuchFileException" for gradle jars, run `--stop` and delete `~/.gradle/daemon/`.

### ArkTS Output Syntax

- Use `let`/`const` (never `var`). No `any` type.
- Access modifiers: `public`/`private`/`protected`. Decorators: `@` prefix.
- Use `struct` for decorated classes, `as` for casting.
- `enum` with optional values, `interface` with optional properties (`prop?: type`).

### Uncertainty Rules

- **Ghidra API:** When unsure about Ghidra plugin interfaces (Loader, Analyzer, Plugin, SLEIGH, etc.), consult the [Ghidra API Javadoc](https://ghidra.re/ghidra_docs/api/) or [Extension Notes](https://ghidra.re/courses/GhidraExternalPluginNotes.pdf) — do not guess API signatures or behavior.
- **ArkTS syntax:** When unsure about ArkTS language syntax or bytecode format, consult the [Ark Bytecode Format](https://gitee.com/openharmony/docs/blob/master/en/application-dev/arkts-utils/arkts-bytecode-format.md) or [OpenHarmony docs](https://gitee.com/openharmony/arkcompiler_ets_frontend) — do not guess syntax rules or opcode semantics.
- **Multi-version compatibility:** The decompiler must handle `.abc` files produced by different ArkTS compiler versions. When adding opcode support or syntax features, verify compatibility across known format variants (e.g., v12 vs v13 ABC header differences). Avoid hardcoding assumptions about a single version's behavior.

### Decompiler Architecture Rules

- **Three entry points:** `decompileInstructions()`, `decompileMethod()`, `decompileFile()`. All three must receive the same post-processing pipeline.
- **Post-processing pipeline order (critical):** `applyConstOptimization → inlineSingleUseVariables → mergeNestedIfConditions → detectSwitchExpressions → simplifyReturnIfTernary → convertIfElseChainToSwitch → removeUnusedVariables`. Order matters — each depends on prior transforms.
- **StatementResult.NO_OP:** Use for handled instructions producing no statement. Return `null` for unhandled (falls through to `/* opcode */` comment).
- **AST node immutability:** `BlockStatement.body`, `SwitchCase.body`, `VariableDeclaration` use `Collections.unmodifiableList`. Use `setKind()` or rebuild nodes — never `List.set()`.
- **BinaryExpression constructor:** `BinaryExpression(left, operator, right)` — left operand first.
- **ForStatement:** `init`/`condition`/`update` have NO public getters — only `getBody()` is public.
- **Dead store elimination:** Too aggressive — only inline into terminal statements (return/throw) with exactly one usage.
- **replaceVariable guard:** Never replace variable in LHS of assignment. Leaf expressions must return themselves.
- **When adding new AST types:** Add to `ExpressionVisitor.replaceVariable()`, `countVariableUsage()`, `OperatorHandler.getAccType()`, `InstructionHandler.inferNameFromExpression()`, and const optimization collectors in `ArkTSDecompiler`.

### Testing

- **Parameter naming:** Use `param_0`, `param_1` etc. (not `p0`/`p1`).
- **Test fixtures:** Use 16384-byte buffer, 200-byte spacing between areas. Space sections apart by 50+ bytes.
- **Opcode values in tests:** ALWAYS verify against `ArkOpcodes` constants. Never guess.
- **LiteralExpression in tests:** String values do NOT include surrounding quotes — `toArkTS()` adds them.
- **Robustness tests:** Use try/catch or relaxed assertions (assertNotNull) for edge cases, not specific output assertions.
- **Null safety in tests:** Always null-check `abcFile` parameter since tests pass null.

### Java API Pitfalls

- **Map.of() max 10 entries.** For 11+, use `Map.ofEntries(Map.entry(...))`.

### Agent Coordination

- Agents frequently break builds — always `git checkout HEAD -- .` and verify clean build before agent changes.
- Prefer agents for NEW files only. Reset and re-implement manually if agents break builds.
- Always compile and run full test suite after agent changes.
- Watch for code added outside class closing `}` in test files.

### Error Handling

- **Method-level isolation:** Each method in `DeclarationBuilder.buildClassDeclaration()` and `buildAnnotationClassDeclaration()` is wrapped in its own try-catch. A DisassemblyException in one method emits a comment placeholder and continues with the rest of the class.
- **Disassembler partial results:** `ArkDisassembler.disassemble()` catches DisassemblyException per-instruction and emits a synthetic "truncated" instruction, returning all successfully decoded instructions before the truncation point. Tests must account for this — FuzzingTest now checks for `"truncated"` mnemonic instead of expecting exceptions.

### 16-bit Variant Opcodes (isa.yaml multi-encoding)

Many Ark instructions have **multiple `opcode_idx` values** — an 8-bit IC slot variant and a 16-bit IC slot variant. Both are PRIMARY opcodes (not 0xFD-prefixed). The 16-bit variants use wider IC slot fields but have the same operand count and order. The decompiler normalizes them via `ArkOpcodesCompat.normalizeVariantOpcode()` so `InstructionHandler` treats them identically to their 8-bit counterparts. When adding new instruction support, check `isa.yaml` for all `opcode_idx` values — there may be 2-3 variants per instruction (e.g., `jmp` has 0x4D/0x4E/0x98 for 8/16/32-bit offsets, `mov` has 0x44/0x45/0x8F for 4/8/16-bit registers).

**Authoritative source:** `https://gitee.com/openharmony/arkcompiler_runtime_core/raw/master/isa/isa.yaml` — contains all opcode_idx values, formats, and properties. Always consult this when adding opcode support.
- **Class-level catch still exists** in `ArkTSDecompiler.decompileFile()` as a safety net for non-method errors (field parsing, decorator detection, etc.).
- **Known issue:** "Not enough bytes for instruction" errors at offset 5 are common in real .abc files — likely ABC version differences in method body format. These are now isolated to individual methods AND produce partial disassembly instead of total failure.

### Loop Iteration Notes

- **Open issues as of 2026-05-10:** #25 (test with real .abc files), #72 (module.json5), #73 (UI jumps), #184 (JEB-like UI). #183 (assignment-in-condition) was closed.
- **Priority for next iteration:** Investigate the "Not enough bytes" root cause in `AbcFile.parseCode()` — the method body format may have additional ULEB128 fields in newer ABC versions. Also: string decoding/obfuscation resolution, missing opcode handlers (unknown_91, unknown_C1, unknown_D1), and module metadata formatting improvements.
- **Wide opcode format:** Wide sub-opcodes are in the 0x80+ range. Sub-opcodes below 0x80 (like 0x28) produce `wide_unknown_XX` mnemonic with UNKNOWN format (length=1) — they don't throw. Test only recognized wide opcodes when testing truncation behavior.

---

## Build Dependencies

- Ghidra 12.0.4 (extension API)
- Java 21 (required)
- Gradle 8.x (wrapper included)
- JUnit 5 (testing)
- SpotBugs + Checkstyle (static analysis)

---

## Key References

- [Ghidra Extension Notes](https://ghidra.re/courses/GhidraExternalPluginNotes.pdf)
- [Ghidra API Javadoc](https://ghidra.re/ghidra_docs/api/)
- [Ark Bytecode Format](https://gitee.com/openharmony/docs/blob/master/en/application-dev/arkts-utils/arkts-bytecode-format.md)
- [OpenHarmony Ark Compiler](https://gitee.com/openharmony/arkcompiler_ets_frontend)
