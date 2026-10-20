---
description: Compiler invariants that apply to any future compiler source file.
applyTo: "src/**/*.java,src/**/*.kt,src/**/*.ara,test/**/*,**/*.gradle.kts,build.gradle.kts"
---

# Compiler invariants

No compiler source exists today. If you are editing a file matched by this
pattern, the repository has left the documentation-only phase, which means an
owner decision authorized it. Verify that authorization in
`docs/progress/DECISIONS.md` before writing anything.

The invariants below come from `AGENTS.md` §7 and `docs/spec/semantics.md`. They
are summaries; the specification is normative.

- Source is UTF-8. Normalize `\n`, `\r\n`, and `\r` to `\n` before lexing.
- Every token carries a half-open span `[start, end)` with 1-based line and column
  counted in Unicode code points.
- Every diagnostic carries a stable `ARA-<STAGE>-<NNNN>` code, a severity, a
  primary span, and optional related spans with notes.
- Never abort at the first error when more errors can be reported soundly.
- Static typing is mandatory. No implicit conversions among `Int`, `Boolean`,
  `String`. No nullable type in the initial subset.
- `val` is single-assignment; assigning to it again is a compile-time error.
- `Int` is 32-bit two's complement with Java `int` overflow semantics. Division by
  zero throws at runtime. Do not leave either behavior implicit.
- `String` equality is structural over code units, never reference identity.
- Evaluation order is left-to-right and fully specified.
- Every reachable non-`Unit` function must return on all paths.
- Unsupported constructs are rejected with a specific diagnostic, never accepted
  with undefined behavior.
- Emit classfile major version 65 only. No other target is authorized.
- Every emitted class must pass `CheckClassAdapter` analysis and the JVM verifier
  (`-Xverify:all`) before it counts as evidence.

## Do not

- Add a dependency, backend target, or build plugin without a recorded decision.
- Introduce a deferred feature from `docs/charter.md` §4 to make an implementation
  simpler.
- Mark a gate `PASS` on a build that succeeded. Compilation is not correctness.
