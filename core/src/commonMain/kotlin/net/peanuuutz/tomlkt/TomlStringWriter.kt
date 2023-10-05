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
 * A [TomlWriter] that writes TOML as string.
 *
 * This writer is NOT thread-safe, so any concurrent usage should be avoided.
 *
 * Use with [Toml.encodeToWriter] to encode objects. This is useful for
 * concatenating two TOML strings. However, programmer should prefer
 * [Toml.encodeToString] if only one object is being encoded.
 *
 * When finished, simply call [toString] to get the result.
 */
public class TomlStringWriter : TomlWriter {
    private val builder: StringBuilder = StringBuilder()

    override fun writeString(string: String) {
        builder.append(string)
    }

    override fun writeChar(char: Char) {
        builder.append(char)
    }

    override fun toString(): String {
        return builder.toString()
    }
}
