package arara.lex;

import java.util.ArrayList;
import java.util.List;

/**
 * 1-based line and column position, counted in Unicode code points.
 *
 * <p>Columns count code points, so a tab is one column and an astral character
 * is one column (see {@code lexical.md} §1.4).
 *
 * @param line   1-based line number
 * @param column 1-based column number within the line
 */
public record Position(int line, int column) {

    public Position {
        if (line < 1) {
            throw new IllegalArgumentException("line must be 1-based: " + line);
        }
        if (column < 1) {
            throw new IllegalArgumentException("column must be 1-based: " + column);
        }
    }

    @Override
    public String toString() {
        return line + ":" + column;
    }

    /**
     * Maps code-point offsets to line/column positions over normalized text.
     *
     * <p>The table is built once per source unit. Lookup is O(log n) in the number
     * of line starts, which keeps diagnostics cheap without materializing a
     * position per offset.
     */
    public static final class Lookup {

        private final int[] lineStarts;

        private Lookup(int[] lineStarts) {
            this.lineStarts = lineStarts;
        }

        /**
         * Builds a lookup over text that has already been normalized.
         *
         * @param codePoints the normalized text as code points
         */
        public static Lookup of(int[] codePoints) {
            List<Integer> starts = new ArrayList<>();
            starts.add(0);
            for (int i = 0; i < codePoints.length; i++) {
                if (codePoints[i] == '\n') {
                    starts.add(i + 1);
                }
            }
            int[] table = new int[starts.size()];
            for (int i = 0; i < table.length; i++) {
                table[i] = starts.get(i);
            }
            return new Lookup(table);
        }

        /** Number of lines; a trailing newline does not create an extra line. */
        public int lineCount() {
            return lineStarts.length;
        }

        /**
         * Returns the position of a code-point offset.
         *
         * <p>An offset equal to the text length is valid and resolves to the
         * position just past the last character, which is where {@code EOF} sits.
         */
        public Position at(int offset) {
            if (offset < 0) {
                throw new IllegalArgumentException("offset must be non-negative: " + offset);
            }
            int low = 0;
            int high = lineStarts.length - 1;
            while (low < high) {
                int mid = (low + high + 1) >>> 1;
                if (lineStarts[mid] <= offset) {
                    low = mid;
                } else {
                    high = mid - 1;
                }
            }
            return new Position(low + 1, offset - lineStarts[low] + 1);
        }
    }
}
