import java.net.URL

plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
    id(libs.plugins.kotlin.js.get().pluginId) apply false
    id(libs.plugins.kapt.get().pluginId)

    id("com.github.rnett.compiler-plugin-utils")

    id(libs.plugins.publish.get().pluginId)
    id(libs.plugins.dokka.get().pluginId)
}

dependencies {
    compileOnly(libs.kotlin.compiler.embeddable)

    testImplementation(kotlin("test-junit5"))

    testImplementation(kotlin("reflect"))
    testImplementation(libs.kotlin.compiler.embeddable)
    testImplementation(libs.compile.testing)

    testRuntimeOnly(kotlin("stdlib-js"))
    //TODO this isn't being found.  Can't resolve w/ attributes
//    testRuntimeOnly(kotlin("test-js"))

    testCompileOnly(libs.autoservice.annotations)
    kaptTest(libs.autoservice)
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