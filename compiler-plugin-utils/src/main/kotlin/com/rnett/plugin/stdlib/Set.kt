package com.rnett.plugin.stdlib

import com.rnett.plugin.ir.withDispatchReceiver
import com.rnett.plugin.ir.withExtensionReceiver
import com.rnett.plugin.ir.withValueArguments
import com.rnett.plugin.naming.ClassRef
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.types.IrType


public open class SetBuilders(
    protected val collections: CollectionsBuilders,
    builder: IrBuilderWithScope,
    context: IrPluginContext,
    type: ClassRef = Kotlin.Collections.Set
) :
    CollectionBuilders(builder, context, type) {
    public fun size(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrFunctionAccessExpression = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.Set.size().owner.getter!!)
            .withDispatchReceiver(receiver.checkType())
    }

    public fun isEmpty(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.Set.isEmpty())
            .withDispatchReceiver(receiver.checkType())
    }

    public fun contains(
        receiver: IrExpression, item: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.Set.contains())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(item)
    }

    public fun iterator(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.Set.iterator())
            .withDispatchReceiver(receiver.checkType())
    }

    public fun containsAll(
        receiver: IrExpression, items: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.Set.containsAll())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(items)
    }

    public fun containsAll(
        receiver: IrExpression,
        elementType: IrType,
        items: Iterable<IrExpression>,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = containsAll(receiver.checkType(), collections.setOf(elementType, items), startOffset, endOffset)

    public fun plusIterable(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.setPlusIterableToSet)
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(other)
    }

    public fun plusElement(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.setPlusElementToSet)
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(other)
    }

    public fun minusIterable(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.setMinusIterableToSet)
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(other)
    }

    public fun minusElement(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.setMinusElementToSet)
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(other)
    }
}

public open class MutableSetBuilders(
    collections: CollectionsBuilders,
    builder: IrBuilderWithScope,
    context: IrPluginContext
) :
    SetBuilders(collections, builder, context, Kotlin.Collections.MutableSet) {
    public fun add(
        receiver: IrExpression,
        item: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableSet.add())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(item)
    }

    public fun remove(
        receiver: IrExpression,
        item: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableSet.remove())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(item)
    }

    public fun addAll(
        receiver: IrExpression,
        items: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableSet.addAll())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(items)
    }

    public fun addAll(
        receiver: IrExpression,
        items: Iterable<IrExpression>,
        itemType: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = addAll(receiver.checkType(), collections.setOf(itemType, items), startOffset, endOffset)

    public fun removeAll(
        receiver: IrExpression,
        items: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableSet.removeAll())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(items)
    }

    public fun removeAll(
        receiver: IrExpression,
        items: Iterable<IrExpression>,
        itemType: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = removeAll(receiver.checkType(), collections.setOf(itemType, items), startOffset, endOffset)

    public fun clear(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableSet.clear())
            .withDispatchReceiver(receiver.checkType())
    }
}