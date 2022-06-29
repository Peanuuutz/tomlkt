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

package net.peanuuutz.tomlkt.internal

import kotlin.math.pow

internal const val INDENT: String = "    "

internal val BARE_KEY_REGEX: Regex = Regex("[A-Za-z0-9_-]+")

internal operator fun Regex.contains(char: Char): Boolean = matches(char.toString())

internal inline val String.singleQuoted: String get() = "'$this'"

internal inline val String.doubleQuoted: String get() = "\"$this\""

internal fun String.doubleQuotedIfNeeded(): String = if (BARE_KEY_REGEX matches this) this else doubleQuoted

internal fun Char.escape(multiline: Boolean = false): String = Mappings.escape(this, multiline)

internal fun String.escape(multiline: Boolean = false): String = map { it.escape(multiline) }.joinToString("")

internal fun String.unescape(): String = if (isBlank()) this else Mappings.unescape(this)

private object Mappings {
    private val mappings: List<String> = buildList(128) {
        for (i in 0..0xf)
            add(i, "\\u000$i")
        for (i in 0x10..0x1f)
            add(i, "\\u00$i")
        for (i in 0x20..0x7f)
            add(i, i.toChar().toString())
        set('\b'.code, "\\b")
        set('\t'.code, "\\t")
        set('\n'.code, "\\n")
        set(12, "\\f")
        set('\r'.code, "\\r")
        set('"'.code, "\\\"")
        set('\\'.code, "\\\\")
    }

    fun escape(char: Char, multiline: Boolean): String = if (char.code >= 128) {
        char.toString()
    } else {
        if (!multiline) {
            mappings[char.code]
        } else {
            if (char == '\\' || char != '\t' && char != '\r' && char != '\n') {
                mappings[char.code]
            } else {
                char.toString()
            }
        }
    }

    fun unescape(string: String): String {
        val builder = StringBuilder()
        var i = -1
        while (++i < string.length) {
            if (string[i] != '\\') {
                builder.append(string[i])
            } else {
                require(i != string.lastIndex)
                when (val c = string[i + 1]) {
                    'b', 't', 'n', 'f', 'r', '"', '\\' -> {
                        builder.append(mappings.indexOf("\\$c").toChar())
                        i++
                    }
                    'u' -> {
                        require(string.lastIndex >= i + 5)
                        builder.append(mappings.indexOf(string.substring(i, i + 6)).toChar())
                        i += 5
                    }
                    else -> throw IllegalArgumentException("$c")
                }
            }
        }
        return builder.toString()
    }
}

internal object S {
    const val COMMENT = '#'
    const val KEY_VALUE_DELIMITER = '='
    const val START_ARRAY = '['
    const val END_ARRAY = ']'
    const val START_TABLE = '{'
    const val END_TABLE = '}'
    const val ITEM_DELIMITER = ','
    const val PATH_DELIMITER = '.'
}

internal const val DEC_RANGE: String = "0123456789"

internal val RADIX: Map<Char, Int> = mapOf('x' to 16, 'o' to 8, 'b' to 2)

internal fun Number.toStringModified(): String = when (this) {
    Double.POSITIVE_INFINITY, Float.POSITIVE_INFINITY -> "inf"
    Double.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY -> "-inf"
    Double.NaN, Float.NaN -> "nan"
    else -> toString()
}

internal fun String.toNumber(
    positive: Boolean,
    radix: Int,
    isDouble: Boolean,
    isExponent: Boolean
): Number = if (isDouble) {
    var factor = if (positive) 1.0 else -1.0
    if (isExponent) {
        val strings = split('e', ignoreCase = true)
        factor *= 10.0.pow(strings[1].toInt())
        strings[0].toDouble() * factor
    } else {
        toDouble() * factor
    }
} else {
    var factor = if (positive) 1L else -1L
    if (isExponent) {
        val strings = split('e', ignoreCase = true)
        factor *= (10.0.pow(strings[1].toInt())).toLong()
        strings[0].toLong(radix) * factor
    } else {
        toLong(radix) * factor
    }
}