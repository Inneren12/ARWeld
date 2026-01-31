package com.example.arweld.feature.drawingimport.editor.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.example.arweld.feature.drawingimport.editor.EditorIntent
import com.example.arweld.feature.drawingimport.editor.EditorState
import com.example.arweld.feature.drawingimport.editor.EditorTool
import com.example.arweld.feature.drawingimport.editor.Vec2

@Composable
fun DrawingEditorCanvas(
    state: EditorState,
    modifier: Modifier = Modifier,
    onIntent: (EditorIntent) -> Unit,
) {
    Box(
        modifier = modifier
            .pointerInput(state.tool, state.viewTransform) {
                detectTapGestures { offset ->
                    if (state.tool == EditorTool.SCALE) {
                        val worldPoint = state.viewTransform.screenToWorld(Vec2(offset.x, offset.y))
                        onIntent(EditorIntent.ScaleTap(worldPoint))
                    }
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            state.scaleDraft?.let { draft ->
                drawScaleDraft(draft, state.viewTransform)
            }
        }
    }
}
