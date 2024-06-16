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
import kotlinx.serialization.serializer
import net.peanuuutz.tomlkt.internal.NonPrimitiveKeyException
import net.peanuuutz.tomlkt.internal.TomlDecodingException
import net.peanuuutz.tomlkt.internal.TomlEncodingException
import java.io.Reader
import java.io.Writer

/**
 * Serializes [value] into [nativeWriter] using [serializer].
 *
 * Note that when finished, `nativeWriter` is automatically
 * [closed][Writer.close].
 *
 * @throws TomlEncodingException if `value` cannot be serialized.
 *
 * @see TomlNativeWriter
 */
public fun <T> Toml.encodeToNativeWriter(
    serializer: SerializationStrategy<T>,
    value: T,
    nativeWriter: Writer
) {
    nativeWriter.buffered().use { buffered ->
        val writer = TomlNativeWriter(buffered)
        encodeToWriter(serializer, value, writer)
    }
}

/**
 * Serializes [value] into [nativeWriter] using the serializer retrieved from
 * reified type parameter.
 *
 * Note that when finished, `nativeWriter` is automatically
 * [closed][Writer.close].
 *
 * @throws TomlEncodingException if `value` cannot be serialized.
 *
 * @see TomlNativeWriter
 */
public inline fun <reified T> Toml.encodeToNativeWriter(
    value: T,
    nativeWriter: Writer
) {
    encodeToNativeWriter(serializersModule.serializer(), value, nativeWriter)
}

/**
 * Deserializes the content of [nativeReader] into a value of type [T] using
 * [deserializer].
 *
 * Note that when finished, `nativeReader` is automatically
 * [closed][Reader.close].
 *
 * @param nativeReader **MUST** contain a TOML file, as this method delegates
 * parsing to [Toml.parseToTomlTable].
 *
 * @throws TomlDecodingException if the content cannot be parsed into
 * [TomlTable] or cannot be deserialized.
 *
 * @see TomlNativeReader
 */
public fun <T> Toml.decodeFromNativeReader(
    deserializer: DeserializationStrategy<T>,
    nativeReader: Reader
): T {
    return nativeReader.buffered().use { buffered ->
        val reader = TomlNativeReader(buffered)
        decodeFromReader(deserializer, reader)
    }
}

/**
 * Deserializes the content of [nativeReader] into a value of type [T] using
 * the serializer retrieved from reified type parameter.
 *
 * Note that when finished, `nativeReader` is automatically
 * [closed][Reader.close].
 *
 * @param nativeReader **MUST** contain a TOML file, as this method delegates
 * parsing to [Toml.parseToTomlTable].
 *
 * @throws TomlDecodingException if the content cannot be parsed into
 * [TomlTable] or cannot be deserialized.
 *
 * @see TomlNativeReader
 */
public inline fun <reified T> Toml.decodeFromNativeReader(nativeReader: Reader): T {
    return decodeFromNativeReader(serializersModule.serializer(), nativeReader)
}

/**
 * Parses the content of [nativeReader] into a [TomlTable] and deserializes the
 * corresponding element fetched with [keys] into a value of type [T] using
 * [deserializer].
 *
 * Note that when finished, `nativeReader` is automatically
 * [closed][Reader.close].
 *
 * @param nativeReader **MUST** contain a TOML file, as this method delegates
 * parsing to [Toml.parseToTomlTable].
 * @param keys the path which leads to the value. Each one item is a single
 * segment. If a [TomlArray] is met, any direct child segment must be [Int]
 * or [String] (will be parsed into integer).
 *
 * @throws TomlDecodingException if the content cannot be parsed into
 * [TomlTable] or the element cannot be deserialized.
 * @throws NonPrimitiveKeyException if provided non-primitive keys.
 *
 * @see get
 */
public fun <T> Toml.decodeFromNativeReader(
    deserializer: DeserializationStrategy<T>,
    nativeReader: Reader,
    vararg keys: Any?
): T {
    return nativeReader.buffered().use { buffered ->
        val reader = TomlNativeReader(buffered)
        decodeFromReader(deserializer, reader, keys = keys)
    }
}

/**
 * Parses the content of [nativeReader] into a [TomlTable] and deserializes the
 * corresponding element fetched with [keys] into a value of type [T] using the
 * serializer retrieved from reified type parameter.
 *
 * Note that when finished, `nativeReader` is automatically
 * [closed][Reader.close].
 *
 * @param nativeReader **MUST** contain a TOML file, as this method delegates
 * parsing to [Toml.parseToTomlTable].
 * @param keys the path which leads to the value. Each one item is a single
 * segment. If a [TomlArray] is met, any direct child segment must be [Int]
 * or [String] (will be parsed into integer).
 *
 * @throws TomlDecodingException if the content cannot be parsed into
 * [TomlTable] or the element cannot be deserialized.
 * @throws NonPrimitiveKeyException if provided non-primitive keys.
 *
 * @see get
 */
public inline fun <reified T> Toml.decodeFromNativeReader(
    nativeReader: Reader,
    vararg keys: Any?
): T {
    return decodeFromNativeReader(serializersModule.serializer(), nativeReader, keys = keys)
}

/**
 * Parses the content of [nativeReader] into equivalent representation of
 * [TomlTable].
 *
 * @throws TomlDecodingException if the content cannot be parsed into
 * `TomlTable`.
 */
public fun Toml.parseToTomlTable(nativeReader: Reader): TomlTable {
    return nativeReader.buffered().use { buffered ->
        val reader = TomlNativeReader(buffered)
        parseToTomlTable(reader)
    }
}
