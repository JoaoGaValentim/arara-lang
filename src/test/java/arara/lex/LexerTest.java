package arara.lex;

import arara.diag.Diagnostic;
import arara.diag.DiagnosticCodes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static arara.lex.LexTestSupport.diagnostics;
import static arara.lex.LexTestSupport.kinds;
import static arara.lex.LexTestSupport.singleCode;
import static arara.lex.LexTestSupport.tokens;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Lexical conformance, organised by the rows of {@code conformance-plan.md} §3.
 *
 * <p>Every negative test asserts an exact code, and where the contract makes the
 * span normative, the exact span.
 */
class LexerTest {

    // ---------------------------------------------------------------- keywords

    @ParameterizedTest
    @CsvSource({
            "fun,FUN", "val,VAL", "var,VAR", "if,IF", "else,ELSE", "return,RETURN",
            "true,TRUE", "false,FALSE", "Int,INT", "Boolean,BOOLEAN", "String,STRING",
            "Unit,UNIT"
    })
    @DisplayName("each of the twelve keywords lexes as its keyword token")
    void keywords(String text, TokenKind expected) {
        List<Token> result = tokens(text);
        assertEquals(expected, result.get(0).kind());
        assertEquals(text, result.get(0).value());
        assertTrue(diagnostics(text).isEmpty());
    }

    @Test
    @DisplayName("keywords are case sensitive")
    void keywordCaseSensitivity() {
        // 'Fun' is not the keyword 'fun'.
        assertEquals(TokenKind.IDENT, kinds("Fun").get(0));
    }

    // --------------------------------------------------------- reserved words

    @ParameterizedTest
    @ValueSource(strings = {
            "class", "object", "interface", "enum", "this", "super", "new", "init",
            "constructor", "when", "while", "for", "do", "break", "continue",
            "try", "catch", "finally", "throw", "null", "package", "import",
            "typealias", "suspend", "annotation", "in", "is", "as"
    })
    @DisplayName("each reserved word produces ARA-LEX-0401")
    void reservedWords(String word) {
        assertEquals(DiagnosticCodes.LEX_RESERVED_WORD, singleCode(word));
        assertEquals(TokenKind.RESERVED, kinds(word).get(0));
    }

    @Test
    @DisplayName("the reserved list has exactly the documented size")
    void reservedWordCount() {
        assertEquals(28, Words.RESERVED.size());
    }

    @Test
    @DisplayName("a reserved word is reported even in identifier position")
    void reservedInIdentifierPosition() {
        assertEquals(DiagnosticCodes.LEX_RESERVED_WORD, singleCode("val null: Int = 1"));
    }

    @Test
    @DisplayName("no keyword is also reserved, which would be an ambiguity")
    void keywordAndReservedAreDisjoint() {
        for (String keyword : Words.KEYWORDS.keySet()) {
            assertTrue(!Words.isReserved(keyword), keyword + " is both keyword and reserved");
        }
    }

    // ------------------------------------------------------------ identifiers

    @Test
    @DisplayName("underscore, digit-suffixed and mixed identifiers are legal")
    void identifiers() {
        assertEquals(TokenKind.IDENT, kinds("_").get(0));
        assertEquals(TokenKind.IDENT, kinds("a1").get(0));
        assertEquals(TokenKind.IDENT, kinds("A_B").get(0));
    }

    @Test
    @DisplayName("a dollar sign is ARA-LEX-0103")
    void dollarSign() {
        assertEquals(DiagnosticCodes.LEX_NO_TOKEN_STARTS_HERE, singleCode("a$b"));
    }

    @Test
    @DisplayName("a non-ASCII letter outside a string is ARA-LEX-0101")
    void nonAsciiIdentifier() {
        assertEquals(DiagnosticCodes.LEX_CHAR_NOT_PERMITTED, singleCode("\u00E9"));
    }

