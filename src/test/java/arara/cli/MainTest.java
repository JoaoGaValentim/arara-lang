package arara.cli;

import arara.diag.DiagnosticCodes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CLI contract: {@code architecture.md} §6 exit codes and {@code D002-G11}'s
 * requirement that {@code lex} exposes tokens without compiling.
 */
class MainTest {

    private record Run(int status, String out, String err) {
    }

    private static Run invoke(String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int status;
        try (PrintStream outStream = new PrintStream(out, true, StandardCharsets.UTF_8);
             PrintStream errStream = new PrintStream(err, true, StandardCharsets.UTF_8)) {
            status = Main.run(args, outStream, errStream);
        }
        return new Run(status, out.toString(StandardCharsets.UTF_8), err.toString(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("no arguments is a usage error with exit status 2")
    void noArgs() {
        Run result = invoke();
        assertEquals(2, result.status());
        assertTrue(result.err().contains("expected 'lex'"));
    }

    @Test
    @DisplayName("lex with no files is ARA-SRC-0001 and exit status 2")
    void noInputFiles() {
        Run result = invoke("lex");
        assertEquals(2, result.status());
        assertTrue(result.err().contains(DiagnosticCodes.SRC_NO_INPUT_FILES));
    }

    @Test
    @DisplayName("an unimplemented subcommand is rejected rather than stubbed")
    void unimplementedCommands() {
        for (String command : new String[] {"check", "compile", "run"}) {
            Run result = invoke(command, "X.ara");
            assertEquals(2, result.status(), command + " must not silently succeed");
            assertTrue(result.err().contains("not implemented"), command);
        }
    }

    @Test
    @DisplayName("parse is rejected in D002-A and names D002-B as its milestone")
    void parseNotYet() {
        Run result = invoke("parse", "X.ara");
        assertEquals(2, result.status());
        assertTrue(result.err().contains("D002-B"));
    }

    @Test
    @DisplayName("an unknown command is rejected")
    void unknownCommand() {
        Run result = invoke("frobnicate", "X.ara");
        assertEquals(2, result.status());
        assertTrue(result.err().contains("unknown command"));
    }

    @Test
    @DisplayName("a clean file dumps tokens to stdout and exits 0")
    void cleanFile(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("Sample.ara");
        Files.writeString(file, "val x: Int = 1\n", StandardCharsets.UTF_8);
        Run result = invoke("lex", file.toString());
        assertEquals(0, result.status(), result.err());
        assertTrue(result.out().contains("VAL(val)@0:3"));
        assertEquals("", result.err());
    }

    @Test
    @DisplayName("a file with a lexical error reports to stderr and exits 2")
    void lexicalError(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("Bad.ara");
        Files.writeString(file, "val x: Int = $", StandardCharsets.UTF_8);
        Run result = invoke("lex", file.toString());
        assertEquals(2, result.status());
        assertTrue(result.err().contains(DiagnosticCodes.LEX_NO_TOKEN_STARTS_HERE));
        assertEquals("", result.out(), "no token dump may be emitted for a failing unit");
    }

    @Test
    @DisplayName("a missing file is ARA-SRC-0002 and does not suppress other units")
    void missingFile(@TempDir Path dir) throws Exception {
        Path good = dir.resolve("Good.ara");
        Files.writeString(good, "val x: Int = 1\n", StandardCharsets.UTF_8);
        Run result = invoke("lex", dir.resolve("Absent.ara").toString(), good.toString());
        assertEquals(2, result.status());
        assertTrue(result.err().contains(DiagnosticCodes.SRC_UNREADABLE_INPUT));
        assertTrue(result.out().contains("VAL(val)"), "the readable unit must still be lexed");
    }

    @Test
    @DisplayName("multiple clean files dump independently")
    void multiFileIsolation(@TempDir Path dir) throws Exception {
        Path a = dir.resolve("A.ara");
        Path b = dir.resolve("B.ara");
        Files.writeString(a, "val x: Int = 1\n", StandardCharsets.UTF_8);
        Files.writeString(b, "var y: Int = 2\n", StandardCharsets.UTF_8);
        Run result = invoke("lex", a.toString(), b.toString());
        assertEquals(0, result.status(), result.err());
        assertTrue(result.out().contains("VAL(val)"));
        assertTrue(result.out().contains("VAR(var)"));
    }

    @Test
    @DisplayName("diagnostics carry line and column in the documented format")
    void diagnosticPositions(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("Pos.ara");
        // The dollar sign is on line 2, column 5.
        Files.writeString(file, "val x\n    $", StandardCharsets.UTF_8);
        Run result = invoke("lex", file.toString());
        assertEquals(2, result.status());
        assertTrue(result.err().contains("2:5"), result.err());
    }
}
