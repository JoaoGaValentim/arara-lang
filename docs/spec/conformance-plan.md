# Arara Conformance Plan

Status: **RATIFIED — D001 contract, `D001-13`.** Frozen for the D002–D004
implementation of the initial subset. Any change requires a new decision that
names the superseded rule.
Scope: how each specified rule is proven. It defines test obligations, test
categories, fixture conventions, and the traceability requirement that connects
specification to tests to implementation.
Related: `lexical.md`, `syntax.ebnf`, `semantics.md`,
`docs/progress/GATES.md`.

Fixture paths use `.ara`, ratified by `D001-18`. Only the fixture suffix depends on
the extension; the obligations below do not.

No test in this plan exists yet. D001 defines obligations; D002–D005 own their
execution.

## 1. Principles

1. Every normative rule has at least one test obligation. Rules that cannot be
   tested with a mechanical check are marked `MANUAL` and require recorded human
   review.
2. Tests assert **codes and spans**, not message prose. Prose may change; codes
   and spans may not.
3. A test that passes because the compiler rejected *something* is not
   conforming. Each negative test asserts exactly one primary diagnostic code.
4. Golden files are reviewed once and then frozen. Regenerating a golden to make a
   test pass is a gate failure.
5. Passing unit tests do not prove a milestone. Each milestone's gate in
   `GATES.md` requires its own evidence class.

## 2. Fixture conventions

```text
tests/
  fixtures/
    lex/         <name>.ara                  input
                 <name>.tokens               expected token stream
    parse/       <name>.ara                  valid input
                 <name>.ast                  expected AST dump
    invalid/     <name>.ara                  input with errors
                 <name>.diag                 expected diagnostics
    run/         <name>.ara                  executable program
                 <name>.out                  expected stdout (ASCII only)
                 <name>.code                 expected exit status
    jvm/         <name>.ara                  program compiled and inspected
                 <name>.verify               expected verification result
```

Diagnostic expectation format, one diagnostic per line, ordered:

```text
<code> <startLine>:<startCol>-<endLine>:<endCol>
```

`*` MAY replace any span field when the span is not the point of the test, but a
test must pin spans for rules whose contract is about spans.

Exit codes for executable tests follow the CLI contract in
`docs/architecture.md` §6.

## 3. Lexical test matrix

| Rule group | Positive obligations | Negative obligations |
| --- | --- | --- |
| UTF-8 decoding | ASCII, BMP, supplementary plane (e.g. U+1F99C) inside a string and a comment | Truncated multi-byte sequence (`ARA-LEX-0001`); overlong encoding (`ARA-LEX-0001`); surrogate encoded as 3 bytes (`ARA-LEX-0001`) |
| BOM | BOM at offset 0 ignored | BOM after whitespace (`ARA-LEX-0101`) |
| Line endings | LF, CRLF, CR each produce identical tokens and identical line numbers | — |
| Character repertoire | Every permitted whitespace character | Non-ASCII outside string/comment (`ARA-LEX-0101`); DEL (`ARA-LEX-0101`); NUL (`ARA-LEX-0101`) |
| Comments | `//` at EOF; `/* */`; `/** */`; nested-open sequence `/* /* */` | Unterminated block comment (`ARA-LEX-0301`) |
| Identifiers | `_`, `a1`, `A_B`, maximal length practical | `$` (`ARA-LEX-0103`); non-ASCII letters (`ARA-LEX-0101`) |
| Keywords | Each of the 12 keywords lexes as its keyword token | Keywords in identifier position produce a parse error, not a lexical error |
| Reserved words | Each reserved word produces `ARA-LEX-0401` | — |
| Integers | `0`, `7`, `2147483647`, `2147483648` all lex successfully | Leading zero (`ARA-LEX-0205`); value above 2147483648 (`ARA-LEX-0204`) |
| Strings | empty, ASCII, escapes `\n \r \t \\ \"`, tab literal, non-ASCII | Raw newline (`ARA-LEX-0203`); invalid escape (`ARA-LEX-0202`); control char (`ARA-LEX-0206`); unterminated at EOF (`ARA-LEX-0201`) |
| Operators | Every two-character operator; every single-character operator | Lone `&` (`ARA-LEX-0103`); lone `|` (`ARA-LEX-0103`); `++` is a parser case, see §4 |
| Separation | A `SEMI` after each of the eight trigger tokens at depth 0 (`IDENT`, `INT_LITERAL`, `STRING_LITERAL`, `true`, `false`, `)`, `}`, `return`); no `SEMI` inside parentheses; `+` at line end continues; `;` followed by a line feed produces exactly one `SEMI` | Two consecutive `SEMI` (`ARA-PARSE-0100`); leading `SEMI` (`ARA-PARSE-0100`); `)` at depth 0 preserved for `ARA-PARSE-0117` |