    @Test
    @DisplayName("a keyword inside a longer identifier is just an identifier")
    void keywordPrefix() {
        assertEquals(TokenKind.IDENT, kinds("function").get(0));
        assertEquals(TokenKind.IDENT, kinds("iffy").get(0));
    }

    // -------------------------------------------------------- integers

    @ParameterizedTest
    @ValueSource(strings = {"0", "7", "2147483647", "2147483648"})
    @DisplayName("in-range and boundary literals lex successfully")
    void integersValid(String literal) {
        assertTrue(diagnostics(literal).isEmpty());
        assertEquals(TokenKind.INT_LITERAL, kinds(literal).get(0));
    }

    @Test
    @DisplayName("a multi-digit literal with a leading zero is ARA-LEX-0205")
    void leadingZero() {
        assertEquals(DiagnosticCodes.LEX_LEADING_ZERO, singleCode("0123"));
    }

    @Test
    @DisplayName("zero alone is valid")
    void zeroAlone() {
        assertTrue(diagnostics("0").isEmpty());
    }

    @Test
    @DisplayName("a value above 2147483648 is ARA-LEX-0204")
    void integerTooLarge() {
        assertEquals(DiagnosticCodes.LEX_INT_LITERAL_TOO_LARGE, singleCode("2147483649"));
    }

    @Test
    @DisplayName("a long run of leading zeros does not become a false overflow")
    void leadingZerosNotOverflow() {
        // The significant value is 1, so only the leading-zero rule may fire.
        assertEquals(List.of(DiagnosticCodes.LEX_LEADING_ZERO),
                diagnostics("0000000000000000001").stream().map(Diagnostic::code).toList());
    }

    @Test
    @DisplayName("a digit sequence followed by letters is a number then an identifier")
    void digitsThenLetters() {
        assertEquals(List.of(TokenKind.INT_LITERAL, TokenKind.IDENT, TokenKind.EOF),
                kinds("123abc"));
    }

    @Test
    @DisplayName("a minus sign is a separate token, not part of the literal")
    void negativeIsTwoTokens() {
        assertEquals(List.of(TokenKind.MINUS, TokenKind.INT_LITERAL, TokenKind.EOF),
                kinds("-1"));
    }

    // -------------------------------------------------------------- operators

    @ParameterizedTest
    @CsvSource({
            "'==',EQ", "'!=',NE", "'<=',LE", "'>=',GE", "'&&',ANDAND", "'||',OROR"
    })
    @DisplayName("every two-character operator is one token by maximal munch")
    void twoCharOperators(String text, TokenKind expected) {
        List<Token> result = tokens(text);
        assertEquals(expected, result.get(0).kind());
        assertEquals(2, result.get(0).span().length());
        assertTrue(diagnostics(text).isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "'(',LPAREN", "')',RPAREN", "'{',LBRACE", "'}',RBRACE", "':',COLON",
            "',',COMMA", "';',SEMI", "'=',ASSIGN", "'<',LT", "'>',GT", "'+',PLUS",
            "'-',MINUS", "'*',STAR", "'/',SLASH", "'%',PERCENT", "'!',BANG"
    })
    @DisplayName("every single-character operator is one token")
    void oneCharOperators(String text, TokenKind expected) {
        assertEquals(expected, kinds(text).get(0));
    }

    @Test
    @DisplayName("a lone ampersand is ARA-LEX-0103 at that character")
    void loneAmpersand() {
        List<Diagnostic> found = diagnostics("a & b");
        assertEquals(1, found.size());
        assertEquals(DiagnosticCodes.LEX_NO_TOKEN_STARTS_HERE, found.get(0).code());
        assertEquals(2, found.get(0).span().start());
    }

    @Test
    @DisplayName("a lone pipe is ARA-LEX-0103 at that character")
    void lonePipe() {
        List<Diagnostic> found = diagnostics("a | b");
        assertEquals(1, found.size());
        assertEquals(DiagnosticCodes.LEX_NO_TOKEN_STARTS_HERE, found.get(0).code());
        assertEquals(2, found.get(0).span().start());
    }

