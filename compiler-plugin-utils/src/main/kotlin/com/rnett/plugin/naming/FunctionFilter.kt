package com.rnett.plugin.naming

import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.util.isVararg

interface IFunctionFilter {
    var numParameters: Int?
    var numTypeParameters: Int?
    var parameters: MutableMap<Int, (IrValueParameter) -> Boolean>
    var hasExtensionReceiver: Boolean?
    var extensionReceiver: ((IrValueParameter) -> Boolean)?
    var hasDispatchReceiver: Boolean?
    var hasVararg: Boolean?
    var isExpect: Boolean?
    fun matches(function: IrFunction): Boolean

    fun filter(filter: (IrFunction) -> Boolean)

    operator fun Int.invoke(filter: (IrValueParameter) -> Boolean) {
        parameters[this] = filter
    }
}

open class FunctionFilter internal constructor() : IFunctionFilter {

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

fun <T : IFunctionFilter> T.withFilter(filter: (IrFunction) -> Boolean) = apply {
    filter(filter)
}

fun <T : IFunctionFilter> T.withExtensionReceiverType(classRef: () -> ClassRef) = apply {
    val ref = lazy(classRef)
    extensionReceiver = { it.type.isClassifierOf(ref.value) }
}

inline operator fun <T : IFunctionFilter> T.invoke(builder: IFunctionFilter.() -> Unit) = apply(builder)