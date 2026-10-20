package arara.source;

import arara.diag.Diagnostic;

import java.util.List;

/**
 * One compilation unit after decoding and normalization.
 *
 * <p>{@code lexical.md} §1: the text is valid UTF-8, {@code \r\n} and lone
 * {@code \r} are replaced by {@code \n} exactly once, and a single leading
 * {@code U+FEFF} is ignored without occupying a position. Spans and positions
 * therefore refer to {@link #codePoints()}, never to the original bytes.
 *
 * @param name       unit name: base file name without the {@code .ara} suffix
 * @param path       the path as given on the command line
 * @param codePoints normalized text as Unicode scalar values
 * @param hadBom     whether a byte order mark was present and skipped
 */
public record SourceUnit(String name, String path, int[] codePoints, boolean hadBom) {

    /** True when the text is empty. */
    public boolean isEmpty() {
        return codePoints.length == 0;
    }

    /** Number of code points. */
    public int length() {
        return codePoints.length;
    }

    /**
     * The decoded text.
     *
     * <p>Provided for diagnostics and messages only. Lexing operates on
     * {@link #codePoints()} so that offsets stay in code points even when a
     * surrogate pair is present.
     */
    public String text() {
        return new String(codePoints, 0, codePoints.length);
    }

    /** A unit together with any diagnostics produced while reading it. */
    public record ReadResult(SourceUnit unit, List<Diagnostic> diagnostics) {
        public ReadResult {
            diagnostics = List.copyOf(diagnostics);
        }

        /** True when the unit is usable and no diagnostic was produced. */
        public boolean isClean() {
            return diagnostics.isEmpty();
        }
    }
}
