# Profiles Specification (v0.1)

This spec defines the supported steel profile designation formats, normalization rules, and the starter catalog used by `core-structural`.

## Supported profile types

- **W** — wide-flange (W-shape)
- **HSS** — hollow structural section (rectangular)
- **C** — channel (C/MC series)
- **L** — angle
- **PL** — plate

## Canonical designation strings

Canonical designations are the normalized strings stored in `model.json` (`members[].profile`) and the catalog (`profiles.json`).

Examples:

- **W**: `W310x39`
- **HSS**: `HSS 6x6x3/8`
- **PL**: `PL 10x250`
- **C**: `C200x20`
- **L**: `L4x4x3/8`

## Normalization rules

Normalization must match the behavior of `parseProfileString(...)` and `ProfileCatalog.findByDesignation(...)`.

- **Type prefix**: always uppercase (`W`, `HSS`, `C`, `L`, `PL`).
- **Whitespace**:
  - `HSS` and `PL` canonical forms include a single space after the type prefix (`HSS 6x6x3/8`, `PL 10x250`).
  - `W`, `C`, `L` canonical forms include no space (`W310x39`, `C200x20`, `L4x4x3/8`).
- **Separators**: `x` is the only canonical separator (input may include spaces or `X`, which are normalized).
- **Fractions**: fractional thickness/leg dimensions are preserved (e.g., `3/8`, `1/4`, `5/16`).
- **Raw input**: the parser keeps the original raw string for error reporting, but all lookups use the canonical designation.

## Units in ProfileSpec

All profile geometry dimensions are stored in **millimeters (mm)**. Mass is stored as **kilograms per meter (kg/m)** when provided.

## Starter catalog (v0.1)

The v0.1 catalog lives at:

- `core-structural/src/main/resources/profiles.json`

Profiles included (must match canonical designations in the catalog):

- `W200x27`
- `W310x39`
- `W360x33`
- `W410x60`
- `HSS 6x6x3/8`
- `HSS 8x4x1/4`
- `HSS 4x4x5/16`
- `C200x20`
- `C380x50`
- `L4x4x3/8`
- `L3x2x1/4`
- `PL 10x250`
- `PL 20x300`
