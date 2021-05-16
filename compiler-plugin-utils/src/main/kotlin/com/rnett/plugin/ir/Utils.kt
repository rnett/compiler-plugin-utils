package com.rnett.plugin.ir

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.IrGeneratorWithScope
import org.jetbrains.kotlin.ir.builders.IrSingleStatementBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrAnonymousInitializer
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.addMember
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrAnonymousInitializerSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.platform.js.isJs
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public val CompilerConfiguration.messageCollector: MessageCollector
    get() = get(
        CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
        MessageCollector.NONE
    )

public fun IrBuilderWithScope.irVararg(elementType: IrType, elements: Iterable<IrExpression>): IrVararg =
    IrVarargImpl(
        startOffset,
        endOffset,
        context.irBuiltIns.arrayClass.typeWith(elementType),
        elementType,
        elements.toList()
    )

public fun IrGeneratorContext.createIrBuilder(
    symbol: IrSymbol,
    startOffset: Int = UNDEFINED_OFFSET,
    endOffset: Int = UNDEFINED_OFFSET,
): DeclarationIrBuilder =
    DeclarationIrBuilder(this, symbol, startOffset, endOffset)

public inline fun <T : IrElement> IrGeneratorWithScope.buildStatement(
    startOffset: Int = UNDEFINED_OFFSET,
    endOffset: Int = UNDEFINED_OFFSET,
    origin: IrStatementOrigin? = null,
    builder: IrSingleStatementBuilder.() -> T,
): T {
    contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }
    return IrSingleStatementBuilder(context, scope, startOffset, endOffset, origin).build(builder)
}

@OptIn(ObsoleteDescriptorBasedAPI::class)
public inline fun IrClass.addAnonymousInitializer(builder: IrAnonymousInitializer.() -> Unit): IrAnonymousInitializer {
    contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }
    return this.factory.createAnonymousInitializer(
        UNDEFINED_OFFSET,
        UNDEFINED_OFFSET,
        IrDeclarationOrigin.DEFINED,
        IrAnonymousInitializerSymbolImpl(this.descriptor)
    ).apply(builder).also {
        this.addMember(it)
        it.parent = this
    }
}

/**
 * JS backend doesn't support IrExprBody yet.
 *
 * TODO deprecate once it does and KT-46316 is fixed.
 */
public fun IrBuilderWithScope.irJsExprBody(expression: IrExpression, useExprOnJvm: Boolean = false): IrBody =
    if (context is IrPluginContext && !(context as IrPluginContext).platform.isJs() && useExprOnJvm) {
        irExprBody(expression)
    } else {
        irBlockBody {
            +irReturn(expression)
        }
    }

/**
 * Get the arguments of an IrCall by their parameter names
 */
public fun IrCall.valueArgumentsByName(): Map<String, IrExpression?> {
    val byIndex = (0 until valueArgumentsCount).associateWith { getValueArgument(it) }
    val names = symbol.owner.valueParameters.map { it.name }
    return byIndex.mapKeys { names[it.key].asString() }
}