# Continuation

Compact handoff. Read this after `AGENTS.md` and before touching anything. It
states what to do and what not to do, with no reliance on prior conversation.

## Where we are

- **Active milestone:** D001 — Foundation. Documentation and configuration only.
- **Authorization:** `D001-01` (milestone) and `D001-06` (language identity) in
  `docs/progress/DECISIONS.md`.
- **Status:** D001 execution is complete. Directive 001-R1 applied the approved
  Arara identity. D001 acceptance is still pending, and no implementation exists
  or is authorized.
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

## What to do next

1. Read `AGENTS.md`, the three rule files, then `STATE.md`, this file,
   `DECISIONS.md`, `GATES.md`.
2. Run the Git checks in `.kilo/commands/resume.md` §3 and compare against
   `STATE.md`.
3. If they agree, report readiness and wait for the owner. Do not start D002.
4. If the owner records acceptance of D001, append a decision entry, do **not**
   rewrite `D001-01` or `D001-06`, and wait for Directive 002.
5. If the owner rules on `.ara` (`OQ-8`), record it as a new decision and change
   only the extension references listed in `STATE.md` §D001-R1.
6. If the owner reports a defect in D001, fix only that defect, re-check the
   grammar's FIRST/FOLLOW properties and cross-references, update `STATE.md`, and
   commit with `checkpoint:`.

## What not to do

- Do not create `build.gradle`, `settings.gradle`, `pom.xml`, `gradlew`, or any
  `src/` tree.
- Do not write lexer, parser, resolver, type-checker, IR, ASM, or verification
  code.
- Do not create tests or CI workflows.
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

Expected: branch `docs/d001-r1-arara-identity`; the D001-R1 commits at HEAD; a
clean tree.

## If the working tree is dirty

Classify every path as project work, unrelated owner change, generated artifact,
or secret. Preserve unrelated changes, never stage them, and record them in
`STATE.md`. If a secret is present, stop and report without committing.

## If a file referenced here is missing

Treat it as a discrepancy under `.kilo/rules/02-context-recovery.md` §Discrepancy
handling. Do not recreate the file from memory. Report which file is missing and
the last commit that should have contained it (`git log --diff-filter=D -- <path>`).
