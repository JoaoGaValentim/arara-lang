package arara.source;

import arara.diag.Diagnostic;
import arara.diag.DiagnosticCodes;
import arara.lex.Span;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and normalizes an Arara source file.
 *
 * <p>Implements {@code lexical.md} §1.1–§1.3:
 *
 * <ul>
 *   <li>strict UTF-8: no overlong forms, no surrogate code points, nothing above
 *       {@code U+10FFFF}; the first invalid byte produces {@code ARA-LEX-0001} and
 *       decoding stops there;
 *   <li>a single leading {@code U+FEFF} is dropped and occupies no position;
 *   <li>{@code \r\n} and lone {@code \r} become {@code \n}.
 * </ul>
 *
 * <p>Normalization happens once, here, so every later stage sees exactly one
 * representation.
 */
public final class SourceReader {

    private SourceReader() {
    }

    /**
     * Reads one unit from disk.
     *
     * <p>An unreadable file is {@code ARA-SRC-0002}. A file that cannot be decoded
     * as UTF-8 yields {@code ARA-LEX-0001} plus the partial text decoded so far,
     * because the contract says the token stream ends at the last token before the
     * invalid byte rather than failing the whole unit.
     */
    public static SourceUnit.ReadResult read(Path path) {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException | SecurityException e) {
            return new SourceUnit.ReadResult(null, List.of(
                    Diagnostic.of(DiagnosticCodes.SRC_UNREADABLE_INPUT, new Span(0, 0),
                            "cannot read input: " + path + " (" + e.getClass().getSimpleName() + ")")));
        }
        return fromBytes(nameOf(path), path.toString(), bytes);
    }

    /**
     * Derives the unit name: the base file name without the {@code .ara} suffix.
     *
     * <p>{@code semantics.md} §1.1 defines the name this way; validation of the
     * name is a driver concern and not performed here.
     */
    public static String nameOf(Path path) {
        String fileName = path.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    /**
     * Decodes and normalizes already-read bytes.
     *
     * <p>Exposed so tests can supply invalid sequences without needing a file whose
     * name is invalid on the host filesystem.
     */
    public static SourceUnit.ReadResult fromBytes(String name, String displayPath, byte[] bytes) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        List<Integer> decoded = new ArrayList<>(bytes.length);
        int invalidAt = -1;

        int i = 0;
        while (i < bytes.length) {
            int b0 = bytes[i] & 0xFF;
            int needed;
            int accumulated;
            int minimum;
            if (b0 < 0x80) {
                decoded.add(b0);
                i++;
                continue;
            } else if (b0 >= 0xC2 && b0 <= 0xDF) {
                needed = 1;
                accumulated = b0 & 0x1F;
                minimum = 0x80;
            } else if (b0 >= 0xE0 && b0 <= 0xEF) {
                needed = 2;
                accumulated = b0 & 0x0F;
                minimum = 0x800;
            } else if (b0 >= 0xF0 && b0 <= 0xF4) {
                needed = 3;
                accumulated = b0 & 0x07;
                minimum = 0x10000;
            } else {
                // 0x80-0xC1 (continuation or overlong lead) and 0xF5-0xFF are never valid leads.
                invalidAt = i;
                break;
            }

            if (i + needed >= bytes.length) {
                invalidAt = i;
                break;
            }
            for (int k = 1; k <= needed; k++) {
                int bk = bytes[i + k] & 0xFF;
                if ((bk & 0xC0) != 0x80) {
                    invalidAt = i;
                    break;
                }
                accumulated = (accumulated << 6) | (bk & 0x3F);
            }
            if (invalidAt >= 0) {
                break;
            }
            // Reject overlong encodings and anything that is not a Unicode scalar value.
            if (accumulated < minimum
                    || (accumulated >= 0xD800 && accumulated <= 0xDFFF)
                    || accumulated > 0x10FFFF) {
                invalidAt = i;
                break;
            }
            decoded.add(accumulated);
            i += needed + 1;
        }

        if (invalidAt >= 0) {
            diagnostics.add(Diagnostic.of(DiagnosticCodes.LEX_INVALID_UTF8,
                    new Span(decoded.size(), decoded.size()),
                    "invalid UTF-8 byte sequence at byte offset " + invalidAt
                            + "; the unit may be truncated or binary"));
        }

        boolean hadBom = !decoded.isEmpty() && decoded.get(0) == 0xFEFF;
        int from = hadBom ? 1 : 0;
        int[] normalized = normalizeLineEndings(decoded, from);
        return new SourceUnit.ReadResult(
                new SourceUnit(name, displayPath, normalized, hadBom), diagnostics);
    }

    /**
     * Applies {@code \r\n -> \n} and lone {@code \r -> \n} a single time.
     *
     * <p>Only the first BOM is skipped; a {@code U+FEFF} anywhere else survives
     * normalization and is rejected by the lexer as {@code ARA-LEX-0101}.
     */
    private static int[] normalizeLineEndings(List<Integer> source, int from) {
        int[] out = new int[source.size() - from];
        int count = 0;
        for (int i = from; i < source.size(); i++) {
            int cp = source.get(i);
            if (cp == '\r') {
                out[count++] = '\n';
                if (i + 1 < source.size() && source.get(i + 1) == '\n') {
                    i++;
                }
            } else {
                out[count++] = cp;
            }
        }
        if (count == out.length) {
            return out;
        }
        int[] trimmed = new int[count];
        System.arraycopy(out, 0, trimmed, 0, count);
        return trimmed;
    }
}
