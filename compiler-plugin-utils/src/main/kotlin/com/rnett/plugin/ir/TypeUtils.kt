package com.rnett.plugin.ir

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrTypeParametersContainer
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeArgument
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.superTypes
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.util.substitute
import org.jetbrains.kotlin.ir.util.superTypes
import org.jetbrains.kotlin.utils.addToStdlib.assertedCast
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.jetbrains.kotlin.utils.addToStdlib.safeAs


public fun IrType.hasTypeArgument(index: Int): Boolean = (safeAs<IrSimpleType>()?.arguments?.lastIndex ?: -1) >= index

public fun IrType.typeArgument(index: Int): IrType =
    assertedCast<IrSimpleType> { "$this is not a simple type" }.arguments[index].typeOrNull
        ?: error("Type argument $index of $this is not a type (is it a wildcard?)")

/**
 * Get the supertypes of a type, substituting type parameters for their values where known and not `*`.
 *
 * I.e. if you have `class A<T>: B<T>`, the supertype of `A<Int>` would be `B<Int>`, not `B<T of A>` like with [superTypes].
 */
public fun IrType.supertypesWithSubstitution(): List<IrType> {
    if (this !is IrSimpleType)
        return superTypes()

    val paramMap = this.classifier.owner.cast<IrTypeParametersContainer>().typeParameters
        .map { it.symbol }.zip(this.arguments.map { it.typeOrNull }).filter { it.second != null }
        .toMap() as Map<IrTypeParameterSymbol, IrType>

    return superTypes().map { it.substitute(paramMap) }
}

/**
 * Gets the lowest superclass or this that matches the predicate.
 * Helpful for discovering the type parameters of supertypes.
 */
public inline fun IrType.raiseTo(predicate: (IrType) -> Boolean): IrType =
    raiseToOrNull(predicate) ?: error("Type doesn't match predicate, and no matching supertypes found")

/**
 * Gets the lowest superclass or this that matches the predicate, or null if none do.
 * Helpful for discovering the type parameters of supertypes.
 *
 * Uses [supertypesWithSubstitution]
 */
public inline fun IrType.raiseToOrNull(predicate: (IrType) -> Boolean): IrType? {
    if (predicate(this))
        return this

    val queue = ArrayDeque<IrType>()
    queue += this.supertypesWithSubstitution()

    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        if (predicate(current))
            return current

        queue += current.supertypesWithSubstitution()
    }

    return null
}

/**
 * Gets the lowest superclass or this that has the given classifier.
 * Helpful for discovering the type parameters of supertypes.
 *
 * Uses [supertypesWithSubstitution]
 */
public fun IrType.raiseTo(classifier: IrClassifierSymbol): IrType = raiseToOrNull(classifier)
    ?: error("Type doesn't have classifier $classifier, and none of it's supertypes do")


/**
 * Gets the lowest superclass or this that matches the predicate, or null if none do.
 * Helpful for discovering the type parameters of supertypes.
 *
 * Uses [supertypesWithSubstitution]
 */
public fun IrType.raiseToOrNull(classifier: IrClassifierSymbol): IrType? =
    raiseToOrNull { it.classifierOrNull == classifier }

//fun IrClassifierSymbol.typeWith(vararg arguments: IrTypeArgument): IrSimpleType = typeWith(arguments.toList())

public fun IrClassifierSymbol.typeWith(arguments: List<IrTypeArgument>): IrSimpleType =
    IrSimpleTypeImpl(
        this,
        false,
        arguments,
        emptyList()
    )

public fun IrClass.typeWith(arguments: List<IrTypeArgument>): IrSimpleType = this.symbol.typeWith(arguments)

//fun IrClass.typeWith(vararg arguments: IrTypeArgument) = this.symbol.typeWith(arguments.toList())

