/*
    Copyright 2023 Peanuuutz

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

package net.peanuuutz.tomlkt

import kotlinx.serialization.InheritableSerialInfo
import kotlinx.serialization.SerialInfo
import net.peanuuutz.tomlkt.TomlInteger.Base.Dec

/**
 * Adds comments to corresponding property.
 *
 * ```kotlin
 * class IntData(
 *     @TomlComment("""
 *         An integer,
 *         but is decoded into Long originally
 *     """)
 *     val int: Int
 * )
 * IntData(10086)
 * ```
 *
 * will produce:
 *
 * ```toml
 * # An integer,
 * # but is decoded into Long originally
 * int = 10086
 * ```
 *
 * @property text the comment text, could be multiline.
 */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class TomlComment(val text: String)

/**
 * Forces the annotated array-like or table-like property to be a one-liner.
 *
 * ```kotlin
 * class Data(
 *     @TomlInline
 *     val inlineProperty: Map<String, String>,
 *     val noInlineProperty: Map<String, String>
 * )
 * val data = mapOf("a" to "something", "b" to "another thing")
 * Data(data, data)
 * ```
 *
 * will produce:
 *
 * ```toml
 * inlineProperty = { a = "something", b = "another thing" }
 *
 * [noInlineProperty]
 * a = "something"
 * b = "another thing"
 * ```
 *
 * Without `@TomlInline`, both of these two properties will act like how
 * `noInlineProperty` behaves.
 */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class TomlInline {
    public companion object {
        /**
         * Shared [TomlInline] instance for [TomlArray.annotations] and
         * [TomlTable.annotations].
         */
        public val Instance: TomlInline = TomlInline()
    }
}

/**
 * Modifies the encoding process of corresponding array-like property, either to
 * force array of table to be encoded as block array, or to change how many
 * items should be encoded per line (this will override the default
 * [config][TomlConfigBuilder.itemsPerLineInBlockArray]).
 *
 * Note that, if the annotated property is also marked with [TomlInline], this
 * annotation will not take effect.
 *
 * ```kotlin
 * class NullablePairList<F, S>(
 *     @TomlBlockArray(2)
 *     val list: List<Pair<F, S>?>
 * )
 * NullablePairList(listOf(Pair("key", 1), null, Pair("key", 3), Pair("key", 4)))
 * ```
 *
 * will produce:
 *
 * ```toml
 * list = [
 *     { first = "key", second = 1 }, null,
 *     { first = "key", second = 3 }, { first = "key", second = 4 }
 * ]
 * ```
 */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class TomlBlockArray(val itemsPerLine: Int = 1)

/**
 * Marks the annotated [String] property as multiline when encoded.
 *
 * ```kotlin
 * class MultilineStringData(
 *     @TomlMultilineString
 *     val multilineString: String
 * )
 * MultilineStringData("""
 *     Do, a deer, a female deer.
 *     Re, a drop of golden sun.
 * """.trimIndent())
 * ```
 *
 * will produce:
 *
 * ```toml
 * multilineString = """
 * Do, a deer, a female deer.
 * Re, a drop of golden sun."""
 * ```
 */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class TomlMultilineString {
    public companion object {
        /**
         * Shared [TomlMultilineString] instance for [TomlArray.annotations] and
         * [TomlTable.annotations].
         */
        public val Instance: TomlMultilineString = TomlMultilineString()
    }
}

/**
 * Marks the annotated [String] property as literal when encoded.
 *
 * ```kotlin
 * class LiteralStringData(
 *     @TomlLiteralString
 *     val literalString: String
 * )
 * LiteralStringData("C:\\Users\\<User>\\.m2\\repositories")
 * ```
 *
 * will produce:
 *
 * ```toml
 * literalString = 'C:\Users\<User>\.m2\repositories'
 * ```
 */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class TomlLiteralString {
    public companion object {
        /**
         * Shared [TomlLiteralString] instance for [TomlArray.annotations] and
         * [TomlTable.annotations].
         */
        public val Instance: TomlLiteralString = TomlLiteralString()
    }
}

/**
 * Changes the representation of the annotated [Byte], [Short], [Int], [Long]
 * property.
 *
 * ```kotlin
 * class ByteCode(
 *     @TomlInteger(
 *         base = TomlInteger.Base.Hex,
 *         group = 2
 *     )
 *     val code: Int
 * )
 * ByteCode(0xFFE490)
 * ```
 *
 * will produce:
 *
 * ```toml
 * code = 0xFF_E4_90
 * ```
 *
 * @property group the size of a digit group separated by '_'. If set to 0, the
 * digits will not be grouped.
 */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class TomlInteger(
    val base: Base = Dec,
    val group: Int = 0
) {
    /**
     * The base of the [integer](https://toml.io/en/v1.0.0#integer).
     */
    public enum class Base(
        public val value: Int,
        public val prefix: String
    ) {
        /**
         * Binary base.
         */
        Bin(2, "0b"),

        /**
         * Octal base.
         */
        Oct(8, "0o"),

        /**
         * Decimal base.
         */
        Dec(10, ""),

        /**
         * Hexadecimal base.
         */
        Hex(16, "0x"),

        @Deprecated(
            message = "Unify singleton style.",
            replaceWith = ReplaceWith("Bin")
        )
        BIN(2, "0b"),

        @Deprecated(
            message = "Unify singleton style.",
            replaceWith = ReplaceWith("Oct")
        )
        OCT(8, "0o"),

        @Deprecated(
            message = "Unify singleton style.",
            replaceWith = ReplaceWith("Dec")
        )
        DEC(10, ""),

        @Deprecated(
            message = "Unify singleton style.",
            replaceWith = ReplaceWith("Hex")
        )
        HEX(16, "0x")
    }
}

/**
 * Changes the key of the class discriminator of a specific type hierarchy.
 *
 * ```kotlin
 * @Serializable
 * @TomlClassDiscriminator("class")
 * sealed class Element
 *
 * @Serializable
 * data class Literal(
 *     val string: String
 * ) : Element()
 *
 * @Serializable
 * data class Data(
 *     val element: Element
 * )
 * Data(Literal("Hello"))
 * ```
 *
 * will produce:
 *
 * ```toml
 * [element]
 * class = "package.Literal"
 * string = "Hello"
 * ```
 *
 * Again, this annotation will affect the whole type hierarchy, so any subclass
 * will use the same discriminator. It is not possible to define different
 * discriminators in different parts of the hierarchy.
 *
 * To change the default discriminator, please refer to [Toml] factory function
 * and [TomlConfigBuilder.classDiscriminator].
 */
@InheritableSerialInfo
@Target(AnnotationTarget.CLASS)
public annotation class TomlClassDiscriminator(val discriminator: String)
