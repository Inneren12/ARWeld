package com.example.arweld.feature.drawingimport.editor

data class EditorState(
    val tool: EditorTool = EditorTool.SELECT,
    val viewTransform: ViewTransform = ViewTransform.identity(),
    val scaleDraft: ScaleDraft? = null,
)
