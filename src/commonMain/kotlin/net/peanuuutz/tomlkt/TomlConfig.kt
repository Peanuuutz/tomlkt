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

    @Deprecated("Issue solved. Now empty array of table in map will be fine")
    public var checkArrayInMap: Boolean = false

    /**
     * Specifies how many items are encoded per line in block array.
     *
     * 1 by default.
     */
    public var itemsPerLineInBlockArray: Int = from.itemsPerLineInBlockArray

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
        itemsPerLineInBlockArray,
        ignoreUnknownKeys
    )
}

// Internal

internal data class TomlConfig(
    val serializersModule: SerializersModule = EmptySerializersModule,
    val itemsPerLineInBlockArray: Int = 1,
    val ignoreUnknownKeys: Boolean = false
)