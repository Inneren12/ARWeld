import kotlinx.kover.api.KoverAggregationType
import kotlinx.kover.api.KoverMetricType

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kover.aggregation)
    kotlin("jvm") version "2.0.21" apply false
}

subprojects {
    plugins.withId("org.jetbrains.kotlin.jvm") {
        apply(plugin = "org.jetbrains.kotlinx.kover")
    }
    plugins.withId("org.jetbrains.kotlin.android") {
        apply(plugin = "org.jetbrains.kotlinx.kover")
    }
}

koverMerged {
    enable()
    htmlReport {
        onCheck = false
    }
    xmlReport {
        onCheck = false
    }
    verify {
        rule {
            name = "overall-baseline"
            bound {
                minValue = 25
                metric = KoverMetricType.INSTRUCTION
                aggregation = KoverAggregationType.COVERED_PERCENTAGE
            }
        }
        rule {
            name = "core-domain-baseline"
            filters {
                projects {
                    includes.add(":core-domain")
                }
            }
            bound {
                minValue = 60
                metric = KoverMetricType.INSTRUCTION
                aggregation = KoverAggregationType.COVERED_PERCENTAGE
            }
        }
    }
}

tasks.register("koverHtmlReport") {
    group = "verification"
    description = "Generates merged HTML coverage report."
    dependsOn("koverMergedHtmlReport")
}

tasks.register("koverXmlReport") {
    group = "verification"
    description = "Generates merged XML coverage report."
    dependsOn("koverMergedXmlReport")
}

tasks.register("koverVerify") {
    group = "verification"
    description = "Verifies merged coverage thresholds."
    dependsOn("koverMergedVerify")
}

tasks.register("s1QualityGate") {
    group = "verification"
    description = "Runs the Sprint 1 local quality gate: assemble, unit tests, and lint (NO instrumentation)."
    dependsOn(
        ":app:assembleDebug",
        ":app:assembleRelease",
        ":app:testDebugUnitTest",
        ":app:lintDebug"
    )
}

tasks.register("s2QualityGate") {
    group = "verification"
    description = "Runs the Sprint 2 local quality gate: assemble, unit tests, and lint (NO instrumentation)."
    dependsOn(
        ":app:assembleDebug",
        ":app:assembleRelease",
        ":app:testDebugUnitTest",
        ":app:lintDebug"
    )
}

tasks.register("s2InstrumentationSmoke") {
    group = "verification"
    description = "Runs instrumentation smoke tests on managed devices (separate from quality gates)."
    dependsOn(":app:pixel6Api34DebugAndroidTest")
}
