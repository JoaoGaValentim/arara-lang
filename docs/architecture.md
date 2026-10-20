# Arara Compiler Architecture

Status: **DRAFT — D001 design proposal.** No implementation exists. Nothing here
authorizes code.

## 1. Goals

- A pipeline of small stages with explicit, testable contracts.
- Source fidelity from lexing to diagnostics.
- A backend that produces verifiable JDK 21 class files.
- Architecture that survives the addition of later milestones without redesign.

## 2. Pipeline

```text
UTF-8 source
   |  (1) normalization: CRLF/CR -> LF
   v
Character stream
   |  (2) lexer
   v
Token stream (each token has a Span)
   |  (3) parser
   v
Abstract syntax tree (lossless enough to reprint spacing-free source for tests)
   |  (4) name resolution
   v
Bound tree (symbols attached; declarations resolved program-wide)
   |  (5) type checking
   v
Typed tree (every expression has a resolved type; no implicit conversions)
   |  (6) lowering
   v
Typed IR (explicit temporaries, explicit control flow, explicit JVM types)
   |  (7) bytecode generation (ASM)
   v
class files (major version 65)
   |  (8) verification
   v
Verified class files
```

Stages (4) and (5) may share a symbol table but must remain separate passes with
separate outputs, because name resolution errors must be reported independently
of type errors.

## 3. Stage contracts

| # | Stage | Input | Output | May fail with |
| --- | --- | --- | --- | --- |
| 1 | Normalize | raw bytes | UTF-8 decoded text, `\n` line endings | `ARA-LEX-0001` invalid UTF-8 |
| 2 | Lex | text | tokens + diagnostics | `ARA-LEX-*` |
| 3 | Parse | tokens | AST + diagnostics | `ARA-PARSE-*` |
| 4 | Resolve | AST | bound AST + diagnostics | `ARA-NAME-*` |
| 5 | Type | bound AST | typed AST + diagnostics | `ARA-TYPE-*` |
| 6 | Lower | typed AST | IR | internal invariants only |
| 7 | Emit | IR | `Map<String, byte[]>` | `ARA-JVM-*` |
| 8 | Verify | class bytes | verified bytes or failure | `ARA-JVM-0002` |

Rules:

- Every stage returns diagnostics rather than throwing for user errors. Internal
  invariant violations may throw a dedicated `AraraInternalError` that carries a
  stage identifier and never a fabricated source span.
- Stages 2–5 may return a partial result with errors so later stages can be
  skipped deterministically. Compilation stops before lowering if any error
  diagnostic exists.
- Stage 8 runs on the exact bytes that will be written; it never re-generates. It
  uses `CheckClassAdapter` with data-flow analysis. The second verification layer
  (`-Xverify:all` under a real JVM) belongs to the test harness, not to the
  compiler process, because it requires loading and executing the class.

## 4. Data model decisions

### Spans

```text
Span  = { start: Offset, end: Offset }        // half-open, byte-independent
Pos   = { offset: Int, line: Int, column: Int }  // line/column 1-based, code points
```

A `SourceFile` owns the normalized text and a line-index that maps offsets to
line/column in O(log n). Spans are attached to tokens, AST nodes, symbols, and
diagnostics. A span never crosses compilation units.

### Diagnostics

```text
Diagnostic = {
  code: String,            // ARA-<STAGE>-<NNNN>
  severity: ERROR | WARNING | NOTE,
  primary: Span,
  message: String,
  related: List<{ span: Span, note: String }>,
  hint: String?
}
```

Diagnostics are collected in a `DiagnosticSink`. Ordering is deterministic:
primary offset ascending, then code ascending, then insertion order.

### AST

- One sealed hierarchy per syntactic category; no `Object` fields.
- Every node carries a `Span` and an identity suitable for stable diagnostics.
- The parser does not attach types, symbols, or JVM names to the AST.

### Symbols and scopes

- Resolution produces explicit `Symbol` objects for functions, parameters, and
  locals.
- Locals are resolved to a `LocalId` allocated at resolution time so that type
  checking and lowering do not re-run scope rules.
- No shadowing: a name may not be redeclared in an enclosing scope. Same-scope
  duplicates are also errors.

### Typed IR

- Explicit `Int`, `Boolean`, `String`, `Unit` types; no union or bottom type in
  the initial subset.
- Explicit control-flow graph per function: basic blocks, branches, returns.
- No implicit conversions, no implicit `Unit` values, no autoboxing.
- Local variable slots are assigned in lowering, not in emission, so slot
  allocation is testable without bytecode.

## 5. Backend strategy

- ASM (`org.ow2.asm:asm`, `asm-util`, `asm-analysis`) is the bytecode library.
  Version is pinned only when D004 is authorized; see
  `docs/adr/0002-compiler-host.md` for the verified current toolchain.
