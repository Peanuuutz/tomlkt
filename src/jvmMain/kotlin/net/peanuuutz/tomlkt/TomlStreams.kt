package net.peanuuutz.tomlkt

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.serializer
import net.peanuuutz.tomlkt.internal.TomlEncodingException
import java.io.OutputStream

/**
 * Serializes [value] into [outputStream] using [serializer].
 *
 * @throws TomlEncodingException when [value] cannot be serialized.
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
 * @throws TomlEncodingException when [value] cannot be serialized.
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
