# ark-ghidra — Ghidra ArkTS Decompiler Plugin

## Project Overview

A Ghidra plugin that provides native decompilation support for ArkTS (the language used by HarmonyOS/OpenHarmony applications). The goal is to enable security researchers and reverse engineers to analyze HarmonyOS `.abc` (Ark Bytecode) files within Ghidra.

**Tech stack:** Java 21 + Gradle + Ghidra 12.0.4 Extension API

**Repository:** https://github.com/miaochiahao/ark-ghidra

---

## Architecture

```
ark-ghidra/
├── CLAUDE.md              # This file — project rules & context
├── README.md              # User-facing documentation
├── build.gradle           # Gradle build with Ghidra extension support
├── settings.gradle
├── gradle.properties      # Ghidra install dir and other config
├── src/
│   ├── main/
│   │   ├── java/          # Plugin source code
│   │   └── resources/     # Ghidra extension metadata (Module.manifest, etc.)
│   └── test/
│       └── java/          # Unit and integration tests
├── data/                  # Test .abc binaries and fixtures
├── scripts/               # Build/release helper scripts
└── ghidra_scripts/        # Ghidra scripting bridge (optional)
```

### Core components

1. **ABC Loader** — Parses `.abc` files, maps sections into Ghidra address spaces, creates namespaces/functions
2. **Bytecode Decoder** — Disassembles ~220 Ark bytecode instructions with full operand support
3. **SLEIGH Processor Module** — Instruction encoding/disassembly (pcode generation pending)
4. **ArkTS Decompiler** — Custom decompiler: CFG → AST → ArkTS source code
5. **Auto-Analyzer** — Wires into Ghidra's analysis pipeline with ArkTS data types
6. **UI Plugin** — Menu actions, ABC structure tree viewer, decompiler output panel

---

## Development Environment Setup

### Prerequisites (install before first build)

```bash
# Java 21 (required — Ghidra 12.0.4 uses Java 21 class files)
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"

# Ghidra 12.0.4 (installed at ~/Documents/ghidra_12.0.4_PUBLIC)
# Used for extension API at compile time — path set in gradle.properties
```

### Build

```bash
./gradlew build                  # Compile plugin
./gradlew test                   # Run tests
./gradlew lint                   # Run static analysis (SpotBugs/Checkstyle)
```

### Install plugin into Ghidra

Build produces a ZIP in `build/dist/`. In Ghidra: File → Install Extensions → drag ZIP.

---

## Claude Loop Workflow

This project uses a **self-directed Claude loop** for autonomous development. Each iteration follows:

### Iteration cycle

1. **Propose** — Claude proposes a new feature/improvement and writes it as a plan
2. **Implement** — Claude writes the code, tests, and documentation
3. **Verify** — Run `./gradlew build test lint` — all must pass
4. **Commit & Push** — Commit with descriptive message, push to GitHub
5. **Learn** — Capture lint errors and new conventions into the "Lint Rules" section below
6. **Repeat** — Propose the next most impactful feature

### Feature priority order (current)

