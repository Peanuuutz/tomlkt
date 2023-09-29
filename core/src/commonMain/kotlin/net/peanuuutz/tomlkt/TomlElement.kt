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
import net.peanuuutz.tomlkt.internal.throwNonPrimitiveKey
import net.peanuuutz.tomlkt.internal.toStringModified
import kotlin.contracts.contract

// -------- TomlElement --------

/**
 * Represents anything in TOML, including and only including [TomlNull],
 * [TomlLiteral], [TomlArray], [TomlTable].
 *
 * **Warning: Only use [Toml] to serialize/deserialize this or any subclass.**
 */
@Serializable(with = TomlElementSerializer::class)
public sealed class TomlElement {
    /**
     * The content of this element.
     */
    public abstract val content: Any?

    /**
     * Gives a string representation of this element.
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
 * Note: Currently the encoded value ("null") cannot be modified.
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
 * Casts [this] to [TomlNull].
 *
 * @throws IllegalStateException if `this` is not `TomlNull`.
 */
public fun TomlElement.asTomlNull(): TomlNull {
    contract { returns() implies (this@asTomlNull is TomlNull) }

    return this as? TomlNull ?: failConversion("TomlNull")
}

@Deprecated(
    message = "Use asTomlNull() instead to better reflect the intent.",
    replaceWith = ReplaceWith(
        expression = "asTomlNull()",
        imports = [
            "net.peanuuutz.tomlkt.asTomlNull"
        ]
    )
)
public fun TomlElement.toTomlNull(): TomlNull {
    return asTomlNull()
}

// -------- TomlLiteral --------

/**
 * Represents a literal value, which can be boolean, number, char, string.
 */
@Serializable(with = TomlLiteralSerializer::class)
public class TomlLiteral internal constructor(
    /**
     * The converted value. (See factory functions with the same name)
     */
    override val content: String,
    /**
     * Indicates whether this literal is actually a [Char] or [String].
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
 * Casts [this] to [TomlLiteral].
 *
 * @throws IllegalStateException if `this` is not `TomlLiteral`.
 */
public fun TomlElement.asTomlLiteral(): TomlLiteral {
    contract { returns() implies (this@asTomlLiteral is TomlLiteral) }

    return this as? TomlLiteral ?: failConversion("TomlLiteral")
}

@Deprecated(
    message = "Use asTomlLiteral() instead to better reflect the intent.",
    replaceWith = ReplaceWith(
        expression = "asTomlLiteral()",
        imports = [
            "net.peanuuutz.tomlkt.asTomlLiteral"
        ]
    )
)
public fun TomlElement.toTomlLiteral(): TomlLiteral {
    return asTomlLiteral()
}

/**
 * Creates a [TomlLiteral] from given boolean [value].
 */
public fun TomlLiteral(value: Boolean): TomlLiteral {
    return TomlLiteral(value.toString(), isString = false)
}

/**
 * Creates a [TomlLiteral] from given numeric [value].
 *
 * @see toStringModified
 */
public fun TomlLiteral(value: Number): TomlLiteral {
    return TomlLiteral(value.toStringModified(), isString = false)
}

/**
 * Creates a [TomlLiteral] from given `UByte` [value].
 */
public fun TomlLiteral(value: UByte): TomlLiteral {
    return TomlLiteral(value.toString(), isString = false)
}

/**
 * Creates a [TomlLiteral] from given `UShort` [value].
 */
public fun TomlLiteral(value: UShort): TomlLiteral {
    return TomlLiteral(value.toString(), isString = false)
}

/**
 * Creates a [TomlLiteral] from given `UInt` [value].
 */
public fun TomlLiteral(value: UInt): TomlLiteral {
    return TomlLiteral(value.toString(), isString = false)
}

/**
 * Creates a [TomlLiteral] from given `ULong` [value].
 */
public fun TomlLiteral(value: ULong): TomlLiteral {
    return TomlLiteral(value.toString(), isString = false)
}

/**
 * Creates a [TomlLiteral] from given char [value].
 */
public fun TomlLiteral(value: Char): TomlLiteral {
    return TomlLiteral(value.toString(), isString = true)
}

/**
 * Creates a [TomlLiteral] from given string [value].
 */
public fun TomlLiteral(value: String): TomlLiteral {
    return TomlLiteral(value, isString = true)
}

/**
 * Creates a [TomlLiteral] from given `TomlLocalDateTime` [value].
 */
public fun TomlLiteral(value: TomlLocalDateTime): TomlLiteral {
    return TomlLiteral(value.toString(), isString = false)
}

/**
 * Creates a [TomlLiteral] from given `TomlOffsetDateTime` [value].
 */
public fun TomlLiteral(value: TomlOffsetDateTime): TomlLiteral {
    return TomlLiteral(value.toString(), isString = false)
}

/**
 * Creates a [TomlLiteral] from given `TomlLocalDate` [value].
 */
public fun TomlLiteral(value: TomlLocalDate): TomlLiteral {
    return TomlLiteral(value.toString(), isString = false)
}

/**
 * Creates a [TomlLiteral] from given `TomlLocalTime` [value].
 */
public fun TomlLiteral(value: TomlLocalTime): TomlLiteral {
    return TomlLiteral(value.toString(), isString = false)
}

/**
 * Creates a [TomlLiteral] from given enum [value]. Delegates to the factory
 * function which consumes string.
 *
 * @param E the enum class which `value` belongs to.
 * @param serializersModule could be ignored, but for contextual serialization,
 * this should be present.
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
 * Returns [content][TomlLiteral] as boolean.
 *
 * @throws IllegalStateException if `content` cannot be converted
 * into boolean.
 */
public fun TomlLiteral.toBoolean(): Boolean {
    return toBooleanOrNull() ?: error("Cannot convert $this to Boolean")
}

/**
 * Returns [content][TomlLiteral] as boolean only if it is "true" or "false",
 * otherwise null.
 */
public fun TomlLiteral.toBooleanOrNull(): Boolean? {
    return when (content) {
        "true" -> true
        "false" -> false
        else -> null
    }
}

/**
 * Returns [content][TomlLiteral] as byte.
 *
 * @throws NumberFormatException if `content` cannot be converted into byte.
 */
public fun TomlLiteral.toByte(): Byte {
    return content.toByte()
}

/**
 * Returns [content][TomlLiteral] as byte only if it can be byte, otherwise null.
 */
public fun TomlLiteral.toByteOrNull(): Byte? {
    return content.toByteOrNull()
}

/**
 * Returns [content][TomlLiteral] as short.
 *
 * @throws NumberFormatException if `content` cannot be converted into short.
 */
public fun TomlLiteral.toShort(): Short {
    return content.toShort()
}

/**
 * Returns [content][TomlLiteral] as short only if it can be short, otherwise
 * null.
 */
public fun TomlLiteral.toShortOrNull(): Short? {
    return content.toShortOrNull()
}

/**
 * Returns [content][TomlLiteral] as int.
 *
 * @throws NumberFormatException if `content` cannot be converted into int.
 */
public fun TomlLiteral.toInt(): Int {
    return content.toInt()
}

/**
 * Returns [content][TomlLiteral] as int only if it can be int, otherwise null.
 */
public fun TomlLiteral.toIntOrNull(): Int? {
    return content.toIntOrNull()
}

/**
 * Returns [content][TomlLiteral] as long.
 *
 * @throws NumberFormatException if `content` cannot be converted into long.
 */
public fun TomlLiteral.toLong(): Long {
    return content.toLong()
}

/**
 * Returns [content][TomlLiteral] as long only if it can be long, otherwise null.
 */
public fun TomlLiteral.toLongOrNull(): Long? {
    return content.toLongOrNull()
}

/**
 * Returns [content][TomlLiteral] as float.
 *
 * @throws NumberFormatException if `content` cannot be converted into float.
 */
public fun TomlLiteral.toFloat(): Float {
    return toFloatOrNull() ?: throw NumberFormatException("Cannot convert $this to Float")
}

/**
 * Returns [content][TomlLiteral] as float only if it can be an exact float or
 * `inf`/`+inf`/`-inf`/`nan`, otherwise null.
 */
public fun TomlLiteral.toFloatOrNull(): Float? {
    return when (content) {
        "inf" -> Float.POSITIVE_INFINITY
        "+inf" -> Float.POSITIVE_INFINITY
        "-inf" -> Float.NEGATIVE_INFINITY
        "nan" -> Float.NaN
        else -> content.toFloatOrNull()
    }
}

/**
 * Returns [content][TomlLiteral] as double.
 *
 * @throws NumberFormatException if `content` cannot be converted into double.
 */
public fun TomlLiteral.toDouble(): Double {
    return toDoubleOrNull() ?: throw NumberFormatException("Cannot convert $this to Double")
}

/**
 * Returns [content][TomlLiteral] as double only if it can be an exact double or
 * `inf`/`+inf`/`-inf`/`nan`, otherwise null.
 */
public fun TomlLiteral.toDoubleOrNull(): Double? {
    return when (content) {
        "inf" -> Double.POSITIVE_INFINITY
        "+inf" -> Double.POSITIVE_INFINITY
        "-inf" -> Double.NEGATIVE_INFINITY
        "nan" -> Double.NaN
        else -> content.toDoubleOrNull()
    }
}

/**
 * Returns [content][TomlLiteral] as char.
 *
 * @throws NoSuchElementException if `content` is empty.
 * @throws IllegalArgumentException if `content` cannot be converted into char.
 */
public fun TomlLiteral.toChar(): Char {
    return content.single()
}

/**
 * Returns [content][TomlLiteral] as char only if the length of it is exactly 1,
 * otherwise null.
 */
public fun TomlLiteral.toCharOrNull(): Char? {
    return content.singleOrNull()
}

/**
 * Returns [content][TomlLiteral] as parsed [TomlLocalDateTime].
 *
 * @throws IllegalArgumentException if `content` cannot be parsed into
 * `TomlLocalDateTime`.
 */
public fun TomlLiteral.toLocalDateTime(): TomlLocalDateTime {
    return NativeLocalDateTime(content)
}

/**
 * Returns [content][TomlLiteral] as [TomlLocalDateTime] only if it can be
 * parsed into it, otherwise null.
 */
public fun TomlLiteral.toLocalDateTimeOrNull(): TomlLocalDateTime? {
    return try {
        toLocalDateTime()
    } catch (e: IllegalArgumentException) {
        null
    }
}

/**
 * Returns [content][TomlLiteral] as parsed [TomlOffsetDateTime].
 *
 * @throws IllegalArgumentException if `content` cannot be parsed into
 * `TomlOffsetDateTime`.
 */
public fun TomlLiteral.toOffsetDateTime(): TomlOffsetDateTime {
    return NativeOffsetDateTime(content)
}

/**
 * Returns [content][TomlLiteral] as [TomlOffsetDateTime] only if it can be
 * parsed into it, otherwise null.
 */
public fun TomlLiteral.toOffsetDateTimeOrNull(): TomlOffsetDateTime? {
    return try {
        toOffsetDateTime()
    } catch (e: IllegalArgumentException) {
        null
    }
}

/**
 * Returns [content][TomlLiteral] as parsed [TomlLocalDate].
 *
 * @throws IllegalArgumentException if `content` cannot be parsed into
 * `TomlLocalDate`.
 */
public fun TomlLiteral.toLocalDate(): TomlLocalDate {
    return NativeLocalDate(content)
}

/**
 * Returns [content][TomlLiteral] as [TomlLocalDate] only if it can be parsed
 * into it, otherwise null.
 */
public fun TomlLiteral.toLocalDateOrNull(): TomlLocalDate? {
    return try {
        toLocalDate()
    } catch (e: IllegalArgumentException) {
        null
    }
}

/**
 * Returns [content][TomlLiteral] as parsed [TomlLocalTime].
 *
 * @throws IllegalArgumentException if `content` cannot be parsed into
 * `TomlLocalTime`.
 */
public fun TomlLiteral.toLocalTime(): TomlLocalTime {
    return NativeLocalTime(content)
}

/**
 * Returns [content][TomlLiteral] as [TomlLocalTime] only if it can be parsed
 * into it, otherwise null.
 */
public fun TomlLiteral.toLocalTimeOrNull(): TomlLocalTime? {
    return try {
        toLocalTime()
    } catch (e: IllegalArgumentException) {
        null
    }
}

/**
 * Returns [content][TomlLiteral] as an enum value given the enum class context.
 *
 * @param E the enum class which `content` converts to.
 * @param serializersModule could be ignored, but for contextual serialization,
 * this should be present.
 *
 * @throws IllegalStateException if `content` cannot be converted into [E].
 */
public inline fun <reified E : Enum<E>> TomlLiteral.toEnum(
    serializersModule: SerializersModule = EmptySerializersModule()
): E {
    return toEnumOrNull<E>(serializersModule) ?: error("Cannot convert $this to ${E::class.simpleName}")
}

/**
 * Returns [content][TomlLiteral] as an enum value given the enum class context
 * only if it suits in, otherwise null.
 *
 * @param E the enum class which `content` converts to.
 * @param serializersModule could be ignored, but for contextual serialization,
 * this should be present.
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
 * Represents array in TOML, whose values are [TomlElement].
 *
 * This type delegates everything to [content], thus all the functions from
 * [List] could be used.
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

    public companion object {
        /**
         * An empty [TomlArray].
         */
        public val Empty: TomlArray = TomlArray(emptyList())
    }
}

