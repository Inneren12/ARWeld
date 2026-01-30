# Drawing2D v1 Specification

## Overview
Drawing2D v1 is the canonical 2D drawing schema used for AR alignment, inspection overlays, and 2D→3D pipelines. It defines a stable JSON shape, a deterministic serialization contract, and an artifact bundle layout for storage and export. The authoritative schema lives in `core-drawing2d` and is validated by the v1 validator before use.【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/v1/Drawing2D.kt†L1-L36】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/validation/DrawingValidatorV1.kt†L1-L262】

## Non-goals
- No rendering or styling definitions beyond `styleId` references.
- No 3D geometry, camera calibration, or AR runtime state.
- No server sync or multi-author conflict resolution (handled in later versions).

## Coordinate space + units
Drawing2D v1 uses rectified pixel coordinates with a fixed origin and axis orientation:
- **Units:** `PX` (pixels) only in v1.
- **Coordinate space type:** `RECTIFIED_PX`.
- **Origin:** top-left of the page.
- **Axes:** X increases to the right, Y increases downward.【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/v1/UnitsV1.kt†L1-L16】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/v1/CoordSpaceV1.kt†L1-L86】

## Drawing2D root schema
Top-level JSON object (all fields are required unless marked optional):

| Field | Type | Notes |
| --- | --- | --- |
| `schemaVersion` | integer | Must be `1` for v1. Default encoded value is `1`. |
| `drawingId` | string | Stable identifier; non-blank. |
| `rev` | integer | Non-negative revision counter. |
| `units` | enum | `PX` only. |
| `coordSpace` | object | `RECTIFIED_PX`, top-left origin, X→right, Y→down. |
| `page` | object | `widthPx`, `heightPx` > 0. |
| `layers` | array | Layer definitions, non-empty. |
| `entities` | array | Geometry + annotations. |
| `attachments` | array | Optional references to stored artifacts. |

Schema source: `Drawing2D` with related v1 types in `core-drawing2d` (see file references below).【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/v1/Drawing2D.kt†L1-L36】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/v1/PageV1.kt†L1-L19】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/v1/LayerV1.kt†L1-L25】

**Minimal root example** (short and readable):
```json
{
  "schemaVersion": 1,
  "drawingId": "dwg-001",
  "rev": 0,
  "units": "PX",
  "coordSpace": {
    "type": "RECTIFIED_PX",
    "origin": "TOP_LEFT",
    "axis": { "x": "RIGHT", "y": "DOWN" }
  },
  "page": { "widthPx": 1280, "heightPx": 720 },
  "layers": [
    { "id": "layer-0", "name": "Base", "order": 0 }
  ],
  "entities": [
    { "type": "line", "id": "e-1", "layerId": "layer-0", "styleId": null,
      "a": { "x": 10, "y": 10 }, "b": { "x": 100, "y": 80 } }
  ],
  "attachments": []
}
```

## Entity catalog (type discriminator values)
Entities are serialized with `type` as the discriminator field (from the configured JSON serializer). Each entity includes the common fields `id`, `layerId`, and optional `styleId`.【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/Drawing2DJson.kt†L1-L67】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/v1/entities/EntityV1.kt†L1-L12】

| `type` | Fields | Notes |
| --- | --- | --- |
| `line` | `a`, `b` | Segment between two points. |
| `polyline` | `points`, `closed` | Ordered points; `closed=true` means looped. |
| `arc` | `c`, `r`, `startAngleDeg`, `endAngleDeg`, `cw` | Center + radius + sweep. |
| `circle` | `c`, `r` | Center + radius. |
| `dimension` | `kind`, `p1`, `p2`, `text?`, `offsetPx?` | `kind` is `LINEAR`. |
| `text` | `anchor`, `value`, `rotationDeg` | Simple anchored text. |
| `tag` | `targetId`, `key`, `value` | Key/value attached to another entity. |
| `group` | `members` | Entity id references. |

Entity definitions: `core-drawing2d/src/main/kotlin/.../v1/entities/*.kt`.【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/v1/entities/LineV1.kt†L1-L19】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/v1/entities/PolylineV1.kt†L1-L20】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/v1/entities/ArcV1.kt†L1-L21】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/v1/entities/CircleV1.kt†L1-L19】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/v1/entities/DimensionV1.kt†L1-L29】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/v1/entities/TextV1.kt†L1-L20】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/v1/entities/TagV1.kt†L1-L19】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/v1/entities/GroupV1.kt†L1-L19】

