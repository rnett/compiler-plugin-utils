package com.rnett.plugin.stdlib

import com.rnett.plugin.tester.BaseIrPluginTest
import com.rnett.plugin.tester.PluginTestReplaceIn
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irInt

class KotlinTests : BaseIrPluginTest() {


    @PluginTestReplaceIn("1 to 2", "Pair<Int, Int>")
    fun IrBuilderWithScope.testPair() =
        stdlib.to(irInt(1), irInt(2))

}