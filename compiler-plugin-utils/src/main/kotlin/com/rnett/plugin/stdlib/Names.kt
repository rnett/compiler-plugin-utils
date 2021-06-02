package com.rnett.plugin.stdlib

import com.rnett.plugin.naming.*
import org.jetbrains.kotlin.ir.backend.js.utils.asString
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.render

//TODO be able to auto-generate name hierarchies for classes/packages.  Needs FIR for the actual generation though

/**
 * A collection of References from Kotlin's standard library.  Accessible in IR from [com.rnett.plugin.ir.HasContext.stdlib]
 */
@Deprecated("Will be superseded by reference generator")
public object Kotlin : RootPackage("kotlin") {
    public object Reflect : PackageRef() {
        public val typeOf: FunctionRef by function("typeOf")
        public val KType: ClassRef by Class("KType")

        public object KClass : ClassRef() {
            public val isInstance: FunctionRef by function()
        }
    }

    public object Any : ClassRef() {
        public val hashCodeRef: FunctionRef by function("hashCode")
        public val toStringRef: FunctionRef by function("toString")
    }

    public object String : ClassRef()

    public val nullableHashCode: FunctionRef by function("hashCode") {
        numParameters = 0
    }
    public val nullableToString: FunctionRef by function("toString") {
        numParameters = 0
    }

    public object Collections : PackageRef() {

        public val listOfNotNullVararg: FunctionRef by function("listOfNotNull") { hasVararg = true }
        public val setOfNotNullVararg: FunctionRef by function("setOfNotNull") { hasVararg = true }

        public val listOfVararg: FunctionRef by function("listOf") { hasVararg = true }
        public val mapOfVararg: FunctionRef by function("mapOf") { hasVararg = true }
        public val setOfVararg: FunctionRef by function("setOf") { hasVararg = true }

        public val mutableListOfVararg: FunctionRef by function("mutableListOf") { hasVararg = true }
        public val mutableMapOfVararg: FunctionRef by function("mutableMapOf") { hasVararg = true }
        public val mutableSetOfVararg: FunctionRef by function("mutableSetOf") { hasVararg = true }

        public val listOfEmpty: FunctionRef by function("listOf") { numParameters = 0 }
        public val mapOfEmpty: FunctionRef by function("mapOf") { numParameters = 0 }
        public val setOfEmpty: FunctionRef by function("setOf") { numParameters = 0 }

        public val mutableListOfEmpty: FunctionRef by function("mutableListOf") { numParameters = 0 }
        public val mutableMapOfEmpty: FunctionRef by function("mutableMapOf") { numParameters = 0 }
        public val mutableSetOfEmpty: FunctionRef by function("mutableSetOf") { numParameters = 0 }

        public val iterablePlusIterableToList: FunctionRef by function("plus") {
            val iterableType by lazy { Iterable }
            extensionReceiver = {
                it.type.isClassifierOf(iterableType)
            }
            numParameters = 1
            parameters[0] = {
                it.type.isClassifierOf(iterableType)
            }
        }

        public val iterablePlusElementToList: FunctionRef by function("plus") {
            val iterableType by lazy { Iterable }
            extensionReceiver = {
                it.type.isClassifierOf(iterableType)
            }
            numParameters = 1
            parameters[0] = {
                it.type.asString() == "T"
            }
        }

        public val iterableMinusIterableToList: FunctionRef by function("minus") {
            val iterableType by lazy { Iterable }
            extensionReceiver = {
                it.type.isClassifierOf(iterableType)
            }
            numParameters = 1
            parameters[0] = {
                it.type.isClassifierOf(iterableType)
            }
        }

        public val iterableMinusElementToList: FunctionRef by function("minus") {
            val iterableType by lazy { Iterable }
            extensionReceiver = {
                it.type.isClassifierOf(iterableType)
            }
            numParameters = 1
            parameters[0] = {
                it.type.asString() == "T"
            }
        }

        public val setPlusIterableToSet: FunctionRef by function("plus") {
            val setType by lazy { Set }
            val iterableType by lazy { Iterable }
            extensionReceiver = {
                it.type.isClassifierOf(setType)
            }
            numParameters = 1
            parameters[0] = {
                it.type.isClassifierOf(iterableType)
            }
        }

        public val setPlusElementToSet: FunctionRef by function("plus") {
            val setType by lazy { Set }
            extensionReceiver = {
                it.type.isClassifierOf(setType)
            }
            numParameters = 1
            parameters[0] = {
                it.type.asString() == "T"
            }
        }