    @Test
    @DisplayName("a triple ampersand is ANDAND then ARA-LEX-0103 at the third")
    void tripleAmpersand() {
        assertEquals(List.of(TokenKind.IDENT, TokenKind.ANDAND, TokenKind.IDENT, TokenKind.EOF),
                kinds("a&&&b"));
        List<Diagnostic> found = diagnostics("a&&&b");
        assertEquals(1, found.size());
        assertEquals(DiagnosticCodes.LEX_NO_TOKEN_STARTS_HERE, found.get(0).code());
        assertEquals(3, found.get(0).span().start());
    }

    @Test
    @DisplayName("a plus-plus is two valid plus tokens and no lexical diagnostic")
    void plusPlusIsNotLexical() {
        assertEquals(List.of(TokenKind.PLUS, TokenKind.PLUS, TokenKind.EOF), kinds("++"));
        assertTrue(diagnostics("++").isEmpty(),
                "++ must be a parser case, never ARA-LEX-0103");
    }

    @Test
    @DisplayName("a minus-minus is two valid minus tokens")
    void minusMinus() {
        assertEquals(List.of(TokenKind.MINUS, TokenKind.MINUS, TokenKind.EOF), kinds("--"));
    }

    @Test
    @DisplayName("maximal munch makes a=-1 identical to a = -1")
    void maximalMunchAssignment() {
        assertEquals(List.of(TokenKind.IDENT, TokenKind.ASSIGN, TokenKind.MINUS, TokenKind.INT_LITERAL),
                kinds("a=-1").subList(0, 4));
    }

    @ParameterizedTest
    @ValueSource(strings = {".", "[", "]", "@", "#", "~", "^", "`"})
    @DisplayName("a character that begins no token is ARA-LEX-0103")
    void noTokenCharacter(String character) {
        assertEquals(DiagnosticCodes.LEX_NO_TOKEN_STARTS_HERE, singleCode(character));
    }

    @Test
    @DisplayName("a backslash outside a string is ARA-LEX-0103")
    void backslashOutsideString() {
        assertEquals(DiagnosticCodes.LEX_NO_TOKEN_STARTS_HERE, singleCode("\\"));
    }

    // ------------------------------------------------------------ characters

    @Test
    @DisplayName("DEL is ARA-LEX-0101 outside a string")
    void delOutsideString() {
        assertEquals(DiagnosticCodes.LEX_CHAR_NOT_PERMITTED, singleCode("\u007F"));
    }

    @Test
    @DisplayName("NUL is ARA-LEX-0101 outside a string")
    void nulOutsideString() {
        assertEquals(DiagnosticCodes.LEX_CHAR_NOT_PERMITTED, singleCode("\u0000"));
    }

    @Test
    @DisplayName("a BOM anywhere but offset zero is ARA-LEX-0101")
    void bomNotAtStart() {
        assertEquals(DiagnosticCodes.LEX_CHAR_NOT_PERMITTED, singleCode(" \uFEFF"));
    }

    @Test
    @DisplayName("every permitted whitespace character separates tokens silently")
    void permittedWhitespace() {
        assertTrue(diagnostics("a\u0020b").isEmpty());
        assertTrue(diagnostics("a\tb").isEmpty());
        assertTrue(diagnostics("a\u000Bb").isEmpty());
        assertTrue(diagnostics("a\u000Cb").isEmpty());
    }

    @Test
    @DisplayName("non-ASCII text is permitted inside a comment")
    void nonAsciiInComment() {
        assertTrue(diagnostics("// \u4E2D\u6587 \uD83E\uDD9C").isEmpty());
        assertTrue(diagnostics("/* \u4E2D\u6587 */").isEmpty());
    }

    // --------------------------------------------------------------- comments

