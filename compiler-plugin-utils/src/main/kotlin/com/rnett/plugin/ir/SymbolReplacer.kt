package com.rnett.plugin.ir

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrEnumEntrySymbol
import org.jetbrains.kotlin.ir.symbols.IrExternalPackageFragmentSymbol
import org.jetbrains.kotlin.ir.symbols.IrFieldSymbol
import org.jetbrains.kotlin.ir.symbols.IrFileSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrLocalDelegatedPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrPropertySymbol
import org.jetbrains.kotlin.ir.symbols.IrReturnableBlockSymbol
import org.jetbrains.kotlin.ir.symbols.IrScriptSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeAliasSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueParameterSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.symbols.IrVariableSymbol
import org.jetbrains.kotlin.ir.util.DeepCopyIrTreeWithSymbols
import org.jetbrains.kotlin.ir.util.SymbolRemapper
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A symbol remapper that only replaces references to symbols
 */
public open class ReferenceRemapper : SymbolRemapper.Empty() {
    final override fun getDeclaredClass(symbol: IrClassSymbol): IrClassSymbol = symbol
    final override fun getDeclaredScript(symbol: IrScriptSymbol): IrScriptSymbol = symbol
    final override fun getDeclaredFunction(symbol: IrSimpleFunctionSymbol): IrSimpleFunctionSymbol = symbol
    final override fun getDeclaredProperty(symbol: IrPropertySymbol): IrPropertySymbol = symbol
    final override fun getDeclaredField(symbol: IrFieldSymbol): IrFieldSymbol = symbol
    final override fun getDeclaredFile(symbol: IrFileSymbol): IrFileSymbol = symbol
    final override fun getDeclaredConstructor(symbol: IrConstructorSymbol): IrConstructorSymbol = symbol
    final override fun getDeclaredEnumEntry(symbol: IrEnumEntrySymbol): IrEnumEntrySymbol = symbol
    final override fun getDeclaredExternalPackageFragment(symbol: IrExternalPackageFragmentSymbol): IrExternalPackageFragmentSymbol = symbol
    final override fun getDeclaredVariable(symbol: IrVariableSymbol): IrVariableSymbol = symbol
    final override fun getDeclaredLocalDelegatedProperty(symbol: IrLocalDelegatedPropertySymbol): IrLocalDelegatedPropertySymbol = symbol
    final override fun getDeclaredTypeParameter(symbol: IrTypeParameterSymbol): IrTypeParameterSymbol = symbol
    final override fun getDeclaredValueParameter(symbol: IrValueParameterSymbol): IrValueParameterSymbol = symbol
    final override fun getDeclaredTypeAlias(symbol: IrTypeAliasSymbol): IrTypeAliasSymbol = symbol
}

/**
 * Deep copy and replace references to symbols
 */
public inline fun <reified T : IrElement> T.deepCopyAndRemapReferences(
    initialParent: IrDeclarationParent? = null,
    referenceRemapper: ReferenceRemapper
): T = deepCopyWithSymbols(initialParent) { _, typeRemapper ->
    DeepCopyIrTreeWithSymbols(referenceRemapper, typeRemapper)
}

/**
 * Remap value symbols.  If given a [IrVariableSymbol], must return a [IrVariableSymbol].
 */
public fun interface ValueRemapper {
    public fun remapValue(symbol: IrValueSymbol): IrValueSymbol
}

/**
 * Deep copy and replace references to values
 */
public inline fun <reified T : IrElement> T.deepCopyAndRemapValues(
    initialParent: IrDeclarationParent? = null,
    referenceRemapper: ValueRemapper
): T = deepCopyAndRemapReferences(initialParent, object : ReferenceRemapper() {
    override fun getReferencedValue(symbol: IrValueSymbol): IrValueSymbol = referenceRemapper.remapValue(symbol)
    override fun getReferencedVariable(symbol: IrVariableSymbol): IrVariableSymbol = referenceRemapper.remapValue(symbol) as IrVariableSymbol
})

