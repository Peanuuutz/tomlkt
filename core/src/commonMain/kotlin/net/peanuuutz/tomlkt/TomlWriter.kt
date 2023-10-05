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

import net.peanuuutz.tomlkt.internal.Comment
import net.peanuuutz.tomlkt.internal.ElementSeparator
import net.peanuuutz.tomlkt.internal.EndArray
import net.peanuuutz.tomlkt.internal.EndInlineTable
import net.peanuuutz.tomlkt.internal.EndTableHead
import net.peanuuutz.tomlkt.internal.KeyValueSeparator
import net.peanuuutz.tomlkt.internal.SegmentSeparator
import net.peanuuutz.tomlkt.internal.StartArray
import net.peanuuutz.tomlkt.internal.StartInlineTable
import net.peanuuutz.tomlkt.internal.StartTableHead
import net.peanuuutz.tomlkt.internal.doubleQuoted
import net.peanuuutz.tomlkt.internal.escape
import net.peanuuutz.tomlkt.internal.singleQuoted
import net.peanuuutz.tomlkt.internal.toStringModified

/**
 * A custom writer used when encoding model class or [TomlElement].
 *
 * @see [TomlStringWriter]
 */
public interface TomlWriter {
    // -------- Core --------

    public fun writeString(string: String)

    public fun writeChar(char: Char) {
        writeString(char.toString())
    }

    // -------- Key --------

    public fun writeSegmentSeparator() {
        writeChar(SegmentSeparator)
    }

    public fun writeBareSegment(segment: String) {
        writeString(segment)
    }

    public fun writeDoubleQuotedSegment(segment: String) {
        writeString(segment.doubleQuoted)
    }

    public fun writeSingleQuotedSegment(segment: String) {
        writeString(segment.singleQuoted)
    }

    // -------- Table Head --------

    public fun startRegularTableHead() {
        writeChar(StartTableHead)
    }

    public fun endRegularTableHead() {
        writeChar(EndTableHead)
    }

    public fun startArrayOfTableHead() {
        writeChar(StartTableHead)
        writeChar(StartTableHead)
    }

    public fun endArrayOfTableHead() {
        writeChar(EndTableHead)
        writeChar(EndTableHead)
    }

    // -------- Value --------

    public fun writeBooleanValue(boolean: Boolean) {
        writeString(boolean.toString())
    }

    public fun writeByteValue(byte: Byte) {
        writeString(byte.toString())
    }

    public fun writeShortValue(short: Short) {
        writeString(short.toString())
    }

    public fun writeIntValue(int: Int) {
        writeString(int.toString())
    }

    public fun writeLongValue(long: Long) {
        writeString(long.toString())
    }

    public fun writeFloatValue(float: Float) {
        writeString(float.toStringModified())
    }

    public fun writeDoubleValue(double: Double) {
        writeString(double.toStringModified())
    }

    public fun writeCharValue(char: Char) {
        writeString(char.escape().doubleQuoted)
    }

    public fun writeStringValue(string: String) {
        writeString(string.escape().doubleQuoted)
    }

    public fun writeNullValue() {
        writeString("null")
    }

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

    public fun startArray() {
        writeChar(StartArray)
    }

    public fun endArray() {
        writeChar(EndArray)
    }

    public fun startInlineTable() {
        writeChar(StartInlineTable)
    }

    public fun endInlineTable() {
        writeChar(EndInlineTable)
    }

    public fun writeKeyValueSeparator() {
        writeChar(KeyValueSeparator)
    }

    public fun writeElementSeparator() {
        writeChar(ElementSeparator)
    }

    // -------- Comment --------

    public fun startComment() {
        writeChar(Comment)
    }

    // -------- Control --------

    public fun writeSpace() {
        writeChar(' ')
    }

    public fun writeIndentation(indentation: TomlIndentation) {
        writeString(indentation.representation)
    }

    public fun writeLineFeed() {
        writeChar('\n')
    }
}
