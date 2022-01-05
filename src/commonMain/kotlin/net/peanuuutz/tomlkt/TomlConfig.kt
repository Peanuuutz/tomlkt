package net.peanuuutz.tomlkt

import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

internal data class TomlConfig(
    val serializersModule: SerializersModule = EmptySerializersModule,
    val checkArrayInMap: Boolean = true,
    val ignoreUnknownKeys: Boolean = false
)

public class TomlConfigBuilder internal constructor(from: TomlConfig) {
    public var serializersModule: SerializersModule = from.serializersModule

    // Serialization

    public var checkArrayInMap: Boolean = from.checkArrayInMap

    // Deserialization

    public var ignoreUnknownKeys: Boolean = from.ignoreUnknownKeys

    // Internal

    internal fun build(): TomlConfig = TomlConfig(
        serializersModule,
        checkArrayInMap,
        ignoreUnknownKeys
    )
}