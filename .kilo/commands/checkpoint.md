---
description: Validate repository state, update the recovery ledger, and create a non-destructive checkpoint commit
---

# Checkpoint

Persist an accurate, resumable project state. A checkpoint is **not** milestone
acceptance and must never be used to hide a failing gate.

## 1. Preconditions

Read `AGENTS.md`, `.kilo/rules/00-governance.md`, and
`docs/progress/STATE.md` before starting. Confirm which milestone is active and
that the work being checkpointed is inside its authorized scope.

## 2. Inspect before touching anything

```sh
git branch --show-current
git status --porcelain=v1 --untracked-files=all
git diff --stat
git diff --cached --stat
git log --oneline -5
```

Classify every entry:

- **Project work** — belongs to the authorized task.
- **Unrelated user change** — preserve, do not stage, list in `STATE.md`.
- **Generated artifact** — must be ignored, not committed.
- **Secret or credential** — stop immediately, do not commit, report.

## 3. Validate

Run the validations that apply to the changed files. For documentation and
configuration phases this means:

```sh
# Every referenced file exists
for f in $(grep -rhoE '(docs|\.kilo)/[A-Za-z0-9._/-]+\.(md|jsonc?|ebnf)' AGENTS.md README.md docs .kilo 2>/dev/null | sort -u); do
  test -e "$f" || echo "MISSING: $f"
done

# JSON/JSONC files parse
python3 - <<'PY'
import json, glob, re
for p in glob.glob('.kilo/*.jsonc') + glob.glob('.kilo/*.json'):
    raw = open(p).read()
    raw = re.sub(r'^\s*//.*$', '', raw, flags=re.M)
    try:
        json.loads(raw)
        print('OK  ', p)
    except Exception as e:
        print('FAIL', p, e)
PY

# Markdown internal links resolve
grep -rnE '\]\([^)#][^)]*\)' docs README.md 2>/dev/null | sed -E 's/.*\]\(([^)#]+).*/\1/' | sort -u
```

Record the actual output. If a check fails, fix it before committing; if the fix
is out of scope, do not commit and report.

## 4. Update the ledger

Update these files with facts only:

- `docs/progress/STATE.md` — current task, branch, HEAD, modified files, verified
  tests, known failures, blockers, next action, forbidden actions.
- `docs/progress/CONTINUATION.md` — the compact next-action document.
- `docs/progress/DECISIONS.md` — only when the owner has recorded a decision.

Never invent a verification result. Never mark a gate PASS in this step.

## 5. Commit

```sh
git add <explicit file list>
git commit -m "<type>(<scope>): <short imperative summary>"
```

Rules:

- Stage explicit paths. Never `git add -A` or `git add .` in a tree that may hold
  unrelated changes.
- Use `checkpoint:` as the type for ledger/state commits that contain no semantic
  change, or `docs:` / `chore:` for content changes.
- Never amend or force-push. Never skip hooks.
- Never commit `.kilo/agent-manager.json`, `.env*`, or build output.

## 6. Report

Output:

```text
CHECKPOINT COMMIT:
BRANCH:
CHANGED PATHS:
VALIDATION RESULTS:
LEDGER UPDATED: yes|no
OPEN FAILURES:
NEXT SAFE ACTION:
```
