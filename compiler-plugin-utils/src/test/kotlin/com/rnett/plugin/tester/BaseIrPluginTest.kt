package com.rnett.plugin.tester

import com.rnett.plugin.HasContext
import com.rnett.plugin.stdlib.Kotlin
import com.rnett.plugin.withDispatchReceiver
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBlockBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irComposite
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irGetObjectValue
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.patchDeclarationParents
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name
import org.junit.jupiter.api.TestFactory
import java.lang.reflect.InvocationTargetException
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.typeOf
import kotlin.test.fail


data class AlsoTest(val input: IrExpression, val block: IrBlockBodyBuilder.(IrValueParameter) -> Unit)

class TestFailureError(override val message: String) : Exception(message)

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> KFunction<*>.requireExtensionReceiver(annotation: KClass<out Annotation>) {
    val type = typeOf<T>()
    if (this.extensionReceiverParameter == null) {
        fail("Method ${this.name} with annotation ${annotation.simpleName} requires an extension receiver of type $type, but didn't have any")
    } else if (!this.extensionReceiverParameter!!.type.isSupertypeOf(type)) {
        fail("Method ${this.name} with annotation ${annotation.simpleName} requires an extension receiver of type $type, but it was of type ${this.extensionReceiverParameter!!.type}")
    }
}

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> KFunction<*>.requireReturnType(annotation: KClass<out Annotation>) {
    val type = typeOf<T>()
    if (!this.returnType.isSubtypeOf(type)) {
        fail("Method ${this.name} with annotation ${annotation.simpleName} requires a return type of type $type, but it was of type ${this.returnType}")
    }
}

data class PluginTestContext(
    val context: IrPluginContext,
    val messageCollector: MessageCollector,
    val testObject: IrClass
)

fun StringBuilder.appendLineWithIndent(indent: String = "    ", builder: StringBuilder.() -> Unit) = appendLine(
    StringBuilder().apply(builder).toString().prependIndent(indent)
)

//TODO make sealed once testing supports 1.4.30 w/ langVersion = 1.5
abstract class PluginTest {
    abstract val annotations: List<String>

    abstract fun StringBuilder.generate()

    data class Run(val body: () -> Unit) : PluginTest() {
        override val annotations: List<String> = emptyList()

        override fun StringBuilder.generate() {
        }
    }

    data class Replace(
        val name: String,
        val returnType: String = "Unit",
        val args: String = "",
        override val annotations: List<String> = emptyList(),
        val body: IrBuilderWithScope.() -> IrExpression
    ) : PluginTest() {

        override fun StringBuilder.generate() {
            appendLine("fun ${name}(${args}): ${returnType} = error(\"Should be replaced by compiler\")")
            appendLine()
        }
    }

    //TODO make sealed once testing supports 1.4.30 w/ langVersion = 1.5
    abstract class HasSpec(
        val name: String,
        val expect: String,
        val checkType: String? = null,
        val suffix: String = "",
        val assertMethod: String = "assertEquals",
        irValueType: String? = null,
        override val annotations: List<String> = emptyList()
    ) : PluginTest() {

        val irValueType = if (!irValueType.isNullOrBlank()) irValueType else checkType

        val oneArg = expect.isBlank()

        override fun StringBuilder.generate() {
            appendLine("@Test")
            annotations.forEach {
                appendLine("@$it")
            }
            appendLine("fun ${name}(){")
            appendLineWithIndent {
                val body = buildString {

                    val irValue = (if (irValueType.isNullOrBlank()) "irValue()" else "irValue<${irValueType}>()")

                    this.append(assertMethod)
                    append("(")
                    if (oneArg) {
                        append("$irValue${suffix}")
                    } else {
                        append("${expect}, $irValue${suffix}")
                    }

                    appendLine(")")
                    if (!checkType.isNullOrBlank()) {
                        appendLine("assertTrue($irValue is ${checkType})")
                    }

                }.trimEnd()
                appendLine("println(\"\"\"    Test: ${name}\n$body\"\"\".trimIndent())")
                appendLine(body)
            }
            appendLine("}")
            appendLine()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is HasSpec) return false
            if (other::class != this::class) return false

            if (name != other.name) return false
            if (expect != other.expect) return false
            if (checkType != other.checkType) return false
            if (suffix != other.suffix) return false
            if (assertMethod != other.assertMethod) return false
            if (irValueType != other.irValueType) return false
            if (annotations != other.annotations) return false

            return true
        }

