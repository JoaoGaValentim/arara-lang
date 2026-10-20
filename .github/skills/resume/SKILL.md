---
name: resume
description: Reconstruct the authorized Arara state from durable files and Git metadata and report the next safe action. Use when starting a session or after an interruption.
disable-model-invocation: false
---
# Resume

Reconstruct state from repository files only. Do not rely on conversation
history, and do not change anything until the reconstruction is reported.

This skill and `arara-recovery.agent.md` are the **Copilot-harness** recovery
path. `.github/prompts/resume.prompt.md` is the Local-harness equivalent and is
retained for compatibility, not assumed to run here.

## 1. Read, in this order

1. `AGENTS.md`
2. `.kilo/rules/00-governance.md`
3. `.kilo/rules/01-compiler-invariants.md`
4. `.kilo/rules/02-context-recovery.md`
5. `docs/progress/STATE.md`
6. `docs/progress/CONTINUATION.md`
7. `docs/progress/DECISIONS.md`
8. `docs/progress/GATES.md`

## 2. Inspect actual state

```sh
git branch --show-current
git rev-parse HEAD
git log --oneline -5
git status --porcelain=v1 --untracked-files=all
git stash list
git tag -l
git remote -v
```

## 3. Reconcile

Compare the recorded values with the command output. Cover:

| Item | Recorded in | Compare against |
| --- | --- | --- |
| Active milestone | `STATE.md` | `DECISIONS.md` |
| Active directive | `STATE.md` | `DECISIONS.md` |
| Branch | `STATE.md` | `git branch --show-current` |
| Last verified commit | `STATE.md` | `git log --oneline` |
| Audit commit | `STATE.md` | `git log --oneline` |
| Working tree | `STATE.md` | `git status --porcelain` |
| Remote and refs | `STATE.md` | `git remote -v`, `git ls-remote origin` |

## 4. Detect unfinished work

- Conflict stages in `git status` (`U`), or `.git/MERGE_HEAD`, `REBASE_HEAD`,
  `CHERRY_PICK_HEAD` present: an operation stopped partway.
- A file named as created in `STATE.md` that is absent: a lost work unit.
- A commit claimed in `STATE.md` that `git log` does not contain: a false record.
- Evidence cited in `GATES.md` that names a command not re-runnable here.

## 5. Report before acting

```text
Active milestone:
Authorized scope:
Branch / HEAD:
Working tree:
Discrepancies:      none | <list>
Unfinished work:    none | <details>
Next authorized action: <exact command or file>
Prohibited now:     <list>
```

## 6. Rules

- If anything disagrees, **stop** and report. Do not repair the tree or the
  document first.
- Never start an implementation task because a milestone "looks complete".
- Never accept a milestone on the owner's behalf.
- Never push, merge, tag, or release.
- Never run `git reset`, `git checkout --`, `git clean`, or `git stash drop`.
- If a check cannot be performed, report `UNVERIFIED`. Do not guess.
