package com.rnett.plugin.stdlib

import com.rnett.plugin.ir.irTypeOf
import com.rnett.plugin.tester.BaseIrPluginTest
import com.rnett.plugin.tester.PluginTestReplaceIn
import com.rnett.plugin.tester.PluginTests
import com.rnett.plugin.tester.TestAnnotations
import com.rnett.plugin.tester.TestProperty
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.expressions.IrExpression

//TODO JS tests, as things are different (i.e. listOfNotNull requires the type param)

@PluginTests(imports = arrayOf("kotlin.reflect.typeOf"))
class KotlinTests : BaseIrPluginTest() {

    @TestProperty("10")
    val IrBuilderWithScope.int: IrExpression by testProperty()

    @PluginTestReplaceIn("1 to 2", "Pair<Int, Int>")
    fun IrBuilderWithScope.testPair() =
        stdlib.to(irInt(1), irInt(2))

    @PluginTestReplaceIn("typeOf<List<Int>>()")
    @TestAnnotations(arrayOf("OptIn(kotlin.ExperimentalStdlibApi::class)"))
    fun IrBuilderWithScope.testTypeOf() =
        stdlib.reflect.typeOf(irTypeOf<List<Int>>())

    @PluginTestReplaceIn("int.let{ it + 10 }")
    fun IrBuilderWithScope.testLetExpr() =
        stdlib.letExpr(int) {
            stdlib.Int.plus(irGet(it), irInt(10))
        }

    @PluginTestReplaceIn("int.let{ it + 10 }")
    fun IrBuilderWithScope.testLet() =
        stdlib.let(int, irTypeOf<Int>()) {
            +irReturn(stdlib.Int.plus(irGet(it), irInt(10)))
        }


    @PluginTestReplaceIn("int.run{ this + 10 }")
    fun IrBuilderWithScope.testRunExpr() =
        stdlib.runExpr(int) {
            stdlib.Int.plus(irGet(it), irInt(10))
        }

    @PluginTestReplaceIn("int.run{ this + 10 }")
    fun IrBuilderWithScope.testRun() =
        stdlib.run(int, context.irBuiltIns.intType) {
            +irReturn(stdlib.Int.plus(irGet(it), irInt(10)))
        }


    @PluginTestReplaceIn("with(int){ this + 10 }")
    fun IrBuilderWithScope.testWithExpr() =
        stdlib.withExpr(int) {
            stdlib.Int.plus(irGet(it), irInt(10))
        }

    @PluginTestReplaceIn("with(int){ this + 10 }")
    fun IrBuilderWithScope.testWith() =
        stdlib.with(int, context.irBuiltIns.intType) {
            +irReturn(stdlib.Int.plus(irGet(it), irInt(10)))
        }

    @PluginTestReplaceIn("kotlin.Unit")
    fun IrBuilderWithScope.testWithUnit() =
        stdlib.withUnit(int) {
            +stdlib.Int.plus(irGet(it), irInt(10))
        }

    @PluginTestReplaceIn("int.also{ it + 10 }")
    fun IrBuilderWithScope.testAlso() =
        stdlib.also(int) {
            +stdlib.Int.plus(irGet(it), irInt(10))
        }

    @PluginTestReplaceIn("int.apply{ this + 10 }")
    fun IrBuilderWithScope.testApply() =
        stdlib.apply(int) {
            +stdlib.Int.plus(irGet(it), irInt(10))
        }

}