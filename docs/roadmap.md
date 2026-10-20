# Arara Roadmap

Status: **DRAFT — D001 planning proposal.** Milestone boundaries are planning
limits. They are not implementation authorization, and each milestone requires a
recorded owner decision before it starts.

## Milestone model

Each milestone has: a scope, entry criteria, deliverables, exit gates, and a
stop rule. Exit gates are objective and are recorded in
`docs/progress/GATES.md`. A milestone is complete only when the owner accepts its
report and records that acceptance in `docs/progress/DECISIONS.md`.

## D001 — Foundation

**Goal:** a trustworthy engineering environment and a reviewable language
contract, with zero implementation.

**Deliverables**

- `AGENTS.md`, `.kilo/rules/*`, `.kilo/kilo.jsonc`, Kilo commands, auditor agent.
- Recovery ledger: `STATE.md`, `CONTINUATION.md`, `DECISIONS.md`, `GATES.md`.
- `charter.md`, `architecture.md`, `roadmap.md`, ADRs 0001–0003.
- Specification drafts: `lexical.md`, `syntax.ebnf`, `semantics.md`,
  `conformance-plan.md`.
- A validated toolchain and model/compaction configuration record.

**Exit gates** (see `GATES.md` §D001)

- No implementation files exist.
- All documents are internally consistent and cross-referenced.
- Every environment claim has a recorded command and result.
- Recovery can be performed from repository files alone, demonstrated by a
  fresh-session run or reported as a limitation.

**Not authorized:** lexer, parser, type checker, IR, backend, build files for the
compiler, tests, CI.

## D002 — Lexer and parser

**Goal:** text to AST, with complete source fidelity and diagnostics.

**Scope**

- UTF-8 decoding and newline normalization.
- Token model, spans, keywords, literals, comments, invalid-token diagnostics.
- Recursive-descent parser for the D001 grammar.
- AST model and a stable AST dump format for golden tests.
- Parse error recovery rules and negative diagnostics.
- CLI subcommand for `lex`/`parse` diagnostics only.

**Explicitly out of scope:** name resolution, types, IR, bytecode, execution.

**Exit gates**

- Token golden tests cover every lexical rule and every invalid case.
- Grammar conformance: every EBNF production has at least one accepted and one
  rejected example where meaningful.
- Spans verified on multi-line and Unicode input, including line/column in code
  points.
- Recovery behavior tested: a single error must not produce cascading noise.
- No type or backend code exists.

## D003 — Semantic frontend

**Goal:** resolve, type-check, and lower the approved subset to typed IR.

**Scope**

- Program-level symbol collection; duplicate and missing entry point errors.
- Scope rules, no shadowing, `val` single assignment enforcement.
- Type checking, return-path analysis, operand typing, no implicit conversions.
- Constant-free typed IR with explicit control flow.
- Semantic diagnostics with stable codes.

**Explicitly out of scope:** bytecode, execution, Java interop.

**Exit gates**

- Every semantic rule in `docs/spec/semantics.md` maps to a positive and, where
  applicable, a negative test.
- Return-path analysis proven on nested `if`/`else`, early `return`, and
  unreachable code cases.
- IR is printable and stable; golden IR tests exist.
- No backend code exists.

## D004 — JVM backend

**Goal:** typed IR to verified, executable class files.

**Scope**

- Type and signature mapping, class naming, entry-point method generation.
- Stack and local allocation with correct stack map frames.
- `println` emission, integer arithmetic and division-by-zero behavior, string
  equality via `String.equals`, boolean short-circuiting.
- Verification integration and execution harness.

**Exit gates**

- Every emitted class passes `CheckClassAdapter` and `-Xverify:all`.
- Executable examples produce expected output for every specified behavior.
- Division by zero throws `ArithmeticException` at runtime, verified by a test.
- Class file major version is 65 in every artifact.
- No Java interop claim is made.

## D005 — Java interoperability

**Goal:** verified bidirectional interaction with real Java code.

**Scope**

- Calling specified Java static methods and constructors from Arara.
- Calling Arara functions from Java.
- Type mapping rules for the supported subset only.
- Explicit restrictions and diagnostics for unsupported interop.

**Exit gates**

- Real cross-language compile-and-run tests, both directions, in CI.
- Documented restrictions; no claim beyond tested cases.
- No reflection-based bridging.

## D006 — Language maturity

**Goal:** controlled growth with backward-compatibility policy.

**Scope:** selected features from the deferred list, each with its own
specification, ADR where needed, and gates. Tooling and standard-library growth
as separately authorized sub-milestones.

**Exit gates:** defined per feature; there is no blanket authorization.

## Cross-cutting rules

- A milestone may not begin before the previous milestone's acceptance is
  recorded.
- A milestone may deliver less than planned; it may not deliver more than
  authorized.
- Any change to a frozen document requires a new decision that names the
  superseded rule.
