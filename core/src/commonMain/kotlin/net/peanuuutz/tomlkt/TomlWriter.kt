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

import net.peanuuutz.tomlkt.internal.toStringModified

/**
 * A custom writer used when encoding model class or [TomlElement].
 *
 * @see [TomlStringWriter]
 */
public interface TomlWriter {
    /**
     * Writes [string].
     */
    public fun writeString(string: String)

    /**
     * Writes [boolean].
     */
    public fun writeBoolean(boolean: Boolean) {
        writeString(boolean.toString())
    }

    /**
     * Writes [byte] **as literal**.
     */
    public fun writeByte(byte: Byte) {
        writeString(byte.toString())
    }

    /**
     * Writes [short] **as literal**.
     */
    public fun writeShort(short: Short) {
        writeString(short.toString())
    }

    /**
     * Writes [int] **as literal**.
     */
    public fun writeInt(int: Int) {
        writeString(int.toString())
    }

    /**
     * Writes [long] **as literal**.
     */
    public fun writeLong(long: Long) {
        writeString(long.toString())
    }

    /**
     * Writes [float].
     */
    public fun writeFloat(float: Float) {
        writeString(float.toStringModified())
    }

    /**
     * Writes [double].
     */
    public fun writeDouble(double: Double) {
        writeString(double.toStringModified())
    }

    /**
     * Writes [char].
     */
    public fun writeChar(char: Char) {
        writeString(char.toString())
    }

    /**
     * Writes "null".
     */
    public fun writeNull() {
        writeString("null")
    }

    /**
     * Writes '\n'.
     */
    public fun writeLineFeed() {
        writeString("\n")
    }
}
