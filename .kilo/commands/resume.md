---
description: Reconstruct project state from durable files and verify it against Git before doing any work
---

# Resume

Read-only recovery procedure. It reconstructs state from persisted repository
information alone. Do not modify files during recovery. If a discrepancy is
found, report it and stop.

## 1. Authority and process

Read, in order:

1. `AGENTS.md`
2. `.kilo/rules/00-governance.md`
3. `.kilo/rules/01-compiler-invariants.md`
4. `.kilo/rules/02-context-recovery.md`

## 2. Durable state

Read, in order:

1. `docs/progress/STATE.md`
2. `docs/progress/CONTINUATION.md`
3. `docs/progress/DECISIONS.md`
4. `docs/progress/GATES.md`
5. `docs/charter.md`

## 3. Repository inspection

Run and record the raw output:

```sh
git branch --show-current
git log --oneline -5
git status --porcelain=v1 --untracked-files=all
git rev-parse HEAD
git remote -v
```

## 4. Consistency checks

Verify each of the following and state PASS or FAIL per item:

1. The active milestone in `STATE.md` has a matching authorization entry in
   `DECISIONS.md`.
2. The recorded branch equals the actual branch.
3. The recorded HEAD commit exists in `git log`.
4. Every file recorded as modified appears in `git status`, and every file in
   `git status` is classified in `STATE.md` as project work or unrelated.
5. Every verification claim in `STATE.md` has a command and a result.
6. No gate is marked PASS in `GATES.md` without evidence.
7. No file exists that the active milestone forbids.

## 5. Summary

Produce a short recovery summary with these fields:

```text
ACTIVE DIRECTIVE:
ACTIVE MILESTONE:
AUTHORIZED SCOPE:
BRANCH:
HEAD:
WORKING TREE:
CURRENT TASK:
NEXT SAFE ACTION:
FORBIDDEN NOW:
DISCREPANCIES:
```

## 6. Stop conditions

Stop and report, without changing anything, when:

- Any consistency check fails.
- `STATE.md` is internally contradictory.
- A git object referenced by the ledger is missing.
- The working tree contains changes not attributable to the recorded task.
- The next safe action requires authorization that `DECISIONS.md` does not contain.
