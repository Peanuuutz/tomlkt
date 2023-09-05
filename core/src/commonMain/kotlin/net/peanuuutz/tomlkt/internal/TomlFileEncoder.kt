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
            TomlNull -> {
                writer.writeNull()
            }
            is TomlLiteral -> {
                writer.writeString(value.toString())
            }
            is TomlArray -> {
                TomlArray.serializer().serialize(this, value)
            }
            is TomlTable -> {
                TomlTable.serializer().serialize(this, value)
            }
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
                        structuredTableLikeIndex = calculateStructuredTableLikeIndex(descriptor),
                        isStructured = true,
                        path = ""
                    )
                }
            }
            OBJECT -> FlowClassEncoder()
            LIST -> throwUnsupportedSerialKind("Please use beginCollection() instead")
            MAP -> throwUnsupportedSerialKind("Please use beginCollection() instead")
            else -> throwUnsupportedSerialKind(kind)
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
                when {
                    collectionSize == 0 -> FlowArrayEncoder(0)
                    forceInline -> FlowArrayEncoder(collectionSize)
                    else -> {
                        BlockArrayEncoder(
                            arraySize = collectionSize,
                            itemsPerLine = config.itemsPerLineInBlockArray
                        )
                    }
                }
            }
            MAP -> {
                when {
                    collectionSize == 0 -> FlowMapEncoder(0)
                    forceInline -> FlowMapEncoder(collectionSize)
                    else -> {
                        MapEncoder(
                            mapSize = collectionSize,
                            valueDescriptor = descriptor.getElementDescriptor(1),
                            isStructured = true,
                            path = ""
                        )
                    }
                }
            }
            CLASS -> throwUnsupportedSerialKind("Please use beginStructure() instead")
            OBJECT -> throwUnsupportedSerialKind("Please use beginStructure() instead")
            else -> throwUnsupportedSerialKind(kind)
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
            require(value >= 0 || base == TomlInteger.Base.Dec) {
                "Negative integer cannot be represented by other bases, but found $value"
            }
            writer.writeString(base.prefix)
            writer.writeString(value.toString(base.value))
        }

        final override fun encodeShort(value: Short) {
            this@TomlFileEncoder.encodeShort(value)
        }

        private fun encodeShortWithBase(value: Short, base: TomlInteger.Base) {
            require(value >= 0 || base == TomlInteger.Base.Dec) {
                "Negative integer cannot be represented by other bases, but found $value"
            }
            writer.writeString(base.prefix)
            writer.writeString(value.toString(base.value))
        }

        final override fun encodeInt(value: Int) {
            this@TomlFileEncoder.encodeInt(value)
        }

        private fun encodeIntWithBase(value: Int, base: TomlInteger.Base) {
            require(value >= 0 || base == TomlInteger.Base.Dec) {
                "Negative integer cannot be represented by other bases, but found $value"
            }
            writer.writeString(base.prefix)
            writer.writeString(value.toString(base.value))
        }

        final override fun encodeLong(value: Long) {
            this@TomlFileEncoder.encodeLong(value)
        }

        private fun encodeLongWithBase(value: Long, base: TomlInteger.Base) {
            require(value >= 0 || base == TomlInteger.Base.Dec) {
                "Negative integer cannot be represented by other bases, but found $value"
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
                "Cannot have '\\'' or '\\n' in literal string, but found $value"
            }
            writer.writeString(value.singleQuoted)
        }

        private fun encodeMultilineLiteralString(value: String) {
            require("'''" !in value) {
                "Cannot have \"\\'\\'\\'\" in multiline literal string, but found $value"
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
            beginElement(descriptor, index, value)
            val annotations = descriptor.getElementAnnotations(index).filterIsInstance<TomlInteger>()
            if (annotations.isEmpty()) {
                encodeNormally(value)
            } else {
                encodeWithBase(value, annotations[0].base)
            }
            endElement(descriptor, index)
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
            beginElement(descriptor, index, value)
            val annotations = descriptor.getElementAnnotations(index)
            val isLiteral = annotations.hasAnnotation<TomlLiteralString>()
            val isMultiline = annotations.hasAnnotation<TomlMultilineString>()
            when {
                !isLiteral && !isMultiline -> encodeString(value)
                !isLiteral -> encodeMultilineString(value)
                !isMultiline -> encodeLiteralString(value)
                else -> encodeMultilineLiteralString(value)
            }
            endElement(descriptor, index)
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
            beginElement(descriptor, index, value)
            serializer.serialize(this, value)
            endElement(descriptor, index)
        }

        protected abstract fun <T> beginElement(
            descriptor: SerialDescriptor,
            index: Int,
            value: T
        )

        protected abstract fun endElement(
            descriptor: SerialDescriptor,
            index: Int
        )
    }

    internal inner class FlowClassEncoder : AbstractEncoder() {
        init { writer.writeString("{ ") }

        override fun <T> beginElement(descriptor: SerialDescriptor, index: Int, value: T) {
            val currentKey = descriptor.getElementName(index)
                .escape()
                .doubleQuotedIfNotPure()
            writer.writeKey(currentKey)
        }

        override fun endElement(descriptor: SerialDescriptor, index: Int) {
            if (index != descriptor.elementsCount - 1) {
                writer.writeString(", ")
            }
        }

        override fun endStructure(descriptor: SerialDescriptor) {
            writer.writeString(" }")
        }
    }

    internal inner class FlowMapEncoder(
        private val mapSize: Int
    ) : AbstractEncoder() {
        private var isKey: Boolean = true

        init { writer.writeString("{ ") }

        override fun <T> encodeSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T
        ) {
            if (isKey) { // This element is key.
                val currentKey = value.toTomlKey()
                    .escape()
                    .doubleQuotedIfNotPure()
                writer.writeKey(currentKey)
            } else { // This element is value (of the entry).
                serializer.serialize(this, value)
                if (index != mapSize * 2 - 1) {
                    writer.writeString(", ")
                }
            }
            isKey = !isKey
        }

        // This shouldn't be called.
        override fun <T> beginElement(descriptor: SerialDescriptor, index: Int, value: T) {}

        // This shouldn't be called.
        override fun endElement(descriptor: SerialDescriptor, index: Int) {}

        override fun endStructure(descriptor: SerialDescriptor) {
            writer.writeString(" }")
        }
    }

    internal inner class FlowArrayEncoder(
        private val arraySize: Int
    ) : AbstractEncoder() {
        init { writer.writeString("[ ") }

        override fun <T> beginElement(descriptor: SerialDescriptor, index: Int, value: T) {}

        override fun endElement(descriptor: SerialDescriptor, index: Int) {
            if (index != arraySize - 1) {
                writer.writeString(", ")
            }
        }

        override fun endStructure(descriptor: SerialDescriptor) {
            writer.writeString(" ]")
        }
    }

    internal inner class BlockArrayEncoder(
        private val arraySize: Int,
        private val itemsPerLine: Int
    ) : AbstractEncoder() {
        private var itemsInCurrentLine: Int = 0

        init {
            writer.writeChar('[')
            writer.writeLineFeed()
        }

        override fun <T> beginElement(descriptor: SerialDescriptor, index: Int, value: T) {
            if (itemsInCurrentLine == 0) {
                writer.writeString(config.indentation.representation)
            } else {
                writer.writeChar(' ')
            }
        }

        override fun endElement(descriptor: SerialDescriptor, index: Int) {
            if (index != arraySize - 1) {
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

    internal abstract inner class TableLikeEncoder(
        protected val isStructured: Boolean,
        private val path: String
    ) : AbstractEncoder() {
        protected lateinit var currentElementPath: String

        protected var shouldInlineCurrentElement: Boolean = false

        protected var shouldStructureCurrentElement: Boolean = false

        override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
            return when (val kind = descriptor.kind) {
                CLASS -> {
                    when {
                        shouldInlineCurrentElement -> FlowClassEncoder()
                        shouldStructureCurrentElement -> {
                            ClassEncoder(
                                structuredTableLikeIndex = calculateStructuredTableLikeIndex(descriptor),
                                isStructured = true,
                                path = currentElementPath
                            )
                        }
                        else -> {
                            val realElementPath = getDroppedPath()
                            if (descriptor.elementsCount == 0) {
                                writer.writeKey(realElementPath)
                                FlowClassEncoder()
                            } else {
                                ClassEncoder(
                                    structuredTableLikeIndex = Int.MAX_VALUE,
                                    isStructured = false,
                                    path = realElementPath
                                )
                            }
                        }
                    }
                }
                OBJECT -> FlowClassEncoder()
                LIST -> throwUnsupportedSerialKind("Please use beginCollection() instead")
                MAP -> throwUnsupportedSerialKind("Please use beginCollection() instead")
                else -> throwUnsupportedSerialKind(kind)
            }
        }

        override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
            return when (val kind = descriptor.kind) {
                LIST -> {
                    when {
                        collectionSize == 0 -> FlowArrayEncoder(0)
                        shouldInlineCurrentElement -> FlowArrayEncoder(collectionSize)
                        shouldStructureCurrentElement && descriptor.isArrayOfTable && blockArrayAnnotation == null -> {
                            ArrayOfTableEncoder(
                                arraySize = collectionSize,
                                path = currentElementPath
                            )
                        }
                        else -> {
                            val itemsPerLine = blockArrayAnnotation?.itemsPerLine ?: config.itemsPerLineInBlockArray
                            BlockArrayEncoder(
                                arraySize = collectionSize,
                                itemsPerLine = itemsPerLine
                            )
                        }
                    }
                }
                MAP -> {
                    when {
                        shouldInlineCurrentElement -> FlowMapEncoder(collectionSize)
                        shouldStructureCurrentElement -> {
                            MapEncoder(
                                mapSize = collectionSize,
                                valueDescriptor = descriptor.getElementDescriptor(1),
                                isStructured = true,
                                path = currentElementPath
                            )
                        }
                        else -> {
                            val realElementPath = getDroppedPath()
                            if (collectionSize == 0) {
                                writer.writeKey(realElementPath)
                                FlowMapEncoder(0)
                            } else {
                                MapEncoder(
                                    mapSize = collectionSize,
                                    valueDescriptor = descriptor.getElementDescriptor(1),
                                    isStructured = false,
                                    path = realElementPath
                                )
                            }
                        }
                    }
                }
                CLASS -> throwUnsupportedSerialKind("Please use beginStructure() instead")
                OBJECT -> throwUnsupportedSerialKind("Please use beginStructure() instead")
                else -> throwUnsupportedSerialKind(kind)
            }
        }

        protected open val blockArrayAnnotation: TomlBlockArray?
            get() = null

        protected fun getConcatenatedPath(currentElementPath: String): String {
            return if (path.isNotEmpty()) {
                "$path.$currentElementPath"
            } else {
                currentElementPath
            }
        }

        private fun getDroppedPath(): String {
            return if (isStructured) {
                currentElementPath.removePrefix("$path.")
            } else {
                currentElementPath
            }
        }

        override fun endStructure(descriptor: SerialDescriptor) {}
    }

    internal inner class ClassEncoder(
        private val structuredTableLikeIndex: Int,
        isStructured: Boolean,
        path: String
    ) : TableLikeEncoder(isStructured, path) {
        private lateinit var currentElementDescriptor: SerialDescriptor

        private lateinit var currentKey: String

        override var blockArrayAnnotation: TomlBlockArray? = null

        override fun <T> beginElement(descriptor: SerialDescriptor, index: Int, value: T) {
            currentElementDescriptor = descriptor.getElementDescriptor(index)
            currentKey = descriptor.getElementName(index)
                .escape()
                .doubleQuotedIfNotPure()
            currentElementPath = getConcatenatedPath(currentKey)
            shouldInlineCurrentElement = descriptor.shouldForceInlineAt(index)
            shouldStructureCurrentElement = isStructured && index >= structuredTableLikeIndex
            processElementAnnotations(descriptor.getElementAnnotations(index))
            when {
                value.isNullOrTomlNull -> {
                    appendKeyDirectly()
                }
                currentElementDescriptor.isCollection.not() -> {
                    appendKey()
                }
                // For non-null collections, defer to beginCollection().
            }
        }

        private fun processElementAnnotations(annotations: List<Annotation>) {
            if (!isStructured) {
                return
            }
            val commentLines = mutableListOf<String>()
            var shouldAddExtraLineFeed = shouldStructureCurrentElement
            for (annotation in annotations) {
                when (annotation) {
                    is TomlComment -> {
                        annotation.text
                            .trimIndent()
                            .split('\n')
                            .map(String::escape)
                            .forEach(commentLines::add)
                    }
                    is TomlBlockArray -> {
                        blockArrayAnnotation = annotation
                    }
                    is TomlInline -> {
                        shouldAddExtraLineFeed = false
                    }
                }
            }
            if (commentLines.size > 0) {
                if (shouldAddExtraLineFeed && currentElementDescriptor.isTableLike) {
                    writer.writeLineFeed()
                }
                for (line in commentLines) {
                    writer.writeString("# $line")
                    writer.writeLineFeed()
                }
            }
        }

        override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
            // descriptor == currentElementDescriptor.
            if (descriptor.isArrayOfTable) {
                if (collectionSize == 0 || !shouldStructureCurrentElement) {
                    appendKeyDirectly()
                }
            } else {
                appendKey()
            }
            return super.beginCollection(descriptor, collectionSize)
        }

        private fun appendKey() {
            when {
                shouldInlineCurrentElement -> {
                    appendKeyDirectly()
                }
                currentElementDescriptor.isTable -> {
                    if (shouldStructureCurrentElement) {
                        writer.writeLineFeed()
                        writer.writeString("[$currentElementPath]")
                        writer.writeLineFeed()
                    }
                }
                else -> {
                    appendKeyDirectly()
                }
            }
        }

        private fun appendKeyDirectly() {
            writer.writeKey(if (isStructured) currentKey else currentElementPath)
        }

        override fun endElement(descriptor: SerialDescriptor, index: Int) {
            if (index != descriptor.elementsCount - 1) {
                writer.writeLineFeed()
            }
            blockArrayAnnotation = null
        }
    }

    internal inner class MapEncoder(
        private val mapSize: Int,
        private val valueDescriptor: SerialDescriptor,
        isStructured: Boolean,
        path: String
    ) : TableLikeEncoder(isStructured, path) {
        private var currentElementIndex: Int = 0

        private val isKey: Boolean
            get() = currentElementIndex % 2 == 0

        private lateinit var currentKey: String

        init {
            shouldInlineCurrentElement = valueDescriptor.shouldForceInline
            shouldStructureCurrentElement = isStructured
        }

        override fun <T> encodeSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T
        ) {
            if (isKey) { // This element is key.
                currentKey = value.toTomlKey()
                    .escape()
                    .doubleQuotedIfNotPure()
                currentElementPath = getConcatenatedPath(currentKey)
            } else { // This element is value (of the entry).
                when {
                    value.isNullOrTomlNull -> {
                        appendKeyDirectly()
                    }
                    valueDescriptor.isCollection.not() -> {
                        appendKey()
                    }
                    // For non-null collections, defer to beginCollection().
                }
                serializer.serialize(this, value)
                if (index != mapSize * 2 - 1) {
                    writer.writeLineFeed()
                }
            }
            currentElementIndex++
        }

        override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
            // descriptor == valueDescriptor.
            if (descriptor.isArrayOfTable) {
                if (collectionSize == 0 || !isStructured) {
                    if (collectionSize == 0 && currentElementIndex != 1) {
                        // Only the first array of table is allowed to be empty when in a map.
                        throwEmptyArrayOfTableInMap()
                    }
                    appendKeyDirectly()
                }
            } else {
                appendKey()
            }
            return super.beginCollection(descriptor, collectionSize)
        }

        private fun appendKey() {
            when {
                shouldInlineCurrentElement -> {
                    appendKeyDirectly()
                }
                valueDescriptor.isTable -> {
                    if (isStructured) {
                        writer.writeLineFeed()
                        writer.writeString("[$currentElementPath]")
                        writer.writeLineFeed()
                    }
                }
                else -> {
                    appendKeyDirectly()
                }
            }
        }

        private fun appendKeyDirectly() {
            writer.writeKey(if (isStructured) currentKey else currentElementPath)
        }

        // This shouldn't be called.
        override fun <T> beginElement(descriptor: SerialDescriptor, index: Int, value: T) {}

        // This shouldn't be called.
        override fun endElement(descriptor: SerialDescriptor, index: Int) {}
    }

    internal inner class ArrayOfTableEncoder(
        private val arraySize: Int,
        path: String
    ) : TableLikeEncoder(true, path) {
        init {
            currentElementPath = path
            shouldStructureCurrentElement = true
        }

        override fun <T> beginElement(descriptor: SerialDescriptor, index: Int, value: T) {
            if (value.isNullOrTomlNull) {
                throwNullInArrayOfTable()
            }
            writer.writeLineFeed()
            writer.writeString("[[$currentElementPath]]")
            writer.writeLineFeed()
        }

        override fun endElement(descriptor: SerialDescriptor, index: Int) {
            if (index != arraySize - 1) {
                writer.writeLineFeed()
            }
        }
    }
}

