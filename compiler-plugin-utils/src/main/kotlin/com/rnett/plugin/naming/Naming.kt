package com.rnett.plugin.naming

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

private inline fun <T> Iterable<T>.singleOrError(onMultiple: String, onNone: String, predicate: (T) -> Boolean): T {
    var single: T? = null
    var found = false
    for (element in this) {
        if (predicate(element)) {
            if (found)
                throw IllegalArgumentException(onMultiple)
            single = element
            found = true
        }
    }
    if (!found)
        throw NoSuchElementException(onNone)
    @Suppress("UNCHECKED_CAST")
    return single as T
}

interface Reference<S : IrBindableSymbol<*, *>> {
    fun resolve(context: IrPluginContext): S
    fun resolveOrNull(context: IrPluginContext): S?
    operator fun invoke(context: IrPluginContext): S = resolve(context)
}

fun FqName.descendant(id: String): FqName {
    if (id.isBlank())
        return this

    return id.split('.')
        .fold(this) { name, part ->
            name.child(Name.guessByFirstCharacter(part))
        }
}

sealed class BaseReference(protected val name: String, open val parent: Namespace?) {
    val fqName: FqName by lazy { parent?.fqName?.descendant(name) ?: FqName(name) }
    override fun toString(): String {
        return fqName.toString()
    }
}

sealed class Namespace(name: String, parent: Namespace? = null, val isRoot: Boolean = false) : BaseReference(name, parent) {
    init {
        if (name.isBlank() && !isRoot)
            error("Can not create a non-root Namespace with a blank name.")
    }
}

abstract class RootPackage(name: String) : Namespace(name, null, true) {
    constructor() : this("")
}

abstract class PackageRef @PublishedApi internal constructor(name: String, parent: Namespace?) : Namespace(name, parent) {
    constructor(name: String) : this(name, null)
    constructor() : this("")

    init {
        if (parent == null)
            error("Can't create a package with no parent.  Use RootPackage for top-level packages, otherwise parent should be filled in by the compiler plugin.")
    }
}

fun Class(fqName: FqName) = object : ClassRef(fqName.asString(), object : RootPackage("") {}) {}
fun Class(ref: KClass<*>): ClassRef = error("Should be replaced by compiler plugin")
fun Namespace.Class(name: String) = object : ClassRef(name, this) {}
fun Namespace.Class() = PropertyDelegateProvider<Any?, ClassRef> { _, property -> this@Class.Class(property.name) }

//TODO support inheritance: get all props, but with new parent?  Do I even want new parent?

abstract class ClassRef @PublishedApi internal constructor(name: String, parent: Namespace?) : Namespace(name, parent), Reference<IrClassSymbol> {
    constructor(name: String) : this(name, null)
    constructor() : this("")

    init {
        if (parent == null)
            error("Can't create a class with no parent.  Parent should be filled in by the compiler plugin.")
    }

    override fun resolveOrNull(context: IrPluginContext): IrClassSymbol? = context.referenceClass(fqName)
    override fun resolve(context: IrPluginContext): IrClassSymbol = resolveOrNull(context) ?: error("Class Not Found")

    inline operator fun getValue(thisRef: Any?, property: KProperty<*>) = this
}

class FunctionRefDelegate internal constructor(val parent: Namespace? = null) : FunctionFilter(), ReadOnlyProperty<Any?, FunctionRef> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>) = FunctionRef(property.name, parent, this)
}

class FunctionRef internal constructor(name: String, parent: Namespace?, filter: IFunctionFilter) :
    BaseReference(name, parent), Reference<IrSimpleFunctionSymbol>, IFunctionFilter by filter {
    override fun resolve(context: IrPluginContext) =
        context.referenceFunctions(fqName).distinct()
            .singleOrError("Multiple function matches for $fqName", "No matches for $fqName") { matches(it.owner) }

    override fun resolveOrNull(context: IrPluginContext): IrSimpleFunctionSymbol? =
        context.referenceFunctions(fqName).distinct().singleOrNull { matches(it.owner) }

    inline operator fun getValue(thisRef: Any?, property: KProperty<*>) = this
}

