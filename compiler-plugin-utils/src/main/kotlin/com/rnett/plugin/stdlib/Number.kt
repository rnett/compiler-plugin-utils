package com.rnett.plugin.stdlib

import com.rnett.plugin.naming.ClassRef
import com.rnett.plugin.withDispatchReceiver
import com.rnett.plugin.withValueArguments
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.expressions.IrExpression

open class NumberBuilders(builder: IrBuilderWithScope, context: IrPluginContext, type: ClassRef = Kotlin.Number) :
    TypedMethodBuilder(type, builder, context) {
    fun toDouble(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Number.toDouble()).withDispatchReceiver(receiver.checkType())
    }

    fun toFloat(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Number.toFloat()).withDispatchReceiver(receiver.checkType())
    }

    fun toLong(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Number.toLong()).withDispatchReceiver(receiver.checkType())
    }

    fun toInt(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Number.toInt()).withDispatchReceiver(receiver.checkType())
    }

    fun toShort(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Number.toShort()).withDispatchReceiver(receiver.checkType())
    }

    fun toByte(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Number.toByte()).withDispatchReceiver(receiver.checkType())
    }

    fun toChar(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Number.toChar()).withDispatchReceiver(receiver.checkType())
    }
}

class MathableBuilders(val mathable: Kotlin.Mathable, builder: IrBuilderWithScope, context: IrPluginContext) :
    NumberBuilders(builder, context, mathable.klass) {
    fun plus(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(mathable.plus(other.type)()).withDispatchReceiver(receiver.checkType()).withValueArguments(other)
    }

    fun minus(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(mathable.minus(other.type)()).withDispatchReceiver(receiver.checkType()).withValueArguments(other)
    }

    fun times(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(mathable.times(other.type)()).withDispatchReceiver(receiver.checkType()).withValueArguments(other)
    }

    fun div(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(mathable.div(other.type)()).withDispatchReceiver(receiver.checkType()).withValueArguments(other)
    }
}