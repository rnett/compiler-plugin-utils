package com.rnett.plugin.tester.plugin

import com.rnett.plugin.tester.BaseIrPluginTest
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import kotlin.reflect.KClass

class TestPluginIrGeneration(val messageCollector: MessageCollector, val generatedClassMap: Map<String, KClass<out BaseIrPluginTest>>) :
    IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        TestPluginIrTransformer(pluginContext, messageCollector, generatedClassMap).lower(moduleFragment)
    }
}