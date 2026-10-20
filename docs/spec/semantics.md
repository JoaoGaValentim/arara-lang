# Arara Semantic Contract

Status: **DRAFT — D001. Submitted for owner review. Not frozen.**
Scope: static semantics, evaluation semantics, the JVM mapping, and consolidated
diagnostics for the initial subset.
Related: `lexical.md`, `syntax.ebnf`, `conformance-plan.md`,
`docs/architecture.md`, `docs/adr/0001-jvm-target.md`.

Source extension: `.ara` is **PROPOSED** by `D001-06` and is not ratified;
`OQ-8` remains open. A different extension would change only file discovery and
the unit-name rule in §1.1, not the language semantics.

Every rule below is normative for D002–D004. No rule may be delegated implicitly
to Java, Kotlin, or JVM behavior; where a rule matches the JVM, that is stated and
tested explicitly.

## 1. Program model

1.1. A **compilation unit** is the normalized text of one source file with the
proposed extension `.ara` (`D001-06`, `OQ-8`). The unit's **name** is the file's
base name without that extension.

1.2. A unit name MUST match `[A-Za-z_][A-Za-z0-9_]*`, MUST not be a Java reserved
word, and MUST be unique within a compilation. "Java reserved word" means the
Java 21 keyword list plus `true`, `false`, and `null`. `_` matches the pattern but
is itself a Java keyword, so the keyword rule rejects it. A violation is
`ARA-SRC-0004`; a duplicate is `ARA-SRC-0003`. Java's *restricted identifiers*
that are not keywords (`var`, `yield`, `record`, `sealed`, `permits`, and the
module words) are not excluded by this rule: the JVM accepts a class with such a
name, which was verified under `-Xverify:all`, and `javac` refusal does not bind
a bytecode emitter. Whether to forbid them anyway is `OQ-10`.

1.3. A **program** is the set of units passed to one compiler invocation.

1.4. There are no packages, imports, or modules. All generated classes are in the
unnamed package.

1.4a. A unit with no function declarations is legal and emits a class with no
declared methods. It can never be called and can never be the entry unit. A
program consisting only of empty units is `ARA-NAME-0106`. Empty units are
otherwise accepted so that a unit's file can be staged before its functions are
written; they produce no diagnostics of their own.

1.5. The **entry point** is the unique function named `main` declared with no
parameters and return type `Unit`. The **entry unit** is the unit declaring it,
and the **entry class** is that unit's generated class. A unit name that equals a
function name is permitted: functions are members of the class, not classes.
A unit name that equals `java` or would collide with `java.lang` is not a
concern because there are no packages and generated classes are in the unnamed
package; the compiler MUST still reject a name that is a Java reserved word
(§1.2).

1.6. `main` MUST be `Unit`-returning with exactly zero parameters; otherwise
`ARA-NAME-0110`. A program without `main` is `ARA-NAME-0106`. A program with two
or more `main` declarations is impossible under §5.8, because duplicate function
names are `ARA-NAME-0101`; the duplicate error is reported for the second
declaration and `ARA-NAME-0106` is not also reported.

1.7. Every function is callable from every unit, including units that appear
later in the argument list. Declaration order is irrelevant.

1.7a. Diagnostic precedence in `main` checking: duplicate function names
(`ARA-NAME-0101`) are reported during function collection, before entry-point
validation. If a duplicate is reported, `ARA-NAME-0106` and `ARA-NAME-0110` are
not also reported for the same program. This keeps one defect to one primary
code.

1.8. Function names occupy one program-wide namespace. Value names (parameters
and locals) occupy a separate per-function namespace. See §5.3–§5.9.

## 2. Types

2.0. A `String` value is a finite sequence of Unicode scalar values, as produced
by the lexical rules. At runtime it is represented by `java.lang.String`, which
stores UTF-16 code units. Because no scalar value is a surrogate and no escape
can produce one (`lexical.md` §8 rules 40–47), the two representations are in
bijection for every value Arara can construct, and `equals` over UTF-16 code
units is therefore the same relation as equality over scalar sequences. This
equivalence is a theorem about the initial subset, not an assumption; if a future
milestone permits surrogate code units, it must restate both this rule and
§7.4.3.