        override fun hashCode(): Int {
            var result = this::class.qualifiedName?.hashCode() ?: 0
            result = 31 * result + name.hashCode()
            result = 31 * result + expect.hashCode()
            result = 31 * result + (checkType?.hashCode() ?: 0)
            result = 31 * result + suffix.hashCode()
            result = 31 * result + assertMethod.hashCode()
            result = 31 * result + (irValueType?.hashCode() ?: 0)
            result = 31 * result + annotations.hashCode()
            return result
        }
    }

    class ReplaceIn(
        name: String,
        expect: String,
        checkType: String? = null,
        suffix: String = "",
        assertMethod: String = "assertEquals",
        irValueType: String? = null,
        annotations: List<String> = emptyList(),
        val body: IrBuilderWithScope.() -> IrExpression
    ) : HasSpec(name, expect, checkType, suffix, assertMethod, irValueType, annotations)

    class ReplaceInAlso(
        name: String,
        expect: String,
        checkType: String? = null,
        suffix: String = "",
        assertMethod: String = "assertEquals",
        irValueType: String? = null,
        annotations: List<String> = emptyList(),
        val body: IrBuilderWithScope.() -> AlsoTest
    ) : HasSpec(name, expect, checkType, suffix, assertMethod, irValueType, annotations)
}

class TestGenerator(val tests: MutableList<PluginTest>) {
    operator fun PluginTest.unaryPlus() {
        tests += this
    }

    fun run(body: () -> Unit) = +PluginTest.Run(body)

    fun replace(
        name: String,
        returnType: String = "Unit",
        args: String = "",
        annotations: List<String> = emptyList(),
        body: IrBuilderWithScope.() -> IrExpression
    ) = +PluginTest.Replace(name, returnType, args, annotations, body)

    fun replaceIn(
        name: String,
        expect: String,
        checkType: String? = null,
        suffix: String = "",
        assertMethod: String = "assertEquals",
        irValueType: String? = null,
        annotations: List<String> = emptyList(),
        body: IrBuilderWithScope.() -> IrExpression
    ) = +PluginTest.ReplaceIn(name, expect, checkType, suffix, assertMethod, irValueType, annotations, body)

    fun replaceInAlso(
        name: String,
        expect: String,
        checkType: String? = null,
        suffix: String = "",
        assertMethod: String = "assertEquals",
        irValueType: String? = null,
        annotations: List<String> = emptyList(),
        body: IrBuilderWithScope.() -> AlsoTest
    ) = +PluginTest.ReplaceInAlso(name, expect, checkType, suffix, assertMethod, irValueType, annotations, body)

    fun replaceInAlso(
        receiver: IrExpression,
        name: String,
        expect: String,
        checkType: String? = null,
        suffix: String = "",
        assertMethod: String = "assertEquals",
        irValueType: String? = null,
        annotations: List<String> = emptyList(),
        body: IrBlockBodyBuilder.(IrValueParameter) -> Unit
    ) = +PluginTest.ReplaceInAlso(name, expect, checkType, suffix, assertMethod, irValueType, annotations) {
        AlsoTest(receiver, body)
    }

}

abstract class BaseIrPluginTest : HasContext {

    @PublishedApi
    internal var testContext: PluginTestContext? = null

    override val context: IrPluginContext get() = testContext?.context ?: error("Context has not been initialized yet")
    private val messageCollector: MessageCollector get() = testContext?.messageCollector ?: error("Context has not been initialized yet")
    protected val testObject: IrClass get() = testContext?.testObject ?: error("Context has not been initialized yet")

    @TestFactory
    fun Tests() = MakeTests(this, this::class)

    protected fun withAlso(expression: IrExpression, block: IrBlockBodyBuilder.(IrValueParameter) -> Unit) = AlsoTest(expression, block)

    protected fun testProperty() = ReadOnlyProperty<IrBuilderWithScope, IrCall> { builder, prop ->
        with(builder) {
            irCall(testObject.getPropertyGetter(prop.name)!!).withDispatchReceiver(irGetObject(testObject.symbol))
        }
    }

    protected fun testFunction() = ReadOnlyProperty<IrBuilderWithScope, IrFunctionAccessExpression> { builder, prop ->
        with(builder) {
            irCall(testObject.functions.single { it.name.asString() == prop.name }).withDispatchReceiver(irGetObject(testObject.symbol))
        }
    }

    protected fun fail(message: String): Nothing = throw TestFailureError(message)

