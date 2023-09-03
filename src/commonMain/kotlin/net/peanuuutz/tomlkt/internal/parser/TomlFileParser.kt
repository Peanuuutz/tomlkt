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

package net.peanuuutz.tomlkt.internal.parser

import net.peanuuutz.tomlkt.NativeLocalDate
import net.peanuuutz.tomlkt.NativeLocalDateTime
import net.peanuuutz.tomlkt.NativeLocalTime
import net.peanuuutz.tomlkt.NativeOffsetDateTime
import net.peanuuutz.tomlkt.TomlArray
import net.peanuuutz.tomlkt.TomlElement
import net.peanuuutz.tomlkt.TomlLiteral
import net.peanuuutz.tomlkt.TomlNull
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
import net.peanuuutz.tomlkt.internal.IncompleteException
import net.peanuuutz.tomlkt.internal.KeyValueDelimiter
import net.peanuuutz.tomlkt.internal.StartArray
import net.peanuuutz.tomlkt.internal.StartTable
import net.peanuuutz.tomlkt.internal.UnexpectedTokenException
import net.peanuuutz.tomlkt.internal.toNumber
import net.peanuuutz.tomlkt.internal.unescape
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal class TomlFileParser(private val source: String) {
    // Cache this number.
    private val lastIndex: Int = source.lastIndex

    private var currentLineNumber: Int = 1

    private var currentIndex: Int = -1

    // region Utils

    private fun getChar(): Char {
        return source[currentIndex]
    }

    private fun getChar(offset: Int): Char {
        return source[currentIndex + offset]
    }

    private fun isEof(): Boolean {
        return currentIndex >= lastIndex
    }

    private fun isEofAfter(offset: Int): Boolean {
        return currentIndex >= lastIndex - offset
    }

    private fun isNotEof(): Boolean {
        return currentIndex < lastIndex
    }

    private fun isNotEofAfter(offset: Int): Boolean {
        return currentIndex < lastIndex - offset
    }

    private fun throwIncomplete(): Nothing {
        throw IncompleteException(currentLineNumber)
    }

    private inline fun throwIncompleteIf(predicate: () -> Boolean) {
        contract { callsInPlace(predicate, InvocationKind.EXACTLY_ONCE) }

        if (predicate()) {
            throwIncomplete()
        }
    }

    private fun throwUnexpectedToken(token: Char): Nothing {
        throw UnexpectedTokenException(token, currentLineNumber)
    }

    private inline fun throwUnexpectedTokenIf(token: Char, predicate: (Char) -> Boolean) {
        contract { callsInPlace(predicate, InvocationKind.EXACTLY_ONCE) }

        if (predicate(token)) {
            throwUnexpectedToken(token)
        }
    }

    private fun expectNext(testChar: Char) {
        throwIncompleteIf { isEof() }
        currentIndex++
        throwUnexpectedTokenIf(getChar()) { it != testChar }
    }

    // Start right before [testString], end on the last token.
    private fun expectNext(testString: String) {
        val requiredOffset = testString.length - 1
        throwIncompleteIf { isEofAfter(requiredOffset) }
        for (i in testString.indices) {
            currentIndex++
            throwUnexpectedTokenIf(getChar()) { it != testString[i] }
        }
    }

    // endregion

    fun parse(): TomlTable {
        val tree = KeyNode("")
        val arrayOfTableIndices = mutableMapOf<Path, Int>()
        var tablePath: Path? = null
        while (++currentIndex <= lastIndex) {
            when (val current = getChar()) {
                ' ', '\t' -> {
                    continue
                }
                '\n' -> {
                    currentLineNumber++
                }
                in BareKeyConstraints, '"', '\'' -> {
                    currentIndex--
                    val localPath = parsePath()
                    expectNext(KeyValueDelimiter)
                    val path = if (tablePath != null) tablePath + localPath else localPath
                    val node = ValueNode(path.last(), parseValue(inStructure = false))
                    tree.addByPath(path, node, arrayOfTableIndices)
                }
                Comment -> {
                    parseComment()
                }
                StartArray -> {
                    throwIncompleteIf { isEof() }
                    val isArrayOfTable = getChar(1) == StartArray
                    if (isArrayOfTable) {
                        currentIndex++
                    }
                    val path = parseTableHead(isArrayOfTable)
                    if (isArrayOfTable) {
                        // Copied from official codebase because theirs is not inline.
                        val iterator = arrayOfTableIndices.keys.iterator()
                        for (item in iterator) {
                            if (item != path && item.containsAll(path)) {
                                iterator.remove()
                            }
                        }
                        val index = arrayOfTableIndices[path]
                        if (index == null) {
                            arrayOfTableIndices[path] = 0
                            val node = ArrayNode(path.last())
                            tree.addByPath(path, node, arrayOfTableIndices)
                        } else {
                            arrayOfTableIndices[path] = index + 1
                        }
                        val virtual = KeyNode("")
                        tree.getByPath<ArrayNode>(path, arrayOfTableIndices).add(virtual)
                    } else {
                        val node = KeyNode(path.last())
                        tree.addByPath(path, node, arrayOfTableIndices)
                    }
                    tablePath = path
                }
                else -> {
                    throwUnexpectedToken(current)
                }
            }
        }
        return TomlTable(tree)
    }

    // Start right on the last '[', end on the last ']'.
    private fun parseTableHead(isArrayOfTable: Boolean): Path {
        var path: Path? = null
        var justEnded = false
        while (++currentIndex <= lastIndex) {
            when (val current = getChar()) {
                ' ', '\t' -> {
                    continue
                }
                '\n' -> {
                    throwIncomplete()
                }
                EndArray -> {
                    if (isArrayOfTable) {
                        throwIncompleteIf { isEof() || getChar(1) != EndArray }
                        currentIndex++
                    }
                    justEnded = true
                    break
                }
                else -> {
                    throwUnexpectedTokenIf(current) { path != null }
                    currentIndex--
                    path = parsePath()
                }
            }
        }
        throwIncompleteIf { path == null || !justEnded }
        return path!!
    }

    // Start right before the actual token, end right before '=' or ']'.
    private fun parsePath(): Path {
        val path = mutableListOf<String>()
        var justEnded = false
        var expectKey = true
        while (++currentIndex <= lastIndex) {
            when (val current = getChar()) {
                ' ', '\t' -> {
                    continue
                }
                '\n' -> {
                    throwIncomplete()
                }
                in BareKeyConstraints -> {
                    throwUnexpectedTokenIf(current) { !expectKey }
                    path.add(parseBareKey())
                    expectKey = false
                }
                '"' -> {
                    throwUnexpectedTokenIf(current) { !expectKey }
                    path.add(parseStringKey())
                    expectKey = false
                }
                '\'' -> {
                    throwUnexpectedTokenIf(current) { !expectKey }
                    path.add(parseLiteralStringKey())
                    expectKey = false
                }
                '.' -> {
                    throwUnexpectedTokenIf(current) { expectKey }
                    expectKey = true
                }
                KeyValueDelimiter, EndArray -> {
                    throwUnexpectedTokenIf(current) { expectKey }
                    justEnded = true
                    currentIndex--
                    break
                }
                else -> {
                    throwUnexpectedToken(current)
                }
            }
        }
        throwIncompleteIf { !justEnded }
        return path
    }

    // Start right on the actual token, end right before ' ' or '\t' or '\n' or '=' or '.' or ']'.
    private fun parseBareKey(): String {
        val builder = StringBuilder().append(getChar())
        while (++currentIndex <= lastIndex) {
            when (val current = getChar()) {
                ' ', '\t', '.', KeyValueDelimiter, EndArray -> {
                    break
                }
                '\n' -> {
                    throwIncomplete()
                }
                else -> {
                    builder.append(current)
                }
            }
        }
        val result = builder.toString()
        if (BareKeyRegex.matches(result).not()) { // Lazy check.
            val unexpectedTokens = result.filterNot(BareKeyConstraints::contains)
            throwUnexpectedToken(unexpectedTokens[0])
        }
        currentIndex--
        return result
    }

    // Start right on the first '"', end on the last '"'.
    private fun parseStringKey(): String {
        return parseStringValue().content
    }

    // Start right on the first '\'', end on the last '\''.
    private fun parseLiteralStringKey(): String {
        return parseLiteralStringValue().content
    }

    // Start right before the actual token, end right before '\n' or ',' or ']' or '}'.
    private fun parseValue(inStructure: Boolean): TomlElement {
        var element: TomlElement? = null
        while (++currentIndex <= lastIndex) {
            when (val current = getChar()) {
                ' ', '\t' -> {
                    continue
                }
                '\n' -> {
                    throwIncompleteIf { element == null }
                    break
                }
                Comment -> {
                    parseComment()
                }
                ',', EndArray, EndTable -> {
                    throwUnexpectedTokenIf(current) { !inStructure }
                    break
                }
                else -> {
                    throwUnexpectedTokenIf(current) { element != null }
                    element = when (current) {
                        't', 'f' -> {
                            parseBooleanValue()
                        }
                        in DecimalConstraints -> {
                            parseNumberOrDateTimeValue()
                        }
                        'i' -> {
                            parseSpecialNumberValue(sign = null)
                        }
                        'n' -> {
                            throwIncompleteIf { isEof() }
                            when (val next = getChar(1)) {
                                'a' -> {
                                    parseSpecialNumberValue(sign = null)
                                }
                                'u' -> {
                                    parseNullValue()
                                }
                                else -> {
                                    throwUnexpectedToken(next)
                                }
                            }
                        }
                        '+', '-' -> {
                            throwIncompleteIf { isEof() }
                            currentIndex++
                            when (val next = getChar()) {
                                in DecimalConstraints -> {
                                    parseNumberValue(current)
                                }
                                'i', 'n' -> {
                                    parseSpecialNumberValue(current)
                                }
                                else -> {
                                    throwUnexpectedToken(next)
                                }
                            }
                        }
                        '"' -> {
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
                            throwUnexpectedToken(current)
                        }
                    }
                }
            }
        }
        throwIncompleteIf { element == null }
        currentIndex--
        return element!!
    }

    // Start right on 't' or 'f', ends on the last token.
    private fun parseBooleanValue(): TomlLiteral {
        return when (val current = getChar()) {
            't' -> {
                expectNext("rue")
                TomlLiteral(true)
            }
            'f' -> {
                expectNext("alse")
                TomlLiteral(false)
            }
            else -> {
                throwUnexpectedToken(current)
            }
        }
    }

    // Start right on 'i' or 'n', end on the last token.
    private fun parseSpecialNumberValue(sign: Char?): TomlLiteral {
        return when (val current = getChar()) {
            'i' -> {
                expectNext("nf")
                val content = if (sign == null) "inf" else sign.toString() + "inf"
                TomlLiteral(
                    content = content,
                    isString = false
                )
            }
            'n' -> {
                expectNext("an")
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

    // Start right on the actual token, end right before ' ' or '\t' or '\n' or '#' or ',' or ']' or
    // '}'.
    private fun parseNumberOrDateTimeValue(): TomlLiteral {
        val maxOffset = lastIndex - currentIndex
        var isNumber = true
        var testOffset = -1
        while (++testOffset <= maxOffset) {
            when (val current = getChar(testOffset)) {
                ' ' -> {
                    if (isNotEofAfter(testOffset) && getChar(testOffset + 1) in DecimalConstraints) {
                        isNumber = false
                    }
                    break
                }
                '\t', '\n', ',', Comment, EndArray, EndTable -> {
                    break
                }
                in DecimalConstraints -> {
                    continue
                }
                '-', '+' -> {
                    val previous = getChar(testOffset - 1)
                    if (previous != 'e' && previous != 'E') {
                        isNumber = false
                    }
                    break
                }
                in DefiniteDateTimeConstraints -> {
                    isNumber = false
                    break
                }
                in DefiniteNumberConstraints -> {
                    break
                }
                else -> {
                    throwUnexpectedToken(current)
                }
            }
        }
        return if (isNumber) {
            parseNumberValue(sign = null)
        } else {
            parseDateTimeValue()
        }
    }

    // Start right on the actual token, end right before ' ' or '\t' or '\n' or '#' or ',' or ']' or
    // '}'.
    private fun parseNumberValue(sign: Char?): TomlLiteral {
        val first = getChar()
        if (isEof()) {
            val content = if (sign == null) {
                first.toString()
            } else {
                sign.toString() + first.toString()
            }
            return TomlLiteral(
                content = content,
                isString = false
            )
        }
        val builder = StringBuilder()
        var radix = 10
        if (first != '0') {
            builder.append(first)
        } else {
            when (getChar(1)) {
                'x' -> {
                    radix = 16
                    currentIndex++
                }
                'o' -> {
                    radix = 8
                    currentIndex++
                }
                'b' -> {
                    radix = 2
                    currentIndex++
                }
                else -> { // Others are checked in the following loop.
                    builder.append('0') // Pretend there's no error.
                }
            }
        }
        var isDouble = false
        var isExponent = false
        while (++currentIndex <= lastIndex) {
            when (val current = getChar()) {
                ' ', '\t', '\n', ',', Comment, EndArray, EndTable -> {
                    break
                }
                '0', '1' -> {
                    builder.append(current)
                }
                '2', '3', '4', '5', '6', '7' -> {
                    throwUnexpectedTokenIf(current) { radix == 2 }
                    builder.append(current)
                }
                '8', '9' -> {
                    throwUnexpectedTokenIf(current) { radix <= 8 }
                    builder.append(current)
                }
                '.' -> {
                    throwUnexpectedTokenIf(current) {
                        val surroundedByNumber = getChar(-1) in DecimalConstraints &&
                                isNotEof() &&
                                getChar(1) in DecimalConstraints
                        val condition = isExponent ||
                                isDouble ||
                                radix != 10 ||
                                !surroundedByNumber
                        condition
                    }
                    builder.append(current)
                    isDouble = true
                }
                'e', 'E' -> {
                    throwUnexpectedTokenIf(current) { radix <= 8 }
                    if (radix == 10) {
                        throwUnexpectedTokenIf(current) {
                            val surroundedByNumber = getChar(-1) in DecimalConstraints &&
                                    isNotEof() &&
                                    getChar(1) in DecimalOrSignConstraints
                            val condition = isExponent ||
                                    !surroundedByNumber
                            condition
                        }
                        isExponent = true
                    }
                    builder.append(current)
                }
                'a', 'b', 'c', 'd', 'f', 'A', 'B', 'C', 'D', 'F' -> {
                    throwUnexpectedTokenIf(current) { radix <= 10 }
                    builder.append(current)
                }
                '_' -> {
                    throwUnexpectedTokenIf(current) {
                        val surroundedByNumber = getChar(-1) in HexadecimalConstraints &&
                                isNotEof() &&
                                getChar(1) in HexadecimalConstraints
                        !surroundedByNumber
                    }
                }
                '+' -> {
                    val previous = getChar(-1)
                    throwUnexpectedTokenIf(current) { previous != 'e' && previous != 'E' }
                }
                '-' -> {
                    val previous = getChar(-1)
                    throwUnexpectedTokenIf(current) { previous != 'e' && previous != 'E' }
                    isDouble = true
                    builder.append(current)
                }
                else -> {
                    throwUnexpectedToken(current)
                }
            }
        }
        currentIndex--
        val number = builder.toString().toNumber(
            positive = sign != '-',
            radix = radix,
            isDouble = isDouble,
            isExponent = isExponent
        )
        return TomlLiteral(number)
    }

    // Start right on the actual token, end right before '\t' or '\n' or '#' or ',' or ']' or '}'.
    private fun parseDateTimeValue(): TomlLiteral {
        val builder = StringBuilder().append(getChar())
        var hasDate = false
        var hasTime = false
        var hasOffset = false
        var hasWhitespace = false
        while (++currentIndex <= lastIndex) {
            when (val current = getChar()) {
                '\t', '\n', ',', Comment, EndArray, EndTable -> {
                    break
                }
                ' ' -> {
                    if (!hasWhitespace) {
                        hasWhitespace = true
                        builder.append('T')
                    } else {
                        break
                    }
                }
                in DecimalConstraints -> {
                    builder.append(current)
                }
                '-' -> {
                    if (hasTime) {
                        hasOffset = true
                    } else {
                        hasDate = true
                    }
                    builder.append('-')
                }
                'T', 't' -> {
                    builder.append('T')
                }
                ':' -> {
                    if (!hasOffset) {
                        hasTime = true
                    }
                    builder.append(':')
                }
                '.' -> {
                    builder.append('.')
                }
                'Z', 'z' -> {
                    hasOffset = true
                    builder.append('Z')
                }
                '+' -> {
                    hasOffset = true
                    builder.append('+')
                }
                else -> {
                    throwUnexpectedToken(current)
                }
            }
        }
        currentIndex--
        val result = builder.toString()
        return when {
            hasDate && hasTime -> {
                if (!hasOffset) {
                    val localDateTime = NativeLocalDateTime(result)
                    TomlLiteral(localDateTime)
                } else {
                    val offsetDateTime = NativeOffsetDateTime(result)
                    TomlLiteral(offsetDateTime)
                }
            }
            hasDate -> {
                val localDate = NativeLocalDate(result)
                TomlLiteral(localDate)
            }
            hasTime -> {
                val localTime = NativeLocalTime(result)
                TomlLiteral(localTime)
            }
            else -> {
                error("Unreachable code")
            }
        }
    }

    // Start right on the first '"', end on the last '"'.
    private fun parseStringValue(): TomlLiteral {
        throwIncompleteIf { isEof() }
        var next = getChar(1)
        val multiline: Boolean
        when {
            next != '"' -> {
                multiline = false
            }
            isNotEofAfter(1) && getChar(2) == '"' -> {
                multiline = true
                currentIndex += 2
                throwIncompleteIf { isEof() }
                if (next == '\n') {
                    // Consume the initial line feed.
                    currentIndex++
                }
            }
            else -> {
                currentIndex++
                return TomlLiteral("")
            }
        }
        val builder = StringBuilder()
        var trim = false
        var justStarted = false
        var justEnded = false
        while (++currentIndex <= lastIndex) {
            when (val current = getChar()) {
                ' ', '\t' -> {
                    if (!trim) {
                        builder.append(current)
                    }
                }
                '\n' -> {
                    throwIncompleteIf { !multiline }
                    if (!trim && justStarted) {
                        builder.append(current)
                    }
                    currentLineNumber++
                }
                '"' -> {
                    if (!multiline) {
                        if (getChar(-1) != '\\') {
                            justEnded = true
                            break
                        }
                    } else {
                        throwIncompleteIf { isEof() }
                        if (getChar(1) == '"' && getChar(2) == '"') {
                            currentIndex += 2
                            justEnded = true
                            break
                        }
                    }
                    builder.append(current)
                }
                '\\' -> {
                    throwIncompleteIf { isEof() }
                    next = getChar(1)
                    when (next) {
                        ' ', '\t', '\n' -> {
                            throwUnexpectedTokenIf(current) { !multiline }
                            trim = true
                        }
                        'u', 'b', 't', 'n', 'f', 'r', '"', '\\' -> {
                            builder.append(current).append(next)
                            currentIndex++
                        }
                        else -> {
                            throwUnexpectedToken(current)
                        }
                    }
                }
                else -> {
                    builder.append(current)
                    trim = false
                }
            }
            justStarted = true
        }
        throwIncompleteIf { !justEnded }
        val content = builder.toString().unescape()
        return TomlLiteral(content)
    }

    // Start right on '\'', end on the last '\''.
    private fun parseLiteralStringValue(): TomlLiteral {
        throwIncompleteIf { isEof() }
        val next = getChar(1)
        val multiline: Boolean
        when {
            next != '\'' -> {
                multiline = false
            }
            isNotEofAfter(1) && getChar(2) == '\'' -> {
                multiline = true
                currentIndex += 2
                throwIncompleteIf { isEof() }
                if (next == '\n') {
                    // Consume the initial line feed.
                    currentIndex++
                }
            }
            else -> {
                currentIndex++
                return TomlLiteral("")
            }
        }
        val builder = StringBuilder()
        var justStarted = false
        var justEnded = false
        while (++currentIndex <= lastIndex) {
            when (val current = getChar()) {
                ' ', '\t' -> {
                    builder.append(current)
                }
                '\n' -> {
                    throwIncompleteIf { !multiline }
                    if (justStarted) {
                        builder.append(current)
                    }
                    currentLineNumber++
                }
                '\'' -> {
                    if (!multiline) {
                        justEnded = true
                        break
                    }
                    throwIncompleteIf { isEof() }
                    if (getChar(1) == '\'' && getChar(2) == '\'') {
                        currentIndex += 2
                        justEnded = true
                        break
                    }
                    builder.append(current)
                }
                else -> {
                    builder.append(current)
                }
            }
            justStarted = true
        }
        throwIncompleteIf { !justEnded }
        val content = builder.toString()
        return TomlLiteral(content)
    }

    // Start right on '[', end on ']'.
    private fun parseArrayValue(): TomlArray {
        val builder = mutableListOf<TomlElement>()
        var expectValue = true
        var justEnded = false
        while (++currentIndex <= lastIndex) {
            when (val current = getChar()) {
                ' ', '\t' -> {
                    continue
                }
                '\n' -> {
                    currentLineNumber++
                }
                EndArray -> {
                    justEnded = true
                    break
                }
                ',' -> {
                    throwUnexpectedTokenIf(current) { expectValue }
                    expectValue = true
                }
                Comment -> {
                    parseComment()
                }
                else -> {
                    currentIndex--
                    builder.add(parseValue(inStructure = true))
                    expectValue = false
                }
            }
        }
        throwIncompleteIf { !justEnded }
        return TomlArray(builder)
    }

    // Start right on '{', end on '}'.
    private fun parseInlineTableValue(): TomlTable {
        val builder = KeyNode("")
        var expectEntry = true
        var justStarted = true
        var justEnded = false
        while (++currentIndex <= lastIndex) {
            when (val current = getChar()) {
                ' ', '\t' -> {
                    continue
                }
                '\n' -> {
                    throwIncomplete()
                }
                EndTable -> {
                    throwUnexpectedTokenIf(current) { expectEntry && !justStarted }
                    justEnded = true
                    break
                }
                ',' -> {
                    throwUnexpectedTokenIf(current) { expectEntry }
                    expectEntry = true
                }
                Comment -> {
                    throwUnexpectedToken(current)
                }
                else -> {
                    currentIndex--
                    val path = parsePath()
                    expectNext(KeyValueDelimiter)
                    val node = ValueNode(path.last(), parseValue(inStructure = true))
                    builder.addByPath(path, node, null)
                    expectEntry = false
                    justStarted = false
                }
            }
        }
        throwIncompleteIf { !justEnded }
        return TomlTable(builder)
    }

    // Start right on 'n', end on the second 'l'.
    private fun parseNullValue(): TomlNull {
        expectNext("ull")
        return TomlNull
    }

    // Start right on '#', end right before '\n'.
    private fun parseComment() {
        while (++currentIndex <= lastIndex) {
            if (getChar() == '\n') {
                break
            }
        }
        currentIndex--
    }
}
