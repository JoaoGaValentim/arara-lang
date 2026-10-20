# Archive — D001 execution report (as delivered)

Status: **ARCHIVED VERBATIM**. Do not edit the quoted body. Corrections to the
record belong in `D001-AUDIT-R2.md` or in a new decision entry.

## Provenance

| Field | Value |
| --- | --- |
| Original deliverable | D001 execution report, section "Required deliverables" of Directive 001 |
| Originally delivered | 2026-10-20, chat response (never committed to the repository) |
| Recovered from | Kilo session store `~/.local/share/kilo/kilo.db`, session `ses_ea6c0cce2ffeR49TVy54TkC3YC`, title "Arara JVM language bootstrap and design", assistant message part of type `text`, 10097 characters |
| Session model | `deepseek-ai/deepseek-v4.1-flash-1017`, provider `robin`, variant `max` |
| Session window | 2026-10-20T09:17:56−03:00 to 2026-10-20T11:18:21−03:00 |
| Session usage | 955,087 input tokens; 127,146 output tokens |
| Repository state at delivery | branch `main`, HEAD `717fcc6`, clean tree |
| Recovered by | Directive 001-R2 audit, 2026-10-20 |
| Fidelity | Verbatim text of the assistant message. No wording, ordering, or table content was altered. |

## Verification limitations of the archived text

1. **Not independent evidence.** The report is the authoring agent's own
   self-assessment of work performed in the same session. Archiving it makes it
   durable; it does not make it independent.
2. **Environment claims are single-machine.** All toolchain and JVM observations
   were made on Corretto 21.0.12.1, macOS arm64, on one host. Behaviour on other
   JDK 21 vendors, architectures, or JVM flags was and remains `UNVERIFIED`.
3. **Three numeric claims in the archived text are wrong** and are corrected in
   `D001-AUDIT-R2.md` §9: "3,859 lines" (actual 3,818 at `c5f376a`), "127
   semantic rules" (actual 130 numbered sub-rules), and the recovery-run
   description, which is substantively accurate but under-specified.
4. **Two technical claims in the archived text were later found incomplete**:
   the local-slot statement ("slot 0 ... unused for zero-parameter methods") and
   the "not a concern" statement about unit names colliding with `java.lang`.
   Both are treated as findings F-10 and F-01 in `D001-AUDIT-R2.md`.
5. **Compaction and recovery.** The report's `UNVERIFIED` labels for automatic
   compaction were correct at delivery and remain correct; see
   `D001-AUDIT-R2.md` §8.

## Archived text

The body below is the exact delivered text.

---

D001 is complete and committed locally. Producing the required formal report.

## A. Environment and repository baseline

| Item | Value | Verification |
| --- | --- | --- |
| OS | macOS Darwin 27.0.0, aarch64 | `java -XshowSettings:properties -version` |
| JDK | Corretto 21.0.12.1, build 21+35-LTS | `java -version`, `javac -version` |
| Gradle | 9.8.1 (launcher JVM 21.0.12.1) | `gradle --version` |
| Git | 2.54.0 (Apple Git-157) | `git --version` |
| GitHub CLI | 2.102.0, authenticated as `JoaoGaValentim` | `gh auth status` |
| Kilo CLI | 7.8.12 | `kilo --version` |
| Python | 3.14.8 (auditing only) | `python3 --version` |
| Remote repo | `JoaoGaValentim/arara-lang`, **private**, created this session | `gh repo view` |
| Local branch | `main`, 7 commits, no upstream, nothing pushed | `git status`, `git log` |

The working directory was empty at start (no Git repo, no files). Toolchain probe: a disposable Gradle project outside the repository using Java 21 + ASM 9.11.1 (`asm`, `asm-util`, `asm-analysis`) + JUnit Jupiter 6.1.3 compiled and passed tests that generated a class, asserted classfile major version 65, loaded and invoked it, and passed `CheckClassAdapter.verify`. Result: `BUILD SUCCESSFUL in 6s`. This test is not in the repository.

## B. Files created or changed

24 tracked files, 3,859 lines. No `.java`, `.kt`, `.gradle`, `.xml`, `.py`, `src/`, `gradlew`, or `pom.xml`.

