package com.rnett.plugin.ir

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrEnumEntrySymbol
import org.jetbrains.kotlin.ir.symbols.IrFieldSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrLocalDelegatedPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrReturnableBlockSymbol
import org.jetbrains.kotlin.ir.symbols.IrScriptSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeAliasSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.symbols.IrVariableSymbol
import org.jetbrains.kotlin.ir.util.DeepCopyIrTreeWithSymbols
import org.jetbrains.kotlin.ir.util.SymbolRemapper
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@PublishedApi
internal class ReferenceSymbolRemapper(private val referenceRemapper: ReferenceRemapper, private val base: SymbolRemapper) : SymbolRemapper by base {
    override fun getReferencedClass(symbol: IrClassSymbol): IrClassSymbol =
        referenceRemapper.getReferencedClass(symbol) ?: base.getReferencedClass(symbol)

    override fun getReferencedScript(symbol: IrScriptSymbol): IrScriptSymbol =
        referenceRemapper.getReferencedScript(symbol) ?: base.getReferencedScript(symbol)

    override fun getReferencedClassOrNull(symbol: IrClassSymbol?): IrClassSymbol? =
        referenceRemapper.getReferencedClassOrNull(symbol) ?: base.getReferencedClassOrNull(symbol)

    override fun getReferencedEnumEntry(symbol: IrEnumEntrySymbol): IrEnumEntrySymbol =
        referenceRemapper.getReferencedEnumEntry(symbol) ?: base.getReferencedEnumEntry(symbol)

    override fun getReferencedVariable(symbol: IrVariableSymbol): IrVariableSymbol =
        referenceRemapper.getReferencedVariable(symbol) ?: base.getReferencedVariable(symbol)

    override fun getReferencedLocalDelegatedProperty(symbol: IrLocalDelegatedPropertySymbol): IrLocalDelegatedPropertySymbol =
        referenceRemapper.getReferencedLocalDelegatedProperty(symbol) ?: base.getReferencedLocalDelegatedProperty(symbol)

    override fun getReferencedField(symbol: IrFieldSymbol): IrFieldSymbol =
        referenceRemapper.getReferencedField(symbol) ?: base.getReferencedField(symbol)

    override fun getReferencedConstructor(symbol: IrConstructorSymbol): IrConstructorSymbol =
        referenceRemapper.getReferencedConstructor(symbol) ?: base.getReferencedConstructor(symbol)

    override fun getReferencedValue(symbol: IrValueSymbol): IrValueSymbol =
        referenceRemapper.getReferencedValue(symbol) ?: base.getReferencedValue(symbol)

    override fun getReferencedFunction(symbol: IrFunctionSymbol): IrFunctionSymbol =
        referenceRemapper.getReferencedFunction(symbol) ?: base.getReferencedFunction(symbol)

    override fun getReferencedProperty(symbol: IrPropertySymbol): IrPropertySymbol =
        referenceRemapper.getReferencedProperty(symbol) ?: base.getReferencedProperty(symbol)

    override fun getReferencedSimpleFunction(symbol: IrSimpleFunctionSymbol): IrSimpleFunctionSymbol =
        referenceRemapper.getReferencedSimpleFunction(symbol) ?: base.getReferencedSimpleFunction(symbol)

    override fun getReferencedReturnableBlock(symbol: IrReturnableBlockSymbol): IrReturnableBlockSymbol =
        referenceRemapper.getReferencedReturnableBlock(symbol) ?: base.getReferencedReturnableBlock(symbol)

    override fun getReferencedClassifier(symbol: IrClassifierSymbol): IrClassifierSymbol =
        referenceRemapper.getReferencedClassifier(symbol) ?: base.getReferencedClassifier(symbol)

    override fun getReferencedTypeAlias(symbol: IrTypeAliasSymbol): IrTypeAliasSymbol =
        referenceRemapper.getReferencedTypeAlias(symbol) ?: base.getReferencedTypeAlias(symbol)
}

/**
 * A symbol remapper that only replaces references to symbols.  Returning `null` will deep copy the symbol.
 */
public interface ReferenceRemapper {
    public fun getReferencedClass(symbol: IrClassSymbol): IrClassSymbol? = null
    public fun getReferencedScript(symbol: IrScriptSymbol): IrScriptSymbol? = null
    public fun getReferencedClassOrNull(symbol: IrClassSymbol?): IrClassSymbol? = null
    public fun getReferencedEnumEntry(symbol: IrEnumEntrySymbol): IrEnumEntrySymbol? = null
    public fun getReferencedVariable(symbol: IrVariableSymbol): IrVariableSymbol? = null
    public fun getReferencedLocalDelegatedProperty(symbol: IrLocalDelegatedPropertySymbol): IrLocalDelegatedPropertySymbol? = null
    public fun getReferencedField(symbol: IrFieldSymbol): IrFieldSymbol? = null
    public fun getReferencedConstructor(symbol: IrConstructorSymbol): IrConstructorSymbol? = null
    public fun getReferencedValue(symbol: IrValueSymbol): IrValueSymbol? = null
    public fun getReferencedFunction(symbol: IrFunctionSymbol): IrFunctionSymbol? = null
    public fun getReferencedProperty(symbol: IrPropertySymbol): IrPropertySymbol? = null
    public fun getReferencedSimpleFunction(symbol: IrSimpleFunctionSymbol): IrSimpleFunctionSymbol? = null
    public fun getReferencedReturnableBlock(symbol: IrReturnableBlockSymbol): IrReturnableBlockSymbol? = null
    public fun getReferencedClassifier(symbol: IrClassifierSymbol): IrClassifierSymbol? = null
    public fun getReferencedTypeAlias(symbol: IrTypeAliasSymbol): IrTypeAliasSymbol? = null
}

