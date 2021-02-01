package com.rnett.plugin.naming

import com.rnett.plugin.typeWith
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeArgument
import org.jetbrains.kotlin.ir.types.impl.IrStarProjectionImpl
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.types.withHasQuestionMark
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.types.Variance
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.typeOf

enum class TypeRefVariance {
    In, Out, Invariant;
}

sealed class TypeProjectionRef {
    val isStar get() = this is StarRef
    abstract fun toIrTypeArg(context: IrPluginContext): IrTypeArgument
}

object StarRef : TypeProjectionRef() {
    override fun toIrTypeArg(context: IrPluginContext): IrTypeArgument = IrStarProjectionImpl
}

//TODO handle annotations
class TypeRef(val classifier: ClassRef, val nullable: Boolean, val arguments: List<TypeProjectionRef>, val variance: TypeRefVariance) :
    TypeProjectionRef() {
    override fun toIrTypeArg(context: IrPluginContext): IrTypeArgument = makeTypeProjection(
        toIrType(context), when (variance) {
            TypeRefVariance.In -> Variance.INVARIANT
            TypeRefVariance.Out -> Variance.IN_VARIANCE
            TypeRefVariance.Invariant -> Variance.OUT_VARIANCE
        }
    )

    fun toIrType(context: IrPluginContext): IrType =
        classifier.resolve(context).typeWith(arguments.map { it.toIrTypeArg(context) }).withHasQuestionMark(nullable)

    fun resolve(context: IrPluginContext) = toIrType(context)
}

private fun makeTypeRef(proj: KTypeProjection): TypeProjectionRef {
    if (proj.type == null)
        return StarRef

    val variance = when (proj.variance!!) {
        KVariance.INVARIANT -> TypeRefVariance.Invariant
        KVariance.IN -> TypeRefVariance.In
        KVariance.OUT -> TypeRefVariance.Out
    }

    return makeTypeRef(proj.type!!, variance)
}

@PublishedApi
internal fun makeTypeRef(type: KType, variance: TypeRefVariance): TypeRef {

    val klass = type.classifier?.let {
        it as? KClass<*> ?: error("Is not a class-based type")
    } ?: error("Type does not have a classifier")

    val classRef = Class(FqName(klass.qualifiedName ?: error("Class $klass does not have a qualified name")))

    val arguments = type.arguments.map { makeTypeRef(it) }
    return TypeRef(classRef, type.isMarkedNullable, arguments, variance)
}

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> typeRef(): TypeRef = makeTypeRef(typeOf<T>(), TypeRefVariance.Invariant)
