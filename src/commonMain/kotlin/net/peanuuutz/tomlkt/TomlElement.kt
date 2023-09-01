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

import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import net.peanuuutz.tomlkt.internal.NonPrimitiveKeyException
import net.peanuuutz.tomlkt.internal.TomlArraySerializer
import net.peanuuutz.tomlkt.internal.TomlElementSerializer
import net.peanuuutz.tomlkt.internal.TomlLiteralSerializer
import net.peanuuutz.tomlkt.internal.TomlNullSerializer
import net.peanuuutz.tomlkt.internal.TomlTableSerializer
import net.peanuuutz.tomlkt.internal.doubleQuoted
import net.peanuuutz.tomlkt.internal.doubleQuotedIfNotPure
import net.peanuuutz.tomlkt.internal.escape
import net.peanuuutz.tomlkt.internal.parser.ArrayNode
import net.peanuuutz.tomlkt.internal.parser.KeyNode
import net.peanuuutz.tomlkt.internal.parser.TreeNode
import net.peanuuutz.tomlkt.internal.parser.ValueNode
import net.peanuuutz.tomlkt.internal.toStringModified

// -------- TomlElement --------

/**
 * Represents anything in TOML, including and only including [TomlNull],
 * [TomlLiteral], [TomlArray], [TomlTable].
 *
 * **Warning: Only use [Toml] to serialize/deserialize any subclass.**
 */
@Serializable(with = TomlElementSerializer::class)
public sealed class TomlElement {
    /**
     * The content of this TomlElement. Each subclass has its own implementation.
     */
    public abstract val content: Any?

    /**
     * Gives a string representation of this TomlElement. Each subclass has its
     * own implementation.
     *
     * ```kotlin
     * val table = TomlTable(mapOf("isEnabled" to true, "port" to 8080))
     * println(table) // { isEnabled = true, port = 8080 }
     * ```
     *
     * @return a JSON-like string containing [content].
     */
    public abstract override fun toString(): String
}

// -------- TomlNull --------

/**
 * Represents null.
 *
 * Note: Currently encoded value can NOT be modified.
 */
@Serializable(with = TomlNullSerializer::class)
public object TomlNull : TomlElement() {
    override val content: Nothing? get() = null

    override fun toString(): String {
        return "null"
    }
}

// ---- To TomlNull ----

/**
 * Convert [this] to TomlNull.
 *
 * @throws IllegalStateException when [this] is not TomlNull
 */
public fun TomlElement.toTomlNull(): TomlNull {
    return this as? TomlNull ?: failConversion("TomlNull")
}

// -------- TomlLiteral --------

/**
 * Represents literal value, which can be booleans, numbers, chars, strings.
 */
