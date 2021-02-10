package com.rnett.plugin

import com.rnett.plugin.naming.*
import com.rnett.plugin.resolve.fqName
import org.jetbrains.kotlin.name.FqName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

object TestPackage : RootPackage("com.rnett.kotnn") {

    val Mod = object : ClassRef("Mod") {

    }

    val Class2 by Class()

    val topLevelFunc by function()

    val topLevelProp by property()

    object Module : ClassRef() {
        val test by function()
        val test2 by function("testNamed")
        val testProp by property()
        val testProp2 by property("testNamedProp")
        val testConstructor by primaryConstructor()
    }

    object OtherClass : ClassRef("Test")

    val otherConstructor = constructor("Other")
    val otherPrimaryConstructor = primaryConstructor("Other")

    object Annotations : PackageRef()

    object ExternalClass : ClassRef("Ext", RootPackage("external"))
}

class TestClass1(val t: Int) {
    fun func() {

    }

    val prop: Int = 1
}

class TestNaming {
    @Test
    fun testHierarchy() {
        assertEquals("com.rnett.kotnn", TestPackage.fqName.asString())

        assertEquals("com.rnett.kotnn.Mod", TestPackage.Mod.fqName.asString())

        assertEquals("com.rnett.kotnn.Class2", TestPackage.Class2.fqName.asString())

        assertEquals("com.rnett.kotnn.topLevelFunc", TestPackage.topLevelFunc.fqName.asString())
        assertEquals("com.rnett.kotnn.topLevelProp", TestPackage.topLevelProp.fqName.asString())

        assertEquals("com.rnett.kotnn.Module", TestPackage.Module.fqName.asString())
        assertEquals("com.rnett.kotnn.Module.test", TestPackage.Module.test.fqName.asString())
        assertEquals("com.rnett.kotnn.Module.testNamed", TestPackage.Module.test2.fqName.asString())
        assertEquals("com.rnett.kotnn.Module.testProp", TestPackage.Module.testProp.fqName.asString())
        assertEquals("com.rnett.kotnn.Module.testNamedProp", TestPackage.Module.testProp2.fqName.asString())
        assertEquals("com.rnett.kotnn.Module", TestPackage.Module.testConstructor.fqName.asString())

        assertEquals("com.rnett.kotnn.Test", TestPackage.OtherClass.fqName.asString())
        assertEquals("com.rnett.kotnn.Other", TestPackage.otherConstructor.fqName.asString())
        assertEquals("com.rnett.kotnn.Other", TestPackage.otherPrimaryConstructor.fqName.asString())
        assertEquals("com.rnett.kotnn.annotations", TestPackage.Annotations.fqName.asString())
        assertEquals("external.Ext", TestPackage.ExternalClass.fqName.asString())
    }

    @Test
    fun testFqNames() {
        assertEquals("test.Class", Class(FqName("test.Class")).fqName.asString())
        assertEquals("test.Class.func", function(FqName("test.Class.func")).fqName.asString())
        assertEquals("test.Class.prop", property(FqName("test.Class.prop")).fqName.asString())
        assertEquals("test.Class", constructor(FqName("test.Class")).fqName.asString())
        assertEquals("test.Class", primaryConstructor(FqName("test.Class")).fqName.asString())
    }

    @Test
    fun testLiterals() {
        assertEquals("com.rnett.plugin.TestClass1", Class(TestClass1::class).fqName.asString())
        assertEquals("com.rnett.plugin.TestClass1.func", function(TestClass1::func).fqName.asString())
        assertEquals("com.rnett.plugin.TestClass1.prop", property(TestClass1::prop).fqName.asString())
        assertEquals("com.rnett.plugin.TestClass1", constructor(TestClass1::class).fqName.asString())
        assertEquals("com.rnett.plugin.TestClass1", primaryConstructor(TestClass1::class).fqName.asString())
        assertEquals("com.rnett.plugin.TestClass1", typeRef<TestClass1>().classifier.fqName.asString())
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testLiteralResolve() {
        assertEquals("com.rnett.plugin.TestClass1", TestClass1::class.fqName().asString())
        assertEquals("com.rnett.plugin.TestClass1.func", TestClass1::func.fqName().asString())
        assertEquals("com.rnett.plugin.TestClass1.prop", TestClass1::prop.fqName().asString())
    }
}