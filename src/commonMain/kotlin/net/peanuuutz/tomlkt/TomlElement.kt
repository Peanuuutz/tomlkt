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

@file:OptIn(ExperimentalStdlibApi::class)
@file:Suppress("UNUSED")

package net.peanuuutz.tomlkt

import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import net.peanuuutz.tomlkt.internal.*
import net.peanuuutz.tomlkt.internal.TomlElementSerializer
import net.peanuuutz.tomlkt.internal.TomlNullSerializer
import net.peanuuutz.tomlkt.internal.parser.ArrayNode
import net.peanuuutz.tomlkt.internal.parser.KeyNode
import net.peanuuutz.tomlkt.internal.parser.ValueNode

// TomlElement

/**
 * Represents anything in TOML, including and only including [TomlNull], [TomlLiteral], [TomlArray], [TomlTable].
 *
 * **Warning: Only use [Toml] to serialize/deserialize any sub-class.**
 */
@Serializable(with = TomlElementSerializer::class)
public sealed class TomlElement {
    /**
     * The content of this TomlElement. Each sub-class has its own implementation.
     */
    public abstract val content: Any?

    /**
     * Gives a string representation of this TomlElement. Each sub-class has its own implementation.
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

// TomlNull

/**
 * Represents null.
 *
 * Note: Currently encoded value can NOT be modified.
 */
@Serializable(with = TomlNullSerializer::class)
public object TomlNull : TomlElement() {
    override val content: Nothing? = null

    override fun toString(): String = "null"
}

// To TomlNull

/**
 * Convert [this] to TomlNull.
 *
 * @throws IllegalStateException when [this] is not TomlNull
 */
public fun TomlElement.toTomlNull(): TomlNull = this as? TomlNull ?: failConversion("TomlNull")

// TomlLiteral

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
    override fun toString(): String = if (isString) content.escape().doubleQuoted else content

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

// To TomlLiteral

/**
 * Convert [this] to TomlLiteral.
 *
 * @throws IllegalStateException when [this] is not TomlLiteral.
 */
public fun TomlElement.toTomlLiteral(): TomlLiteral = this as? TomlLiteral ?: failConversion("TomlLiteral")

/**
 * Creates [TomlLiteral] from the given boolean [value].
 */
public fun TomlLiteral(value: Boolean): TomlLiteral = TomlLiteral(value.toString(), false)

/**
 * Creates [TomlLiteral] from the given numeric [value].
 *
 * @see toStringModified
 */
public fun TomlLiteral(value: Number): TomlLiteral = TomlLiteral(value.toStringModified(), false)

/**
 * Creates [TomlLiteral] from the given char [value].
 */
public fun TomlLiteral(value: Char): TomlLiteral = TomlLiteral(value.toString(), true)

/**
 * Creates [TomlLiteral] from the given string [value].
 */
public fun TomlLiteral(value: String): TomlLiteral = TomlLiteral(value, true)

/**
 * Creates [TomlLiteral] from the given enum [value]. Delegates to creator function which consumes string.
 *
 * @param E the enum class which [value] belongs to.
 * @param serializersModule in most case could be ignored, but for contextual it should be present.
 */
public inline fun <reified E : Enum<E>> TomlLiteral(
    value: E,
    serializersModule: SerializersModule = EmptySerializersModule
): TomlLiteral = TomlLiteral(serializersModule.serializer<E>().descriptor.getElementName(value.ordinal))

// From TomlLiteral

/**
 * Returns content as boolean.
 *
 * @throws IllegalStateException if content cannot be converted into boolean.
 */
public fun TomlLiteral.toBoolean(): Boolean = toBooleanOrNull() ?: error("Cannot convert $this to Boolean")

/**
 * Returns content as boolean only if content is "true" or "false", otherwise null.
 */
public fun TomlLiteral.toBooleanOrNull(): Boolean? = when (content) {
    "true" -> true
    "false" -> false
    else -> null
}

/**
 * Returns content as byte.
 *
 * @throws NumberFormatException if content cannot be converted into byte.
 */
public fun TomlLiteral.toByte(): Byte = content.toByte()

/**
 * Returns content as byte only if content can be byte, otherwise null.
 */
public fun TomlLiteral.toByteOrNull(): Byte? = content.toByteOrNull()

/**
 * Returns content as short.
 *
 * @throws NumberFormatException if content cannot be converted into short.
 */
public fun TomlLiteral.toShort(): Short = content.toShort()

/**
 * Returns content as short only if content can be short, otherwise null.
 */
public fun TomlLiteral.toShortOrNull(): Short? = content.toShortOrNull()

/**
 * Returns content as int.
 *
 * @throws NumberFormatException if content cannot be converted into int.
 */
public fun TomlLiteral.toInt(): Int = content.toInt()

/**
 * Returns content as int only if content can be int, otherwise null.
 */
public fun TomlLiteral.toIntOrNull(): Int? = content.toIntOrNull()

/**
 * Returns content as long.
 *
 * @throws NumberFormatException if content cannot be converted into long.
 */
public fun TomlLiteral.toLong(): Long = content.toLong()

/**
 * Returns content as long only if content can be long, otherwise null.
 */
public fun TomlLiteral.toLongOrNull(): Long? = content.toLongOrNull()

/**
 * Returns content as float.
 *
 * @throws NumberFormatException if content cannot be converted into float.
 */
public fun TomlLiteral.toFloat(): Float = toFloatOrNull() ?: throw NumberFormatException("Cannot convert $this to Float")

/**
 * Returns content as float only if content can be an exact float or inf/-inf/nan, otherwise null.
 */
public fun TomlLiteral.toFloatOrNull(): Float? = when (content) {
    "inf" -> Float.POSITIVE_INFINITY
    "-inf" -> Float.NEGATIVE_INFINITY
    "nan" -> Float.NaN
    else -> content.toFloatOrNull()
}

/**
 * Returns content as double.
 *
 * @throws NumberFormatException if content cannot be converted into double.
 */
public fun TomlLiteral.toDouble(): Double = toDoubleOrNull() ?: throw NumberFormatException("Cannot convert $this to Double")

/**
 * Returns content as double only if content can be an exact double or inf/-inf/nan, otherwise null.
 */
public fun TomlLiteral.toDoubleOrNull(): Double? = when (content) {
    "inf" -> Double.POSITIVE_INFINITY
    "-inf" -> Double.NEGATIVE_INFINITY
    "nan" -> Double.NaN
    else -> content.toDoubleOrNull()
}

/**
 * Returns content as char.
 *
 * @throws NoSuchElementException if content is empty.
 * @throws IllegalArgumentException if content cannot be converted into char.
 */
public fun TomlLiteral.toChar(): Char = content.single()

/**
 * Returns content as char only if the length of content is exactly 1, otherwise null.
 */
public fun TomlLiteral.toCharOrNull(): Char? = content.singleOrNull()

/**
 * Returns content as enum with given enum class context.
 *
 * @param E the enum class which [this] converts to.
 * @param serializersModule in most case could be ignored, but for contextual it should be present.
 *
 * @throws IllegalStateException if content cannot be converted into [E].
 */
public inline fun <reified E : Enum<E>> TomlLiteral.toEnum(
    serializersModule: SerializersModule = EmptySerializersModule
): E = toEnumOrNull<E>(serializersModule) ?: error("Cannot convert $this to ${E::class.simpleName}")

/**
 * Returns content as enum with given enum class context only if content suits in, otherwise null.
 *
 * @param E the enum class which [this] converts to.
 * @param serializersModule in most case could be ignored, but for contextual it should be present.
 */
public inline fun <reified E : Enum<E>> TomlLiteral.toEnumOrNull(
    serializersModule: SerializersModule = EmptySerializersModule
): E? {
    val index = serializersModule.serializer<E>().descriptor.elementNames.indexOf(content)
    return if (index != -1) enumValues<E>()[index] else null
}

// TomlArray

/**
 * Represents array in TOML, which values are [TomlElement].
 *
 * As it delegates to list [content], everything in [List] could be used.
 */
@Serializable(with = TomlArraySerializer::class)
public class TomlArray internal constructor(
    override val content: List<TomlElement>
) : TomlElement(), List<TomlElement> by content {
    override fun toString(): String = content.joinToString(
        prefix = "[ ",
        postfix = " ]"
    )

    override fun equals(other: Any?): Boolean = content == other

    override fun hashCode(): Int = content.hashCode()
}

// To TomlArray

/**
 * Convert [this] to TomlArray.
 *
 * @throws IllegalStateException when [this] is not TomlArray.
 */
public fun TomlElement.toTomlArray(): TomlArray = this as? TomlArray ?: failConversion("TomlArray")

/**
 * Creates [TomlArray] from the given iterable [value].
 */
public fun TomlArray(value: Iterable<*>): TomlArray = TomlArray(value.map(Any?::toTomlElement))

// TomlTable

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
     * More convenient than [Map.get] if this TomlTable is originally a map with **primitive** keys
     *
     * @throws NonPrimitiveKeyException if provide non-primitive key
     */
    public operator fun get(key: Any?): TomlElement? = get(key.toTomlKey())

