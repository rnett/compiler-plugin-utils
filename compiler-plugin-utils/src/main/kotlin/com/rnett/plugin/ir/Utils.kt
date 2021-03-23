package com.rnett.plugin.ir

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
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrAnonymousInitializer
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrTypeParametersContainer
import org.jetbrains.kotlin.ir.declarations.addMember
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.expressions.putValueArgument
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrAnonymousInitializerSymbolImpl
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeArgument
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.substitute
import org.jetbrains.kotlin.ir.util.superTypes
import org.jetbrains.kotlin.utils.addToStdlib.assertedCast
import org.jetbrains.kotlin.utils.addToStdlib.cast
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
 * Get the supertypes of a type, substituting type parameters for their values where known and not `*`.
 *
 * I.e. if you have `class A<T>: B<T>`, the supertype of `A<Int>` would be `B<Int>`, not `B<T of A>` like with [superTypes].
 */
public fun IrType.supertypesWithSubstitution(): List<IrType> {
    if (this !is IrSimpleType)
        return superTypes()

    val paramMap = this.classifier.owner.cast<IrTypeParametersContainer>().typeParameters
        .map { it.symbol }.zip(this.arguments.map { it.typeOrNull }).filter { it.second != null }
        .toMap() as Map<IrTypeParameterSymbol, IrType>

    return superTypes().map { it.substitute(paramMap) }
}

/**
 * Gets the lowest superclass or this that matches the predicate.
 * Helpful for discovering the type parameters of supertypes.
 */
public inline fun IrType.raiseTo(predicate: (IrType) -> Boolean): IrType =
    raiseToOrNull(predicate) ?: error("Type doesn't match predicate, and no matching supertypes found")

/**
 * Gets the lowest superclass or this that matches the predicate, or null if none do.
 * Helpful for discovering the type parameters of supertypes.
 *
 * Uses [supertypesWithSubstitution]
 */
public inline fun IrType.raiseToOrNull(predicate: (IrType) -> Boolean): IrType? {
    if (predicate(this))
        return this

    val queue = ArrayDeque<IrType>()
    queue += this.supertypesWithSubstitution()

    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        if (predicate(current))
            return current

        queue += current.supertypesWithSubstitution()
    }

    return null
}

/**
 * Gets the lowest superclass or this that has the given classifier.
 * Helpful for discovering the type parameters of supertypes.
 *
 * Uses [supertypesWithSubstitution]
 */
public fun IrType.raiseTo(classifier: IrClassifierSymbol): IrType = raiseToOrNull(classifier)
    ?: error("Type doesn't have classifier $classifier, and none of it's supertypes do")


/**
 * Gets the lowest superclass or this that matches the predicate, or null if none do.
 * Helpful for discovering the type parameters of supertypes.
 *
 * Uses [supertypesWithSubstitution]
 */
public fun IrType.raiseToOrNull(classifier: IrClassifierSymbol): IrType? =
    raiseToOrNull { it.classifierOrNull == classifier }

//fun IrClassifierSymbol.typeWith(vararg arguments: IrTypeArgument): IrSimpleType = typeWith(arguments.toList())

public fun IrClassifierSymbol.typeWith(arguments: List<IrTypeArgument>): IrSimpleType =
    IrSimpleTypeImpl(
        this,
        false,
        arguments,
        emptyList()
    )

public fun IrClass.typeWith(arguments: List<IrTypeArgument>): IrSimpleType = this.symbol.typeWith(arguments)

//fun IrClass.typeWith(vararg arguments: IrTypeArgument) = this.symbol.typeWith(arguments.toList())

@OptIn(ObsoleteDescriptorBasedAPI::class)
public fun IrMemberAccessExpression<*>.putValueArguments(vararg namedArgs: Pair<String, IrExpression?>) {
    val paramDescriptors =
        this.symbol.descriptor.cast<CallableDescriptor>().valueParameters.associateBy { it.name.asString() }
    namedArgs.forEach {
        putValueArgument(paramDescriptors[it.first] ?: error("No such parameter ${it.first}"), it.second)
    }
}

public fun <T : IrMemberAccessExpression<*>> T.withValueArguments(vararg namedArgs: Pair<String, IrExpression?>): T =
    apply { putValueArguments(*namedArgs) }

public fun IrMemberAccessExpression<*>.putValueArguments(vararg args: IrExpression?) {
    args.forEachIndexed { i, it ->
        putValueArgument(i, it)
    }
}

public fun <T : IrMemberAccessExpression<*>> T.withValueArguments(vararg args: IrExpression?): T =
    apply { putValueArguments(*args) }

public fun <T : IrMemberAccessExpression<*>> T.withExtensionReceiver(receiver: IrExpression?): T =
    apply { extensionReceiver = receiver }

public fun <T : IrMemberAccessExpression<*>> T.withDispatchReceiver(receiver: IrExpression?): T =
    apply { dispatchReceiver = receiver }

@OptIn(ObsoleteDescriptorBasedAPI::class)
public fun IrMemberAccessExpression<*>.putTypeArguments(vararg namedArgs: Pair<String, IrType?>) {
    val paramDescriptors =
        this.symbol.descriptor.cast<CallableDescriptor>().typeParameters.associateBy { it.name.asString() }
    namedArgs.forEach {
        putTypeArgument(paramDescriptors[it.first]?.index ?: error("No such type parameter ${it.first}"), it.second)
    }
}

public fun <T : IrMemberAccessExpression<*>> T.withTypeArguments(vararg namedArgs: Pair<String, IrType?>): T =
    apply { putTypeArguments(*namedArgs) }

public fun IrMemberAccessExpression<*>.putTypeArguments(vararg args: IrType?) {
    args.forEachIndexed { i, it ->
        putTypeArgument(i, it)
    }
}

public fun <T : IrMemberAccessExpression<*>> T.withTypeArguments(vararg args: IrType?): T =
    apply { putTypeArguments(*args) }

public fun IrType.typeArgument(index: Int): IrType =
    assertedCast<IrSimpleType> { "$this is not a simple type" }.arguments[0].typeOrNull
        ?: error("Type argument $index of $this is not a type (is it a wildcard?)")

/**
 * JS backend doesn't support IrExprBody yet.
 *
 * TODO deprecate once it does.
 */
public inline fun IrBuilderWithScope.irJsExprBody(expression: IrExpression): IrBlockBody = irBlockBody {
    +irReturn(expression)
}