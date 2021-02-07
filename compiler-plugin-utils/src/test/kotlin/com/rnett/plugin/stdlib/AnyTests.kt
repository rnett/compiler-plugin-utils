package com.rnett.plugin.stdlib

import com.rnett.plugin.tester.BaseIrPluginTest
import com.rnett.plugin.tester.PluginTestReplaceIn
import com.rnett.plugin.tester.PluginTests
import com.rnett.plugin.tester.TestProperty
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope

@PluginTests
class AnyTests : BaseIrPluginTest() {
    @TestProperty("10", "Int?")
    val IrBuilderWithScope.nullable by testProperty()

    @TestProperty("20")
    val IrBuilderWithScope.any by testProperty()

    @PluginTestReplaceIn("any.hashCode()")
    fun IrBuilderWithScope.testHashCode() = stdlib.Any.hashCode(any)

    @PluginTestReplaceIn("any.toString()")
    fun IrBuilderWithScope.testToString() = stdlib.Any.toString(any)

    @PluginTestReplaceIn("nullable.hashCode()")
    fun IrBuilderWithScope.testNullableHashCode() = stdlib.Any.hashCode(nullable)

    @PluginTestReplaceIn("nullable.toString()")
    fun IrBuilderWithScope.testNullableToString() = stdlib.Any.toString(nullable)
}