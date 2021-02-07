package com.rnett.plugin.stdlib

import com.rnett.plugin.naming.ClassRef
import com.rnett.plugin.withDispatchReceiver
import com.rnett.plugin.withValueArguments
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrType


open class ListBuilders(
    val collections: CollectionsBuilders,
    builder: IrBuilderWithScope,
    context: IrPluginContext,
    type: ClassRef = Kotlin.Collections.List
) :
    CollectionBuilders(builder, context, type) {
    fun size(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.List.size().owner.getter!!)
            .withDispatchReceiver(receiver.checkType())
    }

    fun isEmpty(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.List.isEmpty())
            .withDispatchReceiver(receiver.checkType())
    }

    fun contains(
        receiver: IrExpression, item: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.List.contains())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(item)
    }

    fun iterator(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.List.iterator())
            .withDispatchReceiver(receiver.checkType())
    }

    fun get(
        receiver: IrExpression, index: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.List.get())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(index)
    }

    fun indexOf(
        receiver: IrExpression, item: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.List.indexOf())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(item)
    }

    fun plusIterable(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = super.plusIterableToList(receiver, other, startOffset, endOffset)

    fun plusElement(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = super.plusElementToList(receiver, other, startOffset, endOffset)

    fun minusIterable(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = super.minusIterableToList(receiver, other, startOffset, endOffset)

    fun minusElement(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = super.minusElementToList(receiver, other, startOffset, endOffset)
}

open class MutableListBuilders(collections: CollectionsBuilders, builder: IrBuilderWithScope, context: IrPluginContext) :
    ListBuilders(collections, builder, context, Kotlin.Collections.MutableList) {
    fun add(
        receiver: IrExpression,
        item: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableList.add())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(item)
    }

    fun remove(
        receiver: IrExpression,
        item: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableList.remove())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(item)
    }

    fun addAll(
        receiver: IrExpression,
        items: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableList.addAll())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(items)
    }

    fun addAll(
        receiver: IrExpression,
        items: Iterable<IrExpression>,
        itemType: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = addAll(receiver.checkType(), collections.listOf(itemType, items), startOffset, endOffset)

    fun removeAll(
        receiver: IrExpression,
        items: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableList.removeAll())
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
        irCall(Kotlin.Collections.MutableList.clear())
            .withDispatchReceiver(receiver.checkType())
    }

    fun set(
        receiver: IrExpression,
        index: IrExpression,
        item: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableList.set())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(index, item)
    }
    //TODO removeLast
}