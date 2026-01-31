import org.gradle.api.artifacts.ProjectDependency
import org.gradle.kotlin.dsl.withGroovyBuilder

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kover.aggregation) apply false
    kotlin("jvm") version "2.0.21" apply false
}

// Kover aggregation is enabled via settings.gradle.kts; reports are generated with -Pkover.
val coverageUnit = Class.forName("kotlinx.kover.gradle.plugin.dsl.CoverageUnit")
    .enumConstants
    .first { (it as Enum<*>).name == "INSTRUCTION" }
val aggregationType = Class.forName("kotlinx.kover.gradle.plugin.dsl.AggregationType")
    .enumConstants
    .first { (it as Enum<*>).name == "COVERED_PERCENTAGE" }

val updateGoldenProperty = providers.gradleProperty("updateGolden").orNull
val updateGoldenEnv = System.getenv("UPDATE_GOLDEN")
val updateGolden = listOf(updateGoldenProperty, updateGoldenEnv).any { value ->
    value?.equals("true", ignoreCase = true) == true
}
val isCi = System.getenv("CI")?.equals("true", ignoreCase = true) == true
if (isCi && updateGolden) {
    throw GradleException("updateGolden is not allowed in CI. Remove -PupdateGolden=true or UPDATE_GOLDEN.")
}

extensions.findByName("kover")?.withGroovyBuilder {
    "merge" {
        "allProjects"()
    }
    "reports" {
        "total" {
            "html" {
                "onCheck"(false)
            }
            "xml" {
                "onCheck"(false)
            }
            "verify" {
                "rule"("overall-baseline") {
                    "minBound"(25, coverageUnit, aggregationType)
                }
                "rule"("core-domain-baseline") {
                    "filters" {
                        "projects" {
                            "includes" {
                                "add"(":core-domain")
                            }
                        }
                    }
                    "minBound"(60, coverageUnit, aggregationType)
                }
            }
        }
    }
}

subprojects {
    tasks.withType<Test>().configureEach {
        systemProperty("updateGolden", updateGolden.toString())
    }
}


tasks.register("verifyArViewDebugLogging") {
    group = "verification"
    description = "Fails if feature-arview contains Log.d/i calls without BuildConfig.DEBUG guards."
    doLast {
        val sourceRoot = file("feature-arview/src/main/kotlin")
        if (!sourceRoot.exists()) return@doLast
        val violations = mutableListOf<String>()
        sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                val lines = file.readLines()
                val recentNonEmpty = java.util.ArrayDeque<String>()
                lines.forEachIndexed { index, line ->
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty()) {
                        if (recentNonEmpty.size >= 3) {
                            recentNonEmpty.removeFirst()
                        }
                        recentNonEmpty.addLast(trimmed)
                    }
                    if (trimmed.contains("Log.d(") || trimmed.contains("Log.i(")) {
                        val guardFound = recentNonEmpty.any { it.contains("BuildConfig.DEBUG") }
                        if (!guardFound) {
                            val relativePath = file.relativeTo(projectDir)
                            violations.add("$relativePath:${index + 1} $trimmed")
                        }
                    }
                }
                recentNonEmpty.clear()
            }
        if (violations.isNotEmpty()) {
            throw GradleException(
                "Unguarded Log.d/i calls found in feature-arview. Add BuildConfig.DEBUG guards:\n" +
                    violations.joinToString("\n"),
            )
        }
    }
}

tasks.register("verifyCoreArBoundaries") {
    group = "verification"
    description = "Fails if :core-ar depends on forbidden modules (:core-domain, :core-data)."
    doLast {
        val coreArProject = project.findProject(":core-ar") ?: return@doLast
        val forbiddenModules = setOf(":core-domain", ":core-data")
        val configsToCheck = listOf("implementation", "api", "compileOnly", "runtimeOnly")
        val violations = configsToCheck.flatMap { configName ->
            val config = coreArProject.configurations.findByName(configName) ?: return@flatMap emptyList()
            config.dependencies.withType(ProjectDependency::class.java)
                .mapNotNull { dependency ->
                    val path = dependency.dependencyProject.path
                    if (path in forbiddenModules) {
                        "${configName}: $path"
                    } else {
                        null
                    }
                }
        }
        if (violations.isNotEmpty()) {
            throw GradleException(
                "Forbidden :core-ar dependencies detected. Remove project dependencies on " +
                    "${forbiddenModules.joinToString()}.\n" +
                    violations.joinToString(separator = "\n"),
            )
        }
    }
}

tasks.register("s1QualityGate") {
    group = "verification"
    description = "Runs the Sprint 1 local quality gate: assemble, unit tests, and lint (NO instrumentation)."
    dependsOn(
        "verifyCoreArBoundaries",
        "verifyArViewDebugLogging",
        "goldenTest",
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
        "verifyCoreArBoundaries",
        "verifyArViewDebugLogging",
        "goldenTest",
        ":app:assembleDebug",
        ":app:assembleRelease",
        ":app:testDebugUnitTest",
        ":app:lintDebug"
    )
}

tasks.register("goldenTest") {
    group = "verification"
    description = "Runs golden tests used as CI gate."
    dependsOn(
        ":core-ar:test",
        ":feature-supervisor:testDebugUnitTest",
    )
}

tasks.register("s2InstrumentationSmoke") {
    group = "verification"
    description = "Runs instrumentation smoke tests on managed devices (separate from quality gates)."
    dependsOn(":app:pixel6Api34DebugAndroidTest")
}
