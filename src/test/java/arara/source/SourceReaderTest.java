package arara.source;

import arara.diag.Diagnostic;
import arara.diag.DiagnosticCodes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Source model conformance: {@code lexical.md} §1.1–§1.3 and the UTF-8 row of
 * {@code conformance-plan.md} §3.
 */
class SourceReaderTest {

    private static SourceUnit.ReadResult read(String text) {
        return SourceReader.fromBytes("Test", "<test>", text.getBytes(StandardCharsets.UTF_8));
    }

    private static SourceUnit.ReadResult readBytes(int... values) {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = (byte) values[i];
        }
        return SourceReader.fromBytes("Test", "<test>", bytes);
    }

    @Test
    @DisplayName("ASCII decodes to one code point per byte")
    void ascii() {
        assertArrayEquals(new int[] {'a', 'b', 'c'}, read("abc").unit().codePoints());
    }

    @Test
    @DisplayName("BMP text decodes to single code points, not UTF-16 units")
    void bmp() {
        // U+00E9 (e acute) and U+4E2D (CJK) are both single code points.
        assertArrayEquals(new int[] {0x00E9, 0x4E2D}, read("\u00E9\u4E2D").unit().codePoints());
    }

    @Test
    @DisplayName("a supplementary plane character is one code point, not two")
    void supplementary() {
        // U+1F99C PARROT, the conformance-plan example. Two UTF-16 units, one code point.
        int[] cps = read("\uD83E\uDD9C").unit().codePoints();
        assertEquals(1, cps.length);
        assertEquals(0x1F99C, cps[0]);
    }

    @Test
    @DisplayName("a truncated multi-byte sequence is ARA-LEX-0001")
    void truncatedSequence() {
        SourceUnit.ReadResult result = readBytes(0xE2, 0x82);
        assertTrue(result.unit().isEmpty());
        assertEquals(List.of(DiagnosticCodes.LEX_INVALID_UTF8),
                result.diagnostics().stream().map(Diagnostic::code).toList());
    }

    @Test
    @DisplayName("an overlong encoding is ARA-LEX-0001")
    void overlong() {
        // C0 80 is an overlong encoding of NUL; C1 BF an overlong of 0x7F.
        assertEquals(List.of(DiagnosticCodes.LEX_INVALID_UTF8),
                readBytes(0xC0, 0x80).diagnostics().stream().map(Diagnostic::code).toList());
    }

    @Test
    @DisplayName("a surrogate encoded as three bytes is ARA-LEX-0001")
    void encodedSurrogate() {
        // ED A0 80 encodes U+D800, which is not a Unicode scalar value.
        assertEquals(List.of(DiagnosticCodes.LEX_INVALID_UTF8),
                readBytes(0xED, 0xA0, 0x80).diagnostics().stream().map(Diagnostic::code).toList());
    }

    @Test
    @DisplayName("text before an invalid byte is retained and decoding stops there")
    void partialDecode() {
        SourceUnit.ReadResult result = readBytes('a', 'b', 0xE2, 0x82);
        assertArrayEquals(new int[] {'a', 'b'}, result.unit().codePoints());
        assertEquals(DiagnosticCodes.LEX_INVALID_UTF8, result.diagnostics().get(0).code());
        // The span is at the first invalid byte, mapped to a text offset.
        assertEquals(2, result.diagnostics().get(0).span().start());
    }

    @Test
    @DisplayName("a BOM at offset 0 is skipped and occupies no position")
    void bomSkipped() {
        SourceUnit.ReadResult result = readBytes(0xEF, 0xBB, 0xBF, 'a');
        assertTrue(result.unit().hadBom());
        assertArrayEquals(new int[] {'a'}, result.unit().codePoints());
        assertTrue(result.diagnostics().isEmpty());
    }

    @Test
    @DisplayName("a BOM after other text is not skipped")
    void bomNotAtOffsetZero() {
        SourceUnit.ReadResult result = readBytes('a', 0xEF, 0xBB, 0xBF);
        assertFalse(result.unit().hadBom());
        assertArrayEquals(new int[] {'a', 0xFEFF}, result.unit().codePoints());
    }

    @Test
    @DisplayName("LF, CRLF and CR normalize to identical text and identical offsets")
    void lineEndingEquivalence() {
        int[] lf = read("a\nb\nc").unit().codePoints();
        int[] crlf = read("a\r\nb\r\nc").unit().codePoints();
        int[] cr = read("a\rb\rc").unit().codePoints();
        assertArrayEquals(lf, crlf);
        assertArrayEquals(lf, cr);
    }

    @Test
    @DisplayName("normalization is applied once, not repeatedly")
    void normalizationIdempotent() {
        // CR LF CR LF must become LF LF, not LF.
        assertArrayEquals(new int[] {'\n', '\n'}, read("\r\n\r\n").unit().codePoints());
    }

    @Test
    @DisplayName("a file need not end with a newline")
    void noTrailingNewline() {
        assertArrayEquals(new int[] {'a'}, read("a").unit().codePoints());
    }

    @Test
    @DisplayName("an empty file is a valid empty unit")
    void empty() {
        SourceUnit.ReadResult result = read("");
        assertTrue(result.unit().isEmpty());
        assertTrue(result.diagnostics().isEmpty());
    }
}
