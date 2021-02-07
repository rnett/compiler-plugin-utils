package com.rnett.plugin.stdlib

import com.rnett.plugin.irVararg
import com.rnett.plugin.naming.ClassRef
import com.rnett.plugin.withExtensionReceiver
import com.rnett.plugin.withTypeArguments
import com.rnett.plugin.withValueArguments
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith

class CollectionsBuilders(val stdlib: StdlibBuilders, builder: IrBuilderWithScope, context: IrPluginContext) : MethodBuilder(builder, context) {
    val Iterable by lazy { IterableBuilders(builder, context) }
    val Collection by lazy { CollectionBuilders(builder, context) }

    val Map by lazy { MapBuilders(this, builder, context) }
    val MutableMap by lazy { MutableMapBuilders(this, builder, context) }

    val List by lazy { ListBuilders(this, builder, context) }
    val MutableList by lazy { MutableListBuilders(this, builder, context) }

    val Set by lazy { SetBuilders(this, builder, context) }
    val MutableSet by lazy { MutableSetBuilders(this, builder, context) }

    fun listOf(
        elementType: IrType,
        items: Iterable<IrExpression>,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.listOfVararg())
            .withValueArguments(irVararg(elementType, items))
    }

    fun setOf(
        elementType: IrType,
        items: Iterable<IrExpression>,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.setOfVararg())
            .withTypeArguments(elementType)
            .withValueArguments(irVararg(elementType, items))
    }

    fun mapOf(
        keyType: IrType,
        valueType: IrType,
        items: Map<IrExpression, IrExpression>,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.mapOfVararg())
            .withTypeArguments(keyType, valueType)
            .withValueArguments(
                irVararg(
                    Kotlin.Pair().typeWith(keyType, valueType),
                    items.map { (k, v) -> stdlib.to(k, keyType, v, valueType) }
                ))
    }

    fun mutableListOf(
        elementType: IrType,
        items: Iterable<IrExpression>,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.mutableListOfVararg(), Kotlin.Collections.MutableList().typeWith(elementType))
            .withTypeArguments(elementType)
            .withValueArguments(irVararg(elementType, items))
    }

    fun mutableSetOf(
        elementType: IrType,
        items: Iterable<IrExpression>,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.mutableSetOfVararg(), Kotlin.Collections.MutableSet().typeWith(elementType))
            .withTypeArguments(elementType)
            .withValueArguments(irVararg(elementType, items))
    }

    fun mutableMapOf(
        keyType: IrType,
        valueType: IrType,
        items: Map<IrExpression, IrExpression>,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.mutableMapOfVararg(), Kotlin.Collections.MutableMap().typeWith(keyType, valueType))
            .withTypeArguments(keyType, valueType)
            .withValueArguments(
                irVararg(
                    Kotlin.Pair().typeWith(keyType, valueType),
                    items.map { (k, v) ->
                        irCall(Kotlin.to(), Kotlin.Pair().typeWith(keyType, valueType))
                            .withTypeArguments(keyType, valueType)
                            .withExtensionReceiver(k)
                            .withValueArguments(v)
                    }.toList()
                )
            )
    }

    fun emptyList(elementType: IrType, startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET) =
        buildStatement(startOffset, endOffset) {
            irCall(Kotlin.Collections.listOfEmpty()).withTypeArguments(elementType)
        }

    fun emptySet(elementType: IrType, startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET) =
        buildStatement(startOffset, endOffset) {
            irCall(Kotlin.Collections.setOfEmpty()).withTypeArguments(elementType)
        }

    fun emptyMap(keyType: IrType, valueType: IrType, startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET) =
        buildStatement(startOffset, endOffset) {
            irCall(Kotlin.Collections.mapOfEmpty()).withTypeArguments(keyType, valueType)
        }

    fun emptyMutableList(elementType: IrType, startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET) =
        buildStatement(startOffset, endOffset) {
            irCall(Kotlin.Collections.mutableListOfEmpty()).withTypeArguments(elementType)
        }

    fun emptyMutableSet(elementType: IrType, startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET) =
        buildStatement(startOffset, endOffset) {
            irCall(Kotlin.Collections.mutableSetOfEmpty()).withTypeArguments(elementType)
        }

    fun emptyMutableMap(keyType: IrType, valueType: IrType, startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET) =
        buildStatement(startOffset, endOffset) {
            irCall(Kotlin.Collections.mutableMapOfEmpty()).withTypeArguments(keyType, valueType)
        }
}

class ArrayBuilders(builder: IrBuilderWithScope, context: IrPluginContext) : TypedMethodBuilder(Kotlin.Array, builder, context) {
    fun toList(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.arrayToList()).withExtensionReceiver(receiver.checkType())
    }

    fun toMutableList(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.arrayToMutableList()).withExtensionReceiver(receiver.checkType())
    }
}

open class IterableBuilders(builder: IrBuilderWithScope, context: IrPluginContext, type: ClassRef = Kotlin.Collections.Iterable) :
    TypedMethodBuilder(type, builder, context) {
    fun toList(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.iterableToList())
            .withExtensionReceiver(receiver.checkType())
    }

    fun toMutableList(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.iterableToMutableList())
            .withExtensionReceiver(receiver.checkType())
    }

    fun toMap(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.iterableToMap())
            .withExtensionReceiver(receiver.checkType())
    }

    fun plusIterableToList(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.iterablePlusIterableToList)
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(other)
    }

    fun plusElementToList(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.iterablePlusElementToList)
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(other)
    }

    fun minusIterableToList(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.iterableMinusIterableToList)
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(other)
    }

    fun minusElementToList(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.iterableMinusElementToList)
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(other)
    }

}

open class CollectionBuilders(builder: IrBuilderWithScope, context: IrPluginContext, type: ClassRef = Kotlin.Collections.Collection) :
    IterableBuilders(builder, context, type) {
    fun toTypedArray(
        receiver: IrExpression,
        elementType: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.collectionToTypedArray(), Kotlin.Array().typeWith(elementType))
            .withExtensionReceiver(receiver.checkType())
            .withTypeArguments(elementType)
    }
}