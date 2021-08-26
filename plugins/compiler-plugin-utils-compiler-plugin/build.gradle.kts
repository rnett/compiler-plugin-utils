plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
    id(libs.plugins.kapt.get().pluginId)

    alias(libs.plugins.shadow)

    id(libs.plugins.publish.get().pluginId)
    id(libs.plugins.dokka.get().pluginId)
}

dependencies {
    compileOnly(libs.kotlin.compiler.embeddable)

    compileOnly(libs.autoservice.annotations)
    kapt(libs.autoservice)
}