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
import net.peanuuutz.tomlkt.internal.S
import net.peanuuutz.tomlkt.internal.BARE_KEY_REGEX
import net.peanuuutz.tomlkt.internal.contains
import net.peanuuutz.tomlkt.internal.RADIX
import net.peanuuutz.tomlkt.internal.DEC_RANGE
import net.peanuuutz.tomlkt.internal.toNumber
import net.peanuuutz.tomlkt.internal.unescape

internal class TomlFileParser(source: String) : TomlParser<TomlTable> {
    private val source: String = source.replace("\r\n", "\n")

    private var line: Int = 1

    private var currentPosition: Int = -1

    // region Utils

    private fun getChar(offset: Int = 0): Char = source[currentPosition + offset]

    private fun beforeFinal(offset: Int = 0): Boolean = currentPosition < source.lastIndex - offset

    private fun surroundedBy(previous: String, next: String): Boolean {
        return getChar(-1) in previous && beforeFinal() && getChar(1) in next
    }

    @OptIn(ExperimentalContracts::class)
    private fun incompleteOn(predicate: Boolean) {
        contract { returns() implies !predicate }
        if (predicate)
            throw IncompleteException(line)
    }

    private fun unexpectedToken(token: Char): Nothing = throw UnexpectedTokenException(token, line)

    @OptIn(ExperimentalContracts::class)
    private fun Char.unexpectedOn(predicate: Boolean) {
        contract { returns() implies !predicate }
        if (predicate)
            unexpectedToken(this)
    }

    // Start right before the [chars], end on the last token
    private fun expectNext(chars: CharSequence) {
        incompleteOn(!beforeFinal(chars.length - 1))
        chars.forEach {
            currentPosition++
            incompleteOn(getChar() != it)
        }
    }

    // endregion

    override fun parse(): TomlTable {
        val tree = KeyNode("")
        val arrayOfTableIndices = mutableMapOf<Path, Int>()
        var tablePath: Path? = null
        while (++currentPosition < source.length) {
            when (val c = getChar()) {
                ' ', '\t' -> continue
                '\n' -> line++
                in BARE_KEY_REGEX, '"', '\'' -> {
                    currentPosition--
                    val actualPath = tablePath?.plus(parsePath()) ?: parsePath()
                    expectNext("${S.KEY_VALUE_DELIMITER}")
                    tree.addByPath(actualPath, ValueNode(actualPath.last(), parseValue(false)), arrayOfTableIndices)
                }
                S.COMMENT -> parseComment()
                S.START_ARRAY -> {
                    incompleteOn(!beforeFinal())
                    val isArrayOfTable = getChar(1) == S.START_ARRAY
                    if (isArrayOfTable)
                        currentPosition++
                    tablePath = parseTableHead(isArrayOfTable).also { path ->
                        if (isArrayOfTable) {
                            arrayOfTableIndices.keys.removeAll { it != path && it.containsAll(path) }
                            val index = arrayOfTableIndices[path]
                            arrayOfTableIndices[path] = if (index == null) {
                                tree.addByPath(path, ArrayNode(path.last()), arrayOfTableIndices)
                                0
                            } else index + 1
                            tree.getByPath<ArrayNode>(path, arrayOfTableIndices).add(KeyNode(""))
                        } else tree.addByPath(path, KeyNode(path.last()), arrayOfTableIndices)
                    }
                }
                else -> unexpectedToken(c)
            }
        }
        return TomlTable(tree)
    }

    // Start right on the last '[', end on the last ']'
    private fun parseTableHead(isArrayOfTable: Boolean): Path {
        var path: Path? = null
        var justEnded = false
        while (++currentPosition < source.length) {
            when (val c = getChar()) {
                ' ', '\t' -> continue
                '\n' -> incompleteOn(true)
                S.END_ARRAY -> {
                    if (isArrayOfTable) {
                        incompleteOn(!beforeFinal() || getChar(1) != S.END_ARRAY)
                        currentPosition++
                    }
                    justEnded = true
                    break
                }
                else -> {
                    c.unexpectedOn(path != null)
                    currentPosition--
                    path = parsePath()
                }
            }
        }
        incompleteOn(path == null || !justEnded)
        return path
    }