2.1. The type universe is exactly:

| Type | Values | JVM representation |
| --- | --- | --- |
| `Int` | the 2^32 values of 32-bit two's complement | `int` (descriptor `I`) |
| `Boolean` | `true`, `false` | `boolean` (descriptor `Z`) |
| `String` | finite sequences of Unicode scalar values; never null | `java.lang.String` |
| `Unit` | the absence of a result; not a value | `void` (descriptor `V`) |

2.2. There are no other types. There is no `Long`, `Double`, `Char`, `Any`,
`Nothing`, array, nullable, generic, function, or user-defined type.

2.3. There are no subtypes. Assignability is type identity.

2.4. `Unit` is not a value type. It may appear only as a function result type.
It MUST NOT appear as a parameter type, local type, or expression type; violation
is `ARA-TYPE-0202`.

2.5. There is no `null` value. Every `String` value is a valid reference to a
`java.lang.String` instance. User code cannot observe a null reference.

2.6. There is no implicit conversion of any kind: not `Int` to `Boolean`, not
`Boolean` to `Int`, not `Int` to `String`, not `String` to `Int`. Any such
attempt is a type error, not a coercion.

## 3. Declarations

3.1. A function declaration introduces a function with a name, an ordered
parameter list, a return type, and a block body.

3.2. Parameter names MUST be unique within the declaration (`ARA-NAME-0105`).

3.3. A parameter has an explicit type from §2. Its type MUST NOT be `Unit`
(`ARA-TYPE-0202`).

3.4. The return type is the declared type after `:`, or `Unit` if omitted.

3.5. A local declaration has the form `val` or `var`, a name, an explicit type,
and an initializer expression. The initializer is mandatory; there is no
uninitialized local.

3.6. The declared type of a local MUST NOT be `Unit` (`ARA-TYPE-0202`).

3.7. The initializer's type MUST be identical to the declared type
(`ARA-TYPE-0201` at the initializer).

3.8. There are no top-level constants, global variables, or top-level declarations
of any other kind.

## 4. Scope, binding, and shadowing

4.1. A function body block is a scope. Every nested block is a scope.

4.2. A parameter is in scope for the entire function body block.

4.3. A local's scope begins immediately **after** its declaration statement and
extends to the end of the enclosing block.

4.4. Consequently, a local's initializer cannot refer to the local being
declared. `val x: Int = x` is `ARA-NAME-0102` because `x` is not yet in scope.

4.5. Two locals with the same name in the same scope are `ARA-NAME-0103`.

4.6. A local that has the same name as a local or parameter in an enclosing scope
is `ARA-NAME-0104`. **Shadowing is prohibited.** This is stricter than Kotlin and
is deliberate: it removes an entire class of ambiguity from diagnostics and
return-path reasoning.

4.7. Parameters participate in rule 4.6: a local may not shadow a parameter.

4.8. A bare block statement introduces a scope even though it declares nothing
of its own.

4.9. There is no temporal dead zone beyond rule 4.4 because every local is
initialized at its declaration.

## 5. Name resolution

5.1. Name resolution runs after parsing and before type checking. If it reports
any error, type checking does not run.

5.2. Function names resolve in the program-wide function namespace.

5.3. Resolution uses two separate namespaces: the **function namespace**
(program functions plus the builtin `println`) and the **value namespace**
(parameters and locals of the enclosing function). A name is looked up in the
namespace required by its syntactic position, never in the other one first.

5.4. A **call** is syntactically `IDENT ( ... )`. Its name is looked up in the
function namespace only. If it is not found there, the call is `ARA-NAME-0102`.
Because lookup is namespace-separated, a local or parameter named `f` never
prevents `f()` from calling the function `f`, and never makes a local callable.

5.5. A **bare identifier** in expression position is looked up in the value
namespace only. If it is not found there, but a function or `println` has that
name, it is `ARA-NAME-0109` (functions are not values). If nothing has that name,
it is `ARA-NAME-0102`.

5.6. An assignment statement target MUST resolve to a parameter or local.
Otherwise `ARA-NAME-0102`.