    @Test
    @DisplayName("a line comment runs to end of file and produces no token")
    void lineCommentAtEof() {
        assertEquals(List.of(TokenKind.EOF), kinds("// trailing"));
    }

    @Test
    @DisplayName("a block comment produces no token")
    void blockComment() {
        assertEquals(List.of(TokenKind.EOF), kinds("/* nothing */"));
    }

    @Test
    @DisplayName("a doc-style block comment is an ordinary block comment")
    void docComment() {
        assertEquals(List.of(TokenKind.EOF), kinds("/** docs */"));
    }

    @Test
    @DisplayName("block comments do not nest: /* /* */ is one comment, then a star and a slash")
    void blockCommentsDoNotNest() {
        // lexical.md §21 states this consequence explicitly.
        assertEquals(List.of(TokenKind.STAR, TokenKind.SLASH, TokenKind.EOF),
                kinds("/* /* */ */").subList(0, 3));
    }

    @Test
    @DisplayName("an unterminated block comment is ARA-LEX-0301 with the opening span")
    void unterminatedBlockComment() {
        List<Diagnostic> found = diagnostics("/* open");
        assertEquals(1, found.size());
        assertEquals(DiagnosticCodes.LEX_UNTERMINATED_BLOCK_COMMENT, found.get(0).code());
        assertEquals(0, found.get(0).span().start());
        assertEquals(2, found.get(0).span().end());
    }

    @Test
    @DisplayName("a comment may not split a token: fun/*x*/ction is three pieces")
    void commentDoesNotJoinTokens() {
        assertEquals(List.of(TokenKind.FUN, TokenKind.IDENT, TokenKind.EOF),
                kinds("fun/*x*/ction"));
    }

    @Test
    @DisplayName("a line comment ends before the line feed, which still yields NEWLINE")
    void lineCommentKeepsNewline() {
        List<Token> raw = LexTestSupport.rawTokens("// c\nx");
        assertEquals(TokenKind.NEWLINE, raw.get(0).kind());
        assertEquals(TokenKind.IDENT, raw.get(1).kind());
    }

    // ---------------------------------------------------------------- strings

    @Test
    @DisplayName("an empty string literal is legal")
    void emptyString() {
        List<Token> result = tokens("\"\"");
        assertEquals(TokenKind.STRING_LITERAL, result.get(0).kind());
        assertEquals("", result.get(0).value());
    }

    @Test
    @DisplayName("the five permitted escapes decode to their values")
    void escapes() {
        List<Token> result = tokens("\"\\n\\r\\t\\\\\\\"\"");
        assertEquals("\n\r\t\\\"", result.get(0).value());
        assertTrue(diagnostics("\"\\n\\r\\t\\\\\\\"\"").isEmpty());
    }

    @Test
    @DisplayName("a literal tab is permitted inside a string")
    void rawTabInString() {
        assertTrue(diagnostics("\"a\tb\"").isEmpty());
    }

    @Test
    @DisplayName("non-ASCII and supplementary text is permitted inside a string")
    void unicodeInString() {
        List<Token> result = tokens("\"\u4E2D\uD83E\uDD9C\"");
        assertEquals(TokenKind.STRING_LITERAL, result.get(0).kind());
        assertEquals("\u4E2D\uD83E\uDD9C", result.get(0).value());
        assertTrue(diagnostics("\"\u4E2D\uD83E\uDD9C\"").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"'", "0", "b", "f", "u0041"})
    @DisplayName("an invalid escape is ARA-LEX-0202 spanning the backslash and the next character")
    void invalidEscape(String escapeBody) {
        String source = "\"\\" + escapeBody + "\"";
        List<Diagnostic> found = diagnostics(source);
        assertEquals(DiagnosticCodes.LEX_INVALID_ESCAPE, found.get(0).code());
        assertEquals(1, found.get(0).span().start());
        assertEquals(3, found.get(0).span().end());
    }

