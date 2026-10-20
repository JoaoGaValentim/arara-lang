# Continuation

Compact handoff. Read this after `AGENTS.md` and before touching anything. It
states what to do and what not to do, with no reliance on prior conversation.

## Where we are

- **Active milestone:** D001 — Foundation. Documentation and configuration only.
- **Authorization:** `D001-01` (milestone) and `D001-06` (language identity) in
  `docs/progress/DECISIONS.md`.
- **Status:** D001 execution and Directives 001-R1, 001-R2, and 001-R3 are
  complete. All fifteen D001 gates are `PASS`. The milestone verdict is
  **`CONDITIONAL`** because authorization is outstanding, not because evidence is
  missing: three decisions gate acceptance (`OQ-3`, `OQ-10`, `OQ-11`) and one
  criterion amendment (`G9-A`) needs approval. D001 acceptance is pending owner
  ratification. No implementation exists or is authorized.
- **Branch:** `docs/d001-r1-arara-identity`, local only. Nothing has been pushed.
- **Remote:** `origin` → `https://github.com/JoaoGaValentim/arara-lang.git`,
  private, empty, no commits pushed.

## What exists

- Authority: `AGENTS.md`, `.kilo/rules/{00,01,02}-*.md`.
- Recovery: `.kilo/commands/resume.md`, `.kilo/commands/checkpoint.md`,
  `docs/progress/{STATE,CONTINUATION,DECISIONS,GATES}.md`.
- Design: `docs/charter.md`, `docs/architecture.md`, `docs/roadmap.md`,
  `docs/adr/000{1,2,3}-*.md`.
- Language contract (draft): `docs/spec/{lexical.md,syntax.ebnf,semantics.md,conformance-plan.md}`.
- Audit record: `docs/progress/audit/{D001-R3,D001-AUDIT-R2,REPRODUCIBILITY}.md`
  and the two verbatim report archives. Read `D001-R3.md` §9 for the current
  decision matrix and §2–§5 for the corrections; `D001-AUDIT-R2.md` is the prior
  pass and contains three claims that `D001-R3` corrects. `REPRODUCIBILITY.md` §9
  holds the checker listings that replace the uncommitted ad-hoc scripts.

## What to do next

1. Read `AGENTS.md`, the three rule files, then `STATE.md`, this file,
   `DECISIONS.md`, `GATES.md`.
2. Run the Git checks in `.kilo/commands/resume.md` §3 and compare against
   `STATE.md`.
3. If they agree, report readiness and wait for the owner. Do not start D002.
4. If the owner records acceptance of D001, append a decision entry, do **not**
   rewrite `D001-01`, `D001-06`, or the D001-R2 audit record, and wait for
   Directive 002.
5. If the owner rules on `.ara` (`OQ-8`), record it as a new decision and change
   only the extension references listed in `STATE.md` §D001-R1.
6. `OQ-10`, `OQ-11`, `OQ-12`, and the §1.2b naming policy have already been
   applied to the specification and are marked *pending ratification*. If the owner
   ratifies, record the decision entry and change nothing else. If the owner
   reverses, the normative change belongs in the **same commit** as the decision,
   and the affected gates are re-evaluated in that commit.
7. If the owner rules on `G9-A`, record the approval. Do not silently replace the
   proposed criterion with a different one, and do not delete the original text.
8. If the owner reports a defect in D001, fix only that defect, re-check the
   grammar's FIRST/FOLLOW properties and cross-references with the checkers in
   `REPRODUCIBILITY.md` §9, update `STATE.md`, and commit with `checkpoint:`.

## What not to do

- Do not create `build.gradle`, `settings.gradle`, `pom.xml`, `gradlew`, or any
  `src/` tree.
- Do not write lexer, parser, resolver, type-checker, IR, ASM, or verification
  code.
- Do not create tests or CI workflows.
- Do not mark a gate `PASS` on evidence that does not meet its criterion. If the
  work is missing, do the work. If the criterion is wrong, propose an amendment
  that preserves the original text and wait for approval. `BLOCKED` means the
  criterion cannot be evaluated yet, not that it is inconvenient. Only the five
  declared gate statuses are legal; a milestone verdict is a separate vocabulary
  (`PASS`, `CONDITIONAL`, `BLOCKED`).
- Do not claim a name is invalid for Java interop without running both a control
  that compiles and the probe. The D001-R2 keyword count and the five-name
  `javac` claim were both wrong for want of a control.
- Do not push, merge, tag, release, or change repository settings.
- Do not rename files or rewrite Git history.
- Do not add a language feature from `docs/charter.md` §4, not even as a stub.
- Do not weaken or delete a rule in `AGENTS.md`, `.kilo/rules/*`, or
  `docs/progress/GATES.md`.
- Do not edit `.kilo/agent-manager.json`.
- Do not "fix" a gate by deleting evidence or regenerating a golden that has not
  yet been created.

## First command to run after recovery

```sh
git branch --show-current && git log --oneline -5 && git status --porcelain=v1 --untracked-files=all
```

Expected: branch `docs/d001-r1-arara-identity`; the D001-R1 commits plus the
D001-R2 audit commit at HEAD; a clean tree. The branch stays local and unpushed by
design.

## If the working tree is dirty

Classify every path as project work, unrelated owner change, generated artifact,
or secret. Preserve unrelated changes, never stage them, and record them in
`STATE.md`. If a secret is present, stop and report without committing.

## If a file referenced here is missing

Treat it as a discrepancy under `.kilo/rules/02-context-recovery.md` §Discrepancy
handling. Do not recreate the file from memory. Report which file is missing and
the last commit that should have contained it (`git log --diff-filter=D -- <path>`).
