package com.rnett.plugin.stdlib

import com.rnett.plugin.*
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.declarations.addExtensionReceiver
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith

class StdlibBuilders(builder: IrBuilderWithScope, context: IrPluginContext) : MethodBuilder(builder, context) {

    val collections by lazy { CollectionsBuilders(this, builder, context) }
    val Array by lazy { ArrayBuilders(builder, context) }

    val Any by lazy { AnyBuilders(builder, context) }

    fun to(
        first: IrExpression,
        firstType: IrType,
        second: IrExpression,
        secondType: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.to())
            .withTypeArguments(firstType, secondType)
            .withExtensionReceiver(first)
            .withValueArguments(second)
    }

    fun to(
        first: IrExpression,
        second: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.to())
            .withExtensionReceiver(first)
            .withValueArguments(second)
    }

    inner class ReflectBuilders {
        fun typeOf(type: IrType, startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET) =
            buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Reflect.typeOf(), Kotlin.Reflect.KType().typeWith())
                    .withTypeArguments(type)
            }
    }

    val reflect = ReflectBuilders()

    fun let(
        receiver: IrExpression,
        returnType: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        body: IrBlockBodyBuilder.(IrValueParameter) -> Unit
    ) =
        buildStatement(startOffset, endOffset) {
            irCall(Kotlin.let()).apply {
                extensionReceiver = receiver
                putTypeArgument(0, receiver.type)
                putTypeArgument(1, returnType)
                putValueArgument(0, lambdaArgument(
                    buildLambda(returnType) {
                        withBuilder {
                            val param = addValueParameter("it", receiver.type)
                            this@buildLambda.body = irBlockBody {
                                body(param)
                            }
                        }
                    }
                ))
            }
        }

    fun letExpr(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        body: (IrExpression) -> IrExpression
    ) =
        buildStatement(startOffset, endOffset) {
            irCall(Kotlin.let()).apply {
                extensionReceiver = receiver
                putTypeArgument(0, receiver.type)
                putValueArgument(0, lambdaArgument(
                    buildLambda(null) {
                        withBuilder {
                            val param = addValueParameter("it", receiver.type)
                            val ret = body(irGet(param))
                            this@buildLambda.body = irExprBody(ret)
                        }
                    }
                ))
            }
        }

    fun run(
        receiver: IrExpression,
        returnType: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        body: IrBlockBodyBuilder.(IrValueParameter) -> Unit
    ) =
        buildStatement(startOffset, endOffset) {
            irCall(Kotlin.run()).apply {
                extensionReceiver = receiver
                putTypeArgument(0, receiver.type)
                putTypeArgument(1, returnType)
                putValueArgument(0, lambdaArgument(
                    buildLambda(returnType) {
                        withBuilder {
                            val param = addExtensionReceiver(receiver.type)
                            this@buildLambda.body = irBlockBody {
                                body(param)
                            }
                        }
                    }
                ))
            }
        }

    fun runExpr(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        body: (IrExpression) -> IrExpression
    ) =
        buildStatement(startOffset, endOffset) {
            irCall(Kotlin.run()).apply {
                extensionReceiver = receiver
                putTypeArgument(0, receiver.type)
                putValueArgument(0, lambdaArgument(
                    buildLambda(null) {
                        withBuilder {
                            val param = addExtensionReceiver(receiver.type)
                            val ret = body(irGet(param))
                            this@buildLambda.body = irExprBody(ret)
                        }
                    }
                ))
            }
        }

    fun with(
        expr: IrExpression,
        returnType: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        body: IrBlockBodyBuilder.(IrValueParameter) -> Unit
    ) =
        buildStatement(startOffset, endOffset) {
            irCall(Kotlin.with()).apply {
                putTypeArgument(0, expr.type)
                putTypeArgument(1, returnType)

                putValueArgument(0, expr)
                putValueArgument(1, lambdaArgument(
                    buildLambda(returnType) {
                        withBuilder {
                            val param = addExtensionReceiver(expr.type)
                            this@buildLambda.body = irBlockBody {
                                body(param)
                            }
                        }
                    }
                ))
            }
        }

    fun withExpr(
        expr: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        body: (IrExpression) -> IrExpression
    ) =
        buildStatement(startOffset, endOffset) {
            irCall(Kotlin.with()).apply {
                putTypeArgument(0, expr.type)

                putValueArgument(0, expr)
                putValueArgument(
                    1, lambdaArgument(
                        buildLambda(null) {
                            withBuilder {
                                val param = addExtensionReceiver(expr.type)
                                val ret = body(irGet(param))
                                this@buildLambda.body = irExprBody(ret)
                            }
                        }
                ))
            }
        }

    fun withUnit(
        expr: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        blockBodyBuilder: IrBlockBodyBuilder.(IrValueParameter) -> Unit
    ) = with(expr, context.irBuiltIns.unitType, startOffset, endOffset, blockBodyBuilder)

    fun also(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        body: IrBlockBodyBuilder.(IrValueParameter) -> Unit
    ) =
        buildStatement(startOffset, endOffset) {
            irCall(Kotlin.also(), receiver.type).apply {
                extensionReceiver = receiver
                putTypeArgument(0, receiver.type)
                putValueArgument(0, lambdaArgument(
                    buildLambda(context.irBuiltIns.unitType) {
                        withBuilder {
                            val param = addValueParameter("it", receiver.type)
                            this@buildLambda.body = irBlockBody {
                                body(param)
                            }
                        }
                    }
                ))
            }
        }

    fun apply(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        body: IrBlockBodyBuilder.(IrValueParameter) -> Unit
    ) =
        buildStatement(startOffset, endOffset) {
            irCall(Kotlin.apply(), receiver.type).apply {
                extensionReceiver = receiver
                putTypeArgument(0, receiver.type)
                putValueArgument(0, lambdaArgument(
                    buildLambda(context.irBuiltIns.unitType) {
                        withBuilder {
                            val param = addExtensionReceiver(receiver.type)
                            this@buildLambda.body = irBlockBody {
                                body(param)
                            }
                        }
                    }
                ))
            }
        }


    @Suppress("unused")
    val Number by lazy { NumberBuilders(builder, context) }

    val Byte by lazy { MathableBuilders(Kotlin.Byte, builder, context) }
    val Short by lazy { MathableBuilders(Kotlin.Short, builder, context) }
    val Int by lazy { MathableBuilders(Kotlin.Int, builder, context) }
    val Long by lazy { MathableBuilders(Kotlin.Long, builder, context) }
    val Float by lazy { MathableBuilders(Kotlin.Float, builder, context) }
    val Double by lazy { MathableBuilders(Kotlin.Double, builder, context) }

    // exception classes are tested via reflection

    val Error by lazy { ExceptionBuildersWithCause(Kotlin.Error, builder, context) }

    @Suppress("unused")
    val Exception by lazy { ExceptionBuildersWithCause(Kotlin.Exception, builder, context) }

    @Suppress("unused")
    val RuntimeException by lazy { ExceptionBuildersWithCause(Kotlin.RuntimeException, builder, context) }

    @Suppress("unused")
    val IllegalArgumentException by lazy { ExceptionBuildersWithCause(Kotlin.IllegalArgumentException, builder, context) }

    @Suppress("unused")
    val IllegalStateException by lazy { ExceptionBuildersWithCause(Kotlin.IllegalStateException, builder, context) }

    @Suppress("unused")
    val UnsupportedOperationException by lazy { ExceptionBuildersWithCause(Kotlin.UnsupportedOperationException, builder, context) }

    @Suppress("unused")
    val AssertionError by lazy { ExceptionBuildersWithCause(Kotlin.AssertionError, builder, context) }

    @Suppress("unused")
    val NoSuchElementException by lazy { ExceptionBuilders(Kotlin.NoSuchElementException, builder, context) }

    @Suppress("unused")
    val IndexOutOfBoundsException by lazy { ExceptionBuilders(Kotlin.IndexOutOfBoundsException, builder, context) }

    @Suppress("unused")
    val ClassCastException by lazy { ExceptionBuilders(Kotlin.ClassCastException, builder, context) }

    @Suppress("unused")
    val NullPointerException by lazy { ExceptionBuilders(Kotlin.NullPointerException, builder, context) }
}