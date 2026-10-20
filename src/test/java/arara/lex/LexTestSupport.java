package arara.lex;

import arara.source.SourceReader;
import arara.source.SourceUnit;

import java.nio.charset.StandardCharsets;
import java.util.List;

/** Shared helpers for lexical tests. */
public final class LexTestSupport {

    private LexTestSupport() {
    }

    /** Builds a unit from a Java string, encoding it as UTF-8. */
    public static SourceUnit unit(String text) {
        return SourceReader.fromBytes("Test", "<test>", text.getBytes(StandardCharsets.UTF_8)).unit();
    }

    /** Builds a unit from raw bytes, for invalid-encoding cases. */
    public static SourceUnit unit(byte[] bytes) {
        return SourceReader.fromBytes("Test", "<test>", bytes).unit();
    }

    /** Diagnostics produced while reading, before lexing. */
    public static List<arara.diag.Diagnostic> readDiagnostics(byte[] bytes) {
        return SourceReader.fromBytes("Test", "<test>", bytes).diagnostics();
    }

    /** Raw tokens including NEWLINE, excluding any transformation. */
    public static List<Token> rawTokens(String text) {
        return Lexer.lex(unit(text)).tokens();
    }

    /** Tokens after statement separation, which is what the contract calls a token stream. */
    public static List<Token> tokens(String text) {
        return Separator.apply(Lexer.lex(unit(text)).tokens()).tokens();
    }

    /** Tokens after separation for an already-decoded unit. */
    public static List<Token> tokens(SourceUnit unit) {
        return Separator.apply(Lexer.lex(unit).tokens()).tokens();
    }

    /** Lexical diagnostics only. */
    public static List<arara.diag.Diagnostic> diagnostics(String text) {
        return Lexer.lex(unit(text)).diagnostics();
    }

    /** The single diagnostic code, failing the test when the count is not exactly one. */
    public static String singleCode(String text) {
        List<arara.diag.Diagnostic> found = diagnostics(text);
        if (found.size() != 1) {
            throw new AssertionError("expected exactly one diagnostic, got " + found.size()
                    + ": " + found.stream().map(arara.diag.Diagnostic::code).toList());
        }
        return found.get(0).code();
    }

    /** Renders tokens as kinds only, which keeps assertions readable. */
    public static List<TokenKind> kinds(String text) {
        return tokens(text).stream().map(Token::kind).toList();
    }
}
