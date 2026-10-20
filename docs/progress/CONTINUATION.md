# Continuation

Compact handoff. Read this after `AGENTS.md` and before touching anything. It
states what to do and what not to do, with no reliance on prior conversation.

## Where we are

- **Active milestone:** D001 — Foundation. Documentation and configuration only.
- **Active directive:** `D001-R4` — public repository, Apache-2.0, and Copilot
  migration.
- **Authorization:** `D001-01` (milestone), `D001-06` (language identity),
  `D001-07` (language and vocabulary policy), `D001-08` (visibility, license,
  agent environment), `D001-09` (G9 reclassification).
- **Status:** D001 is **`BLOCKED`**, not accepted. Fourteen gates are `PASS`;
  `D001-G9` is `BLOCKED` because amendment `G9-A` has never been ratified and is
  not operative. D001 acceptance still needs `OQ-3`, `OQ-10`, `OQ-11`, and a ruling
  on `G9-A`. No implementation exists or is authorized.
- **Branch:** `infra/d001-r4-copilot-migration`, created from `78ca162`.
- **Remote:** `origin` → `https://github.com/JoaoGaValentim/arara-lang.git`.
  Public since `D001-R4`. Still zero pushed refs: the visibility authorization did
  not authorize pushing branches.
- **License:** Apache-2.0, root `LICENSE`, unmodified ASF text
  (SHA-256 `cfc7749b…3d30`).
- **Agent environment:** GitHub Copilot is primary (`.github/`). Kilo remains
  tracked and is the fallback until a fresh Copilot session verifies recovery.

## What exists

- Authority: `AGENTS.md`, `.kilo/rules/{00,01,02}-*.md`.
- Copilot: `.github/copilot-instructions.md`,
  `.github/instructions/*.instructions.md`, `.github/agents/arara-{engineer,
  auditor,recovery}.agent.md`, `.github/prompts/{resume,checkpoint}.prompt.md`.
- Recovery: `docs/progress/{STATE,CONTINUATION,DECISIONS,GATES}.md`.
- Design: `docs/charter.md`, `docs/architecture.md`, `docs/roadmap.md`,
  `docs/adr/000{1,2,3}-*.md`.
- Language contract (draft): `docs/spec/{lexical.md,syntax.ebnf,semantics.md,conformance-plan.md}`.
- Audit record: `docs/progress/audit/{D001-R4,D001-R3,D001-AUDIT-R2,REPRODUCIBILITY}.md`
  and the two verbatim report archives. `D001-R4.md` §8 is the current
  corrections index. `REPRODUCIBILITY.md` §9d and §9e hold the corrected `javac`
  and JVM internal-name experiments; §9c.2 is marked invalid and retained.

## What to do next

1. Read `AGENTS.md`, the three rule files, then `STATE.md`, this file,
   `DECISIONS.md`, `GATES.md`.
2. Run the recovery procedure in `.github/prompts/resume.prompt.md` and compare
   against `STATE.md`.
3. If they agree, report readiness and wait for the owner. Do not start D002.
4. If the owner rules on `G9-A` (`docs/progress/DECISIONS.md` step 1), record the
   decision and reclassify `D001-G9` in `GATES.md` in the same commit. Do not
   delete the original criterion text.
5. If the owner rules on `OQ-3`, `OQ-10`, `OQ-11`, record each as a new decision
   entry. Do not rewrite `D001-01`, `D001-06`, or any audit record.
6. If the owner authorizes pushing `infra/d001-r4-copilot-migration`, push that
   branch only — never `main`.
7. If the owner reports a defect, fix only that defect, re-check the grammar with
   the checkers in `REPRODUCIBILITY.md` §9a, update `STATE.md`, and commit with
   `checkpoint:`.

## What not to do

- Do not create `build.gradle`, `settings.gradle`, `pom.xml`, `gradlew`, or any
  `src/` tree.
- Do not write lexer, parser, resolver, type-checker, IR, ASM, or verification
  code.
- Do not create tests or CI workflows.
- Do not mark a gate `PASS` on evidence that does not meet its criterion, and do
  not mark one `PASS` against a criterion that is only `PROPOSED`.
- Do not claim a name is invalid for Java interop without a **control that
  compiles**. An experiment whose control fails proves nothing. This exact error
  occurred twice.
- Do not claim a unit named `String` emits `java/lang/String`. It emits `String`.
- Do not push, merge, tag, release, or change repository settings without explicit
  authorization for that specific action. The visibility change was authorized;
  pushing was not.
- Do not rename files or rewrite Git history.
- Do not add a language feature from `docs/charter.md` §4, not even as a stub.
- Do not weaken or delete a rule in `AGENTS.md`, `.kilo/rules/*`, or
  `docs/progress/GATES.md`.
- Do not edit `.kilo/agent-manager.json`.
- Do not delete `.kilo/` until a fresh Copilot session proves recovery works.

## First command to run after recovery

```sh
git branch --show-current && git log --oneline -5 && git status --porcelain=v1 --untracked-files=all
```

Expected: branch `infra/d001-r4-copilot-migration`; `78ca162` below the D001-R4
commits; a clean tree. Nothing is pushed by design.

## If the working tree is dirty

Classify every path as project work, unrelated owner change, generated artifact,
or secret. Preserve unrelated changes, never stage them, and record them in
`STATE.md`. If a secret is present, stop and report without committing.

## If a file referenced here is missing

Treat it as a discrepancy under `.kilo/rules/02-context-recovery.md` §Discrepancy
handling. Do not recreate the file from memory. Report which file is missing and
the last commit that should have contained it (`git log --diff-filter=D -- <path>`).
