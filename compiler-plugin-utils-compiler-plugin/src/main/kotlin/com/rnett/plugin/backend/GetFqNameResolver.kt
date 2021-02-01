package com.rnett.plugin.backend

import com.rnett.plugin.Names
import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.js.utils.asString
import org.jetbrains.kotlin.ir.builders.IrSingleStatementBuilder
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrDeclarationReference
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionReference
import org.jetbrains.kotlin.ir.expressions.IrPropertyReference
import org.jetbrains.kotlin.ir.util.copyValueArgumentsFrom
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.nameForIrSerialization
import org.jetbrains.kotlin.ir.util.originalProperty
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.safeAs
import java.io.File

class GetFqNameResolver(val context: IrPluginContext, override val messageCollector: MessageCollector) : IrElementTransformerVoidWithContext(),
    FileLoweringPass, WithReporter {

    val getFqName = context.referenceFunctions(Names.getFqName).toSet()
    val getFqNameExt = context.referenceFunctions(Names.getFqNameExt).toSet()

    val nameBuilderFuncs = Names.refFuncs.associate {
        val funcs = context.referenceFunctions(it)
        funcs.single {
            it.owner.valueParameters.firstOrNull()?.name == Name.identifier("ref")
        } to funcs.single {
            it.owner.valueParameters.firstOrNull()?.type?.asString() == "org.jetbrains.kotlin.name.FqName"
        }
    }

    val FqName = context.referenceClass(Names.FqNameClass)!!

    val FqNameConstructor = context.referenceConstructors(Names.FqNameClass).single {
        it.owner.valueParameters.size == 1 && it.owner.valueParameters.first().type.asString() == "kotlin.String"
    }

    override lateinit var file: File
    override lateinit var fileText: String

    override fun lower(irFile: IrFile) {
        file = File(irFile.path)
        fileText = file.readText()
        irFile.transformChildrenVoid()
    }

    fun getFqNameFor(arg: IrExpression): IrExpression? {
        val fqName = when (arg) {
            is IrFunctionReference -> {
                arg.reflectionTarget?.owner?.kotlinFqName
            }
            is IrPropertyReference -> {
                val prop = arg.symbol.owner.originalProperty
                prop.parent.kotlinFqName.child(prop.nameForIrSerialization)
            }
            is IrDeclarationReference -> {
                arg.symbol.owner.safeAs<IrDeclarationParent>()?.kotlinFqName
            }
            else -> null
        } ?: return null

        val builder = IrSingleStatementBuilder(context, currentScope!!.scope, UNDEFINED_OFFSET, UNDEFINED_OFFSET)

        val str = builder.build { irString(fqName.asString()) }

        return builder.build { irCall(FqNameConstructor).apply { putValueArgument(0, str) } }
    }

    override fun visitCall(expression: IrCall): IrExpression {
        if (expression.symbol in getFqName || expression.symbol in getFqNameExt) {
            val arg = if (expression.symbol in getFqName) expression.getValueArgument(0)!! else expression.extensionReceiver!!
            val fqName = getFqNameFor(arg)

            if (fqName == null) {
                expression.reportErrorAt("Can't get FqName for argument.  Are you passing the reference directly?")
            } else {
                return fqName
            }
        } else if (expression.symbol in nameBuilderFuncs) {
            val replaceWith = nameBuilderFuncs.getValue(expression.symbol)

            val fqName = getFqNameFor(expression.getValueArgument(0)!!)
            if (fqName == null) {
                expression.reportErrorAt("Can't get FqName for argument.  Are you passing the reference directly?")
            } else {

                val builder = IrSingleStatementBuilder(context, currentScope!!.scope, UNDEFINED_OFFSET, UNDEFINED_OFFSET)

                return builder.build {
                    irCall(replaceWith).apply {
                        copyValueArgumentsFrom(expression, replaceWith.owner)
                        putValueArgument(0, fqName)
                    }
                }
            }
        }
        return super.visitCall(expression)
    }
}