        public val setMinusIterableToSet: FunctionRef by function("minus") {
            val setType by lazy { Set }
            val iterableType by lazy { Iterable }
            extensionReceiver = {
                it.type.isClassifierOf(setType)
            }
            numParameters = 1
            parameters[0] = {
                it.type.isClassifierOf(iterableType)
            }
        }

        public val setMinusElementToSet: FunctionRef by function("minus") {
            val setType by lazy { Set }
            extensionReceiver = {
                it.type.isClassifierOf(setType)
            }
            numParameters = 1
            parameters[0] = {
                it.type.asString() == "T"
            }
        }

        public val mapPlusIterablePairs: FunctionRef by function("plus") {
            val mapType by lazy { Map }
            val iterableType by lazy { Iterable }
            extensionReceiver = {
                it.type.isClassifierOf(mapType)
            }
            numParameters = 1
            parameters[0] = {
                it.type.isClassifierOf(iterableType)
            }
        }

        public val mapPlusMap: FunctionRef by function("plus") {
            val mapType by lazy { Map }
            extensionReceiver = {
                it.type.isClassifierOf(mapType)
            }
            numParameters = 1
            parameters[0] = {
                it.type.isClassifierOf(mapType)
            }
        }

        public val mapPlusElementPair: FunctionRef by function("plus") {
            val mapType by lazy { Map }
            val pairType by lazy { Pair }
            extensionReceiver = {
                it.type.isClassifierOf(mapType)
            }
            numParameters = 1
            parameters[0] = {
                it.type.isClassifierOf(Pair)
            }
        }

        public val mapMinusIterableKeys: FunctionRef by function("minus") {
            val mapType by lazy { Map }
            val iterableType by lazy { Iterable }
            extensionReceiver = {
                it.type.isClassifierOf(mapType)
            }
            numParameters = 1
            parameters[0] = {
                it.type.isClassifierOf(iterableType)
            }
        }

        public val mapMinusKey: FunctionRef by function("minus") {
            val mapType by lazy { Map }
            extensionReceiver = {
                it.type.isClassifierOf(mapType)
            }
            numParameters = 1
            parameters[0] = {
                it.type.asString() == "K"
            }
        }

        public object Map : ClassRef() {
            public val size: PropertyRef by property()
            public val isEmpty: FunctionRef by function()
            public val containsKey: FunctionRef by function()
            public val containsValue: FunctionRef by function()
            public val get: FunctionRef by function()

            public val keys: PropertyRef by property()
            public val values: PropertyRef by property()
            public val entries: PropertyRef by property()
        }

        public object MutableMap : ClassRef() {
            public val Map: Map = Collections.Map

            public val put: FunctionRef by function()
            public val remove: FunctionRef by function {
                numParameters = 1
            }

            public val putAll: FunctionRef by function()
            public val putAllIterable: FunctionRef by Collections.function("putAll") {
                hasExtensionReceiver = true
                parameters[0] = {
                    it.type.asString() == "kotlin.collections.Iterable<kotlin.Pair<K,V>>"
                }
            }
            public val clear: FunctionRef by function()
        }

        public object Set : ClassRef() {
            public val size: PropertyRef by property()
            public val isEmpty: FunctionRef by function()
            public val contains: FunctionRef by function()
            public val iterator: FunctionRef by function()

            public val containsAll: FunctionRef by function()
        }

        public object MutableSet : ClassRef() {
            public val Set: Set = Collections.Set

            public val add: FunctionRef by function { numParameters = 1 }
            public val remove: FunctionRef by function()
            public val addAll: FunctionRef by function { numParameters = 1 }
            public val removeAll: FunctionRef by function()
            public val clear: FunctionRef by function()
        }

        public object List : ClassRef() {
            public val size: PropertyRef by property()
            public val isEmpty: FunctionRef by function()
            public val contains: FunctionRef by function()
            public val iterator: FunctionRef by function()

            public val get: FunctionRef by function()
            public val indexOf: FunctionRef by function()
        }

        public object MutableList : ClassRef() {
            public val List: List = Collections.List

            public val add: FunctionRef by function { numParameters = 1 }
            public val remove: FunctionRef by function()
            public val addAll: FunctionRef by function { numParameters = 1 }
            public val removeAll: FunctionRef by function()
            public val clear: FunctionRef by function()
            public val set: FunctionRef by function()
        }