@Serializable(with = TomlLiteralSerializer::class)
public class TomlLiteral internal constructor(
    /**
     * The converted value. (see creator functions with the same name)
     */
    override val content: String,
    /**
     * Indicates whether this TomlLiteral is actually a [Char] or [String].
     */
    private val isString: Boolean
) : TomlElement() {
    override fun toString(): String {
        return if (isString) {
            content.escape().doubleQuoted
        } else {
            content
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as TomlLiteral
        if (isString != other.isString) return false
        if (content != other.content) return false
        return true
    }

    override fun hashCode(): Int {
        var result = content.hashCode()
        result = 31 * result + isString.hashCode()
        return result
    }
}

// ---- To TomlLiteral ----

/**
 * Convert [this] to TomlLiteral.
 *
 * @throws IllegalStateException when [this] is not TomlLiteral.
 */
public fun TomlElement.toTomlLiteral(): TomlLiteral {
    return this as? TomlLiteral ?: failConversion("TomlLiteral")
}

/**
 * Creates [TomlLiteral] from the given boolean [value].
 */
public fun TomlLiteral(value: Boolean): TomlLiteral {
    return TomlLiteral(value.toString(), isString = false)
}

/**
 * Creates [TomlLiteral] from the given numeric [value].
 *
 * @see toStringModified
 */
public fun TomlLiteral(value: Number): TomlLiteral {
    return TomlLiteral(value.toStringModified(), isString = false)
}

/**
 * Creates [TomlLiteral] from the given char [value].
 */
public fun TomlLiteral(value: Char): TomlLiteral {
    return TomlLiteral(value.toString(), isString = true)
}

/**
 * Creates [TomlLiteral] from the given string [value].
 */
public fun TomlLiteral(value: String): TomlLiteral {
    return TomlLiteral(value, isString = true)
}

/**
 * Creates [TomlLiteral] from the given `TomlLocalDateTime` [value].
 */
public fun TomlLiteral(value: TomlLocalDateTime): TomlLiteral {
    return TomlLiteral(value.toString(), isString = false)
}

/**
 * Creates [TomlLiteral] from the given `TomlOffsetDateTime` [value].
 */
public fun TomlLiteral(value: TomlOffsetDateTime): TomlLiteral {
    return TomlLiteral(value.toString(), isString = false)
}

/**
 * Creates [TomlLiteral] from the given `TomlLocalDate` [value].
 */
public fun TomlLiteral(value: TomlLocalDate): TomlLiteral {
    return TomlLiteral(value.toString(), isString = false)
}

/**
 * Creates [TomlLiteral] from the given `TomlLocalTime` [value].
 */
public fun TomlLiteral(value: TomlLocalTime): TomlLiteral {
    return TomlLiteral(value.toString(), isString = false)
}

/**
 * Creates [TomlLiteral] from the given enum [value]. Delegates to creator
 * function which consumes string.
 *
 * @param E the enum class which [value] belongs to.
 * @param serializersModule in most case could be ignored, but for contextual
 * it should be present.
 */
@Suppress("OutdatedDocumentation")
public inline fun <reified E : Enum<E>> TomlLiteral(
    value: E,
    serializersModule: SerializersModule = EmptySerializersModule()
): TomlLiteral {
    val stringRepresentation = serializersModule.serializer<E>()
        .descriptor
        .getElementName(value.ordinal)
    return TomlLiteral(stringRepresentation)
}

// ---- From TomlLiteral ----

/**
 * Returns content as boolean.
 *
 * @throws IllegalStateException if content cannot be converted into boolean.
 */
public fun TomlLiteral.toBoolean(): Boolean {
    return toBooleanOrNull() ?: error("Cannot convert $this to Boolean")
}

/**
 * Returns content as boolean only if content is "true" or "false", otherwise null.
 */
public fun TomlLiteral.toBooleanOrNull(): Boolean? {
    return when (content) {
        "true" -> true
        "false" -> false
        else -> null
    }
}

/**
 * Returns content as byte.
 *
 * @throws NumberFormatException if content cannot be converted into byte.
 */
public fun TomlLiteral.toByte(): Byte {
    return content.toByte()
}

/**
 * Returns content as byte only if content can be byte, otherwise null.
 */
public fun TomlLiteral.toByteOrNull(): Byte? {
    return content.toByteOrNull()
}

/**
 * Returns content as short.
 *
 * @throws NumberFormatException if content cannot be converted into short.
 */
public fun TomlLiteral.toShort(): Short {
    return content.toShort()
}

/**
 * Returns content as short only if content can be short, otherwise null.
 */
public fun TomlLiteral.toShortOrNull(): Short? {
    return content.toShortOrNull()
}

/**
 * Returns content as int.
 *
 * @throws NumberFormatException if content cannot be converted into int.
 */
public fun TomlLiteral.toInt(): Int {
    return content.toInt()
}

/**
 * Returns content as int only if content can be int, otherwise null.
 */
public fun TomlLiteral.toIntOrNull(): Int? {
    return content.toIntOrNull()
}

/**
 * Returns content as long.
 *
 * @throws NumberFormatException if content cannot be converted into long.
 */
public fun TomlLiteral.toLong(): Long {
    return content.toLong()
}

/**
 * Returns content as long only if content can be long, otherwise null.
 */
public fun TomlLiteral.toLongOrNull(): Long? {
    return content.toLongOrNull()
}

/**
 * Returns content as float.
 *
 * @throws NumberFormatException if content cannot be converted into float.
 */
public fun TomlLiteral.toFloat(): Float {
    return toFloatOrNull() ?: throw NumberFormatException("Cannot convert $this to Float")
}

/**
 * Returns content as float only if content can be an exact float or
 * inf/-inf/nan, otherwise null.
 */
public fun TomlLiteral.toFloatOrNull(): Float? {
    return when (content) {
        "inf" -> Float.POSITIVE_INFINITY
        "-inf" -> Float.NEGATIVE_INFINITY
        "nan" -> Float.NaN
        else -> content.toFloatOrNull()
    }
}

/**
 * Returns content as double.
 *
 * @throws NumberFormatException if content cannot be converted into double.
 */
public fun TomlLiteral.toDouble(): Double {
    return toDoubleOrNull() ?: throw NumberFormatException("Cannot convert $this to Double")
}

/**
 * Returns content as double only if content can be an exact double or
 * inf/-inf/nan, otherwise null.
 */
public fun TomlLiteral.toDoubleOrNull(): Double? {
    return when (content) {
        "inf" -> Double.POSITIVE_INFINITY
        "-inf" -> Double.NEGATIVE_INFINITY
        "nan" -> Double.NaN
        else -> content.toDoubleOrNull()
    }
}

/**
 * Returns content as char.
 *
 * @throws NoSuchElementException if content is empty.
 * @throws IllegalArgumentException if content cannot be converted into char.
 */
public fun TomlLiteral.toChar(): Char {
    return content.single()
}

/**
 * Returns content as char only if the length of content is exactly 1, otherwise
 * null.
 */
public fun TomlLiteral.toCharOrNull(): Char? {
    return content.singleOrNull()
}

/**
 * Returns content as parsed [TomlLocalDateTime].
 *
 * @throws IllegalArgumentException if content cannot be parsed into
 * `TomlLocalDateTime`.
 */
public fun TomlLiteral.toLocalDateTime(): TomlLocalDateTime {
    return NativeLocalDateTime(content)
}

/**
 * Returns content as [TomlLocalDateTime] only if content can be parsed into it,
 * otherwise null.
 */
public fun TomlLiteral.toLocalDateTimeOrNull(): TomlLocalDateTime? {
    return try {
        toLocalDateTime()
    } catch (e: IllegalArgumentException) {
        null
    }
}

/**
 * Returns content as parsed [TomlOffsetDateTime].
 *
 * @throws IllegalArgumentException if content cannot be parsed into
 * `TomlOffsetDateTime`.
 */
public fun TomlLiteral.toOffsetDateTime(): TomlOffsetDateTime {
    return NativeOffsetDateTime(content)
}

/**
 * Returns content as [TomlOffsetDateTime] only if content can be parsed into it,
 * otherwise null.
 */
public fun TomlLiteral.toOffsetDateTimeOrNull(): TomlOffsetDateTime? {
    return try {
        toOffsetDateTime()
    } catch (e: IllegalArgumentException) {
        null
    }
}

/**
 * Returns content as parsed [TomlLocalDate].
 *
 * @throws IllegalArgumentException if content cannot be parsed into `TomlLocalDate`.
 */
public fun TomlLiteral.toLocalDate(): TomlLocalDate {
    return NativeLocalDate(content)
}

/**
 * Returns content as [TomlLocalDate] only if content can be parsed into it,
 * otherwise null.
 */
public fun TomlLiteral.toLocalDateOrNull(): TomlLocalDate? {
    return try {
        toLocalDate()
    } catch (e: IllegalArgumentException) {
        null
    }
}

/**
 * Returns content as parsed [TomlLocalTime].
 *
 * @throws IllegalArgumentException if content cannot be parsed into `TomlLocalTime`.
 */
public fun TomlLiteral.toLocalTime(): TomlLocalTime {
    return NativeLocalTime(content)
}

/**
 * Returns content as [TomlLocalTime] only if content can be parsed into it,
 * otherwise null.
 */
public fun TomlLiteral.toLocalTimeOrNull(): TomlLocalTime? {
    return try {
        toLocalTime()
    } catch (e: IllegalArgumentException) {
        null
    }
}

/**
 * Returns content as enum with given enum class context.
 *
 * @param E the enum class which [this] converts to.
 * @param serializersModule in most case could be ignored, but for contextual it
 * should be present.
 *
 * @throws IllegalStateException if content cannot be converted into [E].
 */
public inline fun <reified E : Enum<E>> TomlLiteral.toEnum(
    serializersModule: SerializersModule = EmptySerializersModule()
): E {
    return toEnumOrNull<E>(serializersModule) ?: error("Cannot convert $this to ${E::class.simpleName}")
}

/**
 * Returns content as enum with given enum class context only if content suits
 * in, otherwise null.
 *
 * @param E the enum class which [this] converts to.
 * @param serializersModule in most case could be ignored, but for contextual it
 * should be present.
 */
public inline fun <reified E : Enum<E>> TomlLiteral.toEnumOrNull(
    serializersModule: SerializersModule = EmptySerializersModule()
): E? {
    val index = serializersModule.serializer<E>()
        .descriptor
        .elementNames
        .indexOf(content)
    return if (index != -1) {
        enumValues<E>()[index]
    } else {
        null
    }
}

// -------- TomlArray --------

/**
 * Represents array in TOML, which values are [TomlElement].
 *
 * As it delegates to list [content], everything in [List] could be used.
 */
@Serializable(with = TomlArraySerializer::class)
public class TomlArray internal constructor(
    override val content: List<TomlElement>
) : TomlElement(), List<TomlElement> by content {
    override fun toString(): String {
        return content.joinToString(prefix = "[ ", postfix = " ]")
    }

    override fun equals(other: Any?): Boolean {
        return content == other
    }

    override fun hashCode(): Int {
        return content.hashCode()
    }
}

// ---- To TomlArray ----

/**
 * Convert [this] to TomlArray.
 *
 * @throws IllegalStateException when [this] is not TomlArray.
 */
public fun TomlElement.toTomlArray(): TomlArray {
    return this as? TomlArray ?: failConversion("TomlArray")
}

/**
 * Creates [TomlArray] from the given iterable [value].
 */
public fun TomlArray(value: Iterable<*>): TomlArray {
    val content = value.map(Any?::toTomlElement)
    return TomlArray(content)
}

// -------- TomlTable --------

/**
 * Represents table in TOML, which keys are strings and values are [TomlElement].
 *
 * As it delegates to map [content], everything in [Map] could be used.
 */
@Serializable(with = TomlTableSerializer::class)
public class TomlTable internal constructor(
    override val content: Map<String, TomlElement>
) : TomlElement(), Map<String, TomlElement> by content {
    /**
     * More convenient than [Map.get] if this TomlTable is originally a map with
     * **primitive** keys
     *
     * @throws NonPrimitiveKeyException if provide non-primitive key
     */
    public operator fun get(key: Any?): TomlElement? {
        return get(key.toTomlKey())
    }

    override fun toString(): String {
        return content.entries.joinToString(
            prefix = "{ ",
            postfix = " }"
        ) { (key, value) ->
            val convertedKey = key.escape().doubleQuotedIfNotPure()
            "$convertedKey = $value"
        }
    }

    override fun equals(other: Any?): Boolean {
        return content == other
    }

    override fun hashCode(): Int {
        return content.hashCode()
    }
}

// ---- To TomlTable ----

/**
 * Convert [this] to TomlTable.
 *
 * @throws IllegalStateException when [this] is not TomlTable.
 */
public fun TomlElement.toTomlTable(): TomlTable {
    return this as? TomlTable ?: failConversion("TomlTable")
}

/**
 * Creates [TomlTable] from the given map [value].
 */
public fun TomlTable(value: Map<*, *>): TomlTable {
    val content = buildMap(value.size) {
        for ((k, v) in value) {
            put(k.toTomlKey(), v.toTomlElement())
        }
    }
    return TomlTable(content)
}

// ---- Extensions for TomlTable ----

/**
 * Get value along with path constructed by [keys].
 *
 * @param keys one for a single path segment.
 *
 * @throws NonPrimitiveKeyException if provide non-primitive key
 */
public operator fun TomlTable.get(vararg keys: Any?): TomlElement? {
    return getByPathRecursively(keys, 0)
}

// ======== Internal ========

internal fun TomlArray(value: ArrayNode): TomlArray {
    val content = value.children.map(TreeNode::toTomlElement)
    return TomlArray(content)
}

internal fun TomlTable(value: KeyNode): TomlTable {
    val content = value.children.associate { node ->
        node.key to node.toTomlElement()
    }
    return TomlTable(content)
}

private fun TreeNode.toTomlElement(): TomlElement {
    return when (this) {
        is KeyNode -> TomlTable(this)
        is ArrayNode -> TomlArray(this)
        is ValueNode -> value
    }
}

private tailrec fun TomlTable.getByPathRecursively(
    keys: Array<out Any?>,
    index: Int
): TomlElement? {
    val value = get(keys[index])
    return when {
        index == keys.lastIndex -> value
        value is TomlTable -> value.getByPathRecursively(keys, index + 1)
        else -> null
    }
}

internal fun Any?.toTomlKey(): String {
    return when (this) {
        is Boolean, is Number, is Char -> toString()
        is String -> this
        else -> throw NonPrimitiveKeyException()
    }
}

private fun Any?.toTomlElement(): TomlElement {
    return when (this) {
        null -> TomlNull
        is TomlElement -> this
        is Boolean -> TomlLiteral(this)
        is Byte -> TomlLiteral(this)
        is Short -> TomlLiteral(this)
        is Int -> TomlLiteral(this)
        is Long -> TomlLiteral(this)
        is Float -> TomlLiteral(this)
        is Double -> TomlLiteral(this)
        is Char -> TomlLiteral(this)
        is String -> TomlLiteral(this)
        is NativeLocalDateTime -> TomlLiteral(this)
        is NativeOffsetDateTime -> TomlLiteral(this)
        is NativeLocalDate -> TomlLiteral(this)
        is NativeLocalTime -> TomlLiteral(this)
        is BooleanArray -> TomlArray(this.asIterable())
        is ByteArray -> TomlArray(this.asIterable())
        is ShortArray -> TomlArray(this.asIterable())
        is IntArray -> TomlArray(this.asIterable())
        is LongArray -> TomlArray(this.asIterable())
        is FloatArray -> TomlArray(this.asIterable())
        is DoubleArray -> TomlArray(this.asIterable())
        is CharArray -> TomlArray(this.asIterable())
        is Array<*> -> TomlArray(this.asIterable())
        is Iterable<*> -> TomlArray(this)
        is Map<*, *> -> TomlTable(this)
        else -> error("Unsupported class: ${this::class.simpleName}")
    }
}

private fun TomlElement.failConversion(target: String): Nothing {
    error("Cannot convert ${this::class.simpleName} to $target")
}
