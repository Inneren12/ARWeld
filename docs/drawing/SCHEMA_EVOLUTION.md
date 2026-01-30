# Drawing2D + Artifact Schema Evolution Policy

## Purpose
This policy defines how Drawing2D and Drawing2D artifact schemas evolve without breaking determinism, hashing, or offline export workflows. It applies to:
- Drawing2D root schema (`Drawing2D`, layers, entities, attachments).
- Drawing2D patch events.
- Artifact manifests and project layout metadata.

All requirements use RFC 2119 language (MUST/SHOULD/MAY).

## Versioning model
- `schemaVersion` is an integer and is the single **breaking-change indicator** for each schema family.
- `schemaVersion` MUST be incremented for any breaking change to the JSON shape or semantics.
- Patch-level releases (no `schemaVersion` bump) MUST preserve canonical ordering, defaults, and deterministic serialization.

## What is a breaking change
A change is **breaking** if any of the following are true:
- A required field is added without a default that can be derived deterministically.
- A field type or meaning changes (e.g., `units` meaning changes from `PX` to `MM`).
- Validation rules that previously accepted valid data now reject it (without a new schemaVersion).
- Canonical ordering rules change (affecting hashes and deterministic exports).
- Artifact layout paths or naming rules change for existing kinds or files.

## Backward compatibility rules (new writers → old readers)
When adding new data within the same `schemaVersion`:
- New fields MUST be optional **and** MUST have deterministic defaults defined in the schema docs.
- Writers MUST encode defaults (`encodeDefaults = true`) so old readers can reconstruct the same canonical form.
- Enums MAY add new values, but writers MUST NOT emit new enum values unless `schemaVersion` is bumped.
- New entity `type` values MUST NOT be emitted without a `schemaVersion` bump.
- Validation rules MAY become stricter only when `schemaVersion` is bumped.

## Forward compatibility rules (old readers → new writers)
- Canonical parsers MUST keep `ignoreUnknownKeys = false` to preserve deterministic validation and hashing.
- A reader MAY implement a **tolerant** mode that ignores unknown keys for best-effort viewing, but:
  - The tolerant mode MUST NOT be used for canonicalization, hashing, or exports.
  - Unknown fields MUST be treated as lossy; re-serialization in the same schemaVersion is not guaranteed.
- If forward compatibility is required, a new schemaVersion MUST be introduced with explicit downgrade/export guidance.

## Determinism guarantees (stable across patch versions)
These invariants MUST remain stable across non-breaking releases:
- Canonical JSON configuration (class discriminator, explicit null policy, default encoding).
- Canonical ordering rules for Drawing2D, patch events, and artifact manifests.
- Hashing algorithms and checksum file ordering.
- Stable identity fields (`drawingId`, `layer.id`, `entity.id`) and their uniqueness constraints.

Any change that affects deterministic serialization or hashing MUST trigger a `schemaVersion` bump.

## Adding fields safely
When adding a new field within the same `schemaVersion`:
- The field MUST be optional.
- The field MUST have a deterministic default value that is identical across all writers.
- The field MUST be documented with its default and its impact on canonical ordering (if any).
- Writers MUST include the field in canonical JSON only when it is non-default (unless the schema mandates encodeDefaults for that type).

## Deprecation strategy
- Deprecations MUST be documented for at least one minor release before removal.
- Deprecated fields MUST continue to be accepted and validated until the next `schemaVersion` bump.
- Writers SHOULD stop emitting deprecated fields as soon as all readers can ignore or tolerate them.
- Removal of deprecated fields is a breaking change and MUST bump `schemaVersion`.

## Artifact layout evolution
- New artifact kinds MAY be added, but existing `relPath` conventions for existing kinds MUST remain stable across patch versions.
- `relPath` for existing kinds MUST NOT change without a `schemaVersion` bump.
- New layout directories MAY be introduced, but MUST NOT rename or relocate existing paths in the same schemaVersion.
- Manifest ordering rules and checksum file ordering MUST remain stable across patch versions.

## Migration and dual-emit guidance (v1 → v2)
When a `schemaVersion` bump is required:
- Writers SHOULD offer a dual-emit mode that can produce both v1 and v2 outputs for a transition period.
- v2 writers SHOULD include a downgrade/export path that emits v1 when possible (dropping unsupported data explicitly).
- A v2-to-v1 downgrade MUST be documented with a **loss profile** (which fields are dropped or defaulted).
- v2 readers SHOULD accept v1 inputs and either:
  - Convert to v2 in-memory while preserving v1 semantics, or
  - Keep the data tagged as v1 and avoid emitting v2-only fields on re-export.

## Compatibility checklist (quick reference)
- ✅ Adding optional fields with deterministic defaults → allowed within same `schemaVersion`.
- ✅ Adding new artifact kinds → allowed within same `schemaVersion` if existing paths remain stable.
- ❌ Changing canonical ordering → MUST bump `schemaVersion`.
- ❌ Changing units/coordinate semantics → MUST bump `schemaVersion`.
- ❌ Removing or renaming existing fields → MUST bump `schemaVersion`.

## Related specs
- Drawing2D v1 schema: [DRAWING2D_V1.md](./DRAWING2D_V1.md)
