# ark-ghidra — Ghidra ArkTS Decompiler Plugin

## Project Overview

A Ghidra plugin that provides native decompilation support for ArkTS (the language used by HarmonyOS/OpenHarmony applications). The goal is to enable security researchers and reverse engineers to analyze HarmonyOS `.abc` (Ark Bytecode) files within Ghidra.

**Tech stack:** Java 17 + Gradle + Ghidra 12.0.4 Extension API

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

### Core components (to be built)

1. **ABC Loader** — Parse `.abc` file format, load into Ghidra's program model
2. **ArkTS Analyzer** — Module that wires into Ghidra's auto-analysis pipeline
3. **Bytecode Decoder** — Disassemble Ark bytecode instructions
4. **Pcode Translator** — Map Ark bytecode ops to Ghidra's pcode for decompilation
5. **Type System** — Represent ArkTS types (dynamic, static, generics) in Ghidra's data type manager
6. **UI Integration** — Plugin menu entries, tree viewers for ABC structure

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

### Feature priority order (initial)

1. Project skeleton: Gradle build, Ghidra extension manifest, module wiring
2. ABC file format parser (header, string table, method table)
3. Bytecode instruction decoder
4. Ghidra program loader (maps ABC sections into address spaces)
5. Pcode translation layer (enables decompilation)
6. Auto-analyzer integration
7. Data type manager for ArkTS types
8. UI components (menu, structure viewer)
9. Test coverage expansion and fuzzing
10. Documentation and release packaging

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
