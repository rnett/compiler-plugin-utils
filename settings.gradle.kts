pluginManagement {
    repositories {
        mavenCentral()
        jcenter()
        gradlePluginPortal()
        mavenLocal()
    }
}
includeBuild("plugins")
rootProject.name = "compiler-plugin-utils"
include("compiler-plugin-utils")
