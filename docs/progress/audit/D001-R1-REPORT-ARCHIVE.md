# Archive — D001-R1 verification report (as delivered)

Status: **ARCHIVED VERBATIM**. Do not edit the quoted body. Corrections to the
record belong in `D001-AUDIT-R2.md` or in a new decision entry.

## Provenance

| Field | Value |
| --- | --- |
| Original deliverable | D001-R1 verification report, requirement 8 of Directive 001-R1 |
| Originally delivered | 2026-10-20, chat response (never committed to the repository) |
| Recovered from | Kilo session store `~/.local/share/kilo/kilo.db`, session `ses_ea6516f44ffeH7S27xXMICUPMc`, title "Arara naming decision and doc audit", single assistant `text` part, 2767 characters |
| Session model | `deepseek-ai/deepseek-v4.1-flash-1017`, provider `hive`, variant `max` |
| Session window | 2026-10-20T11:19:34−03:00 to 2026-10-20T12:29:17−03:00 |
| Session usage | 1,156,709 input tokens; 52,869 output tokens |
| Repository state at delivery | branch `docs/d001-r1-arara-identity`, HEAD `82a7afa`, clean tree |
| Recovered by | Directive 001-R2 audit, 2026-10-20 |
| Fidelity | Verbatim text of the assistant message. No wording or ordering altered. |

## Verification limitations of the archived text

1. **Not independent evidence.** Same limitation as the D001 archive: the report
   is the authoring agent's self-assessment.
2. **The "12 files, +332/−95" figure is correct** and is independently
   reproduced in `D001-AUDIT-R2.md` §3 against `717fcc6..ce3c67d`.
3. **The LL(1) claim was recomputed, not merely restated.** Directive 001-R2
   re-derived FIRST/FOLLOW from the committed grammar and reproduced
   "27 productions, 0 selection conflicts"; see `D001-AUDIT-R2.md` §4.
4. **The report asserts `.ara` is marked PROPOSED in six documents.** That count
   is correct for the *prose* claims but understates the surface: the extension
   also appears as a file suffix in commands and fixture paths, which the report
   does not distinguish. See `D001-AUDIT-R2.md` §10.
5. **The report does not record** that the D001 report itself was still not
   committed; that gap is what this archive closes.

## Archived text

The body below is the exact delivered text.

---

**D001-R1 VERIFICATION REPORT**

**Git evidence (VERIFIED, run at HEAD)**
- Branch: `docs/d001-r1-arara-identity` (branched from `main` at `717fcc6`)
- HEAD: `82a7afa93359848801388c5def63c2fd7ed0a147`
- Content commit: `ce3c67d` — 12 files, +332/−95
- Ledger pin commit: `82a7afa` — 2 files, +4/−4
- `git status --porcelain=v1 --untracked-files=all`: 0 lines
- `git remote -v`: only `origin`; `git ls-remote origin`: 0 refs (nothing pushed)
- `git log --oneline`: `82a7afa`, `ce3c67d`, `717fcc6` — no history rewritten, no merge, no tag

**Identity recorded**
`docs/progress/DECISIONS.md` `D001-06`: name **Arara**, repo `JoaoGaValentim/arara-lang`, `.ara` **PROPOSED**, JVM classfile 65 target, Kotlin surface-syntax inspiration only, semantics independently specified, `ARA-<STAGE>-<NNNN>` namespace retained. Identity tables added to `README.md:13` and `docs/charter.md:7`. `.ara` explicitly `PROPOSED` in 6 documents; `OQ-8` open.

**Naming audit (VERIFIED)**
23 tracked docs/config files scanned. No alternate project name anywhere; all prose already said "Arara". Defects fixed: missing decision entry, missing identity statements, and 12 code fences labelled `kotlin` (now `arara`). Remaining Kotlin mentions are all deliberate contrast statements, not naming.

**D001 report audit — unresolved owner decisions**
1. `OQ-8` — ratify `.ara` (still proposed).
2. `OQ-10` (new) — whether to forbid Java *restricted identifiers* (`var`, `yield`, `record`, `sealed`, `permits`, module words) as unit names. JVM accepts them (probed under `-Xverify:all`); `javac` rejects five of them.
3. `OQ-1`–`OQ-7`, `OQ-9` unchanged (visibility, push, spec acceptance, div-by-zero, shadowing, multi-unit, reserved words, license).
4. JVM compatibility: corrected ADR 0002 (`java.lang.classfile` absent on Corretto 21.0.12.1 — `ClassNotFoundException`); String-locals claim corrected in `architecture.md` / `semantics.md` §11.11. Remaining `UNVERIFIED`: other JDK 21 vendors/architectures, and runtime behavior under `-Xverify:none`.
5. Context recovery: recovery validated only with the same Kilo client and model; no compaction event has ever been observed. The D001 report itself was never committed, so it cannot be recovered from the repository. `CONTINUATION.md` branch drift and stale `GATES.md` section citations fixed.

**Scope compliance (VERIFIED)**
Zero `.java`/`.kt`/`.rs`/`.py`, zero `build.gradle*`/`pom.xml`/`gradlew`, zero `src/` directories. No push, merge, tag, release, or D002 work. Validations: `kilo config check` → `No config warnings.`; LL(1) check → `no selection conflicts`; 67 diagnostic codes, 67 table definitions, 0 undefined, 0 duplicates; 0 broken relative links; 0 trailing-whitespace lines.

D001 acceptance and D002 remain pending owner action.
