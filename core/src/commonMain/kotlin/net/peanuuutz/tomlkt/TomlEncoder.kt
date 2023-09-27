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

import kotlinx.serialization.encoding.Encoder
import kotlin.contracts.contract

/**
 * A special [Encoder] which is used internally by encoding process of [Toml],
 * providing an extra [encodeTomlElement] to encode [TomlElement] to the current
 * position.
 */
@SubclassOptInRequired(TomlSpecific::class)
public interface TomlEncoder : Encoder {
    public val toml: Toml

    public fun encodeTomlElement(value: TomlElement)
}

/**
 * More convenient approach to cast [this] Encoder to [TomlEncoder].
 */
public fun Encoder.asTomlEncoder(): TomlEncoder {
    contract { returns() implies (this@asTomlEncoder is TomlEncoder) }

    return this as? TomlEncoder ?: error("Expect TomlEncoder, but found ${this::class.simpleName}")
}
