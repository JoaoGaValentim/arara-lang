package arara.lex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Determinism obligations: {@code D002-G9} and the deterministic-dump rows of
 * {@code conformance-plan.md} §3.
 */
class TokenDumpTest {

    private static final String SAMPLE = """
            val answer: Int = 40 + 2
            fun main() {
                println("hi")
            }
            """;

    @Test
    @DisplayName("the same input produces byte-identical dumps across runs")
    void stableAcrossRuns() {
        String first = TokenDump.render(LexTestSupport.tokens(SAMPLE));
        for (int i = 0; i < 20; i++) {
            assertEquals(first, TokenDump.render(LexTestSupport.tokens(SAMPLE)));
        }
    }

    @Test
    @DisplayName("a fresh JVM-equivalent path is not needed: the dump depends only on input")
    void independentOfCallOrder() {
        String alone = TokenDump.render(LexTestSupport.tokens("val x: Int = 1\n"));
        // Interleave unrelated lexing, then repeat.
        TokenDump.render(LexTestSupport.tokens("fun f() {}\n"));
        String again = TokenDump.render(LexTestSupport.tokens("val x: Int = 1\n"));
        assertEquals(alone, again);
    }

    @Test
    @DisplayName("CRLF input produces the same dump as LF input")
    void lineEndingIndependent() {
        assertEquals(TokenDump.render(LexTestSupport.tokens("a\nb\n")),
                TokenDump.render(LexTestSupport.tokens("a\r\nb\r\n")));
    }

    @Test
    @DisplayName("a BOM does not change the dump")
    void bomIndependent() {
        byte[] withBom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'a'};
        assertEquals(TokenDump.render(LexTestSupport.tokens("a")),
                TokenDump.render(LexTestSupport.tokens(LexTestSupport.unit(withBom))));
    }

    @Test
    @DisplayName("different inputs produce different dumps")
    void distinguishesInputs() {
        assertNotEquals(TokenDump.render(LexTestSupport.tokens("a")),
                TokenDump.render(LexTestSupport.tokens("b")));
    }

    @Test
    @DisplayName("a token dump is one line per token and never contains a raw newline")
    void oneLinePerToken() {
        String dump = TokenDump.render(LexTestSupport.tokens("\"a\\nb\""));
        String[] lines = dump.split("\n", -1);
        // The escape \\n inside the literal must be rendered as two characters.
        assertTrue(dump.contains("\\n"), "the newline escape must be rendered escaped");
        // STRING_LITERAL and EOF are two lines; the split adds one trailing empty element.
        assertEquals(3, lines.length, "two token lines plus the trailing empty split");
    }

    @Test
    @DisplayName("the dump format is KIND(value)@start:end")
    void format() {
        List<Token> tokens = LexTestSupport.tokens("val");
        assertEquals("VAL(val)@0:3", tokens.get(0).dump());
    }

    @Test
    @DisplayName("EOF is rendered with an empty span at the end of the text")
    void eof() {
        List<Token> tokens = LexTestSupport.tokens("abc");
        Token eof = tokens.get(tokens.size() - 1);
        assertEquals(TokenKind.EOF, eof.kind());
        assertEquals("EOF()@3:3", eof.dump());
    }

    @Test
    @DisplayName("a supplementary character shifts later offsets by one, not two")
    void supplementaryOffsets() {
        List<Token> tokens = LexTestSupport.tokens("\"\uD83E\uDD9C\"x");
        // The literal occupies code points 0..3 (quote, parrot, quote), so x is at 3.
        assertEquals(3, tokens.get(1).span().start());
    }
}
