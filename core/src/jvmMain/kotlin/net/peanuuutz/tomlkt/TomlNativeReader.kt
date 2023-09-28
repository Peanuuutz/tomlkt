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

import java.io.Reader

/**
 * A [TomlReader] that reads TOML string from [nativeReader].
 *
 * Note that this reader only wraps the native reader, so the following
 * requirements should be met:
 * * Pass in a [buffered][Reader.buffered] native reader. The reader itself
 * won't buffer in any way.
 * * Call [close][Reader.close] when finished, or use [AutoCloseable.use]
 * for short.
 *
 * Use with [Toml.decodeFromReader] to decode objects. Note that it is more
 * preferable to use [Toml.decodeFromNativeReader] directly as the reader is
 * disposable and should not be used twice.
 */
public class TomlNativeReader(
    private val nativeReader: Reader
) : TomlReader {
    override fun read(): Int {
        return nativeReader.read()
    }
}
