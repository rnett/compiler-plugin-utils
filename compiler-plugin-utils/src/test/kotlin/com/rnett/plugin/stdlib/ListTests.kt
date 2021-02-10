package com.rnett.plugin.stdlib

import com.rnett.plugin.tester.*
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt

class ListTests : BaseIrPluginTest() {

    @TestProperty("listOf(1, 2, 3)")
    val IrBuilderWithScope.list by testProperty()

    @TestFunction("list.toMutableList()")
    val IrBuilderWithScope.newMutableList by testFunction()

    @TestProperty("listOf(10, 20, 3)")
    val IrBuilderWithScope.list2 by testProperty()

    // list

    @PluginTestReplaceIn("list.size")
    fun IrBuilderWithScope.testListSize() = stdlib.collections.List.size(list)

    @PluginTestReplaceIn("list.isEmpty()")
    fun IrBuilderWithScope.testListIsEmpty() = stdlib.collections.List.isEmpty(list)

    @PluginTestReplaceIn("2 in list")
    fun IrBuilderWithScope.testListContains() = stdlib.collections.List.contains(list, irInt(2))

    @PluginTestReplaceIn("list.iterator().asSequence().toList()", suffix = ".asSequence().toList()", irValueType = "Iterator<Int>")
    fun IrBuilderWithScope.testListIterator() = stdlib.collections.List.iterator(list)

    @PluginTestReplaceIn("list.indexOf(2)")
    fun IrBuilderWithScope.testListIndexOf() = stdlib.collections.List.indexOf(list, irInt(2))

    @PluginTestReplaceIn("list + list2")
    fun IrBuilderWithScope.testListPlusList() = stdlib.collections.List.plusIterable(list, list2)

    @PluginTestReplaceIn("list + 21")
    fun IrBuilderWithScope.testListPlusElement() = stdlib.collections.List.plusElement(list, irInt(21))

    @PluginTestReplaceIn("list - list2")
    fun IrBuilderWithScope.testListMinusList() = stdlib.collections.List.minusIterable(list, list2)

    @PluginTestReplaceIn("list - 2")
    fun IrBuilderWithScope.testListMinusElement() = stdlib.collections.List.minusElement(list, irInt(2))

    // mutable list

    @PluginTestReplaceInAlso("list + 5")
    fun IrBuilderWithScope.testListAdd() = withAlso(newMutableList) {
        +stdlib.collections.MutableList.add(irGet(it), irInt(5))
    }

    @PluginTestReplaceInAlso("list - 2")
    fun IrBuilderWithScope.testListRemove() = withAlso(newMutableList) {
        +stdlib.collections.MutableList.remove(irGet(it), irInt(2))
    }

    @PluginTestReplaceInAlso("list + list2.toList()")
    fun IrBuilderWithScope.testListAddAll() = withAlso(newMutableList) {
        +stdlib.collections.MutableList.addAll(irGet(it), list2)
    }

    @PluginTestReplaceInAlso("list + listOf(30, 21)")
    fun IrBuilderWithScope.testListAddAllBuild() = withAlso(newMutableList) {
        +stdlib.collections.MutableList.addAll(irGet(it), listOf(30, 21).map(this::irInt), context.irBuiltIns.intType)
    }

    @PluginTestReplaceInAlso("list - list2")
    fun IrBuilderWithScope.testListRemoveAll() = withAlso(newMutableList) {
        +stdlib.collections.MutableList.removeAll(irGet(it), list2)
    }

    @PluginTestReplaceInAlso("list - listOf(1, 2)")
    fun IrBuilderWithScope.testListRemoveAllBuild() = withAlso(newMutableList) {
        +stdlib.collections.MutableList.removeAll(irGet(it), listOf(1, 2).map(this::irInt), context.irBuiltIns.intType)
    }

    @PluginTestReplaceInAlso("emptyList<Int>()")
    fun IrBuilderWithScope.testListClear() = withAlso(newMutableList) {
        +stdlib.collections.MutableList.clear(irGet(it))
    }

    @PluginTestReplaceInAlso("listOf(10, 2, 3)")
    fun IrBuilderWithScope.testListSet() = withAlso(newMutableList) {
        +stdlib.collections.MutableList.set(irGet(it), irInt(0), irInt(10))
    }

    @PluginTestReplaceInAlso("listOf(1, 2)")
    fun IrBuilderWithScope.testListRemoveLast() = withAlso(newMutableList) {
        +stdlib.collections.MutableList.removeLast(irGet(it))
    }

    @PluginTestReplaceInAlso("listOf(2, 3)")
    fun IrBuilderWithScope.testListRemoveFirst() = withAlso(newMutableList) {
        +stdlib.collections.MutableList.removeFirst(irGet(it))
    }
}