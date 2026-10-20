# ADR 0001 — JVM as the sole compilation target

Status: **PROPOSED** (D001). Becomes accepted only when the owner accepts D001 or
records a separate decision.
Date: 2026-10-20
Supersedes: none

## Context

Arara needs a compilation target that is:
- fast to start and cheap to verify,
- statically linked by a mature runtime with a formal verifier,
- usable for real application code eventually,
- interoperable with a large existing ecosystem.

Candidate targets considered: JVM bytecode, native code via LLVM, JavaScript,
WebAssembly, and transpilation to Java source.

## Decision

Arara compiles to **JVM class files**, classfile major version **65** (JDK 21).
No other backend is authorized for D001–D006.

## Rationale

1. **Verification is external and mechanical.** The JVM verifier is an
   independent check on our backend, not a self-assessment. This is the strongest
   available guard against a backend that "looks correct".
2. **Precise, documented semantics.** Classfile structure, stack map frames, and
   type checking rules are specified by the JVM Specification.
3. **No native toolchain dependency.** No linker, no platform ABI matrix, no
   cross-compilation, no C runtime.
4. **Interop has a clear future path** through ordinary JVM calling conventions
   in D005.
5. **Deterministic artifacts are achievable.** Class files are a well-defined
   container with no embedded timestamps required by our subset.

## Rejected alternatives

| Target | Reason |
| --- | --- |
| Native via LLVM | Large toolchain surface, ABI/platform matrix, slower iteration, weaker external verification story. |
| JavaScript | Dynamic semantics would force us to define and defend a translation of static types; no verifier. |
| WebAssembly | Immature ecosystem for the intended application class; still needs a host ABI specification. |
| Java source transpilation | Hides JVM semantics behind `javac`; cannot guarantee class shape, exact diagnostics, or that we generate bytecode at all. Explicitly rejected by the directive. |

## Consequences

- The classfile version is a hard contract; changing it requires a new ADR.
- We inherit JVM platform constraints: method size limit (65535 bytes), 255
  parameter slots, no unsigned types, no `Int` overflow traps.
- We must own stack map frame generation correctness.
- Any future second backend requires a new ADR and an explicit decision, and must
  not weaken the JVM gates.

## Compliance

- `docs/spec/semantics.md` §11 defines the JVM mapping.
- `docs/progress/GATES.md` D004 requires verification evidence.
- `AGENTS.md` §7 restates the classfile version invariant.
