package com.rnett.plugin.stdlib

import com.rnett.plugin.tester.BaseIrPluginTest
import com.rnett.plugin.tester.PluginTestReplaceIn
import com.rnett.plugin.tester.PluginTestReplaceInAlso
import com.rnett.plugin.tester.TestFunction
import com.rnett.plugin.tester.TestProperty
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt

class SetTests : BaseIrPluginTest() {

    @TestProperty("setOf(1, 2, 3)")
    val IrBuilderWithScope.set by testProperty()

    @TestProperty("listOf(1, 2, 3)")
    val IrBuilderWithScope.list by testProperty()

    @TestProperty("listOf(10, 20, 3)")
    val IrBuilderWithScope.list2 by testProperty()

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

    // mutable set

    @PluginTestReplaceInAlso("set + 5")
    fun IrBuilderWithScope.testSetAdd() = withAlso(newMutableSet) {
        +stdlib.collections.MutableSet.add(irGet(it), irInt(5))
    }

    @PluginTestReplaceInAlso("set - 2")
    fun IrBuilderWithScope.testSetRemove() = withAlso(newMutableSet) {
        +stdlib.collections.MutableSet.remove(irGet(it), irInt(2))
    }

    @PluginTestReplaceInAlso("set + list2.toSet()")
    fun IrBuilderWithScope.testSetAddAll() = withAlso(newMutableSet) {
        +stdlib.collections.MutableSet.addAll(irGet(it), list2)
    }

    @PluginTestReplaceInAlso("set + setOf(30, 21)")
    fun IrBuilderWithScope.testSetAddAllBuild() = withAlso(newMutableSet) {
        +stdlib.collections.MutableSet.addAll(irGet(it), listOf(30, 21).map(this::irInt), context.irBuiltIns.intType)
    }

    @PluginTestReplaceInAlso("set - list2")
    fun IrBuilderWithScope.testSetRemoveAll() = withAlso(newMutableSet) {
        +stdlib.collections.MutableSet.removeAll(irGet(it), list2)
    }

    @PluginTestReplaceInAlso("set - setOf(1, 2)")
    fun IrBuilderWithScope.testSetRemoveAllBuild() = withAlso(newMutableSet) {
        +stdlib.collections.MutableSet.removeAll(irGet(it), listOf(1, 2).map(this::irInt), context.irBuiltIns.intType)
    }

    @PluginTestReplaceInAlso("emptySet<Int>()")
    fun IrBuilderWithScope.testSetClear() = withAlso(newMutableSet) {
        +stdlib.collections.MutableSet.clear(irGet(it))
    }
}