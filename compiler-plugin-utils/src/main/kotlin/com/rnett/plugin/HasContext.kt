package com.rnett.plugin

import com.rnett.plugin.naming.ClassRef
import com.rnett.plugin.naming.Reference
import com.rnett.plugin.naming.TypeRef
import com.rnett.plugin.naming.typeRef
import com.rnett.plugin.stdlib.StdlibBuilders
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.ir.allParameters
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.IrFunctionBuilder
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.symbols.IrBindableSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.KotlinType

interface HasContext {
    val context: IrPluginContext


    fun <R : IrBindableSymbol<*, *>> Reference<R>.resolve(): R = resolve(context)
    fun <R : IrBindableSymbol<*, *>> Reference<R>.resolveOrNull(): R? = resolveOrNull(context)
    operator fun <R : IrBindableSymbol<*, *>> Reference<R>.unaryPlus() = resolve()
    operator fun <R : IrBindableSymbol<*, *>> Reference<R>.invoke() = resolve()

    fun TypeRef.resolve(): IrType = resolve(context)
    operator fun TypeRef.unaryPlus() = resolve()
    operator fun TypeRef.invoke() = resolve()

    fun ClassRef.resolveTypeWith(vararg arguments: IrType) = resolve().typeWith(*arguments)

    fun Boolean.asConst(
        type: IrType = context.irBuiltIns.booleanType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = IrConstImpl.boolean(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, this)

    fun Byte.asConst(
        type: IrType = context.irBuiltIns.byteType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = IrConstImpl.byte(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, this)

    fun Char.asConst(
        type: IrType = context.irBuiltIns.charType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = IrConstImpl.char(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, this)

    fun Double.asConst(
        type: IrType = context.irBuiltIns.doubleType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = IrConstImpl.double(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, this)

    fun Float.asConst(
        type: IrType = context.irBuiltIns.floatType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = IrConstImpl.float(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, this)

    fun Int.asConst(
        type: IrType = context.irBuiltIns.intType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = IrConstImpl.int(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, this)

    fun Long.asConst(
        type: IrType = context.irBuiltIns.longType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = IrConstImpl.long(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, this)

    fun Short.asConst(
        type: IrType = context.irBuiltIns.shortType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = IrConstImpl.short(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, this)

    fun String.asConst(
        type: IrType = context.irBuiltIns.stringType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = IrConstImpl.string(UNDEFINED_OFFSET, UNDEFINED_OFFSET, type, this)


    fun nullConst(
        type: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = IrConstImpl.constNull(startOffset, endOffset, type)

    fun IrBuilderWithScope.buildLambda(
        returnType: IrType,
        funBuilder: IrFunctionBuilder.() -> Unit = {},
        funApply: IrSimpleFunction.() -> Unit
    ): IrSimpleFunction = factory.buildFun {
        name = Name.special("<anonymous>")
        this.returnType = returnType
        this.origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
        this.visibility = DescriptorVisibilities.LOCAL
        funBuilder()
    }.apply {
        this.patchDeclarationParents(this@buildLambda.parent)
        funApply()
    }

    fun IrBuilderWithScope.buildLambda(
        funBuilder: IrFunctionBuilder.() -> Unit = {},
        funApply: IrSimpleFunction.() -> Unit
    ): IrSimpleFunction = factory.buildFun {
        name = Name.special("<anonymous>")
        this.origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
        this.visibility = DescriptorVisibilities.LOCAL
        funBuilder()
    }.apply {
        this.patchDeclarationParents(this@buildLambda.parent)
        funApply()
    }

    fun lambdaArgument(
        lambda: IrSimpleFunction,
        type: IrType = run {
            val base = if (lambda.isSuspend)
                context.irBuiltIns.suspendFunction(lambda.allParameters.size)
            else
                context.irBuiltIns.function(lambda.allParameters.size)

            base.typeWith(lambda.allParameters.map { it.type } + lambda.returnType)
        },
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = IrFunctionExpressionImpl(
        startOffset,
        endOffset,
        type,
        lambda,
        IrStatementOrigin.LAMBDA
    )

    fun createIrBuilder(symbol: IrSymbol, startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET) =
        DeclarationIrBuilder(context, symbol, startOffset, endOffset)

    fun IrSymbolOwner.createIrBuilderAt() = DeclarationIrBuilder(context, symbol, startOffset, endOffset)

    fun <T : IrElement> IrSymbolOwner.buildStatement(
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        origin: IrStatementOrigin? = null,
        block: IrSingleStatementBuilder.() -> T
    ) = createIrBuilderAt().buildStatement(startOffset, endOffset, origin, block)

    fun <T> IrSymbolOwner.withBuilder(
        block: DeclarationIrBuilder.() -> T
    ) = createIrBuilderAt().run(block)

    @ObsoleteDescriptorBasedAPI
    fun KotlinType.toIr() = context.typeTranslator.translateType(this)

    val factory get() = context.irFactory

    val IrBuilderWithScope.stdlib get() = StdlibBuilders(this, this@HasContext.context)

}

inline fun <reified T> HasContext.irTypeOf() = typeRef<T>().toIrType(context)
