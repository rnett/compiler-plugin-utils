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

@Target(AnnotationTarget.FUNCTION)
annotation class TestAnnotations(val annotations: Array<String>)

/**
 * Generates a property w/ the same name, with the given initializer.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class TestProperty(val src: String, val type: String = "")


/**
 * Generates a property w/ the same name, with the expression body.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class TestFunction(val src: String)