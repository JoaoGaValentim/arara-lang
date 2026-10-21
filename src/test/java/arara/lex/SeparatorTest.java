package arara.lex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static arara.lex.LexTestSupport.kinds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Automatic statement separation: {@code lexical.md} §10 rules 52–63 and the
 * Separation row of {@code conformance-plan.md} §3.
 *
 * <p>The pass is tested in isolation from the parser. Where the contract states a
 * parser consequence, the token shape is asserted here and the diagnostic belongs
 * to D002-B.
 */
class SeparatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"x", "1", "\"s\"", "true", "false", ")", "}", "return"})
    @DisplayName("each of the eight trigger tokens yields a SEMI at depth zero")
    void triggers(String trigger) {
        List<TokenKind> result = kinds(trigger + "\n");
        assertEquals(TokenKind.SEMI, result.get(result.size() - 2),
                "a SEMI must be inserted after " + trigger);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", ",", ":", "=", "+", "-", "*", "/", "%", "&&", "||", "!",
            "fun", "val", "var", "if", "else", "<", ">", "<=", ">=", "==", "!=", "("})
    @DisplayName("no other token triggers termination")
    void nonTriggers(String token) {
        List<TokenKind> result = kinds(token + "\n");
        assertTrue(result.stream().noneMatch(k -> k == TokenKind.SEMI),
                token + " must not trigger a SEMI");
    }

    @Test
    @DisplayName("a newline inside parentheses is always discarded")
    void insideParentheses() {
        List<TokenKind> result = kinds("f(\n1\n)");
        assertTrue(result.stream().noneMatch(k -> k == TokenKind.SEMI));
    }

    @Test
    @DisplayName("a newline after the closing paren of a call is at depth zero and terminates")
    void afterCall() {
        List<TokenKind> result = kinds("f(1)\n");
        assertEquals(TokenKind.SEMI, result.get(result.size() - 2));
    }

    @Test
    @DisplayName("a run of newlines yields at most one SEMI")
    void consecutiveNewlines() {
        List<TokenKind> result = kinds("x\n\n\n\n");
        long semis = result.stream().filter(k -> k == TokenKind.SEMI).count();
        assertEquals(1, semis);
    }

    @Test
    @DisplayName("an explicit semicolon followed by a line feed yields exactly one SEMI")
    void explicitSemiThenNewline() {
        // lexical.md §53: SEMI is deliberately not a trigger.
        List<TokenKind> result = kinds("x;\n");
        long semis = result.stream().filter(k -> k == TokenKind.SEMI).count();
        assertEquals(1, semis);
    }

    @Test
    @DisplayName("a line that already ended with a semicolon gains no second one")
    void noDoubleSemi() {
        assertEquals(List.of(TokenKind.IDENT, TokenKind.SEMI, TokenKind.EOF), kinds("x;\n"));
    }

    @Test
    @DisplayName("a binary operator at end of line continues the expression")
    void operatorAtLineEnd() {
        // lexical.md §62: '1 +' newline '2' is one expression.
        List<TokenKind> result = kinds("1 +\n2");
        assertTrue(result.stream().noneMatch(k -> k == TokenKind.SEMI),
                "no SEMI may appear after a trailing binary operator");
    }

    @Test
    @DisplayName("an operator at the start of a line does not continue the previous statement")
    void operatorAtLineStart() {
        // The newline after '1' terminates; the '-' begins a new statement.
        List<TokenKind> result = kinds("return 1\n- 2");
        assertEquals(TokenKind.SEMI, result.get(2));
        assertEquals(TokenKind.MINUS, result.get(3));
    }

    @Test
    @DisplayName("a newline after a colon, equals, comma or open paren terminates nothing")
    void noTerminationAfterContinuationTokens() {
        assertTrue(kinds("f(\n").stream().noneMatch(k -> k == TokenKind.SEMI));
        assertTrue(kinds("val x =\n1").stream().noneMatch(k -> k == TokenKind.SEMI));
        assertTrue(kinds("a,\nb").stream().noneMatch(k -> k == TokenKind.SEMI));
        assertTrue(kinds("a:\nInt").stream().noneMatch(k -> k == TokenKind.SEMI));
    }

    @Test
    @DisplayName("a closing paren at depth zero is preserved and never drives the counter negative")
    void strayClosingParenPreserved() {
        // Depth stays 0, so the following newline still terminates; the token itself
        // survives so the parser can report ARA-PARSE-0117.
        List<TokenKind> result = kinds(")\n");
        assertEquals(TokenKind.RPAREN, result.get(0));
        assertEquals(TokenKind.SEMI, result.get(1));
    }

    @Test
    @DisplayName("two stray closing parens do not accumulate negative depth")
    void twoStrayParens() {
        List<TokenKind> result = kinds("))\n");
        assertEquals(TokenKind.SEMI, result.get(result.size() - 2));
    }

    @Test
    @DisplayName("separator output contains no NEWLINE tokens")
    void noNewlinesRemain() {
        assertTrue(kinds("a\nb\nc\n").stream().noneMatch(k -> k == TokenKind.NEWLINE));
    }

    @Test
    @DisplayName("a SEMI may follow the last statement of a block")
    void semiAfterLastStatement() {
        assertEquals(List.of(TokenKind.LBRACE, TokenKind.IDENT, TokenKind.SEMI,
                        TokenKind.RBRACE, TokenKind.EOF),
                kinds("{\nx\n}"));
    }

    @Test
    @DisplayName("separation is idempotent with respect to repeated application")
    void idempotent() {
        List<Token> once = LexTestSupport.tokens("x\n");
        List<Token> twice = Separator.apply(once).tokens();
        assertEquals(once, twice);
    }

    @Test
    @DisplayName("an empty input yields only EOF")
    void empty() {
        assertEquals(List.of(TokenKind.EOF), kinds(""));
    }

    @Test
    @DisplayName("the separator reports no diagnostics even for a stray paren")
    void separatorNeverReports() {
        Separator.Result result = Separator.apply(LexTestSupport.rawTokens(")\n"));
        assertTrue(result.diagnostics().isEmpty());
    }
}
