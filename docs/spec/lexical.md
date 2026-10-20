# Arara Lexical Contract

Status: **DRAFT — D001. Submitted for owner review. Not frozen.**
Scope: converts normalized source text into a token stream. Normative for
`ARA-LEX-*` diagnostics and for the statement-termination rules that the parser
depends on.
Related: `syntax.ebnf` (grammar), `semantics.md` §12 (consolidated diagnostic
index), `conformance-plan.md` §3 (lexical test matrix).

## 1. Source model

### 1.1 Bytes and encoding

1. A source file is a byte sequence. It MUST be valid UTF-8 in the sense of
   Unicode Standard §3.9: no overlong forms, no surrogate code points encoded
   directly, no code point above U+10FFFF.
2. An invalid byte sequence is `ARA-LEX-0001` at the first invalid byte. The
   remainder of the file is not lexed; the token stream ends at the last token
   produced before the invalid byte.
3. The decoded text is a sequence of Unicode scalar values.

### 1.2 Byte order mark

4. A single U+FEFF at offset 0 of the decoded text is ignored. It produces no
   token and occupies no position.
5. U+FEFF anywhere else is `ARA-LEX-0101`.

### 1.3 Line endings

6. Before lexing, `\r\n` and a lone `\r` are each replaced by a single `\n`.
   Normalization happens once; the normalized text is the only text that spans
   and line numbers refer to.
7. A file need not end with `\n`. The final line exists even if it is empty.

### 1.4 Positions and offsets

```text
Offset : non-negative integer, an index into the normalized text in Unicode
         code points (not bytes, not UTF-16 units).
Line   : 1-based line number; lines are separated by U+000A.
Column : 1-based column number within the line, counted in Unicode code points.
         A tab counts as one column. There are no tab stops.
Span   : half-open interval [start, end) of Offsets. A span never crosses a
         file boundary and never spans two compilation units.
```

8. Every token has a non-empty span except `EOF`, which has an empty span at the
   end of the text.
9. A token's span covers exactly its first character through its last character
   inclusive, i.e. `[firstOffset, lastOffset + 1)`.
10. An implementation may store other units internally, but every reported line,
    column, and span MUST match this definition.

## 2. Character repertoire

11. Outside string literals and comments, the permitted characters are:

    | Set | Characters |
    | --- | --- |
    | Space | U+0020 |
    | Horizontal tab | U+0009 |
    | Vertical tab | U+000B |
    | Form feed | U+000C |
    | Line feed | U+000A |
    | Printable ASCII | U+0021 through U+007E |

12. Any other character outside a string literal or comment is `ARA-LEX-0101`.
    This includes all non-ASCII characters, all C0 controls other than those
    listed above, and U+007F.
13. Consequence, stated explicitly: **identifiers are ASCII-only in the initial
    subset.** Non-ASCII text is permitted only inside string literals and
    comments. This is a deliberate limitation, not an oversight; see
    `docs/charter.md` §4.

## 3. Token kinds

```text
IDENT
INT_LITERAL
STRING_LITERAL
// keywords
fun   val   var   if   else   return   true   false
Int   Boolean   String   Unit
// punctuation and operators
(  )  {  }  :  ,  ;  =  ==  !=  <  <=  >  >=  +  -  *  /  %  &&  ||  !
// internal
NEWLINE       a line break that may trigger statement termination (§10)
RESERVED      a reserved word for a deferred feature (§6)
EOF
```

14. Tokens are produced by maximal munch: at each position the longest token
    that matches wins. This resolves `=` / `==`, `!` / `!=`, `<` / `<=`,
    `>` / `>=`, `&` / `&&`, and `|` / `||`.
15. A single `&` or a single `|` is not a token. Each is `ARA-LEX-0103`.
16. `//` and `/*` are comment openers and take precedence over `/` and `*` by
    maximal munch.

## 4. Whitespace and line breaks

17. Space, horizontal tab, vertical tab, and form feed separate tokens and are
    otherwise ignored.
18. A line feed produces one `NEWLINE` token. Adjacent line feeds produce adjacent
    `NEWLINE` tokens.
19. Whitespace and comments may appear between any two tokens, except inside a
    token.

## 5. Comments

20. `//` starts a line comment that ends immediately before the next U+000A or at
    end of file. The terminating line feed is not part of the comment; it still
    produces `NEWLINE`.
21. `/*` starts a block comment that ends at the first following `*/`. Block
    comments do **not** nest. The text `/* /* */` is one complete comment; the
    following `*/` is lexed as `*` then `/`.
22. A block comment that reaches end of file without `*/` is `ARA-LEX-0301`.
    The span is the opening `/*`.
