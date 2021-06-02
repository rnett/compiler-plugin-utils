package com.rnett.plugin.stdlib

import com.rnett.plugin.ir.irJsExprBody
import com.rnett.plugin.ir.withExtensionReceiver
import com.rnett.plugin.ir.withTypeArguments
import com.rnett.plugin.ir.withValueArguments
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.declarations.addExtensionReceiver
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith

/**
 * The standard library builders, accessible from from [com.rnett.plugin.ir.HasContext.stdlib]
 */
@Deprecated("Will be superseded by reference generator")
public class StdlibBuilders(builder: IrBuilderWithScope, context: IrPluginContext) : MethodBuilder(builder, context) {

    public val collections: CollectionsBuilders by lazy { CollectionsBuilders(this, builder, context) }
    public val Array: ArrayBuilders by lazy { ArrayBuilders(builder, context) }

    public val Any: AnyBuilders by lazy { AnyBuilders(builder, context) }

    public fun to(
        first: IrExpression,
        firstType: IrType,
        second: IrExpression,
        secondType: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.to(), Kotlin.Pair.resolveTypeWith(firstType, secondType))
            .withTypeArguments(firstType, secondType)
            .withExtensionReceiver(first)
            .withValueArguments(second)
    }

    public fun to(
        first: IrExpression,
        second: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.to(), Kotlin.Pair.resolveTypeWith(first.type, second.type))
            .withExtensionReceiver(first)
            .withValueArguments(second)
    }

    public inner class ReflectBuilders {
        public fun typeOf(
            type: IrType,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
        ): IrCall =
            buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Reflect.typeOf(), Kotlin.Reflect.KType().typeWith())
                    .withTypeArguments(type)
            }

        public inner class KClassBuilders {
            public fun isInstance(
                value: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ): IrCall =
                buildStatement(startOffset, endOffset) {
                    irCall(Kotlin.Reflect.KClass.isInstance)
                        .withValueArguments(value)
                }
        }

        public val KClass: KClassBuilders = KClassBuilders()


    }

    public val reflect: ReflectBuilders = ReflectBuilders()

    public fun let(
        receiver: IrExpression,
        returnType: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        body: IrBlockBodyBuilder.(IrValueParameter) -> Unit
    ): IrCall =
        buildStatement(startOffset, endOffset) {
            irCall(Kotlin.let(), returnType).apply {
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

    public fun letExpr(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        body: DeclarationIrBuilder.(IrValueParameter) -> IrExpression,
    ): IrCall =
        buildStatement(startOffset, endOffset) {
            val lambda = buildLambda(null) {
                withBuilder {
                    val param = addValueParameter("it", receiver.type)
                    val ret = body(param)
                    this@buildLambda.body = irJsExprBody(ret)
                }
            }
            irCall(Kotlin.let(), lambda.returnType).apply {
                extensionReceiver = receiver
                putTypeArgument(0, receiver.type)
                putTypeArgument(1, lambda.returnType)
                putValueArgument(0, lambdaArgument(lambda))
            }
        }

    public fun run(
        receiver: IrExpression,
        returnType: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        body: IrBlockBodyBuilder.(IrValueParameter) -> Unit
    ): IrCall =
        buildStatement(startOffset, endOffset) {
            irCall(Kotlin.run(), returnType).apply {
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

    public fun runExpr(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        body: DeclarationIrBuilder.(IrValueParameter) -> IrExpression,
    ): IrCall =
        buildStatement(startOffset, endOffset) {
            val lambda = buildLambda(null) {
                withBuilder {
                    val param = addExtensionReceiver(receiver.type)
                    val ret = body(param)
                    this@buildLambda.body = irJsExprBody(ret)
                }
            }
            irCall(Kotlin.run(), lambda.returnType).apply {
                extensionReceiver = receiver
                putTypeArgument(0, receiver.type)
                putTypeArgument(1, lambda.returnType)
                putValueArgument(0, lambdaArgument(lambda))
            }
        }

    public fun with(
        expr: IrExpression,
        returnType: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        body: IrBlockBodyBuilder.(IrValueParameter) -> Unit
    ): IrCall =
        buildStatement(startOffset, endOffset) {
            irCall(Kotlin.with(), returnType).apply {
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

    public fun withExpr(
        expr: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        body: DeclarationIrBuilder.(IrValueParameter) -> IrExpression,
    ): IrCall =
        buildStatement(startOffset, endOffset) {
            val lambda = buildLambda(null) {
                withBuilder {
                    val param = addExtensionReceiver(expr.type)
                    val ret = body(param)
                    this@buildLambda.body = irJsExprBody(ret)
                }
            }
            irCall(Kotlin.with(), lambda.returnType).apply {
                putTypeArgument(0, expr.type)
                putTypeArgument(1, lambda.returnType)

                putValueArgument(0, expr)
                putValueArgument(
                    1, lambdaArgument(lambda)
                )
            }
        }

    public fun withUnit(
        expr: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        blockBodyBuilder: IrBlockBodyBuilder.(IrValueParameter) -> Unit
    ): IrCall = with(expr, context.irBuiltIns.unitType, startOffset, endOffset, blockBodyBuilder)

    public fun also(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        body: IrBlockBodyBuilder.(IrValueParameter) -> Unit
    ): IrCall =
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

    public fun apply(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        body: IrBlockBodyBuilder.(IrValueParameter) -> Unit
    ): IrCall =
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
    public val Number: NumberBuilders by lazy { NumberBuilders(builder, context) }

    public val Byte: MathableBuilders by lazy { MathableBuilders(Kotlin.Byte, builder, context) }
    public val Short: MathableBuilders by lazy { MathableBuilders(Kotlin.Short, builder, context) }
    public val Int: MathableBuilders by lazy { MathableBuilders(Kotlin.Int, builder, context) }
    public val Long: MathableBuilders by lazy { MathableBuilders(Kotlin.Long, builder, context) }
    public val Float: MathableBuilders by lazy { MathableBuilders(Kotlin.Float, builder, context) }
    public val Double: MathableBuilders by lazy { MathableBuilders(Kotlin.Double, builder, context) }


    public val Throwable: ExceptionBuildersWithCause by lazy {
        ExceptionBuildersWithCause(
            Kotlin.Throwable,
            builder,
            context
        )
    }

    //TODO deprecated because of expect class/typealias resolve issues
    // exception classes are tested via reflection
//
//    public val Error: ExceptionBuildersWithCause by lazy { ExceptionBuildersWithCause(Kotlin.Error, builder, context) }
//
//    @Suppress("unused")
//    public val Exception: ExceptionBuildersWithCause by lazy {
//        ExceptionBuildersWithCause(
//            Kotlin.Exception,
//            builder,
//            context
//        )
//    }
//
//    @Suppress("unused")
//    public val RuntimeException: ExceptionBuildersWithCause by lazy {
//        ExceptionBuildersWithCause(
//            Kotlin.RuntimeException,
//            builder,
//            context
//        )
//    }
//
//    @Suppress("unused")
//    public val IllegalArgumentException: ExceptionBuildersWithCause by lazy {
//        ExceptionBuildersWithCause(
//            Kotlin.IllegalArgumentException,
//            builder,
//            context
//        )
//    }
//
//    @Suppress("unused")
//    public val IllegalStateException: ExceptionBuildersWithCause by lazy {
//        ExceptionBuildersWithCause(
//            Kotlin.IllegalStateException,
//            builder,
//            context
//        )
//    }
//
//    @Suppress("unused")
//    public val UnsupportedOperationException: ExceptionBuildersWithCause by lazy {
//        ExceptionBuildersWithCause(
//            Kotlin.UnsupportedOperationException,
//            builder,
//            context
//        )
//    }
//
//    @Suppress("unused")
//    public val AssertionError: ExceptionBuildersWithCause by lazy {
//        ExceptionBuildersWithCause(
//            Kotlin.AssertionError,
//            builder,
//            context
//        )
//    }
//
//    @Suppress("unused")
//    public val NoSuchElementException: ExceptionBuilders by lazy {
//        ExceptionBuilders(
//            Kotlin.NoSuchElementException,
//            builder,
//            context
//        )
//    }
//
//    @Suppress("unused")
//    public val IndexOutOfBoundsException: ExceptionBuilders by lazy {
//        ExceptionBuilders(
//            Kotlin.IndexOutOfBoundsException,
//            builder,
//            context
//        )
//    }
//
//    @Suppress("unused")
//    public val ClassCastException: ExceptionBuilders by lazy {
//        ExceptionBuilders(
//            Kotlin.ClassCastException,
//            builder,
//            context
//        )
//    }
//
//    @Suppress("unused")
//    public val NullPointerException: ExceptionBuilders by lazy {
//        ExceptionBuilders(
//            Kotlin.NullPointerException,
//            builder,
//            context
//        )
//    }
}