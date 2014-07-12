// this file is not included in sources or tests, you can play with it for debug purposes
// Console run configuration will analyse it and provide lots of debug output
package dokka.playground

fun topLevelFunction() {
}

val topLevelConstantValue = "Hello"

val topLevelValue: String
    get() = "Bye bye"

var topLevelVariable: String
    get() = "Modify me!"
    set(value) {
    }

/**
 * This is a class
 */
class Class {
    fun memberFunction() {
    }

    val memberValue = "Member"
}

/**
 * This is a class with constructor and space after doc
 */

class ClassWithConstructor(
        /** Doc at parameter */ val name: Class)

/**
 * This is data class with constructor and two properties
 *
 * $name Person's name
 * $age Person's age
 *
 */
data class Person(val name: ClassWithConstructor, val age: Int) {}

object Object {
    throws(javaClass<IllegalArgumentException>())
    fun objectFunction() {
    }

    val objectValue: String
            /** one line getter doc */
        get() = "Member"

}

class OuterClass {

    /**
     * $T type of the item
     */
    class NestedClass<T> {
        fun nestedClassFunction(item: T) {
        }
    }

    inner class InnerClass {
        fun innerClassFunction<
                /** doc for R1 type param */
                R1,
                /** doc for R2 type param */
                R2,
                >() {
        }
    }

    object NestedObject {
        fun nestedObjectFunction() {
        }
    }
}