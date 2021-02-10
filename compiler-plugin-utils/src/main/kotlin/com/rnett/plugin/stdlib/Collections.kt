package com.rnett.plugin.stdlib

import com.rnett.plugin.ir.irVararg
import com.rnett.plugin.ir.withExtensionReceiver
import com.rnett.plugin.ir.withTypeArguments
import com.rnett.plugin.ir.withValueArguments
import com.rnett.plugin.naming.ClassRef
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith

public class CollectionsBuilders(internal val stdlib: StdlibBuilders, builder: IrBuilderWithScope, context: IrPluginContext) : MethodBuilder(builder, context) {
    public val Iterable: IterableBuilders by lazy { IterableBuilders(builder, context) }
    public val Collection: CollectionBuilders by lazy { CollectionBuilders(builder, context) }

    public val Map: MapBuilders by lazy { MapBuilders(this, builder, context) }
    public val MutableMap: MutableMapBuilders by lazy { MutableMapBuilders(this, builder, context) }

    public val List: ListBuilders by lazy { ListBuilders(this, builder, context) }
    public val MutableList: MutableListBuilders by lazy { MutableListBuilders(this, builder, context) }

    public val Set: SetBuilders by lazy { SetBuilders(this, builder, context) }
    public val MutableSet: MutableSetBuilders by lazy { MutableSetBuilders(this, builder, context) }

    public fun listOf(
            elementType: IrType,
            items: Iterable<IrExpression>,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.listOfVararg())
                .withValueArguments(irVararg(elementType, items))
    }

    public fun setOf(
            elementType: IrType,
            items: Iterable<IrExpression>,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.setOfVararg())
                .withTypeArguments(elementType)
                .withValueArguments(irVararg(elementType, items))
    }

    public fun mapOf(
            keyType: IrType,
            valueType: IrType,
            items: Map<IrExpression, IrExpression>,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.mapOfVararg())
                .withTypeArguments(keyType, valueType)
                .withValueArguments(
                        irVararg(
                                Kotlin.Pair().typeWith(keyType, valueType),
                                items.map { (k, v) -> stdlib.to(k, keyType, v, valueType) }
                        ))
    }

    public fun mutableListOf(
            elementType: IrType,
            items: Iterable<IrExpression>,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.mutableListOfVararg(), Kotlin.Collections.MutableList().typeWith(elementType))
                .withTypeArguments(elementType)
                .withValueArguments(irVararg(elementType, items))
    }

    public fun mutableSetOf(
            elementType: IrType,
            items: Iterable<IrExpression>,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.mutableSetOfVararg(), Kotlin.Collections.MutableSet().typeWith(elementType))
                .withTypeArguments(elementType)
                .withValueArguments(irVararg(elementType, items))
    }

    public fun mutableMapOf(
            keyType: IrType,
            valueType: IrType,
            items: Map<IrExpression, IrExpression>,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
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

    public fun emptyList(elementType: IrType, startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET): IrCall =
            buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.listOfEmpty()).withTypeArguments(elementType)
            }

    public fun emptySet(elementType: IrType, startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET): IrCall =
            buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.setOfEmpty()).withTypeArguments(elementType)
            }

    public fun emptyMap(keyType: IrType, valueType: IrType, startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET): IrCall =
            buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.mapOfEmpty()).withTypeArguments(keyType, valueType)
            }

    public fun emptyMutableList(elementType: IrType, startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET): IrCall =
            buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.mutableListOfEmpty()).withTypeArguments(elementType)
            }

    public fun emptyMutableSet(elementType: IrType, startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET): IrCall =
            buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.mutableSetOfEmpty()).withTypeArguments(elementType)
            }

    public fun emptyMutableMap(keyType: IrType, valueType: IrType, startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET): IrCall =
            buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.mutableMapOfEmpty()).withTypeArguments(keyType, valueType)
            }
}

public class ArrayBuilders(builder: IrBuilderWithScope, context: IrPluginContext) : TypedMethodBuilder(Kotlin.Array, builder, context) {
    public fun toList(
            receiver: IrExpression,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.arrayToList()).withExtensionReceiver(receiver.checkType())
    }

    public fun toMutableList(
            receiver: IrExpression,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.arrayToMutableList()).withExtensionReceiver(receiver.checkType())
    }
}

public open class IterableBuilders(builder: IrBuilderWithScope, context: IrPluginContext, type: ClassRef = Kotlin.Collections.Iterable) :
        TypedMethodBuilder(type, builder, context) {
    public fun toList(
            receiver: IrExpression,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.iterableToList())
                .withExtensionReceiver(receiver.checkType())
    }

    public fun toMutableList(
            receiver: IrExpression,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.iterableToMutableList())
                .withExtensionReceiver(receiver.checkType())
    }

    public fun toMap(
            receiver: IrExpression,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.iterableToMap())
                .withExtensionReceiver(receiver.checkType())
    }

    public fun plusIterableToList(
            receiver: IrExpression,
            other: IrExpression,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.iterablePlusIterableToList)
                .withExtensionReceiver(receiver.checkType())
                .withValueArguments(other)
    }

    public fun plusElementToList(
            receiver: IrExpression,
            other: IrExpression,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.iterablePlusElementToList)
                .withExtensionReceiver(receiver.checkType())
                .withValueArguments(other)
    }

    public fun minusIterableToList(
            receiver: IrExpression,
            other: IrExpression,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.iterableMinusIterableToList)
                .withExtensionReceiver(receiver.checkType())
                .withValueArguments(other)
    }

    public fun minusElementToList(
            receiver: IrExpression,
            other: IrExpression,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.iterableMinusElementToList)
                .withExtensionReceiver(receiver.checkType())
                .withValueArguments(other)
    }

}

public open class CollectionBuilders(builder: IrBuilderWithScope, context: IrPluginContext, type: ClassRef = Kotlin.Collections.Collection) :
        IterableBuilders(builder, context, type) {
    public fun toTypedArray(
            receiver: IrExpression,
            elementType: IrType,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.collectionToTypedArray(), Kotlin.Array().typeWith(elementType))
                .withExtensionReceiver(receiver.checkType())
                .withTypeArguments(elementType)
    }
}