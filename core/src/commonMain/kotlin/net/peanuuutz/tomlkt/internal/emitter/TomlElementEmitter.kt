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

package net.peanuuutz.tomlkt.internal.emitter

import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.TomlArray
import net.peanuuutz.tomlkt.TomlBlockArray
import net.peanuuutz.tomlkt.TomlComment
import net.peanuuutz.tomlkt.TomlElement
import net.peanuuutz.tomlkt.TomlInline
import net.peanuuutz.tomlkt.TomlInteger
import net.peanuuutz.tomlkt.TomlLiteral
import net.peanuuutz.tomlkt.TomlLiteral.Type
import net.peanuuutz.tomlkt.TomlLiteralString
import net.peanuuutz.tomlkt.TomlMultilineString
import net.peanuuutz.tomlkt.TomlNull
import net.peanuuutz.tomlkt.TomlTable
import net.peanuuutz.tomlkt.TomlWriter
import net.peanuuutz.tomlkt.internal.Path
import net.peanuuutz.tomlkt.internal.escape
import net.peanuuutz.tomlkt.internal.processIntegerString
import net.peanuuutz.tomlkt.toBoolean
import net.peanuuutz.tomlkt.toDouble
import net.peanuuutz.tomlkt.toLongOrNull

// -------- AbstractTomlElementEmitter --------

internal abstract class AbstractTomlElementEmitter(
    val toml: Toml,
    val writer: TomlWriter
) {
    var isInline: Boolean = false

    var blockArray: TomlBlockArray? = null

    var isStringMultiline: Boolean = false

    var isStringLiteral: Boolean = false

    var integerRepresentation: TomlInteger? = null

    // -------- Element --------

    fun emitElement(element: TomlElement) {
        when (element) {
            is TomlNull -> {
                emitNull()
            }
            is TomlLiteral -> {
                emitLiteral(element)
            }
            is TomlArray -> {
                createArrayEmitter(element).emitArray(element)
            }
            is TomlTable -> {
                createTableEmitter(element).emitTable(element)
            }
        }
    }

    // -------- Null --------

    open fun emitNull() {
        writer.writeNullValue()
    }

    // -------- Literal --------

    fun emitLiteral(literal: TomlLiteral) {
        when (literal.type) {
            Type.Boolean -> {
                emitBoolean(literal.toBoolean())
            }
            Type.Integer -> {
                val long = literal.toLongOrNull()
                if (long == null) {
                    // This is a ULong.
                    emitULong(literal.content)
                    return
                }
                emitInteger(long)
            }
            Type.Float -> {
                emitFloat(literal.toDouble())
            }
            Type.String -> {
                emitString(literal.toString())
            }
            Type.LocalDateTime, Type.OffsetDateTime, Type.LocalDate, Type.LocalTime -> {
                emitDateTime(literal.content)
            }
        }
    }

    open fun emitBoolean(boolean: Boolean) {
        writer.writeBooleanValue(boolean)
    }

    open fun emitInteger(integer: Long) {
        val representation = integerRepresentation
        if (representation == null) {
            writer.writeIntegerValue(integer)
            return
        }
        writer.writeIntegerValue(
            integer = integer,
            base = representation.base,
            group = representation.group,
            uppercase = toml.config.uppercaseInteger
        )
    }

    // We handle ULong separately because it's not covered in the spec.
    open fun emitULong(content: String) {
        val representation = integerRepresentation
        if (representation == null) {
            writer.writeString(content)
            return
        }
        require(representation.group >= 0) { "Group size cannot be negative" }
        val string = processIntegerString(
            raw = content,
            base = representation.base,
            group = representation.group,
            uppercase = toml.config.uppercaseInteger
        )
        writer.writeString(string)
    }

    open fun emitFloat(float: Double) {
        writer.writeFloatValue(float)
    }

    open fun emitString(string: String) {
        writer.writeStringValue(
            string = string,
            isMultiline = isStringMultiline,
            isLiteral = isStringLiteral
        )
    }

    open fun emitDateTime(content: String) {
        writer.writeString(content)
    }

    // -------- Array --------

    open fun createArrayEmitter(array: TomlArray): AbstractTomlElementEmitter {
        return TomlInlineArrayEmitter(this)
    }

    open fun emitArray(array: TomlArray) {
        createArrayEmitter(array).emitArray(array)
    }

    // -------- Table --------

    open fun createTableEmitter(table: TomlTable): AbstractTomlElementEmitter {
        return TomlInlineTableEmitter(this)
    }

    open fun emitTable(table: TomlTable) {
        createTableEmitter(table).emitTable(table)
    }
}

// -------- TomlElementEmitter --------