/**
 * Remap references to functions
 */
public fun interface FunctionRemapper {
    public fun remapValue(symbol: IrSimpleFunctionSymbol): IrSimpleFunctionSymbol
}

/**
 * Deep copy and replace references to values
 */
public inline fun <reified T : IrElement> T.deepCopyAndRemapFunctions(
    initialParent: IrDeclarationParent? = null,
    referenceRemapper: FunctionRemapper
): T = deepCopyAndRemapReferences(initialParent, object : ReferenceRemapper() {
    override fun getReferencedFunction(symbol: IrFunctionSymbol): IrFunctionSymbol =
        if (symbol is IrSimpleFunctionSymbol) referenceRemapper.remapValue(symbol) else symbol

    override fun getReferencedSimpleFunction(symbol: IrSimpleFunctionSymbol): IrSimpleFunctionSymbol =
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
public class ReferenceReplacements private constructor(private val map: Map<IrSymbol, IrSymbol>) : ReferenceRemapper() {
    public operator fun contains(symbol: IrSymbol): Boolean = symbol in map
    public operator fun <T : IrSymbol> get(symbol: T): T = (map[symbol] ?: symbol) as T

    public class Builder @PublishedApi internal constructor() {
        private val map = mutableMapOf<IrSymbol, IrSymbol>()
        public operator fun <T : IrSymbol> set(key: T, replacement: T) {
            map[key] = replacement
        }

        @PublishedApi
        internal fun build(): ReferenceReplacements = ReferenceReplacements(map.toMap())
    }

    override fun getReferencedClass(symbol: IrClassSymbol): IrClassSymbol = this[symbol]
    override fun getReferencedScript(symbol: IrScriptSymbol): IrScriptSymbol = this[symbol]
    override fun getReferencedClassOrNull(symbol: IrClassSymbol?): IrClassSymbol? = symbol?.let { map[symbol] as IrClassSymbol? }
    override fun getReferencedEnumEntry(symbol: IrEnumEntrySymbol): IrEnumEntrySymbol = this[symbol]
    override fun getReferencedVariable(symbol: IrVariableSymbol): IrVariableSymbol = this[symbol]
    override fun getReferencedLocalDelegatedProperty(symbol: IrLocalDelegatedPropertySymbol): IrLocalDelegatedPropertySymbol = this[symbol]
    override fun getReferencedField(symbol: IrFieldSymbol): IrFieldSymbol = this[symbol]
    override fun getReferencedConstructor(symbol: IrConstructorSymbol): IrConstructorSymbol = this[symbol]
    override fun getReferencedValue(symbol: IrValueSymbol): IrValueSymbol = this[symbol]
    override fun getReferencedFunction(symbol: IrFunctionSymbol): IrFunctionSymbol = this[symbol]
    override fun getReferencedProperty(symbol: IrPropertySymbol): IrPropertySymbol = this[symbol]
    override fun getReferencedSimpleFunction(symbol: IrSimpleFunctionSymbol): IrSimpleFunctionSymbol = this[symbol]
    override fun getReferencedReturnableBlock(symbol: IrReturnableBlockSymbol): IrReturnableBlockSymbol = this[symbol]
    override fun getReferencedClassifier(symbol: IrClassifierSymbol): IrClassifierSymbol = this[symbol]
    override fun getReferencedTypeAlias(symbol: IrTypeAliasSymbol): IrTypeAliasSymbol = this[symbol]
}

/**
 * Deep copy and replace references
 */
public inline fun <reified T : IrElement> T.deepCopyAndRemapReferences(
    initialParent: IrDeclarationParent? = null,
    referenceReplacements: ReferenceReplacements.Builder.() -> Unit
): T = deepCopyAndRemapReferences(initialParent, ReferenceReplacements(referenceReplacements))