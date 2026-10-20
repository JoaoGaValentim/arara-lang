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
    not ratify it, and `OQ-8` remains open. *(Historical: `D001-18` later ratified
    `.ara`. This entry is not edited, per the no-rewrite rule.)*
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

## D001-08 — Directive 001-R4: public repository, Apache-2.0, Copilot migration

- **Date:** 2026-10-20
- **Decider:** Repository owner (`JoaoGaValentim`)
- **Status:** ACTIVE
- **Supersedes:** `D001-02` (repository visibility) and resolves `OQ-9` (license).
  `D001-02` is retained unmodified as history; this entry is the operative
  visibility decision.
- **Scope:**
  - **`OQ-1` — repository visibility: PUBLIC.** Authorized. `D001-02` recorded
    `private` as the conservative bootstrap default; this entry changes it.
  - **`OQ-9` — license: Apache License, Version 2.0.** SPDX `Apache-2.0`. A root
    `LICENSE` file carries the unmodified ASF text. No `NOTICE` file is created,
    because the repository contains no third-party material requiring one; see
    `docs/progress/audit/D001-R4.md` §3.
  - **Agent environment: GitHub Copilot replaces Kilo as the supported primary
    environment.** `AGENTS.md` remains the governing document. Kilo
    configuration is retained until the replacement is demonstrated working.
  - The DeepSeek model continues to be used, through the VS Code Custom Endpoint
    provider with vendor `customendpoint`.
- **Explicitly not authorized:**
  - Starting D002 or any later milestone.
  - Writing lexer, parser, type-checker, IR, or backend code.
  - Merging a pull request, creating a release, tag, or published artifact.
  - Pushing the D001-R4 branch, or any branch, without separate authorization.
  - Reinterpreting `OQ-3`, `OQ-10`, `OQ-11`, `G9-A`, or any other pending item as
    approved.
- **Rationale:** The owner must be able to read and audit the repository, and an
  unlicensed public repository is not legally reusable. Copilot migration
  consolidates the agent environment while keeping the governance record in the
  repository rather than in a tool-specific configuration.
- **Evidence of authority:** Owner directive "DIRECTIVE 001-R4 — Public
  Repository, Apache-2.0 Adoption & GitHub Copilot Migration", 2026-10-20, §1:
  "`OQ-1` — Repository visibility: APPROVED: PUBLIC", "`OQ-9` — Licensing:
  APPROVED: Apache License, Version 2.0", and "Agent infrastructure: APPROVED:
  Replace Kilo as the active project agent environment with GitHub Copilot".

---

## D001-09 — D001-G9 is `BLOCKED`, not `PASS` (Directive 001-R4 F-R4-03)

- **Date:** 2026-10-20
- **Decider:** Repository owner, via directive §3
- **Status:** ACTIVE
- **Scope:** The authorized criterion for `D001-G9` is the original text:
  "`STATE.md` matches actual branch, HEAD, and working tree at report time".
  Amendment `G9-A` was only ever `PROPOSED`. A gate cannot be marked `PASS`
  against an unapproved criterion, so `D001-G9` is reclassified from `PASS` to
  `BLOCKED`. `G9-A` remains `PROPOSED` and is still awaiting ratification.
- **Not authorized:** treating `G9-A` as operative, deleting the proposed
  amendment, or rewriting the original criterion text.
- **Rationale:** Directive 001-R4 §3 states that D001-G9 was declared `PASS`
  against an amendment that remains `PROPOSED`, and that an unapproved criterion
  amendment is not operative. Because the original criterion cannot be satisfied
  by any ledger written before its own commit, the gate cannot be settled by
  evidence — which is the declared meaning of `BLOCKED`.
- **Consequence:** The D001 milestone verdict can no longer be `CONDITIONAL` on
  the previous reasoning, because one gate is `BLOCKED`. It is recorded as
  `BLOCKED` pending the owner's decision on `G9-A`.
- **Evidence of authority:** Directive 001-R4 §3, F-R4-03.

---

## D001-10 — Directive 001-R5: foundation verification and publication

