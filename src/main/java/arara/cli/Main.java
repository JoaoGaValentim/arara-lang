package arara.cli;

import arara.diag.Diagnostic;
import arara.diag.DiagnosticCodes;
import arara.diag.Severity;
import arara.lex.Lexer;
import arara.lex.Position;
import arara.lex.Separator;
import arara.lex.Token;
import arara.lex.TokenDump;
import arara.source.SourceReader;
import arara.source.SourceUnit;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Command-line entry point.
 *
 * <p>Implements only the D002 subset of {@code architecture.md} §6:
 * {@code arara lex <file.ara>...}. {@code parse}, {@code check}, {@code compile},
 * and {@code run} are not implemented and are rejected rather than stubbed, so an
 * unimplemented stage cannot report success.
 *
 * <p>Exit codes follow {@code architecture.md} §6: {@code 0} success, {@code 1}
 * user program failure, {@code 2} compile-time diagnostics, {@code 3} internal
 * error, {@code 4} verification failure.
 */
public final class Main {

    public static final int EXIT_OK = 0;
    public static final int EXIT_DIAGNOSTICS = 2;
    public static final int EXIT_USAGE = 2;

    private Main() {
    }

    public static void main(String[] args) {
        int status = run(args, System.out, System.err);
        System.exit(status);
    }

    /** Testable entry point: no {@link System#exit}. */
    public static int run(String[] args, PrintStream out, PrintStream err) {
        if (args.length == 0) {
            err.println("arara: no command given; expected 'lex'");
            return EXIT_USAGE;
        }

        String command = args[0];
        if (!command.equals("lex")) {
            if (command.equals("parse") || command.equals("check")
                    || command.equals("compile") || command.equals("run")) {
                err.println("arara: '" + command + "' is not implemented in D002-A"
                        + (command.equals("parse") ? "; it arrives in D002-B" : "; it belongs to a later milestone"));
                return EXIT_USAGE;
            }
            err.println("arara: unknown command '" + command + "'; expected 'lex'");
            return EXIT_USAGE;
        }

        List<String> files = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            files.add(args[i]);
        }
        if (files.isEmpty()) {
            // ARA-SRC-0001: an invocation with an empty argument list.
            err.println("arara lex: " + DiagnosticCodes.SRC_NO_INPUT_FILES
                    + ": no input files");
            return EXIT_DIAGNOSTICS;
        }

        boolean anyDiagnostic = false;
        boolean internalError = false;

        for (String file : files) {
            SourceUnit.ReadResult read = SourceReader.read(Path.of(file));
            List<Diagnostic> readDiagnostics = read.diagnostics();
            if (read.unit() == null) {
                printDiagnostics(readDiagnostics, null, err);
                anyDiagnostic = true;
                continue;
            }

            SourceUnit unit = read.unit();
            Position.Lookup lookup = Position.Lookup.of(unit.codePoints());

            Lexer.Result lexed = Lexer.lex(unit);
            Separator.Result separated = Separator.apply(lexed.tokens());

            List<Diagnostic> all = new ArrayList<>(readDiagnostics);
            all.addAll(lexed.diagnostics());

            if (!all.isEmpty()) {
                printDiagnostics(all, lookup, err);
                anyDiagnostic = true;
            }

            // A token dump is emitted only for a unit with no lexical diagnostic:
            // a partial stream is not a conforming result and must not be mistaken
            // for one by a golden comparison.
            if (all.isEmpty()) {
                out.print(TokenDump.render(separated.tokens()));
            }
        }

        if (internalError) {
            return 3;
        }
        return anyDiagnostic ? EXIT_DIAGNOSTICS : EXIT_OK;
    }

    private static void printDiagnostics(List<Diagnostic> diagnostics,
                                         Position.Lookup lookup,
                                         PrintStream err) {
        for (Diagnostic diagnostic : diagnostics) {
            if (lookup == null) {
                err.println(diagnostic.code() + ": " + diagnostic.message());
                continue;
            }
            Position start = lookup.at(diagnostic.span().start());
            Position end = lookup.at(diagnostic.span().end());
            String severity = diagnostic.severity() == Severity.ERROR ? "error" : "note";
            err.println(start.line() + ":" + start.column() + ": " + severity
                    + " [" + diagnostic.code() + "] " + diagnostic.message()
                    + " (" + start.line() + ":" + start.column()
                    + "-" + end.line() + ":" + end.column() + ")");
        }
    }

    /** Renders one lexed unit; used by tests that need a stable string. */
    public static String renderUnit(SourceUnit unit) {
        Lexer.Result lexed = Lexer.lex(unit);
        return TokenDump.render(Separator.apply(lexed.tokens()).tokens());
    }

    /** Convenience for tests: tokens of a unit after separation. */
    public static List<Token> tokensOf(SourceUnit unit) {
        return Separator.apply(Lexer.lex(unit).tokens()).tokens();
    }
}
