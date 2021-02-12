package com.rnett.plugin.tester

import com.tschuchort.compiletesting.KotlinCompilation
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.full.functions
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalStdlibApi::class)
@TestFactory
fun MakeTests(tests: BaseIrPluginTest, klass: KClass<out BaseIrPluginTest>) = buildList<DynamicTest> {
    val (compileResult, files) = compileTests(tests to klass)

    val objectClass = try {
        compileResult.classLoader.loadClass(klass.getTestObjectName())
    } catch (e: ClassNotFoundException) {
        error(buildString {
            appendLine("Couldn't compile.  Messages: ")
            appendLine(compileResult.messages)
            appendLine()
            appendLine("-".repeat(20))
            appendLine()
            appendLine("Sources:")
            files.forEach {
                appendLine("----- ${it.first} -----")
                appendLine(it.second)
            }
        })
    }
    val objectInstance = objectClass.kotlin.objectInstance
        ?: error("Object was not initialized.  Compiler messages: " + compileResult.messages)

    add(DynamicTest.dynamicTest("Compile") {
        assertEquals(
            KotlinCompilation.ExitCode.OK,
            compileResult.exitCode,
            "In-plugin tests failed: " + compileResult.messages
        )
    })

    addAll(objectClass.kotlin.functions.filter { it.annotations.any { it is Test } }.map {
        DynamicTest.dynamicTest(it.name) {
            try {
                it.call(objectInstance)
            } catch (e: InvocationTargetException) {
                throw e.cause!!
            }
        }
    })

}