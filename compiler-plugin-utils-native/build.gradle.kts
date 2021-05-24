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
    compileOnly("org.jetbrains.kotlin:kotlin-compiler:$kotlinVersion")
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