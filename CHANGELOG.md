# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2026-05-09

### Added

- ABC file format parser (header, classes, methods, fields, protos, literal arrays, try/catch blocks)
- Ghidra loader for `.abc` files with auto-format detection
- SLEIGH processor module for Ark bytecode (~220 instructions)
- Bytecode disassembler with full instruction decoder
- ArkTS decompiler backend (control-flow graph, expression/statement AST, type inference, method signatures)
- Ghidra auto-analyzer with ArkTS data type manager
- UI plugin with decompiler output panel and ABC structure tree viewer
- 420 unit tests covering parser, disassembler, decompiler, loader, analyzer, and plugin
