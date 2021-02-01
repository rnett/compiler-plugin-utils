package com.rnett.plugin.stdlib

import com.rnett.plugin.*
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.buildStatement
import org.jetbrains.kotlin.ir.builders.declarations.addExtensionReceiver
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irExprBody
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith

class StdlibBuilders(private val builder: IrBuilderWithScope, override val context: IrPluginContext) : HasContext {

    fun to(
        first: IrExpression,
        firstType: IrType,
        second: IrExpression,
        secondType: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = builder.buildStatement(startOffset, endOffset) {
        irCall(Kotlin.to())
            .withTypeArguments(firstType, secondType)
            .withExtensionReceiver(first)
            .withValueArguments(second)
    }

    fun to(
        first: IrExpression,
        second: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = builder.buildStatement(startOffset, endOffset) {
        irCall(Kotlin.to())
            .withExtensionReceiver(first)
            .withValueArguments(second)
    }

    inner class CollectionsBuilders {
        fun listOf(
            elementType: IrType,
            items: Iterable<IrExpression>,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
        ) = builder.buildStatement(startOffset, endOffset) {
            irCall(Kotlin.Collections.listOfVararg())
                .withValueArguments(irVararg(elementType, items))
        }

        fun setOf(
            elementType: IrType,
            items: Iterable<IrExpression>,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
        ) = builder.buildStatement(startOffset, endOffset) {
            irCall(Kotlin.Collections.setOfVararg())
                .withTypeArguments(elementType)
                .withValueArguments(irVararg(elementType, items))
        }

        fun mapOf(
            keyType: IrType,
            valueType: IrType,
            items: Map<IrExpression, IrExpression>,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
        ) = builder.buildStatement(startOffset, endOffset) {
            irCall(Kotlin.Collections.mapOfVararg())
                .withTypeArguments(keyType, valueType)
                .withValueArguments(
                    irVararg(
                        Kotlin.Pair().typeWith(keyType, valueType),
                        items.map { (k, v) -> to(k, keyType, v, valueType) }
                    ))
        }

        fun mutableListOf(
            elementType: IrType,
            items: Iterable<IrExpression>,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
        ) = builder.buildStatement(startOffset, endOffset) {
            irCall(Kotlin.Collections.mutableListOfVararg(), Kotlin.Collections.MutableList().typeWith(elementType))
                .withTypeArguments(elementType)
                .withValueArguments(irVararg(elementType, items))
        }

        fun mutableSetOf(
            elementType: IrType,
            items: Iterable<IrExpression>,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
        ) = builder.buildStatement(startOffset, endOffset) {
            irCall(Kotlin.Collections.mutableSetOfVararg(), Kotlin.Collections.MutableSet().typeWith(elementType))
                .withTypeArguments(elementType)
                .withValueArguments(irVararg(elementType, items))
        }

        fun mutableMapOf(
            keyType: IrType,
            valueType: IrType,
            items: Map<IrExpression, IrExpression>,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
        ) = builder.buildStatement(startOffset, endOffset) {
            irCall(Kotlin.Collections.mutableMapOfVararg(), Kotlin.Collections.MutableMap().typeWith(keyType, valueType))
                .withTypeArguments(keyType, valueType)
                .withValueArguments(
                    irVararg(
                        Kotlin.Pair().typeWith(keyType, valueType),
                        items.map { (k, v) ->
                            irCall(Kotlin.to(), Kotlin.Pair().typeWith(keyType, valueType))
                                .withTypeArguments(keyType, valueType)
                                .withExtensionReceiver(k)
                                .withValueArguments(v)
                        }.toList()
                    )
                )
        }

        fun toList(
            expr: IrExpression,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
        ) = builder.buildStatement(startOffset, endOffset) {
            irCall(Kotlin.Collections.toList())
                .withExtensionReceiver(expr)
        }

        fun toMutableList(
            expr: IrExpression,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
        ) = builder.buildStatement(startOffset, endOffset) {
            irCall(Kotlin.Collections.toMutableList())
                .withExtensionReceiver(expr)
        }

        fun toListForMap(
            expr: IrExpression,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
        ) = builder.buildStatement(startOffset, endOffset) {
            irCall(Kotlin.Collections.toListForMap())
                .withExtensionReceiver(expr)
        }

        fun toMapForIterable(
            list: IrExpression,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
        ) = builder.buildStatement(startOffset, endOffset) {
            irCall(Kotlin.Collections.toMapForIterable())
                .withExtensionReceiver(list)
        }

        fun collectionToTypedArray(
            collection: IrExpression,
            elementType: IrType,
            startOffset: Int = UNDEFINED_OFFSET,
            endOffset: Int = UNDEFINED_OFFSET
        ) = builder.buildStatement(startOffset, endOffset) {
            irCall(Kotlin.Collections.collectionToTypedArray(), Kotlin.Array().typeWith(elementType))
                .withExtensionReceiver(collection)
                .withTypeArguments(elementType)
        }

        //TODO toSet, toMutableSet/Map

        inner class MapBuildres {
            fun size(
                map: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.Map.size().owner.getter!!)
                    .withDispatchReceiver(map)
            }

            fun isEmpty(
                map: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.Map.isEmpty())
                    .withDispatchReceiver(map)
            }

            fun containsKey(
                map: IrExpression, key: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.Map.containsKey())
                    .withDispatchReceiver(map)
                    .withValueArguments(key)
            }

            fun containsValue(
                map: IrExpression, value: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.Map.containsValue())
                    .withDispatchReceiver(map)
                    .withValueArguments(value)
            }

