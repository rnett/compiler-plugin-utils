package com.rnett.plugin.tester

import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.superclasses

fun KClass<out BaseIrPluginTest>.getTestObjectName() =
    findAnnotation<PluginTests>()?.testObjectName?.ifBlank { null }
        ?: simpleName
        ?: error("No specified test object name, and class does not have a valid simple name")

fun <T : BaseIrPluginTest> KClass<T>.generateTestObjectSrc(obj: BaseIrPluginTest): String {
    val klass = this
    check(klass.isInstance(obj)) { "Passed object $obj was not an instance of passed class $klass" }

    return buildString {

        val indents = mutableListOf<String>()
        operator fun String.unaryPlus() = appendLine(this.prependIndent(indents.joinToString("")))

        fun withIndent(block: () -> Unit) {
            indents += "    "
            block()
            indents.removeLast()

        }

        val annotation = klass.findAnnotation<PluginTests>()
        annotation?.imports?.forEach {
            +"import $it"
        }

        klass.superclasses.forEach {
            it.findAnnotation<PluginTests>()?.imports?.forEach {
                +"import $it"
            }
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
            klass.members.forEach { member ->
                member.findAnnotation<TestProperty>()?.let {
                    val type = if (it.type.isNotBlank())
                        ": ${it.type}"
                    else
                        ""

                    +"val ${member.name}$type = ${it.src}"
                }

                member.findAnnotation<TestFunction>()?.let {
                    +"fun ${member.name}() = ${it.src}"
                }
            }

            appendLine()
            +"fun <T> irValue(): T = error(\"Should be replaced by compiler\")"
            appendLine()

            val tests = BaseIrPluginTest::allTests.invoke(obj)

            tests.filterIsInstance<PluginTest.Replace>().forEach {
                +"fun ${it.name}(${it.args}): ${it.returnType} = error(\"Should be replaced by compiler\")"
                appendLine()
            }

            tests.forEach {
                it.apply { generate() }
            }

        }

        +"}"
    }.replace("\t", "    ")
}