# D001 final contract audit (Directive 001-R2)

Status: **COMPLETE — awaiting owner ratification**
Baseline audited: `82a7afa93359848801388c5def63c2fd7ed0a147`, branch
`docs/d001-r1-arara-identity`
Verdict: **CONDITIONAL** (see §11)

This document is an internal review, not independent verification. It was
produced by an agent operating in the same repository, on the same host, with
access to the same toolchain as the work it reviews. Where that matters it is
stated explicitly.

Every `VERIFIED` claim has a command in
[`REPRODUCIBILITY.md`](./REPRODUCIBILITY.md). Anything not reproducible there is
labeled `UNVERIFIED`.

## 1. Scope and what this audit did not do

In scope: Git baseline, the full D001-R1 diff, the LL(1) claim, the reserved-word
list, `OQ-10`, every open owner decision, archiving of the two prior reports, and
the governance and recovery configuration.

Out of scope, and **not done**: no compiler code, no lexer, no parser, no type
checker, no bytecode emission, no `build.gradle`, no dependency addition, no D002
activity, no push, no merge, no tag, no release, no license change. The repository
still contains 24 tracked files — 21 `.md`, 1 `.ebnf`, 1 `.jsonc`, 1 `.gitignore`
— plus the 4 audit documents added by this directive. No `.py`, `.java`, `.kt`,
`.rs`, `.gradle`, or `.xml` file is tracked. `D001-G1` continues to hold.

## 2. Git baseline

```text
HEAD      82a7afa93359848801388c5def63c2fd7ed0a147
branch    docs/d001-r1-arara-identity
upstream  none  (fatal: no upstream configured)
remote    origin https://github.com/JoaoGaValentim/arara-lang.git
ls-remote (no refs)
gh        isPrivate: true, isEmpty: true
fsck      clean
tags      0
```

`main` is `717fcc6` with 7 commits. The audit branch is exactly two commits ahead:
`ce3c67d` (D001-R1 content, 12 files, +332/−95) and `82a7afa` (ledger pin). The
expected baseline is confirmed, the working tree was clean at audit start, and
nothing has been pushed.

The 11 `refs/agents/*` refs are Kilo host checkpoint refs with no common ancestry
with the project history. They are not branches, not project history, and must not
be pushed or interpreted as evidence. This is recorded so a future reader does not
mistake them for stray work.

`VERIFIED`.

## 3. D001-R1 change classification

`git diff 717fcc6 ce3c67d` touches 12 files, +332/−95. Classification of every
substantive change:

| File | Δ | Class | Substance |
| --- | --- | --- | --- |
| `README.md` | +16/−1 | identity | Project name Arara; `.ara` marked proposed |
| `docs/charter.md` | +21/−2 | identity | Name; identity section; extension proposal |
| `docs/adr/0002-compiler-host.md` | +11/−2 | documentation correction | `java.lang.classfile` is absent on JDK 21; ASM confirmed |
| `docs/architecture.md` | +11/−1 | documentation correction | Backend API reference aligned with ADR 0002 |
| `docs/spec/syntax.ebnf` | +26/−13 | documentation correction | Grammar unchanged; fence language, prose, citations |
| `docs/spec/semantics.md` | +68/−19 | documentation + one **semantic** correction | §1.5 name rule clarified; `ARA-PARSE-0199` severity note; §11.9 wording; §1.4a |
| `docs/spec/conformance-plan.md` | +14/−0 | documentation correction | Matrix entries for the new unit-name rules |
| `docs/progress/DECISIONS.md` | +43/−0 | governance | `D001-06`; `OQ-8`/`OQ-10` wording |
| `docs/progress/GATES.md` | +14/−7 | governance | Evidence-column citations |
| `docs/progress/STATE.md` | +171/−25 | governance | Ledgers, environment baseline, verification runs |
| `docs/progress/CONTINUATION.md` | +22/−7 | governance | Handoff for the audit branch |
| `.kilo/rules/01-compiler-invariants.md` | +10/−2 | governance | Diagnostic namespaces; `Unit` mapping |

Two checks support the classification:

