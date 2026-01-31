# 2D3D Manual Editor — Members Tool (MVP)

## Goal
The Member tool creates structural members by selecting **node A** and then **node B**. Members are
always anchored to existing nodes (no free-floating endpoints).

## Interaction Flow
1. Set tool = **MEMBER**.
2. Tap a node to select **A** (draft starts).
3. Tap a second node to select **B** and create the member.

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
