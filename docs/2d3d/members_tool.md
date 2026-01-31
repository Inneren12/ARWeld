# 2D3D Manual Editor — Members Tool (MVP)

## Goal
The Member tool creates structural members by selecting **node A** and then **node B**. Members are
always anchored to existing nodes (no free-floating endpoints).

## Interaction Flow
1. Set tool = **MEMBER**.
2. Tap a node to select **A** (draft starts).
3. Tap a second node to select **B** and create the member.

## Selection (S3-24)
- In **Select** mode, tapping a member line selects it.
- When a member is selected, the bottom sheet shows:
  - Member ID
  - Endpoint node IDs (A/B)
  - **Delete member** action

## Delete Member (S3-24)
- The delete action removes only the member; nodes are unchanged.
- Deletion clears the selection.
- The change is persisted immediately and pushed as a single undo snapshot (Undo restores the member).

## Invalid Member Attempts
- **Same node (A == B):** blocked with an explicit error message.
- **Duplicate member:** blocked with an explicit error message when a member already exists between
  the same two nodes (direction-insensitive; `A→B` and `B→A` are treated as the same pair).

### Draft Policy on Invalid Attempts
- The member draft **keeps node A selected** so the user can pick a different second node.
- Invalid attempts do **not** mutate the drawing or push undo history.

## Draft Visualization
- While **A** is selected and **B** is pending, the canvas highlights node **A**.
- The bottom sheet displays a hint: “Member tool: select the second node.”

## Determinism & Endpoint Canonicalization
- Member IDs use deterministic allocation (`M000001`, `M000002`, …) backed by
  `Drawing2DEditorMetaV1.nextMemberId`.
- Endpoints are canonicalized on write: the member stores `(minNodeId, maxNodeId)` so that
  serialization is stable regardless of selection order.

## Persistence Policy
- Member creation is a discrete mutation and is **auto-saved immediately** via
  `Drawing2DRepository.saveCurrentDrawing()`, matching the Nodes tool policy.
- The mutation pushes a single undo snapshot so **Undo** removes the created member in one step.

## Scope Notes
- This MVP handles the happy path only. Duplicate members and invalid endpoints are addressed
  in the follow-up stage (S3-23).
