package com.rnett.plugin.stdlib

import com.rnett.plugin.tester.*
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irReturn

class MapTests : BaseIrPluginTest() {

    @TestProperty("mapOf(1 to 2, 2 to 3)")
    val IrBuilderWithScope.map by testProperty()

    @TestProperty("setOf(1)")
    val IrBuilderWithScope.someKeys by testProperty()

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

    @PluginTestReplaceIn("3")
    fun IrBuilderWithScope.testGetValue() =
        stdlib.collections.Map.getValue(map, irInt(2))

    @PluginTestReplaceIn("10")
    fun IrBuilderWithScope.testGetOrDefault() =
        stdlib.collections.Map.getOrDefault(map, irInt(20), irInt(10))

    @PluginTestReplaceIn("10")
    fun IrBuilderWithScope.testGetOrElse() =
        stdlib.collections.Map.getOrElse(map, irInt(20)) {
            +irReturn(irInt(10))
        }

    @PluginTestReplaceIn("10")
    fun IrBuilderWithScope.testGetOrElseExpr() =
        stdlib.collections.Map.getOrElseExpr(map, irInt(20)) { irInt(10) }

    @PluginTestReplaceIn("map + map2")
    fun IrBuilderWithScope.testMapPlusMap() = stdlib.collections.Map.plusMap(map, map2)

    @PluginTestReplaceIn("map + (21 to 22)")
    fun IrBuilderWithScope.testMapPlusElement() = stdlib.collections.Map.plusElementPairOf(map, irInt(21), irInt(22))

    @PluginTestReplaceIn("map + pairList2")
    fun IrBuilderWithScope.testMapPlusIterablePairs() = stdlib.collections.Map.plusIterablePairs(map, pairList2)

    @PluginTestReplaceIn("map - someKeys")
    fun IrBuilderWithScope.testMapMinusIterableKeys() = stdlib.collections.Map.minusIterableKeys(map, someKeys)

    @PluginTestReplaceIn("map - 2")
    fun IrBuilderWithScope.testMapMinusKey() = stdlib.collections.Map.minusKey(map, irInt(2))

    // mutable map

    @PluginTestReplaceInAlso("map + (3 to 4)")
    fun IrBuilderWithScope.testMapPut() = withAlso(newMutableMap) {
        +stdlib.collections.MutableMap.put(irGet(it), irInt(3), irInt(4))
    }

    @PluginTestReplaceInAlso("map - 2")
    fun IrBuilderWithScope.testMapRemove() = withAlso(newMutableMap) {
        +stdlib.collections.MutableMap.remove(irGet(it), irInt(2))
    }

    @PluginTestReplaceInAlso("map + map2")
    fun IrBuilderWithScope.testMapPutAllList() = withAlso(newMutableMap) {
        +stdlib.collections.MutableMap.putAllIterable(irGet(it), pairList2)
    }

    @PluginTestReplaceInAlso("map + map2")
    fun IrBuilderWithScope.testMapPutAllMap() = withAlso(newMutableMap) {
        +stdlib.collections.MutableMap.putAll(irGet(it), map2)
    }

    @PluginTestReplaceInAlso("map + (20 to 21)")
    fun IrBuilderWithScope.testMapPutAllMakeMap() = withAlso(newMutableMap) {
        +stdlib.collections.MutableMap.putAll(
            irGet(it),
            mapOf(irInt(20) to irInt(21)),
            context.irBuiltIns.intType,
            context.irBuiltIns.intType
        )
    }

    @PluginTestReplaceInAlso("emptyMap<Int, Int>()")
    fun IrBuilderWithScope.testMapClear() = withAlso(newMutableMap) {
        +stdlib.collections.MutableMap.clear(irGet(it))
    }

    @PluginTestReplaceIn("10")
    fun IrBuilderWithScope.testGetOrPut() =
        stdlib.collections.MutableMap.getOrPut(newMutableMap, irInt(20)) {
            +irReturn(irInt(10))
        }

    @PluginTestReplaceIn("10")
    fun IrBuilderWithScope.testGetOrPutExpr() =
        stdlib.collections.MutableMap.getOrPutExpr(newMutableMap, irInt(20)) { irInt(10) }

    //TODO not in stdlib yet
//    @PluginTestReplaceIn("10")
//    fun IrBuilderWithScope.testGetOrPutNullable() =
//        stdlib.collections.MutableMap.getOrPutNullable(newMutableMap, irInt(20)) {
//            +irReturn(irInt(10))
//        }
//
//    @PluginTestReplaceIn("10")
//    fun IrBuilderWithScope.testGetOrPutNullableExpr() =
//        stdlib.collections.MutableMap.getOrPutNullableExpr(newMutableMap, irInt(20)) { irInt(10) }
}