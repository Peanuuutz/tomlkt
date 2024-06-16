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

    /**
     * Writes [string] directly.
     */
    public fun writeString(string: String)

    /**
     * Writes [char] directly.
     */
    public fun writeChar(char: Char)

    // -------- Key --------

    /**
     * Writes [key].
     *
     * Implementation should escape and quote `key` if it cannot be bare.
     */
    public fun writeKey(key: String)

    /**
     * Writes a key separator (no whitespace).
     */
    public fun writeKeySeparator()

    // -------- Table Head --------

    /**
     * Starts a regular table head (no whitespace).
     */
    public fun startRegularTableHead()

    /**
     * Ends a regular table head (no whitespace).
     */
    public fun endRegularTableHead()

    /**
     * Starts an array of table head (no whitespace).
     */
    public fun startArrayOfTableHead()

    /**
     * Ends an array of table head (no whitespace).
     */
    public fun endArrayOfTableHead()

    // -------- Value --------

    /**
     * Writes [boolean] as value.
     */
    public fun writeBooleanValue(boolean: Boolean)

    /**
     * Writes [integer] as value.
     *
     * @param group the size of a digit group separated by '_'. If set to 0,
     * the digits will not be grouped.
     */
    public fun writeIntegerValue(
        integer: Long,
        base: Base = Dec,
        group: Int = 0,
        uppercase: Boolean = true
    )

    /**
     * Writes [float] as value.
     *
     * Implementation should handle special values like [NaN][Double.NaN],
     * [INFINITY][Double.POSITIVE_INFINITY] properly.
     */
    public fun writeFloatValue(float: Double)

    /**
     * Writes [string] **as value**.
     *
     * Unlike [writeString], implementation should escape and quote `string`
     * properly depending on [isMultiline] and [isLiteral].
     */
    public fun writeStringValue(
        string: String,
        isMultiline: Boolean = false,
        isLiteral: Boolean = false
    )

    /**
     * Writes `null` as value.
     */
    public fun writeNullValue()

    // -------- Structure --------

    /**
     * Starts a block array or inline array (no whitespace).
     */
    public fun startArray()

    /**
     * Ends a block array or inline array (no whitespace).
     */
    public fun endArray()

    /**
     * Starts an inline table (no whitespace).
     */
    public fun startInlineTable()

    /**
     * Ends an inline table (no whitespace).
     */
    public fun endInlineTable()

    /**
     * Writes a key-value separator (no whitespace).
     */
    public fun writeKeyValueSeparator()

    /**
     * Writes an element separator (no whitespace).
     */
    public fun writeElementSeparator()

    // -------- Comment --------

    /**
     * Starts a comment (no whitespace).
     */
    public fun startComment()

    // -------- Control --------

    /**
     * Writes a whitespace.
     */
    public fun writeSpace()

    /**
     * Writes [indentation].
     */
    public fun writeIndentation(indentation: TomlIndentation)

    /**
     * Writes a line feed.
     */
    public fun writeLineFeed()
}
