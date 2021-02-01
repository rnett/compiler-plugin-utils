package com.rnett.plugin.tester

import com.rnett.plugin.tester.plugin.TestPlugin
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import kotlin.reflect.KClass

fun compileTests(vararg klasses: KClass<out BaseIrPluginTest>): KotlinCompilation.Result = compile(
    klasses.map { SourceFile.kotlin(it.getTestObjectName() + ".kt", it.generateTestObjectSrc()) },
    TestPlugin(*klasses)
)

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