    // Start right before the actual token, end right before '=' or ']'
    private fun parsePath(): Path {
        val path = mutableListOf<String>()
        var expectKey = true
        while (++currentPosition < source.length) {
            when (val c = getChar()) {
                ' ', '\t' -> continue
                '\n' -> incompleteOn(true)
                in BARE_KEY_REGEX -> {
                    c.unexpectedOn(!expectKey)
                    path.add(parseBareKey())
                    expectKey = false
                }
                '"' -> {
                    c.unexpectedOn(!expectKey)
                    path.add(parseStringKey())
                    expectKey = false
                }
                '\'' -> {
                    c.unexpectedOn(!expectKey)
                    path.add(parseLiteralStringKey())
                    expectKey = false
                }
                S.PATH_DELIMITER -> {
                    c.unexpectedOn(expectKey)
                    expectKey = true
                }
                S.KEY_VALUE_DELIMITER, S.END_ARRAY -> {
                    c.unexpectedOn(expectKey)
                    break
                }
                else -> unexpectedToken(c)
            }
        }
        incompleteOn(!beforeFinal(-1))
        currentPosition--
        return path
    }

    // Start right on the actual token, end right before ' ' or '\t' or '\n' or '=' or '.' or ']'
    private fun parseBareKey(): String {
        val builder = StringBuilder().append(getChar())
        while (++currentPosition < source.length) {
            when (val c = getChar()) {
                ' ', '\t', S.KEY_VALUE_DELIMITER, S.PATH_DELIMITER, S.END_ARRAY -> break
                '\n' -> incompleteOn(true)
                else -> builder.append(c)
            }
        }
        val result = builder.toString()
        if (BARE_KEY_REGEX matches result) { // Lazy verification
            currentPosition--
            return result
        } else unexpectedToken(result.filterNot(BARE_KEY_REGEX::contains)[0])
    }

    // Start right on the first '"', end on the last '"'
    private fun parseStringKey(): String = parseStringValue().content

    // Start right on the first '\'', end on the last '\''
    private fun parseLiteralStringKey(): String = parseLiteralStringValue().content

    // Start right before the actual token, end right before '\n' or ',' or ']' or '}'
    private fun parseValue(inStructure: Boolean): TomlElement {
        var element: TomlElement? = null
        while (++currentPosition < source.length) {
            when (val c = getChar()) {
                ' ', '\t' -> continue
                '\n' -> {
                    incompleteOn(element == null)
                    break
                }
                S.COMMENT -> parseComment()
                S.ITEM_DELIMITER, S.END_ARRAY, S.END_TABLE -> {
                    c.unexpectedOn(!inStructure)
                    break
                }
                else -> {
                    c.unexpectedOn(element != null)
                    element = when (c) {
                        't', 'f' -> parseBooleanValue()
                        in DEC_RANGE -> parseNumberValue(true)
                        'i' -> parseNonNumberValue(true)
                        'n' -> {
                            incompleteOn(!beforeFinal())
                            when (getChar(1)) {
                                'a' -> parseNonNumberValue(true)
                                'u' -> parseNullValue()
                                else -> unexpectedToken(getChar(1))
                            }
                        }
                        '+', '-' -> {
                            incompleteOn(!beforeFinal())
                            when (source[++currentPosition]) {
                                in DEC_RANGE -> parseNumberValue(c == '+')
                                'i', 'n' -> parseNonNumberValue(c == '+')
                                else -> unexpectedToken(getChar())
                            }
                        }
                        '"' -> parseStringValue()
                        '\'' -> parseLiteralStringValue()
                        S.START_ARRAY -> parseArrayValue()
                        S.START_TABLE -> parseInlineTableValue()
                        else -> unexpectedToken(c)
                    }
                }
            }
        }
        incompleteOn(element == null)
        currentPosition--
        return element
    }