// This is the entry point.
internal class TomlElementEmitter(
    toml: Toml,
    writer: TomlWriter
) : AbstractTomlElementEmitter(toml, writer) {
    override fun createArrayEmitter(array: TomlArray): AbstractTomlElementEmitter {
        return if (array.isNotEmpty()) {
            TomlBlockArrayEmitter(this, toml.config.itemsPerLineInBlockArray)
        } else {
            TomlInlineArrayEmitter(this)
        }
    }

    override fun createTableEmitter(table: TomlTable): AbstractTomlElementEmitter {
        return TomlTableEmitter(this, emptyList())
    }
}

// -------- TomlInlineTableEmitter --------

private class TomlInlineTableEmitter(
    delegate: AbstractTomlElementEmitter
) : AbstractTomlElementEmitter(delegate.toml, delegate.writer) {
    override fun emitTable(table: TomlTable) {
        // Begin structure.
        writer.startInlineTable()
        writer.writeSpace()
        // Emit structure.
        val explicitNulls = toml.config.explicitNulls
        val annotations = table.annotations
        val lastIndex = table.size - 1
        table.entries.forEachIndexed loop@{ index, (key, element) ->
            if (element is TomlNull && !explicitNulls) {
                return@loop
            }
            // Begin element.
            val elementAnnotations = annotations[key]
            if (elementAnnotations != null) {
                processAnnotations(elementAnnotations)
            }
            // Emit entry.
            writer.startEntry(key)
            emitElement(element)
            // End element.
            isStringLiteral = false
            integerRepresentation = null
            if (index < lastIndex) {
                writer.writeElementSeparator()
                writer.writeSpace()
            }
        }
        // End structure.
        writer.writeSpace()
        writer.endInlineTable()
    }

    private fun processAnnotations(annotations: List<Annotation>) {
        for (annotation in annotations) {
            when (annotation) {
                is TomlLiteralString -> {
                    isStringLiteral = true
                }
                is TomlInteger -> {
                    integerRepresentation = annotation
                }
            }
        }
    }
}

// -------- TomlInlineArrayEmitter --------

private class TomlInlineArrayEmitter(
    delegate: AbstractTomlElementEmitter
) : AbstractTomlElementEmitter(delegate.toml, delegate.writer) {
    override fun emitArray(array: TomlArray) {
        // Begin structure.
        writer.startArray()
        writer.writeSpace()
        // Emit structure.
        val explicitNulls = toml.config.explicitNulls
        val annotations = array.annotations
        val lastIndex = array.size - 1
        array.forEachIndexed loop@{ index, element ->
            if (element is TomlNull && !explicitNulls) {
                return@loop
            }
            // Begin element.
            val elementAnnotations = annotations.getOrNull(index)
            if (elementAnnotations != null) {
                processAnnotations(elementAnnotations)
            }
            // Emit value.
            emitElement(element)
            // End element.
            isStringLiteral = false
            integerRepresentation = null
            if (index < lastIndex) {
                writer.writeElementSeparator()
                writer.writeSpace()
            }
        }
        // End structure.
        writer.writeSpace()
        writer.endArray()
    }

    private fun processAnnotations(annotations: List<Annotation>) {
        for (annotation in annotations) {
            when (annotation) {
                is TomlLiteralString -> {
                    isStringLiteral = true
                }
                is TomlInteger -> {
                    integerRepresentation = annotation
                }
            }
        }
    }
}

// -------- TomlBlockArrayEmitter --------

private class TomlBlockArrayEmitter(
    delegate: AbstractTomlElementEmitter,
    private val itemsPerLine: Int
) : AbstractTomlElementEmitter(delegate.toml, delegate.writer) {
    override fun emitArray(array: TomlArray) {
        // Begin structure.
        writer.startArray()
        writer.writeLineFeed()
        // Emit structure.
        val explicitNulls = toml.config.explicitNulls
        val indentation = toml.config.indentation
        val itemsPerLine = itemsPerLine
        val annotations = array.annotations
        val lastIndex = array.size - 1
        var currentLineItemCount = 0
        array.forEachIndexed loop@{ index, element ->
            if (element is TomlNull && !explicitNulls) {
                return@loop
            }
            // Begin element.
            val elementAnnotations = annotations.getOrNull(index)
            if (elementAnnotations != null) {
                processAnnotations(elementAnnotations)
            }
            if (currentLineItemCount == 0) {
                // Initial indentation per line.
                writer.writeIndentation(indentation)
            } else {
                writer.writeSpace()
            }
            // Emit value.
            emitElement(element)
            // End element.
            isStringLiteral = false
            integerRepresentation = null
            if (index < lastIndex) {
                writer.writeElementSeparator()
            }
            currentLineItemCount++
            if (currentLineItemCount >= itemsPerLine) {
                writer.writeLineFeed()
                currentLineItemCount = 0
            }
        }
        // End structure.
        if (currentLineItemCount != 0) {
            writer.writeLineFeed()
        }
        writer.endArray()
    }

    private fun processAnnotations(annotations: List<Annotation>) {
        var comment: TomlComment? = null
        for (annotation in annotations) {
            when (annotation) {
                is TomlComment -> {
                    comment = annotation
                }
                is TomlLiteralString -> {
                    isStringLiteral = true
                }
                is TomlInteger -> {
                    integerRepresentation = annotation
                }
            }
        }
        // Emit comment only if there's only one item per line.
        if (comment != null && itemsPerLine == 1) {
            emitComment(comment)
        }
    }

    private fun emitComment(comment: TomlComment) {
        val indentation = toml.config.indentation
        val lines = comment.text.trimIndent().split('\n').map(String::escape)
        for (line in lines) {
            writer.writeIndentation(indentation)
            writer.startComment()
            writer.writeSpace()
            writer.writeString(line)
            writer.writeLineFeed()
        }
    }
}

