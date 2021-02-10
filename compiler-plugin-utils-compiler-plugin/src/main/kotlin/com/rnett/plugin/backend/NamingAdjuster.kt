package com.rnett.plugin.backend

import com.rnett.plugin.Names
import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrSingleStatementBuilder
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.IrDelegatingConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.resolve.descriptorUtil.isAncestorOf
import org.jetbrains.kotlin.utils.addToStdlib.lastIsInstanceOrNull

class NamingAdjuster(val context: IrPluginContext, override val messageCollector: MessageCollector) : IrElementTransformerVoidWithContext(),
    FileLoweringPass, WithReporter {

    val Class = context.referenceClass(Names.Class)!!
    val ClassPrimary = context.referenceConstructors(Names.Class).single { it.owner.isPrimary }
    val ClassWithName = context.referenceConstructors(Names.Class).single { it.owner.valueParameters.size == 1 }
    val ClassEmpty = context.referenceConstructors(Names.Class).single { it.owner.valueParameters.isEmpty() }

    val Package = context.referenceClass(Names.Package)!!
    val PackagePrimary = context.referenceConstructors(Names.Package).single { it.owner.isPrimary }
    val PackageWithName = context.referenceConstructors(Names.Package).single { it.owner.valueParameters.size == 1 }
    val PackageEmpty = context.referenceConstructors(Names.Package).single { it.owner.valueParameters.isEmpty() }

    override lateinit var file: IrFile

    override fun lower(irFile: IrFile) {
        file = irFile
        irFile.transformChildrenVoid()
    }

    fun String.asConst() = IrConstImpl.string(UNDEFINED_OFFSET, UNDEFINED_OFFSET, context.irBuiltIns.stringType, this)

    //TODO error message locations are off

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun visitClassNew(declaration: IrClass): IrStatement {
        if (declaration.isSubclassOf(Class.owner) || declaration.isSubclassOf(Package.owner)) {
            if (declaration.isObject || declaration.isAnonymousObject) {

                val namespace = allScopes.dropLast(1).map { it.irElement }.lastIsInstanceOrNull<IrClass>()
                if (namespace == null) {
                    if (!context.moduleDescriptor.isAncestorOf(Class.descriptor, true))
                        declaration.reportErrorAt("Can't create a Class or Package outside of a namespace.  Wrap in RootPackage.")
                } else {

                    declaration.primaryConstructor!!.body!!.transformChildrenVoid(object : IrElementTransformerVoid() {
                        override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall): IrExpression {
                            val builder = IrSingleStatementBuilder(
                                context,
                                currentScope!!.scope,
                                UNDEFINED_OFFSET,
                                UNDEFINED_OFFSET
                            )

                            when (expression.symbol) {
                                ClassWithName -> {
                                    return builder.build {
                                        irDelegatingConstructorCall(ClassPrimary.owner).apply {
                                            putValueArgument(0, expression.getValueArgument(0)!!.deepCopyWithSymbols())
                                            putValueArgument(1, builder.build { irGetObject(namespace.symbol) })
                                        }
                                    }
                                }
                                ClassEmpty -> {
                                    if (declaration.isAnonymousObject || declaration.name.isSpecial)
                                        declaration.reportErrorAt("Can't infer class name for anonymous object, must specify.")

                                    return builder.build {
                                        irDelegatingConstructorCall(ClassPrimary.owner).apply {
                                            putValueArgument(0, declaration.name.asString().asConst())
                                            putValueArgument(1, builder.build { irGetObject(namespace.symbol) })
                                        }
                                    }
                                }
                                PackageWithName -> {
                                    return builder.build {
                                        irDelegatingConstructorCall(PackagePrimary.owner).apply {
                                            putValueArgument(0, expression.getValueArgument(0)!!.deepCopyWithSymbols())
                                            putValueArgument(1, builder.build { irGetObject(namespace.symbol) })
                                        }
                                    }
                                }
                                PackageEmpty -> {
                                    if (declaration.isAnonymousObject || declaration.name.isSpecial)
                                        declaration.reportErrorAt("Can't infer class name for anonymous object, must specify.")

                                    return builder.build {
                                        irDelegatingConstructorCall(PackagePrimary.owner).apply {
                                            putValueArgument(0, declaration.name.asString().toLowerCase().asConst())
                                            putValueArgument(1, builder.build { irGetObject(namespace.symbol) })
                                        }
                                    }
                                }
                            }
                            return super.visitDelegatingConstructorCall(expression)
                        }
                    })
                }
            } else if (declaration.symbol != Class && declaration.symbol != Package) {
                declaration.reportErrorAt("Can't subclass Class or Package, they are only usable with objects")
            }
        }

        return super.visitClassNew(declaration)
    }
}