package com.rnett.plugin

import com.google.auto.service.AutoService
import com.rnett.plugin.backend.IrGeneration
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(CommandLineProcessor::class)
class CompilerPluginUtilsPlugin : CommandLineProcessor {
    override val pluginId: String = "com.rnett.compiler-plugin-utils-compiler-plugin"
    override val pluginOptions: Collection<AbstractCliOption> = emptyList()
}

@AutoService(ComponentRegistrar::class)
class PluginComponentRegistar : ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)

        IrGenerationExtension.registerExtension(project, IrGeneration(messageCollector))
    }

}
