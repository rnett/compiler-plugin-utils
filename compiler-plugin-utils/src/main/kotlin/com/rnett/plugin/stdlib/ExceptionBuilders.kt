package com.rnett.plugin.stdlib

import com.rnett.plugin.ir.withDispatchReceiver
import com.rnett.plugin.ir.withValueArguments
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression

public open class ExceptionBuilders(
    protected open val klass: ExceptionClass,
    builder: IrBuilderWithScope,
    context: IrPluginContext
) :
    MethodBuilder(builder, context) {

    public fun new(startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET): IrConstructorCall =
        buildStatement(startOffset, endOffset) {
            irCallConstructor(klass.newEmpty(), listOf())
        }

    public fun newWithMessage(
        message: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrConstructorCall =
        buildStatement(startOffset, endOffset) {
            irCallConstructor(klass.newWithMessage(), listOf()).withValueArguments(message)
        }

    public fun getMessage(exception: IrExpression, startOffset: Int, endOffset: Int): IrExpression =
        buildStatement(startOffset, endOffset) {
            irCall(klass.message().owner.getter!!)
                .withDispatchReceiver(exception)
        }

    public fun getCause(exception: IrExpression, startOffset: Int, endOffset: Int): IrExpression =
        buildStatement(startOffset, endOffset) {
            irCall(klass.cause().owner.getter!!)
                .withDispatchReceiver(exception)
        }
}

public class ExceptionBuildersWithCause(
    override val klass: ExceptionClassWithCause,
    builder: IrBuilderWithScope,
    context: IrPluginContext
) :
    ExceptionBuilders(klass, builder, context) {
    public fun newWithMessageAndCause(
        message: IrExpression,
        cause: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrConstructorCall =
        buildStatement(startOffset, endOffset) {
            irCallConstructor(klass.newWithMessageAndClause(), listOf()).withValueArguments(message, cause)
        }

    public fun newWithCause(
        cause: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrConstructorCall =
        buildStatement(startOffset, endOffset) {
            irCallConstructor(klass.newWithClause(), listOf()).withValueArguments(cause)
        }
}