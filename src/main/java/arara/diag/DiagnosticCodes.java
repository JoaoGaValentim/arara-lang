package arara.diag;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The diagnostic codes this milestone can produce.
 *
 * <p>Only codes defined by the frozen contract appear here. The list is
 * deliberately explicit rather than derived from a range, so a typo in a lexer
 * branch fails a test instead of silently producing an unregistered code.
 *
 * <p>{@code ARA-LEX-0102} is listed because the contract documents it, but
 * {@code lexical.md} §11 marks it reserved and states that a conforming
 * implementation MUST NOT produce it. {@link #NEVER_EMITTED} records that fact so
 * {@code D002-G5} can assert its absence rather than its presence.
 */
public final class DiagnosticCodes {

    public static final String LEX_INVALID_UTF8 = "ARA-LEX-0001";
    public static final String LEX_CHAR_NOT_PERMITTED = "ARA-LEX-0101";
    /** Reserved; must never be emitted. See {@code lexical.md} §11 note. */
    public static final String LEX_INVALID_IDENTIFIER = "ARA-LEX-0102";
    public static final String LEX_NO_TOKEN_STARTS_HERE = "ARA-LEX-0103";
    public static final String LEX_UNTERMINATED_STRING = "ARA-LEX-0201";
    public static final String LEX_INVALID_ESCAPE = "ARA-LEX-0202";
    public static final String LEX_RAW_LINE_BREAK_IN_STRING = "ARA-LEX-0203";
    public static final String LEX_INT_LITERAL_TOO_LARGE = "ARA-LEX-0204";
    public static final String LEX_LEADING_ZERO = "ARA-LEX-0205";
    public static final String LEX_FORBIDDEN_CONTROL_CHAR = "ARA-LEX-0206";
    public static final String LEX_UNTERMINATED_BLOCK_COMMENT = "ARA-LEX-0301";
    public static final String LEX_RESERVED_WORD = "ARA-LEX-0401";

    public static final String SRC_NO_INPUT_FILES = "ARA-SRC-0001";
    public static final String SRC_UNREADABLE_INPUT = "ARA-SRC-0002";

    /** All lexical codes documented by the contract, including the reserved one. */
    public static final Set<String> LEXICAL = Set.of(
            LEX_INVALID_UTF8,
            LEX_CHAR_NOT_PERMITTED,
            LEX_INVALID_IDENTIFIER,
            LEX_NO_TOKEN_STARTS_HERE,
            LEX_UNTERMINATED_STRING,
            LEX_INVALID_ESCAPE,
            LEX_RAW_LINE_BREAK_IN_STRING,
            LEX_INT_LITERAL_TOO_LARGE,
            LEX_LEADING_ZERO,
            LEX_FORBIDDEN_CONTROL_CHAR,
            LEX_UNTERMINATED_BLOCK_COMMENT,
            LEX_RESERVED_WORD);

    /** Lexical codes a conforming implementation must never produce. */
    public static final Set<String> NEVER_EMITTED = Set.of(LEX_INVALID_IDENTIFIER);

    /** Lexical codes the lexer is expected to produce. */
    public static final Set<String> LEXICAL_PRODUCIBLE = producible();

    private DiagnosticCodes() {
    }

    private static Set<String> producible() {
        Set<String> codes = new LinkedHashSet<>(List.copyOf(LEXICAL));
        codes.removeAll(NEVER_EMITTED);
        return Set.copyOf(codes);
    }
}
