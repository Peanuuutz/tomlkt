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

package net.peanuuutz.tomlkt.internal.encoder

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind.CONTEXTUAL
import kotlinx.serialization.descriptors.StructureKind.CLASS
import kotlinx.serialization.descriptors.StructureKind.LIST
import kotlinx.serialization.descriptors.StructureKind.MAP
import kotlinx.serialization.descriptors.StructureKind.OBJECT
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.TomlArray
import net.peanuuutz.tomlkt.TomlBlockArray
import net.peanuuutz.tomlkt.TomlComment
import net.peanuuutz.tomlkt.TomlElement
import net.peanuuutz.tomlkt.TomlInline
import net.peanuuutz.tomlkt.TomlInteger
import net.peanuuutz.tomlkt.TomlInteger.Base
import net.peanuuutz.tomlkt.TomlInteger.Base.Dec
import net.peanuuutz.tomlkt.TomlLiteral
import net.peanuuutz.tomlkt.TomlLiteralString
import net.peanuuutz.tomlkt.TomlMultilineString
import net.peanuuutz.tomlkt.TomlNull
import net.peanuuutz.tomlkt.TomlTable
import net.peanuuutz.tomlkt.TomlWriter
import net.peanuuutz.tomlkt.internal.anyIsInstance
import net.peanuuutz.tomlkt.internal.doubleQuoted
import net.peanuuutz.tomlkt.internal.doubleQuotedIfNotPure
import net.peanuuutz.tomlkt.internal.escape
import net.peanuuutz.tomlkt.internal.firstIsInstanceOrNull
import net.peanuuutz.tomlkt.internal.isArrayOfTable
import net.peanuuutz.tomlkt.internal.isCollection
import net.peanuuutz.tomlkt.internal.isExactlyTomlTable
import net.peanuuutz.tomlkt.internal.isTable
import net.peanuuutz.tomlkt.internal.isTableLike
import net.peanuuutz.tomlkt.internal.isUnsignedInteger
import net.peanuuutz.tomlkt.internal.singleQuoted
import net.peanuuutz.tomlkt.internal.throwEmptyArrayOfTableInMap
import net.peanuuutz.tomlkt.internal.throwNullInArrayOfTable
import net.peanuuutz.tomlkt.internal.throwPolymorphicCollection
import net.peanuuutz.tomlkt.internal.throwUnsupportedSerialKind
import net.peanuuutz.tomlkt.toTomlKey

// -------- AbstractTomlFileEncoder --------

internal abstract class AbstractTomlFileEncoder(
    toml: Toml,
    val writer: TomlWriter
) : AbstractTomlEncoder(toml) {
    final override fun encodeBoolean(value: Boolean) {
        writer.writeBoolean(value)
    }

    final override fun encodeByte(value: Byte) {
        writer.writeByte(value)
    }

    final override fun encodeShort(value: Short) {
        writer.writeShort(value)
    }

    final override fun encodeInt(value: Int) {
        writer.writeInt(value)
    }

    final override fun encodeLong(value: Long) {
        writer.writeLong(value)
    }

    final override fun encodeFloat(value: Float) {
        writer.writeFloat(value)
    }

    final override fun encodeDouble(value: Double) {
        writer.writeDouble(value)
    }

    final override fun encodeChar(value: Char) {
        writer.writeString(value.escape().doubleQuoted)
    }

    final override fun encodeString(value: String) {
        writer.writeString(value.escape().doubleQuoted)
    }

    final override fun encodeNull() {
        writer.writeNull()
    }

    final override fun encodeTomlElement(value: TomlElement) {
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

    final override fun encodeInline(descriptor: SerialDescriptor): Encoder {
        return if (descriptor.isUnsignedInteger) {
            TomlFileInlineEncoder(this)
        } else {
            this
        }
    }

    final override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        encodeString(enumDescriptor.getElementName(index))
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        return beginStructurePolymorphically(
            descriptor = descriptor,
            forceInline = false
        )
    }

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        return beginCollectionPolymorphically(
            descriptor = descriptor,
            collectionSize = collectionSize,
            forceInline = false
        )
    }
}

