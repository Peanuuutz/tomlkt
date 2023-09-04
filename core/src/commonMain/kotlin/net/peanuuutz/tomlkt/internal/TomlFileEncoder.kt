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

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind.CONTEXTUAL
import kotlinx.serialization.descriptors.StructureKind.CLASS
import kotlinx.serialization.descriptors.StructureKind.LIST
import kotlinx.serialization.descriptors.StructureKind.MAP
import kotlinx.serialization.descriptors.StructureKind.OBJECT
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import net.peanuuutz.tomlkt.TomlArray
import net.peanuuutz.tomlkt.TomlBlockArray
import net.peanuuutz.tomlkt.TomlComment
import net.peanuuutz.tomlkt.TomlConfig
import net.peanuuutz.tomlkt.TomlElement
import net.peanuuutz.tomlkt.TomlEncoder
import net.peanuuutz.tomlkt.TomlInline
import net.peanuuutz.tomlkt.TomlInteger
import net.peanuuutz.tomlkt.TomlLiteral
import net.peanuuutz.tomlkt.TomlLiteralString
import net.peanuuutz.tomlkt.TomlMultilineString
import net.peanuuutz.tomlkt.TomlNull
import net.peanuuutz.tomlkt.TomlSpecific
import net.peanuuutz.tomlkt.TomlTable
import net.peanuuutz.tomlkt.TomlWriter
import net.peanuuutz.tomlkt.toTomlKey

