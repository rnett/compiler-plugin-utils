import java.net.URL

plugins {
    kotlin("jvm")
    kotlin("js") apply false
    kotlin("kapt")
    `maven-publish` apply true
    id("com.vanniktech.maven.publish")
    id("com.github.rnett.compiler-plugin-utils")
    id("org.jetbrains.dokka")
}

val kotlinVersion: String by extra

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion")

    testImplementation(kotlin("test-junit5"))

    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")

    testImplementation(kotlin("reflect"))
    testImplementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.1")

    testRuntimeOnly(kotlin("stdlib-js"))
    //TODO this isn't being found.  Can't resolve w/ attributes
//    testRuntimeOnly(kotlin("test-js"))

    testCompileOnly("com.google.auto.service:auto-service-annotations:1.0-rc6")
    kaptTest("com.google.auto.service:auto-service:1.0-rc6")
}

kotlin {
    this.explicitApi()
}

val sourceLinkBranch: String by project

tasks.dokkaHtml {
    moduleName.set("Compiler Plugin Utils")
    moduleVersion.set(version.toString())

    dokkaSourceSets {
        all {
            includes.from("docs.md")
            this.includeNonPublic.set(false)

            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl.set(URL("https://github.com/rnett/compiler-plugin-utils/blob/$sourceLinkBranch/compiler-plugin-utils/src/main/kotlin"))
                remoteLineSuffix.set("#L")
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}