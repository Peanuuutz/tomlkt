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

import net.peanuuutz.tomlkt.NativeLocalDate
import net.peanuuutz.tomlkt.NativeLocalDateTime
import net.peanuuutz.tomlkt.NativeLocalTime
import net.peanuuutz.tomlkt.NativeOffsetDateTime
import net.peanuuutz.tomlkt.TomlArray
import net.peanuuutz.tomlkt.TomlElement
import net.peanuuutz.tomlkt.TomlLiteral
import net.peanuuutz.tomlkt.TomlNull
import net.peanuuutz.tomlkt.TomlReader
import net.peanuuutz.tomlkt.TomlTable
import net.peanuuutz.tomlkt.internal.BareKeyConstraints
import net.peanuuutz.tomlkt.internal.BareKeyRegex
import net.peanuuutz.tomlkt.internal.Comment
import net.peanuuutz.tomlkt.internal.DecimalConstraints
import net.peanuuutz.tomlkt.internal.DecimalOrSignConstraints
import net.peanuuutz.tomlkt.internal.DefiniteDateTimeConstraints
import net.peanuuutz.tomlkt.internal.DefiniteNumberConstraints
import net.peanuuutz.tomlkt.internal.EndArray
import net.peanuuutz.tomlkt.internal.EndTable
import net.peanuuutz.tomlkt.internal.HexadecimalConstraints
import net.peanuuutz.tomlkt.internal.KeyValueDelimiter
import net.peanuuutz.tomlkt.internal.StartArray
import net.peanuuutz.tomlkt.internal.StartTable
import net.peanuuutz.tomlkt.internal.throwIncomplete
import net.peanuuutz.tomlkt.internal.throwUnexpectedToken
import net.peanuuutz.tomlkt.internal.toNumber
import net.peanuuutz.tomlkt.internal.unescape
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal class TomlFileParser(
    private val reader: TomlReader,
    private val buffer: CharArray
) {
    private val bufferSize: Int = buffer.size

    private var currentLineNumber: Int = 1

    private var currentIndex: Int = -1

    private var isEof: Boolean = false

    // region Read

    private fun proceed() {
        if (isEof) {
            return
        }
        val code = reader.read()
        if (code != -1) {
            currentIndex++
            buffer[currentIndex % bufferSize] = code.toChar()
        } else {
            isEof = true
        }
    }

    private fun getChar(): Char {
        return buffer[currentIndex % bufferSize]
    }

    private fun getChar(offset: Int): Char {
        return buffer[(currentIndex + offset) % bufferSize]
    }

    // endregion

    // region Check

    private fun throwIncomplete(): Nothing {
        throwIncomplete(currentLineNumber)
    }

    private inline fun throwIncompleteIf(predicate: () -> Boolean) {
        contract { callsInPlace(predicate, InvocationKind.EXACTLY_ONCE) }

        if (predicate()) {
            throwIncomplete()
        }
    }

    private fun throwUnexpectedToken(token: Char): Nothing {
        throwUnexpectedToken(token, currentLineNumber)
    }

    private inline fun throwUnexpectedTokenIf(token: Char, predicate: (Char) -> Boolean) {
        contract { callsInPlace(predicate, InvocationKind.EXACTLY_ONCE) }

        if (predicate(token)) {
            throwUnexpectedToken(token)
        }
    }

    private fun expectNext(expectedToken: Char) {
        proceed()
        throwIncompleteIf { isEof }
        throwUnexpectedTokenIf(getChar()) { it != expectedToken }
    }

    private fun expectNext(expectedTokens: String) {
        for (expectedToken in expectedTokens) {
            expectNext(expectedToken)
        }
    }

    // endregion

    fun parse(): TomlTable {
        val tree = KeyNode("")
        val arrayOfTableIndices = mutableMapOf<Path, Int>()
        var currentTablePath: Path? = null
        proceed()
        while (!isEof) {
            when (val currentChar = getChar()) {
                ' ', '\t' -> {
                    proceed()
                }
                '\n' -> {
                    currentLineNumber++
                    proceed()
                }
                in BareKeyConstraints, '\"', '\'' -> {
                    val localPath = parsePath()
                    throwUnexpectedTokenIf(getChar()) { it != KeyValueDelimiter }
                    proceed()
                    val key = localPath.last()
                    val value = parseValue(insideStructure = false)
                    val node = ValueNode(key, value)
                    val path = if (currentTablePath != null) currentTablePath + localPath else localPath
                    tree.addByPath(path, node, arrayOfTableIndices)
                }
                Comment -> {
                    parseComment()
                }
                StartArray -> {
                    proceed()
                    throwIncompleteIf { isEof }
                    val isArrayOfTable = getChar() == StartArray
                    if (isArrayOfTable) {
                        proceed()
                    }
                    val path = parseTableHead(isArrayOfTable)
                    if (!isArrayOfTable) {
                        val key = path.last()
                        val node = KeyNode(key)
                        tree.addByPath(path, node, arrayOfTableIndices)
                    } else {
                        val iterator = arrayOfTableIndices.keys.iterator()
                        for (key in iterator) {
                            if (key != path && key.containsAll(path)) {
                                iterator.remove()
                            }
                        }
                        val currentIndex = arrayOfTableIndices[path]
                        if (currentIndex == null) {
                            arrayOfTableIndices[path] = 0
                            val key = path.last()
                            val node = ArrayNode(key)
                            tree.addByPath(path, node, arrayOfTableIndices)
                        } else {
                            arrayOfTableIndices[path] = currentIndex + 1
                        }
                        // A virtual node to act like the root of an array element.
                        val node = KeyNode("")
                        tree.getByPath<ArrayNode>(path, arrayOfTableIndices).add(node)
                    }
                    currentTablePath = path
                }
                else -> {
                    throwUnexpectedToken(currentChar)
                }
            }
        }
        return TomlTable(tree)
    }

    /**
     * Start right on the actual token, end right after the last ']'.
     */
    private fun parseTableHead(isArrayOfTable: Boolean): Path {
        var path: Path? = null
        var justEnded = false
        while (!isEof) {
            when (val currentChar = getChar()) {
                ' ', '\t' -> {
                    proceed()
                }
                '\n' -> {
                    throwIncomplete()
                }
                EndArray -> {
                    if (isArrayOfTable) {
                        expectNext(EndArray)
                    }
                    justEnded = true
                    proceed()
                    break
                }
                else -> {
                    throwUnexpectedTokenIf(currentChar) { path != null }
                    path = parsePath()
                }
            }
        }
        throwIncompleteIf { path == null || !justEnded }
        return path!!
    }

    /**
     * Start right on the actual token, end right on '=' or ']'.
     */
    private fun parsePath(): Path {
        val path = mutableListOf<String>()
        var justEnded = false
        var expectKey = true
        while (!isEof) {
            when (val currentChar = getChar()) {
                ' ', '\t' -> {
                    proceed()
                }
                '\n' -> {
                    throwIncomplete()
                }
                in BareKeyConstraints -> {
                    throwUnexpectedTokenIf(currentChar) { !expectKey }
                    path.add(parseBareKey())
                    expectKey = false
                }
                '\"' -> {
                    throwUnexpectedTokenIf(currentChar) { !expectKey }
                    path.add(parseStringKey())
                    expectKey = false
                }
                '\'' -> {
                    throwUnexpectedTokenIf(currentChar) { !expectKey }
                    path.add(parseLiteralStringKey())
                    expectKey = false
                }
                '.' -> {
                    throwUnexpectedTokenIf(currentChar) { expectKey }
                    expectKey = true
                    proceed()
                }
                KeyValueDelimiter, EndArray -> {
                    throwUnexpectedTokenIf(currentChar) { expectKey }
                    justEnded = true
                    break
                }
                else -> {
                    throwUnexpectedToken(currentChar)
                }
            }
        }
        throwIncompleteIf { !justEnded }
        return path
    }

    /**
     * Start right on the actual token, end right on ' ' or '\t' or '.' or '='
     * or ']'.
     */
    private fun parseBareKey(): String {
        val builder = StringBuilder()
        while (!isEof) {
            when (val currentChar = getChar()) {
                ' ', '\t', '.', KeyValueDelimiter, EndArray -> {
                    break
                }
                '\n' -> {
                    throwIncomplete()
                }
                else -> {
                    builder.append(currentChar)
                    proceed()
                }
            }
        }
        val result = builder.toString()
        if (BareKeyRegex.matches(result).not()) { // Lazy check.
            val unexpectedTokens = result.filterNot(BareKeyConstraints::contains)
            throwUnexpectedToken(unexpectedTokens[0])
        }
        return result
    }

    /**
     * Start right on the first '\"', end right after the last '\"'.
     */
    private fun parseStringKey(): String {
        return parseStringValue().content
    }

    /**
     * Start right on the first '\'', end right after the last '\''.
     */
    private fun parseLiteralStringKey(): String {
        return parseLiteralStringValue().content
    }

    /**
     * Start right on the actual token, end right on '\n' or ',' or ']' or '}'.
     */
    private fun parseValue(insideStructure: Boolean): TomlElement {
        var element: TomlElement? = null
        while (!isEof) {
            when (val currentChar = getChar()) {
                ' ', '\t' -> {
                    proceed()
                }
                '\n' -> {
                    break
                }
                Comment -> {
                    parseComment()
                }
                ',', EndArray, EndTable -> {
                    throwUnexpectedTokenIf(currentChar) { !insideStructure }
                    break
                }
                else -> {
                    throwUnexpectedTokenIf(currentChar) { element != null }
                    element = when (currentChar) {
                        't', 'f' -> {
                            parseBooleanValue()
                        }
                        in DecimalConstraints -> {
                            parseNumberOrDateTimeValue(sign = null)
                        }
                        'i' -> {
                            parseSpecialNumberValue(sign = null)
                        }
                        'n' -> {
                            proceed()
                            throwIncompleteIf { isEof }
                            when (val secondChar = getChar()) {
                                'a' -> {
                                    parseSpecialNumberValue(sign = null)
                                }
                                'u' -> {
                                    parseNullValue()
                                }
                                else -> {
                                    throwUnexpectedToken(secondChar)
                                }
                            }
                        }
                        '+', '-' -> {
                            proceed()
                            throwIncompleteIf { isEof }
                            when (val secondChar = getChar()) {
                                in DecimalConstraints -> {
                                    // Pretend it could be a date time.
                                    parseNumberOrDateTimeValue(currentChar)
                                }
                                'i' -> {
                                    parseSpecialNumberValue(currentChar)
                                }
                                'n' -> {
                                    // parseSpecialNumberValue starts on 'a'.
                                    proceed()
                                    throwIncompleteIf { isEof }
                                    parseSpecialNumberValue(currentChar)
                                }
                                else -> {
                                    throwUnexpectedToken(secondChar)
                                }
                            }
                        }
                        '\"' -> {
                            parseStringValue()
                        }
                        '\'' -> {
                            parseLiteralStringValue()
                        }
                        StartArray -> {
                            parseArrayValue()
                        }
                        StartTable -> {
                            parseInlineTableValue()
                        }
                        else -> {
                            throwUnexpectedToken(currentChar)
                        }
                    }
                }
            }
        }
        throwIncompleteIf { element == null }
        return element!!
    }

    /**
     * Start right on 't' or 'f', ends right after the last token.
     */
    private fun parseBooleanValue(): TomlLiteral {
        return when (val currentChar = getChar()) {
            't' -> {
                expectNext("rue")
                proceed()
                TomlLiteral(true)
            }
            'f' -> {
                expectNext("alse")
                proceed()
                TomlLiteral(false)
            }
            else -> {
                throwUnexpectedToken(currentChar)
            }
        }
    }

    /**
     * Start right on 'i' or 'a', end right after the last token.
     */
    private fun parseSpecialNumberValue(sign: Char?): TomlLiteral {
        return when (val current = getChar()) {
            'i' -> {
                val content = if (sign == null) "inf" else sign.toString() + "inf"
                expectNext("nf")
                proceed()
                TomlLiteral(
                    content = content,
                    isString = false
                )
            }
            'a' -> {
                expectNext('n')
                proceed()
                TomlLiteral(
                    content = "nan",
                    isString = false
                )
            }
            else -> {
                throwUnexpectedToken(current)
            }
        }
    }

    /**
     * Start right on the first number, end right on the second ' ' (if this
     * is a date time), '\t', '\n', ',', '#', ']', '}'.
     */
    private fun parseNumberOrDateTimeValue(sign: Char?): TomlLiteral {
        val builder = StringBuilder()
        if (getChar() == '0') {
            proceed()
            if (isEof) {
                val content = if (sign == null) "0" else sign.toString() + "0"
                return TomlLiteral(
                    content = content,
                    isString = false
                )
            }
            when (getChar()) {
                'x' -> {
                    proceed()
                    return parseNumberValue(builder, radix = 16, sign)
                }
                'b' -> {
                    proceed()
                    return parseNumberValue(builder, radix = 2, sign)
                }
                'o' -> {
                    proceed()
                    return parseNumberValue(builder, radix = 8, sign)
                }
                else -> {
                    builder.append('0')
                }
            }
        }
        var isNumber = true
        while (!isEof) {
            when (val currentChar = getChar()) {
                ' ', '\t', '\n', ',', Comment, EndArray, EndTable -> {
                    break
                }
                in DecimalConstraints -> {
                    builder.append(currentChar)
                    proceed()
                }
                '-', '+' -> {
                    val previousChar = getChar(-1)
                    if (previousChar != 'e' || previousChar != 'E') {
                        throwUnexpectedTokenIf(currentChar) { sign != null }
                        isNumber = false
                    }
                    break
                }
                in DefiniteDateTimeConstraints -> {
                    throwUnexpectedTokenIf(currentChar) { sign != null }
                    isNumber = false
                    break
                }
                in DefiniteNumberConstraints -> {
                    break
                }
                else -> {
                    throwUnexpectedToken(currentChar)
                }
            }
        }
        return if (isNumber) {
            parseNumberValue(builder, radix = 10, sign)
        } else {
            parseDateTimeValue(builder)
        }
    }

    /**
     * Start right on the first token that does not belong to a date time, end
     * right on ' ', '\t', '\n', ',', '#', ']', '}'.
     */
    private fun parseNumberValue(
        builder: StringBuilder,
        radix: Int,
        sign: Char?
    ): TomlLiteral {
        var isDouble = false
        var isExponent = false
        while (!isEof) {
            when (val currentChar = getChar()) {
                ' ', '\t', '\n', ',', Comment, EndArray, EndTable -> {
                    break
                }
                '0', '1' -> {
                    builder.append(currentChar)
                    proceed()
                }
                '2', '3', '4', '5', '6', '7' -> {
                    throwUnexpectedTokenIf(currentChar) { radix == 2 }
                    builder.append(currentChar)
                    proceed()
                }
                '8', '9' -> {
                    throwUnexpectedTokenIf(currentChar) { radix <= 8 }
                    builder.append(currentChar)
                    proceed()
                }
                '.' -> {
                    throwUnexpectedTokenIf(currentChar) {
                        isDouble || isExponent || radix != 10 || getChar(-1) !in DecimalConstraints
                    }
                    proceed()
                    throwIncompleteIf { isEof }
                    // Urge check.
                    val nextChar = getChar()
                    throwUnexpectedTokenIf(nextChar) { it !in DecimalConstraints }
                    builder.append(currentChar).append(nextChar)
                    isDouble = true
                    proceed()
                }
                'e', 'E' -> {
                    when (radix) {
                        10 -> {
                            throwUnexpectedTokenIf(currentChar) {
                                isExponent || getChar(-1) !in DecimalConstraints
                            }
                            proceed()
                            throwIncompleteIf { isEof }
                            // Urge check.
                            val nextChar = getChar()
                            throwUnexpectedTokenIf(nextChar) { it !in DecimalOrSignConstraints }
                            builder.append(currentChar).append(nextChar)
                            isExponent = true
                            if (nextChar == '-') {
                                isDouble = true
                            }
                        }
                        16 -> {
                            builder.append(currentChar)
                        }
                        else -> {
                            throwUnexpectedToken(currentChar)
                        }
                    }
                    proceed()
                }
                'a', 'b', 'c', 'd', 'f', 'A', 'B', 'C', 'D', 'F' -> {
                    throwUnexpectedTokenIf(currentChar) { radix <= 10 }
                    builder.append(currentChar)
                    proceed()
                }
                '_' -> {
                    throwUnexpectedTokenIf(currentChar) { getChar(-1) !in HexadecimalConstraints }
                    proceed()
                    throwIncompleteIf { isEof }
                    // Urge check.
                    val nextChar = getChar()
                    throwUnexpectedTokenIf(nextChar) { it !in HexadecimalConstraints }
                }
                else -> {
                    throwUnexpectedToken(currentChar)
                }
            }
        }
        val result = builder.toString()
        val number = result.toNumber(
            positive = sign != '-',
            radix = radix,
            isDouble = isDouble,
            isExponent = isExponent
        )
        return TomlLiteral(number)
    }

    /**
     * Start right on '-', ':', end right on the second ' ', '\t', '\n', ',',
     * '#', ']', '}'.
     */
    private fun parseDateTimeValue(builder: StringBuilder): TomlLiteral {
        var hasDate = false
        var hasTime = false
        var hasOffset = false
        var hasDateTimeSeparator = false
        while (!isEof) {
            when (val currentChar = getChar()) {
                '\t', '\n', ',', Comment, EndArray, EndTable -> {
                    break
                }
                ' ' -> {
                    if (hasDate && !hasDateTimeSeparator) {
                        hasDateTimeSeparator = true
                        builder.append('T')
                        proceed()
                    } else {
                        break
                    }
                }
                in DecimalConstraints -> {
                    builder.append(currentChar)
                    proceed()
                }
                '-' -> {
                    if (!hasTime) {
                        hasDate = true
                    } else {
                        hasOffset = true
                    }
                    builder.append('-')
                    proceed()
                }
                'T', 't' -> {
                    hasDateTimeSeparator = true
                    builder.append('T')
                    proceed()
                }
                ':' -> {
                    if (!hasOffset) {
                        hasTime = true
                    }
                    builder.append(':')
                    proceed()
                }
                '.' -> {
                    builder.append('.')
                    proceed()
                }
                'Z', 'z' -> {
                    hasOffset = true
                    builder.append('Z')
                    proceed()
                }
                '+' -> {
                    hasOffset = true
                    builder.append('+')
                    proceed()
                }
                else -> {
                    throwUnexpectedToken(currentChar)
                }
            }
        }
        val result = builder.toString()
        when {
            hasDate && hasTime -> {
                if (!hasOffset) {
                    NativeLocalDateTime(result)
                } else {
                    NativeOffsetDateTime(result)
                }
            }
            hasDate -> {
                NativeLocalDate(result)
            }
            hasTime -> {
                NativeLocalTime(result)
            }
            else -> {
                error("Malformed date time: $result")
            }
        }
        // Keeps the original text.
        return TomlLiteral(
            content = result,
            isString = false
        )
    }

    /**
     * Start right on the first '\"', end right after the last '\"'.
     */
    private fun parseStringValue(): TomlLiteral {
        proceed()
        throwIncompleteIf { isEof }
        val initialSecondChar = getChar()
        val multiline: Boolean
        if (initialSecondChar != '\"') {
            multiline = false
        } else {
            proceed()
            if (!isEof && getChar() == '\"') {
                multiline = true
                proceed()
                throwIncompleteIf { isEof }
                if (getChar() == '\n') {
                    // Consumes the initial line feed
                    currentLineNumber++
                    proceed()
                }
            } else {
                return TomlLiteral("")
            }
        }
        val builder = StringBuilder()
        var trim = false
        var justEnded = false
        while (!isEof) {
            when (val currentChar = getChar()) {
                ' ', '\t' -> {
                    if (!trim) {
                        builder.append(currentChar)
                    }
                    proceed()
                }
                '\n' -> {
                    throwIncompleteIf { !multiline }
                    if (!trim) {
                        builder.append(currentChar)
                    }
                    currentLineNumber++
                    proceed()
                }
                '\"' -> {
                    if (!multiline) {
                        justEnded = true
                        proceed()
                        break
                    }
                    proceed()
                    throwIncompleteIf { isEof }
                    val secondChar = getChar()
                    if (secondChar != '\"') {
                        builder.append(currentChar)
                        continue
                    }
                    proceed()
                    throwIncompleteIf { isEof }
                    if (getChar() != '\"') {
                        builder.append(currentChar).append(secondChar)
                        continue
                    }
                    justEnded = true
                    proceed()
                    break
                }
                '\\' -> {
                    proceed()
                    throwIncompleteIf { isEof }
                    when (val nextChar = getChar()) {
                        ' ', '\t', '\n' -> {
                            throwUnexpectedTokenIf(currentChar) { !multiline }
                            trim = true
                        }
                        'n', '\"', '\\', 'u', 'U', 't', 'r', 'b', 'f' -> {
                            builder.append(currentChar).append(nextChar)
                            proceed()
                        }
                        else -> {
                            throwUnexpectedToken(currentChar)
                        }
                    }
                }
                else -> {
                    builder.append(currentChar)
                    trim = false
                    proceed()
                }
            }
        }
        throwIncompleteIf { !justEnded }
        val result = builder.toString()
        val content = result.unescape()
        return TomlLiteral(content)
    }

    /**
     * Start right on the first '\'', end right after the last '\''.
     */
    private fun parseLiteralStringValue(): TomlLiteral {
        proceed()
        throwIncompleteIf { isEof }
        val initialSecondChar = getChar()
        val multiline: Boolean
        if (initialSecondChar != '\'') {
            multiline = false
        } else {
            proceed()
            if (!isEof && getChar() == '\'') {
                multiline = true
                proceed()
                throwIncompleteIf { isEof }
                if (getChar() == '\n') {
                    // Consumes the initial line feed.
                    currentLineNumber++
                    proceed()
                }
            } else {
                return TomlLiteral("")
            }
        }
        val builder = StringBuilder()
        var justEnded = false
        while (!isEof) {
            when (val currentChar = getChar()) {
                ' ', '\t' -> {
                    builder.append(currentChar)
                    proceed()
                }
                '\n' -> {
                    throwIncompleteIf { !multiline }
                    builder.append(currentChar)
                    currentLineNumber++
                    proceed()
                }
                '\'' -> {
                    if (!multiline) {
                        justEnded = true
                        proceed()
                        break
                    }
                    proceed()
                    throwIncompleteIf { isEof }
                    val secondChar = getChar()
                    if (secondChar != '\'') {
                        builder.append(currentChar)
                        continue
                    }
                    proceed()
                    throwIncompleteIf { isEof }
                    if (getChar() != '\'') {
                        builder.append(currentChar).append(secondChar)
                        continue
                    }
                    justEnded = true
                    proceed()
                    break
                }
                else -> {
                    builder.append(currentChar)
                    proceed()
                }
            }
        }
        throwIncompleteIf { !justEnded }
        val result = builder.toString()
        return TomlLiteral(result)
    }

    /**
     * Start right on '[', end right after ']'.
     */
    private fun parseArrayValue(): TomlArray {
        proceed()
        val builder = mutableListOf<TomlElement>()
        var expectValue = true
        var justEnded = false
        while (!isEof) {
            when (val currentChar = getChar()) {
                ' ', '\t' -> {
                    proceed()
                }
                '\n' -> {
                    currentLineNumber++
                    proceed()
                }
                EndArray -> {
                    justEnded = true
                    proceed()
                    break
                }
                ',' -> {
                    throwUnexpectedTokenIf(currentChar) { expectValue }
                    expectValue = true
                    proceed()
                }
                Comment -> {
                    parseComment()
                }
                else -> {
                    val value = parseValue(insideStructure = true)
                    builder.add(value)
                    expectValue = false
                }
            }
        }
        throwIncompleteIf { !justEnded }
        return TomlArray(builder)
    }

    /**
     * Start right on '{', end right after '}'.
     */
    private fun parseInlineTableValue(): TomlTable {
        proceed()
        val builder = KeyNode("")
        var expectEntry = true
        var justStarted = true
        var justEnded = false
        while (!isEof) {
            when (val currentChar = getChar()) {
                ' ', '\t' -> {
                    proceed()
                }
                '\n' -> {
                    throwIncomplete()
                }
                EndTable -> {
                    throwUnexpectedTokenIf(currentChar) { expectEntry && !justStarted }
                    justEnded = true
                    proceed()
                    break
                }
                ',' -> {
                    throwUnexpectedTokenIf(currentChar) { expectEntry }
                    expectEntry = true
                    proceed()
                }
                Comment -> {
                    throwUnexpectedToken(currentChar)
                }
                else -> {
                    val localPath = parsePath()
                    throwUnexpectedTokenIf(getChar()) { it != KeyValueDelimiter }
                    proceed()
                    val key = localPath.last()
                    val value = parseValue(insideStructure = true)
                    val node = ValueNode(key, value)
                    builder.addByPath(localPath, node, null)
                    expectEntry = false
                    justStarted = false
                }
            }
        }
        throwIncompleteIf { !justEnded }
        return TomlTable(builder)
    }

    /**
     * Start right on 'u', end right after the second 'l'.
     */
    private fun parseNullValue(): TomlNull {
        expectNext("ll")
        proceed()
        return TomlNull
    }

    /**
     * Start right on '#', end right on '\n'.
     */
    private fun parseComment() {
        proceed()
        while (!isEof) {
            if (getChar() == '\n') {
                break
            }
            proceed()
        }
    }
}