23. Comments are not tokens, are not preserved in the AST, and have no
    documentation semantics. `/** ... */` is an ordinary block comment.
24. Non-ASCII text and all printable characters are permitted inside comments.
25. A comment is not permitted to split a token. `fun/*x*/ction` is `fun`,
    comment, `ction`, not the identifier `function`.

## 6. Identifiers, keywords, and reserved words

26. An identifier starts with `A`–`Z`, `a`–`z`, or `_`, and continues with
    `A`–`Z`, `a`–`z`, `0`–`9`, or `_`. Length is unrestricted.
27. `$` is not permitted in identifiers. A `$` outside a string or comment is
    `ARA-LEX-0103`.
28. An identifier that is not a keyword and not a reserved word is `IDENT`.
29. Keywords are exactly, with this capitalization:

    | Keyword | Role |
    | --- | --- |
    | `fun` | function declaration |
    | `val` | immutable local binding |
    | `var` | mutable local binding |
    | `if` | conditional statement |
    | `else` | alternative branch |
    | `return` | return statement |
    | `true`, `false` | Boolean literals |
    | `Int`, `Boolean`, `String`, `Unit` | type names |

30. Keywords are reserved in every position. They cannot be used as identifiers,
    function names, parameter names, or local names.
31. The following words are **reserved for deferred features**. They lex as
    `RESERVED` and produce `ARA-LEX-0401` wherever they occur, including in
    positions where an identifier would otherwise be legal. Each word is listed
    with the deferred feature category from `docs/charter.md` §4 that owns it:

    | Reserved words | Deferred category |
    | --- | --- |
    | `class`, `object`, `interface`, `enum`, `this`, `super`, `new`, `init`, `constructor` | classes, inheritance, interfaces, objects, enums |
    | `when` | pattern matching |
    | `while`, `for`, `do`, `break`, `continue` | loops and loop control |
    | `try`, `catch`, `finally`, `throw` | exception syntax |
    | `null` | nullable types |
    | `package`, `import` | packages and modules (charter §4) |
    | `typealias` | type declarations |
    | `suspend` | coroutines and async |
    | `annotation` | custom annotations |
    | `in`, `is`, `as` | type tests, casts, and operator forms |

32. The reserved list may only change by an owner decision recorded in
    `docs/progress/DECISIONS.md`, because adding a reserved word is a breaking
    source change. Every reserved word above is justified by a category in
    `docs/charter.md` §4; a word with no corresponding charter category MUST NOT
    be reserved. Conversely, reserving a word for a category that the charter
    does not list is scope expansion and is prohibited.
33. Identifiers that differ only by case are distinct. There is no Unicode
    normalization because non-ASCII identifiers are not permitted.

## 7. Integer literals

34. `INT_LITERAL` is one or more ASCII digits `0`–`9`. There are no signs,
    underscores, radix prefixes, or suffixes in the token. A leading `-` is the
    unary operator, not part of the literal.
35. `0` alone is valid. A literal with more than one digit whose first digit is
    `0` is `ARA-LEX-0205`. This prevents octal ambiguity.
36. The token's value is the mathematical value of its digits. If that value
    exceeds `2147483648`, the token is `ARA-LEX-0204`.
37. The range check that distinguishes `2147483647` from `-2147483648` is a
    semantic rule, not a lexical one; see `semantics.md` §9.2.
38. A digit sequence immediately followed by an identifier start character is
    still tokenized as a number followed by an identifier (`123abc` is
    `INT_LITERAL` `IDENT`). The parser rejects the sequence with
    `ARA-PARSE-0101`; there is no "invalid numeric literal" lexical rule.

## 8. String literals

39. A string literal starts with `"` and ends at the next unescaped `"`.
40. Between the quotes, the permitted raw characters are:
    - any Unicode scalar value at or above U+0020, except `"` (U+0022) and `\`
      (U+005C);
    - horizontal tab U+0009.
41. A raw U+000A or U+000D inside a string literal is `ARA-LEX-0203`. It is not
    permitted to continue a string across lines, not even with a trailing `\`.
    Recovery: report `ARA-LEX-0203`, discard the partial literal, and resume
    lexing at the line break. A string literal that reaches end of file without a
    closing quote is `ARA-LEX-0201` instead; lexing of the unit stops after it.
42. Any other C0 control (U+0000–U+0008, U+000B, U+000C, U+000E–U+001F) or
    U+007F inside a string literal is `ARA-LEX-0206`.
43. The escape sequences are exactly:

    | Escape | Value |
    | --- | --- |
    | `\n` | U+000A |
    | `\r` | U+000D |
    | `\t` | U+0009 |
    | `\\` | U+005C |
    | `\"` | U+0022 |

