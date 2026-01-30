package com.example.arweld.core.drawing2d.v1

import com.example.arweld.core.drawing2d.v1.entities.EntityV1
import kotlinx.serialization.Serializable

/**
 * Root schema object for Drawing2D v1.
 *
 * @property schemaVersion Schema version (always 1 for v1)
 * @property drawingId Stable identifier for the drawing
 * @property rev Revision counter for the drawing
 * @property units Measurement units for the drawing (pixels in v1)
 * @property coordSpace Coordinate space configuration (rectified pixels in v1)
 * @property page Page dimensions in pixels
 * @property layers Layer definitions
 * @property entities Geometric entities in the drawing
 * @property attachments Optional attachment references for related artifacts
 */
@Serializable
data class Drawing2D(
    val schemaVersion: Int = 1,
    val drawingId: String,
    val rev: Long,
    val units: UnitsV1 = UnitsV1.PX,
    val coordSpace: CoordSpaceV1 = CoordSpaceV1(
        type = CoordSpaceTypeV1.RECTIFIED_PX,
        origin = OriginV1.TOP_LEFT,
        axis = AxisV1(
            x = AxisDirectionV1.RIGHT,
            y = AxisDirectionV1.DOWN
        )
    ),
    val page: PageV1,
    val layers: List<LayerV1>,
    val entities: List<EntityV1>,
    val attachments: List<AttachmentRefV1> = emptyList()
)