- Class emission uses `ClassWriter.COMPUTE_FRAMES | COMPUTE_MAXS` with a
  **custom `getCommonSuperClass`** that never loads application classes and never
  consults the compiler's own classloader. For the initial subset the reference
  types that can appear in a descriptor are `java/lang/String` and every
  generated unit class; all generated unit classes share only
  `java/lang/Object` as a supertype, so the resolver can answer from a closed
  table of `java/lang/Object`, `java/lang/String`, and the generated classes. A
  generated class may store a `String` reference in a local, but never a
  reference to a generated unit class, so no merged reference frame for a user
  class can arise in practice; the table exists to make that failure mode
  impossible rather than merely unlikely.
- Verification is two-layered and both layers are mandatory:
  1. `CheckClassAdapter.verify` with data-flow analysis over the emitted bytes.
  2. Loading and executing under a JVM started with `-Xverify:all` in the
     conformance harness.
- Class files must be byte-identical across repeated runs on the same input and
  compiler build. If that becomes impossible, an ADR must record the cause.
- Class emission MUST be deterministic in constant-pool ordering: constants are
  inserted in a documented, input-derived order rather than from a hash-based
  collection, because `HashMap` iteration order is not a contract.

## 6. Command-line contract

The full surface is defined here so that stages have a stable destination. Only
the D002 subset is implemented in D002; the rest is gated by later milestones.
The `.ara` suffix in the examples is the **proposed** extension (`D001-06`,
`OQ-8`); no command depends on the suffix.

```text
arara lex     <file.ara>...            # D002: tokens and lexical diagnostics
arara parse   <file.ara>...            # D002: AST dump and parse diagnostics
arara check   <file.ara>...            # D003: name and type diagnostics, IR dump
arara compile [--out <dir>] <file.ara>...   # D004: class files
arara run     [--out <dir>] <file.ara>...   # D004: compile, verify, execute
```

- `lex` and `parse` never write class files and never execute user code.
- `compile` writes class files and never executes user code.
- `run` compiles, verifies, then launches the entry class in a child JVM.
- Commands that accept multiple units treat the set as the program
  (`semantics.md` §1.3). The entry class is the unit that declares `main`
  (§1.5), so no flag selects it.
- Exit codes: `0` success, `1` user program failure, `2` compile-time
  diagnostics, `3` internal error, `4` verification failure.
- `--out` defaults to the current directory; it is created if absent.
- This CLI contract is normative for D002 (`D002-G11` covers `lex`/`parse`) and
  for `compile`/`run` in D004 (`D004-G9`). It is not implemented in D001.

## 7. Alternatives considered

| Alternative | Why not chosen now |
| --- | --- |
| Hand-written recursive descent | Chosen. Full control of spans, recovery, and diagnostics; no generator dependency. |
| Parser generator (ANTLR, JavaCC) | Generator output obscures span control and error recovery; adds a build dependency and a generated-code review burden. Reconsider only if grammar complexity grows past a documented threshold. |
| Tree-walking interpreter first | Would create a second semantic implementation to keep in sync; the JVM backend is the product, so the typed IR should be the single semantic artifact. |
| Direct AST-to-bytecode without IR | Control-flow and slot allocation become untestable and entangled with ASM; also blocks later optimization and Java interop work. |
| Generate Java source and call `javac` | Not a real backend: it hides JVM semantics behind `javac`, prevents precise diagnostics, and cannot guarantee classfile shape or determinism. Explicitly rejected by the directive. |
| Kotlin compiler as a fork | Inherits an enormous surface area, licensing and maintenance burden, and contradicts the "small language" goal. |
| Use `java.lang.invoke` / dynamic languages | Nothing in the initial subset needs invokedynamic; it adds complexity and verification surface. |

## 8. Testing architecture (for later milestones)

- **Unit tests** per stage, in memory.
- **Golden tests** for tokens, AST dumps, diagnostics, and class files.
- **Negative tests** asserting diagnostic codes and spans, not message text.
- **Execution tests** running generated classes in a child JVM.
- **Verification tests** running `CheckClassAdapter` and `-Xverify:all`.
- **Differential tests** comparing Arara arithmetic/boolean results to a small
  hand-written Java reference matrix for the same operations.
- **Interop tests** (D005 only) compiling against real Java classes.

Gate definitions live in `docs/progress/GATES.md`.

## 9. Non-architecture

The following are explicitly out of architectural scope for D001 and are not
placeholders for "later cleanup":

- Incremental compilation and caches.
- Language server protocol support.
- Debug information and source maps.
- Optimization passes beyond what the JVM does.
- Multiple backends and target abstraction layers.
