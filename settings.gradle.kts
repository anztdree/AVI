pluginManagement {
    repositories {
        google()
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
rootProject.name = "AVI"
// modul Android dipindah ke android-app/ agar tidak bentrok dengan folder app/ milik Next.js
include(":app")
project(":app").projectDir = file("android-app")