@OptIn(TomlSpecific::class)
internal class TomlFileEncoder(
    private val config: TomlConfig,
    override val serializersModule: SerializersModule,
    private val writer: TomlWriter
) : Encoder, TomlEncoder {
    override fun encodeBoolean(value: Boolean) {
        writer.writeBoolean(value)
    }

    override fun encodeByte(value: Byte) {
        writer.writeByte(value)
    }

    override fun encodeShort(value: Short) {
        writer.writeShort(value)
    }

    override fun encodeInt(value: Int) {
        writer.writeInt(value)
    }

    override fun encodeLong(value: Long) {
        writer.writeLong(value)
    }

    override fun encodeFloat(value: Float) {
        writer.writeFloat(value)
    }

    override fun encodeDouble(value: Double) {
        writer.writeDouble(value)
    }

    override fun encodeChar(value: Char) {
        writer.writeString(value.escape().doubleQuoted)
    }

    override fun encodeString(value: String) {
        writer.writeString(value.escape().doubleQuoted)
    }

    override fun encodeNull() {
        writer.writeNull()
    }

    override fun encodeTomlElement(value: TomlElement) {
        when (value) {
            TomlNull -> writer.writeNull()
            is TomlLiteral -> writer.writeString(value.toString())
            is TomlArray -> TomlArray.serializer().serialize(this, value)
            is TomlTable -> TomlTable.serializer().serialize(this, value)
        }
    }

    override fun encodeInline(descriptor: SerialDescriptor): Encoder {
        return this
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        encodeString(enumDescriptor.getElementName(index))
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        return beginStructure(descriptor, forceInline = false)
    }

    private fun beginStructure(
        descriptor: SerialDescriptor,
        forceInline: Boolean
    ): CompositeEncoder {
        return when (val kind = descriptor.kind) {
            CLASS -> {
                if (forceInline) {
                    FlowClassEncoder()
                } else {
                    ClassEncoder(
                        structuredIndex = calculateStructuredIndex(descriptor),
                        structured = true,
                        path = ""
                    )
                }
            }
            OBJECT -> FlowClassEncoder()
            else -> throw UnsupportedSerialKindException(kind)
        }
    }

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        return beginCollection(descriptor, collectionSize, forceInline = false)
    }

    private fun beginCollection(
        descriptor: SerialDescriptor,
        collectionSize: Int,
        forceInline: Boolean
    ): CompositeEncoder {
        return when (val kind = descriptor.kind) {
            LIST -> {
                if (forceInline || collectionSize == 0) {
                    FlowArrayEncoder(collectionSize)
                } else {
                    BlockArrayEncoder(collectionSize, config.itemsPerLineInBlockArray)
                }
            }
            MAP -> {
                if (forceInline || collectionSize == 0) {
                    FlowMapEncoder(collectionSize)
                } else {
                    MapEncoder(
                        collectionSize = collectionSize,
                        valueDescriptor = descriptor.getElementDescriptor(1),
                        structured = true,
                        path = ""
                    )
                }
            }
            else -> throw UnsupportedSerialKindException(kind)
        }
    }

    internal abstract inner class AbstractEncoder : Encoder, CompositeEncoder, TomlEncoder {
        final override val serializersModule: SerializersModule
            get() = this@TomlFileEncoder.serializersModule

        final override fun encodeBoolean(value: Boolean) {
            this@TomlFileEncoder.encodeBoolean(value)
        }

        final override fun encodeByte(value: Byte) {
            this@TomlFileEncoder.encodeByte(value)
        }

        private fun encodeByteWithBase(value: Byte, base: TomlInteger.Base) {
            require(value > 0) {
                "Negative integer cannot be represented by other bases"
            }
            writer.writeString(base.prefix)
            writer.writeString(value.toString(base.value))
        }

        final override fun encodeShort(value: Short) {
            this@TomlFileEncoder.encodeShort(value)
        }

        private fun encodeShortWithBase(value: Short, base: TomlInteger.Base) {
            require(value > 0) {
                "Negative integer cannot be represented by other bases"
            }
            writer.writeString(base.prefix)
            writer.writeString(value.toString(base.value))
        }

        final override fun encodeInt(value: Int) {
            this@TomlFileEncoder.encodeInt(value)
        }

        private fun encodeIntWithBase(value: Int, base: TomlInteger.Base) {
            require(value > 0) {
                "Negative integer cannot be represented by other bases"
            }
            writer.writeString(base.prefix)
            writer.writeString(value.toString(base.value))
        }

        final override fun encodeLong(value: Long) {
            this@TomlFileEncoder.encodeLong(value)
        }

        private fun encodeLongWithBase(value: Long, base: TomlInteger.Base) {
            require(value > 0) {
                "Negative integer cannot be represented by other bases"
            }
            writer.writeString(base.prefix)
            writer.writeString(value.toString(base.value))
        }

        final override fun encodeFloat(value: Float) {
            this@TomlFileEncoder.encodeFloat(value)
        }

        final override fun encodeDouble(value: Double) {
            this@TomlFileEncoder.encodeDouble(value)
        }

        final override fun encodeChar(value: Char) {
            this@TomlFileEncoder.encodeChar(value)
        }

        final override fun encodeString(value: String) {
            this@TomlFileEncoder.encodeString(value)
        }

        private fun encodeMultilineString(value: String) {
            writer.writeString("\"\"\"")
            writer.writeLineFeed()
            writer.writeString(value.escape(multiline = true))
            writer.writeString("\"\"\"")
        }

        private fun encodeLiteralString(value: String) {
            require('\'' !in value && '\n' !in value) {
                "Cannot have '\\'' or '\\n' in literal string"
            }
            writer.writeString(value.singleQuoted)
        }

        private fun encodeMultilineLiteralString(value: String) {
            require("'''" !in value) {
                "Cannot have \"\\'\\'\\'\" in multiline literal string"
            }
            writer.writeString("'''")
            writer.writeLineFeed()
            writer.writeString(value)
            writer.writeString("'''")
        }

        final override fun encodeNull() {
            this@TomlFileEncoder.encodeNull()
        }

        final override fun encodeTomlElement(value: TomlElement) {
            this@TomlFileEncoder.encodeTomlElement(value)
        }

        final override fun encodeInline(descriptor: SerialDescriptor): Encoder {
            return this
        }

        final override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
            this@TomlFileEncoder.encodeEnum(enumDescriptor, index)
        }

        override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
            return beginStructure(descriptor, forceInline = true)
        }

        override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
            return beginCollection(descriptor, collectionSize, forceInline = true)
        }

        final override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
            encodeSerializableElement(descriptor, index, Boolean.serializer(), value)
        }

        final override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
            encodeTomlIntegerElement(descriptor, index, value, this::encodeByte, this::encodeByteWithBase)
        }

        final override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
            encodeTomlIntegerElement(descriptor, index, value, this::encodeShort, this::encodeShortWithBase)
        }

        final override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
            encodeTomlIntegerElement(descriptor, index, value, this::encodeInt, this::encodeIntWithBase)
        }

        final override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
            encodeTomlIntegerElement(descriptor, index, value, this::encodeLong, this::encodeLongWithBase)
        }

        private inline fun <T> encodeTomlIntegerElement(
            descriptor: SerialDescriptor,
            index: Int,
            value: T,
            encodeNormally: (T) -> Unit,
            encodeWithBase: (T, TomlInteger.Base) -> Unit
        ) {
            head(descriptor, index)
            val annotations = descriptor.getElementAnnotations(index).filterIsInstance<TomlInteger>()
            if (annotations.isEmpty()) {
                encodeNormally(value)
            } else {
                encodeWithBase(value, annotations[0].base)
            }
            tail(descriptor, index)
        }

        final override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
            encodeSerializableElement(descriptor, index, Float.serializer(), value)
        }

        final override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
            encodeSerializableElement(descriptor, index, Double.serializer(), value)
        }

        final override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
            encodeSerializableElement(descriptor, index, Char.serializer(), value)
        }

        final override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
            head(descriptor, index)
            val annotations = descriptor.getElementAnnotations(index)
            val isLiteral = annotations.hasAnnotation<TomlLiteralString>()
            val isMultiline = annotations.hasAnnotation<TomlMultilineString>()
            when {
                !isLiteral && !isMultiline -> encodeString(value)
                !isLiteral -> encodeMultilineString(value)
                !isMultiline -> encodeLiteralString(value)
                else -> encodeMultilineLiteralString(value)
            }
            tail(descriptor, index)
        }

        final override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
            TODO("Not yet implemented")
        }

        final override fun <T : Any> encodeNullableSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T?
        ) {
            when (value) { // Revisit type-specific encoding.
                null -> {
                    encodeSerializableElement(descriptor, index, TomlNull.serializer(), TomlNull)
                }
                is Byte -> {
                    encodeByteElement(descriptor, index, value)
                }
                is Short -> {
                    encodeShortElement(descriptor, index, value)
                }
                is Int -> {
                    encodeIntElement(descriptor, index, value)
                }
                is Long -> {
                    encodeLongElement(descriptor, index, value)
                }
                is String -> {
                    encodeStringElement(descriptor, index, value)
                }
                else -> {
                    encodeSerializableElement(descriptor, index, serializer, value)
                }
            }
        }

        override fun <T> encodeSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T
        ) {
            head(descriptor, index)
            serializer.serialize(this, value)
            tail(descriptor, index)
        }

        protected abstract fun head(descriptor: SerialDescriptor, index: Int)

        protected abstract fun tail(descriptor: SerialDescriptor, index: Int)
    }

    internal inner class BlockArrayEncoder(
        private val collectionSize: Int,
        private val itemsPerLine: Int
    ) : AbstractEncoder() {
        private var itemsInCurrentLine: Int = 0

        init {
            writer.writeChar('[')
            writer.writeLineFeed()
        }

        override fun head(descriptor: SerialDescriptor, index: Int) {
            if (itemsInCurrentLine == 0) {
                writer.writeString(config.indentation.representation)
            } else {
                writer.writeChar(' ')
            }
        }

        override fun tail(descriptor: SerialDescriptor, index: Int) {
            if (index != collectionSize - 1) {
                writer.writeChar(',')
            }
            if (++itemsInCurrentLine >= itemsPerLine) {
                writer.writeLineFeed()
                itemsInCurrentLine = 0
            }
        }

        override fun endStructure(descriptor: SerialDescriptor) {
            if (itemsInCurrentLine != 0) {
                writer.writeLineFeed()
            }
            writer.writeChar(']')
        }
    }

    internal inner class FlowArrayEncoder(
        private val collectionSize: Int
    ) : AbstractEncoder() {
        init { writer.writeString("[ ") }

        override fun head(descriptor: SerialDescriptor, index: Int) {}

        override fun tail(descriptor: SerialDescriptor, index: Int) {
            if (index != collectionSize - 1) {
                writer.writeString(", ")
            }
        }

        override fun endStructure(descriptor: SerialDescriptor) {
            writer.writeString(" ]")
        }
    }

    internal inner class FlowClassEncoder : AbstractEncoder() {
        init { writer.writeString("{ ") }

        override fun head(descriptor: SerialDescriptor, index: Int) {
            val key = descriptor.getElementName(index)
                .escape()
                .doubleQuotedIfNotPure()
            writer.writeKey(key)
        }

        override fun tail(descriptor: SerialDescriptor, index: Int) {
            if (index != descriptor.elementsCount - 1) {
                writer.writeString(", ")
            }
        }

        override fun endStructure(descriptor: SerialDescriptor) {
            writer.writeString(" }")
        }
    }

    internal inner class FlowMapEncoder(
        private val collectionSize: Int
    ) : AbstractEncoder() {
        private var isKey: Boolean = true

        init { writer.writeString("{ ") }

        override fun head(descriptor: SerialDescriptor, index: Int) {}

        override fun <T> encodeSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T
        ) {
            if (isKey) {
                val key = value.toTomlKey()
                    .escape()
                    .doubleQuotedIfNotPure()
                writer.writeKey(key)
            } else {
                serializer.serialize(this, value)
            }
            tail(descriptor, index)
        }

        override fun tail(descriptor: SerialDescriptor, index: Int) {
            if (!isKey && index != collectionSize * 2 - 1) {
                writer.writeString(", ")
            }
            isKey = !isKey
        }

        override fun endStructure(descriptor: SerialDescriptor) {
            writer.writeString(" }")
        }
    }

    internal abstract inner class TableLikeEncoder(
        protected val structured: Boolean,
        private val path: String
    ) : AbstractEncoder() {
        protected var inlineChild: Boolean = false

        protected lateinit var currentChildPath: String

        protected var structuredChild: Boolean = false

        override fun <T> encodeSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T
        ) {
            inlineChild = value.isNullLike
            super.encodeSerializableElement(descriptor, index, serializer, value)
        }

        override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
            return when (val kind = descriptor.kind) {
                CLASS -> {
                    if (inlineChild) {
                        FlowClassEncoder()
                    } else {
                        val childPath = if (structured && !structuredChild) {
                            currentChildPath.removePrefix("$path.")
                        } else {
                            currentChildPath
                        }
                        ClassEncoder(
                            structuredIndex = calculateStructuredIndex(descriptor),
                            structured = structuredChild,
                            path = childPath
                        )
                    }
                }
                OBJECT -> FlowClassEncoder()
                else -> throw UnsupportedSerialKindException(kind)
            }
        }

        override fun beginCollection(
            descriptor: SerialDescriptor,
            collectionSize: Int
        ): CompositeEncoder {
            return when (val kind = descriptor.kind) {
                LIST -> {
                    when {
                        inlineChild || collectionSize == 0 -> FlowArrayEncoder(collectionSize)
                        structuredChild && descriptor.isArrayOfTables && blockArrayAnnotation == null -> {
                            ArrayOfTableEncoder(
                                collectionSize = collectionSize,
                                path = currentChildPath
                            )
                        }
                        else -> {
                            val itemsPerLine = blockArrayAnnotation?.itemsPerLine
                                ?: config.itemsPerLineInBlockArray
                            BlockArrayEncoder(
                                collectionSize = collectionSize,
                                itemsPerLine = itemsPerLine
                            )
                        }
                    }
                }
                MAP -> {
                    if (inlineChild || collectionSize == 0) {
                        FlowMapEncoder(collectionSize)
                    } else {
                        MapEncoder(
                            collectionSize = collectionSize,
                            valueDescriptor = descriptor.getElementDescriptor(1),
                            structured = structuredChild,
                            path = currentChildPath
                        )
                    }
                }
                else -> throw UnsupportedSerialKindException(kind)
            }
        }

        protected open val blockArrayAnnotation: TomlBlockArray?
            get() = null

        protected fun concatPath(childPath: String): String {
            return if (path.isNotEmpty()) "$path.$childPath" else childPath
        }

        override fun endStructure(descriptor: SerialDescriptor) {}
    }

    internal inner class ClassEncoder(
        private val structuredIndex: Int,
        structured: Boolean,
        path: String
    ) : TableLikeEncoder(structured, path) {
        override var blockArrayAnnotation: TomlBlockArray? = null

        override fun head(descriptor: SerialDescriptor, index: Int) {
            val elementName = descriptor.getElementName(index)
                .escape()
                .doubleQuotedIfNotPure()
            inlineChild = inlineChild || descriptor.forceInlineAt(index)
            currentChildPath = concatPath(elementName)
            structuredChild = structured && index >= structuredIndex
            val key = if (structured) elementName else currentChildPath
            val elementDescriptor = descriptor.getElementDescriptor(index)
            comment(descriptor, index)
            when {
                inlineChild -> {
                    writer.writeKey(key)
                }
                structuredChild -> {
                    if (elementDescriptor.isTable) {
                        writer.writeLineFeed()
                        writer.writeString("[$currentChildPath]")
                        writer.writeLineFeed()
                    } else if (elementDescriptor.isArrayOfTables) {
                        val annotations = descriptor.getElementAnnotations(index)
                            .filterIsInstance<TomlBlockArray>()
                        if (annotations.isNotEmpty()) {
                            writer.writeKey(key)
                            blockArrayAnnotation = annotations[0]
                        }
                    }
                }
                elementDescriptor.isTable.not() -> {
                    writer.writeKey(key)
                }
            }
        }

        private fun comment(descriptor: SerialDescriptor, index: Int) {
            if (!structured) {
                return
            }
            val lines = mutableListOf<String>()
            val annotations = descriptor.getElementAnnotations(index)
            var forceInline = !structuredChild
            for (annotation in annotations) {
                when (annotation) {
                    is TomlInline -> forceInline = true
                    is TomlComment -> {
                        annotation.text
                            .trimIndent()
                            .split('\n')
                            .map(String::escape)
                            .forEach(lines::add)
                    }
                }
            }
            if (lines.size > 0) {
                val elementDescriptor = descriptor.getElementDescriptor(index)
                if (elementDescriptor.isTableLike && !forceInline) {
                    writer.writeLineFeed()
                }
                for (line in lines) {
                    writer.writeString("# $line")
                    writer.writeLineFeed()
                }
            }
        }

        override fun tail(descriptor: SerialDescriptor, index: Int) {
            if (index != descriptor.elementsCount - 1) {
                writer.writeLineFeed()
            }
            blockArrayAnnotation = null
        }
    }

    internal inner class ArrayOfTableEncoder(
        private val collectionSize: Int,
        path: String
    ) : TableLikeEncoder(true, path) {
        init {
            currentChildPath = path
            structuredChild = structured
        }

        override fun <T> encodeSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T
        ) {
            if (value.isNullLike) {
                throw NullInArrayOfTableException()
            }
            super.encodeSerializableElement(descriptor, index, serializer, value)
        }

        override fun head(descriptor: SerialDescriptor, index: Int) {
            writer.writeLineFeed()
            writer.writeString("[[$currentChildPath]]")
            writer.writeLineFeed()
        }

        override fun tail(descriptor: SerialDescriptor, index: Int) {
            if (index != collectionSize - 1) {
                writer.writeLineFeed()
            }
        }
    }

    internal inner class MapEncoder(
        private val collectionSize: Int,
        private val valueDescriptor: SerialDescriptor,
        structured: Boolean,
        path: String
    ) : TableLikeEncoder(structured, path) {
        private var currentElementIndex: Int = 0

        private val isKey: Boolean
            get() = currentElementIndex % 2 == 0

        private lateinit var key: String

        init {
            inlineChild = valueDescriptor.kind == CONTEXTUAL || valueDescriptor.isExactlyTomlTable
            structuredChild = structured
        }

        override fun <T> encodeSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T
        ) {
            if (isKey) {
                storeKey(value)
            } else {
                val valueKind = valueDescriptor.kind
                if (value.isNullLike) {
                    appendKeyDirectly()
                } else if (valueKind != LIST && valueKind != MAP) {
                    appendKey() // For non-null collections, defer to beginCollection() below.
                }
                serializer.serialize(this, value)
            }
            tail(descriptor, index)
        }

        override fun head(descriptor: SerialDescriptor, index: Int) {}

        private fun <T> storeKey(value: T) {
            key = value.toTomlKey()
                .escape()
                .doubleQuotedIfNotPure()
            currentChildPath = concatPath(key)
        }

        override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
            if (descriptor.isArrayOfTables.not()) {
                appendKey()
            } else if (collectionSize == 0) {
                if (currentElementIndex != 1) {
                    throw EmptyArrayOfTableInMapException()
                }
                appendKeyDirectly()
            }
            return super.beginCollection(descriptor, collectionSize)
        }

        private fun appendKey() {
            when {
                inlineChild -> {
                    appendKeyDirectly()
                }
                structuredChild -> {
                    if (valueDescriptor.isTable) {
                        writer.writeLineFeed()
                        writer.writeString("[$currentChildPath]")
                        writer.writeLineFeed()
                    } else {
                        writer.writeKey(key)
                    }
                }
                valueDescriptor.isTable.not() -> {
                    writer.writeKey(currentChildPath)
                }
            }
        }

        private fun appendKeyDirectly() {
            writer.writeKey(if (structured) key else currentChildPath)
        }

        override fun tail(descriptor: SerialDescriptor, index: Int) {
            if (!isKey && index != collectionSize * 2 - 1) {
                writer.writeLineFeed()
            }
            currentElementIndex++
        }
    }
}

