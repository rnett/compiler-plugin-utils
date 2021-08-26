plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kapt) apply false
    alias(libs.plugins.publish) apply false
    alias(libs.plugins.dokka) apply false
    signing
}

apply("./common.gradle.kts")

subprojects {
    afterEvaluate {
        extensions.getByType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension>().apply {
            sourceSets.all {
                languageSettings {
                    optIn("kotlin.contracts.ExperimentalContracts")
                    optIn("kotlin.RequiresOptIn")
                }
            }

            target {
                attributes {
                    attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
                }
                compilations.configureEach {
                    kotlinOptions {
                        jvmTarget = "1.8"
                    }
                    compileJavaTaskProvider.get().apply {
                        targetCompatibility = "1.8"
                        sourceCompatibility = "1.8"
                    }
                }
            }
        }

        extensions.findByType<com.vanniktech.maven.publish.MavenPublishBaseExtension>()?.apply {
            if (!version.toString().toLowerCase().endsWith("snapshot")) {
                val stagingProfileId = project.findProperty("sonatypeRepositoryId")?.toString()
                publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.DEFAULT, stagingProfileId)
            }
        }
    }
}

tasks.create("documentation") {
    group = "documentation"
    dependsOn(
        ":compiler-plugin-utils:dokkaHtml",
        gradle.includedBuild("plugins").task(":compiler-plugin-utils-compiler-plugin:dokkaHtml"),
        gradle.includedBuild("plugins").task(":compiler-plugin-utils-gradle-plugin:dokkaHtml")
    )
}

tasks.create("publish") {
    group = "publishing"
    dependsOn(
        ":compiler-plugin-utils:publish",
        ":compiler-plugin-utils-native:publish",
        gradle.includedBuild("plugins").task(":compiler-plugin-utils-compiler-plugin:publish"),
        gradle.includedBuild("plugins").task(":compiler-plugin-utils-gradle-plugin:publish")
    )
}

tasks.create("publishToMavenLocal") {
    group = "publishing"
    dependsOn(
        ":compiler-plugin-utils:publishToMavenLocal",
        ":compiler-plugin-utils-native:publishToMavenLocal",
        gradle.includedBuild("plugins").task(":compiler-plugin-utils-compiler-plugin:publishToMavenLocal"),
        gradle.includedBuild("plugins").task(":compiler-plugin-utils-gradle-plugin:publishToMavenLocal")
    )
}
