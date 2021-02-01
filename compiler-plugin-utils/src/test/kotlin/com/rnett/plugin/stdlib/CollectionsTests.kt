package com.rnett.plugin.stdlib

import com.rnett.plugin.tester.BaseIrPluginTest
import com.rnett.plugin.tester.PluginTestReplaceIn
import com.rnett.plugin.tester.TestProperty
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irInt

class CollectionsTests : BaseIrPluginTest() {

    @TestProperty("listOf(1, 2, 3)")
    val IrBuilderWithScope.list by testProperty()

    @TestProperty("setOf(1, 2, 3)")
    val IrBuilderWithScope.set by testProperty()

    @TestProperty("mapOf(1 to 2, 2 to 3)")
    val IrBuilderWithScope.map by testProperty()

    @TestProperty("map.toList()")
    val IrBuilderWithScope.pairList by testProperty()

    @PluginTestReplaceIn("listOf(1, 2)", "List<Int>")
    fun IrBuilderWithScope.testList() =
        stdlib.collections.listOf(context.irBuiltIns.intType, listOf(irInt(1), irInt(2)))


    @PluginTestReplaceIn("setOf(1, 2)", "Set<Int>")
    fun IrBuilderWithScope.testSet() =
        stdlib.collections.setOf(context.irBuiltIns.intType, listOf(irInt(1), irInt(2)))


    @PluginTestReplaceIn("mapOf(1 to 2, 3 to 4)", "Map<Int, Int>")
    fun IrBuilderWithScope.testMap() =
        stdlib.collections.mapOf(context.irBuiltIns.intType, context.irBuiltIns.intType, mapOf(irInt(1) to irInt(2), irInt(3) to irInt(4)))


    @PluginTestReplaceIn("listOf(1, 2)", "MutableList<Int>")
    fun IrBuilderWithScope.testMutableList() =
        stdlib.collections.mutableListOf(context.irBuiltIns.intType, listOf(irInt(1), irInt(2)))


    @PluginTestReplaceIn("setOf(1, 2)", "MutableSet<Int>")
    fun IrBuilderWithScope.testMutableSet() =
        stdlib.collections.mutableSetOf(context.irBuiltIns.intType, listOf(irInt(1), irInt(2)))


    @PluginTestReplaceIn("mapOf(1 to 2, 3 to 4)", "MutableMap<Int, Int>")
    fun IrBuilderWithScope.testMutableMap() =
        stdlib.collections.mutableMapOf(context.irBuiltIns.intType, context.irBuiltIns.intType, mapOf(irInt(1) to irInt(2), irInt(3) to irInt(4)))


    @PluginTestReplaceIn("list", "List<Int>")
    fun IrBuilderWithScope.testSetToList() =
        stdlib.collections.toList(set)

    @PluginTestReplaceIn("list", "MutableList<Int>")
    fun IrBuilderWithScope.testSetToMutableList() =
        stdlib.collections.toMutableList(set)

    @PluginTestReplaceIn("pairList", "List<*>")
    fun IrBuilderWithScope.testMapToList() =
        stdlib.collections.toListForMap(map)

    @PluginTestReplaceIn("map", "Map<Int, Int>")
    fun IrBuilderWithScope.testToMap() =
        stdlib.collections.toMapForIterable(pairList)

    @PluginTestReplaceIn("", suffix = ".contentEquals(list.toTypedArray())", assertMethod = "assertTrue", irValueType = "Array<Int>")
    fun IrBuilderWithScope.testToTypedArray() =
        stdlib.collections.collectionToTypedArray(list, context.irBuiltIns.intType)

}