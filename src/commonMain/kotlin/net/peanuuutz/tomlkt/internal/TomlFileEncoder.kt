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
import kotlinx.serialization.descriptors.StructureKind.CLASS
import kotlinx.serialization.descriptors.StructureKind.LIST
import kotlinx.serialization.descriptors.StructureKind.MAP
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import net.peanuuutz.tomlkt.TomlConfig
import net.peanuuutz.tomlkt.TomlElement
import net.peanuuutz.tomlkt.TomlNull
import net.peanuuutz.tomlkt.TomlLiteral
import net.peanuuutz.tomlkt.TomlArray
import net.peanuuutz.tomlkt.TomlTable
import net.peanuuutz.tomlkt.toTomlKey
import net.peanuuutz.tomlkt.Comment
import net.peanuuutz.tomlkt.Fold
import net.peanuuutz.tomlkt.Multiline
import net.peanuuutz.tomlkt.Literal

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
    override fun encodeTomlElement(value: TomlElement) { // Internal
        when (value) {
            TomlNull, is TomlLiteral -> builder.append(value.toString())
            is TomlArray -> TomlArraySerializer.serialize(this, value)
            is TomlTable -> TomlTableSerializer.serialize(this, value)
        }
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) = encodeString(enumDescriptor.getElementName(index))
    override fun encodeInline(inlineDescriptor: SerialDescriptor): Encoder = this

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = beginStructure(descriptor, false)

    private fun beginStructure(
        descriptor: SerialDescriptor,
        forceFold: Boolean
    ): CompositeEncoder = if (descriptor.kind == CLASS) {
        if (forceFold)
            FlowClassEncoder()
        else {
            ClassEncoder(
                structuredIndex = calculateStructuredIndex(descriptor),
                structured = true,
                path = ""
            )
        }
    } else throw UnsupportedSerialKindException(descriptor.kind)

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder = beginCollection(descriptor, collectionSize, false)

    private fun beginCollection(
        descriptor: SerialDescriptor,
        collectionSize: Int,
        forceFold: Boolean
    ): CompositeEncoder = when (descriptor.kind) {
        LIST -> {
            if (forceFold || collectionSize == 0)
                FlowArrayEncoder(collectionSize)
            else
                BlockArrayEncoder(collectionSize)
        }
        MAP -> {
            if (forceFold || collectionSize == 0)
                FlowMapEncoder(collectionSize)
            else {
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

    private fun SerialDescriptor.foldAt(index: Int): Boolean
        = getElementAnnotations(index).filterIsInstance<Fold>().isNotEmpty()

    private val SerialDescriptor.isTable: Boolean get() = kind == CLASS || kind == MAP

    private val SerialDescriptor.isArrayOfTable: Boolean get() = kind == LIST && getElementDescriptor(0).isTable

    private val SerialDescriptor.isTomlElement: Boolean get() = this == TomlElement.serializer().descriptor

    private fun calculateStructuredIndex(descriptor: SerialDescriptor): Int {
        var structuredIndex = 0
        for (i in descriptor.elementsCount - 1 downTo 0) {
            val elementDescriptor = descriptor.getElementDescriptor(i)
            if (descriptor.foldAt(i) || !elementDescriptor.isTable && !elementDescriptor.isArrayOfTable) {
                structuredIndex = i + 1
                break
            }
        }
        return structuredIndex
    }

    internal abstract inner class AbstractEncoder : Encoder, CompositeEncoder, TomlEncoder {
        final override val serializersModule: SerializersModule = this@TomlFileEncoder.serializersModule

        final override fun encodeBoolean(value: Boolean) = this@TomlFileEncoder.encodeBoolean(value)
        final override fun encodeByte(value: Byte) = this@TomlFileEncoder.encodeByte(value)
        final override fun encodeShort(value: Short) = this@TomlFileEncoder.encodeShort(value)
        final override fun encodeInt(value: Int) = this@TomlFileEncoder.encodeInt(value)
        final override fun encodeLong(value: Long) = this@TomlFileEncoder.encodeLong(value)
        final override fun encodeFloat(value: Float) = this@TomlFileEncoder.encodeFloat(value)
        final override fun encodeDouble(value: Double) = this@TomlFileEncoder.encodeDouble(value)
        final override fun encodeChar(value: Char) = this@TomlFileEncoder.encodeChar(value)
        final override fun encodeString(value: String) = this@TomlFileEncoder.encodeString(value)
        final override fun encodeNull() = this@TomlFileEncoder.encodeNull()
        final override fun encodeTomlElement(value: TomlElement) { // Internal
            when (value) {
                TomlNull, is TomlLiteral -> builder.append(value.toString())
                is TomlArray -> TomlArraySerializer.serialize(this, value)
                is TomlTable -> TomlTableSerializer.serialize(this, value)
            }
        }
        private fun encodeMultilineString(value: String) { builder.append("\"\"\"\n").append(value.escape(true)).append("\"\"\"") }
        private fun encodeLiteralString(value: String) {
            require('\'' !in value && '\n' !in value) { "Cannot have '\\'' or '\\n' in literal string" }
            builder.append(value.singleQuoted)
        }
        private fun encodeMultilineLiteralString(value: String) {
            require("'''" !in value) { "Cannot have \"\\'\\'\\'\" in multiline literal string" }
            builder.append("'''\n").append(value).append("'''")
        }

        final override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) = this@TomlFileEncoder.encodeEnum(enumDescriptor, index)
        final override fun encodeInline(inlineDescriptor: SerialDescriptor): Encoder = this

        override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = beginStructure(descriptor, true)

        override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder = beginCollection(descriptor, collectionSize, true)

        final override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) { encodeSerializableElement(descriptor, index, Boolean.serializer(), value) }
        final override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) { encodeSerializableElement(descriptor, index, Byte.serializer(), value) }
        final override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) { encodeSerializableElement(descriptor, index, Short.serializer(), value) }
        final override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) { encodeSerializableElement(descriptor, index, Int.serializer(), value) }
        final override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) { encodeSerializableElement(descriptor, index, Long.serializer(), value) }
        final override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) { encodeSerializableElement(descriptor, index, Float.serializer(), value) }
        final override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) { encodeSerializableElement(descriptor, index, Double.serializer(), value) }
        final override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) { encodeSerializableElement(descriptor, index, Char.serializer(), value) }
        final override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) { encodeSerializableElement(descriptor, index, String.serializer(), value) }

        final override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder = this

        final override fun <T : Any> encodeNullableSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T?
        ) {
            if (value == null)
                encodeSerializableElement(descriptor, index, TomlNullSerializer, TomlNull)
            else
                encodeSerializableElement(descriptor, index, serializer, value)
        }

        final override fun <T> encodeSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T
        ) {
            head(descriptor, index, value)
            serialize(descriptor, index, serializer, value)
            tail(descriptor, index)
        }

        protected abstract fun <T> head(descriptor: SerialDescriptor, index: Int, value: T)

        protected open fun <T> serialize(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T
        ) {
            if (value !is String)
                serializer.serialize(this, value)
            else {
                val annotations = descriptor.getElementAnnotations(index)
                if (annotations.filterIsInstance<Literal>().isEmpty()) {
                    if (annotations.filterIsInstance<Multiline>().isEmpty())
                        serializer.serialize(this, value)
                    else
                        encodeMultilineString(value)
                } else {
                    if (annotations.filterIsInstance<Multiline>().isEmpty())
                        encodeLiteralString(value)
                    else
                        encodeMultilineLiteralString(value)
                }
            }
        }

        protected abstract fun tail(descriptor: SerialDescriptor, index: Int)

        protected fun Appendable.appendKey(value: String): Appendable = append(value).append(" = ")
    }

    internal abstract inner class BlockEncoder : AbstractEncoder() {
        final override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = super.beginStructure(descriptor)
        final override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder = super.beginCollection(descriptor, collectionSize)
    }

    internal inner class BlockArrayEncoder(
        private val collectionSize: Int
    ) : BlockEncoder() {
        init { builder.appendLine('[') }

        override fun <T> head(descriptor: SerialDescriptor, index: Int, value: T) { builder.append(INDENT) }

        override fun tail(descriptor: SerialDescriptor, index: Int) {
            if (index != collectionSize - 1)
                builder.append(',')
            builder.appendLine()
        }

        override fun endStructure(descriptor: SerialDescriptor) { builder.append(']') }
    }

    internal abstract inner class FlowEncoder : AbstractEncoder() {
        final override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = super.beginStructure(descriptor)
        final override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder = super.beginCollection(descriptor, collectionSize)
    }

    internal inner class FlowArrayEncoder(
        private val collectionSize: Int
    ) : FlowEncoder() {
        init { builder.append("[ ") }

        override fun <T> head(descriptor: SerialDescriptor, index: Int, value: T) {}

        override fun tail(descriptor: SerialDescriptor, index: Int) {
            if (index != collectionSize - 1)
                builder.append(", ")
        }

        override fun endStructure(descriptor: SerialDescriptor) { builder.append(" ]") }
    }

    internal inner class FlowClassEncoder : FlowEncoder() {
        init { builder.append("{ ") }

        override fun <T> head(descriptor: SerialDescriptor, index: Int, value: T) {
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
    ) : FlowEncoder() {
        private var isKey: Boolean = true

        init { builder.append("{ ") }

        override fun <T> head(descriptor: SerialDescriptor, index: Int, value: T) {
            if (isKey)
                builder.appendKey(value.toTomlKey().escape().doubleQuotedIfNeeded())
        }

        override fun <T> serialize(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T
        ) {
            if (!isKey)
                super.serialize(descriptor, index, serializer, value)
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
        protected var foldChild: Boolean = false

        protected lateinit var currentChildPath: String

        protected var structuredChild: Boolean = false

        override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = if (descriptor.kind == CLASS) {
            if (foldChild)
                FlowClassEncoder()
            else {
                ClassEncoder(
                    structuredIndex = calculateStructuredIndex(descriptor),
                    structured = structuredChild,
                    path = currentChildPath
                )
            }
        } else throw UnsupportedSerialKindException(descriptor.kind)

        override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder = when (descriptor.kind) {
            LIST -> {
                if (foldChild || collectionSize == 0) {
                    FlowArrayEncoder(collectionSize)
                } else if (structuredChild && descriptor.isArrayOfTable) {
                    ArrayOfTableEncoder(
                        collectionSize = collectionSize,
                        path = currentChildPath
                    )
                } else BlockArrayEncoder(collectionSize)
            }
            MAP -> {
                val valueDescriptor = descriptor.getElementDescriptor(1)
                if (foldChild || collectionSize == 0 || valueDescriptor.isTomlElement)
                    FlowMapEncoder(collectionSize)
                else {
                    MapEncoder(
                        collectionSize = collectionSize,
                        valueDescriptor = valueDescriptor,
                        structured = structuredChild,
                        path = currentChildPath
                    )
                }
            }
            else -> throw UnsupportedSerialKindException(descriptor.kind)
        }

        protected fun concatPath(childPath: String): String
            = if (path.isNotEmpty()) "$path.$childPath" else childPath

        override fun endStructure(descriptor: SerialDescriptor) {}
    }

    internal inner class ClassEncoder(
        private val structuredIndex: Int,
        structured: Boolean,
        path: String
    ) : TableLikeEncoder(structured, path) {
        override fun <T> head(descriptor: SerialDescriptor, index: Int, value: T) {
            comment(descriptor, index)
            val elementName = descriptor.getElementName(index).escape().doubleQuotedIfNeeded()
            foldChild = descriptor.foldAt(index)
            currentChildPath = concatPath(elementName)
            structuredChild = structured && index >= structuredIndex
            if (foldChild)
                builder.appendKey(if (structured) elementName else currentChildPath)
            else {
                if (structuredChild) {
                    if (descriptor.getElementDescriptor(index).isTable)
                        builder.appendLine().appendLine("[$currentChildPath]")
                } else {
                    if (!descriptor.getElementDescriptor(index).isTable)
                        builder.appendKey(if (structured) elementName else currentChildPath)
                }
            }
        }

        private fun comment(descriptor: SerialDescriptor, index: Int) {
            if (structured) {
                val list = mutableListOf<String>()
                descriptor.getElementAnnotations(index)
                    .filterIsInstance<Comment>()
                    .takeIf { it.isNotEmpty() }
                    ?.let { it[0].texts.forEach(list::add) }
                if (list.size > 0) {
                    if (index != 0)
                        builder.appendLine()
                    list.forEach { builder.appendLine("# ${it.escape(false)}") }
                }
            }
        }

        override fun tail(descriptor: SerialDescriptor, index: Int) {
            if (index != descriptor.elementsCount - 1)
                builder.appendLine()
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

        override fun <T> head(descriptor: SerialDescriptor, index: Int, value: T) {
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
        private var isKey: Boolean = true

        init { structuredChild = structured }

        override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
            if (config.checkArrayInMap && collectionSize == 0 && descriptor.isArrayOfTable)
                throw EmptyArrayOfTableInMapException()
            return super.beginCollection(descriptor, collectionSize)
        }

        override fun <T> head(descriptor: SerialDescriptor, index: Int, value: T) {
            if (isKey) {
                val key = value.toTomlKey().escape().doubleQuotedIfNeeded()
                currentChildPath = concatPath(key)
                if (structuredChild) {
                    if (valueDescriptor.isTable)
                        builder.appendLine().appendLine("[$currentChildPath]")
                    else if (!valueDescriptor.isArrayOfTable)
                        builder.appendKey(key)
                } else {
                    if (!valueDescriptor.isTable)
                        builder.appendKey(currentChildPath)
                }
            }
        }

        override fun <T> serialize(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T
        ) {
            if (!isKey)
                super.serialize(descriptor, index, serializer, value)
        }

        override fun tail(descriptor: SerialDescriptor, index: Int) {
            if (!isKey && index != collectionSize * 2 - 1)
                builder.appendLine()
            isKey = !isKey
        }
    }
}