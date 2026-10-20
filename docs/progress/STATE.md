# Project State

Machine-readable recovery ledger. Update after every completed work unit. Facts
only; no intentions, no forecasts.

```yaml
active_directive: D001-R3 (contract closure, gate integrity, final owner review); milestone D001
active_milestone: D001 — Foundation (documentation and configuration only)
milestone_authorization: D001-01 and D001-06 (docs/progress/DECISIONS.md)
process_authorization: D001-07 (English-only communication, closed gate status vocabulary)
current_task: D001-R3 complete — all 15 gates PASS, verdict CONDITIONAL pending owner ratification of 5 decisions and 1 criterion amendment
current_subtask: none
last_completed_unit: D001-R3 — gate reassessment, Java 21 naming policy, diagnostic traceability closure, grammar revalidation, owner decision package
branch: docs/d001-r1-arara-identity
last_verified_commit: e408966
audit_commit: 49ad593 (D001-R3); previous audit 15f25b2 (D001-R2)
commit_convention: this ledger is edited before each commit, so last_verified_commit is always the commit immediately preceding the one containing this line; verify with `git log --oneline -3`
remote: origin https://github.com/JoaoGaValentim/arara-lang.git (private, empty, no push)
working_tree: clean after 49ad593; nothing pushed
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

## D001-R2 — Final contract audit (2026-10-20)

Directive 001-R2 required an internal final review of D001 and D001-R1 before the
owner decides whether the specification is suitable for implementation. It was a
documentation audit only.

### Deliverables

| Artifact | Purpose |
| --- | --- |
| `docs/progress/audit/D001-AUDIT-R2.md` | Audit report, findings F-01…F-10, verdict, owner decision matrix |
| `docs/progress/audit/REPRODUCIBILITY.md` | Command and observed result for every `VERIFIED` claim |
| `docs/progress/audit/D001-EXECUTION-REPORT-ARCHIVE.md` | D001 report archived verbatim with provenance and limitations |
| `docs/progress/audit/D001-R1-REPORT-ARCHIVE.md` | D001-R1 report archived verbatim with provenance and limitations |

### Audit commit

The D001-R2 documentation and configuration corrections are committed locally on
`docs/d001-r1-arara-identity` as **`15f25b2`** (parent `82a7afa`). This ledger is
updated by the checkpoint commit that immediately follows it, so the SHA of the
ledger's own commit is one step later and must be read from
`git log --oneline -2`. Nothing was pushed; `git ls-remote origin` returns no refs.

### Verdict

`CONDITIONAL`. As a gate status this is not a declared value: eleven D001 gates
are `PASS`, and `D001-G9`, `D001-G12`, and `D001-G13` are `BLOCKED` — their
evidence does not meet their criterion as written, and the criterion cannot be
settled by the gate's own evidence. D001 remains **pending owner ratification**.
The milestone verdict `CONDITIONAL` lives in its own vocabulary, separate from the
closed gate status set.

### Post-audit vocabulary correction

The first pass of this audit recorded the three unsettled gates as
`CONDITIONAL`, a value it had added to `GATES.md` itself. Owner directive
D001-07 then closed the gate vocabulary, so the value was withdrawn and the three
gates re-recorded as `BLOCKED`, which is the declared value for a gate that cannot
be closed on its own evidence. The milestone verdict `CONDITIONAL` is unchanged
and now sits explicitly in the verdict vocabulary. `AGENTS.md` §12 and
`.kilo/rules/00-governance.md` state the separation so the mistake is not
repeated.

### What the audit confirmed independently

- Baseline `82a7afa` on `docs/d001-r1-arara-identity`, no upstream, private empty
  remote, `git fsck` clean, 0 tags.
- D001-R1 changed 12 files, +332/−95. The `syntax.ebnf` production block is
  byte-identical to `717fcc6`: **no grammar or semantic change**.
- LL(1) re-derived from the grammar text: 27 productions, nullable
  `{FunctionTail, StatementTail}`, **0 selection conflicts**, 0 unreachable, 0
  undefined nonterminals.
- 67 diagnostic codes, each defined exactly once, 0 duplicates, 0 dangling.
- 28 reserved words, each traceable to a `charter.md` §4 deferred category, none a
  grammar terminal, none used as an identifier in any example.
- No implementation, dependency, build file, license change, push, tag, or release.

### Defects found and corrected in this commit

| ID | Severity | File | Correction |
| --- | --- | --- | --- |
| F-01 | HIGH | `semantics.md` §1.5a | The claim that a unit named `java` "is not a concern" was false; replaced with the measured `javac` behaviour and `OQ-11` |
| F-02 | MEDIUM | `syntax.ebnf` §6.1 | Three FIRST/FOLLOW cells were wrong (`Separator` FOLLOW missing `EOF`; `Statement` FOLLOW missing `}`; `CallSuffix` FOLLOW wrongly containing `EOF`) |
| F-03 | MEDIUM | `GATES.md` G12 | `ARA-SRC-0002` and `ARA-PARSE-0199` have no test obligation; carried to D002 |
| F-04 | MEDIUM | `GATES.md` G13, this file | Verification rows citing uncommitted scripts were merged across milestones; disambiguated and supplemented by `REPRODUCIBILITY.md` |
| F-05 | LOW | `GATES.md` G9 | The criterion "matches actual HEAD" is unsatisfiable for a ledger written before its own commit; reworded, `audit_commit` added |
| F-06 | LOW | `adr/0003-model-limits.md` | The D001 authoring session ran on provider `robin`; the recorded limits were probed on `hive`. Provenance now separates them; no value changes |
| F-07 | LOW | — | `.ara` appears substantively in 8 files while `OQ-8` is open; all occurrences marked PROPOSED. No change made |
| F-08 | LOW | audit archives | Archived D001 report says 3,859 lines (actual 3,818) and 127 semantic rules (actual 130); corrected in the archive's limitation note |
| F-09 | LOW | `semantics.md` §11.9 | Reserving local slot 0 is a deliberate convention, not a JVM requirement; now stated with rationale |
| F-10 | LOW | — | `conformance-plan.md` shorthand codes are ambiguous (20 numbers span multiple namespaces); raised as `OQ-12`, not rewritten |

### Preserved status

**Automatic compaction remains `UNVERIFIED`.** `kilo debug config` resolves the
whole `compaction` block including `threshold_percent`, which establishes that the
installed build *recognizes* the field. A query of the entire Kilo event store for
any type containing `compact` returns zero rows, so no compaction has ever been
observed firing here. This audit did not manufacture one and did not upgrade the
label.

## D001-R3 — Contract closure and final owner review (2026-10-20)

Documentation and verification only. No implementation file, no D002 activity, no
push.

### What changed and why

| File | Change | Class |
| --- | --- | --- |
| `docs/spec/semantics.md` | §1.2 rewritten; new §1.2a five-namespace table, §1.2b three-rule unit-name policy, §1.2c JVM package note; §1.5a rewritten as a measured per-name table; `ARA-SRC-0005` added | semantic clarification, previously authorized subject matter |
| `docs/spec/conformance-plan.md` | 83 shorthand references expanded to full codes; 7 new obligations (`ARA-SRC-0001`, `ARA-SRC-0002`, `ARA-SRC-0005`, `ARA-PARSE-0199`, `ARA-JVM-0001`, reserved-code absence); naming rows split into rules 1/2/3 | documentation correction |
| `docs/spec/lexical.md` | one shorthand reference expanded | documentation correction |
| `docs/charter.md` | unit-name rule aligned to §1.2b | documentation correction |
| `docs/progress/GATES.md` | G9, G12, G13 reassessed to `PASS`; `G9-A` criterion amendment proposed with original text preserved; self-assessment rewritten | governance correction |
| `docs/progress/DECISIONS.md` | `OQ-10`, `OQ-11`, `OQ-12` recorded as resolved-in-specification pending ratification, with the R2 errors corrected; new `OQ-13` | governance correction |
| `docs/progress/audit/D001-AUDIT-R2.md` | keyword-count and shorthand-count errors corrected in place, marked as corrected by R3 | provenance |
| `docs/progress/STATE.md`, `CONTINUATION.md` | this section and the handoff | recovery |

### Corrections to the D001-R2 audit

Three R2 claims were wrong. Each is corrected at its source, not merely noted.

| R2 claim | Reality | Cause |
| --- | --- | --- |
| JLS 21 declares 52 reserved keywords | **51** | a regex captured the gloss word `underscore` from `_ (underscore)` |
| `javac` rejects 5 contextual keywords as class names | `javac` rejects all 68 keyword names, plus `true`/`false`/`null` | generalized from a five-name probe |
| 54 lines carry a diagnostic shorthand | **83 occurrences over 35 numbers** | the counter matched only some patterns |

One R2 statement was imprecise rather than wrong: `java` breaks Java source only
in the fully qualified `java.` form. `String` and `System` break the unqualified
form. §1.5a now separates the cases.

### Gate status

All fifteen D001 gates are `PASS`. Milestone verdict remains `CONDITIONAL`
because `G9-A` needs owner approval and five decisions are open. G12 and G13 were
closed by doing the missing work, not by redefining the criterion.

### Naming policy summary (§1.2a–§1.2c)

Five namespaces — Arara identifiers, Arara unit names, JVM binary names,
Java-source-accessible names, protected platform packages — with separate
constraints. Function names are deliberately permissive; unit names are
restricted on the 71-keyword list and the five shadowing names. Every claim is a
probe result, and every probe had a control that compiled cleanly.

### Audit commit

The D001-R3 corrections are committed locally as **`49ad593`** (parent
`e408966`). This ledger is updated by the checkpoint commit that follows, so the
ledger's own SHA is one step later; read both with `git log --oneline -2`.
Nothing was pushed.

### Verification performed

Recorded with commands in `docs/progress/audit/REPRODUCIBILITY.md` §9:
`ll1final.py` (97 productions after EBNF lowering, 0 LL(1) conflicts, 0
unreachable, 0 undefined), `traceaudit.py` (68 codes, 0 duplicates, 0 undefined
references), `shortaudit.py` (shorthand eliminated from normative files),
`g12check.py` (68 of 68 codes carry an obligation), a table comparison (all 20
FIRST/FOLLOW rows match the computed sets), and a grammar-driven parser run over
every documentation example (11 valid examples parse; the one example documented
as a parse error is rejected; semantic-error examples parse, as they should).

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

Re-measured during Directive 001-R2 (`docs/progress/audit/REPRODUCIBILITY.md` §1);
all values unchanged.

**Provider provenance.** The Kilo session store records the D001 authoring session
as provider `robin`, model `deepseek-ai/deepseek-v4.1-flash-1017`, variant `max`.
The D001-R1 session and the limit probe ran on provider `hive` with the same model
and variant. The model identity and variant are therefore the same, but the
*endpoint* whose context and output limits are recorded in `ADR 0003` differs from
the endpoint that produced D001. No language rule depends on provider limits; the
distinction is recorded so "verified against the provider" is not ambiguous. See
`docs/progress/audit/D001-AUDIT-R2.md` F-06.

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
6. **Reports are now archived (superseded by D001-R2).** As originally recorded,
   the D001 execution report was delivered in the response and not committed,
   because it contained environment and session details that are not project
   records. Directive 001-R2 required the reports to be archived, so both are now
   committed under `docs/progress/audit/` with an explicit provenance table and an
   explicit list of their verification limitations. Archiving them makes the record
   durable; it does **not** make either report independent evidence, and the
   archive says so in its own header. Whether the archive directory is permanent is
   recorded in `DECISIONS.md` as a decision for the owner.

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

## Verification runs (D001 session)

This table records the **D001** session. The D001-R1 session's runs follow it in
their own table. Directive 001-R2 noted that the two sets were previously read as
one table (see `docs/progress/audit/D001-AUDIT-R2.md` F-04); they are separated
here without altering any recorded result.

Rows citing "ad-hoc audit script (not committed)" are honest records of what was
run, not reproducible commands. `docs/progress/audit/REPRODUCIBILITY.md` supplies
the reproducible equivalent for every audit claim.

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
| Unit-name shadowing probe (bytecode scope) | hand-built units named `java`, `String`, `Object`, `record`, `var`, `when` that also call `java/lang/String.length()` | PASS **for bytecode only** — `java/lang/String` still resolves with a unit class of the same simple name. **Corrected by Directive 001-R2:** this result does NOT extend to Java source. With `java.class` on the classpath, `javac` fails `java.lang.String` with `cannot find symbol: class lang`. See `docs/progress/audit/D001-AUDIT-R2.md` F-01 and `semantics.md` §1.5a |
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

**D001 is `CONDITIONAL`, not accepted.** No gate is `BLOCKED`; all fifteen are
`PASS`. What remains is authorization, not evidence.

| # | Decision | Gates acceptance? | Gates D002? |
| --- | --- | --- | --- |
| `OQ-3` | Accept `docs/spec/*` as the D001 contract | yes — this *is* acceptance | yes |
| `OQ-10` | Ratify the 71-name unit-name rule (§1.2b rule 2) | yes | yes |
| `OQ-11` | Ratify the five shadowing names (§1.2b rule 3, `ARA-SRC-0005`) | yes | yes |
| `G9-A` | Approve the amended G9 criterion | yes | no |
| `OQ-8` | Ratify `.ara` | no — D001 holds either way | yes |
| `OQ-7` | Ratify the 28 reserved words | no | yes |
| `OQ-1`, `OQ-2` | Repository visibility; push authorization | no | no |
| `OQ-4`, `OQ-5`, `OQ-6` | Division by zero; shadowing ban; multi-unit | no | yes |
| `OQ-9` | License | no — private repository | no |
| `OQ-12` | Ratify the diagnostic rewrite (already applied) | no | yes |
| `OQ-13` | Keep the vocabulary split | no | no |

Thirteen open questions are recorded in `docs/progress/DECISIONS.md` under "Open
decisions requested from the owner". The consolidated matrix with alternatives,
recommendation, justification, compatibility impact, and D002 effect is
`docs/progress/audit/D001-R3.md` §9. Recommendations are **not** approvals.

## Next executable action

Owner review of `docs/progress/audit/D001-R3.md`, then recorded decisions in
`docs/progress/DECISIONS.md`. No agent action is authorized until then.

The exact sequence:

1. Owner approves or rejects `G9-A` in `docs/progress/DECISIONS.md`.
2. Owner records decisions on `OQ-3`, `OQ-10`, and `OQ-11`. These three gate D001
   acceptance. Any reversal of a decision already applied to the specification is
   made in the same commit as the decision that authorizes it.
3. Owner records decisions on the D002-facing questions: `OQ-4`, `OQ-5`, `OQ-6`,
   `OQ-7`, `OQ-8`, `OQ-12`, and `OQ-13`. These do not block D001 acceptance.
4. Owner records D001 acceptance or rejection in `docs/progress/DECISIONS.md`.
5. Only then may Directive 002 be issued.

Two D002 obligations were closed by Directive 001-R3 rather than carried forward:
`ARA-SRC-0002` and `ARA-PARSE-0199` now have explicit obligations in
`conformance-plan.md` §6a. No traceability debt remains from D001.

Compaction remains **UNVERIFIED**. Zero compaction events exist in the Kilo store
(`SELECT COUNT(*) FROM event WHERE type LIKE '%compact%'` → 0; no session has a
non-null `time_compacting`). Config *recognition* is verified; config *effect* is
not, and must not be upgraded without an observed event.