// -------- TomlFileEncoder --------

internal class TomlFileEncoder(
    toml: Toml,
    writer: TomlWriter
) : AbstractTomlFileEncoder(toml, writer)

// -------- TomlFileInlineEncoder --------

private class TomlFileInlineEncoder(
    delegate: AbstractTomlFileEncoder
) : AbstractTomlInlineEncoder<AbstractTomlFileEncoder>(delegate) {
    override fun encodeByte(value: Byte) {
        delegate.writer.writeString(value.toUByte().toString())
    }

    override fun encodeShort(value: Short) {
        delegate.writer.writeString(value.toUShort().toString())
    }

    override fun encodeInt(value: Int) {
        delegate.writer.writeString(value.toUInt().toString())
    }

    override fun encodeLong(value: Long) {
        delegate.writer.writeString(value.toULong().toString())
    }
}

// -------- AbstractTomlFileCompositeEncoder --------

private abstract class AbstractTomlFileCompositeEncoder(
    delegate: AbstractTomlFileEncoder
) : AbstractTomlFileEncoder(delegate.toml, delegate.writer), TomlCompositeEncoder {
    private fun encodeByteWithBase(value: Byte, base: Base) {
        require(value >= 0.toByte() || base == Dec) {
            "Negative integer cannot be represented by other bases, but found $value"
        }
        writer.writeString(base.prefix)
        writer.writeString(value.toString(base.value))
    }

    private fun encodeShortWithBase(value: Short, base: Base) {
        require(value >= 0.toShort() || base == Dec) {
            "Negative integer cannot be represented by other bases, but found $value"
        }
        writer.writeString(base.prefix)
        writer.writeString(value.toString(base.value))
    }

    private fun encodeIntWithBase(value: Int, base: Base) {
        require(value >= 0 || base == Dec) {
            "Negative integer cannot be represented by other bases, but found $value"
        }
        writer.writeString(base.prefix)
        writer.writeString(value.toString(base.value))
    }

    private fun encodeLongWithBase(value: Long, base: Base) {
        require(value >= 0L || base == Dec) {
            "Negative integer cannot be represented by other bases, but found $value"
        }
        writer.writeString(base.prefix)
        writer.writeString(value.toString(base.value))
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

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        return beginStructurePolymorphically(
            descriptor = descriptor,
            forceInline = true
        )
    }

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        return beginCollectionPolymorphically(
            descriptor = descriptor,
            collectionSize = collectionSize,
            forceInline = true
        )
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
        encodeWithBase: (T, Base) -> Unit
    ) {
        encodeElement(descriptor, index) {
            val integerAnnotation = descriptor.getElementAnnotations(index)
                .firstIsInstanceOrNull<TomlInteger>()
            if (integerAnnotation == null) {
                encodeNormally(value)
            } else {
                encodeWithBase(value, integerAnnotation.base)
            }
        }
    }

    final override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        encodeElement(descriptor, index) {
            val annotations = descriptor.getElementAnnotations(index)
            val isLiteral = annotations.anyIsInstance<TomlLiteralString>()
            val isMultiline = annotations.anyIsInstance<TomlMultilineString>()
            when {
                !isLiteral && !isMultiline -> {
                    encodeString(value)
                }
                !isLiteral -> {
                    encodeMultilineString(value)
                }
                !isMultiline -> {
                    encodeLiteralString(value)
                }
                else -> {
                    encodeMultilineLiteralString(value)
                }
            }
        }
    }

    final override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
        return if (descriptor.getElementDescriptor(index).isUnsignedInteger) {
            TomlFileInlineElementEncoder(
                parentDescriptor = descriptor,
                elementIndex = index,
                delegate = this
            )
        } else {
            this
        }
    }
}

// -------- TomlFileInlineElementEncoder --------

