# Arara

Arara is a small, statically typed programming language that compiles to JVM
bytecode. It is designed to grow into a production-quality JVM language through
small, independently verified milestones.

Arara is **not** Kotlin, and it does not claim Kotlin compatibility. It borrows a
familiar declaration syntax from Kotlin so that Arara is easy to read, but every
lexical, syntactic, semantic, and code-generation rule is specified in this
repository.

## Language identity

| Property | Value |
| --- | --- |
| Official name | Arara |
| Repository | `JoaoGaValentim/arara-lang` |
| Source extension | `.ara` — **PROPOSED**, not ratified (`OQ-8`) |
| Primary target | JVM class files, classfile major version 65 (JDK 21) |
| Design inspiration | Kotlin, surface syntax only |
| Implementation and semantics | Independently specified; no Kotlin compiler or runtime |
| Diagnostic codes | `ARA-<STAGE>-<NNNN>` |

## Status

**D001 — Foundation. Documentation and configuration only. No compiler exists.**

There is no lexer, parser, type checker, or backend in this repository yet. Any
snippet on this page is a specification example, not a working program.

- Active milestone: see [`docs/progress/STATE.md`](docs/progress/STATE.md)
- Language contract (draft): [`docs/charter.md`](docs/charter.md) and [`docs/spec/`](docs/spec/)
- Architecture: [`docs/architecture.md`](docs/architecture.md)
- Roadmap and gates: [`docs/roadmap.md`](docs/roadmap.md), [`docs/progress/GATES.md`](docs/progress/GATES.md)
- Engineering authority: [`AGENTS.md`](AGENTS.md)

## Target

| Property | Value |
| --- | --- |
| Compilation target | JVM class files |
| Classfile major version | 65 (JDK 21) |
| Baseline runtime | JDK 21 LTS |
| Other backends | None |

## Example of the intended syntax

```arara
fun main() {
    val answer: Int = 40 + 2
    println(answer)
}
```

Assuming D001 is accepted and D002–D004 are authorized and completed, this
program would compile to a class containing a `public static void main(String[])`
that prints `42`. Nothing in this repository implements or verifies that yet.

## Working in this repository

Read [`AGENTS.md`](AGENTS.md) before changing anything. Then follow
[`.kilo/commands/resume.md`](.kilo/commands/resume.md) to reconstruct state, and
[`.kilo/commands/checkpoint.md`](.kilo/commands/checkpoint.md) before persisting
work.

The short version:

1. Only one milestone is active.
2. No implementation without a verified, owner-accepted contract.
3. Every claim of verification names the command, the environment, and the
   commit.
4. Repository records outrank conversation history.

## Repository layout

```text
AGENTS.md                  primary engineering authority
README.md                  this file
.kilo/                     Kilo configuration, rules, commands, auditor agent
docs/charter.md            purpose, scope, non-goals, deferred features
docs/architecture.md       compiler pipeline and stage contracts
docs/roadmap.md            milestone boundaries and entry/exit criteria
docs/adr/                  architecture decision records (proposed until accepted)
docs/spec/                 lexical, grammar, semantic, and conformance contracts
docs/progress/             durable state, decisions, continuation, gates
```