```
AGENTS.md  README.md  .gitignore
.kilo/kilo.jsonc
.kilo/rules/{00-governance,01-compiler-invariants,02-context-recovery}.md
.kilo/commands/{resume,checkpoint}.md
.kilo/agents/auditor.md
docs/{charter,architecture,roadmap}.md
docs/adr/{0001-jvm-target,0002-compiler-host,0003-model-limits}.md
docs/spec/{lexical.md,syntax.ebnf,semantics.md,conformance-plan.md}
docs/progress/{STATE,CONTINUATION,DECISIONS,GATES}.md
```

`.kilo/agent-manager.json` and `.kilo/.gitignore` are Kilo-managed and ignored, per `AGENTS.md` §4.

## C. Kilo configuration and effective model verification

`.kilo/kilo.jsonc` sets the instruction glob, compaction block, `code` agent variant `max`, and model limits for both `robin` and `hive`.

- `kilo config check` → `No config warnings.`
- `kilo debug config` → compaction `{"auto":true,"threshold_percent":70,"prune":true,"tail_turns":4,"preserve_recent_tokens":8000,"reserved":20000}`; three instruction origins resolve to this repo; `agent.code` = `hive/deepseek-ai/deepseek-v4.1-flash-1017`, variant `max`; model `limit` = `{context: 1048576, output: 1048576}`.
- `kilo debug agent code` → provider `hive`, model `deepseek-ai/deepseek-v4.1-flash-1017`, variant `max`.
- `kilo run "…PING"` in a scratch project using this config returned `PING`.

Reasoning: `max` is the highest of the four declared variants (`low/medium/high/max`). Provider probe with `reasoning_effort:"max"` returned HTTP 200 with non-empty `reasoning_content` and `reasoning_tokens: 12`. **VERIFIED** as accepted; that `max` is genuinely the model's ceiling is the provider's declaration, not something independently measurable.

Model limits were measured, not invented: `max_tokens` 1048544 accepted, 1048576 and 999999999 rejected as "reduce the length of the messages or completion". The boundary is `prompt + max_tokens ≤ 1048576`. `output` is set to the context value because no lower output cap was observable.

## D. Context protection and recovery status

- **Layer A (compaction):** configured and resolved. Recognition of `threshold_percent` is `VERIFIED` indirectly — the installed build retains it while dropping unknown keys (`nonsense_key`, `bogus`, `keep`, `buffer`) and near-miss spellings (`threshold_perc`, `thresholdPercent`, `percent`, `threshold`). The published schema does not list it. Its **effect on compaction is `UNVERIFIED`**: no compaction event fired in any test session, including one with a ~100 KB attachment, and DEBUG logs contain no compaction service messages.
- **Layer B (ledger):** `STATE.md`, `CONTINUATION.md`, `DECISIONS.md`, `GATES.md` all populated with facts.
- **Layer C (checkpoints):** 7 small commits, each scoped to one work unit; `.kilo/commands/checkpoint.md` defines the protocol.
- **Recovery test:** two separate `kilo run --agent plan` processes with read-only prompts. Run 1 (pre-commit) correctly reported `GIT_HEAD=none`, 24 untracked files, and flagged the ledger discrepancy. Run 2 (post-commit) correctly reconstructed milestone, scope, current task, next action, forbidden actions, branch `main`, HEAD `c5f376a`, and a clean tree. **VERIFIED** with the limitation that both used the same client and model.

## E. Architecture decisions and alternatives

`docs/architecture.md` defines eight stages with explicit input/output contracts: normalize → lex → parse → resolve → type → lower → emit → verify. Spans are half-open offsets in Unicode code points; diagnostics carry stable `ARA-<STAGE>-<NNNN>` codes with deterministic ordering; no stage mutates an earlier stage's tree.

Alternatives rejected: parser generators (span/recovery control), tree-walking interpreter (duplicate semantics), AST-to-bytecode without IR (untestable control flow), Java-source transpilation (explicitly prohibited), Kotlin fork (scope), `invokedynamic` (unneeded). Backend uses ASM 9.11.1 with a closed `getCommonSuperClass` table and two mandatory verification layers.

ADRs 0001–0003 are **PROPOSED**, not accepted.

## F. Language specification findings

