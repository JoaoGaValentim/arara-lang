package arara.lex;

import arara.diag.Diagnostic;
import arara.diag.DiagnosticCodes;
import arara.source.SourceUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * The Arara lexer.
 *
 * <p>Implements {@code lexical.md} §2–§9 and reports the §11 diagnostics. It
 * produces {@code NEWLINE} tokens rather than separators; {@link Separator}
 * performs the post-lexing transformation of §10.
 *
 * <p>Two rules govern how far lexing goes after an error:
 *
 * <ul>
 *   <li>{@code ARA-LEX-0201} (unterminated string literal) stops lexing of the
 *       unit, because the contract says so explicitly;
 *   <li>{@code ARA-LEX-0001} (invalid UTF-8) is already handled by
 *       {@link arara.source.SourceReader}, which truncates the text at the first
 *       invalid byte, so the lexer never sees it.
 * </ul>
 *
 * <p>Every other diagnostic is recoverable: the lexer reports it and continues,
 * so independent defects in one file each produce their own diagnostic rather
 * than a single early abort.
 *
 * <p>{@code ARA-LEX-0102} is never produced. {@code lexical.md} §11 marks it
 * reserved and forbids its use in conformance tests.
 */
public final class Lexer {

    /** Result of lexing one unit. */
    public record Result(List<Token> tokens, List<Diagnostic> diagnostics) {
        public Result {
            tokens = List.copyOf(tokens);
            diagnostics = List.copyOf(diagnostics);
        }
    }

    private static final int SUPPLEMENTARY_NONE = -1;

    private final int[] text;
    private final int length;
    private final List<Token> tokens = new ArrayList<>();
    private final List<Diagnostic> diagnostics = new ArrayList<>();
    private int pos;

    private Lexer(int[] text) {
        this.text = text;
        this.length = text.length;
    }

    /** Lexes a unit, producing raw tokens including {@code NEWLINE}. */
    public static Result lex(SourceUnit unit) {
        Lexer lexer = new Lexer(unit.codePoints());
        lexer.run();
        return new Result(lexer.tokens, lexer.diagnostics);
    }

    private void run() {
        boolean stop = false;
        while (pos < length && !stop) {
            int cp = text[pos];
            switch (cp) {
                case ' ', '\t', 0x0B, 0x0C -> pos++;
                case '\n' -> {
                    emit(TokenKind.NEWLINE, pos, pos + 1, "");
                    pos++;
                }
                case '/' -> stop = lexSlash();
                case '"' -> stop = lexString();
                case '&' -> stop = lexAmpersand();
                case '|' -> stop = lexPipe();
                default -> stop = lexOther(cp);
            }
        }
        emit(TokenKind.EOF, pos, pos, "");
    }

    /** Handles {@code /} including the two comment openers, which win by maximal munch. */
    private boolean lexSlash() {
        int start = pos;
        if (pos + 1 < length && text[pos + 1] == '/') {
            pos += 2;
            while (pos < length && text[pos] != '\n') {
                pos++;
            }
            return false;
        }
        if (pos + 1 < length && text[pos + 1] == '*') {
            int close = indexOfClose(start + 2);
            if (close < 0) {
                diagnostics.add(Diagnostic.of(DiagnosticCodes.LEX_UNTERMINATED_BLOCK_COMMENT,
                        new Span(start, start + 2),
                        "block comment is not terminated before end of file"));
                pos = length;
                return false;
            }
            pos = close + 2;
            return false;
        }
        emit(TokenKind.SLASH, start, start + 1, "");
        pos = start + 1;
        return false;
    }

    /** Finds the first {@code *}{@code /} at or after {@code from}; block comments do not nest. */
    private int indexOfClose(int from) {
        for (int i = from; i + 1 < length; i++) {
            if (text[i] == '*' && text[i + 1] == '/') {
                return i;
            }
        }
        return -1;
    }

