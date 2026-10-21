package arara.lex;

/**
 * A half-open source span in Unicode code points.
 *
 * <p>Offsets are indices into the <em>normalized</em> text (see
 * {@code lexical.md} §1.3) and are measured in Unicode code points, not bytes
 * and not UTF-16 units. A span never crosses a file boundary.
 *
 * @param start inclusive start offset
 * @param end   exclusive end offset
 */
public record Span(int start, int end) {

    public Span {
        if (start < 0) {
            throw new IllegalArgumentException("span start must be non-negative: " + start);
        }
        if (end < start) {
            throw new IllegalArgumentException("span end " + end + " precedes start " + start);
        }
    }

    /** Length in code points. */
    public int length() {
        return end - start;
    }

    /** True when this span covers no code points; only {@code EOF} has an empty span. */
    public boolean isEmpty() {
        return start == end;
    }

    /** The smallest span covering both operands. */
    public Span union(Span other) {
        return new Span(Math.min(start, other.start), Math.max(end, other.end));
    }

    @Override
    public String toString() {
        return "[" + start + ", " + end + ")";
    }
}