// ---- To TomlArray ----

/**
 * Casts [this] to [TomlArray].
 *
 * @throws IllegalStateException if `this` is not `TomlArray`.
 */
public fun TomlElement.asTomlArray(): TomlArray {
    contract { returns() implies (this@asTomlArray is TomlArray) }

    return this as? TomlArray ?: failConversion("TomlArray")
}

@Deprecated(
    message = "Use asTomlArray() instead to better reflect the intent.",
    replaceWith = ReplaceWith(
        expression = "asTomlArray()",
        imports = [
            "net.peanuuutz.tomlkt.asTomlArray"
        ]
    )
)
public fun TomlElement.toTomlArray(): TomlArray {
    return asTomlArray()
}

/**
 * Creates a [TomlArray] from given `Iterable` [value].
 */
public fun TomlArray(value: Iterable<*>): TomlArray {
    if (value is Collection && value.isEmpty()) {
        return TomlArray.Empty
    }
    val content = value.map { it.toTomlElement() }
    return TomlArray(content)
}

/**
 * Creates a [TomlArray] with given [values].
 */
public fun TomlArray(vararg values: Any?): TomlArray {
    if (values.isEmpty()) {
        return TomlArray.Empty
    }
    val content = values.map { it.toTomlElement() }
    return TomlArray(content)
}