private class TomlFileInlineElementEncoder(
    parentDescriptor: SerialDescriptor,
    elementIndex: Int,
    delegate: AbstractTomlFileCompositeEncoder
) : AbstractTomlInlineElementEncoder<AbstractTomlFileCompositeEncoder>(parentDescriptor, elementIndex, delegate) {
    override fun encodeByte(value: Byte) {
        delegate.encodeElement(parentDescriptor, elementIndex) {
            delegate.writer.writeString(value.toUByte().toString())
        }
    }

    override fun encodeShort(value: Short) {
        delegate.encodeElement(parentDescriptor, elementIndex) {
            delegate.writer.writeString(value.toUShort().toString())
        }
    }

    override fun encodeInt(value: Int) {
        delegate.encodeElement(parentDescriptor, elementIndex) {
            delegate.writer.writeString(value.toUInt().toString())
        }
    }

    override fun encodeLong(value: Long) {
        delegate.encodeElement(parentDescriptor, elementIndex) {
            delegate.writer.writeString(value.toULong().toString())
        }
    }
}

// -------- TomlFileFlowClassEncoder --------

private class TomlFileFlowClassEncoder(
    delegate: AbstractTomlFileEncoder
) : AbstractTomlFileCompositeEncoder(delegate) {
    init { writer.writeString("{ ") }

    override fun encodeDiscriminatorElement(discriminator: String, serialName: String, isEmptyStructure: Boolean) {
        writer.writeKey(discriminator)
        encodeString(serialName)
        if (!isEmptyStructure) {
            writer.writeString(", ")
        }
    }

    override fun beginElement(descriptor: SerialDescriptor, index: Int) {
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

// -------- TomlFileFlowMapEncoder --------

private class TomlFileFlowMapEncoder(
    delegate: AbstractTomlFileEncoder,
    private val mapSize: Int
) : AbstractTomlFileCompositeEncoder(delegate) {
    private var isKey: Boolean = true

    private lateinit var currentKey: String

    init { writer.writeString("{ ") }

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
        } else { // This element is value (of the entry).
            if (value.isNullLike.not() || toml.config.explicitNulls) {
                writer.writeKey(currentKey)
                encodeSerializableValue(serializer, value)
                if (index != mapSize * 2 - 1) {
                    writer.writeString(", ")
                }
            }
        }
        isKey = !isKey
    }

    override fun encodeDiscriminatorElement(discriminator: String, serialName: String, isEmptyStructure: Boolean) {
        throwPolymorphicCollection()
    }

    // This shouldn't be called.
    override fun beginElement(descriptor: SerialDescriptor, index: Int) {}

    // This shouldn't be called.
    override fun endElement(descriptor: SerialDescriptor, index: Int) {}

    override fun endStructure(descriptor: SerialDescriptor) {
        writer.writeString(" }")
    }
}

// -------- TomlFileFlowArrayEncoder --------

private class TomlFileFlowArrayEncoder(
    delegate: AbstractTomlFileEncoder,
    private val arraySize: Int
) : AbstractTomlFileCompositeEncoder(delegate) {
    init { writer.writeString("[ ") }

    override fun encodeDiscriminatorElement(discriminator: String, serialName: String, isEmptyStructure: Boolean) {
        throwPolymorphicCollection()
    }

    override fun beginElement(descriptor: SerialDescriptor, index: Int) {}

    override fun endElement(descriptor: SerialDescriptor, index: Int) {
        if (index != arraySize - 1) {
            writer.writeString(", ")
        }
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        writer.writeString(" ]")
    }
}

// -------- TomlFileBlockArrayEncoder --------

