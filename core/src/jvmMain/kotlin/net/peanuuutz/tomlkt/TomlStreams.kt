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

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.serializer
import net.peanuuutz.tomlkt.internal.TomlEncodingException
import java.io.OutputStream

/**
 * Serializes [value] into [outputStream] using [serializer].
 *
 * @throws TomlEncodingException if `value` cannot be serialized.
 *
 * @see TomlStreamWriter
 */
public fun <T> Toml.encodeToStream(
    serializer: SerializationStrategy<T>,
    value: T,
    outputStream: OutputStream
) {
    val writer = TomlStreamWriter(outputStream)
    encodeToWriter(
        serializer = serializer,
        value = value,
        writer = writer
    )
}

/**
 * Serializes [value] into [outputStream] using serializer retrieved from
 * reified type parameter.
 *
 * @throws TomlEncodingException if `value` cannot be serialized.
 *
 * @see TomlStreamWriter
 */
public inline fun <reified T> Toml.encodeToStream(
    value: T,
    outputStream: OutputStream
) {
    encodeToStream(
        serializer = serializersModule.serializer(),
        value = value,
        outputStream = outputStream
    )
}
