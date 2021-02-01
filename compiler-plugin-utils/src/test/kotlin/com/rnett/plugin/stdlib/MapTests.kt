package com.rnett.plugin.stdlib

import com.rnett.plugin.tester.BaseIrPluginTest
import com.rnett.plugin.tester.PluginTestReplaceIn
import com.rnett.plugin.tester.PluginTestReplaceInAlso
import com.rnett.plugin.tester.TestFunction
import com.rnett.plugin.tester.TestProperty
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt

class MapTests : BaseIrPluginTest() {

    @TestProperty("mapOf(1 to 2, 2 to 3)")
    val IrBuilderWithScope.map by testProperty()

    @TestProperty("mapOf(10 to 12)")
    val IrBuilderWithScope.map2 by testProperty()

    @TestProperty("map.toList()")
    val IrBuilderWithScope.pairList by testProperty()

    @TestProperty("map2.toList()")
    val IrBuilderWithScope.pairList2 by testProperty()

    @TestFunction("map.toMutableMap()")
    val IrBuilderWithScope.newMutableMap by testFunction()


    // map

    @PluginTestReplaceIn("map.size")
    fun IrBuilderWithScope.testMapSize() =
        stdlib.collections.Map.size(map)

    @PluginTestReplaceIn("map.isEmpty()")
    fun IrBuilderWithScope.testMapIsEmpty() =
        stdlib.collections.Map.isEmpty(map)

    @PluginTestReplaceIn("map.containsKey(2)")
    fun IrBuilderWithScope.testMapContainsKey() =
        stdlib.collections.Map.containsKey(map, irInt(2))

    @PluginTestReplaceIn("map.containsValue(2)")
    fun IrBuilderWithScope.testMapContainsValue() =
        stdlib.collections.Map.containsValue(map, irInt(3))

    //TODO getValue

    @PluginTestReplaceIn("map[2]")
    fun IrBuilderWithScope.testMapGet() =
        stdlib.collections.Map.get(map, irInt(2))

    @PluginTestReplaceIn("map.keys")
    fun IrBuilderWithScope.testMapKeys() =
        stdlib.collections.Map.keys(map)

    @PluginTestReplaceIn("map.values")
    fun IrBuilderWithScope.testMapValues() =
        stdlib.collections.Map.values(map)

    @PluginTestReplaceIn("map.entries")
    fun IrBuilderWithScope.testMapEntries() =
        stdlib.collections.Map.entries(map)

    // mutable map

    @PluginTestReplaceInAlso("map + (3 to 4)")
    fun IrBuilderWithScope.testMapPut() = withLet(newMutableMap) {
        +stdlib.collections.MutableMap.put(irGet(it), irInt(3), irInt(4))
    }

    @PluginTestReplaceInAlso("map - 2")
    fun IrBuilderWithScope.testMapRemove() = withLet(newMutableMap) {
        +stdlib.collections.MutableMap.remove(irGet(it), irInt(2))
    }

    @PluginTestReplaceInAlso("map + map2")
    fun IrBuilderWithScope.testMapPutAllList() = withLet(newMutableMap) {
        +stdlib.collections.MutableMap.putAllIterable(irGet(it), pairList2)
    }

    @PluginTestReplaceInAlso("map + map2")
    fun IrBuilderWithScope.testMapPutAllMap() = withLet(newMutableMap) {
        +stdlib.collections.MutableMap.putAll(irGet(it), map2)
    }

    @PluginTestReplaceInAlso("map + (20 to 21)")
    fun IrBuilderWithScope.testMapPutAllMakeMap() = withLet(newMutableMap) {
        +stdlib.collections.MutableMap.putAll(irGet(it), mapOf(irInt(20) to irInt(21)), context.irBuiltIns.intType, context.irBuiltIns.intType)
    }

    @PluginTestReplaceInAlso("emptyMap<Int, Int>()")
    fun IrBuilderWithScope.testMapClear() = withLet(newMutableMap) {
        +stdlib.collections.MutableMap.clear(irGet(it))
    }
}