pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://oss.sonatype.org/content/repositories/snapshots") {
            mavenContent { snapshotsOnly() }
        }
    }
}

enableFeaturePreview("VERSION_CATALOGS")

includeBuild("plugins")

rootProject.name = "compiler-plugin-utils"

include("compiler-plugin-utils", "compiler-plugin-utils-native")
