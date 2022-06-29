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
import net.peanuuutz.tomlkt.toTomlKey
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
import net.peanuuutz.tomlkt.TomlTable

internal class TomlFileEncoder(
    private val config: TomlConfig,
    override val serializersModule: SerializersModule,
    private val builder: Appendable
) : Encoder, TomlEncoder {
    override fun encodeBoolean(value: Boolean) { builder.append(value.toString()) }
    override fun encodeByte(value: Byte) { builder.append(value.toString()) }
    override fun encodeShort(value: Short) { builder.append(value.toString()) }
    override fun encodeInt(value: Int) { builder.append(value.toString()) }
    override fun encodeLong(value: Long) { builder.append(value.toString()) }
    override fun encodeFloat(value: Float) { builder.append(value.toString()) }
    override fun encodeDouble(value: Double) { builder.append(value.toString()) }
    override fun encodeChar(value: Char) { builder.append(value.escape().doubleQuoted) }
    override fun encodeString(value: String) { builder.append(value.escape().doubleQuoted) }
    override fun encodeNull() { encodeTomlElement(TomlNull) }
    override fun encodeTomlElement(value: TomlElement) {
        when (value) {
            TomlNull, is TomlLiteral -> builder.append(value.toString())
            is TomlArray -> TomlArraySerializer.serialize(this, value)
            is TomlTable -> TomlTableSerializer.serialize(this, value)
        }
    }

    override fun encodeInline(inlineDescriptor: SerialDescriptor): Encoder = this
    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        encodeString(enumDescriptor.getElementName(index))
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        return beginStructure(descriptor, false)
    }

    private fun beginStructure(
        descriptor: SerialDescriptor,
        forceInline: Boolean
    ): CompositeEncoder = when (descriptor.kind) {
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
        else -> throw UnsupportedSerialKindException(descriptor.kind)
    }

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        return beginCollection(descriptor, collectionSize, false)
    }

    private fun beginCollection(
        descriptor: SerialDescriptor,
        collectionSize: Int,
        forceInline: Boolean
    ): CompositeEncoder = when (descriptor.kind) {
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
        else -> throw UnsupportedSerialKindException(descriptor.kind)
    }

    // region Utils

    private fun calculateStructuredIndex(descriptor: SerialDescriptor): Int {
        var structuredIndex = 0
        for (i in descriptor.elementsCount - 1 downTo 0) {
            val elementDescriptor = descriptor.getElementDescriptor(i)
            if (descriptor.forceInlineAt(i) || !elementDescriptor.isTableLike) {
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
                elementDescriptor.isTomlTable ||
                elementDescriptor.isInline
    }

    private val SerialDescriptor.isTableLike: Boolean get() = isTable || isArrayOfTables

    private val SerialDescriptor.isTable: Boolean get() = kind == CLASS || kind == MAP

    private val SerialDescriptor.isArrayOfTables: Boolean get() = kind == LIST && getElementDescriptor(0).isTable

    private val SerialDescriptor.isTomlTable: Boolean get() {
        return serialName.removeSuffix("?") == TomlTableSerializer.descriptor.serialName
    }

    private inline fun <reified T> List<Annotation>.hasAnnotation(): Boolean {
        return filterIsInstance<T>().isNotEmpty()
    }

    private val <T> T.isNull: Boolean get() = this == null || this == TomlNull

    private fun Appendable.appendKey(value: String): Appendable = append(value).append(" = ")

    // endregion

    internal abstract inner class AbstractEncoder : Encoder, CompositeEncoder, TomlEncoder {
        final override val serializersModule: SerializersModule = this@TomlFileEncoder.serializersModule

        final override fun encodeBoolean(value: Boolean) = this@TomlFileEncoder.encodeBoolean(value)
        final override fun encodeByte(value: Byte) = this@TomlFileEncoder.encodeByte(value)
        private fun encodeByteWithBase(value: Byte, base: TomlInteger.Base) {
            require(value > 0) { "Negative integer cannot be represented by other bases" }
            builder.append(base.prefix).append(value.toString(base.value))
        }
        final override fun encodeShort(value: Short) = this@TomlFileEncoder.encodeShort(value)
        private fun encodeShortWithBase(value: Short, base: TomlInteger.Base) {
            require(value > 0) { "Negative integer cannot be represented by other bases" }
            builder.append(base.prefix).append(value.toString(base.value))
        }
        final override fun encodeInt(value: Int) = this@TomlFileEncoder.encodeInt(value)
        private fun encodeIntWithBase(value: Int, base: TomlInteger.Base) {
            require(value > 0) { "Negative integer cannot be represented by other bases" }
            builder.append(base.prefix).append(value.toString(base.value))
        }
        final override fun encodeLong(value: Long) = this@TomlFileEncoder.encodeLong(value)
        private fun encodeLongWithBase(value: Long, base: TomlInteger.Base) {
            require(value > 0) { "Negative integer cannot be represented by other bases" }
            builder.append(base.prefix).append(value.toString(base.value))
        }
        final override fun encodeFloat(value: Float) = this@TomlFileEncoder.encodeFloat(value)
        final override fun encodeDouble(value: Double) = this@TomlFileEncoder.encodeDouble(value)
        final override fun encodeChar(value: Char) = this@TomlFileEncoder.encodeChar(value)
        final override fun encodeString(value: String) = this@TomlFileEncoder.encodeString(value)
        private fun encodeMultilineString(value: String) {
            builder.appendLine("\"\"\"").append(value.escape(true)).append("\"\"\"")
        }
        private fun encodeLiteralString(value: String) {
            require('\'' !in value && '\n' !in value) { "Cannot have '\\'' or '\\n' in literal string" }
            builder.append(value.singleQuoted)
        }
        private fun encodeMultilineLiteralString(value: String) {
            require("'''" !in value) { "Cannot have \"\\'\\'\\'\" in multiline literal string" }
            builder.appendLine("'''").append(value).append("'''")
        }
        final override fun encodeNull() = this@TomlFileEncoder.encodeNull()
        final override fun encodeTomlElement(value: TomlElement) {
            when (value) {
                TomlNull, is TomlLiteral -> builder.append(value.toString())
                is TomlArray -> TomlArraySerializer.serialize(this, value)
                is TomlTable -> TomlTableSerializer.serialize(this, value)
            }
        }

        final override fun encodeInline(inlineDescriptor: SerialDescriptor): Encoder = this
        final override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
            this@TomlFileEncoder.encodeEnum(enumDescriptor, index)
        }

        override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
            return beginStructure(descriptor, true)
        }

        override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
            return beginCollection(descriptor, collectionSize, true)
        }

        final override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
            encodeSerializableElement(descriptor, index, Boolean.serializer(), value)
        }
        final override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
            encodeTomlIntegerElement(descriptor, index, value, ::encodeByte, ::encodeByteWithBase)
        }
        final override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
            encodeTomlIntegerElement(descriptor, index, value, ::encodeShort, ::encodeShortWithBase)
        }
        final override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
            encodeTomlIntegerElement(descriptor, index, value, ::encodeInt, ::encodeIntWithBase)
        }
        final override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
            encodeTomlIntegerElement(descriptor, index, value, ::encodeLong, ::encodeLongWithBase)
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
            if (isLiteral) {
                if (isMultiline) {
                    encodeMultilineLiteralString(value)
                } else {
                    encodeLiteralString(value)
                }
            } else {
                if (isMultiline) {
                    encodeMultilineString(value)
                } else {
                    encodeString(value)
                }
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
            when (value) { // Revisit type-specific encoding
                null -> encodeSerializableElement(descriptor, index, TomlNullSerializer, TomlNull)
                is Byte -> encodeByteElement(descriptor, index, value)
                is Short -> encodeShortElement(descriptor, index, value)
                is Int -> encodeIntElement(descriptor, index, value)
                is Long -> encodeLongElement(descriptor, index, value)
                is String -> encodeStringElement(descriptor, index, value)
                else -> encodeSerializableElement(descriptor, index, serializer, value)
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

        init { builder.appendLine('[') }

        override fun head(descriptor: SerialDescriptor, index: Int) {
            if (itemsInCurrentLine == 0) {
                builder.append(INDENT)
            } else {
                builder.append(' ')
            }
        }

        override fun tail(descriptor: SerialDescriptor, index: Int) {
            if (index != collectionSize - 1)
                builder.append(',')
            if (++itemsInCurrentLine >= itemsPerLine) {
                builder.appendLine()
                itemsInCurrentLine = 0
            }
        }

        override fun endStructure(descriptor: SerialDescriptor) {
            if (itemsInCurrentLine != 0)
                builder.appendLine()
            builder.append(']')
        }
    }

    internal inner class FlowArrayEncoder(
        private val collectionSize: Int
    ) : AbstractEncoder() {
        init { builder.append("[ ") }

        override fun head(descriptor: SerialDescriptor, index: Int) {}

        override fun tail(descriptor: SerialDescriptor, index: Int) {
            if (index != collectionSize - 1)
                builder.append(", ")
        }

        override fun endStructure(descriptor: SerialDescriptor) { builder.append(" ]") }
    }

    internal inner class FlowClassEncoder : AbstractEncoder() {
        init { builder.append("{ ") }

        override fun head(descriptor: SerialDescriptor, index: Int) {
            builder.appendKey(descriptor.getElementName(index).escape().doubleQuotedIfNeeded())
        }

        override fun tail(descriptor: SerialDescriptor, index: Int) {
            if (index != descriptor.elementsCount - 1)
                builder.append(", ")
        }

        override fun endStructure(descriptor: SerialDescriptor) { builder.append(" }") }
    }

    internal inner class FlowMapEncoder(
        private val collectionSize: Int
    ) : AbstractEncoder() {
        private var isKey: Boolean = true

        init { builder.append("{ ") }

        override fun head(descriptor: SerialDescriptor, index: Int) {}

        override fun <T> encodeSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T
        ) {
            if (isKey) {
                builder.appendKey(value.toTomlKey().escape().doubleQuotedIfNeeded())
            } else {
                serializer.serialize(this, value)
            }
            tail(descriptor, index)
        }

        override fun tail(descriptor: SerialDescriptor, index: Int) {
            if (!isKey && index != collectionSize * 2 - 1)
                builder.append(", ")
            isKey = !isKey
        }

        override fun endStructure(descriptor: SerialDescriptor) { builder.append(" }") }
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
            inlineChild = value.isNull
            super.encodeSerializableElement(descriptor, index, serializer, value)
        }

        override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = when (descriptor.kind) {
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
            else -> throw UnsupportedSerialKindException(descriptor.kind)
        }

        override fun beginCollection(
            descriptor: SerialDescriptor,
            collectionSize: Int
        ): CompositeEncoder = when (descriptor.kind) {
            LIST -> {
                if (inlineChild || collectionSize == 0) {
                    FlowArrayEncoder(collectionSize)
                } else if (structuredChild && descriptor.isArrayOfTables && blockArrayModification == null) {
                    ArrayOfTableEncoder(
                        collectionSize = collectionSize,
                        path = currentChildPath
                    )
                } else {
                    BlockArrayEncoder(
                        collectionSize = collectionSize,
                        itemsPerLine = blockArrayModification?.itemsPerLine ?: config.itemsPerLineInBlockArray
                    )
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
            else -> throw UnsupportedSerialKindException(descriptor.kind)
        }

        protected open val blockArrayModification: TomlBlockArray? get() = null

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
        override var blockArrayModification: TomlBlockArray? = null

        override fun head(descriptor: SerialDescriptor, index: Int) {
            comment(descriptor, index)
            val elementName = descriptor.getElementName(index).escape().doubleQuotedIfNeeded()
            val key = if (structured) elementName else currentChildPath
            val elementDescriptor = descriptor.getElementDescriptor(index)
            inlineChild = inlineChild || descriptor.forceInlineAt(index)
            currentChildPath = concatPath(elementName)
            structuredChild = structured && index >= structuredIndex
            if (inlineChild) {
                builder.appendKey(key)
            } else if (structuredChild) {
                if (elementDescriptor.isTable) {
                    builder.appendLine().appendLine("[$currentChildPath]")
                } else if (elementDescriptor.isArrayOfTables) {
                    val annotations = descriptor.getElementAnnotations(index).filterIsInstance<TomlBlockArray>()
                    if (annotations.isNotEmpty()) {
                        builder.appendKey(key)
                        blockArrayModification = annotations[0]
                    }
                }
            } else {
                if (!elementDescriptor.isTable)
                    builder.appendKey(key)
            }
        }

        private fun comment(descriptor: SerialDescriptor, index: Int) {
            if (!structured)
                return
            val lines = mutableListOf<String>()
            val annotations = descriptor.getElementAnnotations(index)
            var inline = false
            for (annotation in annotations) {
                when (annotation) {
                    is TomlInline -> inline = true
                    is TomlComment -> annotation.text.trimIndent().split('\n').forEach(lines::add)
                }
            }
            if (lines.size > 0) {
                val elementDescriptor = descriptor.getElementDescriptor(index)
                if (elementDescriptor.isTableLike && !inline)
                    builder.appendLine()
                for (line in lines)
                    builder.appendLine("# ${line.escape(false)}")
            }
        }

        override fun tail(descriptor: SerialDescriptor, index: Int) {
            if (index != descriptor.elementsCount - 1)
                builder.appendLine()
            blockArrayModification = null
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
            if (value.isNull)
                throw NullInArrayOfTableException()
            super.encodeSerializableElement(descriptor, index, serializer, value)
        }

        override fun head(descriptor: SerialDescriptor, index: Int) {
            builder.appendLine().appendLine("[[$currentChildPath]]")
        }

        override fun tail(descriptor: SerialDescriptor, index: Int) {
            if (index != collectionSize - 1)
                builder.appendLine()
        }
    }

    internal inner class MapEncoder(
        private val collectionSize: Int,
        private val valueDescriptor: SerialDescriptor,
        structured: Boolean,
        path: String
    ) : TableLikeEncoder(structured, path) {
        private var currentElementIndex: Int = 0

        private val isKey: Boolean get() = currentElementIndex % 2 == 0

        private lateinit var key: String

        init {
            inlineChild = valueDescriptor.kind == CONTEXTUAL || valueDescriptor.isTomlTable
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
                if (value.isNull) {
                    appendKeyDirectly()
                } else if (valueDescriptor.kind != LIST && valueDescriptor.kind != MAP) {
                    appendKey() // For non-null collections, defer to beginCollection() below
                }
                serializer.serialize(this, value)
            }
            tail(descriptor, index)
        }

        override fun head(descriptor: SerialDescriptor, index: Int) {}

        private fun <T> storeKey(value: T) {
            key = value.toTomlKey().escape().doubleQuotedIfNeeded()
            currentChildPath = concatPath(key)
        }

        override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
            if (!descriptor.isArrayOfTables) { // This descriptor is the same as valueDescriptor property
                appendKey()
            } else if (collectionSize == 0) {
                if (currentElementIndex != 1)
                    throw EmptyArrayOfTableInMapException()
                appendKeyDirectly()
            }
            return super.beginCollection(descriptor, collectionSize)
        }

        private fun appendKey() {
            if (inlineChild) {
                builder.appendKey(if (structured) key else currentChildPath)
            } else if (structuredChild) {
                if (valueDescriptor.isTable) {
                    builder.appendLine().appendLine("[$currentChildPath]")
                } else {
                    builder.appendKey(key)
                }
            } else {
                if (!valueDescriptor.isTable)
                    builder.appendKey(currentChildPath)
            }
        }

        private fun appendKeyDirectly() {
            builder.appendKey(if (structured) key else currentChildPath)
        }

        override fun tail(descriptor: SerialDescriptor, index: Int) {
            if (!isKey && index != collectionSize * 2 - 1)
                builder.appendLine()
            currentElementIndex++
        }
    }
}