    fun findAnnotationTests(): List<PluginTest> {
        Kotlin.apply { }

        val tests = mutableListOf<PluginTest>()

        fun KFunction<*>.testAnnotations() = findAnnotation<TestAnnotations>()?.annotations?.toList().orEmpty()

        fun KFunction<*>.reportError(e: InvocationTargetException) {
            when (val exception = e.cause) {
                is TestFailureError -> messageCollector.report(
                    CompilerMessageSeverity.ERROR,
                    "Test \"${name}\" failed with message: ${exception.message}"
                )
                else -> messageCollector.report(
                    CompilerMessageSeverity.ERROR,
                    "Test \"${name}\" failed with exception: ${exception?.stackTraceToString()}"
                )
            }
        }

        this::class.functions.filter { it.annotations.any { it is PluginRun } }
            .forEach {
                try {
                    tests += PluginTest.Run { it.call(this) }
                } catch (e: InvocationTargetException) {
                    it.reportError(e)
                }
            }

        this::class.functions.filter { it.annotations.any { it is PluginReplace } }
            .forEach {
                it.requireExtensionReceiver<IrBuilderWithScope>(PluginReplace::class)
                it.requireReturnType<IrExpression>(PluginReplace::class)
                val annotation = it.findAnnotation<PluginReplace>()!!

                try {
                    tests += PluginTest.Replace(it.name, annotation.returnType, annotation.args, it.testAnnotations()) {
                        it.call(
                            this@BaseIrPluginTest,
                            this
                        ) as IrExpression
                    }
                } catch (e: InvocationTargetException) {
                    it.reportError(e)
                }
            }

        this::class.functions.filter { it.annotations.any { it is PluginTestReplaceIn } }
            .forEach { func ->
                func.requireExtensionReceiver<IrBuilderWithScope>(PluginReplace::class)
                func.requireReturnType<IrExpression>(PluginReplace::class)
                val annotation = func.findAnnotation<PluginTestReplaceIn>()!!
                try {
                    tests += PluginTest.ReplaceIn(
                        func.name,
                        annotation.expect,
                        annotation.checkType.ifBlank { null },
                        annotation.suffix,
                        annotation.assertMethod,
                        annotation.irValueType.ifBlank { null },
                        func.testAnnotations()
                    ) { func.call(this@BaseIrPluginTest, this) as IrExpression }
                } catch (e: InvocationTargetException) {
                    func.reportError(e)
                }
            }

        this::class.functions.filter { it.annotations.any { it is PluginTestReplaceInAlso } }
            .forEach { func ->
                func.requireExtensionReceiver<IrBuilderWithScope>(PluginReplace::class)
                func.requireReturnType<AlsoTest>(PluginReplace::class)
                val annotation = func.findAnnotation<PluginTestReplaceInAlso>()!!
                try {
                    tests += PluginTest.ReplaceInAlso(
                        func.name,
                        annotation.expect,
                        annotation.checkType.ifBlank { null },
                        annotation.suffix,
                        annotation.assertMethod,
                        annotation.irValueType.ifBlank { null },
                        func.testAnnotations()
                    ) { func.call(this@BaseIrPluginTest, this) as AlsoTest }
                } catch (e: InvocationTargetException) {
                    func.reportError(e)
                }
            }
        return tests
    }

    /**
     * Will be called w/ fake context, can't use [context], [messageCollector], or [testObject].
     */
    open fun TestGenerator.generateTests() {
    }

    private fun makeGeneratedTests(): List<PluginTest> {
        val tests = mutableListOf<PluginTest>()
        val generator = TestGenerator(tests)
        generator.generateTests()
        return tests
    }

    fun allTests() = findAnnotationTests() + makeGeneratedTests()

    fun buildAllTests() {

        val tests = allTests()

        tests.filterIsInstance<PluginTest.Run>().forEach {
            it.body()
        }

        tests.filterIsInstance<PluginTest.Replace>().forEach {
            replaceFunctionWithReturn(it.name) {
                it.body(this)
            }
        }

        tests.filterIsInstance<PluginTest.ReplaceIn>().forEach { func ->
            testObject.functions.single { it.name == Name.identifier(func.name) }.apply {
                transformChildrenVoid(object : IrElementTransformerVoid() {
                    override fun visitCall(expression: IrCall): IrExpression =
                        if (expression.symbol.owner.name == Name.identifier("irValue"))
                            testObject.buildStatement { func.body(this) }
                        else
                            super.visitCall(expression)
                })
                this.patchDeclarationParents(this.parent)
            }
        }

        tests.filterIsInstance<PluginTest.ReplaceInAlso>().forEach { func ->
            testObject.functions.single { it.name == Name.identifier(func.name) }.apply {
                transformChildrenVoid(object : IrElementTransformerVoid() {
                    override fun visitCall(expression: IrCall): IrExpression {
                        return if (expression.symbol.owner.name == Name.identifier("irValue"))
                            this@apply.buildStatement {
                                val builder = func.body(this)
                                stdlib.also(builder.input, body = builder.block)
                            }
                        else
                            super.visitCall(expression)
                    }
                })
                this.patchDeclarationParents(this.parent)
            }
        }
    }

    inline fun IrBuilderWithScope.unitExpr(statements: IrBlockBuilder.() -> Unit) {
        irComposite {
            statements()
            +irReturn(irGetObjectValue(context.irBuiltIns.unitType, context.irBuiltIns.unitClass))
        }
    }

    fun addFunction(name: String, returnType: IrType, body: IrBlockBodyBuilder.() -> Unit) {
        testObject.addFunction(name, returnType).apply {
            withBuilder {
                this@apply.body = irBlockBody {
                    body()
                }
            }
        }
    }

    fun addFunctionWithReturn(name: String, returnType: IrType, body: IrBuilderWithScope.() -> IrExpression) = addFunction(name, returnType) {
        +irReturn(body())
    }

    fun replaceFunction(name: String, body: IrBlockBodyBuilder.() -> Unit) {
        testObject.functions.single { it.name == Name.identifier(name) }.apply {
            withBuilder {
                this@apply.body = irBlockBody {
                    body()
                }
            }
        }
    }

    fun replaceFunctionWithReturn(name: String, body: IrBuilderWithScope.() -> IrExpression) = replaceFunction(name) {
        +irReturn(body())
    }
}