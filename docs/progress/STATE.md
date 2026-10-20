# Project State

Machine-readable recovery ledger. Update after every completed work unit. Facts
only; no intentions, no forecasts.

```yaml
active_directive: D001
active_milestone: D001 — Foundation (documentation and configuration only)
milestone_authorization: D001-01 (docs/progress/DECISIONS.md)
current_task: D001 execution complete; awaiting owner review
current_subtask: none
last_completed_unit: D001 bootstrap authored and independently audited; bootstrap commit pending
branch: main
last_verified_commit: none (zero commits at time of writing; updated after the bootstrap commit)
remote: origin https://github.com/JoaoGaValentim/arara-lang.git (private, no push)
working_tree: all D001 files untracked until the bootstrap commit; see git status
```

## Authorized scope

D001 only: governance, Kilo configuration, recovery mechanism, architecture,
roadmap, ADRs, language specification drafts, conformance plan, and a formal
report. Local commits are permitted. Nothing has been pushed.

## Forbidden now

- Any lexer, parser, type checker, IR, backend, or bytecode code.
- `build.gradle(.kts)`, `settings.gradle(.kts)`, `pom.xml`, `gradlew`, or a
  compiler source tree.
- Tests or CI configuration.
- Starting D002.
- Pushing to any remote, merging, releasing, tagging, or publishing.
- Weakening `AGENTS.md`, `.kilo/rules/*`, or `docs/progress/GATES.md`.
- Introducing any feature from `docs/charter.md` §4.

## Files created in D001

| Path | Purpose | Status |
| --- | --- | --- |
| `AGENTS.md` | Primary engineering authority | complete |
| `README.md` | Project overview and navigation | complete |
| `.gitignore` | Ignore local state, build output, secrets | complete |
| `.kilo/kilo.jsonc` | Compaction, instruction globs, agent variant, model limits | validated |
| `.kilo/rules/00-governance.md` | Authority, change classes, conflict protocol | complete |
| `.kilo/rules/01-compiler-invariants.md` | Stage, determinism, diagnostics, JVM invariants | complete |
| `.kilo/rules/02-context-recovery.md` | Durable-state recovery rules | complete |
| `.kilo/commands/resume.md` | Read-only recovery procedure | complete |
| `.kilo/commands/checkpoint.md` | Validate and persist state; commit protocol | complete |
| `.kilo/agents/auditor.md` | Read-only independent auditor subagent | complete |
| `docs/charter.md` | Purpose, scope, non-goals, program model | draft |
| `docs/architecture.md` | Pipeline, stage contracts, alternatives | draft |
| `docs/roadmap.md` | D001–D006 boundaries and exit gates | draft |
| `docs/adr/0001-jvm-target.md` | JVM as sole target | proposed |
| `docs/adr/0002-compiler-host.md` | Java 21, Gradle, ASM, JUnit | proposed |
| `docs/adr/0003-model-limits.md` | Measured model context/output limits | proposed |
| `docs/spec/lexical.md` | Lexical contract | draft |
| `docs/spec/syntax.ebnf` | Grammar and parser analysis | draft |
| `docs/spec/semantics.md` | Static, dynamic, and JVM contract | draft |
| `docs/spec/conformance-plan.md` | Test obligations | draft |
| `docs/progress/STATE.md` | This file | current |
| `docs/progress/CONTINUATION.md` | Next-action handoff | current |
| `docs/progress/DECISIONS.md` | Owner decisions and open questions | current |
| `docs/progress/GATES.md` | Milestone gates | current |

## Environment baseline (verified this session)

| Item | Value | Command |
| --- | --- | --- |
| OS | Darwin 27.0.0 arm64 (macOS) | `java -XshowSettings:properties -version` |
| JDK | Corretto 21.0.12.1, build 21+35-LTS | `java -version` |
| javac | 21.0.12.1 | `javac -version` |
| Gradle | 9.8.1 | `gradle --version` |
| Git | 2.54.0 (Apple Git-157) | `git --version` |
| GitHub CLI | 2.102.0, authenticated as `JoaoGaValentim` | `gh --version`, `gh auth status` |
| Kilo CLI | 7.8.12 | `kilo --version` |
| Python | 3.14.8 | `python3 --version` |
| ASM (published) | 9.11.1 | Maven Central `maven-metadata.xml` |
| JUnit Jupiter (published) | 6.1.3 | Maven Central `maven-metadata.xml` |

Toolchain smoke test: a throwaway Gradle project outside the repository using
Java 21 + ASM 9.11.1 (with `asm-util` and `asm-analysis`) + JUnit Jupiter 6.1.3
compiled and passed tests that generated a class, asserted classfile major
version 65, loaded and invoked it, and passed `CheckClassAdapter.verify`. Result:
`BUILD SUCCESSFUL` (two runs). This test is **not** part of the repository and is
not D002 implementation.

