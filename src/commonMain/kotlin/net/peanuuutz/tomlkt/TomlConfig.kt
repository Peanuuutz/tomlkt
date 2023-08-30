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
import kotlin.jvm.JvmInline

/**
 * Builder provided for `Toml { ... }` factory function.
 */
public class TomlConfigBuilder @PublishedApi internal constructor(from: TomlConfig) {
    /**
     * SerializersModule with contextual serializers to be used in the Toml
     * instance.
     *
     * [EmptySerializersModule] by default.
     */
    public var serializersModule: SerializersModule = from.serializersModule

    // -------- Serialization --------

    /**
     * Specifies how indentation presents in string output.
     *
     * [4 spaces][TomlIndentation.Space4] by default.
     */
    public var indentation: TomlIndentation = from.indentation

    /**
     * Specifies how many items are encoded per line in block array.
     *
     * 1 by default.
     */
    public var itemsPerLineInBlockArray: Int = from.itemsPerLineInBlockArray

    // -------- Deserialization --------

    /**
     * Specifies whether encounters of unknown keys should be ignored instead
     * of throwing [UnknownKeyException].
     *
     * `false` by default.
     */
    public var ignoreUnknownKeys: Boolean = from.ignoreUnknownKeys

    // ======== Internal ========

    @PublishedApi
    internal fun build(): TomlConfig = TomlConfig(
        serializersModule = serializersModule,
        indentation = indentation,
        itemsPerLineInBlockArray = itemsPerLineInBlockArray,
        ignoreUnknownKeys = ignoreUnknownKeys
    )
}

/**
 * Indicates indentation representation in string output.
 *
 * Ideally, users are supposed to use only spaces or tabs as representation.
 *
 * @property representation the string form of this TomlIndentation.
 */
@JvmInline
public value class TomlIndentation(public val representation: String) {
    init {
        require(representation.isBlank()) {
            "Cannot use non-blank characters inside representation, but found $representation"
        }
    }

    override fun toString(): String {
        return representation
    }

    public companion object {
        /**
         * Indentation with 4 spaces.
         */
        public val Space4: TomlIndentation = TomlIndentation("    ")

        /**
         * Indentation with 2 spaces.
         */
        public val Space2: TomlIndentation = TomlIndentation("  ")

        /**
         * Indentation with a tab (`\t`).
         */
        public val Tab: TomlIndentation = TomlIndentation("\t")
    }
}

// ======== Internal ========

internal class TomlConfig(
    val serializersModule: SerializersModule,
    val indentation: TomlIndentation,
    val itemsPerLineInBlockArray: Int,
    val ignoreUnknownKeys: Boolean
) {
    companion object {
        val Default: TomlConfig = TomlConfig(
            serializersModule = EmptySerializersModule(),
            indentation = TomlIndentation.Space4,
            itemsPerLineInBlockArray = 1,
            ignoreUnknownKeys = false
        )
    }
}