Note on `++`: maximal munch produces `+` then `+`, both valid tokens. The parser
then reports `ARA-PARSE-0105` (expected an expression after the first `+` in a
prefix position) or `ARA-PARSE-0101` in postfix position. `++` is therefore a *parser*
diagnostic, not a lexical one, and the test belongs in §4. No lexical test asserts
`ARA-LEX-0103` for `++`.

Note on lone `&`/`|`: these are single characters with no token. `ARA-LEX-0103` is at that
character. A test MUST assert the exact column.

## 4. Syntactic test matrix

For every production in `syntax.ebnf` §2:

| Production | Positive | Negative |
| --- | --- | --- |
| `Program` | empty file; declarations only; trailing separator | body-only file (`ARA-PARSE-0100` expected `fun`); leading separator (`ARA-PARSE-0100`) |
| `FunctionList` | one, two, and many functions | missing separator between functions (`ARA-PARSE-0100`); two separators between functions (`ARA-PARSE-0100`) |
| `FunctionDecl` | no params; one param; many params; omitted return type; explicit `Unit` | `fun (` (`ARA-PARSE-0106`); missing body (`ARA-PARSE-0114`); missing `(` after the name (`ARA-PARSE-0111`) |
| `ParameterList` / `Parameter` | trailing comma is invalid; sep by `,` | missing `:` (`ARA-PARSE-0112`); missing type (`ARA-PARSE-0104`); missing separator (`ARA-PARSE-0113`); trailing comma (`ARA-PARSE-0100`, expected `Parameter`) |
| `Type` | all four type names | `Unit` as parameter type is a *semantic* error, not syntactic |
| `Block` | empty; one statement; nested blocks | missing `}` (`ARA-PARSE-0103`); missing `{` (`ARA-PARSE-0115`) |
| `Statement` | all five forms | expression statement (`ARA-PARSE-0109`); bare identifier (`ARA-PARSE-0110`) |
| `LocalDecl` | `val`, `var`, every type | missing `=`; missing type (`ARA-PARSE-0104`) |
| `IdentStatement` | assignment; call; call with args | `x` alone (`ARA-PARSE-0110`); `f(` unclosed (`ARA-PARSE-0102`) |
| `CallSuffix`/`ArgumentList` | zero, one, many; nested calls | missing `)` (`ARA-PARSE-0102`); missing `,` between arguments (`ARA-PARSE-0101`, preceding token completed an expression); trailing comma (`ARA-PARSE-0105`, expected an expression) |
| `ReturnStatement` | bare; with expression; each expression form | — |
| `IfStatement` | no else; else block; else-if chain; nested if | missing `(` (`ARA-PARSE-0116`); missing `)` (`ARA-PARSE-0102`); non-block body (`ARA-PARSE-0115` expected `{`); `if` in expression position (`ARA-PARSE-0109`) |
| `Expression` and every precedence level | one test per level; mixed-precedence test per adjacent level pair | — |
| `UnaryExpression` | `-x`, `!b`, `--x`, `!!b`, `-(-x)` | `!` on `Int` is semantic |
| `PrimaryExpression` | literal, `true`, `false`, identifier, call, parenthesized | empty parens (`ARA-PARSE-0105`); `()` as expression |
| Newline rules | `{` on the same line as `)`; `else` on the same line as `}` | `{` on the next line (`ARA-PARSE-0107`); `else` on the next line (`ARA-PARSE-0108`) |
| Recovery | one error in a function does not lose the next function | error cascade: after a bad statement, exactly one diagnostic per bad statement |

