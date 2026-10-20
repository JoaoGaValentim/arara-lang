# AGENTS.md — Arara Engineering Authority

This file is the primary persistent engineering authority for this repository.
It binds every human and AI contributor, including autonomous agents. It survives
conversation compaction and new sessions. When this file conflicts with a chat
transcript, this file wins.

Project: **Arara**, a small statically typed JVM language. Repository:
`JoaoGaValentim/arara-lang`.

## 1. Authority order

Highest authority first:

1. The owner (repository owner) acting through an explicit, recorded directive.
2. `docs/charter.md`, `docs/spec/*`, and `docs/progress/GATES.md` for their
   respective scopes.
3. Accepted ADRs in `docs/adr/` within their stated scope.
4. `AGENTS.md` and `.kilo/rules/*.md` (this file defines process, not language
   semantics).
5. `docs/architecture.md` and `docs/roadmap.md` (descriptive, not normative
   where they conflict with the specification).
6. Everything else, including model-generated proposals.

Rules:

- Conversation history is not an authoritative substitute for repository records.
- A model-generated proposal is never equivalent to owner approval.
- Only the owner authorizes milestone transitions and scope changes.
- A directive recorded in `docs/progress/DECISIONS.md` with its identifier and
  date is the only durable evidence of owner authorization.
- If a request conflicts with an accepted contract, stop and report the conflict.
  Do not reinterpret the contract to fit the request.

## 2. Fundamental engineering law

**No implementation without a verified contract.**

Every phase follows this order, with no step skipped:

1. Define exact scope.
2. Specify syntax and semantics.
3. Identify ambiguities and invariants.
4. Define positive and negative conformance tests.
5. Review architectural compatibility.
6. Establish objective acceptance gates in `docs/progress/GATES.md`.
7. Obtain explicit owner authorization.
8. Implement only the authorized subset.
9. Execute real tests and independent verification.
10. Record evidence and stop at the next gate.

A phase is never successful because documentation looks complete, because code
compiles, or because a build passes. A passing build does not prove language
correctness. Never silently reinterpret a specification to accommodate an
implementation.

## 3. Scope control

- Exactly one milestone may be active. The active milestone is recorded in
  `docs/progress/STATE.md`. D001 is the only authorized milestone until the owner
  records a transition.
- No implementation outside the authorized milestone.
- No speculative refactoring, no "while we are here" cleanup, no hidden feature
  expansion.
- No new dependency, build plugin, or backend target without an ADR or an
  explicit owner decision recorded in `docs/progress/DECISIONS.md`.
- No silent change to frozen semantics. A semantic change requires a new
  decision record that names the superseded rule.
- Deferred features listed in `docs/charter.md` must not be introduced to
  simplify an implementation. Exclusion is a design constraint, not a gap.
- If a task appears to require an unauthorized feature, stop and report instead
  of inventing a workaround.

## 4. Repository safety

- Never force-push. Never rewrite published history.
- Never use destructive Git cleanup (`reset --hard`, `clean -fdx`, branch
  deletion, stashdrop) to resolve uncertainty. Investigate first.
- Never delete or overwrite unrelated user changes. If the working tree contains
  changes you did not make, preserve them and report them.
- Never push directly to `main`. Development happens on `develop` or on a
  short-lived branch proposed by the owner.
- Never approve, merge, or mark a PR ready without explicit owner authorization.
- Keep implementation PRs small and independently reviewable.
- Never create a release, tag, or published artifact without authorization.
- Never commit secrets, tokens, API keys, or provider credentials.
- Never edit `.kilo/agent-manager.json`; it is persisted UI state, not
  configuration.

## 5. Evidence discipline

- Never fabricate CI results, test output, tool versions, or command output.
- Never claim tests passed unless they were executed in this session or are
  recorded with a commit and environment in `docs/progress/STATE.md`.
- Never claim a feature works based only on code inspection.
- Record the exact commit, command, and environment for every verification
  result.
- Classify every claim as one of: `VERIFIED`, `UNVERIFIED`, `FAILED`, `BLOCKED`.
- Preserve failing tests and failing evidence until the defect is resolved or the
  owner records a decision. Do not delete evidence to make a gate look green.
- When a tool, dependency, or provider setting cannot be verified, say so
  explicitly and label it `UNVERIFIED`.
- "I could not check" is an acceptable report. "It probably works" is not.

## 6. Context continuity

- Restore project state from durable files, never solely from the current
  conversation.
- Before continuing work, follow `.kilo/commands/resume.md`.
- After every completed work unit, before any high-risk operation, and before
  voluntary compaction, update `docs/progress/STATE.md` and
  `docs/progress/CONTINUATION.md`.
- Preserve exact next actions, including the file and command to run.
- Revalidate Git state after any interrupted session.
- Never assume an interrupted operation succeeded. Verify, then act.
- Keep work units small enough that an interruption loses at most the current
  unfinished unit, not architectural knowledge.

