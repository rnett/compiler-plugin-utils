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

@DslMarker
internal annotation class NameDSL

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

/**
 * A reference that is resolvable to an IR symbol via a [IrPluginContext]
 */
@Deprecated("Will be superseded by reference generator")
public interface Reference<S : IrBindableSymbol<*, *>> {
    public fun resolve(context: IrPluginContext): S
    public fun resolveOrNull(context: IrPluginContext): S?
    public operator fun invoke(context: IrPluginContext): S = resolve(context)
}

/**
 * Get the descendant of a FqName (i.e. child but [id] may include `.`).  Returns [this] if [id] is blank.
 */
public fun FqName.descendant(id: String): FqName {
    if (id.isBlank())
        return this

    return id.split('.')
        .fold(this) { name, part ->
            name.child(Name.guessByFirstCharacter(part))
        }
}

public sealed class BaseReference(protected val name: String, internal open val parent: Namespace?) {
    public val fqName: FqName by lazy { parent?.fqName?.descendant(name) ?: FqName(name) }
    override fun toString(): String {
        return fqName.toString()
    }
}

/**
 * A namespace that defiens a FqName.  Either a class or package for now.
 */
@Deprecated("Will be superseded by reference generator")
public sealed class Namespace(name: String, parent: Namespace? = null, protected val isRoot: Boolean = false) :
    BaseReference(name, parent) {
    init {
        if (name.isBlank() && !isRoot)
            error("Can not create a non-root Namespace with a blank name.")
    }
}

/**
 * A package with no parent.  Does not have to be a single package (i.e. [name] can have `.`s).
 *
 * Generally, should be used as the superclass of an object.
 *
 * For an example, see [com.rnett.plugin.stdlib.Kotlin].
 */
@Deprecated("Will be superseded by reference generator")
public open class RootPackage(name: String) : Namespace(name, null, true)

//TODO see about making parent non-nullable and using a new subclass to denote compiler plugin replacement

/**
 * A package reference.  Should be the superclass of an object inside another [Namespace].
 * The parent will be auto-filled (at compile time), as will the name if not set.  The parent will be the enclosing object, which must be a [Namespace].
 * By default, the name is the lower case object name.
 *
 * For an example, see [com.rnett.plugin.stdlib.Kotlin.Collections].
 */
@Deprecated("Will be superseded by reference generator")
public abstract class PackageRef(name: String, parent: Namespace?) :
    Namespace(name, parent) {
    public constructor(name: String) : this(name, null)

    /**
     * Create a [PackageRef] with auto-filled name and parent.  Name will be the lower cased name of the object or class that extends this.
     */
    public constructor() : this("")

    init {
        if (parent == null)
            error("Can't create a package with no parent.  Use RootPackage for top-level packages, otherwise parent should be filled in by the compiler plugin.")
    }
}

/**
 * Create a [ClassRef] from a [FqName].  No parent is used.
 */
@Deprecated("Will be superseded by reference generator")
public fun Class(fqName: FqName): ClassRef = object : ClassRef(fqName.asString(), object : RootPackage("") {}) {}

/**
 * Create a [ClassRef] from a class reference.  [ref] must be a literal, it can not be a variable.
 */
@Deprecated("Will be superseded by reference generator")
public fun Class(@Suppress("UNUSED_PARAMETER") ref: KClass<*>): ClassRef =
    error("Should be replaced by compiler plugin")

/**
 * Create a [ClassRef] with the given name, with the current [Namespace] as its parent.
 */
@Deprecated("Will be superseded by reference generator")
@NameDSL
public fun Namespace.Class(name: String): ClassRef = object : ClassRef(name, this) {}

/**
 * Create a [ClassRef] with the property's name, with the current [Namespace] as its parent.
 */
@Deprecated("Will be superseded by reference generator")
@NameDSL
public fun Namespace.Class(): PropertyDelegateProvider<Any?, ClassRef> =
    PropertyDelegateProvider<Any?, ClassRef> { _, property -> this@Class.Class(property.name) }

//TODO support inheritance: get all props, but with new parent?  Do I even want new parent?

/**
 * A class reference.  Should be the superclass of an object inside another [Namespace].
 * The parent will be auto-filled (at compile time), as will the name if not set.  The parent will be the enclosing object, which must be a [Namespace].
 * By default, the name is the object name.
 *
 * If [parent] is specified, it must be non-null.
 *
 * For an example, see [com.rnett.plugin.stdlib.Kotlin.Collections.List].
 */
@Deprecated("Will be superseded by reference generator")
public abstract class ClassRef(name: String, parent: Namespace?) : Namespace(name, parent), Reference<IrClassSymbol> {
    public constructor(name: String) : this(name, null)
    public constructor() : this("")

    init {
        if (parent == null)
            error("Can't create a class with no parent.  Parent should be specified or filled in by the compiler plugin if used inside a package object.")
    }

    override fun resolveOrNull(context: IrPluginContext): IrClassSymbol? = context.referenceClass(fqName)
    override fun resolve(context: IrPluginContext): IrClassSymbol = resolveOrNull(context) ?: error("Class Not Found")