- 63 lexical rules, 27 grammar productions, 127 semantic rules, 67 diagnostic codes, all defined exactly once with no undefined or duplicate codes.
- Grammar is **LL(1)**, verified mechanically by FIRST/FOLLOW analysis. The original formulation was LL(2) at separator runs; it was rewritten with `FunctionTail`/`StatementTail` and the reason is documented in §6.4.
- Substantive design decisions that need owner attention: `Int` arithmetic is 32-bit two's-complement with Java overflow, truncating division, and `ArithmeticException` on zero; **all shadowing is prohibited**; **strings support `==` but not `+`**; **multiple compilation units share one function namespace**; **`if` is a statement, never an expression**; **every local requires an explicit type and initializer**.
- Two internal contradictions found by audit were real and are fixed: a leading `SEMI` accepted by the grammar but rejected by the lexical rules, and two incompatible name-resolution rules for a local sharing a function's name.

## G. Conformance-plan status

`conformance-plan.md` defines fixture conventions, a lexical matrix, a per-production syntax matrix, a semantic matrix keyed to rule numbers, a JVM matrix, a driver/diagnostics section, and a three-way traceability requirement (rule → test ID → implementation location). Evaluation order is testable through user functions that print. **No test exists yet**; this is an obligation list, not evidence.

## H. Verification commands and actual results

| Command | Result |
| --- | --- |
| `git status --porcelain=v1 -uall` | empty (clean) |
| `git log --oneline` | 7 commits |
| `git ls-files` | 24 files, extensions `md`/`ebnf`/`jsonc`/`gitignore` only |
| `kilo config check` | `No config warnings.` |
| `kilo debug config` | fields resolved as written |
| `kilo debug agent code` | hive, `deepseek-v4.1-flash-1017`, `max` |
| `kilo models hive --verbose` | `limit {'context': 1048576, 'output': 1048576}` |
| `gradle --no-daemon test` (external probe) | `BUILD SUCCESSFUL` |
| Diagnostic code cross-check | 67 used, 67 defined, 0 duplicate |
| Grammar structure check | 27 productions, 0 undefined, 0 unused, 0 duplicate |
| FIRST/FOLLOW analysis | no selection conflict |
| Link audit | all relative links and backticked paths resolve |
| Whitespace audit | no trailing whitespace |
| Fresh-process recovery ×2 | reconstructed state correctly; run 1 caught the missing commit |

## I. Git evidence

- Branch `main`; no upstream; `origin` → `JoaoGaValentim/arara-lang`.
- HEAD `717fcc6`; bootstrap `c5f376a`; clean working tree.
- **No push occurred.** The remote repository exists and is empty. `AGENTS.md` §4 forbids development on `main`, so the ledger records D001-03: bootstrap committed locally on `main` and **not** pushed; D002 must branch to `develop` first.
- Explicit limitation: the bootstrap exists only on this machine.

## J. Open questions requiring owner decisions

1. Repository visibility — private was chosen; confirm or change.
2. Authorize pushing the bootstrap and creating `develop`, or keep it local.
3. Accept, reject, or amend `docs/spec/*` as the D001 contract.
4. Division by zero: runtime `ArithmeticException` (drafted) vs compile-time rejection.
5. Shadowing prohibition — stricter than Kotlin; confirm.
6. Multi-unit programs with one shared function namespace; confirm or restrict to one unit.
7. Reserved-word list (28 words).
8. Source extension `.ara`.
9. License — none present; required before any publication.
10. Whether the D001 execution report should be archived in the repository.

## K. D001 acceptance recommendation

**PASS — READY FOR OWNER REVIEW**

All 15 D001 gates carry recorded evidence. Two gates were honestly `BLOCKED` before the bootstrap commit and became `PASS` only after it. One gate (compaction *firing*) remains `UNVERIFIED` and is documented as such rather than asserted. No implementation exists, and none of the prohibited artifacts is present.

This is a self-assessment. **D001 is not accepted**; only the owner can accept it, and acceptance must be recorded in `docs/progress/DECISIONS.md`.

## L. Next proposed directive

**Directive 002 — Lexer and Parser**, scoped to: UTF-8 decoding and newline normalization; the token model with spans; the lexer with all `ARA-LEX-*` diagnostics; the recursive-descent parser for the frozen grammar with the specified recovery behavior; the AST and its stable dump format; `lex` and `parse` CLI subcommands; golden, negative, and recovery tests; and an automated FIRST/FOLLOW regression check. No name resolution, typing, IR, or bytecode. Entry requires recorded acceptance of D001 plus the toolchain versions pinned in a build file.
