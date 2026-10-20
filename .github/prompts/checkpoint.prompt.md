---
description: Record a durable checkpoint of the current work unit in the Arara recovery ledger.
---

# Checkpoint

Persist the current work unit so a fresh session can resume without the
transcript. Run this after a completed unit, before a risky operation, and before
compaction.

## 1. Establish what is true

```sh
git branch --show-current
git rev-parse HEAD
git status --porcelain=v1 --untracked-files=all
git log --oneline -3
```

Do not write a checkpoint from memory. Every value below comes from these
commands or from a file you read.

## 2. Update `docs/progress/STATE.md`

Update the YAML header. Keep every field; do not delete one because it is
inconvenient.

```yaml
active_directive:
active_milestone:
milestone_authorization:
process_authorization:
current_task:
current_subtask:
last_completed_unit:
branch:
last_verified_commit:
audit_commit:
commit_convention:
remote:
working_tree:
```

`last_verified_commit` is the commit **preceding** the one that will contain the
ledger. State that explicitly; a ledger cannot record its own SHA.

## 3. Update `docs/progress/CONTINUATION.md`

It must contain:

- Current directive and authorized scope
- Branch and commit
- Completed work in this unit
- Tests or checks actually run, with results
- Outstanding issues and known limitations
- Pending owner decisions
- The exact next action, with the file and command
- Explicit prohibitions

## 4. Record decisions and gates

- A new owner authorization goes in `docs/progress/DECISIONS.md` as a new
  `Dxxx-yy` entry. Never edit a historical entry.
- A gate status change goes in `docs/progress/GATES.md` with the commit, command,
  and environment that justify it. Only `NOT_STARTED`, `IN_PROGRESS`, `PASS`,
  `FAIL`, `BLOCKED` are legal statuses.

## 5. Verify before committing

- Every claim in the checkpoint names a command or a file.
- Anything you could not check is labelled `UNVERIFIED`.
- No secret, token, or credential is present in the diff.
- The diff contains only this unit's work.

## 6. Commit

```sh
git add docs/progress/STATE.md docs/progress/CONTINUATION.md
git commit -m "checkpoint(<milestone>): <one-line description>"
```

Do not push. Do not amend a published commit.
