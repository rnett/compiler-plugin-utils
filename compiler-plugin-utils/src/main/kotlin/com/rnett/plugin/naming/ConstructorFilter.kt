package com.rnett.plugin.naming

import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.util.isVararg

/**
 * A filter for resolving IR constructors.
 */
@Deprecated("Will be superseded by reference generator")
public interface IConstructorFilter {
    public var numParameters: Int?
    public var parameters: MutableMap<Int, (IrValueParameter) -> Boolean>
    public var hasVararg: Boolean?
    public var isPrimary: Boolean?
    public var isExpect: Boolean?
    public fun matches(function: IrConstructor): Boolean

    public fun filter(filter: (IrConstructor) -> Boolean)

    public operator fun Int.invoke(filter: (IrValueParameter) -> Boolean) {
        parameters[this] = filter
    }
}

/**
 * Implementation of [IConstructorFilter]
 */
@Deprecated("Will be superseded by reference generator")
public open class ConstructorFilter internal constructor() : IConstructorFilter {

    override var numParameters: Int? = null
    override var parameters: MutableMap<Int, (IrValueParameter) -> Boolean> = mutableMapOf()

    override var hasVararg: Boolean? = null

    override var isPrimary: Boolean? = null
    override var isExpect: Boolean? = null

    internal var filter: (IrConstructor) -> Boolean = { true }

    override fun matches(function: IrConstructor): Boolean {
        val params = function.valueParameters

        numParameters?.let {
            if (params.size != it)
                return false
        }

        parameters.forEach { (i, it) ->
            if (i > params.lastIndex)
                return false

            if (!it(params[i]))
                return false
        }

        hasVararg?.let {
            if (it != function.valueParameters.any { it.isVararg })
                return false
        }

        isPrimary?.let {
            if (it != function.isPrimary)
                return false
        }

        isExpect?.let {
            if (it != function.isExpect)
                return false
        }

        return filter(function)
    }

    override fun filter(filter: (IrConstructor) -> Boolean) {
        val old = this.filter
        this.filter = { old(it) && filter(it) }
    }
}

public fun <T : IConstructorFilter> T.withFilter(filter: (IrConstructor) -> Boolean): T = apply {
    filter(filter)
}

public inline operator fun <T : IConstructorFilter> T.invoke(builder: IConstructorFilter.() -> Unit): T = apply(builder)