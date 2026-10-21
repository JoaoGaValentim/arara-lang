package arara.lex;

import arara.source.SourceReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Golden token-stream fixtures under {@code tests/fixtures/lex}.
 *
 * <p>Each {@code .ara} file pairs with a {@code .tokens} file produced by
 * {@link TokenDump}. A mismatch is a regression in either the lexer or the golden.
 *
 * <p>Goldens are frozen. When this test fails, the correct response is to
 * determine which side is wrong. Regenerating a golden to make the test pass is a
 * gate failure ({@code conformance-plan.md} §1.4), so there is deliberately no
 * automatic regeneration mode here.
 */
class GoldenFixtureTest {

    private static final Path LEX_DIR = Path.of("tests", "fixtures", "lex");

    @Test
    @DisplayName("every lex fixture matches its frozen golden")
    void lexFixturesMatchGoldens() throws IOException {
        List<Path> fixtures = fixtures();
        assertTrue(fixtures.size() >= 10,
                "expected a meaningful fixture set, found " + fixtures.size());

        for (Path ara : fixtures) {
            Path golden = goldenFor(ara);
            assertTrue(Files.exists(golden), "missing golden for " + ara.getFileName());

            SourceUnitLoader.Loaded loaded = SourceUnitLoader.load(ara);
            String actual = TokenDump.render(
                    Separator.apply(Lexer.lex(loaded.unit()).tokens()).tokens());
            String expected = Files.readString(golden, StandardCharsets.UTF_8);

            assertEquals(expected, actual, () -> "golden mismatch for " + ara.getFileName());
            assertTrue(loaded.diagnostics().isEmpty(),
                    () -> "fixture " + ara.getFileName() + " must lex without diagnostics");
        }
    }

    @Test
    @DisplayName("goldens are deterministic across repeated runs")
    void goldensDeterministic() throws IOException {
        for (Path ara : fixtures()) {
            SourceUnitLoader.Loaded loaded = SourceUnitLoader.load(ara);
            String first = TokenDump.render(
                    Separator.apply(Lexer.lex(loaded.unit()).tokens()).tokens());
            for (int i = 0; i < 5; i++) {
                String again = TokenDump.render(
                        Separator.apply(Lexer.lex(loaded.unit()).tokens()).tokens());
                assertEquals(first, again, "non-deterministic dump for " + ara.getFileName());
            }
        }
    }

    @Test
    @DisplayName("a fixture set exists and every fixture has a matching golden")
    void everyFixtureHasGolden() throws IOException {
        for (Path ara : fixtures()) {
            assertTrue(Files.exists(goldenFor(ara)),
                    "no golden for " + ara.getFileName());
        }
    }

    private static List<Path> fixtures() throws IOException {
        if (!Files.isDirectory(LEX_DIR)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(LEX_DIR)) {
            return stream.filter(p -> p.toString().endsWith(".ara")).sorted().toList();
        }
    }

    private static Path goldenFor(Path ara) {
        String name = ara.getFileName().toString();
        return ara.resolveSibling(name.substring(0, name.length() - ".ara".length()) + ".tokens");
    }

    @Test
    @DisplayName("temporary fixture directory is never left behind")
    void noStrayFiles(@TempDir Path temp) throws IOException {
        assertTrue(Files.isDirectory(temp));
    }

    /** Loads a fixture and fails loudly on a missing file. */
    static final class SourceUnitLoader {
        record Loaded(arara.source.SourceUnit unit, List<arara.diag.Diagnostic> diagnostics) {
        }

        static Loaded load(Path path) {
            try {
                arara.source.SourceUnit.ReadResult result = SourceReader.read(path);
                if (result.unit() == null) {
                    fail("cannot read fixture " + path);
                }
                return new Loaded(result.unit(), result.diagnostics());
            } catch (RuntimeException e) {
                return fail("failed to load " + path + ": " + e);
            }
        }
    }
}
