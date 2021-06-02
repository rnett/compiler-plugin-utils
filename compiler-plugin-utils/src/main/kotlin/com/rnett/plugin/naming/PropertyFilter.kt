package com.rnett.plugin.naming

import org.jetbrains.kotlin.ir.backend.js.utils.asString
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrValueParameter

/**
 * A filter for resolving IR properties
 */
@Deprecated("Will be superseded by reference generator")
public interface IPropertyFilter {
    public var isDelegated: Boolean?
    public var hasBackingField: Boolean?
    public var fieldType: String?
    public var hasGetter: Boolean?
    public var getterType: String?
    public var hasSetter: Boolean?
    public var type: String?
    public var isExpect: Boolean?
    public var hasExtensionReceiver: Boolean?
    public var extensionReceiver: ((IrValueParameter) -> Boolean)?
    public fun filter(filter: (IrProperty) -> Boolean)
    public fun matches(property: IrProperty): Boolean
}

/**
 * Implementation of [IPropertyFilter]
 */
@Deprecated("Will be superseded by reference generator")
public open class PropertyFilter internal constructor() : IPropertyFilter {
    private var filter: (IrProperty) -> Boolean = { true }
    override fun filter(filter: (IrProperty) -> Boolean) {
        val old = this.filter
        this.filter = { old(it) && filter(it) }
    }

    override var isDelegated: Boolean? = null

    override var hasBackingField: Boolean? = null
    override var fieldType: String? = null

    override var hasGetter: Boolean? = null
    override var getterType: String? = null

    override var hasSetter: Boolean? = null

    override var type: String? = null
    override var isExpect: Boolean? = null

    override var hasExtensionReceiver: Boolean? = null
    override var extensionReceiver: ((IrValueParameter) -> Boolean)? = null

    override fun matches(property: IrProperty): Boolean {

        isDelegated?.let {
            if (it != property.isDelegated)
                return false
        }

        hasBackingField?.let {
            if (it != (property.backingField != null))
                return false
        }

        fieldType?.let {
            if (property.backingField == null)
                return false

            if (it != property.backingField!!.type.asString())
                return false
        }

        hasGetter?.let {
            if (it != (property.getter != null))
                return false
        }

        getterType?.let {
            if (property.getter == null)
                return false

            if (it != property.getter!!.returnType.asString())
                return false
        }

        hasSetter?.let {
            if (it != (property.setter != null))
                return false
        }

        hasExtensionReceiver?.let {
            if (property.getter == null)
                return false

            if (it != (property.getter!!.extensionReceiverParameter != null))
                return false
        }

        extensionReceiver?.let {
            if (property.getter == null)
                return false

            if (property.getter!!.extensionReceiverParameter == null)
                return false

            if (!it(property.getter!!.extensionReceiverParameter!!))
                return false
        }

        type?.let {
            if (property.getter != null) {
                if (it != property.getter!!.returnType.asString())
                    return false
            } else if (property.backingField != null) {
                if (it != property.backingField!!.type.asString())
                    return false
            } else
                return false
        }

        isExpect?.let {
            if (it != property.isExpect)
                return false
        }

        return filter(property)
    }

}

public fun <T : IPropertyFilter> T.withFilter(filter: (IrProperty) -> Boolean): T = apply {
    filter(filter)
}

public inline operator fun <T : IPropertyFilter> T.invoke(builder: IPropertyFilter.() -> Unit): T = apply(builder)