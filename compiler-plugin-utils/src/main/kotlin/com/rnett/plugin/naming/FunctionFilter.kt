package com.rnett.plugin.naming

import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.util.isVararg

/**
 * A filter for resolving IR functions
 */
@Deprecated("Will be superseded by reference generator")
public interface IFunctionFilter {
    public var numParameters: Int?
    public var numTypeParameters: Int?
    public var parameters: MutableMap<Int, (IrValueParameter) -> Boolean>
    public var hasExtensionReceiver: Boolean?
    public var extensionReceiver: ((IrValueParameter) -> Boolean)?
    public var hasDispatchReceiver: Boolean?
    public var hasVararg: Boolean?
    public var isExpect: Boolean?
    public fun matches(function: IrFunction): Boolean

    public fun filter(filter: (IrFunction) -> Boolean)

    public operator fun Int.invoke(filter: (IrValueParameter) -> Boolean) {
        parameters[this] = filter
    }
}

/**
 * Implementation of [IFunctionFilter]
 */
@Deprecated("Will be superseded by reference generator")
public open class FunctionFilter internal constructor() : IFunctionFilter {

    override var numParameters: Int? = null
    override var numTypeParameters: Int? = null
    override var parameters: MutableMap<Int, (IrValueParameter) -> Boolean> = mutableMapOf()

    override var hasExtensionReceiver: Boolean? = null
    override var extensionReceiver: ((IrValueParameter) -> Boolean)? = null

    override var hasDispatchReceiver: Boolean? = null

    override var hasVararg: Boolean? = null
    override var isExpect: Boolean? = null

    internal var filter: (IrFunction) -> Boolean = { true }

    override fun matches(function: IrFunction): Boolean {
        val params = function.valueParameters

        numParameters?.let {
            if (params.size != it)
                return false
        }

        numTypeParameters?.let {
            if (function.typeParameters.size != it)
                return false
        }

        parameters.forEach { (i, it) ->
            if (i > params.lastIndex)
                return false

            if (!it(params[i]))
                return false
        }

        hasExtensionReceiver?.let {
            if (it != (function.extensionReceiverParameter != null))
                return false
        }

        hasDispatchReceiver?.let {
            if (it != (function.dispatchReceiverParameter != null))
                return false
        }

        extensionReceiver?.let {
            if (function.extensionReceiverParameter == null)
                return false

            if (!it(function.extensionReceiverParameter!!))
                return false
        }

        hasVararg?.let {
            if (it != function.valueParameters.any { it.isVararg })
                return false
        }

        isExpect?.let {
            if (it != function.isExpect)
                return false
        }

        return filter(function)
    }

    override fun filter(filter: (IrFunction) -> Boolean) {
        val old = this.filter
        this.filter = { old(it) && filter(it) }
    }
}

/**
 * Add a lambda filter
 */
public fun <T : IFunctionFilter> T.withFilter(filter: (IrFunction) -> Boolean): T = apply {
    filter(filter)
}

/**
 * Only select functions with a matching (lazily resolved) extension receiver
 */
public fun <T : IFunctionFilter> T.withExtensionReceiverType(classRef: () -> ClassRef): T = apply {
    val ref = lazy(classRef)
    extensionReceiver = { it.type.isClassifierOf(ref.value) }
}

public inline operator fun <T : IFunctionFilter> T.invoke(builder: IFunctionFilter.() -> Unit): T = apply(builder)