package com.rnett.plugin.tester

import com.tschuchort.compiletesting.KotlinCompilation
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.full.functions
import kotlin.test.Test
import kotlin.test.assertEquals

//TODO make the JS tests run things.  Gen and run a main method?

@OptIn(ExperimentalStdlibApi::class)
@TestFactory
fun MakeTests(tests: BaseIrPluginTest, klass: KClass<out BaseIrPluginTest>, doJs: Boolean = false) = buildList<DynamicTest> {
    val (compileResult, jsResult, files) = compileTests(tests to klass, doJs = doJs)

    add(DynamicTest.dynamicTest("Jvm Compile") {
        assertEquals(
            KotlinCompilation.ExitCode.OK,
            compileResult.exitCode,
            "In-plugin tests failed: " + compileResult.messages
        )
    })

    if (jsResult != null) {
        add(DynamicTest.dynamicTest("JS Compile") {
            assertEquals(
                KotlinCompilation.ExitCode.OK,
                jsResult.exitCode,
                "In-plugin tests failed: " + jsResult.messages
            )
        })
    }

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