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

---

## Open decisions requested from the owner

These are recorded here so they are not lost; they are **not** decisions yet.

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