5.7. The builtin name `println` is reserved. A user function named `println` is
`ARA-NAME-0108`. A local or parameter named `println` is also `ARA-NAME-0108`,
because value and function namespaces are separate but the builtin name is
reserved in both.

5.8. Function names MUST be unique program-wide (`ARA-NAME-0101`). There is no
overloading.

5.9. A local or parameter MAY share a name with a user function. Rules 5.4 and
5.5 define the meaning unambiguously: `f()` is always the function and `f` alone
is always the local.

5.10. `ARA-NAME-0111` (a value is not callable) is not reachable under this
resolution order, because a call never consults the value namespace. The code is
reserved and MUST NOT be produced or used in conformance tests; see
`lexical.md` §11 for the same treatment of `ARA-LEX-0102`.

## 6. The builtin `println`

6.1. `println` is the only output facility. It is a builtin, not a user
declaration, and it cannot be shadowed, redeclared, or assigned.

6.2. It has exactly three signatures, resolved by the exact static type of its
single argument:

| Argument type | Emitted call | Result |
| --- | --- | --- |
| `Int` | `java/io/PrintStream.println(I)V` | `Unit` |
| `Boolean` | `java/io/PrintStream.println(Z)V` | `Unit` |
| `String` | `java/io/PrintStream.println(Ljava/lang/String;)V` | `Unit` |

6.3. The receiver is `System.out`, loaded with `getstatic java/lang/System.out`.

6.4. `println` takes exactly one argument. Zero or two or more arguments is
`ARA-TYPE-0206`. A single argument whose type is `Unit` is `ARA-TYPE-0205`, not
`0206` and not `0215`: the arity is correct and the defect is using a `Unit`
result as a value.

6.5. The observable output is exactly what the named `PrintStream.println`
overload produces: the decimal representation of the `int`, `true` or `false` for
a `boolean`, or the string's characters, followed by the **platform line
separator** of the JVM that runs the program. Because that is host-dependent,
conformance tests that compare bytes MUST run on a recorded platform and MUST fix
the expected separator for it; a test harness may instead compare after
normalizing `\r\n` to `\n`, and if it does, it MUST state that it normalizes.

6.6. The encoding used for non-ASCII characters is the JVM's stdout encoding and
is **not** part of this contract. Conformance tests MUST restrict output
assertions to ASCII text; any test that asserts non-ASCII output bytes is
non-conforming.

6.7. Because `println` returns `Unit`, using it as an expression value is
`ARA-TYPE-0205`; using it as a statement is valid.

## 7. Expressions

### 7.1 Evaluation order

7.1.1. Evaluation is strictly left-to-right.

7.1.2. For a binary operator `a op b`: evaluate `a` fully, then `b`, then apply
the operator.

7.1.3. For `&&` and `||`: evaluate the left operand; if the result determines the
outcome (`false` for `&&`, `true` for `||`), the right operand is not evaluated
and produces no effects. Otherwise evaluate the right operand.

7.1.4. For a call `f(a, b)`: evaluate arguments left to right, then invoke.

7.1.5. For `if (c) A else B`: evaluate `c`, then execute exactly one of `A` or
`B`.

7.1.6. For a block: execute statements in order until one transfers control.

7.1.7. A local declaration evaluates its initializer, then binds the name.

7.1.8. The only effects are `println` output, assignment to a `var` local,
non-termination, and exceptional termination. Left-to-right order is observable
through the first two: a called function may print before a later argument's
function prints, and assignment is sequenced after its initializer. §7.1 is
therefore testable, not merely declaratory; `conformance-plan.md` §5 gives the
fixture.

### 7.2 Literals

7.2.1. `INT_LITERAL` has type `Int`.

7.2.2. An integer literal with mathematical value `v` where `0 <= v <= 2147483647`
denotes `v`.

7.2.3. The token `2147483648` is permitted by the lexer but has no `Int` value.
It is valid **only** as the immediate operand of unary `-`, where it denotes
`-2147483648`. In every other position it is `ARA-TYPE-0204` at the literal.

7.2.4. "Immediate operand" means the literal is the direct operand of the unary
`-` in the parsed tree, with no intervening parentheses. `-2147483648` is valid;
`-(2147483648)` is `ARA-TYPE-0204`; `2147483648` alone is `ARA-TYPE-0204`;
`0 - 2147483648` is `ARA-TYPE-0204`.

