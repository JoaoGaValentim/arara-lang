# 02 — Context Recovery

This rule file is subordinate to `AGENTS.md`. It defines how an agent recovers
project state from durable files alone. It exists because conversation context
can be truncated, compacted, or lost.

## Priority of sources

When reconstructing state, use this order and stop at the first contradiction:

1. `docs/progress/DECISIONS.md` (authorization)
2. `docs/progress/GATES.md` (acceptance criteria)
3. `docs/progress/STATE.md` (current recorded state)
4. `docs/progress/CONTINUATION.md` (next action)
5. `AGENTS.md` and `.kilo/rules/*` (process)
6. Git objects (`git log`, `git status`, `git diff`)
7. The working tree

Git objects outrank the working tree, but neither outranks a recorded owner
decision.

## Required reading order

1. `AGENTS.md`
2. `.kilo/rules/00-governance.md`
3. `.kilo/rules/01-compiler-invariants.md`
4. `.kilo/rules/02-context-recovery.md` (this file)
5. `docs/progress/STATE.md`
6. `docs/progress/CONTINUATION.md`
7. `docs/progress/DECISIONS.md`
8. `docs/progress/GATES.md`

## Mandatory verification

After reading, run:

```sh
git branch --show-current
git log --oneline -5
git status --porcelain=v1 --untracked-files=all
git rev-parse HEAD
```

Then compare:

| Recorded | Check |
| --- | --- |
| Active milestone | Matches `docs/progress/STATE.md` and the latest accepted decision |
| Branch | Matches `STATE.md`; unexpected branch is a discrepancy |
| Last verified commit | Appears in `git log --oneline` |
| Files listed as modified | Match `git status` |
| Claimed verification | Has a command and a result recorded, not just a claim |

## Discrepancy handling

A discrepancy exists when any of these is true:

- Recorded branch differs from the actual branch.
- Recorded HEAD commit does not exist or is not an ancestor of the current HEAD.
- The working tree has changes not listed in `STATE.md`.
- `STATE.md` claims a verification that has no recorded command or result.
- A file described as absent exists, or a file described as present is absent.

On any discrepancy:

1. Do not modify tracked files.
2. Write a short discrepancy note in the final response.
3. Ask the owner which side is authoritative, unless a higher-authority record
   already answers the question.

## Interrupted-operation rule

Never assume an interrupted operation succeeded. Re-run the verification for the
suspected operation and compare output to the recorded expectation. If
re-running is not safe (for example, a partially applied migration), report
`BLOCKED` and stop.

## What not to do

- Do not reconstruct state from conversation memory when a durable file covers it.
- Do not "fix" the ledger to match an accidental change; fix the change or report it.
- Do not continue into an unauthorized milestone because the ledger looks empty.
- Do not delete a failing test or a failing gate record to unblock work.
