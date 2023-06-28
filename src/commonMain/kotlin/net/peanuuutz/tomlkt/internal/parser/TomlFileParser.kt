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

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import net.peanuuutz.tomlkt.TomlElement
import net.peanuuutz.tomlkt.TomlNull
import net.peanuuutz.tomlkt.TomlLiteral
import net.peanuuutz.tomlkt.TomlArray
import net.peanuuutz.tomlkt.TomlTable
import net.peanuuutz.tomlkt.internal.IncompleteException
import net.peanuuutz.tomlkt.internal.UnexpectedTokenException
import net.peanuuutz.tomlkt.internal.BARE_KEY_REGEX
import net.peanuuutz.tomlkt.internal.COMMENT
import net.peanuuutz.tomlkt.internal.contains
import net.peanuuutz.tomlkt.internal.DEC_CHARS
import net.peanuuutz.tomlkt.internal.DEC_CHARS_AND_SIGN
import net.peanuuutz.tomlkt.internal.END_ARRAY
import net.peanuuutz.tomlkt.internal.END_TABLE
import net.peanuuutz.tomlkt.internal.KEY_VALUE_DELIMITER
import net.peanuuutz.tomlkt.internal.START_ARRAY
import net.peanuuutz.tomlkt.internal.START_TABLE
import net.peanuuutz.tomlkt.internal.toNumber
import net.peanuuutz.tomlkt.internal.unescape

internal class TomlFileParser(source: String) {
    private val source: String = source.replace("\r\n", "\n")

    private val lastIndex: Int = this.source.lastIndex

    private var currentLine: Int = 1

    private var currentPosition: Int = -1

    // region Utils

    private inline fun getChar(): Char {
        return source[currentPosition]
    }

    private inline fun getChar(offset: Int): Char {
        return source[currentPosition + offset]
    }

    private inline fun lastOrEOF(): Boolean {
        return currentPosition >= lastIndex
    }

    private inline fun lastOrEOF(offset: Int): Boolean {
        return currentPosition >= lastIndex + offset
    }

    private inline fun beforeLast(): Boolean {
        return currentPosition < lastIndex
    }

    private inline fun beforeLast(offset: Int): Boolean {
        return currentPosition < lastIndex + offset
    }

    private fun incomplete(): Nothing {
        throw IncompleteException(currentLine)
    }

    @OptIn(ExperimentalContracts::class)
    private fun incompleteIf(predicate: Boolean) {
        contract { returns() implies !predicate }

        if (predicate) {
            incomplete()
        }
    }

    private fun unexpectedToken(token: Char): Nothing {
        throw UnexpectedTokenException(token, currentLine)
    }

    @OptIn(ExperimentalContracts::class)
    private fun Char.unexpectedIf(predicate: Boolean) {
        contract { returns() implies !predicate }

        if (predicate) {
            unexpectedToken(this)
        }
    }

    private fun expectNext(testChar: Char) {
        incompleteIf(lastOrEOF())
        currentPosition++
        val currentChar = getChar()
        currentChar.unexpectedIf(currentChar != testChar)
    }

    // Start right before [testString], end on the last token
    private fun expectNext(testString: String) {
        val requiredOffset = -(testString.length - 1)
        incompleteIf(lastOrEOF(requiredOffset))
        for (i in testString.indices) {
            currentPosition++
            val currentChar = getChar()
            currentChar.unexpectedIf(currentChar != testString[i])
        }
    }

    // endregion