private val SerialDescriptor.isTable: Boolean
    get() {
        val kind = kind
        return kind == CLASS || kind == MAP
    }

private val SerialDescriptor.isCollection: Boolean
    get() {
        val kind = kind
        return kind == LIST || kind == MAP
    }

private val SerialDescriptor.isArrayOfTable: Boolean
    get() = kind == LIST && getElementDescriptor(0).isTable

private val SerialDescriptor.isTableLike: Boolean
    get() = isTable || isArrayOfTable

private val SerialDescriptor.isExactlyTomlTable: Boolean
    get() = serialName.removeSuffix("?") == TomlTable.serializer().descriptor.serialName

private val SerialDescriptor.shouldForceInline: Boolean
    get() = kind == CONTEXTUAL ||
            isExactlyTomlTable ||
            isInline

private val Any?.isNullOrTomlNull: Boolean
    get() = this == null || this == TomlNull

private fun calculateStructuredTableLikeIndex(descriptor: SerialDescriptor): Int {
    var structuredIndex = 0
    for (i in descriptor.elementsCount - 1 downTo 0) {
        val elementDescriptor = descriptor.getElementDescriptor(i)
        if (descriptor.shouldForceInlineAt(i) || elementDescriptor.isTableLike.not()) {
            structuredIndex = i + 1
            break
        }
    }
    return structuredIndex
}

private fun SerialDescriptor.shouldForceInlineAt(index: Int): Boolean {
    val elementDescriptor = getElementDescriptor(index)
    return getElementAnnotations(index).hasAnnotation<TomlInline>() || elementDescriptor.shouldForceInline
}

private inline fun <reified T> List<Annotation>.hasAnnotation(): Boolean {
    return filterIsInstance<T>().isNotEmpty()
}

private fun TomlWriter.writeKey(key: String) {
    writeString("$key = ")
}
