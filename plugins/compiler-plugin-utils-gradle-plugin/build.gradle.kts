plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
    id(libs.plugins.kapt.get().pluginId)

    `java-gradle-plugin`
    alias(libs.plugins.buildconfig)

    id(libs.plugins.publish.get().pluginId)
    id(libs.plugins.dokka.get().pluginId)
}
dependencies {
    implementation(libs.kgp.api)

    compileOnly(libs.kgp)

    compileOnly(libs.autoservice.annotations)
    kapt(libs.autoservice)
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
            id = "com.github.rnett.compiler-plugin-utils"
            displayName = "Compiler Plugin Utils Plugin"
            description = "Compiler Plugin Utils Plugin"
            implementationClass = "com.rnett.plugin.GradlePlugin"
        }
    }
}