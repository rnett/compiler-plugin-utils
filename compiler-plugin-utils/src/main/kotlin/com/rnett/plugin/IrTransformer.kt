package com.rnett.plugin

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrSingleStatementBuilder
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.declarations.path
import java.io.File

abstract class IrTransformer(override val context: IrPluginContext, override val messageCollector: MessageCollector) :
    IrElementTransformerVoidWithContext(), FileLoweringPass,
    HasContext, HasMessageCollector {
    override lateinit var file: File
    override lateinit var fileText: String

    override fun lower(irFile: IrFile) {
        file = File(irFile.path)
        fileText = file.readText()
        irFile.transformChildrenVoid()
    }

    protected val currentSymbolOwner get() = allScopes.lastOrNull { it.irElement is IrSymbolOwner }?.irElement as? IrSymbolOwner
    fun <T : IrElement> buildInCurrent(
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        build: IrSingleStatementBuilder.() -> T
    ) = IrSingleStatementBuilder(context, currentScope?.scope ?: error("Not in a scope"), startOffset, endOffset).build(build)
}