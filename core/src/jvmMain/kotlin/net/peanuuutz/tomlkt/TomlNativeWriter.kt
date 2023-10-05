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

import java.io.Writer

/**
 * A [TomlWriter] that writes TOML string to [nativeWriter].
 *
 * Note that this writer only wraps the native writer, so the following
 * requirements should be met:
 * * Pass in a [buffered][Writer.buffered] native writer. The writer itself
 * won't buffer in any way.
 * * Call [close][Writer.close] when finished, or use [AutoCloseable.use]
 * for short.
 *
 * Use with [Toml.encodeToWriter] to encode objects. This is useful for
 * concatenating two TOML streams. However, programmer should prefer
 * [Toml.encodeToNativeWriter] if only one object is being encoded.
 */
public class TomlNativeWriter(
    private val nativeWriter: Writer
) : AbstractTomlWriter() {
    override fun writeString(string: String) {
        nativeWriter.write(string)
    }

    override fun writeChar(char: Char) {
        nativeWriter.write(char.code)
    }
}
