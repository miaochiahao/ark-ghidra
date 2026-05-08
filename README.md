# ark-ghidra -- Ghidra ArkTS Decompiler Plugin

A [Ghidra](https://ghidra-sre.org/) extension for decompiling HarmonyOS / OpenHarmony Ark Bytecode (`.abc`) files into readable ArkTS source code.

## Features

- **ABC Loader** -- Parses `.abc` files and maps sections into Ghidra's address spaces
- **Disassembler** -- Decodes ~220 Ark bytecode instructions with full operand support
- **SLEIGH Processor Module** -- Enables Ghidra's native disassembly and pcode generation
- **Decompiler** -- Produces ArkTS source (not TypeScript / JavaScript) from bytecode
- **Auto-Analyzer** -- Wires into Ghidra's analysis pipeline with ArkTS data types
- **Structure Viewer** -- Browse classes, methods, fields, and literal arrays in a tree panel
- **Type Inference** -- Infers ArkTS types and annotates decompiled output
- Supports classes, interfaces, enums, decorators, async/await, generators, try/catch

## Requirements

- Ghidra 12.0.4 or later
- Java 21 or later

## Installation

1. Download the latest release ZIP from the [Releases](https://github.com/miaochiahao/ark-ghidra/releases) page
2. In Ghidra: **File** -> **Install Extensions** -> drag the ZIP file
3. Restart Ghidra

## Usage

1. Drag a `.abc` file into a Ghidra CodeBrowser tool
2. The **Ark Bytecode Loader** will auto-detect the format and load it
3. Run **Tools** -> **ArkTS** -> **Decompile to ArkTS** on a selected function
4. Run **Tools** -> **ArkTS** -> **Show ABC Structure** to browse the file layout

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
  format/       ABC binary format parser (header, classes, methods, fields, protos)
  loader/       Ghidra program loader that maps ABC sections into address spaces
  disasm/       Bytecode instruction decoder and disassembler
  decompile/    ArkTS decompiler backend (CFG, AST, type inference)
  analyzer/     Ghidra auto-analyzer and ArkTS data type manager
  plugin/       UI components -- decompiler panel, structure viewer, menu actions
data/languages/ SLEIGH processor module (.cspec, .ldefs, .pspec, .slaspec, .sinc, .sla)
```

## Project Status

This is an early-stage research tool. Ark Bytecode is complex and not fully documented; some instructions or file format features may not be supported. Issues and pull requests are welcome.

## License

[MIT](LICENSE)
