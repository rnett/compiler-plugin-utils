package com.rnett.plugin.resolve

import org.jetbrains.kotlin.name.FqName
import kotlin.reflect.KAnnotatedElement

//TODO implement.  I need to apply the compiler plugin to this.  I really need two libraries, one before compiler plugin one with it applied
//TODO  or just one with it applied?  I think that's the way to go


/**
 * Has to be called directly on the reference.
 */
fun getFqName(element: KAnnotatedElement): FqName = error("Should be replaced by compiler plugin")


/**
 * Has to be called directly on the reference.
 */
fun KAnnotatedElement.fqName(): FqName = error("Should be replaced by compiler plugin")

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