package com.rnett.plugin.backend

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.dump
import java.io.File

class IrGeneration(val messageCollector: MessageCollector) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        NamingAdjuster(pluginContext, messageCollector).lower(moduleFragment)
        GetFqNameResolver(pluginContext, messageCollector).lower(moduleFragment)
        File("C:\\Users\\jimne\\Desktop\\My Stuff\\compiler-plugin-utils\\log").writeText(moduleFragment.dump(true))
    }
}