package com.rnett.plugin.tester

import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.findAnnotation

fun KClass<out BaseIrPluginTest>.getTestObjectName() =
    findAnnotation<PluginTests>()?.testObjectName?.ifBlank { null }
        ?: simpleName
        ?: error("No specified test object name, and class does not have a valid simple name")

fun <T : BaseIrPluginTest> KClass<T>.generateTestObjectSrc(): String {
    val klass = this
    return buildString {

        val indents = mutableListOf<String>()
        operator fun String.unaryPlus() = appendLine(this.prependIndent(indents.joinToString("")))

        fun withIndent(block: () -> Unit) {
            indents += "\t"
            block()
            indents.removeLast()

        }

        val annotation = klass.annotations.firstIsInstanceOrNull<PluginTests>()
        annotation?.imports?.forEach {
            +"import $it"
        }
        appendLine()
        +"import kotlin.test.Test"
        +"import kotlin.test.assertEquals"
        +"import kotlin.test.assertTrue"
        appendLine()

        val name = getTestObjectName()


        +"@com.rnett.plugin.tester.plugin.TestObject"
        +"object $name {"
        withIndent {
            klass.declaredMembers.forEach { member ->
                member.findAnnotation<TestProperty>()?.let {
                    +"val ${member.name} = ${it.src}"
                }

                member.findAnnotation<TestFunction>()?.let {
                    +"fun ${member.name}() = ${it.src}"
                }
            }

            appendLine()
            +"fun <T> irValue(): T = error(\"Should be replaced by compiler\")"
            appendLine()

            klass.declaredFunctions.forEach { func ->
                func.findAnnotation<PluginReplace>()?.let {
                    +"fun ${func.name}(${it.args}): ${it.returnType} = error(\"Should be replaced by compiler\")"
                    appendLine()
                }

                (func.findAnnotation<PluginTestReplaceIn>()?.toSpec()
                    ?: func.findAnnotation<PluginTestReplaceInAlso>()?.toSpec()
                        )?.let {
                        val funcString = buildString {
                            +"@Test"
                            +"fun ${func.name}(){"
                            withIndent {
                                val body = it.generate()
                                +"println(\"\"\"\tTest: ${func.name}\n$body\"\"\".trimIndent())"
                                +body
                            }
                            +"}"
                        }
                        appendLine()
                    }
            }

        }

        +"}"
    }
}