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

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import net.peanuuutz.tomlkt.Toml.Default
import net.peanuuutz.tomlkt.internal.NonPrimitiveKeyException
import net.peanuuutz.tomlkt.internal.TomlDecodingException
import net.peanuuutz.tomlkt.internal.TomlEncodingException
import net.peanuuutz.tomlkt.internal.decoder.TomlElementDecoder
import net.peanuuutz.tomlkt.internal.encoder.TomlElementEncoder
import net.peanuuutz.tomlkt.internal.encoder.TomlFileEncoder
import net.peanuuutz.tomlkt.internal.parser.TomlFileParser

/**
 * The main entry point to use TOML.
 *
 * Programmer could simply use [Default] instance or customize configuration by
 * using the factory function with the same name.
 *
 * Basic usage:
 *
 * ```kotlin
 * @Serializable
 * data class User(
 *     val name: String,
 *     val account: Account? // Nullable property.
 * )
 *
 * @Serializable
 * data class Account(
 *     val username: String,
 *     val password: String
 * )
 *
 * val user = User("Peanuuutz", Account("Peanuuutz", "123456"))
 * // With [Default] instance.
 * // Either is OK, but to explicitly pass a serializer is faster.
 * val tomlString = Toml.encodeToString(User.serializer(), user)
 * val tomlString = Toml.encodeToString<User>(user)
 * // Print it.
 * println(tomlString)
 * /*
 * name = "Peanuuutz"
 *
 * [account]
 * username = "Peanuuutz"
 * password = "123456"
 * */
 *
 * // Vice versa.
 * val user = Toml.decodeFromString(User.serializer(), tomlString)
 * // Print it.
 * println(user)
 * /*
 * User(name=Peanuuutz,account=Account(username=Peanuuutz,password=123456))
 * */
 *
 * // If you don't have a model class, try [TomlElement].
 * val config = Toml.parseToTomlTable(tomlString)
 * // Now you can access all the entries (this is an extension as "get by path").
 * val password = config["account", "password"]!!.toTomlLiteral().content
 * ```
 *
 * @see TomlConfigBuilder
 * @see TomlElement
 */
public sealed class Toml(
    @PublishedApi internal val config: TomlConfig,
    override val serializersModule: SerializersModule = config.serializersModule
) : StringFormat {
    /**
     * The default implementation of [Toml] with default configuration.
     *
     * @see TomlConfigBuilder
     */
    public companion object Default : Toml(TomlConfig.Default)

    /**
     * Serializes [value] into TOML string using [serializer].
     *
     * @throws TomlEncodingException if `value` cannot be serialized.
     */
    override fun <T> encodeToString(
        serializer: SerializationStrategy<T>,
        value: T
    ): String {
        val writer = TomlStringWriter()
        encodeToWriter(
            serializer = serializer,
            value = value,
            writer = writer
        )
        return writer.toString()
    }

    /**
     * Serializes [value] into [writer] using [serializer].
     *
     * @throws TomlEncodingException if `value` cannot be serialized.
     */
    public fun <T> encodeToWriter(
        serializer: SerializationStrategy<T>,
        value: T,
        writer: TomlWriter
    ) {
        val encoder = TomlFileEncoder(
            config = config,
            serializersModule = serializersModule,
            writer = writer
        )
        encoder.encodeSerializableValue(serializer, value)
    }

    /**
     * Serializes [value] into [TomlElement] using [serializer].
     *
     * @throws TomlEncodingException if `value` cannot be serialized.
     */
    public fun <T> encodeToTomlElement(
        serializer: SerializationStrategy<T>,
        value: T
    ): TomlElement {
        val encoder = TomlElementEncoder(config, serializersModule)
        encoder.encodeSerializableValue(serializer, value)
        return encoder.element
    }

    /**
     * Deserializes [string] into a value of type [T] using [deserializer].
     *
     * @param string **MUST** be a TOML file, as this method delegates parsing
     * to [parseToTomlTable].
     *
     * @throws TomlDecodingException if `string` cannot be parsed into
     * [TomlTable] or cannot be deserialized.
     */
    @Suppress("OutdatedDocumentation")
    override fun <T> decodeFromString(
        deserializer: DeserializationStrategy<T>,
        string: String
    ): T {
        val table = parseToTomlTable(string)
        return decodeFromTomlElement(deserializer, table)
    }

    /**
     * Parses [string] into a [TomlTable] and deserializes the corresponding
     * element fetched with [keys] into a value of type [T] using [deserializer].
     *
     * @param string **MUST** be a TOML file, as this method delegates parsing
     * to [parseToTomlTable].
     * @param keys the path which leads to the value. Each one item is a single
     * segment. If a [TomlArray] is met, any direct child segment must be [Int]
     * or [String] (will be parsed into integer).
     *
     * @throws TomlDecodingException if `string` cannot be parsed into
     * [TomlTable] or the element cannot be deserialized.
     * @throws NonPrimitiveKeyException if provided non-primitive keys.
     *
     * @see get
     */
    @Suppress("OutdatedDocumentation")
    public fun <T> decodeFromString(
        deserializer: DeserializationStrategy<T>,
        string: String,
        vararg keys: Any?
    ): T {
        val table = parseToTomlTable(string)
        val element = table.get(keys = keys)!!
        return decodeFromTomlElement(deserializer, element)
    }

    /**
     * Deserializes [element] into a value of type [T] using [deserializer].
     *
     * @throws TomlDecodingException if `element` cannot be deserialized.
     */
    public fun <T> decodeFromTomlElement(
        deserializer: DeserializationStrategy<T>,
        element: TomlElement
    ): T {
        val decoder = TomlElementDecoder(
            config = config,
            serializersModule = serializersModule,
            element = element
        )
        return decoder.decodeSerializableValue(deserializer)
    }

    /**
     * Parses [string] into equivalent representation of [TomlTable].
     *
     * @throws TomlDecodingException if `string` cannot be parsed into
     * [TomlTable].
     */
    public fun parseToTomlTable(string: String): TomlTable {
        val refined = refine(string)
        return TomlFileParser(refined).parse()
    }
}

/**
 * Factory function for customizing [Toml].
 *
 * @param from the [Toml] instance from which the default values are read.
 * [Toml.Default] by default.
 * @param config builder DSL with `this` as [TomlConfigBuilder].
 */
public inline fun Toml(
    from: Toml = Toml,
    config: TomlConfigBuilder.() -> Unit
): Toml = TomlImpl(TomlConfigBuilder(from.config).apply(config).build())

/**
 * Serializes [value] into [writer] using serializer retrieved from reified type
 * parameter.
 *
 * @throws TomlEncodingException if `value` cannot be serialized.
 */
public inline fun <reified T> Toml.encodeToWriter(
    value: T,
    writer: TomlWriter
) {
    encodeToWriter(
        serializer = serializersModule.serializer(),
        value = value,
        writer = writer
    )
}

/**
 * Serializes [value] into [TomlElement] using serializer retrieved from reified
 * type parameter.
 *
 * @throws TomlEncodingException if `value` cannot be serialized.
 */
public inline fun <reified T> Toml.encodeToTomlElement(value: T): TomlElement {
    return encodeToTomlElement(serializersModule.serializer(), value)
}

/**
 * Parses [string] into a [TomlTable] and deserializes the corresponding
 * element fetched with [keys] into a value of type [T] using serializer
 * retrieved from reified type parameter.
 *
 * @param string **MUST** be a TOML file, as this method delegates parsing
 * to [Toml.parseToTomlTable].
 * @param keys the path which leads to the value. Each one item is a single
 * segment. If a [TomlArray] is met, any direct child segment must be [Int]
 * or [String] (will be parsed into integer).
 *
 * @throws TomlDecodingException if `string` cannot be parsed into
 * [TomlTable] or the element cannot be deserialized.
 * @throws NonPrimitiveKeyException if provided non-primitive keys.
 *
 * @see get
 */
@Suppress("OutdatedDocumentation")
public inline fun <reified T> Toml.decodeFromString(
    string: String,
    vararg keys: Any?
): T {
    return decodeFromString(serializersModule.serializer(), string, keys = keys)
}

/**
 * Deserializes [element] into a value of type [T] using serializer retrieved
 * from reified type parameter.
 *
 * @throws TomlDecodingException if `element` cannot be deserialized.
 */
public inline fun <reified T> Toml.decodeFromTomlElement(element: TomlElement): T {
    return decodeFromTomlElement(serializersModule.serializer(), element)
}

// ======== Internal ========

@PublishedApi
internal class TomlImpl(config: TomlConfig) : Toml(config)

private fun refine(raw: String): String {
    return raw.replace("\r\n", "\n")
}