private val SerialDescriptor.isTableLike: Boolean
    get() = isTable || isArrayOfTables

private val SerialDescriptor.isTable: Boolean
    get() {
        val kind = kind
        return kind == CLASS || kind == MAP
    }

private val SerialDescriptor.isArrayOfTables: Boolean
    get() = kind == LIST && getElementDescriptor(0).isTable

private val SerialDescriptor.isExactlyTomlTable: Boolean
    get() = serialName.removeSuffix("?") == TomlTable.serializer().descriptor.serialName

private val Any?.isNullLike: Boolean
    get() = this == null || this == TomlNull

private fun calculateStructuredIndex(descriptor: SerialDescriptor): Int {
    var structuredIndex = 0
    for (i in descriptor.elementsCount - 1 downTo 0) {
        val elementDescriptor = descriptor.getElementDescriptor(i)
        if (descriptor.forceInlineAt(i) || elementDescriptor.isTableLike.not()) {
            structuredIndex = i + 1
            break
        }
    }
    return structuredIndex
}

private fun SerialDescriptor.forceInlineAt(index: Int): Boolean {
    val elementDescriptor = getElementDescriptor(index)
    return getElementAnnotations(index).hasAnnotation<TomlInline>() ||
            elementDescriptor.kind == CONTEXTUAL ||
            elementDescriptor.isExactlyTomlTable ||
            elementDescriptor.isInline
}

private inline fun <reified T> List<Annotation>.hasAnnotation(): Boolean {
    return filterIsInstance<T>().isNotEmpty()
}

private fun TomlWriter.writeKey(key: String) {
    writeString("$key = ")
}
