package com.example.arweld.core.drawing2d.validation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ViolationModelTest {

    @Test
    fun `canonicalSorted orders by severity path and code`() {
        val violations = listOf(
            ViolationV1(
                code = "B_CODE",
                severity = SeverityV1.ERROR,
                path = "$.b",
                message = "b error",
            ),
            ViolationV1(
                code = "A_CODE",
                severity = SeverityV1.WARN,
                path = "$.a",
                message = "a warn",
            ),
            ViolationV1(
                code = "C_CODE",
                severity = SeverityV1.ERROR,
                path = "$.a",
                message = "a error",
            ),
            ViolationV1(
                code = "A_CODE",
                severity = SeverityV1.ERROR,
                path = "$.a",
                message = "a error earlier",
            ),
            ViolationV1(
                code = "Z_CODE",
                severity = SeverityV1.INFO,
                path = "$.z",
                message = "z info",
            ),
        )

        val sorted = violations.canonicalSorted()

        assertThat(sorted.map { it.code }).containsExactly(
            "A_CODE",
            "C_CODE",
            "B_CODE",
            "A_CODE",
            "Z_CODE",
        ).inOrder()
    }

    @Test
    fun `path helpers build canonical paths`() {
        assertThat(PathV1.root).isEqualTo("$")
        assertThat(PathV1.field(PathV1.root, "page")).isEqualTo("$.page")
        assertThat(PathV1.index(PathV1.root, "entities", 3)).isEqualTo("$.entities[3]")
        assertThat(PathV1.idSelector(PathV1.root, "entities", "E1"))
            .isEqualTo("$.entities[id=E1]")
    }
}
