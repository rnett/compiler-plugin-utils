package com.rnett.plugin.stdlib

import com.rnett.plugin.tester.BaseIrPluginTest
import com.rnett.plugin.tester.PluginTestReplaceIn
import com.rnett.plugin.tester.TestFunction
import com.rnett.plugin.tester.TestProperty
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irInt

class SetTests : BaseIrPluginTest() {

    @TestProperty("setOf(1, 2, 3)")
    val IrBuilderWithScope.set by testProperty()

    @TestProperty("listOf(1, 2, 3)")
    val IrBuilderWithScope.list by testProperty()

    @TestFunction("set.toMutableSet()")
    val IrBuilderWithScope.newMutableSet by testFunction()


    // set

    @PluginTestReplaceIn("set.size")
    fun IrBuilderWithScope.testSetSize() = stdlib.collections.Set.size(set)

    @PluginTestReplaceIn("set.isEmpty()")
    fun IrBuilderWithScope.testSetIsEmpty() = stdlib.collections.Set.isEmpty(set)

    @PluginTestReplaceIn("2 in set")
    fun IrBuilderWithScope.testSetContains() = stdlib.collections.Set.contains(set, irInt(2))

    @PluginTestReplaceIn("set.iterator().asSequence().toList()", suffix = ".asSequence().toList()", irValueType = "Iterator<Int>")
    fun IrBuilderWithScope.testSetIterator() = stdlib.collections.Set.iterator(set)

    @PluginTestReplaceIn("set.containsAll(list)")
    fun IrBuilderWithScope.testContainsAll() = stdlib.collections.Set.containsAll(set, list)

    @PluginTestReplaceIn("set.containsAll(listOf(1, 2, 3, 4))")
    fun IrBuilderWithScope.testContainsAllBuildSet() =
        stdlib.collections.Set.containsAll(set, context.irBuiltIns.intType, setOf(1, 2, 3, 4).map { irInt(it) })
}