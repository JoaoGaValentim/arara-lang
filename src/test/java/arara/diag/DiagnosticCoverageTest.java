package arara.diag;

import arara.lex.Lexer;
import arara.lex.LexTestSupport;
import arara.source.SourceReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Diagnostic coverage: {@code D002-G5}.
 *
 * <p>Every producible {@code ARA-LEX-*} code must be produced by at least one
 * input, and {@code ARA-LEX-0102} must be provably absent.
 */
class DiagnosticCoverageTest {

    /** One representative input per producible lexical code. */
    private static final Map<String, String> TRIGGERS = new LinkedHashMap<>();

    static {
        TRIGGERS.put(DiagnosticCodes.LEX_CHAR_NOT_PERMITTED, "\u00E9");
        TRIGGERS.put(DiagnosticCodes.LEX_NO_TOKEN_STARTS_HERE, "$");
        TRIGGERS.put(DiagnosticCodes.LEX_UNTERMINATED_STRING, "\"abc");
        TRIGGERS.put(DiagnosticCodes.LEX_INVALID_ESCAPE, "\"\\q\"");
        TRIGGERS.put(DiagnosticCodes.LEX_RAW_LINE_BREAK_IN_STRING, "\"a\nb\"");
        TRIGGERS.put(DiagnosticCodes.LEX_INT_LITERAL_TOO_LARGE, "2147483649");
        TRIGGERS.put(DiagnosticCodes.LEX_LEADING_ZERO, "0123");
        TRIGGERS.put(DiagnosticCodes.LEX_FORBIDDEN_CONTROL_CHAR, "\"a\u0001\"");
        TRIGGERS.put(DiagnosticCodes.LEX_UNTERMINATED_BLOCK_COMMENT, "/* open");
        TRIGGERS.put(DiagnosticCodes.LEX_RESERVED_WORD, "class");
    }

    @Test
    @DisplayName("every producible lexical code is produced by at least one input")
    void everyProducibleCodeIsProduced() {
        Set<String> produced = new LinkedHashSet<>();
        for (Map.Entry<String, String> entry : TRIGGERS.entrySet()) {
            Set<String> codes = new LinkedHashSet<>();
            Lexer.lex(LexTestSupport.unit(entry.getValue()))
                    .diagnostics().forEach(d -> codes.add(d.code()));
            assertTrue(codes.contains(entry.getKey()),
                    "input for " + entry.getKey() + " produced " + codes);
            produced.addAll(codes);
        }
        // ARA-LEX-0001 comes from the reader, not the lexer, so it is checked separately.
        byte[] invalid = {(byte) 0xC0, (byte) 0x80};
        SourceReader.fromBytes("T", "<t>", invalid).diagnostics()
                .forEach(d -> produced.add(d.code()));

        Set<String> expected = new LinkedHashSet<>(DiagnosticCodes.LEXICAL_PRODUCIBLE);
        assertEquals(expected, produced, "coverage must be exact, not merely non-empty");
    }

    @Test
    @DisplayName("ARA-LEX-0102 is documented but must never be emitted")
    void reservedCodeNeverEmitted() {
        assertTrue(DiagnosticCodes.NEVER_EMITTED.contains(DiagnosticCodes.LEX_INVALID_IDENTIFIER));
        assertFalse(DiagnosticCodes.LEXICAL_PRODUCIBLE.contains(DiagnosticCodes.LEX_INVALID_IDENTIFIER),
                "the reserved code must not be in the producible set");

        Set<String> allCodes = new LinkedHashSet<>();
        for (String source : TRIGGERS.values()) {
            Lexer.lex(LexTestSupport.unit(source)).diagnostics().forEach(d -> allCodes.add(d.code()));
        }
        String corpus = "val x: Int = 1\nfun f(a: Int): Int { return a }\n"
                + "// comment \u4E2D\u6587\n/* block */ x = 1 + 2 * 3\n"
                + "\"string\\n\"\nif (x < 2) { } else { }\n";
        Lexer.lex(LexTestSupport.unit(corpus)).diagnostics().forEach(d -> allCodes.add(d.code()));
        assertFalse(allCodes.contains(DiagnosticCodes.LEX_INVALID_IDENTIFIER),
                "ARA-LEX-0102 is reserved and must never be produced");
    }

    @Test
    @DisplayName("a non-ASCII identifier candidate reports ARA-LEX-0101, not ARA-LEX-0102")
    void nonAsciiIs0101() {
        // One non-ASCII code point so the assertion is unambiguous; the surrounding
        // ASCII letters are valid identifier characters and produce no diagnostic.
        assertEquals(DiagnosticCodes.LEX_CHAR_NOT_PERMITTED,
                LexTestSupport.singleCode("et\u00E9"));
        assertEquals(DiagnosticCodes.LEX_CHAR_NOT_PERMITTED,
                LexTestSupport.singleCode("\u00E9t\u00E9".substring(0, 2)));
    }

    @Test
    @DisplayName("each code has severity ERROR in the initial subset")
    void severityIsError() {
        for (Map.Entry<String, String> entry : TRIGGERS.entrySet()) {
            LexTestSupport.diagnostics(entry.getValue()).forEach(d ->
                    assertEquals(Severity.ERROR, d.severity(), entry.getKey()));
        }
    }

    @Test
    @DisplayName("a clean program produces no diagnostic at all")
    void cleanProgram() {
        String source = "fun main() {\n    val greeting: String = \"hi\"\n"
                + "    println(greeting)\n}\n";
        assertEquals(0, Lexer.lex(LexTestSupport.unit(source)).diagnostics().size());
    }

    @Test
    @DisplayName("independent defects each produce their own diagnostic")
    void noEarlyAbort() {
        String source = "a $\nb &\nc \u00E9\n";
        Set<String> codes = new LinkedHashSet<>();
        Lexer.lex(LexTestSupport.unit(source)).diagnostics().forEach(d -> codes.add(d.code()));
        assertEquals(Set.of(DiagnosticCodes.LEX_NO_TOKEN_STARTS_HERE,
                DiagnosticCodes.LEX_CHAR_NOT_PERMITTED), codes);
        assertEquals(3, Lexer.lex(LexTestSupport.unit(source)).diagnostics().size(),
                "each independent defect must report exactly once");
    }
}
