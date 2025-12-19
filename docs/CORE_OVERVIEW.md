# CORE_OVERVIEW — core-structural (v0.1)

`core-structural` is a pure Kotlin/JVM module that owns the structural steel model used by AR and QC flows. The module is Android-free and uses millimeters (mm) for all geometry and kilograms per meter (kg/m) for linear weight.

## Purpose

- Provide canonical models for steel frames (nodes, members, connections, plates).
- Normalize and resolve profile designations through a reusable catalog.
- Parse and validate `model.json` documents before they enter AR/QC pipelines.

## Key Models

- `Node(id, xMm, yMm, zMm)` — coordinates in mm.
- `Member(id, kind, profileDesignation, nodeStartId, nodeEndId, orientation?)`
- `OrientationMeta(rollAngleDeg?, camberMm?)`
- `Plate(id, thicknessMm, widthMm, lengthMm)`
- `Connection(id, memberIds, plateIds?)`
- `StructuralModel(id, nodes, members, connections = [], plates = [], meta = {})` (plates optional; variant B)

## Profiles

- `ProfileType` enum: `W`, `HSS`, `C`, `L`, `PL`.
- `ProfileSpec` holds depth/width/thickness/area in **mm** (area in mm², weight in kg/m).
- Seed catalog shipped as `profiles_seed.json` (10–20 common shapes: W, HSS, C, L, PL).
- `parseProfileString(raw)` normalizes inputs such as `W310x39`, `hss 6x6x3/8`, `PL 10x250`.
- `ProfileCatalog` provides `loadSeedProfiles()` and `findByDesignation()` lookup with normalization.

## JSON Format

- See `docs/MODEL_JSON_SPEC.md` for full schema and examples.
- Units are fixed to `"mm"` in v0.1 (case-insensitive input, normalized to `"mm"`).
- Members carry `profile` as a designation string; resolution happens via `ProfileCatalog`.
- Optional `plates` and `connections` arrays; `connection.plateIds` reference `plates.id` (variant B).
- Unknown JSON fields are ignored (`ignoreUnknownKeys = true`).

## Parsing & Validation API

- `StructuralModelCore` interface:
  - `loadModelFromJson(json: String): StructuralModel`
  - `validate(model: StructuralModel): ValidationResult`
- `DefaultStructuralModelCore(profileCatalog)`:
  - Parses with `ModelJsonParser` (kotlinx-serialization, `ignoreUnknownKeys=true`).
  - Calls `validate` and throws `IllegalArgumentException` if invalid.
  - Validation checks:
    - unique node/member ids,
    - member endpoints exist,
    - connections reference existing members/plates (variant B),
    - profiles resolve through the catalog.

## Limitations (v0.1)

- Geometry units fixed to **mm**; no mixed-unit support.
- Limited seed catalog (10–20 shapes) for lookup/validation only.
- No mesh/3D generation; members are topology + profile ids only.
- No coordinate-system metadata beyond local mm axes (future docs to define).
