# Project State

Machine-readable recovery ledger. Update after every completed work unit. Facts
only; no intentions, no forecasts.

```yaml
active_directive: D001-R1 (language identity); milestone D001
active_milestone: D001 — Foundation (documentation and configuration only)
milestone_authorization: D001-01 and D001-06 (docs/progress/DECISIONS.md)
current_task: D001-R1 documentation identity audit complete; awaiting owner review
current_subtask: none
last_completed_unit: D001-R1 identity record, naming audit, and specification corrections
branch: docs/d001-r1-arara-identity
last_verified_commit: ce3c67d
commit_convention: this ledger is edited before each commit, so last_verified_commit is always the commit immediately preceding the one containing this line; verify with `git log --oneline -3`
remote: origin https://github.com/JoaoGaValentim/arara-lang.git (private, empty, no push)
working_tree: clean after the D001-R1 content commit; no untracked or unrelated changes
```

## D001-R1 changed paths

```text
.kilo/rules/01-compiler-invariants.md
README.md
docs/adr/0002-compiler-host.md
docs/architecture.md
docs/charter.md
docs/progress/CONTINUATION.md
docs/progress/DECISIONS.md
docs/progress/GATES.md
docs/progress/STATE.md
docs/spec/conformance-plan.md
docs/spec/semantics.md
docs/spec/syntax.ebnf
```

## D001-R1 — Language identity (2026-10-20)

Directive 001-R1 fixes the official name and requires a naming and audit pass.
Scope actually executed: documentation only, on `docs/d001-r1-arara-identity`,
branched from `717fcc6`.

- `D001-06` recorded in `docs/progress/DECISIONS.md`: name **Arara**,
  repository `JoaoGaValentim/arara-lang`, extension `.ara` **PROPOSED**,
  target JVM classfile 65, Kotlin as surface-syntax inspiration only,
  implementation and semantics independently specified.
- Naming audit: 23 tracked documentation and configuration files scanned; no
  alternate project name exists in any tracked document. Every language
  reference already used "Arara". The gaps were the missing decision entry, the
  missing identity statement in `README.md` and `docs/charter.md`, and 12 code
  fences labelled `kotlin`, which implied a Kotlin source claim. All 12 now use
  `arara`.
- Extension status: `.ara` is marked PROPOSED in `README.md`, `docs/charter.md`,
  `docs/spec/semantics.md` §1.1, `docs/spec/conformance-plan.md`, and
  `docs/architecture.md` §6. `OQ-8` stays open.
- Diagnostic namespace: `ARA-<STAGE>-<NNNN>` is retained unchanged, with `ARA` as
  the abbreviation of the approved name. This is stated in `README.md`,
  `docs/charter.md`, and `D001-06`; no diagnostic code was renamed.
- Unit-name rule: `semantics.md` §1.2 and `charter.md` §6 now state explicitly
  that Java *restricted identifiers* (`var`, `yield`, `record`, `sealed`,
  `permits`, module words) remain legal because they are not keywords, and
  record `OQ-10` so the owner can forbid them if desired. `_` is rejected
  because it is a Java keyword.
- No file was renamed, no Git history was rewritten, and no architecture,
  specification scope, or milestone boundary changed.

### D001 report audit (scope item 5)

The D001 report was delivered in the previous session and is **not** a committed
artifact (limitation 6), so only its repository-visible claims can be audited.
The audit compared the D001 documents against probes run in this session and
produced three kinds of unresolved items:

**JVM compatibility.** Corrected in this unit:
- ADR 0002 claimed Java 21 "gives direct access to `java.lang.classfile`-adjacent
  tooling". The API does not exist on JDK 21: a probe on Corretto 21.0.12.1 threw
  `ClassNotFoundException` for `java.lang.classfile.ClassFile` (`java.lang.classfile`
  was finalized in JDK 24, not 21). ADR 0002 now records the probe and keeps ASM
  as the bytecode API.
- `architecture.md` and `semantics.md` §11.11 claimed no generated class stores a
  reference in a local. That is false for `String` locals created by `val s:
  String = ...`, which the initial subset permits. Both now state that a `String`
  local is legal and only references to generated unit classes are excluded.
- `semantics.md` §11.3 (a method may share its class's name) and §11.10 (a class
  with no `<init>`) were **verified** by probe rather than asserted: a `javac`
  class `Foo` with `static void Foo()` and a hand-built class with no
  constructor both run under `-Xverify:all`.
- Unresolved and left to the owner: the initial subset is validated only on
  Corretto 21.0.12.1 on macOS arm64. Behavior on other JDK 21 vendors, other
  architectures, or a JVM invoked with `-Xverify:none` is `UNVERIFIED`. The
  specification requires `-Xverify:all` for the gate, which compensates for the
  default `BytecodeVerificationLocal=false`, but a user running `java` directly
  relies on the default verifier.

