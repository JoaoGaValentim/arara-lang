package arara.lex;

/**
 * Token kinds.
 *
 * <p>The set is exactly {@code lexical.md} §3. Keywords and type names have their
 * own kinds rather than carrying a keyword flag, because the grammar dispatches on
 * them directly and the parser needs them distinguishable without a value lookup.
 */
public enum TokenKind {

    // Literals and identifiers
    IDENT,
    INT_LITERAL,
    STRING_LITERAL,

    // Keywords
    FUN("fun"),
    VAL("val"),
    VAR("var"),
    IF("if"),
    ELSE("else"),
    RETURN("return"),
    TRUE("true"),
    FALSE("false"),

    // Type names
    INT("Int"),
    BOOLEAN("Boolean"),
    STRING("String"),
    UNIT("Unit"),

    // Punctuation and operators
    LPAREN("("),
    RPAREN(")"),
    LBRACE("{"),
    RBRACE("}"),
    COLON(":"),
    COMMA(","),
    SEMI(";"),
    ASSIGN("="),
    EQ("=="),
    NE("!="),
    LT("<"),
    LE("<="),
    GT(">"),
    GE(">="),
    PLUS("+"),
    MINUS("-"),
    STAR("*"),
    SLASH("/"),
    PERCENT("%"),
    BANG("!"),
    ANDAND("&&"),
    OROR("||"),

    // Internal
    /** A line break that may trigger statement termination ({@code lexical.md} §10). */
    NEWLINE,
    /** A reserved word for a deferred feature ({@code lexical.md} §6). */
    RESERVED,
    EOF;

    private final String fixedText;

    TokenKind() {
        this.fixedText = null;
    }

    TokenKind(String fixedText) {
        this.fixedText = fixedText;
    }

    /** True when this kind always denotes the same source text. */
    public boolean hasFixedText() {
        return fixedText != null;
    }

    /** The source text of a fixed kind, or {@code null} for variable kinds. */
    public String fixedText() {
        return fixedText;
    }
}