// -------- TomlTableEmitter --------

private class TomlTableEmitter(
    delegate: AbstractTomlElementEmitter,
    private val path: Path
) : AbstractTomlElementEmitter(delegate.toml, delegate.writer) {
    // We delay the emission of comments.
    private var comment: TomlComment? = null

    // Array of table is handled separately.
    override fun createArrayEmitter(array: TomlArray): AbstractTomlElementEmitter {
        return if (!isInline && array.isNotEmpty()) {
            val itemsPerLine = blockArray?.itemsPerLine ?: toml.config.itemsPerLineInBlockArray
            TomlBlockArrayEmitter(this, itemsPerLine)
        } else {
            TomlInlineArrayEmitter(this)
        }
    }

    // Table is handled separately.
    override fun createTableEmitter(table: TomlTable): AbstractTomlElementEmitter {
        return TomlInlineTableEmitter(this)
    }

    override fun emitTable(table: TomlTable) {
        // Emit structure.
        val explicitNulls = toml.config.explicitNulls
        val annotations = table.annotations
        val remainingComments = mutableMapOf<String, TomlComment>()
        val tables = mutableMapOf<String, TomlTable>()
        val arrayOfTables = mutableMapOf<String, TomlArray>()
        var hasNormalEntry = false
        // Emit all the normal key-value entries, record tables and array of tables.
        table.entries.forEachIndexed loop@{ _, (key, element) ->
            if (element is TomlNull && !explicitNulls) {
                return@loop
            }
            // Begin element.
            val elementAnnotations = annotations[key]
            if (elementAnnotations != null) {
                processAnnotations(elementAnnotations)
            }
            // Emit entry.
            val emitted = tryEmitEntry(
                key = key,
                element = element,
                remainingComments = remainingComments,
                tables = tables,
                arrayOfTables = arrayOfTables,
                hasNormalEntry = hasNormalEntry
            )
            // End element.
            comment = null
            isInline = false
            blockArray = null
            isStringMultiline = false
            isStringLiteral = false
            integerRepresentation = null
            if (!hasNormalEntry) {
                hasNormalEntry = emitted
            }
        }
        // Emit tables.
        if (tables.isNotEmpty()) {
            if (hasNormalEntry) {
                writer.writeLineFeed()
            }
            emitInnerTables(remainingComments, tables)
        }
        // Emit array of tables.
        if (arrayOfTables.isNotEmpty()) {
            if (hasNormalEntry || tables.isNotEmpty()) {
                writer.writeLineFeed()
            }
            emitInnerArrayOfTables(remainingComments, arrayOfTables)
        }
    }

    private fun processAnnotations(annotations: List<Annotation>) {
        for (annotation in annotations) {
            when (annotation) {
                is TomlComment -> {
                    comment = annotation
                }
                is TomlInline -> {
                    isInline = true
                }
                is TomlBlockArray -> {
                    blockArray = annotation
                }
                is TomlMultilineString -> {
                    isStringMultiline = true
                }
                is TomlLiteralString -> {
                    isStringLiteral = true
                }
                is TomlInteger -> {
                    integerRepresentation = annotation
                }
            }
        }
    }

    private fun emitComment(comment: TomlComment) {
        val lines = comment.text.trimIndent().split('\n').map(String::escape)
        for (line in lines) {
            writer.startComment()
            writer.writeSpace()
            writer.writeString(line)
            writer.writeLineFeed()
        }
    }

    private fun tryEmitEntry(
        key: String,
        element: TomlElement,
        remainingComments: MutableMap<String, TomlComment>,
        tables: MutableMap<String, TomlTable>,
        arrayOfTables: MutableMap<String, TomlArray>,
        hasNormalEntry: Boolean
    ): Boolean {
        val comment = comment
        // Record tables and array of tables.
        when (element) {
            is TomlArray -> {
                val shouldStructure = !isInline &&
                        blockArray == null &&
                        element.isNotEmpty() &&
                        element.all { it is TomlTable } &&
                        element.annotations.flatten().none { it is TomlInline }
                if (shouldStructure) {
                    arrayOfTables[key] = element
                    if (comment != null) {
                        remainingComments[key] = comment
                    }
                    return false
                }
            }
            is TomlTable -> {
                val shouldStructure = !isInline && element.isNotEmpty()
                if (shouldStructure) {
                    tables[key] = element
                    if (comment != null) {
                        remainingComments[key] = comment
                    }
                    return false
                }
            }
            else -> {}
        }
        // Emit normal key-value entries.
        if (hasNormalEntry) {
            writer.writeLineFeed()
        }
        if (comment != null) {
            emitComment(comment)
        }
        writer.startEntry(key)
        emitElement(element)
        return true
    }

    private fun emitInnerTables(
        remainingComments: Map<String, TomlComment>,
        tables: Map<String, TomlTable>
    ) {
        val path = path
        val lastIndex = tables.size - 1
        tables.entries.forEachIndexed { index, (key, table) ->
            // Begin element.
            val comment = remainingComments[key]
            if (comment != null) {
                writer.writeLineFeed()
                emitComment(comment)
            }
            // Emit entry.
            val innerPath = path + key
            writer.writeLineFeed()
            writer.writeRegularTableHead(innerPath)
            writer.writeLineFeed()
            TomlTableEmitter(this, innerPath).emitTable(table)
            // End element.
            if (index < lastIndex) {
                writer.writeLineFeed()
            }
        }
    }

    private fun emitInnerArrayOfTables(
        remainingComments: Map<String, TomlComment>,
        arrayOfTables: Map<String, TomlArray>
    ) {
        val path = path
        val lastIndex = arrayOfTables.size - 1
        arrayOfTables.entries.forEachIndexed { index, (key, array) ->
            // Begin element.
            val comment = remainingComments[key]
            if (comment != null) {
                writer.writeLineFeed()
                emitComment(comment)
            }
            // Emit entry (no key needed).
            val innerPath = path + key
            TomlArrayOfTableEmitter(this, innerPath).emitArray(array)
            // End element.
            if (index < lastIndex) {
                writer.writeLineFeed()
            }
        }
    }
}

