package arara.diag;

/**
 * Diagnostic severity.
 *
 * <p>{@code lexical.md} §11 states that every lexical diagnostic in the initial
 * subset is {@code ERROR}. {@code NOTE} exists because {@code ARA-PARSE-0199} is
 * specified as a note marking the diagnostic ceiling ({@code conformance-plan.md}
 * §6a).
 */
public enum Severity {
    ERROR,
    NOTE
}
