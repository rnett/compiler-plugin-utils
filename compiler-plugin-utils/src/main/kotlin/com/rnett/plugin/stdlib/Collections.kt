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
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isSubtypeOf
import org.jetbrains.kotlin.ir.types.typeWith

class CollectionsBuilders(
    internal val stdlib: StdlibBuilders,
    builder: IrBuilderWithScope,
    context: IrPluginContext
) : MethodBuilder(builder, context) {
    val Iterable: IterableBuilders by lazy { IterableBuilders(builder, context) }
    val Collection: CollectionBuilders by lazy { CollectionBuilders(builder, context) }

    val Map: MapBuilders by lazy { MapBuilders(this, builder, context) }
    val MutableMap: MutableMapBuilders by lazy { MutableMapBuilders(this, builder, context) }

    val List: ListBuilders by lazy { ListBuilders(this, builder, context) }
    val MutableList: MutableListBuilders by lazy { MutableListBuilders(this, builder, context) }

    val Set: SetBuilders by lazy { SetBuilders(this, builder, context) }
    val MutableSet: MutableSetBuilders by lazy { MutableSetBuilders(this, builder, context) }

    fun listOf(
        elementType: IrType,
        items: Iterable<IrExpression>,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.listOfVararg(), Kotlin.Collections.List.resolveTypeWith(elementType))
            .withTypeArguments(elementType)
            .withValueArguments(irVararg(elementType, items))
    }

    fun listOfVararg(
        elementType: IrType,
        items: IrVararg,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        require(items.varargElementType.isSubtypeOf(elementType, context.irBuiltIns)) {
            "Vararg type ${items.varargElementType} is not a subtype of the list type $elementType"
        }
        irCall(Kotlin.Collections.listOfVararg(), Kotlin.Collections.List.resolveTypeWith(elementType))
            .withTypeArguments(elementType)
            .withValueArguments(items)
    }

    fun listOfVararg(
        items: IrVararg,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = listOfVararg(items.varargElementType, items, startOffset, endOffset)

    fun setOf(
        elementType: IrType,
        items: Iterable<IrExpression>,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.setOfVararg(), Kotlin.Collections.Set.resolveTypeWith(elementType))
            .withTypeArguments(elementType)
            .withValueArguments(irVararg(elementType, items))
    }

    fun listOfNotNull(
        elementType: IrType,
        items: Iterable<IrExpression>,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.listOfNotNullVararg(), Kotlin.Collections.List.resolveTypeWith(elementType))
            .withTypeArguments(elementType)
            .withValueArguments(irVararg(elementType, items))
    }

    fun setOfNotNull(
        elementType: IrType,
        items: Iterable<IrExpression>,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.setOfNotNullVararg(), Kotlin.Collections.Set.resolveTypeWith(elementType))
            .withTypeArguments(elementType)
            .withValueArguments(irVararg(elementType, items))
    }

    fun mapOf(
        keyType: IrType,
        valueType: IrType,
        items: Map<IrExpression, IrExpression>,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.mapOfVararg(), Kotlin.Collections.Map.resolveTypeWith(keyType, valueType))
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
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.mutableListOfVararg(), Kotlin.Collections.MutableList().typeWith(elementType))
            .withTypeArguments(elementType)
            .withValueArguments(irVararg(elementType, items))
    }

    fun mutableSetOf(
        elementType: IrType,
        items: Iterable<IrExpression>,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
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

    fun emptyList(
        elementType: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall =
        buildStatement(startOffset, endOffset) {
            irCall(Kotlin.Collections.listOfEmpty(), Kotlin.Collections.List.resolveTypeWith(elementType)).withTypeArguments(elementType)
        }

    fun emptySet(
        elementType: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall =
        buildStatement(startOffset, endOffset) {
            irCall(Kotlin.Collections.setOfEmpty(), Kotlin.Collections.Set.resolveTypeWith(elementType)).withTypeArguments(elementType)
        }

    fun emptyMap(
        keyType: IrType,
        valueType: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall =
        buildStatement(startOffset, endOffset) {
            irCall(Kotlin.Collections.mapOfEmpty(), Kotlin.Collections.Map.resolveTypeWith(keyType, valueType)).withTypeArguments(keyType, valueType)
        }

    fun emptyMutableList(
        elementType: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall =
        buildStatement(startOffset, endOffset) {
            irCall(
                Kotlin.Collections.mutableListOfEmpty(),
                Kotlin.Collections.MutableList.resolveTypeWith(elementType)
            ).withTypeArguments(elementType)
        }

    fun emptyMutableSet(
        elementType: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall =
        buildStatement(startOffset, endOffset) {
            irCall(Kotlin.Collections.mutableSetOfEmpty(), Kotlin.Collections.MutableSet.resolveTypeWith(elementType)).withTypeArguments(elementType)
        }

    fun emptyMutableMap(
        keyType: IrType,
        valueType: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall =
        buildStatement(startOffset, endOffset) {
            irCall(Kotlin.Collections.mutableMapOfEmpty(), Kotlin.Collections.MutableMap.resolveTypeWith(keyType, valueType)).withTypeArguments(
                keyType,
                valueType
            )
        }
}

class ArrayBuilders(builder: IrBuilderWithScope, context: IrPluginContext) :
    TypedMethodBuilder(Kotlin.Array, builder, context) {
    fun toList(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.arrayToList()).withExtensionReceiver(receiver.checkType())
    }

    fun toMutableList(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.arrayToMutableList()).withExtensionReceiver(receiver.checkType())
    }
}

open class IterableBuilders(
    builder: IrBuilderWithScope,
    context: IrPluginContext,
    type: ClassRef = Kotlin.Collections.Iterable
) :
    TypedMethodBuilder(type, builder, context) {
    fun toList(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.iterableToList())
            .withExtensionReceiver(receiver.checkType())
    }

    fun toMutableList(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.iterableToMutableList())
            .withExtensionReceiver(receiver.checkType())
    }

    fun toMap(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.iterableToMap())
            .withExtensionReceiver(receiver.checkType())
    }

    fun plusIterableToList(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.iterablePlusIterableToList)
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(other)
    }

    fun plusElementToList(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.iterablePlusElementToList)
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(other)
    }

    fun minusIterableToList(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.iterableMinusIterableToList)
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(other)
    }

    fun minusElementToList(
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

open class CollectionBuilders(
    builder: IrBuilderWithScope,
    context: IrPluginContext,
    type: ClassRef = Kotlin.Collections.Collection
) :
    IterableBuilders(builder, context, type) {
    fun toTypedArray(
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