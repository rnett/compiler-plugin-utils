plugins {
    kotlin("jvm") version "1.4.30" apply false
    kotlin("kapt") version "1.4.30" apply false
    id("com.gradle.plugin-publish") version "0.11.0" apply false
    id("com.github.johnrengelman.shadow") version "5.2.0" apply false
    id("com.github.gmazzo.buildconfig") version "2.0.2" apply false
    id("com.vanniktech.maven.publish") version "0.14.0" apply false
    id("org.jetbrains.dokka") version "1.4.20" apply false
    signing
}

apply("./common.gradle.kts")

//allprojects {
//    apply(plugin = "org.gradle.signing")
//    signing {
//        if (!(project.extra["isReleaseVersion"] as Boolean)) {
//            this.isRequired = false
//        }
//        if (project.hasProperty("RELEASE_SIGNING_ENABLED") && project.property("RELEASE_SIGNING_ENABLED").let {
//                if (it is Boolean)
//                    it == false
//                else if (it is String)
//                    it == "false"
//                else
//                    false
//            }) {
//            this.isRequired = false
//        }
//    }
//}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            useIR = true
            freeCompilerArgs = listOf("-Xjvm-default=compatibility")
        }
    }
}