7.2.5. `STRING_LITERAL` has type `String`.

7.2.6. `true` and `false` have type `Boolean`.

7.2.7. A parenthesized expression has the type of its inner expression. Parentheses
do not create a value, a scope, or an evaluation-order change.

### 7.3 Operators

Precedence, from lowest binding to highest:

| Level | Operators | Associativity |
| --- | --- | --- |
| 1 | `\|\|` | left |
| 2 | `&&` | left |
| 3 | `==` `!=` | left |
| 4 | `<` `<=` `>` `>=` | left |
| 5 | `+` `-` (binary) | left |
| 6 | `*` `/` `%` | left |
| 7 | unary `-`, unary `!` | right (prefix) |
| 8 | primary, call, parentheses | — |

7.3.1. Typing rules:

| Form | Required operand types | Result |
| --- | --- | --- |
| `a + b`, `a - b`, `a * b`, `a / b`, `a % b` | `Int`, `Int` | `Int` |
| `-a` | `Int` | `Int` |
| `a < b`, `a <= b`, `a > b`, `a >= b` | `Int`, `Int` | `Boolean` |
| `a == b`, `a != b` | `Int`,`Int` or `Boolean`,`Boolean` or `String`,`String` | `Boolean` |
| `a && b`, `a \|\| b` | `Boolean`, `Boolean` | `Boolean` |
| `!a` | `Boolean` | `Boolean` |

7.3.2. Any other operand combination is `ARA-TYPE-0209` at the operator, with a
related span at the offending operand.

7.3.3. `+` does not concatenate strings. `"a" + "b"` is `ARA-TYPE-0209`.

7.3.4. Strings support only `==` and `!=`; relational operators on strings are
`ARA-TYPE-0209`.

7.3.5. Chained comparisons such as `a < b < c` parse left-associatively as
`(a < b) < c` and are rejected because the left operand of `<` is `Boolean`
(`ARA-TYPE-0209`).

### 7.4 Equality

7.4.1. `Int` equality compares the 32-bit values.

7.4.2. `Boolean` equality compares truth values.

7.4.3. `String` equality is **structural**: `a == b` is true if and only if `a`
and `b` contain the same sequence of UTF-16 code units. It is emitted as
`java/lang/String.equals`, and `!=` as the negation of that result.

7.4.4. Reference identity is never the meaning of `==` for `String`. Because
every `String` is non-null, `equals` cannot throw `NullPointerException` from
generated equality code.

7.4.5. `==` and `!=` are not overloadable and have no user-defined meaning.

### 7.5 Calls

7.5.1. A call names a program function or `println`. There are no function
values, method references, or dynamic dispatch.

7.5.2. The number of arguments MUST equal the number of parameters, otherwise
`ARA-TYPE-0206` at the argument list.

7.5.3. Each argument's type MUST be identical to the corresponding parameter type,
otherwise `ARA-TYPE-0215` at that argument.

7.5.4. A call to a `Unit`-returning function is a valid **statement**
(`IdentStatement`) and its result is discarded.

7.5.5. A call to a non-`Unit` function used as a statement is also valid; the
result is discarded and has no observable effect.

7.5.6. A call whose result is `Unit` MUST NOT be used where a value is required;
that is `ARA-TYPE-0205`.

7.5.7. Recursion, including mutual recursion, is permitted and is not bounded by
the compiler. Exceeding the JVM stack raises `StackOverflowError`, which is not
caught; see §11.6.

### 7.6 Conditions

7.6.1. The condition of `if` MUST have type `Boolean` (`ARA-TYPE-0211`).

7.6.2. There is no truthiness. `Int` and `String` are not conditions.

## 8. Statements and control flow

8.1. Statement forms are exactly: local declaration, assignment or call,
`return`, `if`, and block. See `syntax.ebnf` §2.

8.2. An assignment `x = e` requires:
- `x` resolves to a local or parameter (`ARA-NAME-0102`);
- if `x` is a parameter, `ARA-TYPE-0207`;
- if `x` is a `val`, `ARA-TYPE-0203`;
- otherwise `e`'s type MUST be identical to `x`'s declared type
  (`ARA-TYPE-0201`).

