package com.rnett.plugin.stdlib

import com.rnett.plugin.tester.BaseIrPluginTest
import com.rnett.plugin.tester.PluginTestReplaceIn
import com.rnett.plugin.tester.TestProperty
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.expressions.IrExpression

abstract class MathableTests : BaseIrPluginTest() {
    // I only need to test one, since they use the same underlying code

    abstract val IrBuilderWithScope.builder: MathableBuilders

    abstract val IrBuilderWithScope.receiver: IrExpression

    @TestProperty("20.0")
    val IrBuilderWithScope.double by testProperty()

    @PluginTestReplaceIn("receiver + 10")
    fun IrBuilderWithScope.testPlus() = builder.plus(receiver, irInt(10))

    @PluginTestReplaceIn("receiver - 2")
    fun IrBuilderWithScope.testMinus() = builder.minus(receiver, irInt(2))

    @PluginTestReplaceIn("receiver * 3")
    fun IrBuilderWithScope.testTimes() = builder.times(receiver, irInt(3))

    @PluginTestReplaceIn("receiver / 2")
    fun IrBuilderWithScope.testDiv() = builder.div(receiver, irInt(2))

    @PluginTestReplaceIn("receiver + double")
    fun IrBuilderWithScope.testPlusDouble() = builder.plus(receiver, double)

    @PluginTestReplaceIn("receiver - double")
    fun IrBuilderWithScope.testMinusDouble() = builder.minus(receiver, double)

    @PluginTestReplaceIn("receiver * double")
    fun IrBuilderWithScope.testTimesDouble() = builder.times(receiver, double)

    @PluginTestReplaceIn("receiver / double")
    fun IrBuilderWithScope.testDivDouble() = builder.div(receiver, double)

    @PluginTestReplaceIn("receiver.toInt().toByte()")
    fun IrBuilderWithScope.testToByte() = builder.toByte(receiver)

    @PluginTestReplaceIn("receiver.toInt().toShort()")
    fun IrBuilderWithScope.testToShort() = builder.toShort(receiver)

    @PluginTestReplaceIn("receiver.toInt()")
    fun IrBuilderWithScope.testToInt() = builder.toInt(receiver)

    @PluginTestReplaceIn("receiver.toLong()")
    fun IrBuilderWithScope.testToLong() = builder.toLong(receiver)

    @PluginTestReplaceIn("receiver.toFloat()")
    fun IrBuilderWithScope.testToFloat() = builder.toFloat(receiver)

    @PluginTestReplaceIn("receiver.toDouble()")
    fun IrBuilderWithScope.testToDouble() = builder.toDouble(receiver)

    @PluginTestReplaceIn("receiver.toChar()")
    fun IrBuilderWithScope.testToChar() = builder.toChar(receiver)
}

class ByteTests : MathableTests() {
    override val IrBuilderWithScope.builder get() = stdlib.Byte

    @TestProperty("10.toByte()")
    override val IrBuilderWithScope.receiver by testProperty()
}

class ShortTests : MathableTests() {
    override val IrBuilderWithScope.builder get() = stdlib.Short

    @TestProperty("10.toShort()")
    override val IrBuilderWithScope.receiver by testProperty()
}

class IntTests : MathableTests() {
    override val IrBuilderWithScope.builder get() = stdlib.Int

    @TestProperty("10")
    override val IrBuilderWithScope.receiver by testProperty()
}

class LongTests : MathableTests() {
    override val IrBuilderWithScope.builder get() = stdlib.Long

    @TestProperty("10L")
    override val IrBuilderWithScope.receiver by testProperty()
}

class FloatTests : MathableTests() {
    override val IrBuilderWithScope.builder get() = stdlib.Float

    @TestProperty("10.0f")
    override val IrBuilderWithScope.receiver by testProperty()
}

class DoubleTests : MathableTests() {
    override val IrBuilderWithScope.builder get() = stdlib.Double

    @TestProperty("10.0")
    override val IrBuilderWithScope.receiver by testProperty()
}