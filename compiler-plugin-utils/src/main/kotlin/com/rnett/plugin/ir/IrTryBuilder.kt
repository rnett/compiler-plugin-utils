package com.rnett.plugin.ir

import org.jetbrains.kotlin.backend.common.lower.irCatch
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.declarations.buildVariable
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrCatch
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrTry
import org.jetbrains.kotlin.ir.expressions.impl.IrTryImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isSubtypeOf
import org.jetbrains.kotlin.name.Name
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


public fun IrBuilderWithScope.irTry(
    result: IrExpression,
    type: IrType,
    catches: List<IrCatch>,
    finally: IrExpression? = null,
): IrTry =
    IrTryImpl(startOffset, endOffset, type, result, catches, finally)

public fun IrBuilderWithScope.irTry(result: IrExpression, type: IrType = result.type): IrTry =
    irTry(result, type, emptyList(), null)

//TODO convert to extensions on IrTry.  Needs multiple receivers for builder
public class IrTryBuilder(private val builder: IrBuilderWithScope) {
    private val catches = mutableListOf<IrCatch>()
    private val caughtTypes = mutableSetOf<IrType>()
    private var finallyExpression: IrExpression? = null

    @OptIn(ExperimentalContracts::class)
    public fun irCatch(throwableType: IrType, body: IrBuilderWithScope.(IrVariable) -> IrExpression) {
        contract { callsInPlace(body, InvocationKind.EXACTLY_ONCE) }
        if (!throwableType.isSubtypeOf(
                builder.context.irBuiltIns.throwableType,
                builder.context.irBuiltIns
            ) && throwableType != builder.context.irBuiltIns.throwableType
        )
            error("Can only catch types that inherit from kotlin.Throwable")

        if (!caughtTypes.add(throwableType))
            error("Already caught type $throwableType")

        val catchVariable =
            buildVariable(
                builder.scope.getLocalDeclarationParent(),
                builder.startOffset,
                builder.endOffset,
                IrDeclarationOrigin.CATCH_PARAMETER,
                Name.identifier("t_${catches.size}"),
                throwableType
            )

        catches += builder.irCatch(catchVariable, builder.body(catchVariable))
    }

    public fun irFinally(expression: IrExpression) {
        if (finallyExpression != null)
            error("finally expression already set")

        finallyExpression = expression
    }

    @PublishedApi
    internal fun build(result: IrExpression, type: IrType): IrTry =
        builder.irTry(result, type, catches, finallyExpression)
}

public inline fun IrBuilderWithScope.irTry(
    result: IrExpression,
    type: IrType = result.type,
    catches: IrTryBuilder.() -> Unit,
): IrTry =
    IrTryBuilder(this).apply(catches).build(result, type)