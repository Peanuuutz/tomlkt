package net.peanuuutz.tomlkt

import kotlinx.serialization.SerialInfo

/**
 * Add comments to corresponding property.
 *
 * ```kotlin
 * data class IntData(
 *     @Comment("An integer,", "but is decoded into Long originally")
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
 * @property texts the comment texts. As it is vararg, one for each line (Note: '\n' will be escaped)
 */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class Comment(vararg val texts: String)

/**
 * Force fold the corresponding property(needs to be list-like or map-like).
 *
 * ```kotlin
 * data class Data(
 *     @Fold
 *     val foldProperty: Map<String, String>,
 *     val noFoldProperty: Map<String, String>
 * )
 * val data = mapOf("a" to "something", "b" to "another thing")
 * Data(data, data)
 * ```
 *
 * will produce:
 *
 * ```toml
 * foldProperty = { a = "something", b = "another thing" }
 *
 * [noFoldProperty]
 * a = "something"
 * b = "another thing"
 * ```
 *
 * Without that @Fold, both of these will act like how noFoldProperty behaves.
 *
 * Just try it. ;)
 */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class Fold

/**
 * Mark the corresponding [kotlin.String] property as multiline when encoded.
 *
 * ```kotlin
 * class MultilineStringData(
 *     @Multiline
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
public annotation class Multiline

/**
 * Mark the corresponding [kotlin.String] property as literal when encoded.
 *
 * ```kotlin
 * class LiteralStringData(
 *     @Literal
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
public annotation class Literal