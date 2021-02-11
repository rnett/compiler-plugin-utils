package com.rnett.plugin

import org.jetbrains.kotlin.name.FqName

object Names {
    val Class = FqName("com.rnett.plugin.naming.ClassRef")
    val Package = FqName("com.rnett.plugin.naming.PackageRef")

    val getFqName = FqName("com.rnett.plugin.resolve.getFqName")
    val getFqNameExt = FqName("com.rnett.plugin.resolve.fqName")
    val FqNameClass = FqName("org.jetbrains.kotlin.name.FqName")

    val ClassFunc = FqName("com.rnett.plugin.naming.Class")
    val FunctionFunc = FqName("com.rnett.plugin.naming.function")
    val PropertyFunc = FqName("com.rnett.plugin.naming.property")
    val ConstructorFunc = FqName("com.rnett.plugin.naming.constructor")
    val PrimaryConstructorFunc = FqName("com.rnett.plugin.naming.primaryConstructor")
    val refFuncs = setOf(ClassFunc, FunctionFunc, PropertyFunc, ConstructorFunc, PrimaryConstructorFunc)
}