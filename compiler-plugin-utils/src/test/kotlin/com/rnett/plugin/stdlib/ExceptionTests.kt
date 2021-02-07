package com.rnett.plugin.stdlib

import com.rnett.plugin.tester.BaseIrPluginTest
import com.rnett.plugin.tester.TestGenerator
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf

@OptIn(ExperimentalStdlibApi::class)
class ExceptionTests : BaseIrPluginTest() {

    val exceptions: List<KProperty1<StdlibBuilders, ExceptionBuilders>> = StdlibBuilders::class.declaredMemberProperties
        .filter { it.returnType.isSubtypeOf(typeOf<ExceptionBuilders>()) }
        .map { it as KProperty1<StdlibBuilders, ExceptionBuilders> }

    fun TestGenerator.testsFor(prop: KProperty1<StdlibBuilders, ExceptionBuilders>) {
        val name = prop.name

        replaceIn("test${name}NoArg", "", assertMethod = "assertTrue", suffix = " is $name", irValueType = name) {
            prop.get(stdlib).new()
        }

        replaceIn("test${name}Message", "\"Message\"", suffix = ".message", checkType = name, irValueType = name) {
            prop.get(stdlib).newWithMessage("Message".asConst())
        }


        if (prop.returnType == typeOf<ExceptionBuildersWithCause>()) {

            @Suppress("UNCHECKED_CAST")
            prop as KProperty1<StdlibBuilders, ExceptionBuildersWithCause>

            replaceIn("test${name}MessageAndCauseMessage", "\"Message\"", suffix = ".message", checkType = name, irValueType = name) {
                prop.get(stdlib).newWithMessageAndCause("Message".asConst(), stdlib.Error.new())
            }

            replaceIn("test${name}MessageAndCauseCause", "Error::class", suffix = ".cause!!::class", checkType = name, irValueType = name) {
                prop.get(stdlib).newWithMessageAndCause("Message".asConst(), stdlib.Error.new())
            }

            replaceIn("test${name}Cause", "Error::class", suffix = ".cause!!::class", checkType = name, irValueType = name) {
                prop.get(stdlib).newWithCause(stdlib.Error.new())
            }
        }
    }

    override fun TestGenerator.generateTests() {
        exceptions.forEach {
            testsFor(it)
        }
    }
}