## 7. Language-specific invariants

These hold for every milestone. `docs/spec/semantics.md` is normative for detail;
these are the non-negotiable summaries.

- Source is UTF-8. Line endings `\n`, `\r\n`, and `\r` are normalized to `\n`
  before lexing.
- Every token carries a half-open source span `[start, end)` with 1-based line
  and column, counting Unicode code points.
- Every diagnostic carries a stable code, a severity, a primary span, and an
  optional list of related spans with notes. Diagnostics never abort at the first
  error when more can be reported soundly.
- Static typing is mandatory. There is no implicit conversion between `Int`,
  `Boolean`, and `String`. There is no nullable type in the initial subset.
- `val` is a single-assignment binding; `var` is mutable. Assignment to a `val`
  is a compile-time error.
- Integer arithmetic is 32-bit two's complement with Java `int` overflow
  semantics; division by zero throws at runtime rather than being undefined.
  This is specified in `docs/spec/semantics.md`, not inherited implicitly.
- `String` equality is structural equality of UTF-16 code unit sequences, and it
  is specified explicitly. Reference identity is never the default equality.
- Evaluation order is left-to-right, fully specified in `docs/spec/semantics.md`.
- Every reachable non-`Unit` function must return on all paths. Falling off the
  end of a non-`Unit` function is a compile-time error.
- Unsupported constructs are rejected with a specific diagnostic, never accepted
  with undefined behavior.
- The JVM backend emits class files for classfile major version 65 (JDK 21). No
  other target is authorized.
- Every emitted class must pass `CheckClassAdapter` analysis and the JVM verifier
  before it is treated as evidence.

## 8. Prohibited actions in the current state

While D001 is the active milestone, these are forbidden:

- Writing lexer, parser, type checker, IR, backend, or bytecode emission code.
- Adding a `build.gradle`, `pom.xml`, or any compiler source tree.
- Starting D002 or any later milestone.
- Adding language features beyond the D001 documented subset.
- Weakening or bypassing any rule in this file, `.kilo/rules/`, or
  `docs/progress/GATES.md` to make progress.
- Pushing to `main`, merging PRs, publishing packages, or producing releases.

## 9. Required recovery procedure

On every new session or after any interruption:

1. Read this file.
2. Read `.kilo/rules/00-governance.md`, `01-compiler-invariants.md`, and
   `02-context-recovery.md`.
3. Read `docs/progress/STATE.md`.
4. Read `docs/progress/CONTINUATION.md`.
5. Read `docs/progress/DECISIONS.md`.
6. Read `docs/progress/GATES.md`.
7. Inspect `git branch --show-current`, `git log --oneline -5`, `git status`.
8. Compare recorded state with actual files and Git.
9. If they disagree, stop and report the discrepancy before changing anything.
10. Resume only the currently authorized task.

The exact command form of this procedure is `.kilo/commands/resume.md`.

## 10. Reporting

- Report facts, not intentions. Every claim of completion names the file, command,
  and result that supports it.
- Use the four evidence classes from Section 5 in all status reports.
- When a mandatory gate is unverified, the verdict is not `PASS`.
- End a milestone with a formal report containing environment baseline, files
  changed, verification commands and actual output, Git evidence, open questions,
  acceptance recommendation, and the next proposed directive.
- Stop at the gate. Do not proceed into the next milestone automatically.

## 11. Communication language

All agent-authored communication is English only. This covers visible reasoning
and analysis traces, planning and execution messages, progress updates and
checkpoint summaries, technical reports, audits and diagnostics, source code
comments and documentation, Git commit messages and pull request descriptions,
and error explanations and recovery instructions.

Chinese (Mandarin), Chinese characters, and automatic language switching are
prohibited in agent-authored communication. If Chinese output is produced
unexpectedly, return to English immediately and continue the technical task
without changing it.

Exceptions are permitted only for exact quotations of external material, existing
source data, or an explicit owner request. Never translate or modify external
evidence merely to satisfy this rule.

This policy survives context compaction, agent handoffs, and session recovery.
It is mandatory for every directive until the owner rescinds it.

## 12. Status vocabularies

Two vocabularies exist and must not be mixed.

Gate statuses are closed. A gate row may carry only `NOT_STARTED`,
`IN_PROGRESS`, `PASS`, `FAIL`, or `BLOCKED`, as declared in
`docs/progress/GATES.md`. No other identifier may appear in a gate table, and
adding a value requires an owner decision amending the declaration first.

Milestone verdicts are separate. A verdict is `PASS`, `CONDITIONAL`, or
`BLOCKED`, and appears only in a self-assessment line or report, never in a gate
status column.

Never introduce an undocumented status identifier to describe an intermediate
state. If the declared values do not fit, the vocabulary declaration is wrong and
must be corrected by explicit decision before use.
