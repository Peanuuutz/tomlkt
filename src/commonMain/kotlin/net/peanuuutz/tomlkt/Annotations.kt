/*
    Copyright 2022 Peanuuutz

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

import kotlinx.serialization.SerialInfo

/**
 * Add comments to corresponding property.
 *
 * ```kotlin
 * data class IntData(
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
 * @property text the comment texts.
 */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class TomlComment(val text: String)

@Deprecated(
    message = "Multiline string is now supported",
    replaceWith = ReplaceWith(
        expression = "TomlComment",
        imports = [ "net.peanuuutz.tomlkt.TomlComment" ]
    ),
    level = DeprecationLevel.HIDDEN
)
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class Comment(vararg val texts: String)

/**
 * Force inline the corresponding array-like or table-like property.
 *
 * ```kotlin
 * data class Data(
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
 * Without @TomlInline, both of the two properties will act like how noInlineProperty behaves.
 */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class TomlInline

/**
 * Modify the encoding of corresponding array-like property, either to force array of tables
 * to be encoded as block array, or to change how many items will be encoded per line
 * (will override [TomlConfig][TomlConfigBuilder.itemsPerLineInBlockArray]).
 *
 * Note: If the corresponding property is marked [TomlInline], this annotation will not take effect.
 *
 * ```kotlin
 * data class NullablePairList<F, S>(
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

@Deprecated(
    message = "Name changed",
    replaceWith = ReplaceWith(
        expression = "TomlInline",
        imports = [ "net.peanuuutz.tomlkt.TomlInline" ]
    ),
    level = DeprecationLevel.HIDDEN
)
public typealias Fold = TomlInline

/**
 * Mark the corresponding [String] property as multiline when encoded.
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
public annotation class TomlMultilineString

@Deprecated(
    message = "Name changed",
    replaceWith = ReplaceWith(
        expression = "TomlMultilineString",
        imports = [ "net.peanuuutz.tomlkt.TomlMultilineString" ]
    ),
    level = DeprecationLevel.HIDDEN
)
public typealias Multiline = TomlMultilineString

/**
 * Mark the corresponding [String] property as literal when encoded.
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
public annotation class TomlLiteralString

@Deprecated(
    message = "Name changed",
    replaceWith = ReplaceWith(
        expression = "TomlLiteralString",
        imports = [ "net.peanuuutz.tomlkt.TomlLiteralString" ]
    ),
    level = DeprecationLevel.HIDDEN
)
public typealias Literal = TomlLiteralString

/**
 * Set the representation of the corresponding [Byte], [Short], [Int], [Long] property.
 *
 * ```kotlin
 * class ByteCode(
 *     @TomlInteger(TomlInteger.Base.BIN)
 *     val code: Byte
 * )
 * ByteCode(0b1101)
 * ```
 *
 * will produce:
 *
 * ```toml
 * code = 0b1101
 * ```
 */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class TomlInteger(val base: Base) {
    /**
     * Representation of a [TOML integer](https://toml.io/en/v1.0.0#integer).
     */
    public enum class Base(
        public val value: Int,
        public val prefix: String
    ) {
        DEC(10, ""),
        HEX(16, "0x"),
        BIN(2, "0b"),
        OCT(8, "0o");
    }
}
