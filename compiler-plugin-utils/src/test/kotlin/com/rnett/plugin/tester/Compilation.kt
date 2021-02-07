package com.rnett.plugin.tester

import com.rnett.plugin.tester.plugin.TestPlugin
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import kotlin.reflect.KClass

fun compileTests(vararg klasses: Pair<BaseIrPluginTest, KClass<out BaseIrPluginTest>>): Pair<KotlinCompilation.Result, List<Pair<String, String>>> {
    val files = klasses.map { (it.second.getTestObjectName() + ".kt") to it.second.generateTestObjectSrc(it.first) }
    return compile(
        files.map { SourceFile.kotlin(it.first, it.second) },
        TestPlugin(klasses.map { it.second })
    ) to files
}

fun compile(
    sourceFiles: List<SourceFile>,
    plugin: ComponentRegistrar
): KotlinCompilation.Result {
    return KotlinCompilation().apply {
        sources = sourceFiles
        useIR = true
        compilerPlugins = listOf(plugin)
        inheritClassPath = true
    }.compile()
}

fun compile(
    sourceFile: SourceFile,
    plugin: ComponentRegistrar
): KotlinCompilation.Result {
    return compile(listOf(sourceFile), plugin)
}