private class TomlFileBlockArrayEncoder(
    delegate: AbstractTomlFileEncoder,
    private val arraySize: Int,
    private val itemsPerLine: Int
) : AbstractTomlFileCompositeEncoder(delegate) {
    private var currentLineItemsCount: Int = 0

    init {
        writer.writeChar('[')
        writer.writeLineFeed()
    }

    override fun encodeDiscriminatorElement(discriminator: String, serialName: String, isEmptyStructure: Boolean) {
        throwPolymorphicCollection()
    }

    override fun beginElement(descriptor: SerialDescriptor, index: Int) {
        if (currentLineItemsCount == 0) {
            writer.writeString(toml.config.indentation.representation)
        } else {
            writer.writeChar(' ')
        }
    }

    override fun endElement(descriptor: SerialDescriptor, index: Int) {
        if (index != arraySize - 1) {
            writer.writeChar(',')
        }
        if (++currentLineItemsCount >= itemsPerLine) {
            writer.writeLineFeed()
            currentLineItemsCount = 0
        }
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        if (currentLineItemsCount != 0) {
            writer.writeLineFeed()
        }
        writer.writeChar(']')
    }
}

// -------- TomlFileTableLikeEncoder --------

private abstract class TomlFileTableLikeEncoder(
    delegate: AbstractTomlFileEncoder,
    val isStructured: Boolean,
    val path: String
) : AbstractTomlFileCompositeEncoder(delegate) {
    lateinit var currentElementKey: String

    val currentElementPath: String
        get() {
            val path = path
            val currentElementKey = currentElementKey
            return when {
                currentElementKey.isEmpty() -> path
                path.isEmpty() -> currentElementKey
                isStructured && !shouldStructureCurrentElement -> currentElementKey
                else -> "$path.$currentElementKey"
            }
        }

    var shouldInlineCurrentElement: Boolean = false

    var shouldStructureCurrentElement: Boolean = false

    final override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        val compositeEncoder = when (val kind = descriptor.kind) {
            CLASS, is PolymorphicKind, OBJECT -> {
                when {
                    shouldInlineCurrentElement -> {
                        TomlFileFlowClassEncoder(this)
                    }
                    shouldStructureCurrentElement -> {
                        TomlFileClassEncoder(
                            delegate = this,
                            isStructured = true,
                            path = currentElementPath,
                            structuredTableLikeIndex = calculateStructuredTableLikeIndex(descriptor)
                        )
                    }
                    descriptor.elementsCount == 0 -> {
                        writer.writeKey(currentElementPath)
                        TomlFileFlowClassEncoder(this)
                    }
                    else -> {
                        TomlFileClassEncoder(
                            delegate = this,
                            isStructured = false,
                            path = currentElementPath,
                            structuredTableLikeIndex = Int.MAX_VALUE
                        )
                    }
                }
            }
            else -> throwUnsupportedSerialKind(kind)
        }
        onBeginStructurePolymorphically(
            compositeEncoder = compositeEncoder,
            descriptor = descriptor,
            isEmptyStructure = descriptor.elementsCount == 0
        )
        return compositeEncoder
    }

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        return when (val kind = descriptor.kind) {
            LIST -> {
                when {
                    collectionSize == 0 -> {
                        TomlFileFlowArrayEncoder(this, 0)
                    }
                    shouldInlineCurrentElement -> {
                        TomlFileFlowArrayEncoder(this, collectionSize)
                    }
                    shouldStructureCurrentElement && blockArray == null && descriptor.isArrayOfTable -> {
                        TomlFileArrayOfTableEncoder(
                            delegate = this,
                            path = currentElementPath,
                            arraySize = collectionSize
                        )
                    }
                    else -> {
                        TomlFileBlockArrayEncoder(
                            delegate = this,
                            arraySize = collectionSize,
                            itemsPerLine = blockArray?.itemsPerLine ?: toml.config.itemsPerLineInBlockArray
                        )
                    }
                }
            }
            MAP -> {
                when {
                    shouldInlineCurrentElement -> {
                        TomlFileFlowMapEncoder(this, collectionSize)
                    }
                    shouldStructureCurrentElement -> {
                        TomlFileMapEncoder(
                            delegate = this,
                            isStructured = true,
                            path = currentElementPath,
                            mapSize = collectionSize,
                            valueDescriptor = descriptor.getElementDescriptor(1)
                        )
                    }
                    collectionSize == 0 -> {
                        writer.writeKey(currentElementPath)
                        TomlFileFlowMapEncoder(this, 0)
                    }
                    else -> {
                        TomlFileMapEncoder(
                            delegate = this,
                            isStructured = false,
                            path = currentElementPath,
                            mapSize = collectionSize,
                            valueDescriptor = descriptor.getElementDescriptor(1)
                        )
                    }
                }
            }
            else -> throwUnsupportedSerialKind(kind)
        }
    }

    protected open val blockArray: TomlBlockArray?
        get() = null

    final override fun endStructure(descriptor: SerialDescriptor) {}
}

