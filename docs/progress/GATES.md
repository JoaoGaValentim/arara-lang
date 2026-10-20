# Arara Gates

Acceptance gates by milestone. A gate is objective or it is not a gate. No gate
may be marked PASS without recorded evidence naming the commit, command, and
environment.

Status values: `NOT_STARTED`, `IN_PROGRESS`, `PASS`, `FAIL`, `BLOCKED`.

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
| D001-G3 | Authority consistency | `AGENTS.md` and `.kilo/rules/*` contain no rule weaker than the directive and no mutual contradiction | PASS | `docs/progress/STATE.md` §Audit findings |
| D001-G4 | Kilo config valid | `.kilo/kilo.jsonc` parses and `kilo config check` reports no warnings | PASS | Command output quoted in ADR 0003 and `STATE.md` |
| D001-G5 | Kilo config effective | `kilo debug config` resolves compaction, instruction globs, and agent variant as written | PASS | `STATE.md` §Kilo configuration evidence |
| D001-G6 | Model limits | Non-zero context and output limits appear in resolved model metadata | PASS | ADR 0003; `kilo models hive --verbose` |
| D001-G7 | Reasoning effort | The strongest declared variant is selected and accepted by the provider | PASS | ADR 0003; `kilo debug agent code` → `variant: max` |
| D001-G8 | Recovery completeness | `resume.md` reconstructs directive, scope, state, decisions, pending work, and next action from repository files alone | PASS | `STATE.md` §Independent recovery simulation |
| D001-G9 | Ledger accuracy | `STATE.md` matches actual branch, HEAD, and working tree at report time | PASS | Branch `main`, HEAD `c5f376a`, clean tree; see `STATE.md` §Independent recovery validation |
| D001-G10 | Specification consistency | No two normative documents conflict; every diagnostic code defined exactly once | PASS | `STATE.md` §Audit findings, cross-check results |
| D001-G11 | Grammar derivability | Every example in the specification is derivable from `syntax.ebnf`; FIRST/FOLLOW analysis shows no selection conflict | PASS | `syntax.ebnf` §5, §6.3; `STATE.md` §Audit findings |
| D001-G12 | Traceability | Every normative rule has a test obligation or an explicit `MANUAL` marker | PASS | `conformance-plan.md` §3–§6a |
| D001-G13 | No unverified claims | Every environment claim carries a command and result | PASS | ADRs 0002–0003; `STATE.md` §Verification runs |
| D001-G14 | Git state | Work is committed locally on the recorded branch; no unauthorized push | PASS | `git log` shows the bootstrap commit `c5f376a` and the ledger checkpoint `2b964ff` on `main`; `git status` clean after each; `git remote -v` shows only `origin` and no push occurred |
| D001-G15 | Independent recovery | A fresh session reconstructs state without the transcript | PASS | Two separate `kilo run` processes reconstructed milestone, scope, next action, forbidden actions, and Git state from files alone; the first correctly flagged the missing commit. See `STATE.md` §Independent recovery validation. Limitation: same client and model. |

**D001 self-assessment:** all D001 gates are `PASS` with recorded evidence.
Verdict for owner review: `PASS — READY FOR OWNER REVIEW`. This is not milestone
acceptance: only the owner can accept D001, and acceptance must be recorded in
`docs/progress/DECISIONS.md`.

## D002 — Lexer and parser

Entry: D001 accepted by recorded owner decision.

| # | Gate | Criterion | Status |
| --- | --- | --- | --- |
| D002-G1 | Token model | Token kinds, spans, and line/column semantics match `lexical.md` §1.4 and §3 | NOT_STARTED |
| D002-G2 | Lexical conformance | All positive and negative cases in `conformance-plan.md` §3 pass | NOT_STARTED |
| D002-G3 | Grammatical coverage | Every production in `syntax.ebnf` has a positive fixture and a negative fixture where meaningful | NOT_STARTED |
| D002-G4 | Separation | Automatic separation cases in `lexical.md` §10 pass, including `0107` and `0108` | NOT_STARTED |
| D002-G5 | Diagnostics | Every `ARA-LEX-*` and `ARA-PARSE-*` code is produced by at least one test with the specified span | NOT_STARTED |
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
