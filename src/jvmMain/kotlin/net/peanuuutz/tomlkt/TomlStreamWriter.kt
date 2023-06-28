package net.peanuuutz.tomlkt

import net.peanuuutz.tomlkt.internal.LINE_FEED_CODE
import java.io.OutputStream

/**
 * A [TomlWriter] that writes TOML to [outputStream].
 *
 * Note that this writer only wraps the stream, so the following requirements
 * should be met:
 * * Pass in a [buffered][OutputStream.buffered] stream. The writer itself won't
 * buffer in any way.
 * * Call [close][OutputStream.close] when finished, or use [AutoCloseable.use]
 * for short.
 *
 * Use with [Toml.encodeToWriter] to encode objects. This is useful for
 * concatenating two TOML streams. However, user should prefer
 * [Toml.encodeToStream] if only one object is being encoded.
 */
public class TomlStreamWriter(
    private val outputStream: OutputStream
) : TomlWriter {
    override fun writeString(string: String) {
        outputStream.write(string.toByteArray())
    }

    override fun writeChar(char: Char) {
        outputStream.write(char.code)
    }

    override fun writeLineFeed() {
        outputStream.write(LINE_FEED_CODE)
    }
}
