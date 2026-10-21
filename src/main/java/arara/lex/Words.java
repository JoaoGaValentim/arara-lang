package arara.lex;

import java.util.Map;
import java.util.Set;

/**
 * The keyword and reserved-word tables from {@code lexical.md} §6.
 *
 * <p>Both tables are transcribed from the contract rather than derived, so a
 * change to either is a visible edit that requires a superseding decision
 * ({@code D001-17}).
 */
public final class Words {

    /** The twelve keywords, with exact capitalization ({@code lexical.md} §29). */
    public static final Map<String, TokenKind> KEYWORDS = Map.ofEntries(
            Map.entry("fun", TokenKind.FUN),
            Map.entry("val", TokenKind.VAL),
            Map.entry("var", TokenKind.VAR),
            Map.entry("if", TokenKind.IF),
            Map.entry("else", TokenKind.ELSE),
            Map.entry("return", TokenKind.RETURN),
            Map.entry("true", TokenKind.TRUE),
            Map.entry("false", TokenKind.FALSE),
            Map.entry("Int", TokenKind.INT),
            Map.entry("Boolean", TokenKind.BOOLEAN),
            Map.entry("String", TokenKind.STRING),
            Map.entry("Unit", TokenKind.UNIT));

    /**
     * Words reserved for deferred features ({@code lexical.md} §31).
     *
     * <p>Each produces {@code ARA-LEX-0401} wherever it occurs, including in a
     * position where an identifier would otherwise be legal.
     */
    public static final Set<String> RESERVED = Set.of(
            "class", "object", "interface", "enum", "this", "super", "new", "init",
            "constructor",
            "when",
            "while", "for", "do", "break", "continue",
            "try", "catch", "finally", "throw",
            "null",
            "package", "import",
            "typealias",
            "suspend",
            "annotation",
            "in", "is", "as");

    private Words() {
    }

    /** True when the identifier is a keyword. */
    public static boolean isKeyword(String text) {
        return KEYWORDS.containsKey(text);
    }

    /** True when the identifier is reserved for a deferred feature. */
    public static boolean isReserved(String text) {
        return RESERVED.contains(text);
    }
}
