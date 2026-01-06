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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ArWeld"
include(":app")
include(":core-domain")
include(":core-data")
include(":core-auth")
include(":core-structural")
include(":feature-home")
include(":feature-work")
include(":feature-scanner")
include(":feature-arview")
include(":feature-supervisor")
include(":feature-assembler")
include(":feature-qc")
 
