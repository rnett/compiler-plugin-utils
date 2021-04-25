plugins {
    kotlin("jvm") version "1.4.32" apply false
    kotlin("kapt") version "1.4.32" apply false
    id("com.gradle.plugin-publish") version "0.11.0" apply false
    id("com.github.johnrengelman.shadow") version "5.2.0" apply false
    id("com.github.gmazzo.buildconfig") version "2.0.2" apply false
    id("com.vanniktech.maven.publish") version "0.14.0" apply false
    id("org.jetbrains.dokka") version "1.4.30" apply false
    signing
}

apply("./common.gradle.kts")

subprojects {
    afterEvaluate {
        extensions.getByType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension>().target {
            compilations.configureEach {
                kotlinOptions {
                    jvmTarget = "1.8"
                    useIR = true
                }
                compileJavaTaskProvider.get().apply {
                    targetCompatibility = "1.8"
                    sourceCompatibility = "1.8"
                }
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
        gradle.includedBuild("plugins").task(":compiler-plugin-utils-compiler-plugin:publish"),
        gradle.includedBuild("plugins").task(":compiler-plugin-utils-gradle-plugin:publish")
    )
}

tasks.create("publishToMavenLocal") {
    group = "publishing"
    dependsOn(
        ":compiler-plugin-utils:publishToMavenLocal",
        gradle.includedBuild("plugins").task(":compiler-plugin-utils-compiler-plugin:publishToMavenLocal"),
        gradle.includedBuild("plugins").task(":compiler-plugin-utils-gradle-plugin:publishToMavenLocal")
    )
}
