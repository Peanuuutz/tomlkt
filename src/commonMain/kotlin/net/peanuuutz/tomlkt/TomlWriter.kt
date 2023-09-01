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
 * A custom writer used when encoding model class or [TomlElement].
 *
 * @see [TomlStringWriter]
 */
public interface TomlWriter {
    public fun writeString(string: String)

    public fun writeBoolean(boolean: Boolean) {
        writeString(boolean.toString())
    }

    public fun writeByte(byte: Byte) {
        writeString(byte.toString())
    }

    public fun writeShort(short: Short) {
        writeString(short.toString())
    }

    public fun writeInt(int: Int) {
        writeString(int.toString())
    }

    public fun writeLong(long: Long) {
        writeString(long.toString())
    }

    public fun writeFloat(float: Float) {
        writeString(float.toString())
    }

    public fun writeDouble(double: Double) {
        writeString(double.toString())
    }

    public fun writeChar(char: Char) {
        writeString(char.toString())
    }

    public fun writeNull() {
        writeString("null")
    }

    public fun writeLineFeed() {
        writeString("\n")
    }
}
