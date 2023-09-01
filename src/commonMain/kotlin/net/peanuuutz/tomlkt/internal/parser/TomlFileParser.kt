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
import net.peanuuutz.tomlkt.internal.IncompleteException
import net.peanuuutz.tomlkt.internal.KeyValueDelimiter
import net.peanuuutz.tomlkt.internal.StartArray
import net.peanuuutz.tomlkt.internal.StartTable
import net.peanuuutz.tomlkt.internal.UnexpectedTokenException
import net.peanuuutz.tomlkt.internal.toNumber
import net.peanuuutz.tomlkt.internal.unescape
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

internal class TomlFileParser(source: String) {
    private val source: String = source.replace("\r\n", "\n")

    private val lastIndex: Int = this.source.lastIndex

    private var currentLineNumber: Int = 1

    private var currentIndex: Int = -1

    // region Utils

    @Suppress("NOTHING_TO_INLINE")
    private inline fun getChar(): Char {
        return source[currentIndex]
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun getChar(offset: Int): Char {
        return source[currentIndex + offset]
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun isEof(): Boolean {
        return currentIndex >= lastIndex
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun isEof(offset: Int): Boolean {
        return currentIndex >= lastIndex + offset
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun beforeEof(): Boolean {
        return currentIndex < lastIndex
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun beforeEof(offset: Int): Boolean {
        return currentIndex < lastIndex + offset
    }

    private fun incomplete(): Nothing {
        throw IncompleteException(currentLineNumber)
    }

    @OptIn(ExperimentalContracts::class)
    private fun incompleteIf(predicate: Boolean) {
        contract { returns() implies !predicate }

        if (predicate) {
            incomplete()
        }
    }

    private fun unexpectedToken(token: Char): Nothing {
        throw UnexpectedTokenException(token, currentLineNumber)
    }

    @OptIn(ExperimentalContracts::class)
    private fun Char.unexpectedIf(predicate: Boolean) {
        contract { returns() implies !predicate }

        if (predicate) {
            unexpectedToken(this)
        }
    }

    private fun expectNext(testChar: Char) {
        incompleteIf(isEof())
        currentIndex++
        val currentChar = getChar()
        currentChar.unexpectedIf(currentChar != testChar)
    }

    // Start right before [testString], end on the last token.
    private fun expectNext(testString: String) {
        val requiredOffset = -(testString.length - 1)
        incompleteIf(isEof(requiredOffset))
        for (i in testString.indices) {
            currentIndex++
            val currentChar = getChar()
            currentChar.unexpectedIf(currentChar != testString[i])
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
                    incompleteIf(isEof())
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
                    unexpectedToken(current)
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
                    incomplete()
                }
                EndArray -> {
                    if (isArrayOfTable) {
                        incompleteIf(isEof() || getChar(1) != EndArray)
                        currentIndex++
                    }
                    justEnded = true
                    break
                }
                else -> {
                    current.unexpectedIf(path != null)
                    currentIndex--
                    path = parsePath()
                }
            }
        }
        incompleteIf(path == null || !justEnded)
        return path
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
                    incomplete()
                }
                in BareKeyConstraints -> {
                    current.unexpectedIf(!expectKey)
                    path.add(parseBareKey())
                    expectKey = false
                }
                '"' -> {
                    current.unexpectedIf(!expectKey)
                    path.add(parseStringKey())
                    expectKey = false
                }
                '\'' -> {
                    current.unexpectedIf(!expectKey)
                    path.add(parseLiteralStringKey())
                    expectKey = false
                }
                '.' -> {
                    current.unexpectedIf(expectKey)
                    expectKey = true
                }
                KeyValueDelimiter, EndArray -> {
                    current.unexpectedIf(expectKey)
                    justEnded = true
                    currentIndex--
                    break
                }
                else -> {
                    unexpectedToken(current)
                }
            }
        }
        incompleteIf(!justEnded)
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
                    incomplete()
                }
                else -> {
                    builder.append(current)
                }
            }
        }
        val result = builder.toString()
        if (BareKeyRegex.matches(result).not()) { // Lazy check.
            val unexpectedTokens = result.filterNot(BareKeyConstraints::contains)
            unexpectedToken(unexpectedTokens[0])
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
                    incompleteIf(element == null)
                    break
                }
                Comment -> {
                    parseComment()
                }
                ',', EndArray, EndTable -> {
                    current.unexpectedIf(!inStructure)
                    break
                }
                else -> {
                    current.unexpectedIf(element != null)
                    element = when (current) {
                        't', 'f' -> {
                            parseBooleanValue()
                        }
                        in DecimalConstraints -> {
                            parseNumberOrDateTimeValue()
                        }
                        'i' -> {
                            parseSpecialNumberValue(positive = true)
                        }
                        'n' -> {
                            incompleteIf(isEof())
                            when (val next = getChar(1)) {
                                'a' -> {
                                    parseSpecialNumberValue(positive = true)
                                }
                                'u' -> {
                                    parseNullValue()
                                }
                                else -> {
                                    unexpectedToken(next)
                                }
                            }
                        }
                        '+', '-' -> {
                            incompleteIf(isEof())
                            currentIndex++
                            when (val next = getChar()) {
                                in DecimalConstraints -> {
                                    parseNumberValue(current == '+')
                                }
                                'i', 'n' -> {
                                    parseSpecialNumberValue(current == '+')
                                }
                                else -> {
                                    unexpectedToken(next)
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
                            unexpectedToken(current)
                        }
                    }
                }
            }
        }
        incompleteIf(element == null)
        currentIndex--
        return element
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
                unexpectedToken(current)
            }
        }
    }

    // Start right on 'i' or 'n', end on the last token.
    private fun parseSpecialNumberValue(positive: Boolean): TomlLiteral {
        return when (val current = getChar()) {
            'i' -> {
                expectNext("nf")
                TomlLiteral(
                    content = if (positive) "inf" else "-inf",
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
                unexpectedToken(current)
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
                    if (beforeEof(-testOffset) && getChar(testOffset + 1) in DecimalConstraints) {
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
                    unexpectedToken(current)
                }
            }
        }
        return if (isNumber) {
            parseNumberValue(positive = true)
        } else {
            parseDateTimeValue()
        }
    }

    // Start right on the actual token, end right before ' ' or '\t' or '\n' or '#' or ',' or ']' or
    // '}'.
    private fun parseNumberValue(positive: Boolean): TomlLiteral {
        val first = getChar()
        if (isEof()) {
            return TomlLiteral(
                content = first.toString(),
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
                    current.unexpectedIf(radix == 2)
                    builder.append(current)
                }
                '8', '9' -> {
                    current.unexpectedIf(radix <= 8)
                    builder.append(current)
                }
                '.' -> {
                    val surroundedByNumber = getChar(-1) in DecimalConstraints &&
                            beforeEof() &&
                            getChar(1) in DecimalConstraints
                    val unexpected = isExponent ||
                            isDouble ||
                            radix != 10 ||
                            !surroundedByNumber
                    current.unexpectedIf(unexpected)
                    builder.append(current)
                    isDouble = true
                }
                'e', 'E' -> {
                    current.unexpectedIf(radix <= 8)
                    if (radix == 10) {
                        val surroundedByNumber = getChar(-1) in DecimalConstraints &&
                                beforeEof() &&
                                getChar(1) in DecimalOrSignConstraints
                        val unexpected = isExponent || !surroundedByNumber
                        current.unexpectedIf(unexpected)
                        isExponent = true
                    }
                    builder.append(current)
                }
                'a', 'b', 'c', 'd', 'f', 'A', 'B', 'C', 'D', 'F' -> {
                    current.unexpectedIf(radix <= 10)
                    builder.append(current)
                }
                '_' -> {
                    val surroundedByNumber = getChar(-1) in DecimalConstraints &&
                            beforeEof() &&
                            getChar(1) in DecimalConstraints
                    current.unexpectedIf(!surroundedByNumber)
                }
                '+' -> {
                    val previous = getChar(-1)
                    current.unexpectedIf(previous != 'e' && previous != 'E')
                }
                '-' -> {
                    val previous = getChar(-1)
                    current.unexpectedIf(previous != 'e' && previous != 'E')
                    isDouble = true
                    builder.append(current)
                }
                else -> {
                    unexpectedToken(current)
                }
            }
        }
        currentIndex--
        val number = builder.toString().toNumber(positive, radix, isDouble, isExponent)
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
                    unexpectedToken(current)
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
        incompleteIf(isEof())
        var next = getChar(1)
        val multiline: Boolean
        when {
            next != '"' -> {
                multiline = false
            }
            beforeEof(-1) && getChar(2) == '"' -> {
                multiline = true
                currentIndex += 2
                incompleteIf(isEof())
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
        var justEnded = false
        while (++currentIndex <= lastIndex) {
            when (val current = getChar()) {
                ' ', '\t' -> {
                    if (!trim) {
                        builder.append(current)
                    }
                }
                '\n' -> {
                    incompleteIf(!multiline)
                    if (!trim) {
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
                        incompleteIf(isEof())
                        if (getChar(1) == '"' && getChar(2) == '"') {
                            currentIndex += 2
                            justEnded = true
                            break
                        }
                    }
                    builder.append(current)
                }
                '\\' -> {
                    incompleteIf(isEof())
                    next = getChar(1)
                    when (next) {
                        ' ', '\t', '\n' -> {
                            current.unexpectedIf(!multiline)
                            trim = true
                        }
                        'u', 'b', 't', 'n', 'f', 'r', '"', '\\' -> {
                            builder.append(current).append(next)
                            currentIndex++
                        }
                        else -> {
                            unexpectedToken(current)
                        }
                    }
                }
                else -> {
                    builder.append(current)
                    trim = false
                }
            }
        }
        incompleteIf(!justEnded)
        val content = builder.toString().unescape()
        return TomlLiteral(content)
    }

    // Start right on '\'', end on the last '\''.
    private fun parseLiteralStringValue(): TomlLiteral {
        incompleteIf(isEof())
        val next = getChar(1)
        val multiline: Boolean
        when {
            next != '\'' -> {
                multiline = false
            }
            beforeEof(-1) && getChar(2) == '\'' -> {
                multiline = true
                currentIndex += 2
                incompleteIf(isEof())
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
        var justEnded = false
        while (++currentIndex <= lastIndex) {
            when (val current = getChar()) {
                ' ', '\t' -> {
                    builder.append(current)
                }
                '\n' -> {
                    incompleteIf(!multiline)
                    builder.append(current)
                    currentLineNumber++
                }
                '\'' -> {
                    if (!multiline) {
                        justEnded = true
                        break
                    }
                    incompleteIf(isEof())
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
        }
        incompleteIf(!justEnded)
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
                    current.unexpectedIf(expectValue)
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
        incompleteIf(!justEnded)
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
                    incomplete()
                }
                EndTable -> {
                    current.unexpectedIf(expectEntry && !justStarted)
                    justEnded = true
                    break
                }
                ',' -> {
                    current.unexpectedIf(expectEntry)
                    expectEntry = true
                }
                Comment -> {
                    unexpectedToken(current)
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
        incompleteIf(!justEnded)
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
