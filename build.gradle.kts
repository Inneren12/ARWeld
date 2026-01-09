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
}

tasks.register("s2QualityGate") {
    group = "verification"
    description = "Runs the Sprint 2 local quality gate: assemble, unit tests, and lint (NO instrumentation)."
}

tasks.register("s2InstrumentationSmoke") {
    group = "verification"
    description = "Runs instrumentation smoke tests on managed devices (separate from quality gates)."
}

gradle.projectsEvaluated {
    // Quality gate tasks (NO instrumentation)
    val qualityGateTasks = mutableListOf<String>()

    // Instrumentation smoke tasks (separate)
    val instrumentationTasks = mutableListOf<String>()

    fun addIfExists(taskPath: String) {
        if (tasks.findByPath(taskPath) != null) qualityGateTasks += taskPath
    }

    fun addFirstExisting(vararg taskPaths: String) {
        taskPaths.firstOrNull { tasks.findByPath(it) != null }?.let(qualityGateTasks::add)
    }

    fun addFirstInstrumentationTask(vararg taskPaths: String) {
        taskPaths.firstOrNull { tasks.findByPath(it) != null }?.let(instrumentationTasks::add)
    }

    // Quality gate dependencies
    addIfExists(":app:assembleDebug")
    addIfExists(":app:assembleRelease")
    addFirstExisting(":app:testDebugUnitTest", ":app:test")
    addFirstExisting(":app:lintDebug", ":app:lint")

    // Instrumentation dependencies (separate)
    addFirstInstrumentationTask(
        ":app:allDevicesDebugAndroidTest",
        ":app:pixel6Api34DebugAndroidTest",
        ":app:gmdDebugAndroidTest",
        ":app:connectedDebugAndroidTest"
    )

    tasks.named("s1QualityGate").configure {
        dependsOn(qualityGateTasks)
    }

    tasks.named("s2QualityGate").configure {
        dependsOn(qualityGateTasks)
    }

    tasks.named("s2InstrumentationSmoke").configure {
        dependsOn(instrumentationTasks)
    }
}
