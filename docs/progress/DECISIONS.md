# Decisions

Durable record of owner decisions. A decision here is the only evidence of
authorization. Never rewrite a historical decision to make an implementation
appear compliant; supersede it with a new entry instead.

Format: `Dxxx-yy` where `xxx` is the directive number and `yy` is a sequence.

---

## D001-01 — Authorize Directive 001: bootstrap and design only

- **Date:** 2026-10-20
- **Decider:** Repository owner (`JoaoGaValentim`)
- **Status:** ACTIVE
- **Scope:**
  - Milestone **D001 — Foundation** only.
  - Repository governance (`AGENTS.md`, `.kilo/rules/*`), Kilo configuration,
    recovery mechanism, architecture, roadmap, ADRs, and the initial language
    specification (lexical, grammar, semantics, conformance plan).
  - Local Git commits for the bootstrap are permitted.
  - Creating the GitHub repository `JoaoGaValentim/arara-lang` is permitted.
- **Explicitly not authorized:**
  - Lexer, parser, type checker, IR, backend, or any compiler source code.
  - `build.gradle`, `pom.xml`, Gradle wrapper, or a compiler source tree.
  - Tests, CI configuration, or generated artifacts.
  - Starting D002 or any later milestone.
  - Pushing to `main`, merging PRs, publishing packages, creating releases or
    tags, or changing repository protections.
  - Weakening any governance rule.
- **Rationale:** Establish a reliable engineering process and a coherent language
  contract before any implementation, to avoid the failure modes named in the
  directive.
- **Evidence of authority:** Owner directive "DIRECTIVE 001 — JVM LANGUAGE
  FOUNDATION & CONTEXT-SAFE ENGINEERING", 2026-10-20, received in the project
  session, with the explicit instruction to perform the authorized bootstrap and
  to create the repository with `gh`.

## D001-02 — Repository visibility

- **Date:** 2026-10-20
- **Decider:** Repository owner (`JoaoGaValentim`)
- **Status:** ACTIVE
- **Scope:** `JoaoGaValentim/arara-lang` is created **private**. Visibility
  changes require a separate decision.
- **Rationale:** The directive authorized repository creation but did not state
  visibility. Private is the conservative default for a repository with no
  accepted release.
- **Note:** This entry records the choice made during execution. If the owner
  intended a different visibility, record a superseding decision; do not
  reinterpret this one.

## D001-03 — Development branch policy for the bootstrap

- **Date:** 2026-10-20
- **Decider:** Repository owner, via directive §12
- **Status:** ACTIVE
- **Scope:** `AGENTS.md` forbids pushing directly to `main`. The repository's
  initial branch is `main` because GitHub creates it. Bootstrap commits are made
  locally on `main` **and are not pushed** during D001. D002 work must occur on
  `develop` or a short-lived branch, created before any implementation commit.
- **Rationale:** `AGENTS.md` §4 and directive §12 both prohibit direct
  development on `main`. Creating an empty remote and an un-pushed local history
  avoids publishing an unauthorized branch layout while keeping the bootstrap
  auditable.
- **Limitation:** This means the D001 commit exists only locally after execution.
  The owner must authorize any push.

## D001-04 — Model reasoning effort and limit metadata

- **Date:** 2026-10-20
- **Decider:** Repository owner, via directive §6
- **Status:** ACTIVE
- **Scope:** `.kilo/kilo.jsonc` selects variant `max` for the `code` agent and
  records measured model limits (`context` and `output` = 1048576) for
  `deepseek-ai/deepseek-v4.1-flash-1017` under both `robin` and `hive`.
- **Rationale:** Directive §6 requires the highest genuinely supported reasoning
  effort and accurate model-limit metadata; both are constrained by what the
  installed Kilo client and provider actually expose. See
  `docs/adr/0003-model-limits.md` for the measurement.
- **Limitation:** Provider limits are measured behavior, not a published
  contract. See ADR 0003 §Risks.

## D001-05 — Specification status

- **Date:** 2026-10-20
- **Decider:** Repository owner, via directive §9 and §15
- **Status:** ACTIVE
- **Scope:** `docs/spec/*`, `docs/charter.md`, `docs/architecture.md`,
  `docs/roadmap.md`, and ADRs 0001–0003 are **DRAFT / PROPOSED** until the owner
  accepts D001. None of them is frozen, and none authorizes implementation.
