# 2D3D Project Workspace Layout

This document defines the **single source of truth** for where 2D3D editor files live in a project workspace.

## Directory Structure

```
<projectRoot>/
  workspace/
    2d3d/
      drawing2d.json
      overlay_preview.png
      <underlay image (optional)>
```

## Filenames (stable contract)

| File | Required | Notes |
| --- | --- | --- |
| `drawing2d.json` | ✅ | Canonical Drawing2D JSON produced by the editor. |
| `overlay_preview.png` | ✅ | Preview image used for overlays (PNG). |
| `underlay` (optional) | ❌ | Underlay image captured or selected by the user. The name can be `underlay.<ext>` (default) or the original filename if preserved. |

## Source of Truth

Naming and path joins are centralized in:

- `Project2D3DWorkspace` (core-domain) — stable filenames + relative paths.
- `Project2D3DWorkspaceResolver` (core-data) — helpers for `File`, `Uri`, and `DocumentFile`.

## Required vs Optional

- **Required:** `drawing2d.json`, `overlay_preview.png`
- **Optional:** Underlay image. If present, use a stable name such as `underlay.jpg` or preserve the original filename.

## Storage Notes (Android)

- The workspace root is a project-specific directory (e.g., from `filesDir` or a SAF Document tree).
- `Project2D3DWorkspaceResolver` provides helpers for both local `File` access and SAF `DocumentFile` access to keep storage handling consistent.

## Drawing2D IO Behavior

- **Atomic save:** `drawing2d.json` is written to a temporary file (`drawing2d.json.tmp`), flushed/fsynced, and then renamed into place to avoid partial writes on crash.
- **Missing file:** loading `drawing2d.json` returns an empty `Drawing2D` (schemaVersion = 1, empty nodes/members) when the file does not exist.
- **Error handling:** IO and parse errors surface to the caller; no silent failure paths are used.
