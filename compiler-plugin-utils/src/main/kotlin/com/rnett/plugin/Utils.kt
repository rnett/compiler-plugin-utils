package com.rnett.plugin

import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.IrGeneratorContext
import org.jetbrains.kotlin.ir.builders.IrGeneratorWithScope
import org.jetbrains.kotlin.ir.builders.IrSingleStatementBuilder
import org.jetbrains.kotlin.ir.declarations.IrAnonymousInitializer
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.addMember
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.expressions.putValueArgument
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrAnonymousInitializerSymbolImpl
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeArgument
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.superTypes
import org.jetbrains.kotlin.utils.addToStdlib.cast

val CompilerConfiguration.messageCollector get() = get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)

fun IrBuilderWithScope.irVararg(elementType: IrType, elements: Iterable<IrExpression>) =
    IrVarargImpl(
        startOffset,
        endOffset,
        context.irBuiltIns.arrayClass.typeWith(elementType),
        elementType,
        elements.toList()
    )

fun IrGeneratorContext.createIrBuilder(symbol: IrSymbol, startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET) =
    DeclarationIrBuilder(this, symbol, startOffset, endOffset)

inline fun <T : IrElement> IrGeneratorWithScope.buildStatement(
    startOffset: Int = UNDEFINED_OFFSET,
    endOffset: Int = UNDEFINED_OFFSET,
    origin: IrStatementOrigin? = null,
    builder: IrSingleStatementBuilder.() -> T
) = IrSingleStatementBuilder(context, scope, startOffset, endOffset, origin).builder()

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun IrClass.addAnonymousInitializer(builder: IrAnonymousInitializer.() -> Unit): IrAnonymousInitializer {
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

fun IrType.raiseTo(predicate: (IrType) -> Boolean): IrType =
    raiseToOrNull(predicate) ?: error("Type doesn't match predicate, and no matching supertypes found")

fun IrType.raiseToOrNull(predicate: (IrType) -> Boolean): IrType? {
    if (predicate(this))
        return this
    return this.superTypes().firstOrNull(predicate)
}

//fun IrClassifierSymbol.typeWith(vararg arguments: IrTypeArgument): IrSimpleType = typeWith(arguments.toList())

fun IrClassifierSymbol.typeWith(arguments: List<IrTypeArgument>): IrSimpleType =
    IrSimpleTypeImpl(
        this,
        false,
        arguments,
        emptyList()
    )

fun IrClass.typeWith(arguments: List<IrTypeArgument>) = this.symbol.typeWith(arguments)

//fun IrClass.typeWith(vararg arguments: IrTypeArgument) = this.symbol.typeWith(arguments.toList())

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun IrMemberAccessExpression<*>.putValueArguments(vararg namedArgs: Pair<String, IrExpression?>) {
    val paramDescriptors = this.symbol.descriptor.cast<CallableDescriptor>().valueParameters.associateBy { it.name.asString() }
    namedArgs.forEach {
        putValueArgument(paramDescriptors[it.first] ?: error("No such parameter ${it.first}"), it.second)
    }
}

fun <T : IrMemberAccessExpression<*>> T.withValueArguments(vararg namedArgs: Pair<String, IrExpression?>) = apply { putValueArguments(*namedArgs) }

fun IrMemberAccessExpression<*>.putValueArguments(vararg args: IrExpression?) {
    args.forEachIndexed { i, it ->
        putValueArgument(i, it)
    }
}

fun <T : IrMemberAccessExpression<*>> T.withValueArguments(vararg args: IrExpression?) = apply { putValueArguments(*args) }

fun <T : IrMemberAccessExpression<*>> T.withExtensionReceiver(receiver: IrExpression?) = apply { extensionReceiver = receiver }

fun <T : IrMemberAccessExpression<*>> T.withDispatchReceiver(receiver: IrExpression?) = apply { dispatchReceiver = receiver }

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun IrMemberAccessExpression<*>.putTypeArguments(vararg namedArgs: Pair<String, IrType?>) {
    val paramDescriptors = this.symbol.descriptor.cast<CallableDescriptor>().typeParameters.associateBy { it.name.asString() }
    namedArgs.forEach {
        putTypeArgument(paramDescriptors[it.first]?.index ?: error("No such type parameter ${it.first}"), it.second)
    }
}

fun <T : IrMemberAccessExpression<*>> T.withTypeArguments(vararg namedArgs: Pair<String, IrType?>) = apply { putTypeArguments(*namedArgs) }

fun IrMemberAccessExpression<*>.putTypeArguments(vararg args: IrType?) {
    args.forEachIndexed { i, it ->
        putTypeArgument(i, it)
    }
}

fun <T : IrMemberAccessExpression<*>> T.withTypeArguments(vararg args: IrType?) = apply { putTypeArguments(*args) }