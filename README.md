# Kotlin Compiler Plugin Utils

[![Maven Central](https://img.shields.io/maven-central/v/com.github.rnett.compiler-plugin-utils/compiler-plugin-utils)](https://search.maven.org/artifact/com.github.rnett.compiler-plugin-utils/compiler-plugin-utils)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/com.github.rnett.compiler-plugin-utils/compiler-plugin-utils?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/com/github/rnett/compiler-plugin-utils/)

Utilities for writing Kotlin compiler plugins.

### Artifacts

* Library: `com.github.rnett.compiler-plugin-utils:compiler-plugin-utils`
    * Native: `compiler-plugin-utils-native` for native compiler plugins. It will have to be shaded in.
      Uses `kotlin-compiler` instead of
      `kotlin-compiler-embedable`.
* Compiler plugin (actually the gradle plugin for it): `com.github.rnett.compiler-plugin-utils`

The compiler plugin is optional, but is required for some features (it is explicitly noted when required).

Releases are on maven central, snapshots are on `https://oss.sonatype.org/content/repositories/snapshots`.

### [Docs](https://rnett.github.io/compiler-plugin-utils/release/-compiler%20-plugin%20-utils)

[For latest SNAPSHOT build](https://rnett.github.io/compiler-plugin-utils/snapshot/-compiler%20-plugin%20-utils/)

## Features

**Names and stdlib builders are deprecated, and will be replaced by a reference generator I'm working on**

Stdlib is fully tested on JVM, as is Naming. IR utilities are mostly tested.

The usage of Naming and some of the IR utilities can be seen in
the [stdlib code](compiler-plugin-utils/src/main/kotlin/com/rnett/plugin/stdlib).

Using the Stdlib builders on JS may cause `Not found Idx for public kotlin/to|9142910121690433229[0]` like errors. These
are because the return types of `irCall`s are not sufficiently specified. Make an issue here and I'll see what I can do.
Testing JS linking is not feasible ATM, so I can't guarantee you won't run into these, but they shouldn't happen too
often. As a workaround, you can use the IR utility `inferReturnType` or implement something similar yourself.

### IR Utilities

The `com.rnett.plugin.ir` package contains a number of utilities for working with IR. This includes basic utilities such
as `CompilerConfiguration.messageCollector`, `IrClass.addAnonymousInitializer`, `IrType.raiseTo`,
and `IrClass.typeWith(List<IrTypeArgument>)`, all of which are available as extension functions.

Many utilities require a `IrPluginContext`, so in lieu of multiple receivers, they are put in `HasContext` which has
a `val context: IrPluginContext`. It can be easily implemented by `IrElementTransformer`s. Of special note are
the `IrBuilderWithScope.buildLambda` and `lambdaArgument` functions.

`KnowsCurrentFile` is a similar interface, but requires a `IrFile` and provides extensions for getting message locations
from `IrElement`s using said file.

Both of these are implemented by `IrTransformer`, which is usable as a replacement for `IrElementTransformerVoid`
or `IrElementTransformerVoidWithContext`. In addition to implementing `IrElementTransformerVoidWithContext`
, `KnowsCurrentFile`, and `HasContext`, it modifies the file transformer so that new declarations can be added to the
current file without running into `ConcurrentModificationException` (it does so by running transforms on a copy of the
declaration list, and then on newly added declarations until no more are added).

### Naming

**Is deprecated, will be replaced by a reference generator.**

The `com.rnett.plugin.naming` package provides ways to get `FqName`s and IR symbols for declarations. It primarily
provides a structured method based on nested objects, but also provides direct access. Each of these reference types
exposes the `FqName` of the element, and can be resolved with an `IrPluginContext`. Function, property, and constructor
references all accept filters (essentially `(IrSimpleFunction) -> Boolean` lambdas or the equivalent, but with some
helper methods for common conditions) to disambiguate overloads. Some utility functions are provided in `HasContext` for
working with references in IR, such as `irCall(FunctionRef)` or `ClassRef.resolveTypeWith`.

#### Structured

The structured name resolution can be seen in
the [stdlib names](compiler-plugin-utils/src/main/kotlin/com/rnett/plugin/stdlib/Names.kt). Objects
extending `RootPackage`, `PackageRef`, and `ClassRef` can be nested, each auto-detecting the name from the object named
and adding its parent's name as a prefix to its own.  **This requires the compiler plugin** since there is
no `inner object` or object delegation. However, if both `name`
and `parent` are specified the compiler plugin is unnecessary. Property, function, and constructor references can be
created inside of class or package references (with constructor references in packages requiring the class name). Note
that just like in IR, extension receivers are not part of an element's `FqName`, only dispatch receivers.

#### Direct

Methods to directly get references are also provided for classes, functions, properties, and constructors. Literal
versions are also provided, that take a class, function, property, or class literal, respectively.  **Using the literal
versions requires the compiler plugin**, since the `FqName`s of the literals will be resolved at compile time.
The `getFqName(literal)` and `literal.fqName()` functions work similarly, **and also require the compiler plugin**. Note
that all of these literal functions only work with literal arguments for the declaration being resolved (thus the name),
not variables or parameters.

#### Types

`typeOf()`-like type resolution methods are also provided. They enable getting a `TypeRef` from a type literal
with `typeRef<T>()`, which can be resolved to an `IrType` later with an `IrPluginContext` or `HasContext`. They can also
be compared to `IrType`s without resolving using `eq`. Methods using `eq` such as `IrType.isType<T>()`
and `IrType.isClassifierOf<T>()` are also provided. The `classifier` of a `TypeRef` is a `ClassRef`, which provides
another method for resolving classes.

### Stdlib

**Is deprecated, will be replaced by a reference generator.**

The `com.rnett.plugin.stdlib` package provides builders for common standard library functions, using the Naming and IR
Utilities features (and providing good examples of how to use them). Collections, `toString` and `hashCode`, `typeOf`,
scope functions, numbers, and common exceptions are included. All builder methods are tested. Note that unlike for
Naming, extension functions are members of their extension receiver, i.e. `Map.getValue`. In some cases, builders will
resolve their functions based on the types of arguments, such as for number operators or nullable `toString`
and `hashCode`. The receiver arguments are type checked (in IR) in most cases, the other arguments aren't.

**Note that using a lot of these functions is a bad idea.**  If you find yourself generating a lot of code using these
methods, you should probably create a utility function and call it from IR instead. The number of builders is there to
provide breadth, not depth.
