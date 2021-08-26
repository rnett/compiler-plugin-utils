pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://oss.sonatype.org/content/repositories/snapshots") {
            mavenContent { snapshotsOnly() }
        }
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create(defaultLibrariesExtensionName.get()) {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "plugins"

include("compiler-plugin-utils-compiler-plugin", "compiler-plugin-utils-gradle-plugin")
