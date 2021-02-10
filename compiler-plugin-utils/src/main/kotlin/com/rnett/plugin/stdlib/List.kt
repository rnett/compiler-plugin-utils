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


public open class ListBuilders(
    protected val collections: CollectionsBuilders,
    builder: IrBuilderWithScope,
    context: IrPluginContext,
    type: ClassRef = Kotlin.Collections.List
) :
    CollectionBuilders(builder, context, type) {
    public fun size(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrFunctionAccessExpression = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.List.size().owner.getter!!)
            .withDispatchReceiver(receiver.checkType())
    }

    public fun isEmpty(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.List.isEmpty())
            .withDispatchReceiver(receiver.checkType())
    }

    public fun contains(
        receiver: IrExpression, item: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.List.contains())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(item)
    }

    public fun iterator(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.List.iterator())
            .withDispatchReceiver(receiver.checkType())
    }

    public fun get(
        receiver: IrExpression, index: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.List.get())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(index)
    }

    public fun indexOf(
        receiver: IrExpression, item: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.List.indexOf())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(item)
    }

    public fun plusIterable(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = super.plusIterableToList(receiver, other, startOffset, endOffset)

    public fun plusElement(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = super.plusElementToList(receiver, other, startOffset, endOffset)

    public fun minusIterable(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = super.minusIterableToList(receiver, other, startOffset, endOffset)

    public fun minusElement(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = super.minusElementToList(receiver, other, startOffset, endOffset)
}

public open class MutableListBuilders(
    collections: CollectionsBuilders,
    builder: IrBuilderWithScope,
    context: IrPluginContext
) :
    ListBuilders(collections, builder, context, Kotlin.Collections.MutableList) {
    public fun add(
        receiver: IrExpression,
        item: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableList.add())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(item)
    }

    public fun remove(
        receiver: IrExpression,
        item: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableList.remove())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(item)
    }

    public fun addAll(
        receiver: IrExpression,
        items: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableList.addAll())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(items)
    }

    public fun addAll(
        receiver: IrExpression,
        items: Iterable<IrExpression>,
        itemType: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = addAll(receiver.checkType(), collections.listOf(itemType, items), startOffset, endOffset)

    public fun removeAll(
        receiver: IrExpression,
        items: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableList.removeAll())
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
        irCall(Kotlin.Collections.MutableList.clear())
            .withDispatchReceiver(receiver.checkType())
    }

    public fun set(
        receiver: IrExpression,
        index: IrExpression,
        item: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableList.set())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(index, item)
    }

    public fun removeLast(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.mutableListRemoveLast).withExtensionReceiver(receiver.checkType())
    }

    public fun removeFirst(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.mutableListRemoveFirst).withExtensionReceiver(receiver.checkType())
    }
}