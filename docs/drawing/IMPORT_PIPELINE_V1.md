# Drawing Import Pipeline v1 (Sprint 2)

This document defines the **stable, deterministic contract** for the Sprint 2 drawing import pipeline. It describes the capture → detect → rectify → quality → artifacts flow, expected inputs/outputs, and artifact finalization rules. It uses RFC 2119 keywords (MUST/SHOULD/MAY). See [DRAWING2D_V1.md](./DRAWING2D_V1.md) for the Drawing2D schema and [SCHEMA_EVOLUTION.md](./SCHEMA_EVOLUTION.md) for compatibility policy.

## Overview
The v1 import pipeline takes a single captured image and deterministically produces:
- A rectified PNG image.
- A `capture_meta.json` payload describing geometry, metrics, and quality decision.
- A finalized artifact bundle with manifest, checksums, and completion marker.

The pipeline is designed to be deterministic and reproducible, with stable failure codes and canonical artifact paths.

## Non-goals
- This spec does **not** define UI behavior or user-facing copy.
- This spec does **not** define 2D drawing extraction; it ends at rectification + capture metadata.
- This spec does **not** redefine Drawing2D schema or artifact layout beyond the import-specific parts.

## Pipeline stages and outcomes
The pipeline is orchestrated by `DrawingImportPipelineV1` and runs the stages in order. Each stage MUST emit a `PageDetectOutcomeV1` with the stage name and a stable failure code when errors occur.【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/pipeline/DrawingImportPipelineV1.kt†L31-L232】【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/preprocess/PageDetectContractsV1.kt†L1-L82】

**Stage order (v1):**
1. `LOAD_UPRIGHT` — Decode the raw image, apply EXIF rotation, and load an upright bitmap. Guardrails enforce max decode pixels/side and return stable failure codes on oversize or OOM risk.【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/preprocess/DrawingImportGuardrailsV1.kt†L1-L34】【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/preprocess/PageDetectContractsV1.kt†L12-L46】
2. `PREPROCESS` — Downscale to a fixed max side, then convert to deterministic grayscale (Rec.601 luma) for detection.【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/preprocess/PageDetectPreprocessor.kt†L10-L94】
3. `EDGES` — Deterministic edge detection (Gaussian blur → Sobel gradients → non-max suppression → hysteresis).【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/preprocess/PageDetectEdgeDetector.kt†L8-L91】
4. `CONTOURS` — Extract connected components, build convex hull contours with area/perimeter stats.【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/preprocess/PageDetectContourExtractor.kt†L8-L112】
5. `QUAD_SELECT` — Select the best convex quad candidate by area/rectangularity/aspect scoring with a minimum area threshold.【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/preprocess/PageQuadSelector.kt†L18-L118】
6. `ORDER` — Deterministic corner ordering (top-left clockwise) with degeneracy checks.【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/preprocess/CornerOrderingV1.kt†L6-L75】
7. `REFINE` — Optional corner refinement using edge-based local search; on failure, the pipeline MAY fall back to ordered corners.【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/preprocess/CornerRefinerV1.kt†L20-L118】【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/pipeline/DrawingImportPipelineV1.kt†L163-L181】
8. `RECTIFY_SIZE` — Compute rectified output size using the deterministic size policy (see below).【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/preprocess/RectifySizePolicyV1.kt†L11-L104】
9. `RECTIFY` — Warp the upright bitmap to rectified space and capture the 3×3 homography (`homographyH`).【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/pipeline/DrawingImportPipelineV1.kt†L724-L788】
10. `SAVE` — Write rectified PNG + `capture_meta.json`, update manifest, and commit the project transaction.【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/pipeline/DrawingImportPipelineV1.kt†L269-L340】【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/artifacts/RectifiedArtifactWriterV1.kt†L19-L75】【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/artifacts/CaptureMetaWriterV1.kt†L10-L49】

### Failure codes (stable contract)
The following codes MUST be treated as stable and deterministic. They are emitted per-stage in `PageDetectFailureV1` outcomes.【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/preprocess/PageDetectContractsV1.kt†L14-L46】