- **Date:** 2026-10-20
- **Decider:** Repository owner (`JoaoGaValentim`)
- **Status:** ACTIVE
- **Scope:** Authorizes the operational actions in Directive 001-R5:
  - Correcting defects found in D001-R4 review, including the numerical defect in
    the `javac` experiment report (59 → 60 rejected).
  - Validating and correcting the user-level VS Code DeepSeek model
    configuration, with a local backup created before modification. The
    configuration is user-level and outside version control; no credential or
    endpoint address is recorded in this repository.
  - Testing GitHub Copilot custom agents, their tool allowlists, and
    fresh-session recovery under the Copilot harness.
  - Publishing the reviewed foundation: initializing remote `main` from the
    existing baseline, pushing `infra/d001-r4-copilot-migration` separately,
    and opening a Draft PR into `main`.
  - Configuring default-branch protections appropriate to a single maintainer.
  - Recording administrative decisions and verification evidence, and making
    documentation-only corrections required by these tasks.
- **Ratifies `G9-A` within its stated scope.** The replacement criterion text
  already in `docs/progress/GATES.md` ("`STATE.md` records the branch, the commit
  preceding this ledger, the audit commit, and the working-tree state, and each is
  verifiable by a named command") is ratified as written, but only because it
  satisfies the directive's condition: it drops the impossible requirement that a
  commit contain its own SHA and replaces it with independently observable Git
  state. The original criterion remains quoted verbatim and unedited as history.
  No other gate is reworded or re-evaluated by this entry.
- **Explicitly not authorized:**
  - Starting D002 or any later milestone; writing lexer, parser, type checker, IR,
    or backend code.
  - Bypassing a failed acceptance criterion, or inventing unreviewed semantic
    decisions.
  - Force-pushing, rewriting published history, merging the Draft PR, creating a
    release or tag, or publishing an artifact.
  - Marking `OQ-3`, `OQ-10`, `OQ-11`, or any other open item accepted merely
    because this directive authorized operational corrections.
- **Rationale:** The foundation was complete on one machine but unverifiable to
  anyone else, and an unverified agent migration cannot be trusted as the primary
  environment. Publication and independent verification are prerequisites for a
  reviewable D001 acceptance decision.
- **Evidence of authority:** Owner directive "DIRECTIVE 001-R5 — Copilot
  Activation, Repository Publication & Final Foundation Verification",
  2026-10-20, §1 "Owner authorization: GRANTED FOR THE OPERATIONS SPECIFIED
  BELOW", §7 `G9-A` conditional ratification, and §8 publication authorization.
  The directive states "No merge is authorized under this directive" and "No D002
  implementation is authorized."

---

## D001-11 — `G9-A` ratified; `D001-G9` reclassified

- **Date:** 2026-10-20
- **Decider:** Repository owner (`JoaoGaValentim`), via Directive 001-R5 §7
- **Status:** ACTIVE
- **Supersedes:** The `PROPOSED` status of `G9-A` only. `D001-09` (which recorded
  `D001-G9` as `BLOCKED` against the original criterion) is retained unmodified
  as history.
- **Scope:** `G9-A` is **RATIFIED** and operative. `D001-G9` is evaluated against
  the replaced criterion and is reclassified from `BLOCKED` to `PASS` on
  verification, not on assertion: `STATE.md` records the branch, the commit
  preceding the ledger, the audit commit, and the working-tree state, and each is
  checkable with a named command. The original criterion text is preserved in
  `GATES.md` and is not deleted.
- **Condition satisfied:** Directive 001-R5 §7 permits ratification only if the
  replacement "verify real ledger consistency using independently observable Git
  state, rather than requiring a commit to contain its own hash before that
  commit exists", and only if it "does not weaken any other gate." The ratified
  text names branch, preceding commit, audit commit, and working-tree state, and
  requires a named command for each. It does not relax `D001-G14`, `D001-G15`, or
  any other gate.
- **Explicitly not authorized:** deleting the original criterion; treating this
  ratification as D001 acceptance; using it to authorize D002.
- **Rationale:** The original criterion was unsatisfiable by construction for any
  accurate ledger, so the gate could never be settled by evidence. The
  replacement measures the same property — ledger consistency — using state an
  observer can recompute.
- **Consequence:** With `D001-G9` at `PASS`, no D001 gate is `BLOCKED`. The D001
  verdict still cannot be `PASS` while `OQ-3`, `OQ-10`, `OQ-11`, and the other
  open items are unresolved; see the D001-R5 report for the current verdict.

---

---

## D001-12 — Directive 001-R6 authorization and owner delegation

- **Date:** 2026-10-20
- **Decider:** Repository owner (`JoaoGaValentim`)
- **Status:** ACTIVE
- **Scope:** Authorizes the D001-R6 workflow: recording the owner's ratification
  of `OQ-3`–`OQ-8` and `OQ-10`–`OQ-14`, freezing the D001 contract at an exact
  revision, correcting the PR #1 description, pre-merge verification, marking
  PR #1 ready and merging it into `main` with a **merge commit**, post-merge
  verification, and recording D001 acceptance if and only if every condition
  holds.
- **Delegation boundary:** The directive delegates ratification of the eleven named
  options. It does **not** delegate language design. No ratification recorded
  under this directive may introduce a rule the specification does not already
  contain, and none may alter a semantic in the course of acceptance.
- **Explicitly not authorized:** starting D002; implementing a lexer, parser,
  resolver, type checker, IR, or backend; adding a language feature; squashing or
  rebasing the reviewed commits; force-pushing; bypassing branch protection;
  deleting `.kilo/`; publishing a release; introducing an unapproved dependency.
- **Rationale:** The D001 contract has passed independent verification and its
  evidence is published. What remained was the owner's authority over the
  contract, not further engineering. Recording the decisions and integrating the
  reviewed revision converts a local foundation into an accepted baseline that
  D002 can safely build on.
- **Evidence of authority:** Owner directive "ARARA — DIRECTIVE 001-R6 — Owner
  Contract Ratification, PR #1 Integration and Foundation Closure", 2026-10-20,
  §2 (ratifications), §6 (merge authorization), and §8 (acceptance conditions).

---

## D001-13 — `OQ-3` ratified: initial language contract

- **Date:** 2026-10-20
- **Decider:** Repository owner (`JoaoGaValentim`), via Directive 001-R6 §2
- **Status:** ACTIVE — **RATIFIED**
- **Scope:** The current D001 specification —
  `docs/spec/lexical.md`, `docs/spec/syntax.ebnf`, `docs/spec/semantics.md`, and
  `docs/spec/conformance-plan.md` — is approved as the baseline for the initial
  Arara implementation, starting at D002.
- **Coverage limit:** This approval covers **only the currently documented
  subset**. It is not a general license to add features, and it does not approve
  anything in `docs/charter.md` §4, all of which remain deferred.
- **Superseding rule:** Any future semantic change requires an explicit new
  decision that names the rule it supersedes. Silence, implementation convenience,
  or a passing build never amends the contract.
- **Not authorized by this entry:** starting D002, writing compiler code,
  accepting a later gate, or treating this ratification as a release.
- **Verification performed before ratification:** 68 diagnostic codes defined
  once and none undefined; 68 of 68 carrying a conformance obligation; EBNF
  lowering reproduced at 27 declared productions, 53 nonterminals, 97 BNF
  productions, 38 terminals; FIRST/FOLLOW analysis reporting 0 unreachable,
  0 undefined, and **0 LL(1) conflicts**; no compiler implementation present.
  Commands and output are recorded in `docs/progress/audit/D001-R6.md` §B.
- **Rationale:** A contract that is never accepted cannot gate an implementation.
  The verification above is what makes acceptance a decision about the language
  rather than an assertion of hope.
- **Evidence of authority:** Directive 001-R6 §2, `OQ-3`.

---

## D001-14 — `OQ-4` ratified: integer division by zero

- **Date:** 2026-10-20
- **Decider:** Repository owner, via Directive 001-R6 §2
- **Status:** ACTIVE — **RATIFIED**
- **Scope:** Integer division by zero raises a Java-compatible runtime
  `ArithmeticException` at the point of division. It is specified behaviour, not
  undefined behaviour, and not a compile-time error.
- **Constraint:** "Java-compatible" means the observable JVM exception type and
  the specified evaluation order. It does not import any other Java arithmetic
  rule not already written in `semantics.md`.
- **Rationale:** A defined failure mode is testable and portable; an undefined one
  makes the conformance plan unwritable.
- **Evidence of authority:** Directive 001-R6 §2, `OQ-4`.

---

## D001-15 — `OQ-5` ratified: no lexical shadowing in the initial subset

- **Date:** 2026-10-20
- **Decider:** Repository owner, via Directive 001-R6 §2
- **Status:** ACTIVE — **RATIFIED**
- **Scope:** The initial subset prohibits lexical shadowing. A binding whose name
  is already visible is an error, not a new nested binding.
- **Reconsideration:** This restriction **may be revisited in a later language
  milestone**. It is a scoping choice for the initial subset, not a permanent
  claim that shadowing is harmful.
- **Rationale:** Rejecting shadowing removes a whole class of resolution
  ambiguity from the first implementation, at the cost of a restriction that is
  easy to relax later and hard to remove after programs depend on it.
- **Evidence of authority:** Directive 001-R6 §2, `OQ-5`.

---

## D001-16 — `OQ-6` ratified: multiple units, one function namespace

- **Date:** 2026-10-20
- **Decider:** Repository owner, via Directive 001-R6 §2
- **Status:** ACTIVE — **RATIFIED**
- **Scope:** A program may consist of multiple `.ara` compilation units, with a
  single program-wide function namespace. Name collisions across units are an
  error, not a shadowing opportunity.
- **Not implied:** packages, imports, modules, or per-unit namespaces. `§1.4`
  states there are none, and this entry does not add them.
- **Rationale:** A program-wide namespace matches the flat, package-free model the
  rest of the contract already assumes, and it keeps resolution a single global
  table for D003.
- **Evidence of authority:** Directive 001-R6 §2, `OQ-6`.

---

## D001-17 — `OQ-7` ratified: the documented reserved-word list

- **Date:** 2026-10-20
- **Decider:** Repository owner, via Directive 001-R6 §2
- **Status:** ACTIVE — **RATIFIED**
- **Scope:** The explicitly documented reserved-word list in `lexical.md` §6 is
  approved as-is. Reserved words map to a `docs/charter.md` §4 category, as
  `lexical.md` §31–32 require.
- **Expansion rule:** The list MUST NOT be expanded without a new decision. In
  particular, a future JLS release adding a keyword does not silently widen the
  Arara list.
- **Rationale:** An enumeration is auditable and stable; a rule like "all future
  Java keywords" is neither, and it would let an external release change this
  language's contract.
- **Evidence of authority:** Directive 001-R6 §2, `OQ-7`.

---

## D001-18 — `OQ-8` ratified: `.ara` source extension

- **Date:** 2026-10-20
- **Decider:** Repository owner, via Directive 001-R6 §2
- **Status:** ACTIVE — **RATIFIED**
- **Supersedes:** The `PROPOSED` status of `.ara` recorded by `D001-06`. The
  identity decision itself is unchanged; only the extension's status moves from
  proposed to ratified.
- **Scope:** `.ara` is the official Arara source-file extension. The conformance
  plan's fixture suffixes and the driver's file discovery use it.
- **Effect of change:** A future change of extension would affect file discovery
  and the unit-name derivation in `semantics.md` §1.1 only, not language
  semantics. Nothing else is renegotiated by this entry.
- **Rationale:** An extension left permanently `PROPOSED` propagates "not
  ratified" caveats into the contract, the conformance plan, and the README, which
  makes the frozen contract read as tentative in three places at once.
- **Evidence of authority:** Directive 001-R6 §2, `OQ-8`.

---

## D001-19 — `OQ-10` ratified: the 71-name unit-name exclusion

- **Date:** 2026-10-20
- **Decider:** Repository owner, via Directive 001-R6 §2
- **Status:** ACTIVE — **RATIFIED**
- **Scope:** The conservative 71-name exclusion in `semantics.md` §1.2b rule 2 is
  approved as an initial Arara policy.
- **Preserved evidence, kept explicitly:** on the tested candidates, `javac`
  21.0.12.1 rejects **60** names — the 51 reserved keywords, `true`/`false`/`null`,
  `var`/`yield`/`record`/`sealed`/`permits`, and `non-sealed` — and **accepts 11**
  contextual keywords: `exports`, `opens`, `requires`, `to`, `uses`, `provides`,
  `with`, `transitive`, `open`, `module`, `when`. The experiment was rerun with
  five compiling controls and its arithmetic asserted programmatically
  (`accepted + rejected = 71`).
- **Interpretation rule:** the 11 additional exclusions are an **Arara
  compatibility choice, not a Java requirement**. No document may justify the
  71-name list by claiming `javac` rejects all 71; it rejects 60.
- **Rationale:** A flat enumeration keeps unit naming stable if a future JLS
  release promotes a contextual keyword, and it costs nothing today because no
  Arara program needs a unit named `module`.
- **Evidence of authority:** Directive 001-R6 §2, `OQ-10`; measurement in
  `docs/progress/audit/REPRODUCIBILITY.md` §9d.3.

---

## D001-20 — `OQ-11` ratified: measured `java.lang` shadowing restriction

- **Date:** 2026-10-20
- **Decider:** Repository owner, via Directive 001-R6 §2
- **Status:** ACTIVE — **RATIFIED**
- **Scope:** The measured restriction in `semantics.md` §1.2b rule 3 is approved:
  `java`, `String`, `System`, `Object`, and `Math` are forbidden as compilation-unit
  names, reported as `ARA-SRC-0005`.
- **Explicit limit on the claim:** these five names are a **measured subset**, not
  an exhaustive Java-platform collision policy. Untested: names in packages other
  than `java.lang`, collisions that arise only through `import`, and names a future
  JDK may introduce.
- **Interoperability:** full Java interoperability remains a **separate milestone**
  (D005). This entry makes no compatibility guarantee, and no document may infer
  one from the five measured names.
- **Rationale:** The five names break Java source compiled against the same
  classpath, which is a real and testable defect; the rule is narrow because the
  measurement is narrow.
- **Evidence of authority:** Directive 001-R6 §2, `OQ-11`; measurement in
  `docs/progress/audit/REPRODUCIBILITY.md` §9e.

---

## D001-21 — `OQ-12` ratified: canonical diagnostic references

- **Date:** 2026-10-20
- **Decider:** Repository owner, via Directive 001-R6 §2
- **Status:** ACTIVE — **RATIFIED**
- **Scope:** The corrected canonical diagnostic references and their recorded
  conformance obligations are approved. Every normative obligation uses a full
  `ARA-NS-NNNN` code; the range notation in `semantics.md` §12.2–§12.3 was
  rewritten as explicit lists.
- **Verified counts:** 68 codes defined exactly once (LEX 12, PARSE 19, SRC 5,
  NAME 11, TYPE 15, JVM 6), 0 duplicated, 0 referenced-but-undefined, and 68 of 68
  carrying at least one obligation in `conformance-plan.md`.
- **Rationale:** A diagnostic that cannot be traced from rule to obligation to test
  is a diagnostic nobody will implement correctly.
- **Evidence of authority:** Directive 001-R6 §2, `OQ-12`; checks rerun in
  `docs/progress/audit/D001-R6.md` §B.

---

## D001-22 — `OQ-13` ratified: separate status vocabularies

- **Date:** 2026-10-20
- **Decider:** Repository owner, via Directive 001-R6 §2
- **Status:** ACTIVE — **RATIFIED**
- **Scope:** Gate statuses (`NOT_STARTED`, `IN_PROGRESS`, `PASS`, `FAIL`,
  `BLOCKED`) and milestone verdicts (`PASS`, `CONDITIONAL`, `BLOCKED`) are
  **permanently separate** vocabularies. A verdict value may never appear in a gate
  status column, and no undocumented status identifier may be introduced.
- **Rationale:** The overlap between the two sets is what allowed a verdict to be
  written into a gate column, which is the defect D001-R3 corrected. Keeping the
  vocabularies distinct makes the recurrence structural rather than stylistic.
- **Evidence of authority:** Directive 001-R6 §2, `OQ-13`.

---

## D001-23 — `OQ-14` ratified: retain Kilo as fallback

- **Date:** 2026-10-20
- **Decider:** Repository owner, via Directive 001-R6 §2
- **Status:** ACTIVE — **RATIFIED**
- **Scope:** `.kilo/` configuration is **retained** as a fallback until the
  remaining GitHub Copilot compatibility and context-management limitations are
  resolved. It MUST NOT be deleted.
- **Unresolved limitations that justify retention:** whether VS Code forwards
  `reasoning_effort: max` to the provider is `UNVERIFIED`; Copilot context
  compaction behaviour is `UNVERIFIED`.
- **Not authorized:** deleting `.kilo/`, or treating retention as a permanent
  decision. Retirement remains available through a later explicit decision.
- **Rationale:** Removing the only independently verified fallback while two
  client-side behaviours remain unverified trades a working recovery path for
  tidiness.
- **Evidence of authority:** Directive 001-R6 §2, `OQ-14`.

---

## Decision index

Every option raised during D001 is now resolved by a recorded owner decision.
This table is the index; the entries above are the authority. A recommended
option is not a decision — only an entry with a `Dxxx-yy` identifier is.

| Option | Subject | Decision | Content |
| --- | --- | --- | --- |
| `OQ-1` | Repository visibility | `D001-08` | Public |
| `OQ-2` | Remote publication | `D001-10` | `main` bootstrapped, branch pushed, Draft PR opened |
| `OQ-3` | Initial language contract | `D001-13` | **RATIFIED** — current D001 subset only |
| `OQ-4` | Integer division by zero | `D001-14` | **RATIFIED** — Java-compatible `ArithmeticException` |
| `OQ-5` | Shadowing | `D001-15` | **RATIFIED** — prohibited in the initial subset, revisitable later |
| `OQ-6` | Compilation units | `D001-16` | **RATIFIED** — multiple units, one program-wide function namespace |
| `OQ-7` | Reserved words | `D001-17` | **RATIFIED** — documented list only, no silent expansion |
| `OQ-8` | Source extension | `D001-18` | **RATIFIED** — `.ara` |
| `OQ-9` | License | `D001-08` | Apache-2.0 |
| `OQ-10` | JVM unit naming | `D001-19` | **RATIFIED** — 71-name exclusion as Arara policy; 60 rejected / 11 accepted preserved |
| `OQ-11` | `java.lang` shadowing | `D001-20` | **RATIFIED** — five measured names; not exhaustive; interop stays D005 |
| `OQ-12` | Diagnostic traceability | `D001-21` | **RATIFIED** — canonical references, 68/68 obligations |
| `OQ-13` | Status vocabularies | `D001-22` | **RATIFIED** — permanent separation |
| `OQ-14` | Kilo fallback | `D001-23` | **RATIFIED** — retain; do not delete `.kilo/` |

Nothing in this table authorizes D002. Implementation begins only under a
separate directive after D001 is recorded as accepted.

### Amendments a later decision must name

A ratified rule is not amended by silence. Each of these requires a new decision
that quotes the rule it supersedes:

- Any semantic change to `docs/spec/*` after `D001-13` (`D001-13` states this).
- Expanding the reserved-word list (`D001-17`).
- Changing the source extension (`D001-18`).
- Widening or narrowing the 71-name exclusion (`D001-19`).
- Relaxing the shadowing prohibition (`D001-15`).
- Retiring Kilo (`D001-23`).
