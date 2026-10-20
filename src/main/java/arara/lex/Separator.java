package arara.lex;

import arara.diag.Diagnostic;

import java.util.ArrayList;
import java.util.List;

/**
 * Automatic statement separation ({@code lexical.md} §10).
 *
 * <p>This is a post-lexing transformation over the token stream, defined
 * independently of parse context so it is deterministic and testable on its own.
 * It converts some {@code NEWLINE} tokens to {@code SEMI} and discards the rest.
 * It never reports a diagnostic: a stray {@code )} at depth zero is preserved so
 * the parser can report {@code ARA-PARSE-0117}.
 */
public final class Separator {

    /** Result of separation. */
    public record Result(List<Token> tokens, List<Diagnostic> diagnostics) {
        public Result {
            tokens = List.copyOf(tokens);
            diagnostics = List.copyOf(diagnostics);
        }
    }

    private Separator() {
    }

    /**
     * Converts newlines to separators.
     *
     * <p>Rule 52 fixes the depth counter, rule 53 the trigger set, rule 54 the
     * collapse of a run, and rule 55 the discard inside parentheses.
     */
    public static Result apply(List<Token> lexed) {
        List<Token> out = new ArrayList<>(lexed.size());
        int depth = 0;
        Token previousSignificant = null;
        boolean semiEmittedInThisRun = false;

        for (Token token : lexed) {
            if (token.kind() == TokenKind.NEWLINE) {
                if (depth == 0 && triggersTermination(previousSignificant) && !semiEmittedInThisRun) {
                    // Rule 54: a run of newlines yields at most one SEMI.
                    out.add(new Token(TokenKind.SEMI, token.span(), ""));
                    semiEmittedInThisRun = true;
                }
                continue;
            }

            if (token.kind() == TokenKind.LPAREN) {
                depth++;
            } else if (token.kind() == TokenKind.RPAREN && depth > 0) {
                // Rule 52: never decrement below zero.
                depth--;
            }

            semiEmittedInThisRun = false;
            out.add(token);
            if (token.kind() != TokenKind.EOF) {
                previousSignificant = token;
            }
        }

        return new Result(out, List.of());
    }

    /**
     * Rule 53b: the eight token kinds after which a depth-zero newline terminates.
     *
     * <p>{@code SEMI} is deliberately absent: a line that already ended with an
     * explicit {@code ;} must not gain a second one from its line feed.
     */
    private static boolean triggersTermination(Token previous) {
        if (previous == null) {
            return false;
        }
        return switch (previous.kind()) {
            case IDENT, INT_LITERAL, STRING_LITERAL, TRUE, FALSE, RPAREN, RBRACE, RETURN -> true;
            default -> false;
        };
    }
}