        public val Iterable: ClassRef by Class()

        public val Collection: ClassRef by Class()

        public val iterableToList: FunctionRef by function("toList").withExtensionReceiverType { Iterable }
        public val iterableToMutableList: FunctionRef by function("toMutableList").withExtensionReceiverType { Iterable }
        public val mapToList: FunctionRef by function("toList").withExtensionReceiverType { Map }
        public val iterableToMap: FunctionRef by function("toMap") {
            numParameters = 0
        }.withExtensionReceiverType { Iterable }

        public val collectionToTypedArray: FunctionRef by function("toTypedArray") {
            extensionReceiver = { it.type.asString() == "kotlin.collections.Collection<T>" }
        }

        public val arrayToList: FunctionRef by function("toList").withExtensionReceiverType { Array }

        public val arrayToMutableList: FunctionRef by function("toMutableList").withExtensionReceiverType { Array }

        public val mapGetValue: FunctionRef by function("getValue") {
            numParameters = 1
        }.withExtensionReceiverType { Map }
        public val mapGetOrDefault: FunctionRef by function("getOrDefault").withExtensionReceiverType { Map }
        public val mapGetOrElse: FunctionRef by function("getOrElse").withExtensionReceiverType { Map }
        public val mutableMapGetOrPut: FunctionRef by function("getOrPut") {
            numParameters = 2
        }.withExtensionReceiverType { MutableMap }

        public val mutableListRemoveLast: FunctionRef by function("removeLast").withExtensionReceiverType { MutableList }

        public val mutableListRemoveFirst: FunctionRef by function("removeFirst").withExtensionReceiverType { MutableList }

        //TODO not in stdlib yet (same for getOrElseNullable)
//        public val mutableMapGetOrPutNullable: FunctionRef by function("getOrPutNullable"){
//            numParameters = 2
//        }.withExtensionReceiverType{ MutableMap }

    }

    public val to: FunctionRef by function()
    public val Pair: ClassRef by Class()

    public val Array: ClassRef by Class()

    public val error: FunctionRef by function()

    public val Annotation: ClassRef by Class()

    public val let: FunctionRef by function()
    public val also: FunctionRef by function()
    public val run: FunctionRef by function {
        hasExtensionReceiver = true
    }
    public val apply: FunctionRef by function()
    public val with: FunctionRef by function()

    public object Number : ClassRef() {
        public val toDouble: FunctionRef by function()
        public val toFloat: FunctionRef by function()
        public val toLong: FunctionRef by function()
        public val toInt: FunctionRef by function()
        public val toChar: FunctionRef by function()
        public val toShort: FunctionRef by function()
        public val toByte: FunctionRef by function()
    }

    private fun isMathable(otherType: IrType) =
        otherType.isByte() || otherType.isShort() || otherType.isInt() ||
                otherType.isLong() || otherType.isFloat() || otherType.isDouble()

    private fun requireMathable(otherType: IrType) =
        require(isMathable(otherType)) { "Can only do math with Byte, Short, Int, Long, Float, and Double, got $otherType" }

    public interface Mathable {
        public val klass: ClassRef

        public fun plus(otherType: IrType): FunctionRef {
            requireMathable(otherType)
            return klass.function("plus") {
                numParameters = 1
                parameters[0] = {
                    it.type == otherType
                }
            }
        }

        public fun minus(otherType: IrType): FunctionRef {
            requireMathable(otherType)
            return klass.function("minus") {
                numParameters = 1
                parameters[0] = {
                    it.type == otherType
                }
            }
        }

        public fun times(otherType: IrType): FunctionRef {
            requireMathable(otherType)
            return klass.function("times") {
                numParameters = 1
                parameters[0] = {
                    it.type == otherType
                }
            }
        }

        public fun div(otherType: IrType): FunctionRef {
            requireMathable(otherType)
            return klass.function("div") {
                numParameters = 1
                parameters[0] = {
                    it.type == otherType
                }
            }
        }
    }

    public object Byte : ClassRef(), Mathable {
        override val klass: ClassRef = this
    }

    public object Short : ClassRef(), Mathable {
        override val klass: ClassRef = this
    }

    public object Int : ClassRef(), Mathable {
        override val klass: ClassRef = this
    }

    public object Long : ClassRef(), Mathable {
        override val klass: ClassRef = this
    }

