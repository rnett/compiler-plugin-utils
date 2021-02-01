package com.rnett.plugin.tester


/**
 * Lets you specify the generated object name (default is className + "TestObject") and additional imports.
 */
@Target(AnnotationTarget.CLASS)
annotation class PluginTests(val testObjectName: String = "", val imports: Array<String> = arrayOf())

/**
 * Is ran in IR.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class PluginRun

/**
 * For a function that have a IrBuilderWithScope receiver and return an IrExpression.
 * Generates a function in the test object that returns the expression.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class PluginReplace(val returnType: String = "Unit", val args: String = "")

/**
 * For a function that have a IrBuilderWithScope receiver and return an IrExpression.
 * Generates a function that calls [assertMethod] with [expect] and the returned IrExpression + [suffix].
 * If [checkType] is not blank, checks the type against it (either [expect] or [irValueType] must be specified).
 */
@Target(AnnotationTarget.FUNCTION)
annotation class PluginTestReplaceIn(
    val expect: String,
    val checkType: String = "",
    val suffix: String = "",
    val assertMethod: String = "assertEquals",
    val irValueType: String = ""
)

fun PluginTestReplaceIn.toSpec() = TestGenSpec(expect, checkType, suffix, assertMethod, irValueType)


/**
 * Like [PluginTestReplaceIn], for for functions that return [AlsoTest].
 * Calls `input.also{ block() }` and does the comparisons with that.
 * @see PluginTestReplaceIn
 */
@Target(AnnotationTarget.FUNCTION)
annotation class PluginTestReplaceInAlso(
    val expect: String,
    val checkType: String = "",
    val suffix: String = "",
    val assertMethod: String = "assertEquals",
    val irValueType: String = "",
)

fun PluginTestReplaceInAlso.toSpec() = TestGenSpec(expect, checkType, suffix, assertMethod, irValueType)

class TestGenSpec(val expect: String, val checkType: String, val suffix: String, val assertMethod: String, irValueType: String) {
    val irValueType = if (irValueType.isNotBlank()) irValueType else checkType

    val oneArg = expect.isBlank()
    fun generate() = buildString {

        val irValue = (if (irValueType.isBlank()) "irValue()" else "irValue<$irValueType>()")

        append(assertMethod)
        append("(")
        if (oneArg) {
            append("$irValue$suffix")
        } else {
            append("$expect, $irValue$suffix")
        }

        appendLine(")")
        if (checkType.isNotBlank()) {
            appendLine("assertTrue($irValue is $checkType)")
        }

    }.trimEnd()
}

/**
 * Generates a property w/ the same name, with the given initializer.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class TestProperty(val src: String)


/**
 * Generates a property w/ the same name, with the expression body.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class TestFunction(val src: String)