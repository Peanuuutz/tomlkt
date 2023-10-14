package net.peanuuutz.tomlkt

import net.peanuuutz.tomlkt.TomlInteger.Base
import net.peanuuutz.tomlkt.TomlInteger.Base.Dec
import net.peanuuutz.tomlkt.internal.Comment
import net.peanuuutz.tomlkt.internal.ElementSeparator
import net.peanuuutz.tomlkt.internal.EndArray
import net.peanuuutz.tomlkt.internal.EndInlineTable
import net.peanuuutz.tomlkt.internal.EndTableHead
import net.peanuuutz.tomlkt.internal.KeySeparator
import net.peanuuutz.tomlkt.internal.KeyValueSeparator
import net.peanuuutz.tomlkt.internal.StartArray
import net.peanuuutz.tomlkt.internal.StartInlineTable
import net.peanuuutz.tomlkt.internal.StartTableHead
import net.peanuuutz.tomlkt.internal.doubleQuoted
import net.peanuuutz.tomlkt.internal.doubleQuotedIfNotPure
import net.peanuuutz.tomlkt.internal.escape
import net.peanuuutz.tomlkt.internal.processIntegerString
import net.peanuuutz.tomlkt.internal.singleQuoted
import net.peanuuutz.tomlkt.internal.toStringModified

/**
 * The basic implementation of [TomlWriter], handling all the essential logic
 * except [writeString] and (optional) [writeChar].
 */
public abstract class AbstractTomlWriter : TomlWriter {
    // -------- Core --------

    // Better implementation is welcomed.
    override fun writeChar(char: Char) {
        writeString(char.toString())
    }

    // -------- Key --------

    final override fun writeKey(key: String) {
        writeString(key.escape().doubleQuotedIfNotPure())
    }

    final override fun writeKeySeparator() {
        writeChar(KeySeparator)
    }

    // -------- Table Head --------

    final override fun startRegularTableHead() {
        writeChar(StartTableHead)
    }

    final override fun endRegularTableHead() {
        writeChar(EndTableHead)
    }

    final override fun startArrayOfTableHead() {
        writeChar(StartTableHead)
        writeChar(StartTableHead)
    }

    final override fun endArrayOfTableHead() {
        writeChar(EndTableHead)
        writeChar(EndTableHead)
    }

    // -------- Value --------

    final override fun writeBooleanValue(boolean: Boolean) {
        writeString(boolean.toString())
    }

    final override fun writeIntegerValue(integer: Long, base: Base, group: Int, uppercase: Boolean) {
        require(group >= 0) { "Group size cannot be negative" }
        require(integer >= 0L || base == Dec) {
            "Negative integer cannot be represented by other bases, but found $integer"
        }
        val string = processIntegerString(
            raw = integer.toString(base.value),
            base = base,
            group = group,
            uppercase = uppercase
        )
        writeString(string)
    }

    @Deprecated(
        message = "Use writeFloatValue(Double) instead.",
        replaceWith = ReplaceWith("writeFloatValue")
    )
    override fun writeFloatValue(float: Float) {
        writeString(float.toStringModified())
    }

    final override fun writeFloatValue(float: Double) {
        writeString(float.toStringModified())
    }

    final override fun writeStringValue(string: String, isMultiline: Boolean, isLiteral: Boolean) {
        when {
            !isMultiline && !isLiteral -> {
                writeString(string.escape().doubleQuoted)
            }
            !isMultiline -> {
                require('\'' !in string && '\n' !in string) {
                    "Cannot have '\\'' or '\\n' in literal string, but found $string"
                }
                writeString(string.singleQuoted)
            }
            !isLiteral -> {
                writeString("\"\"\"")
                writeLineFeed()
                writeString(string.escape(multiline = true))
                writeString("\"\"\"")
            }
            else -> {
                require("\'\'\'" !in string) {
                    "Cannot have \"\\'\\'\\'\" in multiline literal string, but found $string"
                }
                writeString("\'\'\'")
                writeLineFeed()
                writeString(string)
                writeString("\'\'\'")
            }
        }
    }

    final override fun writeNullValue() {
        writeString("null")
    }

    // -------- Structure --------

    final override fun startArray() {
        writeChar(StartArray)
    }

    final override fun endArray() {
        writeChar(EndArray)
    }

    final override fun startInlineTable() {
        writeChar(StartInlineTable)
    }

    final override fun endInlineTable() {
        writeChar(EndInlineTable)
    }

    final override fun writeKeyValueSeparator() {
        writeChar(KeyValueSeparator)
    }

    final override fun writeElementSeparator() {
        writeChar(ElementSeparator)
    }

    // -------- Comment --------

    final override fun startComment() {
        writeChar(Comment)
    }

    // -------- Control --------

    final override fun writeSpace() {
        writeChar(' ')
    }

    final override fun writeIndentation(indentation: TomlIndentation) {
        writeString(indentation.representation)
    }

    final override fun writeLineFeed() {
        writeChar('\n')
    }
}
