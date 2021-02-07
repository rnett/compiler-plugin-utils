package com.rnett.plugin.stdlib

import com.rnett.plugin.naming.Class
import com.rnett.plugin.naming.ClassRef
import com.rnett.plugin.naming.ConstructorRef
import com.rnett.plugin.naming.FunctionRef
import com.rnett.plugin.naming.PackageRef
import com.rnett.plugin.naming.RootPackage
import com.rnett.plugin.naming.constructor
import com.rnett.plugin.naming.function
import com.rnett.plugin.naming.isClassifierOf
import com.rnett.plugin.naming.property
import com.rnett.plugin.naming.withExtensionReceiverType
import org.jetbrains.kotlin.ir.backend.js.utils.asString
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isByte
import org.jetbrains.kotlin.ir.types.isDouble
import org.jetbrains.kotlin.ir.types.isFloat
import org.jetbrains.kotlin.ir.types.isInt
import org.jetbrains.kotlin.ir.types.isLong
import org.jetbrains.kotlin.ir.types.isNullableAny
import org.jetbrains.kotlin.ir.types.isShort

//TODO be able to auto-generate name hierarchies for classes/packages.  Needs FIR for the actual generation though

object Kotlin : RootPackage("kotlin") {
    object Reflect : PackageRef() {
        val typeOf by function("typeOf")
        val KType by Class("KType")
    }

    object Any : ClassRef() {
        val hashCodeRef by function("hashCode")
        val toStringRef by function("toString")
    }

    object String : ClassRef()

    val nullableHashCode by function("hashCode") {
        numParameters = 0
    }
    val nullableToString by function("toString") {
        numParameters = 0
    }

    object Collections : PackageRef() {
        val listOfVararg by function("listOf") { hasVararg = true }
        val mapOfVararg by function("mapOf") { hasVararg = true }
        val setOfVararg by function("setOf") { hasVararg = true }

        val mutableListOfVararg by function("mutableListOf") { hasVararg = true }
        val mutableMapOfVararg by function("mutableMapOf") { hasVararg = true }
        val mutableSetOfVararg by function("mutableSetOf") { hasVararg = true }

        val listOfEmpty by function("listOf") { numParameters = 0 }
        val mapOfEmpty by function("mapOf") { numParameters = 0 }
        val setOfEmpty by function("setOf") { numParameters = 0 }

        val mutableListOfEmpty by function("mutableListOf") { numParameters = 0 }
        val mutableMapOfEmpty by function("mutableMapOf") { numParameters = 0 }
        val mutableSetOfEmpty by function("mutableSetOf") { numParameters = 0 }

        val iterablePlusIterableToList by function("plus") {
            val iterableType by lazy { Iterable }
            extensionReceiver = {
                it.type.isClassifierOf(iterableType)
            }
            numParameters = 1
            parameters[0] = {
                it.type.isClassifierOf(iterableType)
            }
        }

        val iterablePlusElementToList by function("plus") {
            val iterableType by lazy { Iterable }
            extensionReceiver = {
                it.type.isClassifierOf(iterableType)
            }
            numParameters = 1
            parameters[0] = {
                it.type.asString() == "T"
            }
        }

        val iterableMinusIterableToList by function("minus") {
            val iterableType by lazy { Iterable }
            extensionReceiver = {
                it.type.isClassifierOf(iterableType)
            }
            numParameters = 1
            parameters[0] = {
                it.type.isClassifierOf(iterableType)
            }
        }

        val iterableMinusElementToList by function("minus") {
            val iterableType by lazy { Iterable }
            extensionReceiver = {
                it.type.isClassifierOf(iterableType)
            }
            numParameters = 1
            parameters[0] = {
                it.type.asString() == "T"
            }
        }

