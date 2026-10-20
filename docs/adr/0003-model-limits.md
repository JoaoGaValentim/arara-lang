# ADR 0003 — Model context limit metadata for compaction

Status: **PROPOSED** (D001). Becomes accepted only when the owner accepts D001.
Date: 2026-10-20
Supersedes: none

## Context

Kilo enables automatic compaction from model metadata. The custom providers
`robin` and `hive` declare `deepseek-ai/deepseek-v4.1-flash-1017` without
`limit.context` or `limit.output`. Kilo reports both as `0`. A zero context
window can prevent or break automatic compaction, defeating Layer A of the
context-protection design.

Provider-verified values do not exist in a documented form: the Hive
`/models` endpoint returns only identifiers and creation times, and neither
models.dev nor the provider exposes an explicit context or output limit for this
model ID.

## Decision

Set explicit model limits for `deepseek-ai/deepseek-v4.1-flash-1017` under both
custom providers in `.kilo/kilo.jsonc`:

```jsonc
"limit": { "context": 1048576, "output": 1048576 }
```

These values are **measured empirically against the live provider endpoint**, not
invented and not taken from another vendor's listing. The probe used the API key
already stored by this environment for the `hive` provider and POSTed to
`https://api-cdn.thehive.ai/api/v3/chat/completions`.

Request shape (one line, JSON):

```json
{"model":"deepseek-ai/deepseek-v4.1-flash-1017",
 "messages":[{"role":"user","content":"hi"}],
 "max_tokens":<PROBE>}
```

| `max_tokens` probe | Observed | Provider `prompt_tokens` | Provider `completion_tokens` |
| --- | --- | --- | --- |
| 393216 | HTTP 200 | 31 | 46 |
| 524288 | HTTP 200 | 31 | 31 |
| 786432 | HTTP 200 | 31 | 46 |
| 917504 | HTTP 200 | 31 | 31 |
| 1000000 | HTTP 200 | 31 | 31 |
| 1048544 | HTTP 200 | 31 | 40 |
| 1048576 | HTTP 400 | — | — |
| 999999999 | HTTP 400 | — | — |

The HTTP 400 body was exactly
`{"status_code":400,"message":"Please reduce the length of the messages or completion"}`.

`prompt_tokens` was 31 because the tokenizer counts the chat envelope; no
prompt content large enough to move the boundary was sent. The accepted boundary
is therefore `prompt_tokens + max_tokens <= 1048576`, i.e. a 1048576-token
context window.

A separate probe sent `"reasoning_effort":"max"` and received HTTP 200 with a
non-empty `reasoning_content` field and
`usage.completion_tokens_details.reasoning_tokens: 12`, confirming that the
provider accepts the highest declared variant.

No separate output cap was observed below the context window, so `output` is set
to the context value. Effective output remains bounded by the remaining context at
request time.

What this probe does **not** establish: that the provider guarantees 1048576
forever, that Kilo's own token accounting matches the provider's, or that a
different model ID honors the same boundary.

## Rationale

1. A non-zero context window is required for compaction to function at all.
2. The measured value is reproducible with the recorded probe.
3. Setting `output` to the context window does not over-permit: the provider
   rejects over-allocation at request time.
4. The alternative — leaving `0` — is known-broken for compaction.

## Risks and limits

- This is a provider-behavior measurement, not a published contract. If the
  provider changes its limit, compaction may over- or under-estimate. The probe
  must be re-run when the provider changes model metadata or after a provider
  migration.
- The measurement used a 31-token prompt. It establishes the sum constraint, not
  a per-direction split.
- Kilo's own accounting may add overhead tokens beyond the raw chat payload; the
  70% compaction threshold leaves substantial margin.

## Compliance

- `.kilo/kilo.jsonc` carries the values and a comment block explaining their
  provenance.
- `docs/progress/STATE.md` records the probe commands and results.
- The probe is reproducible by anyone with a valid provider key: it is a single
  `curl` POST and the table above is its complete result set.
- This ADR is `PROPOSED`; the configuration is active but not contractually
  frozen until the owner accepts D001.