| Code | Meaning |
| --- | --- |
| `DECODE_FAILED` | Raw image decode failed. |
| `INPUT_TOO_LARGE` | Raw input exceeds decode guardrails (pixel count or max side). |
| `OOM_RISK` | Decode hit an out-of-memory risk. |
| `EXIF_FAILED` | EXIF orientation read failed. |
| `EDGES_FAILED` | Edge detection failed. |
| `CONTOURS_EMPTY` | No usable contours after extraction. |
| `PAGE_NOT_FOUND` | No contours available for quad selection. |
| `NO_CONVEX_QUAD` | No convex quad candidates found. |
| `QUAD_TOO_SMALL` | Quad candidates below area threshold. |
| `ORDER_NOT_FOUR_POINTS` | Ordering received a non-quad list. |
| `ORDER_DEGENERATE` | Quad geometry was degenerate/invalid. |
| `REFINE_FAILED` | Corner refinement failed. |
| `RECTIFIED_TOO_LARGE` | Rectified output exceeds pixel/side caps. |
| `OUTPUT_OPEN_FAILED` | Failed to open output transaction staging directory. |
| `OUTPUT_COMMIT_FAILED` | Failed to commit staged output to final directory. |
| `OUTPUT_ROLLBACK` | Failed to rollback staged output after a failure. |
| `TIME_BUDGET_EXCEEDED` | Pipeline exceeded the configured time budget. |
| `UNKNOWN` | Unclassified failure. |

## Rectify size policy summary
The rectified size policy is deterministic and enforces the following constraints.【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/preprocess/RectifySizePolicyV1.kt†L16-L104】

- **Aspect ratio cap:** The quad aspect ratio MUST be ≤ 4:1; otherwise `UNKNOWN` is returned for `RECTIFY_SIZE`.
- **Min/max side:** The output MUST be within `[minSide, maxSide]` while preserving aspect ratio.
- **Downscale rule:** If the quad’s max side exceeds `maxSide`, the output is deterministically downscaled.
- **Upscale rule:** If the quad’s min side would fall below `minSide`, the output is scaled up.
- **Even constraint:** When `enforceEven=true`, width/height MUST be adjusted to even values within bounds.
- **Max pixels:** If `width * height > maxPixels`, the stage fails with `RECTIFIED_TOO_LARGE`.

Default pipeline settings (unless overridden) are `maxSide=2048`, `minSide=256`, `enforceEven=true`, `maxRectifiedPixels=12,000,000`, and `maxRectifiedSide=4096` (caps are enforced during rectification).【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/pipeline/DrawingImportPipelineV1.kt†L521-L535】【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/preprocess/DrawingImportGuardrailsV1.kt†L1-L34】

## Metrics definitions
Metrics are computed deterministically and written into `capture_meta.json`.

### Blur (variance of Laplacian)
- **Metric:** Variance of a 3×3 Laplacian kernel over the rectified bitmap.
- **Grayscale conversion:** Rec.601 coefficients (`0.299R + 0.587G + 0.114B`).
- **Notes:** If blur computation fails, `blurVar` is set to `null` and quality still evaluates other metrics.【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/quality/QualityMetricsV1.kt†L33-L120】【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/pipeline/DrawingImportPipelineV1.kt†L420-L450】

### Exposure
- **Luma:** `luma = (77 * R + 150 * G + 29 * B) >> 8` (Rec.601 integer form).
- **meanY:** average luma value over all pixels.
- **clipLowPct/clipHighPct:** percentage of pixels ≤ 8 or ≥ 247 respectively (clipping thresholds).【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/quality/QualityMetricsV1.kt†L21-L27】【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/quality/QualityMetricsV1.kt†L157-L219】

### Skew
Skew is computed in the **downscaled frame** coordinate space using the ordered quad corners:
- **keystoneWidthRatio/keystoneHeightRatio:** max of width/height ratios to measure keystone distortion.
- **angleMaxAbsDeg/angleMeanAbsDeg:** maximum/mean absolute deviation from 90° of the quad’s corner angles.
- **pageFillRatio:** quad area divided by image area.
- **Degenerate handling:** Non-finite or zero-length edges are reported as `DEGENERATE` status with zeros for metrics.【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/quality/QualityMetricsV1.kt†L11-L156】【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/quality/QualityMetricsV1.kt†L221-L275】