        val setPlusIterableToSet by function("plus") {
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

        val setPlusElementToSet by function("plus") {
            val setType by lazy { Set }
            extensionReceiver = {
                it.type.isClassifierOf(setType)
            }
            numParameters = 1
            parameters[0] = {
                it.type.asString() == "T"
            }
        }

        val setMinusIterableToSet by function("minus") {
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

        val setMinusElementToSet by function("minus") {
            val setType by lazy { Set }
            extensionReceiver = {
                it.type.isClassifierOf(setType)
            }
            numParameters = 1
            parameters[0] = {
                it.type.asString() == "T"
            }
        }

        val mapPlusIterablePairs by function("plus") {
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

        val mapPlusMap by function("plus") {
            val mapType by lazy { Map }
            extensionReceiver = {
                it.type.isClassifierOf(mapType)
            }
            numParameters = 1
            parameters[0] = {
                it.type.isClassifierOf(mapType)
            }
        }

        val mapPlusElementPair by function("plus") {
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

        val mapMinusIterableKeys by function("minus") {
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

        val mapMinusKey by function("minus") {
            val mapType by lazy { Map }
            extensionReceiver = {
                it.type.isClassifierOf(mapType)
            }
            numParameters = 1
            parameters[0] = {
                it.type.asString() == "K"
            }
        }

        object Map : ClassRef() {
            val size by property()
            val isEmpty by function()
            val containsKey by function()
            val containsValue by function()
            val get by function()

            val keys by property()
            val values by property()
            val entries by property()
        }

        object MutableMap : ClassRef() {
            val Map = Collections.Map

            val put by function()
            val remove by function {
                numParameters = 1
            }

            val putAll by function()
            val putAllIterable by Collections.function("putAll") {
                hasExtensionReceiver = true
                parameters[0] = {
                    it.type.asString() == "kotlin.collections.Iterable<kotlin.Pair<K,V>>"
                }
            }
            val clear by function()
        }

        object Set : ClassRef() {
            val size by property()
            val isEmpty by function()
            val contains by function()
            val iterator by function()

            val containsAll by function()
        }

        object MutableSet : ClassRef() {
            val Set = Collections.Set

            val add by function { numParameters = 1 }
            val remove by function()
            val addAll by function { numParameters = 1 }
            val removeAll by function()
            val clear by function()
        }

        object List : ClassRef() {
            val size by property()
            val isEmpty by function()
            val contains by function()
            val iterator by function()

            val get by function()
            val indexOf by function()
        }

        object MutableList : ClassRef() {
            val List = Collections.List

            val add by function { numParameters = 1 }
            val remove by function()
            val addAll by function { numParameters = 1 }
            val removeAll by function()
            val clear by function()
            val set by function()
        }

        val Iterable by Class()

        val Collection by Class()

        val iterableToList by function("toList").withExtensionReceiverType { Iterable }
        val iterableToMutableList by function("toMutableList").withExtensionReceiverType { Iterable }
        val mapToList by function("toList").withExtensionReceiverType { Map }
        val iterableToMap by function("toMap") {
            numParameters = 0
        }.withExtensionReceiverType { Iterable }

        val collectionToTypedArray by function("toTypedArray") {
            extensionReceiver = { it.type.asString() == "kotlin.collections.Collection<T>" }
        }

        val arrayToList by function("toList").withExtensionReceiverType { Array }

        val arrayToMutableList by function("toMutableList").withExtensionReceiverType { Array }

        val mapGetValue by function("getValue") {
            numParameters = 1
        }.withExtensionReceiverType { Map }
        val mapGetOrDefault by function("getOrDefault").withExtensionReceiverType { Map }
        val mapGetOrElse by function("getOrElse").withExtensionReceiverType { Map }
        val mutableMapGetOrPut by function("getOrPut") {
            numParameters = 2
        }.withExtensionReceiverType { MutableMap }

        //TODO not in stdlib yet (same for getOrElseNullable)
//        val mutableMapGetOrPutNullable by function("getOrPutNullable"){
//            numParameters = 2
//        }.withExtensionReceiverType{ MutableMap }

    }

    val to by function()
    val Pair by Class()

    val Array by Class()

    val error by function()

    val Annotation by Class()

    val let by function()
    val also by function()
    val run by function {
        hasExtensionReceiver = true
    }
    val apply by function()
    val with by function()

    fun test() {
        val i = 3
    }

    object Number : ClassRef() {
        val toDouble by function()
        val toFloat by function()
        val toLong by function()
        val toInt by function()
        val toChar by function()
        val toShort by function()
        val toByte by function()
    }

    private fun isMathable(otherType: IrType) =
        otherType.isByte() || otherType.isShort() || otherType.isInt() ||
                otherType.isLong() || otherType.isFloat() || otherType.isDouble()

    private fun requireMathable(otherType: IrType) =
        require(isMathable(otherType)) { "Can only do math with Byte, Short, Int, Long, Float, and Double, got $otherType" }

    interface Mathable {
        val klass: ClassRef

        fun plus(otherType: IrType): FunctionRef {
            requireMathable(otherType)
            return klass.function("plus") {
                numParameters = 1
                parameters[0] = {
                    it.type == otherType
                }
            }
        }

        fun minus(otherType: IrType): FunctionRef {
            requireMathable(otherType)
            return klass.function("minus") {
                numParameters = 1
                parameters[0] = {
                    it.type == otherType
                }
            }
        }

        fun times(otherType: IrType): FunctionRef {
            requireMathable(otherType)
            return klass.function("times") {
                numParameters = 1
                parameters[0] = {
                    it.type == otherType
                }
            }
        }

        fun div(otherType: IrType): FunctionRef {
            requireMathable(otherType)
            return klass.function("div") {
                numParameters = 1
                parameters[0] = {
                    it.type == otherType
                }
            }
        }
    }

    object Byte : ClassRef(), Mathable {
        override val klass: ClassRef = this
    }

    object Short : ClassRef(), Mathable {
        override val klass: ClassRef = this
    }

    object Int : ClassRef(), Mathable {
        override val klass: ClassRef = this
    }

    object Long : ClassRef(), Mathable {
        override val klass: ClassRef = this
    }

    object Float : ClassRef(), Mathable {
        override val klass: ClassRef = this
    }

    object Double : ClassRef(), Mathable {
        override val klass: ClassRef = this
    }

    val Throwable by Class()

    val Error = JavaLang.Error
    val Exception = JavaLang.Exception
    val RuntimeException = JavaLang.RuntimeException
    val IllegalArgumentException = JavaLang.IllegalArgumentException
    val IllegalStateException = JavaLang.IllegalStateException
    val UnsupportedOperationException = JavaLang.UnsupportedOperationException
    val AssertionError = JavaLang.AssertionError

    val NoSuchElementException = JavaUtil.NoSuchElementException
    val IndexOutOfBoundsException = JavaLang.IndexOutOfBoundsException
    val ClassCastException = JavaLang.ClassCastException
    val NullPointerException = JavaLang.NullPointerException

    //TODO List toVararg?  Can I use toTypedArray for everything or do I need to use toIntArray etc for primitives?
    //TODO String
    //TODO javadoc comments w/ required types/signatures
    //TODO make it easier to escape the generated hierarchy, i.e. for the exception classes.  Easiest is to make RootPackage open instead of abstract
}

interface ExceptionClass {
    val klass: ClassRef
    val newEmpty: ConstructorRef
        get() = klass.constructor {
            numParameters = 0
        }.value

    val newWithMessage: ConstructorRef
        get() = klass.constructor {
            numParameters = 1
            parameters[0] = {
                it.type.isClassifierOf(Kotlin.String)
            }
        }.value
}

interface ExceptionClassWithCause : ExceptionClass {
    val newWithMessageAndClause: ConstructorRef
        get() = klass.constructor {
            numParameters = 2
        }.value

    val newWithClause: ConstructorRef
        get() = klass.constructor {
            numParameters = 1
            parameters[0] = {
                it.type.isClassifierOf(Kotlin.Throwable)
            }
        }.value
}

object JavaLang : RootPackage("java.lang") {

    object Error : ClassRef(), ExceptionClassWithCause {
        override val klass: ClassRef = this
    }

    object Exception : ClassRef(), ExceptionClassWithCause {
        override val klass: ClassRef = this
    }

    object RuntimeException : ClassRef(), ExceptionClassWithCause {
        override val klass: ClassRef = this
    }

    object IllegalArgumentException : ClassRef(), ExceptionClassWithCause {
        override val klass: ClassRef = this
    }

    object IllegalStateException : ClassRef(), ExceptionClassWithCause {
        override val klass: ClassRef = this
    }

    object UnsupportedOperationException : ClassRef(), ExceptionClassWithCause {
        override val klass: ClassRef = this
    }

    object AssertionError : ClassRef(), ExceptionClassWithCause {
        override val klass: ClassRef = this

        override val newWithMessage: ConstructorRef
            get() = klass.constructor {
                numParameters = 1
                parameters[0] = {
                    it.type.isNullableAny()
                }
            }.value

        override val newWithClause: ConstructorRef
            get() = newWithMessage
    }

    object IndexOutOfBoundsException : ClassRef(), ExceptionClass {
        override val klass: ClassRef = this
    }

    object ClassCastException : ClassRef(), ExceptionClass {
        override val klass: ClassRef = this
    }

    object NullPointerException : ClassRef(), ExceptionClass {
        override val klass: ClassRef = this
    }
}

object JavaUtil : RootPackage("java.util") {


    object NoSuchElementException : ClassRef(), ExceptionClass {
        override val klass: ClassRef = this
    }

}