    @Test
    @DisplayName("a raw line break in a string is ARA-LEX-0203 spanning the line break")
    void rawNewlineInString() {
        List<Diagnostic> found = diagnostics("\"ab\ncd\"");
        assertEquals(DiagnosticCodes.LEX_RAW_LINE_BREAK_IN_STRING, found.get(0).code());
        assertEquals(3, found.get(0).span().start());
        assertEquals(4, found.get(0).span().end());
    }

    @Test
    @DisplayName("a backslash before a line break is still a raw line break, not an invalid escape")
    void backslashBeforeLineBreak() {
        // Rule 41: a string may not continue across lines "not even with a trailing
        // backslash". Before this regression test existed, the backslash consumed the
        // line break and reported ARA-LEX-0202 instead of ARA-LEX-0203, and the
        // literal silently continued on the next line.
        List<Diagnostic> found = diagnostics("\"ab\\\ncd\"");
        assertEquals(DiagnosticCodes.LEX_RAW_LINE_BREAK_IN_STRING, found.get(0).code());
        // The span covers exactly the line break, which sits at offset 4.
        assertEquals(4, found.get(0).span().start());
        assertEquals(5, found.get(0).span().end());
        assertTrue(found.stream().noneMatch(d -> d.code().equals(DiagnosticCodes.LEX_INVALID_ESCAPE)),
                "a trailing backslash must not be reported as an invalid escape");
    }

    @Test
    @DisplayName("a backslash before a line break does not let the literal span the break")
    void backslashBeforeLineBreakDoesNotContinueLiteral() {
        List<Token> result = LexTestSupport.rawTokens("\"ab\\\ncd\"");
        assertTrue(result.stream().noneMatch(t -> t.kind() == TokenKind.STRING_LITERAL),
                "the partial literal must be discarded, not emitted across the break");
        // Lexing resumes at the break, so a NEWLINE token must be present.
        assertTrue(result.stream().anyMatch(t -> t.kind() == TokenKind.NEWLINE));
    }

    @Test
    @DisplayName("after a raw line break, lexing resumes at the break rather than aborting")
    void recoveryAfterRawNewline() {
        // The partial literal is discarded; lexing continues on the next line.
        List<TokenKind> result = kinds("\"ab\ncd");
        assertTrue(result.contains(TokenKind.IDENT), "lexing must resume after the break");
    }

    @Test
    @DisplayName("an unterminated string at end of file is ARA-LEX-0201 at the opening quote")
    void unterminatedString() {
        List<Diagnostic> found = diagnostics("\"abc");
        assertEquals(DiagnosticCodes.LEX_UNTERMINATED_STRING, found.get(0).code());
        assertEquals(0, found.get(0).span().start());
        assertEquals(1, found.get(0).span().end());
    }

    @Test
    @DisplayName("an unterminated string stops lexing of the unit")
    void unterminatedStringStops() {
        List<Token> result = tokens("\"abc");
        // Only EOF follows: the contract says lexing of the unit stops.
        assertEquals(TokenKind.EOF, result.get(result.size() - 1).kind());
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("a trailing backslash at end of file is ARA-LEX-0201, not ARA-LEX-0202")
    void trailingBackslash() {
        assertEquals(DiagnosticCodes.LEX_UNTERMINATED_STRING, singleCode("\"abc\\"));
    }

    @Test
    @DisplayName("a control character inside a string is ARA-LEX-0206")
    void controlInString() {
        assertEquals(DiagnosticCodes.LEX_FORBIDDEN_CONTROL_CHAR, singleCode("\"a\u0001b\""));
    }

    @Test
    @DisplayName("DEL inside a string is ARA-LEX-0206")
    void delInString() {
        assertEquals(DiagnosticCodes.LEX_FORBIDDEN_CONTROL_CHAR, singleCode("\"a\u007Fb\""));
    }
}