## Attachments (optional)
`attachments` list references related artifacts by relative path and optional checksum:
- `kind` values: `RAW_IMAGE`, `RECTIFIED_IMAGE`, `OVERLAY`, `CAPTURE_META`, `DRAWING2D_JSON`, `MODEL_JSON`, `PATCH_JSON`.
- `relPath` must be a relative, safe path (no `..`, no backslashes, no absolute prefix).
- `sha256` is optional until storage is finalized; when present it should be a 64-char hex string.【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/v1/AttachmentKindV1.kt†L1-L16】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/v1/AttachmentRefV1.kt†L1-L17】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/validation/DrawingValidatorV1.kt†L174-L217】

## PatchEvent schema (v1)
Patch events capture incremental changes to a drawing. Each patch references the drawing id and the revision it applies to.

**Shape:**
- `schemaVersion` (must be `1`)
- `eventId` (stable id for the patch)
- `drawingId` (must match the target drawing)
- `baseRev` (<= drawing `rev`)
- `ops` (ordered list, order is semantically meaningful)
- `author?` (optional)
- `meta` (optional key/value list)

**Operations:**
- `add_entity` / `replace_entity` / `remove_entity`
- `add_layer` / `replace_layer` / `remove_layer`

Schema source: `DrawingPatchEvent` + `PatchOpV1` in `core-drawing2d`.【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/v1/patch/DrawingPatchEvent.kt†L1-L17】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/v1/patch/PatchOpV1.kt†L1-L66】

**Minimal patch example:**
```json
{
  "schemaVersion": 1,
  "eventId": "patch-000001",
  "drawingId": "dwg-001",
  "baseRev": 0,
  "ops": [
    { "type": "add_entity",
      "entity": { "type": "circle", "id": "e-2", "layerId": "layer-0",
                  "styleId": null, "c": { "x": 64, "y": 64 }, "r": 12 } }
  ]
}
```

## Validation summary (v1)
Validation occurs in `DrawingValidatorV1` and its geometry rules. High-level rules include:

**Structural rules:**
- `schemaVersion` is `1`.
- `drawingId` non-blank; `rev` non-negative.
- `page.widthPx` and `page.heightPx` are positive.
- `layers` is non-empty; `layer.id` unique; `layer.order` non-negative (duplicate orders warn).
- `entity.id` unique; `entity.layerId` must exist.
- `group.members` and `tag.targetId` must reference existing entity ids.
- `attachments[].relPath` must be a safe relative path; `sha256` format is validated when present.
- Patch events: `schemaVersion` matches, `drawingId` matches, `baseRev <= drawing.rev`.

**Geometry rules:**
- Numeric fields must be finite (no NaN/Inf).
- `line` endpoints should not be identical (warn).
- `polyline` requires ≥2 points; closed polylines require ≥3 points.
- `circle`/`arc` radius must be > 0.
- `text.rotationDeg` and `dimension.offsetPx` must be finite if present.

Source: `DrawingValidatorV1` + `GeometryRulesV1` + `CodesV1`.【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/validation/DrawingValidatorV1.kt†L1-L262】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/validation/GeometryRulesV1.kt†L1-L157】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/validation/CodesV1.kt†L1-L38】

## Determinism contract
Determinism ensures repeatable hashing and stable exports:

**Canonical JSON config** (for all Drawing2D types):
- `classDiscriminator = "type"`
- `encodeDefaults = true` (default fields included)
- `explicitNulls = false` (nulls omitted)
- `ignoreUnknownKeys = false` (strict parsing)

**Canonical ordering (Drawing2D):**
- Layers sorted by `(order, id)`.
- Entities sorted by `(layerId, id)`.
- Attachments sorted by `(kind, relPath)`.

**Canonical ordering (PatchEvent):**
- `meta` sorted by key.
- `ops` keep author-specified order.

**Canonical ordering (Manifest):**
- Artifacts sorted by `(kind ordinal, relPath)`.

**Checksums file ordering:**
- Entries sorted by `relPath` only.

