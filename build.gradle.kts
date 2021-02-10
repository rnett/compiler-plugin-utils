plugins {
    kotlin("jvm") version "1.4.30" apply false
    kotlin("kapt") version "1.4.30" apply false
    id("com.gradle.plugin-publish") version "0.11.0" apply false
    id("com.github.johnrengelman.shadow") version "5.2.0" apply false
    id("com.github.gmazzo.buildconfig") version "2.0.2" apply false
    id("com.vanniktech.maven.publish") version "0.14.0" apply false
    id("org.jetbrains.dokka") version "1.4.20" apply false
}

allprojects {
    group = "com.github.rnett.compiler-plugin-utils"
    version = "0.1.0"

    repositories {
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        mavenCentral()
        maven("https://oss.jfrog.org/artifactory/oss-snapshot-local/")
        google()
        jcenter()
        mavenLocal()
        maven("https://oss.sonatype.org/content/repositories/snapshots") {
            mavenContent { snapshotsOnly() }
        }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    setProperty("GROUP", group)
    setProperty("POM_ARTIFACT_ID", name)
    setProperty("VERSION_NAME", version)

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            useIR = true
            freeCompilerArgs = listOf("-Xjvm-default=compatibility")
        }
    }
}