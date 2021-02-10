package com.rnett.plugin.tester.plugin

import com.rnett.plugin.ir.messageCollector
import com.rnett.plugin.tester.BaseIrPluginTest
import com.rnett.plugin.tester.getTestObjectName
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import kotlin.reflect.KClass

class TestPlugin(klasses: List<KClass<out BaseIrPluginTest>>) : ComponentRegistrar {
    constructor(vararg klasses: KClass<out BaseIrPluginTest>) : this(klasses.toList())

    val generatedClassMap = klasses.associateBy {
        it.getTestObjectName()
    }

    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        IrGenerationExtension.registerExtension(project, TestPluginIrGeneration(configuration.messageCollector, generatedClassMap))
    }

}
