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

abstract class BaseIrPluginTest : HasContext {

    @PublishedApi
    internal var testContext: PluginTestContext? = null

    override val context: IrPluginContext get() = testContext?.context ?: error("Context has not been initialized yet")
    private val messageCollector: MessageCollector get() = testContext?.messageCollector ?: error("Context has not been initialized yet")
    protected val testObject: IrClass get() = testContext?.testObject ?: error("Context has not been initialized yet")

    @TestFactory
    fun Tests() = MakeTests(this::class)

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

    fun buildAllTests() {

        Kotlin.apply { }

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
                    it.call(this)
                } catch (e: InvocationTargetException) {
                    it.reportError(e)
                }
            }

        this::class.functions.filter { it.annotations.any { it is PluginReplace } }
            .forEach {

                try {
                    it.requireExtensionReceiver<IrBuilderWithScope>(PluginReplace::class)
                    it.requireReturnType<IrExpression>(PluginReplace::class)
                    replaceFunctionWithReturn(it.name) {
                        it.call(this@BaseIrPluginTest, this) as IrExpression
                    }

                } catch (e: InvocationTargetException) {
                    it.reportError(e)
                }
            }

        this::class.functions.filter { it.annotations.any { it is PluginTestReplaceIn } }
            .forEach { func ->
                func.requireExtensionReceiver<IrBuilderWithScope>(PluginReplace::class)
                func.requireReturnType<IrExpression>(PluginReplace::class)
                try {
                    testObject.functions.single { it.name == Name.identifier(func.name) }.apply {
                        transformChildrenVoid(object : IrElementTransformerVoid() {
                            override fun visitCall(expression: IrCall): IrExpression =
                                if (expression.symbol.owner.name == Name.identifier("irValue"))
                                    testObject.buildStatement { func.call(this@BaseIrPluginTest, this) as IrExpression }
                                else
                                    super.visitCall(expression)
                        })
                        this.patchDeclarationParents(this.parent)
                    }
                } catch (e: InvocationTargetException) {
                    func.reportError(e)
                }
            }

        this::class.functions.filter { it.annotations.any { it is PluginTestReplaceInAlso } }
            .forEach { func ->
                func.requireExtensionReceiver<IrBuilderWithScope>(PluginReplace::class)
                func.requireReturnType<AlsoTest>(PluginReplace::class)
                try {
                    testObject.functions.single { it.name == Name.identifier(func.name) }.apply {
                        transformChildrenVoid(object : IrElementTransformerVoid() {
                            override fun visitCall(expression: IrCall): IrExpression {
                                return if (expression.symbol.owner.name == Name.identifier("irValue"))
                                    this@apply.buildStatement {
                                        val builder = func.call(this@BaseIrPluginTest, this) as AlsoTest
                                        stdlib.also(builder.input, builder.block)
                                    }
                                else
                                    super.visitCall(expression)
                            }
                        })
                        this.patchDeclarationParents(this.parent)
                    }
                } catch (e: InvocationTargetException) {
                    func.reportError(e)
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