Precedence tests must pin exact grouping. Recommended form: an AST dump with fully
parenthesized output, e.g. `1 + 2 * 3` dumps as `(1 + (2 * 3))`.

## 5. Semantic test matrix

Each row is a rule in `semantics.md` and its obligation.

| Rule | Positive | Negative code |
| --- | --- | --- |
| §2.1–2.2 type universe | one program per type | `Any`, `Long`, `Double`, `Char` as a type name (`ARA-PARSE-0104`, expected a type) |
| §2.4 `Unit` misuse | `Unit` result only | `ARA-TYPE-0202` |
| §2.6 no conversions | same-type assignment | `ARA-TYPE-0201` |
| §3.2 duplicate params | distinct names | `ARA-NAME-0105` |
| §3.5 initializer required | all four types | missing `=` is syntactic |
| §3.7 initializer type | exact match | `ARA-TYPE-0201` |
| §4.3 local scope start | use after declaration | `ARA-NAME-0102` for self-reference |
| §4.5 same-scope duplicate | distinct names | `ARA-NAME-0103` |
| §4.6 no shadowing | sibling scopes with same name in different blocks | `ARA-NAME-0104` |
| §5.2 duplicate functions | distinct functions | `ARA-NAME-0101` |
| §5.4 function as value | call form | `ARA-NAME-0109` |
| §5.4–§5.5 namespace separation | a local or parameter named `f` does not shadow the function `f`; `f()` still calls it, and bare `f` still reads the value | `ARA-NAME-0102` for a call to an undefined function; `ARA-NAME-0109` for a bare function name. `ARA-NAME-0111` is unreachable and MUST NOT be asserted |
| §5.7 `println` reserved | user function named differently | `ARA-NAME-0108` |
| §6.2 `println` overloads | all three types | `ARA-TYPE-0206` for zero/two args |
| §6.7 `println` as value | statement use | `ARA-TYPE-0205` |
| §7.1 evaluation order | left-to-right is observable through user functions that print. Fixture: `fun a(): Int { println("a"); return 1 }` and `fun b(): Int { println("b"); return 2 }`, then `println(a() + b())` must produce `a`, `b`, `3` in that order; also `f(a(), b())` for a two-parameter `f` | output test asserts exact stdout order |
| §7.1.3 short-circuit | `false && f()` does not call `f`; `true \|\| f()` does not call `f` | output-based test |
| §7.2.2–7.2.4 literal range | `2147483647`, `-2147483648` | `ARA-TYPE-0204` in the four positions listed |
| §7.3.1 operator typing | every valid combination | `ARA-TYPE-0209` per invalid combination |
| §7.3.5 chained comparison | `a < b` alone | `ARA-TYPE-0209` |
| §7.4.3 string equality | equal and unequal contents; two distinct literals with equal contents | `MANUAL` review of emitted `equals` call |
| §7.5.2 arity | exact arity | `ARA-TYPE-0206` |
| §7.5.3 argument type | exact type | `ARA-TYPE-0215` |
| §7.5.6 `Unit` as value | `Unit` call as statement | `ARA-TYPE-0205` |
| §7.6.1 condition type | `Boolean` | `ARA-TYPE-0211` |
| §8.2 assignment rules | `var` assignment | `ARA-TYPE-0203` for `val`; `ARA-TYPE-0207` for parameter; `ARA-TYPE-0201` for type |
| §8.6 return rules | both directions | `ARA-TYPE-0212`, `ARA-TYPE-0213`, `ARA-TYPE-0214` |
| §8.8–8.11 return paths and reachability | nested if/else, early return, block-in-block | `ARA-TYPE-0208`, `ARA-TYPE-0210` |
| §9.3 overflow | `2147483647 + 1 == -2147483648` | execution test |
| §9.4–9.5 division and remainder signs | all four sign combinations for each operator | execution test |
| §9.6 division by zero | `1 / 0`, `1 % 0`, `0 / 0` | execution test: `ArithmeticException`, exit status 1 |
| §9.7 `Int.MIN / -1` | one case | execution test |

