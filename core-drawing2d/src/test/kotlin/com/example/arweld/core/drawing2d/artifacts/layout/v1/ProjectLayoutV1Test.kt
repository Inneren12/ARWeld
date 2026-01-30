package com.example.arweld.core.drawing2d.artifacts.layout.v1

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ProjectLayoutV1Test {

    @Test
    fun `patch formats with zero padding`() {
        assertThat(ProjectLayoutV1.patch(1)).isEqualTo("drawing2d/patches/000001.patch.json")
    }

    @Test
    fun `overlay sanitizes names`() {
        assertThat(ProjectLayoutV1.overlay("Corners Debug"))
            .isEqualTo("overlays/corners_debug.png")
    }
}
