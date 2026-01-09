// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.compose.compiler) apply false
    kotlin("jvm") version "2.0.21" apply false
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
