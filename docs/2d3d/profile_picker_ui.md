# 2D3D Profile Picker UI (S3-28)

## Location
- The profile picker is launched from the **Member** selection bottom sheet.
- Tap **Choose profile** to open the picker.

## Picker Layout
- **Header:** "Choose profile" with **Clear** and **Close** actions.
- **Search field:** single-line text input labeled "Search profiles".
- **Results list:** profile display name + profileRef.
- **Loading state:** spinner while search is in progress.
- **Empty state:** "No profiles match this query."

## Selection Behavior
- Tapping a profile:
  - Validates the profileRef against the catalog (`lookup(profileRef)`).
  - Updates `Member2D.profileRef` with the canonical reference.
  - Closes the picker.
  - Persists immediately and pushes **one undo snapshot**.
- **Clear** sets `Member2D.profileRef = null`, closes the picker, and persists as a single undo step.
- If the selected profileRef cannot be resolved, show an explicit error and do **not** mutate.

## Search + Debouncing Policy
- Search is **debounced** to avoid running on every keystroke (250 ms delay).
- Each search is executed off the main thread via `ProfileCatalogQuery`, which is already cached and
  deterministic in ordering (type order, then profileRef).
- Results preserve catalog ordering to stay deterministic across sessions.

## Error Messaging
- Profile lookup failures show: **"Profile not found in catalog."**
- Missing member selection shows: **"Selected member not found."**
