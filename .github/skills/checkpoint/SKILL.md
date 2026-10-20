---
name: checkpoint
description: Record a durable checkpoint of the current work unit in the Arara recovery ledger. Use after a completed unit, before a risky operation, or before compaction.
disable-model-invocation: false
---
# Checkpoint

Persist the current work unit so a fresh session can resume without the
transcript. Run this after a completed unit, before a risky operation, and before
compaction. Under the Copilot harness this skill replaces
`.github/prompts/checkpoint.prompt.md`, which serves the Local harness.

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
milestone_status:
milestone_authorization:
acceptance_decision:
contract_status:
contract_decision:
spec_hashes_sha256:
process_authorization:
current_task:
current_subtask:
last_completed_unit:
ledger_branch:
last_verified_commit:
audit_commit:
mainline_observed_at:
mainline_merge_commit:
mainline_merge_parents:
mainline_snapshot_rule:
ledger_commit_rule:
live_state_rule:
remote:
working_tree_at_write:
```

Three rules, each learned from a real defect:

1. `last_verified_commit` is the commit **preceding** the one that will contain
   the ledger, on `ledger_branch`. A ledger cannot record its own SHA.
2. `mainline_merge_commit` is a **historical observation**, never a live pin. Do
   not rewrite it when `main` advances, and do not claim it equals today's `main`.
   Its only hard invariant is that it remains an ancestor of `main`.
3. Never record the current branch or HEAD as a ledger fact. A recovery session
   discovers those live and reports them separately. Recording them invites a
   later reader to mistake a snapshot for the present.

Also never assert the working tree is clean at read time. Record the state at
write time (`working_tree_at_write`) and let the reader verify it.

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
