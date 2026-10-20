---
description: Arara governance rules for documentation, specification, and progress records.
applyTo: "docs/**/*.md,AGENTS.md,README.md,.github/**/*.md"
---

# Documentation and governance edits

## Before editing

- Confirm the active milestone in `docs/progress/STATE.md`. A documentation change
  is still a scoped change: it must belong to the active directive.
- A normative rule change (`docs/spec/*`, `docs/charter.md`) requires an owner
  decision that names the rule. Do not "clarify" semantics on your own initiative.

## While editing

- Never rewrite a historical decision in `docs/progress/DECISIONS.md`. Supersede it
  with a new `Dxxx-yy` entry that names the entry it replaces.
- Preserve the original text of a gate criterion when proposing an amendment. The
  amendment goes in the amendment table with status `PROPOSED`; it is not
  operative until the owner ratifies it.
- Use only the five declared gate statuses: `NOT_STARTED`, `IN_PROGRESS`, `PASS`,
  `FAIL`, `BLOCKED`. Milestone verdicts (`PASS`, `CONDITIONAL`, `BLOCKED`) never
  appear in a gate status column.
- Every quantitative claim needs a command, an environment, and a result. If you
  cannot run it, label the claim `UNVERIFIED`.
- Every experimental claim needs a **control** that succeeds. An experiment whose
  control fails proves nothing; fix the harness before drawing a conclusion.
- Cite sections by number. Do not restate a normative rule in a second document;
  cross-reference it.
- Keep English only. Quoted external evidence stays in its original language.

## After editing

- Update `docs/progress/STATE.md` if the change affects branch, commit, active
  task, gate status, or known limitations.
- Update `docs/progress/CONTINUATION.md` if the change affects the next action.
- Check every internal link you touched resolves.
