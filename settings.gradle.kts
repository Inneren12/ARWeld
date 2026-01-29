import org.gradle.kotlin.dsl.withGroovyBuilder

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.jetbrains.kotlinx.kover.aggregation") version "0.8.3"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
extensions.configure<Any>("kover") {
    withGroovyBuilder {
        "enableCoverage"()
    }
}

rootProject.name = "ArWeld"
include(":app")
include(":core-domain")
include(":core-data")
include(":core-auth")
include(":core-structural")
include(":core-ar")
include(":feature-home")
include(":feature-work")
include(":feature-scanner")
include(":feature-arview")
include(":feature-supervisor")
include(":feature-assembler")
include(":feature-qc")
 
