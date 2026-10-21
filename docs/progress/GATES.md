# Arara Gates

Acceptance gates by milestone. A gate is objective or it is not a gate. No gate
may be marked PASS without recorded evidence naming the commit, command, and
environment.

Status values: `NOT_STARTED`, `IN_PROGRESS`, `PASS`, `FAIL`, `BLOCKED`.

This list is closed. No other status identifier may appear in a gate table. A
milestone verdict is not a gate status and is drawn from its own vocabulary:
`PASS`, `CONDITIONAL`, or `BLOCKED`, recorded only in the self-assessment line
below each table.

`BLOCKED` means the gate cannot be settled by the current evidence: something
outside the gate's own scope must happen first — an owner decision on a criterion,
a missing obligation, or a required artifact. It never means "mostly done".

Evidence classes follow `AGENTS.md` §5: `VERIFIED`, `UNVERIFIED`, `FAILED`,
`BLOCKED`.

---

## D001 — Foundation

Scope: documentation and configuration only. No implementation.

Gate status is `PASS` only when the evidence column names a recorded artifact
that exists in this repository at the current commit. Environment observations are
recorded in `docs/adr/0002-compiler-host.md`, `docs/adr/0003-model-limits.md`, and
`docs/progress/STATE.md`.

| # | Gate | Criterion | Status | Evidence |
| --- | --- | --- | --- | --- |
| D001-G1 | No implementation | No `.java`/`.kt`/`.rs`/`.py` source, no `build.gradle*`, no `pom.xml`, no `gradlew`, no `src/` tree | PASS | Repository file inventory in `STATE.md`; ADR 0002 §"Non-decisions" |
| D001-G2 | Required structure | Every path required by the directive exists with meaningful content | PASS | File inventory in `STATE.md` |
| D001-G3 | Authority consistency | `AGENTS.md` and `.kilo/rules/*` contain no rule weaker than the directive and no mutual contradiction | PASS | `docs/progress/STATE.md` §Internal audit resolution |
| D001-G4 | Kilo config valid | `.kilo/kilo.jsonc` parses and `kilo config check` reports no warnings | PASS | Command output quoted in ADR 0003 and `STATE.md` |
| D001-G5 | Kilo config effective | `kilo debug config` resolves compaction, instruction globs, and agent variant as written | PASS | `STATE.md` §Kilo configuration evidence |
| D001-G6 | Model limits | Non-zero context and output limits appear in resolved model metadata | PASS | ADR 0003; `kilo models hive --verbose` |
| D001-G7 | Reasoning effort | The strongest declared variant is selected and accepted by the provider | PASS | ADR 0003; `kilo debug agent code` → `variant: max` |
| D001-G8 | Recovery completeness | `resume.md` reconstructs directive, scope, state, decisions, pending work, and next action from repository files alone | PASS | `STATE.md` §Independent recovery validation |
| D001-G9 | Ledger accuracy | `STATE.md` records the branch, the commit preceding this ledger, the audit commit, and the working-tree state, and each is verifiable by a named command | PASS | Criterion `G9-A`, ratified by `D001-11` under Directive 001-R5 §7. The original criterion and its `BLOCKED` history are preserved below and in `D001-09`. Verified first at `2607608` and again against `e707b10`: `STATE.md` records branch `infra/d001-r4-copilot-migration`, `last_verified_commit`, `audit_commit`, and `working_tree`; each is checkable with `git branch --show-current`, `git log --oneline -3`, and `git status --porcelain=v1 -uall`. The commit containing this ledger necessarily postdates the value it records, which is exactly what `G9-A` permits and the original text forbade. Re-verified against `e707b10` in `docs/progress/audit/D001-R5.md` §F.1, where HEAD's parent equals `last_verified_commit` |
| D001-G10 | Specification consistency | No two normative documents conflict; every diagnostic code defined exactly once | PASS | `STATE.md` §Internal audit resolution and §D001-R1 verification runs |
| D001-G11 | Grammar derivability | Every example in the specification is derivable from `syntax.ebnf`; FIRST/FOLLOW analysis shows no selection conflict | PASS | `syntax.ebnf` §5, §6.3; `STATE.md` §D001-R1 verification runs |
| D001-G12 | Traceability | Every normative rule has a test obligation or an explicit `MANUAL` marker | PASS | Directive 001-R3 closed both gaps instead of deferring them. `ARA-SRC-0002` now has a driver test, `ARA-PARSE-0199` a 101-defect fixture, `ARA-SRC-0001` an empty-argument test, `ARA-JVM-0001` an emitter injection test, and the reserved codes `ARA-JVM-0100`/`ARA-NAME-0107` an absence assertion. `ARA-SRC-0005` was added with five negative fixtures. Reproducible check: `REPRODUCIBILITY.md` §9 `g12check.py` reports 68 of 68 defined codes carrying at least one obligation |
| D001-G13 | No unverified claims | Every environment claim carries a command and result | PASS | Directive 001-R3 replaced the two "ad-hoc audit script (not committed)" rows with executed commands and recorded them in `REPRODUCIBILITY.md`: `ll1final.py` for the grammar lint and FIRST/FOLLOW/selection analysis, `traceaudit.py` for code uniqueness, `shortaudit.py` for shorthand, `g12check.py` for coverage. The D001 and D001-R1 run tables remain separate. Remaining limitation: the checker is reproduced as a listing, not a tracked file, because `D001-G1` forbids tracked `.py` sources |
| D001-G14 | Git state | Work is committed locally on the recorded branch; no unauthorized push | PASS | `git log` shows `c5f376a` (bootstrap), ledger checkpoints through `717fcc6`, and D001-R1 content commit `ce3c67d`. Until Directive 001-R5 the remote had no refs and no push had occurred; `D001-10` then authorized publishing `main` once and pushing the migration branch. State **at R5 evidence time**: `main` = `717fcc6` and the migration branch is pushed, both authorized; every other historical commit was local-only. `git remote -v` shows only `origin`. Since then `main` has advanced by the three authorized merges (PR #1 `5441a9f`, PR #2 `992e84a`, PR #3 `a53b390`); the `717fcc6` observation is historical, not the present tip. The gate criterion is unchanged: no push, merge, or history rewrite occurred outside a recorded authorization, and `main` remains protected against direct and forced pushes. |
| D001-G15 | Independent recovery | A fresh session reconstructs state without the transcript | PASS | A fresh `arara-recovery` invocation under the **GitHub Copilot** harness reconstructed directive, milestone, authorized work, branch, HEAD, open decisions, next safe action, and forbidden actions from files alone, and correctly reported the working tree `UNVERIFIED`. See `docs/progress/audit/D001-R5.md` §F.1. Earlier evidence: two separate `kilo run` processes produced the same result for the Kilo client (`STATE.md` §Independent recovery validation). **Limitation:** both clients were verified against the same durable files; whether either harness survives compaction is `UNVERIFIED` |

**D001 self-assessment (as ratified by Directive 001-R6):** fifteen gates are
`PASS` and none is `BLOCKED`. `D001-G9` was reclassified from `BLOCKED` to `PASS`
after the owner ratified `G9-A` in `D001-11`; the original criterion and the
`BLOCKED` period remain quoted below, so the reclassification is auditable rather
than silent.

The owner ratified the eleven outstanding options in `D001-13`–`D001-23`, and the
integration was verified after the merge. **Milestone verdict: `PASS`** — D001 is
formally **ACCEPTED** in `D001-24`.

Accepted, integrated, and reconciled are three distinct facts, and the first R6
text wrongly reported them as one:

| Fact | Status | Evidence |
| --- | --- | --- |
| Contract accepted by the owner | **SATISFIED** | `D001-24`, `D001-13`–`D001-23` |
| Foundation integrated into `main` | **SATISFIED** | merge `5441a9f`, tree verified |
| Recovery ledger reconciled for `main` | **SATISFIED under D001-R7** | was outstanding at R6; merged as `992e84a`; `docs/progress/audit/D001-R7.md` §C, §J |

No gate is `BLOCKED` or `FAIL`, and no decision remains open. Gate status is
independent of the ledger reconciliation: no gate criterion referenced it, which is
why the gap did not block acceptance but still had to be disclosed and closed.

Acceptance is not authorization to implement. D002 requires its own directive.

### Criterion amendments proposed by Directive 001-R3

A gate criterion is never rewritten to make incomplete work look sufficient. Where
a criterion was defective, the defect is named, the original text is preserved, and
the replacement is proposed for owner approval rather than applied silently. An
amendment that has not been approved by a recorded owner decision is **not**
operative. `G9-A` is the only amendment in this table, and it is now ratified by
`D001-11`, so it is operative; the original criterion text below is preserved
verbatim for audit and is not deleted.

| ID | Gate | Original criterion (verbatim) | Proposed criterion | Why the original is defective | Status |
| --- | --- | --- | --- | --- | --- |
| `G9-A` | D001-G9 | "`STATE.md` matches actual branch, HEAD, and working tree at report time" | "`STATE.md` records the branch, the commit preceding this ledger, the audit commit, and the working-tree state, and each is verifiable by a named command" | A ledger committed after the work it describes can never contain its own commit SHA, so "matches actual HEAD at report time" is unsatisfiable for any accurate ledger. Every D001 ledger failed it for that reason alone | **RATIFIED** by `D001-11` under Directive 001-R5 §7; `D001-G9` re-evaluated against it |

The correction history is preserved rather than overwritten: D001-R2 recorded G9,
G12, and G13 as `BLOCKED` on the theory that the work could not be settled inside
D001. Directive 001-R3 established that G12 and G13 were in fact correctable
within D001 — the missing obligations were written and the unreproducible
commands were executed — so those two moved to `PASS` on evidence, not on a
reworded criterion. G9 was the only genuine criterion defect; Directive 001-R5
satisfied the condition D001-R3 attached to its replacement, so `G9-A` is now
operative. The original criterion text above is retained verbatim and is not
deleted.

## D002 — Lexer and parser

Entry: D001 accepted by recorded owner decision.

Status values are the closed set declared at the top of this file. A gate carries
an evidence column only where evidence exists; an empty cell means no evidence has
been recorded yet.

| # | Gate | Criterion | Status | Evidence |
| --- | --- | --- | --- | --- |
| D002-G1 | Token model | Token kinds, spans, and line/column semantics match `lexical.md` §1.4 and §3 | PASS | `docs/progress/audit/D002-A.md` §E verifies §1.4 spans and positions through the CLI on the reviewed commit `c622d5c`: 1-based line/column, tab as one column, an astral character as one column (hexdump-confirmed 4-byte UTF-8), and half-open spans. `PositionTest` 9 tests. `Token` enforces in its constructor that every non-EOF token has a non-empty span and `EOF` an empty one, so a malformed token cannot exist |
| D002-G2 | Lexical conformance | All positive and negative cases in `conformance-plan.md` §3 pass | PASS | All positive and negative rows of `conformance-plan.md` §3 pass: 223 tests, 0 failures, 0 skipped, counted from the JUnit XML at `c622d5c`. §E of the audit re-checks the boundary cases through the CLI rather than trusting the tests, including the §36 boundary (2147483648 legal, 2147483649 `ARA-LEX-0204`), the §11 spans for `0301`/`0201`/`0203`/`0401`, and 200,000 fuzz inputs with no exception or hang |
| D002-G3 | Grammatical coverage | Every production in `syntax.ebnf` has a positive fixture and a negative fixture where meaningful | NOT_STARTED | none yet |
| D002-G4 | Separation | Automatic separation cases in `lexical.md` §10 pass, including `ARA-PARSE-0107` and `ARA-PARSE-0108` | PASS | `lexical.md` §10 verified both by `SeparatorTest` (47 tests) and by direct CLI sweeps in `D002-A.md` §E: all 8 trigger tokens produce exactly one `SEMI`, all 17 non-triggers produce none, `SEMI` is not a trigger (`x;` + LF yields exactly one), depth never goes negative, `)` at depth 0 is preserved for `ARA-PARSE-0117`, and a run of newlines collapses to one. `ARA-PARSE-0107`/`0108` are lexer-observable only as the `SEMI` that causes them and are asserted in that form; the parser diagnostics belong to D002-B |
| D002-G5 | Diagnostics | Every producible `ARA-LEX-*` and `ARA-PARSE-*` code is produced by at least one test with the specified span; `ARA-LEX-0102`, which the specification marks as reserved and never produced, is asserted absent instead | PASS | `DiagnosticCoverageTest` asserts the produced set equals the producible set **exactly**, not merely that it is non-empty: 12 documented lexical codes minus the reserved one. `ARA-LEX-0102` is asserted absent, as the specification requires, and a non-ASCII identifier candidate is asserted to report `ARA-LEX-0101` instead. Spans for `0201`, `0202`, `0203`, `0301`, `0401`, `0204`, `0205` were re-verified through the CLI in `D002-A.md` §E. `ARA-PARSE-*` codes have no producer yet |
| D002-G6 | Recovery | Recovery rules in `syntax.ebnf` §8 are implemented and tested: one diagnostic per defect, no cascade | IN_PROGRESS | Lexer-side recovery is implemented and verified: a raw break inside a string discards the partial literal and resumes at the break (rules 41, and the backslash-before-break case found by review), an invalid escape skips the escape and continues (rule 44), an unterminated string stops the unit (rule 41), and independent defects each report once. **Not closable on this evidence:** `syntax.ebnf` §8 governs parser recovery, and no parser exists until D002-B |
| D002-G7 | AST stability | AST dump is deterministic and suitable for goldens | NOT_STARTED | none yet |
| D002-G8 | No leakage | No name resolution, typing, IR, or bytecode code exists | PASS | No `Resolver`, `TypeCheck`, `Ir`, `Emitter`, or `Bytecode` file exists in `src`; `org.objectweb.asm` appears in no source file; no ASM, Kotlin, or parser-generator dependency is declared. CI step 7 enforces both conditions on every run, so a regression fails the check rather than being noticed later |
| D002-G9 | Determinism | Identical input produces identical token and AST dumps across runs | PASS | `TokenDumpTest` asserts byte-identical dumps across repeated runs, across interleaved unrelated lexing, across LF/CRLF/CR input, and with or without a BOM. `GoldenFixtureTest` asserts determinism over all 14 fixtures. CI step 6 re-runs the determinism tests on the exact reviewed commit |
| D002-G10 | LL(1) regression | An automated FIRST/FOLLOW check over the committed grammar reports no selection conflict, and the grammar text is unchanged from D001 or a decision records the change | NOT_STARTED | none yet |
| D002-G11 | CLI surface | `lex` and `parse` subcommands expose tokens and AST dumps without compiling, matching `architecture.md` §6 | IN_PROGRESS | `arara lex <file.ara>...` is complete and tested: tokens to stdout, diagnostics to stderr, exit 0 on success and 2 on diagnostics, and `check`/`compile`/`run` are **rejected as unimplemented rather than stubbed** so an absent stage cannot appear to succeed. `D002-G11` covers `lex` **and** `parse`; `parse` is D002-B, so the gate is not closed |

**D002 self-assessment (D002-A only, `c622d5c`):** six gates are `PASS`
(`G1`, `G2`, `G4`, `G5`, `G8`, `G9`), two are `IN_PROGRESS` (`G6`, `G11`), three
are `NOT_STARTED` (`G3`, `G7`, `G10`), and none is `FAIL` or `BLOCKED`. The five
open items are open because their subject — the parser and the AST — does not
exist yet; they are not open because of a defect in the lexer.

**D002 verdict: not yet assignable.** D002 closes only when all eleven gates
satisfy their criterion. As of `c622d5c` the increment is `D002-A`, not D002.


---

## D003 — Semantic frontend

| # | Gate | Criterion | Status |
| --- | --- | --- | --- |
| D003-G1 | Name resolution | All `ARA-NAME-*` rules tested, including the function/value namespace split | NOT_STARTED |
| D003-G2 | Types | All `ARA-TYPE-*` rules tested with exact codes and spans | NOT_STARTED |
| D003-G3 | Return paths | `alwaysReturns` behavior and unreachable-statement rejection match `semantics.md` §8.9–8.11 | NOT_STARTED |
| D003-G4 | Typed IR | Printable, deterministic, golden-tested; no bytecode concerns leak into it | NOT_STARTED |
| D003-G5 | No backend | No ASM dependency and no emission code exists | NOT_STARTED |
| D003-G6 | Traceability | Coverage table `rule-id \| test-id \| result \| commit` is complete for every rule | NOT_STARTED |
| D003-G7 | Namespace separation | Function and value namespaces resolve exactly as `semantics.md` §5.3–§5.9, including a local whose name equals a function name | NOT_STARTED |

---

## D004 — JVM backend

| # | Gate | Criterion | Status |
| --- | --- | --- | --- |
| D004-G1 | Classfile version | Every emitted class is major version 65 | NOT_STARTED |
| D004-G2 | Structural verification | Every emitted class passes `CheckClassAdapter.verify` with data-flow analysis | NOT_STARTED |
| D004-G3 | JVM verification | Every emitted class loads and runs under `-Xverify:all` | NOT_STARTED |
| D004-G4 | Execution behavior | All execution fixtures match expected stdout and exit status | NOT_STARTED |
| D004-G5 | Integer semantics | Overflow, truncating division, remainder signs, division by zero, and `MIN / -1` match `semantics.md` §9 | NOT_STARTED |
| D004-G6 | Entry point | `main([Ljava/lang/String;)V` bridge delegates correctly and ignores its argument | NOT_STARTED |
| D004-G7 | Determinism | Byte-identical output across repeated compilations | NOT_STARTED |
| D004-G8 | No interop claim | No document or test claims Java interoperability | NOT_STARTED |
| D004-G9 | CLI surface | `compile` and `run` behave as specified in `architecture.md` §6, including `--out` and exit codes | NOT_STARTED |

---

## D005 — Java interoperability

| # | Gate | Criterion | Status |
| --- | --- | --- | --- |
| D005-G1 | Java to Arara | Real Java code calls an Arara function and observes the specified result | NOT_STARTED |
| D005-G2 | Arara to Java | Real Arara code calls a specified Java API and observes the specified result | NOT_STARTED |
| D005-G3 | Restrictions | Unsupported interop is rejected with a specific diagnostic, not undefined behavior | NOT_STARTED |
| D005-G4 | CI evidence | Both directions run in CI at a recorded revision | NOT_STARTED |

---

## D006 — Language maturity

Gates are defined per feature when the owner authorizes a feature. There is no
blanket gate for D006.