// -------- TomlTable --------

/**
 * Represents table in TOML, whose keys are strings and values are
 * [TomlElement]s.
 *
 * This type delegates everything to [content], thus all the functions from
 * [Map] could be used.
 */
@Serializable(with = TomlTableSerializer::class)
public class TomlTable internal constructor(
    override val content: Map<String, TomlElement>
) : TomlElement(), Map<String, TomlElement> by content {
    /**
     * More convenient means than [Map.get] if this table is originally a map
     * with **primitive** keys.
     *
     * @throws NonPrimitiveKeyException if provided non-primitive key.
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

    public companion object {
        /**
         * An empty [TomlTable].
         */
        public val Empty: TomlTable = TomlTable(emptyMap())
    }
}

// ---- To TomlTable ----

/**
 * Casts [this] to [TomlTable].
 *
 * @throws IllegalStateException if `this` is not `TomlTable`.
 */
public fun TomlElement.asTomlTable(): TomlTable {
    contract { returns() implies (this@asTomlTable is TomlTable) }

    return this as? TomlTable ?: failConversion("TomlTable")
}

@Deprecated(
    message = "Use asTomlTable() instead to better reflect the intent.",
    replaceWith = ReplaceWith(
        expression = "asTomlTable()",
        imports = [
            "net.peanuuutz.tomlkt.asTomlTable"
        ]
    )
)
public fun TomlElement.toTomlTable(): TomlTable {
    return asTomlTable()
}

