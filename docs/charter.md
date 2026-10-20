# Arara Charter

Status: **DRAFT — submitted for owner review as part of D001. Not frozen.**
Authority: this document is normative for purpose, scope, and non-goals. It does
not define language behavior; `docs/spec/*` does.

## Language identity

- Official language name: **Arara**.
- Repository: `JoaoGaValentim/arara-lang`.
- Source extension: `.ara`, **PROPOSED** by `D001-06`; it remains subject to
  `OQ-8` and is not ratified.
- Primary target: JVM class files, classfile major version 65 (JDK 21).
- Design inspiration: Kotlin, for surface syntax only. Arara is not a Kotlin
  fork, does not claim Kotlin compatibility, and does not inherit Kotlin
  semantics.
- Implementation and semantics: independently specified in this repository. No
  Kotlin compiler, Kotlin runtime, or Kotlin standard library is part of the
  language implementation.
- Diagnostic namespace: `ARA-<STAGE>-<NNNN>`. `ARA` is the stable abbreviation of
  Arara; `D001-06` does not rename existing codes.

## 1. Purpose

Arara is a small, statically typed programming language that compiles to JVM
bytecode. Its purpose is to become a production-quality JVM language through
independently verified milestones, without repeating the failure modes of
previous language projects: premature implementation, uncontrolled scope,
inconsistent grammar, underspecified semantics, and lost execution context.

The project optimizes for **provability of each step**, not for feature count.

## 2. Design principles

1. **Contract before code.** A construct exists only after its syntax, typing,
   evaluation, diagnostics, JVM mapping, and tests are specified.
2. **Smallest coherent language.** Every feature must justify its presence by
   enabling a class of programs that the current subset cannot express.
3. **Explicit over inherited.** No behavior is "whatever Java does" or "whatever
   Kotlin does". Every rule is written down, including rules that happen to match
   the JVM.
4. **Deterministic diagnostics.** Stable codes, stable spans, deterministic
   ordering, no first-error abort when more errors can be reported soundly.
5. **Verifiable output.** Bytecode is not trusted because it was generated; it is
   trusted after structural analysis and JVM verification.
6. **Reversible process.** Any session can be reconstructed from repository files
   alone.
7. **Honest evidence.** Claims are `VERIFIED`, `UNVERIFIED`, `FAILED`, or
   `BLOCKED`, never assumed.

## 3. Scope of the initial language (proposed)

The initial subset, to be implemented only in authorized later milestones:

- Top-level `fun` declarations with typed parameters and optional return type.
- `val` (single assignment) and `var` (mutable) local bindings.
- Types: `Int`, `Boolean`, `String`, `Unit`.
- Integer arithmetic: `+ - * / %`, unary `-`, with 32-bit two's complement
  semantics.
- Boolean logic: `&& || !`, with short-circuit evaluation.
- Comparison: `< <= > >=` on `Int`; equality `== !=` on `Int`, `Boolean`,
  `String`.
- `if` / `else` statements with block bodies.
- Explicit `return`.
- Block scopes, with shadowing prohibited.
- `println` as the only output facility.
- A single program entry point, `fun main()`, mapped to the JVM entry point.
- Recursion (a consequence of function calls, not a separate feature).

## 4. Non-goals for the initial subset

These are excluded deliberately. They are not missing features and must not be
introduced to simplify an implementation:

- Generics and type parameters.
- Coroutines and async.
- Classes, inheritance, interfaces, objects.
- Extension functions.
- Lambdas and closures; functions are not values.
- Reflection and dynamic dispatch.
- Custom annotations.
- Nullable types and null safety.
- Operator overloading. Built-in operators are fixed by the specification.
- Multiplatform compilation.
- A general standard library.
- Packages and modules.
- Loops (`while`, `for`, `do`) and loop control (`break`, `continue`).
  Iteration is expressed by recursion in the initial subset.
- Pattern matching (`when`).
- Type aliases (`typealias`).
- Type tests and casts (`is`, `as`) and the `in` operator form.
- Enums as a distinct declaration kind; a future class or object feature would
  cover the category.
- String concatenation and string interpolation.
- Floating-point types.
- Exception handling syntax (`try`/`catch`/`throw`).
- Multiple source files with packages; see §6 for the multi-file rule.
- Incremental compilation, REPL, IDE integration, formatter, package manager.

## 5. Platform commitment

- Compilation target: JVM class files, classfile major version 65 (JDK 21).
- Baseline runtime: JDK 21 LTS.
- No other backend is authorized in any milestone through D006. A second backend
  would require a new ADR and an explicit owner decision.
- Java interoperability is a milestone (D005), not an assumption. Until D005 is
  accepted, no compatibility claim is made.

See `docs/adr/0001-jvm-target.md`.

## 6. Program model (proposed)

- A **compilation unit** is one `.ara` source file.
- The file's base name (without `.ara`) is the name of the generated class. It
  must match `[A-Za-z_][A-Za-z0-9_]*`, must not be any of the 51 reserved or 17
  contextual keywords of JLS 21 §3.9, must not be `true`, `false`, or `null`
  (`ARA-SRC-0004`), and must not shadow `java` or a `java.lang` type in the
  emitted subset (`ARA-SRC-0005`). The 71-name exclusion is broader than `javac`
  requires: 11 contextual keywords, including `module` and `open`, are legal
  class names, and Arara excludes them by policy. The normative statement and the
  probe evidence are in `semantics.md` §1.2a–§1.5a; `OQ-10` records the
  ratification question.
- There are no package declarations in the initial subset. All classes are in the
  unnamed package.
- Multiple compilation units may be compiled together. Function names share one
  program-wide namespace; duplicate names are an error.
- A **program** is the set of compilation units passed to one compiler
  invocation.
- Exactly one function named `main`, with no parameters and `Unit` result, must
  exist in a program. It is the entry point. The class containing it is the
  **entry class**.
- Execution entry is `public static void main(String[])` on the entry class.

These rules are normative only after D001 acceptance; they are stated here to
make the architecture and specification reviewable.

## 7. What this project will never claim without evidence

- Kotlin compatibility, source compatibility, or binary compatibility.
- Java interoperability before real cross-language compile-and-run tests.
- Completeness of the specification for constructs outside the initial subset.
- Correctness of an implementation because it compiles.
