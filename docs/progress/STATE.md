# Project State

Machine-readable recovery ledger. Update after every completed work unit. Facts
only; no intentions, no forecasts.

```yaml
active_directive: D001-R6 (owner contract ratification, PR #1 integration, foundation closure); milestone D001
active_milestone: D001 — Foundation (documentation and configuration only)
milestone_authorization: D001-01 and D001-06 (docs/progress/DECISIONS.md)
process_authorization: D001-07 (English-only communication, closed gate status vocabulary)
visibility_and_license_authorization: D001-08 (public visibility, Apache-2.0, Copilot migration)
gate_correction_authorization: D001-09 (D001-G9 was BLOCKED; superseded by D001-11)
r5_authorization: D001-10 (R5 operations, publication)
gate_ratification: D001-11 (G9-A ratified; D001-G9 BLOCKED -> PASS)
r6_authorization: D001-12 (R6 workflow; ratification delegated, language design not delegated)
contract_ratification: D001-13 through D001-23 (OQ-3,4,5,6,7,8,10,11,12,13,14)
current_task: D001-R6 complete — contract ratified and frozen, PR #1 integrated as merge commit 5441a9f, post-merge verified, D001 ACCEPTED (D001-24)
current_subtask: none — D001 boundary reached; D002 not authorized
last_completed_unit: D001-R6 — eleven ratifications, contract freeze, merge 5441a9f, post-merge verification, D001 acceptance
branch: infra/d001-r4-copilot-migration
last_verified_commit: 99a41c1
audit_commit: D001-R6 report in docs/progress/audit/D001-R6.md; previous 230abd6 (R5), 2607608 (R4)
commit_convention: this ledger is edited before each commit, so last_verified_commit is always the commit immediately preceding the one containing this line; verify with `git log --oneline -3`
remote: origin https://github.com/JoaoGaValentim/arara-lang.git (public; default branch `main` at 5441a9f after the authorized merge)
working_tree: clean at the recorded last_verified_commit before this checkpoint commit
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

### Verdict at D001-R2 *(historical — superseded)*

> This records the D001-R2 verdict. It is **not** the current status. Directives
> 001-R3 through 001-R6 superseded it: G12 and G13 were closed on evidence, `G9-A`
> was ratified, and the owner ratified the remaining decisions. See §"Decision
> status" and `docs/progress/audit/D001-R6.md`.

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
| `javac` rejects 5 contextual keywords as class names | ~~`javac` rejects all 68 keyword names, plus `true`/`false`/`null`~~ — **wrong; corrected by 001-R4, count fixed by 001-R5:** `javac` rejects 60 names and accepts 11 contextual keywords | generalized from a five-name probe, then measured with a harness that wrote every candidate to `t.java` |
| 54 lines carry a diagnostic shorthand | **83 occurrences over 35 numbers** | the counter matched only some patterns |

The second row is deliberately kept with its wrong value struck through: R3
replaced one unmeasured claim with another invalid measurement, and 001-R4 found
it. The corrected counts and the reproduced listing are in
`docs/progress/audit/REPRODUCIBILITY.md` §9d.

One R2 statement was imprecise rather than wrong: `java` breaks Java source only
in the fully qualified `java.` form. `String` and `System` break the unqualified
form. §1.5a now separates the cases.

### Gate status

*As recorded by D001-R3 at the time:* all fifteen D001 gates were `PASS` with the
milestone verdict `CONDITIONAL`. **Superseded by D001-R4:** `D001-G9` is
`BLOCKED` against the authorized criterion (`D001-09`), so the verdict is
`BLOCKED`. The R3 sentence below describes the state it recorded, not the current
state. G12 and G13 were closed by doing the missing work, not by redefining the
criterion.

Original R3 text: "All fifteen D001 gates are `PASS`. Milestone verdict remains
`CONDITIONAL` because `G9-A` needs owner approval and five decisions are open."

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

## D001-R4 — Public repository, Apache-2.0, Copilot migration (2026-10-20)

Documentation, licensing, and infrastructure only. No compiler source, build file,
test, or CI workflow. No push, merge, tag, or release.

### What changed and why

| File | Change | Class |
| --- | --- | --- |
| `LICENSE` | New. Unmodified Apache License 2.0 text from `apache.org/licenses/LICENSE-2.0.txt`, 202 lines, SHA-256 `cfc7749b96f63bd31c3c42b5c471bf756814053e847c10f3eb003417bc523d30` | license adoption (`D001-08`) |
| `README.md` | License section, license and agent-environment identity rows, Copilot entry point, layout | documentation |
| `docs/spec/semantics.md` | §1.2b rule 2 corrected: the 71-name list is Arara policy, not a `javac` requirement; measured table added. §1.5a corrected: unit `String` emits `String`, not `java/lang/String`, with the coexistence table | semantic correction (`F-R4-01`, `F-R4-02`) |
| `docs/spec/conformance-plan.md` | Keyword row states the policy scope and forbids asserting `javac` behavior; §1.5a obligation now asserts the emitted internal name | documentation correction |
| `docs/charter.md` | Unit-name rule notes the 71-name exclusion is broader than `javac` | documentation correction |
| `docs/progress/GATES.md` | `D001-G9` reclassified `PASS` → `BLOCKED`; D001 verdict `CONDITIONAL` → `BLOCKED`; `G9-A` marked not operative; G15 limitation expanded | governance correction (`D001-09`) |
| `docs/progress/DECISIONS.md` | `D001-08` (visibility, license, agent environment), `D001-09` (G9); `OQ-9` resolved; `OQ-10`/`OQ-11` corrected | governance |
| `docs/progress/audit/D001-R3.md` | Header correction banner; §3.2 and §3.4 corrected in place with the original conclusions quoted; decision matrix row 10 corrected | provenance |
| `docs/progress/audit/REPRODUCIBILITY.md` | New §9d (corrected `javac` experiment) and §9e (JVM internal names); §9c.2 marked invalid and retained | provenance |
| `docs/progress/audit/D001-R4.md` | New. Execution record for this directive | audit |
| `.github/copilot-instructions.md` | New. Repository-wide Copilot entry point | infrastructure |
| `.github/instructions/*.instructions.md` | New. Documentation-governance and compiler-invariant instructions | infrastructure |
| `.github/agents/*.agent.md` | New. `arara-engineer`, `arara-auditor`, `arara-recovery` | infrastructure |
| `.github/prompts/*.prompt.md` | New. `checkpoint`, `resume` workflows | infrastructure |
| `docs/progress/STATE.md`, `CONTINUATION.md` | This section and the handoff | recovery |

### F-R4-01 — the `javac` experiment, corrected

D001-R3 wrote every candidate source to `t.java` while declaring
`public class <candidate>`. Java requires a public top-level class to match its
file name, so the experiment failed for every candidate, legal or not. A harness
whose controls fail cannot support a conclusion.

Corrected result on `javac 21.0.12.1` / Corretto 21.0.12.12.1 / macOS arm64, with
five compiling controls: **60 rejected** (51 reserved keywords,
`true`/`false`/`null`, `var`/`yield`/`record`/`sealed`/`permits`, and
`non-sealed`), **11 contextual keywords accepted** (`exports`, `opens`,
`requires`, `to`, `uses`, `provides`, `with`, `transitive`, `open`, `module`,
`when`). 60 + 11 = 71.

D001-R4 first reported 59 by subtracting `non-sealed` as "rejected by rule 1 on
the pattern". That is true of rule order and false of the experiment: it was
compiled and `javac` rejected it. Directive 001-R5 found the arithmetic defect;
`REPRODUCIBILITY.md` §9d.3 now asserts accepted + rejected = candidates.

`OQ-10` was **not** ratified automatically in R4. The 71-name list remained a
documented policy choice, explicitly labelled broader than `javac` requires,
pending owner ratification. *(Superseded by `D001-19`: the owner ratified the
policy in Directive 001-R6. The R4 wording is retained as history.)*

### F-R4-02 — JVM internal names, corrected

`semantics.md` §1.5a had claimed a unit named `String` emits an internal name such
as `java/lang/String`, contradicting §11.2. Measured with `javap`: the unit's
`this_class` is `String`; `java.lang.String` remains `java/lang/String`; both
coexist in one class file that compiles, verifies under `-Xverify:all`, and runs
(exit 0). `defineClass("String", …)` succeeds; `defineClass("java/lang/String", …)`
fails with `IllegalName`. Shadowing is a Java-source resolution problem only.

### F-R4-03 — gate integrity, corrected

`D001-G9` had been `PASS` against amendment `G9-A`, which is still `PROPOSED`. An
unapproved criterion is not operative, so the gate is now `BLOCKED` against the
original authorized text. The original criterion and the proposed amendment are
both preserved in `GATES.md`. The milestone verdict is `BLOCKED`.

### Verification performed

See `docs/progress/audit/D001-R4.md` §9 and the final report. Environment:
macOS arm64; `javac`/`java` 21.0.12.1 (Corretto 21.0.12.12.1); `gh` authenticated
as `JoaoGaValentim`.

- Repository inventory: 29 tracked paths before, plus the D001-R4 additions; no
  `*.java`, `*.kt`, `*.rs`, `*.py`, `*.class`, `src/`, `build.gradle*`, `pom.xml`,
  or `gradlew`. `D001-G1` remains `PASS`.
- Secret scan over tracked files and reachable history: no key, token, password,
  private key, personal path, or private endpoint in tracked content.
- Apache-2.0 integrity: byte-identical to the ASF source; no inserted copyright
  line.
- Copilot file syntax: all eight `.github` files parse as YAML frontmatter plus
  Markdown (`python3` + `yaml.safe_load`).
- DeepSeek bridge: reachable, chat completion `200`, tool call `200` with
  `finish_reason: tool_calls`, all reasoning-effort values accepted, context
  boundary measured at 1 048 576 with a 31-token prompt.
- **UNVERIFIED:** Copilot recovery in a fresh session; Copilot context management;
  whether VS Code forwards `reasoning_effort: max`; Kilo compaction events.

## D001-R5 — Copilot activation, publication, final verification (2026-10-20)

Documentation, configuration, and repository administration only. No compiler
source, build file, test, or CI workflow. No merge, tag, or release.

### Baseline

| Check | Value | Class |
| --- | --- | --- |
| Branch at start | `infra/d001-r4-copilot-migration` | VERIFIED |
| HEAD at start | `26076083700b53a56d40ce454d7accea54a66da3` | VERIFIED |
| Reported R4 commits | `2607608`, `175541d`, `32884b4` present in that order | VERIFIED |
| Working tree at start | 9 modified tracked paths, 2 untracked skill files — all R5 work from an interrupted earlier attempt, none of it foreign | VERIFIED |
| `LICENSE` | Apache-2.0, SHA-256 `cfc7749b…3d30`, unmodified | VERIFIED |
| Remote refs before | none; `gh repo view` reported `isEmpty: true` | VERIFIED |
| D002 implementation | none: no `*.java`, `*.kt`, `*.rs`, `*.py`, `*.class`, `src/`, `build.gradle*`, `pom.xml` | VERIFIED |

The dirty tree was **not** foreign work: every modified path was a D001-R5
correction described in this section. It was preserved and continued rather than
discarded. No reset, rewrite, or stash operation was performed.

### Numerical defect corrected (directive §3)

The D001-R4 report stated 59 rejected `javac` class names. The correct figure is
**60**, because 51 reserved keywords + 3 literals + 5 restricted identifiers + 1
`non-sealed` = 60 rejected, and 11 contextual keywords are accepted, totalling the
71 tested candidates. The earlier figure deducted `non-sealed` as untested even
though it had been compiled and rejected.

The experiment was **rerun in this session** rather than trusted from the report.
Result, identical to the corrected R4 measurement:

| Group | n | Accepted | Rejected |
| --- | --- | --- | --- |
| Reserved keywords | 51 | 0 | 51 |
| Contextual keywords | 17 | 11 | 6 |
| Literals | 3 | 0 | 3 |
| Controls | 5 | 5 | 0 |
| **Total** | **76** | **16** | **60** |

Programmatic assertions over the raw rows (not by eye):
`accepted + rejected == len(candidates) == 71`, `accepted == 11`,
`rejected == 60`, `controls == [5, 0]`. All passed. The check and its observed
output are recorded in `REPRODUCIBILITY.md` §9d.3. Language semantics were not
changed to fit the arithmetic: only the reported count and the explanation
changed.

Corrected at their source: `D001-R3.md` §3.2, `REPRODUCIBILITY.md` §9d.3,
`DECISIONS.md` `OQ-10`, `STATE.md` §"Corrections to the D001-R2 audit",
`semantics.md` §1.2b, `conformance-plan.md`, and `arara-auditor.agent.md`.
Historical claims in the two verbatim report archives were **not** edited; they are
archived deliverables and remain as delivered.

### Copilot custom agents verified by execution (directive §4)

Discovery and effective permissions were tested with real agent invocations under
the Copilot harness, not by YAML parsing alone. All three agents were discovered
and each was invoked:

| Agent | Tools actually available | Write attempt | Shell attempt |
| --- | --- | --- | --- |
| `arara-auditor` | `view` only | no mechanism available | no terminal available |
| `arara-recovery` | `view` only | no mechanism available | not attempted; no terminal |
| `arara-engineer` | `view`, `create`, `edit`, `bash`, `read_bash`, `stop_bash`, `list_bash`, `task`, `read_agent`, `list_agents`, `write_agent` | succeeded (`/tmp/arara-agent-permission-probe-engineer.txt`) | `bash` present |

No agent modified a repository file during the probes. The read-only allowlists
are therefore **enforced**, not merely declared: `tools: ['read', 'search']`
resolves to a read-only set with no file writer, no shell, and no delegation — one
tool (`view`) in the agent host, five (`view`, `rg`, `glob`, `skill`, `sql`) in the
Copilot CLI — and the write/execute/delegate tools appear only for the agent whose
frontmatter lists them. The auditor's probe also recorded that it could not run
`git`, `curl`, or `gh` despite the environment metadata naming them — the allowlist
wins over the ambient environment.

Residual limitation: this verifies the harness behaviour observed in this VS Code
version. A different client version could change the tool-name mapping, and no
mechanism here detects that.

### DeepSeek configuration validated (directive §5)

| Check | Result | Class |
| --- | --- | --- |
| Config file | `~/Library/Application Support/Code/User/chatLanguageModels.json` (user level, outside the repository) | VERIFIED |
| Backup before change | `chatLanguageModels.json.bak-d001-r5-20261020T1636`, mode `600` | VERIFIED |
| Limits | `maxInputTokens` 1018576 + `maxOutputTokens` 30000 = 1048576, exactly the provider context window | VERIFIED |
| JSON syntax | `json.load` succeeds; unrelated groups and keys preserved | VERIFIED |
| Reasoning effort | `max` selected in both the model entry and the Copilot group's `thinkingLevel` | VERIFIED (config) |
| Provider accepts `max` | `reasoning_effort: max` → HTTP 200 with `reasoning_tokens` present | VERIFIED |
| Tool calling at `max` | HTTP 200, `finish_reason: tool_calls`, arguments `{"x":"hello"}` | VERIFIED |
| Reasoning is really active | `reasoning_content` and `completion_tokens_details.reasoning_tokens` non-zero for every effort value | VERIFIED |
| Effort changes behaviour | The provider returns 200 for **any** effort string, including a bogus one, so HTTP status cannot prove the setting is applied | VERIFIED limitation |
| VS Code forwards `max` to the provider | Not observable from inside the client; the copilot extension filters against the declared `supportsReasoningEffort` list and only then writes `reasoning_effort` | UNVERIFIED |

No endpoint address or credential is recorded in this repository. The provider URL
lives only in the user-level file.

### Fresh-session recovery verified (directive §6)

`arara-recovery` was invoked as a subagent process with no access to this
conversation's history and reconstructed, from files alone: active directive,
active milestone, authorized work, branch `infra/d001-r4-copilot-migration`, HEAD
`2607608…`, the full open-decision list, the next safe action, and the forbidden
actions. It correctly reported `working tree: UNVERIFIED` rather than guessing,
because its allowlist has no terminal.

It also found a real defect that this section corrects: `STATE.md` recorded
`last_verified_commit: 32884b4` while HEAD was `2607608`, and `.git/packed-refs`
was stale for the current branch. The first is the normal ledger lag of one
commit that `commit_convention` describes and `G9-A` permits; the second is
ordinary Git behaviour that the recovery procedure did not explain, causing it to
report a false discrepancy. Both are now documented:

- `STATE.md` `last_verified_commit` and `audit_commit` are pinned to `2607608`.
- `arara-recovery.agent.md` explains loose-ref precedence and stale
  `packed-refs`, and tells the agent to take the commit from the loose ref and the
  reflog.

The Copilot-native entry point is now the `resume`/`checkpoint` **skills** plus
`arara-recovery.agent.md`. The `.github/prompts/*.prompt.md` files are retained
for Local-harness compatibility and are explicitly not assumed to run under
Copilot.

### Publication (directive §8)

Pre-publication scan before the first push: tracked content, all reachable
history, and every dangling blob were searched for key/token/private-key
patterns, personal paths, and endpoint addresses. No credential found. No
`.java`/`.kt`/`.rs`/`.py`/`.class`/`.jar`/build file has ever been committed
(`git log --all --name-only`), so no implementation was published.

`main` was published from the reviewed local baseline without rewriting history:

```sh
git push origin refs/heads/main:refs/heads/main
```

Result: `* [new branch] main -> main`; remote refs then showed
`717fcc6… HEAD` and `717fcc6… refs/heads/main`, and the provider's default branch
is `main`. The migration branch is pushed separately and is **not** the default
branch.

### Kilo status (directive §10)

**Retained.** All ten migration criteria are now met except the two explicitly
`UNVERIFIED` client-side items (whether VS Code forwards `max`, and Copilot
context management). Retiring Kilo is therefore permitted but **not performed**:
the directive requires a separate, minimal, reversible commit, and removing a
working fallback is not part of the foundation-verification scope. `.kilo/` is
untouched and still tracked.

## Authorized scope

D001 only: governance, agent configuration, recovery mechanism, architecture,
roadmap, ADRs, language specification drafts, conformance plan, repository
publication and branch protection, and formal reports. `D001-10` authorizes the
D001-R5 operations, including the one-time bootstrap publication of `main` and a
separate push of the migration branch. No merge and no D002 work.

## Forbidden now

- Any lexer, parser, type checker, IR, backend, or bytecode code.
- `build.gradle(.kts)`, `settings.gradle(.kts)`, `pom.xml`, `gradlew`, or a
  compiler source tree.
- Tests or CI configuration.
- Starting D002.
- **Merging** the Draft PR, creating a release or tag, or publishing an artifact.
  Publishing `main` once and opening the Draft PR were authorized; merging was not.
- Force-pushing, rewriting published history, or pushing directly to `main` after
  the initial bootstrap.
- Weakening `AGENTS.md`, `.kilo/rules/*`, or `docs/progress/GATES.md`.
- Introducing any feature from `docs/charter.md` §4.
- Marking `OQ-3`, `OQ-10`, `OQ-11`, or any other open item accepted because the
  infrastructure migration succeeded.

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

## Decision status

**No decision remains open.** After Directive 001-R6 every option raised during
D001 is resolved by a recorded owner decision; the index lives in
`docs/progress/DECISIONS.md`. The eleven outstanding items were ratified as
`D001-13`–`D001-23`, each with its own scope and limitations, and D001 itself was
accepted as `D001-24` after the integration was verified.

| Option | Decision | Status |
| --- | --- | --- |
| `OQ-1` | `D001-08` | Public visibility |
| `OQ-2` | `D001-10` | Published; `main` bootstrapped |
| `OQ-3` | `D001-13` | **RATIFIED** — initial contract |
| `OQ-4` | `D001-14` | **RATIFIED** — `ArithmeticException` |
| `OQ-5` | `D001-15` | **RATIFIED** — shadowing prohibited |
| `OQ-6` | `D001-16` | **RATIFIED** — multiple units, one namespace |
| `OQ-7` | `D001-17` | **RATIFIED** — documented reserved words |
| `OQ-8` | `D001-18` | **RATIFIED** — `.ara` |
| `OQ-9` | `D001-08` | Apache-2.0 |
| `OQ-10` | `D001-19` | **RATIFIED** — 71-name policy, 60/11 preserved |
| `OQ-11` | `D001-20` | **RATIFIED** — five measured names, not exhaustive |
| `OQ-12` | `D001-21` | **RATIFIED** — 68/68 obligations |
| `OQ-13` | `D001-22` | **RATIFIED** — separate vocabularies |
| `OQ-14` | `D001-23` | **RATIFIED** — retain Kilo |

Ratification is not amendment. Each entry states that a future change needs a new
decision naming the superseded rule; the decision index lists those targets.

## Next executable action

D001 is `ACCEPTED` (`D001-24`). The next action is **owner-issued, not
agent-initiated**:

1. Issue a D002 directive. D002 is not authorized by D001-R6 and no compiler code
   may be written before that directive exists.
2. Optionally authorize a narrowly scoped documentation-only follow-up branch to
   reconcile this ledger for post-merge `main`, per `D001-R6.md` §I. Never commit
   that reconciliation directly to protected `main`.
3. `OQ-14` remains ratified as "retain Kilo"; retiring it still requires a new
   decision.
4. D002's first task is to write a lexer against the frozen contract, and it will
   need CI that actually executes tests: none exists today.

Two D002 obligations were closed by Directive 001-R3 rather than carried forward:
`ARA-SRC-0002` and `ARA-PARSE-0199` now have explicit obligations in
`conformance-plan.md` §6a. No traceability debt remains from D001.

Compaction remains **UNVERIFIED**. Zero compaction events exist in the Kilo store
(`SELECT COUNT(*) FROM event WHERE type LIKE '%compact%'` → 0; no session has a
non-null `time_compacting`). Config *recognition* is verified; config *effect* is
not, and must not be upgraded without an observed event. Copilot context handling
is a different mechanism and is `UNVERIFIED` as well.