/**
 * Creates a [TomlTable] from given `Map` [value].
 */
public fun TomlTable(value: Map<*, *>): TomlTable {
    return when (val size = value.size) {
        0 -> TomlTable.Empty
        1 -> {
            val (k, v) = value.entries.first()
            val content = mapOf(pair = k.toTomlKey() to v.toTomlElement())
            TomlTable(content)
        }
        else -> {
            val content = buildMap(size) {
                for ((k, v) in value) {
                    put(k.toTomlKey(), v.toTomlElement())
                }
            }
            TomlTable(content)
        }
    }
}

/**
 * Creates a [TomlTable] with given [entries].
 */
public fun TomlTable(vararg entries: Pair<*, *>): TomlTable {
    return when (val size = entries.size) {
        0 -> TomlTable.Empty
        1 -> {
            val (k, v) = entries[0]
            val content = mapOf(pair = k.toTomlKey() to v.toTomlElement())
            TomlTable(content)
        }
        else -> {
            val content = buildMap(size) {
                for ((k, v) in entries) {
                    put(k.toTomlKey(), v.toTomlElement())
                }
            }
            TomlTable(content)
        }
    }
}

// ---- Extensions for TomlTable ----

/**
 * Gets the element along the path constructed by [keys].
 *
 * Given the following TOML file:
 *
 * ```toml
 * i = 0
 *
 * [data]
 * list = [
 *     "any",
 *     { type: "integer", value: 0 }
 * ]
 * ```
 *
 * With `keys = ["data", "list", 1, "value"]`, the above table emits 0 (in
 * [TomlLiteral]).
 *
 * @param keys the path which leads to the value. Each one item is a single
 * segment. If a [TomlArray] is met, any direct child segment must be [Int] or
 * [String] (will be parsed into integer).
 *
 * @throws NonPrimitiveKeyException if provided non-primitive keys.
 */
