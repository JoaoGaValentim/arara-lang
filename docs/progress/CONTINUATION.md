# Continuation

Compact handoff. Read this after `AGENTS.md` and before touching anything. It
states what to do and what not to do, with no reliance on prior conversation.

## Where we are

- **Active milestone:** D002 — Lexer and Parser. Authorized by `D002-01`.
- **D001 status:** **ACCEPTED AND INTEGRATED.** Closed under `D001-24`. Do not
  reopen its architecture; its contract is frozen.
- **D002 status:** in progress. The current unit is the D001 documentation
  handover (Directive 002 §3); D002-A has not started.
- **D002 increments:** D002-A (build, CI, lexer) is authorized. D002-B (parser,
  AST) is authorized **conditionally**, only after D002-A's exit conditions are
  verified and its merge confirmed. D002-C is the closure evaluation.
- **Contract:** `FROZEN` at the `D001-13` revision. The four hashes are in
  `docs/progress/audit/D001-R6.md` §B.1 and repeated in `STATE.md`. Verify them
  before and after every implementation integration.
- **License:** Apache-2.0, root `LICENSE`, unmodified ASF text
  (SHA-256 `cfc7749b…3d30`).
- **Agent environment:** GitHub Copilot is primary (`.github/`). Kilo is retained
  as the fallback and must not be deleted (`D001-23`).
- **D003:** NOT AUTHORIZED. No name resolution, type checking, IR, or bytecode.

## Three facts that must stay separate

D001's R6 record wrongly merged these into one. Keep them distinct in every
report:

1. **Contract accepted** by the owner — `D001-24`.
2. **Foundation integrated** into `main` — merge `5441a9f`.
3. **Recovery ledger reconciled** — completed under `D001-R7`, merged as
   `992e84a`. At R6 it was outstanding; that is history, not present state.

## How to read the ledger

`STATE.md` stores three different kinds of revision, and confusing them produces
false discrepancies:

- **`mainline_merge_commit`** — a revision of `main` observed at a named time. It
  is a **historical snapshot, not a live HEAD pin**. Later commits on `main` do not
  invalidate it. Its only hard invariant is that it remains an ancestor of `main`.
- **`ledger_branch` / `last_verified_commit`** — the branch the ledger is committed
  on and the commit *preceding* the ledger commit (`G9-A`). Compare
  `last_verified_commit` to HEAD's **parent**, never to HEAD itself.
- **Live branch and HEAD** — discovered from `.git` at recovery time and never
  stored. A branch or HEAD that differs from the ledger is normal, not an error.

A **true** divergence — the snapshot is not an ancestor of `main`, or the ledger
commit is not descended from `last_verified_commit` on its branch — is a hard
failure. Report it; do not smooth it over.

## What exists

- Authority: `AGENTS.md`, `.kilo/rules/{00,01,02}-*.md`.
- Copilot agents: `.github/agents/arara-{engineer,auditor,recovery}.agent.md`.
  Auditor and recovery declare `tools: ['read', 'search']`, which resolves to a
  read-only **set**, not a single tool: one tool (`view`) in the VS Code agent
  host, five (`view`, `rg`, `glob`, `skill`, `sql`) under the Copilot CLI. Neither
  set contains a writer, a shell, or delegation. Verified by execution in both
  harnesses, not by parsing.
- Copilot recovery entry point: the `resume` and `checkpoint` skills in
  `.github/skills/*/SKILL.md`, plus `arara-recovery.agent.md`.
  `.github/prompts/{resume,checkpoint}.prompt.md` are Local-harness only and are
  retained for compatibility.
- Recovery: `docs/progress/{STATE,CONTINUATION,DECISIONS,GATES}.md`.
- Design: `docs/charter.md`, `docs/architecture.md`, `docs/roadmap.md`,
  `docs/adr/000{1,2,3}-*.md`.
- Language contract (**RATIFIED**, `D001-13`):
  `docs/spec/{lexical.md,syntax.ebnf,semantics.md,conformance-plan.md}`.