    public inline operator fun getValue(thisRef: Any?, property: KProperty<*>): ClassRef = this
}

/**
 * A delegate to get a [FunctionRef] with the property's name
 */
@Deprecated("Will be superseded by reference generator")
public class FunctionRefDelegate internal constructor(private val parent: Namespace? = null) : FunctionFilter(),
    ReadOnlyProperty<Any?, FunctionRef> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): FunctionRef =
        FunctionRef(property.name, parent, this)
}

/**
 * A function reference that gets the function matching the name and filter.
 */
@Deprecated("Will be superseded by reference generator")
public class FunctionRef internal constructor(name: String, parent: Namespace?, filter: IFunctionFilter) :
    BaseReference(name, parent), Reference<IrSimpleFunctionSymbol>, IFunctionFilter by filter {
    override fun resolve(context: IrPluginContext): IrSimpleFunctionSymbol =
        context.referenceFunctions(fqName).distinct()
            .singleOrError("Multiple function matches for $fqName", "No matches for $fqName") { matches(it.owner) }

    override fun resolveOrNull(context: IrPluginContext): IrSimpleFunctionSymbol? =
        context.referenceFunctions(fqName).distinct().singleOrNull { matches(it.owner) }

    public inline operator fun getValue(thisRef: Any?, property: KProperty<*>): FunctionRef = this
}

/**
 * Get a function from a function literal.  Can only be called on literals, not variables.
 */
@Deprecated("Will be superseded by reference generator")
public fun function(
    @Suppress("UNUSED_PARAMETER") ref: KFunction<*>,
    @Suppress("UNUSED_PARAMETER") filter: FunctionFilter.() -> Unit = {},
): FunctionRef = error("Should be replaced by compiler plugin")

/**
 * Get a function with [fqName], matching [filter]
 */
@Deprecated("Will be superseded by reference generator")
public fun function(fqName: FqName, filter: FunctionFilter.() -> Unit = {}): FunctionRef =
    FunctionRef(fqName.asString(), null, FunctionFilter().apply(filter))

/**
 * Get a function in the current [Namespace] with the given name, matching [filter]
 */
@Deprecated("Will be superseded by reference generator")
@NameDSL
public fun Namespace.function(name: String, filter: FunctionFilter.() -> Unit = {}): FunctionRef =
    FunctionRef(name, this, FunctionFilter().apply(filter))

/**
 * Get a function delegate in the current [Namespace] with the property's name, matching [filter]
 */
@Deprecated("Will be superseded by reference generator")
@NameDSL
public fun Namespace.function(filter: FunctionFilter.() -> Unit = {}): FunctionRefDelegate =
    FunctionRefDelegate(this).apply(filter)


/**
 * A delegate to get a [PropertyRef] with the property's name
 */
@Deprecated("Will be superseded by reference generator")
public class PropertyRefDelegate internal constructor(private val parent: Namespace? = null) : PropertyFilter(),
    ReadOnlyProperty<Any?, PropertyRef> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): PropertyRef =
        PropertyRef(property.name, parent, this)
}


/**
 * A property reference that gets the property matching the name and filter.
 */
@Deprecated("Will be superseded by reference generator")
public class PropertyRef internal constructor(name: String, parent: Namespace? = null, filter: IPropertyFilter) :
    BaseReference(name, parent), Reference<IrPropertySymbol>, IPropertyFilter by filter {
    override fun resolve(context: IrPluginContext): IrPropertySymbol =
        context.referenceProperties(fqName).distinct()
            .singleOrError("Multiple property matches for $fqName", "No matches for $fqName") { matches(it.owner) }

    override fun resolveOrNull(context: IrPluginContext): IrPropertySymbol? =
        context.referenceProperties(fqName).distinct().singleOrNull { matches(it.owner) }

    public inline operator fun getValue(thisRef: Any?, property: KProperty<*>): PropertyRef = this
}

/**
 * Get a property from a property literal.  Can only be called on literals, not variables.
 */
@Deprecated("Will be superseded by reference generator")
public fun property(
    @Suppress("UNUSED_PARAMETER") ref: KProperty<*>,
    @Suppress("UNUSED_PARAMETER") filter: PropertyFilter.() -> Unit = {},
): PropertyRef = error("Should be replaced by compiler plugin")

/**
 * Get a property with [fqName], matching [filter]
 */
@Deprecated("Will be superseded by reference generator")
public fun property(fqName: FqName, filter: PropertyFilter.() -> Unit = {}): PropertyRef =
    PropertyRef(fqName.asString(), null, PropertyFilter().apply(filter))

/**
 * Get a property in the current [Namespace] with the given name, matching [filter]
 */
@Deprecated("Will be superseded by reference generator")
@NameDSL
public fun Namespace.property(name: String, filter: PropertyFilter.() -> Unit = {}): PropertyRef =
    PropertyRef(name, this, PropertyFilter().apply(filter))