## 6. JVM test matrix

| Rule | Obligation |
| --- | --- |
| §11.1 classfile version | read bytes 6–7 of every emitted class; assert `00 41` (65) |
| §11.2 class shape | `javap -v` (or ASM tree) shows public final super, no fields, no interfaces |
| §11.3 descriptors | `javap -s` matches each function's expected descriptor |
| §11.4 cross-unit calls | two-unit program; assert `invokestatic` target |
| §11.5 entry point | `javap` shows `main([Ljava/lang/String;)V` and `main()V`; executing the class runs the Arara `main` |
| §11.6 exceptions | division by zero produces `ArithmeticException` in stderr and exit 1 |
| §11.7 string equality | execution test with equal contents from distinct literals |
| §11.8 output | execution test with ASCII output compared byte-for-byte |
| §11.9–11.11 frames | `CheckClassAdapter.verify` with data flow on every fixture class |
| §11.12 verification | load and run every fixture class under `-Xverify:all` |
| §11.13 determinism | compile twice; compare SHA-256 of each class file |
| §11.14 limits | synthesized inputs near each limit; assert the documented code. Named codes: `ARA-JVM-0005` (string constant over 65535 modified-UTF-8 bytes), `ARA-JVM-0006` (method bytecode over 65535 bytes), `ARA-JVM-0007` (over 255 parameter slots) |
| §11.12 verification failure | `ARA-JVM-0002` — a deliberately corrupted fixture class loaded under `-Xverify:all` must fail, and the compiler must report this code rather than a warning |
| §11.9 local slot convention | `javap -l` shows slot 0 reserved and the first local at slot 1 for a zero-parameter function. The JVM also accepts the other convention; the test asserts **this project's convention**, not a JVM requirement (`semantics.md` §11.9) |

## 6a. Driver and diagnostic-plumbing obligations

