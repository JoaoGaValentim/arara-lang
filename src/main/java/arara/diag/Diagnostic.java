package arara.diag;

import arara.lex.Span;

import java.util.List;
import java.util.Objects;

/**
 * One diagnostic: a stable code, a severity, a primary span, and related spans.
 *
 * <p>{@code AGENTS.md} §7 requires every diagnostic to carry a stable code, a
 * severity, a primary span, and an optional list of related spans with notes.
 * Ordering is defined by {@code semantics.md} §12.7.1; this record does not impose
 * an order of its own.
 *
 * @param code     stable identifier, e.g. {@code ARA-LEX-0101}
 * @param severity severity class
 * @param span     the primary span
 * @param message  human-readable explanation; tests never assert this text
 * @param related  related spans with notes, possibly empty
 */
public record Diagnostic(
        String code,
        Severity severity,
        Span span,
        String message,
        List<Related> related) {

    public Diagnostic {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(span, "span");
        Objects.requireNonNull(message, "message");
        related = List.copyOf(related);
    }

    /** A diagnostic with no related spans. */
    public static Diagnostic of(String code, Span span, String message) {
        return new Diagnostic(code, Severity.ERROR, span, message, List.of());
    }

    /** A related span and its note. */
    public record Related(Span span, String note) {
        public Related {
            Objects.requireNonNull(span, "span");
            Objects.requireNonNull(note, "note");
        }
    }
}