- Audit record: `docs/progress/audit/{D001-R7,D001-R6,D001-R5,D001-R4,D001-R3,D001-AUDIT-R2,REPRODUCIBILITY}.md`
  and the two verbatim report archives. `D001-R7.md` is the acceptance-reconciliation
  record. `D001-R6.md` §B.1 holds the frozen hashes. `D001-R4.md` §8 is the
  corrections index for R3/R4. `REPRODUCIBILITY.md` §9d.3 holds the rerun `javac`
  experiment with its sum assertions; §9c.2 is marked invalid and retained.
- Not present: `.github/workflows/`, any build file, any `src/` tree, any test.

## What to do next

1. Read `AGENTS.md`, the three rule files, then `STATE.md`, this file,
   `DECISIONS.md`, `GATES.md`.
2. Run recovery through the Copilot entry point: select the `arara-recovery` agent,
   or use the `resume` skill. `.github/prompts/resume.prompt.md` is for the Local
   harness only. Compare the result against `STATE.md`.
3. Resolve branch and HEAD from Git. Do not compare them against a hardcoded
   expectation.
4. Then work the next authorized unit. For D002-A that means: build foundation with
   a pinned, checksum-verified Gradle wrapper, CI that runs on the exact reviewed
   commit, then the lexer. Read the frozen contract first; do not re-derive rules
   from audit prose.
5. If the owner reports a defect, fix only that defect, re-check the grammar with
   the checkers in `REPRODUCIBILITY.md` §9a, update `STATE.md`, and commit with
   `checkpoint:`.

## What not to do

- Do not modify `docs/spec/*`. A contradiction is escalated, never silently
  amended so a test passes.
- Do not write name resolution, type checking, a typed IR, ASM, JVM class-file
  generation, or a Java interop layer. Those are D003 and D004.
- Do not add ASM, Kotlin sources or a Kotlin compiler dependency, ANTLR/JavaCC or
  any parser generator, packaging, or release automation.
- Do not invent syntactic sugar or a language feature from `docs/charter.md` §4.
- Do not mark a gate `PASS` on evidence that does not meet its criterion. A missing
  CI workflow is not passing CI, and a skipped required test is not `PASS`.
- Do not regenerate a golden expectation to hide a regression. A regenerated golden
  to make a test pass is a gate failure.
- Do not assert `ARA-LEX-0102` to satisfy a coverage counter; the specification
  marks it reserved and never produced, and `D002-G5` requires asserting its
  **absence**.
- Do not treat `++` as an invalid lexical character: it is two valid `+` tokens and
  is a parser diagnostic.
- Do not claim a name is invalid for Java interop without a **control that
  compiles**. An experiment whose control fails proves nothing. This exact error
  occurred twice.
- Do not claim a unit named `String` emits `java/lang/String`. It emits `String`.
- Do not claim the five forbidden shadowing names are an exhaustive analysis of
  Java name collisions. They are a measured subset.
- Do not reuse the figure 59 for `javac`-rejected class names. The correct count is
  60, and `accepted + rejected = candidates` must hold.
- Do not force-push, rewrite published history, squash, rebase, or push directly to
  `main`. A ruleset blocks direct and forced pushes; merges go through a reviewed
  PR with a merge commit.
- Do not create a tag, release, or published artifact.
- Do not delete `.kilo/` before a decision supersedes `D001-23`.
- Do not edit `.kilo/agent-manager.json`.
- Do not treat a missing `tools` declaration as read-only protection. The
  allowlist is the protection.

## First command to run after recovery

```sh
git fetch origin --prune && git branch --show-current && git log --oneline -5 && git status --porcelain=v1 --untracked-files=all
```

Expected: a branch consistent with the ledger's `ledger_branch` or a newer
authorized branch; a clean tree. `origin/main` is the authoritative remote
baseline; local `main` may lag it. Do not assume either is current — read them.

## If the working tree is dirty

Classify every path as project work, unrelated owner change, generated artifact,
or secret. Preserve unrelated changes, never stage them, and record them in
`STATE.md`. If a secret is present, stop and report without committing.

## If a file referenced here is missing

Treat it as a discrepancy under `.kilo/rules/02-context-recovery.md` §Discrepancy
handling. Do not recreate the file from memory. Report which file is missing and
the last commit that should have contained it (`git log --diff-filter=D -- <path>`).
