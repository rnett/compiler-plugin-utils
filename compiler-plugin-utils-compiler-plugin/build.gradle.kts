plugins {
    kotlin("jvm")
    kotlin("kapt")
    `maven-publish` apply true
    id("com.github.johnrengelman.shadow") apply true
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.4.30")

    compileOnly("com.google.auto.service:auto-service-annotations:1.0-rc6")
    kapt("com.google.auto.service:auto-service:1.0-rc6")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.freeCompilerArgs += "-Xjvm-default=enable"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
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