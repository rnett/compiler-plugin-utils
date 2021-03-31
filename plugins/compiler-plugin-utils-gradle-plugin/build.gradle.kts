plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    kotlin("kapt")
    `maven-publish`
    id("com.github.gmazzo.buildconfig")
    id("com.gradle.plugin-publish")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:1.4.30")

    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.30")

    compileOnly("com.google.auto.service:auto-service-annotations:1.0-rc6")
    kapt("com.google.auto.service:auto-service:1.0-rc6")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
        freeCompilerArgs = listOf("-Xjvm-default=compatibility")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

buildConfig {
    val project = project(":compiler-plugin-utils-compiler-plugin")
    packageName("com.rnett.plugin")
    buildConfigField("String", "PROJECT_GROUP_ID", "\"${project.group}\"")
    buildConfigField("String", "PROJECT_ARTIFACT_ID", "\"${project.name}\"")
    buildConfigField("String", "PROJECT_VERSION", "\"${project.version}\"")
}

gradlePlugin {
    plugins {
        create("compilerPluginUtilsPlugin") {
            id = project.group.toString()
            displayName = "Compiler Plugin Utils Plugin"
            description = "Compiler Plugin Utils Plugin"
            implementationClass = "com.rnett.plugin.GradlePlugin"
        }
    }
}