8.3. An assignment evaluates `e` first, then stores. The stored value is the
evaluated value, unchanged.

8.4. A block executes its statements in order. A block with no statements
completes normally.

8.5. `if (c) A else B` evaluates `c` left-to-right first; if the value is `true`
it executes `A` and not `B`; otherwise it executes `B` and not `A`. With no
`else`, a false condition executes nothing and the `if` completes normally. An
`if` is a statement and never yields a value (`syntax.ebnf` §3 item 3).

8.6. `return` transfers control to the caller. Return has no value in the
enclosing expression grammar; the `return` statement is only a statement form:
- in a `Unit` function, `return` with no expression is valid; `return e` is
  `ARA-TYPE-0214`;
- in a non-`Unit` function, `return e` is required and `e`'s type MUST be
  identical to the declared return type (`ARA-TYPE-0212`); bare `return` is
  `ARA-TYPE-0213`.

8.7. A `Unit` function that reaches the end of its body returns normally to the
caller.

8.8. A non-`Unit` function MUST return on every reachable path; otherwise
`ARA-TYPE-0208` at the function name.

8.9. Return-path analysis is purely syntactic. Conditions are never evaluated at
compile time; `if (true) { return 1 }` does not prove a return.

Definition. For a block `B = { s1 ... sn }`, statements are classified left to
right, in one pass:

```text
reachable := true
returns   := false
for i := 1 to n:
    reachable(si) := reachable
    if reachable(si) and alwaysReturns(si):
        returns   := true
        reachable := false
alwaysReturns(B) := returns
```

`alwaysReturns(s)` for a non-block statement:

- `return` (with or without an expression): `true`.
- `if (c) A else B`: `alwaysReturns(A) and alwaysReturns(B)`.
- `if (c) A` without `else`: `false`.
- a local declaration, assignment, or call statement: `false`.

Consequence 1: an unreachable statement never contributes to `returns`, so
`{ return 1; val x: Int = 2 }` has `alwaysReturns = true` and reports
`ARA-TYPE-0210` for `val x: Int = 2` only.

Consequence 2: `{ if (c) { return 1 } else { return 0 }; return 2 }` has
`alwaysReturns = true` for the `if`, so `return 2` is unreachable and reports
`ARA-TYPE-0210`; the function itself satisfies §8.8.

Consequence 3: the pass is total and deterministic; no fixed point or
order-dependent iteration is required.

8.10. §8.8 is satisfied when `alwaysReturns(body)` is `true` for the function
body block.

8.11. A statement that is not reachable is `ARA-TYPE-0210` at its first token.
This is an error, not a warning, so that dead control flow is never silently
ignored. A function body consisting only of `return` is reachable and valid.

8.12. There is no `break`, `continue`, `throw`, `try`, or loop statement. Those
keywords are reserved and rejected lexically (`ARA-LEX-0401`).

## 9. Integer semantics

9.1. `Int` is a 32-bit two's complement signed integer. The value set is
`-2147483648` through `2147483647`.

9.2. Literal range is defined in §7.2.2–§7.2.4.

9.3. `+`, `-` (binary and unary), and `*` are computed modulo 2^32, exactly
matching JVM `iadd`, `isub`, `ineg`, and `imul`. Overflow wraps; it never traps and
is never a compile-time error.

9.4. `/` is integer division truncated toward zero, matching JVM `idiv`. Examples:
`7 / 2 == 3`, `-7 / 2 == -3`, `7 / -2 == -3`, `-7 / -2 == 3`.

9.5. `%` is the remainder consistent with §9.4: `a % b == a - (a / b) * b`,
matching JVM `irem`. The result takes the sign of the dividend. Examples:
`7 % 2 == 1`, `-7 % 2 == -1`, `7 % -2 == 1`, `-7 % -2 == -1`.

9.6. Division or remainder with a zero right operand raises
`java.lang.ArithmeticException` at runtime. It is not undefined behavior and is
not a compile-time error, even when the zero is a literal. The exception is not
caught and propagates per §11.6.

9.7. `-2147483648 / -1` overflows. JVM `idiv` returns `-2147483648`; Arara
specifies the same result. This is the only division that does not raise on a
zero divisor or fit in the mathematical range, and it is explicitly specified
rather than inherited.