/**
 * Deep copy and replace references to symbols
 */
public inline fun <reified T : IrElement> T.deepCopyAndRemapReferences(
    initialParent: IrDeclarationParent? = null,
    referenceRemapper: ReferenceRemapper
): T = deepCopyWithSymbols(initialParent) { symbolRemapper, typeRemapper ->
    DeepCopyIrTreeWithSymbols(ReferenceSymbolRemapper(referenceRemapper, symbolRemapper), typeRemapper)
}

/**
 * Remap value symbols.  If given a [IrVariableSymbol], must return a [IrVariableSymbol].  Returning `null` will deep copy the symbol.
 */
public fun interface ValueRemapper : ReferenceRemapper {
    public fun remapValue(symbol: IrValueSymbol): IrValueSymbol?
}

/**
 * Deep copy and replace references to values
 */
public inline fun <reified T : IrElement> T.deepCopyAndRemapValues(
    initialParent: IrDeclarationParent? = null,
    referenceRemapper: ValueRemapper
): T = deepCopyAndRemapReferences(initialParent, object : ReferenceRemapper {
    override fun getReferencedValue(symbol: IrValueSymbol): IrValueSymbol? = referenceRemapper.remapValue(symbol)
    override fun getReferencedVariable(symbol: IrVariableSymbol): IrVariableSymbol? = referenceRemapper.remapValue(symbol) as IrVariableSymbol?
})

/**
 * Remap references to functions.  Returning `null` will deep copy the symbol.
 */
public fun interface FunctionRemapper {
    public fun remapValue(symbol: IrSimpleFunctionSymbol): IrSimpleFunctionSymbol?
}

/**
 * Deep copy and replace references to values.
 */
public inline fun <reified T : IrElement> T.deepCopyAndRemapFunctions(
    initialParent: IrDeclarationParent? = null,
    referenceRemapper: FunctionRemapper
): T = deepCopyAndRemapReferences(initialParent, object : ReferenceRemapper {
    override fun getReferencedFunction(symbol: IrFunctionSymbol): IrFunctionSymbol? =
        if (symbol is IrSimpleFunctionSymbol) referenceRemapper.remapValue(symbol) else symbol

    override fun getReferencedSimpleFunction(symbol: IrSimpleFunctionSymbol): IrSimpleFunctionSymbol? =
        referenceRemapper.remapValue(symbol)
})

/**
 * Build a list of symbol replacements
 */
public inline fun ReferenceReplacements(builder: ReferenceReplacements.Builder.() -> Unit): ReferenceReplacements {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }
    return ReferenceReplacements.Builder().apply(builder).build()
}

/**
 * A list of symbol replacements, providing a [ReferenceRemapper].
 */
public class ReferenceReplacements private constructor(private val map: Map<IrSymbol, IrSymbol>) : ReferenceRemapper {
    public operator fun contains(symbol: IrSymbol): Boolean = symbol in map
    public operator fun <T : IrSymbol> get(symbol: T): T? = map[symbol] as T?

    public class Builder @PublishedApi internal constructor() {
        private val map = mutableMapOf<IrSymbol, IrSymbol>()
        public operator fun <T : IrSymbol> set(key: T, replacement: T) {
            map[key] = replacement
        }

        @PublishedApi
        internal fun build(): ReferenceReplacements = ReferenceReplacements(map.toMap())
    }

    override fun getReferencedClass(symbol: IrClassSymbol): IrClassSymbol? = this[symbol]
    override fun getReferencedScript(symbol: IrScriptSymbol): IrScriptSymbol? = this[symbol]
    override fun getReferencedClassOrNull(symbol: IrClassSymbol?): IrClassSymbol? = symbol?.let { this[symbol] }
    override fun getReferencedEnumEntry(symbol: IrEnumEntrySymbol): IrEnumEntrySymbol? = this[symbol]
    override fun getReferencedVariable(symbol: IrVariableSymbol): IrVariableSymbol? = this[symbol]
    override fun getReferencedLocalDelegatedProperty(symbol: IrLocalDelegatedPropertySymbol): IrLocalDelegatedPropertySymbol? = this[symbol]
    override fun getReferencedField(symbol: IrFieldSymbol): IrFieldSymbol? = this[symbol]
    override fun getReferencedConstructor(symbol: IrConstructorSymbol): IrConstructorSymbol? = this[symbol]
    override fun getReferencedValue(symbol: IrValueSymbol): IrValueSymbol? = this[symbol]
    override fun getReferencedFunction(symbol: IrFunctionSymbol): IrFunctionSymbol? = this[symbol]
    override fun getReferencedProperty(symbol: IrPropertySymbol): IrPropertySymbol? = this[symbol]
    override fun getReferencedSimpleFunction(symbol: IrSimpleFunctionSymbol): IrSimpleFunctionSymbol? = this[symbol]
    override fun getReferencedReturnableBlock(symbol: IrReturnableBlockSymbol): IrReturnableBlockSymbol? = this[symbol]
    override fun getReferencedClassifier(symbol: IrClassifierSymbol): IrClassifierSymbol? = this[symbol]
    override fun getReferencedTypeAlias(symbol: IrTypeAliasSymbol): IrTypeAliasSymbol? = this[symbol]
}

/**
 * Deep copy and replace references
 */
public inline fun <reified T : IrElement> T.deepCopyAndRemapReferences(
    initialParent: IrDeclarationParent? = null,
    referenceReplacements: ReferenceReplacements.Builder.() -> Unit
): T = deepCopyAndRemapReferences(initialParent, ReferenceReplacements(referenceReplacements))