| Rule | Obligation |
| --- | --- |
| §1.2 no input files | `ARA-SRC-0001` for an invocation with an empty argument list. Asserts the code, a non-zero exit status, and that no unit compilation is attempted |
| §1.2b rule 1 pattern | `ARA-SRC-0004` for a name starting with a digit, or containing `-`, `.`, `;`, `[`, or `/` |
| §1.2b rule 2 keywords | `ARA-SRC-0004` for a unit named after any of the 51 JLS 21 reserved keywords, any of the 17 contextual keywords, or `true`/`false`/`null`. Parameterized over the full 71-name list in `semantics.md` §1.2b so a future JLS change is a fixture edit, not a code change. Includes `_`, `int`, `class`, `var`, `yield`, `record`, `non-sealed`, and `when`. **Policy scope, not a `javac` rule:** 60 of these 71 are rejected by `javac` as class names; 11 (`exports`, `opens`, `requires`, `to`, `uses`, `provides`, `with`, `transitive`, `open`, `module`, `when`) are accepted by `javac` and are excluded by Arara policy only. Fixtures MUST assert the Arara diagnostic, never `javac` behavior |
| §1.2 duplicate unit names | `ARA-SRC-0003` for two files with the same base name in different directories passed in one invocation |
| §1.2 unreadable input unit | `ARA-SRC-0002` for a unit path that exists in the argument list but cannot be opened or decoded. Driver-level test: pass a path with mode `0000` and a path that is a directory; assert the code, the offending path in the message, and that no other unit's diagnostics are suppressed |
| §1.2b rule 3 shadowing | `ARA-SRC-0005` for a unit named `java`, `String`, `System`, `Object`, or `Math`. Five negative fixtures, one per name. The diagnostic is distinct from `ARA-SRC-0004` so a program can tell a keyword clash from a shadowing clash |
| §1.5a interop evidence | `MANUAL`. For each of the five names: build the unit, load it under `-Xverify:all` (must succeed), then compile a Java fixture that uses the shadowed simple name against a classpath containing that unit (must fail). The Java fixture is test-harness material, not Arara code, and the recorded evidence is the `javac` error text. The reason this is `MANUAL` rather than automated is that the assertion is about `javac`'s behaviour, which is not a dependency of the compiler. The unit's emitted class must also be checked to carry the internal name `String`/`System`/… and **not** `java/lang/…` (§11.2) |
| §1.6 no `main` | `ARA-NAME-0106` |
| §1.6 wrong `main` signature | `ARA-NAME-0110` for a parameterized `main` and for a non-`Unit` `main` |
| §1.7a duplicate versus `main` precedence | a program with two `main` declarations reports only `ARA-NAME-0101` |
| §12.7.1 ordering | a fixture with three diagnostics at known offsets asserts exact emission order |
| §12.7.2 one per span | a fixture with two defects on one token asserts one diagnostic |
| §12.7.3 suppression | a unit with a lexical error produces no parse diagnostics; a unit with a type error produces no class file |
| §7 `ARA-PARSE-0199` diagnostic ceiling | a generated fixture with 101 independent syntax defects in one unit asserts that exactly 100 `ERROR` diagnostics plus one `ARA-PARSE-0199` `NOTE` are emitted, that parsing stops after the note, and that no diagnostic is emitted for defects past the cutoff. A second fixture with exactly 100 defects asserts no note |
| §11.9 local slot 0 | `javap -l`/ASM inspection shows slot 0 unused for zero-parameter functions and holding the parameter otherwise |
| §11.1 internal emission failure | `ARA-JVM-0001` is not reachable from source. Obligation: an injection test in the emitter's own test suite forces a writer failure and asserts the code is reported rather than a stack trace escaping. This is a compiler-internal test, not a language fixture |
| §11.x reserved codes | `ARA-JVM-0100` and `ARA-NAME-0107` are declared reserved and never produced. The obligation is an assertion that neither appears in the codebase's emission paths and that both remain documented, mirroring the `ARA-LEX-0102` treatment in `D002-G5` |
| §11.10 no uninitialized refs | `CheckClassAdapter` analysis reports no `uninitialized` verification type on every fixture |
| §11.11 closed frame table | a unit test asserts the `getCommonSuperClass` override rejects an unknown class name instead of loading it |
| §11.13 determinism | two compilations of the same program produce equal SHA-256 for every class |
| §6.5 line separator | output fixtures state whether they normalize `\r\n` |

## 7. Traceability requirement

Each implemented rule must be reachable from three places:

1. A numbered rule in `lexical.md`, `syntax.ebnf`, or `semantics.md`.
2. A test identifier `T-<STAGE>-<NNN>` listed in the milestone's test inventory.
3. An implementation location named in the milestone's evidence report.

A rule without a test is an open gate item. A test without a rule is either a
duplicate, a derived invariant, or a defect in the specification; it must be
resolved in one of those directions, not left undocumented.

The milestone report MUST include a coverage table:

```text
rule-id | test-id | result | evidence-commit
```

## 8. Differential and execution testing

- Arithmetic and equality are compared against a hand-written Java matrix that
  performs the same operations on `int` and `String`. The matrix is small,
  reviewed, and independent of the compiler.
- Output tests are byte comparisons of stdout. Non-ASCII output is excluded
  (`semantics.md` §6.6).
- Exit status is asserted for success, compile error, and runtime exception
  cases.

## 9. What this plan does not claim

- It does not claim the tests exist.
- It does not claim any implementation passes them.
- It does not claim Kotlin or Java compatibility.
- It does not define D005 interop tests beyond the requirement that they be real
  compile-and-run tests in both directions.