fun function(ref: KFunction<*>, filter: FunctionFilter.() -> Unit = {}): FunctionRef = error("Should be replaced by compiler plugin")
fun function(fqName: FqName, filter: FunctionFilter.() -> Unit = {}) = FunctionRef(fqName.asString(), null, FunctionFilter().apply(filter))
fun Namespace.function(name: String, filter: FunctionFilter.() -> Unit = {}) = FunctionRef(name, this, FunctionFilter().apply(filter))
fun Namespace.function(filter: FunctionFilter.() -> Unit = {}) = FunctionRefDelegate(this).apply(filter)


class PropertyRefDelegate internal constructor(val parent: Namespace? = null) : PropertyFilter(), ReadOnlyProperty<Any?, PropertyRef> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>) = PropertyRef(property.name, parent, this)
}

class PropertyRef internal constructor(name: String, parent: Namespace? = null, filter: IPropertyFilter) :
    BaseReference(name, parent), Reference<IrPropertySymbol>, IPropertyFilter by filter {
    override fun resolve(context: IrPluginContext) =
        context.referenceProperties(fqName).distinct()
            .singleOrError("Multiple property matches for $fqName", "No matches for $fqName") { matches(it.owner) }

    override fun resolveOrNull(context: IrPluginContext): IrPropertySymbol? =
        context.referenceProperties(fqName).distinct().singleOrNull { matches(it.owner) }

    inline operator fun getValue(thisRef: Any?, property: KProperty<*>) = this
}

fun property(ref: KProperty<*>, filter: PropertyFilter.() -> Unit = {}): PropertyRef = error("Should be replaced by compiler plugin")
fun property(fqName: FqName, filter: PropertyFilter.() -> Unit = {}) = PropertyRef(fqName.asString(), null, PropertyFilter().apply(filter))
fun Namespace.property(name: String, filter: PropertyFilter.() -> Unit = {}) = PropertyRef(name, this, PropertyFilter().apply(filter))
fun Namespace.property(filter: PropertyFilter.() -> Unit = {}) = PropertyRefDelegate(this).apply(filter)


class ConstructorRefDelegate internal constructor(val parent: ClassRef) : ConstructorFilter(), ReadOnlyProperty<Any?, ConstructorRef> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>) = value
    val value = ConstructorRef(parent, this)
}

class ConstructorRef internal constructor(override val parent: ClassRef, filter: ConstructorFilter) :
    BaseReference("", parent), Reference<IrConstructorSymbol>, IConstructorFilter by filter {
    override fun resolve(context: IrPluginContext) =
        context.referenceConstructors(fqName).distinct()
            .singleOrError("Multiple constructor matches for $fqName", "No matches for $fqName") { matches(it.owner) }

    override fun resolveOrNull(context: IrPluginContext): IrConstructorSymbol? =
        context.referenceConstructors(fqName).distinct().singleOrNull { matches(it.owner) }

    inline operator fun getValue(thisRef: Any?, property: KProperty<*>) = this
}

fun constructor(ref: KClass<*>, filter: ConstructorFilter.() -> Unit = {}): ConstructorRef = error("Should be replaced by compiler plugin")
fun constructor(fqClassName: FqName, filter: ConstructorFilter.() -> Unit = {}) =
    ConstructorRef(Class(fqClassName), ConstructorFilter().apply(filter))

fun Namespace.constructor(name: String, filter: ConstructorFilter.() -> Unit = {}) =
    ConstructorRef(object : ClassRef(name, this) {}, ConstructorFilter().apply(filter))

fun ClassRef.constructor(filter: ConstructorFilter.() -> Unit) = ConstructorRefDelegate(this).apply(filter)


fun primaryConstructor(ref: KClass<*>, filter: ConstructorFilter.() -> Unit = {}): ConstructorRef = error("Should be replaced by compiler plugin")
fun primaryConstructor(fqClassName: FqName, filter: ConstructorFilter.() -> Unit = {}) =
    ConstructorRef(Class(fqClassName), ConstructorFilter().apply(filter).apply { isPrimary = true })

fun Namespace.primaryConstructor(name: String, filter: ConstructorFilter.() -> Unit = {}) =
    ConstructorRef(object : ClassRef(name, this) {}, ConstructorFilter().apply(filter).apply { isPrimary = true })

fun ClassRef.primaryConstructor(filter: ConstructorFilter.() -> Unit) = ConstructorRefDelegate(this).apply(filter).apply { isPrimary = true }
