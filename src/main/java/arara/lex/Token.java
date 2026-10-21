package arara.lex;

import java.util.Objects;

/**
 * One token: kind, span, and decoded value.
 *
 * <p>{@code lexical.md} §1.4 requires that every token except {@code EOF} has a
 * non-empty span. That invariant is enforced here rather than left to the lexer,
 * so a bug cannot produce a malformed token silently.
 *
 * @param kind  token kind
 * @param span  half-open code-point span
 * @param value decoded value: identifier/keyword text, integer value as text, or
 *              the decoded string contents. Never {@code null} for meaningful
 *              kinds; {@code NEWLINE}, {@code SEMI}, operators and {@code EOF}
 *              carry an empty string.
 */
public record Token(TokenKind kind, Span span, String value) {

    public Token {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(span, "span");
        Objects.requireNonNull(value, "value");
        if (kind != TokenKind.EOF && span.isEmpty()) {
            throw new IllegalArgumentException(kind + " must have a non-empty span");
        }
        if (kind == TokenKind.EOF && !span.isEmpty()) {
            throw new IllegalArgumentException("EOF must have an empty span");
        }
    }

    /** True when this token is a statement separator produced by §10. */
    public boolean isSemi() {
        return kind == TokenKind.SEMI;
    }

    /**
     * Deterministic single-line rendering.
     *
     * <p>Format is {@code KIND(text)@start:end}. The value is rendered with
     * explicit escapes so a token dump never contains a raw newline and can be
     * compared byte-for-byte across runs and platforms.
     */
    public String dump() {
        return kind.name() + "(" + escape(value) + ")@" + span.start() + ":" + span.end();
    }

    private static String escape(String raw) {
        StringBuilder out = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                case '"' -> out.append("\\\"");
                default -> {
                    if (c < 0x20 || c == 0x7F) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }
}
