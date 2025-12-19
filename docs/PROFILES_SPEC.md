# Profile Catalog + OCR/BOM Normalization Spec

This document describes the contract between OCR/BOM ingestion and the core-structural profile catalog. It is intentionally CSA-first but structured to support AISC additions later.

## Supported profile types
- **W** — Wide flange
- **C / MC** — Channels; MC preserved when present in the source
- **HSS** — Hollow structural sections
- **L** — Angles
- **PL** — Plates (parametric fallback)

## Canonical format rules
- Prefixes are uppercase (`W`, `C`, `MC`, `HSS`, `L`, `PL`).
- Separators use lowercase `x` with no spaces (`W310x39`, `C200x17`).
- HSS retains a single space after the prefix (`HSS 152x152x6.4`).
- MC is preserved as the channel prefix when provided (e.g., `MC250x33`).
- Fractions are preserved inside aliases (e.g., `HSS 6x6x1/4`).

## Normalization for OCR/BOM strings
- Trim whitespace; collapse separators like `X`, `×`, or spaced `x` into lowercase `x`.
- Remove internal spaces unless required by the canonical (HSS retains one space after the prefix).
- Preserve fraction tokens; if fractions or inch symbols are present, set `standardHint` to **AISC**; otherwise default to **CSA**.
- Plate strings parse as `PL t x w` with optional `xL` length tokens ignored for catalog lookup.

## Lookup + fallback rules
- `findByDesignation(raw, preferredStandard)` parses the input, then searches standards in order: `standardHint` (if any) → `preferredStandard` → other known standards.
- Aliases are part of the index; collisions across designations are rejected to keep mappings unambiguous.
- Plates: when no catalog entry exists, a parametric `PlateSpec` is synthesized from the plate regex using `t` and `w`.

## Adding AISC catalogs later
1. Drop new files under `resources/profiles/` (e.g., `catalog_aisc_w.json`).
2. Append them to `resources/profiles/catalog_index.json`.
3. Add aliases that map imperial/fractional notations to canonical metric-friendly forms as needed.
4. No directory listing is used; the explicit index keeps loading JAR-safe.

## Scope of OCR Stage 1
- Stage 1 only normalizes BOM/spec strings to canonical designations plus standard hints.
- Full drawing parsing, geometry extraction, or part-level relationships are **out of scope** for this stage.