9.8. There are no unsigned operations, shifts, bitwise operators, or `Long`
values.

## 10. Unsound or unsupported constructs

10.1. Any construct outside this contract MUST be rejected with a diagnostic. It
MUST NOT be accepted with unspecified or "whatever the JVM does" behavior.

10.2. Reserved keywords for deferred features produce `ARA-LEX-0401` and stop
lexing of that unit; see `lexical.md` §6.

10.3. Syntax that is well-formed in Kotlin but absent here is rejected by the
parser, not silently reinterpreted. Examples: expression-bodied functions,
single-statement `if` bodies, string templates, `when`, `while`, lambda syntax,
`?.`, `!!`, `is`, `as`, ranges, and `by`.

10.4. There is no mechanism for the user to declare a type, package, annotation,
operator, or external function.

10.5. There is no escape from the type system: no casts, no reflection, no
`Any`, and no way to construct a `String` with an unpaired surrogate.

## 11. JVM compatibility contract

11.1. **Classfile version.** Every emitted class has major version 65 and minor
version 0. No other version is emitted.

11.2. **Class mapping.** Each compilation unit `Foo.ara` emits exactly one class
`Foo` in the unnamed package, with access flags `ACC_PUBLIC | ACC_FINAL |
ACC_SUPER`, superclass `java/lang/Object`, no interfaces, and no fields.

11.3. **Methods.** Every Arara function becomes a `public static` method with the
same name and descriptor. A function whose name equals its unit's class name is
legal: a method and its class may share a name in the JVM, and there is no
`<init>` to collide with because classes have no constructors in the initial
subset. This case is explicit so that no implementation invents a prohibition:

| Arara type | Descriptor |
| --- | --- |
| `Int` | `I` |
| `Boolean` | `Z` |
| `String` | `Ljava/lang/String;` |
| `Unit` (result only) | `V` |

There are no instance methods, no `<init>`, no `<clinit>`, no synthetic members,
and no overloading, so no two methods share a name and descriptor.

11.4. **Cross-unit calls.** A call to a function declared in unit `U` is emitted
as `invokestatic U/<name><descriptor>`. The only reference types that can appear
in a descriptor are `java/lang/String` and the generated unit classes, so the
closed frame-computation table in §11.11 stays closed for cross-unit calls.

11.5. **Entry point.** The entry unit's class additionally declares:

```text
public static void main(java.lang.String[] args)
```

with descriptor `([Ljava/lang/String;)V`. Its body invokes
`invokestatic <EntryClass>.main()V` and returns. The Arara-level `main` is a
separate method `main()V`, so the two descriptors never collide. The bridge
ignores `args`; no `null` check or argument validation is performed.

11.6. **Exceptions.** Generated code throws only:
- `java.lang.ArithmeticException` from `idiv`/`irem` with a zero divisor
  (§9.6);
- `java.lang.StackOverflowError` from unbounded recursion;
- `java.lang.OutOfMemoryError` from the JVM under memory exhaustion.

No exception handler is emitted in the initial subset. An uncaught exception
propagates out of `main(String[])`, and the JVM's default handler prints a stack
trace to stderr and exits with status 1. User programs have no way to observe or
suppress exceptions.

11.7. **String equality.** `a == b` emits `invokevirtual
java/lang/String.equals(Ljava/lang/Object;)Z`; `!=` emits the same followed by
`ifne`/negation. Structural equality is therefore exactly UTF-16 code-unit
sequence equality, as specified in §7.4.3.

11.8. **Output.** `println` emits `getstatic java/lang/System.out` and one of the
three `PrintStream.println` calls in §6.2. No other I/O is emitted.

11.9. **Stack and locals.** The backend emits stack map frames valid under the
JDK 21 verifier. `max_stack` and `max_locals` are computed, not guessed. Local
slot 0 holds the parameter (or is unused for zero-parameter methods), and no slot
is read before it is written, because every local declaration has an initializer
(§3.5).

11.10. **No uninitialized references.** Because no `<init>` is emitted, no
`uninitialized` or `uninitializedThis` verification type can appear.

