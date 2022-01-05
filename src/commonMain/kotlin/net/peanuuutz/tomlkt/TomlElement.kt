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

@Serializable(with = TomlElementSerializer::class)
public sealed class TomlElement {
    public abstract val content: Any?

    public abstract override fun toString(): String
}

// TomlNull

@Serializable(with = TomlNullSerializer::class)
public object TomlNull : TomlElement() {
    override val content: Nothing? = null

    override fun toString(): String = "null"
}

public fun TomlElement.toTomlNull(): TomlNull = this as? TomlNull ?: failConversion("TomlNull")

// TomlLiteral

@Serializable(with = TomlLiteralSerializer::class)
public class TomlLiteral internal constructor(
    override val content: String,
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

public fun TomlElement.toTomlLiteral(): TomlLiteral = this as? TomlLiteral ?: failConversion("TomlLiteral")

public fun TomlLiteral(value: Boolean): TomlLiteral = TomlLiteral(value.toString(), false)

public fun TomlLiteral(value: Number): TomlLiteral = TomlLiteral(value.toStringModified(), false)

public fun TomlLiteral(value: Char): TomlLiteral = TomlLiteral(value.toString(), true)

public fun TomlLiteral(value: String): TomlLiteral = TomlLiteral(value, true)

public inline fun <reified E : Enum<E>> TomlLiteral(
    value: E,
    serializersModule: SerializersModule = EmptySerializersModule
): TomlLiteral = TomlLiteral(serializersModule.serializer<E>().descriptor.getElementName(value.ordinal))

// From TomlLiteral

public fun TomlLiteral.toBoolean(): Boolean = toBooleanOrNull() ?: error("Cannot convert $this to Boolean")

public fun TomlLiteral.toBooleanOrNull(): Boolean? = when (content) {
    "true" -> true
    "false" -> false
    else -> null
}

public fun TomlLiteral.toByte(): Byte = content.toByte()

public fun TomlLiteral.toByteOrNull(): Byte? = content.toByteOrNull()

public fun TomlLiteral.toShort(): Short = content.toShort()

public fun TomlLiteral.toShortOrNull(): Short? = content.toShortOrNull()

public fun TomlLiteral.toInt(): Int = content.toInt()

public fun TomlLiteral.toIntOrNull(): Int? = content.toIntOrNull()

public fun TomlLiteral.toLong(): Long = content.toLong()

public fun TomlLiteral.toLongOrNull(): Long? = content.toLongOrNull()

public fun TomlLiteral.toFloat(): Float = toFloatOrNull() ?: error("Cannot convert $this to Float")

public fun TomlLiteral.toFloatOrNull(): Float? = when (content) {
    "inf" -> Float.POSITIVE_INFINITY
    "-inf" -> Float.NEGATIVE_INFINITY
    "nan" -> Float.NaN
    else -> content.toFloatOrNull()
}

public fun TomlLiteral.toDouble(): Double = toDoubleOrNull() ?: error("Cannot convert $this to Double")

public fun TomlLiteral.toDoubleOrNull(): Double? = when (content) {
    "inf" -> Double.POSITIVE_INFINITY
    "-inf" -> Double.NEGATIVE_INFINITY
    "nan" -> Double.NaN
    else -> content.toDoubleOrNull()
}

public fun TomlLiteral.toChar(): Char = content.single()

public fun TomlLiteral.toCharOrNull(): Char? = content.singleOrNull()

public inline fun <reified E : Enum<E>> TomlLiteral.toEnum(
    serializersModule: SerializersModule = EmptySerializersModule
): E = toEnumOrNull<E>(serializersModule) ?: error("Cannot convert $this to ${E::class.simpleName}")

public inline fun <reified E : Enum<E>> TomlLiteral.toEnumOrNull(
    serializersModule: SerializersModule = EmptySerializersModule
): E? {
    val index = serializersModule.serializer<E>().descriptor.elementNames.indexOf(content)
    return if (index != -1) enumValues<E>()[index] else null
}

// TomlArray

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

public fun TomlElement.toTomlArray(): TomlArray = this as? TomlArray ?: failConversion("TomlArray")

public fun TomlArray(value: BooleanArray): TomlArray = TomlArray(value.map(::TomlLiteral))

public fun TomlArray(value: ByteArray): TomlArray = TomlArray(value.map(::TomlLiteral))

public fun TomlArray(value: ShortArray): TomlArray = TomlArray(value.map(::TomlLiteral))

public fun TomlArray(value: IntArray): TomlArray = TomlArray(value.map(::TomlLiteral))

public fun TomlArray(value: LongArray): TomlArray = TomlArray(value.map(::TomlLiteral))

public fun TomlArray(value: FloatArray): TomlArray = TomlArray(value.map(::TomlLiteral))

public fun TomlArray(value: DoubleArray): TomlArray = TomlArray(value.map(::TomlLiteral))

public fun TomlArray(value: CharArray): TomlArray = TomlArray(value.map(::TomlLiteral))

public fun TomlArray(value: Array<*>): TomlArray = TomlArray(value.map(Any?::toTomlElement))

public fun TomlArray(value: Iterable<*>): TomlArray = TomlArray(value.map(Any?::toTomlElement))

// TomlTable

@Serializable(with = TomlTableSerializer::class)
public class TomlTable internal constructor(
    override val content: Map<String, TomlElement>
) : TomlElement(), Map<String, TomlElement> by content {
    public operator fun get(value: Any?): TomlElement? = get(value.toTomlKey())

    override fun toString(): String = content.entries.joinToString(
        prefix = "{ ",
        postfix = " }"
    ) { (k, v) -> "${k.escape().doubleQuotedIfNeeded()} = $v" }

    override fun equals(other: Any?): Boolean = content == other

    override fun hashCode(): Int = content.hashCode()
}

// To TomlTable

public fun TomlElement.toTomlTable(): TomlTable = this as? TomlTable ?: failConversion("TomlTable")

public fun TomlTable(value: Map<*, *>): TomlTable = TomlTable(buildMap(value.size) {
    value.forEach { (k, v) -> put(k.toTomlKey(), v.toTomlElement()) }
})

// Internal

internal fun TomlTable(value: KeyNode): TomlTable = TomlTable(buildMap(value.children.size) {
    value.children.forEach { put(it.key, it.toTomlElement()) }
})

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
    is BooleanArray -> TomlArray(this)
    is ByteArray -> TomlArray(this)
    is ShortArray -> TomlArray(this)
    is IntArray -> TomlArray(this)
    is LongArray -> TomlArray(this)
    is FloatArray -> TomlArray(this)
    is DoubleArray -> TomlArray(this)
    is CharArray -> TomlArray(this)
    is Array<*> -> TomlArray(this)
    is Iterable<*> -> TomlArray(this)
    is Map<*, *> -> TomlTable(this)
    is KeyNode -> TomlTable(this)
    is ArrayNode -> TomlArray(array)
    is ValueNode -> value
    else -> error("Unsupported class: ${this::class.simpleName}")
}

private fun TomlElement.failConversion(target: String): Nothing = error("Cannot convert ${this::class.simpleName} to $target")