    /**
     * Lexes a string literal.
     *
     * @return true when lexing of the unit must stop
     */
    private boolean lexString() {
        int start = pos;
        pos++;
        StringBuilder value = new StringBuilder();
        while (true) {
            if (pos >= length) {
                diagnostics.add(Diagnostic.of(DiagnosticCodes.LEX_UNTERMINATED_STRING,
                        new Span(start, start + 1),
                        "string literal is not terminated before end of file"));
                pos = length;
                return true;
            }
            int cp = text[pos];
            if (cp == '"') {
                pos++;
                emit(TokenKind.STRING_LITERAL, start, pos, value.toString());
                return false;
            }
            if (cp == '\n') {
                // Recovery: discard the partial literal and resume at the line break.
                diagnostics.add(Diagnostic.of(DiagnosticCodes.LEX_RAW_LINE_BREAK_IN_STRING,
                        new Span(pos, pos + 1),
                        "raw line break inside a string literal"));
                return false;
            }
            if (cp == '\\') {
                if (pos + 1 >= length) {
                    diagnostics.add(Diagnostic.of(DiagnosticCodes.LEX_UNTERMINATED_STRING,
                            new Span(start, start + 1),
                            "string literal is not terminated before end of file"));
                    pos = length;
                    return true;
                }
                int next = text[pos + 1];
                int decoded = switch (next) {
                    case 'n' -> '\n';
                    case 'r' -> '\r';
                    case 't' -> '\t';
                    case '\\' -> '\\';
                    case '"' -> '"';
                    default -> SUPPLEMENTARY_NONE;
                };
                if (decoded == SUPPLEMENTARY_NONE) {
                    diagnostics.add(Diagnostic.of(DiagnosticCodes.LEX_INVALID_ESCAPE,
                            new Span(pos, pos + 2),
                            "invalid escape sequence"));
                    // Recovery: skip the backslash and the following character.
                    pos += 2;
                    continue;
                }
                value.appendCodePoint(decoded);
                pos += 2;
                continue;
            }
            if (isForbiddenControl(cp)) {
                diagnostics.add(Diagnostic.of(DiagnosticCodes.LEX_FORBIDDEN_CONTROL_CHAR,
                        new Span(pos, pos + 1),
                        "control character is not permitted inside a string literal"));
                pos++;
                continue;
            }
            value.appendCodePoint(cp);
            pos++;
        }
    }

    /** True for C0 controls other than tab, plus DEL ({@code lexical.md} §42). */
    private static boolean isForbiddenControl(int cp) {
        if (cp == 0x7F) {
            return true;
        }
        return cp < 0x20 && cp != '\t';
    }

    private boolean lexAmpersand() {
        int start = pos;
        if (pos + 1 < length && text[pos + 1] == '&') {
            emit(TokenKind.ANDAND, start, start + 2, "");
            pos = start + 2;
            return false;
        }
        diagnostics.add(Diagnostic.of(DiagnosticCodes.LEX_NO_TOKEN_STARTS_HERE,
                new Span(start, start + 1), "'&' does not begin a token"));
        pos = start + 1;
        return false;
    }

    private boolean lexPipe() {
        int start = pos;
        if (pos + 1 < length && text[pos + 1] == '|') {
            emit(TokenKind.OROR, start, start + 2, "");
            pos = start + 2;
            return false;
        }
        diagnostics.add(Diagnostic.of(DiagnosticCodes.LEX_NO_TOKEN_STARTS_HERE,
                new Span(start, start + 1), "'|' does not begin a token"));
        pos = start + 1;
        return false;
    }