## Kilo configuration evidence

- `kilo config check` → `No config warnings.`
- `kilo debug config` resolves:
  - `instructions: [".kilo/rules/*.md"]` with three instruction origins in this
    repository.
  - `compaction: {"auto":true,"threshold_percent":70,"prune":true,"tail_turns":4,"preserve_recent_tokens":8000,"reserved":20000}`.
  - `agent.code: {"model":"hive/deepseek-ai/deepseek-v4.1-flash-1017","variant":"max", ...}`.
  - provider model limits `{"context":1048576,"output":1048576}`.
- `kilo debug agent code` → provider `hive`, model
  `deepseek-ai/deepseek-v4.1-flash-1017`, variant `max`.
- `kilo run "Reply with the single word: PING"` in a scratch project using this
  config returned `PING`, confirming the configuration does not break execution.
- Provider probe: `reasoning_effort: "max"` accepted (HTTP 200, `reasoning_content`
  present); `max_tokens` 1048544 accepted; 1048576 rejected; 999999999 rejected.

## Known limitations

1. **Compaction configuration.** `.kilo/kilo.jsonc` sets `auto`,
   `threshold_percent`, `prune`, `tail_turns`, `preserve_recent_tokens`, and
   `reserved`. Evidence:
   - The schema fetched from `https://app.kilo.ai/config.json` declares `auto`,
     `prune`, `tail_turns`, `preserve_recent_tokens`, `reserved`, `keep`, and
     `buffer`, but **not** `threshold_percent`.
   - The installed build (7.8.12) drops unknown keys: `nonsense_key`, `bogus`,
     `keep`, and `buffer` are all absent from `kilo debug config`.
   - The installed build **retains** `threshold_percent`, and also drops
     near-miss spellings (`threshold_perc`, `thresholdPercent`, `percent`,
     `threshold`). Therefore the installed build recognizes the field even though
     the published schema does not describe it.
   - Status: recognition `VERIFIED`; effect on compaction `UNVERIFIED`.
2. **Compaction firing.** No compaction event was observed. A session with a
   ~100 KB attached file completed without a compaction log line, and DEBUG logs
   contain no compaction service messages at all. Status: `UNVERIFIED`.
3. **Model limits** are measured provider behavior, not a published contract. See
   ADR 0003.
4. **Remaining context** is not exposed by a documented provider endpoint; the
   boundary was established by binary probing of `max_tokens`.
5. **Independent recovery test.** A separate `kilo run` process was started in
   this repository with a read-only recovery prompt. Its output is recorded in
   the D001 report §D. It is a real fresh-context process, but it uses the same
   installed client and model, so it does not prove recovery under a different
   client.

## Verification runs (this session)

| Check | Command | Result |
| --- | --- | --- |
| Kilo config parse | `kilo config check` | PASS — no warnings |
| Kilo effective config | `kilo debug config` | PASS — fields resolved |
| Agent model/variant | `kilo debug agent code` | PASS — hive, variant max |
| Model limits | `kilo models hive --verbose` | PASS — 1048576 / 1048576 |
| End-to-end prompt | `kilo run ...` | PASS — returned `PING` |
| Grammar structural lint | ad-hoc audit script (not committed) | PASS — 27 productions, 0 undefined, 0 unused |
| FIRST/FOLLOW analysis | ad-hoc audit script (not committed) | PASS — no LL(1) selection conflict |
| Diagnostic code uniqueness | ad-hoc Python cross-check | PASS — 62 codes, 0 duplicates, 0 undefined |
| Trailing whitespace | `grep -rnE ' +$'` over all files | PASS — no matches |
| Relative links | link audit over all `.md` | PASS — all resolve |
| Backticked paths | path audit over all `.md` | PASS — all exist |
| Diagnostic codes | cross-check over lexical/syntax/semantics | PASS — 67 used, 67 defined, 0 duplicates |
| Grammar structure | ad-hoc parse of the EBNF block | PASS — 27 productions, 0 undefined, 0 unused, 0 duplicate |
| FIRST/FOLLOW | ad-hoc LL(1) analysis | PASS — no selection conflict |
| Fresh-process recovery | `kilo run` read-only recovery prompt | PASS with limitation — see §Known limitations 5 |

## Blocking decisions

None block D001 completion. Nine open questions are recorded in
`docs/progress/DECISIONS.md` under "Open decisions requested from the owner".
They affect D002 scope, not D001 correctness.

## Next executable action

Owner review of the D001 report and the specification drafts. No agent action is
authorized until the owner records a decision. If the owner accepts D001, the next
action is to record acceptance in `DECISIONS.md` and issue Directive 002.
