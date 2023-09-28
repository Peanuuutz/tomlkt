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

package net.peanuuutz.tomlkt.internal.parser

import net.peanuuutz.tomlkt.TomlReader
import net.peanuuutz.tomlkt.TomlTable
import net.peanuuutz.tomlkt.internal.BufferPool

internal class TomlFileParser(private val reader: TomlReader) {
    private val buffer: CharArray = BufferPool.take()

    private var currentLineNumber: Int = 1

    fun parse(): TomlTable {
        val string = buildString {
            var code = reader.read()
            while (code != -1) {
                append(code.toChar())
                code = reader.read()
            }
        }
        return OldTomlFileParser(string).parse()
    }
}
