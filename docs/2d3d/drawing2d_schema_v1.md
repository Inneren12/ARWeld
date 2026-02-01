# Drawing2D v1 — Manual Editor Schema

## Overview
Drawing2D v1 (manual editor) is a minimal, stable JSON schema for authoring 2D node/member layouts that can be converted to 3D. It focuses on structural topology (nodes + members) with optional scale calibration.

- **Schema version:** `1` (required)
- **Coordinate space:** editor-defined 2D space (units are implicit; use `ScaleInfo.realLengthMm` to calibrate)
- **Determinism:** avoid unordered maps; metadata is represented as ordered key/value lists.

## Root object
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `schemaVersion` | int | ✅ | Must be `1`. |
| `nodes` | array of `Node2D` | ✅ | Node list; `id` must be unique. |
| `members` | array of `Member2D` | ✅ | Member list; `aNodeId`/`bNodeId` reference nodes. |
| `scale` | `ScaleInfo` | ❌ | Optional scale calibration. |
| `meta` | `Drawing2DEditorMetaV1` | ❌ | Optional metadata and deterministic ID counters. |

## Node2D
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `id` | string | ✅ | Unique identifier for the node. |
| `x` | number | ✅ | X coordinate in editor space. |
| `y` | number | ✅ | Y coordinate in editor space. |
| `meta` | array of `MetaEntryV1` | ❌ | Optional metadata. |

## Member2D
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `id` | string | ✅ | Unique identifier for the member. |
| `aNodeId` | string | ✅ | Start node id. |
| `bNodeId` | string | ✅ | End node id. |
| `profileRef` | string | ❌ | Optional canonical profile reference (e.g., `W310x39`); see `docs/2d3d/profile_catalog_integration.md` for normalization rules. |
| `meta` | array of `MetaEntryV1` | ❌ | Optional metadata. |

## ScaleInfo
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `pointA` | `Point2D` | ✅ | First calibration point in editor space. |
| `pointB` | `Point2D` | ✅ | Second calibration point in editor space. |
| `realLengthMm` | number | ✅ | Real-world distance between points in millimeters. `mmPerPx` is derived at runtime as `realLengthMm / distance(pointA, pointB)` to avoid drift. |

## Point2D
| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `x` | number | ✅ | X coordinate. |
| `y` | number | ✅ | Y coordinate. |

## MetaEntryV1
Metadata entries are ordered key/value pairs shared across Drawing2D v1 schemas:

| Field | Type | Required |
| --- | --- | --- |
| `key` | string | ✅ |
| `value` | string | ✅ |

## Drawing2DEditorMetaV1
Manual editor metadata and deterministic ID counters:

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `nextNodeId` | number | ❌ | Next node counter (default `1`). |
| `nextMemberId` | number | ❌ | Next member counter (default `1`). |
| `entries` | array of `MetaEntryV1` | ❌ | Optional metadata entries (ordered). |

## Deterministic IDs & Ordering
To prevent save diffs/flapping, the manual editor uses deterministic IDs and canonical ordering rules.

**ID strategy (editor v1):**
- Node IDs are generated as `N000001`, `N000002`, ... using a zero-padded, 6-digit counter.
- Member IDs are generated as `M000001`, `M000002`, ... using a zero-padded, 6-digit counter.
- Counters are stored in `meta.nextNodeId` and `meta.nextMemberId`.
- Each allocation increments the corresponding counter by 1 (monotonic, session-local).

**Canonical ordering on save:**
- `nodes` are sorted by `id` ascending.
- `members` are sorted by `id` ascending.

## Example JSON
```json
{
  "schemaVersion": 1,
  "nodes": [
    { "id": "n1", "x": 10.0, "y": 20.0 },
    { "id": "n2", "x": 40.0, "y": 60.0, "meta": [{ "key": "tag", "value": "anchor" }] }
  ],
  "members": [
    { "id": "m1", "aNodeId": "n1", "bNodeId": "n2", "profileRef": "W310x39" }
  ],
  "scale": {
    "pointA": { "x": 10.0, "y": 20.0 },
    "pointB": { "x": 40.0, "y": 60.0 },
    "realLengthMm": 1500.0
  },
  "meta": {
    "nextNodeId": 3,
    "nextMemberId": 2,
    "entries": [
      { "key": "source", "value": "manual" }
    ]
  }
}
```

## Validation helper
A minimal helper reports members referencing missing nodes without throwing; see `missingNodeReferences()` in the model package.