- **Rationale:** The directive requires a design contract for review, not a
  frozen language.

## D001-06 — Language identity: Arara (Directive 001-R1)

- **Date:** 2026-10-20
- **Decider:** Repository owner (`JoaoGaValentim`)
- **Status:** ACTIVE
- **Scope:**
  - Official language name: **Arara**.
  - Repository identity: `JoaoGaValentim/arara-lang`.
  - Proposed source extension: `.ara`. It remains **PROPOSED**; this entry does
    not ratify it, and `OQ-8` remains open.
  - Primary target: JVM. Classfile major version 65 (JDK 21) and the
    single-backend rule in `docs/adr/0001-jvm-target.md` are unchanged.
  - Design inspiration: Kotlin, for surface syntax only. Arara is not a Kotlin
    fork, does not claim Kotlin compatibility, and does not inherit Kotlin
    semantics.
  - Language implementation and semantics: independently specified in
    `docs/spec/*`; every rule is normative in this repository.
  - Diagnostic namespace: the existing `ARA-<STAGE>-<NNNN>` codes are retained
    unchanged, with `ARA` as the abbreviation of the approved name. Renaming them
    would invalidate every diagnostic reference and is not authorized.
- **Explicitly not authorized:**
  - Acceptance of D001. D001 remains submitted for owner review.
  - Starting D002 or any later milestone.
  - Implementing a lexer, parser, type checker, IR, backend, or compiler source.
  - Changing frozen semantics, adding dependencies, or adding a backend target.
  - Pushing, merging, releasing, tagging, or publishing.
- **Rationale:** The documents already used "Arara" consistently, but the name
  had no explicit decision entry. This entry makes the identity durable and
  authoritative before D001 acceptance, while leaving the extension and the
  milestone gates undecided.
- **Evidence of authority:** Owner directive "DIRECTIVE 001-R1 — ARARA LANGUAGE
  IDENTITY", 2026-10-20, with the explicit statement "Owner decision: Language
  name ARARA — APPROVED. D001 acceptance and D002 implementation remain
  pending."

---

## D001-07 — Communication language and status vocabularies

- **Date:** 2026-10-20
- **Decider:** Repository owner (`JoaoGaValentim`)
- **Status:** ACTIVE
- **Scope:**
  - All agent-authored communication is English only: reasoning and analysis
    traces, planning and execution messages, progress and checkpoint summaries,
    reports, audits, diagnostics, source comments and documentation, commit
    messages and pull request descriptions, and error and recovery instructions.
  - Chinese (Mandarin), Chinese characters, and automatic language switching are
    prohibited in agent-authored communication. An unexpected Chinese output is
    corrected by returning to English and continuing the unchanged technical task.
  - Exceptions are limited to exact quotations of external material, existing
    source data, or an explicit owner request. External evidence is never
    translated or modified to satisfy the rule.
  - The policy survives context compaction, agent handoffs, and session recovery,
    and applies to every subsequent directive until the owner rescinds it.
  - Gate statuses are restricted to the closed set declared in
    `docs/progress/GATES.md`: `NOT_STARTED`, `IN_PROGRESS`, `PASS`, `FAIL`,
    `BLOCKED`. Undocumented status identifiers may not be introduced.
  - Milestone verdicts are a separate vocabulary — `PASS`, `CONDITIONAL`,
    `BLOCKED` — and may not be written into a gate status column.
- **Explicitly not authorized:** adding a gate status value without an amending
  decision; translating repository evidence; switching language on the basis of
  an input's language.
- **Rationale:** The repository is reviewed by more than one agent and resumed
  across sessions. Language drift and invented status identifiers both degrade
  auditability, and neither is detectable from a single transcript.
- **Evidence of authority:** Owner directive "MANDATORY LANGUAGE POLICY — ENGLISH
  ONLY", 2026-10-20, with the explicit statement "This policy is mandatory for
  every subsequent directive." Recorded in `AGENTS.md` §11 and §12 and in
  `.kilo/rules/00-governance.md`.

---

## Open decisions requested from the owner

These are recorded here so they are not lost; they are **not** decisions yet.
`D001-06` resolves the language name only; it does not accept D001 or authorize
D002.

1. **OQ-1 — Repository visibility.** Confirm `private` or request `public`.
2. **OQ-2 — Remote publication.** Authorize pushing the bootstrap commit and
   creating `develop`, or keep the bootstrap local.