## Quality gate decision and reason codes
Quality gates are computed deterministically using `QualityGateV1.evaluate` and return a `PASS`, `WARN`, or `FAIL` decision. Any FAIL reason overrides WARN reasons; otherwise WARN is returned if any WARN reasons are present.【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/quality/QualityGateV1.kt†L36-L85】

### Default threshold values
These defaults SHOULD be treated as the v1 contract unless explicitly overridden by pipeline parameters.【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/quality/QualityGateV1.kt†L26-L45】

| Metric | Fail threshold | Warn threshold |
| --- | --- | --- |
| Blur variance | `< 80.0` | `< 140.0` |
| Exposure meanY | `< 60.0` (too dark) / `> 200.0` (too bright) | — |
| Clip shadows/highlights | `> 18%` | — |
| Keystone width/height ratio | `> 1.35` | `> 1.18` |
| Angle deviation (max) | `> 10°` | `> 6°` |
| Page fill ratio | `< 0.45` | `< 0.62` |

### Reason codes
Reason codes are stable, ordered, and MUST be preserved in outputs. The `capture_meta.json` uses the enum names as strings in the `quality.reasons` list.【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/quality/QualityGateV1.kt†L12-L34】【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/pipeline/DrawingImportPipelineV1.kt†L491-L508】

| Code | Trigger (summary) |
| --- | --- |
| `PAGE_NOT_FOUND` | Page detection failed upstream (no quad available). |
| `BLUR_TOO_HIGH` | Blur variance below fail threshold. |
| `BLUR_WARN` | Blur variance below warn threshold. |
| `EXPOSURE_TOO_DARK` | meanY below low threshold. |
| `EXPOSURE_TOO_BRIGHT` | meanY above high threshold. |
| `CLIP_SHADOWS_HIGH` | clipLowPct above threshold. |
| `CLIP_HIGHLIGHTS_HIGH` | clipHighPct above threshold. |
| `KEYSTONE_WIDTH_HIGH` | keystone width ratio above threshold. |
| `KEYSTONE_HEIGHT_HIGH` | keystone height ratio above threshold. |
| `ANGLE_DEVIATION_HIGH` | angleMaxAbsDeg above threshold. |
| `PAGE_FILL_LOW` | pageFillRatio below threshold. |
| `DEGENERATE_QUAD` | Quad metrics are degenerate (invalid geometry). |

## Artifact layout and canonical relPaths
Artifact paths MUST follow `ProjectLayoutV1` and Drawing import conventions. Overlay names MUST be sanitized (`safeName`) when constructed.【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/artifacts/layout/v1/ProjectLayoutV1.kt†L1-L28】

```
<artifactsRoot>/projects/<projectId>/
├── manifest.json
├── checksums.sha256
├── raw/
│   └── image.jpg
├── rectified/
│   └── rectified.png
├── overlays/
│   └── <safe-name>.png
├── drawing2d/
│   ├── drawing2d.json
│   └── patches/
│       └── 000000.patch.json
├── model/
│   └── model.json
└── meta/
    ├── capture_meta.json
    └── project_complete.json
```

**Canonical relPaths:**
- `raw/image.jpg` is the raw capture path used by the import pipeline.【F:feature-drawing-import/src/main/kotlin/com/example/arweld/feature/drawingimport/artifacts/DrawingImportArtifacts.kt†L7-L21】
- `rectified/rectified.png`, `meta/capture_meta.json`, `meta/project_complete.json`, `manifest.json`, and `checksums.sha256` are defined by `ProjectLayoutV1` and MUST match exactly.【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/artifacts/layout/v1/ProjectLayoutV1.kt†L1-L13】

## Finalization: manifest, checksums, and completion marker
The pipeline writes artifacts into a staging directory and commits them into a finalized project directory. Finalization MUST ensure hash correctness and include a project completion marker.

