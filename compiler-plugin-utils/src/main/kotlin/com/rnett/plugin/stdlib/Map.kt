package com.rnett.plugin.stdlib

import com.rnett.plugin.ir.irJsExprBody
import com.rnett.plugin.ir.raiseToOrNull
import com.rnett.plugin.ir.withDispatchReceiver
import com.rnett.plugin.ir.withExtensionReceiver
import com.rnett.plugin.ir.withValueArguments
import com.rnett.plugin.naming.ClassRef
import com.rnett.plugin.naming.isClassifierOf
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

public open class MapBuilders(
    protected val collections: CollectionsBuilders,
    builder: IrBuilderWithScope,
    context: IrPluginContext,
    type: ClassRef = Kotlin.Collections.Map
) :
    TypedMethodBuilder(type, builder, context) {
    public fun size(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrFunctionAccessExpression = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.Map.size().owner.getter!!)
            .withDispatchReceiver(receiver.checkType())
    }

    public fun isEmpty(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.Map.isEmpty())
            .withDispatchReceiver(receiver.checkType())
    }

    public fun containsKey(
        receiver: IrExpression, key: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.Map.containsKey())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(key)
    }

    public fun containsValue(
        receiver: IrExpression, value: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.Map.containsValue())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(value)
    }

    public fun get(
        receiver: IrExpression, key: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.Map.get())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(key)
    }

    public fun keys(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrFunctionAccessExpression = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.Map.keys().owner.getter!!)
            .withDispatchReceiver(receiver.checkType())
    }

    public fun values(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrFunctionAccessExpression = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.Map.values().owner.getter!!)
            .withDispatchReceiver(receiver.checkType())
    }

    public fun entries(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrFunctionAccessExpression = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.Map.entries().owner.getter!!)
            .withDispatchReceiver(receiver.checkType())
    }

    public fun toList(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.mapToList())
            .withExtensionReceiver(receiver.checkType())
    }

    public fun getValue(
        receiver: IrExpression,
        key: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.mapGetValue())
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(key)
    }

    public fun getOrDefault(
        receiver: IrExpression,
        key: IrExpression,
        default: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.mapGetOrDefault())
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(key, default)
    }

    public fun getOrElse(
        receiver: IrExpression,
        key: IrExpression,
        valueType: IrType = receiver.type.raiseToOrNull { it.isClassifierOf(Kotlin.Collections.Map) }
            ?.safeAs<IrSimpleType>()?.arguments?.get(1)?.typeOrNull
            ?: error("Can't auto-detect value type, must specify"),
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        otherwise: IrBlockBodyBuilder.() -> Unit
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.mapGetOrElse())
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(key, lambdaArgument(buildLambda(valueType) {
                withBuilder {
                    body = irBlockBody(body = otherwise)
                }
            }))
    }

    public fun getOrElseExpr(
        receiver: IrExpression,
        key: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        otherwise: IrBuilderWithScope.() -> IrExpression
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.mapGetOrElse())
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(key, lambdaArgument(buildLambda(null) {
                withBuilder {
                    body = irJsExprBody(otherwise().also { this@buildLambda.returnType = it.type })
                }
            }))
    }

    public fun plusIterablePairs(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.mapPlusIterablePairs)
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(other)
    }

    public fun plusMap(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.mapPlusMap)
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(other)
    }

    public fun plusElementPair(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.mapPlusElementPair)
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(other)
    }

    public fun plusElementPairOf(
        receiver: IrExpression,
        key: IrExpression,
        value: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = plusElementPair(receiver, collections.stdlib.to(key, value), startOffset, endOffset)

    public fun minusIterableKeys(
        receiver: IrExpression,
        keys: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.mapMinusIterableKeys)
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(keys)
    }

    public fun minusKey(
        receiver: IrExpression,
        other: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.mapMinusKey)
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(other)
    }
}

