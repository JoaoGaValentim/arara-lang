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

## 3. Reconcile three distinct things

Do not merge these into one "matches" verdict. Doing so produced a false
consistency claim in D001-R6.

| # | Thing | Recorded in | Compare against | Failure condition |
| --- | --- | --- | --- | --- |
| a | **Mainline snapshot** — the `main` revision observed at verification | `STATE.md` `mainline_merge_commit` | `git merge-base --is-ancestor <sha> origin/main` | Not an ancestor of `main` → **failure** (rewrite or wrong repo). A newer `main` is normal, not a failure |
| b | **Ledger branch and preceding commit** | `STATE.md` `ledger_branch`, `last_verified_commit` | the ledger commit's parent on that branch | Parent relation broken → **discrepancy** |
| c | **Live branch and HEAD now** | discovered from `.git` | `.git/HEAD`, loose ref, reflog | Live branch differs from `ledger_branch` → **note** (snapshot of another branch), unless (a) or (b) also fails |

Also compare:

| Item | Recorded in | Compare against |
| --- | --- | --- |
| Active milestone and status | `STATE.md` | `DECISIONS.md`, `GATES.md` |
| Active directive | `STATE.md` | `DECISIONS.md` |
| Contract hashes | `STATE.md` `spec_hashes_sha256` | `shasum -a 256 docs/spec/*` |
| D002 authorization | `STATE.md` | `DECISIONS.md` |
| Working tree | never asserted by the ledger | `git status --porcelain`; if unavailable, report `UNVERIFIED` |
| Remote and refs | `STATE.md` | `git remote -v`, `git ls-remote origin` |

A historical snapshot does not become false when `main` advances. Only the
ancestor test can fail. Never weaken that test to make a report look clean.

## 4. Detect unfinished work

- Conflict stages in `git status` (`U`), or `.git/MERGE_HEAD`, `REBASE_HEAD`,
  `CHERRY_PICK_HEAD` present: an operation stopped partway.
- A file named as created in `STATE.md` that is absent: a lost work unit.
- A commit claimed in `STATE.md` that `git log` does not contain: a false record.
- Evidence cited in `GATES.md` that names a command not re-runnable here.

## 5. Report before acting

```text
Active milestone / status:
Authorized scope:
Live branch / HEAD:
Ledger branch / preceding commit:   relation: parent/equal/other
Mainline snapshot:                  ancestor of main: yes/no
Contract:                           FROZEN at <hash> / mismatch
D002:                               <STATE.d002_authorization value>
Implementation state:               <STATE.implementation_state; verify by listing the tree>
Working tree:                       UNVERIFIED | <value>
Discrepancies:                      none | <list>
Unfinished work:                    none | <details>
Next authorized action:             <exact command or file>
Prohibited now:                     <list>
```

## 6. Rules

- If anything disagrees, **stop** and report. Do not repair the tree or the
  document first.
- Never start an implementation task because a milestone "looks complete".
- Never accept a milestone on the owner's behalf.
- Never push, merge, tag, or release.
- Never run `git reset`, `git checkout --`, `git clean`, or `git stash drop`.
- If a check cannot be performed, report `UNVERIFIED`. Do not guess.
