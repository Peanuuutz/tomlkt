@file:Suppress("UNUSED")

package net.peanuuutz.tomlkt

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import net.peanuuutz.tomlkt.internal.TomlElementDecoder
import net.peanuuutz.tomlkt.internal.TomlFileEncoder
import net.peanuuutz.tomlkt.internal.parser.TomlFileParser

public sealed class Toml(
    internal val config: TomlConfig,
    override val serializersModule: SerializersModule = config.serializersModule
) : StringFormat {
    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String {
        val stringBuilder = StringBuilder()
        serializer.serialize(TomlFileEncoder(config, serializersModule, stringBuilder), value)
        return stringBuilder.trim().toString()
    }

    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T
        = deserializer.deserialize(TomlElementDecoder(config, serializersModule, parseToTomlTable(string)))

    public fun <T> decodeFromTomlElement(deserializer: DeserializationStrategy<T>, element: TomlElement): T
        = deserializer.deserialize(TomlElementDecoder(config, serializersModule, element))

    public fun parseToTomlTable(string: String): TomlTable
        = TomlFileParser(string).parse()

    public companion object Default : Toml(TomlConfig())
}

public fun Toml(
    from: Toml = Toml,
    config: TomlConfigBuilder.() -> Unit = {}
): Toml = TomlImpl(TomlConfigBuilder(from.config).apply(config).build())

public inline fun <reified T> Toml.decodeFromTomlElement(element: TomlElement): T
    = decodeFromTomlElement(serializersModule.serializer(), element)

// Internal

internal class TomlImpl(config: TomlConfig) : Toml(config)