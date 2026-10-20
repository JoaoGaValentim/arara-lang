# 01 — Compiler Invariants

This rule file is subordinate to `AGENTS.md` and to `docs/spec/*`. It lists
engineering invariants that every later implementation phase must satisfy. It
does not authorize implementation.

## Stage contracts

Each compiler stage has one input type, one output type, and no hidden global
state. The stages are:

```text
Source text  ->  Token stream     (lexer)
Token stream ->  AST              (parser)
AST          ->  Bound AST        (name resolution)
Bound AST    ->  Typed AST        (type checking)
Typed AST    ->  Typed IR         (lowering)
Typed IR     ->  class files      (bytecode generation)
class files  ->  verified classes (verification)
```

Rules:

- A stage never reaches back into a later stage's data.
- A later stage never re-lexes or re-parses source text.
- Source positions flow forward unchanged until the final diagnostic is emitted.
- No stage mutates the AST produced by an earlier stage in place; each stage
  produces a new tree or an annotated copy.

## Determinism

- Identical input bytes, identical compiler version, and identical options must
  produce byte-identical class files. If a later milestone cannot guarantee
  byte-identical output (for example due to timestamps), the difference must be
  documented and justified in an ADR.
- Diagnostics must be emitted in a deterministic order: primary sort by source
  offset, secondary sort by diagnostic code.

## Diagnostics

- Diagnostic codes are stable, namespaced, and never reused for a different
  meaning: `ARA-LEX-####`, `ARA-PARSE-####`, `ARA-NAME-####`, `ARA-TYPE-####`,
  `ARA-JVM-####`.
- A diagnostic includes: code, severity, primary span, message, optional related
  spans with notes, and an optional hint.
- Error recovery must be specified per stage before it is implemented. Recovery
  may suppress cascading errors but may never invent successful parses.

## Types and values

- The type system is explicitly specified in `docs/spec/semantics.md`. No type
  rule may be delegated to Java, Kotlin, or JVM behavior by default.
- Values of type `Int` map to JVM `int`, `Boolean` to `boolean`, `String` to
  `java.lang.String`, and `Unit` to `void` for returns and
  `kotlin.Unit`-free local representation as specified in the semantics
  document.
- No implicit coercions exist between `Int`, `Boolean`, and `String`.

## JVM emission

- Target classfile major version is 65. No other version is authorized.
- Every method must have a stack map frame set valid for verification, or be
  computed with a documented and verified frame-computation strategy.
- Every generated class must pass `CheckClassAdapter` with data-flow analysis and
  must load and execute under `-Xverify:all`.
- The entry point mapping is exactly as specified in
  `docs/spec/semantics.md`; the backend may not invent alternative entry points.

## Evidence

- A stage is not "done" because its unit tests pass. It is done when its
  conformance tests, negative tests, and integration evidence are recorded in
  `docs/progress/GATES.md` with a commit hash.
- Golden files are generated once, reviewed by a human or an independent agent,
  and then frozen. Regenerating goldens to make a test pass is prohibited and is
  reported as a failed gate.
