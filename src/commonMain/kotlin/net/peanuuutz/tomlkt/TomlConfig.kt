package net.peanuuutz.tomlkt

import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import net.peanuuutz.tomlkt.internal.UnknownKeyException

/**
 * Builder provided for `Toml { ... }` factory function.
 */
public class TomlConfigBuilder internal constructor(from: TomlConfig) {
    /**
     * SerializersModule with contextual serializers to be used in the Toml instance.
     *
     * [EmptySerializersModule] by default.
     */
    public var serializersModule: SerializersModule = from.serializersModule

    // Serialization

    /**
     * Specifies whether to check empty array of table as value inside table.
     *
     * Due to no context before encoding value(array of table), encoder cannot decide whether to
     * encode key as table-like `[[key]]` or key-value-like `key = [...]`.
     *
     * **If set to `false`, unspecific behavior would be witnessed.**
     *
     * `true` by default.
     */
    public var checkArrayInMap: Boolean = from.checkArrayInMap

    // Deserialization

    /**
     * Specifies whether encounters of unknown keys should be ignored instead of throwing [UnknownKeyException].
     *
     * `false` by default.
     */
    public var ignoreUnknownKeys: Boolean = from.ignoreUnknownKeys

    // Internal

    internal fun build(): TomlConfig = TomlConfig(
        serializersModule,
        checkArrayInMap,
        ignoreUnknownKeys
    )
}

// Internal

internal data class TomlConfig(
    val serializersModule: SerializersModule = EmptySerializersModule,
    val checkArrayInMap: Boolean = true,
    val ignoreUnknownKeys: Boolean = false
)