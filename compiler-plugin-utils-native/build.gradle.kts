plugins {
    kotlin("jvm")
    kotlin("js") apply false
    kotlin("kapt")
    `maven-publish` apply true
    id("com.vanniktech.maven.publish")
    id("com.github.rnett.compiler-plugin-utils")
    id("org.jetbrains.dokka")
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler:1.5.0")

    testImplementation(kotlin("test-junit5"))

    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")

    testImplementation(kotlin("reflect"))
    testImplementation("org.jetbrains.kotlin:kotlin-compiler:1.5.0")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.3.5")

    testRuntimeOnly(kotlin("stdlib-js"))
    //TODO this isn't being found.  Can't resolve w/ attributes
//    testRuntimeOnly(kotlin("test-js"))

    testCompileOnly("com.google.auto.service:auto-service-annotations:1.0-rc6")
    kaptTest("com.google.auto.service:auto-service:1.0-rc6")
}

kotlin {
    this.explicitApi()
}

tasks.named("compileKotlin") { dependsOn("syncSource") }
tasks.register<Sync>("syncSource") {
    from(project(":compiler-plugin-utils").sourceSets.main.get().allSource)
    into("src/main/kotlin")
    filter {
        // Replace shadowed imports from plugin module
        when (it) {
            "import org.jetbrains.kotlin.com.intellij.mock.MockProject" -> "import com.intellij.mock.MockProject"
            else -> it
        }
    }
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}