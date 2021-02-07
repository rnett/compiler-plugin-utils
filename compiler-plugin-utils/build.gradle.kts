plugins {
    kotlin("jvm")
    kotlin("kapt")
    `maven-publish` apply true
    id("com.rnett.compiler-plugin-utils") version "1.0-SNAPSHOT"
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.4.30")

    testImplementation(kotlin("test-junit5"))

    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")

    testImplementation(kotlin("reflect"))
    testImplementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.4.30")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.3.5")

    testCompileOnly("com.google.auto.service:auto-service-annotations:1.0-rc6")
    kaptTest("com.google.auto.service:auto-service:1.0-rc6")
}

kotlin {
    sourceSets.all {
        languageSettings.apply {
            useExperimentalAnnotation("kotlin.contracts.ExperimentalContracts")
//            languageVersion = "1.5"
        }
    }
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}

val sourcesJar = tasks.create<Jar>("sourcesJar") {
    classifier = "sources"
    from(kotlin.sourceSets["main"].kotlin.srcDirs)
}

publishing {
    publications {
        create<MavenPublication>("default") {
            from(components["java"])
            artifact(sourcesJar)
        }
    }
}

tasks.getByName("compileKotlin") {
    dependsOn(":compiler-plugin-utils-compiler-plugin:publishToMavenLocal")
}