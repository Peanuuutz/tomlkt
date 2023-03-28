/*
    Copyright 2022 Peanuuutz

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
 */
public class TomlStringWriter : TomlWriter {
    private val delegate: StringBuilder = StringBuilder()

    override fun writeString(string: String) {
        delegate.append(string)
    }

    override fun toString(): String {
        return delegate.toString()
    }
}