// -------- TomlFileClassEncoder --------

private class TomlFileClassEncoder(
    delegate: AbstractTomlFileEncoder,
    isStructured: Boolean,
    path: String,
    private val structuredTableLikeIndex: Int
) : TomlFileTableLikeEncoder(delegate, isStructured, path) {
    private lateinit var currentElementDescriptor: SerialDescriptor

    override var blockArray: TomlBlockArray? = null

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) {
        if (value.isNullLike && toml.config.explicitNulls) {
            shouldInlineCurrentElement = true
        }
        super.encodeSerializableElement(descriptor, index, serializer, value)
    }

    override fun encodeDiscriminatorElement(discriminator: String, serialName: String, isEmptyStructure: Boolean) {
        currentElementKey = discriminator
        appendKeyDirectly()
        encodeString(serialName)
        if (!isEmptyStructure) {
            writer.writeLineFeed()
        }
    }

    override fun beginElement(descriptor: SerialDescriptor, index: Int) {
        currentElementDescriptor = descriptor.getElementDescriptor(index)
        currentElementKey = descriptor.getElementName(index)
            .escape()
            .doubleQuotedIfNotPure()
        shouldInlineCurrentElement = shouldInlineCurrentElement || descriptor.shouldForceInlineAt(index)
        shouldStructureCurrentElement = isStructured && index >= structuredTableLikeIndex
        processElementAnnotations(descriptor.getElementAnnotations(index))
        if (currentElementDescriptor.isCollection.not()) {
            appendKey()
        } // For non-null collections, defer to beginCollection().
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
                    blockArray = annotation
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
        writer.writeKey(if (isStructured) currentElementKey else currentElementPath)
    }

    override fun endElement(descriptor: SerialDescriptor, index: Int) {
        if (index != descriptor.elementsCount - 1) {
            writer.writeLineFeed()
        }
        shouldInlineCurrentElement = false
        blockArray = null
    }
}

// -------- TomlFileMapEncoder --------

private class TomlFileMapEncoder(
    delegate: AbstractTomlFileEncoder,
    isStructured: Boolean,
    path: String,
    private val mapSize: Int,
    private val valueDescriptor: SerialDescriptor
) : TomlFileTableLikeEncoder(delegate, isStructured, path) {
    private var currentElementIndex: Int = 0

    private val isKey: Boolean
        get() = currentElementIndex % 2 == 0

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
            currentElementKey = value.toTomlKey()
                .escape()
                .doubleQuotedIfNotPure()
        } else { // This element is value (of the entry).
            if (value.isNullLike.not() || toml.config.explicitNulls) {
                when {
                    value.isNullLike -> {
                        appendKeyDirectly()
                    }
                    valueDescriptor.isCollection.not() -> {
                        appendKey()
                    }
                    // For non-null collections, defer to beginCollection().
                }
                encodeSerializableValue(serializer, value)
                if (index != mapSize * 2 - 1) {
                    writer.writeLineFeed()
                }
            }
        }
        currentElementIndex++
    }

    override fun encodeDiscriminatorElement(discriminator: String, serialName: String, isEmptyStructure: Boolean) {
        throwPolymorphicCollection()
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
        writer.writeKey(if (isStructured) currentElementKey else currentElementPath)
    }

    // This shouldn't be called.
    override fun beginElement(descriptor: SerialDescriptor, index: Int) {}

    // This shouldn't be called.
    override fun endElement(descriptor: SerialDescriptor, index: Int) {}
}