// -------- TomlArrayOfTableEmitter --------

private class TomlArrayOfTableEmitter(
    delegate: AbstractTomlElementEmitter,
    private val path: Path
) : AbstractTomlElementEmitter(delegate.toml, delegate.writer) {
    override fun createTableEmitter(table: TomlTable): AbstractTomlElementEmitter {
        return TomlTableEmitter(this, path)
    }

    override fun emitArray(array: TomlArray) {
        val path = path
        val annotations = array.annotations
        val lastIndex = array.size - 1
        array.forEachIndexed { index, table ->
            table as TomlTable
            // Begin element.
            val elementAnnotations = annotations.getOrNull(index)
            if (elementAnnotations != null) {
                processAnnotations(elementAnnotations)
            }
            writer.writeLineFeed()
            writer.writeArrayOfTableHead(path)
            writer.writeLineFeed()
            // Emit value.
            createTableEmitter(table).emitTable(table)
            if (index < lastIndex) {
                writer.writeLineFeed()
            }
        }
    }

    private fun processAnnotations(annotations: List<Annotation>) {
        var comment: TomlComment? = null
        for (annotation in annotations) {
            when (annotation) {
                is TomlComment -> {
                    comment = annotation
                }
            }
        }
        if (comment != null) {
            writer.writeLineFeed()
            emitComment(comment)
        }
    }

    private fun emitComment(comment: TomlComment) {
        val lines = comment.text.trimIndent().split('\n').map(String::escape)
        for (line in lines) {
            writer.startComment()
            writer.writeSpace()
            writer.writeString(line)
            writer.writeLineFeed()
        }
    }
}

// -------- Utils --------

private fun TomlWriter.startEntry(key: String) {
    writeKey(key)
    writeSpace()
    writeKeyValueSeparator()
    writeSpace()
}

private fun TomlWriter.writeRegularTableHead(path: Path) {
    startRegularTableHead()
    writePath(path)
    endRegularTableHead()
}

private fun TomlWriter.writeArrayOfTableHead(path: Path) {
    startArrayOfTableHead()
    writePath(path)
    endArrayOfTableHead()
}

private fun TomlWriter.writePath(path: Path) {
    val lastIndex = path.lastIndex
    for (i in 0..<lastIndex) {
        writeKey(path[i])
        writeKeySeparator()
    }
    writeKey(path[lastIndex])
}