public open class MutableMapBuilders(
    collections: CollectionsBuilders,
    builder: IrBuilderWithScope,
    context: IrPluginContext
) :
    MapBuilders(collections, builder, context, Kotlin.Collections.MutableMap) {
    public fun put(
        receiver: IrExpression,
        key: IrExpression,
        value: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableMap.put())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(key, value)
    }

    public fun remove(
        receiver: IrExpression,
        key: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableMap.remove())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(key)
    }

    public fun putAll(
        receiver: IrExpression,
        items: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableMap.putAll())
            .withDispatchReceiver(receiver.checkType())
            .withValueArguments(items)
    }

    public fun putAllIterable(
        receiver: IrExpression,
        items: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableMap.putAllIterable())
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(items)
    }

    public fun putAll(
        receiver: IrExpression,
        itemsMap: Map<IrExpression, IrExpression>,
        itemsKeyType: IrType,
        itemsValueType: IrType,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall =
        putAll(receiver.checkType(), collections.mapOf(itemsKeyType, itemsValueType, itemsMap), startOffset, endOffset)

    public fun clear(
        receiver: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.MutableMap.clear())
            .withDispatchReceiver(receiver.checkType())
    }

    public fun getOrPut(
        receiver: IrExpression,
        key: IrExpression,
        valueType: IrType = receiver.type.raiseToOrNull { it.isClassifierOf(Kotlin.Collections.MutableMap) }
            ?.safeAs<IrSimpleType>()?.arguments?.get(1)?.typeOrNull
            ?: error("Can't auto-detect value type, must specify"),
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        otherwise: IrBlockBodyBuilder.() -> Unit
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.mutableMapGetOrPut())
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(key, lambdaArgument(buildLambda(valueType) {
                withBuilder {
                    body = irBlockBody(body = otherwise)
                }
            }))
    }

    public fun getOrPutExpr(
        receiver: IrExpression,
        key: IrExpression,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET,
        otherwise: IrBuilderWithScope.() -> IrExpression
    ): IrCall = buildStatement(startOffset, endOffset) {
        irCall(Kotlin.Collections.mutableMapGetOrPut())
            .withExtensionReceiver(receiver.checkType())
            .withValueArguments(key, lambdaArgument(buildLambda(null) {
                withBuilder {
                    body = irJsExprBody(otherwise().also { this@buildLambda.returnType = it.type })
                }
            }))
    }

    //TODO not in stdlib yet

//    public fun getOrPutNullable(
//        receiver: IrExpression,
//        key: IrExpression,
//        valueType: IrType = receiver.type.raiseToOrNull { it.isClassifierOf(Kotlin.Collections.MutableMap) }
//            ?.safeAs<IrSimpleType>()?.arguments?.get(1)?.typeOrNull ?: error("Can't auto-detect value type, must specify"),
//        startOffset: Int = UNDEFINED_OFFSET,
//        endOffset: Int = UNDEFINED_OFFSET,
//        otherwise: IrBlockBodyBuilder.() -> Unit
//    ) = buildStatement(startOffset, endOffset) {
//        irCall(Kotlin.Collections.mutableMapGetOrPutNullable())
//            .withExtensionReceiver(receiver.checkType())
//            .withValueArguments(key, lambdaArgument(buildLambda(valueType) {
//                withBuilder {
//                    body = irBlockBody(body = otherwise)
//                }
//            }))
//    }
//
//    public fun getOrPutNullableExpr(
//        receiver: IrExpression,
//        key: IrExpression,
//        startOffset: Int = UNDEFINED_OFFSET,
//        endOffset: Int = UNDEFINED_OFFSET,
//        otherwise: IrBuilderWithScope.() -> IrExpression
//    ) = buildStatement(startOffset, endOffset) {
//        irCall(Kotlin.Collections.mutableMapGetOrPutNullable())
//            .withExtensionReceiver(receiver.checkType())
//            .withValueArguments(key, lambdaArgument(buildLambda(null) {
//                withBuilder {
//                    body = irJsExprBody(otherwise().also { this@buildLambda.returnType = it.type })
//                }
//            }))
//    }
}