# MODEL_JSON_SPEC — Structural Model JSON (v0.1)

This specification defines the `model.json` payload consumed by `core-structural`. Version **v0.1** is millimeter-only; all coordinates and dimensions are expressed in **mm**, and linear weight is expressed in **kg/m**. Unknown fields are ignored during parsing (`ignoreUnknownKeys = true`).

## Top-Level Structure

```json
{
  "id": "example_frame_01",
  "units": "mm",
  "nodes": [ ... ],
  "members": [ ... ],
  "plates": [ ... ],
  "connections": [ ... ],
  "meta": { }
}
```

Required fields: `id`, `units`, `nodes`, `members`.

- `id` — Model identifier.
- `units` — Must be `"mm"` in v0.1. Case-insensitive on input; normalized to `"mm"`.
- `nodes` — Array of node objects.
- `members` — Array of member objects referencing node ids.
- `plates` — Optional array of plate definitions (can be omitted).
- `connections` — Optional array describing how members/plates are grouped.
- `meta` — Optional string map for auxiliary properties (e.g., source, revision).

## Nodes

```json
{ "id": "N1", "x": 0.0, "y": 0.0, "z": 0.0 }
```

- Coordinates `x`, `y`, `z` are in millimeters (mm), project-local axes.

## Members

```json
{
  "id": "M1",
  "kind": "BEAM",
  "profile": "W310x39",
  "nodeStartId": "N1",
  "nodeEndId": "N2",
  "orientation": { "rollAngleDeg": 0.0, "camberMm": 8.0 }
}
```

- `kind` — One of `BEAM`, `COLUMN`, `BRACE`, `OTHER`.
- `profile` — Profile designation string (e.g., `W310x39`, `HSS 6x6x3/8`, `PL 10x250`). Normalized via `parseProfileString` then resolved through the ProfileCatalog.
- `nodeStartId` / `nodeEndId` — Node ids for the member endpoints.
- `orientation` — Optional; `rollAngleDeg` in degrees, `camberMm` in millimeters. Both fields are optional.

## Plates (variant B)

```json
{ "id": "P1", "thicknessMm": 12.0, "widthMm": 200.0, "lengthMm": 300.0 }
```

- All plate dimensions are in millimeters (mm).

## Connections

```json
{ "id": "C1", "memberIds": ["M1", "M2"], "plateIds": ["P1"] }
```

- `memberIds` — Members participating in the connection.
- `plateIds` — Optional plate references (empty if not used). References `plates.id`.

## Validation Rules (v0.1)

1. `units` must be `"mm"`.
2. Node ids must be unique.
3. Every `member.nodeStartId` and `member.nodeEndId` must exist in `nodes`.
4. Every `connection.memberIds` entry must exist in `members`.
5. Every `connection.plateIds` entry must exist in `plates` (if provided).
6. `profile` must resolve via `ProfileCatalog` (lookup after normalization).

## Minimal Example (no plates)

```json
{
  "id": "example_frame_01",
  "units": "mm",
  "nodes": [
    { "id": "N1", "x": 0, "y": 0, "z": 0 },
    { "id": "N2", "x": 6000, "y": 0, "z": 0 }
  ],
  "members": [
    { "id": "M1", "kind": "BEAM", "profile": "W310x39", "nodeStartId": "N1", "nodeEndId": "N2" }
  ],
  "connections": []
}
```

## Extended Example (plates + connection.plateIds)

```json
{
  "id": "example_frame_plates",
  "units": "mm",
  "nodes": [
    { "id": "N1", "x": 0, "y": 0, "z": 0 },
    { "id": "N2", "x": 6000, "y": 0, "z": 0 }
  ],
  "members": [
    { "id": "M1", "kind": "BEAM", "profile": "W310x39", "nodeStartId": "N1", "nodeEndId": "N2" }
  ],
  "plates": [
    { "id": "P1", "thicknessMm": 12, "widthMm": 200, "lengthMm": 300 }
  ],
  "connections": [
    { "id": "C1", "memberIds": ["M1"], "plateIds": ["P1"] }
  ]
}
```
