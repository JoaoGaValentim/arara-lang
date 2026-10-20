# ADR 0002 — Compiler host language and build toolchain

Status: **PROPOSED** (D001). Becomes accepted only when the owner accepts D001 or
records a separate decision.
Date: 2026-10-20
Supersedes: none

## Context

The compiler needs a host language and build system that:
- runs on the target JDK without an extra toolchain,
- gives precise control over bytecode generation,
- supports fast, dependable test iteration,
- has a stable, verifiable dependency set.

Candidates: Java 21, Kotlin, Rust, and a generator-based parser stack.

## Decision

- Host language: **Java 21** (`javac 21.0.12.1`, Corretto).
- Build: **Gradle** (verified: 9.8.1), with a Gradle wrapper pinned when D002
  creates the build.
- Bytecode: **ASM** (`org.ow2.asm:asm`, plus `asm-util`, `asm-analysis` when
  used). Current published version verified 9.11.1; the version is not frozen by
  this ADR and must be pinned in D002 after a compatibility check.
- Tests: **JUnit Jupiter**. Current published version verified 6.1.3; same
  pinning rule applies.
- No dependency is added until the milestone that needs it is authorized.

## Verified environment (2026-10-20, macOS arm64)

| Component | Version | Evidence |
| --- | --- | --- |
| JDK | Corretto 21.0.12.1, build 21.0.12.1+12-LTS | `java -version`, `java -XshowSettings:properties -version` |
| javac | 21.0.12.1 | `javac -version` |
| Gradle | 9.8.1, launcher JVM 21.0.12.1 | `gradle --version` |
| ASM | 9.11.1 published | Maven Central `maven-metadata.xml` |
| JUnit Jupiter | 6.1.3 published | Maven Central `maven-metadata.xml` |
| OS | macOS 27.0.1, Darwin 27.0.0, aarch64 | `uname -a`, `sw_vers`, `java -XshowSettings:properties` |
| Python (ad-hoc audit only) | 3.14.8 | `python3 --version` |

### Recorded toolchain probe

An external, disposable Gradle project was created outside this repository at
`$TMPDIR/kilo/toolchain` (not committed) with:

- `settings.gradle.kts`: `rootProject.name = "arara-toolchain-check"`
- `build.gradle.kts`: `plugins { java }`, `mavenCentral()`, Java toolchain 21,
  `org.ow2.asm:asm:9.11.1`, `org.ow2.asm:asm-util:9.11.1`,
  `org.ow2.asm:asm-analysis:9.11.1`, `org.junit.jupiter:junit-jupiter:6.1.3`,
  `junit-platform-launcher`, `useJUnitPlatform()`
- two JUnit tests: `SmokeTest` (generate a class with `ClassWriter`, assert
  classfile major version 65 by reading bytes 6–7, write it, load it with a
  `URLClassLoader`, invoke a static method returning 42) and `VerifyTest`
  (generate a class and pass `CheckClassAdapter.verify` with data-flow
  analysis, asserting the analysis writer is empty).

Command and observed result:

```text
$ gradle --no-daemon test --console=plain
> Task :compileTestJava
> Task :test
BUILD SUCCESSFUL in 6s
2 actionable tasks: 2 executed
```

This establishes that the chosen host language, build tool, bytecode library,
analyzer, and test framework are mutually compatible on this machine. It does
**not** establish anything about the Arara compiler, which does not exist.

## Rationale

1. Java 21 keeps the host on the same JVM family as the target, exposes exact JVM
   type behavior, and has no impedance mismatch for bytecode work. The JDK 21
   class-file API (`java.lang.classfile`) is **not** available: a probe on
   Corretto 21.0.12.1 returned `ClassNotFoundException` for
   `java.lang.classfile.ClassFile`, so ASM remains the bytecode API.
2. A Java 21 host keeps the compiler usable with the same JDK that target
   programs require, simplifying the user story.
3. ASM is mature, widely used, and provides both generation and analysis
   (`CheckClassAdapter`), which is required for the verification gate.
4. JUnit Jupiter is the standard and requires no bespoke harness.
5. Gradle supports toolchains, so the build can pin Java 21 even on a machine
   with several JDKs.

## Rejected alternatives

| Alternative | Reason |
| --- | --- |
| Kotlin host | Slower build feedback, an extra language to reason about, and no capability the backend needs. |
| Rust host | Would require a separate JVM interop layer for testing generated classes; heavier iteration. |
| C/C++ host | Toolchain burden and memory-safety risk for no benefit. |
| Maven | Viable, but weaker toolchain pinning and slower multi-module iteration for this project; not a technical blocker. |

## Consequences

- The compiler will be a JVM application and needs the JDK 21 runtime.
- Dependency versions must be re-verified at the start of D002 and pinned in a
  build file plus a lock/record in `STATE.md`.
- ASM's `CheckClassAdapter` is a *supplementary* verification tool. The JVM
  verifier under `-Xverify:all` remains the authoritative check.

## Non-decisions

- This ADR does not authorize creating `build.gradle(.kts)`, wrapper files, or a
  source tree. Those belong to D002.
- The compiler artifact's packaging and distribution model is undecided.