    // Start right on 't' or 'f', ends on the last token
    private fun parseBooleanValue(): TomlLiteral = when (getChar()) {
        't' -> {
            expectNext("rue")
            TomlLiteral(true)
        }
        'f' -> {
            expectNext("alse")
            TomlLiteral(false)
        }
        else -> unexpectedToken(getChar())
    }

    // Start right on 'i' or 'n', end on the last token
    private fun parseNonNumberValue(positive: Boolean): TomlLiteral = when (getChar()) {
        'i' -> {
            expectNext("nf")
            TomlLiteral(if (positive) "inf" else "-inf", false)
        }
        'n' -> {
            expectNext("an")
            TomlLiteral("nan", false)
        }
        else -> unexpectedToken(getChar())
    }

    // Start right on the actual token, end right before ' ' or '\t' or '\n' or '#' or ',' or ']' or '}'
    private fun parseNumberValue(positive: Boolean): TomlLiteral {
        val builder = StringBuilder().append(getChar())
        if (!beforeFinal())
            return TomlLiteral(builder.toString().toLong())
        val radix = if (builder[0] != '0')
            10
        else when (val c = getChar(1)) {
            'x', 'o', 'b' -> {
                currentPosition++
                RADIX[c]!!
            }
            in DEC_RANGE -> unexpectedToken(c)
            else -> 10
        }
        var isDouble = false
        var isExponent = false
        while (++currentPosition < source.length) {
            when (val c = getChar()) {
                ' ', '\t', '\n', S.COMMENT, S.ITEM_DELIMITER, S.END_ARRAY, S.END_TABLE -> break
                '0', '1' -> builder.append(c)
                '2', '3', '4', '5', '6', '7' -> {
                    c.unexpectedOn(radix == 2)
                    builder.append(c)
                }
                '8', '9' -> {
                    c.unexpectedOn(radix <= 8)
                    builder.append(c)
                }
                S.PATH_DELIMITER -> {
                    c.unexpectedOn(isExponent || isDouble || radix != 10 || !surroundedBy(DEC_RANGE, DEC_RANGE))
                    builder.append(c)
                    isDouble = true
                }
                'e', 'E' -> {
                    c.unexpectedOn(radix <= 8)
                    if (radix == 10) {
                        c.unexpectedOn(isExponent || !surroundedBy(DEC_RANGE, "$DEC_RANGE+-"))
                        isExponent = true
                    }
                    builder.append(c)
                }
                'a', 'b', 'c', 'd', 'f', 'A', 'B', 'C', 'D', 'F' -> {
                    c.unexpectedOn(radix <= 10)
                    builder.append(c)
                }
                '_' -> c.unexpectedOn(!surroundedBy(DEC_RANGE, DEC_RANGE))
                '+' -> c.unexpectedOn(getChar(-1) != 'e' && getChar(-1) != 'E')
                '-' -> {
                    c.unexpectedOn(getChar(-1) != 'e' && getChar(-1) != 'E')
                    isDouble = true
                    builder.append(c)
                }
                else -> unexpectedToken(c)
            }
        }
        currentPosition--
        return TomlLiteral(builder.toString().toNumber(positive, radix, isDouble, isExponent))
    }

    // Start right on the first '"', end on the last '"'
    private fun parseStringValue(): TomlLiteral {
        incompleteOn(!beforeFinal())
        val multiline = checkIsMultiline('"') { return TomlLiteral("") }
        val builder = StringBuilder()
        var trim = false
        var justEnded = false
        while (++currentPosition < source.length) {
            when (val c = getChar()) {
                ' ', '\t' -> if (!trim) builder.append(c)
                '\n' -> {
                    incompleteOn(!multiline)
                    if (!trim)
                        builder.append(c)
                    line++
                }
                '"' -> {
                    if (!multiline) {
                        if (getChar(-1) != '\\') {
                            justEnded = true
                            break
                        }
                        builder.append(c)
                    } else {
                        incompleteOn(!beforeFinal(1))
                        if (getChar(1) == '"' && getChar(2) == '"') {
                            currentPosition += 2
                            justEnded = true
                            break
                        }
                        builder.append(c)
                    }
                }
                '\\' -> {
                    incompleteOn(!beforeFinal())
                    when (val nextC = getChar(1)) {
                        ' ', '\t', '\n' -> {
                            c.unexpectedOn(!multiline)
                            trim = true
                        }
                        'u', 'b', 't', 'n', 'f', 'r', '"', '\\' -> {
                            builder.append(c).append(nextC)
                            currentPosition++
                        }
                        else -> unexpectedToken(c)
                    }
                }
                else -> {
                    builder.append(c)
                    trim = false
                }
            }
        }
        incompleteOn(!justEnded)
        return TomlLiteral(builder.toString().unescape())
    }