1. ~~Project skeleton: Gradle build, Ghidra extension manifest, module wiring~~ DONE
2. ~~ABC file format parser (header, string table, method table)~~ DONE
3. ~~Bytecode instruction decoder~~ DONE
4. ~~Ghidra program loader (maps ABC sections into address spaces)~~ DONE
5. ~~SLEIGH processor module (disassembly)~~ DONE
6. ~~ArkTS decompiler (CFG, AST, type inference)~~ DONE
7. ~~Auto-analyzer integration~~ DONE
8. ~~UI components (menu, structure viewer, output panel)~~ DONE
9. ~~Release packaging, CI, documentation~~ DONE
10. ~~Real .abc file integration testing (#18)~~ DONE
11. ~~Pcode generation for instructions (#19, #21, #22)~~ DONE
12. ~~Complete ABC format parser — debug info, source maps (#20)~~ DONE
13. ~~Fuzzing and robustness testing (#23)~~ DONE
14. Decompiler quality: switch with fall-through, nested try/catch, ternary expressions
15. Real .abc file support: test with actual HarmonyOS compiler output
16. Performance: large file handling and incremental decompilation

### Rules for the loop

- **Never skip tests.** Every feature must have at least one test before commit.
- **Never skip lint.** Fix all lint errors before pushing.
- **One feature per commit.** Small, reviewable commits.
- **Push after each feature.** `git push origin main` after every green build.
- **Update this file** whenever a new convention or lint rule is discovered.

---

## Git Conventions

- **Branch:** Work directly on `main` for now (single-developer project)
- **Commit format:**
  ```
  feat: add ABC file header parser

  Parses the ABC magic number, version, and top-level table offsets.
  Includes unit test with a minimal valid ABC fixture.
  ```
  Prefixes: `feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`, `lint:`
- **Push:** After every completed feature with passing tests

---

## Code Style

- **Language:** Java 21 (required by Ghidra 12.0.4 — class version 65.0)
- **Indent:** 4 spaces, no tabs
- **Max line length:** 120 characters
- **Naming:** standard Java conventions — PascalCase classes, camelCase methods/fields, UPPER_SNAKE constants
- **Imports:** no wildcard imports; order: java. → javax. → ghidra. → com. → project
- **Exceptions:** use Ghidra's exception hierarchy where possible (ghidra.util.exception)
- **Logging:** use Ghidra's Msg class (Msg.info, Msg.warn, Msg.error) with a static OWNER string
- **Null:** no javax.annotation in Ghidra 12.0.4 classpath — omit `@Nonnull`/`@Nullable` or use `ghidra` annotations if available
- **Tests:** JUnit 5, use descriptive test method names: `testParseMethodTable_withThreeEntries_returnsCorrectCount()`
- **No TODO comments** — either implement it or create a GitHub issue

---

## Lint Rules (auto-accumulated)

_This section is updated automatically when lint reveals new patterns to enforce._

<!-- LINT_RULES_START -->
- **Checkstyle 10.15.0 rules:** AvoidStarImport, ConstantName, EmptyBlock, LeftCurly(eol), NeedBraces, RightCurly, LineLength(120), Indentation(4), MethodParamPad, ParenPad, TypecastParenPad, WhitespaceAfter, WhitespaceAround, ModifierOrder, RedundantModifier, UnusedImports, UpperEll
- **LeftCurly:** No single-line methods with `{ return x; }`. Always use multi-line format: `{` on same line as closing paren, body on next line, `}` on its own line.
- **WhitespaceAround:** Empty braces `{}` must have a space: `{ }` is wrong, use multi-line instead.
- **LineLength must be under Checker (not TreeWalker):** In checkstyle.xml, `LineLength` must be a direct child of `Checker`, not `TreeWalker` (changed in checkstyle 10.x).
- **JAVA_HOME:** Always set `JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home` before running gradlew. (Ghidra 12.0.4 requires Java 21 class files; Java 17 won't work.)
- **Gradle daemon:** If builds fail with "NoSuchFileException" for gradle jars, stop the daemon: run gradle with `--stop` and delete `~/.gradle/daemon/` cache.
- **Test fixture binary layout:** When building synthetic .abc binary fixtures in tests, ensure code sections and method structures don't overlap in the byte buffer. Use sufficiently spaced offsets.
- **Ghidra 12.0 Loader API:** `AbstractProgramWrapperLoader` has abstract `load(Program, ImporterSettings)`. `loadProgram(ImporterSettings)` and `loadProgramInto(Program, ImporterSettings)` are concrete (not abstract) — they call `load()`. `ImporterSettings` is a `Record` with `provider()`, `monitor()`, `log()`, `loadSpec()`, `options()`, `consumer()`.
- **Ghidra JAR locations:** Loader/opinion classes are in `Ghidra/Features/Base/lib/Base.jar`, not in Framework jars. Use `fileTree(dir: ghidraDir, include: '**/lib/*.jar')` to include all.
- **ByteProvider in tests:** `RandomAccessByteProvider` triggers `Application.initializeApplication()`. Use a custom `ByteProvider` implementation for unit tests to avoid Ghidra init overhead.
- **SymbolTable namespace API:** Use `getOrCreateNameSpace()` (not `createClassNamespace` which doesn't exist). Use `createClass()` for GhidraClass namespaces.
- **FunctionManager.createFunction:** Takes `(String, Namespace, Address, AddressSetView, SourceType)` — requires `AddressSet` for body, not bare `Address`.
- **AbstractAnalyzer API (Ghidra 12.0):** Extend `AbstractAnalyzer(name, description, AnalyzerType)`. Override `canAnalyze(Program)`, `getDefaultEnablement(Program)`, and `added(Program, AddressSetView, TaskMonitor, MessageLog)`. Use `AnalyzerType.BYTE_ANALYZER` for bytecode analysis.
- **Ghidra Analyzer registration:** Analyzers implementing `ExtensionPoint` are auto-discovered. Must be on the classpath. Name must match pspec property `Analyzers.<name>`.
- **Ark bytecode instruction formats:** 30+ formats covering all instruction layouts. Wide prefix (0xFD) changes operand sizes (8-bit → 16-bit). Jump offsets are signed. Packed 4-bit operands use bit fields.
- **Duplicate opcode handling:** When generating opcode lookup tables, ensure each opcode maps to exactly one mnemonic. Watch for duplicates like `neg 0x1F`, `createarraywithbuffer 0x06/0x81`, `stsuperbyname 0xD0`, `stsuperbyvalue 0xC9`.
- **CFG construction:** Leaders are at branch targets, exception handlers, and offsets after terminators (return/jmp). Backward edges indicate loops. Forward conditional edges are if/else.
- **Decompiler variable tracking:** v0-v255 are virtual registers. First N are parameters (from AbcCode.numArgs). Use `let` for first assignment, bare name for reassignment. Accumulator (acc) is implicit.
- **ArkTS syntax:** Use `let`/`const` (not `var`). No `any` type. Type annotations use `: type` syntax. Access modifiers are `public`/`private`/`protected`. Decorators use `@` prefix.
- **Test fixture complexity:** Multi-class fixtures need careful offset management. Use AbcTestFixture helper for building complex ABC binaries. Space string area, class defs, code sections, and indexes apart by 50+ bytes.
- **SLEIGH token naming:** Each token position needs unique field names (e.g., `vreg1`, `vreg2`, not `vreg` shared). Use `inst_byte` not `instruction` for the opcode token. Register attachment must be per-position: `attach variables vreg1 [...]`.
- **SLEIGH jump targets:** Use constraint-based `reloc` with `[ reloc = inst_start + simm; ]` then `goto [reloc]`. Direct `goto simm` doesn't work for relative addressing.
- **SLEIGH compilation:** Ghidra 12.0 can compile SLEIGH at runtime or via `sleigh` tool. The `.sla` binary must be in `data/languages/` alongside `.slaspec`. Include it in the extension ZIP.
- **Type inference in decompiler:** Track types per register. Arithmetic ops → `number`, comparisons → `boolean`, ldtrue/ldfalse → `boolean`, ldai → `number`, lda.str → `string`. Skip `Object` annotation to reduce noise.
- **Stale build artifacts:** If tests fail after agent changes, run `clean` first. Gradle incremental compilation can produce stale class files.
- **Ghidra Plugin API (12.0):** Extend `Plugin(PluginTool)`. Use `DockingAction` for menu/toolbar actions. Use `ComponentProvider` for panels. Register actions in plugin constructor via `tool.addAction()`. Position providers with `getWindowInstance()` and Ghidra window areas.
- **UI testing without Ghidra runtime:** Test static helper methods and constants from plugin/action classes. Avoid testing code that calls Ghidra APIs (requires `Application.initializeApplication()`). Package-private access helps testability.
- **ArkTS decompilation pipeline:** Full method: disassemble → CFG → control flow reconstruction → type inference → expression tree → statement generation → pretty-print. Full file: iterate classes → decompile methods → resolve imports → generate output.
- **ArkTS-specific syntax:** Use `struct` for decorated classes, `let`/`const` (never `var`), no `any` type, `as` for casting, `@Decorator` syntax, `enum` with optional values, `interface` with optional properties (`prop?: type`).
- **Extension ZIP packaging:** Ghidra expects `<module_name>/<files>` layout. Use `processExtensionProperties` task to substitute `@extension.name@`/`@extension.version@` tokens. Include `.sla` compiled SLEIGH binary.
- **Parallel subagent coordination:** Launch independent work streams (e.g., release packaging + decompiler improvements) in parallel. Each agent does its own build+lint verification. After both complete, do one final unified build before committing.
- **Try/catch decompilation:** Use `AbcCode.getTryBlocks()` → `AbcTryBlock.getCatchBlocks()` → `AbcCatchBlock` to reconstruct exception handling. Map try start/end PC ranges to CFG block addresses. Catch-all blocks (typeIdx=0) map to `finally`.
- **Jump offset calculation:** `jmp +0` at offset 0 with instruction length 2 gives target = 0+2+0 = 2 (not 0). For infinite loop (jmp to self), need negative offset = -instruction_length (e.g., `0xFE` for 2-byte jmp).
- **Parameter naming convention:** Use `param_0`, `param_1` etc. (not `p0`/`p1`) for better readability. Falls back to untyped when no proto info available.
- **Test count tracking:** 598 tests across 13 test suites (as of 2026-05-09). After any decompiler change, check that existing tests still match expected output strings.
- **ABC debug info parsing:** Tags 0x07 (SOURCE_FILE), 0x03 (DEBUG_INFO) in class/method tag values. Debug info contains line_start, num_params, param name string offsets, constant pool. LNP uses DWARF v3 state machine with special opcodes.
- **Realistic test fixture design:** Use 16384-byte buffer with 200-byte spacing between areas (strings at 200, classes at 800, code at 2000, protos at 6000, etc.). Encode methods with ULEB128 for vregs/args/codeSize/triesSize.
- **Debug parameter name resolution:** `AbcFile.getDebugInfoForMethod()` → `AbcDebugInfo.getParameterNames()` → pass to `MethodSignatureBuilder.buildParams(proto, numArgs, debugNames)`. Falls back to `param_N` for unnamed.
- **SLEIGH pcode:** Most Ark instructions already have pcode. Use `acc` (32-bit) and `acc64` (64-bit) for accumulator. NaN/infinity must use 64-bit local temp + acc64. Custom pcodeops (arkCallRuntime, arkThrow, etc.) for complex operations.
- **Parser robustness:** AbcReader validates all reads with `checkRemaining()`. ULEB128 max 5 bytes. AbcFile validates header offsets. AbcFormatException for descriptive error messages. All 63 fuzzing tests pass.
- **Break/continue detection:** Track loop context (headerOffset, endOffset) via a stack in DecompilationContext. `isBreakJump()` checks if jump target == endOffset, `isContinueJump()` checks if target == headerOffset. Push context on loop entry, pop on exit.
- **Short-circuit evaluation (&&/||):** Detect via consecutive conditional branches: condition→true_branch→merge←false_branch for &&, condition→false_branch→merge←true_branch for ||. Use `detectShortCircuitPattern()` with PatternType enum.
- **Ternary expression detection:** Pattern: condition→true_branch→merge←false_branch where both branches assign to same variable or produce a value. Use `detectTernaryPattern()` and `processTernary()` to emit `cond ? val1 : val2`.
- **Nested try/catch:** Process try/catch regions recursively. Inner regions handled before outer. Each region maps to a try/catch/finally block in ArkTS output.
<!-- LINT_RULES_END -->

---

## Build Dependencies

- Ghidra 12.0.4 (extension API)
- Java 21 (Ghidra 12.0.4 requires Java 21+)
- Gradle 8.x (wrapper included)
- JUnit 5 (testing)
- SpotBugs + Checkstyle (static analysis)

---

## Key References

- [Ghidra Extension Notes](https://ghidra.re/courses/GhidraExternalPluginNotes.pdf)
- [Ghidra API Javadoc](https://ghidra.re/ghidra_docs/api/)
- [Ark Bytecode Format](https://gitee.com/openharmony/docs/blob/master/en/application-dev/arkts-utils/arkts-bytecode-format.md)
- [OpenHarmony Ark Compiler](https://gitee.com/openharmony/arkcompiler_ets_frontend)
