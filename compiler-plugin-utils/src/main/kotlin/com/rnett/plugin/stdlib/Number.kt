package com.rnett.plugin.stdlib

import com.rnett.plugin.ir.withDispatchReceiver
import com.rnett.plugin.ir.withValueArguments
import com.rnett.plugin.naming.ClassRef
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression

public open class NumberBuilders(
    builder: IrBuilderWithScope,
    context: IrPluginContext,
    type: ClassRef = Kotlin.Number
) :
    TypedMethodBuilder(type, builder, context) {
    public fun toDouble(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Number.toDouble()).withDispatchReceiver(receiver.checkType())
    }

    public fun toFloat(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Number.toFloat()).withDispatchReceiver(receiver.checkType())
    }

    public fun toLong(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Number.toLong()).withDispatchReceiver(receiver.checkType())
    }

    public fun toInt(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Number.toInt()).withDispatchReceiver(receiver.checkType())
    }

    public fun toShort(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Number.toShort()).withDispatchReceiver(receiver.checkType())
    }

    public fun toByte(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Number.toByte()).withDispatchReceiver(receiver.checkType())
    }

    public fun toChar(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Number.toChar()).withDispatchReceiver(receiver.checkType())
    }
}

public class MathableBuilders(
    protected val mathable: Kotlin.Mathable,
    builder: IrBuilderWithScope,
    context: IrPluginContext
) :
    NumberBuilders(builder, context, mathable.klass) {
    public fun plus(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(mathable.plus(other.type)()).withDispatchReceiver(receiver.checkType()).withValueArguments(other)
    }

    public fun minus(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(mathable.minus(other.type)()).withDispatchReceiver(receiver.checkType()).withValueArguments(other)
    }

    public fun times(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(mathable.times(other.type)()).withDispatchReceiver(receiver.checkType()).withValueArguments(other)
    }

    public fun div(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(mathable.div(other.type)()).withDispatchReceiver(receiver.checkType()).withValueArguments(other)
    }
}