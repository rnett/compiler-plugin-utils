package com.rnett.plugin.stdlib

import com.rnett.plugin.ir.HasContext
import com.rnett.plugin.naming.ClassRef
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.IrSingleStatementBuilder
import org.jetbrains.kotlin.ir.builders.buildStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isSubtypeOf
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass

public abstract class MethodBuilder(protected val builder: IrBuilderWithScope, override val context: IrPluginContext) :
    HasContext {
    protected inline fun <T : IrElement> buildStatement(
        startOffset: Int,
        endOffset: Int,
        block: IrSingleStatementBuilder.() -> T
    ): T = builder.buildStatement(startOffset, endOffset, block)
}

public abstract class TypedMethodBuilder(
    protected val typeCheck: (IrType) -> Boolean,
    builder: IrBuilderWithScope,
    context: IrPluginContext,
    protected val message: (IrExpression) -> String = { "Expression $it did not pass type check, type was ${it.type}" }
) : MethodBuilder(builder, context) {

    public constructor(supertype: IrType, builder: IrBuilderWithScope, context: IrPluginContext) : this(
        { it.isSubtypeOf(supertype, context.irBuiltIns) },
        builder,
        context,
        { "Expression $it is not a subtype of $supertype, was ${it.type}" }
    )

    public constructor(supertype: IrClassSymbol, builder: IrBuilderWithScope, context: IrPluginContext) : this(
        { it.isSubtypeOfClass(supertype) },
        builder,
        context,
        { "Expression $it is not a subtype of $supertype, was ${it.type}" }
    )

    public constructor(supertype: ClassRef, builder: IrBuilderWithScope, context: IrPluginContext) : this(
        supertype(
            context
        ), builder, context
    )

    public constructor(
        supertype: IrClass,
        builder: IrBuilderWithScope,
        context: IrPluginContext
    ) : this(supertype.symbol, builder, context)

    protected fun <T : IrExpression> T.checkType(): T = apply {
        require(typeCheck(this.type)) { message(this) }
    }

    protected fun <T : IrExpression> T.checkType(type: IrType): T = apply {
        require(
            this.type.isSubtypeOf(
                type,
                context.irBuiltIns
            )
        ) { "Expression $this is not a subtype of $type, was ${this.type}" }
    }
}