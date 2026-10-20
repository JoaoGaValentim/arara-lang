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
| D001-G14 | Git state | Work is committed locally on the recorded branch; no unauthorized push | PASS | `git log` shows `c5f376a` (bootstrap), ledger checkpoints through `717fcc6`, and D001-R1 content commit `ce3c67d`. Until Directive 001-R5 the remote had no refs and no push had occurred; `D001-10` then authorized publishing `main` once and pushing the migration branch. Current state: `main` = `717fcc6` and the migration branch is pushed, both authorized; every other historical commit is local-only. `git remote -v` shows only `origin` |
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
| Recovery ledger reconciled for `main` | **SATISFIED under D001-R7** | was outstanding at R6; `docs/progress/audit/D001-R7.md` §C |

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

| # | Gate | Criterion | Status |
| --- | --- | --- | --- |
| D002-G1 | Token model | Token kinds, spans, and line/column semantics match `lexical.md` §1.4 and §3 | NOT_STARTED |
| D002-G2 | Lexical conformance | All positive and negative cases in `conformance-plan.md` §3 pass | NOT_STARTED |
| D002-G3 | Grammatical coverage | Every production in `syntax.ebnf` has a positive fixture and a negative fixture where meaningful | NOT_STARTED |
| D002-G4 | Separation | Automatic separation cases in `lexical.md` §10 pass, including `ARA-PARSE-0107` and `ARA-PARSE-0108` | NOT_STARTED |
| D002-G5 | Diagnostics | Every producible `ARA-LEX-*` and `ARA-PARSE-*` code is produced by at least one test with the specified span; `ARA-LEX-0102`, which the specification marks as reserved and never produced, is asserted absent instead | NOT_STARTED |
| D002-G6 | Recovery | Recovery rules in `syntax.ebnf` §8 are implemented and tested: one diagnostic per defect, no cascade | NOT_STARTED |
| D002-G7 | AST stability | AST dump is deterministic and suitable for goldens | NOT_STARTED |
| D002-G8 | No leakage | No name resolution, typing, IR, or bytecode code exists | NOT_STARTED |
| D002-G9 | Determinism | Identical input produces identical token and AST dumps across runs | NOT_STARTED |
| D002-G10 | LL(1) regression | An automated FIRST/FOLLOW check over the committed grammar reports no selection conflict, and the grammar text is unchanged from D001 or a decision records the change | NOT_STARTED |
| D002-G11 | CLI surface | `lex` and `parse` subcommands expose tokens and AST dumps without compiling, matching `architecture.md` §6 | NOT_STARTED |

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
