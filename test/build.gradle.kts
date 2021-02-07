plugins {
    kotlin("jvm")
    id("com.rnett.compiler-plugin-utils") version "1.0-SNAPSHOT"
}

dependencies {
    implementation(project(":compiler-plugin-utils"))
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.4.30")
}

kotlin {
    sourceSets.all {
        languageSettings.apply {
            useExperimentalAnnotation("kotlin.contracts.ExperimentalContracts")
        }
    }
}

tasks.getByName("compileKotlin") {
    dependsOn(":compiler-plugin-utils-compiler-plugin:publishToMavenLocal")
}