package com.rnett.plugin.naming

import com.rnett.plugin.typeWith
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrStarProjection
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeArgument
import org.jetbrains.kotlin.ir.types.IrTypeProjection
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.impl.IrStarProjectionImpl
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.types.isMarkedNullable
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

    fun toIr() = when (this) {
        In -> Variance.IN_VARIANCE
        Out -> Variance.OUT_VARIANCE
        Invariant -> Variance.INVARIANT
    }
}

sealed class TypeProjectionRef {
    val isStar get() = this is StarRef
    abstract fun toIrTypeArg(context: IrPluginContext): IrTypeArgument
    abstract override fun toString(): String
    abstract fun toIrString(): String
}

object StarRef : TypeProjectionRef() {
    override fun toIrTypeArg(context: IrPluginContext): IrTypeArgument = IrStarProjectionImpl
    override fun toString(): String = "*"
    override fun toIrString(): String = "*"
}

//TODO handle annotations
class TypeRef(val classifier: ClassRef, val nullable: Boolean, val arguments: List<TypeProjectionRef>, val variance: TypeRefVariance) :
    TypeProjectionRef() {
    override fun toIrTypeArg(context: IrPluginContext): IrTypeArgument = makeTypeProjection(toIrType(context), variance.toIr())

    override fun toString(): String = toString(false)

    override fun toIrString(): String = toString(true)

    private fun toString(ir: Boolean): String = buildString {
        if (variance != TypeRefVariance.Invariant) {
            append(variance.name.toLowerCase() + " ")
        }
        append(classifier)
        if (arguments.isNotEmpty()) {
            append("<")
            append(arguments.joinToString(if (ir) "," else ", "))
            append(">")
        }
        if (nullable)
            append("?")
    }

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

infix fun IrType.eq(other: TypeRef): Boolean {
    if (this.isMarkedNullable() != other.nullable)
        return false

    if (this.classFqName != other.classifier.fqName)
        return false

    if (this !is IrSimpleType)
        return false

    if (this.arguments.size != other.arguments.size)
        return false

    return this.arguments.zip(other.arguments).all { (ir, ref) -> ir eq ref }
}

inline fun <reified T> IrType.isType() = this eq typeRef<T>()

inline fun <reified T> IrType.isClassifierOf() = this.isClassifierOf(typeRef<T>().classifier)

fun IrType.isClassifierOf(classRef: ClassRef) = this.classFqName == classRef.fqName

private infix fun IrTypeArgument.eq(ref: TypeProjectionRef): Boolean =
    when (this) {
        is IrTypeProjection -> {
            if (ref !is TypeRef)
                false
            else {
                this.variance eq ref.variance && this.type eq ref
            }
        }
        is IrStarProjection -> ref == StarRef
        else -> false
    }

private infix fun Variance.eq(variance: TypeRefVariance): Boolean = this == variance.toIr()
