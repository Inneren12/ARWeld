# PROFILES_SPEC — Profile catalog, designations, and OCR normalization (v1)

## Supported profile types
- **W** — Wide-flange shapes
- **HSS** — Hollow structural sections (square/rectangular)
- **C** — Channels (C/MC)
- **L** — Angles
- **PL** — Plates (parametric)

## Canonical designation format (CSA v1)
- Canonical designations are compact, lowercase `x` separators with no embedded spaces for W/C/L/PL and a single space after the prefix for HSS.
- Examples (CSA): `W310x39`, `C200x17`, `L51x38x6.4`, `PL10x190`, `HSS 6x6x3/8`.
- `parseProfileString` detects type prefixes (`W`, `HSS`, `C`/`MC`, `L`, `PL`) and produces a canonical designation that `ProfileCatalog` uses as the lookup key; `ProfileStandard.CSA` is the current default hint.

## Normalization rules for OCR/BOM strings
Normalization is applied before parsing and lookup so OCR/BOM text can be matched reliably:
- **Whitespace:** collapse consecutive spaces and trim ends (e.g., `"W 310 x 39" → "W310x39"`).
- **Separator:** convert `×`/`X` to lowercase `x` (e.g., `"C200×17" → "C200x17"`).
- **Decimal comma:** convert digit-comma-digit to digit-dot-digit (e.g., `"6,4" → "6.4"` in `L51x38x6,4`).
- **Prefix handling:** canonical output strips duplicated prefixes and re-applies the standard form (e.g., `"hss6x6x3/8" → "HSS 6x6x3/8"`, `"pl 10 x 190" → "PL10x190"`).
- **Noise trimming:** leading/trailing OCR artifacts such as `| , ;` are dropped.

## Parsing and catalog lookup flow
1. **normalizeRawProfileInput(raw)** — applies separator, decimal, whitespace, and noise cleanup.
2. **parseProfileString(raw)** — infers `ProfileType` from the prefix and emits a canonical designation plus `standardHint = CSA`.
3. **ProfileCatalog.findByDesignation(raw, preferredStandard)** — normalizes the string again, resolves by designation and aliases within the requested standard, and falls back to cross-standard matches if needed.

## Plate (PL) as parametric shapes
- Plates are generated on the fly from the normalized `PLt x w` body when no explicit catalog entry exists.
- The fallback produces `PlateSpec(tMm = t, wMm = w, areaMm2 = t * w, massKgPerM = null)` using the caller’s `preferredStandard` so CSA plates resolve today and AISC plates can resolve later without new code.

## Extending with AISC (planned)
- Add JSON resources named `catalog_aisc_*.json` under `core-structural/src/main/resources/profiles/` (one per profile type) alongside CSA files.
- Populate `aliases` to cover imperial-style labels (e.g., `W10x30`, `HSS 6x6x3/8`) that normalize to the same canonical designation used in the metric catalog entry.
- Callers can request AISC-first lookups via `preferredStandard = ProfileStandard.AISC`; catalog resolution already honors the preferred standard before falling back to other matches.

## Contract for OCR stage 1 (BOM/spec only)
- OCR returns a designation string exactly as seen in the specification table (no geometry extraction from drawings).
- Pipeline: `OCR text → normalize → parseProfileString → ProfileCatalog` lookup.
- Full drawing parsing (geometry from plans) is **out of scope** for this stage.
