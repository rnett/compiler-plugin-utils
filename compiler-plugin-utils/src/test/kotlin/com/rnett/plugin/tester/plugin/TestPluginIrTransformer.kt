package com.rnett.plugin.tester.plugin

import com.rnett.plugin.ir.IrTransformer
import com.rnett.plugin.tester.BaseIrPluginTest
import com.rnett.plugin.tester.PluginTestContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.name.FqName
import kotlin.reflect.KClass

fun constructClass(
    klass: KClass<out BaseIrPluginTest>,
    context: IrPluginContext,
    messageCollector: MessageCollector,
    testObject: IrClass
): BaseIrPluginTest {
    val cont = klass.constructors.first { it.parameters.isEmpty() }
    return cont.call().apply {
        testContext = PluginTestContext(context, messageCollector, testObject)
    }
}

class TestPluginIrTransformer(
    context: IrPluginContext,
    messageCollector: MessageCollector,
    val generatedClassMap: Map<String, KClass<out BaseIrPluginTest>>
) : IrTransformer(context, messageCollector) {
    override fun visitClassNew(declaration: IrClass): IrStatement {
        if (declaration.hasAnnotation(FqName("com.rnett.plugin.tester.plugin.TestObject"))) {
            val klass = generatedClassMap[declaration.name.asString()]
                ?: error("No test class found for name ${declaration.name.asString()}")
            constructClass(klass, context, messageCollector, declaration).buildAllTests()
        }
        return super.visitClassNew(declaration)
    }
}