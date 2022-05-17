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

@file:Suppress("UNUSED")

package net.peanuuutz.tomlkt

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import net.peanuuutz.tomlkt.internal.*
import net.peanuuutz.tomlkt.internal.TomlElementDecoder
import net.peanuuutz.tomlkt.internal.TomlElementEncoder
import net.peanuuutz.tomlkt.internal.TomlFileEncoder
import net.peanuuutz.tomlkt.internal.parser.TomlFileParser

/**
 * The main entry point to use TOML.
 *
 * User could simple use [Default] instance or customize by using creator function with the same name.
 *
 * Basic usage:
 *
 * ```kotlin
 * @Serializable
 * data class User(
 *     val name: String,
 *     val account: Account? // Nullability
 * )
 *
 * @Serializable
 * data class Account(
 *     val username: String,
 *     val password: String
 * )
 *
 * // Now with [Default] instance
 * val user = User("Peanuuutz", Account("Peanuuutz", "123456"))
 * // Either is OK, but to explicitly pass a serializer is faster
 * val tomlString = Toml.encodeToString(User.serializer(), user)
 * val tomlString = Toml.encodeToString<User>(user)
 * // Print it
 * println(tomlString)
 * /*
 * name = "Peanuuutz"
 *
 * [account]
 * username = "Peanuuutz"
 * password = "123456"
 * */
 *
 * // And to reverse...
 * val user = Toml.decodeFromString(User.serializer(), tomlString)
 * // Print it
 * println(user)
 * // User(name=Peanuuutz,account=Account(username=Peanuuutz,password=123456))
 *
 * // Or you're lazy to create model class, try [TomlElement]
 * val config = Toml.parseToTomlTable(tomlString)
 * // Now access to all entry (think you need getByPath)
 * val password: TomlLiteral = config["account", "password"]!!.toTomlLiteral()
 * ```
 *
 * @see TomlConfigBuilder
 * @see TomlElement
 */
public sealed class Toml(
    internal val config: TomlConfig,
    override val serializersModule: SerializersModule = config.serializersModule
) : StringFormat {
    /**
     * Default implementation of [Toml] with default config.
     *
     * @see TomlConfigBuilder
     */
    public companion object Default : Toml(TomlConfig())

    /**
     * Serializes [value] into TOML string using [serializer].
     *
     * @throws TomlEncodingException when [value] cannot be serialized.
     */
    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String {
        val stringBuilder = StringBuilder()
        serializer.serialize(TomlFileEncoder(config, serializersModule, stringBuilder), value)
        return stringBuilder.trim().toString() // Consider removing that trim()
    }

    /**
     * Serializes [value] into [TomlElement] using [serializer].
     *
     * @throws TomlEncodingException when [value] cannot be serialized.
     */
    public fun <T> encodeToTomlElement(serializer: SerializationStrategy<T>, value: T): TomlElement {
        val encoder = TomlElementEncoder(config, serializersModule)
        serializer.serialize(encoder, value)
        return encoder.element
    }

    /**
     * Deserializes [string] into a value of type [T] using [deserializer].
     *
     * @param string **MUST** be a TOML file, as this method delegates parsing to [parseToTomlTable].
     *
     * @throws TomlDecodingException when [string] cannot be parsed into [TomlTable] or cannot be deserialized.
     */
    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T
        = deserializer.deserialize(TomlElementDecoder(config, serializersModule, parseToTomlTable(string)))

    /**
     * Deserializes [element] into a value of type [T] using [deserializer].
     *
     * @throws TomlDecodingException when [element] cannot be deserialized.
     */
    public fun <T> decodeFromTomlElement(deserializer: DeserializationStrategy<T>, element: TomlElement): T
        = deserializer.deserialize(TomlElementDecoder(config, serializersModule, element))

    /**
     * Parses [string] into equivalent representation of [TomlTable].
     *
     * @throws TomlDecodingException when [string] cannot be parsed into [TomlTable].
     */
    public fun parseToTomlTable(string: String): TomlTable = TomlFileParser(string).parse()
}

/**
 * Factory function for creating custom instance of [Toml].
 *
 * @param from the original Toml instance. [Toml.Default] by default.
 * @param config builder DSL with [TomlConfigBuilder].
 */
public fun Toml(
    from: Toml = Toml,
    config: TomlConfigBuilder.() -> Unit
): Toml = TomlImpl(TomlConfigBuilder(from.config).apply(config).build())

/**
 * Serializes [value] into [TomlElement] using serializer retrieved from reified type parameter.
 *
 * @throws TomlEncodingException when [value] cannot be serialized.
 */
public inline fun <reified T> Toml.encodeToTomlElement(value: T): TomlElement
    = encodeToTomlElement(serializersModule.serializer(), value)

/**
 * Deserializes [element] into a value of type [T] using serializer retrieved from reified type parameter.
 *
 * @throws TomlDecodingException when [element] cannot be deserialized.
 */
public inline fun <reified T> Toml.decodeFromTomlElement(element: TomlElement): T
    = decodeFromTomlElement(serializersModule.serializer(), element)

// Internal

internal class TomlImpl(config: TomlConfig) : Toml(config)