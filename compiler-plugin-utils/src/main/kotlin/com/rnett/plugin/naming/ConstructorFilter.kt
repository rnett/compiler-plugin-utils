package com.rnett.plugin.naming

import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.util.isVararg

interface IConstructorFilter {
    var numParameters: Int?
    var parameters: MutableMap<Int, (IrValueParameter) -> Boolean>
    var hasVararg: Boolean?
    var isPrimary: Boolean?
    var isExpect: Boolean?
    fun matches(function: IrConstructor): Boolean

    fun filter(filter: (IrConstructor) -> Boolean)

    operator fun Int.invoke(filter: (IrValueParameter) -> Boolean) {
        parameters[this] = filter
    }
}

open class ConstructorFilter internal constructor() : IConstructorFilter {

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

fun <T : IConstructorFilter> T.withFilter(filter: (IrConstructor) -> Boolean) = apply {
    filter(filter)
}

inline operator fun <T : IConstructorFilter> T.invoke(builder: IConstructorFilter.() -> Unit) = apply(builder)