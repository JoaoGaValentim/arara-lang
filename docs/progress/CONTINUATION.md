# Continuation

Compact handoff. Read this after `AGENTS.md` and before touching anything. It
states what to do and what not to do, with no reliance on prior conversation.

## Where we are

- **Active milestone:** D001 — Foundation. Documentation and configuration only.
- **Active directive:** `D001-R5` — Copilot activation, publication, and final
  foundation verification.
- **Authorization:** `D001-01` (milestone), `D001-06` (language identity),
  `D001-07` (language and vocabulary policy), `D001-08` (visibility, license,
  agent environment), `D001-09` (G9 reclassification), `D001-10` (R5 operations,
  publication, conditional `G9-A` ratification), `D001-11` (`G9-A` ratified,
  `D001-G9` reclassified).
- **Status:** D001 is **`BLOCKED`**, not accepted — but no longer because of a
  gate. Fifteen gates are `PASS` and none is `BLOCKED`; `D001-G9` moved from
  `BLOCKED` to `PASS` when the owner ratified `G9-A` in `D001-11`. Acceptance
  still needs `OQ-3`, `OQ-10`, and `OQ-11`, which are owner decisions, not
  evidence gaps. No implementation exists or is authorized.
- **Branch:** `infra/d001-r4-copilot-migration`.
- **Remote:** `origin` → `https://github.com/JoaoGaValentim/arara-lang.git`.
  Public. `main` is published at `717fcc6` and is the default branch; the
  migration branch is published at `e707b10` (its R5 content commit is `230abd6`).
  A **Draft** PR (#1) is open against `main`; the live head advances with each
  documentation checkpoint. Nothing is merged, and merging is not authorized.
- **License:** Apache-2.0, root `LICENSE`, unmodified ASF text
  (SHA-256 `cfc7749b…3d30`). It is on the migration branch; `main` receives it
  only if the Draft PR is merged.
- **Agent environment:** GitHub Copilot is primary (`.github/`). Kilo remains
  tracked and is the fallback (`OQ-14` records retirement as open).

## What exists

- Authority: `AGENTS.md`, `.kilo/rules/{00,01,02}-*.md`.
- Copilot agents: `.github/agents/arara-{engineer,auditor,recovery}.agent.md`.
  Auditor and recovery declare `tools: ['read', 'search']`, which resolves to a
  single read-only file tool; this was verified by execution, not by parsing.
- Copilot recovery entry point: the `resume` and `checkpoint` skills in
  `.github/skills/*/SKILL.md`, plus `arara-recovery.agent.md`.
  `.github/prompts/{resume,checkpoint}.prompt.md` are Local-harness only and are
  retained for compatibility.
- Recovery: `docs/progress/{STATE,CONTINUATION,DECISIONS,GATES}.md`.
- Design: `docs/charter.md`, `docs/architecture.md`, `docs/roadmap.md`,
  `docs/adr/000{1,2,3}-*.md`.
- Language contract (draft): `docs/spec/{lexical.md,syntax.ebnf,semantics.md,conformance-plan.md}`.
- Audit record: `docs/progress/audit/{D001-R5,D001-R4,D001-R3,D001-AUDIT-R2,REPRODUCIBILITY}.md`
  and the two verbatim report archives. `D001-R5.md` is the current directive
  report (sections A–N). `D001-R4.md` §8 is the corrections index for R3/R4.
  `REPRODUCIBILITY.md` §9d.3 holds the rerun `javac` experiment with its sum
  assertions; §9c.2 is marked invalid and retained.

## What to do next

1. Read `AGENTS.md`, the three rule files, then `STATE.md`, this file,
   `DECISIONS.md`, `GATES.md`.
2. Run recovery through the Copilot entry point: select the `arara-recovery` agent,
   or use the `resume` skill. `.github/prompts/resume.prompt.md` is for the Local
   harness only. Compare the result against `STATE.md`.
3. If they agree, report readiness and wait for the owner. Do not start D002.
4. If the owner rules on `OQ-3`, `OQ-10`, or `OQ-11`, record each as a new decision
   entry. Do not rewrite `D001-01`, `D001-06`, or any audit record. These three are
   the only remaining obstacles to D001 acceptance.
5. If the owner rules on `OQ-14`, retire Kilo in one separate, minimal, reversible
   commit. Until then, `.kilo/` stays.
6. If the owner authorizes merging the Draft PR, that is a new decision entry. Do
   not merge because the branch is green.
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
- Do not claim the five forbidden shadowing names are an exhaustive analysis of
  Java name collisions. They are a measured subset.
- Do not reuse the figure 59 for `javac`-rejected class names. The correct count is
  60, and `accepted + rejected = candidates` must hold.
- Do not merge, tag, or release. The Draft PR is for review; merging it is not
  authorized. Do not force-push and do not push to `main` directly — a ruleset now
  blocks both.
- Do not rename files or rewrite Git history.
- Do not add a language feature from `docs/charter.md` §4, not even as a stub.
- Do not weaken or delete a rule in `AGENTS.md`, `.kilo/rules/*`, or
  `docs/progress/GATES.md`.
- Do not edit `.kilo/agent-manager.json`.
- Do not delete `.kilo/` before the owner rules on `OQ-14`.
- Do not treat a missing `tools` declaration as read-only protection. The
  allowlist is the protection.

## First command to run after recovery

```sh
git branch --show-current && git log --oneline -5 && git status --porcelain=v1 --untracked-files=all
```

Expected: branch `infra/d001-r4-copilot-migration`; `main` at `717fcc6` on the
remote; a clean tree. HEAD is the latest checkpoint, so it is later than the R5
content commit `230abd6`. `STATE.md` records `last_verified_commit` as the commit
preceding the ledger commit: compare it to HEAD's **parent**, not to HEAD itself.
The four paths the concurrent process touched after the content commit were
reconciled and committed; they are not expected to be dirty.

## If the working tree is dirty

Classify every path as project work, unrelated owner change, generated artifact,
or secret. Preserve unrelated changes, never stage them, and record them in
`STATE.md`. If a secret is present, stop and report without committing.

## If a file referenced here is missing

Treat it as a discrepancy under `.kilo/rules/02-context-recovery.md` §Discrepancy
handling. Do not recreate the file from memory. Report which file is missing and
the last commit that should have contained it (`git log --diff-filter=D -- <path>`).
