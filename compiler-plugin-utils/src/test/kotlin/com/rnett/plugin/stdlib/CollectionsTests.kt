package com.rnett.plugin.stdlib

import com.rnett.plugin.ir.irTypeOf
import com.rnett.plugin.tester.BaseIrPluginTest
import com.rnett.plugin.tester.PluginTestReplaceIn
import com.rnett.plugin.tester.TestProperty
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irNull

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

    @PluginTestReplaceIn("listOfNotNull(null, 1, 2, null)", "List<Int>")
    fun IrBuilderWithScope.testListNotNull() =
        stdlib.collections.listOfNotNull(context.irBuiltIns.intType, listOf(irNull(), irInt(1), irInt(2), irNull()))

    @PluginTestReplaceIn("setOfNotNull(null, 1, 2, null)", "Set<Int>")
    fun IrBuilderWithScope.testSetNotNull() =
        stdlib.collections.setOfNotNull(context.irBuiltIns.intType, listOf(irNull(), irInt(1), irInt(2), irNull()))

    @PluginTestReplaceIn("mapOf(1 to 2, 3 to 4)", "Map<Int, Int>")
    fun IrBuilderWithScope.testMap() =
        stdlib.collections.mapOf(
            context.irBuiltIns.intType,
            context.irBuiltIns.intType,
            mapOf(irInt(1) to irInt(2), irInt(3) to irInt(4))
        )


    @PluginTestReplaceIn("listOf(1, 2)", "MutableList<Int>")
    fun IrBuilderWithScope.testMutableList() =
        stdlib.collections.mutableListOf(context.irBuiltIns.intType, listOf(irInt(1), irInt(2)))


    @PluginTestReplaceIn("setOf(1, 2)", "MutableSet<Int>")
    fun IrBuilderWithScope.testMutableSet() =
        stdlib.collections.mutableSetOf(context.irBuiltIns.intType, listOf(irInt(1), irInt(2)))


    @PluginTestReplaceIn("mapOf(1 to 2, 3 to 4)", "MutableMap<Int, Int>")
    fun IrBuilderWithScope.testMutableMap() =
        stdlib.collections.mutableMapOf(
            context.irBuiltIns.intType,
            context.irBuiltIns.intType,
            mapOf(irInt(1) to irInt(2), irInt(3) to irInt(4))
        )


    @PluginTestReplaceIn("pairList", "List<*>")
    fun IrBuilderWithScope.testMapToList() =
        stdlib.collections.Map.toList(map)

    @PluginTestReplaceIn("emptyList<Int>()")
    fun IrBuilderWithScope.testEmptyList() =
        stdlib.collections.emptyList(irTypeOf<Int>())

    @PluginTestReplaceIn("emptySet<Int>()")
    fun IrBuilderWithScope.testEmptySet() =
        stdlib.collections.emptySet(irTypeOf<Int>())

    @PluginTestReplaceIn("emptyMap<Int, Int>()")
    fun IrBuilderWithScope.testEmptyMap() =
        stdlib.collections.emptyMap(irTypeOf<Int>(), irTypeOf<Int>())

    @PluginTestReplaceIn("emptyList<Int>()", "MutableList<Int>")
    fun IrBuilderWithScope.testEmptyMutableList() =
        stdlib.collections.emptyMutableList(irTypeOf<Int>())

    @PluginTestReplaceIn("emptySet<Int>()", "MutableSet<Int>")
    fun IrBuilderWithScope.testEmptyMutableSet() =
        stdlib.collections.emptyMutableSet(irTypeOf<Int>())

    @PluginTestReplaceIn("emptyMap<Int, Int>()", "MutableMap<Int, Int>")
    fun IrBuilderWithScope.testEmptyMutableMap() =
        stdlib.collections.emptyMutableMap(irTypeOf<Int>(), irTypeOf<Int>())

}


class ArrayTests : BaseIrPluginTest() {

    @TestProperty("listOf(1, 2, 3)")
    val IrBuilderWithScope.list by testProperty()

    @TestProperty("arrayOf(1, 2, 3)")
    val IrBuilderWithScope.array by testProperty()

    @PluginTestReplaceIn("list", "List<Int>")
    fun IrBuilderWithScope.testToList() = stdlib.Array.toList(array)

    @PluginTestReplaceIn("list", "MutableList<Int>")
    fun IrBuilderWithScope.testToMutableList() = stdlib.Array.toMutableList(array)
}


class CollectionTests : BaseIrPluginTest() {

    @TestProperty("listOf(1, 2, 3)")
    val IrBuilderWithScope.list by testProperty()

    @PluginTestReplaceIn(
        "",
        suffix = ".contentEquals(list.toTypedArray())",
        assertMethod = "assertTrue",
        irValueType = "Array<Int>"
    )
    fun IrBuilderWithScope.testToTypedArray() =
        stdlib.collections.Collection.toTypedArray(list, context.irBuiltIns.intType)
}


class IterableTests : BaseIrPluginTest() {

    @TestProperty("setOf(1, 2, 3)")
    val IrBuilderWithScope.set by testProperty()

    @TestProperty("mapOf(1 to 2, 2 to 3)")
    val IrBuilderWithScope.map by testProperty()

    @TestProperty("map.toList()")
    val IrBuilderWithScope.pairList by testProperty()

    @TestProperty("listOf(1, 2, 3)")
    val IrBuilderWithScope.list by testProperty()

    @PluginTestReplaceIn("map", "Map<Int, Int>")
    fun IrBuilderWithScope.testToMap() =
        stdlib.collections.Iterable.toMap(pairList)

    @PluginTestReplaceIn("list", "List<Int>")
    fun IrBuilderWithScope.testSetToList() =
        stdlib.collections.Iterable.toList(set)

    @PluginTestReplaceIn("list", "MutableList<Int>")
    fun IrBuilderWithScope.testSetToMutableList() =
        stdlib.collections.Iterable.toMutableList(set)

}