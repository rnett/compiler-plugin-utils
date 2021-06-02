package com.rnett.plugin.naming

import org.jetbrains.kotlin.name.FqName
import kotlin.reflect.KCallable
import kotlin.reflect.KClass

//TODO implement.  I need to apply the compiler plugin to this.  I really need two libraries, one before compiler plugin one with it applied
//TODO  or just one with it applied?  I think that's the way to go


/**
 * Has to be called directly on the literal (i.e. not on a reference passed as a parameter).
 * Will be replaced during compilation.
 */
@Suppress("unused")
@Deprecated("Will be superseded by reference generator")
public fun getFqName(element: KCallable<*>): FqName = error("Should be replaced by compiler plugin")


/**
 * Has to be called directly on the literal (i.e. not on a reference passed as a parameter).
 * Will be replaced during compilation.
 */
@Suppress("unused")
@Deprecated("Will be superseded by reference generator")
public fun getFqName(element: KClass<*>): FqName = error("Should be replaced by compiler plugin")


/**
 * Has to be called directly on the literal (i.e. not on a reference passed as a parameter).
 * Will be replaced during compilation.
 */
@Suppress("unused")
@Deprecated("Will be superseded by reference generator")
public fun KCallable<*>.fqName(): FqName = error("Should be replaced by compiler plugin")


/**
 * Has to be called directly on the literal (i.e. not on a reference passed as a parameter).
 * Will be replaced during compilation.
 */
@Suppress("unused")
@Deprecated("Will be superseded by reference generator")
public fun KClass<*>.fqName(): FqName = error("Should be replaced by compiler plugin")

//
///**
// * Has to be called directly on the reference.
// */
////TODO https://youtrack.jetbrains.com/issue/KT-43545
//fun getFqName(element: KFunction<*>): FqName = error("Should be replaced by compiler plugin")
//
//
///**
// * Has to be called directly on the reference.
// */
////TODO https://youtrack.jetbrains.com/issue/KT-43545
//fun KFunction<*>.fqName(): FqName = error("Should be replaced by compiler plugin")