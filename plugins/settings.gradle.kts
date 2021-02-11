pluginManagement {
    repositories {
        maven("https://dl.bintray.com/kotlin/kotlin-eap")

        mavenCentral()

        maven("https://plugins.gradle.org/m2/")
        jcenter()
        gradlePluginPortal()
        mavenLocal()
    }
}
rootProject.name = "compiler-plugin-utils-plugins"
include("compiler-plugin-utils-compiler-plugin", "compiler-plugin-utils-gradle-plugin")
