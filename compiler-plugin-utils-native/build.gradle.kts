plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
    id(libs.plugins.kotlin.js.get().pluginId) apply false
    id(libs.plugins.kapt.get().pluginId)

    id("com.github.rnett.compiler-plugin-utils")

    id(libs.plugins.publish.get().pluginId)
    id(libs.plugins.dokka.get().pluginId)
}

dependencies {
    compileOnly(libs.kotlin.compiler)
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