44. Any other character after `\` is `ARA-LEX-0202`, reported at the backslash
    and the following character. This includes `\'`, `\0`, `\b`, `\f`, and
    `\uXXXX`. Recovery: skip the backslash and the following character and
    continue the literal. A trailing `\` at end of file is `ARA-LEX-0201`
    (unterminated literal), not `0202`, and lexing of the unit stops.
45. There are no raw string literals, no string interpolation, no multi-line
    strings, and no character literals.
46. The literal's value is the sequence of Unicode scalar values obtained after
    escape decoding. It may be empty (`""`).
47. Because source is valid UTF-8 and no escape can produce a surrogate code
    point, a String value is a well-formed sequence of Unicode scalar values.
    Unpaired UTF-16 surrogates cannot be constructed in the initial subset.
48. A literal whose encoded form exceeds the class-file constant limit is
    `ARA-JVM-0005` at emission time, not a lexical error.

## 9. Operators and punctuation

| Text | Token | Text | Token |
| --- | --- | --- | --- |
| `(` | LPAREN | `)` | RPAREN |
| `{` | LBRACE | `}` | RBRACE |
| `:` | COLON | `,` | COMMA |
| `;` | SEMI | `=` | ASSIGN |
| `==` | EQ | `!=` | NE |
| `<` | LT | `<=` | LE |
| `>` | GT | `>=` | GE |
| `+` | PLUS | `-` | MINUS |
| `*` | STAR | `/` | SLASH |
| `%` | PERCENT | `!` | BANG |
| `&&` | ANDAND | `\|\|` | OROR |

(The `\|\|` spelling is Markdown table escaping for the two-character token
`||`; the token itself is two ASCII pipe characters.)

49. There is no `++`, `--`, `+=`, `-=`, `*=`, `/=`, `%=`, `?:`, `.`, `[`, `]`,
    `->`, `::`, `@`, `#`, `~`, `^`, `\`, or backtick. Each such character, when
    not part of a longer valid token, is `ARA-LEX-0103`.
50. Because maximal munch applies, `a=-1` is `IDENT` `ASSIGN` `MINUS`
    `INT_LITERAL` and is the same token stream as `a = -1`.
51. Because maximal munch applies, `a&&&b` is `IDENT` `ANDAND` then
    `ARA-LEX-0103` at the second `&`.

## 10. Statement termination (automatic separation)

The lexer produces `NEWLINE` tokens. A post-lexing transformation turns some of
them into `SEMI` tokens. This transformation defines where a newline ends a
statement. It is defined on the token stream, independently of parse context, so
it is deterministic and testable.

52. Maintain a parenthesis depth, initially 0. `(` increments it; `)` decrements
    it. There are no other bracket kinds. A `)` at depth 0 does not decrement
    below zero: the counter stays at 0, and the token is preserved unchanged so
    the parser reports `ARA-PARSE-0117`. The separation pass never reports an
    error itself; it only classifies line breaks.
53. A `NEWLINE` is converted to a single `SEMI` if and only if both hold:
    a. parenthesis depth is 0 at that point, and
    b. the immediately preceding token is one of:
       `IDENT`, `INT_LITERAL`, `STRING_LITERAL`, `true`, `false`, `)`, `}`, or
       `return`.
    `SEMI` is deliberately **not** a trigger: a line that already ended with an
    explicit `;` must not gain a second `SEMI` from its line feed.
54. Consecutive `NEWLINE` tokens in a run produce at most one `SEMI`. A `NEWLINE`
    that does not meet the condition is discarded. Consequently there is exactly
    one `SEMI` between two statements whether the author used a line feed, an
    explicit `;`, or `;` followed by a line feed.
55. A `NEWLINE` inside parentheses is always discarded.
56. The tokens `{`, `,`, `:`, `=`, every binary operator, every unary operator,
    and the keywords `fun`, `val`, `var`, `if`, `else` never trigger
    termination.
57. An explicit `;` is always a `SEMI`. The grammar requires exactly one `SEMI`
    token between two statements or two top-level declarations; consecutive `;`
    tokens are therefore a parse error (`ARA-PARSE-0100` at the second `;`), not
    redundant separators. This is deliberate: it prevents an accidental second
    statement from being silently absorbed, and it keeps the grammar LL(1).
58. `SEMI` tokens are separators only. There is no empty statement. A single
    `SEMI` may appear after the last statement of a block or after the last
    top-level function; it may not appear before the first statement, before the
    first function, or twice in a row. A `SEMI` is required between two
    statements or two declarations; `syntax.ebnf` §3 items 6 and 6a state the
    consequences.

Consequences, each normative:

