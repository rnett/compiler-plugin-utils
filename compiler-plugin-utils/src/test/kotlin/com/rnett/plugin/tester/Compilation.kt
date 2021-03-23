package com.rnett.plugin.tester

import com.rnett.plugin.tester.plugin.TestPlugin
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinJsCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import kotlin.reflect.KClass

data class CompileTestResult(
    val jvmResult: KotlinCompilation.Result,
    val jsResult: KotlinJsCompilation.Result?,
    val files: List<Pair<String, String>>,
)

fun compileTests(vararg klasses: Pair<BaseIrPluginTest, KClass<out BaseIrPluginTest>>, doJs: Boolean = true): CompileTestResult {
    val files = klasses.map { (it.second.getTestObjectName() + ".kt") to it.second.generateTestObjectSrc(it.first) }

    val jvmSources = files.map { SourceFile.kotlin(it.first, it.second) }
    val plugin = TestPlugin(klasses.map { it.second })

    val result = compileJvm(jvmSources, plugin)

    val jsResult = if (doJs) {
        val jsFiles = klasses.map { (it.second.getTestObjectName() + ".kt") to it.second.generateTestObjectSrc(it.first, true) }
        compileJs(jsFiles.map { SourceFile.kotlin(it.first, it.second) }, plugin)
    } else null

    return CompileTestResult(result, jsResult, files)
}

fun compileJvm(
    sourceFiles: List<SourceFile>,
    plugin: ComponentRegistrar,
): KotlinCompilation.Result {
    return KotlinCompilation().apply {
        sources = sourceFiles
        useIR = true
        compilerPlugins = listOf(plugin)
        inheritClassPath = true
    }.compile()
}

fun compileJs(
    sourceFiles: List<SourceFile>,
    plugin: ComponentRegistrar,
): KotlinJsCompilation.Result {
    return KotlinJsCompilation().apply {
        sources = sourceFiles
        irProduceJs = true
        irProduceKlibDir = true
        compilerPlugins = listOf(plugin)
        inheritClassPath = true
    }.compile()
}