    private boolean lexOther(int cp) {
        int start = pos;

        if (isIdentifierStart(cp)) {
            pos++;
            while (pos < length && isIdentifierPart(text[pos])) {
                pos++;
            }
            String word = new String(text, start, pos - start);
            TokenKind keyword = Words.KEYWORDS.get(word);
            if (keyword != null) {
                emit(keyword, start, pos, word);
                return false;
            }
            if (Words.isReserved(word)) {
                emit(TokenKind.RESERVED, start, pos, word);
                diagnostics.add(Diagnostic.of(DiagnosticCodes.LEX_RESERVED_WORD,
                        new Span(start, pos),
                        "'" + word + "' is reserved for a deferred feature"));
                return false;
            }
            emit(TokenKind.IDENT, start, pos, word);
            return false;
        }

        if (cp >= '0' && cp <= '9') {
            pos++;
            while (pos < length && text[pos] >= '0' && text[pos] <= '9') {
                pos++;
            }
            String digits = new String(text, start, pos - start);
            if (digits.length() > 1 && digits.charAt(0) == '0') {
                diagnostics.add(Diagnostic.of(DiagnosticCodes.LEX_LEADING_ZERO,
                        new Span(start, pos),
                        "multi-digit integer literal with a leading zero"));
            }
            if (exceedsBound(digits)) {
                diagnostics.add(Diagnostic.of(DiagnosticCodes.LEX_INT_LITERAL_TOO_LARGE,
                        new Span(start, pos),
                        "integer literal exceeds 2147483648"));
            }
            emit(TokenKind.INT_LITERAL, start, pos, digits);
            return false;
        }

        TokenKind single = singleCharToken(cp);
        if (single != null) {
            // Two-character operators win by maximal munch.
            TokenKind pair = pairToken(cp, start + 1 < length ? text[start + 1] : -1);
            if (pair != null) {
                emit(pair, start, start + 2, "");
                pos = start + 2;
                return false;
            }
            emit(single, start, start + 1, "");
            pos = start + 1;
            return false;
        }

        if (isPermittedOutsideToken(cp)) {
            // Whitespace other than space and tab is handled in run(); reaching
            // here means a permitted character that begins no token.
            diagnostics.add(Diagnostic.of(DiagnosticCodes.LEX_NO_TOKEN_STARTS_HERE,
                    new Span(start, start + 1), "character does not begin a token"));
            pos = start + 1;
            return false;
        }

        diagnostics.add(Diagnostic.of(DiagnosticCodes.LEX_CHAR_NOT_PERMITTED,
                new Span(start, start + 1), "character is not permitted outside a string or comment"));
        pos = start + 1;
        return false;
    }

    /** The one-token form of an operator or punctuation character, if any. */
    private static TokenKind singleCharToken(int cp) {
        return switch (cp) {
            case '(' -> TokenKind.LPAREN;
            case ')' -> TokenKind.RPAREN;
            case '{' -> TokenKind.LBRACE;
            case '}' -> TokenKind.RBRACE;
            case ':' -> TokenKind.COLON;
            case ',' -> TokenKind.COMMA;
            case ';' -> TokenKind.SEMI;
            case '=' -> TokenKind.ASSIGN;
            case '<' -> TokenKind.LT;
            case '>' -> TokenKind.GT;
            case '+' -> TokenKind.PLUS;
            case '-' -> TokenKind.MINUS;
            case '*' -> TokenKind.STAR;
            case '%' -> TokenKind.PERCENT;
            case '!' -> TokenKind.BANG;
            default -> null;
        };
    }

    /** The two-character operator formed by {@code first} followed by {@code next}, if any. */
    private static TokenKind pairToken(int first, int next) {
        if (next < 0) {
            return null;
        }
        return switch (first) {
            case '=' -> next == '=' ? TokenKind.EQ : null;
            case '!' -> next == '=' ? TokenKind.NE : null;
            case '<' -> next == '=' ? TokenKind.LE : null;
            case '>' -> next == '=' ? TokenKind.GE : null;
            default -> null;
        };
    }

    private static boolean isIdentifierStart(int cp) {
        return (cp >= 'A' && cp <= 'Z') || (cp >= 'a' && cp <= 'z') || cp == '_';
    }

    private static boolean isIdentifierPart(int cp) {
        return isIdentifierStart(cp) || (cp >= '0' && cp <= '9');
    }

    /**
     * True for printable ASCII, which is the only region where a "no token"
     * character is reported as {@code ARA-LEX-0103} rather than
     * {@code ARA-LEX-0101}.
     */
    private static boolean isPermittedOutsideToken(int cp) {
        return cp >= 0x21 && cp <= 0x7E;
    }

    /**
     * True when the literal's value exceeds 2147483648.
     *
     * <p>Compared as text after removing leading zeros so that a long run of zeros
     * followed by a small value is not misreported as overflow.
     */
    private static boolean exceedsBound(String digits) {
        int first = 0;
        while (first < digits.length() && digits.charAt(first) == '0') {
            first++;
        }
        String significant = digits.substring(first);
        if (significant.isEmpty()) {
            return false;
        }
        if (significant.length() != 10) {
            return significant.length() > 10;
        }
        return significant.compareTo("2147483648") > 0;
    }

    private void emit(TokenKind kind, int start, int end, String value) {
        tokens.add(new Token(kind, new Span(start, end), value));
    }
}