    override fun toString(): String = content.entries.joinToString(
        prefix = "{ ",
        postfix = " }"
    ) { (k, v) -> "${k.escape().doubleQuotedIfNeeded()} = $v" }

    override fun equals(other: Any?): Boolean = content == other

    override fun hashCode(): Int = content.hashCode()
}

// To TomlTable

/**
 * Convert [this] to TomlTable.
 *
 * @throws IllegalStateException when [this] is not TomlTable.
 */
public fun TomlElement.toTomlTable(): TomlTable = this as? TomlTable ?: failConversion("TomlTable")

/**
 * Creates [TomlTable] from the given map [value].
 */
public fun TomlTable(value: Map<*, *>): TomlTable = TomlTable(buildMap(value.size) {
    value.forEach { (k, v) -> put(k.toTomlKey(), v.toTomlElement()) }
})

// Extensions for TomlTable

/**
 * Get value along with path constructed by [keys].
 *
 * @param keys one for a single path segment.
 *
 * @throws NonPrimitiveKeyException if provide non-primitive key
 */
public operator fun TomlTable.get(vararg keys: Any?): TomlElement? = getByPathRecursively(keys, 0)

// Internal

internal fun TomlTable(value: KeyNode): TomlTable = TomlTable(buildMap(value.children.size) {
    value.children.forEach { put(it.key, it.toTomlElement()) }
})

private tailrec fun TomlTable.getByPathRecursively(
    keys: Array<out Any?>,
    index: Int
): TomlElement? {
    val value = get(keys[index])
    return if (index == keys.lastIndex)
        value
    else when (value) {
        is TomlTable -> value.getByPathRecursively(keys, index + 1)
        TomlNull, is TomlLiteral, is TomlArray, null -> null
    }
}

internal fun Any?.toTomlKey(): String = when (this) {
    is Boolean, is Number, is Char -> toString()
    is String -> this
    else -> throw NonPrimitiveKeyException()
}

private fun Any?.toTomlElement(): TomlElement = when (this) {
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
    is KeyNode -> TomlTable(this)
    is ArrayNode -> TomlArray(array)
    is ValueNode -> value
    else -> error("Unsupported class: ${this::class.simpleName}")
}

private fun TomlElement.failConversion(target: String): Nothing = error("Cannot convert ${this::class.simpleName} to $target")