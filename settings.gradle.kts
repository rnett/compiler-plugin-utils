pluginManagement {
    repositories {
        maven("https://dl.bintray.com/kotlin/kotlin-eap")

        mavenCentral()

        maven("https://plugins.gradle.org/m2/")
        gradlePluginPortal()
        mavenLocal()
    }
}
rootProject.name = "compiler-plugin-utils"
include("compiler-plugin-utils", "test", "compiler-plugin-utils-compiler-plugin", "compiler-plugin-utils-gradle-plugin")