**Specification consistency.** Corrected in this unit:
- `conformance-plan.md` assigned `ARA-PARSE-0111` to a value-called-as-function
  case that `semantics.md` §5.10 marks unreachable, and later assigned `0116` to
  a missing `(` in a function declaration while `0111` had no trigger. `0111`
  now owns the declaration case, `0116` owns only `if`, and the conformance rows
  match. `D002-G5` now asserts `ARA-LEX-0102` absent instead of naming a second
  unreachable code.
- `syntax.ebnf` §7 declared every parse diagnostic `ERROR` while `0199` is a
  `NOTE`; the preamble now says so. `semantics.md` §12.3 called `0199` reserved
  when it is produced; corrected.
- `semantics.md` §2.0 cited "§2.1" for the surrogate rule, which is actually in
  `lexical.md` §8; the citation is now explicit.
- `semantics.md` §1.4a claimed an empty unit "is reachable only if other units
  call nothing from it", which is vacuous for a unit with no functions;
  rewritten.
- `charter.md` said classes and objects "cover" the enum category while classes
  and objects are themselves deferred; reworded to "a future class or object
  feature would cover the category".
- `conformance-plan.md` row "leading and trailing separators" contradicted
  `lexical.md` §10 rule 58 (leading `SEMI` is an error) and cited the wrong code
  for a body-only file (`0106` instead of `0100`); corrected.
- Unresolved: `docs/roadmap.md` and `docs/architecture.md` still describe the
  full D001–D006 plan, not a frozen contract; they are explicitly descriptive in
  `AGENTS.md` §1 and do not override the specification.

**Context recovery limitations.** Recorded, not resolved:
- Recovery was validated twice with the same Kilo client and model. Recovery
  under a different client, model, or machine remains `UNVERIFIED`, and the D001
  report itself was never committed, so a fresh session cannot read the report
  text, only `STATE.md` and `GATES.md`.
- The D001 gate citations pointed at `STATE.md` §Audit findings and §Independent
  recovery simulation. The actual headings are §Internal audit resolution and
  §Independent recovery validation. `GATES.md` citations are corrected; the D001
  report text is not a repository record and was not edited.
- `CONTINUATION.md` recorded branch `main` and told the next session to expect
  `main`. That was accurate until this branch was created and would have produced
  a false discrepancy in a fresh session. Both `CONTINUATION.md` and `GATES.md`
  now name the actual branch.
- Unresolved: automatic compaction has never been observed firing, so recovery
  after a real compaction event is `UNVERIFIED`. See limitation 2.

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
| OS | macOS 27.0.1, Darwin 27.0.0 arm64 | `sw_vers`, `uname -a` |
| JDK | Corretto 21.0.12.1, build 21.0.12.1+12-LTS | `java -version` |
| javac | 21.0.12.1 | `javac -version` |
| Gradle | 9.8.1, launcher JVM 21.0.12.1 | `gradle --version` |
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
5. **Independent recovery test.** Two separate `kilo run` processes were started
   in this repository with read-only recovery prompts. Their outputs are recorded
   below. They are real fresh-context processes, but they use the same installed
   client and model, so they do not prove recovery under a different client.
6. **No report file in the repository.** The D001 execution report is delivered in
   the response, not committed, because it contains environment and session
   details that are not project records. `GATES.md` therefore cites `STATE.md`
   sections rather than report sections. If the owner wants the report archived,
   that requires a decision.

## Independent recovery validation

Two separate `kilo run --agent plan` processes were started in this repository
with read-only instructions and no access to this session's transcript.

Run 1 (before the bootstrap commit) correctly reported:
`GIT_HEAD=none`, 24 untracked files, and a discrepancy between the ledger and
Git. That discrepancy was accurate and was fixed by committing.

Run 2 (after commit `c5f376a`) reported:

```text
ACTIVE_MILESTONE=D001 — Foundation (documentation and configuration only)
GIT_BRANCH=main
GIT_HEAD=c5f376af28ea76c9f802e258331185aab666cde4
WORKTREE_CLEAN=true
DISCREPANCIES=Minor stale ledger only: STATE.md still records "zero commits..."
```

Assessments:

- Directive, authorized scope, current task, next safe action, and forbidden
  actions were reconstructed correctly from files alone. `VERIFIED`.
- Git state was read correctly. `VERIFIED`.
- The stale-ledger discrepancy in run 2 is the expected consequence of running
  recovery before this ledger update; this update closes it. `VERIFIED` as a
  self-detection capability.
- Limitation: both runs used the same installed Kilo client and model. Recovery
  under a different client, model, or machine is `UNVERIFIED`.

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
| Diagnostic code uniqueness | ad-hoc Python cross-check | PASS — 67 unique codes, 0 duplicates, 0 undefined |
| Trailing whitespace | `grep -rnE ' +$'` over all files | PASS — no matches |
| Relative links | link audit over all `.md` | PASS — all resolve |
| Backticked paths | path audit over all `.md` | PASS — all exist |
| Fresh-process recovery | `kilo run --agent plan`, twice, read-only | PASS with limitation — §Independent recovery validation |

### D001-R1 verification runs (2026-10-20)

