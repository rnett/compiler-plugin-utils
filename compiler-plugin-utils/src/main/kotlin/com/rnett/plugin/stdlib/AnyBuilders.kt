package com.rnett.plugin.stdlib

import com.rnett.plugin.ir.withDispatchReceiver
import com.rnett.plugin.ir.withExtensionReceiver
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.isNullable

public class AnyBuilders(builder: IrBuilderWithScope, context: IrPluginContext) : MethodBuilder(builder, context) {
    public fun hashCode(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall =
        buildStatement(startOffset, endOffset) {
            if (receiver.type.isNullable())
                irCall(Kotlin.nullableHashCode()).withExtensionReceiver(receiver)
            else
                irCall(Kotlin.Any.hashCodeRef()).withDispatchReceiver(receiver)
        }

    public fun toString(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall =
        buildStatement(startOffset, endOffset) {
            if (receiver.type.isNullable())
                irCall(Kotlin.nullableToString()).withExtensionReceiver(receiver)
            else
                irCall(Kotlin.Any.toStringRef()).withDispatchReceiver(receiver)
        }
}