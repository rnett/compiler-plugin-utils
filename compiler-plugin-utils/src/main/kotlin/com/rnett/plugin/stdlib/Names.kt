package com.rnett.plugin.stdlib

import com.rnett.plugin.naming.Class
import com.rnett.plugin.naming.ClassRef
import com.rnett.plugin.naming.PackageRef
import com.rnett.plugin.naming.RootPackage
import com.rnett.plugin.naming.function
import com.rnett.plugin.naming.property
import org.jetbrains.kotlin.ir.backend.js.utils.asString

//TODO be able to auto-generate name hierarchies for classes/packages.  Needs FIR for the actual generation though

//TODO let/apply/run/also w/ nice builders
object Kotlin : RootPackage("kotlin") {
    object Reflect : PackageRef() {
        val typeOf by function("typeOf")
        val KType by Class("KType")
    }

    val to by function()
    val Pair by Class()

    val Array by Class()

    val error by function()

    val Annotation by Class()

    val let by function()
    val also by function()
    val run by function()
    val apply by function()
    val with by function()

    object Collections : PackageRef() {
        val x = listOf(1)

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

        val toList by function { extensionReceiver = { it.type.asString() == "kotlin.collections.Iterable<T>" } }
        val toMutableList by function { extensionReceiver = { it.type.asString() == "kotlin.collections.Iterable<T>" } }
        val toListForMap by function("toList") { extensionReceiver = { it.type.asString() == "kotlin.collections.Map<out K,V>" } }
        val toMapForIterable by function("toMap") {
            extensionReceiver = { it.type.asString() == "kotlin.collections.Iterable<kotlin.Pair<K,V>>" }
            numParameters = 0
        }

        val collectionToTypedArray by function("toTypedArray") {
            extensionReceiver = { it.type.asString() == "kotlin.collections.Collection<T>" }
        }

        //TODO Array<>.toList()

        //TODO List toVararg?  Can I use toTypedArray for everything or do I need to use toIntArray etc for primitives?
        //TODO Numbers & math?
        //TODO extensive tests

        fun test(m: kotlin.collections.MutableMap<Int, Int>) {
            m.putAll(listOf(1 to 2))
        }
    }

}