| Check | Command | Result |
| --- | --- | --- |
| Naming audit | `grep -rniE` over `*.md`, `*.ebnf`, `*.jsonc` for Arara, `.ara`, Kotlin, and alternate names | PASS — 0 alternate project names; 12 `kotlin` fences found and relabelled `arara` |
| Diagnostic uniqueness after edits | ad-hoc Python cross-check | PASS — 67 unique codes, 0 duplicates, 0 references without a table definition |
| Grammar LL(1) after edits | external `ll1_check.py docs/spec/syntax.ebnf` | PASS — `no selection conflicts` |
| Config parse | `kilo config check` | PASS — `No config warnings.` |
| Agent resolution | `kilo debug agent code` | PASS — `hive`, variant `max` |
| Model limits | `kilo models hive --verbose` | PASS — context 1048576, output 1048576 |
| JSON/JSONC parse | `python3` parse of `.kilo/*.jsonc` | PASS |
| Referenced paths | path audit over `AGENTS.md`, `README.md`, `docs`, `.kilo` | PASS — none missing |
| Relative links | Markdown link audit | PASS — none broken |
| Trailing whitespace | `grep -rnE ' +$'` | PASS — no matches |
| JDK class-file API probe | `Class.forName("java.lang.classfile.ClassFile")` on Corretto 21.0.12.1 | `ClassNotFoundException` — ASM remains the bytecode API |
| Restricted class-name probe | hand-built classes named `record`, `var`, `yield`, `when`, `module` loaded under `-Xverify:all` | PASS — the JVM accepts them, so the unit-name rule depends on the Java **keyword** list as specified |
| Unit-name shadowing probe | hand-built units named `java`, `String`, `Object`, `record`, `var`, `when` that also call `java/lang/String.length()` | PASS — `java/lang/String` still resolves with a unit class of the same simple name; unnamed-package classes cannot shadow a package |
| Method/class name collision probe | `javac` class `Foo` with `static void Foo()` run under `-Xverify:all` | PASS — legal, matching `semantics.md` §11.3 |
| Constructor-free class probe | hand-built class with no `<init>` run under `-Xverify:all` | PASS — legal, matching `semantics.md` §11.10 |
| Verifier strength probe | corrupted `areturn` class under default and `-Xverify:all` | PASS both — `VerifyError`; default flags report `BytecodeVerificationLocal=false`, `Remote=true` |
| Toolchain smoke test | external Gradle project, `gradle --no-daemon test` | PASS — `BUILD SUCCESSFUL`, 2 tasks up to date |

## Internal audit resolution

An independent read-only audit was run against the D001 documents. Findings and
dispositions:

| Finding | Resolution |
| --- | --- |
| Grammar accepted a leading `SEMI` while `lexical.md` forbade it | Grammar rewritten with `FunctionTail`/`StatementTail`; both documents now agree that a leading or doubled `SEMI` is `ARA-PARSE-0100` |
| Name resolution described two incompatible rules for a local named like a function | `semantics.md` §5.3–§5.10 rewritten as two explicit namespaces; `ARA-NAME-0111` marked unreachable |
| `alwaysReturns` ignored unreachability | Replaced with a deterministic one-pass block algorithm plus three worked consequences |
| Two worked examples had headings that contradicted their code | `semantics.md` §13.2 and §13.5 corrected; §13.5a added |
| `architecture.md` claimed a single generated reference type | Corrected to every generated unit class with `java/lang/Object` as the only common supertype |
| CLI contract pointed at a gate that did not exist | CLI contract made explicit per milestone; `D002-G11` and `D004-G9` added |
| `charter.md` referenced an undefined "`Arara` digit pattern" | Replaced with the regex plus the Java reserved-word rule, reported as `ARA-SRC-0004` |
| Toolchain and provider probes lacked reproducible evidence | ADR 0002 gained the probe project description and command output; ADR 0003 gained the request shape and full probe table |
| A committed helper script violated the "documentation only" rule | Removed from the repository; analyses re-run externally and results recorded as data |
| Conformance plan claimed output-observable evaluation order was untestable | Corrected: user functions that print make it observable; a fixture is specified |
| Conformance plan assigned two codes to one defect; missing `(` had no code | Corrected; `ARA-PARSE-0115`, `0116`, `0117` added |
| Missing obligations for driver, ordering, suppression, frames, determinism | `conformance-plan.md` §6a added |
| Reserved-word list was not traceable to charter categories | `lexical.md` §31–32 now require a charter category per word |
| `GATES.md` reported PASS before the bootstrap commit existed | Rewritten to `BLOCKED` at the time, then to `PASS` only after the commit and after fresh-process recovery |

## Blocking decisions

None block D001 completion. Ten open questions are recorded in
`docs/progress/DECISIONS.md` under "Open decisions requested from the owner".
They affect D002 scope, not D001 correctness.

## Next executable action

Owner review of the D001 report and the specification drafts. No agent action is
authorized until the owner records a decision. If the owner accepts D001, the next
action is to record acceptance in `DECISIONS.md` and issue Directive 002. If the
owner rules on `OQ-8` (extension) or `OQ-10` (restricted unit names), record the
decision first and change only the corresponding references.