11.11. **Frame computation.** If ASM's `COMPUTE_FRAMES` is used, the
`getCommonSuperClass` implementation MUST NOT load application classes and MUST
answer from a closed table: `java/lang/Object`, `java/lang/String`, and every
generated unit class, whose only common supertypes are `java/lang/Object`. Any
emission path that can merge other reference types is out of contract for the
initial subset. A generated class may hold a `String` in a local, but never a
reference to a generated unit class, so the table is not consulted for a
user-defined class in the initial subset; the rule exists so that a later
milestone cannot silently load classes from the compiler's own classloader.

11.12. **Verification evidence.** Emitted bytes are verified twice before being
treated as evidence:
1. `CheckClassAdapter.verify` with data-flow analysis on the exact bytes that will
   be written;
2. loading and execution in a JVM started with `-Xverify:all`.

Failure of either is `ARA-JVM-0002` and is a test failure, never a warning.

11.13. **Determinism.** The same input and compiler version produce byte-identical
class files. No timestamp, random identifier, hash-order-dependent constant pool
entry, or absolute path is embedded.

11.14. **Limits.** A method whose bytecode exceeds 65535 bytes is `ARA-JVM-0006`.
A function with more than 255 parameter slots is `ARA-JVM-0007`. A string
constant whose modified UTF-8 encoding exceeds 65535 bytes is `ARA-JVM-0005`.

11.15. **Java calling convention.** All generated methods are `public static` and
use ordinary JVM descriptors, so a Java class could in principle call them. This
is **not** an interoperability guarantee: it is a statement about the calling
convention only. Java interoperability is D005, and no compatibility claim may be
made before D005's gates pass.

11.16. **Not in the initial subset.** Packages, instance methods, constructors,
fields, interfaces, annotations, generic signatures, exceptions declared in
`throws` clauses, module descriptors, `invokedynamic`, `invokeinterface`,
synchronization, and native methods are not emitted.

## 12. Diagnostic index

Severity is `ERROR` unless stated. Spans are the primary span; related spans are
listed where the contract requires them.

### 12.1 Source and driver

| Code | Condition |
| --- | --- |
| `ARA-SRC-0001` | No input files |
| `ARA-SRC-0002` | Input file cannot be read |
| `ARA-SRC-0003` | Duplicate compilation-unit name |
| `ARA-SRC-0004` | Unit name is not a valid class name |

### 12.2 Lexical

Defined in `lexical.md` §11: `ARA-LEX-0001`, `0101`, `0102` (reserved, never
produced), `0103`, `0201`–`0206`, `0301`, `0401`. The full table there is
normative.

### 12.3 Syntactic

Defined in `syntax.ebnf` §7: `ARA-PARSE-0100`–`0117`, plus the produced `NOTE`
code `ARA-PARSE-0199`. The full table there is normative.

### 12.4 Name resolution

| Code | Condition |
| --- | --- |
| `ARA-NAME-0101` | Duplicate function name in the program |
| `ARA-NAME-0102` | Unresolved name |
| `ARA-NAME-0103` | Duplicate local in the same scope |
| `ARA-NAME-0104` | Local shadows an enclosing local or parameter |
| `ARA-NAME-0105` | Duplicate parameter name |
| `ARA-NAME-0106` | Program has no `main` |
| `ARA-NAME-0108` | User declaration named `println` |
| `ARA-NAME-0109` | Function name used as a value |
| `ARA-NAME-0110` | `main` does not have zero parameters and `Unit` result |
| `ARA-NAME-0107` | Reserved; never defined and never produced. The gap is intentional so that adding a name-resolution code cannot silently reuse a number. |
| `ARA-NAME-0111` | Reserved, unreachable under §5; MUST NOT be produced |

### 12.5 Type checking

