# GitHub Copilot instructions — Arara

This file is a **pointer and a gate**, not a specification. The authoritative
engineering rules live in the repository. Read them; do not paraphrase them into
a prompt.

## Authority order

`AGENTS.md` is the governing document and outranks this file. Read, in order:

1. `AGENTS.md` — authority order, engineering law, scope control, evidence classes.
2. `.kilo/rules/00-governance.md`, `01-compiler-invariants.md`,
   `02-context-recovery.md` — operational rules carried over from the previous
   agent environment. They remain in force until the owner retires them.
3. `docs/progress/STATE.md` — machine-readable current state.
4. `docs/progress/CONTINUATION.md` — compact handoff and first command to run.
5. `docs/progress/DECISIONS.md` — the only evidence of owner authorization.
6. `docs/progress/GATES.md` — objective acceptance gates.

`docs/charter.md`, `docs/architecture.md`, `docs/roadmap.md`, `docs/spec/*`, and
`docs/adr/*` describe the language contract. They are `DRAFT`/`PROPOSED` until the
owner records acceptance.

## Non-negotiable constraints

- **Exactly one active milestone.** It is recorded in `docs/progress/STATE.md`.
  Confirm it from the file every session; do not assume.
- **No implementation without an accepted contract.** Writing a lexer, parser,
  resolver, type checker, IR, backend, build file, test, or CI workflow is
  forbidden unless the active directive authorizes that specific artifact.
- **Only the owner authorizes** milestone transitions, scope changes, dependencies,
  releases, merges, and pushes. A recorded entry in `docs/progress/DECISIONS.md`
  is the only durable evidence.
- **Never push to `main`.** Never force-push. Never rewrite published history.
- **Never commit secrets** or endpoint credentials. Provider configuration belongs
  in user-level settings, not in this repository.
- **Preserve unrelated working-tree changes.** If the tree is dirty with work you
  did not create, report it and stop.
- **English only** for all agent-authored text: reasoning summaries, plans,
  reports, diagnostics, documentation, comments, commit messages, and PR
  descriptions. Quote external evidence verbatim instead of translating it.
- **No fabricated evidence.** Classify claims as `VERIFIED`, `UNVERIFIED`,
  `FAILED`, or `BLOCKED`. A passing build does not prove language correctness.
- **Do not edit `.kilo/agent-manager.json`.** It is persisted UI state.

## Session start

The recovery entry point depends on the harness:

- **Copilot harness:** select the `arara-recovery` agent. It has an explicit
  `read`/`search` allowlist, reconstructs state from durable files and Git
  metadata, and reports discrepancies without modifying the repository. The
  `resume` skill carries the same procedure for Copilot chat.
- **Local harness:** `/resume` runs `.github/prompts/resume.prompt.md`. That file
  is retained for Local-harness compatibility; it is not the Copilot entry point.

Whichever entry point is used, the engineer or owner then runs:

```sh
git branch --show-current
git log --oneline -5
git status --porcelain=v1 --untracked-files=all
```

Compare the result against `docs/progress/STATE.md`. If they disagree, report the
discrepancy and change nothing.

## Where the detail lives

| Need | Read |
| --- | --- |
| Lexical rules | `docs/spec/lexical.md` |
| Grammar | `docs/spec/syntax.ebnf` |
| Semantics and diagnostics | `docs/spec/semantics.md` |
| Conformance obligations | `docs/spec/conformance-plan.md` |
| Gate status vocabulary | `docs/progress/GATES.md` |
| Recovery procedure (Copilot) | `.github/agents/arara-recovery.agent.md`, `.github/skills/resume/SKILL.md` |
| Checkpoint procedure (Copilot) | `.github/skills/checkpoint/SKILL.md` |
| Recovery and checkpoint (Local) | `.github/prompts/resume.prompt.md`, `.github/prompts/checkpoint.prompt.md` |

Do not duplicate normative specification text into prompts or agent files. When a
rule matters, cite the section and let the reader open it.
