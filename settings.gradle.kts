pluginManagement {
    includeBuild("build-logic")
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
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        maven {
            url = uri("https://devrepo.kakao.com/nexus/content/groups/public/")
            content {
                includeGroup("com.kakao.sdk")
            }
        }
        gradlePluginPortal()
    }
}

rootProject.name = "yadanbeopseok"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")

include(":core:model")
include(":core:data")
include(":core:common")
include(":core:database")
include(":core:network")
include(":core:designsystem")
include(":core:ui")
include(":core:datastore")
include(":core:notifications")
include(":core:navigation")
include(":sync")