| Code | Condition |
| --- | --- |
| `ARA-TYPE-0201` | Type mismatch in a local initializer or assignment |
| `ARA-TYPE-0202` | `Unit` used as a parameter, local, or value type |
| `ARA-TYPE-0203` | Assignment to a `val` |
| `ARA-TYPE-0204` | Integer literal `2147483648` outside direct unary `-` |
| `ARA-TYPE-0205` | A `Unit` result is used as a value |
| `ARA-TYPE-0206` | Wrong number of arguments |
| `ARA-TYPE-0207` | Assignment to a parameter |
| `ARA-TYPE-0208` | A non-`Unit` function may reach its end without returning |
| `ARA-TYPE-0209` | Invalid operand types for an operator |
| `ARA-TYPE-0210` | Unreachable statement |
| `ARA-TYPE-0211` | `if` condition is not `Boolean` |
| `ARA-TYPE-0212` | Returned expression type differs from the declared result |
| `ARA-TYPE-0213` | Bare `return` in a non-`Unit` function |
| `ARA-TYPE-0214` | `return` with a value in a `Unit` function |
| `ARA-TYPE-0215` | Argument type differs from the parameter type |

### 12.6 JVM emission and verification

| Code | Condition |
| --- | --- |
| `ARA-JVM-0001` | Internal emission failure (never a source-level condition) |
| `ARA-JVM-0100` | Reserved for a future compiler-internal invariant failure; not produced in the initial subset |
| `ARA-JVM-0002` | Emitted class failed structural analysis or JVM verification |
| `ARA-JVM-0005` | String constant exceeds the class-file limit |
| `ARA-JVM-0006` | Method bytecode exceeds 65535 bytes |
| `ARA-JVM-0007` | More than 255 parameter slots |

### 12.7 Determinism and ordering

12.7.1. Diagnostics are sorted by primary span offset ascending, then by code
ascending, then by insertion order.

12.7.2. A stage reports at most one diagnostic per source span.

12.7.3. Lexical errors suppress parsing. Parse errors suppress name resolution.
Name-resolution errors suppress type checking for the affected function. Type
errors suppress lowering and emission for the affected unit. A unit with any
error produces no class file.

12.7.4. No diagnostic is ever downgraded to a warning to allow emission.

## 13. Worked semantic examples

13.1 Valid, returns `42`:

```arara
fun answer(): Int {
    val x: Int = 40
    val y: Int = 2
    return x + y
}

fun main() {
    println(answer())
}
```

13.2 Invalid, `ARA-TYPE-0209`, because `+` does not concatenate strings:

```arara
fun main() {
    val a: String = "ar" + "ara"
}
```

The valid form uses two equal string literals and compares them structurally;
this prints `true`:

```arara
fun main() {
    val a: String = "arara"
    val b: String = "arara"
    println(a == b)
}
```

13.3 Invalid, `ARA-TYPE-0203`:

```arara
fun main() {
    val x: Int = 1
    x = 2
}
```

13.4 Invalid, `ARA-TYPE-0208`, because the `if` has no `else`:

```arara
fun f(c: Boolean): Int {
    if (c) {
        return 1
    }
}
```

13.5 Valid:

```arara
fun f(c: Boolean): Int {
    if (c) {
        return 1
    } else {
        return 0
    }
}
```

13.5a Invalid, `ARA-TYPE-0210` at `println(2)`, because the preceding `if`
`alwaysReturns`:

```arara
fun f(c: Boolean): Int {
    if (c) {
        return 1
    } else {
        return 0
    }
    println(2)
    return 2
}
```

13.6 Invalid, `ARA-TYPE-0204`: `val x: Int = 2147483648`. Valid:
`val x: Int = -2147483648`.

13.7 Invalid, `ARA-NAME-0104` (shadowing):

```arara
fun main() {
    val x: Int = 1
    if (x > 0) {
        val x: Int = 2
    }
}
```

13.8 Invalid, `ARA-PARSE-0108` (else must be on the same line as `}`):

```arara
fun main() {
    if (true) {
        println(1)
    }
    else {
        println(2)
    }
}
```

13.9 Runtime behavior, not a compile error:

```arara
fun main() {
    println(1 / 0)
}
```
Compiles; at runtime, `main` throws `ArithmeticException`, the JVM prints a stack
trace to stderr, and the process exits with status 1.

## 14. Non-goals

This contract does not specify: exceptions as a language feature, nullability,
generics, closures, classes, modules, concurrency, or optimization. It does not
claim compatibility with Kotlin's or Java's semantics for any construct. Where a
rule matches the JVM, the rule is stated here and must be tested directly.
