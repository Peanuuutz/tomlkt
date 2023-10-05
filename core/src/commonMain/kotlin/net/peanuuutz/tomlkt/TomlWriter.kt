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

import net.peanuuutz.tomlkt.TomlInteger.Base
import net.peanuuutz.tomlkt.TomlInteger.Base.Dec

/**
 * A custom writer used when encoding model class or [TomlElement].
 *
 * @see AbstractTomlWriter
 * @see TomlStringWriter
 */
public interface TomlWriter {
    // -------- Core --------

    public fun writeString(string: String)

    public fun writeChar(char: Char)

    // -------- Key --------

    public fun writeSegmentSeparator()

    public fun writeBareSegment(segment: String)

    public fun writeDoubleQuotedSegment(segment: String)

    public fun writeSingleQuotedSegment(segment: String)

    // -------- Table Head --------

    public fun startRegularTableHead()

    public fun endRegularTableHead()

    public fun startArrayOfTableHead()

    public fun endArrayOfTableHead()

    // -------- Value --------

    public fun writeBooleanValue(boolean: Boolean)

    public fun writeByteValue(
        byte: Byte,
        base: Base = Dec,
        group: Int = 0,
        uppercase: Boolean = true
    )

    public fun writeShortValue(
        short: Short,
        base: Base = Dec,
        group: Int = 0,
        uppercase: Boolean = true
    )

    public fun writeIntValue(
        int: Int,
        base: Base = Dec,
        group: Int = 0,
        uppercase: Boolean = true
    )

    public fun writeLongValue(
        long: Long,
        base: Base = Dec,
        group: Int = 0,
        uppercase: Boolean = true
    )

    public fun writeFloatValue(float: Float)

    public fun writeDoubleValue(double: Double)

    public fun writeCharValue(char: Char)

    public fun writeStringValue(
        string: String,
        isMultiline: Boolean = false,
        isLiteral: Boolean = false
    )

    public fun writeNullValue()

    @Deprecated(
        message = "Use writeBooleanValue instead.",
        replaceWith = ReplaceWith("writeBooleanValue")
    )
    public fun writeBoolean(boolean: Boolean) {
        writeBooleanValue(boolean)
    }

    @Deprecated(
        message = "Use writeByteValue instead.",
        replaceWith = ReplaceWith("writeByteValue")
    )
    public fun writeByte(byte: Byte) {
        writeByteValue(byte)
    }

    @Deprecated(
        message = "Use writeShortValue instead.",
        replaceWith = ReplaceWith("writeShortValue")
    )
    public fun writeShort(short: Short) {
        writeShortValue(short)
    }

    @Deprecated(
        message = "Use writeIntValue instead.",
        replaceWith = ReplaceWith("writeIntValue")
    )
    public fun writeInt(int: Int) {
        writeIntValue(int)
    }

    @Deprecated(
        message = "Use writeLongValue instead.",
        replaceWith = ReplaceWith("writeLongValue")
    )
    public fun writeLong(long: Long) {
        writeLongValue(long)
    }

    @Deprecated(
        message = "Use writeFloatValue instead.",
        replaceWith = ReplaceWith("writeFloatValue")
    )
    public fun writeFloat(float: Float) {
        writeFloatValue(float)
    }

    @Deprecated(
        message = "Use writeDoubleValue instead.",
        replaceWith = ReplaceWith("writeDoubleValue")
    )
    public fun writeDouble(double: Double) {
        writeDoubleValue(double)
    }

    @Deprecated(
        message = "Use writeNullValue instead.",
        replaceWith = ReplaceWith("writeNullValue")
    )
    public fun writeNull() {
        writeNullValue()
    }

    // -------- Structure --------

    public fun startArray()

    public fun endArray()

    public fun startInlineTable()

    public fun endInlineTable()

    public fun writeKeyValueSeparator()

    public fun writeElementSeparator()

    // -------- Comment --------

    public fun startComment()

    // -------- Control --------

    public fun writeSpace()

    public fun writeIndentation(indentation: TomlIndentation)

    public fun writeLineFeed()
}
