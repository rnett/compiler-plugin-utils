pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://oss.sonatype.org/content/repositories/snapshots") {
            mavenContent { snapshotsOnly() }
        }
    }
}
rootProject.name = "plugins"
include("compiler-plugin-utils-compiler-plugin", "compiler-plugin-utils-gradle-plugin")
