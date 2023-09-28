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

/**
 * A [TomlReader] that reads TOML string.
 *
 * This reader is NOT thread-safe, so any concurrent usage should be avoided.
 *
 * Use with [Toml.decodeFromReader] to decode objects. Note that it is more
 * preferable to use [Toml.decodeFromString] directly as the reader is
 * disposable and should not be used twice.
 */
public class TomlStringReader(
    private val source: String
) : TomlReader {
    private val length: Int = source.length

    private var currentIndex: Int = 0

    override fun read(): Int {
        if (currentIndex >= length) {
            return -1
        }
        return source[currentIndex++].code
    }
}