    public object Float : ClassRef(), Mathable {
        override val klass: ClassRef = this
    }

    public object Double : ClassRef(), Mathable {
        override val klass: ClassRef = this
    }

    public object Throwable : ClassRef(), ExceptionClassWithCause {
        override val klass: ClassRef = this

    }

    public val throwablePrintStackTrace: FunctionRef = function("printStackTrace") {
        numParameters = 0
        extensionReceiver = {
            it.type.render() == "kotlin.Throwable"
        }
    }

//    public val Error: JavaLang.Error = JavaLang.Error
//    public val Exception: JavaLang.Exception = JavaLang.Exception
//    public val RuntimeException: JavaLang.RuntimeException = JavaLang.RuntimeException
//    public val IllegalArgumentException: JavaLang.IllegalArgumentException = JavaLang.IllegalArgumentException
//    public val IllegalStateException: JavaLang.IllegalStateException = JavaLang.IllegalStateException
//    public val UnsupportedOperationException: JavaLang.UnsupportedOperationException =
//        JavaLang.UnsupportedOperationException
//    public val AssertionError: JavaLang.AssertionError = JavaLang.AssertionError
//
//    public val NoSuchElementException: JavaUtil.NoSuchElementException = JavaUtil.NoSuchElementException
//    public val IndexOutOfBoundsException: JavaLang.IndexOutOfBoundsException = JavaLang.IndexOutOfBoundsException
//    public val ClassCastException: JavaLang.ClassCastException = JavaLang.ClassCastException
//    public val NullPointerException: JavaLang.NullPointerException = JavaLang.NullPointerException

    //TODO List toVararg?  Can I use toTypedArray for everything or do I need to use toIntArray etc for primitives?
    //TODO String
    //TODO javadoc comments w/ required types/signatures
}

public interface ExceptionClass {
    public val klass: ClassRef
    public val newEmpty: ConstructorRef
        get() = klass.constructor {
            numParameters = 0
        }

    public val newWithMessage: ConstructorRef
        get() = klass.constructor {
            numParameters = 1
            parameters[0] = {
                it.type.isClassifierOf(Kotlin.String)
            }
        }

    public val cause: PropertyRef
        get() = klass.property("cause")

    public val message: PropertyRef
        get() = klass.property("message")
}

public interface ExceptionClassWithCause : ExceptionClass {


    public val newWithMessageAndClause: ConstructorRef
        get() = klass.constructor {
            numParameters = 2
        }

    public val newWithClause: ConstructorRef
        get() = klass.constructor {
            numParameters = 1
            parameters[0] = {
                it.type.isClassifierOf(Kotlin.Throwable)
            }
        }
}

public object JavaLang : RootPackage("java.lang") {

    public object Error : ClassRef(), ExceptionClassWithCause {
        override val klass: ClassRef = this
    }

    public object Exception : ClassRef(), ExceptionClassWithCause {
        override val klass: ClassRef = this
    }

    public object RuntimeException : ClassRef(), ExceptionClassWithCause {
        override val klass: ClassRef = this
    }

    public object IllegalArgumentException : ClassRef(), ExceptionClassWithCause {
        override val klass: ClassRef = this
    }

    public object IllegalStateException : ClassRef(), ExceptionClassWithCause {
        override val klass: ClassRef = this
    }

    public object UnsupportedOperationException : ClassRef(), ExceptionClassWithCause {
        override val klass: ClassRef = this
    }

    public object AssertionError : ClassRef(), ExceptionClassWithCause {
        override val klass: ClassRef = this

        override val newWithMessage: ConstructorRef
            get() = klass.constructor {
                numParameters = 1
                parameters[0] = {
                    it.type.isNullableAny()
                }
            }

        override val newWithClause: ConstructorRef
            get() = newWithMessage
    }

    public object IndexOutOfBoundsException : ClassRef(), ExceptionClass {
        override val klass: ClassRef = this
    }

    public object ClassCastException : ClassRef(), ExceptionClass {
        override val klass: ClassRef = this
    }

    public object NullPointerException : ClassRef(), ExceptionClass {
        override val klass: ClassRef = this
    }
}

@Deprecated("Will be superseded by reference generator")
public object JavaUtil : RootPackage("java.util") {


    public object NoSuchElementException : ClassRef(), ExceptionClass {
        override val klass: ClassRef = this
    }

}