/**
 * Get a property delegate in the current [Namespace] with the property's name, matching [filter]
 */
@Deprecated("Will be superseded by reference generator")
@NameDSL
public fun Namespace.property(filter: PropertyFilter.() -> Unit = {}): PropertyRefDelegate =
    PropertyRefDelegate(this).apply(filter)

/**
 * A constructor reference that gets the constructor of the parent class matching the filter.
 */
@Deprecated("Will be superseded by reference generator")
public class ConstructorRef internal constructor(
    override val parent: ClassRef,
    filter: ConstructorFilter = ConstructorFilter()
) :
    BaseReference("", parent), Reference<IrConstructorSymbol>, IConstructorFilter by filter {
    override fun resolve(context: IrPluginContext): IrConstructorSymbol =
        context.referenceConstructors(fqName).distinct()
            .singleOrError("Multiple constructor matches for $fqName", "No matches for $fqName") { matches(it.owner) }

    override fun resolveOrNull(context: IrPluginContext): IrConstructorSymbol? =
        context.referenceConstructors(fqName).distinct().singleOrNull { matches(it.owner) }

    public inline operator fun getValue(thisRef: Any?, property: KProperty<*>): ConstructorRef = this
}

/**
 * Get a constructor from the given class literal matching [filter].  Must be called with a literal.
 */
@Deprecated("Will be superseded by reference generator")
public fun constructor(
    @Suppress("UNUSED_PARAMETER") ref: KClass<*>,
    @Suppress("UNUSED_PARAMETER") filter: ConstructorFilter.() -> Unit = {},
): ConstructorRef = error("Should be replaced by compiler plugin")

/**
 * Get a constructor from the class with [fqClassName], matching [filter].
 */
@Deprecated("Will be superseded by reference generator")
public fun constructor(fqClassName: FqName, filter: ConstructorFilter.() -> Unit = {}): ConstructorRef =
    ConstructorRef(Class(fqClassName), ConstructorFilter().apply(filter))

/**
 * Get a constructor from this [ClassRef] that matches [filter].
 */
@Deprecated("Will be superseded by reference generator")
@NameDSL
public fun ClassRef.constructor(filter: IConstructorFilter.() -> Unit): ConstructorRef =
    ConstructorRef(this).apply(filter)


/**
 * Get the primary constructor from the given class literal matching [filter].  Must be called with a literal.
 */
@Deprecated("Will be superseded by reference generator")
public fun primaryConstructor(
    @Suppress("UNUSED_PARAMETER") ref: KClass<*>,
    @Suppress("UNUSED_PARAMETER") filter: ConstructorFilter.() -> Unit = {},
): ConstructorRef = error("Should be replaced by compiler plugin")

/**
 * Get the primary constructor from the class with [fqClassName], matching [filter].
 */
@Deprecated("Will be superseded by reference generator")
public fun primaryConstructor(fqClassName: FqName, filter: ConstructorFilter.() -> Unit = {}): ConstructorRef =
    ConstructorRef(Class(fqClassName), ConstructorFilter().apply(filter).apply { isPrimary = true })

/**
 * Get the primary constructor from this [ClassRef] that matches [filter].
 */
@NameDSL
public fun ClassRef.primaryConstructor(filter: IConstructorFilter.() -> Unit = {}): ConstructorRef =
    constructor(filter).apply { isPrimary = true }

/**
 * Get a constructor for the class with [className] in this [PackageRef], matching [filter].
 */
@Deprecated("Will be superseded by reference generator")
@Suppress("unused")
@NameDSL
public fun PackageRef.constructor(className: String, filter: ConstructorFilter.() -> Unit = {}): ConstructorRef =
    ConstructorRef(object : ClassRef(className, this) {}, ConstructorFilter().apply(filter))

/**
 * Get the primary constructor for the class with [className] in this [PackageRef], matching [filter].
 */
@Deprecated("Will be superseded by reference generator")
@Suppress("unused")
@NameDSL
public fun PackageRef.primaryConstructor(className: String, filter: ConstructorFilter.() -> Unit = {}): ConstructorRef =
    ConstructorRef(object : ClassRef(className, this) {}, ConstructorFilter().apply(filter).apply { isPrimary = true })

/**
 * Get a constructor for the class with [className] in this [RootPackage], matching [filter].
 */
@Deprecated("Will be superseded by reference generator")
@NameDSL
public fun RootPackage.constructor(className: String, filter: ConstructorFilter.() -> Unit = {}): ConstructorRef =
    ConstructorRef(object : ClassRef(className, this) {}, ConstructorFilter().apply(filter))

/**
 * Get the primary constructor for the class with [className] in this [RootPackage], matching [filter].
 */
@Deprecated("Will be superseded by reference generator")
@NameDSL
public fun RootPackage.primaryConstructor(
    className: String,
    filter: ConstructorFilter.() -> Unit = {}
): ConstructorRef =
    ConstructorRef(object : ClassRef(className, this) {}, ConstructorFilter().apply(filter).apply { isPrimary = true })
