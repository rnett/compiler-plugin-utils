package com.rnett.plugin.naming

import com.rnett.plugin.ir.typeWith
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.IrStarProjectionImpl
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.types.Variance
import java.util.*
import kotlin.reflect.*

@Deprecated("Will be superseded by reference generator")
public enum class TypeRefVariance {
    In, Out, Invariant;

    public fun toIr(): Variance = when (this) {
        In -> Variance.IN_VARIANCE
        Out -> Variance.OUT_VARIANCE
        Invariant -> Variance.INVARIANT
    }
}

@Deprecated("Will be superseded by reference generator")
public sealed class TypeProjectionRef {
    public val isStar: Boolean get() = this is StarRef
    public abstract fun toIrTypeArg(context: IrPluginContext): IrTypeArgument
    abstract override fun toString(): String
    public abstract fun toIrString(): String
}

@Deprecated("Will be superseded by reference generator")
public object StarRef : TypeProjectionRef() {
    override fun toIrTypeArg(context: IrPluginContext): IrTypeArgument = IrStarProjectionImpl
    override fun toString(): String = "*"
    override fun toIrString(): String = "*"
}

//TODO handle annotations
@Deprecated("Will be superseded by reference generator")
public data class TypeRef(
    public val classifier: ClassRef,
    public val nullable: Boolean,
    public val arguments: List<TypeProjectionRef>,
    public val variance: TypeRefVariance
) :
    TypeProjectionRef() {
    override fun toIrTypeArg(context: IrPluginContext): IrTypeArgument =
        makeTypeProjection(toIrType(context), variance.toIr())

    override fun toString(): String = toString(false)

    override fun toIrString(): String = toString(true)

    private fun toString(ir: Boolean): String = buildString {
        if (variance != TypeRefVariance.Invariant) {
            append(variance.name.lowercase(Locale.getDefault()) + " ")
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

    public fun toIrType(context: IrPluginContext): IrType =
        classifier.resolve(context).typeWith(arguments.map { it.toIrTypeArg(context) }).withHasQuestionMark(nullable)

    public fun resolve(context: IrPluginContext): IrType = toIrType(context)
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

@Deprecated("Will be superseded by reference generator")
@PublishedApi
internal fun makeTypeRef(type: KType, variance: TypeRefVariance): TypeRef {

    val klass = type.classifier?.let {
        it as? KClass<*> ?: error("Is not a class-based type")
    } ?: error("Type does not have a classifier")

    val classRef = Class(FqName(klass.qualifiedName ?: error("Class $klass does not have a qualified name")))

    val arguments = type.arguments.map { makeTypeRef(it) }
    return TypeRef(classRef, type.isMarkedNullable, arguments, variance)
}

@Deprecated("Will be superseded by reference generator")
@OptIn(ExperimentalStdlibApi::class)
public inline fun <reified T> typeRef(): TypeRef = makeTypeRef(typeOf<T>(), TypeRefVariance.Invariant)

@Deprecated("Will be superseded by reference generator")
public infix fun IrType.eq(other: TypeRef): Boolean {
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

@Deprecated("Will be superseded by reference generator")
public inline fun <reified T> IrType.isType(): Boolean = this eq typeRef<T>()

@Deprecated("Will be superseded by reference generator")
public inline fun <reified T> IrType.isClassifierOf(): Boolean = this.isClassifierOf(typeRef<T>().classifier)

@Deprecated("Will be superseded by reference generator")
public fun IrType.isClassifierOf(classRef: ClassRef): Boolean = this.classFqName == classRef.fqName

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
