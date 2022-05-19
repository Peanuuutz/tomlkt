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
    message = "Support for multiline strings",
    replaceWith = ReplaceWith(
        expression = "TomlComment",
        imports = [ "net.peanuuutz.tomlkt.TomlComment" ]
    )
)
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class Comment(vararg val texts: String)

/**
 * Force inline the corresponding property (needs to be list-like or map-like).
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
 * Without the @TomlInline, both of the two properties will act like how noInlineProperty behaves.
 */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class TomlInline

@Deprecated(
    message = "Name change",
    replaceWith = ReplaceWith(
        expression = "Inline",
        imports = [ "net.peanuuutz.tomlkt.Inline" ]
    )
)
public typealias Fold = TomlInline

/**
 * Mark the corresponding [kotlin.String] property as multiline when encoded.
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
    message = "Name change",
    replaceWith = ReplaceWith(
        expression = "TomlMultilineString",
        imports = [ "net.peanuuutz.tomlkt.TomlMultilineString" ]
    )
)
public typealias Multiline = TomlMultilineString

/**
 * Mark the corresponding [kotlin.String] property as literal when encoded.
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
    message = "Name change",
    replaceWith = ReplaceWith(
        expression = "TomlLiteralString",
        imports = [ "net.peanuuutz.tomlkt.TomlLiteralString" ]
    )
)
public typealias Literal = TomlLiteralString