package test

import com.rnett.plugin.naming.ClassRef
import com.rnett.plugin.naming.PackageRef
import com.rnett.plugin.naming.RootPackage
import com.rnett.plugin.naming.function

object Kotnn : RootPackage("com.rnett.kotnn") {

    fun test() {

    }

    val Mod = object : ClassRef("Mod") {

    }

    object Module : ClassRef() {
        val test by function()
        val test2 by function()
    }

    object OtherClass : ClassRef("Test")

    object Annotations : PackageRef()
}

fun main() {
    println(Kotnn.fqName)
    println(Kotnn.Mod.fqName)
    println(Kotnn.Module.fqName)
    println(Kotnn.Module.test.fqName)
    println(Kotnn.Module.test2.fqName)
    println(Kotnn.OtherClass.fqName)
    println(Kotnn.Annotations.fqName)

//    println(getFqName(Kotnn::test))

}