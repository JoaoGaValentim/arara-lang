package arara.lex;

import arara.diag.Diagnostic;
import arara.lex.Position.Lookup;

import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic token and diagnostic serialization.
 *
 * <p>Token dumps are the golden format for {@code D002-G9}. Nothing here may
 * depend on hash iteration order, locale, time zone, or wall clock.
 */
public final class TokenDump {

    private TokenDump() {
    }

    /** Renders tokens one per line, in order, without EOF-relative ambiguity. */
    public static String render(List<Token> tokens) {
        StringBuilder out = new StringBuilder();
        for (Token token : tokens) {
            out.append(token.dump()).append('\n');
        }
        return out.toString();
    }

    /**
     * Renders diagnostics in the {@code conformance-plan.md} §2 format:
     * {@code <code> <startLine>:<startCol>-<endLine>:<endCol>}, one per line.
     *
     * <p>Emission order is the order produced by the pipeline, which
     * {@code semantics.md} §12.7.1 requires to be stable.
     */
    public static String renderDiagnostics(List<Diagnostic> diagnostics, Lookup lookup) {
        StringBuilder out = new StringBuilder();
        for (Diagnostic diagnostic : diagnostics) {
            Position start = lookup.at(diagnostic.span().start());
            Position end = lookup.at(diagnostic.span().end());
            out.append(diagnostic.code())
                    .append(' ')
                    .append(start.line()).append(':').append(start.column())
                    .append('-')
                    .append(end.line()).append(':').append(end.column())
                    .append('\n');
        }
        return out.toString();
    }

    /** Renders diagnostics with their message, for human-facing CLI output. */
    public static String renderDiagnosticsVerbose(
            List<Diagnostic> diagnostics, Lookup lookup, String unitName) {
        List<String> lines = new ArrayList<>();
        for (Diagnostic diagnostic : diagnostics) {
            Position start = lookup.at(diagnostic.span().start());
            Position end = lookup.at(diagnostic.span().end());
            lines.add(unitName + ":" + start.line() + ":" + start.column()
                    + ": " + diagnostic.severity().name().toLowerCase(java.util.Locale.ROOT)
                    + " [" + diagnostic.code() + "] " + diagnostic.message()
                    + " (" + start.line() + ":" + start.column()
                    + "-" + end.line() + ":" + end.column() + ")");
        }
        StringBuilder out = new StringBuilder();
        for (String line : lines) {
            out.append(line).append('\n');
        }
        return out.toString();
    }
}
