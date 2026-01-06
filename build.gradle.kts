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
    description = "Runs the Sprint 1 local quality gate: assemble, unit tests, lint, and instrumentation smoke."
}

gradle.projectsEvaluated {
    val requiredTasks = mutableListOf<String>()

    fun addIfExists(taskPath: String) {
        if (tasks.findByPath(taskPath) != null) requiredTasks += taskPath
    }

    fun addFirstExisting(vararg taskPaths: String) {
        taskPaths.firstOrNull { tasks.findByPath(it) != null }?.let(requiredTasks::add)
    }

    addIfExists(":app:assembleDebug")
    addIfExists(":app:assembleRelease")
    addFirstExisting(":app:testDebugUnitTest", ":app:test")
    addFirstExisting(":app:lintDebug", ":app:lint")
    addFirstExisting(
        ":app:allDevicesDebugAndroidTest",
        ":app:gmdDebugAndroidTest",
        ":app:connectedDebugAndroidTest"
    )

    tasks.named("s1QualityGate").configure {
        dependsOn(requiredTasks)
    }
}