**ID + hash expectations:**
- `drawingId`, `layer.id`, and `entity.id` must be stable and unique within a drawing.
- SHA-256 digests are hex lowercase strings produced by `Sha256V1` and written into manifest entries and checksums lists.

Source: canonicalizers, JSON config, and hashing utilities in `core-drawing2d`.【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/Drawing2DJson.kt†L1-L67】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/Drawing2DCanonicalizer.kt†L1-L36】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/v1/patch/PatchCanonicalizer.kt†L1-L27】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/artifacts/v1/ManifestCanonicalizer.kt†L1-L18】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/artifacts/io/ChecksumsWriterV1.kt†L1-L31】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/crypto/Sha256V1.kt†L1-L45】

## Artifact storage layout (v1)
Drawing2D exports use a stable directory layout for assets. The v1 layout is defined by `ProjectLayoutV1`.

```
<bundle-root>/
├── manifest.json
├── checksums.sha256
├── raw/
│   └── image
├── rectified/
│   └── rectified.png
├── overlays/
│   └── <name>.png
├── drawing2d/
│   ├── drawing2d.json
│   └── patches/
│       ├── 000000.patch.json
│       └── 000001.patch.json
├── model/
│   └── model.json
└── meta/
    └── capture_meta.json
```

Paths are relative and do not contain backslashes or parent traversal. Overlay names are sanitized by `safeName()` when constructed. Patch filenames are zero-padded 6-digit sequences. Layout definitions: `ProjectLayoutV1`.【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/artifacts/layout/v1/ProjectLayoutV1.kt†L1-L28】

## manifest.json schema + example
**Schema:**
- `schemaVersion` (int, `1`)
- `projectId` (string)
- `createdAtUtc?` (ISO-8601 UTC string)
- `createdBy?` (`appVersion`, `gitSha`, `deviceModel`)
- `artifacts[]` (list of entries)

**Artifact entry:**
- `kind` (enum from `ArtifactKindV1`)
- `relPath` (relative path)
- `sha256` (lowercase hex)
- `byteSize` (bytes)
- `mime` (MIME type)
- `pixelSha256?` (optional image pixel hash)

Source: `ManifestV1`, `ArtifactEntryV1`, `ArtifactKindV1`.【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/artifacts/v1/ManifestV1.kt†L1-L27】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/artifacts/v1/ArtifactEntryV1.kt†L1-L28】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/artifacts/v1/ArtifactKindV1.kt†L1-L19】

**Minimal example:**
```json
{
  "schemaVersion": 1,
  "projectId": "proj-001",
  "createdAtUtc": "2026-03-01T12:00:00Z",
  "createdBy": { "appVersion": "0.1.0", "gitSha": "abc123" },
  "artifacts": [
    {
      "kind": "DRAWING2D_JSON",
      "relPath": "drawing2d/drawing2d.json",
      "sha256": "6f4d3f3e5cde1c0b0b2b6b2f4b55f6c1b9a45f0a8f9a7e1d2c3b4a5f6e7d8c9b",
      "byteSize": 4821,
      "mime": "application/json"
    },
    {
      "kind": "MANIFEST_JSON",
      "relPath": "manifest.json",
      "sha256": "1b2c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef01",
      "byteSize": 912,
      "mime": "application/json"
    }
  ]
}
```

## checksums.sha256 format + example
The checksums file lists SHA-256 digests for each artifact, sorted by `relPath`, one per line with two spaces between the hash and path, and a trailing newline. Format is compatible with standard sha256sum tools.

Source: `ChecksumsWriterV1`.【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/artifacts/io/ChecksumsWriterV1.kt†L1-L31】

**Example:**
```
1b2c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef01  manifest.json
6f4d3f3e5cde1c0b0b2b6b2f4b55f6c1b9a45f0a8f9a7e1d2c3b4a5f6e7d8c9b  drawing2d/drawing2d.json
```

## Versioning notes (brief)
- `schemaVersion` is a breaking-change indicator for Drawing2D and patch events.
- v1 is fixed to pixel-based coordinates and rectified space.
- Detailed compatibility policy is tracked in PR17.

## Related code references
- Drawing2D schema: `core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/v1/`.
- Patch events: `core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/v1/patch/`.
- Validator: `core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/validation/`.
- Artifact layout + manifest: `core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/artifacts/`.
