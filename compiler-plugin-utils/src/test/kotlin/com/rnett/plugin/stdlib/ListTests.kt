package com.rnett.plugin.stdlib

import com.rnett.plugin.tester.BaseIrPluginTest
import com.rnett.plugin.tester.TestFunction
import com.rnett.plugin.tester.TestProperty
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope

class ListTests : BaseIrPluginTest() {

    @TestProperty("listOf(1, 2, 3)")
    val IrBuilderWithScope.list by testProperty()

    @TestFunction("list.toMutableList()")
    val IrBuilderWithScope.newMutableList by testFunction()
}