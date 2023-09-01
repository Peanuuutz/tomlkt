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

internal const val Comment = '#'

internal const val KeyValueDelimiter = '='

internal const val StartArray = '['

internal const val EndArray = ']'

internal const val StartTable = '{'

internal const val EndTable = '}'

internal const val DecimalConstraints: String = "0123456789"

internal const val DecimalOrSignConstraints: String = "0123456789+-"

internal const val BareKeyConstraints: String =
    "abcdefghijklmnopqrstuvwxyz" + "-_" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789"

internal const val DefiniteDateTimeConstraints: String = "Tt:Zz"

internal const val DefiniteNumberConstraints: String = "xob.acdefABCDEF_"

internal val BareKeyRegex: Regex = Regex("[A-Za-z0-9_-]+")

internal val AsciiMapping: List<String> = buildList(128) {
    for (i in 0x00..0x0f) {
        add(i, "\\u000$i")
    }
    for (i in 0x10..0x1f) {
        add(i, "\\u00$i")
    }
    for (i in 0x20..0x7f) {
        add(i, i.toChar().toString())
    }
    set('\b'.code, "\\b")
    set('\t'.code, "\\t")
    set('\n'.code, "\\n")
    set('\r'.code, "\\r")
    set('"'.code, "\\\"")
    set('\\'.code, "\\\\")
}

internal const val LineFeedCode: Int = '\n'.code

internal inline val String.singleQuoted: String
    get() = "'$this'"

internal inline val String.doubleQuoted: String
    get() = "\"$this\""

internal fun String.doubleQuotedIfNotPure(): String {
    return if (BareKeyRegex matches this) this else doubleQuoted
}

internal fun Char.escape(multiline: Boolean = false): String {
    return when {
        code >= 128 -> toString()
        !multiline -> AsciiMapping[code]
        this == '\\' -> "\\\\"
        this == '\n' -> "\n"
        this == '\t' -> "\t"
        this == '\r' -> "\r"
        else -> AsciiMapping[code]
    }
}

internal fun String.escape(multiline: Boolean = false): String {
    val builder = StringBuilder()
    for (i in indices) {
        builder.append(get(i).escape(multiline))
    }
    return builder.toString()
}

internal fun String.unescape(): String {
    if (isBlank()) {
        return this
    }
    val builder = StringBuilder()
    val lastIndex = lastIndex
    var i = 0
    while (i <= lastIndex) {
        val current = get(i)
        if (current != '\\') {
            builder.append(current)
            i++
            continue
        }
        require(i != lastIndex) { "Unexpected end in $this" }
        when (val next = get(i + 1)) {
            'n' -> {
                builder.append('\n')
                i++
            }
            't' -> {
                builder.append('\t')
                i++
            }
            'r' -> {
                builder.append('\r')
                i++
            }
            '"' -> {
                builder.append('\"')
                i++
            }
            '\\' -> {
                builder.append('\\')
                i++
            }
            'b' -> {
                builder.append('\b')
                i++
            }
            'u' -> {
                require(lastIndex >= i + 5) { "Unexpected end in $this" }
                val index = AsciiMapping.indexOf(substring(i, i + 6))
                if (index == -1) {
                    builder.append("\\u")
                    i++
                } else {
                    builder.append(index.toChar())
                    i += 5
                }
            }
            else -> error("Unknown escape $next")
        }
        i++
    }
    return builder.toString()
}

internal fun Number.toStringModified(): String {
    return when (this) {
        Double.POSITIVE_INFINITY, Float.POSITIVE_INFINITY -> "inf"
        Double.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY -> "-inf"
        Double.NaN, Float.NaN -> "nan"
        else -> toString()
    }
}

internal fun String.toNumber(
    positive: Boolean,
    radix: Int,
    isDouble: Boolean,
    isExponent: Boolean
): Number {
    return if (isDouble) {
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
            factor *= 10.0.pow(strings[1].toInt()).toLong()
            strings[0].toLong(radix) * factor
        } else {
            toLong(radix) * factor
        }
    }
}
