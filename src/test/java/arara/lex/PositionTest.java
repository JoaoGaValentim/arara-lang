package arara.lex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Span and position conformance: {@code lexical.md} §1.4. */
class PositionTest {

    private static Position.Lookup lookupOf(String text) {
        return Position.Lookup.of(text.codePoints().toArray());
    }

    @Test
    @DisplayName("line and column are 1-based")
    void oneBased() {
        Position.Lookup lookup = lookupOf("abc");
        assertEquals(new Position(1, 1), lookup.at(0));
        assertEquals(new Position(1, 2), lookup.at(1));
        assertEquals(new Position(1, 3), lookup.at(2));
    }

    @Test
    @DisplayName("column resets after a line feed")
    void columnResets() {
        Position.Lookup lookup = lookupOf("ab\ncd");
        assertEquals(new Position(1, 3), lookup.at(2));
        assertEquals(new Position(2, 1), lookup.at(3));
        assertEquals(new Position(2, 2), lookup.at(4));
    }

    @Test
    @DisplayName("a tab counts as exactly one column")
    void tabIsOneColumn() {
        Position.Lookup lookup = lookupOf("\tx");
        assertEquals(new Position(1, 2), lookup.at(1));
    }

    @Test
    @DisplayName("a supplementary character counts as one column")
    void supplementaryIsOneColumn() {
        // The parrot is a single code point, so the following character is column 2.
        Position.Lookup lookup = lookupOf("\uD83E\uDD9Cx");
        assertEquals(new Position(1, 2), lookup.at(1));
    }

    @Test
    @DisplayName("the offset one past the last character is a valid position")
    void pastEnd() {
        Position.Lookup lookup = lookupOf("ab");
        assertEquals(new Position(1, 3), lookup.at(2));
    }

    @Test
    @DisplayName("a trailing newline starts a new line")
    void trailingNewline() {
        Position.Lookup lookup = lookupOf("a\n");
        assertEquals(new Position(2, 1), lookup.at(2));
    }

    @Test
    @DisplayName("a span is half-open and reports its length")
    void span() {
        Span span = new Span(2, 5);
        assertEquals(3, span.length());
        assertEquals(false, span.isEmpty());
    }

    @Test
    @DisplayName("an empty span is allowed only for EOF-like positions")
    void emptySpan() {
        Span span = new Span(4, 4);
        assertEquals(0, span.length());
        assertEquals(true, span.isEmpty());
    }

    @Test
    @DisplayName("a span rejects a negative start and a reversed range")
    void spanValidation() {
        assertThrows(IllegalArgumentException.class, () -> new Span(-1, 2));
        assertThrows(IllegalArgumentException.class, () -> new Span(5, 2));
    }
}
