plugins {
    kotlin("jvm")
    kotlin("kapt")
    `maven-publish` apply true
    id("com.github.johnrengelman.shadow") apply true
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.dokka")
}

val kotlinVersion: String by extra

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion")

    compileOnly("com.google.auto.service:auto-service-annotations:1.0-rc6")
    kapt("com.google.auto.service:auto-service:1.0-rc6")
}