// -------- TomlFileArrayOfTableEncoder --------

private class TomlFileArrayOfTableEncoder(
    delegate: AbstractTomlFileEncoder,
    path: String,
    private val arraySize: Int
) : TomlFileTableLikeEncoder(delegate, isStructured = true, path) {
    init {
        currentElementKey = ""
        shouldStructureCurrentElement = true
    }

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) {
        when {
            value.isNullLike.not() -> {
                super.encodeSerializableElement(descriptor, index, serializer, value)
            }
            toml.config.explicitNulls -> {
                throwNullInArrayOfTable(path)
            }
        }
    }

    override fun encodeDiscriminatorElement(discriminator: String, serialName: String, isEmptyStructure: Boolean) {
        throwPolymorphicCollection()
    }

    override fun beginElement(descriptor: SerialDescriptor, index: Int) {
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

// -------- Utils --------

private fun AbstractTomlFileEncoder.beginStructurePolymorphically(
    descriptor: SerialDescriptor,
    forceInline: Boolean
): CompositeEncoder {
    val compositeEncoder = when (val kind = descriptor.kind) {
        CLASS, is PolymorphicKind, OBJECT -> {
            if (forceInline) {
                TomlFileFlowClassEncoder(this)
            } else {
                TomlFileClassEncoder(
                    delegate = this,
                    isStructured = true,
                    path = "",
                    structuredTableLikeIndex = calculateStructuredTableLikeIndex(descriptor)
                )
            }
        }
        else -> throwUnsupportedSerialKind(kind)
    }
    onBeginStructurePolymorphically(
        compositeEncoder = compositeEncoder,
        descriptor = descriptor,
        isEmptyStructure = descriptor.elementsCount == 0
    )
    return compositeEncoder
}

// Currently polymorphic collection-like types are not supported.
private fun AbstractTomlFileEncoder.beginCollectionPolymorphically(
    descriptor: SerialDescriptor,
    collectionSize: Int,
    forceInline: Boolean
): CompositeEncoder {
    return when (val kind = descriptor.kind) {
        LIST -> {
            when {
                collectionSize == 0 -> {
                    TomlFileFlowArrayEncoder(this, 0)
                }
                forceInline -> {
                    TomlFileFlowArrayEncoder(this, collectionSize)
                }
                else -> {
                    TomlFileBlockArrayEncoder(
                        delegate = this,
                        arraySize = collectionSize,
                        itemsPerLine = toml.config.itemsPerLineInBlockArray
                    )
                }
            }
        }
        MAP -> {
            when {
                collectionSize == 0 -> {
                    TomlFileFlowMapEncoder(this, 0)
                }
                forceInline -> {
                    TomlFileFlowMapEncoder(this, collectionSize)
                }
                else -> {
                    TomlFileMapEncoder(
                        delegate = this,
                        isStructured = true,
                        path = "",
                        mapSize = collectionSize,
                        valueDescriptor = descriptor.getElementDescriptor(1)
                    )
                }
            }
        }
        else -> throwUnsupportedSerialKind(kind)
    }
}

private val SerialDescriptor.shouldForceInline: Boolean
    get() = kind == CONTEXTUAL || isExactlyTomlTable || isInline

private fun SerialDescriptor.shouldForceInlineAt(index: Int): Boolean {
    val elementDescriptor = getElementDescriptor(index)
    return getElementAnnotations(index).anyIsInstance<TomlInline>() || elementDescriptor.shouldForceInline
}

private fun calculateStructuredTableLikeIndex(descriptor: SerialDescriptor): Int {
    var structuredIndex = 0
    for (index in descriptor.elementsCount - 1 downTo 0) {
        val elementDescriptor = descriptor.getElementDescriptor(index)
        if (descriptor.shouldForceInlineAt(index) || elementDescriptor.isTableLike.not()) {
            structuredIndex = index + 1
            break
        }
    }
    return structuredIndex
}

private fun TomlWriter.writeKey(key: String) {
    writeString("$key = ")
}