- **Transaction semantics:** A project write uses `.staging/<projectId>` for all writes; on success it is moved to `projects/<projectId>`. If the final directory exists, its contents are copied into staging first to allow incremental updates.【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/artifacts/io/v1/ProjectTransactionV1.kt†L13-L67】
- **Project completion marker:** `meta/project_complete.json` is written during finalization and included in the manifest.【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/artifacts/io/v1/ProjectFinalizerV1.kt†L37-L73】
- **Manifest + checksums:** Finalization rewrites `manifest.json`, generates `checksums.sha256` (sorted by relPath), and verifies file existence + SHA-256 hashes for every entry.【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/artifacts/io/v1/ProjectFinalizerV1.kt†L21-L90】【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/artifacts/io/v1/ChecksumsWriterV1.kt†L8-L35】

## `capture_meta.json` schema (v1)
The capture metadata schema is defined by `CaptureMetaV1`. Writers MUST emit `schemaVersion=1` and include all required blocks. Field values are serialized using canonical JSON settings from `Drawing2DJson`.

### Schema overview
- `schemaVersion` (int, `1`)
- `projectId` (string)
- `raw` (ImageInfoV1): `widthPx`, `heightPx`, `rotationAppliedDeg`
- `upright` (ImageInfoV1): `widthPx`, `heightPx`, `rotationAppliedDeg`
- `downscaleFactor` (double)
- `cornersDownscaledPx` (list of PointV1, length 4)
- `cornersUprightPx` (list of PointV1, length 4)
- `homographyH` (list of 9 doubles; 3×3 transform matrix)
- `rectified` (ImageInfoV1): `widthPx`, `heightPx`, `rotationAppliedDeg`
- `metrics` (MetricsBlockV1): `blurVar`, `exposure`, `skew`
- `quality` (QualityGateBlockV1): `decision`, `reasons`

Schema source: `CaptureMetaV1` and related blocks in `core-drawing2d` artifacts v1.【F:core-drawing2d/src/main/kotlin/com/example/arweld/core/drawing2d/artifacts/v1/CaptureMetaV1.kt†L1-L52】

### Minimal example
```json
{
  "schemaVersion": 1,
  "projectId": "proj-001",
  "raw": { "widthPx": 4032, "heightPx": 3024, "rotationAppliedDeg": 0 },
  "upright": { "widthPx": 3024, "heightPx": 4032, "rotationAppliedDeg": 90 },
  "downscaleFactor": 2.0,
  "cornersDownscaledPx": [
    { "x": 120.0, "y": 96.0 },
    { "x": 880.0, "y": 102.0 },
    { "x": 892.0, "y": 1250.0 },
    { "x": 110.0, "y": 1240.0 }
  ],
  "cornersUprightPx": [
    { "x": 240.0, "y": 192.0 },
    { "x": 1760.0, "y": 204.0 },
    { "x": 1784.0, "y": 2500.0 },
    { "x": 220.0, "y": 2480.0 }
  ],
  "homographyH": [1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0],
  "rectified": { "widthPx": 2048, "heightPx": 1536, "rotationAppliedDeg": 0 },
  "metrics": {
    "blurVar": 132.4,
    "exposure": { "meanY": 121.2, "clipLowPct": 2.1, "clipHighPct": 0.4 },
    "skew": {
      "angleMaxAbsDeg": 3.4,
      "angleMeanAbsDeg": 1.8,
      "keystoneWidthRatio": 1.04,
      "keystoneHeightRatio": 1.03,
      "pageFillRatio": 0.74
    }
  },
  "quality": {
    "decision": "PASS",
    "reasons": []
  }
}
```

## Determinism and evolution notes
- All stage codes and reason codes MUST remain stable within v1.
- Any changes that affect hashing, relPaths, or serialization MUST follow [SCHEMA_EVOLUTION.md](./SCHEMA_EVOLUTION.md).
- The pipeline MUST preserve canonical relPaths and manifest/checksum ordering as defined by v1 artifacts.
