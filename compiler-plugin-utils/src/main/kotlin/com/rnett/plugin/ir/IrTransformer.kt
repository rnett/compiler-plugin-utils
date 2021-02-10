package com.rnett.plugin.ir

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.ScopeWithIr
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrSingleStatementBuilder
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrPackageFragment

public abstract class IrTransformer(
    override val context: IrPluginContext,
    public val messageCollector: MessageCollector
) :
    IrElementTransformerVoidWithContext(), FileLoweringPass,
    HasContext, KnowsCurrentFile {

    final override lateinit var file: IrFile
        private set

    override fun lower(irFile: IrFile) {
        file = irFile
        irFile.transformChildrenVoid()
    }

    override fun visitPackageFragment(declaration: IrPackageFragment): IrPackageFragment {
        // supports adding declarations to file during passes
        if (declaration is IrFile) {
            var declarations = declaration.declarations.toSet()
            while (declarations.isNotEmpty()) {
                declarations.forEach { it.transformChildrenVoid() }
                declarations = declaration.declarations.toSet() - declarations
            }
            return declaration
        }
        return super.visitPackageFragment(declaration)
    }

    protected inline fun <T : IrElement> buildInCurrent(
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        build: IrSingleStatementBuilder.() -> T
    ): T = IrSingleStatementBuilder(
        context, `access$currentScope`?.scope
            ?: error("Not in a scope"), startOffset, endOffset
    ).build(build)

    @PublishedApi
    internal val `access$currentScope`: ScopeWithIr?
        get() = currentScope
}