            fun get(
                map: IrExpression, key: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.Map.get())
                    .withDispatchReceiver(map)
                    .withValueArguments(key)
            }

            fun keys(
                map: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.Map.keys().owner.getter!!)
                    .withDispatchReceiver(map)
            }

            fun values(
                map: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.Map.values().owner.getter!!)
                    .withDispatchReceiver(map)
            }

            fun entries(
                map: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.Map.entries().owner.getter!!)
                    .withDispatchReceiver(map)
            }
        }

        val Map = MapBuildres()

        inner class MutableMapBuilders {
            val Map = Kotlin.Collections.Map

            fun put(
                map: IrExpression,
                key: IrExpression,
                value: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.MutableMap.put())
                    .withDispatchReceiver(map)
                    .withValueArguments(key, value)
            }

            fun remove(
                map: IrExpression,
                key: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.MutableMap.remove())
                    .withDispatchReceiver(map)
                    .withValueArguments(key)
            }

            fun putAll(
                map: IrExpression,
                items: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.MutableMap.putAll())
                    .withDispatchReceiver(map)
                    .withValueArguments(items)
            }

            fun putAllIterable(
                map: IrExpression,
                items: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.MutableMap.putAllIterable())
                    .withExtensionReceiver(map)
                    .withValueArguments(items)
            }

            fun putAll(
                map: IrExpression,
                itemsMap: Map<IrExpression, IrExpression>,
                itemsKeyType: IrType,
                itemsValueType: IrType,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = putAll(map, mapOf(itemsKeyType, itemsValueType, itemsMap), startOffset, endOffset)

            fun clear(
                map: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.MutableMap.clear())
                    .withDispatchReceiver(map)
            }
        }

        val MutableMap = MutableMapBuilders()

        inner class SetBuilders {
            fun size(
                set: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.Set.size().owner.getter!!)
                    .withDispatchReceiver(set)
            }

            fun isEmpty(
                set: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.Set.isEmpty())
                    .withDispatchReceiver(set)
            }

            fun contains(
                set: IrExpression, item: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.Set.contains())
                    .withDispatchReceiver(set)
                    .withValueArguments(item)
            }

            fun iterator(
                set: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.Set.iterator())
                    .withDispatchReceiver(set)
            }

            fun containsAll(
                set: IrExpression, items: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.Set.containsAll())
                    .withDispatchReceiver(set)
                    .withValueArguments(items)
            }

            fun containsAll(
                set: IrExpression,
                elementType: IrType,
                items: Iterable<IrExpression>,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = containsAll(set, setOf(elementType, items))

        }

        val Set = SetBuilders()

        inner class MutableSetBuilders {
            val Set = Kotlin.Collections.Set

            fun add(
                set: IrExpression,
                item: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.MutableSet.add())
                    .withDispatchReceiver(set)
                    .withValueArguments(item)
            }

            fun remove(
                set: IrExpression,
                item: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.MutableSet.remove())
                    .withDispatchReceiver(set)
                    .withValueArguments(item)
            }

            fun addAll(
                set: IrExpression,
                items: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.MutableSet.addAll())
                    .withDispatchReceiver(set)
                    .withValueArguments(items)
            }

            fun addAll(
                set: IrExpression,
                items: Iterable<IrExpression>,
                itemType: IrType,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = addAll(set, setOf(itemType, items), startOffset, endOffset)

            fun removeAll(
                set: IrExpression,
                items: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.MutableSet.removeAll())
                    .withDispatchReceiver(set)
                    .withValueArguments(items)
            }

            fun removeAll(
                set: IrExpression,
                items: Iterable<IrExpression>,
                itemType: IrType,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = removeAll(set, setOf(itemType, items), startOffset, endOffset)

            fun clear(
                set: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.MutableSet.clear())
                    .withDispatchReceiver(set)
            }

        }

        val MutableSet = MutableSetBuilders()

        inner class ListBuilders {
            fun size(
                list: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.List.size().owner.getter!!)
                    .withDispatchReceiver(list)
            }

            fun isEmpty(
                list: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.List.isEmpty())
                    .withDispatchReceiver(list)
            }

            fun contains(
                list: IrExpression, item: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.List.contains())
                    .withDispatchReceiver(list)
                    .withValueArguments(item)
            }

            fun iterator(
                list: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.List.iterator())
                    .withDispatchReceiver(list)
            }

            fun get(
                list: IrExpression, index: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.List.get())
                    .withDispatchReceiver(list)
                    .withValueArguments(index)
            }

            fun indexOf(
                list: IrExpression, item: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.List.indexOf())
                    .withDispatchReceiver(list)
                    .withValueArguments(item)
            }
        }

        val List = ListBuilders()

        inner class MutableListBuilders {
            val List = Kotlin.Collections.List

            fun add(
                list: IrExpression,
                item: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.MutableList.add())
                    .withDispatchReceiver(list)
                    .withValueArguments(item)
            }

            fun remove(
                list: IrExpression,
                item: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.MutableList.remove())
                    .withDispatchReceiver(list)
                    .withValueArguments(item)
            }

            fun addAll(
                list: IrExpression,
                items: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.MutableList.addAll())
                    .withDispatchReceiver(list)
                    .withValueArguments(items)
            }

            fun addAll(
                list: IrExpression,
                items: Iterable<IrExpression>,
                itemType: IrType,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = addAll(list, listOf(itemType, items), startOffset, endOffset)

            fun removeAll(
                list: IrExpression,
                items: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.MutableList.removeAll())
                    .withDispatchReceiver(list)
                    .withValueArguments(items)
            }

            fun removeAll(
                list: IrExpression,
                items: Iterable<IrExpression>,
                itemType: IrType,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = removeAll(list, setOf(itemType, items), startOffset, endOffset)

            fun clear(
                list: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.MutableList.clear())
                    .withDispatchReceiver(list)
            }

            fun set(
                list: IrExpression,
                item: IrExpression,
                startOffset: Int = UNDEFINED_OFFSET,
                endOffset: Int = UNDEFINED_OFFSET
            ) = builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Collections.MutableList.set())
                    .withDispatchReceiver(list)
                    .withValueArguments(item)
            }

        }

        val MutableList = MutableListBuilders()

    }

    val collections = CollectionsBuilders()

    inner class ReflectBuilders {
        fun typeOf(type: IrType, startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET) =
            builder.buildStatement(startOffset, endOffset) {
                irCall(Kotlin.Reflect.typeOf(), Kotlin.Reflect.KType().typeWith())
                    .withTypeArguments(type)
            }
    }

    val reflect = ReflectBuilders()

    fun let(
        expr: IrExpression,
        returnType: IrType,
        body: IrBlockBodyBuilder.(IrValueParameter) -> Unit,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) =
        builder.buildStatement(startOffset, endOffset) {
            irCall(Kotlin.let()).apply {
                extensionReceiver = expr
                putTypeArgument(0, expr.type)
                putTypeArgument(1, returnType)
                putValueArgument(0, lambdaArgument(
                    buildLambda(returnType) {
                        val param = addValueParameter("it", expr.type)
                        this.body = irBlockBody {
                            body(param)
                        }
                    }
                ))
            }
        }

    fun let(expr: IrExpression, body: (IrExpression) -> IrExpression, startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET) =
        builder.buildStatement(startOffset, endOffset) {
            irCall(Kotlin.let()).apply {
                extensionReceiver = expr
                putTypeArgument(0, expr.type)
                putValueArgument(0, lambdaArgument(
                    buildLambda {
                        withBuilder {
                            val param = addValueParameter("it", expr.type)
                            val ret = body(irGet(param))
                            this@buildLambda.body = irExprBody(ret)
                            this@buildLambda.returnType = ret.type
                            this@apply.putTypeArgument(1, ret.type)
                        }
                    }
                ))
            }
        }

    fun run(
        expr: IrExpression,
        returnType: IrType,
        body: IrBlockBodyBuilder.(IrValueParameter) -> Unit,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) =
        builder.buildStatement(startOffset, endOffset) {
            irCall(Kotlin.run()).apply {
                extensionReceiver = expr
                putTypeArgument(0, expr.type)
                putTypeArgument(1, returnType)
                putValueArgument(0, lambdaArgument(
                    buildLambda(returnType) {
                        val param = addExtensionReceiver(expr.type)
                        this.body = irBlockBody {
                            body(param)
                        }
                    }
                ))
            }
        }

    fun run(expr: IrExpression, body: (IrExpression) -> IrExpression, startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET) =
        builder.buildStatement(startOffset, endOffset) {
            irCall(Kotlin.run()).apply {
                extensionReceiver = expr
                putTypeArgument(0, expr.type)
                putValueArgument(0, lambdaArgument(
                    buildLambda {
                        withBuilder {
                            val param = addExtensionReceiver(expr.type)
                            val ret = body(irGet(param))
                            this@buildLambda.body = irExprBody(ret)
                            this@buildLambda.returnType = ret.type
                            this@apply.putTypeArgument(1, ret.type)
                        }
                    }
                ))
            }
        }

    fun with(
        expr: IrExpression,
        returnType: IrType,
        body: IrBlockBodyBuilder.(IrValueParameter) -> Unit,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) =
        builder.buildStatement(startOffset, endOffset) {
            irCall(Kotlin.with()).apply {
                putTypeArgument(0, expr.type)
                putTypeArgument(1, returnType)

                putValueArgument(0, expr)
                putValueArgument(1, lambdaArgument(
                    buildLambda(returnType) {
                        val param = addExtensionReceiver(expr.type)
                        this.body = irBlockBody {
                            body(param)
                        }
                    }
                ))
            }
        }

    fun with(expr: IrExpression, body: (IrExpression) -> IrExpression, startOffset: Int = UNDEFINED_OFFSET, endOffset: Int = UNDEFINED_OFFSET) =
        builder.buildStatement(startOffset, endOffset) {
            irCall(Kotlin.with()).apply {
                putTypeArgument(0, expr.type)

                putValueArgument(0, expr)
                putValueArgument(1, lambdaArgument(
                    buildLambda {
                        withBuilder {
                            val param = addExtensionReceiver(expr.type)
                            val ret = body(irGet(param))
                            this@buildLambda.body = irExprBody(ret)
                            this@buildLambda.returnType = ret.type
                            this@apply.putTypeArgument(1, ret.type)
                        }
                    }
                ))
            }
        }

    fun withUnit(
        expr: IrExpression,
        blockBodyBuilder: IrBlockBodyBuilder.(IrValueParameter) -> Unit,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = with(expr, context.irBuiltIns.unitType, blockBodyBuilder, startOffset, endOffset)

    fun also(
        expr: IrExpression,
        body: IrBlockBodyBuilder.(IrValueParameter) -> Unit,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) =
        builder.buildStatement(startOffset, endOffset) {
            irCall(Kotlin.also(), expr.type).apply {
                extensionReceiver = expr
                putTypeArgument(0, expr.type)
                putValueArgument(0, lambdaArgument(
                    buildLambda(context.irBuiltIns.unitType) {
                        val param = addValueParameter("it", expr.type)
                        this.body = irBlockBody {
                            body(param)
                        }
                    }
                ))
            }
        }

    fun apply(
        expr: IrExpression,
        body: IrBlockBodyBuilder.(IrValueParameter) -> Unit,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) =
        builder.buildStatement(startOffset, endOffset) {
            irCall(Kotlin.apply(), expr.type).apply {
                extensionReceiver = expr
                putTypeArgument(0, expr.type)
                putValueArgument(0, lambdaArgument(
                    buildLambda(context.irBuiltIns.unitType) {
                        val param = addExtensionReceiver(expr.type)
                        this.body = irBlockBody {
                            body(param)
                        }
                    }
                ))
            }
        }
}