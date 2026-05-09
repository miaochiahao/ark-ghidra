# ark-ghidra -- Ghidra ArkTS Decompiler Plugin

A [Ghidra](https://ghidra-sre.org/) extension for decompiling HarmonyOS / OpenHarmony Ark Bytecode (`.abc`) files into readable ArkTS source code.

## Features

- **ABC Loader** -- Parses `.abc` files and maps sections into Ghidra's address spaces
- **HAP Loader** -- Loads `.hap` packages, extracts and decompiles embedded `.abc` files
- **Disassembler** -- Decodes ~220 Ark bytecode instructions with full operand support
- **SLEIGH Processor Module** -- Enables Ghidra's native disassembly and pcode generation
- **Decompiler** -- Produces ArkTS source (not TypeScript / JavaScript) from bytecode
  - Control flow reconstruction: if/else, while, for, for-of, for-in, do-while, switch
  - Type inference: number, string, boolean, array element types, nullable types
  - Variable name resolution from debug info and usage context inference
  - Source line number comments from debug line number programs
  - Expression simplification: constant folding, boolean comparison, nullish coalescing
  - Pattern detection: destructuring, template literals, IIFE, optional chaining
- **Auto-Analyzer** -- Wires into Ghidra's analysis pipeline with ArkTS data types
- **Structure Viewer** -- Browse classes, methods, fields, and literal arrays in a tree panel
- **Syntax Highlighting** -- ArkTS-aware coloring in the decompiler output panel
- Supports classes, interfaces, enums, generics, decorators, async/await, generators, try/catch, module imports/exports

## Supported ArkTS Features

| Category | Features |
|----------|----------|
| Classes | Inheritance, abstract, sendable, readonly fields, getters/setters, override |
| Types | Generics, union types, nullable types, type predicates, const assertions |
| Control flow | if/else, while, for, for-of, for-in, for-await-of, do-while, switch, try/catch/finally |
| Expressions | Destructuring, template literals, nullish coalescing, optional chaining, spread |
| Functions | Arrow functions, async/generator, rest params, default params, IIFE |
| Modules | Import/export, dynamic import, namespace import, re-export |
| Operators | All arithmetic, bitwise, comparison, logical, compound assignment |
| Error handling | try/catch, multi-catch, catch-without-binding, finally |
| Async/await | async functions, for-await-of, Promise patterns |

## Requirements

- Ghidra 12.0.4 or later
- Java 21 or later

## Installation

1. Download the latest release ZIP from the [Releases](https://github.com/miaochiahao/ark-ghidra/releases) page
2. In Ghidra: **File** -> **Install Extensions** -> drag the ZIP file
3. Restart Ghidra

## Usage

### Loading ABC files

1. Drag a `.abc` file into a Ghidra CodeBrowser tool
2. The **Ark Bytecode Loader** will auto-detect the format and load it

### Loading HAP packages

1. Drag a `.hap` file into Ghidra
2. The **HAP Loader** will extract all `.abc` files and map them as separate memory blocks

### Decompiling

1. Run **Tools** -> **ArkTS** -> **Decompile to ArkTS** on a selected function
2. The decompiled ArkTS source appears in the output panel with syntax highlighting
3. Run **Tools** -> **ArkTS** -> **Show ABC Structure** to browse the file layout

## Building from Source

### Prerequisites

- Java 21 JDK
- Ghidra 12.0.4 installed locally

### Steps

```bash
export JAVA_HOME=/path/to/jdk21
# Point gradle.properties at your Ghidra install
echo "ghidra.install.dir=/path/to/ghidra_12.0.4_PUBLIC" > gradle.properties
./gradlew build
```

The extension ZIP will be at `build/dist/ark-ghidra-<version>.zip`.

### Run tests

```bash
./gradlew test
```

### Run lint

```bash
./gradlew lint
```

## Architecture

```
src/main/java/com/arkghidra/
  format/       ABC binary format parser (header, classes, methods, fields, protos, debug info)
  loader/       Ghidra program loader (ABC + HAP), shared loading utilities
  disasm/       Bytecode instruction decoder and disassembler (~220 opcodes)
  decompile/    ArkTS decompiler backend (CFG, AST, type inference, expression visitor)
  analyzer/     Ghidra auto-analyzer and ArkTS data type manager
  plugin/       UI components -- decompiler panel, structure viewer, menu actions
data/languages/ SLEIGH processor module (.cspec, .ldefs, .pspec, .slaspec, .sinc, .sla)
```

## Testing

The project has 1585+ tests across 27 test suites covering:

- ABC format parsing (header, classes, methods, fields, strings, debug info)
- Bytecode disassembly (all instruction formats, wide opcodes)
- Decompiler output quality (control flow, expressions, types, patterns)
- End-to-end decompilation (multi-class fixtures, namespace grouping)
- Error recovery and robustness (truncated bytecode, invalid jumps, edge cases)
- Performance (large file handling, string cache, timeout support)

## License

[MIT](LICENSE)