```sh
git show 717fcc6:docs/spec/syntax.ebnf | awk '/```ebnf/{f=1} f' > /tmp/a
git show ce3c67d:docs/spec/syntax.ebnf | awk '/```ebnf/{f=1} f' > /tmp/b
diff /tmp/a /tmp/b     # empty
```

The production block is byte-identical across D001-R1. **R1 changed no grammar
rule, no operator precedence, no evaluation order, and no type rule.** The single
`semantics.md` change that is arguably semantic — the §1.5 sentence about unit
names colliding with `java.lang` — asserted an incorrect claim (§7.1 below), so
correcting it is a documentation correction, not a semantic modification.

No frozen semantics were altered by D001-R1. `VERIFIED`.

## 4. LL(1) — independent re-derivation

The published claim is that `syntax.ebnf`'s grammar is LL(1). This audit
re-derived it from the grammar text with an independent checker rather than
re-reading the existing tables. The checker lowers EBNF (`[ ]`, `{ }`, `|`) to 59
BNF productions, computes nullable/FIRST/FOLLOW to a fixed point, and compares
selection sets pairwise for every alternative of every nonterminal. Source is
listed in [`REPRODUCIBILITY.md`](./REPRODUCIBILITY.md) §6.

```text
productions: 27
nullable declared: ['FunctionTail', 'StatementTail']
LL(1) conflicts: 0
```

Reachability: 27 declarations, 27 reachable from `Program`, 0 unreachable, 0
undefined nonterminals. The "27 productions" figure in `syntax.ebnf` §6.3 is
correct.

**The LL(1) verdict survives, but the published table does not.** Comparing the
computed FIRST/FOLLOW sets against the §6.1 table row by row found three wrong
cells:

| Line | Nonterminal | Cell | Published | Correct | Effect |
| --- | --- | --- | --- | --- | --- |
| 245 | `Separator` | FOLLOW | `IDENT`, `fun`, `if`, `return`, `val`, `var`, `{`, `}` | same **plus `EOF`** | `FunctionTail ::= Separator [ FunctionList ]` puts `EOF` in FOLLOW(`Separator`) |
| 246 | `Statement` | FOLLOW | `SEMI` | `SEMI`, **`}`** | the last statement of a block is followed by `}`; `StatementTail` derives empty |
| 249 | `CallSuffix` | FOLLOW | `…`, `SEMI`, `}`, **`EOF`** | `…`, `SEMI`, `}` (**no `EOF`**) | `CallSuffix` is never in final position; `IdentStatement` production ends there, so FOLLOW is that of `IdentStatement` |

All three are transcription errors in the human summary. None is a grammar
defect, and none creates or masks a conflict: the checker's corrected sets
already yield 0 conflicts, and no decision in §6.2 depends on the three wrong
cells. `FunctionTail` vs `EOF` is unaffected (`EOF` in FOLLOW(`Separator`)
*strengthens* the disjointness argument), and `Statement`'s `}` does not touch
any choice point because `Statement` has no alternatives that begin with `}`.

One further observation: §6.1 lists 20 of the 27 nonterminals. The seven
`Expression` precedence levels are omitted from the table. They are covered by §4
of the conformance plan ("one test per level") and their FIRST/FOLLOW sets are
identical to `Expression`'s, so this is a presentation gap, not an analysis gap.
It is recorded here so the omission is not later mistaken for an oversight.

`VERIFIED` for the LL(1) property. `FAILED` for three cells of the §6.1 table,
corrected in this audit.

## 5. Reserved words

`lexical.md` §6 rules 29–31: 12 keywords, **28** reserved words, each mapped to a
`docs/charter.md` §4 deferred-feature category.

Cross-checks performed:

- No reserved word appears as a grammar terminal. The grammar's keyword literals
  are exactly `else false fun if return true val var`.
- No reserved word appears as an identifier in any of the 12 `arara` example
  fences.
- All 28 trace to a charter §4 deferred category, so the list is defensible as
  "reserved for features the charter already excludes".

One inconsistency: **`yield` is not in the reserved list**, while
`semantics.md` §1.2 and `charter.md` discuss it as a Java restricted identifier
that stays legal. That is correct for the current subset — `yield` is a Java
*contextual* keyword, and Arara has no generators. But it means the reserved list
is not "Java keywords not in Arara"; it is a hand-curated set. That is a
defensible choice, and it is `OQ-7`'s subject. It should be ratified explicitly
rather than inherited silently.

`VERIFIED`.

## 6. OQ-10 — restricted unit names

### 6.1 What the primary sources say

Java SE 21 Language Specification §3.9 defines **52** reserved keywords: the 50
familiar ones plus `_`, plus three that are reserved but unused (`goto`, `const`,
and `_` as of release 9). It separately defines **17 contextual keywords**:
`exports opens requires uses yield module permits sealed var non-sealed provides to when open record transitive with`.
`true`, `false`, and `null` are literals, not keywords. §3.8 additionally defines
`TypeIdentifier: Identifier but not permits, record, sealed, var, or yield`.

JVMS 21 §4.2.1 constrains a class name only to be a valid *unqualified name*: a
non-empty sequence of characters not containing `.`, `;`, `[`, or `/`. §4.2.2
constrains method names by the same rule plus the exclusion of `<` and `>`.

**The JVM's constraint is therefore far weaker than the Java keyword list.** The
two lists are not approximations of each other; they answer different questions.

### 6.2 Measured behaviour (Corretto 21.0.12.1, `-Xverify:all`)

| Name | `javac` accepts as a class name | JVM accepts as class name | JVM accepts as method name |
| --- | --- | --- | --- |
| `var`, `yield`, `record`, `sealed`, `permits` | no | yes | yes |
| `_` | no | yes | yes |
| `module`, `open`, `requires`, `transitive`, `exports`, `opens`, `to`, `uses`, `provides`, `with` | yes | yes | yes |
| `when` | yes | yes | yes |
| `if`, `int`, `class`, `void`, `null`, `goto`, `const`, `switch`, `do`, `true`, `false`, `for`, `else`, `return` | no | **yes** | **yes** |
| `String`, `Object`, `java`, `Int`, `Boolean`, `Unit`, `fun`, `val` | yes | yes | yes |

Every JVM-accepted case was loaded and executed under `-Xverify:all`. Keyword
method names are JVM-valid but not Java-source-callable: a `javac` client
invoking `K.int()` fails with `<identifier> expected`.

### 6.3 Assessment of the specification's position

`semantics.md` §1.2 excludes *Java reserved words* (the keyword list plus `true`,
`false`, `null`) and permits restricted identifiers. That position sits between
the two real constraints: stricter than the JVM, looser than `javac`. The
specification's justification is accurate as far as it goes — "the JVM accepts a
class with such a name, which was verified under `-Xverify:all`, and `javac`
refusal does not bind a bytecode emitter."

What `OQ-10` understates is the *breadth* of the `javac` refusal. It names five
words (`var`, `yield`, `record`, `sealed`, `permits`). The measured set of
Arara-legal names that `javac` rejects is larger: `when` is accepted, but every
Arara keyword used as a *class* name is rejected, and `OQ-10`'s decision affects
a class of names, not five names.

**Recommendation (not an approval):** forbid all 17 contextual keywords plus the
52 reserved keywords as *unit names*, and keep them legal as *function names*. **Corrected by Directive 001-R3:** the JLS 21 §3.9 count is **51**, not 52; the extra item was the gloss word `underscore` in `_ (underscore)` captured by a regex. The recommendation stands with 51 reserved plus 17 contextual, and `semantics.md` §1.2b now states the policy.
Rationale: a unit name is the class-file name, which is exactly the boundary the
D005 interop milestone has to cross; a function name is a method name in an
unnamed-package class that Arara calls with `invokestatic`, so keyword method
names cost nothing today but would also need forbidding if D005 ever wants Java
source to call Arara code. Forbidding the full set now is a one-line change to
§1.2 that cannot break any conforming program, because no conforming program can
depend on a name the owner has not ratified. The cost of the looser rule is paid
only in D005, and then under time pressure.

`VERIFIED`. The decision itself remains `OQ-10`, open.

## 7. Findings

Findings are ordered by severity. Each names the file, the claim, and the
evidence.

### F-01 — `semantics.md` §1.5 dismisses a real `java.lang` shadowing hazard (`HIGH`)

**File:** `docs/spec/semantics.md:48`

**Claim:** "A unit name that equals `java` or would collide with `java.lang` is
not a concern because there are no packages and generated classes are in the
unnamed package."

**Evidence:** this is false for Java *source* on the classpath. With a unit class
`java.class` present:

```text
C.java:1: error: cannot find symbol
  symbol:   class lang
  location: class java
```

The control case (no `java.class`) compiles. Same class of failure for
`System.class` (`cannot find symbol: variable out`) and `String.class`
(`java.lang.String cannot be converted to String`).

**Why it matters:** Arara's own emitted bytecode uses internal names
(`java/lang/String`), so an Arara program is unaffected. But D005 is defined as
bidirectional Java interop, and a unit named `java`, `String`, `System`, `Object`,
or `Math` makes Java code on the same classpath uncompilable. The specification
asserts the opposite and calls it "not a concern", which forecloses the question
instead of deciding it.

**Correction applied:** the sentence now states the measured behaviour and
records that the rule is a source-interop limitation rather than a non-issue. The
underlying policy question is added to the decision matrix as `OQ-11`.

### F-02 — three FIRST/FOLLOW cells are wrong (`MEDIUM`)

Covered in §4. Transcription errors in a table that the specification presents as
mechanically derived. Corrected in `syntax.ebnf` §6.1. The LL(1) verdict is
unchanged.

### F-03 — `D001-G12` (traceability) does not support `PASS` (`MEDIUM`)

**File:** `docs/progress/GATES.md:36`

**Claim:** every normative rule has a test obligation.

**Evidence:** 67 diagnostic codes are defined across the three normative
specifications and all 67 appear exactly once in the consolidated tables. Six of
them are never named in `conformance-plan.md`:

`ARA-JVM-0002`, `ARA-JVM-0005`, `ARA-JVM-0006`, `ARA-JVM-0007`,
`ARA-PARSE-0199`, `ARA-SRC-0002`.

Of those six, four are covered *substantively* but not by name:
`ARA-JVM-0002` by §11.12 ("load and run every fixture under `-Xverify:all`") and
`ARA-JVM-0005/0006/0007` by §11.14 ("synthesized inputs near each limit; assert
the documented code"). Two have no obligation of any kind:

- `ARA-SRC-0002` — input file cannot be read. Nothing in the plan exercises an
  unreadable or missing input file.
- `ARA-PARSE-0199` — parse error limit reached at 100 errors. Nothing in the plan
  exercises the error-count limit, the `NOTE` severity of the companion
  diagnostic, or the "parsing stops" behaviour.

So the strict reading is 6 unnamed codes; the substantive reading is 2 codes with
no test obligation at all. Either way the gate text "every normative rule has a
test obligation" is not true as written, and `PASS` is not defensible.

Separately, 10 of 27 grammar productions are not individually named in
`conformance-plan.md` §4: `FunctionTail`, `StatementList`, `StatementTail`,
`Separator`, and the six binary precedence levels. §4 opens with "For every
production in `syntax.ebnf` §2", which makes the intent clear, and §4's
"`Expression` and every precedence level" row plus its recovery row do cover them.
This is a presentation weakness, not a hole, and is recorded without changing the
gate.

**Correction applied:** `D001-G12` is now `BLOCKED`, with the two substantive
gaps named as D002 obligations rather than papered over.

### F-04 — `D001-G13` (no unverified claims) is overstated (`MEDIUM`)

**File:** `docs/progress/GATES.md:37`, `docs/progress/STATE.md:292–293`

`STATE.md` §Verification runs contains rows describing checks that are not
commands and are not reproducible from the repository:

- `Grammar structural lint | ad-hoc audit script (not committed) | PASS`
- `FIRST/FOLLOW analysis | ad-hoc audit script (not committed) | PASS`

This is a legitimate consequence of the no-implementation rule, but the gate
claims "every environment claim carries a command and result", and "ad-hoc script,
not committed" is neither. The rows also duplicate the D001-R1 section's rows for
the same checks, which conflates two milestones' evidence into one table.

The audit supplies the missing reproducibility artifact:
[`REPRODUCIBILITY.md`](./REPRODUCIBILITY.md) contains the checker source and the
command for every audit claim. The pre-existing rows are annotated rather than
rewritten, because rewriting them would destroy the record of what was actually
run.

**Correction applied:** `D001-G13` is now `BLOCKED`; the ledger rows are
disambiguated by milestone.

### F-05 — `D001-G9` (ledger accuracy) describes a moving target (`LOW`)

**File:** `docs/progress/GATES.md:33`, `docs/progress/STATE.md:14–15`

`STATE.md` pins `last_verified_commit: ce3c67d` while HEAD is `82a7afa`. That is
*by design*: the ledger is edited before each commit, so its pin is always the
preceding commit, and the convention is documented in the header. The gate however
says "`STATE.md` matches actual branch, HEAD, and working tree at report time",
which the ledger does not — and cannot — satisfy if it is also written before the
commit that contains it. The gate and the convention are mutually unsatisfiable.

This is not a defect in the work; it is a defect in the gate's phrasing. It is
corrected, not waived: `D001-G9` now reads "records the branch, the commit
preceding this ledger, and the working-tree state", which is checkable.

**Correction applied:** `D001-G9` is now `BLOCKED` with the corrected
criterion, and `STATE.md` gains an explicit `audit_commit` field so a reader can
verify both the pin and the ledger's own commit.

### F-06 — the D001 authoring session ran on a different provider than recorded (`LOW`)

**Files:** `docs/progress/STATE.md` §Environment baseline, `docs/adr/0003-model-limits.md`

The Kilo session store records the D001 execution session as provider `robin`,
model `deepseek-ai/deepseek-v4.1-flash-1017`, variant `max`. `STATE.md`,
`GATES.md`, and `ADR 0003` record `hive`, and `ADR 0003`'s empirical limit probe
was made against the Hive endpoint. The D001-R1 session did run on `hive`.

So the model *identity* is the same and the variant is the same, but the provider
*endpoint* differs between the session that produced D001 and the endpoint whose
limits are recorded. This does not invalidate any specification claim — no
language rule depends on provider limits — but it does mean "verified against the
provider" is ambiguous for D001.

**Correction applied:** `ADR 0003` and `STATE.md` now distinguish the authoring
session's provider from the probed provider. No limit values change; the
provenance does.

### F-07 — `.ara` appears in 10 files while `OQ-8` is open (`LOW`)

`.ara` is referenced in `README.md`, `docs/architecture.md`, `docs/charter.md`,
`docs/progress/{STATE,CONTINUATION,DECISIONS}.md`,
`docs/spec/{semantics,conformance-plan}.md`, and the two archive documents — 30
occurrences, 8 substantive. Every normative occurrence is already marked
**PROPOSED** and tied to `D001-06` / `OQ-8`. The risk is not that the value is
wrong; it is that repetition creates pressure to treat a proposal as settled.
This is a live decision for the owner, not a defect.

### F-08 — the archived D001 report contains three wrong numbers (`LOW`)

Corrected in the archive's limitation note and here:

| Archived claim | Actual | How measured |
| --- | --- | --- |
| "3,859 lines" of documentation at `c5f376a` | **3,818** | `git ls-tree -r c5f376a` + `wc -l` per file |
| "127 semantic rules" | **130** numbered sub-rules (128 distinct base ids, `1.4a`/`1.7a` being suffixed additions) | regex over `semantics.md` |
| "24 tracked files" | 24 | `git ls-files \| wc -l` |
| "67 diagnostic codes" | 67 | regex census over the three specifications |

The two errors do not affect any conclusion in that report; both are counts, not
claims. They are recorded because the report is archived as evidence, and evidence
that is quietly wrong is worse than evidence that is labeled wrong.

### F-09 — `§11.9` local slot 0 is a convention, not a JVM requirement (`LOW`)

**Files:** `docs/spec/semantics.md` §11.9, `docs/spec/conformance-plan.md` §6a

The specification says slot 0 is "unused for zero-parameter methods". Measured:
`javac` *does* use slot 0 for the first local in a zero-parameter static method
(`istore_0`/`iload_0` per `javap -l`), and a hand-built method that reserves slot 0
and starts its locals at 1 also verifies and runs. So both conventions are legal;
the specification is choosing one. That is a good choice — it keeps local
numbering uniform — but it should be stated as a deliberate convention so a D002
implementer does not "optimize" it away believing the JVM requires it.

**Correction applied:** §11.9 now states the convention and its rationale
explicitly.

### F-10 — `conformance-plan.md` uses ambiguous diagnostic shorthand (`LOW`)

The plan abbreviates codes as bare four-digit numbers after a full code on the
same line. 20 of the 67 numbers are ambiguous across namespaces — `0001` maps to
`ARA-JVM-0001`, `ARA-LEX-0001`, and `ARA-SRC-0001`; `0101` to three more; `0201`
to two. On 54 lines a shorthand number appears with *no* full code, and on lines
70, 73, 74, 75, 82, 84, 93–102, 104–108, 145–147 the intended namespace must be
inferred from the table's subject.

The inference is reliable in practice, because each table section covers one
namespace. But §7 requires that a rule be reachable from its test, and a reader
cannot follow `(0103)` on line 70 to a code without knowing that the lexical table
implies the lexical namespace. This is a documentation-quality defect in a
document whose entire purpose is traceability, and it made this audit's own first
gap count wrong until the shorthand was resolved.

**Not corrected in this audit** — rewriting 54 lines of a normative conformance
matrix is a substantive change to a document the owner has not yet ratified. It is
raised as `OQ-12`.

What this audit did add to `conformance-plan.md` §6a is narrower and safe: the
four `ARA-JVM-*` codes that had no named obligation, a `MANUAL` note for §1.5a
until `OQ-11` is decided, and an explicit statement that the §11.9 slot-0 test
asserts this project's convention rather than a JVM requirement. Those are
additive rows in an already-blanket section; they do not rewrite existing cells.

## 8. Governance, recovery, and configuration

| Item | Check | Result |
| --- | --- | --- |
| `AGENTS.md` | authority order, evidence classes, prohibited actions, recovery procedure read against the rule files | consistent; no rule weaker than the directive |
| `.kilo/rules/00-governance.md` | read | consistent with `AGENTS.md` §1–§6 |
| `.kilo/rules/01-compiler-invariants.md` | read after the D001-R1 edit | diagnostic namespaces and `Unit` mapping correct |
| `.kilo/rules/02-context-recovery.md` | read | consistent with `AGENTS.md` §9 and `resume.md` |
| `.kilo/commands/resume.md` | 10-step procedure compared to `AGENTS.md` §9 | identical in substance |
| `.kilo/commands/checkpoint.md` | read | commit convention matches `STATE.md` |
| `.kilo/agents/auditor.md` | read | read-only constraints correct |
| `.kilo/kilo.jsonc` | `kilo config check`; strict JSON parse after comment stripping | `No config warnings.`; parses |
| Effective config | `kilo debug config` | compaction resolves `auto:true, threshold_percent:70, prune:true, tail_turns:4, preserve_recent_tokens:8000, reserved:20000` |
| Agent resolution | `kilo debug agent code` | `hive/deepseek-ai/deepseek-v4.1-flash-1017`, `variant: max` |
| Model limits | `kilo models hive --verbose` | 1048576 context / 1048576 output |

**Compaction remains `UNVERIFIED`, and that label is correct.** The configuration
resolves completely, including `threshold_percent`, which the published schema
does not declare but the installed build retains and near-miss spellings do not.
That establishes *recognition*. It does not establish *effect*: a query of the
entire Kilo event store for any event type containing `compact` returns **zero
rows**, and no session row for this repository has a non-null `time_compacting`.
No compaction has ever been observed firing here.

This audit therefore preserves the `UNVERIFIED` status exactly as recorded. It did
not manufacture a compaction event, and a passing configuration check is not
evidence of behaviour.

`VERIFIED` for configuration. `UNVERIFIED` for compaction firing — unchanged.

## 9. Residual risks this audit cannot close

1. **Single machine, single JDK.** Every JVM observation is Corretto 21.0.12.1 on
   macOS arm64. Other JDK 21 vendors and architectures are `UNVERIFIED`. The
   specification's JVM claims are about the *format*, which is vendor-independent,
   but the probes are not.
2. **Self-review.** This audit was performed by an agent in the same repository,
   with the same toolchain, reading documents produced by agents of the same
   family. It is not independent in the sense `AGENTS.md` §5 uses. The only
   genuinely independent artifact in the record is the separate read-only audit
   subagent session, and it shares the same model family and host.
3. **Verifier sufficiency.** The malformed-class probes were rejected by the
   default verifier as well as `-Xverify:all`. That is an observation about the
   shapes probed, not a proof that default verification suffices in general;
   `-Xverify:all` remains the correct gate.
4. **No implementation exists**, so every "the compiler must" statement in the
   specification is unexercised. Conformance is untested by construction, which is
   what D001 being a documentation milestone means.

## 10. Deliverables produced by this directive

| Deliverable | Location | Status |
| --- | --- | --- |
| D001 final audit report | this file | complete |
| Owner decision matrix | §11 | complete |
| Archived D001 execution evidence | [`D001-EXECUTION-REPORT-ARCHIVE.md`](./D001-EXECUTION-REPORT-ARCHIVE.md) | complete, 170 lines, verbatim |
| Archived D001-R1 evidence | [`D001-R1-REPORT-ARCHIVE.md`](./D001-R1-REPORT-ARCHIVE.md) | complete, 69 lines, verbatim |
| Reproducibility findings | [`REPRODUCIBILITY.md`](./REPRODUCIBILITY.md) | complete, includes the checker source |
| Updated recovery ledger | `docs/progress/STATE.md` §D001-R2, `docs/progress/CONTINUATION.md` | complete |
| Gate corrections | `docs/progress/GATES.md` G9, G12, G13, verdict line | complete |
| Final recommendation | §11 | **CONDITIONAL** |

### Resolution (Directive 001-R3)

The shorthand described in F-10 and `OQ-12` has been resolved in the specification
files. `conformance-plan.md`, `lexical.md`, and `semantics.md` now carry only full
`ARA-NS-NNNN` codes in every normative obligation; the ranges in `semantics.md`
§12.2 and §12.3 were rewritten as explicit lists. Two further problems surfaced
during the rewrite:

- The count in F-10 was low. A strict scan found **83** shorthand occurrences over
  **35** distinct numbers, not 54. Of those, 20 numbers mapped to more than one
  namespace (`0001` to JVM, LEX, and SRC; `0100` to JVM and PARSE; the whole
  `0101`–`0111` band to both PARSE and NAME), so the ambiguity was wider than
  first reported.
- Two obligations were missing rather than merely unnamed: `ARA-SRC-0002` and
  `ARA-PARSE-0199`. Both now have explicit obligations in
  `conformance-plan.md` §6a, closing the `D001-G12` gap without deferring it.

References in this report and in `STATE.md` keep their original shorthand, because
they are historical statements about the state of the tree at commit `15f25b2`
rather than normative specifications.

## 11. Final recommendation and owner decision matrix

### Verdict: `CONDITIONAL`

`CONDITIONAL` is a milestone verdict, not a gate status. Gate statuses use only
the five values declared in `GATES.md`; the three settled-in-favour-of-caution
gates are `BLOCKED`, meaning the gate cannot be closed on its own evidence and
requires an owner decision first.

What holds up:

- The Git baseline is exactly as recorded; the branch is local, unpushed, and
  contains no implementation. `D001-G1`, `G2`, `G3`, `G4`, `G5`, `G6`, `G7`,
  `G8`, `G10`, `G11`, `G14` are supported.
- The grammar really is LL(1). Re-derived independently: 27 productions, two
  nullable nonterminals, zero selection conflicts, zero unreachable or undefined
  nonterminals.
- D001-R1 changed no grammar and no frozen semantics. The EBNF production block is
  byte-identical to `717fcc6`.
- All 67 diagnostic codes are defined exactly once, with no duplicates and no
  dangling references.
- No implementation, no dependency, no build file, no license change, no push.

What blocks an unconditional `PASS`:

- **F-01** is a normative sentence that is factually wrong and was introduced by
  D001-R1. A specification that says a hazard "is not a concern" when it is a
  concern is not ready to be a contract.
- **F-03** means `D001-G12` was marked `PASS` on a claim it does not satisfy. At
  least two diagnostic codes have no test obligation at all.
- **F-04/F-05** mean two further gates were marked `PASS` on criteria that are not
  checkable as worded.
- **OQ-10** asks the owner to choose a name policy, and the specification's
  current text depends on that choice being made. It is not a blocker for the
  grammar, but it is a blocker for calling §1.2 settled.

None of these is an architecture problem. The language design, the grammar, the
type rules, the JVM mapping, and the diagnostic architecture are coherent and
internally consistent. The defects are in the *evidence layer* — a wrong sentence,
three wrong table cells, three gate statuses that overstate what was verified, and
a conformance matrix whose traceability claims do not fully hold. That is
fixable documentation, not a redesign.

### Decision matrix

Nothing below is approved by this audit. Each row needs an explicit owner entry in
`docs/progress/DECISIONS.md` with an identifier and a date, per `AGENTS.md` §1.
"Recommendation" is technical advice, not authorization.

| # | Item | Question | Recommendation | Justification |
| --- | --- | --- | --- | --- |
| 1 | `OQ-1` | Repository private or public? | keep private until D003 | no artifact to publish; publication is irreversible in practice |
| 2 | `OQ-2` | Push the bootstrap and create `develop`? | not yet | the branch is unpushed and the spec is unratified; pushing now publishes unreviewed normative text |
| 3 | `OQ-3` | Accept `docs/spec/*` as the D001 contract? | accept **after** F-01/F-02/F-03/F-04/F-05 are corrected and re-reviewed | the corrections are already applied here; a second read is cheap and this is the contract everything else depends on |
| 4 | `OQ-4` | `Int` division by zero: runtime exception? | confirm `ArithmeticException` | matches JVM `idiv`; the alternative (defined value) costs a branch per division; `min/-1` overflow already must be accepted as `min` |
| 5 | `OQ-5` | Forbid all shadowing? | confirm the prohibition | a total ban is one rule, trivially checkable, and removeable later without breaking programs; Kotlin-style nesting is a per-scope data structure the initial subset does not need |
| 6 | `OQ-6` | Multiple `.ara` units per program? | confirm multi-unit, single function namespace | the unit-name rules, `ARA-SRC-0003`, and the cross-unit `invokestatic` tests already assume it; restricting to one unit would delete tested behaviour |
| 7 | `OQ-7` | Confirm the 28 reserved words? | confirm, and add a `yield` rationale note | the list is defensible and charter-traceable; `yield`'s absence needs a sentence so it is a decision rather than an oversight (F-09 aside, §5 above) |
| 8 | `OQ-8` | Confirm `.ara` as the extension? | confirm `.ara` | it is already used in 8 substantive places, is unambiguous against Java, Kotlin, and Rust sources, and the alternative (`.arara`) buys nothing |
| 9 | `OQ-9` | Choose a license? | defer to first publication, but record the *intent* now | no artifact is published; choosing a license under time pressure at release is how mismatched licenses happen |
| 10 | `OQ-10` | Forbid JVM-legal / `javac`-illegal unit names? | **forbid all 69 keywords + 17 contextual keywords as unit names**, keep them legal as function names | §6.3: the `javac` refusal is broader than the five names the OQ lists; the cost is one line in §1.2 and no conforming program can depend on an unratified name |
| 11 | `OQ-11` *(new, F-01)* | Forbid unit names that shadow `java.lang` types (`java`, `String`, `System`, `Object`, `Math`)? | forbid the fixed list of `java.lang` names the subset can emit or reference | measured: such a unit makes Java source on the same classpath uncompilable; D005 is bidirectional by definition, so this is a real constraint, not a hypothetical |
| 12 | `OQ-12` *(new, F-10)* | Rewrite `conformance-plan.md` codes to full `ARA-NS-NNNN` form? | yes, before D002 | §7 makes traceability the point of the document; 54 ambiguous shorthand references defeat it, and a mechanical rewrite is low-risk. The four `ARA-JVM-*` codes and the §1.5a/§11.9 rows were added by this audit; the *shorthand* rewrite is what remains for the owner |
| 13 | *(process)* | Retain `docs/progress/audit/` permanently? | retain | `STATE.md` limitation 6 said no report was in the repository because it contained session details; that reasoning no longer holds now that the reports are archived with explicit provenance and limitations, and the archive is what makes "what was claimed at the time" auditable |
| 14 | *(process)* | Commit the LL(1) checker into the repository? | no — keep it in `REPRODUCIBILITY.md` as a listing | `D001-G1` forbids tracked `.py` sources; a fenced listing is reproducible without violating the gate, and D002 must build its own automated FIRST/FOLLOW test anyway |

### Required owner action

To move D001 from `CONDITIONAL` to accepted, the owner must record, in
`docs/progress/DECISIONS.md`:

1. A decision accepting `docs/spec/*` as the D001 contract (rows 3, and 4–10 or
   their alternatives).
2. A decision resolving `OQ-10` (row 10) and `OQ-11` (row 11), because both change
   normative text in `semantics.md` §1.2/§1.5.
3. A decision on `OQ-12` (row 12), which changes a normative conformance matrix.
4. An explicit statement of whether the two archived reports satisfy `STATE.md`
   limitation 6, and whether `docs/progress/audit/` is permanent.

Until those entries exist, D001 is **not accepted** and D002 is **not
authorized**.

**Stop point.** This audit ends here. No push occurred, no merge occurred, no tag
or release exists, no implementation was written, and D002 was not started.
