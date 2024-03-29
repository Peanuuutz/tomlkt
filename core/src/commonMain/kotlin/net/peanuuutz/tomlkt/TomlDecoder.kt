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

import kotlinx.serialization.encoding.Decoder
import kotlin.contracts.contract

/**
 * A special [Decoder] used internally by the decoding process, providing an
 * extra [decodeTomlElement] to decode [TomlElement] from current position.
 */
@SubclassOptInRequired(TomlSpecific::class)
public interface TomlDecoder : Decoder {
    /**
     * The [Toml] instance used to create this decoder.
     */
    public val toml: Toml

    /**
     * Decodes [TomlElement] from current position.
     */
    public fun decodeTomlElement(): TomlElement
}

/**
 * More convenient means to cast this [Decoder] to [TomlDecoder].
 */
public fun Decoder.asTomlDecoder(): TomlDecoder {
    contract { returns() implies (this@asTomlDecoder is TomlDecoder) }

    return requireNotNull(this as? TomlDecoder) {
        "Expect TomlDecoder, but found ${this::class.simpleName}"
    }
}