59. A block-opening `{` that follows a `)` MUST appear on the same line as that
    `)`, and a return type after `)` MUST begin on the same line as that `)`.
    Otherwise rule 53 inserts `SEMI` after `)` and the parser reports
    `ARA-PARSE-0107`.
60. `else` MUST appear on the same line as the `}` that closes the then-block.
    Otherwise rule 53 inserts `SEMI` after `}` and the parser reports
    `ARA-PARSE-0108`.
61. `return` at the end of a line is a bare `return`; the next line starts a new
    statement. `return` newline `1` is a bare return followed by a statement that
    begins with `1`. Because `syntax.ebnf` permits only declarations, assignments,
    calls, returns, conditionals, and blocks as statements, that statement is a
    parse error (`ARA-PARSE-0109`), not a silent `return 1`.
62. A binary operator at the end of a line continues the expression:
    `1 +` newline `2` is `1 + 2`. A binary operator at the *start* of a line does
    not continue the previous statement: `return 1` newline `- 2` is a complete
    `return 1` followed by a statement beginning with `-`, which is a parse error
    (`ARA-PARSE-0109`).
63. A newline after `:` or `=` or `,` or `(` does not terminate anything, so
    declarations, parameter lists, and argument lists may span lines.

## 11. Lexical diagnostics

Severity is `ERROR` for every diagnostic in the initial subset. The primary span
is the offending text unless stated otherwise.

| Code | Condition | Primary span |
| --- | --- | --- |
| `ARA-LEX-0001` | Invalid UTF-8 byte sequence | First invalid byte, mapped to the nearest text offset |
| `ARA-LEX-0101` | Character not permitted outside string/comment | That character |
| `ARA-LEX-0102` | Invalid identifier (reserved, unused; see note) | The identifier candidate |
| `ARA-LEX-0103` | Character that starts no token | That character |
| `ARA-LEX-0201` | Unterminated string literal at end of file | The opening `"` |
| `ARA-LEX-0202` | Invalid escape sequence | The backslash and following character |
| `ARA-LEX-0203` | Raw line break in a string literal | The line break |
| `ARA-LEX-0204` | Integer literal value above 2147483648 | The literal |
| `ARA-LEX-0205` | Leading zero in a multi-digit integer literal | The literal |
| `ARA-LEX-0206` | Forbidden control character in a string literal | That character |
| `ARA-LEX-0301` | Unterminated block comment | The opening `/*` |
| `ARA-LEX-0401` | Reserved word for a deferred feature | The word |

Note on `ARA-LEX-0102`: because identifiers are ASCII-only, a non-ASCII
identifier candidate is already `ARA-LEX-0101`. `ARA-LEX-0102` is reserved for
implementations that choose to scan an identifier candidate before validating its
characters; it is not required to be produced by a conforming implementation and
therefore MUST NOT be used in conformance tests. The code is listed to keep the
range documented and to prevent reuse.

Related spans: `ARA-LEX-0001` and `ARA-LEX-0201` SHOULD include a note indicating
the file may be truncated or binary.

## 12. Worked token examples

Each case gives source, then the token sequence with `SEMI` shown only where §10
produces one.

```text
val answer: Int = 40 + 2
```
`val` `IDENT(answer)` `:` `Int` `=` `INT_LITERAL(40)` `+` `INT_LITERAL(2)` `SEMI`
(the final `SEMI` comes from the line feed if one is present)

```text
fun main() {
    println("hi")
}
```
`fun` `IDENT(main)` `(` `)` `{` `IDENT(println)` `(` `STRING_LITERAL(hi)` `)`
`SEMI` `}` — the `SEMI` after `)` comes from the line feed after it; the closing
`}` is followed by end of file or a `SEMI` if a line feed follows.

```text
val x: Int = 1 +
    2
```
`val` `IDENT(x)` `:` `Int` `=` `INT_LITERAL(1)` `+` `INT_LITERAL(2)` `SEMI` — the
line feed after `+` is discarded by rule 56.

```text
return 1
- 2
```
`return` `INT_LITERAL(1)` `SEMI` `-` `INT_LITERAL(2)` `SEMI` — rule 53 fires after
`1`; the `- 2` line is a parse error under rule 62.

```text
fun f(
    a: Int,
    b: String
): Unit {
}
```
No `SEMI` after `Int`, `,`, or `String`, because parenthesis depth is 1. After
`)` the next token is `:`, so no termination occurs and `: Unit {` continues the
declaration.

## 13. Non-goals

This contract does not define: Unicode identifiers, Unicode normalization,
Unicode escapes, character literals, raw or multi-line strings, string
interpolation, numeric separators, hex/binary/octal literals, floating-point
literals, comment preservation, or doc-comment semantics. None of these may be
inferred from Kotlin or Java behavior.
