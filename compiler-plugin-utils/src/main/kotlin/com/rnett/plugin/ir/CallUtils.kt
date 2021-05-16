package com.rnett.plugin.ir

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.putValueArgument
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.substitute
import org.jetbrains.kotlin.ir.util.typeSubstitutionMap
import org.jetbrains.kotlin.utils.addToStdlib.cast

@OptIn(ObsoleteDescriptorBasedAPI::class)
public fun IrMemberAccessExpression<*>.putValueArguments(
    vararg namedArgs: Pair<String, IrExpression?>,
    substitute: Boolean = this is IrCall
) {
    val paramDescriptors =
        this.symbol.descriptor.cast<CallableDescriptor>().valueParameters.associateBy { it.name.asString() }
    namedArgs.forEach {
        putValueArgument(paramDescriptors[it.first] ?: error("No such parameter ${it.first}"), it.second)
    }

    if (substitute && this is IrCall)
        substituteTypeParams()
}

public fun <T : IrMemberAccessExpression<*>> T.withValueArguments(
    vararg namedArgs: Pair<String, IrExpression?>,
    substitute: Boolean = this is IrCall,
): T =
    apply { putValueArguments(*namedArgs, substitute = substitute) }

public fun IrMemberAccessExpression<*>.putValueArguments(
    vararg args: IrExpression?,
    substitute: Boolean = this is IrCall
) {
    args.forEachIndexed { i, it ->
        putValueArgument(i, it)
    }
    if (substitute && this is IrCall)
        substituteTypeParams()
}

public fun <T : IrMemberAccessExpression<*>> T.withValueArguments(
    vararg args: IrExpression?,
    substitute: Boolean = this is IrCall
): T =
    apply { putValueArguments(*args, substitute = substitute) }

public fun <T : IrMemberAccessExpression<*>> T.withExtensionReceiver(
    receiver: IrExpression?,
    substitute: Boolean = this is IrCall
): T =
    apply {
        extensionReceiver = receiver
        if (substitute && this is IrCall)
            substituteTypeParams()
    }

public fun <T : IrMemberAccessExpression<*>> T.withDispatchReceiver(
    receiver: IrExpression?,
    substitute: Boolean = this is IrCall
): T =
    apply {
        dispatchReceiver = receiver
        if (substitute && this is IrCall)
            substituteTypeParams()
    }

/**
 * Set the type arguments of the call.  If [substitute] is `true` and [this] is an [IrCall], calls [substituteTypeParams].
 */
@OptIn(ObsoleteDescriptorBasedAPI::class)
public fun IrMemberAccessExpression<*>.putTypeArguments(
    vararg namedArgs: Pair<String, IrType?>,
    substitute: Boolean = this is IrCall
) {
    val paramDescriptors =
        this.symbol.descriptor.cast<CallableDescriptor>().typeParameters.associateBy { it.name.asString() }
    namedArgs.forEach {
        putTypeArgument(paramDescriptors[it.first]?.index ?: error("No such type parameter ${it.first}"), it.second)
    }
    if (substitute && this is IrCall) {
        substituteTypeParams()
    }
}

/**
 * Set the type arguments of the call.  If [substitute] is `true` and [this] is an [IrCall], calls [substituteTypeParams].
 */
public fun <T : IrMemberAccessExpression<*>> T.withTypeArguments(
    vararg namedArgs: Pair<String, IrType?>,
    substitute: Boolean = this is IrCall
): T =
    apply { putTypeArguments(*namedArgs, substitute = substitute) }

/**
 * Set the type arguments of the call.  If [substitute] is `true` and [this] is an [IrCall], calls [substituteTypeParams].
 */
public fun IrMemberAccessExpression<*>.putTypeArguments(vararg args: IrType?, substitute: Boolean = this is IrCall) {
    args.forEachIndexed { i, it ->
        putTypeArgument(i, it)
    }
    if (substitute && this is IrCall) {
        substituteTypeParams()
    }
}

/**
 * Set the type arguments of the call.  If [substitute] is `true` and [this] is an [IrCall], calls [substituteTypeParams].
 */
public fun <T : IrMemberAccessExpression<*>> T.withTypeArguments(
    vararg args: IrType?,
    substitute: Boolean = this is IrCall
): T =
    apply { putTypeArguments(*args, substitute = substitute) }

/**
 * Substitute the set type parameters into the return type.
 */
public fun IrCall.substituteTypeParams(): IrCall = cast<IrCallImpl>().also {
    //TODO detect type params from args
    if ((0 until typeArgumentsCount).none { getTypeArgument(it) == null })
        it.type = it.type.substitute(it.typeSubstitutionMap)
}