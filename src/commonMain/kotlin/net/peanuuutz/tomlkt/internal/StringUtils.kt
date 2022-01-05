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
    @OptIn(ExperimentalStdlibApi::class)
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
        set(S.DOUBLE_QUOTE.code, "\\\"")
        set(S.BACK_SLASH.code, "\\\\")
    }

    fun escape(char: Char, multiline: Boolean): String = if (char.toInt() >= 128) {
        char.toString()
    } else {
        if (!multiline)
            mappings[char.toInt()]
        else {
            if (char == S.BACK_SLASH || char != '\t' && char != '\r' && char != '\n')
                mappings[char.toInt()]
            else
                char.toString()
        }
    }

    fun unescape(string: String): String {
        val builder = StringBuilder()
        var i = -1
        while (++i < string.length) {
            if (string[i] != S.BACK_SLASH)
                builder.append(string[i])
            else {
                require(i != string.lastIndex)
                when (val c = string[i + 1]) {
                    'b', 't', 'n', 'f', 'r', S.DOUBLE_QUOTE, S.BACK_SLASH -> {
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
    const val SHARP = '#'
    const val EQUAL = '='
    const val OPEN_BRACKET = '['
    const val CLOSE_BRACKET = ']'
    const val OPEN_BRACE = '{'
    const val CLOSE_BRACE = '}'
    const val COMMA = ','
    const val DOT = '.'
    const val DOUBLE_QUOTE = '"'
    const val SINGLE_QUOTE = '\''
    const val POSITIVE = '+'
    const val NEGATIVE = '-'
    const val UNDERSCORE = '_'
    const val BACK_SLASH = '\\'
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
    } else toDouble() * factor
} else {
    var factor = if (positive) 1L else -1L
    if (isExponent) {
        val strings = split('e', ignoreCase = true)
        factor *= (10.0.pow(strings[1].toInt())).toLong()
        strings[0].toLong(radix) * factor
    } else toLong(radix) * factor
}