public operator fun TomlTable.get(vararg keys: Any?): TomlElement? {
    return getByPathRecursively(keys, 0)
}

// ======== Internal ========

internal fun TomlArray(value: ArrayNode): TomlArray {
    val children = value.children
    return when (children.size) {
        0 -> TomlArray.Empty
        1 -> {
            val element = TomlTable(children[0])
            val content = listOf(element = element)
            TomlArray(content)
        }
        else -> {
            val content = children.map(::TomlTable)
            TomlArray(content)
        }
    }
}

internal fun TomlTable(value: KeyNode): TomlTable {
    val children = value.children
    if (children.isEmpty()) {
        return TomlTable.Empty
    }
    val content = buildMap(children.size) {
        for (node in children) {
            put(node.key, node.toTomlElement())
        }
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
    depth: Int
): TomlElement? {
    val key = keys[depth]
    val value = get(key)
    return when {
        depth == keys.lastIndex -> value
        value is TomlTable -> value.getByPathRecursively(keys, depth + 1)
        value is TomlArray -> value.getByPathRecursively(keys, depth + 1)
        else -> null
    }
}

private tailrec fun TomlArray.getByPathRecursively(
    keys: Array<out Any?>,
    depth: Int
): TomlElement? {
    val index = when (val key = keys[depth]) {
        is Int -> key
        is String -> key.toInt()
        else -> {
            val message = "Expect integer key when accessing TomlArray, but found $key"
            throw IllegalArgumentException(message)
        }
    }
    val value = get(index)
    return when {
        depth == keys.lastIndex -> value
        value is TomlTable -> value.getByPathRecursively(keys, depth + 1)
        value is TomlArray -> value.getByPathRecursively(keys, depth + 1)
        else -> null
    }
}

internal fun Any?.toTomlKey(): String {
    return when (this) {
        is Boolean, is Number, is Char -> toString()
        is String -> this
        else -> throwNonPrimitiveKey(this)
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
