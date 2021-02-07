package com.rnett.plugin.stdlib

import com.rnett.plugin.withValueArguments
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCallConstructor
import org.jetbrains.kotlin.ir.expressions.IrExpression

open class ExceptionBuilders(open val klass: ExceptionClass, builder: IrBuilderWithScope, context: IrPluginContext) :
    MethodBuilder(builder, context) {

    fun new(startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET) = buildStatement(startOffset, endOffset) {
        irCallConstructor(klass.newEmpty(), listOf())
    }

    fun newWithMessage(message: IrExpression, startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET) =
        buildStatement(startOffset, endOffset) {
            irCallConstructor(klass.newWithMessage(), listOf()).withValueArguments(message)
        }
}

class ExceptionBuildersWithCause(override val klass: ExceptionClassWithCause, builder: IrBuilderWithScope, context: IrPluginContext) :
    ExceptionBuilders(klass, builder, context) {
    fun newWithMessageAndCause(message: IrExpression, cause: IrExpression, startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET) =
        buildStatement(startOffset, endOffset) {
            irCallConstructor(klass.newWithMessageAndClause(), listOf()).withValueArguments(message, cause)
        }

    fun newWithCause(cause: IrExpression, startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET) =
        buildStatement(startOffset, endOffset) {
            irCallConstructor(klass.newWithClause(), listOf()).withValueArguments(cause)
        }
}