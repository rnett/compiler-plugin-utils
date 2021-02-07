package com.rnett.plugin.stdlib

import com.rnett.plugin.naming.ClassRef
import com.rnett.plugin.withDispatchReceiver
import com.rnett.plugin.withExtensionReceiver
import com.rnett.plugin.withValueArguments
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrType


open class SetBuilders(
    val collections: CollectionsBuilders,
    builder: IrBuilderWithScope,
    context: IrPluginContext,
    type: ClassRef = Kotlin.Collections.Set
) :
    CollectionBuilders(builder, context, type) {
    fun size(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.Set.size().owner.getter!!)
            .withDispatchReceiver(receiver.checkType())
    }

    fun isEmpty(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.Set.isEmpty())
            .withDispatchReceiver(receiver.checkType())
    }

    fun contains(
        receiver: IrExpression, item: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.Set.contains())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(item)
    }

    fun iterator(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.Set.iterator())
            .withDispatchReceiver(receiver.checkType())
    }

    fun containsAll(
        receiver: IrExpression, items: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.Set.containsAll())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(items)
    }

    fun containsAll(
        receiver: IrExpression,
        elementType: IrType,
        items: Iterable<IrExpression>,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = containsAll(receiver.checkType(), collections.setOf(elementType, items), startOffset, endOffset)

    fun plusIterable(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.setPlusIterableToSet)
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(other)
    }

    fun plusElement(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.setPlusElementToSet)
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(other)
    }

    fun minusIterable(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.setMinusIterableToSet)
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(other)
    }

    fun minusElement(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.setMinusElementToSet)
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(other)
    }
}

open class MutableSetBuilders(collections: CollectionsBuilders, builder: IrBuilderWithScope, context: IrPluginContext) :
    SetBuilders(collections, builder, context, Kotlin.Collections.MutableSet) {
    fun add(
        receiver: IrExpression,
        item: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableSet.add())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(item)
    }

    fun remove(
        receiver: IrExpression,
        item: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableSet.remove())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(item)
    }

    fun addAll(
        receiver: IrExpression,
        items: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableSet.addAll())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(items)
    }

    fun addAll(
        receiver: IrExpression,
        items: Iterable<IrExpression>,
        itemType: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = addAll(receiver.checkType(), collections.setOf(itemType, items), startOffset, endOffset)

    fun removeAll(
        receiver: IrExpression,
        items: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableSet.removeAll())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(items)
    }

    fun removeAll(
        receiver: IrExpression,
        items: Iterable<IrExpression>,
        itemType: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = removeAll(receiver.checkType(), collections.setOf(itemType, items), startOffset, endOffset)

    fun clear(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableSet.clear())
            .withDispatchReceiver(receiver.checkType())
    }
}