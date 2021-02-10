plugins {
    kotlin("jvm")
    id("com.rnett.compiler-plugin-utils") version "1.0-SNAPSHOT"
}

dependencies {
    implementation(project(":compiler-plugin-utils"))
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.4.30")

    testImplementation(kotlin("test-junit5"))

    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")
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

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}