3. **OQ-3 — Specification acceptance.** Accept, reject, or request changes to
   `docs/spec/*` as the D001 contract.
4. **OQ-4 — `Int` division by zero.** The draft specifies a runtime
   `ArithmeticException`. Confirm or request a different behavior.
5. **OQ-5 — Shadowing prohibition.** The draft forbids all shadowing, which is
   stricter than Kotlin. Confirm or request Kotlin-like nested shadowing.
6. **OQ-6 — Multi-unit programs.** The draft allows multiple `.ara` units per
   program with one program-wide function namespace. Confirm or restrict to a
   single unit for the initial subset.
7. **OQ-7 — Reserved word list.** Confirm the deferred-feature reserved words in
   `lexical.md` §6.
8. **OQ-8 — File extension.** Confirm `.ara` as the source extension.
9. **OQ-9 — License.** No license file is included; the repository is private.
   Choose a license before any publication.
10. **OQ-10 — Restricted unit names.** RESOLVED IN THE SPECIFICATION, PENDING
    RATIFICATION. `semantics.md` §1.2b states a three-part unit-name rule:
    pattern (rule 1), exclusion of all 51 reserved and 17 contextual keywords of
    JLS 21 §3.9 plus `true`/`false`/`null` (`ARA-SRC-0004`, rule 2), and
    exclusion of shadowing names (`ARA-SRC-0005`, rule 3, see `OQ-11`). The owner
    ratifies or reverses; the previous text proposed excluding keywords only.
    *Evidence, Directive 001-R2, corrected by 001-R3:* JLS 21 §3.9 declares
    **51** reserved and **17** contextual keywords. The R2 figure of 52 was
    wrong; the extra item came from a regex capturing the gloss word
    `underscore` in `_ (underscore)`. JVMS 21 §4.2.1 forbids only `.` `;` `[`
    `/` in a class name, so the JVM constraint and the Java-source constraint
    are different questions, not approximations of each other. Measured on
    Corretto 21.0.12.1 under `-Xverify:all`: the JVM accepts **all 68** names as
    class names; `javac` rejects **all 68** as class names, plus `true`, `false`,
    and `null` (71 total). The earlier claim that `javac` rejects only five was
    wrong.
11. **OQ-11 — Units that shadow `java.lang`.** RESOLVED IN THE SPECIFICATION,
    PENDING RATIFICATION. `semantics.md` §1.2b rule 3 forbids `java`, `String`,
    `System`, `Object`, and `Math` as unit names, reported as `ARA-SRC-0005`.
    §1.5a records the measured behaviour in a per-name table, each with a control
    that compiles. The previous text said the rule permits them and reports
    nothing.
    *Correction, Directive 001-R3:* the R2 statement that a unit named `java`
    breaks `java.lang.String` holds only when the Java source writes a **fully
    qualified** `java.` name; a file using only simple names compiles unaffected.
    A unit named `String` or `System` breaks the unqualified form directly. Both
    behaviours are now recorded separately in §1.5a instead of being collapsed
    into one universal claim.
12. **OQ-12 — Diagnostic shorthand in the conformance plan.** RESOLVED, PENDING
    RATIFICATION OF THE REWRITE. Every normative obligation in
    `conformance-plan.md`, `lexical.md`, and `semantics.md` now uses a full
    `ARA-NS-NNNN` code; the ranges in `semantics.md` §12.2 and §12.3 were
    rewritten as explicit lists.
    *Correction, Directive 001-R3:* the R2 count was low. A strict scan found
    **83** shorthand occurrences over **35** distinct numbers, not 54, and 20
    numbers spanned more than one namespace. Two codes were not merely unnamed
    but uncovered: `ARA-SRC-0002` and `ARA-PARSE-0199`. Both now carry explicit
    obligations in `conformance-plan.md` §6a, and all **68** defined codes have
    at least one obligation.
13. **OQ-13 — Vocabulary separation.** Whether to retain the split between the
    closed gate status set (`NOT_STARTED`, `IN_PROGRESS`, `PASS`, `FAIL`,
    `BLOCKED`) and the milestone verdict set (`PASS`, `CONDITIONAL`, `BLOCKED`)
    as the permanent convention. Recorded in `AGENTS.md` §12 and `D001-07`. The
    alternative is one vocabulary, which would force a verdict value into a gate
    column — the defect Directive 001-R3 corrected.