    // Start right on '\'', end on the last '\''
    private fun parseLiteralStringValue(): TomlLiteral {
        incompleteOn(!beforeFinal())
        val multiline = checkIsMultiline('\'') { return TomlLiteral("") }
        val builder = StringBuilder()
        var justEnded = false
        while (++currentPosition < source.length) {
            when (val c = getChar()) {
                ' ', '\t' -> builder.append(c)
                '\n' -> {
                    incompleteOn(!multiline)
                    builder.append(c)
                    line++
                }
                '\'' -> {
                    if (!multiline) {
                        justEnded = true
                        break
                    }
                    incompleteOn(!beforeFinal(1))
                    if (getChar(1) == '\'' && getChar(2) == '\'') {
                        currentPosition += 2
                        justEnded = true
                        break
                    }
                    builder.append(c)
                }
                else -> builder.append(c)
            }
        }
        incompleteOn(!justEnded)
        return TomlLiteral(builder.toString())
    }

    private inline fun checkIsMultiline(quote: Char, whenEmpty: () -> Unit): Boolean {
        return if (getChar(1) != quote) {
            false
        } else if (beforeFinal(1) && getChar(2) == quote) {
            currentPosition += 2
            incompleteOn(!beforeFinal())
            if (getChar(1) == '\n')
                currentPosition++
            true
        } else {
            currentPosition++
            whenEmpty() // Should always return in place
            false
        }
    }

    // Start right on '[', end on ']'
    private fun parseArrayValue(): TomlArray {
        val builder = mutableListOf<TomlElement>()
        var expectValue = true
        var justEnded = false
        while (++currentPosition < source.length) {
            when (val c = getChar()) {
                ' ', '\t' -> continue
                '\n' -> line++
                S.END_ARRAY -> {
                    justEnded = true
                    break
                }
                S.ITEM_DELIMITER -> {
                    c.unexpectedOn(expectValue)
                    expectValue = true
                }
                S.COMMENT -> parseComment()
                else -> {
                    currentPosition--
                    builder.add(parseValue(true))
                    expectValue = false
                }
            }
        }
        incompleteOn(!justEnded)
        return TomlArray(builder)
    }

    // Start right on '{', end on '}'
    private fun parseInlineTableValue(): TomlTable {
        val builder = KeyNode("")
        var expectEntry = true
        var justStarted = true
        var justEnded = false
        while (++currentPosition < source.length) {
            when (val c = getChar()) {
                ' ', '\t' -> continue
                '\n' -> incompleteOn(true)
                S.END_TABLE -> {
                    c.unexpectedOn(expectEntry && !justStarted)
                    justEnded = true
                    break
                }
                S.ITEM_DELIMITER -> {
                    c.unexpectedOn(expectEntry)
                    expectEntry = true
                }
                S.COMMENT -> unexpectedToken(c)
                else -> {
                    currentPosition--
                    val path = parsePath()
                    expectNext("${S.KEY_VALUE_DELIMITER}")
                    builder.addByPath(path, ValueNode(path.last(), parseValue(true)), null)
                    expectEntry = false
                    justStarted = false
                }
            }
        }
        incompleteOn(!justEnded)
        return TomlTable(builder)
    }

    // Start right on 'n', end on the last token
    private fun parseNullValue(): TomlNull {
        expectNext("ull")
        return TomlNull
    }

    // Start right on '#', end right before '\n'
    private fun parseComment() {
        while (++currentPosition < source.length) {
            if (getChar() == '\n')
                break
        }
        currentPosition--
    }
}