pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                //includeGroupByRegex("com\\.google\\.devrel\\.ksp.*") // <-- ¡Añade esta línea!
                //includeGroupByRegex("org\\.jetbrains\\.kotlin\\.plugin\\.compose.*") // <-- ¡Añade esta línea!
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

rootProject.name = "Reclutadores"
include(":app")
 