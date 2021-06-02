package com.rnett.plugin.ir

import com.rnett.plugin.naming.*
import com.rnett.plugin.stdlib.StdlibBuilders
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.allParameters
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.IrSingleStatementBuilder
import org.jetbrains.kotlin.ir.builders.declarations.IrFunctionBuilder
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.parent
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.symbols.IrBindableSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.isSubclassOf
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.utils.addToStdlib.cast


/**
 * A bunch of things using [context] that should mostly be moved to multiple receivers
 */
public interface HasContext {
    public val context: IrPluginContext


    public fun <R : IrBindableSymbol<*, *>> Reference<R>.resolve(): R = resolve(context)
    public fun <R : IrBindableSymbol<*, *>> Reference<R>.resolveOrNull(): R? = resolveOrNull(context)
    public operator fun <R : IrBindableSymbol<*, *>> Reference<R>.invoke(): R = resolve()

    public fun TypeRef.resolve(): IrType = resolve(context)
    public operator fun TypeRef.invoke(): IrType = resolve()

    public fun ClassRef.resolveTypeWith(vararg arguments: IrType): IrSimpleType = resolve().typeWith(*arguments)

    public fun Boolean.asConst(
        type: IrType = context.irBuiltIns.booleanType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrConst<Boolean> = IrConstImpl.boolean(startOffset, endOffset, type, this)

    public fun Byte.asConst(
        type: IrType = context.irBuiltIns.byteType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrConst<Byte> = IrConstImpl.byte(startOffset, endOffset, type, this)

    public fun Char.asConst(
        type: IrType = context.irBuiltIns.charType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrConst<Char> = IrConstImpl.char(startOffset, endOffset, type, this)

    public fun Double.asConst(
        type: IrType = context.irBuiltIns.doubleType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrConst<Double> = IrConstImpl.double(startOffset, endOffset, type, this)

    public fun Float.asConst(
        type: IrType = context.irBuiltIns.floatType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrConst<Float> = IrConstImpl.float(startOffset, endOffset, type, this)

    public fun Int.asConst(
        type: IrType = context.irBuiltIns.intType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrConst<Int> = IrConstImpl.int(startOffset, endOffset, type, this)

    public fun Long.asConst(
        type: IrType = context.irBuiltIns.longType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrConst<Long> = IrConstImpl.long(startOffset, endOffset, type, this)

    public fun Short.asConst(
        type: IrType = context.irBuiltIns.shortType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrConst<Short> = IrConstImpl.short(startOffset, endOffset, type, this)

    public fun String.asConst(
        type: IrType = context.irBuiltIns.stringType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrConst<String> = IrConstImpl.string(startOffset, endOffset, type, this)

    public fun nullConst(
        type: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrConst<Nothing?> = IrConstImpl.constNull(startOffset, endOffset, type)

    public fun IrBuilderWithScope.irCall(funcRef: FunctionRef): IrCall = irCall(funcRef.resolve())
    public fun IrBuilderWithScope.irCall(funcRef: FunctionRef, type: IrType): IrCall = irCall(funcRef.resolve(), type)

    public fun IrClass.isSubclassOf(klass: ClassRef): Boolean = isSubclassOf(klass().owner)
    public fun IrType.isSubclassOf(klass: ClassRef): Boolean = this.isSubtypeOfClass(klass())

    /**
     * Builds a local function to be used as a lambda, likely with [lambdaArgument]
     * Return type must be specified at some point, but may be passed as null and specified later.
     * Will be automatically set if the lambda has an expression body and it wasn't set in [funBuilder] or [funApply]
     */
    public fun IrBuilderWithScope.buildLambda(
        returnType: IrType?,
        funBuilder: IrFunctionBuilder.() -> Unit = {},
        funApply: IrSimpleFunction.() -> Unit
    ): IrSimpleFunction = factory.buildFun {
        name = Name.special("<anonymous>")
        if (returnType != null)
            this.returnType = returnType
        this.origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
        this.visibility = DescriptorVisibilities.LOCAL
        funBuilder()
    }.apply {
        this.patchDeclarationParents(this@buildLambda.parent)
        funApply()

        val returnTypeSet = try {
            this.returnType
            true
        } catch (e: Throwable) {
            false
        }

        val body = this.body
        if (!returnTypeSet && body is IrExpressionBody) {
            this.returnType = body.expression.type
        }
        if (!returnTypeSet && body is IrBlockBody && body.statements.size == 1 && body.statements[0] is IrReturn && body.statements[0].cast<IrReturn>().returnTargetSymbol == this.symbol) {
            this.returnType = body.statements[0].cast<IrReturn>().value.type
        }
    }

    /**
     * Creates a [IrFunctionExpressionImpl] from the given function, auto-inferring the type if not specified
     */
    public fun lambdaArgument(
        lambda: IrSimpleFunction,
        type: IrType = run {
            //TODO workaround for https://youtrack.jetbrains.com/issue/KT-46896
            val base = if (lambda.isSuspend)
                context.referenceClass(
                    StandardNames.getSuspendFunctionClassId(lambda.allParameters.size).asSingleFqName()
                )
                    ?: error("suspend function type not found")
            else
                context.referenceClass(StandardNames.getFunctionClassId(lambda.allParameters.size).asSingleFqName())
                    ?: error("function type not found")

            base.typeWith(lambda.allParameters.map { it.type } + lambda.returnType)
        },
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrFunctionExpression = IrFunctionExpressionImpl(
        startOffset,
        endOffset,
        type,
        lambda,
        IrStatementOrigin.LAMBDA
    )

    public fun createIrBuilder(
        symbol: IrSymbol,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): DeclarationIrBuilder =
        DeclarationIrBuilder(context, symbol, startOffset, endOffset)

    public fun IrSymbolOwner.createIrBuilderAt(): DeclarationIrBuilder =
        DeclarationIrBuilder(context, symbol, startOffset, endOffset)

    public fun <T : IrElement> IrSymbolOwner.buildStatement(
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        origin: IrStatementOrigin? = null,
        block: IrSingleStatementBuilder.() -> T
    ): T {
        return createIrBuilderAt().buildStatement(startOffset, endOffset, origin, block)
    }

    public fun <T> IrSymbolOwner.withBuilder(
        block: DeclarationIrBuilder.() -> T
    ): T = createIrBuilderAt().run(block)

    @ObsoleteDescriptorBasedAPI
    public fun KotlinType.toIr(): IrType = context.typeTranslator.translateType(this)

    public val factory: IrFactory get() = context.irFactory

    /**
     * Get the standard library builders
     */
    @Deprecated("Will be superseded by reference generator")
    public val IrBuilderWithScope.stdlib: StdlibBuilders
        get() = StdlibBuilders(this, this@HasContext.context)

    /**
     * Get the standard library builders
     */
    @Deprecated("Will be superseded by reference generator")
    public val IrBuilderWithScope.kotlin: StdlibBuilders
        get() = stdlib

}

public inline fun <reified T> HasContext.irTypeOf(): IrType = typeRef<T>().toIrType(context)
public inline fun <reified T> IrPluginContext.irTypeOf(): IrType = typeRef<T>().toIrType(this)