    fun parse(): TomlTable {
        val tree = KeyNode("")
        val arrayOfTableIndices = mutableMapOf<Path, Int>()
        var tablePath: Path? = null
        while (++currentPosition <= lastIndex) {
            when (val current = getChar()) {
                ' ', '\t' -> continue
                '\n' -> currentLine++
                in BARE_KEY_REGEX, '"', '\'' -> {
                    currentPosition--
                    val localPath = parsePath()
                    expectNext(KEY_VALUE_DELIMITER)
                    val path = if (tablePath != null) tablePath + localPath else localPath
                    val node = ValueNode(path.last(), parseValue(inStructure = false))
                    tree.addByPath(path, node, arrayOfTableIndices)
                }
                COMMENT -> parseComment()
                START_ARRAY -> {
                    incompleteIf(lastOrEOF())
                    val isArrayOfTable = getChar(1) == START_ARRAY
                    if (isArrayOfTable) {
                        currentPosition++
                    }
                    val path = parseTableHead(isArrayOfTable)
                    if (isArrayOfTable) {
                        // Copied from official codebase because theirs is not inline
                        val iterator = arrayOfTableIndices.keys.iterator()
                        while (iterator.hasNext()) {
                            val next = iterator.next()
                            if (next != path && next.containsAll(path)) {
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
                else -> unexpectedToken(current)
            }
        }
        return TomlTable(tree)
    }

    // Start right on the last '[', end on the last ']'
    private fun parseTableHead(isArrayOfTable: Boolean): Path {
        var path: Path? = null
        var justEnded = false
        while (++currentPosition <= lastIndex) {
            when (val current = getChar()) {
                ' ', '\t' -> continue
                '\n' -> incomplete()
                END_ARRAY -> {
                    if (isArrayOfTable) {
                        incompleteIf(lastOrEOF() || getChar(1) != END_ARRAY)
                        currentPosition++
                    }
                    justEnded = true
                    break
                }
                else -> {
                    current.unexpectedIf(path != null)
                    currentPosition--
                    path = parsePath()
                }
            }
        }
        incompleteIf(path == null || !justEnded)
        return path
    }

    // Start right before the actual token, end right before '=' or ']'
    private fun parsePath(): Path {
        val path = mutableListOf<String>()
        var justEnded = false
        var expectKey = true
        while (++currentPosition <= lastIndex) {
            when (val current = getChar()) {
                ' ', '\t' -> continue
                '\n' -> incomplete()
                in BARE_KEY_REGEX -> {
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
                KEY_VALUE_DELIMITER, END_ARRAY -> {
                    current.unexpectedIf(expectKey)
                    justEnded = true
                    currentPosition--
                    break
                }
                else -> unexpectedToken(current)
            }
        }
        incompleteIf(!justEnded)
        return path
    }

    // Start right on the actual token, end right before ' ' or '\t' or '\n' or '=' or '.' or ']'
    private fun parseBareKey(): String {
        val builder = StringBuilder().append(getChar())
        while (++currentPosition <= lastIndex) {
            when (val current = getChar()) {
                ' ', '\t', '.', KEY_VALUE_DELIMITER, END_ARRAY -> break
                '\n' -> incomplete()
                else -> builder.append(current)
            }
        }
        val result = builder.toString()
        if (BARE_KEY_REGEX matches result) { // Lazy check
            currentPosition--
            return result
        } else {
            val unexpectedTokens = result.filterNot(BARE_KEY_REGEX::contains)
            unexpectedToken(unexpectedTokens[0])
        }
    }

    // Start right on the first '"', end on the last '"'
    private fun parseStringKey(): String {
        return parseStringValue().content
    }

    // Start right on the first '\'', end on the last '\''
    private fun parseLiteralStringKey(): String {
        return parseLiteralStringValue().content
    }

    // Start right before the actual token, end right before '\n' or ',' or ']' or '}'
    private fun parseValue(inStructure: Boolean): TomlElement {
        var element: TomlElement? = null
        while (++currentPosition <= lastIndex) {
            when (val current = getChar()) {
                ' ', '\t' -> continue
                '\n' -> {
                    incompleteIf(element == null)
                    break
                }
                COMMENT -> parseComment()
                ',', END_ARRAY, END_TABLE -> {
                    current.unexpectedIf(!inStructure)
                    break
                }
                else -> {
                    current.unexpectedIf(element != null)
                    element = when (current) {
                        't', 'f' -> parseBooleanValue()
                        in DEC_CHARS -> parseNumberValue(positive = true)
                        'i' -> parseNonNumberValue(positive = true)
                        'n' -> {
                            incompleteIf(lastOrEOF())
                            when (val next = getChar(1)) {
                                'a' -> parseNonNumberValue(positive = true)
                                'u' -> parseNullValue()
                                else -> unexpectedToken(next)
                            }
                        }
                        '+', '-' -> {
                            incompleteIf(lastOrEOF())
                            currentPosition++
                            when (val next = getChar()) {
                                in DEC_CHARS -> parseNumberValue(current == '+')
                                'i', 'n' -> parseNonNumberValue(current == '+')
                                else -> unexpectedToken(next)
                            }
                        }
                        '"' -> parseStringValue()
                        '\'' -> parseLiteralStringValue()
                        START_ARRAY -> parseArrayValue()
                        START_TABLE -> parseInlineTableValue()
                        else -> unexpectedToken(current)
                    }
                }
            }
        }
        incompleteIf(element == null)
        currentPosition--
        return element
    }

    // Start right on 't' or 'f', ends on the last token
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
            else -> unexpectedToken(current)
        }
    }

    // Start right on 'i' or 'n', end on the last token
    private fun parseNonNumberValue(positive: Boolean): TomlLiteral {
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
            else -> unexpectedToken(current)
        }
    }

    // Start right on the actual token, end right before ' ' or '\t' or '\n' or '#' or ',' or ']' or '}'
    private fun parseNumberValue(positive: Boolean): TomlLiteral {
        val first = getChar()
        if (lastOrEOF()) {
            return TomlLiteral(
                content = first.toString(),
                isString = false
            )
        }
        val radix = if (first != '0') {
            10
        } else {
            when (val next = getChar(1)) {
                'x' -> {
                    currentPosition++
                    16
                }
                'o' -> {
                    currentPosition++
                    8
                }
                'b' -> {
                    currentPosition++
                    2
                }
                in DEC_CHARS -> unexpectedToken(next)
                else -> 10
            }
        }
        val builder = StringBuilder().append(first)
        var isDouble = false
        var isExponent = false
        while (++currentPosition <= lastIndex) {
            when (val current = getChar()) {
                ' ', '\t', '\n', ',', COMMENT, END_ARRAY, END_TABLE -> break
                '0', '1' -> builder.append(current)
                '2', '3', '4', '5', '6', '7' -> {
                    current.unexpectedIf(radix == 2)
                    builder.append(current)
                }
                '8', '9' -> {
                    current.unexpectedIf(radix <= 8)
                    builder.append(current)
                }
                '.' -> {
                    val surroundedByNumber = getChar(-1) in DEC_CHARS &&
                            beforeLast() &&
                            getChar(1) in DEC_CHARS
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
                        val surroundedByNumber = getChar(-1) in DEC_CHARS &&
                                beforeLast() &&
                                getChar(1) in DEC_CHARS_AND_SIGN
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
                    val surroundedByNumber = getChar(-1) in DEC_CHARS &&
                            beforeLast() &&
                            getChar(1) in DEC_CHARS
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
                else -> unexpectedToken(current)
            }
        }
        currentPosition--
        val number = builder.toString().toNumber(positive, radix, isDouble, isExponent)
        return TomlLiteral(number)
    }

    // Start right on the first '"', end on the last '"'
    private fun parseStringValue(): TomlLiteral {
        incompleteIf(lastOrEOF())
        val initialNext = getChar(1)
        val multiline: Boolean
        if (initialNext != '"') {
            multiline = false
        } else if (beforeLast(-1) && getChar(2) == '"') {
            multiline = true
            currentPosition += 2
            incompleteIf(lastOrEOF())
            if (initialNext == '\n') {
                // Consume the initial line feed
                currentPosition++
            }
        } else {
            currentPosition++
            return TomlLiteral("")
        }
        val builder = StringBuilder()
        var trim = false
        var justEnded = false
        while (++currentPosition <= lastIndex) {
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
                    currentLine++
                }
                '"' -> {
                    if (!multiline) {
                        if (getChar(-1) != '\\') {
                            justEnded = true
                            break
                        }
                    } else {
                        incompleteIf(lastOrEOF())
                        if (getChar(1) == '"' && getChar(2) == '"') {
                            currentPosition += 2
                            justEnded = true
                            break
                        }
                    }
                    builder.append(current)
                }
                '\\' -> {
                    incompleteIf(lastOrEOF())
                    when (val next = getChar(1)) {
                        ' ', '\t', '\n' -> {
                            current.unexpectedIf(!multiline)
                            trim = true
                        }
                        'u', 'b', 't', 'n', 'f', 'r', '"', '\\' -> {
                            builder.append(current).append(next)
                            currentPosition++
                        }
                        else -> unexpectedToken(current)
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

    // Start right on '\'', end on the last '\''
    private fun parseLiteralStringValue(): TomlLiteral {
        incompleteIf(lastOrEOF())
        val initialNext = getChar(1)
        val multiline: Boolean
        if (initialNext != '\'') {
            multiline = false
        } else if (beforeLast(-1) && getChar(2) == '\'') {
            multiline = true
            currentPosition += 2
            incompleteIf(lastOrEOF())
            if (initialNext == '\n') {
                currentPosition++
            }
        } else {
            currentPosition++
            return TomlLiteral("")
        }
        val builder = StringBuilder()
        var justEnded = false
        while (++currentPosition <= lastIndex) {
            when (val current = getChar()) {
                ' ', '\t' -> builder.append(current)
                '\n' -> {
                    incompleteIf(!multiline)
                    builder.append(current)
                    currentLine++
                }
                '\'' -> {
                    if (!multiline) {
                        justEnded = true
                        break
                    }
                    incompleteIf(lastOrEOF())
                    if (getChar(1) == '\'' && getChar(2) == '\'') {
                        currentPosition += 2
                        justEnded = true
                        break
                    }
                    builder.append(current)
                }
                else -> builder.append(current)
            }
        }
        incompleteIf(!justEnded)
        val content = builder.toString()
        return TomlLiteral(content)
    }

    // Start right on '[', end on ']'
    private fun parseArrayValue(): TomlArray {
        val builder = mutableListOf<TomlElement>()
        var expectValue = true
        var justEnded = false
        while (++currentPosition <= lastIndex) {
            when (val current = getChar()) {
                ' ', '\t' -> continue
                '\n' -> currentLine++
                END_ARRAY -> {
                    justEnded = true
                    break
                }
                ',' -> {
                    current.unexpectedIf(expectValue)
                    expectValue = true
                }
                COMMENT -> parseComment()
                else -> {
                    currentPosition--
                    builder.add(parseValue(inStructure = true))
                    expectValue = false
                }
            }
        }
        incompleteIf(!justEnded)
        return TomlArray(builder)
    }

    // Start right on '{', end on '}'
    private fun parseInlineTableValue(): TomlTable {
        val builder = KeyNode("")
        var expectEntry = true
        var justStarted = true
        var justEnded = false
        while (++currentPosition <= lastIndex) {
            when (val current = getChar()) {
                ' ', '\t' -> continue
                '\n' -> incomplete()
                END_TABLE -> {
                    current.unexpectedIf(expectEntry && !justStarted)
                    justEnded = true
                    break
                }
                ',' -> {
                    current.unexpectedIf(expectEntry)
                    expectEntry = true
                }
                COMMENT -> unexpectedToken(current)
                else -> {
                    currentPosition--
                    val path = parsePath()
                    expectNext(KEY_VALUE_DELIMITER)
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

    // Start right on 'n', end on the second 'l'
    private fun parseNullValue(): TomlNull {
        expectNext("ull")
        return TomlNull
    }

    // Start right on '#', end right before '\n'
    private fun parseComment() {
        while (++currentPosition <= lastIndex) {
            if (getChar() == '\n') {
                break
            }
        }
        currentPosition--
    }
}
