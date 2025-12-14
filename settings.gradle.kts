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
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                username = "mapbox"
                password = "sk.eyJ1Ijoibmh1dHJhbmduZyIsImEiOiJjbWlkNDRtZW8wMmRqMmxzYjZueWZsZ2F5In0._DV-fH37NHdfnpiAm7MElw"
            }
        }
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

rootProject.name = "Demo"
include(":app")
 